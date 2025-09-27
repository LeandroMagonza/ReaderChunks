import java.io.*;
import java.util.List;

public class TestImprovedSegmentation {
    public static void main(String[] args) {
        try {
            // Read the extracted text preserving original line breaks
            BufferedReader reader = new BufferedReader(new FileReader("quijote_output.txt"));
            StringBuilder fullText = new StringBuilder();
            String line;
            boolean inFullText = false;

            while ((line = reader.readLine()) != null) {
                if (line.equals("=== FULL TEXT ===")) {
                    inFullText = true;
                    continue;
                }
                if (line.equals("=== END OF TEXT ===")) {
                    break;
                }
                if (inFullText) {
                    // Preserve original line breaks instead of converting to spaces
                    fullText.append(line).append("\n");
                }
            }
            reader.close();

            // Get first 2000 characters to analyze
            String textToAnalyze = fullText.toString().substring(0, Math.min(2000, fullText.length()));

            System.out.println("=== IMPROVED SENTENCE SEGMENTATION TEST ===");
            System.out.println("Text length: " + textToAnalyze.length() + " characters");
            System.out.println();

            // Show raw text with line breaks visible
            System.out.println("=== RAW TEXT (first 800 chars) ===");
            System.out.println(textToAnalyze.substring(0, Math.min(800, textToAnalyze.length())));
            System.out.println("\n" + "=".repeat(50));

            // Segment into sentences using improved algorithm
            List<String> sentences = SentenceSegmenterTest.segmentIntoSentences(textToAnalyze);

            System.out.println("\n=== IMPROVED SENTENCE SEGMENTATION ===");
            System.out.println("Total sentences found: " + sentences.size());
            System.out.println();

            for (int i = 0; i < Math.min(15, sentences.size()); i++) {
                System.out.println((i + 1) + ". [" + sentences.get(i).length() + " chars] " + sentences.get(i));
                System.out.println();
            }

        } catch (IOException e) {
            System.err.println("Error: " + e.getMessage());
        }
    }
}