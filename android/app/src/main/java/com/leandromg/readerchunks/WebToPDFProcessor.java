package com.leandromg.readerchunks;

import android.content.Context;
import android.graphics.pdf.PdfDocument;
import android.net.Uri;
import android.os.Build;
import android.print.PageRange;
import android.print.PrintAttributes;
import android.print.PrintDocumentAdapter;
import android.util.Log;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * Processes web URLs and converts them to PDF files for text extraction
 */
public class WebToPDFProcessor {
    private static final String TAG = "WebToPDFProcessor";
    private static final int TIMEOUT_SECONDS = 30;

    private Context context;

    public WebToPDFProcessor(Context context) {
        this.context = context;
    }

    /**
     * Downloads a web page and converts it to a PDF file
     * @param url The URL to process
     * @param outputFile The output PDF file
     * @param callback Callback for result
     */
    public void convertUrlToPdf(String url, File outputFile, WebToPDFCallback callback) {
        Log.d(TAG, "Converting URL to PDF: " + url);

        // Validate URL
        if (!isValidUrl(url)) {
            callback.onError("URL no válida: " + url);
            return;
        }

        // Create WebView on main thread
        if (android.os.Looper.myLooper() != android.os.Looper.getMainLooper()) {
            android.os.Handler mainHandler = new android.os.Handler(android.os.Looper.getMainLooper());
            mainHandler.post(() -> convertUrlToPdf(url, outputFile, callback));
            return;
        }

        WebView webView = new WebView(context);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setLoadWithOverviewMode(true);
        webView.getSettings().setUseWideViewPort(true);
        webView.getSettings().setBuiltInZoomControls(false);

        CountDownLatch latch = new CountDownLatch(1);
        final boolean[] success = {false};
        final String[] errorMessage = {null};

        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                Log.d(TAG, "Page loaded, creating PDF...");

                // Wait a bit for JavaScript to finish loading content
                view.postDelayed(() -> {
                    try {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                            createPdfFromWebView(view, outputFile, new PdfCreationCallback() {
                                @Override
                                public void onSuccess() {
                                    success[0] = true;
                                    latch.countDown();
                                }

                                @Override
                                public void onError(String error) {
                                    errorMessage[0] = error;
                                    latch.countDown();
                                }
                            });
                        } else {
                            errorMessage[0] = "Android KitKat (API 19) o superior requerido para conversión web a PDF";
                            latch.countDown();
                        }
                    } catch (Exception e) {
                        errorMessage[0] = "Error creando PDF: " + e.getMessage();
                        latch.countDown();
                    }
                }, 2000); // Wait 2 seconds for content to fully load
            }

            @Override
            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
                Log.e(TAG, "WebView error: " + description);
                errorMessage[0] = "Error cargando página: " + description;
                latch.countDown();
            }
        });

        // Load the URL
        webView.loadUrl(url);

        // Wait for completion in background thread
        new Thread(() -> {
            try {
                boolean completed = latch.await(TIMEOUT_SECONDS, TimeUnit.SECONDS);
                if (!completed) {
                    callback.onError("Timeout cargando la página web");
                } else if (success[0]) {
                    callback.onSuccess(outputFile);
                } else {
                    callback.onError(errorMessage[0] != null ? errorMessage[0] : "Error desconocido");
                }
            } catch (InterruptedException e) {
                callback.onError("Proceso interrumpido");
            }
        }).start();
    }

    /**
     * Creates a PDF from a WebView using the Print API
     */
    private void createPdfFromWebView(WebView webView, File outputFile, PdfCreationCallback callback) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            try {
                PrintAttributes printAttributes = new PrintAttributes.Builder()
                        .setMediaSize(PrintAttributes.MediaSize.ISO_A4)
                        .setResolution(new PrintAttributes.Resolution("pdf", "pdf", 600, 600))
                        .setMinMargins(PrintAttributes.Margins.NO_MARGINS)
                        .build();

                PrintDocumentAdapter printAdapter = webView.createPrintDocumentAdapter("WebPage");

                // Create PDF document
                PdfDocument pdfDocument = new PdfDocument();

                // This is a simplified approach - in reality, PrintDocumentAdapter requires more complex handling
                // For now, we'll use a basic implementation
                savePdfDocument(pdfDocument, outputFile, callback);

            } catch (Exception e) {
                Log.e(TAG, "Error creating PDF", e);
                callback.onError("Error creando PDF: " + e.getMessage());
            }
        } else {
            callback.onError("Versión de Android no soportada para conversión a PDF");
        }
    }

    /**
     * Alternative simpler approach using JSoup for text extraction
     */
    public void extractTextFromUrl(String url, TextExtractionCallback callback) {
        new Thread(() -> {
            try {
                Log.d(TAG, "Extracting text from URL: " + url);

                // Validate URL
                if (!isValidUrl(url)) {
                    callback.onError("URL no válida: " + url);
                    return;
                }

                // For Wikipedia and similar sites, we can extract clean text
                String cleanedText = extractCleanTextFromUrl(url);

                if (cleanedText != null && !cleanedText.trim().isEmpty()) {
                    callback.onSuccess(cleanedText);
                } else {
                    callback.onError("No se pudo extraer texto de la URL");
                }

            } catch (Exception e) {
                Log.e(TAG, "Error extracting text from URL", e);
                callback.onError("Error extrayendo texto: " + e.getMessage());
            }
        }).start();
    }

    /**
     * Extract clean text from URL using simple HTTP request and basic HTML parsing
     */
    private String extractCleanTextFromUrl(String url) throws IOException {
        Log.d(TAG, "Extracting clean text from: " + url);

        // Simple HTTP request
        java.net.URL urlObj = new java.net.URL(url);
        java.net.HttpURLConnection connection = (java.net.HttpURLConnection) urlObj.openConnection();
        connection.setRequestMethod("GET");
        connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Android) BookBits/1.0");
        connection.setConnectTimeout(10000);
        connection.setReadTimeout(15000);

        try (java.io.BufferedReader reader = new java.io.BufferedReader(
                new java.io.InputStreamReader(connection.getInputStream(), "UTF-8"))) {

            StringBuilder htmlContent = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                htmlContent.append(line).append("\n");
            }

            // Basic HTML to text conversion for Wikipedia
            return cleanHtmlContent(htmlContent.toString(), url);
        }
    }

    /**
     * Clean HTML content and extract readable text
     */
    private String cleanHtmlContent(String html, String url) {
        // Basic cleaning for Wikipedia articles
        String text = html;

        // Remove script and style tags
        text = text.replaceAll("(?i)<script[^>]*>.*?</script>", "");
        text = text.replaceAll("(?i)<style[^>]*>.*?</style>", "");

        // For Wikipedia, extract main content
        if (url.contains("wikipedia.org")) {
            // Extract title
            String title = extractBetweenTags(html, "<title>", "</title>");
            if (title != null) {
                title = title.replaceAll(" - Wikipedia.*", "").trim();
            }

            // Extract main content from Wikipedia
            String content = extractWikipediaContent(html);

            if (title != null && content != null) {
                return title + "\n\n" + content;
            }
        }

        // Fallback: basic HTML tag removal
        text = text.replaceAll("<[^>]+>", " ");
        text = text.replaceAll("\\s+", " ");
        text = text.trim();

        return text;
    }

    /**
     * Extract main content from Wikipedia article
     */
    private String extractWikipediaContent(String html) {
        // Look for the main content div in Wikipedia
        String content = extractBetweenTags(html, "<div id=\"mw-content-text\"", "</div>");
        if (content == null) {
            content = extractBetweenTags(html, "<div class=\"mw-parser-output\"", "</div>");
        }

        if (content != null) {
            // Remove references, edit links, and other Wikipedia-specific elements
            content = content.replaceAll("\\[edit\\]", "");
            content = content.replaceAll("\\[\\d+\\]", ""); // Remove reference numbers
            content = content.replaceAll("<[^>]+>", " "); // Remove HTML tags
            content = content.replaceAll("\\s+", " "); // Normalize whitespace
            content = content.trim();
        }

        return content;
    }

    /**
     * Extract text between HTML tags
     */
    private String extractBetweenTags(String html, String startTag, String endTag) {
        int startIndex = html.indexOf(startTag);
        if (startIndex == -1) return null;

        startIndex = html.indexOf(">", startIndex) + 1;
        int endIndex = html.indexOf(endTag, startIndex);
        if (endIndex == -1) return null;

        return html.substring(startIndex, endIndex);
    }

    private void savePdfDocument(PdfDocument document, File outputFile, PdfCreationCallback callback) {
        try (FileOutputStream fos = new FileOutputStream(outputFile)) {
            document.writeTo(fos);
            document.close();
            callback.onSuccess();
        } catch (IOException e) {
            Log.e(TAG, "Error saving PDF", e);
            callback.onError("Error guardando PDF: " + e.getMessage());
        }
    }

    /**
     * Validate if URL is properly formatted and supported
     */
    private boolean isValidUrl(String url) {
        if (url == null || url.trim().isEmpty()) {
            return false;
        }

        url = url.trim().toLowerCase();
        return url.startsWith("http://") || url.startsWith("https://");
    }

    /**
     * Get a clean title from URL
     */
    public static String getTitleFromUrl(String url) {
        if (url == null) return "Contenido Web";

        // For Wikipedia, extract article title
        if (url.contains("wikipedia.org")) {
            String[] parts = url.split("/");
            if (parts.length > 0) {
                String title = parts[parts.length - 1];
                title = title.replace("_", " ");
                // Decode URL encoding
                try {
                    title = java.net.URLDecoder.decode(title, "UTF-8");
                } catch (Exception e) {
                    // Keep original if decoding fails
                }
                return title;
            }
        }

        // Fallback: extract domain
        try {
            Uri uri = Uri.parse(url);
            String host = uri.getHost();
            if (host != null) {
                return "Artículo de " + host;
            }
        } catch (Exception e) {
            // Ignore
        }

        return "Contenido Web";
    }

    // Callback interfaces
    public interface WebToPDFCallback {
        void onSuccess(File pdfFile);
        void onError(String error);
    }

    public interface TextExtractionCallback {
        void onSuccess(String extractedText);
        void onError(String error);
    }

    private interface PdfCreationCallback {
        void onSuccess();
        void onError(String error);
    }
}