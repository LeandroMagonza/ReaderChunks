import java.io.*;
import java.util.List;

public class TestSegmentation {
    public static void main(String[] args) {
        try {
            // Read the extracted text
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
                    fullText.append(line).append(" ");
                }
            }
            reader.close();

            // Get first 2000 characters to analyze
            String textToAnalyze = fullText.toString().substring(0, Math.min(2000, fullText.length()));

            System.out.println("=== FIRST 2000 CHARACTERS OF RAW TEXT ===");
            System.out.println(textToAnalyze);
            System.out.println("\n" + "=".repeat(50));

            // Segment into sentences
            List<String> sentences = SentenceSegmenter.segmentIntoSentences(textToAnalyze);

            System.out.println("\n=== CURRENT SENTENCE SEGMENTATION ===");
            System.out.println("Total sentences found: " + sentences.size());
            System.out.println();

            for (int i = 0; i < Math.min(10, sentences.size()); i++) {
                System.out.println((i + 1) + ". [" + sentences.get(i).length() + " chars] " + sentences.get(i));
                System.out.println();
            }

        } catch (IOException e) {
            System.err.println("Error: " + e.getMessage());
        }
    }
}