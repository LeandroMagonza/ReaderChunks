import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

public class TestQuijote {
    public static void main(String[] args) {
        try {
            System.out.println("Extracting Don Quijote text...");

            String text = PDFTextExtractor.extractText("Don Quijote I -- de Cervantes, Saavedra Miguel -- 0 -- f214210ab306fdbabb19fac243f8e01f -- Anna's Archive.pdf");

            // Save raw text
            FileWriter writer = new FileWriter("quijote_raw.txt");
            writer.write(text);
            writer.close();

            // Segment into sentences
            List<String> sentences = SentenceSegmenter.segmentIntoSentences(text);

            // Save first 20 sentences
            FileWriter sentenceWriter = new FileWriter("quijote_sentences.txt");
            for (int i = 0; i < Math.min(20, sentences.size()); i++) {
                sentenceWriter.write((i + 1) + ". " + sentences.get(i) + "\n\n");
            }
            sentenceWriter.close();

            System.out.println("Total sentences: " + sentences.size());
            System.out.println("First 20 sentences saved to quijote_sentences.txt");
            System.out.println("Raw text saved to quijote_raw.txt");

            // Show first few lines of raw text
            String[] lines = text.split("\n");
            System.out.println("\n=== FIRST 10 LINES OF RAW TEXT ===");
            for (int i = 0; i < Math.min(10, lines.length); i++) {
                System.out.println((i + 1) + ": " + lines[i]);
            }

        } catch (IOException e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}