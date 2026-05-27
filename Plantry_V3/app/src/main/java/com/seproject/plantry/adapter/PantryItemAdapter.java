package com.seproject.plantry.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.TooltipCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.seproject.plantry.R;
import com.seproject.plantry.database.PantryItem;

import java.util.List;

public class PantryItemAdapter extends RecyclerView.Adapter<PantryItemAdapter.PantryViewHolder> {

    private List<PantryItem> items;
    private final OnItemMenuClickListener menuListener;

    public PantryItemAdapter(List<PantryItem> items, OnItemMenuClickListener menuListener) {
        this.items = items;
        this.menuListener = menuListener;
    }

    @NonNull
    @Override
    public PantryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_pantry, parent, false);
        return new PantryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PantryViewHolder holder, int position) {
        PantryItem item = items.get(position);
        holder.quantity.setText(String.valueOf(item.quantity));
        holder.expiration.setText(item.expirationDate.toString());

        // Handle Status Icon and Default Date indicator
        updateStatusIcon(holder, item);

        holder.moreButton.setOnClickListener(v -> {
            if (menuListener != null) menuListener.onMenuClick(v, item);
        });
    }

    private void updateStatusIcon(PantryViewHolder holder, PantryItem item) {
        // Reset state
        holder.statusIcon.setVisibility(View.GONE);
        TooltipCompat.setTooltipText(holder.statusIcon, null);

        if (item.quantity <= 0) {
            holder.statusIcon.setVisibility(View.VISIBLE);
            holder.statusIcon.setImageResource(R.drawable.ic_error);
            holder.statusIcon.setColorFilter(holder.itemView.getContext().getColor(R.color.md_theme_error));
            TooltipCompat.setTooltipText(holder.statusIcon, "Out of stock");
            return;
        }

        if (item.expirationDate == null) {
            holder.statusIcon.setVisibility(View.INVISIBLE);
            return;
        }

        switch (item.getExpirationStatus()) {
            case EXPIRED:
                holder.statusIcon.setVisibility(View.VISIBLE);
                holder.statusIcon.setImageResource(R.drawable.ic_error);
                holder.statusIcon.setColorFilter(holder.itemView.getContext().getColor(R.color.md_theme_error));
                TooltipCompat.setTooltipText(holder.statusIcon, "Expired");
                break;
            case SOON:
                holder.statusIcon.setVisibility(View.VISIBLE);
                holder.statusIcon.setImageResource(R.drawable.ic_error);
                holder.statusIcon.setColorFilter(holder.itemView.getContext().getColor(R.color.md_theme_onSurface));
                TooltipCompat.setTooltipText(holder.statusIcon, "Expiring Soon");
                break;
            default:
                holder.statusIcon.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    public int getItemCount() {
        return items != null ? items.size() : 0;
    }

    public void setItems(List<PantryItem> items) {
        this.items = items;
        notifyDataSetChanged();
    }

    public interface OnItemMenuClickListener {
        void onMenuClick(View view, PantryItem item);
    }

    static class PantryViewHolder extends RecyclerView.ViewHolder {
        TextView quantity, expiration;
        ImageView statusIcon;
        ImageButton moreButton;

        public PantryViewHolder(@NonNull View itemView) {
            super(itemView);
            quantity = itemView.findViewById(R.id.item_quantity);
            expiration = itemView.findViewById(R.id.item_expiration_date);
            statusIcon = itemView.findViewById(R.id.status_icon);
            moreButton = itemView.findViewById(R.id.item_more_button);
        }
    }
}
