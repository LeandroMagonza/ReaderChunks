package com.leandromg.readerchunks;

import android.content.Context;
import android.net.Uri;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * Text extractor for web URLs
 * Extracts clean text content from web pages, optimized for Wikipedia and similar sites
 */
public class WebTextExtractorImpl implements TextExtractor {
    private static final String TAG = "WebTextExtractorImpl";
    private Context context;

    public WebTextExtractorImpl(Context context) {
        this.context = context;
    }

    @Override
    public String extractTextFromUri(Context context, Uri uri) throws IOException {
        String url = uri.toString();
        Log.d(TAG, "Extracting text from web URL: " + url);

        if (!isValidWebUrl(url)) {
            throw new IOException("URL no válida: " + url);
        }

        try {
            return extractTextFromUrl(url);
        } catch (Exception e) {
            throw new IOException("Error extrayendo texto de URL: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean supportsExtension(String extension) {
        // This extractor doesn't work with file extensions
        return false;
    }

    @Override
    public String[] getSupportedMimeTypes() {
        // Web URLs don't have traditional MIME types for file selection
        return new String[0];
    }

    /**
     * Check if this extractor can handle the given URI
     */
    public static boolean canHandleUri(Uri uri) {
        if (uri == null) return false;
        String scheme = uri.getScheme();
        return "http".equals(scheme) || "https".equals(scheme);
    }

    /**
     * Extract text from a web URL
     */
    private String extractTextFromUrl(String url) throws IOException {
        Log.d(TAG, "Starting text extraction from: " + url);

        // Use a simple HTTP request approach for better reliability
        return performHttpTextExtraction(url);
    }

    /**
     * Perform HTTP request and extract clean text
     */
    private String performHttpTextExtraction(String url) throws IOException {
        java.net.URL urlObj = new java.net.URL(url);
        java.net.HttpURLConnection connection = (java.net.HttpURLConnection) urlObj.openConnection();

        try {
            connection.setRequestMethod("GET");
            connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Linux; Android 10) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.120 Mobile Safari/537.36 BookBits/1.0");
            connection.setRequestProperty("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
            connection.setRequestProperty("Accept-Language", "es-ES,es;q=0.9,en;q=0.8");
            connection.setConnectTimeout(15000);
            connection.setReadTimeout(20000);

            int responseCode = connection.getResponseCode();
            if (responseCode != 200) {
                throw new IOException("Error HTTP " + responseCode + " al acceder a la URL");
            }

            // Read the HTML content
            StringBuilder htmlContent = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(connection.getInputStream(), "UTF-8"))) {

                String line;
                while ((line = reader.readLine()) != null) {
                    htmlContent.append(line).append("\n");
                }
            }

            // Extract and clean the text
            String cleanText = extractCleanText(htmlContent.toString(), url);

            if (cleanText == null || cleanText.trim().isEmpty()) {
                throw new IOException("No se pudo extraer texto legible de la página web");
            }

            Log.d(TAG, "Successfully extracted " + cleanText.length() + " characters from URL");
            return cleanText;

        } finally {
            connection.disconnect();
        }
    }

    /**
     * Extract clean readable text from HTML content
     */
    private String extractCleanText(String html, String url) {
        Log.d(TAG, "Cleaning HTML content, size: " + html.length());

        try {
            // Extract title
            String title = extractTitle(html, url);

            // Extract main content
            String content = extractMainContent(html, url);

            if (content == null || content.trim().isEmpty()) {
                // Fallback to basic HTML cleaning
                content = basicHtmlCleaning(html);
            }

            // Combine title and content
            StringBuilder result = new StringBuilder();
            if (title != null && !title.trim().isEmpty()) {
                result.append(title.trim()).append("\n\n");
            }

            if (content != null && !content.trim().isEmpty()) {
                result.append(content.trim());
            }

            String finalText = result.toString().trim();
            Log.d(TAG, "Final cleaned text length: " + finalText.length());

            return finalText;

        } catch (Exception e) {
            Log.e(TAG, "Error cleaning HTML content", e);
            // Fallback to basic cleaning
            return basicHtmlCleaning(html);
        }
    }

    /**
     * Extract page title
     */
    private String extractTitle(String html, String url) {
        // Try to extract from <title> tag
        String title = extractBetween(html, "<title>", "</title>");
        if (title != null) {
            title = title.trim();

            // Clean Wikipedia titles
            if (url.contains("wikipedia.org")) {
                title = title.replaceAll(" - Wikipedia.*", "");
                title = title.replaceAll(" — Wikipedia.*", "");
            }

            // Decode HTML entities
            title = decodeHtmlEntities(title);
            return title;
        }

        // Fallback: generate title from URL
        return WebToPDFProcessor.getTitleFromUrl(url);
    }

