package com.plexus.keyvalue;

import androidx.annotation.NonNull;

import com.plexus.dependecies.PlexusDependencies;

import java.util.ArrayList;
import java.util.List;

public class PlexusStore {

    private static final PlexusStore INSTANCE = new PlexusStore();

    private final KeyValueStore store;
    private final MiscellaneousValues misc;
    private final EmojiValues emojiValues;
    private final SettingsValue settingsValue;
    private final PlexusPayValues plexusPayValues;
    private final TooltipValues tooltipValues;
    private final WallpaperValues wallpaperValues;

    private PlexusStore() {
        this.store = new KeyValueStore(PlexusDependencies.getApplication());
        this.misc = new MiscellaneousValues(store);
        this.emojiValues = new EmojiValues(store);
        this.settingsValue = new SettingsValue(store);
        this.plexusPayValues = new PlexusPayValues(store);
        this.tooltipValues = new TooltipValues(store);
        this.wallpaperValues = new WallpaperValues(store);
    }

    public static void onFirstEverAppLaunch() {
        misc().onFirstEverAppLaunch();
        emojiValues().onFirstEverAppLaunch();
        settingsValue().onFirstEverAppLaunch();
        plexusPayValues().onFirstEverAppLaunch();
        tooltipValues().onFirstEverAppLaunch();
        wallpaper().onFirstEverAppLaunch();
    }

    public static List<String> getKeysToIncludeInBackup() {
        List<String> keys = new ArrayList<>();
        keys.addAll(misc().getKeysToIncludeInBackup());
        keys.addAll(emojiValues().getKeysToIncludeInBackup());
        keys.addAll(settingsValue().getKeysToIncludeInBackup());
        keys.addAll(plexusPayValues().getKeysToIncludeInBackup());
        keys.addAll(tooltipValues().getKeysToIncludeInBackup());
        keys.addAll(wallpaper().getKeysToIncludeInBackup());
        return keys;
    }

    public static @NonNull
    MiscellaneousValues misc() {
        return INSTANCE.misc;
    }

    public static @NonNull
    EmojiValues emojiValues() {
        return INSTANCE.emojiValues;
    }

    public static  @NonNull SettingsValue settingsValue(){
        return INSTANCE.settingsValue;
    }

    public static @NonNull PlexusPayValues plexusPayValues(){
        return INSTANCE.plexusPayValues;
    }

    public static @NonNull TooltipValues tooltipValues(){
        return INSTANCE.tooltipValues;
    }

    public static @NonNull WallpaperValues wallpaper() {
        return INSTANCE.wallpaperValues;
    }


    /**
     * Ensures any pending writes are finished.
     */
    public static void blockUntilAllWritesFinished() {
        getStore().blockUntilAllWritesFinished();
    }

    private static @NonNull
    KeyValueStore getStore() {
        return INSTANCE.store;
    }

}
