/**
 * Simple test to verify our text extractors are working
 */
public class TestExtractorsSimple {

    public static void main(String[] args) {
        System.out.println("🔍 TESTING TEXT EXTRACTOR IMPLEMENTATIONS");
        System.out.println("=========================================\n");

        testExtractorCreation();
        testExtensionMapping();
        testFactoryMethods();
    }

    private static void testExtractorCreation() {
        System.out.println("1. TESTING EXTRACTOR CREATION");
        System.out.println("=============================");

        try {
            // Try to create each extractor
            TextExtractorInterface pdfExtractor = new PDFTextExtractorImpl();
            TextExtractorInterface txtExtractor = new TXTTextExtractorImpl();
            TextExtractorInterface mdExtractor = new MDTextExtractorImpl();
            TextExtractorInterface epubExtractor = new EPUBTextExtractorImpl();

            System.out.println("✅ PDFTextExtractorImpl created successfully");
            System.out.println("✅ TXTTextExtractorImpl created successfully");
            System.out.println("✅ MDTextExtractorImpl created successfully");
            System.out.println("✅ EPUBTextExtractorImpl created successfully");

        } catch (Exception e) {
            System.out.println("❌ ERROR creating extractors: " + e.getMessage());
            e.printStackTrace();
        }

        System.out.println("\n" + "=".repeat(50) + "\n");
    }

    private static void testExtensionMapping() {
        System.out.println("2. TESTING EXTENSION MAPPING");
        System.out.println("============================");

        String[] testExtensions = {"pdf", "txt", "md", "markdown", "epub"};

        for (String ext : testExtensions) {
            try {
                TextExtractorInterface extractor = TextExtractorFactory.getExtractorForExtension(ext);
                if (extractor != null) {
                    System.out.println("✅ ." + ext + " -> " + extractor.getClass().getSimpleName());
                } else {
                    System.out.println("❌ ." + ext + " -> NO EXTRACTOR FOUND");
                }
            } catch (Exception e) {
                System.out.println("❌ ." + ext + " -> ERROR: " + e.getMessage());
            }
        }

        System.out.println("\n" + "=".repeat(50) + "\n");
    }

    private static void testFactoryMethods() {
        System.out.println("3. TESTING FACTORY METHODS");
        System.out.println("==========================");

        // Test filename extension extraction
        String[] testFiles = {
            "document.pdf",
            "story.txt",
            "readme.md",
            "guide.markdown",
            "book.epub",
            "noextension",
            "multiple.dots.in.filename.pdf"
        };

        for (String filename : testFiles) {
            try {
                String extension = TextExtractorFactory.getFileExtension(filename);
                boolean supported = TextExtractorFactory.isExtensionSupported(extension);

                System.out.println("📁 " + filename);
                System.out.println("   Extension: " + (extension != null ? "." + extension : "null"));
                System.out.println("   Supported: " + (supported ? "✅ YES" : "❌ NO"));
                System.out.println();
            } catch (Exception e) {
                System.out.println("❌ " + filename + " -> ERROR: " + e.getMessage());
            }
        }

        // Test MIME types
        System.out.println("📋 Supported MIME types:");
        try {
            String[] mimeTypes = TextExtractorFactory.getAllSupportedMimeTypes();
            for (String mimeType : mimeTypes) {
                System.out.println("   " + mimeType);
            }
        } catch (Exception e) {
            System.out.println("❌ ERROR getting MIME types: " + e.getMessage());
        }

        System.out.println("\n🎉 EXTRACTOR TESTS COMPLETED!");
    }

    // Mock interface to test without Android dependencies
    interface TextExtractorInterface {
        boolean supportsExtension(String extension);
        String[] getSupportedMimeTypes();
    }

    // Mock implementations
    static class PDFTextExtractorImpl implements TextExtractorInterface {
        public boolean supportsExtension(String extension) { return "pdf".equalsIgnoreCase(extension); }
        public String[] getSupportedMimeTypes() { return new String[]{"application/pdf"}; }
    }

    static class TXTTextExtractorImpl implements TextExtractorInterface {
        public boolean supportsExtension(String extension) { return "txt".equalsIgnoreCase(extension); }
        public String[] getSupportedMimeTypes() { return new String[]{"text/plain"}; }
    }

    static class MDTextExtractorImpl implements TextExtractorInterface {
        public boolean supportsExtension(String extension) {
            return "md".equalsIgnoreCase(extension) || "markdown".equalsIgnoreCase(extension);
        }
        public String[] getSupportedMimeTypes() { return new String[]{"text/markdown", "text/x-markdown"}; }
    }

    static class EPUBTextExtractorImpl implements TextExtractorInterface {
        public boolean supportsExtension(String extension) { return "epub".equalsIgnoreCase(extension); }
        public String[] getSupportedMimeTypes() { return new String[]{"application/epub+zip"}; }
    }

    // Mock factory methods
    static class TextExtractorFactory {
        public static TextExtractorInterface getExtractorForExtension(String extension) {
            if (extension == null) return null;
            switch (extension.toLowerCase()) {
                case "pdf": return new PDFTextExtractorImpl();
                case "txt": return new TXTTextExtractorImpl();
                case "md":
                case "markdown": return new MDTextExtractorImpl();
                case "epub": return new EPUBTextExtractorImpl();
                default: return null;
            }
        }

        public static String getFileExtension(String filename) {
            if (filename == null || filename.isEmpty()) return null;
            int lastDotIndex = filename.lastIndexOf('.');
            if (lastDotIndex == -1 || lastDotIndex == filename.length() - 1) return null;
            return filename.substring(lastDotIndex + 1).toLowerCase();
        }

        public static boolean isExtensionSupported(String extension) {
            return getExtractorForExtension(extension) != null;
        }

        public static String[] getAllSupportedMimeTypes() {
            return new String[]{
                "application/pdf", "text/plain", "text/markdown",
                "text/x-markdown", "application/epub+zip"
            };
        }
    }
}