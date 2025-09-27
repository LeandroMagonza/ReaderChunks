package com.leandromg.readerchunks;

import java.util.Date;

public class Book {
    private String id;              // Hash único del PDF
    private String title;           // Título del libro
    private String fileName;        // Nombre del archivo original
    private int totalSentences;     // Total de oraciones
    private int currentPosition;    // Posición actual de lectura
    private Date lastReadDate;      // Última fecha de lectura
    private long fileSizeBytes;     // Tamaño del archivo original
    private Date processedDate;     // Fecha de procesamiento

    public Book() {
        this.currentPosition = 0;
        this.lastReadDate = new Date();
        this.processedDate = new Date();
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
    public Date getLastReadDate() { return lastReadDate; }
    public long getFileSizeBytes() { return fileSizeBytes; }
    public Date getProcessedDate() { return processedDate; }

    // Setters
    public void setId(String id) { this.id = id; }
    public void setTitle(String title) { this.title = title; }
    public void setFileName(String fileName) { this.fileName = fileName; }
    public void setTotalSentences(int totalSentences) { this.totalSentences = totalSentences; }
    public void setCurrentPosition(int currentPosition) { this.currentPosition = currentPosition; }
    public void setLastReadDate(Date lastReadDate) { this.lastReadDate = lastReadDate; }
    public void setFileSizeBytes(long fileSizeBytes) { this.fileSizeBytes = fileSizeBytes; }
    public void setProcessedDate(Date processedDate) { this.processedDate = processedDate; }

    // Utility methods
    public double getProgressPercentage() {
        if (totalSentences == 0) return 0.0;
        return ((double) currentPosition / totalSentences) * 100.0;
    }

    public boolean isCompleted() {
        return currentPosition >= totalSentences - 1;
    }

    public boolean isStarted() {
        return currentPosition > 0;
    }

    public String getDisplayTitle() {
        return title != null && !title.trim().isEmpty() ? title : fileName;
    }

    @Override
    public String toString() {
        return String.format("Book{id='%s', title='%s', progress=%.1f%%}",
                           id, getDisplayTitle(), getProgressPercentage());
    }
}