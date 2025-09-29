import java.util.ArrayList;
import java.util.List;

public class SentenceSegmenterPreservingTest {

    /**
     * Splits text by sentence-ending punctuation while preserving the original text exactly.
     * Only splits when punctuation is followed by whitespace or end of text (true sentence ending).
     * Does not add artificial periods - keeps the text as it was in the original.
     */
    private static List<String> splitPreservingPunctuation(String text) {
        List<String> sentences = new ArrayList<>();

        if (text == null || text.trim().isEmpty()) {
            return sentences;
        }

        int start = 0;

        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);

            // Check if this is sentence-ending punctuation
            if (c == '.' || c == '!' || c == '?') {
                // Look ahead to see if there are more punctuation marks
                int end = i + 1;
                while (end < text.length() && (text.charAt(end) == '.' || text.charAt(end) == '!' || text.charAt(end) == '?')) {
                    end++;
                }

                // Only split if the punctuation is followed by whitespace, end of text, or start of new sentence
                boolean isTrueSentenceEnd = false;
                if (end >= text.length()) {
                    // End of text
                    isTrueSentenceEnd = true;
                } else {
                    char nextChar = text.charAt(end);
                    // Split if followed by whitespace
                    if (Character.isWhitespace(nextChar)) {
                        isTrueSentenceEnd = true;
                    }
                    // Also split if followed by uppercase letter (new sentence)
                    else if (Character.isUpperCase(nextChar)) {
                        isTrueSentenceEnd = true;
                    }
                }

                if (isTrueSentenceEnd) {
                    // Extract the sentence including the punctuation
                    String sentence = text.substring(start, end).trim();
                    if (!sentence.isEmpty()) {
                        sentences.add(sentence);
                    }
                    start = end;
                }

                i = end - 1; // Adjust loop counter
            }
        }

        // Add remaining text if any (text without ending punctuation)
        if (start < text.length()) {
            String remaining = text.substring(start).trim();
            if (!remaining.isEmpty()) {
                sentences.add(remaining);
            }
        }

        return sentences;
    }

    public static void main(String[] args) {
        // Test the problematic text with !a
        System.out.println("=== TESTING PUNCTUATION PRESERVATION ===");
        String problematicText = "Por cuanto por parte de vos, Miguel de Cervantes, nos fue fecha relación que habíades compuesto un libro intitulado El ingenioso hidalgo de !a Mancha, compuesto por Miguel de Cervantes.";

        System.out.println("Original text:");
        System.out.println(problematicText);
        System.out.println();

        List<String> sentences = splitPreservingPunctuation(problematicText);

        System.out.println("Split into " + sentences.size() + " sentences:");
        for (int i = 0; i < sentences.size(); i++) {
            System.out.println((i + 1) + ". [" + sentences.get(i).length() + " chars] " + sentences.get(i));
        }
        System.out.println();

        // Check specifically if !a is preserved
        for (String sentence : sentences) {
            if (sentence.contains("!a")) {
                System.out.println("✅ FOUND PRESERVED !a: " + sentence);
            }
            if (sentence.contains(".a")) {
                System.out.println("❌ FOUND CORRUPTED .a: " + sentence);
            }
        }
        System.out.println();

        // Test with multiple punctuation
        System.out.println("=== TESTING MULTIPLE PUNCTUATION ===");
        String multiPunct = "¡Qué maravilla!!! ¿No te parece increíble?? Sí, definitivamente.";
        System.out.println("Text: " + multiPunct);

        List<String> multiSentences = splitPreservingPunctuation(multiPunct);
        for (int i = 0; i < multiSentences.size(); i++) {
            System.out.println((i + 1) + ". " + multiSentences.get(i));
        }
        System.out.println();

        // Test with no ending punctuation
        System.out.println("=== TESTING NO ENDING PUNCTUATION ===");
        String noPunct = "Este texto no tiene punto al final";
        System.out.println("Text: " + noPunct);

        List<String> noPunctSentences = splitPreservingPunctuation(noPunct);
        for (int i = 0; i < noPunctSentences.size(); i++) {
            System.out.println((i + 1) + ". " + noPunctSentences.get(i));
        }
    }
}