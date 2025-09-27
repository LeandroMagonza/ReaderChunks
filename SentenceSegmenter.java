import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class SentenceSegmenter {

    private static final Pattern SENTENCE_PATTERN = Pattern.compile("[.!?]+");

    public static void main(String[] args) {
        if (args.length != 1) {
            System.out.println("Usage: java SentenceSegmenter \"<text>\"");
            System.exit(1);
        }

        String text = args[0];
        List<String> sentences = segmentIntoSentences(text);

        System.out.println("=== SENTENCE SEGMENTATION RESULT ===");
        System.out.println("Total sentences: " + sentences.size());
        System.out.println();

        for (int i = 0; i < sentences.size(); i++) {
            System.out.println((i + 1) + ". " + sentences.get(i));
        }
    }

    public static List<String> segmentIntoSentences(String text) {
        List<String> sentences = new ArrayList<>();

        if (text == null || text.trim().isEmpty()) {
            return sentences;
        }

        String[] parts = SENTENCE_PATTERN.split(text);

        for (String part : parts) {
            String trimmed = part.trim();
            if (!trimmed.isEmpty()) {
                sentences.add(trimmed + ".");
            }
        }

        return sentences;
    }
}