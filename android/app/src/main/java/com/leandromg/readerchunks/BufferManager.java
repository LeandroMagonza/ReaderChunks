package com.leandromg.readerchunks;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class BufferManager {
    private static final int WINDOW_SIZE = 50;          // Ventana principal
    private static final int BUFFER_SIZE = 25;          // Buffer anterior/siguiente
    private static final double PRELOAD_THRESHOLD = 0.7; // Cargar al 70%

    private BookCacheManager cacheManager;
    private ExecutorService executor;
    private String currentBookId;
    private int totalSentences;

    // Buffers en memoria
    private List<String> previousBuffer;    // [pos-25, pos-1]
    private List<String> currentWindow;     // [pos, pos+49]
    private List<String> nextBuffer;        // [pos+50, pos+74]

    // Posiciones de los buffers
    private int previousBufferStart = -1;
    private int currentWindowStart = -1;
    private int nextBufferStart = -1;

    private BufferLoadListener listener;

    public interface BufferLoadListener {
        void onBufferLoaded();
        void onBufferError(String error);
    }

    public BufferManager(BookCacheManager cacheManager) {
        this.cacheManager = cacheManager;
        this.executor = Executors.newSingleThreadExecutor();
        this.previousBuffer = new ArrayList<>();
        this.currentWindow = new ArrayList<>();
        this.nextBuffer = new ArrayList<>();
    }

    public void initialize(String bookId, int totalSentences, int startPosition, BufferLoadListener listener) {
        this.currentBookId = bookId;
        this.totalSentences = totalSentences;
        this.listener = listener;

        // Limpiar buffers existentes
        clearBuffers();

        // Cargar buffers iniciales
        loadBuffersForPosition(startPosition);
    }

    public String getSentence(int position) {
        // Verificar en ventana actual
        if (isInCurrentWindow(position)) {
            int index = position - currentWindowStart;
            if (index >= 0 && index < currentWindow.size()) {
                checkPreloadTrigger(position);
                return currentWindow.get(index);
            }
        }

        // Verificar en buffer anterior
        if (isInPreviousBuffer(position)) {
            int index = position - previousBufferStart;
            if (index >= 0 && index < previousBuffer.size()) {
                return previousBuffer.get(index);
            }
        }

        // Verificar en buffer siguiente
        if (isInNextBuffer(position)) {
            int index = position - nextBufferStart;
            if (index >= 0 && index < nextBuffer.size()) {
                return nextBuffer.get(index);
            }
        }

        // No está en buffers - cargar síncronamente
        try {
            loadBuffersForPosition(position);
            if (isInCurrentWindow(position)) {
                int index = position - currentWindowStart;
                return currentWindow.get(index);
            }
        } catch (Exception e) {
            return "Error cargando oración: " + e.getMessage();
        }

        return "Oración no disponible";
    }

    private void checkPreloadTrigger(int currentPosition) {
        if (currentWindowStart == -1) return;

        int relativePosition = currentPosition - currentWindowStart;
        double threshold = WINDOW_SIZE * PRELOAD_THRESHOLD;

        // Trigger para cargar siguiente
        if (relativePosition >= threshold && needsNextBuffer(currentPosition)) {
            preloadNext(currentPosition);
        }

        // Trigger para cargar anterior
        if (relativePosition <= (WINDOW_SIZE * (1 - PRELOAD_THRESHOLD)) && needsPreviousBuffer(currentPosition)) {
            preloadPrevious(currentPosition);
        }
    }

    private boolean needsNextBuffer(int position) {
        int expectedNextStart = currentWindowStart + WINDOW_SIZE;
        return nextBufferStart != expectedNextStart && expectedNextStart < totalSentences;
    }

    private boolean needsPreviousBuffer(int position) {
        int expectedPrevStart = Math.max(0, currentWindowStart - BUFFER_SIZE);
        return previousBufferStart != expectedPrevStart && currentWindowStart > 0;
    }

    private void preloadNext(int currentPosition) {
        executor.execute(() -> {
            try {
                int newNextStart = currentWindowStart + WINDOW_SIZE;
                if (newNextStart < totalSentences) {
                    List<String> newNextBuffer = cacheManager.getSentences(
                        currentBookId, newNextStart, BUFFER_SIZE);

                    synchronized(this) {
                        nextBuffer = newNextBuffer;
                        nextBufferStart = newNextStart;
                    }

                    // Limpiar buffer anterior si está muy atrás
                    cleanOldBuffers(currentPosition);
                }
            } catch (IOException e) {
                if (listener != null) {
                    listener.onBufferError("Error precargando: " + e.getMessage());
                }
            }
        });
    }

    private void preloadPrevious(int currentPosition) {
        executor.execute(() -> {
            try {
                int newPrevStart = Math.max(0, currentWindowStart - BUFFER_SIZE);
                if (newPrevStart < currentWindowStart) {
                    List<String> newPrevBuffer = cacheManager.getSentences(
                        currentBookId, newPrevStart, BUFFER_SIZE);

                    synchronized(this) {
                        previousBuffer = newPrevBuffer;
                        previousBufferStart = newPrevStart;
                    }
                }
            } catch (IOException e) {
                if (listener != null) {
                    listener.onBufferError("Error precargando anterior: " + e.getMessage());
                }
            }
        });
    }

    private void cleanOldBuffers(int currentPosition) {
        // Limpiar buffer anterior si el usuario avanzó mucho
        if (previousBufferStart != -1 && currentPosition > previousBufferStart + BUFFER_SIZE + WINDOW_SIZE) {
            synchronized(this) {
                previousBuffer.clear();
                previousBufferStart = -1;
            }
        }
    }

    public void jumpToPosition(int position) {
        // Para saltos grandes, recargar todos los buffers
        clearBuffers();
        loadBuffersForPosition(position);
    }

    private void loadBuffersForPosition(int position) {
        executor.execute(() -> {
            try {
                // Calcular posiciones óptimas
                int windowStart = Math.max(0, position);
                int prevStart = Math.max(0, windowStart - BUFFER_SIZE);
                int nextStart = windowStart + WINDOW_SIZE;

                // Cargar ventana actual (prioritario)
                List<String> window = cacheManager.getSentences(currentBookId, windowStart, WINDOW_SIZE);

                // Cargar buffer anterior si es posible
                List<String> prev = new ArrayList<>();
                if (prevStart < windowStart) {
                    prev = cacheManager.getSentences(currentBookId, prevStart,
                                                   Math.min(BUFFER_SIZE, windowStart - prevStart));
                }

                // Cargar buffer siguiente si es posible
                List<String> next = new ArrayList<>();
                if (nextStart < totalSentences) {
                    next = cacheManager.getSentences(currentBookId, nextStart,
                                                   Math.min(BUFFER_SIZE, totalSentences - nextStart));
                }

                // Actualizar buffers de forma atómica
                synchronized(this) {
                    currentWindow = window;
                    currentWindowStart = windowStart;

                    previousBuffer = prev;
                    previousBufferStart = prevStart;

                    nextBuffer = next;
                    nextBufferStart = nextStart;
                }

                if (listener != null) {
                    listener.onBufferLoaded();
                }

            } catch (IOException e) {
                if (listener != null) {
                    listener.onBufferError("Error cargando buffers: " + e.getMessage());
                }
            }
        });
    }

    private boolean isInCurrentWindow(int position) {
        return currentWindowStart != -1 &&
               position >= currentWindowStart &&
               position < currentWindowStart + currentWindow.size();
    }

    private boolean isInPreviousBuffer(int position) {
        return previousBufferStart != -1 &&
               position >= previousBufferStart &&
               position < previousBufferStart + previousBuffer.size();
    }

    private boolean isInNextBuffer(int position) {
        return nextBufferStart != -1 &&
               position >= nextBufferStart &&
               position < nextBufferStart + nextBuffer.size();
    }

    private void clearBuffers() {
        synchronized(this) {
            previousBuffer.clear();
            currentWindow.clear();
            nextBuffer.clear();
            previousBufferStart = -1;
            currentWindowStart = -1;
            nextBufferStart = -1;
        }
    }

    public void shutdown() {
        if (executor != null) {
            executor.shutdown();
        }
    }

    // Debug methods
    public String getBufferStatus() {
        return String.format("Buffers: Prev[%d-%d] Current[%d-%d] Next[%d-%d]",
                           previousBufferStart, previousBufferStart + previousBuffer.size() - 1,
                           currentWindowStart, currentWindowStart + currentWindow.size() - 1,
                           nextBufferStart, nextBufferStart + nextBuffer.size() - 1);
    }
}