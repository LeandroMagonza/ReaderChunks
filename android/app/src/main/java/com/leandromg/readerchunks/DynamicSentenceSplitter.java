package com.leandromg.readerchunks;

import android.util.Log;
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

        // Debug logging para ver qué límite recibe
        DebugLogger.d("SPLITTER_DEBUG", String.format(
            "Creating splitter with maxLength: %d, paragraph length: %d",
            maxLength, paragraph != null ? paragraph.length() : 0
        ));

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
                int originalLength = end - start;
                DebugLogger.d("SPLITTER_DEBUG", String.format(
                    "Sentence too long! Length: %d, MaxLength: %d, Text: '%.50s...'",
                    originalLength, maxLength, paragraph.substring(start, Math.min(start + 50, end))
                ));

                SplitResult splitResult = findBestSplitPointWithType(start, end);
                end = splitResult.position;
                endType = splitResult.type;

                DebugLogger.d("SPLITTER_DEBUG", String.format(
                    "Cut from %d to %d chars, EndType: %s", originalLength, end - start, endType
                ));
            } else if (isNaturalSentenceEnd) {
                DebugLogger.d("SPLITTER_DEBUG", String.format(
                    "Sentence OK: %d chars (limit: %d)", end - start, maxLength
                ));
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
     * New improved algorithm with punctuation priority:
     * 1. Strong breaks: ; : (higher priority)
     * 2. Medium breaks: , ' " ( ¡ ¿
     * 3. Space breaks (as fallback)
     * 4. Hard cut at maxLength (worst case)
     *
     * Always chooses the character closest to maxLength within each priority tier.
     */
    private SplitResult findBestSplitPointWithType(int start, int sentenceEnd) {
        int targetPosition = start + maxLength;
        int maxSearchEnd = Math.min(targetPosition, sentenceEnd);

        // Priority tiers - higher priority characters first
        char[][] priorityTiers = {
            {';', ':'}, // Tier 1: Strong punctuation breaks
            {',', '\'', '"', '(', '¡', '¿', ')', ']', '}', '!', '?', '-', '–', '—', '[', '{', '/', '\\', '*', '&'}, // Tier 2: Medium breaks
            {' '} // Tier 3: Space breaks (fallback)
        };

        SentenceEndType[] tierTypes = {
            SentenceEndType.SOFT_BREAK, // Strong breaks
            SentenceEndType.SOFT_BREAK, // Medium breaks
            SentenceEndType.CHARACTER_LIMIT // Space breaks
        };

        // Search each priority tier
        for (int tierIndex = 0; tierIndex < priorityTiers.length; tierIndex++) {
            char[] tierChars = priorityTiers[tierIndex];
            SentenceEndType tierType = tierTypes[tierIndex];

            // Find the closest valid character to target position within this tier
            int bestPosition = -1;

            // Search backwards from target position to find the closest valid split
            for (int position = maxSearchEnd - 1; position > start; position--) {
                char currentChar = paragraph.charAt(position);

                // Check if current character is in this tier
                for (char splitChar : tierChars) {
                    if (currentChar == splitChar) {
                        if (isValidSplitPosition(position)) {
                            // Handle special paired characters
                            if (isPairedCharacter(currentChar) && !isValidPairedSplit(position)) {
                                continue; // Skip if it breaks a pair
                            }

                            bestPosition = position;
                            break; // Found a valid position in this tier
                        }
                    }
                }

                if (bestPosition != -1) {
                    break; // Found the closest position in this tier
                }
            }

            // If we found a valid position in this tier, use it
            if (bestPosition != -1) {
                char splitChar = paragraph.charAt(bestPosition);
                String textPreview = paragraph.substring(Math.max(0, bestPosition - 10),
                    Math.min(paragraph.length(), bestPosition + 10));
                Log.d("SENTENCE_SPLIT", String.format("Split at tier %d, char '%c' at pos %d: ...%s...",
                    tierIndex + 1, splitChar, bestPosition, textPreview));
                return new SplitResult(bestPosition + 1, tierType);
            }
        }

        // No good split found anywhere, hard cut at maxLength
        String textPreview = paragraph.substring(Math.max(0, maxSearchEnd - 15),
            Math.min(paragraph.length(), maxSearchEnd + 5));
        DebugLogger.d("SENTENCE_SPLIT", String.format("Hard cut at pos %d: ...%s...", maxSearchEnd, textPreview));
        return new SplitResult(maxSearchEnd, SentenceEndType.CHARACTER_LIMIT);
    }

    /**
     * Check if character is part of a pair that should stay together
     */
    private boolean isPairedCharacter(char c) {
        return c == '"' || c == '\'' || c == '(' || c == ')' ||
               c == '[' || c == ']' || c == '{' || c == '}' ||
               c == '¡' || c == '¿' || c == '*';
    }

    /**
     * Check if splitting at this paired character position is valid
     * (i.e., we're not breaking up a pair)
     */
    private boolean isValidPairedSplit(int position) {
        char currentChar = paragraph.charAt(position);

        // For opening characters, prefer to keep the pair together if it's short
        if (currentChar == '(' || currentChar == '"' || currentChar == '\'' ||
            currentChar == '[' || currentChar == '{' || currentChar == '¡' || currentChar == '¿') {

            // Look ahead to find the closing character
            char closingChar = getClosingChar(currentChar);
            if (closingChar != '\0') {
                for (int i = position + 1; i < Math.min(position + 50, paragraph.length()); i++) {
                    if (paragraph.charAt(i) == closingChar) {
                        // If the pair is short (< 50 chars), try to keep it together
                        int pairLength = i - position;
                        return pairLength > 30; // Only split if pair is long enough
                    }
                }
            }
        }

        return true; // Default to allowing the split
    }

    /**
     * Get the closing character for a given opening character
     */
    private char getClosingChar(char openChar) {
        switch (openChar) {
            case '(': return ')';
            case '[': return ']';
            case '{': return '}';
            case '"': return '"';
            case '\'': return '\'';
            case '¡': return '!';
            case '¿': return '?';
            default: return '\0';
        }
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