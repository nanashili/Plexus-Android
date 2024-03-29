package com.plexus.glide.apng;

import android.content.Context;

import com.plexus.glide.apng.decode.APNGDecoder;
import com.plexus.glide.common.FrameAnimationDrawable;
import com.plexus.glide.common.decode.FrameSeqDecoder;
import com.plexus.glide.common.loader.AssetStreamLoader;
import com.plexus.glide.common.loader.FileLoader;
import com.plexus.glide.common.loader.Loader;
import com.plexus.glide.common.loader.ResourceStreamLoader;

public class APNGDrawable extends FrameAnimationDrawable<APNGDecoder> {
    public APNGDrawable(Loader provider) {
        super(provider);
    }

    public APNGDrawable(APNGDecoder decoder) {
        super(decoder);
    }

    @Override
    protected APNGDecoder createFrameSeqDecoder(Loader streamLoader, FrameSeqDecoder.RenderListener listener) {
        return new APNGDecoder(streamLoader, listener);
    }


    public static APNGDrawable fromAsset(Context context, String assetPath) {
        AssetStreamLoader assetStreamLoader = new AssetStreamLoader(context, assetPath);
        return new APNGDrawable(assetStreamLoader);
    }

    public static APNGDrawable fromFile(String filePath) {
        FileLoader fileLoader = new FileLoader(filePath);
        return new APNGDrawable(fileLoader);
    }

    public static APNGDrawable fromResource(Context context, int resId) {
        ResourceStreamLoader resourceStreamLoader = new ResourceStreamLoader(context, resId);
        return new APNGDrawable(resourceStreamLoader);
    }
}
