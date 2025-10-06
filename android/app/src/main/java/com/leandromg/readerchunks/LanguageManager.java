package com.leandromg.readerchunks;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class LanguageManager {

    private static final String PREF_NAME = "language_prefs";
    private static final String KEY_LANGUAGE = "selected_language";
    private static final String KEY_FIRST_LAUNCH = "is_first_launch";

    private final Context context;
    private final SharedPreferences prefs;

    public LanguageManager(Context context) {
        this.context = context;
        this.prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public static class Language {
        public final String code;
        public final String displayName;
        public final int flagResource;

        public Language(String code, String displayName, int flagResource) {
            this.code = code;
            this.displayName = displayName;
            this.flagResource = flagResource;
        }
    }

    public List<Language> getSupportedLanguages() {
        List<Language> languages = new ArrayList<>();
        languages.add(new Language("es", "Español", R.drawable.ic_flag_spain));
        languages.add(new Language("en", "English", R.drawable.ic_flag_uk));
        languages.add(new Language("pt", "Português", R.drawable.ic_flag_brazil));
        languages.add(new Language("fr", "Français", R.drawable.ic_flag_france));
        languages.add(new Language("de", "Deutsch", R.drawable.ic_flag_germany));
        return languages;
    }

    public String detectSystemLanguage() {
        String systemLang = Locale.getDefault().getLanguage();

        // Check if system language is supported
        for (Language lang : getSupportedLanguages()) {
            if (lang.code.equals(systemLang)) {
                return systemLang;
            }
        }

        // Default to Spanish if system language not supported
        return "es";
    }

    public String getCurrentLanguage() {
        return prefs.getString(KEY_LANGUAGE, detectSystemLanguage());
    }

    public void setLanguage(String languageCode) {
        prefs.edit().putString(KEY_LANGUAGE, languageCode).apply();
        applyLanguage(languageCode);

        // Set flag to recreate all activities
        prefs.edit().putBoolean("language_changed", true).apply();
    }

    public void applyStoredLanguage() {
        String savedLanguage = getCurrentLanguage();
        applyLanguage(savedLanguage);
    }

    private void applyLanguage(String languageCode) {
        Locale locale = new Locale(languageCode);
        Locale.setDefault(locale);

        Resources resources = context.getResources();
        Configuration config = resources.getConfiguration();
        config.setLocale(locale);
        resources.updateConfiguration(config, resources.getDisplayMetrics());
    }

    public Language getCurrentLanguageInfo() {
        String currentLangCode = getCurrentLanguage();
        for (Language lang : getSupportedLanguages()) {
            if (lang.code.equals(currentLangCode)) {
                return lang;
            }
        }
        // Fallback to Spanish
        return new Language("es", "Español", R.drawable.ic_flag_spain);
    }

    public boolean isFirstLaunch() {
        return prefs.getBoolean(KEY_FIRST_LAUNCH, true);
    }

    public void setFirstLaunchCompleted() {
        prefs.edit().putBoolean(KEY_FIRST_LAUNCH, false).apply();
    }

    public boolean shouldRecreateActivity() {
        return prefs.getBoolean("language_changed", false);
    }

    public void clearRecreateFlag() {
        prefs.edit().putBoolean("language_changed", false).apply();
    }
}