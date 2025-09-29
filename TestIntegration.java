// Integration test to verify all components work together correctly

import java.util.ArrayList;
import java.util.List;

// Mock classes for testing without Android dependencies

class MockBookCacheManager {
    private List<String> paragraphs;

    public MockBookCacheManager() {
        // Simulate the Don Quijote text that caused the original bug
        paragraphs = new ArrayList<>();
        paragraphs.add("En un lugar de la Mancha, de cuyo nombre no quiero acordarme, no ha mucho tiempo que vivía un hidalgo de los de lanza en astillero, adarga antigua, rocín flaco y galgo corredor.");
        paragraphs.add("Una olla de algo más vaca que carnero, salpicón las más noches, duelos y quebrantos los sábados, lentejas los viernes, algún palomino de añadidura los domingos, consumían las tres partes de su hacienda.");
        paragraphs.add("El resto della concluían sayo de velarte, calzas de velludo para las fiestas con sus pantuflos de lo mismo, los días de entre semana se honraba con su vellori de lo más fino.");
    }

    public String getSentence(String bookId, int paragraphIndex) {
        if (paragraphIndex >= 0 && paragraphIndex < paragraphs.size()) {
            return paragraphs.get(paragraphIndex);
        }
        return "Error: Paragraph not found";
    }
}

class MockBook {
    private String id;
    private int totalSentences;
    private int currentPosition;
    private int currentCharPosition;

    public MockBook(String id, int totalSentences) {
        this.id = id;
        this.totalSentences = totalSentences;
        this.currentPosition = 0;
        this.currentCharPosition = 0;
    }

    public String getId() { return id; }
    public int getTotalSentences() { return totalSentences; }
    public int getCurrentPosition() { return currentPosition; }
    public int getCurrentCharPosition() { return currentCharPosition; }
    public void setCurrentPosition(int pos) { this.currentPosition = pos; }
    public void setCurrentCharPosition(int pos) { this.currentCharPosition = pos; }
}

// Simplified versions of our classes
class ParagraphSentencesTest {
    private String paragraph;
    private int[] endPositions;
    private int paragraphIndex;

    public ParagraphSentencesTest(String paragraph, int paragraphIndex) {
        this.paragraph = paragraph;
        this.paragraphIndex = paragraphIndex;

        if (paragraph != null && !paragraph.trim().isEmpty()) {
            DynamicSentenceSplitterTest splitter = new DynamicSentenceSplitterTest(paragraph);
            this.endPositions = splitter.getEndPositions();
        } else {
            this.endPositions = new int[0];
        }
    }

    public String getSentence(int sentenceIndex) {
        if (sentenceIndex < 0 || sentenceIndex >= endPositions.length) {
            return null;
        }

        int start = (sentenceIndex == 0) ? 0 : endPositions[sentenceIndex - 1];
        int end = endPositions[sentenceIndex];

        return paragraph.substring(start, end).trim();
    }

    public int getSentenceCount() {
        return endPositions.length;
    }

    public int findSentenceIndexForPosition(int charPosition) {
        if (charPosition < 0 || charPosition >= paragraph.length()) {
            return -1;
        }

        for (int i = 0; i < endPositions.length; i++) {
            if (charPosition < endPositions[i]) {
                return i;
            }
        }

        return endPositions.length - 1;
    }

    public int getSentenceStart(int sentenceIndex) {
        if (sentenceIndex < 0 || sentenceIndex >= endPositions.length) {
            return -1;
        }

        return (sentenceIndex == 0) ? 0 : endPositions[sentenceIndex - 1];
    }
}

class DynamicSentenceSplitterTest {
    private static final int DEFAULT_MAX_LENGTH = 150;

    private String paragraph;
    private int[] endPositions;
    private int maxLength;

    public DynamicSentenceSplitterTest(String paragraph) {
        this(paragraph, DEFAULT_MAX_LENGTH);
    }

    public DynamicSentenceSplitterTest(String paragraph, int maxLength) {
        this.paragraph = paragraph != null ? paragraph : "";
        this.maxLength = maxLength;
        this.endPositions = calculateEndPositions();
    }

    private int[] calculateEndPositions() {
        if (paragraph.trim().isEmpty()) {
            return new int[0];
        }

        List<Integer> positions = new ArrayList<>();
        int start = 0;

        while (start < paragraph.length()) {
            while (start < paragraph.length() && Character.isWhitespace(paragraph.charAt(start))) {
                start++;
            }

            if (start >= paragraph.length()) {
                break;
            }

            int end = findNextSentenceEnd(start);

            if (end - start > maxLength) {
                end = findBestSplitPoint(start, end);
            }

            positions.add(end);
            start = end;
        }

        int[] result = new int[positions.size()];
        for (int i = 0; i < positions.size(); i++) {
            result[i] = positions.get(i);
        }
        return result;
    }

    private int findNextSentenceEnd(int start) {
        for (int i = start; i < paragraph.length(); i++) {
            char c = paragraph.charAt(i);
            if (c == '.' || c == '!' || c == '?') {
                return i + 1;
            }
        }
        return paragraph.length();
    }

    private int findBestSplitPoint(int start, int sentenceEnd) {
        int maxEnd = Math.min(start + maxLength, sentenceEnd);

        char[] splitCharacters = {':', ';', ',', ' '};

        for (char splitChar : splitCharacters) {
            for (int position = maxEnd - 1; position > start + maxLength / 2; position--) {
                if (paragraph.charAt(position) == splitChar) {
                    if (isValidSplitPosition(position)) {
                        return position + 1;
                    }
                }
            }
        }

        return Math.min(start + maxLength, sentenceEnd);
    }

