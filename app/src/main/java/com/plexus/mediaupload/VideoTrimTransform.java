package com.plexus.mediaupload;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.WorkerThread;

public final class VideoTrimTransform implements MediaTransform {

    private final MediaSendVideoFragment.Data data;

    VideoTrimTransform(@NonNull MediaSendVideoFragment.Data data) {
        this.data = data;
    }

    @WorkerThread
    @Override
    public @NonNull Media transform(@NonNull Context context, @NonNull Media media) {
        return new Media(media.getUri(),
                media.getMimeType(),
                media.getDate(),
                media.getWidth(),
                media.getHeight(),
                media.getSize(),
                media.getDuration(),
                media.isBorderless(),
                media.isVideoGif());
    }
}
