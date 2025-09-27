package com.leandromg.readerchunks;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.progressindicator.LinearProgressIndicator;

import java.util.ArrayList;
import java.util.List;

public class SentenceReaderActivity extends AppCompatActivity {

    private TextView tvSentence;
    private TextView tvProgress;
    private MaterialButton btnPrevious;
    private MaterialButton btnNext;
    private MaterialButton btnBack;
    private LinearProgressIndicator progressBar;

    private List<String> sentences;
    private int currentIndex = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sentence_reader);

        initViews();
        loadSentences();
        setupClickListeners();
        updateDisplay();
    }

    private void initViews() {
        tvSentence = findViewById(R.id.tvSentence);
        tvProgress = findViewById(R.id.tvProgress);
        btnPrevious = findViewById(R.id.btnPrevious);
        btnNext = findViewById(R.id.btnNext);
        btnBack = findViewById(R.id.btnBack);
        progressBar = findViewById(R.id.progressBar);
    }

    private void loadSentences() {
        Intent intent = getIntent();
        sentences = intent.getStringArrayListExtra("sentences");

        if (sentences == null || sentences.isEmpty()) {
            finish();
            return;
        }

        progressBar.setMax(sentences.size());
    }

    private void setupClickListeners() {
        btnPrevious.setOnClickListener(v -> previousSentence());
        btnNext.setOnClickListener(v -> nextSentence());
        btnBack.setOnClickListener(v -> finish());
    }

    private void previousSentence() {
        if (currentIndex > 0) {
            currentIndex--;
            updateDisplay();
        }
    }

    private void nextSentence() {
        if (currentIndex < sentences.size() - 1) {
            currentIndex++;
            updateDisplay();
        } else {
            showCompletionDialog();
        }
    }

    private void updateDisplay() {
        // Update sentence text
        tvSentence.setText(sentences.get(currentIndex));

        // Update progress text
        String progressText = getString(R.string.progress_format, currentIndex + 1, sentences.size());
        tvProgress.setText(progressText);

        // Update progress bar
        progressBar.setProgress(currentIndex + 1);

        // Update button states
        btnPrevious.setEnabled(currentIndex > 0);
        btnNext.setEnabled(currentIndex < sentences.size() - 1);

        // Change button text for last sentence
        if (currentIndex == sentences.size() - 1) {
            btnNext.setText("Finalizar");
        } else {
            btnNext.setText(getString(R.string.next));
        }
    }

    private void showCompletionDialog() {
        new AlertDialog.Builder(this)
                .setTitle(getString(R.string.finished_reading))
                .setMessage(getString(R.string.congratulations))
                .setPositiveButton("Volver al inicio", (dialog, which) -> {
                    finish();
                })
                .setNegativeButton("Leer otra vez", (dialog, which) -> {
                    currentIndex = 0;
                    updateDisplay();
                })
                .setCancelable(false)
                .show();
    }

    @Override
    public void onBackPressed() {
        new AlertDialog.Builder(this)
                .setTitle("Salir de la lectura")
                .setMessage("¿Estás seguro que quieres salir? Se perderá el progreso actual.")
                .setPositiveButton("Salir", (dialog, which) -> {
                    super.onBackPressed();
                })
                .setNegativeButton("Continuar leyendo", null)
                .show();
    }
}