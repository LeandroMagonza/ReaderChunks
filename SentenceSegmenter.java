package com.leandromg.readerchunks;

import java.util.ArrayList;
import java.util.List;

public class SentenceSegmenter {

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