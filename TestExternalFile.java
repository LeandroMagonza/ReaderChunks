/**
 * Test class to verify external file handling functionality
 * This simulates how the MainActivity should behave when receiving external file intents
 */
public class TestExternalFile {

    public static void main(String[] args) {
        System.out.println("🔗 TESTING EXTERNAL FILE INTENT HANDLING");
        System.out.println("========================================\n");

        testIntentPatternMatching();
        testFileExtensionHandling();
        testMimeTypeHandling();
    }

    private static void testIntentPatternMatching() {
        System.out.println("1. TESTING INTENT PATTERN MATCHING");
        System.out.println("==================================");

        // Simulate different file paths and URIs
        String[] testFiles = {
            "/storage/emulated/0/Download/document.pdf",
            "/storage/emulated/0/Documents/story.txt",
            "/sdcard/Books/novel.epub",
            "/storage/emulated/0/Notes/readme.md",
            "/data/media/0/file.markdown",
            "content://com.android.providers.downloads.documents/document/12345"
        };

        for (String filePath : testFiles) {
            String extension = extractExtension(filePath);
            String mimeType = getMimeTypeFromExtension(extension);
            boolean supported = isFormatSupported(extension);

            System.out.println("📁 File: " + filePath);
            System.out.println("   Extension: " + extension);
            System.out.println("   MIME Type: " + mimeType);
            System.out.println("   Supported: " + (supported ? "✅ YES" : "❌ NO"));
            System.out.println();
        }

        System.out.println("=".repeat(50) + "\n");
    }

    private static void testFileExtensionHandling() {
        System.out.println("2. TESTING FILE EXTENSION HANDLING");
        System.out.println("==================================");

        String[] extensions = {"pdf", "txt", "md", "markdown", "epub", "doc", "rtf"};

        for (String ext : extensions) {
            boolean supported = isFormatSupported(ext);
            String intentFilter = getIntentFilterForExtension(ext);

            System.out.println("📋 Extension: ." + ext);
            System.out.println("   Supported: " + (supported ? "✅ YES" : "❌ NO"));
            System.out.println("   Intent Filter: " + intentFilter);
            System.out.println();
        }

        System.out.println("=".repeat(50) + "\n");
    }

    private static void testMimeTypeHandling() {
        System.out.println("3. TESTING MIME TYPE HANDLING");
        System.out.println("=============================");

        String[] mimeTypes = {
            "application/pdf",
            "text/plain",
            "text/markdown",
            "text/x-markdown",
            "application/epub+zip",
            "application/msword",
            "application/vnd.ms-excel"
        };

        for (String mimeType : mimeTypes) {
            boolean supported = isMimeTypeSupported(mimeType);
            String extension = getExtensionFromMimeType(mimeType);

            System.out.println("📎 MIME Type: " + mimeType);
            System.out.println("   Supported: " + (supported ? "✅ YES" : "❌ NO"));
            System.out.println("   Extension: " + extension);
            System.out.println();
        }

        System.out.println("=".repeat(50) + "\n");
        System.out.println("🎉 EXTERNAL FILE INTENT TEST COMPLETED!");
    }

    // Helper methods to simulate the logic
    private static String extractExtension(String filePath) {
        if (filePath == null) return "";

        // Handle content:// URIs
        if (filePath.startsWith("content://")) {
            return "content"; // Placeholder - real implementation would query ContentResolver
        }

        int lastDot = filePath.lastIndexOf('.');
        if (lastDot == -1 || lastDot == filePath.length() - 1) {
            return "";
        }

        return filePath.substring(lastDot + 1).toLowerCase();
    }

    private static String getMimeTypeFromExtension(String extension) {
        switch (extension.toLowerCase()) {
            case "pdf": return "application/pdf";
            case "txt": return "text/plain";
            case "md": return "text/markdown";
            case "markdown": return "text/x-markdown";
            case "epub": return "application/epub+zip";
            default: return "application/octet-stream";
        }
    }

    private static boolean isFormatSupported(String extension) {
        return "pdf".equals(extension) || "txt".equals(extension) ||
               "md".equals(extension) || "markdown".equals(extension) ||
               "epub".equals(extension);
    }

    private static boolean isMimeTypeSupported(String mimeType) {
        return "application/pdf".equals(mimeType) ||
               "text/plain".equals(mimeType) ||
               "text/markdown".equals(mimeType) ||
               "text/x-markdown".equals(mimeType) ||
               "application/epub+zip".equals(mimeType);
    }

    private static String getIntentFilterForExtension(String extension) {
        if (isFormatSupported(extension)) {
            return "android.intent.action.VIEW + " + getMimeTypeFromExtension(extension);
        }
        return "Not supported";
    }

    private static String getExtensionFromMimeType(String mimeType) {
        switch (mimeType) {
            case "application/pdf": return ".pdf";
            case "text/plain": return ".txt";
            case "text/markdown": return ".md";
            case "text/x-markdown": return ".markdown";
            case "application/epub+zip": return ".epub";
            default: return "unknown";
        }
    }
}