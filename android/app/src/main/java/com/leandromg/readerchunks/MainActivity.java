package com.leandromg.readerchunks;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.progressindicator.CircularProgressIndicator;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {

    private MaterialButton btnSelectPdf;
    private MaterialButton btnStartReading;
    private CircularProgressIndicator progressIndicator;
    private TextView tvStatus;
    private TextView tvPdfInfo;
    private MaterialCardView cardInfo;

    private ExecutorService executor;
    private String selectedPdfPath;
    private List<String> sentences;

    private ActivityResultLauncher<String[]> pdfPickerLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initViews();
        setupExecutor();
        setupPdfPicker();
        setupClickListeners();
    }

    private void initViews() {
        btnSelectPdf = findViewById(R.id.btnSelectPdf);
        btnStartReading = findViewById(R.id.btnStartReading);
        progressIndicator = findViewById(R.id.progressIndicator);
        tvStatus = findViewById(R.id.tvStatus);
        tvPdfInfo = findViewById(R.id.tvPdfInfo);
        cardInfo = findViewById(R.id.cardInfo);
    }

    private void setupExecutor() {
        executor = Executors.newSingleThreadExecutor();
    }

    private void setupPdfPicker() {
        pdfPickerLauncher = registerForActivityResult(
                new ActivityResultContracts.OpenDocument(),
                this::handlePdfSelection
        );
    }

    private void setupClickListeners() {
        btnSelectPdf.setOnClickListener(v -> openPdfPicker());
        btnStartReading.setOnClickListener(v -> startReading());
    }

    private void openPdfPicker() {
        pdfPickerLauncher.launch(new String[]{"application/pdf"});
    }

    private void handlePdfSelection(Uri uri) {
        if (uri != null) {
            selectedPdfPath = uri.toString();
            processPdf(uri);
        }
    }

    private void processPdf(Uri uri) {
        showLoading(true);
        tvStatus.setText("Procesando PDF...");

        executor.execute(() -> {
            try {
                String text = PDFTextExtractor.extractTextFromUri(this, uri);
                sentences = SentenceSegmenter.segmentIntoSentences(text);

                runOnUiThread(() -> {
                    showLoading(false);
                    showPdfInfo(uri, sentences.size());
                });

            } catch (Exception e) {
                runOnUiThread(() -> {
                    showLoading(false);
                    showError("Error procesando PDF: " + e.getMessage());
                });
            }
        });
    }

    private void showLoading(boolean show) {
        progressIndicator.setVisibility(show ? View.VISIBLE : View.GONE);
        tvStatus.setVisibility(show ? View.VISIBLE : View.GONE);
        btnSelectPdf.setEnabled(!show);
    }

    private void showPdfInfo(Uri uri, int sentenceCount) {
        String fileName = getFileNameFromUri(uri);
        String info = String.format(
                "📄 Archivo: %s\n📝 Oraciones extraídas: %d\n\n¡Listo para comenzar la lectura!",
                fileName, sentenceCount
        );

        tvPdfInfo.setText(info);
        cardInfo.setVisibility(View.VISIBLE);
    }

    private void showError(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
        cardInfo.setVisibility(View.GONE);
    }

    private void startReading() {
        if (sentences != null && !sentences.isEmpty()) {
            Intent intent = new Intent(this, SentenceReaderActivity.class);
            intent.putStringArrayListExtra("sentences", (java.util.ArrayList<String>) sentences);
            startActivity(intent);
        }
    }

    private String getFileNameFromUri(Uri uri) {
        String path = uri.getPath();
        if (path != null) {
            int index = path.lastIndexOf('/');
            if (index != -1) {
                return path.substring(index + 1);
            }
        }
        return "documento.pdf";
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (executor != null) {
            executor.shutdown();
        }
    }
}