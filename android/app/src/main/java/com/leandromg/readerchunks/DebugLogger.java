package com.leandromg.readerchunks;

import android.content.Context;
import java.io.File;
import java.io.FileWriter;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class DebugLogger {
    private static final String DEBUG_FILE = "debug_logs.txt";
    private static final int MAX_LOG_LINES = 500; // Limitar tamaño del archivo
    private static Context context;

    public static void init(Context ctx) {
        context = ctx.getApplicationContext();
    }

    /**
     * Log de debug que se guarda solo en archivo
     */
    public static void d(String tag, String message) {
        if (context == null) return;

        String timestamp = new SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(new Date());
        String logEntry = String.format("[%s] %s: %s\n", timestamp, tag, message);

        // Guardar en archivo
        saveToFile(logEntry);
    }

    private static void saveToFile(String logEntry) {
        try {
            File debugFile = new File(context.getFilesDir(), DEBUG_FILE);

            // Si el archivo es muy grande, rotarlo
            if (debugFile.exists() && debugFile.length() > 50000) { // 50KB
                rotateLogFile(debugFile);
            }

            // Escribir nueva entrada
            FileWriter writer = new FileWriter(debugFile, true);
            writer.write(logEntry);
            writer.close();

        } catch (IOException e) {
            // Fallar silenciosamente en debug
        }
    }

    private static void rotateLogFile(File debugFile) {
        try {
            // Mantener solo las últimas MAX_LOG_LINES líneas
            BufferedReader reader = new BufferedReader(new FileReader(debugFile));
            StringBuilder content = new StringBuilder();
            String line;
            int lineCount = 0;

            // Contar líneas totales primero
            while ((line = reader.readLine()) != null) {
                lineCount++;
            }
            reader.close();

            // Si hay más líneas de las permitidas, mantener solo las últimas
            if (lineCount > MAX_LOG_LINES) {
                reader = new BufferedReader(new FileReader(debugFile));
                int skipLines = lineCount - MAX_LOG_LINES;

                // Saltear las primeras líneas
                for (int i = 0; i < skipLines; i++) {
                    reader.readLine();
                }

                // Guardar las últimas líneas
                while ((line = reader.readLine()) != null) {
                    content.append(line).append("\n");
                }
                reader.close();

                // Reescribir archivo
                FileWriter writer = new FileWriter(debugFile, false);
                writer.write(content.toString());
                writer.close();
            }

        } catch (IOException e) {
            // Fallar silenciosamente
        }
    }

    /**
     * Obtiene todos los logs para mostrar en modal
     */
    public static String getAllLogs() {
        if (context == null) return "Debug no inicializado";

        try {
            File debugFile = new File(context.getFilesDir(), DEBUG_FILE);
            if (!debugFile.exists()) {
                return "No hay logs de debug aún";
            }

            BufferedReader reader = new BufferedReader(new FileReader(debugFile));
            StringBuilder content = new StringBuilder();
            String line;

            while ((line = reader.readLine()) != null) {
                content.append(line).append("\n");
            }
            reader.close();

            return content.length() > 0 ? content.toString() : "Archivo de logs vacío";

        } catch (IOException e) {
            return "Error leyendo logs: " + e.getMessage();
        }
    }

    /**
     * Limpia todos los logs
     */
    public static void clearLogs() {
        if (context == null) return;

        try {
            File debugFile = new File(context.getFilesDir(), DEBUG_FILE);
            if (debugFile.exists()) {
                debugFile.delete();
            }
        } catch (Exception e) {
            // Fallar silenciosamente
        }
    }

    /**
     * Log específico para cálculo dinámico
     */
    public static void logDynamicCalc(int screenWidth, int screenHeight,
                                      int availableWidth, int availableHeight,
                                      int fontSize, float fontSizePx,
                                      int charsPerLine, int maxLines,
                                      int screenCapacity, float userMultiplier,
                                      int finalLength) {
        String message = String.format(
            "Screen: %dx%d, Available: %dx%d, Font: %dsp (%.1fpx), " +
            "CharsPerLine: %d, MaxLines: %d, Capacity: %d, Multiplier: %.1f, Final: %d",
            screenWidth, screenHeight, availableWidth, availableHeight,
            fontSize, fontSizePx, charsPerLine, maxLines, screenCapacity,
            userMultiplier, finalLength
        );

        d("DYNAMIC_CALC", message);
    }
}