    /**
     * Extract main content from HTML
     */
    private String extractMainContent(String html, String url) {
        String content = null;

        // Wikipedia-specific extraction
        if (url.contains("wikipedia.org")) {
            Log.d(TAG, "Processing Wikipedia URL: " + url);
            content = extractWikipediaContent(html);
        }

        // General content extraction strategies
        if (content == null) {
            Log.d(TAG, "No Wikipedia content found, trying general extraction methods");
            // Try common content containers with nested div support for complex structures
            content = extractNestedDivContent(html, "<main");
            if (content == null) {
                content = extractNestedDivContent(html, "<article");
            }
            if (content == null) {
                content = extractBetween(html, "class=\"content\"", "</div>");
            }
            if (content == null) {
                // Fallback to simple extraction methods
                content = extractBetween(html, "<main", "</main>");
            }
            if (content == null) {
                content = extractBetween(html, "<article", "</article>");
            }
        }

        if (content != null) {
            Log.d(TAG, "Raw content extracted, length: " + content.length());
            content = cleanTextContent(content);
            Log.d(TAG, "Cleaned content length: " + content.length());
        } else {
            Log.w(TAG, "No content extracted from URL: " + url);
        }

        return content;
    }

    /**
     * Extract content from Wikipedia articles
     */
    private String extractWikipediaContent(String html) {
        // Look for the main content div with proper nested div handling
        String content = extractNestedDivContent(html, "<div class=\"mw-parser-output\">");

        if (content == null) {
            // Fallback to alternative Wikipedia content container
            content = extractNestedDivContent(html, "<div id=\"mw-content-text\"");
        }

        if (content != null) {
            Log.d(TAG, "Extracted Wikipedia content length: " + content.length());

            // Remove Wikipedia-specific elements
            content = content.replaceAll("\\[edit\\]", "");
            content = content.replaceAll("\\[\\d+\\]", ""); // Remove reference numbers
            content = content.replaceAll("\\[citation needed\\]", "");
            content = content.replaceAll("\\[clarification needed\\]", "");

            // Remove infoboxes and navigation elements
            content = removeElementsWithClass(content, "infobox");
            content = removeElementsWithClass(content, "navbox");
            content = removeElementsWithClass(content, "ambox");
            content = removeElementsWithClass(content, "hatnote");

            Log.d(TAG, "Cleaned Wikipedia content length: " + content.length());
        } else {
            Log.w(TAG, "No Wikipedia content found, trying general extraction");
        }

        return content;
    }

    /**
     * Clean text content by removing HTML tags and normalizing whitespace
     */
    private String cleanTextContent(String content) {
        if (content == null) return null;

        // Remove script and style tags with their content
        content = content.replaceAll("(?i)<script[^>]*>.*?</script>", " ");
        content = content.replaceAll("(?i)<style[^>]*>.*?</style>", " ");

        // Remove comments
        content = content.replaceAll("<!--.*?-->", " ");

        // Convert paragraph tags to double line breaks BEFORE removing HTML tags
        content = content.replaceAll("(?i)</p>\\s*<p[^>]*>", "\n\n");
        content = content.replaceAll("(?i)</p>", "\n\n");
        content = content.replaceAll("(?i)<p[^>]*>", "");

        // Convert div tags that likely represent paragraphs
        content = content.replaceAll("(?i)</div>\\s*<div[^>]*>", "\n\n");

        // Convert heading tags to paragraph breaks
        content = content.replaceAll("(?i)</h[1-6]>\\s*", "\n\n");
        content = content.replaceAll("(?i)<h[1-6][^>]*>", "");

        // Convert br tags to single line breaks
        content = content.replaceAll("(?i)<br[^>]*>", "\n");

        // Remove all remaining HTML tags
        content = content.replaceAll("<[^>]+>", " ");

        // Decode HTML entities
        content = decodeHtmlEntities(content);

        // Normalize whitespace WHILE preserving paragraph breaks
        // First, normalize spaces within lines
        content = content.replaceAll("[ \\t]+", " ");
        // Then, normalize multiple line breaks to double line breaks
        content = content.replaceAll("\\n{3,}", "\n\n");
        // Clean up lines that only contain spaces
        content = content.replaceAll("\\n[ \\t]*\\n", "\n\n");

        return content.trim();
    }

