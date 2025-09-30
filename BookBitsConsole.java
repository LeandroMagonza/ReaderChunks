import java.io.IOException;
import java.util.List;
import java.util.Scanner;

public class BookBitsConsole {

    public static void main(String[] args) {
        if (args.length != 1) {
            System.out.println("Usage: java BookBitsConsole <path-to-pdf>");
            System.exit(1);
        }

        String pdfPath = args[0];

        try {
            System.out.println("📱 ReaderChunks - Console Version");
            System.out.println("==================================");

            String text = PDFTextExtractor.extractText(pdfPath);
            System.out.println("✅ PDF text extracted successfully");

            List<String> sentences = SentenceSegmenter.segmentIntoSentences(text);
            System.out.println("✅ Text segmented into " + sentences.size() + " sentences");

            startReader(sentences);

        } catch (IOException e) {
            System.err.println("❌ Error processing PDF: " + e.getMessage());
            System.exit(1);
        } catch (Exception e) {
            System.err.println("❌ Unexpected error: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }

    private static void startReader(List<String> sentences) {
        Scanner scanner = new Scanner(System.in);
        int currentIndex = 0;

        System.out.println("\n📖 Starting ReaderChunks experience...");
        System.out.println("Commands: 'n' = next, 'p' = previous, 'q' = quit, 'h' = help");
        System.out.println("=========================================================\n");

        while (currentIndex < sentences.size()) {
            displaySentence(currentIndex, sentences);

            System.out.print("\n> ");
            String command = scanner.nextLine().trim().toLowerCase();

            switch (command) {
                case "n":
                case "next":
                case "":
                    if (currentIndex < sentences.size() - 1) {
                        currentIndex++;
                    } else {
                        System.out.println("🎉 You've reached the end! Well done!");
                        return;
                    }
                    break;

                case "p":
                case "prev":
                case "previous":
                    if (currentIndex > 0) {
                        currentIndex--;
                    } else {
                        System.out.println("ℹ️ You're at the beginning");
                    }
                    break;

                case "q":
                case "quit":
                case "exit":
                    System.out.println("👋 Thanks for using ReaderChunks!");
                    return;

                case "h":
                case "help":
                    showHelp();
                    break;

                default:
                    System.out.println("❓ Unknown command. Type 'h' for help.");
                    break;
            }
        }

        scanner.close();
    }

    private static void displaySentence(int index, List<String> sentences) {
        System.out.println("┌─────────────────────────────────────────────────────────┐");
        System.out.printf("│ Progress: %d/%d (%.1f%%)%s│%n",
                index + 1,
                sentences.size(),
                ((double) (index + 1) / sentences.size()) * 100,
                " ".repeat(Math.max(0, 32 - String.valueOf(index + 1).length() - String.valueOf(sentences.size()).length())));
        System.out.println("├─────────────────────────────────────────────────────────┤");
        System.out.printf("│ %s%s │%n",
                sentences.get(index),
                " ".repeat(Math.max(0, 55 - sentences.get(index).length())));
        System.out.println("└─────────────────────────────────────────────────────────┘");
    }

    private static void showHelp() {
        System.out.println("\n📖 ReaderChunks Help");
        System.out.println("Commands:");
        System.out.println("  'n' or 'next' or [Enter] - Go to next sentence");
        System.out.println("  'p' or 'prev'           - Go to previous sentence");
        System.out.println("  'q' or 'quit'           - Exit the application");
        System.out.println("  'h' or 'help'           - Show this help");
        System.out.println();
    }
}