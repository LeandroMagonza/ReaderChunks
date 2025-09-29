package com.leandromg.readerchunks;

import android.content.Context;
import android.net.Uri;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

/**
 * TXT text extractor implementation
 * Supports plain text files with UTF-8 encoding
 */
public class TXTTextExtractorImpl implements TextExtractor {

    @Override
    public String extractTextFromUri(Context context, Uri uri) throws IOException {
        InputStream inputStream = null;
        BufferedReader reader = null;

        try {
            inputStream = context.getContentResolver().openInputStream(uri);
            if (inputStream == null) {
                throw new IOException("No se pudo abrir el archivo TXT");
            }

            reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
            StringBuilder content = new StringBuilder();
            String line;

            while ((line = reader.readLine()) != null) {
                content.append(line).append("\n");
            }

            String text = content.toString().trim();

            if (text.isEmpty()) {
                throw new IOException("El archivo TXT está vacío");
            }

            return text;

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
                throw new IOException("No se pudo abrir el archivo TXT");
            }

            reader = new BufferedReader(new InputStreamReader(inputStream));
            StringBuilder content = new StringBuilder();
            String line;

            while ((line = reader.readLine()) != null) {
                content.append(line).append("\n");
            }

            String text = content.toString().trim();

            if (text.isEmpty()) {
                throw new IOException("El archivo TXT está vacío");
            }

            return text;

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

    @Override
    public boolean supportsExtension(String extension) {
        return "txt".equalsIgnoreCase(extension);
    }

    @Override
    public String[] getSupportedMimeTypes() {
        return new String[]{"text/plain"};
    }
}