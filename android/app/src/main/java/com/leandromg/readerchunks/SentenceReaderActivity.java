package com.leandromg.readerchunks;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.progressindicator.LinearProgressIndicator;

import java.util.Date;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SentenceReaderActivity extends AppCompatActivity implements BufferManager.BufferLoadListener {

    private TextView tvSentence;
    private TextView tvProgress;
    private MaterialButton btnPrevious;
    private MaterialButton btnNext;
    private MaterialButton btnBack;
    private LinearProgressIndicator progressBar;

    private BookCacheManager cacheManager;
    private BufferManager bufferManager;
    private ExecutorService executor;
    private Book currentBook;
    private int currentIndex = 0;
    private boolean isLoading = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sentence_reader);

        initViews();
        setupManagers();
        loadBook();
        setupClickListeners();
    }

    private void initViews() {
        tvSentence = findViewById(R.id.tvSentence);
        tvProgress = findViewById(R.id.tvProgress);
        btnPrevious = findViewById(R.id.btnPrevious);
        btnNext = findViewById(R.id.btnNext);
        btnBack = findViewById(R.id.btnBack);
        progressBar = findViewById(R.id.progressBar);
    }

    private void setupManagers() {
        cacheManager = new BookCacheManager(this);
        bufferManager = new BufferManager(cacheManager);
        executor = Executors.newSingleThreadExecutor();
    }

    private void loadBook() {
        Intent intent = getIntent();
        String bookId = intent.getStringExtra("book_id");

        if (bookId == null) {
            Toast.makeText(this, "Error: Libro no encontrado", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        executor.execute(() -> {
            try {
                currentBook = cacheManager.loadBookMeta(bookId);
                currentIndex = currentBook.getCurrentPosition();

                runOnUiThread(() -> {
                    progressBar.setMax(currentBook.getTotalSentences());
                    bufferManager.initialize(bookId, currentBook.getTotalSentences(), currentIndex, this);
                    updateDisplay();
                });

            } catch (Exception e) {
                runOnUiThread(() -> {
                    Toast.makeText(this, "Error cargando libro: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    finish();
                });
            }
        });
    }

    private void setupClickListeners() {
        btnPrevious.setOnClickListener(v -> previousSentence());
        btnNext.setOnClickListener(v -> nextSentence());
        btnBack.setOnClickListener(v -> finish());
    }

    private void previousSentence() {
        if (currentIndex > 0 && !isLoading) {
            currentIndex--;
            updateDisplay();
            saveProgressAsync();
        }
    }

    private void nextSentence() {
        if (currentBook == null || isLoading) return;

        if (currentIndex < currentBook.getTotalSentences() - 1) {
            currentIndex++;
            updateDisplay();
            saveProgressAsync();
        } else {
            showCompletionDialog();
        }
    }

    private void updateDisplay() {
        if (currentBook == null) return;

        // Get sentence from buffer
        String sentence = bufferManager.getSentence(currentIndex);
        tvSentence.setText(sentence);

        // Update progress text
        String progressText = getString(R.string.progress_format, currentIndex + 1, currentBook.getTotalSentences());
        tvProgress.setText(progressText);

        // Update progress bar
        progressBar.setProgress(currentIndex + 1);

        // Update button states
        btnPrevious.setEnabled(currentIndex > 0 && !isLoading);
        btnNext.setEnabled(currentIndex < currentBook.getTotalSentences() - 1 && !isLoading);

        // Change button text for last sentence
        if (currentIndex == currentBook.getTotalSentences() - 1) {
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
                    bufferManager.jumpToPosition(0);
                    saveProgressAsync();
                    updateDisplay();
                })
                .setCancelable(false)
                .show();
    }

    private void saveProgressAsync() {
        if (currentBook == null) return;

        executor.execute(() -> {
            try {
                currentBook.setCurrentPosition(currentIndex);
                currentBook.setLastReadDate(new Date());
                cacheManager.saveBookMeta(currentBook);
            } catch (Exception e) {
                // Log error but don't interrupt reading
            }
        });
    }

    @Override
    public void onBufferLoaded() {
        runOnUiThread(() -> {
            isLoading = false;
            updateDisplay();
        });
    }

    @Override
    public void onBufferError(String error) {
        runOnUiThread(() -> {
            isLoading = false;
            Toast.makeText(this, "Error de carga: " + error, Toast.LENGTH_SHORT).show();
        });
    }

    @Override
    public void onBackPressed() {
        saveProgressAsync();
        new AlertDialog.Builder(this)
                .setTitle("Salir de la lectura")
                .setMessage("El progreso se ha guardado automáticamente.")
                .setPositiveButton("Salir", (dialog, which) -> {
                    super.onBackPressed();
                })
                .setNegativeButton("Continuar leyendo", null)
                .show();
    }

    @Override
    protected void onPause() {
        super.onPause();
        saveProgressAsync();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (bufferManager != null) {
            bufferManager.shutdown();
        }
        if (executor != null) {
            executor.shutdown();
        }
    }
}