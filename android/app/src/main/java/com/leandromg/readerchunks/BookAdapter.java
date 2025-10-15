package com.leandromg.readerchunks;

import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.progressindicator.LinearProgressIndicator;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class BookAdapter extends RecyclerView.Adapter<BookAdapter.BookViewHolder> {

    private List<Book> books;
    private OnBookClickListener listener;
    private BookCacheManager cacheManager;

    public interface OnBookClickListener {
        void onContinueReading(Book book);
        void onRenameBook(Book book);
        void onResetProgress(Book book);
        void onDeleteBook(Book book);
    }

    public BookAdapter(List<Book> books, OnBookClickListener listener, BookCacheManager cacheManager) {
        this.books = books;
        this.listener = listener;
        this.cacheManager = cacheManager;
    }

    @NonNull
    @Override
    public BookViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_book, parent, false);
        return new BookViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BookViewHolder holder, int position) {
        Book book = books.get(position);
        holder.bind(book);
    }

    @Override
    public int getItemCount() {
        return books.size();
    }

    public void updateBooks(List<Book> newBooks) {
        this.books = newBooks;
        notifyDataSetChanged();
    }

    class BookViewHolder extends RecyclerView.ViewHolder {
        private TextView tvBookTitle;
        private TextView tvBookProgress;
        private TextView tvLastRead;
        private LinearProgressIndicator progressBar;
        private MaterialButton btnContinue;
        private MaterialButton btnBookOptions;

        public BookViewHolder(@NonNull View itemView) {
            super(itemView);
            tvBookTitle = itemView.findViewById(R.id.tvBookTitle);
            tvBookProgress = itemView.findViewById(R.id.tvBookProgress);
            tvLastRead = itemView.findViewById(R.id.tvLastRead);
            progressBar = itemView.findViewById(R.id.progressBar);
            btnContinue = itemView.findViewById(R.id.btnContinue);
            btnBookOptions = itemView.findViewById(R.id.btnBookOptions);
        }

        public void bind(Book book) {
            tvBookTitle.setText(book.getDisplayTitle());

            // Progress text with precise calculation
            double progressPercent = book.getPreciseProgressPercentage(cacheManager);

            // Fix the display to show correct paragraph count when completed
            int displayCurrentPosition = book.isCompleted()
                ? book.getTotalSentences()
                : book.getCurrentPosition() + 1;

            String progressText = itemView.getContext().getString(R.string.progress_completed_paragraphs,
                    progressPercent,
                    displayCurrentPosition,
                    book.getTotalSentences());
            tvBookProgress.setText(progressText);

            // Progress bar
            progressBar.setProgress((int) progressPercent);

            // Last read time
            String lastReadText = formatLastReadTime(book.getLastReadDate());
            tvLastRead.setText(lastReadText);

            // Button text based on progress
            if (book.isCompleted()) {
                btnContinue.setText(itemView.getContext().getString(R.string.read_again));
            } else if (book.isStarted()) {
                btnContinue.setText(itemView.getContext().getString(R.string.continue_reading));
            } else {
                btnContinue.setText(itemView.getContext().getString(R.string.start_reading));
            }

            // Click listeners
            btnContinue.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onContinueReading(book);
                }
            });

            btnBookOptions.setOnClickListener(v -> showOptionsMenu(v, book));
        }

        private String formatLastReadTime(Date lastRead) {
            if (lastRead == null) {
                return itemView.getContext().getString(R.string.never_read);
            }

            long diffInMillis = new Date().getTime() - lastRead.getTime();
            long diffInDays = TimeUnit.MILLISECONDS.toDays(diffInMillis);
            long diffInHours = TimeUnit.MILLISECONDS.toHours(diffInMillis);
            long diffInMinutes = TimeUnit.MILLISECONDS.toMinutes(diffInMillis);

            if (diffInMinutes < 1) {
                return itemView.getContext().getString(R.string.just_now);
            } else if (diffInMinutes < 60) {
                return itemView.getContext().getString(R.string.minutes_ago, diffInMinutes);
            } else if (diffInHours < 24) {
                return itemView.getContext().getString(R.string.hours_ago, diffInHours);
            } else if (diffInDays == 1) {
                return itemView.getContext().getString(R.string.yesterday);
            } else if (diffInDays < 7) {
                return itemView.getContext().getString(R.string.days_ago, diffInDays);
            } else {
                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                return itemView.getContext().getString(R.string.date_prefix, sdf.format(lastRead));
            }
        }

        private void showOptionsMenu(View view, Book book) {
            // Create PopupMenu with custom theme for better text visibility
            ContextThemeWrapper contextThemeWrapper = new ContextThemeWrapper(view.getContext(), R.style.CustomPopupMenu);
            PopupMenu popup = new PopupMenu(contextThemeWrapper, view);
            popup.getMenuInflater().inflate(R.menu.book_options_menu, popup.getMenu());

            popup.setOnMenuItemClickListener(item -> {
                if (listener == null) return false;

                int itemId = item.getItemId();
                if (itemId == R.id.action_rename) {
                    listener.onRenameBook(book);
                    return true;
                } else if (itemId == R.id.action_reset_progress) {
                    listener.onResetProgress(book);
                    return true;
                } else if (itemId == R.id.action_delete) {
                    listener.onDeleteBook(book);
                    return true;
                }
                return false;
            });

            popup.show();
        }
    }
}