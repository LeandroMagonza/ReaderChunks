package com.leandromg.readerchunks;

import android.content.Context;
import android.content.SharedPreferences;
import androidx.appcompat.app.AppCompatDelegate;

public class ThemeManager {
    private static final String THEME_PREFS = "theme_preferences";
    private static final String THEME_KEY = "current_theme";
    private static final String DARK_THEME = "dark";
    private static final String LIGHT_THEME = "light";

    private SharedPreferences prefs;

    public ThemeManager(Context context) {
        prefs = context.getSharedPreferences(THEME_PREFS, Context.MODE_PRIVATE);
    }

    public void applyTheme() {
        String currentTheme = getCurrentTheme();
        if (DARK_THEME.equals(currentTheme)) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }
    }

    public void toggleTheme() {
        String currentTheme = getCurrentTheme();
        String newTheme = DARK_THEME.equals(currentTheme) ? LIGHT_THEME : DARK_THEME;
        setTheme(newTheme);
        applyTheme();
    }

    public boolean isDarkMode() {
        return DARK_THEME.equals(getCurrentTheme());
    }

    public String getCurrentTheme() {
        return prefs.getString(THEME_KEY, DARK_THEME); // Default to dark mode
    }

    private void setTheme(String theme) {
        prefs.edit().putString(THEME_KEY, theme).apply();
    }
}