import java.io.File;
import java.io.IOException;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;

// Simple standalone Java class to test PDF text extraction
// This will be later integrated into React Native as a native module
public class PDFTextExtractor {

    public static void main(String[] args) {
        if (args.length != 1) {
            System.out.println("Usage: java PDFTextExtractor <path-to-pdf>");
            System.exit(1);
        }

        String pdfPath = args[0];

        try {
            String text = extractText(pdfPath);
            System.out.println("=== PDF TEXT EXTRACTION RESULT ===");
            System.out.println("Text length: " + text.length() + " characters");
            System.out.println("First 500 characters:");
            System.out.println(text.substring(0, Math.min(500, text.length())));
            System.out.println("=== FULL TEXT ===");
            System.out.println(text);
            System.out.println("=== END OF TEXT ===");
        } catch (IOException e) {
            System.err.println("Error extracting text from PDF: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        } catch (Exception e) {
            System.err.println("Unexpected error: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }

    public static String extractText(String pdfPath) throws IOException {
        File file = new File(pdfPath);
        if (!file.exists()) {
            throw new IOException("PDF file not found: " + pdfPath);
        }

        System.out.println("Processing PDF file: " + file.getAbsolutePath());
        System.out.println("File size: " + file.length() + " bytes");

        PDDocument document = null;
        try {
            // Load the PDF document
            document = PDDocument.load(file);

            // Check if the document is encrypted
            if (document.isEncrypted()) {
                throw new IOException("PDF is encrypted and cannot be processed");
            }

            // Extract text using PDFTextStripper
            PDFTextStripper stripper = new PDFTextStripper();
            String text = stripper.getText(document);

            System.out.println("Successfully extracted text from " + document.getNumberOfPages() + " pages");

            return text.trim();

        } finally {
            if (document != null) {
                document.close();
            }
        }
    }
}