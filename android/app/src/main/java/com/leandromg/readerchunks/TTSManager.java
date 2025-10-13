package com.leandromg.readerchunks;

import android.content.Context;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.util.Log;
import java.util.Locale;

public class TTSManager implements TextToSpeech.OnInitListener {
    private static final String TAG = "TTSManager";
    private static final String UTTERANCE_ID = "BookBitsTTS";

    private TextToSpeech tts;
    private Context context;
    private boolean isInitialized = false;
    private boolean isEnabled = false;
    private TTSListener listener;
    private String pendingText = null;

    public interface TTSListener {
        void onTTSReady();
        void onTTSFinished();
        void onTTSError();
    }

    public TTSManager(Context context, TTSListener listener) {
        this.context = context;
        this.listener = listener;
        initializeTTS();
    }

    private void initializeTTS() {
        try {
            if (tts == null) {
                tts = new TextToSpeech(context, this);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error initializing TTS: " + e.getMessage());
            if (listener != null) {
                listener.onTTSError();
            }
        }
    }

    @Override
    public void onInit(int status) {
        if (status == TextToSpeech.SUCCESS) {
            try {
                isInitialized = true;
                Log.d(TAG, "TTS initialized successfully");

                // Set language to device default
                Locale deviceLocale = Locale.getDefault();
                int langResult = tts.setLanguage(deviceLocale);

                if (langResult == TextToSpeech.LANG_MISSING_DATA ||
                    langResult == TextToSpeech.LANG_NOT_SUPPORTED) {
                    // Try English as fallback
                    langResult = tts.setLanguage(Locale.ENGLISH);
                    if (langResult == TextToSpeech.SUCCESS) {
                        Log.d(TAG, "Using English as fallback language");
                    } else {
                        Log.w(TAG, "No supported language found");
                    }
                } else {
                    Log.d(TAG, "Using device language: " + deviceLocale.toString());
                }

                // Set default speech rate
                tts.setSpeechRate(1.0f);

                // Set up utterance progress listener
                tts.setOnUtteranceProgressListener(new UtteranceProgressListener() {
                    @Override
                    public void onStart(String utteranceId) {
                        Log.d(TAG, "TTS started speaking");
                    }

                    @Override
                    public void onDone(String utteranceId) {
                        Log.d(TAG, "TTS finished speaking");
                        if (listener != null) {
                            listener.onTTSFinished();
                        }
                    }

                    @Override
                    public void onError(String utteranceId) {
                        Log.e(TAG, "TTS error occurred");
                        if (listener != null) {
                            listener.onTTSError();
                        }
                    }
                });

                // Notify that TTS is ready
                if (listener != null) {
                    listener.onTTSReady();
                }

                // If there was pending text, speak it now
                if (pendingText != null && isEnabled) {
                    speakText(pendingText);
                    pendingText = null;
                }
            } catch (Exception e) {
                Log.e(TAG, "Error in onInit: " + e.getMessage());
                isInitialized = false;
                if (listener != null) {
                    listener.onTTSError();
                }
            }
        } else {
            Log.e(TAG, "TTS initialization failed with status: " + status);
            isInitialized = false;
            if (listener != null) {
                listener.onTTSError();
            }
        }
    }

    public void speakText(String text) {
        if (!isEnabled) {
            Log.d(TAG, "TTS is disabled, not speaking");
            return;
        }

        if (!isInitialized || tts == null) {
            Log.d(TAG, "TTS not ready, storing text for later");
            pendingText = text;
            return;
        }

        if (text == null || text.trim().isEmpty()) {
            Log.d(TAG, "Empty text, nothing to speak");
            return;
        }

        try {
            // Stop any current speech
            stop();

            // Clean text for better TTS
            String cleanText = cleanTextForTTS(text);

            Log.d(TAG, "Speaking text: " + cleanText.substring(0, Math.min(50, cleanText.length())) + "...");

            // Use QUEUE_FLUSH to stop any previous speech and start new one
            int result = tts.speak(cleanText, TextToSpeech.QUEUE_FLUSH, null, UTTERANCE_ID);

            if (result == TextToSpeech.ERROR) {
                Log.e(TAG, "Error in TTS speak");
                if (listener != null) {
                    listener.onTTSError();
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Exception in speakText: " + e.getMessage());
            if (listener != null) {
                listener.onTTSError();
            }
        }
    }

    private String cleanTextForTTS(String text) {
        if (text == null) return "";

        // Remove special markers like [BREAK]
        String cleaned = text.replaceAll("\\[BREAK\\]", "");

        // Remove excessive whitespace
        cleaned = cleaned.replaceAll("\\s+", " ").trim();

        // Replace common abbreviations for better pronunciation
        cleaned = cleaned.replaceAll("\\bDr\\.", "Doctor");
        cleaned = cleaned.replaceAll("\\bSr\\.", "Señor");
        cleaned = cleaned.replaceAll("\\bSra\\.", "Señora");
        cleaned = cleaned.replaceAll("\\betc\\.", "etcétera");

        return cleaned;
    }

    public void stop() {
        try {
            if (tts != null && isInitialized) {
                tts.stop();
                Log.d(TAG, "TTS stopped");
            }
        } catch (Exception e) {
            Log.e(TAG, "Error stopping TTS: " + e.getMessage());
        }
    }

    public boolean isSpeaking() {
        try {
            return tts != null && tts.isSpeaking();
        } catch (Exception e) {
            Log.e(TAG, "Error checking if speaking: " + e.getMessage());
            return false;
        }
    }

    public void setEnabled(boolean enabled) {
        this.isEnabled = enabled;
        Log.d(TAG, "TTS enabled: " + enabled);

        if (!enabled) {
            stop(); // Stop current speech if disabling
        }
    }

    public boolean isEnabled() {
        return isEnabled;
    }

    public void setSpeechRate(float rate) {
        try {
            if (tts != null && isInitialized) {
                // Clamp rate between 0.5 and 2.0
                float clampedRate = Math.max(0.5f, Math.min(2.0f, rate));
                tts.setSpeechRate(clampedRate);
                Log.d(TAG, "Speech rate set to: " + clampedRate);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error setting speech rate: " + e.getMessage());
        }
    }

    public boolean isInitialized() {
        return isInitialized;
    }

    public void shutdown() {
        try {
            if (tts != null) {
                stop();
                tts.shutdown();
                tts = null;
                isInitialized = false;
                Log.d(TAG, "TTS shut down");
            }
        } catch (Exception e) {
            Log.e(TAG, "Error shutting down TTS: " + e.getMessage());
        }
    }

    public void destroy() {
        shutdown();
        listener = null;
        context = null;
    }
}