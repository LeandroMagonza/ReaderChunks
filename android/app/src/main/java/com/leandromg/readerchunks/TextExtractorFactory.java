package com.leandromg.readerchunks;

import android.net.Uri;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Factory class for creating appropriate text extractors based on file type
 */
public class TextExtractorFactory {

    private static final List<TextExtractor> EXTRACTORS;

    static {
        EXTRACTORS = Arrays.asList(
                new PDFTextExtractorImpl(),
                new TXTTextExtractorImpl(),
                new MDTextExtractorImpl(),
                new EPUBTextExtractorImpl()
        );

        // Log initialization for debugging
        android.util.Log.d("TextExtractorFactory", "Initialized " + EXTRACTORS.size() + " extractors:");
        for (TextExtractor extractor : EXTRACTORS) {
            android.util.Log.d("TextExtractorFactory", "  - " + extractor.getClass().getSimpleName());
        }
    }

    /**
     * Get the appropriate text extractor for a file URI
     * @param uri File URI
     * @return TextExtractor instance or null if format is not supported
     */
    public static TextExtractor getExtractorForUri(Uri uri) {
        String extension = getFileExtension(uri);
        if (extension == null) {
            return null;
        }

        for (TextExtractor extractor : EXTRACTORS) {
            if (extractor.supportsExtension(extension)) {
                return extractor;
            }
        }

        return null;
    }

    /**
     * Get the appropriate text extractor for a file extension
     * @param extension File extension (without dot)
     * @return TextExtractor instance or null if format is not supported
     */
    public static TextExtractor getExtractorForExtension(String extension) {
        android.util.Log.d("TextExtractorFactory", "Looking for extractor for extension: " + extension);

        if (extension == null) {
            android.util.Log.w("TextExtractorFactory", "Extension is null");
            return null;
        }

        for (TextExtractor extractor : EXTRACTORS) {
            android.util.Log.d("TextExtractorFactory", "Checking extractor: " + extractor.getClass().getSimpleName());
            if (extractor.supportsExtension(extension)) {
                android.util.Log.d("TextExtractorFactory", "Found matching extractor: " + extractor.getClass().getSimpleName());
                return extractor;
            }
        }

        android.util.Log.w("TextExtractorFactory", "No extractor found for extension: " + extension);
        return null;
    }

    /**
     * Get all supported MIME types for file picker
     * @return Array of all supported MIME types
     */
    public static String[] getAllSupportedMimeTypes() {
        List<String> allMimeTypes = new ArrayList<>();

        for (TextExtractor extractor : EXTRACTORS) {
            String[] mimeTypes = extractor.getSupportedMimeTypes();
            allMimeTypes.addAll(Arrays.asList(mimeTypes));
        }

        return allMimeTypes.toArray(new String[0]);
    }

    /**
     * Get all supported file extensions
     * @return List of supported extensions (without dots)
     */
    public static List<String> getAllSupportedExtensions() {
        List<String> extensions = new ArrayList<>();

        // Add known extensions for each extractor
        extensions.add("pdf");
        extensions.add("txt");
        extensions.add("md");
        extensions.add("markdown");
        extensions.add("epub");

        return extensions;
    }

    /**
     * Check if a file extension is supported
     * @param extension File extension (without dot)
     * @return true if supported, false otherwise
     */
    public static boolean isExtensionSupported(String extension) {
        return getExtractorForExtension(extension) != null;
    }

    /**
     * Get human-readable description of supported formats
     * @return Formatted string listing supported formats
     */
    public static String getSupportedFormatsDescription() {
        return "PDF, TXT, Markdown (MD), EPUB";
    }

    /**
     * Extract file extension from URI
     * @param uri File URI
     * @return File extension without dot, or null if not found
     */
    private static String getFileExtension(Uri uri) {
        String path = uri.getPath();
        if (path == null) {
            return null;
        }

        int lastDotIndex = path.lastIndexOf('.');
        if (lastDotIndex == -1 || lastDotIndex == path.length() - 1) {
            return null;
        }

        return path.substring(lastDotIndex + 1).toLowerCase();
    }

    /**
     * Extract file extension from filename
     * @param filename File name
     * @return File extension without dot, or null if not found
     */
    public static String getFileExtension(String filename) {
        if (filename == null || filename.isEmpty()) {
            return null;
        }

        int lastDotIndex = filename.lastIndexOf('.');
        if (lastDotIndex == -1 || lastDotIndex == filename.length() - 1) {
            return null;
        }

        return filename.substring(lastDotIndex + 1).toLowerCase();
    }
}