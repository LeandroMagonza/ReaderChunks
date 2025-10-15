package com.leandromg.readerchunks;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.util.Log;

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
    private static final String KEY_TTS_PITCH = "tts_pitch";
    private static final String KEY_TTS_VOICE_NAME = "tts_voice_name";
    private static final String KEY_TTS_LANGUAGE_PREFIX = "tts_language_"; // + bookId
    private static final String KEY_SENTENCE_LENGTH_MULTIPLIER = "sentence_length_multiplier";

    // Navigation method keys
    private static final String KEY_NAV_BUTTONS = "nav_buttons";
    private static final String KEY_NAV_SWIPE_HORIZONTAL = "nav_swipe_horizontal";
    private static final String KEY_NAV_SWIPE_VERTICAL = "nav_swipe_vertical";
    private static final String KEY_NAV_TAP_HORIZONTAL = "nav_tap_horizontal";
    private static final String KEY_NAV_TAP_VERTICAL = "nav_tap_vertical";

    // Default values
    private static final boolean DEFAULT_DARK_MODE = false;
    private static final int DEFAULT_FONT_SIZE = 24; // sp
    private static final int DEFAULT_LINE_SPACING = 4; // sp
    private static final int DEFAULT_PADDING_HORIZONTAL = 16; // dp
    private static final int DEFAULT_BIONIC_READING_MODE = 0; // 0=OFF, 1=CLASSIC, 2=MODERN
    private static final boolean DEFAULT_TTS_ENABLED = false;
    private static final boolean DEFAULT_TTS_AUTO_SCROLL = false;
    private static final float DEFAULT_TTS_SPEECH_RATE = 1.0f; // Normal speed
    private static final float DEFAULT_TTS_PITCH = 1.0f; // Normal pitch
    private static final String DEFAULT_TTS_VOICE_NAME = ""; // Empty = system default
    private static final float DEFAULT_SENTENCE_LENGTH_MULTIPLIER = 1.0f; // 100%

    // Navigation method defaults (buttons, swipe horizontal, swipe vertical enabled by default)
    private static final boolean DEFAULT_NAV_BUTTONS = true;
    private static final boolean DEFAULT_NAV_SWIPE_HORIZONTAL = true;
    private static final boolean DEFAULT_NAV_SWIPE_VERTICAL = true;
    private static final boolean DEFAULT_NAV_TAP_HORIZONTAL = false;
    private static final boolean DEFAULT_NAV_TAP_VERTICAL = false;

    // Ranges
    public static final int MIN_FONT_SIZE = 12;
    public static final int MAX_FONT_SIZE = 60;
    public static final int MIN_LINE_SPACING = 0;
    public static final int MAX_LINE_SPACING = 60;
    public static final int MIN_PADDING = 4;
    public static final int MAX_PADDING = 32;
    public static final int INCREMENT = 2;

    // Sentence Length ranges
    public static final float MIN_SENTENCE_MULTIPLIER = 0.5f; // 50%
    public static final float MAX_SENTENCE_MULTIPLIER = 5.0f; // 500%
    public static final int BASE_SENTENCE_LENGTH = 150; // Base characters for 24sp
    public static final int BASE_FONT_SIZE = 24; // sp
    public static final int CHAR_ADJUSTMENT_PER_SP = 10; // characters per font size point

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
        // Clamp between 0.5 and 2.0 (Google TTS maximum effective speed)
        float clampedRate = Math.max(0.5f, Math.min(2.0f, rate));
        prefs.edit().putFloat(KEY_TTS_SPEECH_RATE, clampedRate).apply();
    }

    public float getTTSPitch() {
        return prefs.getFloat(KEY_TTS_PITCH, DEFAULT_TTS_PITCH);
    }

    public void setTTSPitch(float pitch) {
        // Clamp between 0.5 and 2.0 (Android TTS effective range)
        float clampedPitch = Math.max(0.5f, Math.min(2.0f, pitch));
        prefs.edit().putFloat(KEY_TTS_PITCH, clampedPitch).apply();
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

    // Sentence Length Multiplier
    public float getSentenceLengthMultiplier() {
        float multiplier = prefs.getFloat(KEY_SENTENCE_LENGTH_MULTIPLIER, DEFAULT_SENTENCE_LENGTH_MULTIPLIER);
        return Math.max(MIN_SENTENCE_MULTIPLIER, Math.min(MAX_SENTENCE_MULTIPLIER, multiplier));
    }

    public void setSentenceLengthMultiplier(float multiplier) {
        float clampedMultiplier = Math.max(MIN_SENTENCE_MULTIPLIER, Math.min(MAX_SENTENCE_MULTIPLIER, multiplier));
        prefs.edit().putFloat(KEY_SENTENCE_LENGTH_MULTIPLIER, clampedMultiplier).apply();
    }

    /**
     * Calculate optimal sentence length based on actual screen dimensions and font size
     * @param context Context needed to get display metrics
     * @return Optimal sentence length for current screen and settings
     */
    public int calculateOptimalSentenceLength(Context context) {
        if (context == null) {
            // Fallback to old method if no context available
            return getMaxSentenceLengthLegacy();
        }

        // Get display metrics
        DisplayMetrics metrics = context.getResources().getDisplayMetrics();
        int screenWidth = metrics.widthPixels;
        int screenHeight = metrics.heightPixels;

        // Calculate available space for text
        int availableHeight = calculateAvailableTextHeight(context, screenHeight);
        int availableWidth = calculateAvailableTextWidth(context, screenWidth);

        // Get current font settings
        int fontSize = getFontSize();
        float fontSizePx = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_SP,
            fontSize,
            metrics
        );

        // Calculate text dimensions
        float charWidth = fontSizePx * 0.55f; // More accurate average character width
        float lineHeight = fontSizePx * 1.3f; // Include line spacing

        // Calculate capacity
        int charsPerLine = Math.max(10, (int)(availableWidth / charWidth));
        int maxLines = Math.max(1, (int)(availableHeight / lineHeight));
        int screenCapacity = charsPerLine * maxLines;

        // Apply user multiplier but keep it reasonable
        float userMultiplier = getSentenceLengthMultiplier();
        int optimalLength = (int)(screenCapacity * userMultiplier);

        // Ensure reasonable bounds (min 30, max based on screen capacity)
        int minLength = 30;
        int maxLength = screenCapacity * 3; // Allow up to 3x screen capacity for very long sentences
        int finalLength = Math.max(minLength, Math.min(maxLength, optimalLength));

        // Debug logging
        DebugLogger.logDynamicCalc(screenWidth, screenHeight, availableWidth, availableHeight,
            fontSize, fontSizePx, charsPerLine, maxLines, screenCapacity, userMultiplier, finalLength);

        return finalLength;
    }

    /**
     * Calculate available height for text content
     */
    private int calculateAvailableTextHeight(Context context, int screenHeight) {
        DisplayMetrics metrics = context.getResources().getDisplayMetrics();

        // Estimate UI overhead (status bar, navigation, header, buttons)
        int statusBarHeight = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 24, metrics);
        int navigationBarHeight = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 48, metrics);
        int headerHeight = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 80, metrics);
        int bottomButtonsHeight = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 80, metrics);
        int ttsControlHeight = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 56, metrics);
        int verticalPadding = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 32, metrics);

        int totalOverhead = statusBarHeight + navigationBarHeight + headerHeight +
                           bottomButtonsHeight + ttsControlHeight + verticalPadding;

        return Math.max(200, screenHeight - totalOverhead);
    }

    /**
     * Calculate available width for text content
     */
    private int calculateAvailableTextWidth(Context context, int screenWidth) {
        DisplayMetrics metrics = context.getResources().getDisplayMetrics();

        // Get horizontal padding
        int horizontalPadding = (int) TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            getPaddingHorizontal() * 2, // Both sides
            metrics
        );

        // Add some margin for text padding
        int textPadding = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 32, metrics);

        return Math.max(200, screenWidth - horizontalPadding - textPadding);
    }

    /**
     * Legacy calculation method (fallback)
     */
    private int getMaxSentenceLengthLegacy() {
        int fontSize = getFontSize();
        float userMultiplier = getSentenceLengthMultiplier();

        // Auto-adjusted length based on font size
        int autoAdjusted = BASE_SENTENCE_LENGTH - (fontSize - BASE_FONT_SIZE) * CHAR_ADJUSTMENT_PER_SP;

        // Apply user multiplier
        int finalLength = (int)(autoAdjusted * userMultiplier);

        // Ensure minimum of 30 characters
        return Math.max(30, finalLength);
    }

    /**
     * Main method to get maximum sentence length - now uses dynamic calculation
     */
    public int getMaxSentenceLength() {
        // This will be updated to use context when called from Activity
        return getMaxSentenceLengthLegacy();
    }

    /**
     * Get maximum sentence length with context for dynamic calculation
     */
    public int getMaxSentenceLength(Context context) {
        return calculateOptimalSentenceLength(context);
    }

    // Navigation Methods
    public boolean isNavigationButtonsEnabled() {
        return prefs.getBoolean(KEY_NAV_BUTTONS, DEFAULT_NAV_BUTTONS);
    }

    public void setNavigationButtonsEnabled(boolean enabled) {
        prefs.edit().putBoolean(KEY_NAV_BUTTONS, enabled).apply();
    }

    public boolean isNavigationSwipeHorizontalEnabled() {
        return prefs.getBoolean(KEY_NAV_SWIPE_HORIZONTAL, DEFAULT_NAV_SWIPE_HORIZONTAL);
    }

    public void setNavigationSwipeHorizontalEnabled(boolean enabled) {
        prefs.edit().putBoolean(KEY_NAV_SWIPE_HORIZONTAL, enabled).apply();
    }

    public boolean isNavigationSwipeVerticalEnabled() {
        return prefs.getBoolean(KEY_NAV_SWIPE_VERTICAL, DEFAULT_NAV_SWIPE_VERTICAL);
    }

    public void setNavigationSwipeVerticalEnabled(boolean enabled) {
        prefs.edit().putBoolean(KEY_NAV_SWIPE_VERTICAL, enabled).apply();
    }

    public boolean isNavigationTapHorizontalEnabled() {
        return prefs.getBoolean(KEY_NAV_TAP_HORIZONTAL, DEFAULT_NAV_TAP_HORIZONTAL);
    }

    public void setNavigationTapHorizontalEnabled(boolean enabled) {
        prefs.edit().putBoolean(KEY_NAV_TAP_HORIZONTAL, enabled).apply();
    }

    public boolean isNavigationTapVerticalEnabled() {
        return prefs.getBoolean(KEY_NAV_TAP_VERTICAL, DEFAULT_NAV_TAP_VERTICAL);
    }

    public void setNavigationTapVerticalEnabled(boolean enabled) {
        prefs.edit().putBoolean(KEY_NAV_TAP_VERTICAL, enabled).apply();
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
            .putFloat(KEY_SENTENCE_LENGTH_MULTIPLIER, DEFAULT_SENTENCE_LENGTH_MULTIPLIER)
            .putBoolean(KEY_NAV_BUTTONS, DEFAULT_NAV_BUTTONS)
            .putBoolean(KEY_NAV_SWIPE_HORIZONTAL, DEFAULT_NAV_SWIPE_HORIZONTAL)
            .putBoolean(KEY_NAV_SWIPE_VERTICAL, DEFAULT_NAV_SWIPE_VERTICAL)
            .putBoolean(KEY_NAV_TAP_HORIZONTAL, DEFAULT_NAV_TAP_HORIZONTAL)
            .putBoolean(KEY_NAV_TAP_VERTICAL, DEFAULT_NAV_TAP_VERTICAL)
            .apply();
    }
}