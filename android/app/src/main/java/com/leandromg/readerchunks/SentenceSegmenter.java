package com.leandromg.readerchunks;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class SentenceSegmenter {

    private static final Pattern SENTENCE_PATTERN = Pattern.compile("[.!?]+");

    public static List<String> segmentIntoSentences(String text) {
        List<String> sentences = new ArrayList<>();

        if (text == null || text.trim().isEmpty()) {
            return sentences;
        }

        // Clean up the text
        text = text.replaceAll("\\s+", " ").trim();

        // Split by sentence-ending punctuation
        String[] parts = SENTENCE_PATTERN.split(text);

        for (String part : parts) {
            String trimmed = part.trim();
            if (!trimmed.isEmpty()) {
                // Add back the period for readability
                sentences.add(trimmed + ".");
            }
        }

        // If no sentences were found (no punctuation), treat whole text as one sentence
        if (sentences.isEmpty() && !text.isEmpty()) {
            sentences.add(text + ".");
        }

        return sentences;
    }
}