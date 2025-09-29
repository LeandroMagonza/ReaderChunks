package com.leandromg.readerchunks;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class BufferManagerNew {
    private BookCacheManager cacheManager;
    private ExecutorService executor;
    private String currentBookId;
    private int totalParagraphs;

    // Simple 3-paragraph buffer
    private String previousParagraph = null;  // párrafo anterior
    private String currentParagraph = null;   // párrafo actual
    private String nextParagraph = null;      // párrafo siguiente

    // Tracking current paragraph index
    private int currentParagraphIndex = -1;

    private BufferLoadListener listener;

    public interface BufferLoadListener {
        void onBufferLoaded();
        void onBufferError(String error);
    }

    public BufferManagerNew(BookCacheManager cacheManager) {
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
     * Gets the paragraph at the specified index.
     * Loads paragraphs around this index if not already buffered.
     */
    public String getParagraph(int paragraphIndex) {
        // If the requested paragraph is already current, return it
        if (paragraphIndex == currentParagraphIndex && currentParagraph != null) {
            return currentParagraph;
        }

        // If it's the previous paragraph
        if (paragraphIndex == currentParagraphIndex - 1 && previousParagraph != null) {
            return previousParagraph;
        }

        // If it's the next paragraph
        if (paragraphIndex == currentParagraphIndex + 1 && nextParagraph != null) {
            return nextParagraph;
        }

        // Need to load paragraphs around this index
        try {
            loadParagraphsAroundIndexSync(paragraphIndex);
            if (paragraphIndex == currentParagraphIndex && currentParagraph != null) {
                return currentParagraph;
            }
        } catch (Exception e) {
            if (listener != null) {
                listener.onBufferError("Error loading paragraph: " + e.getMessage());
            }
        }

        return "Error: Contenido no disponible";
    }

    /**
     * Moves to the next paragraph. Preloads if necessary.
     */
    public void moveToNext() {
        if (currentParagraphIndex < totalParagraphs - 1) {
            // Shift buffers: current becomes previous, next becomes current
            previousParagraph = currentParagraph;
            currentParagraph = nextParagraph;
            currentParagraphIndex++;

            // Load new next paragraph asynchronously
            loadNextParagraphAsync();
        }
    }

    /**
     * Moves to the previous paragraph. Preloads if necessary.
     */
    public void moveToPrevious() {
        if (currentParagraphIndex > 0) {
            // Shift buffers: current becomes next, previous becomes current
            nextParagraph = currentParagraph;
            currentParagraph = previousParagraph;
            currentParagraphIndex--;

            // Load new previous paragraph asynchronously
            loadPreviousParagraphAsync();
        }
    }

    /**
     * Jumps to a specific paragraph index.
     */
    public void jumpToPosition(int paragraphIndex) {
        if (paragraphIndex >= 0 && paragraphIndex < totalParagraphs) {
            loadParagraphsAroundIndex(paragraphIndex);
        }
    }

    private void clearBuffers() {
        previousParagraph = null;
        currentParagraph = null;
        nextParagraph = null;
        currentParagraphIndex = -1;
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

    private void loadParagraphsAroundIndexSync(int paragraphIndex) throws IOException {
        currentParagraphIndex = paragraphIndex;

        // Load current paragraph
        currentParagraph = cacheManager.getSentence(currentBookId, paragraphIndex);

        // Load previous paragraph if exists
        if (paragraphIndex > 0) {
            previousParagraph = cacheManager.getSentence(currentBookId, paragraphIndex - 1);
        } else {
            previousParagraph = null;
        }

        // Load next paragraph if exists
        if (paragraphIndex < totalParagraphs - 1) {
            nextParagraph = cacheManager.getSentence(currentBookId, paragraphIndex + 1);
        } else {
            nextParagraph = null;
        }
    }

    private void loadNextParagraphAsync() {
        int nextIndex = currentParagraphIndex + 1;
        if (nextIndex < totalParagraphs) {
            executor.execute(() -> {
                try {
                    nextParagraph = cacheManager.getSentence(currentBookId, nextIndex);
                } catch (IOException e) {
                    // Silent fail for async preload
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
                    previousParagraph = cacheManager.getSentence(currentBookId, prevIndex);
                } catch (IOException e) {
                    // Silent fail for async preload
                }
            });
        } else {
            previousParagraph = null;
        }
    }

    public int getCurrentParagraphIndex() {
        return currentParagraphIndex;
    }

    public void shutdown() {
        if (executor != null && !executor.isShutdown()) {
            executor.shutdown();
        }
    }
}