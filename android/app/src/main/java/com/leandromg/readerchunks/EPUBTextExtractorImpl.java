package com.leandromg.readerchunks;

import android.content.Context;
import android.net.Uri;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * EPUB text extractor implementation
 * Extracts text content from EPUB files by parsing HTML content
 */
public class EPUBTextExtractorImpl implements TextExtractor {

    // Regex patterns for HTML cleanup
    private static final Pattern HTML_TAG_PATTERN = Pattern.compile("<[^>]+>");
    private static final Pattern HTML_ENTITY_PATTERN = Pattern.compile("&[a-zA-Z]+;|&#\\d+;");
    private static final Pattern WHITESPACE_PATTERN = Pattern.compile("\\s+");

    @Override
    public String extractTextFromUri(Context context, Uri uri) throws IOException {
        InputStream inputStream = null;
        ZipInputStream zipInputStream = null;

        try {
            inputStream = context.getContentResolver().openInputStream(uri);
            if (inputStream == null) {
                throw new IOException("No se pudo abrir el archivo EPUB");
            }

            zipInputStream = new ZipInputStream(inputStream);
            StringBuilder content = new StringBuilder();
            ZipEntry entry;

            // Read all HTML/XHTML files from the EPUB
            while ((entry = zipInputStream.getNextEntry()) != null) {
                String fileName = entry.getName().toLowerCase();

                // Process HTML/XHTML content files
                if ((fileName.endsWith(".html") || fileName.endsWith(".xhtml")) &&
                    !fileName.contains("toc") && !fileName.contains("nav")) {

                    String htmlContent = readZipEntryContent(zipInputStream);
                    String plainText = convertHtmlToPlainText(htmlContent);

                    if (!plainText.trim().isEmpty()) {
                        content.append(plainText).append("\n\n");
                    }
                }

                zipInputStream.closeEntry();
            }

            String extractedText = content.toString().trim();

            if (extractedText.isEmpty()) {
                throw new IOException("El archivo EPUB no contiene texto extraíble");
            }

            return extractedText;

        } finally {
            if (zipInputStream != null) {
                try {
                    zipInputStream.close();
                } catch (IOException e) {
                    // Log but don't throw
                }
            }
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    // Log but don't throw
                }
            }
        }
    }

    /**
     * Read content from a ZIP entry
     */
    private String readZipEntryContent(ZipInputStream zipInputStream) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(zipInputStream, "UTF-8"));
        StringBuilder content = new StringBuilder();
        String line;

        while ((line = reader.readLine()) != null) {
            content.append(line).append("\n");
        }

        return content.toString();
    }

    /**
     * Convert HTML content to plain text
     */
    private String convertHtmlToPlainText(String html) {
        String text = html;

        // Remove script and style tags completely
        text = text.replaceAll("(?i)<script[^>]*>.*?</script>", "");
        text = text.replaceAll("(?i)<style[^>]*>.*?</style>", "");

        // Convert common block elements to line breaks
        text = text.replaceAll("(?i)</(p|div|h[1-6]|br)>", "\n");
        text = text.replaceAll("(?i)<br\\s*/?>", "\n");

        // Remove all HTML tags
        text = HTML_TAG_PATTERN.matcher(text).replaceAll("");

        // Convert common HTML entities
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

        // Remove remaining HTML entities
        text = HTML_ENTITY_PATTERN.matcher(text).replaceAll("");

        // Clean up whitespace
        text = WHITESPACE_PATTERN.matcher(text).replaceAll(" ");
        text = text.replaceAll("\\n\\s*\\n\\s*\\n", "\n\n"); // Multiple empty lines to double
        text = text.trim();

        return text;
    }

    @Override
    public boolean supportsExtension(String extension) {
        return "epub".equalsIgnoreCase(extension);
    }

    @Override
    public String[] getSupportedMimeTypes() {
        return new String[]{"application/epub+zip"};
    }
}