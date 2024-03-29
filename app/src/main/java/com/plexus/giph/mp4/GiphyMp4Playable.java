package com.plexus.giph.mp4;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.exoplayer2.source.MediaSource;

public interface GiphyMp4Playable {
    /**
     * Shows the area in which a video would be projected. Called when a video will not
     * play back.
     */
    void showProjectionArea();

    /**
     * Hides the area in which a video would be projected. Called when a video is ready
     * to play back.
     */
    void hideProjectionArea();

    /**
     * @return The MediaSource to play back in the given VideoPlayer
     */
    default @Nullable MediaSource getMediaSource() {
        return null;
    }

    /**
     * A Playback policy enforcer, or null to loop forever.
     */
    default @Nullable GiphyMp4PlaybackPolicyEnforcer getPlaybackPolicyEnforcer() {
        return null;
    }

    /**
     * @return The position this item is in it's corresponding adapter
     */
    int getAdapterPosition();

    /**
     * Width, height, and (x,y) of view which video player will "project" into
     */
    @NonNull GiphyMp4Projection getProjection(@NonNull RecyclerView recyclerview);

    /**
     * Specifies whether the content can start playing.
     */
    boolean canPlayContent();
}
