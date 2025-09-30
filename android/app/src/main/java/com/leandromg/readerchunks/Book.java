package com.leandromg.readerchunks;

import java.util.Date;

public class Book {
    private String id;              // Hash único del PDF
    private String title;           // Título del libro
    private String fileName;        // Nombre del archivo original
    private int totalSentences;     // Total de párrafos (se mantiene el nombre por compatibilidad)
    private int currentPosition;    // Párrafo actual de lectura (índice)
    private int currentCharPosition; // Posición del carácter dentro del párrafo actual
    private Date lastReadDate;      // Última fecha de lectura
    private long fileSizeBytes;     // Tamaño del archivo original
    private Date processedDate;     // Fecha de procesamiento
    private long totalCharacters;   // Total de caracteres en el libro (para cálculo preciso)
    private boolean isFullParagraphMode; // Modo de lectura: false = oraciones, true = párrafos

    public Book() {
        this.currentPosition = 0;
        this.currentCharPosition = 0;
        this.lastReadDate = new Date();
        this.processedDate = new Date();
        this.totalCharacters = 0;
        this.isFullParagraphMode = false; // Por defecto modo oraciones
    }

    public Book(String id, String title, String fileName, int totalSentences) {
        this();
        this.id = id;
        this.title = title;
        this.fileName = fileName;
        this.totalSentences = totalSentences;
    }

    // Getters
    public String getId() { return id; }
    public String getTitle() { return title; }
    public String getFileName() { return fileName; }
    public int getTotalSentences() { return totalSentences; }
    public int getCurrentPosition() { return currentPosition; }
    public int getCurrentCharPosition() { return currentCharPosition; }
    public Date getLastReadDate() { return lastReadDate; }
    public long getFileSizeBytes() { return fileSizeBytes; }
    public Date getProcessedDate() { return processedDate; }
    public long getTotalCharacters() { return totalCharacters; }
    public boolean isFullParagraphMode() { return isFullParagraphMode; }

    // Setters
    public void setId(String id) { this.id = id; }
    public void setTitle(String title) { this.title = title; }
    public void setFileName(String fileName) { this.fileName = fileName; }
    public void setTotalSentences(int totalSentences) { this.totalSentences = totalSentences; }
    public void setCurrentPosition(int currentPosition) { this.currentPosition = currentPosition; }
    public void setCurrentCharPosition(int currentCharPosition) { this.currentCharPosition = currentCharPosition; }
    public void setLastReadDate(Date lastReadDate) { this.lastReadDate = lastReadDate; }
    public void setFileSizeBytes(long fileSizeBytes) { this.fileSizeBytes = fileSizeBytes; }
    public void setProcessedDate(Date processedDate) { this.processedDate = processedDate; }
    public void setTotalCharacters(long totalCharacters) { this.totalCharacters = totalCharacters; }
    public void setFullParagraphMode(boolean isFullParagraphMode) { this.isFullParagraphMode = isFullParagraphMode; }

    // Utility methods
    public double getProgressPercentage() {
        if (totalSentences == 0) return 0.0;
        return ((double) currentPosition / totalSentences) * 100.0;
    }

    /**
     * Calculate precise progress percentage based on character position
     * Formula: (currentPosition / totalSentences * 100) + (charPosition / paragraphLength) * (100 / totalSentences)
     */
    public double getPreciseProgressPercentage(BookCacheManager cacheManager) {
        if (totalSentences == 0) {
            return 0.0;
        }

        // Base percentage: completed paragraphs
        double basePercentage = ((double) currentPosition / totalSentences) * 100.0;

        // If no character position or cache manager, return base percentage
        if (currentCharPosition <= 0 || cacheManager == null) {
            return basePercentage;
        }

        // Get current paragraph text to calculate progress within it
        try {
            String currentParagraphText = cacheManager.getSentence(id, currentPosition);
            if (currentParagraphText != null && currentParagraphText.length() > 0) {
                // Progress within current paragraph (0.0 to 1.0)
                double paragraphProgress = (double) currentCharPosition / currentParagraphText.length();

                // Each paragraph is worth (100 / totalSentences) percent
                double paragraphWeight = 100.0 / totalSentences;

                // Add fractional progress within current paragraph
                return basePercentage + (paragraphProgress * paragraphWeight);
            }
        } catch (Exception e) {
            // Return base percentage if can't read paragraph
        }

        return basePercentage;
    }

    public boolean isCompleted() {
        return currentPosition >= totalSentences - 1;
    }

    public boolean isStarted() {
        return currentPosition > 0 || currentCharPosition > 0;
    }

    public String getDisplayTitle() {
        return title != null && !title.trim().isEmpty() ? title : fileName;
    }

    public Date getMostRecentDate() {
        if (lastReadDate == null && processedDate == null) return null;
        if (lastReadDate == null) return processedDate;
        if (processedDate == null) return lastReadDate;
        return lastReadDate.after(processedDate) ? lastReadDate : processedDate;
    }

    @Override
    public String toString() {
        return String.format("Book{id='%s', title='%s', progress=%.1f%%}",
                           id, getDisplayTitle(), getProgressPercentage());
    }
}