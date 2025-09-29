import java.io.*;
import java.nio.file.*;

public class TestExtractors {

    public static void main(String[] args) {
        System.out.println("=== TESTING TEXT EXTRACTORS ===\n");

        testTXTExtractor();
        testMDExtractor();
        testEPUBExtractor();
    }

    private static void testTXTExtractor() {
        System.out.println("1. TESTING TXT EXTRACTOR");
        System.out.println("========================");

        try {
            // Simulate the TXT extractor logic without Android dependencies
            String content = Files.readString(Paths.get("example.txt"));

            // Basic validation like our TXT extractor does
            if (content.trim().isEmpty()) {
                System.out.println("❌ ERROR: Empty file");
                return;
            }

            System.out.println("✅ File successfully read");
            System.out.println("📏 Total characters: " + content.length());
            System.out.println("📄 Total lines: " + content.split("\n").length);

            // Show first few lines
            String[] lines = content.split("\n");
            System.out.println("📖 First 10 lines:");
            for (int i = 0; i < Math.min(10, lines.length); i++) {
                System.out.println("   " + (i+1) + ": " + lines[i].trim());
            }

            // Test paragraph extraction (simulating sentence segmentation)
            String[] paragraphs = content.split("\n\n+");
            System.out.println("📚 Total paragraphs found: " + paragraphs.length);
            System.out.println("📝 First paragraph preview: " +
                            paragraphs[0].replaceAll("\n", " ").trim().substring(0,
                            Math.min(150, paragraphs[0].replaceAll("\n", " ").trim().length())) + "...");

        } catch (Exception e) {
            System.out.println("❌ ERROR: " + e.getMessage());
        }

        System.out.println("\n" + "=".repeat(50) + "\n");
    }

    private static void testMDExtractor() {
        System.out.println("2. TESTING MD EXTRACTOR (Creating test file)");
        System.out.println("===========================================");

        try {
            // Create a test markdown file
            String testMarkdown = """
                # Chapter 1: Introduction

                This is a **bold** text and this is *italic* text.

                ## Section 1.1: Lists

                Here's a list:
                - First item
                - Second item with `inline code`
                - Third item

                ### Subsection: Links and Code

                Visit [Google](https://www.google.com) for more information.

                ```java
                public class Test {
                    public static void main(String[] args) {
                        System.out.println("Hello World!");
                    }
                }
                ```

                > This is a blockquote
                > with multiple lines

                And here's some more normal text.
                """;

            Files.write(Paths.get("test.md"), testMarkdown.getBytes());

            // Now test the conversion logic
            String converted = convertMarkdownToPlainText(testMarkdown);

            System.out.println("✅ Markdown conversion completed");
            System.out.println("📏 Original length: " + testMarkdown.length());
            System.out.println("📏 Converted length: " + converted.length());
            System.out.println("📖 Converted text:");
            System.out.println(converted);

        } catch (Exception e) {
            System.out.println("❌ ERROR: " + e.getMessage());
        }

        System.out.println("\n" + "=".repeat(50) + "\n");
    }

    private static void testEPUBExtractor() {
        System.out.println("3. TESTING EPUB EXTRACTOR");
        System.out.println("=========================");

        try {
            System.out.println("📁 Checking EPUB file: example.epub");
            File epubFile = new File("example.epub");

            if (!epubFile.exists()) {
                System.out.println("❌ ERROR: example.epub not found");
                return;
            }

            System.out.println("✅ EPUB file found");
            System.out.println("📏 File size: " + epubFile.length() + " bytes");

            // Basic ZIP validation
            try (java.util.zip.ZipFile zipFile = new java.util.zip.ZipFile(epubFile)) {
                int htmlCount = 0;
                System.out.println("📚 EPUB contents:");

                zipFile.stream().forEach(entry -> {
                    System.out.println("   📄 " + entry.getName() + " (" + entry.getSize() + " bytes)");
                });

                htmlCount = (int) zipFile.stream()
                    .filter(entry -> entry.getName().toLowerCase().endsWith(".html") ||
                                   entry.getName().toLowerCase().endsWith(".xhtml"))
                    .count();

                System.out.println("✅ HTML/XHTML files found: " + htmlCount);

                // Try to extract content from first HTML file
                zipFile.stream()
                    .filter(entry -> (entry.getName().toLowerCase().endsWith(".html") ||
                                    entry.getName().toLowerCase().endsWith(".xhtml")) &&
                                   !entry.getName().contains("toc") &&
                                   !entry.getName().contains("nav"))
                    .findFirst()
                    .ifPresent(entry -> {
                        try (InputStream is = zipFile.getInputStream(entry);
                             BufferedReader reader = new BufferedReader(new InputStreamReader(is))) {

                            StringBuilder content = new StringBuilder();
                            String line;
                            int lineCount = 0;

                            while ((line = reader.readLine()) != null && lineCount < 20) {
                                content.append(line).append("\n");
                                lineCount++;
                            }

                            String htmlContent = content.toString();
                            String plainText = convertHtmlToPlainText(htmlContent);

                            System.out.println("📖 Sample content from " + entry.getName() + ":");
                            System.out.println("📏 HTML length: " + htmlContent.length());
                            System.out.println("📏 Plain text length: " + plainText.length());
                            System.out.println("📝 First 300 characters:");
                            System.out.println(plainText.substring(0, Math.min(300, plainText.length())) + "...");

                        } catch (Exception e) {
                            System.out.println("❌ Error reading entry: " + e.getMessage());
                        }
                    });
            }

        } catch (Exception e) {
            System.out.println("❌ ERROR: " + e.getMessage());
        }

        System.out.println("\n" + "=".repeat(50) + "\n");
    }

    // Markdown conversion logic (simplified version of our Android implementation)
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

    // HTML conversion logic (simplified version of our Android implementation)
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

        // Clean up whitespace
        text = text.replaceAll("\\s+", " ");
        text = text.replaceAll("\\n\\s*\\n\\s*\\n", "\n\n");

        return text.trim();
    }
}