package com.plexus.giph.ui;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.loader.content.Loader;

import com.plexus.giph.model.GiphyImage;
import com.plexus.giph.net.GiphyStickerLoader;

import java.util.List;

public class GiphyStickerFragment extends GiphyFragment {
    @Override
    public @NonNull
    Loader<List<GiphyImage>> onCreateLoader(int id, Bundle args) {
        return new GiphyStickerLoader(getActivity(), searchString);
    }

    @Override
    public void onLoaderReset(@NonNull Loader<List<GiphyImage>> loader) {

    }
}
