package com.plexus.imageeditor.renderers;

import android.os.Parcel;

import androidx.annotation.NonNull;

import com.plexus.imageeditor.Bounds;
import com.plexus.imageeditor.Renderer;
import com.plexus.imageeditor.RendererContext;

/**
 * A rectangle that will be rendered on the blur mask layer. Intended for blurring faces.
 */
public final class FaceBlurRenderer implements Renderer {

    public static final Creator<FaceBlurRenderer> CREATOR = new Creator<FaceBlurRenderer>() {
        @Override
        public FaceBlurRenderer createFromParcel(Parcel in) {
            return new FaceBlurRenderer();
        }

        @Override
        public FaceBlurRenderer[] newArray(int size) {
            return new FaceBlurRenderer[size];
        }
    };

    @Override
    public void render(@NonNull RendererContext rendererContext) {
        rendererContext.canvas.drawRect(Bounds.FULL_BOUNDS, rendererContext.getMaskPaint());
    }

    @Override
    public boolean hitTest(float x, float y) {
        return Bounds.FULL_BOUNDS.contains(x, y);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
    }
}
