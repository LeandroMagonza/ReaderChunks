package com.leandromg.readerchunks;

import android.content.Context;
import android.content.SharedPreferences;

public class SettingsManager {
    private static final String PREFS_NAME = "BookBitsSettings";

    // Keys
    private static final String KEY_DARK_MODE = "dark_mode";
    private static final String KEY_FONT_SIZE = "font_size";
    private static final String KEY_LINE_SPACING = "line_spacing";
    private static final String KEY_PADDING_HORIZONTAL = "padding_horizontal";
    private static final String KEY_BIONIC_READING_MODE = "bionic_reading_mode";
    private static final String KEY_TTS_ENABLED = "tts_enabled";
    private static final String KEY_TTS_AUTO_SCROLL = "tts_auto_scroll";
    private static final String KEY_TTS_SPEECH_RATE = "tts_speech_rate";
    private static final String KEY_TTS_VOICE_NAME = "tts_voice_name";
    private static final String KEY_TTS_LANGUAGE_PREFIX = "tts_language_"; // + bookId

    // Default values
    private static final boolean DEFAULT_DARK_MODE = false;
    private static final int DEFAULT_FONT_SIZE = 24; // sp
    private static final int DEFAULT_LINE_SPACING = 4; // sp
    private static final int DEFAULT_PADDING_HORIZONTAL = 16; // dp
    private static final int DEFAULT_BIONIC_READING_MODE = 0; // 0=OFF, 1=CLASSIC, 2=MODERN
    private static final boolean DEFAULT_TTS_ENABLED = false;
    private static final boolean DEFAULT_TTS_AUTO_SCROLL = false;
    private static final float DEFAULT_TTS_SPEECH_RATE = 1.0f; // Normal speed
    private static final String DEFAULT_TTS_VOICE_NAME = ""; // Empty = system default

    // Ranges
    public static final int MIN_FONT_SIZE = 12;
    public static final int MAX_FONT_SIZE = 36;
    public static final int MIN_LINE_SPACING = 0;
    public static final int MAX_LINE_SPACING = 16;
    public static final int MIN_PADDING = 4;
    public static final int MAX_PADDING = 32;
    public static final int INCREMENT = 2;

    private final SharedPreferences prefs;

    public SettingsManager(Context context) {
        this.prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    // Dark Mode
    public boolean isDarkMode() {
        return prefs.getBoolean(KEY_DARK_MODE, DEFAULT_DARK_MODE);
    }

    public void setDarkMode(boolean darkMode) {
        prefs.edit().putBoolean(KEY_DARK_MODE, darkMode).apply();
    }

    // Font Size
    public int getFontSize() {
        return clamp(prefs.getInt(KEY_FONT_SIZE, DEFAULT_FONT_SIZE), MIN_FONT_SIZE, MAX_FONT_SIZE);
    }

    public void setFontSize(int fontSize) {
        int clampedSize = clamp(fontSize, MIN_FONT_SIZE, MAX_FONT_SIZE);
        prefs.edit().putInt(KEY_FONT_SIZE, clampedSize).apply();
    }

    // Line Spacing
    public int getLineSpacing() {
        return clamp(prefs.getInt(KEY_LINE_SPACING, DEFAULT_LINE_SPACING), MIN_LINE_SPACING, MAX_LINE_SPACING);
    }

    public void setLineSpacing(int lineSpacing) {
        int clampedSpacing = clamp(lineSpacing, MIN_LINE_SPACING, MAX_LINE_SPACING);
        prefs.edit().putInt(KEY_LINE_SPACING, clampedSpacing).apply();
    }

    // Padding Horizontal
    public int getPaddingHorizontal() {
        return clamp(prefs.getInt(KEY_PADDING_HORIZONTAL, DEFAULT_PADDING_HORIZONTAL), MIN_PADDING, MAX_PADDING);
    }

    public void setPaddingHorizontal(int padding) {
        int clampedPadding = clamp(padding, MIN_PADDING, MAX_PADDING);
        prefs.edit().putInt(KEY_PADDING_HORIZONTAL, clampedPadding).apply();
    }

    // Utility methods
    private int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }

    public int adjustFontSize(int delta) {
        int newSize = getFontSize() + delta;
        setFontSize(newSize);
        return getFontSize();
    }

    public int adjustLineSpacing(int delta) {
        int newSpacing = getLineSpacing() + delta;
        setLineSpacing(newSpacing);
        return getLineSpacing();
    }

    public int adjustPadding(int delta) {
        int newPadding = getPaddingHorizontal() + delta;
        setPaddingHorizontal(newPadding);
        return getPaddingHorizontal();
    }

    public boolean canIncreaseFontSize() {
        return getFontSize() < MAX_FONT_SIZE;
    }

    public boolean canDecreaseFontSize() {
        return getFontSize() > MIN_FONT_SIZE;
    }

