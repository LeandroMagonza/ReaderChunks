package com.leandromg.readerchunks;

import java.util.ArrayList;
import java.util.List;

public class DynamicSentenceSplitter {

    private static final int DEFAULT_MAX_LENGTH = 150;

    private String paragraph;
    private int[] endPositions;
    private int maxLength;

    public DynamicSentenceSplitter(String paragraph) {
        this(paragraph, DEFAULT_MAX_LENGTH);
    }

    public DynamicSentenceSplitter(String paragraph, int maxLength) {
        this.paragraph = paragraph != null ? paragraph : "";
        this.maxLength = maxLength;
        this.endPositions = calculateEndPositions();
    }

    /**
     * Pre-calculates all sentence end positions for the paragraph.
     * Always splits by .!? first, then splits long sentences by :;, priority.
     */
    private int[] calculateEndPositions() {
        if (paragraph.trim().isEmpty()) {
            return new int[0];
        }

        List<Integer> positions = new ArrayList<>();
        int start = 0;

        while (start < paragraph.length()) {
            // Skip leading whitespace
            while (start < paragraph.length() && Character.isWhitespace(paragraph.charAt(start))) {
                start++;
            }

            if (start >= paragraph.length()) {
                break;
            }

            // Find next .!?
            int end = findNextSentenceEnd(start);

            // If too long, split by priority: : > ; > , > space
            if (end - start > maxLength) {
                end = findBestSplitPoint(start, end);
            }

            positions.add(end);
            start = end;
        }

        // Convert to array
        int[] result = new int[positions.size()];
        for (int i = 0; i < positions.size(); i++) {
            result[i] = positions.get(i);
        }
        return result;
    }

    /**
     * Find next sentence ending (.!?)
     * Only considers punctuation followed by space or end of paragraph as valid
     */
    private int findNextSentenceEnd(int start) {
        for (int i = start; i < paragraph.length(); i++) {
            char c = paragraph.charAt(i);
            if (c == '.' || c == '!' || c == '?') {
                // IMPORTANT: Check if this is a valid sentence ending
                if (isValidSentenceEnding(i)) {
                    return i + 1; // Include the punctuation
                }
            }
        }
        return paragraph.length(); // No valid sentence ending found
    }

    /**
     * Check if this punctuation mark is a valid sentence ending
     * Must be followed by space or end of paragraph
     */
    private boolean isValidSentenceEnding(int position) {
        // If at end of paragraph, always valid
        if (position >= paragraph.length() - 1) {
            return true;
        }

        // Next character must be a space
        char nextChar = paragraph.charAt(position + 1);
        return nextChar == ' ';
    }

    /**
     * Find best split point when sentence is too long.
     * Priority: : > ; > , > space
     * Only split if followed by whitespace or end of text (not inside words)
     */
    private int findBestSplitPoint(int start, int sentenceEnd) {
        int maxEnd = Math.min(start + maxLength, sentenceEnd);

        // Priority characters: : > ; > , > space
        char[] splitCharacters = {':', ';', ',', ' '};

        for (char splitChar : splitCharacters) {
            // Search backwards from maxEnd
            for (int position = maxEnd - 1; position > start + maxLength / 2; position--) {
                if (paragraph.charAt(position) == splitChar) {
                    // Check if followed by whitespace or end of text (not inside word)
                    if (isValidSplitPosition(position)) {
                        return position + 1; // Split after the character
                    }
                }
            }
        }

        // No good split found, cut at maxLength
        return Math.min(start + maxLength, sentenceEnd);
    }

    /**
     * Check if position is valid for splitting.
     * The split character must be followed by:
     * - A space (' ') that is NOT followed by another space
     * - End of paragraph
     *
     * This prevents cutting in the middle of:
     * - Multiple punctuation (HOLA!!!!)
     * - IP addresses (1.2.3.4)
     * - Abbreviations (Dr.Martinez)
     * - URLs (http://example.com)
     * - Multiple spaces (imp  rimir -> imp rimir)
     */
    private boolean isValidSplitPosition(int position) {
        char currentChar = paragraph.charAt(position);

        // If at end of paragraph, always valid
        if (position >= paragraph.length() - 1) {
            return true;
        }

        // For space characters, they must NOT be followed by another space
        if (currentChar == ' ') {
            char nextChar = paragraph.charAt(position + 1);
            return nextChar != ' '; // Valid only if next char is NOT a space
        }

        // For punctuation characters (:;,), they must be followed by a space
        char nextChar = paragraph.charAt(position + 1);
        return nextChar == ' ';
    }

    /**
     * Get sentence by index
     */
    public String getSentence(int index) {
        if (index < 0 || index >= endPositions.length) {
            return null;
        }

        int start = (index == 0) ? 0 : endPositions[index - 1];
        int end = endPositions[index];

        return paragraph.substring(start, end).trim();
    }

    /**
     * Get total number of sentences
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
     * Get all end positions for debugging
     */
    public int[] getEndPositions() {
        return endPositions.clone();
    }
}