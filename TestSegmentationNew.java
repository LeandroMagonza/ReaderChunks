import java.io.*;
import java.nio.file.*;
import java.util.*;

public class TestSegmentationNew {

    public static void main(String[] args) {
        System.out.println("=== TESTING PARAGRAPH SEGMENTATION ===\n");

        testTXTSegmentation();
        testEPUBContentExtraction();
        testMDSegmentation();
    }

    private static void testTXTSegmentation() {
        System.out.println("1. TESTING TXT PARAGRAPH SEGMENTATION");
        System.out.println("=====================================");

        try {
            String content = Files.readString(Paths.get("example.txt"));

            // Simulate our SentenceSegmenter logic
            List<String> paragraphs = segmentIntoSentences(content);

            System.out.println("✅ Segmentation completed");
            System.out.println("📚 Total paragraphs: " + paragraphs.size());
            System.out.println("📏 Original length: " + content.length() + " chars");

            // Show first 5 paragraphs
            System.out.println("\n📖 First 5 paragraphs:");
            for (int i = 0; i < Math.min(5, paragraphs.size()); i++) {
                String para = paragraphs.get(i).trim();
                String preview = para.length() > 100 ? para.substring(0, 97) + "..." : para;
                System.out.println("   " + (i+1) + ": " + preview.replaceAll("\n", " "));
                System.out.println("      Length: " + para.length() + " chars");
            }

            // Show some from the middle
            if (paragraphs.size() > 10) {
                System.out.println("\n📖 Middle paragraphs (around " + (paragraphs.size()/2) + "):");
                int mid = paragraphs.size() / 2;
                for (int i = mid; i < Math.min(mid + 3, paragraphs.size()); i++) {
                    String para = paragraphs.get(i).trim();
                    String preview = para.length() > 100 ? para.substring(0, 97) + "..." : para;
                    System.out.println("   " + (i+1) + ": " + preview.replaceAll("\n", " "));
                    System.out.println("      Length: " + para.length() + " chars");
                }
            }

        } catch (Exception e) {
            System.out.println("❌ ERROR: " + e.getMessage());
            e.printStackTrace();
        }

        System.out.println("\n" + "=".repeat(60) + "\n");
    }

    private static void testEPUBContentExtraction() {
        System.out.println("2. TESTING EPUB CONTENT EXTRACTION & SEGMENTATION");
        System.out.println("================================================");

        try {
            File epubFile = new File("example.epub");
            if (!epubFile.exists()) {
                System.out.println("❌ ERROR: example.epub not found");
                return;
            }

            StringBuilder fullContent = new StringBuilder();

            try (java.util.zip.ZipFile zipFile = new java.util.zip.ZipFile(epubFile)) {
                List<String> htmlFiles = new ArrayList<>();

                // Collect HTML files in order
                zipFile.stream()
                    .filter(entry -> (entry.getName().toLowerCase().endsWith(".html") ||
                                    entry.getName().toLowerCase().endsWith(".xhtml")) &&
                                   !entry.getName().contains("toc") &&
                                   !entry.getName().contains("nav") &&
                                   !entry.getName().contains("calibre_title"))
                    .forEach(entry -> htmlFiles.add(entry.getName()));

                // Sort to maintain order
                htmlFiles.sort(String::compareTo);

                System.out.println("📚 Processing " + htmlFiles.size() + " content files:");

                for (String fileName : htmlFiles) {
                    var entry = zipFile.getEntry(fileName);
                    if (entry != null) {
                        try (InputStream is = zipFile.getInputStream(entry);
                             BufferedReader reader = new BufferedReader(new InputStreamReader(is))) {

                            StringBuilder htmlContent = new StringBuilder();
                            String line;
                            while ((line = reader.readLine()) != null) {
                                htmlContent.append(line).append("\n");
                            }

                            String plainText = convertHtmlToPlainText(htmlContent.toString());
                            if (!plainText.trim().isEmpty()) {
                                fullContent.append(plainText).append("\n\n");
                                System.out.println("   📄 " + fileName + " -> " + plainText.length() + " chars");
                            }
                        }
                    }
                }
            }

            String extractedText = fullContent.toString().trim();
            System.out.println("\n✅ Content extraction completed");
            System.out.println("📏 Total extracted text: " + extractedText.length() + " chars");

            // Now segment into paragraphs
            List<String> paragraphs = segmentIntoSentences(extractedText);
            System.out.println("📚 Total paragraphs after segmentation: " + paragraphs.size());

            // Show sample content
            System.out.println("\n📖 Sample paragraphs from EPUB:");
            for (int i = 0; i < Math.min(3, paragraphs.size()); i++) {
                String para = paragraphs.get(i).trim();
                String preview = para.length() > 200 ? para.substring(0, 197) + "..." : para;
                System.out.println("   " + (i+1) + ": " + preview.replaceAll("\n", " "));
                System.out.println("      Length: " + para.length() + " chars");
            }

        } catch (Exception e) {
            System.out.println("❌ ERROR: " + e.getMessage());
            e.printStackTrace();
        }

        System.out.println("\n" + "=".repeat(60) + "\n");
    }

