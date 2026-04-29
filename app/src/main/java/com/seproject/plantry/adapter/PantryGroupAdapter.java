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
    private static final int ITEMS_PER_PAGE = 10;

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
                    g.name.toLowerCase().contains(lowerQuery) || g.category.toLowerCase().contains(lowerQuery)
            ).collect(Collectors.toList());
        }

        currentPage = 0;
        updateDisplayList();
    }

    public int getCurrentPage() {
        return currentPage;
    }

    public void setCurrentPage(int page) {
        this.currentPage = page;
        updateDisplayList();
    }

    public int getPageCount() {
        if (currentFilteredGroups.isEmpty()) return 1;
        return (int) Math.ceil((double) currentFilteredGroups.size() / ITEMS_PER_PAGE);
    }

    private void updateDisplayList() {
        flatItems.clear();
        currentFilteredGroups.sort(Comparator.comparing(PantryGroup::getExpiryState));
        int start = currentPage * ITEMS_PER_PAGE;
        int end = Math.min(start + ITEMS_PER_PAGE, currentFilteredGroups.size());

        List<PantryGroup> pageItems = new ArrayList<>();
        if (start < currentFilteredGroups.size()) {
            pageItems = currentFilteredGroups.subList(start, end);
        }

        if (!pageItems.isEmpty()) {
            Map<String, List<PantryGroup>> categoryGroups = new HashMap<>();
            Map<String, ExpirationStatus> categoryStatuses = new HashMap<>();

            for (PantryGroup item : pageItems) {
                String category = item.category != null ? item.category : "Uncategorized";

                if (!categoryGroups.containsKey(category)) {
                    categoryGroups.put(category, new ArrayList<>());
                }

                categoryGroups.get(category).add(item);

                // Priority based on lowest value (0 is expired).
                categoryStatuses.put(category, item.expiryState);
            }

            // Sorted by worst expiration status. If same status, sorts alphabetically.
            List<String> categories = categoryStatuses.entrySet().stream().sorted(Map.Entry.comparingByValue()).map(Map.Entry::getKey).collect(Collectors.toList());

            for (String category : categories) {
                flatItems.add(category); // Add Header
                flatItems.addAll(categoryGroups.get(category));
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
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_group, parent, false);
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
            switch (group.expiryState) {
                case EXPIRED:
                    itemHolder.statusIcon.setVisibility(View.VISIBLE);
                    itemHolder.statusIcon.setImageResource(R.drawable.ic_error);
                    itemHolder.statusIcon.setColorFilter(itemHolder.itemView.getContext().getColor(R.color.md_theme_error));
                    break;
                case SOON:
                    itemHolder.statusIcon.setVisibility(View.VISIBLE);
                    itemHolder.statusIcon.setImageResource(R.drawable.ic_error); // Replace with warning icon if available
                    itemHolder.statusIcon.setColorFilter(itemHolder.itemView.getContext().getColor(R.color.md_theme_onSecondaryContainer)); // Yellowish/Orange
                    break;
                default:
                    itemHolder.statusIcon.setVisibility(View.INVISIBLE);
                    itemHolder.statusIcon.clearColorFilter(); //So that red doesn't go onto safe stuff.
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

        ItemViewHolder(View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.item_name);
            statusIcon = itemView.findViewById(R.id.status_icon);
        }
    }
}
