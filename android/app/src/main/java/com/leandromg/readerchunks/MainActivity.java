package com.leandromg.readerchunks;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.progressindicator.CircularProgressIndicator;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity implements BookAdapter.OnBookClickListener {

    private LinearLayout layoutLoading;
    private LinearLayout layoutEmpty;
    private RecyclerView recyclerBooks;
    private ExtendedFloatingActionButton fabAddBook;
    private CircularProgressIndicator progressIndicator;
    private TextView tvStatus;

    private ExecutorService executor;
    private BookCacheManager cacheManager;
    private BookAdapter bookAdapter;
    private List<Book> books;

    private ActivityResultLauncher<String[]> pdfPickerLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initViews();
        setupExecutor();
        setupCacheManager();
        setupRecyclerView();
        setupPdfPicker();
        setupClickListeners();
        loadBooks();
    }

    private void initViews() {
        layoutLoading = findViewById(R.id.layoutLoading);
        layoutEmpty = findViewById(R.id.layoutEmpty);
        recyclerBooks = findViewById(R.id.recyclerBooks);
        fabAddBook = findViewById(R.id.fabAddBook);
        progressIndicator = findViewById(R.id.progressIndicator);
        tvStatus = findViewById(R.id.tvStatus);
    }

    private void setupExecutor() {
        executor = Executors.newSingleThreadExecutor();
    }

    private void setupCacheManager() {
        cacheManager = new BookCacheManager(this);
    }

    private void setupRecyclerView() {
        books = new ArrayList<>();
        bookAdapter = new BookAdapter(books, this);
        recyclerBooks.setLayoutManager(new LinearLayoutManager(this));
        recyclerBooks.setAdapter(bookAdapter);
    }

    private void setupPdfPicker() {
        pdfPickerLauncher = registerForActivityResult(
                new ActivityResultContracts.OpenDocument(),
                this::handlePdfSelection
        );
    }

    private void setupClickListeners() {
        fabAddBook.setOnClickListener(v -> openPdfPicker());
    }

    private void openPdfPicker() {
        pdfPickerLauncher.launch(new String[]{"application/pdf"});
    }

    private void handlePdfSelection(Uri uri) {
        if (uri != null) {
            String fileName = getFileNameFromUri(uri);
            processPdf(uri, fileName);
        }
    }

    private void processPdf(Uri uri, String fileName) {
        showLoading(true);
        tvStatus.setText("Procesando " + fileName + "...");

        executor.execute(() -> {
            try {
                Book book = cacheManager.processAndCacheBook(uri, fileName);

                runOnUiThread(() -> {
                    showLoading(false);
                    books.add(0, book);
                    bookAdapter.updateBooks(books);
                    updateViewState();

                    Toast.makeText(this, "Libro agregado: " + book.getDisplayTitle(), Toast.LENGTH_SHORT).show();
                });

            } catch (Exception e) {
                runOnUiThread(() -> {
                    showLoading(false);
                    showError("Error procesando PDF: " + e.getMessage());
                });
            }
        });
    }

    private void showLoading(boolean show) {
        layoutLoading.setVisibility(show ? View.VISIBLE : View.GONE);
        fabAddBook.setEnabled(!show);

        if (!show) {
            updateViewState();
        }
    }

    private void loadBooks() {
        showLoading(true);
        tvStatus.setText("Cargando biblioteca...");

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

    private String getFileNameFromUri(Uri uri) {
        String path = uri.getPath();
        if (path != null) {
            int index = path.lastIndexOf('/');
            if (index != -1) {
                return path.substring(index + 1);
            }
        }
        return "documento.pdf";
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (executor != null) {
            executor.shutdown();
        }
    }
}