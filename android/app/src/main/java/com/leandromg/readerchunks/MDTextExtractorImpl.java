package com.leandromg.readerchunks;

import android.content.Context;
import android.net.Uri;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.regex.Pattern;

/**
 * Markdown text extractor implementation
 * Converts basic Markdown to plain text, removing formatting symbols
 */
public class MDTextExtractorImpl implements TextExtractor {

    // Regex patterns for common Markdown elements
    private static final Pattern HEADER_PATTERN = Pattern.compile("^#+\\s*", Pattern.MULTILINE);
    private static final Pattern BOLD_PATTERN = Pattern.compile("\\*\\*(.*?)\\*\\*");
    private static final Pattern ITALIC_PATTERN = Pattern.compile("\\*(.*?)\\*");
    private static final Pattern CODE_BLOCK_PATTERN = Pattern.compile("```[\\s\\S]*?```");
    private static final Pattern INLINE_CODE_PATTERN = Pattern.compile("`(.*?)`");
    private static final Pattern LINK_PATTERN = Pattern.compile("\\[([^\\]]+)\\]\\([^\\)]+\\)");
    private static final Pattern IMAGE_PATTERN = Pattern.compile("!\\[([^\\]]*)\\]\\([^\\)]+\\)");
    private static final Pattern LIST_PATTERN = Pattern.compile("^[\\s]*[-*+]\\s+", Pattern.MULTILINE);
    private static final Pattern NUMBERED_LIST_PATTERN = Pattern.compile("^[\\s]*\\d+\\.\\s+", Pattern.MULTILINE);
    private static final Pattern BLOCKQUOTE_PATTERN = Pattern.compile("^>\\s*", Pattern.MULTILINE);

    @Override
    public String extractTextFromUri(Context context, Uri uri) throws IOException {
        InputStream inputStream = null;
        BufferedReader reader = null;

        try {
            inputStream = context.getContentResolver().openInputStream(uri);
            if (inputStream == null) {
                throw new IOException("No se pudo abrir el archivo Markdown");
            }

            reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
            StringBuilder content = new StringBuilder();
            String line;

            while ((line = reader.readLine()) != null) {
                content.append(line).append("\n");
            }

            String markdown = content.toString().trim();

            if (markdown.isEmpty()) {
                throw new IOException("El archivo Markdown está vacío");
            }

            // Convert Markdown to plain text
            String plainText = convertMarkdownToPlainText(markdown);

            return plainText;

        } catch (IOException e) {
            // Try with default charset if UTF-8 fails
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException ignored) {}
            }

            // Retry with default charset
            inputStream = context.getContentResolver().openInputStream(uri);
            if (inputStream == null) {
                throw new IOException("No se pudo abrir el archivo Markdown");
            }

            reader = new BufferedReader(new InputStreamReader(inputStream));
            StringBuilder content = new StringBuilder();
            String line;

            while ((line = reader.readLine()) != null) {
                content.append(line).append("\n");
            }

            String markdown = content.toString().trim();

            if (markdown.isEmpty()) {
                throw new IOException("El archivo Markdown está vacío");
            }

            // Convert Markdown to plain text
            String plainText = convertMarkdownToPlainText(markdown);

            return plainText;

        } finally {
            if (reader != null) {
                try {
                    reader.close();
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
     * Convert Markdown text to plain text by removing formatting
     */
    private String convertMarkdownToPlainText(String markdown) {
        String text = markdown;

        // Remove code blocks first (to avoid processing their content)
        text = CODE_BLOCK_PATTERN.matcher(text).replaceAll("");

        // Convert headers to plain text
        text = HEADER_PATTERN.matcher(text).replaceAll("");

        // Convert bold text
        text = BOLD_PATTERN.matcher(text).replaceAll("$1");

        // Convert italic text
        text = ITALIC_PATTERN.matcher(text).replaceAll("$1");

        // Convert links (keep link text, remove URL)
        text = LINK_PATTERN.matcher(text).replaceAll("$1");

        // Convert images (keep alt text if any)
        text = IMAGE_PATTERN.matcher(text).replaceAll("$1");

        // Remove inline code formatting
        text = INLINE_CODE_PATTERN.matcher(text).replaceAll("$1");

        // Convert lists (remove bullet points and numbers)
        text = LIST_PATTERN.matcher(text).replaceAll("");
        text = NUMBERED_LIST_PATTERN.matcher(text).replaceAll("");

        // Convert blockquotes
        text = BLOCKQUOTE_PATTERN.matcher(text).replaceAll("");

        // Clean up extra whitespace and empty lines
        text = text.replaceAll("\\n\\s*\\n\\s*\\n", "\n\n"); // Multiple empty lines to double
        text = text.replaceAll("[ \\t]+", " "); // Multiple spaces/tabs to single space
        text = text.trim();

        return text;
    }

    @Override
    public boolean supportsExtension(String extension) {
        return "md".equalsIgnoreCase(extension) || "markdown".equalsIgnoreCase(extension);
    }

    @Override
    public String[] getSupportedMimeTypes() {
        return new String[]{"text/markdown", "text/x-markdown"};
    }
}