package com.leandromg.readerchunks;

/**
 * Enum to represent different types of sentence endings
 * Used to determine appropriate TTS pause durations
 */
public enum SentenceEndType {
    /**
     * End of a paragraph - longest pause
     */
    PARAGRAPH_END,

    /**
     * Natural sentence ending (.!?) - medium pause
     */
    SENTENCE_END,

    /**
     * Soft break (: ; ,) - short pause
     */
    SOFT_BREAK,

    /**
     * Cut due to character limit - no pause
     */
    CHARACTER_LIMIT
}