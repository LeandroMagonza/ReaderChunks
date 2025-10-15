package com.leandromg.readerchunks;

import android.content.Context;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.SwitchCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.List;

public class SettingsDialogManager {

    public enum SettingsCategory {
        MAIN_LIST,
        LANGUAGE,
        FONT,
        VOICE,
        READING_MODE,
        DEBUG_LOGS
    }

    private Context context;
    private SettingsManager settingsManager;
    private LanguageManager languageManager;
    private AlertDialog mainDialog;
    private View currentView;
    private SettingsCategory currentCategory = SettingsCategory.MAIN_LIST;
    private boolean isNavigating = false; // Flag to prevent premature callbacks during navigation
    private boolean hasActualChanges = false; // Flag to track if any actual settings were changed

    // Reading mode state
    private boolean isFullParagraphMode = false;
    private ReadingModeChangeListener readingModeChangeListener;

    // Callback for settings changes (optional)
    public interface SettingsChangeListener {
        void onSettingsChanged();
    }

    // Callback for reading mode changes
    public interface ReadingModeChangeListener {
        void onReadingModeChanged(boolean isFullParagraphMode);
    }

    // Callback for debug logs
    public interface DebugLogsListener {
        void onDebugLogsRequested();
    }

    private SettingsChangeListener changeListener;
    private DebugLogsListener debugLogsListener;

    public SettingsDialogManager(Context context, SettingsManager settingsManager, LanguageManager languageManager) {
        this.context = context;
        this.settingsManager = settingsManager;
        this.languageManager = languageManager;
    }

    public void setSettingsChangeListener(SettingsChangeListener listener) {
        this.changeListener = listener;
    }

    public void setReadingModeChangeListener(ReadingModeChangeListener listener) {
        this.readingModeChangeListener = listener;
    }

    public void setCurrentReadingMode(boolean isFullParagraphMode) {
        this.isFullParagraphMode = isFullParagraphMode;
    }

    public void setDebugLogsListener(DebugLogsListener listener) {
        this.debugLogsListener = listener;
    }

    public void show() {
        hasActualChanges = false; // Reset changes flag when opening settings
        showCategoryList();
    }

    private void showCategoryList() {
        currentCategory = SettingsCategory.MAIN_LIST;
        View dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_settings_main, null);

        ImageButton btnClose = dialogView.findViewById(R.id.btnClose);
        btnClose.setOnClickListener(v -> closeDialog());

        RecyclerView recyclerCategories = dialogView.findViewById(R.id.recyclerCategories);
        recyclerCategories.setLayoutManager(new LinearLayoutManager(context));

        List<CategoryItem> categories = createCategoryItems();
        CategoryAdapter adapter = new CategoryAdapter(categories, this::onCategoryClick);
        recyclerCategories.setAdapter(adapter);

