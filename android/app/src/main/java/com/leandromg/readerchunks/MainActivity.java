package com.leandromg.readerchunks;

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

    private ActivityResultLauncher<String[]> documentPickerLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Initialize theme before setting content view
        themeManager = new ThemeManager(this);
        themeManager.applyTheme();

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initViews();
        setupToolbar();
        setupExecutor();
        setupCacheManager();
        setupRecyclerView();
        setupDocumentPicker();
        setupClickListeners();
        loadBooks();

        // Handle external file opening intent
        handleExternalFileIntent();
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
        btnAddDocument.setOnClickListener(v -> openDocumentPicker());
        btnAddDocumentEmpty.setOnClickListener(v -> openDocumentPicker());
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
                    Toast.makeText(this, "✅ " + fileType + " agregado: " + book.getDisplayTitle(), Toast.LENGTH_LONG).show();
                });

            } catch (Exception e) {
                runOnUiThread(() -> {
                    showLoading(false);
                    String fileType = extension != null ? extension.toUpperCase() : "archivo";
                    showError("❌ Error procesando " + fileType + ": " + e.getMessage());
                });
            }
        });
    }

    private void showLoading(boolean show) {
        layoutLoading.setVisibility(show ? View.VISIBLE : View.GONE);
        btnAddDocument.setEnabled(!show);
        btnAddDocumentEmpty.setEnabled(!show);

        if (!show) {
            updateViewState();
        }
    }

    private void loadBooks() {
        showLoading(true);
        tvStatus.setText("Cargando tu biblioteca...");

        executor.execute(() -> {
            try {
                List<Book> loadedBooks = cacheManager.getAllBooks();

                runOnUiThread(() -> {
                    showLoading(false);
                    books.clear();
                    books.addAll(loadedBooks);
                    bookAdapter.updateBooks(books);
                    updateViewState();
                });

            } catch (Exception e) {
                runOnUiThread(() -> {
                    showLoading(false);
                    updateViewState();
                    showError("Error cargando biblioteca");
                });
            }
        });
    }

    private void showError(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }

    private void updateViewState() {
        if (books.isEmpty()) {
            layoutEmpty.setVisibility(View.VISIBLE);
            recyclerBooks.setVisibility(View.GONE);
        } else {
            layoutEmpty.setVisibility(View.GONE);
            recyclerBooks.setVisibility(View.VISIBLE);
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
        builder.setTitle("Renombrar libro");

        EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        input.setText(book.getDisplayTitle());
        input.setSelection(book.getDisplayTitle().length());
        builder.setView(input);

        builder.setPositiveButton("Guardar", (dialog, which) -> {
            String newTitle = input.getText().toString().trim();
            if (!newTitle.isEmpty() && !newTitle.equals(book.getDisplayTitle())) {
                renameBook(book, newTitle);
            }
        });

        builder.setNegativeButton("Cancelar", (dialog, which) -> dialog.cancel());
        builder.show();
    }

    private void showResetProgressDialog(Book book) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Restablecer progreso");
        builder.setMessage("¿Estás seguro de que deseas restablecer el progreso de \"" +
                          book.getDisplayTitle() + "\"? Volverás al inicio del libro.");

        builder.setPositiveButton("Restablecer", (dialog, which) -> resetBookProgress(book));
        builder.setNegativeButton("Cancelar", (dialog, which) -> dialog.cancel());
        builder.show();
    }

    private void showDeleteBookDialog(Book book) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Eliminar libro");
        builder.setMessage("¿Estás seguro de que deseas eliminar \"" +
                          book.getDisplayTitle() + "\"? Esta acción no se puede deshacer.");

        builder.setPositiveButton("Eliminar", (dialog, which) -> deleteBook(book));
        builder.setNegativeButton("Cancelar", (dialog, which) -> dialog.cancel());
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
                    Toast.makeText(this, "Libro renombrado correctamente", Toast.LENGTH_SHORT).show();
                });

            } catch (Exception e) {
                runOnUiThread(() -> {
                    showError("Error al renombrar el libro: " + e.getMessage());
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
                    Toast.makeText(this, "Progreso restablecido correctamente", Toast.LENGTH_SHORT).show();
                });

            } catch (Exception e) {
                runOnUiThread(() -> {
                    showError("Error al restablecer el progreso: " + e.getMessage());
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
                        Toast.makeText(this, "Libro eliminado correctamente", Toast.LENGTH_SHORT).show();
                    });
                } else {
                    runOnUiThread(() -> {
                        showError("No se pudo eliminar el libro");
                    });
                }

            } catch (Exception e) {
                runOnUiThread(() -> {
                    showError("Error al eliminar el libro: " + e.getMessage());
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

    @Override
    protected void onResume() {
        super.onResume();
        // Refresh book progress when returning from reading
        refreshBookProgress();
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

        // Update the intent and handle the new file
        setIntent(intent);
        handleExternalFileIntent();
    }

    /**
     * Handle when the app is opened via an external file intent
     */
    private void handleExternalFileIntent() {
        Intent intent = getIntent();
        if (Intent.ACTION_VIEW.equals(intent.getAction())) {
            Uri fileUri = intent.getData();
            if (fileUri != null) {
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
                Toast.makeText(this, "Abriendo " + fileName + "...", Toast.LENGTH_SHORT).show();

                // Process the external file directly
                processDocument(fileUri, fileName);
            }
        }
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
        }
        return super.onOptionsItemSelected(item);
    }

    private void toggleTheme() {
        themeManager.toggleTheme();
        // Recreate activity to apply theme changes
        recreate();
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (executor != null) {
            executor.shutdown();
        }
    }
}