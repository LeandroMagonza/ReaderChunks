package com.leandromg.readerchunks;

import android.content.Context;
import android.net.Uri;

import com.tom_roush.pdfbox.android.PDFBoxResourceLoader;
import com.tom_roush.pdfbox.pdmodel.PDDocument;
import com.tom_roush.pdfbox.text.PDFTextStripper;

import java.io.IOException;
import java.io.InputStream;

/**
 * PDF text extractor implementation using PDFBox
 */
public class PDFTextExtractorImpl implements TextExtractor {

    @Override
    public String extractTextFromUri(Context context, Uri uri) throws IOException {
        PDFBoxResourceLoader.init(context);

        InputStream inputStream = null;
        PDDocument document = null;

        try {
            inputStream = context.getContentResolver().openInputStream(uri);
            if (inputStream == null) {
                throw new IOException("No se pudo abrir el archivo PDF");
            }

            document = PDDocument.load(inputStream);

            if (document.isEncrypted()) {
                throw new IOException("El PDF está encriptado y no se puede procesar");
            }

            PDFTextStripper stripper = new PDFTextStripper();
            String text = stripper.getText(document);

            if (text == null || text.trim().isEmpty()) {
                throw new IOException("El PDF no contiene texto extraíble");
            }

            return text.trim();

        } finally {
            if (document != null) {
                try {
                    document.close();
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
        return "pdf".equalsIgnoreCase(extension);
    }

    @Override
    public String[] getSupportedMimeTypes() {
        return new String[]{"application/pdf"};
    }
}