    private static void testMDSegmentation() {
        System.out.println("3. TESTING MARKDOWN SEGMENTATION");
        System.out.println("================================");

        try {
            String testMarkdown = "# The Art of Programming\n\nProgramming is both an art and a science. It requires creativity, logic, and patience.\n\n## Getting Started\n\nWhen you first start programming, it can seem overwhelming. There are so many languages to choose from: Python, Java, JavaScript, C++, and many more.\n\n### Choosing a Language\n\nThe best first language depends on your goals:\n\n- **Python**: Great for beginners, data science, and automation\n- **JavaScript**: Essential for web development\n- **Java**: Widely used in enterprise applications\n- **C++**: Perfect for system programming and game development";

            Files.write(Paths.get("test_long.md"), testMarkdown.getBytes());

            // Convert markdown to plain text
            String plainText = convertMarkdownToPlainText(testMarkdown);
            System.out.println("✅ Markdown converted to plain text");
            System.out.println("📏 Original length: " + testMarkdown.length() + " chars");
            System.out.println("📏 Plain text length: " + plainText.length() + " chars");

            // Segment into paragraphs
            List<String> paragraphs = segmentIntoSentences(plainText);
            System.out.println("📚 Total paragraphs: " + paragraphs.size());

            System.out.println("\n📖 Segmented paragraphs from Markdown:");
            for (int i = 0; i < Math.min(5, paragraphs.size()); i++) {
                String para = paragraphs.get(i).trim();
                String preview = para.length() > 150 ? para.substring(0, 147) + "..." : para;
                System.out.println("   " + (i+1) + ": " + preview.replaceAll("\n", " "));
                System.out.println("      Length: " + para.length() + " chars");
            }

        } catch (Exception e) {
            System.out.println("❌ ERROR: " + e.getMessage());
            e.printStackTrace();
        }

        System.out.println("\n" + "=".repeat(60) + "\n");
    }

    // Simulate SentenceSegmenter.segmentIntoSentences() logic
    private static List<String> segmentIntoSentences(String text) {
        List<String> sentences = new ArrayList<>();

        if (text == null || text.trim().isEmpty()) {
            return sentences;
        }

        // Split by double newlines (paragraph breaks)
        String[] paragraphs = text.split("\\n\\s*\\n");

        for (String paragraph : paragraphs) {
            String cleaned = paragraph.trim().replaceAll("\\s+", " ");
            if (!cleaned.isEmpty()) {
                sentences.add(cleaned);

                // Add break marker between paragraphs (except for the last one)
                if (!paragraph.equals(paragraphs[paragraphs.length - 1])) {
                    sentences.add("[BREAK]");
                }
            }
        }

        return sentences;
    }

    // HTML to plain text conversion (same as our Android implementation)
    private static String convertHtmlToPlainText(String html) {
        String text = html;

        // Remove script and style tags
        text = text.replaceAll("(?i)<script[^>]*>.*?</script>", "");
        text = text.replaceAll("(?i)<style[^>]*>.*?</style>", "");

        // Convert block elements to line breaks
        text = text.replaceAll("(?i)</(p|div|h[1-6]|br)>", "\n");
        text = text.replaceAll("(?i)<br\\s*/?>", "\n");

        // Remove all HTML tags
        text = text.replaceAll("<[^>]+>", "");

        // Convert HTML entities
        text = text.replace("&nbsp;", " ");
        text = text.replace("&amp;", "&");
        text = text.replace("&lt;", "<");
        text = text.replace("&gt;", ">");
        text = text.replace("&quot;", "\"");
        text = text.replace("&apos;", "'");
        text = text.replace("&#8220;", "\"");
        text = text.replace("&#8221;", "\"");
        text = text.replace("&#8217;", "'");
        text = text.replace("&#8216;", "'");

        // Clean up whitespace
        text = text.replaceAll("\\s+", " ");
        text = text.replaceAll("\\n\\s*\\n\\s*\\n", "\n\n");

        return text.trim();
    }

    // Markdown to plain text conversion (same as our Android implementation)
    private static String convertMarkdownToPlainText(String markdown) {
        String text = markdown;

        // Remove code blocks first
        text = text.replaceAll("```[\\s\\S]*?```", "");

        // Convert headers
        text = text.replaceAll("^#+\\s*", "");

        // Convert bold
        text = text.replaceAll("\\*\\*(.*?)\\*\\*", "$1");

        // Convert italic
        text = text.replaceAll("\\*(.*?)\\*", "$1");

        // Convert links
        text = text.replaceAll("\\[([^\\]]+)\\]\\([^\\)]+\\)", "$1");

        // Convert inline code
        text = text.replaceAll("`(.*?)`", "$1");

        // Convert lists
        text = text.replaceAll("^[\\s]*[-*+]\\s+", "");
        text = text.replaceAll("^[\\s]*\\d+\\.\\s+", "");

        // Convert blockquotes
        text = text.replaceAll("^>\\s*", "");

        // Clean up whitespace
        text = text.replaceAll("\\n\\s*\\n\\s*\\n", "\n\n");
        text = text.replaceAll("[ \\t]+", " ");

        return text.trim();
    }
}