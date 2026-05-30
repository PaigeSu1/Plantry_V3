package com.seproject.plantry.adapter;

import android.content.res.ColorStateList;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.card.MaterialCardView;
import com.seproject.plantry.R;
import com.seproject.plantry.database.PantryGroup;
import com.seproject.plantry.utils.ExpirationStatus;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class PantryGroupAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int TYPE_HEADER = 0;
    private static final int TYPE_ITEM = 1;
    private static final int ITEMS_PER_PAGE = 50; // Increased for storage view

    private final List<Object> flatItems = new ArrayList<>();
    private final OnItemClickListener listener;
    private List<PantryGroup> allGroups = new ArrayList<>();
    private List<PantryGroup> currentFilteredGroups = new ArrayList<>();
    private int currentPage = 0;

    public PantryGroupAdapter(List<PantryGroup> items, OnItemClickListener listener) {
        this.listener = listener;
        setAllGroups(items);
    }

    public void setAllGroups(List<PantryGroup> items) {
        this.allGroups = items != null ? items : new ArrayList<>();
        updateList(allGroups);
    }

    public void filter(String query) {
        if (query.isEmpty()) {
            currentFilteredGroups = new ArrayList<>(allGroups);
        } else {
            String lowerQuery = query.toLowerCase();
            currentFilteredGroups = allGroups.stream().filter(g ->
                    g.name.toLowerCase().contains(lowerQuery) || 
                    (g.category != null && g.category.toLowerCase().contains(lowerQuery))
            ).collect(Collectors.toList());
        }
        currentPage = 0;
        updateDisplayList();
    }

    private void updateDisplayList() {
        flatItems.clear();
        // Sort items: Expired first, then by name
        currentFilteredGroups.sort((g1, g2) -> {
            if (g1.expiryState != g2.expiryState) {
                return g1.expiryState.compareTo(g2.expiryState);
            }
            return g1.name.compareToIgnoreCase(g2.name);
        });

        if (!currentFilteredGroups.isEmpty()) {
            Map<String, List<PantryGroup>> categoryGroups = new HashMap<>();
            for (PantryGroup item : currentFilteredGroups) {
                String category = (item.category != null && !item.category.isEmpty()) 
                                   ? item.category.toUpperCase() : "UNCATEGORIZED";
                if (!categoryGroups.containsKey(category)) {
                    categoryGroups.put(category, new ArrayList<>());
                }
                categoryGroups.get(category).add(item);
            }

            // Add headers and items to the flat list
            List<String> sortedCategories = new ArrayList<>(categoryGroups.keySet());
            java.util.Collections.sort(sortedCategories);

            for (String category : sortedCategories) {
                flatItems.add(category); // Header
                flatItems.addAll(categoryGroups.get(category)); // Items
            }
        }
        notifyDataSetChanged();
    }

    private void updateList(List<PantryGroup> items) {
        this.currentFilteredGroups = items != null ? items : new ArrayList<>();
        updateDisplayList();
    }

    @Override
    public int getItemViewType(int position) {
        return flatItems.get(position) instanceof String ? TYPE_HEADER : TYPE_ITEM;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == TYPE_HEADER) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_category_header, parent, false);
            return new HeaderViewHolder(view);
        } else {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_group, parent, false);
            return new ItemViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof HeaderViewHolder) {
            HeaderViewHolder headerHolder = (HeaderViewHolder) holder;
            String title = (String) flatItems.get(position);
            headerHolder.headerText.setText(title);
            
            // Set header color based on category name for "Glass" look
            int colorRes = R.color.md_theme_primary;
            if (title.contains("DAIRY")) colorRes = R.color.cat_fridge_text;
            else if (title.contains("FRUIT")) colorRes = R.color.cat_pantry_text;
            else if (title.contains("VEGETABLE")) colorRes = R.color.cat_counter_text;
            
            headerHolder.headerText.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), colorRes));

        } else {
            PantryGroup group = (PantryGroup) flatItems.get(position);
            ItemViewHolder itemHolder = (ItemViewHolder) holder;
            itemHolder.name.setText(group.name);

            // Apply Status Colors & Glass Effect
            int bgColor = R.color.status_ok_bg;
            int strokeColor = R.color.status_ok_stroke;
            int iconVisibility = View.GONE;

            if (group.expiryState == ExpirationStatus.EXPIRED) {
                bgColor = R.color.status_expired_bg;
                strokeColor = R.color.status_expired_stroke;
                iconVisibility = View.VISIBLE;
                itemHolder.statusIcon.setImageResource(R.drawable.ic_error);
                itemHolder.statusIcon.setColorFilter(ContextCompat.getColor(holder.itemView.getContext(), R.color.md_theme_error));
            } else if (group.expiryState == ExpirationStatus.SOON) {
                bgColor = R.color.status_warning_bg;
                strokeColor = R.color.status_warning_stroke;
                iconVisibility = View.VISIBLE;
                itemHolder.statusIcon.setImageResource(R.drawable.ic_error); // Or ic_warning
                itemHolder.statusIcon.setColorFilter(ContextCompat.getColor(holder.itemView.getContext(), R.color.colorCaution));
            }

            itemHolder.card.setCardBackgroundColor(ContextCompat.getColor(holder.itemView.getContext(), bgColor));
            itemHolder.card.setStrokeColor(ContextCompat.getColor(holder.itemView.getContext(), strokeColor));
            itemHolder.statusIcon.setVisibility(iconVisibility);

            itemHolder.itemView.setOnClickListener(v -> {
                if (listener != null) listener.onItemClick(group);
            });
        }
    }

    @Override
    public int getItemCount() {
        return flatItems.size();
    }

    public interface OnItemClickListener {
        void onItemClick(PantryGroup group);
    }

    static class HeaderViewHolder extends RecyclerView.ViewHolder {
        TextView headerText;
        HeaderViewHolder(View itemView) {
            super(itemView);
            headerText = itemView.findViewById(R.id.category_header);
        }
    }

    static class ItemViewHolder extends RecyclerView.ViewHolder {
        TextView name;
        ImageView statusIcon;
        MaterialCardView card;

        ItemViewHolder(View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.item_name);
            statusIcon = itemView.findViewById(R.id.status_icon);
            card = itemView.findViewById(R.id.item_card);
        }
    }
    
    // Pagination methods kept for compatibility with PantryFragment
    public int getCurrentPage() { return currentPage; }
    public void setCurrentPage(int page) { this.currentPage = page; updateDisplayList(); }
    public int getPageCount() { return 1; }
}
