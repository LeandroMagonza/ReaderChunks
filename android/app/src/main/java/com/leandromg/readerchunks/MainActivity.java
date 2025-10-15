package com.leandromg.readerchunks;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.InputType;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.progressindicator.CircularProgressIndicator;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity implements BookAdapter.OnBookClickListener {

    private LinearLayout layoutLoading;
    private LinearLayout layoutEmpty;
    private RecyclerView recyclerBooks;
    private MaterialButton btnAddDocument;
    private MaterialButton btnAddDocumentEmpty;
    private CircularProgressIndicator progressIndicator;
    private TextView tvStatus;
    private MaterialToolbar toolbar;

    private ExecutorService executor;
    private BookCacheManager cacheManager;
    private BookAdapter bookAdapter;
    private List<Book> books;
    private ThemeManager themeManager;
    private LanguageManager languageManager;
    private SettingsManager settingsManager;
    private SettingsDialogManager settingsDialogManager;
    private boolean isProcessing = false;

    private ActivityResultLauncher<String[]> documentPickerLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Initialize language and theme before setting content view
        languageManager = new LanguageManager(this);
        languageManager.applyStoredLanguage();

        themeManager = new ThemeManager(this);
        themeManager.applyTheme();

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize TextExtractorFactory with context for web support
        TextExtractorFactory.initialize(this);

        initViews();
        setupToolbar();
        setupExecutor();
        setupCacheManager();
        setupSettingsManager();
        setupRecyclerView();
        setupDocumentPicker();
        setupClickListeners();
        loadBooks();

        // Handle external file or URL sharing intent
        handleExternalIntent();
    }

    private void initViews() {
        layoutLoading = findViewById(R.id.layoutLoading);
        layoutEmpty = findViewById(R.id.layoutEmpty);
        recyclerBooks = findViewById(R.id.recyclerBooks);
        btnAddDocument = findViewById(R.id.btnAddDocument);
        btnAddDocumentEmpty = findViewById(R.id.btnAddDocumentEmpty);
        progressIndicator = findViewById(R.id.progressIndicator);
        tvStatus = findViewById(R.id.tvStatus);
        toolbar = findViewById(R.id.toolbar);
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
    }

    private void setupExecutor() {
        executor = Executors.newSingleThreadExecutor();
    }

    private void setupCacheManager() {
        cacheManager = new BookCacheManager(this);
    }

    private void setupSettingsManager() {
        settingsManager = new SettingsManager(this);
        settingsDialogManager = new SettingsDialogManager(this, settingsManager, languageManager);
    }

    private void setupRecyclerView() {
        books = new ArrayList<>();
        bookAdapter = new BookAdapter(books, this, cacheManager);
        recyclerBooks.setLayoutManager(new LinearLayoutManager(this));
        recyclerBooks.setAdapter(bookAdapter);
    }

    private void setupDocumentPicker() {
        documentPickerLauncher = registerForActivityResult(
                new ActivityResultContracts.OpenDocument(),
                this::handleDocumentSelection
        );
    }

    private void setupClickListeners() {
        btnAddDocument.setOnClickListener(v -> showAddContentDialog());
        btnAddDocumentEmpty.setOnClickListener(v -> showAddContentDialog());
    }

    private void openDocumentPicker() {
        // Get all supported MIME types from the TextExtractorFactory
        String[] supportedMimeTypes = TextExtractorFactory.getAllSupportedMimeTypes();
        documentPickerLauncher.launch(supportedMimeTypes);
    }

    private void handleDocumentSelection(Uri uri) {
        if (uri != null) {
            String fileName = getFileNameFromUri(uri);
            processDocument(uri, fileName);
        }
    }

    private void processDocument(Uri uri, String fileName) {
        android.util.Log.d("MainActivity", "processDocument called with URI: " + uri.toString());
        android.util.Log.d("MainActivity", "processDocument called with fileName: " + fileName);
        android.util.Log.d("MainActivity", "URI scheme: " + uri.getScheme());

        // Check if it's a web URL or file
        if (WebTextExtractorImpl.canHandleUri(uri)) {
            android.util.Log.d("MainActivity", "Detected as web URL, calling processWebUrl");
            processWebUrl(uri, fileName);
            return;
        }

        android.util.Log.d("MainActivity", "Detected as local file, processing normally");

        // Check if file format is supported
        String extension = TextExtractorFactory.getFileExtension(fileName);
        if (extension == null) {
            showError("❌ No se pudo determinar el tipo de archivo: " + fileName);
            return;
        }

        if (!TextExtractorFactory.isExtensionSupported(extension)) {
            showError("❌ Formato no soportado: ." + extension +
                     "\nFormatos válidos: " + TextExtractorFactory.getSupportedFormatsDescription());
            return;
        }

        showLoading(true);
        tvStatus.setText("Extrayendo texto del archivo...");

        executor.execute(() -> {
            try {
                // Update status during processing
                runOnUiThread(() -> tvStatus.setText("Analizando contenido..."));

                // Debug information
                android.util.Log.d("MainActivity", "Processing file: " + fileName);
                android.util.Log.d("MainActivity", "URI: " + uri.toString());
                android.util.Log.d("MainActivity", "Extension: " + extension);

                Book book = cacheManager.processAndCacheBook(uri, fileName);

                runOnUiThread(() -> {
                    tvStatus.setText("Guardando en biblioteca...");
                });

                // Small delay to show the final status
                Thread.sleep(500);

                runOnUiThread(() -> {
                    showLoading(false);
                    books.add(0, book);
                    bookAdapter.updateBooks(books);
                    updateViewState();

                    String fileType = extension.toUpperCase();
                    Toast.makeText(this, getString(R.string.file_added_success, fileType, book.getDisplayTitle()), Toast.LENGTH_LONG).show();
                });

            } catch (Exception e) {
                runOnUiThread(() -> {
                    showLoading(false);
                    String fileType = extension != null ? extension.toUpperCase() : "archivo";
                    showError(getString(R.string.error_processing_file, fileType, e.getMessage()));
                });
            }
        });
    }

    private void processWebUrl(Uri uri, String fileName) {
        String url = uri.toString();
        android.util.Log.d("MainActivity", "=== PROCESSING WEB URL ===");
        android.util.Log.d("MainActivity", "URL: " + url);
        android.util.Log.d("MainActivity", "URI: " + uri.toString());
        android.util.Log.d("MainActivity", "URI scheme: " + uri.getScheme());
        android.util.Log.d("MainActivity", "Can handle URI: " + WebTextExtractorImpl.canHandleUri(uri));

        // Validate the URI is actually a web URL
        if (!WebTextExtractorImpl.canHandleUri(uri)) {
            android.util.Log.e("MainActivity", "ERROR: URI is not a valid web URL: " + uri.toString());
            showError("❌ URL no válida para contenido web: " + url);
            return;
        }

        showLoading(true);
        tvStatus.setText("Conectando a la página web...");

        executor.execute(() -> {
            try {
                // Update status during processing
                runOnUiThread(() -> tvStatus.setText("Descargando contenido web..."));

                // Generate a proper title for the web content
                String webTitle = WebToPDFProcessor.getTitleFromUrl(url);
                android.util.Log.d("MainActivity", "Generated web title: " + webTitle);

                android.util.Log.d("MainActivity", "Calling cacheManager.processAndCacheBook...");
                Book book = cacheManager.processAndCacheBook(uri, webTitle);
                android.util.Log.d("MainActivity", "Successfully processed web content. Book ID: " + book.getId());

                runOnUiThread(() -> {
                    tvStatus.setText("Guardando en biblioteca...");
                });

                // Small delay to show the final status
                Thread.sleep(500);

                runOnUiThread(() -> {
                    showLoading(false);
                    books.add(0, book);
                    bookAdapter.updateBooks(books);
                    updateViewState();

                    Toast.makeText(this, getString(R.string.web_content_added_success, webTitle), Toast.LENGTH_LONG).show();
                });

            } catch (Exception e) {
                android.util.Log.e("MainActivity", "Error processing web URL: " + url, e);
                android.util.Log.e("MainActivity", "Exception type: " + e.getClass().getSimpleName());
                android.util.Log.e("MainActivity", "Exception message: " + e.getMessage());
                if (e.getCause() != null) {
                    android.util.Log.e("MainActivity", "Exception cause: " + e.getCause().getMessage());
                }

                runOnUiThread(() -> {
                    showLoading(false);
                    String errorMsg = e.getMessage();
                    if (errorMsg != null && errorMsg.contains("no content provider")) {
                        errorMsg = "Error de configuración interna. Por favor intenta de nuevo.";
                    }
                    showError(getString(R.string.error_processing_web_content, errorMsg));
                });
            }
        });
    }

    private void showLoading(boolean show) {
        isProcessing = show;
        layoutLoading.setVisibility(show ? View.VISIBLE : View.GONE);
        btnAddDocument.setEnabled(!show);
        btnAddDocumentEmpty.setEnabled(!show);

        // Always update view state to handle empty message visibility
        updateViewState();
    }

    private void loadBooks() {
        showLoading(true);
        tvStatus.setText(getString(R.string.processing));

        executor.execute(() -> {
            try {
                List<Book> loadedBooks = cacheManager.getAllBooks();

                runOnUiThread(() -> {
                    showLoading(false);
                    books.clear();
                    books.addAll(loadedBooks);

                    // Sort by most recent date (read or creation) - newest first
                    books.sort((book1, book2) -> {
                        Date date1 = book1.getMostRecentDate();
                        Date date2 = book2.getMostRecentDate();
                        if (date1 == null && date2 == null) return 0;
                        if (date1 == null) return 1;  // books without dates go to end
                        if (date2 == null) return -1;
                        return date2.compareTo(date1); // newer dates first
                    });

                    bookAdapter.updateBooks(books);
                    updateViewState();
                });

            } catch (Exception e) {
                runOnUiThread(() -> {
                    showLoading(false);
                    updateViewState();
                    showError(getString(R.string.error_loading_library));
                });
            }
        });
    }

    private void showError(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }

    private void updateViewState() {
        // Don't show empty state while processing
        android.util.Log.d("MainActivity", "updateViewState: books.isEmpty()=" + books.isEmpty() + ", isProcessing=" + isProcessing);
        if (books.isEmpty() && !isProcessing) {
            android.util.Log.d("MainActivity", "Showing empty state");
            layoutEmpty.setVisibility(View.VISIBLE);
            recyclerBooks.setVisibility(View.GONE);
        } else {
            android.util.Log.d("MainActivity", "Hiding empty state");
            layoutEmpty.setVisibility(View.GONE);
            recyclerBooks.setVisibility(books.isEmpty() ? View.GONE : View.VISIBLE);
        }
    }

    @Override
    public void onContinueReading(Book book) {
        Intent intent = new Intent(this, SentenceReaderActivity.class);
        intent.putExtra("book_id", book.getId());
        startActivity(intent);
    }

    @Override
    public void onRenameBook(Book book) {
        showRenameDialog(book);
    }

    @Override
    public void onResetProgress(Book book) {
        showResetProgressDialog(book);
    }

    @Override
    public void onDeleteBook(Book book) {
        showDeleteBookDialog(book);
    }

    private void showRenameDialog(Book book) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getString(R.string.rename_book_title));

        EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        input.setText(book.getDisplayTitle());
        input.setSelection(book.getDisplayTitle().length());
        builder.setView(input);

        builder.setPositiveButton(getString(R.string.save), (dialog, which) -> {
            String newTitle = input.getText().toString().trim();
            if (!newTitle.isEmpty() && !newTitle.equals(book.getDisplayTitle())) {
                renameBook(book, newTitle);
            }
        });

        builder.setNegativeButton(getString(R.string.cancel), (dialog, which) -> dialog.cancel());
        builder.show();
    }

    private void showResetProgressDialog(Book book) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getString(R.string.reset_progress_title));
        builder.setMessage(getString(R.string.reset_progress_message, book.getDisplayTitle()));

        builder.setPositiveButton(getString(R.string.reset), (dialog, which) -> resetBookProgress(book));
        builder.setNegativeButton(getString(R.string.cancel), (dialog, which) -> dialog.cancel());
        builder.show();
    }

    private void showDeleteBookDialog(Book book) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getString(R.string.delete_book_title));
        builder.setMessage(getString(R.string.delete_book_message, book.getDisplayTitle()));

        builder.setPositiveButton(getString(R.string.delete), (dialog, which) -> deleteBook(book));
        builder.setNegativeButton(getString(R.string.cancel), (dialog, which) -> dialog.cancel());
        builder.show();
    }

    private void renameBook(Book book, String newTitle) {
        executor.execute(() -> {
            try {
                cacheManager.updateBookTitle(book.getId(), newTitle);

                // Update the book object and refresh the list
                book.setTitle(newTitle);

                runOnUiThread(() -> {
                    bookAdapter.updateBooks(books);
                    Toast.makeText(this, getString(R.string.book_renamed_success), Toast.LENGTH_SHORT).show();
                });

            } catch (Exception e) {
                runOnUiThread(() -> {
                    showError(getString(R.string.error_rename_book, e.getMessage()));
                });
            }
        });
    }

    private void resetBookProgress(Book book) {
        executor.execute(() -> {
            try {
                cacheManager.resetBookProgress(book.getId());

                // Update the book object and refresh the list
                book.setCurrentPosition(0);
                book.setCurrentCharPosition(0);

                runOnUiThread(() -> {
                    bookAdapter.updateBooks(books);
                    Toast.makeText(this, getString(R.string.progress_reset_success), Toast.LENGTH_SHORT).show();
                });

            } catch (Exception e) {
                runOnUiThread(() -> {
                    showError(getString(R.string.error_reset_progress, e.getMessage()));
                });
            }
        });
    }

    private void deleteBook(Book book) {
        executor.execute(() -> {
            try {
                boolean deleted = cacheManager.deleteBook(book.getId());

                if (deleted) {
                    runOnUiThread(() -> {
                        books.remove(book);
                        bookAdapter.updateBooks(books);
                        updateViewState();
                        Toast.makeText(this, getString(R.string.book_deleted_success), Toast.LENGTH_SHORT).show();
                    });
                } else {
                    runOnUiThread(() -> {
                        showError(getString(R.string.error_could_not_delete));
                    });
                }

            } catch (Exception e) {
                runOnUiThread(() -> {
                    showError(getString(R.string.error_delete_book, e.getMessage()));
                });
            }
        });
    }

    private String getFileNameFromUri(Uri uri) {
        String fileName = "documento";

        // Try to get filename from content resolver first (for content:// URIs)
        if ("content".equals(uri.getScheme())) {
            try (android.database.Cursor cursor = getContentResolver().query(
                    uri, null, null, null, null)) {
                if (cursor != null && cursor.moveToFirst()) {
                    int nameIndex = cursor.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME);
                    if (nameIndex >= 0) {
                        String name = cursor.getString(nameIndex);
                        if (name != null && !name.trim().isEmpty()) {
                            fileName = name;
                        }
                    }
                }
            } catch (Exception e) {
                // Fall back to path extraction
            }
        }

        // Fall back to extracting from path for file:// URIs or if content resolver failed
        if ("documento".equals(fileName)) {
            String path = uri.getPath();
            if (path != null) {
                int index = path.lastIndexOf('/');
                if (index != -1 && index < path.length() - 1) {
                    fileName = path.substring(index + 1);
                }
            }
        }

        return fileName;
    }


    private void refreshBookProgress() {
        if (books.isEmpty()) return;

        executor.execute(() -> {
            try {
                // Simply reload the entire book list to get fresh data
                List<Book> updatedBooks = cacheManager.getAllBooks();

                runOnUiThread(() -> {
                    books.clear();
                    books.addAll(updatedBooks);

                    // Sort by most recent date (read or creation) - newest first
                    books.sort((book1, book2) -> {
                        Date date1 = book1.getMostRecentDate();
                        Date date2 = book2.getMostRecentDate();
                        if (date1 == null && date2 == null) return 0;
                        if (date1 == null) return 1;  // books without dates go to end
                        if (date2 == null) return -1;
                        return date2.compareTo(date1); // newer dates first
                    });

                    bookAdapter.updateBooks(books);
                });

            } catch (Exception e) {
                // Silently fail - not critical for user experience
            }
        });
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        // Update the intent and handle the new file or URL
        setIntent(intent);
        handleExternalIntent();
    }

    /**
     * Handle when the app is opened via an external file or URL sharing intent
     */
    private void handleExternalIntent() {
        Intent intent = getIntent();
        String action = intent.getAction();

        android.util.Log.d("MainActivity", "=== HANDLING EXTERNAL INTENT ===");
        android.util.Log.d("MainActivity", "Intent action: " + action);
        android.util.Log.d("MainActivity", "Intent data: " + intent.getData());
        android.util.Log.d("MainActivity", "Intent type: " + intent.getType());

        // Log all extras
        if (intent.getExtras() != null) {
            for (String key : intent.getExtras().keySet()) {
                android.util.Log.d("MainActivity", "Extra '" + key + "': " + intent.getExtras().get(key));
            }
        }

        if (Intent.ACTION_VIEW.equals(action)) {
            // Handle file opening or direct URL opening
            Uri fileUri = intent.getData();
            android.util.Log.d("MainActivity", "ACTION_VIEW with URI: " + fileUri);
            if (fileUri != null) {
                handleFileUri(fileUri);
            }
        } else if (Intent.ACTION_SEND.equals(action)) {
            // Handle URL sharing from other apps
            String sharedText = intent.getStringExtra(Intent.EXTRA_TEXT);
            android.util.Log.d("MainActivity", "ACTION_SEND with text: " + sharedText);
            if (sharedText != null) {
                handleSharedUrl(sharedText);
            } else {
                android.util.Log.w("MainActivity", "ACTION_SEND but no EXTRA_TEXT found");
            }
        } else {
            android.util.Log.w("MainActivity", "Unknown action: " + action);
        }
    }

    private void handleFileUri(Uri fileUri) {
        android.util.Log.d("MainActivity", "handleFileUri called with: " + fileUri);
        android.util.Log.d("MainActivity", "URI scheme: " + fileUri.getScheme());

        // Check if this is actually a web URL that came through ACTION_VIEW
        if (WebTextExtractorImpl.canHandleUri(fileUri)) {
            android.util.Log.d("MainActivity", "URI is web URL, processing as web content");
            String title = WebToPDFProcessor.getTitleFromUrl(fileUri.toString());
            processWebUrl(fileUri, title);
            return;
        }

        // For content:// URIs, try to get persistent access permission
        if ("content".equals(fileUri.getScheme())) {
            try {
                getContentResolver().takePersistableUriPermission(fileUri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION);
            } catch (SecurityException e) {
                // Permission not available, but we can still try to process the file
                android.util.Log.w("MainActivity", "Could not get persistent permission for: " + fileUri);
            }
        }

        String fileName = getFileNameFromUri(fileUri);
        android.util.Log.d("MainActivity", "External file intent - File: " + fileName + ", URI: " + fileUri);

        // Show a toast to indicate the file is being processed
        Toast.makeText(this, getString(R.string.opening_file, fileName), Toast.LENGTH_SHORT).show();

        // Process the external file directly
        processDocument(fileUri, fileName);
    }

    private void handleSharedUrl(String sharedText) {
        android.util.Log.d("MainActivity", "=== HANDLING SHARED URL ===");
        android.util.Log.d("MainActivity", "Shared text received: " + sharedText);

        // Extract URL from shared text (might contain additional text)
        String url = extractUrlFromSharedText(sharedText);
        android.util.Log.d("MainActivity", "Extracted URL: " + url);

        if (url != null && TextExtractorFactory.isSupportedWebUrl(url)) {
            android.util.Log.d("MainActivity", "URL is supported, processing: " + url);

            // Show a toast to indicate the URL is being processed
            String title = WebToPDFProcessor.getTitleFromUrl(url);
            Toast.makeText(this, getString(R.string.processing_shared_url, title), Toast.LENGTH_SHORT).show();

            // Convert to URI and validate
            Uri uri = Uri.parse(url);
            android.util.Log.d("MainActivity", "Parsed URI: " + uri.toString());
            android.util.Log.d("MainActivity", "URI scheme: " + uri.getScheme());
            android.util.Log.d("MainActivity", "canHandleUri result: " + WebTextExtractorImpl.canHandleUri(uri));

            // Double-check that the URI is valid for web processing
            if (!WebTextExtractorImpl.canHandleUri(uri)) {
                android.util.Log.e("MainActivity", "ERROR: Parsed URI is not valid for web processing: " + uri.toString());
                showError("❌ Error procesando URL compartida: " + url);
                return;
            }

            // Force web URL processing to avoid Content Provider issues
            processWebUrl(uri, title);
        } else {
            android.util.Log.e("MainActivity", "URL not supported or invalid");
            android.util.Log.e("MainActivity", "URL: " + url);
            android.util.Log.e("MainActivity", "URL is null: " + (url == null));
            if (url != null) {
                android.util.Log.e("MainActivity", "isSupportedWebUrl result: " + TextExtractorFactory.isSupportedWebUrl(url));
            }
            showError("❌ URL no soportada o no válida: " + sharedText);
        }
    }

    /**
     * Extract URL from shared text (which might contain additional content)
     */
    private String extractUrlFromSharedText(String sharedText) {
        android.util.Log.d("MainActivity", "extractUrlFromSharedText input: '" + sharedText + "'");

        if (sharedText == null || sharedText.trim().isEmpty()) {
            android.util.Log.d("MainActivity", "Shared text is null or empty");
            return null;
        }

        // Look for http:// or https:// URLs in the text
        String[] words = sharedText.split("\\s+");
        android.util.Log.d("MainActivity", "Split into " + words.length + " words");

        for (String word : words) {
            word = word.trim();
            android.util.Log.d("MainActivity", "Checking word: '" + word + "'");
            if (word.startsWith("http://") || word.startsWith("https://")) {
                android.util.Log.d("MainActivity", "Found URL in words: " + word);
                return word;
            }
        }

        // If the entire text looks like a URL
        sharedText = sharedText.trim();
        if (sharedText.startsWith("http://") || sharedText.startsWith("https://")) {
            android.util.Log.d("MainActivity", "Entire text is URL: " + sharedText);
            return sharedText;
        }

        android.util.Log.d("MainActivity", "No URL found in shared text");
        return null;
    }

    /**
     * Process text from clipboard and create a book from it
     */
    private void processClipboardText() {
        android.util.Log.d("MainActivity", "=== PROCESSING CLIPBOARD TEXT ===");

        // Get clipboard manager
        ClipboardManager clipboardManager = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        if (clipboardManager == null) {
            android.util.Log.e("MainActivity", "ClipboardManager is null");
            showError(getString(R.string.clipboard_error, "No se pudo acceder al portapapeles"));
            return;
        }

        // Check if clipboard has data
        if (!clipboardManager.hasPrimaryClip()) {
            android.util.Log.d("MainActivity", "Clipboard is empty");
            Toast.makeText(this, getString(R.string.clipboard_empty), Toast.LENGTH_SHORT).show();
            return;
        }

        ClipData clipData = clipboardManager.getPrimaryClip();
        if (clipData == null || clipData.getItemCount() == 0) {
            android.util.Log.d("MainActivity", "Clipboard data is null or empty");
            Toast.makeText(this, getString(R.string.clipboard_empty), Toast.LENGTH_SHORT).show();
            return;
        }

        // Get the text from clipboard
        ClipData.Item clipItem = clipData.getItemAt(0);
        CharSequence clipText = clipItem.getText();

        if (clipText == null || clipText.toString().trim().isEmpty()) {
            android.util.Log.d("MainActivity", "Clipboard contains no text");
            Toast.makeText(this, getString(R.string.clipboard_no_text), Toast.LENGTH_SHORT).show();
            return;
        }

        String text = clipText.toString().trim();
        android.util.Log.d("MainActivity", "Clipboard text length: " + text.length());
        android.util.Log.d("MainActivity", "Clipboard text preview: " + text.substring(0, Math.min(100, text.length())));

        // Check if the clipboard contains a URL
        if (isUrl(text)) {
            android.util.Log.d("MainActivity", "Clipboard contains URL, processing as web content");
            Uri uri = Uri.parse(text);
            String title = WebToPDFProcessor.getTitleFromUrl(text);
            processWebUrl(uri, title);
            return;
        }

        // Process as plain text
        processTextAsBook(text);
    }

    /**
     * Check if text looks like a URL
     */
    private boolean isUrl(String text) {
        text = text.trim();
        return text.startsWith("http://") || text.startsWith("https://");
    }

    /**
     * Process plain text and create a book from it
     */
    private void processTextAsBook(String text) {
        android.util.Log.d("MainActivity", "=== PROCESSING TEXT AS BOOK ===");
        android.util.Log.d("MainActivity", "Text length: " + text.length());

        // Validate text length
        if (text.length() < 10) {
            showError("❌ El texto es demasiado corto para crear un libro");
            return;
        }

        showLoading(true);
        tvStatus.setText(getString(R.string.clipboard_processing));

        executor.execute(() -> {
            try {
                // Update status during processing
                runOnUiThread(() -> tvStatus.setText("Procesando texto..."));

                // Generate a title from the first line or first few words
                String title = generateTitleFromText(text);
                android.util.Log.d("MainActivity", "Generated title: " + title);

                // Create a book directly from the text
                Book book = cacheManager.processAndCacheTextAsBook(text, title);
                android.util.Log.d("MainActivity", "Successfully processed text. Book ID: " + book.getId());

                runOnUiThread(() -> {
                    tvStatus.setText("Guardando en biblioteca...");
                });

                // Small delay to show the final status
                Thread.sleep(500);

                runOnUiThread(() -> {
                    showLoading(false);
                    books.add(0, book);
                    bookAdapter.updateBooks(books);
                    updateViewState();

                    Toast.makeText(this, getString(R.string.clipboard_text_added), Toast.LENGTH_LONG).show();
                });

            } catch (Exception e) {
                android.util.Log.e("MainActivity", "Error processing clipboard text", e);
                runOnUiThread(() -> {
                    showLoading(false);
                    showError(getString(R.string.clipboard_error, e.getMessage()));
                });
            }
        });
    }

    /**
     * Generate a title from the beginning of the text
     */
    private String generateTitleFromText(String text) {
        // Try to get the first line as title
        String[] lines = text.split("\\n");
        String firstLine = lines[0].trim();

        // If first line is too long, take first few words
        if (firstLine.length() > 50) {
            String[] words = firstLine.split("\\s+");
            StringBuilder titleBuilder = new StringBuilder();
            for (int i = 0; i < Math.min(8, words.length); i++) {
                if (titleBuilder.length() > 0) titleBuilder.append(" ");
                titleBuilder.append(words[i]);
                if (titleBuilder.length() > 45) break;
            }
            return titleBuilder.toString() + "...";
        }

        // If first line is reasonable length, use it
        if (firstLine.length() > 3) {
            return firstLine;
        }

        // Fallback: use default title with timestamp
        return getString(R.string.clipboard_text_title) + " " +
               android.text.format.DateFormat.format("yyyy-MM-dd HH:mm", new java.util.Date());
    }

    /**
     * Show dialog with options to add content from files or clipboard
     */
    private void showAddContentDialog() {
        android.util.Log.d("MainActivity", "=== SHOWING ADD CONTENT DIALOG ===");

        // Inflate the dialog layout
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_add_content, null);

        // Get views from dialog
        MaterialButton btnSelectFiles = dialogView.findViewById(R.id.btnSelectFiles);
        MaterialButton btnSelectClipboard = dialogView.findViewById(R.id.btnSelectClipboard);
        TextView tvClipboardStatus = dialogView.findViewById(R.id.tvClipboardStatus);

        // Check clipboard status and update UI
        ClipboardStatus clipboardStatus = getClipboardStatus();
        updateClipboardButtonState(btnSelectClipboard, tvClipboardStatus, clipboardStatus);

        // Create and show dialog
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(dialogView)
                .setCancelable(true)
                .create();

        // Set up button listeners
        btnSelectFiles.setOnClickListener(v -> {
            dialog.dismiss();
            openDocumentPicker();
        });

        btnSelectClipboard.setOnClickListener(v -> {
            dialog.dismiss();
            if (clipboardStatus.isAvailable) {
                processClipboardText();
            }
        });

        dialog.show();
    }

    /**
     * Get current clipboard status
     */
    private ClipboardStatus getClipboardStatus() {
        ClipboardStatus status = new ClipboardStatus();

        try {
            ClipboardManager clipboardManager = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
            if (clipboardManager == null) {
                android.util.Log.w("MainActivity", "ClipboardManager is null");
                status.isAvailable = false;
                status.statusText = getString(R.string.clipboard_unavailable);
                return status;
            }

            if (!clipboardManager.hasPrimaryClip()) {
                android.util.Log.d("MainActivity", "Clipboard is empty");
                status.isAvailable = false;
                status.statusText = getString(R.string.clipboard_empty);
                return status;
            }

            ClipData clipData = clipboardManager.getPrimaryClip();
            if (clipData == null || clipData.getItemCount() == 0) {
                android.util.Log.d("MainActivity", "Clipboard data is null or empty");
                status.isAvailable = false;
                status.statusText = getString(R.string.clipboard_empty);
                return status;
            }

            ClipData.Item clipItem = clipData.getItemAt(0);
            CharSequence clipText = clipItem.getText();

            if (clipText == null || clipText.toString().trim().isEmpty()) {
                android.util.Log.d("MainActivity", "Clipboard contains no text");
                status.isAvailable = false;
                status.statusText = getString(R.string.clipboard_no_text);
                return status;
            }

            String text = clipText.toString().trim();
            android.util.Log.d("MainActivity", "Clipboard text length: " + text.length());

            // Check if it's a URL
            if (isUrl(text)) {
                android.util.Log.d("MainActivity", "Clipboard contains URL");
                status.isAvailable = true;
                status.statusText = getString(R.string.clipboard_url_detected);
                return status;
            }

            // Regular text
            android.util.Log.d("MainActivity", "Clipboard contains text");
            status.isAvailable = true;
            status.statusText = getString(R.string.clipboard_characters_detected, text.length());
            return status;

        } catch (Exception e) {
            android.util.Log.e("MainActivity", "Error checking clipboard status", e);
            status.isAvailable = false;
            status.statusText = getString(R.string.clipboard_unavailable);
            return status;
        }
    }

    /**
     * Update clipboard button state based on clipboard status
     */
    private void updateClipboardButtonState(MaterialButton btnClipboard, TextView tvStatus, ClipboardStatus status) {
        btnClipboard.setEnabled(status.isAvailable);

        if (status.isAvailable) {
            btnClipboard.setAlpha(1.0f);
        } else {
            btnClipboard.setAlpha(0.5f);
        }

        tvStatus.setText(status.statusText);
        tvStatus.setVisibility(View.VISIBLE);
    }

    /**
     * Inner class to hold clipboard status information
     */
    private static class ClipboardStatus {
        boolean isAvailable = false;
        String statusText = "";
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        MenuItem themeItem = menu.findItem(R.id.action_theme_toggle);
        if (themeItem != null) {
            if (themeManager.isDarkMode()) {
                // Dark mode - show moon icon (current mode)
                themeItem.setIcon(R.drawable.ic_moon);
            } else {
                // Light mode - show sun icon (current mode)
                themeItem.setIcon(R.drawable.ic_sun);
            }
        }
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_theme_toggle) {
            toggleTheme();
            return true;
        } else if (item.getItemId() == R.id.action_settings) {
            settingsDialogManager.show();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void toggleTheme() {
        themeManager.toggleTheme();
        // Recreate activity to apply theme changes
        recreate();
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Check if language was changed and recreate if needed
        if (languageManager.shouldRecreateActivity()) {
            languageManager.clearRecreateFlag();
            recreate();
            return; // Don't refresh books if we're recreating
        }

        // Refresh book progress when returning from reading
        refreshBookProgress();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (executor != null) {
            executor.shutdown();
        }
    }
}