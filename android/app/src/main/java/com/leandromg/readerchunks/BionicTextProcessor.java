package com.leandromg.readerchunks;

import android.text.SpannableString;
import android.text.style.StyleSpan;
import android.graphics.Typeface;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

public class BionicTextProcessor {

    // Pattern to match words (letters, numbers, and some special characters)
    private static final Pattern WORD_PATTERN = Pattern.compile("\\b[\\p{L}\\p{N}]+\\b");

    public enum BionicMode {
        OFF,      // No bionic reading
        CLASSIC,  // Classic: min(max(wordLength / 2, 1), 7) - all words get at least 1 letter
        MODERN    // Modern: min(ROUNDDOWN(wordLength / 2, 1), 7) - single letters not marked
    }

    /**
     * Process text with bionic reading formatting
     * @param text The input text to process
     * @param mode The bionic reading mode to use
     * @return SpannableString with bionic formatting applied
     */
    public static SpannableString process(String text, BionicMode mode) {
        if (text == null || text.isEmpty() || mode == BionicMode.OFF) {
            return new SpannableString(text == null ? "" : text);
        }

        SpannableString spannable = new SpannableString(text);
        Matcher matcher = WORD_PATTERN.matcher(text);

        while (matcher.find()) {
            String word = matcher.group();
            int wordLength = word.length();

            int lettersToBold = calculateBoldLetters(wordLength, mode);

            // Apply bold style to the first N letters
            if (lettersToBold > 0 && lettersToBold <= wordLength) {
                StyleSpan boldSpan = new StyleSpan(Typeface.BOLD);
                spannable.setSpan(boldSpan, matcher.start(), matcher.start() + lettersToBold,
                                SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
        }

        return spannable;
    }

    /**
     * Overload for backward compatibility - uses CLASSIC mode
     */
    public static SpannableString process(String text) {
        return process(text, BionicMode.CLASSIC);
    }

    /**
     * Calculate the number of letters to bold for a given word length
     * @param wordLength The length of the word
     * @param mode The bionic reading mode
     * @return Number of letters to bold
     */
    public static int calculateBoldLetters(int wordLength, BionicMode mode) {
        if (wordLength <= 0 || mode == BionicMode.OFF) return 0;

        switch (mode) {
            case CLASSIC:
                // Classic: min(max(wordLength / 2, 1), 7) - all words get at least 1 letter
                return Math.min(Math.max(wordLength / 2, 1), 7);

            case MODERN:
                // Modern: min(ROUNDDOWN(wordLength / 2.1), 7) - Excel formula exactly
                // Division by 2.1 gives the table you provided
                return Math.min((int) Math.floor(wordLength / 2.1), 7);

            default:
                return 0;
        }
    }

    /**
     * Overload for backward compatibility - uses CLASSIC mode
     */
    public static int calculateBoldLetters(int wordLength) {
        return calculateBoldLetters(wordLength, BionicMode.CLASSIC);
    }

    /**
     * Test method to demonstrate both formulas
     */
    public static void testFormulas() {
        System.out.println("Bionic Reading Formula Test:");
        System.out.println("Word Length -> Classic -> Modern");

        for (int i = 1; i <= 20; i++) {
            int classic = calculateBoldLetters(i, BionicMode.CLASSIC);
            int modern = calculateBoldLetters(i, BionicMode.MODERN);
            System.out.println(i + " -> " + classic + " -> " + modern);
        }

        /*
        Expected output with division by 2.1:
        Word Length -> Classic -> Modern
        1 -> 1 -> 0  (1/2.1 = 0.47 → 0)
        2 -> 1 -> 0  (2/2.1 = 0.95 → 0)
        3 -> 1 -> 1  (3/2.1 = 1.42 → 1)
        4 -> 2 -> 1  (4/2.1 = 1.90 → 1)
        5 -> 2 -> 2  (5/2.1 = 2.38 → 2)
        6 -> 3 -> 2  (6/2.1 = 2.85 → 2)
        7 -> 3 -> 3  (7/2.1 = 3.33 → 3)
        8 -> 4 -> 3  (8/2.1 = 3.80 → 3)
        9 -> 4 -> 4  (9/2.1 = 4.28 → 4)
        10 -> 5 -> 4 (10/2.1 = 4.76 → 4)
        11 -> 5 -> 5 (11/2.1 = 5.23 → 5)
        12 -> 6 -> 5 (12/2.1 = 5.71 → 5)
        13 -> 6 -> 6 (13/2.1 = 6.19 → 6)
        14 -> 7 -> 6 (14/2.1 = 6.66 → 6)
        15+ -> 7 -> 7 (15/2.1 = 7.14 → 7, max)
        */
    }
}