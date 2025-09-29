import java.util.ArrayList;
import java.util.List;

public class DynamicSentenceSplitterTest {

    private static final int DEFAULT_MAX_LENGTH = 150;

    public static List<String> splitIfNeeded(String sentence, int maxLength) {
        List<String> parts = new ArrayList<>();

        if (sentence == null || sentence.trim().isEmpty()) {
            return parts;
        }

        // Don't split [BREAK] markers
        if ("[BREAK]".equals(sentence)) {
            parts.add(sentence);
            return parts;
        }

        // If sentence is within limit, return as-is
        if (sentence.length() <= maxLength) {
            parts.add(sentence);
            return parts;
        }

        // Split long sentence
        return splitLongSentence(sentence, maxLength);
    }

    public static List<String> splitIfNeeded(String sentence) {
        return splitIfNeeded(sentence, DEFAULT_MAX_LENGTH);
    }

    private static List<String> splitLongSentence(String sentence, int maxLength) {
        List<String> parts = new ArrayList<>();
        String remaining = sentence;

        while (remaining.length() > maxLength) {
            int splitPoint = findBestSplitPoint(remaining, maxLength);

            if (splitPoint == -1) {
                // No good split point found, force split at maxLength
                splitPoint = maxLength;
            }

            String part = remaining.substring(0, splitPoint).trim();
            if (!part.isEmpty()) {
                parts.add(part);
            }

            remaining = remaining.substring(splitPoint).trim();
        }

        // Add remaining part
        if (!remaining.isEmpty()) {
            parts.add(remaining);
        }

        return parts;
    }

    private static int findBestSplitPoint(String text, int maxLength) {
        if (maxLength >= text.length()) {
            return text.length();
        }

        int bestSplitPoint = -1;
        int bestPriority = -1; // Higher number = better priority

        // Single loop from maxLength backwards to find best split point
        for (int i = Math.min(maxLength, text.length() - 1); i >= maxLength / 2; i--) {
            char c = text.charAt(i);
            int priority = -1;

            // Assign priority to punctuation
            if (c == ';' || c == ':') {
                priority = 3; // Highest priority
            } else if (c == ',') {
                priority = 2; // Medium priority
            } else if (c == ' ') {
                priority = 1; // Lowest priority
            }

            // Keep the split point with highest priority, or closest to maxLength if same priority
            if (priority > bestPriority) {
                bestPriority = priority;
                bestSplitPoint = i + 1; // Split after the punctuation/space
            }
        }

        return bestSplitPoint;
    }

    // Test method
    public static void main(String[] args) {
        // Test with the problematic sentence reported by user
        System.out.println("=== BUG REPRODUCTION TEST ===");
        String problematicText = "habiendo visto por los señores dél un libro intitulado El ingenioso hidalgo de la Mancha, compuesto por Miguel de Cervantes Saavedra, tasaron cada pliego";
        System.out.println("Text length: " + problematicText.length() + " characters");
        System.out.println("Text: " + problematicText);
        System.out.println();

        List<String> problemParts = splitIfNeeded(problematicText);
        System.out.println("Split into " + problemParts.size() + " parts:");
        for (int i = 0; i < problemParts.size(); i++) {
            System.out.println((i + 1) + ". [" + problemParts.get(i).length() + " chars] " + problemParts.get(i));
        }
        System.out.println();

        // Test with a long sentence from Don Quijote
        System.out.println("=== FULL SENTENCE TEST ===");
        String longSentence = "Yo, Juan Gallo de Andrada, escribano de Cámara del Rey, nuestro señor, de los que residen en su Consejo, certifico y doy fe que, habiendo visto por los señores dél un libro intitulado El ingenioso hidalgo de la Mancha, compuesto por Miguel de Cervantes Saavedra, tasaron cada pliego del dicho libro a tres maravedís y medio el cual tiene ochenta y tres pliegos, que al dicho precio monta el dicho libro docientos y noventa maravedís y medio, en que se ha de vender en papel; y dieron licencia para que a este precio se pueda vender, y mandaron que esta tasa se ponga al principio del dicho libro, v no se pueda vender sin ella.";

        System.out.println("Original sentence length: " + longSentence.length() + " characters");
        List<String> parts = splitIfNeeded(longSentence);

        System.out.println("Split into " + parts.size() + " parts:");
        for (int i = 0; i < parts.size(); i++) {
            System.out.println((i + 1) + ". [" + parts.get(i).length() + " chars] " + parts.get(i));
        }
        System.out.println();

        // Test with short sentence
        System.out.println("=== SHORT SENTENCE TEST ===");
        String shortSentence = "Miguel de Cervantes.";
        List<String> shortParts = splitIfNeeded(shortSentence);
        System.out.println("Short sentence: " + shortParts.get(0));
        System.out.println();

        // Test with [BREAK] marker
        System.out.println("=== BREAK MARKER TEST ===");
        List<String> breakParts = splitIfNeeded("[BREAK]");
        System.out.println("Break marker: " + breakParts.get(0));
    }
}