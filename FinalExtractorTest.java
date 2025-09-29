import java.io.*;
import java.nio.file.*;
import java.util.*;

/**
 * Final comprehensive test of all text extractors
 * This simulates the complete flow from file -> text -> segmentation
 */
public class FinalExtractorTest {

    public static void main(String[] args) {
        System.out.println("🚀 COMPREHENSIVE EXTRACTOR TEST");
        System.out.println("==============================\n");

        testCompleteFlow();
    }

    private static void testCompleteFlow() {
        // Test TXT file
        System.out.println("📄 TESTING TXT COMPLETE FLOW");
        System.out.println("============================");
        testFileExtraction("example.txt", "TXT");

        System.out.println("\n" + "=".repeat(50) + "\n");

        // Test EPUB file
        System.out.println("📚 TESTING EPUB COMPLETE FLOW");
        System.out.println("=============================");
        testEPUBExtraction("example.epub");

        System.out.println("\n" + "=".repeat(50) + "\n");

        // Test MD file (create one)
        System.out.println("📝 TESTING MD COMPLETE FLOW");
        System.out.println("===========================");
        createAndTestMDFile();

        System.out.println("\n🎉 ALL TESTS COMPLETED!");
    }

    private static void testFileExtraction(String filename, String format) {
        try {
            File file = new File(filename);
            if (!file.exists()) {
                System.out.println("❌ File not found: " + filename);
                return;
            }

            System.out.println("📁 File: " + filename);
            System.out.println("📏 Size: " + file.length() + " bytes");

            // Step 1: Extract text (simulating our TextExtractor)
            String extractedText;
            if ("TXT".equals(format)) {
                extractedText = Files.readString(Paths.get(filename));
            } else {
                System.out.println("❌ Unsupported format in this test: " + format);
                return;
            }

            System.out.println("✅ Text extraction completed");
            System.out.println("📏 Extracted text length: " + extractedText.length() + " chars");

            // Step 2: Segment into sentences/paragraphs (simulating SentenceSegmenter)
            List<String> segments = segmentIntoSentences(extractedText);
            System.out.println("✅ Segmentation completed");
            System.out.println("📚 Total segments: " + segments.size());

            // Step 3: Analyze segments
            analyzeSegments(segments, filename);

            // Step 4: Save processed content (simulating cache)
            saveProcessedContent(segments, filename + "_processed.txt");

        } catch (Exception e) {
            System.out.println("❌ ERROR: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void testEPUBExtraction(String filename) {
        try {
            File file = new File(filename);
            if (!file.exists()) {
                System.out.println("❌ File not found: " + filename);
                return;
            }

            System.out.println("📁 File: " + filename);
            System.out.println("📏 Size: " + file.length() + " bytes");

            // Step 1: Extract text from EPUB (simulating EPUBTextExtractorImpl)
            StringBuilder fullContent = new StringBuilder();

            try (java.util.zip.ZipFile zipFile = new java.util.zip.ZipFile(file)) {
                zipFile.stream()
                    .filter(entry -> (entry.getName().toLowerCase().endsWith(".html") ||
                                    entry.getName().toLowerCase().endsWith(".xhtml")) &&
                                   !entry.getName().contains("toc") &&
                                   !entry.getName().contains("nav") &&
                                   !entry.getName().contains("calibre_title"))
                    .sorted((a, b) -> a.getName().compareTo(b.getName()))
                    .forEach(entry -> {
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
                            }
                        } catch (Exception e) {
                            System.out.println("⚠️ Warning: Could not process " + entry.getName());
                        }
                    });
            }

            String extractedText = fullContent.toString().trim();
            System.out.println("✅ EPUB extraction completed");
            System.out.println("📏 Extracted text length: " + extractedText.length() + " chars");

            // Step 2: Segment into sentences/paragraphs
            List<String> segments = segmentIntoSentences(extractedText);
            System.out.println("✅ Segmentation completed");
            System.out.println("📚 Total segments: " + segments.size());

            // Step 3: Analyze segments
            analyzeSegments(segments, filename);

            // Step 4: Save processed content
            saveProcessedContent(segments, filename + "_processed.txt");

        } catch (Exception e) {
            System.out.println("❌ ERROR: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void createAndTestMDFile() {
        try {
            String markdownContent = "# Sample Document\n\n" +
                "This is a **sample** Markdown document to test our extraction capabilities.\n\n" +
                "## Features\n\n" +
                "Our reader supports:\n\n" +
                "- PDF files using PDFBox\n" +
                "- Plain text files with UTF-8 encoding\n" +
                "- Markdown files with formatting removal\n" +
                "- EPUB files with HTML content extraction\n\n" +
                "## Code Example\n\n" +
                "Here's a simple Java example:\n\n" +
                "```java\n" +
                "public class Hello {\n" +
                "    public static void main(String[] args) {\n" +
                "        System.out.println(\"Hello, World!\");\n" +
                "    }\n" +
                "}\n" +
                "```\n\n" +
                "## Conclusion\n\n" +
                "This demonstrates our multi-format support. Visit [our website](https://example.com) for more information.\n\n" +
                "> \"The best way to learn programming is by practicing.\" - Anonymous";

            String filename = "sample.md";
            Files.write(Paths.get(filename), markdownContent.getBytes());

            System.out.println("📁 File: " + filename + " (created)");
            System.out.println("📏 Size: " + markdownContent.length() + " chars");

            // Step 1: Convert MD to plain text (simulating MDTextExtractorImpl)
            String plainText = convertMarkdownToPlainText(markdownContent);
            System.out.println("✅ Markdown conversion completed");
            System.out.println("📏 Plain text length: " + plainText.length() + " chars");

            // Step 2: Segment into sentences/paragraphs
            List<String> segments = segmentIntoSentences(plainText);
            System.out.println("✅ Segmentation completed");
            System.out.println("📚 Total segments: " + segments.size());

            // Step 3: Analyze segments
            analyzeSegments(segments, filename);

            // Step 4: Save processed content
            saveProcessedContent(segments, filename + "_processed.txt");

        } catch (Exception e) {
            System.out.println("❌ ERROR: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void analyzeSegments(List<String> segments, String originalFile) {
        System.out.println("\n📊 SEGMENT ANALYSIS for " + originalFile);
        System.out.println("─".repeat(40));

        int textSegments = 0;
        int breakMarkers = 0;
        int totalLength = 0;
        int shortSegments = 0;  // < 50 chars
        int mediumSegments = 0; // 50-200 chars
        int longSegments = 0;   // > 200 chars

        for (String segment : segments) {
            if ("[BREAK]".equals(segment)) {
                breakMarkers++;
            } else {
                textSegments++;
                totalLength += segment.length();

                if (segment.length() < 50) {
                    shortSegments++;
                } else if (segment.length() <= 200) {
                    mediumSegments++;
                } else {
                    longSegments++;
                }
            }
        }

        System.out.println("📄 Text segments: " + textSegments);
        System.out.println("🔀 Break markers: " + breakMarkers);
        System.out.println("📏 Average segment length: " + (textSegments > 0 ? totalLength / textSegments : 0) + " chars");
        System.out.println("📊 Length distribution:");
        System.out.println("   Short (< 50 chars): " + shortSegments);
        System.out.println("   Medium (50-200): " + mediumSegments);
        System.out.println("   Long (> 200): " + longSegments);

        // Show sample segments
        System.out.println("\n📖 Sample segments:");
        int count = 0;
        for (int i = 0; i < Math.min(3, segments.size()) && count < 3; i++) {
            String segment = segments.get(i);
            if (!"[BREAK]".equals(segment)) {
                count++;
                String preview = segment.length() > 100 ? segment.substring(0, 97) + "..." : segment;
                System.out.println("   " + count + ": " + preview.replaceAll("\n", " "));
                System.out.println("      (" + segment.length() + " chars)");
            }
        }
    }

    private static void saveProcessedContent(List<String> segments, String outputFile) {
        try {
            try (PrintWriter writer = new PrintWriter(new FileWriter(outputFile))) {
                for (String segment : segments) {
                    writer.println(segment);
                }
            }
            System.out.println("✅ Processed content saved to: " + outputFile);
        } catch (IOException e) {
            System.out.println("⚠️ Warning: Could not save processed content: " + e.getMessage());
        }
    }

    // Same helper methods as before
    private static List<String> segmentIntoSentences(String text) {
        List<String> sentences = new ArrayList<>();
        if (text == null || text.trim().isEmpty()) {
            return sentences;
        }

        String[] paragraphs = text.split("\\n\\s*\\n");
        for (int i = 0; i < paragraphs.length; i++) {
            String cleaned = paragraphs[i].trim().replaceAll("\\s+", " ");
            if (!cleaned.isEmpty()) {
                sentences.add(cleaned);
                if (i < paragraphs.length - 1) {
                    sentences.add("[BREAK]");
                }
            }
        }
        return sentences;
    }

    private static String convertHtmlToPlainText(String html) {
        String text = html;
        text = text.replaceAll("(?i)<script[^>]*>.*?</script>", "");
        text = text.replaceAll("(?i)<style[^>]*>.*?</style>", "");
        text = text.replaceAll("(?i)</(p|div|h[1-6]|br)>", "\n");
        text = text.replaceAll("(?i)<br\\s*/?>", "\n");
        text = text.replaceAll("<[^>]+>", "");
        text = text.replace("&nbsp;", " ");
        text = text.replace("&amp;", "&");
        text = text.replace("&lt;", "<");
        text = text.replace("&gt;", ">");
        text = text.replace("&quot;", "\"");
        text = text.replace("&apos;", "'");
        text = text.replaceAll("\\s+", " ");
        text = text.replaceAll("\\n\\s*\\n\\s*\\n", "\n\n");
        return text.trim();
    }

    private static String convertMarkdownToPlainText(String markdown) {
        String text = markdown;
        text = text.replaceAll("```[\\s\\S]*?```", "");
        text = text.replaceAll("^#+\\s*", "");
        text = text.replaceAll("\\*\\*(.*?)\\*\\*", "$1");
        text = text.replaceAll("\\*(.*?)\\*", "$1");
        text = text.replaceAll("\\[([^\\]]+)\\]\\([^\\)]+\\)", "$1");
        text = text.replaceAll("`(.*?)`", "$1");
        text = text.replaceAll("^[\\s]*[-*+]\\s+", "");
        text = text.replaceAll("^[\\s]*\\d+\\.\\s+", "");
        text = text.replaceAll("^>\\s*", "");
        text = text.replaceAll("\\n\\s*\\n\\s*\\n", "\n\n");
        text = text.replaceAll("[ \\t]+", " ");
        return text.trim();
    }
}