        showDialog(dialogView, context.getString(R.string.settings));
    }

    private List<CategoryItem> createCategoryItems() {
        List<CategoryItem> categories = new ArrayList<>();

        // Language category
        LanguageManager.Language currentLang = languageManager.getCurrentLanguageInfo();
        categories.add(new CategoryItem(
            SettingsCategory.LANGUAGE,
            "🌐",
            context.getString(R.string.language),
            currentLang.displayName,
            currentLang.flagResource
        ));

        // Font category
        int fontSize = settingsManager.getFontSize();
        String fontSubtitle = fontSize + "sp, " +
            (settingsManager.getBionicReadingMode() != BionicTextProcessor.BionicMode.OFF ?
                context.getString(R.string.bionic_reading) :
                context.getString(R.string.normal_text));
        categories.add(new CategoryItem(
            SettingsCategory.FONT,
            "Aa",
            context.getString(R.string.font_settings),
            fontSubtitle,
            0
        ));

        // Voice category
        boolean ttsEnabled = settingsManager.isTTSEnabled();
        float speed = settingsManager.getTTSSpeechRate();
        String voiceSubtitle = (ttsEnabled ? context.getString(R.string.enabled) : context.getString(R.string.disabled)) +
                             ", " + String.format("%.1fx", speed);
        categories.add(new CategoryItem(
            SettingsCategory.VOICE,
            "🔊",
            context.getString(R.string.voice_settings),
            voiceSubtitle,
            0
        ));

        // Reading mode category
        String readingModeSubtitle = isFullParagraphMode ?
            context.getString(R.string.reading_mode_paragraph) :
            context.getString(R.string.reading_mode_sentence);
        categories.add(new CategoryItem(
            SettingsCategory.READING_MODE,
            "📖",
            context.getString(R.string.reading_mode),
            readingModeSubtitle,
            0
        ));

        // Debug logs category
        categories.add(new CategoryItem(
            SettingsCategory.DEBUG_LOGS,
            "🐛",
            context.getString(R.string.debug_logs),
            context.getString(R.string.debug_logs_description),
            0
        ));

        return categories;
    }

    private void onCategoryClick(SettingsCategory category) {
        switch (category) {
            case LANGUAGE:
                showLanguageSettings();
                break;
            case FONT:
                showFontSettings();
                break;
            case VOICE:
                showVoiceSettings();
                break;
            case READING_MODE:
                showReadingModeSettings();
                break;
            case DEBUG_LOGS:
                showDebugLogs();
                break;
        }
    }

    private void showLanguageSettings() {
        currentCategory = SettingsCategory.LANGUAGE;
        View dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_settings_language, null);

        ImageButton btnBack = dialogView.findViewById(R.id.btnBack);
        RecyclerView recyclerLanguages = dialogView.findViewById(R.id.recyclerLanguages);

        btnBack.setOnClickListener(v -> showCategoryList());

        // Setup language list
        recyclerLanguages.setLayoutManager(new LinearLayoutManager(context));
        List<LanguageManager.Language> languages = languageManager.getSupportedLanguages();
        String currentLangCode = languageManager.getCurrentLanguage();

        LanguageAdapter languageAdapter = new LanguageAdapter(languages, currentLangCode, (language) -> {
            languageManager.setLanguage(language.code);
            hasActualChanges = true; // Mark that an actual change was made
            Toast.makeText(context, context.getString(R.string.language_changed), Toast.LENGTH_SHORT).show();
            // Note: Changes will be applied when dialog closes
            showCategoryList(); // Return to main list with updated info
        });
        recyclerLanguages.setAdapter(languageAdapter);

        showDialog(dialogView, context.getString(R.string.language));
    }

    private void showFontSettings() {
        currentCategory = SettingsCategory.FONT;
        View dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_settings_font, null);

        setupFontControls(dialogView);
        showDialog(dialogView, context.getString(R.string.font_settings));
    }

    private void setupFontControls(View dialogView) {
        ImageButton btnBack = dialogView.findViewById(R.id.btnBack);

        // Font size controls
        ImageButton btnDecreaseFontSize = dialogView.findViewById(R.id.btnDecreaseFontSize);
        ImageButton btnIncreaseFontSize = dialogView.findViewById(R.id.btnIncreaseFontSize);
        TextView tvFontSize = dialogView.findViewById(R.id.tvFontSize);

        // Line spacing controls
        ImageButton btnDecreaseLineSpacing = dialogView.findViewById(R.id.btnDecreaseLineSpacing);
        ImageButton btnIncreaseLineSpacing = dialogView.findViewById(R.id.btnIncreaseLineSpacing);
        TextView tvLineSpacing = dialogView.findViewById(R.id.tvLineSpacing);

        // Padding controls
        ImageButton btnDecreasePadding = dialogView.findViewById(R.id.btnDecreasePadding);
        ImageButton btnIncreasePadding = dialogView.findViewById(R.id.btnIncreasePadding);
        TextView tvPadding = dialogView.findViewById(R.id.tvPadding);

        // Sentence length controls
        SeekBar seekBarSentenceLength = dialogView.findViewById(R.id.seekBarSentenceLength);
        TextView tvSentenceLengthValue = dialogView.findViewById(R.id.tvSentenceLengthValue);

        // Bionic reading controls
        RadioGroup radioGroupBionic = dialogView.findViewById(R.id.radioGroupBionic);
        RadioButton radioBionicOff = dialogView.findViewById(R.id.radioBionicOff);
        RadioButton radioBionicClassic = dialogView.findViewById(R.id.radioBionicClassic);
        RadioButton radioBionicModern = dialogView.findViewById(R.id.radioBionicModern);

        // Preview
        TextView tvPreview = dialogView.findViewById(R.id.tvPreview);
        LinearLayout previewContainer = dialogView.findViewById(R.id.previewContainer);

        btnBack.setOnClickListener(v -> showCategoryList());

        // Helper to update all UI elements
        Runnable updateUI = () -> {
            int fontSize = settingsManager.getFontSize();
            int lineSpacing = settingsManager.getLineSpacing();
            int padding = settingsManager.getPaddingHorizontal();
            float sentenceMultiplier = settingsManager.getSentenceLengthMultiplier();
            int maxSentenceLength = settingsManager.getMaxSentenceLength(context);
            BionicTextProcessor.BionicMode bionicMode = settingsManager.getBionicReadingMode();

            tvFontSize.setText(fontSize + " sp");
            tvLineSpacing.setText(lineSpacing + " sp");
            tvPadding.setText(padding + " dp");

            int percentageValue = Math.round(sentenceMultiplier * 100);
            tvSentenceLengthValue.setText(percentageValue + "% (" + maxSentenceLength + " caracteres)");

            int seekBarProgress = (int)((sentenceMultiplier - 0.5f) * 100 / 4.5f);
            seekBarSentenceLength.setProgress(seekBarProgress);

            // Update bionic radio buttons
            switch (bionicMode) {
                case CLASSIC:
                    radioBionicClassic.setChecked(true);
                    break;
                case MODERN:
                    radioBionicModern.setChecked(true);
                    break;
                default:
                    radioBionicOff.setChecked(true);
                    break;
            }

            // Update preview
            updatePreview(tvPreview, previewContainer, fontSize, lineSpacing, padding, maxSentenceLength, bionicMode);

        };

        // Set initial values
        updateUI.run();

        // Set up listeners
        btnDecreaseFontSize.setOnClickListener(v -> {
            settingsManager.adjustFontSize(-SettingsManager.INCREMENT);
            hasActualChanges = true; // Mark that an actual change was made
            updateUI.run();
        });

        btnIncreaseFontSize.setOnClickListener(v -> {
            settingsManager.adjustFontSize(SettingsManager.INCREMENT);
            hasActualChanges = true; // Mark that an actual change was made
            updateUI.run();
        });

        btnDecreaseLineSpacing.setOnClickListener(v -> {
            settingsManager.adjustLineSpacing(-SettingsManager.INCREMENT);
            hasActualChanges = true; // Mark that an actual change was made
            updateUI.run();
        });

        btnIncreaseLineSpacing.setOnClickListener(v -> {
            settingsManager.adjustLineSpacing(SettingsManager.INCREMENT);
            hasActualChanges = true; // Mark that an actual change was made
            updateUI.run();
        });

        btnDecreasePadding.setOnClickListener(v -> {
            settingsManager.adjustPadding(-SettingsManager.INCREMENT);
            hasActualChanges = true; // Mark that an actual change was made
            updateUI.run();
        });

        btnIncreasePadding.setOnClickListener(v -> {
            settingsManager.adjustPadding(SettingsManager.INCREMENT);
            hasActualChanges = true; // Mark that an actual change was made
            updateUI.run();
        });

        seekBarSentenceLength.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    float multiplier = 0.5f + (progress * 4.5f / 100.0f);
                    settingsManager.setSentenceLengthMultiplier(multiplier);
                    hasActualChanges = true; // Mark that an actual change was made
                    updateUI.run();
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        radioGroupBionic.setOnCheckedChangeListener((group, checkedId) -> {
            BionicTextProcessor.BionicMode mode;
            if (checkedId == R.id.radioBionicClassic) {
                mode = BionicTextProcessor.BionicMode.CLASSIC;
            } else if (checkedId == R.id.radioBionicModern) {
                mode = BionicTextProcessor.BionicMode.MODERN;
            } else {
                mode = BionicTextProcessor.BionicMode.OFF;
            }
            settingsManager.setBionicReadingMode(mode);
            hasActualChanges = true; // Mark that an actual change was made
            updateUI.run();
        });
    }

    private void updatePreview(TextView tvPreview, LinearLayout previewContainer,
                             int fontSize, int lineSpacing, int padding,
                             int maxSentenceLength, BionicTextProcessor.BionicMode bionicMode) {
        tvPreview.setTextSize(TypedValue.COMPLEX_UNIT_SP, fontSize);
        tvPreview.setLineSpacing(lineSpacing, 1.0f);

        int paddingPx = (int) TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            padding,
            context.getResources().getDisplayMetrics()
        );
        previewContainer.setPadding(paddingPx, previewContainer.getPaddingTop(),
                                  paddingPx, previewContainer.getPaddingBottom());

        String previewText = context.getString(R.string.preview_text);

        // Calculate how much text fits in the available height
        String truncatedText = calculateTextForHeight(previewText, tvPreview, fontSize, lineSpacing);

        // Ensure it doesn't exceed maxSentenceLength either
        if (truncatedText.length() > maxSentenceLength) {
            truncatedText = truncatedText.substring(0, maxSentenceLength - 3) + "...";
        }

        previewText = truncatedText;

        if (bionicMode != BionicTextProcessor.BionicMode.OFF) {
            tvPreview.setText(BionicTextProcessor.process(previewText, bionicMode), TextView.BufferType.SPANNABLE);
        } else {
            tvPreview.setText(previewText);
        }
    }

    private String calculateTextForHeight(String fullText, TextView tvPreview, int fontSize, int lineSpacing) {
        // Get the height of the preview container (should be 50% of 700dp = 350dp)
        // Subtract padding to get available text height
        int containerHeightPx = (int) TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            320, // Approximately 350dp minus some padding for the "Preview" label
            context.getResources().getDisplayMetrics()
        );

        // Calculate approximate line height
        float fontSizePx = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_SP,
            fontSize,
            context.getResources().getDisplayMetrics()
        );
        float lineSpacingPx = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_SP,
            lineSpacing,
            context.getResources().getDisplayMetrics()
        );

        float lineHeight = fontSizePx + lineSpacingPx;
        int maxLines = (int) (containerHeightPx / lineHeight);

        if (maxLines <= 0) {
            maxLines = 1; // At least one line
        }

        // Get the width of the TextView to calculate characters per line
        int textWidthPx = (int) TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            280, // Approximate width after padding
            context.getResources().getDisplayMetrics()
        );

        // Estimate characters per line (this is approximate)
        float charWidth = fontSizePx * 0.6f; // Rough estimate for average character width
        int charsPerLine = (int) (textWidthPx / charWidth);

        if (charsPerLine <= 0) {
            charsPerLine = 20; // Minimum
        }

        int maxChars = maxLines * charsPerLine;

        if (fullText.length() <= maxChars) {
            return fullText;
        }

        // Truncate at word boundary when possible
        String truncated = fullText.substring(0, Math.min(maxChars - 3, fullText.length()));
        int lastSpace = truncated.lastIndexOf(' ');
        if (lastSpace > truncated.length() / 2) {
            truncated = truncated.substring(0, lastSpace);
        }

        return truncated + "...";
    }

    public void showVoiceSettings() {
        hasActualChanges = false; // Reset changes flag when opening voice settings
        currentCategory = SettingsCategory.VOICE;
        View dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_settings_voice, null);

        ImageButton btnBack = dialogView.findViewById(R.id.btnBack);
        SwitchCompat switchTTSEnabled = dialogView.findViewById(R.id.switchTTSEnabled);
        SwitchCompat switchAutoScroll = dialogView.findViewById(R.id.switchAutoScroll);
        ImageButton btnTTSSpeedDown = dialogView.findViewById(R.id.btnTTSSpeedDown);
        ImageButton btnTTSSpeedUp = dialogView.findViewById(R.id.btnTTSSpeedUp);
        TextView tvTTSSpeed = dialogView.findViewById(R.id.tvTTSSpeed);
        TextView tvSpeedValue = dialogView.findViewById(R.id.tvSpeedValue);
        ImageButton btnTTSPitchDown = dialogView.findViewById(R.id.btnTTSPitchDown);
        ImageButton btnTTSPitchUp = dialogView.findViewById(R.id.btnTTSPitchUp);
        TextView tvTTSPitch = dialogView.findViewById(R.id.tvTTSPitch);
        TextView tvPitchValue = dialogView.findViewById(R.id.tvPitchValue);

        btnBack.setOnClickListener(v -> showCategoryList());

        // Set current values
        switchTTSEnabled.setChecked(settingsManager.isTTSEnabled());
        switchAutoScroll.setChecked(settingsManager.isTTSAutoScrollEnabled());

        float currentSpeed = settingsManager.getTTSSpeechRate();
        updateTTSSpeedDisplay(tvTTSSpeed, tvSpeedValue, currentSpeed);

        float currentPitch = settingsManager.getTTSPitch();
        updateTTSPitchDisplay(tvTTSPitch, tvPitchValue, currentPitch);

        // Set up listeners
        switchTTSEnabled.setOnCheckedChangeListener((buttonView, isChecked) -> {
            settingsManager.setTTSEnabled(isChecked);
            hasActualChanges = true; // Mark that an actual change was made
            // Immediately notify of settings change when TTS is enabled/disabled (needed for button visibility)
            if (changeListener != null) {
                changeListener.onSettingsChanged();
            }
        });

        switchAutoScroll.setOnCheckedChangeListener((buttonView, isChecked) -> {
            settingsManager.setTTSAutoScrollEnabled(isChecked);
            hasActualChanges = true; // Mark that an actual change was made
        });

        btnTTSSpeedDown.setOnClickListener(v -> {
            float speed = settingsManager.getTTSSpeechRate();
            if (speed > 0.5f) {
                float newSpeed = Math.max(0.5f, speed - 0.1f);
                settingsManager.setTTSSpeechRate(newSpeed);
                updateTTSSpeedDisplay(tvTTSSpeed, tvSpeedValue, newSpeed);
                hasActualChanges = true;
            }
        });

        btnTTSSpeedUp.setOnClickListener(v -> {
            float speed = settingsManager.getTTSSpeechRate();
            if (speed < 2.0f) {
                float newSpeed = Math.min(2.0f, speed + 0.1f);
                settingsManager.setTTSSpeechRate(newSpeed);
                updateTTSSpeedDisplay(tvTTSSpeed, tvSpeedValue, newSpeed);
                hasActualChanges = true;
            }
        });

        btnTTSPitchDown.setOnClickListener(v -> {
            float pitch = settingsManager.getTTSPitch();
            if (pitch > 0.5f) {
                float newPitch = Math.max(0.5f, pitch - 0.1f);
                settingsManager.setTTSPitch(newPitch);
                updateTTSPitchDisplay(tvTTSPitch, tvPitchValue, newPitch);
                hasActualChanges = true;
            }
        });

        btnTTSPitchUp.setOnClickListener(v -> {
            float pitch = settingsManager.getTTSPitch();
            if (pitch < 2.0f) {
                float newPitch = Math.min(2.0f, pitch + 0.1f);
                settingsManager.setTTSPitch(newPitch);
                updateTTSPitchDisplay(tvTTSPitch, tvPitchValue, newPitch);
                hasActualChanges = true;
            }
        });

        showDialog(dialogView, context.getString(R.string.voice_settings));
    }

    private void showReadingModeSettings() {
        currentCategory = SettingsCategory.READING_MODE;
        View dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_settings_reading_mode, null);

        ImageButton btnBack = dialogView.findViewById(R.id.btnBack);
        RadioGroup radioGroupReadingMode = dialogView.findViewById(R.id.radioGroupReadingMode);
        RadioButton radioSentenceMode = dialogView.findViewById(R.id.radioSentenceMode);
        RadioButton radioParagraphMode = dialogView.findViewById(R.id.radioParagraphMode);

        btnBack.setOnClickListener(v -> showCategoryList());

        // Set current mode
        if (isFullParagraphMode) {
            radioParagraphMode.setChecked(true);
        } else {
            radioSentenceMode.setChecked(true);
        }

        // Set up listener for reading mode changes
        radioGroupReadingMode.setOnCheckedChangeListener((group, checkedId) -> {
            boolean newIsFullParagraphMode = (checkedId == R.id.radioParagraphMode);
            if (newIsFullParagraphMode != isFullParagraphMode) {
                isFullParagraphMode = newIsFullParagraphMode;
                hasActualChanges = true; // Mark that an actual change was made

                // Notify the reading activity immediately
                if (readingModeChangeListener != null) {
                    readingModeChangeListener.onReadingModeChanged(isFullParagraphMode);
                }

                // Return to main list with updated info
                showCategoryList();
            }
        });

        showDialog(dialogView, context.getString(R.string.reading_mode));
    }

    private void showDebugLogs() {
        // Close the settings dialog and trigger debug logs
        dismiss();
        if (debugLogsListener != null) {
            debugLogsListener.onDebugLogsRequested();
        }
    }

    private void updateSpeedText(TextView tvSpeedValue, float speed) {
        String speedText;
        if (speed < 0.7f) {
            speedText = context.getString(R.string.slow);
        } else if (speed > 1.3f) {
            speedText = context.getString(R.string.fast);
        } else {
            speedText = context.getString(R.string.normal_speed);
        }
        speedText += " (" + String.format("%.1fx", speed) + ")";
        tvSpeedValue.setText(speedText);
    }

    private void showDialog(View view, String title) {
        if (mainDialog != null && mainDialog.isShowing()) {
            // Set navigating flag to prevent premature callback during navigation
            isNavigating = true;
            mainDialog.dismiss();
        }

        currentView = view;
        mainDialog = new AlertDialog.Builder(context)
                .setView(view)
                .setCancelable(true)
                .setOnDismissListener(dialog -> {
                    // Only execute callback when not navigating (actual close) AND there were actual changes
                    if (!isNavigating && hasActualChanges && changeListener != null) {
                        changeListener.onSettingsChanged();
                    }
                    // Reset flags after handling dismiss
                    isNavigating = false;
                    hasActualChanges = false;
                })
                .create();

        mainDialog.show();
        // Reset navigating flag after showing new dialog
        isNavigating = false;
    }

    public void dismiss() {
        if (mainDialog != null && mainDialog.isShowing()) {
            mainDialog.dismiss();
        }
    }

    private void closeDialog() {
        // Ensure callback is executed when closing via close button
        isNavigating = false;
        dismiss();
    }

    private void updateTTSSpeedDisplay(TextView tvTTSSpeed, TextView tvSpeedValue, float speed) {
        // Update the main speed display
        tvTTSSpeed.setText(String.format("%.1fx", speed));

        // Update the descriptive text
        String speedText;
        if (speed < 0.7f) {
            speedText = context.getString(R.string.slow);
        } else if (speed > 1.3f) {
            speedText = context.getString(R.string.fast);
        } else {
            speedText = context.getString(R.string.normal_speed);
        }
        speedText += " (" + String.format("%.1fx", speed) + ")";
        tvSpeedValue.setText(speedText);
    }

    private void updateTTSPitchDisplay(TextView tvTTSPitch, TextView tvPitchValue, float pitch) {
        // Update the main pitch display
        tvTTSPitch.setText(String.format("%.1f", pitch));

        // Update the descriptive text
        String pitchText;
        if (pitch < 0.8f) {
            pitchText = "Grave";
        } else if (pitch > 1.2f) {
            pitchText = "Agudo";
        } else {
            pitchText = "Normal";
        }
        pitchText += " (" + String.format("%.1f", pitch) + ")";
        tvPitchValue.setText(pitchText);
    }

    // Data classes
    public static class CategoryItem {
        public SettingsCategory category;
        public String icon;
        public String title;
        public String subtitle;
        public int iconResource;

        public CategoryItem(SettingsCategory category, String icon, String title, String subtitle, int iconResource) {
            this.category = category;
            this.icon = icon;
            this.title = title;
            this.subtitle = subtitle;
            this.iconResource = iconResource;
        }
    }
}