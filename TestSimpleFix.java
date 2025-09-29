// Simple test to verify the fix for sentence ending validation

import java.util.ArrayList;
import java.util.List;

class SimpleDynamicSplitter {
    private String paragraph;
    private int[] endPositions;

    public SimpleDynamicSplitter(String paragraph) {
        this.paragraph = paragraph != null ? paragraph : "";
        this.endPositions = calculateEndPositions();
    }

    private int[] calculateEndPositions() {
        if (paragraph.trim().isEmpty()) {
            return new int[0];
        }

        List<Integer> positions = new ArrayList<>();
        int start = 0;

        while (start < paragraph.length()) {
            while (start < paragraph.length() && Character.isWhitespace(paragraph.charAt(start))) {
                start++;
            }

            if (start >= paragraph.length()) {
                break;
            }

            int end = findNextSentenceEnd(start);

            if (end - start > 150) {
                end = findBestSplitPoint(start, end);
            }

            positions.add(end);
            start = end;
        }

        int[] result = new int[positions.size()];
        for (int i = 0; i < positions.size(); i++) {
            result[i] = positions.get(i);
        }
        return result;
    }

    /**
     * Find next sentence ending (.!?) - WITH VALIDATION
     */
    private int findNextSentenceEnd(int start) {
        for (int i = start; i < paragraph.length(); i++) {
            char c = paragraph.charAt(i);
            if (c == '.' || c == '!' || c == '?') {
                // VALIDATION: Must be followed by space or end of text
                if (isValidSentenceEnding(i)) {
                    return i + 1;
                }
                // Continue searching if not valid
            }
        }
        return paragraph.length();
    }

    private boolean isValidSentenceEnding(int position) {
        if (position >= paragraph.length() - 1) {
            return true; // End of text
        }
        char nextChar = paragraph.charAt(position + 1);
        return nextChar == ' ';
    }

    private int findBestSplitPoint(int start, int sentenceEnd) {
        int maxEnd = Math.min(start + 150, sentenceEnd);
        char[] splitCharacters = {':', ';', ',', ' '};

        for (char splitChar : splitCharacters) {
            for (int position = maxEnd - 1; position > start + 75; position--) {
                if (paragraph.charAt(position) == splitChar) {
                    if (isValidSplitPosition(position)) {
                        return position + 1;
                    }
                }
            }
        }

        return Math.min(start + 150, sentenceEnd);
    }

    private boolean isValidSplitPosition(int position) {
        if (position >= paragraph.length() - 1) {
            return true;
        }
        char nextChar = paragraph.charAt(position + 1);
        return nextChar == ' ';
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
}

public class TestSimpleFix {

    public static void main(String[] args) {
        System.out.println("=== Testing Simple Fix ===\n");

        // Test cases that should NOT split
        String[] testCases = {
            "don Quijote de !a Mancha es famoso",  // Should NOT split at !
            "Visit http://example.com for info",   // Should NOT split at :
            "HOLA!!!! Como estas",                // Should NOT split at first 3 !
            "IP 1.2.3.4.5 address",              // Should NOT split at any .
            "Dr.Martinez said hello"              // Should NOT split at .
        };

        for (int t = 0; t < testCases.length; t++) {
            String testCase = testCases[t];
            System.out.println("Test " + (t + 1) + ": \"" + testCase + "\"");

            SimpleDynamicSplitter splitter = new SimpleDynamicSplitter(testCase);
            System.out.println("Split into " + splitter.getSentenceCount() + " sentences:");

            for (int i = 0; i < splitter.getSentenceCount(); i++) {
                String sentence = splitter.getSentence(i);
                System.out.println("  " + (i + 1) + ". \"" + sentence + "\"");
            }

            // Validate
            if (splitter.getSentenceCount() == 1) {
                System.out.println("  ✅ CORRECT: No invalid splits detected");
            } else {
                System.out.println("  ❌ PROBLEM: Text was split when it shouldn't be");
            }
            System.out.println();
        }

        // Test cases that SHOULD split
        System.out.println("=== Cases that SHOULD split ===");
        String[] shouldSplit = {
            "First sentence. Second sentence",      // Should split at .
            "Question? Answer here",               // Should split at ?
            "Exclamation! More text",             // Should split at !
        };

        for (int t = 0; t < shouldSplit.length; t++) {
            String testCase = shouldSplit[t];
            System.out.println("Test " + (t + 1) + ": \"" + testCase + "\"");

            SimpleDynamicSplitter splitter = new SimpleDynamicSplitter(testCase);
            System.out.println("Split into " + splitter.getSentenceCount() + " sentences:");

            for (int i = 0; i < splitter.getSentenceCount(); i++) {
                String sentence = splitter.getSentence(i);
                System.out.println("  " + (i + 1) + ". \"" + sentence + "\"");
            }

            if (splitter.getSentenceCount() > 1) {
                System.out.println("  ✅ CORRECT: Proper splits detected");
            } else {
                System.out.println("  ❌ PROBLEM: Should have split but didn't");
            }
            System.out.println();
        }
    }
}