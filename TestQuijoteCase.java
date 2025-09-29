// Test the specific Quijote case where "!a Mancha" should NOT be split

import java.util.ArrayList;
import java.util.List;

class DynamicSentenceSplitterQuijoteTest {
    private static final int DEFAULT_MAX_LENGTH = 150;

    private String paragraph;
    private int[] endPositions;
    private int maxLength;

    public DynamicSentenceSplitterQuijoteTest(String paragraph) {
        this(paragraph, DEFAULT_MAX_LENGTH);
    }

    public DynamicSentenceSplitterQuijoteTest(String paragraph, int maxLength) {
        this.paragraph = paragraph != null ? paragraph : "";
        this.maxLength = maxLength;
        this.endPositions = calculateEndPositions();
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

    /**
     * Find next sentence ending (.!?)
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
     * Must be followed by space or end of text
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

        // Next character must be a space
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

    public int[] getEndPositions() {
        return endPositions.clone();
    }
}

public class TestQuijoteCase {

    public static void main(String[] args) {
        System.out.println("=== Testing Quijote '!a Mancha' Case ===\n");

        // The specific problematic text from Quijote
        String quijoteText = "Después de haber puesto nombre, y tan a su gusto, a su caballo, quiso ponérsele a sí mismo, y en este pensamiento duró otros ocho días, y al cabo se vino a llamar don Quijote; de donde, como queda dicho, tomaron ocasión los autores desta verdadera historia que, sin duda, se debía de llamar Quijada, y no Quesada, como otros quisieron decir. Pero, acordándose que el valeroso Amadís no sólo se había contentado con llamarse Amadís a secas, sino que añadió el nombre de su reino y patria, por hacerla famosa, y se llamó Amadís de Gaula, así quiso, como buen caballero, añadir al suyo el nombre de la suya y llamarse don Quijote de !a Mancha, con que, a su parecer, declaraba muy al vivo su linaje y patria, y la honraba con tomar el sobrenombre della.";

        System.out.println("Original text:");
        System.out.println("\"" + quijoteText + "\"");
        System.out.println("Length: " + quijoteText.length() + " characters");

        // Look for the problematic part
        int manchaIndex = quijoteText.indexOf("!a Mancha");
        if (manchaIndex != -1) {
            System.out.println("\nFound '!a Mancha' at position: " + manchaIndex);
            int start = Math.max(0, manchaIndex - 20);
            int end = Math.min(quijoteText.length(), manchaIndex + 30);
            String context = quijoteText.substring(start, end);
            System.out.println("Context: \"" + context + "\"");
        }

        // Test with our algorithm
        DynamicSentenceSplitterQuijoteTest splitter = new DynamicSentenceSplitterQuijoteTest(quijoteText);

        System.out.println("\n=== ALGORITHM RESULT ===");
        System.out.println("Split into " + splitter.getSentenceCount() + " sentences:");

        boolean foundError = false;
        for (int i = 0; i < splitter.getSentenceCount(); i++) {
            String sentence = splitter.getSentence(i);
            System.out.println((i + 1) + ". [" + sentence.length() + " chars] \"" + sentence + "\"");

            // Check for the specific error
            if (sentence.startsWith("a Mancha") || sentence.contains("!a") || sentence.endsWith("!")) {
                System.out.println("   ❌ POTENTIAL ERROR: This sentence might be incorrectly split at '!'");
                foundError = true;
            }
        }

        System.out.println("\n=== VALIDATION ===");
        if (foundError) {
            System.out.println("❌ PROBLEM DETECTED: The algorithm is still splitting at '!' even when not followed by space");
        } else {
            System.out.println("✅ LOOKS GOOD: No obvious splitting errors detected");
        }

        // Manual validation of specific case
        System.out.println("\n=== MANUAL VALIDATION ===");
        for (int i = 0; i < quijoteText.length() - 1; i++) {
            char c = quijoteText.charAt(i);
            if (c == '!') {
                char next = quijoteText.charAt(i + 1);
                System.out.println("Found '!' at position " + i + ", next char: '" + next + "'");
                if (next != ' ') {
                    System.out.println("  -> This '!' should NOT be used for splitting (next char is not space)");
                } else {
                    System.out.println("  -> This '!' CAN be used for splitting (followed by space)");
                }
            }
        }
    }
}