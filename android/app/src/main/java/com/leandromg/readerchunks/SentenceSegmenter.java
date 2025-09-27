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

        // First split by double line breaks (empty lines that separate sections/paragraphs)
        String[] blocks = text.split("\\n\\s*\\n");

        for (int blockIndex = 0; blockIndex < blocks.length; blockIndex++) {
            String cleanBlock = blocks[blockIndex].trim();
            if (cleanBlock.isEmpty()) {
                continue;
            }

            // Replace single line breaks within the block with spaces (page width line breaks)
            cleanBlock = cleanBlock.replaceAll("\\n", " ");
            // Clean up multiple spaces
            cleanBlock = cleanBlock.replaceAll("\\s+", " ").trim();

            // If it's a short block (likely a title, author, section header), treat as one sentence
            if (cleanBlock.length() < 50 && !cleanBlock.matches(".*[.!?]\\s*$")) {
                sentences.add(cleanBlock + ".");
            } else {
                // For longer blocks, split by sentence-ending punctuation
                String[] parts = SENTENCE_PATTERN.split(cleanBlock);

                for (String part : parts) {
                    String trimmed = part.trim();
                    if (!trimmed.isEmpty()) {
                        // Add back the period for readability
                        sentences.add(trimmed + ".");
                    }
                }

                // If no sentences were found in this block (no punctuation), treat whole block as one sentence
                if (parts.length == 1 && !cleanBlock.isEmpty()) {
                    // Avoid duplicate if already added above
                    if (sentences.isEmpty() || !sentences.get(sentences.size() - 1).equals(cleanBlock + ".")) {
                        sentences.add(cleanBlock + ".");
                    }
                }
            }

            // Add paragraph break marker after each block (except the last one)
            if (blockIndex < blocks.length - 1) {
                sentences.add("[BREAK]");
            }
        }

        return sentences;
    }
}