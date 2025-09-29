package com.leandromg.readerchunks;

public class ParagraphSentences {
    private String paragraph;
    private int[] endPositions;
    private int paragraphIndex;

    public ParagraphSentences(String paragraph, int paragraphIndex) {
        this.paragraph = paragraph;
        this.paragraphIndex = paragraphIndex;

        if (paragraph != null && !paragraph.trim().isEmpty()) {
            DynamicSentenceSplitter splitter = new DynamicSentenceSplitter(paragraph);
            this.endPositions = splitter.getEndPositions();
        } else {
            this.endPositions = new int[0];
        }
    }

    /**
     * Get sentence by index within this paragraph
     */
    public String getSentence(int sentenceIndex) {
        if (sentenceIndex < 0 || sentenceIndex >= endPositions.length) {
            return null;
        }

        int start = (sentenceIndex == 0) ? 0 : endPositions[sentenceIndex - 1];
        int end = endPositions[sentenceIndex];

        return paragraph.substring(start, end).trim();
    }

    /**
     * Get total number of sentences in this paragraph
     */
    public int getSentenceCount() {
        return endPositions.length;
    }

    /**
     * Find which sentence contains the given character position
     */
    public int findSentenceIndexForPosition(int charPosition) {
        if (charPosition < 0 || charPosition >= paragraph.length()) {
            return -1;
        }

        for (int i = 0; i < endPositions.length; i++) {
            if (charPosition < endPositions[i]) {
                return i;
            }
        }

        return endPositions.length - 1; // Last sentence
    }

    /**
     * Get character position where sentence starts
     */
    public int getSentenceStart(int sentenceIndex) {
        if (sentenceIndex < 0 || sentenceIndex >= endPositions.length) {
            return -1;
        }

        return (sentenceIndex == 0) ? 0 : endPositions[sentenceIndex - 1];
    }

    /**
     * Get character position where sentence ends
     */
    public int getSentenceEnd(int sentenceIndex) {
        if (sentenceIndex < 0 || sentenceIndex >= endPositions.length) {
            return -1;
        }

        return endPositions[sentenceIndex];
    }

    /**
     * Get the original paragraph text
     */
    public String getParagraph() {
        return paragraph;
    }

    /**
     * Get the paragraph index in the book
     */
    public int getParagraphIndex() {
        return paragraphIndex;
    }

    /**
     * Get all end positions for debugging
     */
    public int[] getEndPositions() {
        return endPositions.clone();
    }

    /**
     * Check if this paragraph has any sentences
     */
    public boolean isEmpty() {
        return endPositions.length == 0;
    }
}