    /**
     * Basic HTML cleaning as fallback
     */
    private String basicHtmlCleaning(String html) {
        // Remove script, style, and other non-content tags
        html = html.replaceAll("(?i)<(script|style|nav|header|footer|aside)[^>]*>.*?</\\1>", " ");

        // Convert paragraph and heading tags to preserve structure
        html = html.replaceAll("(?i)</p>\\s*<p[^>]*>", "\n\n");
        html = html.replaceAll("(?i)</p>", "\n\n");
        html = html.replaceAll("(?i)<p[^>]*>", "");
        html = html.replaceAll("(?i)</h[1-6]>\\s*", "\n\n");
        html = html.replaceAll("(?i)<h[1-6][^>]*>", "");
        html = html.replaceAll("(?i)<br[^>]*>", "\n");

        // Remove all remaining HTML tags
        html = html.replaceAll("<[^>]+>", " ");

        // Decode HTML entities
        html = decodeHtmlEntities(html);

        // Normalize whitespace while preserving paragraph breaks
        html = html.replaceAll("[ \\t]+", " ");
        html = html.replaceAll("\\n{3,}", "\n\n");
        html = html.replaceAll("\\n[ \\t]*\\n", "\n\n");

        return html.trim();
    }

    /**
     * Remove elements with specific CSS class
     */
    private String removeElementsWithClass(String html, String className) {
        return html.replaceAll("(?i)<[^>]*class=\"[^\"]*" + className + "[^\"]*\"[^>]*>.*?</[^>]+>", " ");
    }

    /**
     * Extract content from nested div structure by properly matching opening and closing tags
     */
    private String extractNestedDivContent(String html, String startMarker) {
        int startIndex = html.indexOf(startMarker);
        if (startIndex == -1) {
            Log.d(TAG, "Start marker not found: " + startMarker);
            return null;
        }

        // Find the end of the opening tag
        startIndex = html.indexOf(">", startIndex);
        if (startIndex == -1) return null;
        startIndex++; // Move past the '>'

        Log.d(TAG, "Found start marker, starting extraction from index: " + startIndex);

        // Find the matching closing </div> by counting nested divs
        int divCount = 1; // We already have one opening div
        int currentIndex = startIndex;

        while (divCount > 0 && currentIndex < html.length()) {
            int nextOpenDiv = html.indexOf("<div", currentIndex);
            int nextCloseDiv = html.indexOf("</div>", currentIndex);

            if (nextCloseDiv == -1) {
                Log.w(TAG, "No more closing divs found, extraction incomplete");
                break; // No more closing divs
            }

            if (nextOpenDiv != -1 && nextOpenDiv < nextCloseDiv) {
                // Found opening div before closing div
                divCount++;
                currentIndex = nextOpenDiv + 4; // Move past "<div"
            } else {
                // Found closing div
                divCount--;
                if (divCount == 0) {
                    // This is our matching closing div
                    String content = html.substring(startIndex, nextCloseDiv);
                    Log.d(TAG, "Successfully extracted nested div content, length: " + content.length());
                    return content;
                }
                currentIndex = nextCloseDiv + 6; // Move past "</div>"
            }
        }

        Log.w(TAG, "Could not find matching closing div for: " + startMarker);
        return null; // Couldn't find matching closing div
    }

    /**
     * Extract text between two markers (simple version for non-nested content)
     */
    private String extractBetween(String text, String start, String end) {
        int startIndex = text.indexOf(start);
        if (startIndex == -1) return null;

        // For tags, find the end of the opening tag
        if (start.startsWith("<") && !start.endsWith(">")) {
            startIndex = text.indexOf(">", startIndex);
            if (startIndex == -1) return null;
            startIndex++;
        } else {
            startIndex += start.length();
        }

        int endIndex = text.indexOf(end, startIndex);
        if (endIndex == -1) return null;

        return text.substring(startIndex, endIndex);
    }

    /**
     * Decode common HTML entities
     */
    private String decodeHtmlEntities(String text) {
        if (text == null) return null;

        text = text.replace("&amp;", "&");
        text = text.replace("&lt;", "<");
        text = text.replace("&gt;", ">");
        text = text.replace("&quot;", "\"");
        text = text.replace("&#39;", "'");
        text = text.replace("&apos;", "'");
        text = text.replace("&nbsp;", " ");

        // Decode numeric entities (basic)
        text = text.replaceAll("&#(\\d+);", " ");
        text = text.replaceAll("&#x([0-9a-fA-F]+);", " ");

        return text;
    }

    /**
     * Validate if the URL is a valid web URL
     */
    private boolean isValidWebUrl(String url) {
        if (url == null || url.trim().isEmpty()) {
            return false;
        }

        url = url.trim().toLowerCase();
        return url.startsWith("http://") || url.startsWith("https://");
    }

    /**
     * Check if URL is supported (currently supports most web URLs)
     */
    public static boolean isSupportedUrl(String url) {
        if (url == null || url.trim().isEmpty()) {
            return false;
        }

        url = url.trim().toLowerCase();

        // Basic validation
        if (!url.startsWith("http://") && !url.startsWith("https://")) {
            return false;
        }

        // Could add specific site validation here if needed
        // For now, we support most web URLs
        return true;
    }
}