    public boolean canIncreaseLineSpacing() {
        return getLineSpacing() < MAX_LINE_SPACING;
    }

    public boolean canDecreaseLineSpacing() {
        return getLineSpacing() > MIN_LINE_SPACING;
    }

    public boolean canIncreasePadding() {
        return getPaddingHorizontal() < MAX_PADDING;
    }

    public boolean canDecreasePadding() {
        return getPaddingHorizontal() > MIN_PADDING;
    }

    // Bionic Reading
    public BionicTextProcessor.BionicMode getBionicReadingMode() {
        int mode = prefs.getInt(KEY_BIONIC_READING_MODE, DEFAULT_BIONIC_READING_MODE);
        switch (mode) {
            case 1: return BionicTextProcessor.BionicMode.CLASSIC;
            case 2: return BionicTextProcessor.BionicMode.MODERN;
            default: return BionicTextProcessor.BionicMode.OFF;
        }
    }

    public void setBionicReadingMode(BionicTextProcessor.BionicMode mode) {
        int modeInt;
        switch (mode) {
            case CLASSIC: modeInt = 1; break;
            case MODERN: modeInt = 2; break;
            default: modeInt = 0; break; // OFF
        }
        prefs.edit().putInt(KEY_BIONIC_READING_MODE, modeInt).apply();
    }

    // Legacy method for backward compatibility
    public boolean isBionicReading() {
        return getBionicReadingMode() != BionicTextProcessor.BionicMode.OFF;
    }

    // Legacy method for backward compatibility
    public void setBionicReading(boolean bionicReading) {
        setBionicReadingMode(bionicReading ? BionicTextProcessor.BionicMode.CLASSIC : BionicTextProcessor.BionicMode.OFF);
    }

    // TTS Settings
    public boolean isTTSEnabled() {
        return prefs.getBoolean(KEY_TTS_ENABLED, DEFAULT_TTS_ENABLED);
    }

    public void setTTSEnabled(boolean enabled) {
        prefs.edit().putBoolean(KEY_TTS_ENABLED, enabled).apply();
    }

    public boolean isTTSAutoScrollEnabled() {
        return prefs.getBoolean(KEY_TTS_AUTO_SCROLL, DEFAULT_TTS_AUTO_SCROLL);
    }

    public void setTTSAutoScrollEnabled(boolean enabled) {
        prefs.edit().putBoolean(KEY_TTS_AUTO_SCROLL, enabled).apply();
    }

    public float getTTSSpeechRate() {
        return prefs.getFloat(KEY_TTS_SPEECH_RATE, DEFAULT_TTS_SPEECH_RATE);
    }

    public void setTTSSpeechRate(float rate) {
        // Clamp between 0.5 and 2.0
        float clampedRate = Math.max(0.5f, Math.min(2.0f, rate));
        prefs.edit().putFloat(KEY_TTS_SPEECH_RATE, clampedRate).apply();
    }

    public String getTTSVoiceName() {
        return prefs.getString(KEY_TTS_VOICE_NAME, DEFAULT_TTS_VOICE_NAME);
    }

    public void setTTSVoiceName(String voiceName) {
        prefs.edit().putString(KEY_TTS_VOICE_NAME, voiceName != null ? voiceName : "").apply();
    }

    // Per-book language settings
    public String getTTSLanguageForBook(String bookId) {
        return prefs.getString(KEY_TTS_LANGUAGE_PREFIX + bookId, ""); // Empty = auto-detect
    }

    public void setTTSLanguageForBook(String bookId, String languageCode) {
        prefs.edit().putString(KEY_TTS_LANGUAGE_PREFIX + bookId,
                              languageCode != null ? languageCode : "").apply();
    }

    // Reset to defaults
    public void resetToDefaults() {
        prefs.edit()
            .putBoolean(KEY_DARK_MODE, DEFAULT_DARK_MODE)
            .putInt(KEY_FONT_SIZE, DEFAULT_FONT_SIZE)
            .putInt(KEY_LINE_SPACING, DEFAULT_LINE_SPACING)
            .putInt(KEY_PADDING_HORIZONTAL, DEFAULT_PADDING_HORIZONTAL)
            .putInt(KEY_BIONIC_READING_MODE, DEFAULT_BIONIC_READING_MODE)
            .putBoolean(KEY_TTS_ENABLED, DEFAULT_TTS_ENABLED)
            .putBoolean(KEY_TTS_AUTO_SCROLL, DEFAULT_TTS_AUTO_SCROLL)
            .putFloat(KEY_TTS_SPEECH_RATE, DEFAULT_TTS_SPEECH_RATE)
            .putString(KEY_TTS_VOICE_NAME, DEFAULT_TTS_VOICE_NAME)
            .apply();
    }
}