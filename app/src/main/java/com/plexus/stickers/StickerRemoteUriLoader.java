package com.plexus.stickers;

import androidx.annotation.NonNull;

import com.bumptech.glide.load.Options;
import com.bumptech.glide.load.model.ModelLoader;
import com.bumptech.glide.load.model.ModelLoaderFactory;
import com.bumptech.glide.load.model.MultiModelLoaderFactory;
import com.plexus.dependecies.PlexusDependencies;

import java.io.InputStream;

/**
 * Glide loader to fetch a sticker remotely.
 */
public final class StickerRemoteUriLoader implements ModelLoader<StickerRemoteUri, InputStream> {

    /*private final SignalServiceMessageReceiver receiver;

    public StickerRemoteUriLoader(@NonNull SignalServiceMessageReceiver receiver) {
        this.receiver = receiver;
    }*/


    @Override
    public @NonNull LoadData<InputStream> buildLoadData(@NonNull StickerRemoteUri sticker, int width, int height, @NonNull Options options) {
        /*return new LoadData<>(sticker, new StickerRemoteUriFetcher(receiver, sticker));*/
        return null;
    }

    @Override
    public boolean handles(@NonNull StickerRemoteUri sticker) {
        return true;
    }

    public static class Factory implements ModelLoaderFactory<StickerRemoteUri, InputStream> {

        @Override
        public @NonNull ModelLoader<StickerRemoteUri, InputStream> build(@NonNull MultiModelLoaderFactory multiFactory) {
            /*return new StickerRemoteUriLoader(PlexusDependencies.getSignalServiceMessageReceiver());*/
            return null;
        }

        @Override
        public void teardown() {
        }
    }
}
