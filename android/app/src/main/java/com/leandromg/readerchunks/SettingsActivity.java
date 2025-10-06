package com.leandromg.readerchunks;

import android.os.Bundle;
import android.util.TypedValue;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import com.google.android.material.button.MaterialButton;
import android.widget.RadioGroup;
import android.widget.RadioButton;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import java.util.List;

public class SettingsActivity extends AppCompatActivity {

    private SettingsManager settingsManager;
    private LanguageManager languageManager;

    // Font Size controls
    private ImageButton btnDecreaseFontSize, btnIncreaseFontSize;
    private TextView tvFontSize;

    // Line Spacing controls
    private ImageButton btnDecreaseLineSpacing, btnIncreaseLineSpacing;
    private TextView tvLineSpacing;

    // Padding controls
    private ImageButton btnDecreasePadding, btnIncreasePadding;
    private TextView tvPadding;

    // Bionic Reading
    private RadioGroup radioGroupBionicReading;
    private RadioButton radioBionicOff, radioBionicClassic, radioBionicModern;

    // Preview
    private TextView tvPreview;
    private android.widget.LinearLayout previewContainer;

    // Reset button
    private MaterialButton btnReset;

    // Language selection
    private LinearLayout layoutLanguageSelector;
    private ImageView ivLanguageFlag;
    private TextView tvLanguageName;

    // Collapsible sections
    private LinearLayout sectionFontHeader;
    private LinearLayout sectionFontContent;
    private ImageView ivFontExpand;
    private LinearLayout sectionBionicHeader;
    private LinearLayout sectionBionicContent;
    private ImageView ivBionicExpand;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        languageManager = new LanguageManager(this);
        languageManager.applyStoredLanguage();

