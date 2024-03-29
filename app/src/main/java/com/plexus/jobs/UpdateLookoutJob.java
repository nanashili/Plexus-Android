package com.plexus.jobs;

import android.app.DownloadManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.plexus.BuildConfig;
import com.plexus.jobmanagers.BaseJob;
import com.plexus.jobmanagers.Data;
import com.plexus.jobmanagers.Job;
import com.plexus.jobmanagers.impl.NetworkConstraint;
import com.plexus.services.UpdateApkReadyListener;
import com.plexus.utils.FileUtils;
import com.plexus.utils.Hex;
import com.plexus.utils.JsonUtils;
import com.plexus.utils.PlexusPreferences;
import com.plexus.utils.logging.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.MessageDigest;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class UpdateLookoutJob extends BaseJob {

    public static final String KEY = "UpdateLookoutJob";

    private static final String TAG = UpdateLookoutJob.class.getSimpleName();

    public UpdateLookoutJob() {
        this(new Job.Parameters.Builder()
                .setQueue("UpdateLookoutJob")
                .addConstraint(NetworkConstraint.KEY)
                .setMaxAttempts(2)
                .build());
    }

    private UpdateLookoutJob(@NonNull Job.Parameters parameters) {
        super(parameters);
    }

    private static void clearPreviousDownloads(@NonNull Context context) {
        File directory = context.getExternalFilesDir(null);

        if (directory == null) {
            Log.w(TAG, "Failed to read external files directory.");
            return;
        }

        for (File file : directory.listFiles()) {
            if (file.getName().startsWith("lookout-update")) {
                if (file.delete()) {
                    Log.d(TAG, "Deleted " + file.getName());
                }
            }
        }
    }

    @Override
    public @NonNull
    Data serialize() {
        return Data.EMPTY;
    }

    @Override
    public @NonNull
    String getFactoryKey() {
        return KEY;
    }

    @Override
    public void onRun() throws IOException, PackageManager.NameNotFoundException {
        if (!BuildConfig.PLAY_STORE_DISABLED) return;

        Log.i(TAG, "Checking for APK update...");

        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder().url(String.format("%s/latest.json", BuildConfig.NOPLAY_UPDATE_URL)).build();

        Response response = client.newCall(request).execute();

        if (!response.isSuccessful()) {
            throw new IOException("Bad response: " + response.message());
        }

        UpdateDescriptor updateDescriptor = JsonUtils.fromJson(response.body().string(), UpdateDescriptor.class);
        byte[] digest = Hex.fromStringCondensed(updateDescriptor.getDigest());

        Log.i(TAG, "Got descriptor: " + updateDescriptor);

        if (updateDescriptor.getVersionCode() > getVersionCode()) {
            DownloadStatus downloadStatus = getDownloadStatus(updateDescriptor.getUrl(), digest);

            Log.i(TAG, "Download status: " + downloadStatus.getStatus());

            if (downloadStatus.getStatus() == DownloadStatus.Status.COMPLETE) {
                Log.i(TAG, "Download status complete, notifying...");
                handleDownloadNotify(downloadStatus.getDownloadId());
            } else if (downloadStatus.getStatus() == DownloadStatus.Status.MISSING) {
                Log.i(TAG, "Download status missing, starting download...");
                handleDownloadStart(updateDescriptor.getUrl(), updateDescriptor.getVersionName(), digest);
            }
        }
    }

    @Override
    public boolean onShouldRetry(@NonNull Exception e) {
        return e instanceof IOException;
    }

    @Override
    public void onFailure() {
        Log.w(TAG, "Update check failed");
    }

    private int getVersionCode() throws PackageManager.NameNotFoundException {
        PackageManager packageManager = context.getPackageManager();
        PackageInfo packageInfo = packageManager.getPackageInfo(context.getPackageName(), 0);

        return packageInfo.versionCode;
    }

    private DownloadStatus getDownloadStatus(String uri, byte[] theirDigest) {
        DownloadManager downloadManager = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
        DownloadManager.Query query = new DownloadManager.Query();

        query.setFilterByStatus(DownloadManager.STATUS_PAUSED | DownloadManager.STATUS_PENDING | DownloadManager.STATUS_RUNNING | DownloadManager.STATUS_SUCCESSFUL);

        long pendingDownloadId = PlexusPreferences.getUpdateApkDownloadId(context);
        byte[] pendingDigest = getPendingDigest(context);
        Cursor cursor = downloadManager.query(query);

        try {
            DownloadStatus status = new DownloadStatus(DownloadStatus.Status.MISSING, -1);

            while (cursor != null && cursor.moveToNext()) {
                int jobStatus = cursor.getInt(cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_STATUS));
                String jobRemoteUri = cursor.getString(cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_URI));
                long downloadId = cursor.getLong(cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_ID));
                byte[] digest = getDigestForDownloadId(downloadId);

                if (jobRemoteUri != null && jobRemoteUri.equals(uri) && downloadId == pendingDownloadId) {

                    if (jobStatus == DownloadManager.STATUS_SUCCESSFUL &&
                            digest != null && pendingDigest != null &&
                            MessageDigest.isEqual(pendingDigest, theirDigest) &&
                            MessageDigest.isEqual(digest, theirDigest)) {
                        return new DownloadStatus(DownloadStatus.Status.COMPLETE, downloadId);
                    } else if (jobStatus != DownloadManager.STATUS_SUCCESSFUL) {
                        status = new DownloadStatus(DownloadStatus.Status.PENDING, downloadId);
                    }
                }
            }

            return status;
        } finally {
            if (cursor != null) cursor.close();
        }
    }

    private void handleDownloadStart(String uri, String versionName, byte[] digest) {
        clearPreviousDownloads(context);

        DownloadManager downloadManager = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
        DownloadManager.Request downloadRequest = new DownloadManager.Request(Uri.parse(uri));

        downloadRequest.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI);
        downloadRequest.setTitle("Downloading LookOut update");
        downloadRequest.setDescription("Downloading LookOut " + versionName);
        downloadRequest.setVisibleInDownloadsUi(false);
        downloadRequest.setDestinationInExternalFilesDir(context, null, "lookout-update.apk");
        downloadRequest.setNotificationVisibility(DownloadManager.Request.VISIBILITY_HIDDEN);

        long downloadId = downloadManager.enqueue(downloadRequest);
        PlexusPreferences.setUpdateApkDownloadId(context, downloadId);
        PlexusPreferences.setUpdateApkDigest(context, Hex.toStringCondensed(digest));
    }

    private void handleDownloadNotify(long downloadId) {
        Intent intent = new Intent(DownloadManager.ACTION_DOWNLOAD_COMPLETE);
        intent.putExtra(DownloadManager.EXTRA_DOWNLOAD_ID, downloadId);

        new UpdateApkReadyListener().onReceive(context, intent);
    }

    private @Nullable
    byte[] getDigestForDownloadId(long downloadId) {
        try {
            DownloadManager downloadManager = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
            FileInputStream fin = new FileInputStream(downloadManager.openDownloadedFile(downloadId).getFileDescriptor());
            byte[] digest = FileUtils.getFileDigest(fin);

            fin.close();

            return digest;
        } catch (IOException e) {
            Log.w(TAG, e);
            return null;
        }
    }

    private @Nullable
    byte[] getPendingDigest(Context context) {
        try {
            String encodedDigest = PlexusPreferences.getUpdateApkDigest(context);

            if (encodedDigest == null) return null;

            return Hex.fromStringCondensed(encodedDigest);
        } catch (IOException e) {
            Log.w(TAG, e);
            return null;
        }
    }

    private static class UpdateDescriptor {
        @JsonProperty
        private int versionCode;

        @JsonProperty
        private String versionName;

        @JsonProperty
        private String url;

        @JsonProperty
        private String sha256sum;


        public int getVersionCode() {
            return versionCode;
        }

        public String getVersionName() {
            return versionName;
        }

        public String getUrl() {
            return url;
        }

        public @NonNull
        String toString() {
            return "[" + versionCode + ", " + versionName + ", " + url + "]";
        }

        public String getDigest() {
            return sha256sum;
        }
    }

    private static class DownloadStatus {
        private final Status status;
        private final long downloadId;
        DownloadStatus(Status status, long downloadId) {
            this.status = status;
            this.downloadId = downloadId;
        }

        public Status getStatus() {
            return status;
        }

        public long getDownloadId() {
            return downloadId;
        }

        enum Status {
            PENDING,
            COMPLETE,
            MISSING
        }
    }

    public static final class Factory implements Job.Factory<UpdateLookoutJob> {
        @Override
        public @NonNull
        UpdateLookoutJob create(@NonNull Parameters parameters, @NonNull Data data) {
            return new UpdateLookoutJob(parameters);
        }
    }
}
