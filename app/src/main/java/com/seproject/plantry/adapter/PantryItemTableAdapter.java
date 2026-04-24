package com.seproject.plantry.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.seproject.plantry.R;
import com.seproject.plantry.database.PantryItem;

public class PantryItemTableAdapter extends ListAdapter<PantryItem, PantryItemTableAdapter.PantryItemTableViewHolder> {
    protected PantryItemTableAdapter() {
        super(DIFF_CALLBACK);
    }

    private static final DiffUtil.ItemCallback<PantryItem> DIFF_CALLBACK =
            new DiffUtil.ItemCallback<PantryItem>() {
                @Override
                public boolean areItemsTheSame(@NonNull PantryItem oldItem, @NonNull PantryItem newItem) {
                    return oldItem.getId() == newItem.getId();
                }

                @Override
                public boolean areContentsTheSame(@NonNull PantryItem oldItem, @NonNull PantryItem newItem) {
                    return oldItem.equals(newItem);
                }
            };

    @NonNull
    @Override
    public PantryItemTableAdapter.PantryItemTableViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_pantry, parent, false);
        return new PantryItemTableAdapter.PantryItemTableViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PantryItemTableAdapter.PantryItemTableViewHolder holder, int position) {
        PantryItem item = getItem(position);
        holder.quantity.setText(String.valueOf(item.quantity));
        holder.buy.setText(String.valueOf(item.buyDate));
        holder.expiration.setText(String.valueOf(item.expirationDate));
    }

    public static class PantryItemTableViewHolder extends RecyclerView.ViewHolder {
        TextView quantity;
        TextView buy;
        TextView expiration;

        public PantryItemTableViewHolder(@NonNull View itemView) {
            super(itemView);
            quantity = itemView.findViewById(R.id.item_quantity);
            buy = itemView.findViewById(R.id.item_buy_date);
            expiration = itemView.findViewById(R.id.item_expiration_date);
        }
    }
}
