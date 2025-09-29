// Test the corrected algorithm that follows your specification exactly

class DynamicSentenceSplitterCorrected {

    private static final int DEFAULT_MAX_LENGTH = 150;

    private String currentParagraph;
    private int startPointer;
    private int endPointer;
    private int maxLength;

    public DynamicSentenceSplitterCorrected(String paragraph) {
        this(paragraph, DEFAULT_MAX_LENGTH);
    }

    public DynamicSentenceSplitterCorrected(String paragraph, int maxLength) {
        this.currentParagraph = paragraph != null ? paragraph : "";
        this.maxLength = maxLength;
        this.startPointer = 0;
        this.endPointer = 0;
    }

    /**
     * Your algorithm exactly:
     * 1. Find next .!?
     * 2. If end - start > 150, split by priority: : > ; > , > space (searching backwards from 150)
     * 3. Else cut at 150
     */
    public String getNext() {
        if (startPointer >= currentParagraph.length()) {
            return null;
        }

        // Move start to current end
        startPointer = endPointer;

        // Skip leading whitespace
        while (startPointer < currentParagraph.length() &&
               Character.isWhitespace(currentParagraph.charAt(startPointer))) {
            startPointer++;
        }

        if (startPointer >= currentParagraph.length()) {
            return null;
        }

        // Find next .!?
        endPointer = findNextSentenceEnd(startPointer);

        // If end - start > 150, split into subsentences
        if (endPointer - startPointer > maxLength) {
            // Priority characters: : > ; > , > space
            char[] splitCharacters = {':', ';', ',', ' '};

            for (char splitChar : splitCharacters) {
                // Search backwards from maxLength position
                for (int position = startPointer + maxLength; position > startPointer; position--) {
                    if (position < currentParagraph.length() &&
                        currentParagraph.charAt(position) == splitChar) {

                        endPointer = position + 1; // Include the split character
                        break;
                    }
                }
                if (endPointer - startPointer <= maxLength) {
                    break; // Found a good split
                }
            }

            // If still too long, cut at maxLength
            if (endPointer - startPointer > maxLength) {
                endPointer = startPointer + maxLength;
            }
        }

        return currentParagraph.substring(startPointer, endPointer).trim();
    }

    public int getCurrentCharPosition() {
        return startPointer;
    }

    public void reset() {
        startPointer = 0;
        endPointer = 0;
    }

    private int findNextSentenceEnd(int start) {
        for (int i = start; i < currentParagraph.length(); i++) {
            char c = currentParagraph.charAt(i);
            if (c == '.' || c == '!' || c == '?') {
                return i + 1; // Include the punctuation
            }
        }
        return currentParagraph.length(); // No sentence ending found
    }
}

public class TestCorrectedAlgorithm {

    public static void main(String[] args) {
        System.out.println("=== Testing Corrected Algorithm ===\n");

        // Test 1: Basic sentence splitting by .!?
        testBasicSentenceSplitting();

        // Test 2: Priority-based sub-splitting
        testPrioritySplitting();

        // Test 3: Hard cut at 150
        testHardCut();
    }

    private static void testBasicSentenceSplitting() {
        System.out.println("1. Testing Basic Sentence Splitting by .!?:");
        System.out.println("===========================================");

        String paragraph = "First sentence is short. Second sentence is also short! Third sentence is short too?";
        DynamicSentenceSplitterCorrected splitter = new DynamicSentenceSplitterCorrected(paragraph);

        System.out.println("Paragraph: \"" + paragraph + "\"");
        System.out.println("Sub-sentences:");

        String sub;
        int count = 0;
        while ((sub = splitter.getNext()) != null) {
            count++;
            System.out.println(count + ". [" + sub.length() + " chars] \"" + sub + "\"");
        }
        System.out.println();
    }

    private static void testPrioritySplitting() {
        System.out.println("2. Testing Priority-based Splitting (: > ; > , > space):");
        System.out.println("========================================================");

        // Long sentence that needs to be split by priority
        String longSentence = "This is a very long sentence that definitely exceeds one hundred and fifty characters and contains various punctuation marks: colons, semicolons; commas, and spaces that should be used for splitting according to priority rules.";

        DynamicSentenceSplitterCorrected splitter = new DynamicSentenceSplitterCorrected(longSentence, 80); // Lower limit to force splits

        System.out.println("Long sentence (" + longSentence.length() + " chars):");
        System.out.println("\"" + longSentence + "\"");
        System.out.println("\nSub-sentences with 80-char limit:");

        String sub;
        int count = 0;
        while ((sub = splitter.getNext()) != null) {
            count++;
            System.out.println(count + ". [" + sub.length() + " chars] \"" + sub + "\"");
        }
        System.out.println();
    }

    private static void testHardCut() {
        System.out.println("3. Testing Hard Cut at 150 (no punctuation available):");
        System.out.println("======================================================");

        // Text with no punctuation to force hard cut
        String noPunctText = "Thisisaverylongstringwithoutanyspacesorpunctuationthatshouldbecutatexactlytheonehundredandfiftycharactermarkbecausetherearenoothersplitoptionsavailableinthisparticularcase";

        DynamicSentenceSplitterCorrected splitter = new DynamicSentenceSplitterCorrected(noPunctText);

        System.out.println("No-punctuation text (" + noPunctText.length() + " chars):");
        System.out.println("\"" + noPunctText + "\"");
        System.out.println("\nSub-sentences:");

        String sub;
        int count = 0;
        while ((sub = splitter.getNext()) != null) {
            count++;
            System.out.println(count + ". [" + sub.length() + " chars] \"" + sub + "\"");
        }
        System.out.println();
    }
}