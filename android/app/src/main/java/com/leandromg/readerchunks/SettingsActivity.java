package com.leandromg.readerchunks;

import android.os.Bundle;
import android.util.TypedValue;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import com.google.android.material.button.MaterialButton;

public class SettingsActivity extends AppCompatActivity {

    private SettingsManager settingsManager;

    // Font Size controls
    private ImageButton btnDecreaseFontSize, btnIncreaseFontSize;
    private TextView tvFontSize;

    // Line Spacing controls
    private ImageButton btnDecreaseLineSpacing, btnIncreaseLineSpacing;
    private TextView tvLineSpacing;

    // Padding controls
    private ImageButton btnDecreasePadding, btnIncreasePadding;
    private TextView tvPadding;

    // Preview
    private TextView tvPreview;
    private android.widget.LinearLayout previewContainer;

    // Reset button
    private MaterialButton btnReset;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

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

        // Preview
        tvPreview = findViewById(R.id.tvPreview);
        previewContainer = findViewById(R.id.previewContainer);

        // Reset
        btnReset = findViewById(R.id.btnReset);
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

        // Reset
        btnReset.setOnClickListener(v -> {
            settingsManager.resetToDefaults();
            updateUI();
        });
    }

    private void updateUI() {
        int fontSize = settingsManager.getFontSize();
        int lineSpacing = settingsManager.getLineSpacing();
        int padding = settingsManager.getPaddingHorizontal();

        // Update text displays
        tvFontSize.setText(fontSize + " sp");
        tvLineSpacing.setText(lineSpacing + " sp");
        tvPadding.setText(padding + " dp");

        // Update button states
        btnDecreaseFontSize.setEnabled(settingsManager.canDecreaseFontSize());
        btnIncreaseFontSize.setEnabled(settingsManager.canIncreaseFontSize());

        btnDecreaseLineSpacing.setEnabled(settingsManager.canDecreaseLineSpacing());
        btnIncreaseLineSpacing.setEnabled(settingsManager.canIncreaseLineSpacing());

        btnDecreasePadding.setEnabled(settingsManager.canDecreasePadding());
        btnIncreasePadding.setEnabled(settingsManager.canIncreasePadding());

        // Update preview
        updatePreview(fontSize, lineSpacing, padding);
    }

    private void updatePreview(int fontSize, int lineSpacing, int padding) {
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
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}