import java.util.ArrayList;
import java.util.List;

// Simple versions without package declarations for testing

class SentenceSegmenterTest {
    public static List<String> segmentIntoSentences(String text) {
        List<String> paragraphs = new ArrayList<>();

        if (text == null || text.trim().isEmpty()) {
            return paragraphs;
        }

        // Split by double line breaks only (paragraph breaks)
        String[] blocks = text.split("\\n\\s*\\n");

        for (String block : blocks) {
            String cleanBlock = block.trim();
            if (cleanBlock.isEmpty()) {
                continue;
            }

            // Replace single line breaks within the block with spaces (page width line breaks)
            cleanBlock = cleanBlock.replaceAll("\\n", " ");
            // Clean up multiple spaces
            cleanBlock = cleanBlock.replaceAll("\\s+", " ").trim();

            // Add the complete paragraph without modification
            paragraphs.add(cleanBlock);
        }

        return paragraphs;
    }
}

class DynamicSentenceSplitterTest {
    private static final int DEFAULT_MAX_LENGTH = 150;

    private String currentParagraph;
    private int startPointer;
    private int endPointer;
    private int maxLength;

    public DynamicSentenceSplitterTest(String paragraph) {
        this(paragraph, DEFAULT_MAX_LENGTH);
    }

    public DynamicSentenceSplitterTest(String paragraph, int maxLength) {
        this.currentParagraph = paragraph != null ? paragraph : "";
        this.maxLength = maxLength;
        this.startPointer = 0;
        this.endPointer = 0;
    }

    public String getNext() {
        if (startPointer >= currentParagraph.length()) {
            return null;
        }

        startPointer = endPointer;

        // Skip leading whitespace
        while (startPointer < currentParagraph.length() &&
               Character.isWhitespace(currentParagraph.charAt(startPointer))) {
            startPointer++;
        }

        if (startPointer >= currentParagraph.length()) {
            return null;
        }

        // Find next sentence ending punctuation
        endPointer = findNextSentenceEnd(startPointer);

        // If the sub-sentence is too long, find best split point
        if (endPointer - startPointer > maxLength) {
            endPointer = findBestSplitPoint(startPointer, startPointer + maxLength);
        }

        return currentParagraph.substring(startPointer, endPointer).trim();
    }

    public int getCurrentCharPosition() {
        return startPointer;
    }

    public int getTotalCharacters() {
        return currentParagraph.length();
    }

    private int findNextSentenceEnd(int start) {
        for (int i = start; i < currentParagraph.length(); i++) {
            char c = currentParagraph.charAt(i);
            if (c == '.' || c == '!' || c == '?') {
                return i + 1;
            }
        }
        return currentParagraph.length();
    }

    private int findBestSplitPoint(int start, int maxEnd) {
        int bestSplitPoint = maxEnd;
        int bestPriority = -1;

        for (int i = Math.min(maxEnd, currentParagraph.length()) - 1;
             i >= Math.max(start, maxEnd - maxLength / 2); i--) {

            char c = currentParagraph.charAt(i);
            int priority = -1;

            if (c == ';' || c == ':') {
                priority = 3; // Highest priority
            } else if (c == ',') {
                priority = 2; // Medium priority
            } else if (c == ' ') {
                priority = 1; // Lowest priority
            }

            if (priority > bestPriority) {
                bestPriority = priority;
                bestSplitPoint = i + 1;
            }
        }

        return Math.min(bestSplitPoint, currentParagraph.length());
    }
}

public class TestNewAlgorithmsSimple {

    public static void main(String[] args) {
        System.out.println("=== Testing New Algorithms ===\n");

        // Test 1: SentenceSegmenter - paragraph splitting
        testSentenceSegmenter();

        // Test 2: DynamicSentenceSplitter - character position tracking
        testDynamicSentenceSplitter();

        // Test 3: The problematic text that caused the bug
        testProblematicText();
    }

