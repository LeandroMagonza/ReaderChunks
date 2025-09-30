package com.leandromg.readerchunks;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class BufferManager {
    private BookCacheManager cacheManager;
    private ExecutorService executor;
    private String currentBookId;
    private int totalParagraphs;

    // 3-paragraph buffer with sentence positions
    private ParagraphSentences previousParagraph = null;
    private ParagraphSentences currentParagraph = null;
    private ParagraphSentences nextParagraph = null;

    // Current position tracking
    private int currentParagraphIndex = -1;
    private int currentSentenceIndex = 0;

    private BufferLoadListener listener;

    public interface BufferLoadListener {
        void onBufferLoaded();
        void onBufferError(String error);
    }

    public BufferManager(BookCacheManager cacheManager) {
        this.cacheManager = cacheManager;
        this.executor = Executors.newSingleThreadExecutor();
    }

    public void initialize(String bookId, int totalParagraphs, int startParagraphIndex, BufferLoadListener listener) {
        this.currentBookId = bookId;
        this.totalParagraphs = totalParagraphs;
        this.listener = listener;

        // Clear existing buffers
        clearBuffers();

        // Load buffers for the starting paragraph
        loadParagraphsAroundIndex(startParagraphIndex);
    }

    /**
     * Initialize with specific paragraph and sentence position
     */
    public void initialize(String bookId, int totalParagraphs, int paragraphIndex, int sentenceIndex, BufferLoadListener listener) {
        this.currentBookId = bookId;
        this.totalParagraphs = totalParagraphs;
        this.listener = listener;
        this.currentSentenceIndex = sentenceIndex;

        // Clear existing buffers
        clearBuffers();

        // Load buffers for the starting paragraph
        loadParagraphsAroundIndex(paragraphIndex);
    }

    /**
     * Initialize with specific paragraph and character position
     */
    public void initializeWithCharPosition(String bookId, int totalParagraphs, int paragraphIndex, int charPosition, BufferLoadListener listener) {
        this.currentBookId = bookId;
        this.totalParagraphs = totalParagraphs;
        this.listener = listener;

        // Clear existing buffers
        clearBuffers();

        // Load buffers for the starting paragraph and set character position
        loadParagraphsAroundIndexWithCharPosition(paragraphIndex, charPosition);
    }

    /**
     * Get sentence from current paragraph
     */
    public String getCurrentSentence() {
        if (currentParagraph == null) {
            return "Error: No hay párrafo cargado";
        }

        String sentence = currentParagraph.getSentence(currentSentenceIndex);
        return sentence != null ? sentence : "Error: Oración no disponible";
    }

    /**
     * Move to next sentence (within paragraph or to next paragraph)
     */
    public boolean moveToNextSentence() {
        if (currentParagraph == null) {
            return false;
        }

        // Try to move to next sentence within current paragraph
        if (currentSentenceIndex < currentParagraph.getSentenceCount() - 1) {
            currentSentenceIndex++;
            return true;
        }

        // Move to next paragraph's first sentence
        if (moveToNextParagraph()) {
            currentSentenceIndex = 0;
            return true;
        }

        return false; // End of book
    }

    /**
     * Move to previous sentence (within paragraph or to previous paragraph)
     */
    public boolean moveToPreviousSentence() {
        if (currentParagraph == null) {
            return false;
        }

        // Try to move to previous sentence within current paragraph
        if (currentSentenceIndex > 0) {
            currentSentenceIndex--;
            return true;
        }

        // Move to previous paragraph's last sentence
        if (moveToPreviousParagraph()) {
            currentSentenceIndex = currentParagraph.getSentenceCount() - 1;
            return true;
        }

        return false; // Beginning of book
    }

    /**
     * Move to next paragraph
     */
    public boolean moveToNextParagraph() {
        if (currentParagraphIndex >= totalParagraphs - 1) {
            return false; // Already at last paragraph
        }

        // Shift buffers: current becomes previous, next becomes current
        previousParagraph = currentParagraph;
        currentParagraph = nextParagraph;
        currentParagraphIndex++;

        // Load new next paragraph asynchronously
        loadNextParagraphAsync();

        return true;
    }

    /**
     * Move to previous paragraph
     */
    public boolean moveToPreviousParagraph() {
        if (currentParagraphIndex <= 0) {
            return false; // Already at first paragraph
        }

        // Shift buffers: current becomes next, previous becomes current
        nextParagraph = currentParagraph;
        currentParagraph = previousParagraph;
        currentParagraphIndex--;

        // Load new previous paragraph asynchronously
        loadPreviousParagraphAsync();

        return true;
    }

    /**
     * Jump to specific paragraph and sentence
     */
    public void jumpToPosition(int paragraphIndex, int sentenceIndex) {
        if (paragraphIndex >= 0 && paragraphIndex < totalParagraphs) {
            this.currentSentenceIndex = Math.max(0, sentenceIndex);
            loadParagraphsAroundIndex(paragraphIndex);
        }
    }

    /**
     * Jump to specific paragraph (sentence 0)
     */
    public void jumpToPosition(int paragraphIndex) {
        jumpToPosition(paragraphIndex, 0);
    }

    /**
     * Get current paragraph index
     */
    public int getCurrentParagraphIndex() {
        return currentParagraphIndex;
    }

    /**
     * Get current sentence index within paragraph
     */
    public int getCurrentSentenceIndex() {
        return currentSentenceIndex;
    }

    /**
     * Set current sentence index within paragraph (used for mode switching)
     */
    public void setCurrentSentenceIndex(int sentenceIndex) {
        if (currentParagraph != null && sentenceIndex >= 0 && sentenceIndex < currentParagraph.getSentenceCount()) {
            this.currentSentenceIndex = sentenceIndex;
        }
    }

    /**
     * Get current character position for saving progress (relative position within paragraph)
     */
    public int getCurrentCharPosition() {
        if (currentParagraph == null) {
            return 0;
        }

        return currentParagraph.getSentenceStart(currentSentenceIndex);
    }

    /**
     * Set position by character offset within current paragraph
     */
    public void setCharacterPosition(int charPosition) {
        if (currentParagraph != null) {
            int sentenceIndex = currentParagraph.findSentenceIndexForPosition(charPosition);
            if (sentenceIndex >= 0) {
                currentSentenceIndex = sentenceIndex;
            }
        }
    }

    /**
     * Find which sentence contains the given character position
     */
    public int findSentenceIndexForCharPosition(int charPosition) {
        if (currentParagraph != null) {
            return currentParagraph.findSentenceIndexForPosition(charPosition);
        }
        return -1;
    }

    /**
     * Get the current paragraph sentences object for detailed sentence operations
     */
    public ParagraphSentences getCurrentParagraphSentences() {
        return currentParagraph;
    }

    /**
     * Get total sentences in current paragraph
     */
    public int getCurrentParagraphSentenceCount() {
        return currentParagraph != null ? currentParagraph.getSentenceCount() : 0;
    }

    /**
     * Get the full text of the current paragraph (for paragraph mode)
     */
    public String getCurrentParagraphText() {
        if (currentParagraph == null) {
            return "Error: No hay párrafo cargado";
        }

        String paragraph = currentParagraph.getParagraph();
        return paragraph != null ? paragraph : "Error: Párrafo no disponible";
    }

    /**
     * Check if at end of book
     */
    public boolean isAtEndOfBook() {
        return currentParagraphIndex >= totalParagraphs - 1 &&
               currentSentenceIndex >= getCurrentParagraphSentenceCount() - 1;
    }

    /**
     * Check if at beginning of book
     */
    public boolean isAtBeginningOfBook() {
        return currentParagraphIndex <= 0 && currentSentenceIndex <= 0;
    }

    private void clearBuffers() {
        previousParagraph = null;
        currentParagraph = null;
        nextParagraph = null;
        currentParagraphIndex = -1;
        currentSentenceIndex = 0;
    }

    private void loadParagraphsAroundIndex(int paragraphIndex) {
        executor.execute(() -> {
            try {
                loadParagraphsAroundIndexSync(paragraphIndex);
                if (listener != null) {
                    listener.onBufferLoaded();
                }
            } catch (Exception e) {
                if (listener != null) {
                    listener.onBufferError("Error loading paragraphs: " + e.getMessage());
                }
            }
        });
    }

    private void loadParagraphsAroundIndexWithCharPosition(int paragraphIndex, int charPosition) {
        executor.execute(() -> {
            try {
                loadParagraphsAroundIndexSync(paragraphIndex);

                // Set character position after paragraphs are loaded
                if (charPosition > 0 && currentParagraph != null) {
                    int sentenceIndex = currentParagraph.findSentenceIndexForPosition(charPosition);
                    if (sentenceIndex >= 0) {
                        currentSentenceIndex = sentenceIndex;
                    } else {
                        // Fallback to first sentence if character position is invalid
                        currentSentenceIndex = 0;
                    }
                } else {
                    currentSentenceIndex = 0;
                }

                if (listener != null) {
                    listener.onBufferLoaded();
                }
            } catch (Exception e) {
                if (listener != null) {
                    listener.onBufferError("Error loading paragraphs: " + e.getMessage());
                }
            }
        });
    }

    private void loadParagraphsAroundIndexSync(int paragraphIndex) throws IOException {
        currentParagraphIndex = paragraphIndex;

        // Load current paragraph
        String currentText = cacheManager.getSentence(currentBookId, paragraphIndex);
        currentParagraph = new ParagraphSentences(currentText, paragraphIndex);

        // Load previous paragraph if exists
        if (paragraphIndex > 0) {
            String prevText = cacheManager.getSentence(currentBookId, paragraphIndex - 1);
            previousParagraph = new ParagraphSentences(prevText, paragraphIndex - 1);
        } else {
            previousParagraph = null;
        }

        // Load next paragraph if exists
        if (paragraphIndex < totalParagraphs - 1) {
            String nextText = cacheManager.getSentence(currentBookId, paragraphIndex + 1);
            nextParagraph = new ParagraphSentences(nextText, paragraphIndex + 1);
        } else {
            nextParagraph = null;
        }

        // Ensure sentence index is valid for the loaded paragraph
        if (currentParagraph != null && currentSentenceIndex >= currentParagraph.getSentenceCount()) {
            currentSentenceIndex = Math.max(0, currentParagraph.getSentenceCount() - 1);
        }
    }

    private void loadNextParagraphAsync() {
        int nextIndex = currentParagraphIndex + 1;
        if (nextIndex < totalParagraphs) {
            executor.execute(() -> {
                try {
                    String nextText = cacheManager.getSentence(currentBookId, nextIndex);
                    nextParagraph = new ParagraphSentences(nextText, nextIndex);
                } catch (IOException e) {
                    // Silent fail for async preload
                    nextParagraph = null;
                }
            });
        } else {
            nextParagraph = null;
        }
    }

    private void loadPreviousParagraphAsync() {
        int prevIndex = currentParagraphIndex - 1;
        if (prevIndex >= 0) {
            executor.execute(() -> {
                try {
                    String prevText = cacheManager.getSentence(currentBookId, prevIndex);
                    previousParagraph = new ParagraphSentences(prevText, prevIndex);
                } catch (IOException e) {
                    // Silent fail for async preload
                    previousParagraph = null;
                }
            });
        } else {
            previousParagraph = null;
        }
    }

    public void shutdown() {
        if (executor != null && !executor.isShutdown()) {
            executor.shutdown();
        }
    }
}