        settingsManager = new SettingsManager(this);
        initViews();
        setupToolbar();
        setupClickListeners();
        updateUI();
    }

    private void initViews() {
        // Font Size
        btnDecreaseFontSize = findViewById(R.id.btnDecreaseFontSize);
        btnIncreaseFontSize = findViewById(R.id.btnIncreaseFontSize);
        tvFontSize = findViewById(R.id.tvFontSize);

        // Line Spacing
        btnDecreaseLineSpacing = findViewById(R.id.btnDecreaseLineSpacing);
        btnIncreaseLineSpacing = findViewById(R.id.btnIncreaseLineSpacing);
        tvLineSpacing = findViewById(R.id.tvLineSpacing);

        // Padding
        btnDecreasePadding = findViewById(R.id.btnDecreasePadding);
        btnIncreasePadding = findViewById(R.id.btnIncreasePadding);
        tvPadding = findViewById(R.id.tvPadding);

        // Bionic Reading
        radioGroupBionicReading = findViewById(R.id.radioGroupBionicReading);
        radioBionicOff = findViewById(R.id.radioBionicOff);
        radioBionicClassic = findViewById(R.id.radioBionicClassic);
        radioBionicModern = findViewById(R.id.radioBionicModern);

        // Preview
        tvPreview = findViewById(R.id.tvPreview);
        previewContainer = findViewById(R.id.previewContainer);

        // Reset
        btnReset = findViewById(R.id.btnReset);

        // Language selection
        layoutLanguageSelector = findViewById(R.id.layoutLanguageSelector);
        ivLanguageFlag = findViewById(R.id.ivLanguageFlag);
        tvLanguageName = findViewById(R.id.tvLanguageName);

        // Collapsible sections
        sectionFontHeader = findViewById(R.id.sectionFontHeader);
        sectionFontContent = findViewById(R.id.sectionFontContent);
        ivFontExpand = findViewById(R.id.ivFontExpand);
        sectionBionicHeader = findViewById(R.id.sectionBionicHeader);
        sectionBionicContent = findViewById(R.id.sectionBionicContent);
        ivBionicExpand = findViewById(R.id.ivBionicExpand);
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void setupClickListeners() {
        // Font Size
        btnDecreaseFontSize.setOnClickListener(v -> {
            settingsManager.adjustFontSize(-SettingsManager.INCREMENT);
            updateUI();
        });

        btnIncreaseFontSize.setOnClickListener(v -> {
            settingsManager.adjustFontSize(SettingsManager.INCREMENT);
            updateUI();
        });

        // Line Spacing
        btnDecreaseLineSpacing.setOnClickListener(v -> {
            settingsManager.adjustLineSpacing(-SettingsManager.INCREMENT);
            updateUI();
        });

        btnIncreaseLineSpacing.setOnClickListener(v -> {
            settingsManager.adjustLineSpacing(SettingsManager.INCREMENT);
            updateUI();
        });

        // Padding
        btnDecreasePadding.setOnClickListener(v -> {
            settingsManager.adjustPadding(-SettingsManager.INCREMENT);
            updateUI();
        });

        btnIncreasePadding.setOnClickListener(v -> {
            settingsManager.adjustPadding(SettingsManager.INCREMENT);
            updateUI();
        });

        // Bionic Reading
        radioGroupBionicReading.setOnCheckedChangeListener((group, checkedId) -> {
            BionicTextProcessor.BionicMode mode;
            if (checkedId == R.id.radioBionicClassic) {
                mode = BionicTextProcessor.BionicMode.CLASSIC;
            } else if (checkedId == R.id.radioBionicModern) {
                mode = BionicTextProcessor.BionicMode.MODERN;
            } else {
                mode = BionicTextProcessor.BionicMode.OFF;
            }
            settingsManager.setBionicReadingMode(mode);
            updateUI();
        });

        // Reset
        btnReset.setOnClickListener(v -> {
            settingsManager.resetToDefaults();
            updateUI();
        });

        // Language Selection
        layoutLanguageSelector.setOnClickListener(v -> showLanguageSelectionDialog());

        // Collapsible sections
        sectionFontHeader.setOnClickListener(v -> toggleSection(sectionFontContent, ivFontExpand));
        sectionBionicHeader.setOnClickListener(v -> toggleSection(sectionBionicContent, ivBionicExpand));
    }

    private void updateUI() {
        int fontSize = settingsManager.getFontSize();
        int lineSpacing = settingsManager.getLineSpacing();
        int padding = settingsManager.getPaddingHorizontal();
        BionicTextProcessor.BionicMode bionicMode = settingsManager.getBionicReadingMode();

        // Update text displays
        tvFontSize.setText(fontSize + " sp");
        tvLineSpacing.setText(lineSpacing + " sp");
        tvPadding.setText(padding + " dp");

        // Update bionic reading radio buttons
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

        // Update button states
        btnDecreaseFontSize.setEnabled(settingsManager.canDecreaseFontSize());
        btnIncreaseFontSize.setEnabled(settingsManager.canIncreaseFontSize());

        btnDecreaseLineSpacing.setEnabled(settingsManager.canDecreaseLineSpacing());
        btnIncreaseLineSpacing.setEnabled(settingsManager.canIncreaseLineSpacing());

        btnDecreasePadding.setEnabled(settingsManager.canDecreasePadding());
        btnIncreasePadding.setEnabled(settingsManager.canIncreasePadding());

        // Update language selection UI
        updateLanguageUI();

        // Update preview
        updatePreview(fontSize, lineSpacing, padding, bionicMode);
    }

    private void updatePreview(int fontSize, int lineSpacing, int padding, BionicTextProcessor.BionicMode bionicMode) {
        // Update preview text properties
        tvPreview.setTextSize(TypedValue.COMPLEX_UNIT_SP, fontSize);
        tvPreview.setLineSpacing(lineSpacing, 1.0f); // lineSpacingExtra, lineSpacingMultiplier

        // Update preview container padding
        int paddingPx = (int) TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            padding,
            getResources().getDisplayMetrics()
        );
        previewContainer.setPadding(paddingPx, previewContainer.getPaddingTop(),
                                  paddingPx, previewContainer.getPaddingBottom());

        // Apply bionic reading based on mode
        String previewText = getString(R.string.preview_text);
        if (bionicMode != BionicTextProcessor.BionicMode.OFF) {
            tvPreview.setText(BionicTextProcessor.process(previewText, bionicMode), TextView.BufferType.SPANNABLE);
        } else {
            tvPreview.setText(previewText);
        }
    }

    private void updateLanguageUI() {
        LanguageManager.Language currentLanguage = languageManager.getCurrentLanguageInfo();
        ivLanguageFlag.setImageResource(currentLanguage.flagResource);
        tvLanguageName.setText(currentLanguage.displayName);
    }

    private void showLanguageSelectionDialog() {
        List<LanguageManager.Language> languages = languageManager.getSupportedLanguages();
        String currentLangCode = languageManager.getCurrentLanguage();

        // Create custom list view
        ListView listView = new ListView(this);
        LanguageAdapter adapter = new LanguageAdapter(languages, currentLangCode);
        listView.setAdapter(adapter);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getString(R.string.select_language));
        builder.setView(listView);
        builder.setNegativeButton(getString(R.string.cancel), (dialog, which) -> dialog.dismiss());

        AlertDialog dialog = builder.create();

        listView.setOnItemClickListener((parent, view, position, id) -> {
            LanguageManager.Language selectedLang = languages.get(position);
            languageManager.setLanguage(selectedLang.code);
            updateLanguageUI();

            // Recreate activity to apply language change
            recreate();
            dialog.dismiss();
        });

        dialog.show();
    }

    private class LanguageAdapter extends BaseAdapter {
        private final List<LanguageManager.Language> languages;
        private final String currentLanguageCode;

        public LanguageAdapter(List<LanguageManager.Language> languages, String currentLanguageCode) {
            this.languages = languages;
            this.currentLanguageCode = currentLanguageCode;
        }

        @Override
        public int getCount() {
            return languages.size();
        }

        @Override
        public Object getItem(int position) {
            return languages.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = LayoutInflater.from(SettingsActivity.this)
                    .inflate(R.layout.dialog_language_item, parent, false);
            }

            LanguageManager.Language language = languages.get(position);

            ImageView ivFlag = convertView.findViewById(R.id.ivFlag);
            TextView tvLanguageName = convertView.findViewById(R.id.tvLanguageName);
            RadioButton rbLanguage = convertView.findViewById(R.id.rbLanguage);

            ivFlag.setImageResource(language.flagResource);
            tvLanguageName.setText(language.displayName);
            rbLanguage.setChecked(language.code.equals(currentLanguageCode));

            return convertView;
        }
    }

    private void toggleSection(LinearLayout content, ImageView expandIcon) {
        if (content.getVisibility() == View.VISIBLE) {
            content.setVisibility(View.GONE);
            expandIcon.setRotation(0);
        } else {
            content.setVisibility(View.VISIBLE);
            expandIcon.setRotation(180);
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}