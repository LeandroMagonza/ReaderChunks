package com.leandromg.readerchunks;

import android.content.Context;
import android.net.Uri;
import java.io.IOException;

/**
 * Interface for extracting text from different file formats
 */
public interface TextExtractor {

    /**
     * Extract text content from a file URI
     * @param context Android context
     * @param uri File URI
     * @return Extracted text content
     * @throws IOException if extraction fails
     */
    String extractTextFromUri(Context context, Uri uri) throws IOException;

    /**
     * Check if this extractor supports the given file extension
     * @param extension File extension (without dot, e.g. "pdf", "txt")
     * @return true if supported, false otherwise
     */
    boolean supportsExtension(String extension);

    /**
     * Get the supported MIME types for file picker
     * @return Array of MIME types
     */
    String[] getSupportedMimeTypes();
}