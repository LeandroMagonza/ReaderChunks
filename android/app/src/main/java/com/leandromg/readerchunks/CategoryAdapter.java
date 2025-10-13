package com.leandromg.readerchunks;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class CategoryAdapter extends RecyclerView.Adapter<CategoryAdapter.CategoryViewHolder> {

    private List<SettingsDialogManager.CategoryItem> categories;
    private OnCategoryClickListener listener;

    public interface OnCategoryClickListener {
        void onCategoryClick(SettingsDialogManager.SettingsCategory category);
    }

    public CategoryAdapter(List<SettingsDialogManager.CategoryItem> categories, OnCategoryClickListener listener) {
        this.categories = categories;
        this.listener = listener;
    }

    @NonNull
    @Override
    public CategoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.settings_category_item, parent, false);
        return new CategoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CategoryViewHolder holder, int position) {
        SettingsDialogManager.CategoryItem item = categories.get(position);

        if (item.iconResource != 0) {
            holder.ivIcon.setImageResource(item.iconResource);
            holder.ivIcon.setVisibility(View.VISIBLE);
            holder.tvIcon.setVisibility(View.GONE);
        } else {
            holder.tvIcon.setText(item.icon);
            holder.tvIcon.setVisibility(View.VISIBLE);
            holder.ivIcon.setVisibility(View.GONE);
        }

        holder.tvTitle.setText(item.title);
        holder.tvSubtitle.setText(item.subtitle);

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onCategoryClick(item.category);
            }
        });
    }

    @Override
    public int getItemCount() {
        return categories.size();
    }

    static class CategoryViewHolder extends RecyclerView.ViewHolder {
        ImageView ivIcon;
        TextView tvIcon;
        TextView tvTitle;
        TextView tvSubtitle;
        ImageView ivArrow;

        CategoryViewHolder(@NonNull View itemView) {
            super(itemView);
            ivIcon = itemView.findViewById(R.id.ivIcon);
            tvIcon = itemView.findViewById(R.id.tvIcon);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvSubtitle = itemView.findViewById(R.id.tvSubtitle);
            ivArrow = itemView.findViewById(R.id.ivArrow);
        }
    }
}