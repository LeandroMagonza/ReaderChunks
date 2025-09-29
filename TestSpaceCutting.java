// Test the space cutting issue (imp  rimir -> imp rimir)

import java.util.ArrayList;
import java.util.List;

class SpaceCuttingSplitter {
    private String paragraph;
    private int[] endPositions;
    private int maxLength;

    public SpaceCuttingSplitter(String paragraph, int maxLength) {
        this.paragraph = paragraph != null ? paragraph : "";
        this.maxLength = maxLength;
        this.endPositions = calculateEndPositions();
    }

    public SpaceCuttingSplitter(String paragraph) {
        this(paragraph, 30); // Short length to force cuts
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

            if (end - start > maxLength) {
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

    private int findNextSentenceEnd(int start) {
        for (int i = start; i < paragraph.length(); i++) {
            char c = paragraph.charAt(i);
            if (c == '.' || c == '!' || c == '?') {
                if (isValidSentenceEnding(i)) {
                    return i + 1;
                }
            }
        }
        return paragraph.length();
    }

    private boolean isValidSentenceEnding(int position) {
        if (position >= paragraph.length() - 1) {
            return true;
        }
        char nextChar = paragraph.charAt(position + 1);
        return nextChar == ' ';
    }

    private int findBestSplitPoint(int start, int sentenceEnd) {
        int maxEnd = Math.min(start + maxLength, sentenceEnd);

        char[] splitCharacters = {':', ';', ',', ' '};

        for (char splitChar : splitCharacters) {
            for (int position = maxEnd - 1; position > start + maxLength / 2; position--) {
                if (paragraph.charAt(position) == splitChar) {
                    if (isValidSplitPosition(position)) {
                        return position + 1;
                    }
                }
            }
        }

        return Math.min(start + maxLength, sentenceEnd);
    }

    private boolean isValidSplitPosition(int position) {
        char currentChar = paragraph.charAt(position);

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

public class TestSpaceCutting {

    public static void main(String[] args) {
        System.out.println("=== Testing Space Cutting Issue ===\n");

        // Test cases with multiple spaces that should NOT be cut
        String[] testCases = {
            "poder imprimir y previlegio por el tiempo",  // Normal spaces - should cut normally
            "poder  imprimir  y  previlegio  por  el  tiempo",  // Double spaces - should NOT cut in double spaces
            "EL REY   Por cuanto por parte de vos",  // Triple spaces - should NOT cut in triple spaces
            "texto con    muchos    espacios    seguidos",  // Multiple spaces - should find single spaces only
            "palabra1,palabra2 palabra3   palabra4"  // Mixed - should prefer comma, then single space
        };

        for (int t = 0; t < testCases.length; t++) {
            String testCase = testCases[t];
            System.out.println("Test " + (t + 1) + ": \"" + testCase + "\"");
            System.out.println("Length: " + testCase.length() + " chars");

            SpaceCuttingSplitter splitter = new SpaceCuttingSplitter(testCase);
            System.out.println("Split into " + splitter.getSentenceCount() + " sentences:");

            boolean hasInvalidCut = false;
            for (int i = 0; i < splitter.getSentenceCount(); i++) {
                String sentence = splitter.getSentence(i);
                System.out.println("  " + (i + 1) + ". [" + sentence.length() + " chars] \"" + sentence + "\"");

                // Check for invalid cuts (broken words)
                if (sentence.length() > 0) {
                    char lastChar = sentence.charAt(sentence.length() - 1);
                    if (Character.isLetter(lastChar)) {
                        // Check if next sentence starts with a letter (might be a broken word)
                        if (i < splitter.getSentenceCount() - 1) {
                            String nextSentence = splitter.getSentence(i + 1);
                            if (nextSentence.length() > 0 && Character.isLetter(nextSentence.charAt(0))) {
                                System.out.println("    ❌ POSSIBLE BROKEN WORD: '" + sentence + "' + '" + nextSentence + "'");
                                hasInvalidCut = true;
                            }
                        }
                    }
                }
            }

            if (!hasInvalidCut) {
                System.out.println("  ✅ NO BROKEN WORDS DETECTED");
            }
            System.out.println();
        }

        // Test the specific Quijote case
        System.out.println("=== Specific Quijote Text ===");
        String quijoteFragment = "facultad para le poder imprimir, y previlegio por el tiempo que fuésemos servidos, o como la nuestra merced fuese";
        System.out.println("Text: \"" + quijoteFragment + "\"");
        System.out.println("Length: " + quijoteFragment.length() + " chars");

        SpaceCuttingSplitter quijoteSplitter = new SpaceCuttingSplitter(quijoteFragment, 50);
        System.out.println("Split with 50-char limit:");

        for (int i = 0; i < quijoteSplitter.getSentenceCount(); i++) {
            String sentence = quijoteSplitter.getSentence(i);
            System.out.println("  " + (i + 1) + ". \"" + sentence + "\"");
        }
    }
}