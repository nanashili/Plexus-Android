package com.plexus.wallpaper.crop;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Point;

import androidx.annotation.MainThread;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.plexus.core.utils.concurrent.PlexusExecutors;
import com.plexus.core.utils.logging.Log;
import com.plexus.imageeditor.model.EditorModel;
import com.plexus.model.account.User;
import com.plexus.utils.AsynchronousCallback;
import com.plexus.utils.BitmapUtil;
import com.plexus.utils.livedata.LiveDataUtil;
import com.plexus.wallpaper.ChatWallpaper;

import java.io.IOException;
import java.util.Objects;

final class WallpaperCropViewModel extends ViewModel {

  private static final String TAG = Log.tag(WallpaperCropViewModel.class);

  private final @NonNull WallpaperCropRepository  repository;
  private final @NonNull MutableLiveData<Boolean> blur;
  private @NonNull LiveData<User>      recipient;

  public WallpaperCropViewModel(@Nullable User recipientId,
                                @NonNull WallpaperCropRepository repository)
  {
    this.repository = repository;
    this.blur       = new MutableLiveData<>(false);
    /*this.recipient  = recipientId != null ? Recipient.live(recipientId).getLiveData() : LiveDataUtil.just(Recipient.UNKNOWN);*/
  }

  void render(@NonNull Context context,
              @NonNull EditorModel model,
              @NonNull Point size,
              @NonNull AsynchronousCallback.WorkerThread<ChatWallpaper, Error> callback)
  {
    PlexusExecutors.BOUNDED.execute(
            () -> {
              Bitmap bitmap = model.render(context, size);
              try {
                ChatWallpaper chatWallpaper = repository.setWallPaper(BitmapUtil.toWebPByteArray(bitmap));
                callback.onComplete(chatWallpaper);
              } catch (IOException e) {
                Log.w(TAG, e);
                callback.onError(Error.SAVING);
              } finally {
                bitmap.recycle();
              }
            });
  }

  LiveData<Boolean> getBlur() {
    return Transformations.distinctUntilChanged(blur);
  }

  LiveData<User> getRecipient() {
    return recipient;
  }

  @MainThread
  void setBlur(boolean blur) {
    this.blur.setValue(blur);
  }

  public static class Factory implements ViewModelProvider.Factory {

    private final User recipientId;

    public Factory(@Nullable User recipientId) {
      this.recipientId = recipientId;
    }

    @Override
    public @NonNull <T extends ViewModel> T create(@NonNull Class<T> modelClass) {

      WallpaperCropRepository wallpaperCropRepository = new WallpaperCropRepository(recipientId);

      return Objects.requireNonNull(modelClass.cast(new WallpaperCropViewModel(recipientId, wallpaperCropRepository)));
    }
  }

  enum Error {
    SAVING
  }
}
