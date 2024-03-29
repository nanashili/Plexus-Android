package com.plexus.keyvalue;

import androidx.annotation.NonNull;

import com.plexus.components.emoji.EmojiUtil;

import java.util.List;

public class EmojiValues extends PlexusStoreValues {

    private static final String PREFIX = "emojiPref__";

    EmojiValues(@NonNull KeyValueStore store) {
        super(store);
    }

    @Override
    void onFirstEverAppLaunch() {

    }

    @NonNull
    @Override
    List<String> getKeysToIncludeInBackup() {
        return null;
    }

    public void setPreferredVariation(@NonNull String emoji) {
        String canonical = EmojiUtil.getCanonicalRepresentation(emoji);

        if (canonical.equals(emoji)) {
            getStore().beginWrite().remove(PREFIX + canonical).apply();
        } else {
            putString(PREFIX + canonical, emoji);
        }
    }

    public @NonNull String getPreferredVariation(@NonNull String emoji) {
        String canonical = EmojiUtil.getCanonicalRepresentation(emoji);

        return getString(PREFIX + canonical, emoji);
    }
}
