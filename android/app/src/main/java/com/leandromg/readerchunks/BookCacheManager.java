package com.leandromg.readerchunks;

import android.content.Context;
import android.net.Uri;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class BookCacheManager {
    private static final String BOOKS_DIR = "books";
    private static final String LIBRARY_FILE = "library.json";
    private static final String CONTENT_FILE = "content.txt";
    private static final String META_FILE = "meta.json";

    private Context context;
    private File booksDirectory;
    private SimpleDateFormat dateFormat;

    public BookCacheManager(Context context) {
        this.context = context;
        this.booksDirectory = new File(context.getFilesDir(), BOOKS_DIR);
        this.dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());

        if (!booksDirectory.exists()) {
            booksDirectory.mkdirs();
        }
    }

    public String generateBookId(Uri uri) throws IOException {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");

            InputStream inputStream = context.getContentResolver().openInputStream(uri);
            if (inputStream == null) {
                throw new IOException("Cannot open file");
            }

            byte[] buffer = new byte[8192];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                md.update(buffer, 0, bytesRead);
            }
            inputStream.close();

            byte[] digest = md.digest();
            StringBuilder sb = new StringBuilder();
            for (byte b : digest) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();

        } catch (NoSuchAlgorithmException e) {
            return String.valueOf(uri.toString().hashCode());
        }
    }

    public Book processAndCacheBook(Uri uri, String fileName) throws IOException {
        String bookId = generateBookId(uri);

        if (isBookCached(bookId)) {
            return loadBookMeta(bookId);
        }

        String text = PDFTextExtractor.extractTextFromUri(context, uri);
        List<String> sentences = SentenceSegmenter.segmentIntoSentences(text);

        String title = extractTitleFromFileName(fileName);
        Book book = new Book(bookId, title, fileName, sentences.size());

        File bookDir = new File(booksDirectory, bookId);
        if (!bookDir.exists()) {
            bookDir.mkdirs();
        }

        saveContentToFile(bookDir, sentences);
        saveBookMeta(book);
        updateLibrary(book);

        return book;
    }

    private void saveContentToFile(File bookDir, List<String> sentences) throws IOException {
        File contentFile = new File(bookDir, CONTENT_FILE);
        BufferedWriter writer = new BufferedWriter(new FileWriter(contentFile));

        for (String sentence : sentences) {
            writer.write(sentence);
            writer.newLine();
        }

        writer.close();
    }

    public void saveBookMeta(Book book) throws IOException {
        File bookDir = new File(booksDirectory, book.getId());
        File metaFile = new File(bookDir, META_FILE);

        try {
            JSONObject json = new JSONObject();
            json.put("id", book.getId());
            json.put("title", book.getTitle());
            json.put("fileName", book.getFileName());
            json.put("totalSentences", book.getTotalSentences());
            json.put("currentPosition", book.getCurrentPosition());
            json.put("lastReadDate", dateFormat.format(book.getLastReadDate()));
            json.put("fileSizeBytes", book.getFileSizeBytes());
            json.put("processedDate", dateFormat.format(book.getProcessedDate()));

            BufferedWriter writer = new BufferedWriter(new FileWriter(metaFile));
            writer.write(json.toString(2));
            writer.close();

        } catch (JSONException e) {
            throw new IOException("Error saving book metadata", e);
        }
    }

    public Book loadBookMeta(String bookId) throws IOException {
        File bookDir = new File(booksDirectory, bookId);
        File metaFile = new File(bookDir, META_FILE);

        if (!metaFile.exists()) {
            throw new IOException("Book metadata not found");
        }

        try {
            BufferedReader reader = new BufferedReader(new FileReader(metaFile));
            StringBuilder content = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                content.append(line);
            }
            reader.close();

            JSONObject json = new JSONObject(content.toString());
            Book book = new Book();
            book.setId(json.getString("id"));
            book.setTitle(json.getString("title"));
            book.setFileName(json.getString("fileName"));
            book.setTotalSentences(json.getInt("totalSentences"));
            book.setCurrentPosition(json.getInt("currentPosition"));
            book.setLastReadDate(dateFormat.parse(json.getString("lastReadDate")));
            book.setFileSizeBytes(json.getLong("fileSizeBytes"));
            book.setProcessedDate(dateFormat.parse(json.getString("processedDate")));

            return book;

        } catch (Exception e) {
            throw new IOException("Error loading book metadata", e);
        }
    }

    public List<Book> getAllBooks() throws IOException {
        List<Book> books = new ArrayList<>();
        File libraryFile = new File(booksDirectory, LIBRARY_FILE);

        if (!libraryFile.exists()) {
            return books;
        }

        try {
            BufferedReader reader = new BufferedReader(new FileReader(libraryFile));
            StringBuilder content = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                content.append(line);
            }
            reader.close();

            JSONObject json = new JSONObject(content.toString());
            JSONArray booksArray = json.getJSONArray("books");

            for (int i = 0; i < booksArray.length(); i++) {
                String bookId = booksArray.getString(i);
                try {
                    Book book = loadBookMeta(bookId);
                    books.add(book);
                } catch (IOException e) {
                    // Skip corrupted books
                }
            }

        } catch (Exception e) {
            throw new IOException("Error loading library", e);
        }

        return books;
    }

    private void updateLibrary(Book book) throws IOException {
        List<String> bookIds = new ArrayList<>();

        try {
            List<Book> existingBooks = getAllBooks();
            for (Book existingBook : existingBooks) {
                if (!existingBook.getId().equals(book.getId())) {
                    bookIds.add(existingBook.getId());
                }
            }
        } catch (IOException e) {
            // Ignore if library doesn't exist yet
        }

        bookIds.add(book.getId());

        try {
            JSONObject json = new JSONObject();
            JSONArray booksArray = new JSONArray(bookIds);
            json.put("books", booksArray);
            json.put("lastUpdated", dateFormat.format(new Date()));

            File libraryFile = new File(booksDirectory, LIBRARY_FILE);
            BufferedWriter writer = new BufferedWriter(new FileWriter(libraryFile));
            writer.write(json.toString(2));
            writer.close();

        } catch (JSONException e) {
            throw new IOException("Error updating library", e);
        }
    }

    public boolean isBookCached(String bookId) {
        File bookDir = new File(booksDirectory, bookId);
        File contentFile = new File(bookDir, CONTENT_FILE);
        File metaFile = new File(bookDir, META_FILE);
        return contentFile.exists() && metaFile.exists();
    }

    public String getSentence(String bookId, int position) throws IOException {
        File bookDir = new File(booksDirectory, bookId);
        File contentFile = new File(bookDir, CONTENT_FILE);

        BufferedReader reader = new BufferedReader(new FileReader(contentFile));
        String sentence = null;

        for (int i = 0; i <= position; i++) {
            sentence = reader.readLine();
            if (sentence == null) {
                reader.close();
                throw new IOException("Sentence not found at position " + position);
            }
        }

        reader.close();
        return sentence;
    }

    public List<String> getSentences(String bookId, int startPosition, int count) throws IOException {
        List<String> sentences = new ArrayList<>();
        File bookDir = new File(booksDirectory, bookId);
        File contentFile = new File(bookDir, CONTENT_FILE);

        BufferedReader reader = new BufferedReader(new FileReader(contentFile));

        // Skip to start position
        for (int i = 0; i < startPosition; i++) {
            if (reader.readLine() == null) {
                reader.close();
                throw new IOException("Start position beyond file");
            }
        }

        // Read sentences
        for (int i = 0; i < count; i++) {
            String sentence = reader.readLine();
            if (sentence == null) {
                break;
            }
            sentences.add(sentence);
        }

        reader.close();
        return sentences;
    }

    private String extractTitleFromFileName(String fileName) {
        if (fileName == null) return "Libro sin título";

        // Remove extension
        int dotIndex = fileName.lastIndexOf('.');
        if (dotIndex > 0) {
            fileName = fileName.substring(0, dotIndex);
        }

        // Replace underscores and dashes with spaces
        fileName = fileName.replace("_", " ").replace("-", " ");

        // Capitalize first letter of each word
        String[] words = fileName.split("\\s+");
        StringBuilder title = new StringBuilder();
        for (String word : words) {
            if (word.length() > 0) {
                title.append(Character.toUpperCase(word.charAt(0)));
                if (word.length() > 1) {
                    title.append(word.substring(1).toLowerCase());
                }
                title.append(" ");
            }
        }

        return title.toString().trim();
    }
}