package com.leandromg.readerchunks;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.progressindicator.LinearProgressIndicator;

import java.util.Date;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SentenceReaderActivity extends AppCompatActivity implements BufferManager.BufferLoadListener {

    private TextView tvSentence;
    private TextView tvParagraphProgress;
    private TextView tvSentenceProgress;
    private MaterialButton btnPrevious;
    private MaterialButton btnNext;
    private MaterialButton btnBack;
    private LinearProgressIndicator progressBar;
    private View dividerParagraph;
    private ProgressBar progressCircle;
    private TextView tvProgressPercentage;

    private BookCacheManager cacheManager;
    private BufferManager bufferManager;
    private ExecutorService executor;
    private Book currentBook;
    private int currentIndex = 0;
    private boolean isLoading = false;

    // Current reading position (managed by BufferManager)
    private int currentParagraphIndex = 0;
    private int currentSentenceIndex = 0;

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
        tvParagraphProgress = findViewById(R.id.tvParagraphProgress);
        tvSentenceProgress = findViewById(R.id.tvSentenceProgress);
        btnPrevious = findViewById(R.id.btnPrevious);
        btnNext = findViewById(R.id.btnNext);
        btnBack = findViewById(R.id.btnBack);
        progressBar = findViewById(R.id.progressBar);
        dividerParagraph = findViewById(R.id.dividerParagraph);
        progressCircle = findViewById(R.id.progressCircle);
        tvProgressPercentage = findViewById(R.id.tvProgressPercentage);
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
                currentParagraphIndex = currentBook.getCurrentPosition();
                int savedCharPosition = currentBook.getCurrentCharPosition();

                runOnUiThread(() -> {
                    // Initialize buffer with both paragraph and character position
                    bufferManager.initializeWithCharPosition(bookId, currentBook.getTotalSentences(),
                                                           currentParagraphIndex, savedCharPosition, this);
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
        if (isLoading) return;

        if (bufferManager.moveToPreviousSentence()) {
            updateDisplay();
            saveProgressAsync();
        }
    }

    private void nextSentence() {
        if (currentBook == null || isLoading) return;

        if (bufferManager.moveToNextSentence()) {
            updateDisplay();
            saveProgressAsync();
        } else {
            showCompletionDialog();
        }
    }

    private void updateDisplay() {
        if (currentBook == null) return;

        // Get current sentence from buffer manager
        String currentSentence = bufferManager.getCurrentSentence();
        if (currentSentence.startsWith("Error:")) {
            // Handle error cases
            tvSentence.setText("Cargando...");
            return;
        }

        // Display current sentence
        tvSentence.setText(currentSentence);

        // Update current positions from buffer manager
        currentParagraphIndex = bufferManager.getCurrentParagraphIndex();
        currentSentenceIndex = bufferManager.getCurrentSentenceIndex();

        // Update book object for real-time percentage calculation
        currentBook.setCurrentPosition(currentParagraphIndex);
        currentBook.setCurrentCharPosition(bufferManager.getCurrentCharPosition());

        // Show paragraph divider at end of paragraphs (last sentence of paragraph)
        boolean showDivider = (currentSentenceIndex == bufferManager.getCurrentParagraphSentenceCount() - 1) &&
                             (currentParagraphIndex < currentBook.getTotalSentences() - 1);
        dividerParagraph.setVisibility(showDivider ? View.VISIBLE : View.GONE);

        // Update paragraph progress text
        String paragraphText = getString(R.string.progress_format, currentParagraphIndex + 1, currentBook.getTotalSentences());
        tvParagraphProgress.setText(paragraphText);

        // Update sentence progress text
        if (bufferManager.getCurrentParagraphSentenceCount() > 1) {
            String sentenceText = "(" + (currentSentenceIndex + 1) + "/" + bufferManager.getCurrentParagraphSentenceCount() + ")";
            tvSentenceProgress.setText(sentenceText);
            tvSentenceProgress.setVisibility(View.VISIBLE);
        } else {
            tvSentenceProgress.setVisibility(View.GONE);
        }

        // Update circular progress with precise calculation
        double bookProgress = currentBook.getPreciseProgressPercentage(cacheManager);
        int progressValue = (int) Math.round(bookProgress);
        progressCircle.setProgress(progressValue);
        String percentageText = String.format(Locale.getDefault(), "%.1f%%", bookProgress);
        tvProgressPercentage.setText(percentageText);

        // Update progress bar - show sentence progress within current paragraph
        progressBar.setMax(bufferManager.getCurrentParagraphSentenceCount());
        progressBar.setProgress(currentSentenceIndex + 1);

        // Update button states
        boolean hasPrevious = !bufferManager.isAtBeginningOfBook();
        boolean hasNext = !bufferManager.isAtEndOfBook();

        btnPrevious.setEnabled(hasPrevious && !isLoading);
        btnNext.setEnabled(hasNext && !isLoading);

        // Change button text for last sentence
        if (bufferManager.isAtEndOfBook()) {
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
                    bufferManager.jumpToPosition(0, 0);
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
                currentBook.setCurrentPosition(bufferManager.getCurrentParagraphIndex());
                currentBook.setCurrentCharPosition(bufferManager.getCurrentCharPosition());
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