    private static void testSentenceSegmenter() {
        System.out.println("1. Testing SentenceSegmenter:");
        System.out.println("================================");

        String testText = "Primer párrafo con texto normal.\n\nSegundo párrafo que contiene el texto !a Mancha muy importante.\n\nTercer párrafo final.";

        List<String> paragraphs = SentenceSegmenterTest.segmentIntoSentences(testText);

        for (int i = 0; i < paragraphs.size(); i++) {
            System.out.println("Paragraph " + i + ": \"" + paragraphs.get(i) + "\"");
        }
        System.out.println("Total paragraphs: " + paragraphs.size());
        System.out.println();
    }

    private static void testDynamicSentenceSplitter() {
        System.out.println("2. Testing DynamicSentenceSplitter:");
        System.out.println("===================================");

        String paragraph = "Este es un párrafo muy largo que debería ser dividido en sub-oraciones más pequeñas; contiene varios signos de puntuación: comas, punto y coma, y otros elementos que ayudan a determinar los mejores puntos de corte para una lectura más cómoda.";

        DynamicSentenceSplitterTest splitter = new DynamicSentenceSplitterTest(paragraph);

        System.out.println("Original paragraph (" + paragraph.length() + " chars):");
        System.out.println("\"" + paragraph + "\"\n");

        System.out.println("Sub-sentences:");
        String subSentence;
        int count = 0;
        while ((subSentence = splitter.getNext()) != null) {
            count++;
            int charPos = splitter.getCurrentCharPosition();
            System.out.println(count + ". [pos " + charPos + "] \"" + subSentence + "\"");
        }
        System.out.println();
    }

    private static void testProblematicText() {
        System.out.println("3. Testing Problematic Text (the bug case):");
        System.out.println("===========================================");

        String problematicText = "En un lugar de la Mancha, de cuyo nombre no quiero acordarme, no ha mucho tiempo que vivía un hidalgo de los de lanza en astillero, adarga antigua, rocín flaco y galgo corredor. Una olla de algo más vaca que carnero, salpicón las más noches, duelos y quebrantos los sábados, lentejas los viernes, algún palomino de añadidura los domingos, consumían las tres partes de su hacienda.";

        // Test paragraph segmentation
        String textWithBreaks = "Primer párrafo antes.\n\n" + problematicText + "\n\nPárrafo después.";
        List<String> paragraphs = SentenceSegmenterTest.segmentIntoSentences(textWithBreaks);

        System.out.println("Found " + paragraphs.size() + " paragraphs");
        System.out.println("Middle paragraph contains 'la Mancha': " + paragraphs.get(1).contains("la Mancha"));
        System.out.println("Middle paragraph does NOT contain '!a': " + !paragraphs.get(1).contains("!a"));

        // Test that the text is preserved exactly
        String middleParagraph = paragraphs.get(1);
        System.out.println("Text around 'Mancha':");
        int manchaIndex = middleParagraph.indexOf("Mancha");
        if (manchaIndex > 10) {
            String context = middleParagraph.substring(manchaIndex - 10, Math.min(manchaIndex + 20, middleParagraph.length()));
            System.out.println("\"" + context + "\"");
        }

        // Test dynamic splitting
        DynamicSentenceSplitterTest splitter = new DynamicSentenceSplitterTest(middleParagraph);
        System.out.println("\nFirst few sub-sentences:");
        for (int i = 0; i < 3; i++) {
            String sub = splitter.getNext();
            if (sub != null) {
                System.out.println((i + 1) + ". \"" + sub + "\"");
            }
        }

        // Test priority-based splitting
        System.out.println("\n4. Testing Priority-based Splitting:");
        System.out.println("====================================");

        String testSentence = "Esta es una oración con coma, punto y coma; y dos puntos: que debe cortarse correctamente por prioridades.";
        DynamicSentenceSplitterTest prioritySplitter = new DynamicSentenceSplitterTest(testSentence, 50); // Short length to force splits

        System.out.println("Test sentence: \"" + testSentence + "\"");
        System.out.println("Sub-sentences with 50-char limit:");

        String sub;
        int counter = 0;
        while ((sub = prioritySplitter.getNext()) != null) {
            counter++;
            System.out.println(counter + ". \"" + sub + "\"");
        }

        System.out.println();
    }
}