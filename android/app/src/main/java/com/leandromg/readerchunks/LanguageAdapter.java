package com.leandromg.readerchunks;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class LanguageAdapter extends RecyclerView.Adapter<LanguageAdapter.LanguageViewHolder> {

    private List<LanguageManager.Language> languages;
    private String currentLanguageCode;
    private OnLanguageSelectedListener listener;

    public interface OnLanguageSelectedListener {
        void onLanguageSelected(LanguageManager.Language language);
    }

    public LanguageAdapter(List<LanguageManager.Language> languages, String currentLanguageCode, OnLanguageSelectedListener listener) {
        this.languages = languages;
        this.currentLanguageCode = currentLanguageCode;
        this.listener = listener;
    }

    @NonNull
    @Override
    public LanguageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.dialog_language_item, parent, false);
        return new LanguageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull LanguageViewHolder holder, int position) {
        LanguageManager.Language language = languages.get(position);

        holder.ivFlag.setImageResource(language.flagResource);
        holder.tvLanguageName.setText(language.displayName);
        holder.rbLanguage.setChecked(language.code.equals(currentLanguageCode));

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onLanguageSelected(language);
            }
        });

        holder.rbLanguage.setOnClickListener(v -> {
            if (listener != null) {
                listener.onLanguageSelected(language);
            }
        });
    }

    @Override
    public int getItemCount() {
        return languages.size();
    }

    static class LanguageViewHolder extends RecyclerView.ViewHolder {
        ImageView ivFlag;
        TextView tvLanguageName;
        RadioButton rbLanguage;

        LanguageViewHolder(@NonNull View itemView) {
            super(itemView);
            ivFlag = itemView.findViewById(R.id.ivFlag);
            tvLanguageName = itemView.findViewById(R.id.tvLanguageName);
            rbLanguage = itemView.findViewById(R.id.rbLanguage);
        }
    }
}