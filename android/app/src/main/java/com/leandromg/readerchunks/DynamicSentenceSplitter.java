package com.leandromg.readerchunks;

import java.util.ArrayList;
import java.util.List;

public class DynamicSentenceSplitter {

    private static final int DEFAULT_MAX_LENGTH = 150;

    private String paragraph;
    private int[] endPositions;
    private SentenceEndType[] endTypes;
    private int maxLength;

    public DynamicSentenceSplitter(String paragraph) {
        this(paragraph, DEFAULT_MAX_LENGTH);
    }

    public DynamicSentenceSplitter(String paragraph, int maxLength) {
        this.paragraph = paragraph != null ? paragraph : "";
        this.maxLength = maxLength;
        calculateEndPositionsAndTypes();
    }

    /**
     * Pre-calculates all sentence end positions and their types for the paragraph.
     * Always splits by .!? first, then splits long sentences by :;, priority.
     */
    private void calculateEndPositionsAndTypes() {
        if (paragraph.trim().isEmpty()) {
            this.endPositions = new int[0];
            this.endTypes = new SentenceEndType[0];
            return;
        }

        List<Integer> positions = new ArrayList<>();
        List<SentenceEndType> types = new ArrayList<>();
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
            SentenceEndType endType;

            // Determine if it's a natural sentence end
            boolean isNaturalSentenceEnd = end < paragraph.length() &&
                (paragraph.charAt(end - 1) == '.' || paragraph.charAt(end - 1) == '!' || paragraph.charAt(end - 1) == '?');

            // If too long, split by priority: : > ; > , > space
            if (end - start > maxLength) {
                SplitResult splitResult = findBestSplitPointWithType(start, end);
                end = splitResult.position;
                endType = splitResult.type;
            } else if (isNaturalSentenceEnd) {
                // Check if it's end of paragraph
                if (end >= paragraph.length()) {
                    endType = SentenceEndType.PARAGRAPH_END;
                } else {
                    endType = SentenceEndType.SENTENCE_END;
                }
            } else {
                // Reached end of paragraph without punctuation
                endType = SentenceEndType.PARAGRAPH_END;
            }

            positions.add(end);
            types.add(endType);
            start = end;
        }

        // Convert to arrays
        this.endPositions = new int[positions.size()];
        this.endTypes = new SentenceEndType[types.size()];
        for (int i = 0; i < positions.size(); i++) {
            this.endPositions[i] = positions.get(i);
            this.endTypes[i] = types.get(i);
        }
    }

    /**
     * Helper class to hold split results with type information
     */
    private static class SplitResult {
        final int position;
        final SentenceEndType type;

        SplitResult(int position, SentenceEndType type) {
            this.position = position;
            this.type = type;
        }
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
     * Find best split point when sentence is too long, returning position and type.
     * Priority: : > ; > , > space
     * Only split if followed by whitespace or end of text (not inside words)
     */
    private SplitResult findBestSplitPointWithType(int start, int sentenceEnd) {
        int maxEnd = Math.min(start + maxLength, sentenceEnd);

        // Priority characters with their corresponding types
        char[] splitCharacters = {':', ';', ','};

        // First try punctuation splits (soft breaks)
        for (char splitChar : splitCharacters) {
            // Search backwards from maxEnd
            for (int position = maxEnd - 1; position > start + maxLength / 2; position--) {
                if (paragraph.charAt(position) == splitChar) {
                    // Check if followed by whitespace or end of text (not inside word)
                    if (isValidSplitPosition(position)) {
                        return new SplitResult(position + 1, SentenceEndType.SOFT_BREAK);
                    }
                }
            }
        }

        // Try space splits (character limit)
        for (int position = maxEnd - 1; position > start + maxLength / 2; position--) {
            if (paragraph.charAt(position) == ' ') {
                if (isValidSplitPosition(position)) {
                    return new SplitResult(position + 1, SentenceEndType.CHARACTER_LIMIT);
                }
            }
        }

        // No good split found, cut at maxLength (character limit)
        return new SplitResult(Math.min(start + maxLength, sentenceEnd), SentenceEndType.CHARACTER_LIMIT);
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
     * Get the type of sentence ending for a given sentence index
     */
    public SentenceEndType getSentenceEndType(int sentenceIndex) {
        if (sentenceIndex < 0 || sentenceIndex >= endTypes.length) {
            return SentenceEndType.CHARACTER_LIMIT; // Default fallback
        }
        return endTypes[sentenceIndex];
    }

    /**
     * Get all end positions for debugging
     */
    public int[] getEndPositions() {
        return endPositions.clone();
    }

    /**
     * Get all end types for debugging
     */
    public SentenceEndType[] getEndTypes() {
        return endTypes.clone();
    }
}