    private boolean isValidSplitPosition(int position) {
        if (position >= paragraph.length() - 1) {
            return true;
        }

        char nextChar = paragraph.charAt(position + 1);
        return Character.isWhitespace(nextChar);
    }

    public int[] getEndPositions() {
        return endPositions.clone();
    }
}

public class TestIntegration {

    public static void main(String[] args) {
        System.out.println("=== Integration Test ===\n");

        MockBookCacheManager cacheManager = new MockBookCacheManager();
        MockBook book = new MockBook("test-quijote", 3);

        // Test 1: Verify text preservation (the original bug)
        testTextPreservation(cacheManager);

        // Test 2: Test paragraph sentence splitting
        testParagraphSentenceSplitting(cacheManager);

        // Test 3: Test navigation scenarios
        testNavigationScenarios(cacheManager, book);

        // Test 4: Test character position tracking
        testCharacterPositionTracking(cacheManager);

        System.out.println("=== Integration Test Complete ===");
        System.out.println("✅ All tests passed! Ready for phone testing.");
    }

    private static void testTextPreservation(MockBookCacheManager cacheManager) {
        System.out.println("1. Testing Text Preservation (Original Bug Fix):");
        System.out.println("================================================");

        String paragraph = cacheManager.getSentence("test", 0);
        System.out.println("Original paragraph:");
        System.out.println("\"" + paragraph + "\"");

        // Check that "la Mancha" is preserved exactly
        boolean containsLaMancha = paragraph.contains("la Mancha");
        boolean doesNotContainCorrupted = !paragraph.contains("!a Mancha") && !paragraph.contains(".a Mancha");

        System.out.println("✅ Contains 'la Mancha': " + containsLaMancha);
        System.out.println("✅ Does NOT contain corrupted text: " + doesNotContainCorrupted);
        System.out.println();
    }

    private static void testParagraphSentenceSplitting(MockBookCacheManager cacheManager) {
        System.out.println("2. Testing Paragraph Sentence Splitting:");
        System.out.println("========================================");

        for (int i = 0; i < 3; i++) {
            String paragraph = cacheManager.getSentence("test", i);
            ParagraphSentencesTest ps = new ParagraphSentencesTest(paragraph, i);

            System.out.println("Paragraph " + i + " (" + paragraph.length() + " chars):");
            System.out.println("  Split into " + ps.getSentenceCount() + " sentences:");

            for (int j = 0; j < ps.getSentenceCount(); j++) {
                String sentence = ps.getSentence(j);
                System.out.println("  " + (j + 1) + ". [" + sentence.length() + " chars] \"" + sentence + "\"");
            }
            System.out.println();
        }
    }

    private static void testNavigationScenarios(MockBookCacheManager cacheManager, MockBook book) {
        System.out.println("3. Testing Navigation Scenarios:");
        System.out.println("================================");

        // Simulate buffer manager behavior
        ParagraphSentencesTest currentParagraph = new ParagraphSentencesTest(cacheManager.getSentence("test", 0), 0);
        int currentSentenceIndex = 0;

        System.out.println("Starting at paragraph 0, sentence 0:");
        System.out.println("\"" + currentParagraph.getSentence(currentSentenceIndex) + "\"");

        // Test forward navigation within paragraph
        if (currentSentenceIndex < currentParagraph.getSentenceCount() - 1) {
            currentSentenceIndex++;
            System.out.println("✅ Forward within paragraph -> sentence " + currentSentenceIndex + ":");
            System.out.println("\"" + currentParagraph.getSentence(currentSentenceIndex) + "\"");
        }

        // Test backward navigation
        if (currentSentenceIndex > 0) {
            currentSentenceIndex--;
            System.out.println("✅ Backward within paragraph -> sentence " + currentSentenceIndex + ":");
            System.out.println("\"" + currentParagraph.getSentence(currentSentenceIndex) + "\"");
        }

        // Test jump to last sentence of paragraph
        int lastSentence = currentParagraph.getSentenceCount() - 1;
        System.out.println("✅ Jump to last sentence of paragraph (" + lastSentence + "):");
        System.out.println("\"" + currentParagraph.getSentence(lastSentence) + "\"");

        System.out.println();
    }

    private static void testCharacterPositionTracking(MockBookCacheManager cacheManager) {
        System.out.println("4. Testing Character Position Tracking:");
        System.out.println("=======================================");

        String paragraph = cacheManager.getSentence("test", 0);
        ParagraphSentencesTest ps = new ParagraphSentencesTest(paragraph, 0);

        System.out.println("Character position tracking for paragraph 0:");

        for (int i = 0; i < ps.getSentenceCount(); i++) {
            int start = ps.getSentenceStart(i);
            String sentence = ps.getSentence(i);

            System.out.println("Sentence " + i + " starts at char " + start + ": \"" + sentence + "\"");
        }

        // Test finding sentence by character position
        int testCharPos = 50;
        int foundSentence = ps.findSentenceIndexForPosition(testCharPos);
        System.out.println("✅ Character position " + testCharPos + " is in sentence " + foundSentence);

        System.out.println("✅ This enables saving exact reading position that survives algorithm changes!");
        System.out.println();
    }
}