package com.plexus.utils.dynamiclanguage;

import android.content.Context;
import android.content.res.Configuration;

import androidx.annotation.NonNull;

import com.plexus.utils.PlexusPreferences;

import java.util.Locale;

/**
 * Updates a context with an alternative language.
 */
public final class DynamicLanguageContextWrapper {
    private DynamicLanguageContextWrapper() {
    }

    public static void prepareOverrideConfiguration(@NonNull Context context, @NonNull Configuration base) {
        Locale newLocale = getUsersSelectedLocale(context);

        Locale.setDefault(newLocale);
        base.setLocale(newLocale);
    }

    public static @NonNull
    Locale getUsersSelectedLocale(@NonNull Context context) {
        String language = PlexusPreferences.getAppLanguage(context);
        return LocaleParser.findBestMatchingLocaleForLanguage(language);
    }

    public static void updateContext(@NonNull Context base) {
        Configuration config = base.getResources().getConfiguration();

        prepareOverrideConfiguration(base, config);
    }
}
