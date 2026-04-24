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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class PantryItemAdapter extends RecyclerView.Adapter<PantryItemAdapter.PantryViewHolder> {

    private List<PantryItem> items;
    private OnItemMenuClickListener menuListener;

    public interface OnItemMenuClickListener {
        void onMenuClick(View view, PantryItem item);
    }

    public PantryItemAdapter(List<PantryItem> items, OnItemMenuClickListener menuListener) {
        this.items = items;
        this.menuListener = menuListener;
    }

    @NonNull
    @Override
    public PantryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_pantry_batch, parent, false);
        return new PantryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PantryViewHolder holder, int position) {
        PantryItem item = items.get(position);
        holder.quantity.setText(String.valueOf(item.quantity));
        holder.expiration.setText(item.expirationDate);

        // Handle Status Icon
        updateStatusIcon(holder, item);

        holder.moreButton.setOnClickListener(v -> {
            if (menuListener != null) menuListener.onMenuClick(v, item);
        });
    }

    private void updateStatusIcon(PantryViewHolder holder, PantryItem item) {
        if (item.quantity <= 0) {
            holder.statusIcon.setVisibility(View.VISIBLE);
            holder.statusIcon.setImageResource(R.drawable.ic_error);
            holder.statusIcon.setColorFilter(holder.itemView.getContext().getColor(R.color.md_theme_error));
            TooltipCompat.setTooltipText(holder.statusIcon, "Out of stock");
            return;
        }

        if (item.expirationDate == null || item.expirationDate.isEmpty()) {
            holder.statusIcon.setVisibility(View.GONE);
            return;
        }

        try {
            SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy", Locale.US);
            Date expDate = sdf.parse(item.expirationDate);
            Date today = new Date();
            
            long diff = expDate.getTime() - today.getTime();
            long days = diff / (24 * 60 * 60 * 1000);

            if (diff < 0) {
                // Expired
                holder.statusIcon.setVisibility(View.VISIBLE);
                holder.statusIcon.setImageResource(R.drawable.ic_error);
                holder.statusIcon.setColorFilter(holder.itemView.getContext().getColor(R.color.md_theme_error));
                TooltipCompat.setTooltipText(holder.statusIcon, "Expired");
            } else if (days <= 7) {
                // Expiring soon (within 7 days)
                holder.statusIcon.setVisibility(View.VISIBLE);
                holder.statusIcon.setImageResource(R.drawable.ic_error);
                holder.statusIcon.setColorFilter(holder.itemView.getContext().getColor(R.color.md_theme_tertiary));
                TooltipCompat.setTooltipText(holder.statusIcon, "Expiring Soon");
            } else {
                holder.statusIcon.setVisibility(View.GONE);
            }
        } catch (ParseException e) {
            holder.statusIcon.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return items != null ? items.size() : 0;
    }

    static class PantryViewHolder extends RecyclerView.ViewHolder {
        TextView quantity, expiration;
        ImageView statusIcon;
        ImageButton moreButton;

        public PantryViewHolder(@NonNull View itemView) {
            super(itemView);
            quantity = itemView.findViewById(R.id.item_quantity);
            expiration = itemView.findViewById(R.id.item_expiration_date);
            statusIcon = itemView.findViewById(R.id.item_status_icon);
            moreButton = itemView.findViewById(R.id.item_more_button);
        }
    }

    public void setItems(List<PantryItem> items) {
        this.items = items;
        notifyDataSetChanged();
    }
}
