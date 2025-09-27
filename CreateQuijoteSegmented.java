import java.io.*;
import java.util.List;

public class CreateQuijoteSegmented {
    public static void main(String[] args) {
        try {
            // Read the full extracted text preserving original line breaks
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
                    // Preserve original line breaks
                    fullText.append(line).append("\n");
                }
            }
            reader.close();

            // Segment the full text using improved algorithm
            List<String> sentences = SentenceSegmenterTest.segmentIntoSentences(fullText.toString());

            // Write results to file
            BufferedWriter writer = new BufferedWriter(new FileWriter("quijote_sentences_improved.txt"));

            writer.write("=== IMPROVED SENTENCE SEGMENTATION RESULT ===\n");
            writer.write("Total sentences: " + sentences.size() + "\n\n");

            for (int i = 0; i < sentences.size(); i++) {
                writer.write((i + 1) + ". " + sentences.get(i) + "\n");
            }

            writer.close();

            System.out.println("✅ Segmentation completed!");
            System.out.println("Total sentences: " + sentences.size());
            System.out.println("Output written to: quijote_sentences_improved.txt");

            // Show first 10 sentences
            System.out.println("\n=== FIRST 10 SENTENCES ===");
            for (int i = 0; i < Math.min(10, sentences.size()); i++) {
                System.out.println((i + 1) + ". [" + sentences.get(i).length() + " chars] " + sentences.get(i));
            }

        } catch (IOException e) {
            System.err.println("Error: " + e.getMessage());
        }
    }
}