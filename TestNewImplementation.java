// Test the new pre-calculated sentence positions implementation

import java.util.ArrayList;
import java.util.List;

// Simplified test classes without package declarations

class DynamicSentenceSplitterNew {
    private static final int DEFAULT_MAX_LENGTH = 150;

    private String paragraph;
    private int[] endPositions;
    private int maxLength;

    public DynamicSentenceSplitterNew(String paragraph, int maxLength) {
        this.paragraph = paragraph != null ? paragraph : "";
        this.maxLength = maxLength;
        this.endPositions = calculateEndPositions();
    }

    public DynamicSentenceSplitterNew(String paragraph) {
        this(paragraph, DEFAULT_MAX_LENGTH);
    }

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

    private int findNextSentenceEnd(int start) {
        for (int i = start; i < paragraph.length(); i++) {
            char c = paragraph.charAt(i);
            if (c == '.' || c == '!' || c == '?') {
                return i + 1; // Include the punctuation
            }
        }
        return paragraph.length(); // No sentence ending found
    }

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

    private boolean isValidSplitPosition(int position) {
        // If at end of paragraph, always valid
        if (position >= paragraph.length() - 1) {
            return true;
        }

        // Check next character is whitespace
        char nextChar = paragraph.charAt(position + 1);
        return Character.isWhitespace(nextChar);
    }

    public String getSentence(int index) {
        if (index < 0 || index >= endPositions.length) {
            return null;
        }

        int start = (index == 0) ? 0 : endPositions[index - 1];
        int end = endPositions[index];

        return paragraph.substring(start, end).trim();
    }

    public int getSentenceCount() {
        return endPositions.length;
    }

    public int[] getEndPositions() {
        return endPositions.clone();
    }
}

public class TestNewImplementation {

    public static void main(String[] args) {
        System.out.println("=== Testing New Pre-calculated Implementation ===\n");

        // Test 1: Always split by .!? first
        testSentenceEndingSplits();

        // Test 2: Split character validation (not inside words)
        testSplitCharacterValidation();

        // Test 3: Priority-based splitting : > ; > , > space
        testPrioritySplitting();

        // Test 4: Navigation and position tracking
        testPositionTracking();
    }

    private static void testSentenceEndingSplits() {
        System.out.println("1. Testing Always Split by .!? First:");
        System.out.println("=====================================");

        String paragraph = "First sentence. Second sentence! Third sentence? All should be separate regardless of length.";
        DynamicSentenceSplitterNew splitter = new DynamicSentenceSplitterNew(paragraph);

        System.out.println("Paragraph: \"" + paragraph + "\"");
        System.out.println("Found " + splitter.getSentenceCount() + " sentences:");

        for (int i = 0; i < splitter.getSentenceCount(); i++) {
            String sentence = splitter.getSentence(i);
            System.out.println((i + 1) + ". [" + sentence.length() + " chars] \"" + sentence + "\"");
        }

        System.out.println("End positions: " + java.util.Arrays.toString(splitter.getEndPositions()));
        System.out.println();
    }

    private static void testSplitCharacterValidation() {
        System.out.println("2. Testing Split Character Validation (not inside words):");
        System.out.println("=========================================================");

        // Test with : inside URL vs : followed by space
        String paragraph = "Visit http://example.com for details: this should split here, not at the URL.";
        DynamicSentenceSplitterNew splitter = new DynamicSentenceSplitterNew(paragraph, 50); // Force split

        System.out.println("Paragraph: \"" + paragraph + "\"");
        System.out.println("With 50-char limit (should NOT split at http:// but should split at 'details:'):");

        for (int i = 0; i < splitter.getSentenceCount(); i++) {
            String sentence = splitter.getSentence(i);
            System.out.println((i + 1) + ". \"" + sentence + "\"");
        }
        System.out.println();
    }

    private static void testPrioritySplitting() {
        System.out.println("3. Testing Priority-based Splitting (: > ; > , > space):");
        System.out.println("========================================================");

        String longSentence = "This is a very long sentence that contains various punctuation marks: colons first priority; semicolons second priority, commas third priority and spaces last priority for splitting when text exceeds the character limit.";
        DynamicSentenceSplitterNew splitter = new DynamicSentenceSplitterNew(longSentence, 80); // Force splits

        System.out.println("Long sentence (" + longSentence.length() + " chars):");
        System.out.println("\"" + longSentence + "\"");
        System.out.println("\nSplit with 80-char limit (should prioritize : > ; > , > space):");

        for (int i = 0; i < splitter.getSentenceCount(); i++) {
            String sentence = splitter.getSentence(i);
            System.out.println((i + 1) + ". [" + sentence.length() + " chars] \"" + sentence + "\"");
        }
        System.out.println();
    }

    private static void testPositionTracking() {
        System.out.println("4. Testing Position Tracking:");
        System.out.println("=============================");

        String paragraph = "Short first. This is a longer second sentence that might need splitting! Final sentence.";
        DynamicSentenceSplitterNew splitter = new DynamicSentenceSplitterNew(paragraph, 60);

        System.out.println("Paragraph: \"" + paragraph + "\"");
        System.out.println("End positions: " + java.util.Arrays.toString(splitter.getEndPositions()));
        System.out.println();

        System.out.println("Position tracking demonstration:");
        System.out.println("Sentence index -> [start-end] -> content");

        for (int i = 0; i < splitter.getSentenceCount(); i++) {
            int start = (i == 0) ? 0 : splitter.getEndPositions()[i - 1];
            int end = splitter.getEndPositions()[i];
            String sentence = splitter.getSentence(i);

            System.out.println("Sentence " + i + " -> [" + start + "-" + end + "] -> \"" + sentence + "\"");
        }

        System.out.println("\nThis enables:");
        System.out.println("- Forward navigation: just increment sentence index");
        System.out.println("- Backward navigation: just decrement sentence index");
        System.out.println("- Jump to last sentence of paragraph: use last index");
        System.out.println("- Character position saving: use sentence start position");
        System.out.println();
    }
}