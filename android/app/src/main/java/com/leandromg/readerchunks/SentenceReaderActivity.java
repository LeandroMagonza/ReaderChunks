package com.leandromg.readerchunks;

import android.content.Intent;
import android.os.Bundle;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.ProgressBar;
import android.widget.ScrollView;
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
    private MaterialButton btnToggleMode;
    private LinearProgressIndicator progressBar;
    private View dividerParagraph;
    private ProgressBar progressCircle;
    private TextView tvProgressPercentage;
    private ScrollView scrollView;

    private BookCacheManager cacheManager;
    private BufferManager bufferManager;
    private ExecutorService executor;
    private Book currentBook;
    private int currentIndex = 0;
    private boolean isLoading = false;
    private GestureDetector gestureDetector;
    private ThemeManager themeManager;
    private SettingsManager settingsManager;
    private LanguageManager languageManager;

    // Current reading position (managed by BufferManager)
    private int currentParagraphIndex = 0;
    private int currentSentenceIndex = 0;

    // Reading mode toggle
    private boolean isFullParagraphMode = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Initialize language, theme and settings before setting content view
        languageManager = new LanguageManager(this);
        languageManager.applyStoredLanguage();

        themeManager = new ThemeManager(this);
        themeManager.applyTheme();
        settingsManager = new SettingsManager(this);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sentence_reader);

        initViews();
        applySettings();
        setupManagers();
        loadBook();
        setupClickListeners();
        setupGestureDetector();
    }

    private void initViews() {
        tvSentence = findViewById(R.id.tvSentence);
        tvParagraphProgress = findViewById(R.id.tvParagraphProgress);
        tvSentenceProgress = findViewById(R.id.tvSentenceProgress);
        btnPrevious = findViewById(R.id.btnPrevious);
        btnNext = findViewById(R.id.btnNext);
        btnBack = findViewById(R.id.btnBack);
        btnToggleMode = findViewById(R.id.btnToggleMode);
        progressBar = findViewById(R.id.progressBar);
        dividerParagraph = findViewById(R.id.dividerParagraph);
        progressCircle = findViewById(R.id.progressCircle);
        tvProgressPercentage = findViewById(R.id.tvProgressPercentage);
        scrollView = findViewById(R.id.scrollView);
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
            Toast.makeText(this, getString(R.string.error_book_not_found), Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        executor.execute(() -> {
            try {
                currentBook = cacheManager.loadBookMeta(bookId);
                currentParagraphIndex = currentBook.getCurrentPosition();
                int savedCharPosition = currentBook.getCurrentCharPosition();

                runOnUiThread(() -> {
                    // Load saved reading mode
                    isFullParagraphMode = currentBook.isFullParagraphMode();

                    // Initialize buffer with both paragraph and character position
                    bufferManager.initializeWithCharPosition(bookId, currentBook.getTotalSentences(),
                                                           currentParagraphIndex, savedCharPosition, this);
                    updateDisplay();
                });

            } catch (Exception e) {
                runOnUiThread(() -> {
                    Toast.makeText(this, getString(R.string.error_loading_book, e.getMessage()), Toast.LENGTH_SHORT).show();
                    finish();
                });
            }
        });
    }

    private void setupClickListeners() {
        btnPrevious.setOnClickListener(v -> previousSentence());
        btnNext.setOnClickListener(v -> nextSentence());
        btnBack.setOnClickListener(v -> finish());
        btnToggleMode.setOnClickListener(v -> toggleReadingMode());
    }

    private void setupGestureDetector() {
        gestureDetector = new GestureDetector(this, new GestureDetector.SimpleOnGestureListener() {
            private static final int SWIPE_THRESHOLD = 100;
            private static final int SWIPE_VELOCITY_THRESHOLD = 100;

            @Override
            public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
                if (e1 == null || e2 == null) {
                    return false;
                }

                float diffY = e2.getY() - e1.getY();
                float diffX = e2.getX() - e1.getX();

                if (Math.abs(diffX) > Math.abs(diffY)) {
                    if (Math.abs(diffX) > SWIPE_THRESHOLD && Math.abs(velocityX) > SWIPE_VELOCITY_THRESHOLD) {
                        if (diffX > 0) {
                            // Right swipe - previous sentence/paragraph
                            previousSentence();
                        } else {
                            // Left swipe - next sentence/paragraph
                            nextSentence();
                        }
                        return true;
                    }
                }
                return false;
            }
        });

        // Set up touch listener for ScrollView to handle gestures
        scrollView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                // Let gesture detector handle the event first
                if (gestureDetector.onTouchEvent(event)) {
                    return true;
                }
                // If not a swipe gesture, let ScrollView handle it normally
                return false;
            }
        });

        // Set up scroll listener to save progress based on scroll position in paragraph mode
        scrollView.getViewTreeObserver().addOnScrollChangedListener(new ViewTreeObserver.OnScrollChangedListener() {
            private long lastScrollTime = 0;

            @Override
            public void onScrollChanged() {
                if (isFullParagraphMode) {
                    // Debounce scroll events - only save every 500ms
                    long currentTime = System.currentTimeMillis();
                    if (currentTime - lastScrollTime > 500) {
                        lastScrollTime = currentTime;
                        saveProgressAsync();
                    }
                }
            }
        });
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (gestureDetector.onTouchEvent(event)) {
            return true;
        }
        return super.onTouchEvent(event);
    }

    private void previousSentence() {
        if (isLoading) return;

        if (isFullParagraphMode) {
            // In paragraph mode, navigate to previous paragraph
            if (bufferManager.moveToPreviousParagraph()) {
                updateDisplay();
                resetScrollPosition();
                saveProgressAsync();
            }
        } else {
            // In sentence mode, navigate to previous sentence
            if (bufferManager.moveToPreviousSentence()) {
                updateDisplay();
                saveProgressAsync();
            }
        }
    }

    private void nextSentence() {
        if (currentBook == null || isLoading) return;

        if (isFullParagraphMode) {
            // In paragraph mode, navigate to next paragraph
            if (bufferManager.moveToNextParagraph()) {
                updateDisplay();
                resetScrollPosition();
                saveProgressAsync();
            } else {
                showCompletionDialog();
            }
        } else {
            // In sentence mode, navigate to next sentence
            if (bufferManager.moveToNextSentence()) {
                updateDisplay();
                saveProgressAsync();
            } else {
                showCompletionDialog();
            }
        }
    }

    private void resetScrollPosition() {
        if (scrollView != null) {
            scrollView.scrollTo(0, 0);
        }
    }

    private void restoreScrollPosition() {
        if (scrollView == null || tvSentence == null || !isFullParagraphMode) {
            return;
        }

        // Wait for layout to be complete before restoring scroll
        tvSentence.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                tvSentence.getViewTreeObserver().removeOnGlobalLayoutListener(this);

                try {
                    // Get saved character position (relative to paragraph)
                    int savedCharPosition = currentBook.getCurrentCharPosition();

                    // Get paragraph text to validate position
                    String paragraphText = bufferManager.getCurrentParagraphText();
                    if (paragraphText == null) return;

                    // Ensure saved position is within paragraph bounds
                    int relativeCharPosition = Math.max(0, Math.min(savedCharPosition, paragraphText.length() - 1));

                    if (relativeCharPosition <= 5) { // Avoid tiny scrolls
                        scrollView.scrollTo(0, 0);
                        return;
                    }

                    // Use same logic as scrollToCharPosition but without the +85 buffer
                    float scrollPercent = Math.min(100f, (relativeCharPosition * 100f) / paragraphText.length());

                    // Calculate scroll position after layout is complete
                    int maxScrollY = Math.max(0, tvSentence.getHeight() - scrollView.getHeight());
                    int scrollY = (int) (maxScrollY * scrollPercent / 100f);

                    // Post again to ensure layout is fully settled
                    scrollView.post(() -> {
                        scrollView.smoothScrollTo(0, scrollY);
                    });

                } catch (Exception e) {
                    // If restoration fails, stay at top
                    scrollView.scrollTo(0, 0);
                }
            }
        });
    }

    private int estimateCharactersBeforeCurrentSentence(String paragraphText) {
        try {
            int currentSentenceIndex = bufferManager.getCurrentSentenceIndex();
            if (currentSentenceIndex <= 0) {
                return 0;
            }

            // Simple estimation: split by sentence and count characters
            // This is a rough approximation
            String[] sentences = paragraphText.split("[.!?]+\\s*");
            int charCount = 0;

            for (int i = 0; i < currentSentenceIndex && i < sentences.length; i++) {
                charCount += sentences[i].length();
                if (i < sentences.length - 1) {
                    charCount += 2; // Approximate for punctuation and space
                }
            }

            return charCount;
        } catch (Exception e) {
            return 0;
        }
    }

    private int calculateScrollBasedCharPosition() {
        if (scrollView == null || tvSentence == null || !isFullParagraphMode) {
            return bufferManager.getCurrentCharPosition();
        }

        try {
            // Get current scroll position
            int scrollY = scrollView.getScrollY();
            int maxScrollY = Math.max(1, tvSentence.getHeight() - scrollView.getHeight());

            // Calculate scroll percentage
            float scrollPercent = Math.min(1.0f, (float)scrollY / maxScrollY);

            // Get paragraph text
            String paragraphText = bufferManager.getCurrentParagraphText();
            if (paragraphText == null || paragraphText.isEmpty()) {
                return bufferManager.getCurrentCharPosition();
            }

            // Calculate target character based on scroll percentage
            int targetChar = (int)(paragraphText.length() * scrollPercent);

            // Return the character position (no buffer needed)
            return Math.min(targetChar, paragraphText.length() - 1);

        } catch (Exception e) {
            // If calculation fails, return current position
            return bufferManager.getCurrentCharPosition();
        }
    }

    private int getFirstVisibleSentenceIndex() {
        if (scrollView == null || tvSentence == null || !isFullParagraphMode) {
            return bufferManager.getCurrentSentenceIndex();
        }

        try {
            // Get current scroll position
            int scrollY = scrollView.getScrollY();
            int maxScrollY = Math.max(1, tvSentence.getHeight() - scrollView.getHeight());

            // Calculate scroll percentage
            float scrollPercent = Math.min(1.0f, (float)scrollY / maxScrollY);

            // Special case: if we're near the end, show one of the last sentences
            String paragraphText = bufferManager.getCurrentParagraphText();
            if (paragraphText == null) return bufferManager.getCurrentSentenceIndex();

            // Get total number of sentences in this paragraph
            int totalSentences = bufferManager.getCurrentParagraphSentenceCount();

            if (scrollPercent >= 0.95f && totalSentences > 3) {
                // Near the end - show 3rd to last sentence so there's content visible
                return totalSentences - 3;
            }

            // Calculate target character based on scroll percentage
            int targetChar = (int)(paragraphText.length() * scrollPercent);

            // Find which sentence contains this character
            int sentenceIndex = bufferManager.findSentenceIndexForCharPosition(targetChar);

            return Math.max(0, sentenceIndex);

        } catch (Exception e) {
            return bufferManager.getCurrentSentenceIndex();
        }
    }

    private void updateDisplay() {
        if (currentBook == null) return;

        if (isFullParagraphMode) {
            // Display full paragraph
            String currentParagraph = bufferManager.getCurrentParagraphText();
            if (currentParagraph == null || currentParagraph.startsWith("Error:")) {
                tvSentence.setText("Cargando...");
                return;
            }
            // Apply bionic reading based on mode
            if (settingsManager != null) {
                BionicTextProcessor.BionicMode bionicMode = settingsManager.getBionicReadingMode();
                if (bionicMode != BionicTextProcessor.BionicMode.OFF) {
                    tvSentence.setText(BionicTextProcessor.process(currentParagraph, bionicMode), TextView.BufferType.SPANNABLE);
                } else {
                    tvSentence.setText(currentParagraph);
                }
            } else {
                tvSentence.setText(currentParagraph);
            }

            // Restore scroll position based on character position
            restoreScrollPosition();
        } else {
            // Display current sentence (bite-size mode)
            String currentSentence = bufferManager.getCurrentSentence();
            if (currentSentence.startsWith("Error:")) {
                // Handle error cases
                tvSentence.setText("Cargando...");
                return;
            }
            // Apply bionic reading based on mode
            if (settingsManager != null) {
                BionicTextProcessor.BionicMode bionicMode = settingsManager.getBionicReadingMode();
                if (bionicMode != BionicTextProcessor.BionicMode.OFF) {
                    tvSentence.setText(BionicTextProcessor.process(currentSentence, bionicMode), TextView.BufferType.SPANNABLE);
                } else {
                    tvSentence.setText(currentSentence);
                }
            } else {
                tvSentence.setText(currentSentence);
            }
        }

        // Update current positions from buffer manager
        currentParagraphIndex = bufferManager.getCurrentParagraphIndex();
        currentSentenceIndex = bufferManager.getCurrentSentenceIndex();

        // Update book object for real-time percentage calculation
        currentBook.setCurrentPosition(currentParagraphIndex);
        // Note: Don't update currentCharPosition here - it should only be updated in saveProgressAsync()

        // Show paragraph divider only in sentence mode at end of paragraphs
        if (isFullParagraphMode) {
            dividerParagraph.setVisibility(View.GONE);
        } else {
            boolean showDivider = (currentSentenceIndex == bufferManager.getCurrentParagraphSentenceCount() - 1) &&
                                 (currentParagraphIndex < currentBook.getTotalSentences() - 1);
            dividerParagraph.setVisibility(showDivider ? View.VISIBLE : View.GONE);
        }

        // Update paragraph progress text
        String paragraphText = getString(R.string.progress_format, currentParagraphIndex + 1, currentBook.getTotalSentences());
        tvParagraphProgress.setText(paragraphText);

        // Update sentence progress text (only in sentence mode)
        if (isFullParagraphMode) {
            tvSentenceProgress.setVisibility(View.GONE);
        } else {
            if (bufferManager.getCurrentParagraphSentenceCount() > 1) {
                String sentenceText = "(" + (currentSentenceIndex + 1) + "/" + bufferManager.getCurrentParagraphSentenceCount() + ")";
                tvSentenceProgress.setText(sentenceText);
                tvSentenceProgress.setVisibility(View.VISIBLE);
            } else {
                tvSentenceProgress.setVisibility(View.GONE);
            }
        }

        // Update circular progress with precise calculation
        double bookProgress = currentBook.getPreciseProgressPercentage(cacheManager);
        int progressValue = (int) Math.round(bookProgress);
        progressCircle.setProgress(progressValue);
        String percentageText = String.format(Locale.getDefault(), "%.1f%%", bookProgress);
        tvProgressPercentage.setText(percentageText);

        // Update progress bar - only show in sentence mode
        if (isFullParagraphMode) {
            progressBar.setVisibility(View.GONE);
        } else {
            progressBar.setVisibility(View.VISIBLE);
            progressBar.setMax(bufferManager.getCurrentParagraphSentenceCount());
            progressBar.setProgress(currentSentenceIndex + 1);
        }

        // Update button states
        boolean hasPrevious = !bufferManager.isAtBeginningOfBook();
        boolean hasNext = !bufferManager.isAtEndOfBook();

        btnPrevious.setEnabled(hasPrevious && !isLoading);
        btnNext.setEnabled(hasNext && !isLoading);

        // Change button text for last sentence/paragraph
        if (bufferManager.isAtEndOfBook()) {
            btnNext.setText("Finalizar");
        } else {
            btnNext.setText(getString(R.string.next));
        }

        // Update toggle button icon - show CURRENT mode
        if (isFullParagraphMode) {
            btnToggleMode.setText(getString(R.string.mode_paragraph_icon)); // |☰|
        } else {
            btnToggleMode.setText(getString(R.string.mode_sentence_icon)); // |-|
        }
    }

    private void toggleReadingMode() {
        if (isFullParagraphMode) {
            // Currently in paragraph mode, switching to sentence mode
            // Find the first visible sentence based on scroll position
            int visibleSentenceIndex = getFirstVisibleSentenceIndex();

            // Set the BufferManager to that sentence
            bufferManager.setCurrentSentenceIndex(visibleSentenceIndex);

            // Get the character position of that sentence for saving
            int sentenceCharPosition = bufferManager.getCurrentCharPosition();
            currentBook.setCurrentCharPosition(sentenceCharPosition);

            // Toast.makeText(this, "Párrafo→Oración: oración " + (visibleSentenceIndex + 1) + " (char " + sentenceCharPosition + ")", Toast.LENGTH_SHORT).show();
        } else {
            // Currently in sentence mode, switching to paragraph mode
            // Get current sentence and calculate its middle position for better centering
            int currentSentenceIndex = bufferManager.getCurrentSentenceIndex();

            ParagraphSentences sentences = bufferManager.getCurrentParagraphSentences();
            if (sentences != null) {
                int sentenceStart = sentences.getSentenceStart(currentSentenceIndex);
                int sentenceEnd = sentences.getSentenceEnd(currentSentenceIndex);
                int sentenceMiddle = (sentenceStart + sentenceEnd) / 2;

                currentBook.setCurrentCharPosition(sentenceMiddle);
                scrollToCharPosition(sentenceMiddle);

                // Toast.makeText(this, "Oración→Párrafo: oración " + (currentSentenceIndex + 1) + " (char " + sentenceMiddle + ")", Toast.LENGTH_SHORT).show();
            } else {
                // Fallback to current position
                int currentCharPosition = bufferManager.getCurrentCharPosition();
                currentBook.setCurrentCharPosition(currentCharPosition);
                scrollToCharPosition(currentCharPosition);

                // Toast.makeText(this, "Oración→Párrafo: char " + currentCharPosition, Toast.LENGTH_SHORT).show();
            }
        }

        // Switch the mode
        isFullParagraphMode = !isFullParagraphMode;

        // Update display immediately - no reloading needed
        updateDisplay();

        // Save progress
        saveProgressAsync();
    }

    private void setSentenceFromCharPosition(int charPosition) {
        // Use BufferManager to find which sentence contains this character position
        bufferManager.setCharacterPosition(charPosition);
    }

    private void scrollToCharPosition(int charPosition) {
        if (scrollView == null || tvSentence == null) {
            return;
        }

        // Wait for layout to be complete before calculating scroll
        tvSentence.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                tvSentence.getViewTreeObserver().removeOnGlobalLayoutListener(this);

                try {
                    String paragraphText = bufferManager.getCurrentParagraphText();
                    if (paragraphText == null || paragraphText.isEmpty()) return;

                    // Calculate scroll percentage directly from character position
                    // Use exact percentage - no adjustment to avoid bias
                    float scrollPercent = Math.min(100f, (charPosition * 100f) / paragraphText.length());

                    // Calculate scroll position after layout is complete
                    int maxScrollY = Math.max(0, tvSentence.getHeight() - scrollView.getHeight());
                    int scrollY = (int) (maxScrollY * scrollPercent / 100f);

                    // Post again to ensure layout is fully settled
                    scrollView.post(() -> {
                        scrollView.smoothScrollTo(0, scrollY);
                        // Toast.makeText(SentenceReaderActivity.this, "Scroll: " + (int)scrollPercent + "% → " + scrollY + "px (max:" + maxScrollY + ")", Toast.LENGTH_SHORT).show();
                    });

                } catch (Exception e) {
                    // If calculation fails, stay at current position
                }
            }
        });
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

                // In paragraph mode, calculate character position based on scroll
                if (isFullParagraphMode) {
                    int scrollBasedCharPosition = calculateScrollBasedCharPosition();
                    currentBook.setCurrentCharPosition(scrollBasedCharPosition);
                } else {
                    currentBook.setCurrentCharPosition(bufferManager.getCurrentCharPosition());
                }

                currentBook.setLastReadDate(new Date());
                currentBook.setFullParagraphMode(isFullParagraphMode); // Guardar modo de lectura
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

            // Save progress after buffer loads to persist the mode switch
            saveProgressAsync();
        });
    }

    @Override
    public void onBufferError(String error) {
        runOnUiThread(() -> {
            isLoading = false;
            Toast.makeText(this, getString(R.string.error_loading_buffer, error), Toast.LENGTH_SHORT).show();
        });
    }

    @Override
    public void onBackPressed() {
        saveProgressAsync();
        new AlertDialog.Builder(this)
                .setTitle(getString(R.string.exit_reading_title))
                .setMessage(getString(R.string.exit_reading_message))
                .setPositiveButton(getString(R.string.save_and_exit), (dialog, which) -> {
                    super.onBackPressed();
                })
                .setNegativeButton(getString(R.string.continue_reading_dialog), null)
                .show();
    }

    @Override
    protected void onPause() {
        super.onPause();
        saveProgressAsync();
    }

    private void applySettings() {
        if (settingsManager == null || tvSentence == null) return;

        // Apply font size
        float fontSize = settingsManager.getFontSize();
        tvSentence.setTextSize(android.util.TypedValue.COMPLEX_UNIT_SP, fontSize);

        // Apply line spacing (lineSpacingExtra)
        float lineSpacing = settingsManager.getLineSpacing();
        tvSentence.setLineSpacing(lineSpacing, 1.0f);

        // Apply horizontal padding to the container
        View container = findViewById(R.id.sentenceContainer);
        if (container != null) {
            int paddingHorizontal = (int) android.util.TypedValue.applyDimension(
                android.util.TypedValue.COMPLEX_UNIT_DIP,
                settingsManager.getPaddingHorizontal(),
                getResources().getDisplayMetrics()
            );
            container.setPadding(paddingHorizontal, container.getPaddingTop(),
                               paddingHorizontal, container.getPaddingBottom());
        }

        // Refresh text display with current content to apply bionic reading if needed
        updateDisplay();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Reapply settings when returning from settings screen
        applySettings();
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