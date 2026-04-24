package com.seproject.plantry.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.seproject.plantry.R;
import com.seproject.plantry.database.PantryGroup;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PantryGroupAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int TYPE_HEADER = 0;
    private static final int TYPE_ITEM = 1;

    private List<Object> flatItems = new ArrayList<>();
    private List<PantryGroup> allGroups = new ArrayList<>();
    private List<PantryGroup> currentFilteredGroups = new ArrayList<>();
    private OnItemClickListener listener;
    
    private int itemsPerPage = 10;
    private int currentPage = 0;

    public interface OnItemClickListener {
        void onItemClick(PantryGroup group);
    }

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
            currentFilteredGroups = new ArrayList<>();
            String lowerQuery = query.toLowerCase();
            for (PantryGroup g : allGroups) {
                boolean matchesName = g.name.toLowerCase().contains(lowerQuery);
                boolean matchesCategory = g.category != null && g.category.toLowerCase().contains(lowerQuery);
                
                if (matchesName || matchesCategory) {
                    currentFilteredGroups.add(g);
                }
            }
        }
        currentPage = 0;
        updateDisplayList();
    }

    public void setCurrentPage(int page) {
        this.currentPage = page;
        updateDisplayList();
    }

    public int getCurrentPage() {
        return currentPage;
    }

    public int getPageCount() {
        if (currentFilteredGroups.isEmpty()) return 1;
        return (int) Math.ceil((double) currentFilteredGroups.size() / itemsPerPage);
    }

    private void updateDisplayList() {
        flatItems.clear();
        
        int start = currentPage * itemsPerPage;
        int end = Math.min(start + itemsPerPage, currentFilteredGroups.size());
        
        List<PantryGroup> pageItems = new ArrayList<>();
        if (start < currentFilteredGroups.size()) {
            pageItems = currentFilteredGroups.subList(start, end);
        }

        if (!pageItems.isEmpty()) {
            Map<String, List<PantryGroup>> grouped = new HashMap<>();
            for (PantryGroup item : pageItems) {
                String cat = item.category != null ? item.category : "Uncategorized";
                if (!grouped.containsKey(cat)) {
                    grouped.put(cat, new ArrayList<>());
                }
                grouped.get(cat).add(item);
            }

            List<String> categories = new ArrayList<>(grouped.keySet());
            Collections.sort(categories);

            for (String category : categories) {
                flatItems.add(category); // Add Header
                flatItems.addAll(grouped.get(category));
            }
        }
        notifyDataSetChanged();
    }

    private void updateList(List<PantryGroup> items) {
        this.currentFilteredGroups = items != null ? items : new ArrayList<>();
        currentPage = 0;
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
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_pantry, parent, false);
            return new ItemViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof HeaderViewHolder) {
            ((HeaderViewHolder) holder).headerText.setText((String) flatItems.get(position));
        } else {
            PantryGroup group = (PantryGroup) flatItems.get(position);
            ItemViewHolder itemHolder = (ItemViewHolder) holder;
            itemHolder.name.setText(group.name);
            
            // Handle Status Icon based on expiryState
            if ("expired".equals(group.expiryState)) {
                itemHolder.statusIcon.setVisibility(View.VISIBLE);
                itemHolder.statusIcon.setImageResource(R.drawable.ic_error);
                itemHolder.statusIcon.setColorFilter(itemHolder.itemView.getContext().getColor(R.color.md_theme_error));
            } else if ("soon".equals(group.expiryState)) {
                itemHolder.statusIcon.setVisibility(View.VISIBLE);
                itemHolder.statusIcon.setImageResource(R.drawable.ic_error); // Replace with warning icon if available
                itemHolder.statusIcon.setColorFilter(itemHolder.itemView.getContext().getColor(R.color.md_theme_tertiary)); // Yellowish/Orange
            } else {
                itemHolder.statusIcon.setVisibility(View.GONE);
            }

            itemHolder.itemView.setOnClickListener(v -> {
                if (listener != null) listener.onItemClick(group);
            });
        }
    }

    @Override
    public int getItemCount() {
        return flatItems.size();
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
        ItemViewHolder(View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.item_name);
            statusIcon = itemView.findViewById(R.id.item_status_icon);
        }
    }
}
