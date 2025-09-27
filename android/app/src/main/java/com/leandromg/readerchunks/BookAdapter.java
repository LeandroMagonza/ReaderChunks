package com.leandromg.readerchunks;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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

    public interface OnBookClickListener {
        void onContinueReading(Book book);
    }

    public BookAdapter(List<Book> books, OnBookClickListener listener) {
        this.books = books;
        this.listener = listener;
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

        public BookViewHolder(@NonNull View itemView) {
            super(itemView);
            tvBookTitle = itemView.findViewById(R.id.tvBookTitle);
            tvBookProgress = itemView.findViewById(R.id.tvBookProgress);
            tvLastRead = itemView.findViewById(R.id.tvLastRead);
            progressBar = itemView.findViewById(R.id.progressBar);
            btnContinue = itemView.findViewById(R.id.btnContinue);
        }

        public void bind(Book book) {
            tvBookTitle.setText(book.getDisplayTitle());

            // Progress text
            double progressPercent = book.getProgressPercentage();
            String progressText = String.format(Locale.getDefault(),
                    "%.1f%% completado • %d / %d oraciones",
                    progressPercent,
                    book.getCurrentPosition() + 1,
                    book.getTotalSentences());
            tvBookProgress.setText(progressText);

            // Progress bar
            progressBar.setProgress((int) progressPercent);

            // Last read time
            String lastReadText = formatLastReadTime(book.getLastReadDate());
            tvLastRead.setText(lastReadText);

            // Button text based on progress
            if (book.isCompleted()) {
                btnContinue.setText("Leer de nuevo");
            } else if (book.isStarted()) {
                btnContinue.setText("Continuar");
            } else {
                btnContinue.setText("Comenzar");
            }

            // Click listener
            btnContinue.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onContinueReading(book);
                }
            });
        }

        private String formatLastReadTime(Date lastRead) {
            if (lastRead == null) {
                return "Nunca leído";
            }

            long diffInMillis = new Date().getTime() - lastRead.getTime();
            long diffInDays = TimeUnit.MILLISECONDS.toDays(diffInMillis);
            long diffInHours = TimeUnit.MILLISECONDS.toHours(diffInMillis);
            long diffInMinutes = TimeUnit.MILLISECONDS.toMinutes(diffInMillis);

            if (diffInMinutes < 1) {
                return "Hace un momento";
            } else if (diffInMinutes < 60) {
                return String.format(Locale.getDefault(), "Hace %d min", diffInMinutes);
            } else if (diffInHours < 24) {
                return String.format(Locale.getDefault(), "Hace %d horas", diffInHours);
            } else if (diffInDays == 1) {
                return "Ayer";
            } else if (diffInDays < 7) {
                return String.format(Locale.getDefault(), "Hace %d días", diffInDays);
            } else {
                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                return "El " + sdf.format(lastRead);
            }
        }
    }
}