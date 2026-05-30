package com.seproject.plantry.adapter;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.textfield.TextInputEditText;
import com.seproject.plantry.R;

import java.util.List;

public class BulkAddAdapter extends RecyclerView.Adapter<BulkAddAdapter.ViewHolder> {

    public static class BulkItem {
        public String name = "";
        public String quantity = "1";
    }

    private List<BulkItem> items;

    public BulkAddAdapter(List<BulkItem> items) {
        this.items = items;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_bulk_add_row, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        BulkItem item = items.get(position);
        
        if (holder.nameWatcher != null) holder.nameInput.removeTextChangedListener(holder.nameWatcher);
        if (holder.quantityWatcher != null) holder.quantityInput.removeTextChangedListener(holder.quantityWatcher);

        holder.nameInput.setText(item.name);
        holder.quantityInput.setText(item.quantity);

        holder.nameWatcher = new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override
            public void afterTextChanged(Editable s) {
                item.name = s.toString();
            }
        };
        holder.nameInput.addTextChangedListener(holder.nameWatcher);

        holder.quantityWatcher = new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override
            public void afterTextChanged(Editable s) {
                item.quantity = s.toString();
            }
        };
        holder.quantityInput.addTextChangedListener(holder.quantityWatcher);

        holder.deleteButton.setOnClickListener(v -> {
            int pos = holder.getAdapterPosition();
            if (pos != RecyclerView.NO_POSITION) {
                items.remove(pos);
                notifyItemRemoved(pos);
            }
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextInputEditText nameInput;
        EditText quantityInput;
        ImageButton deleteButton;
        TextWatcher nameWatcher;
        TextWatcher quantityWatcher;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            nameInput = itemView.findViewById(R.id.item_name_input);
            quantityInput = itemView.findViewById(R.id.item_quantity_input);
            deleteButton = itemView.findViewById(R.id.delete_button);
        }
    }
}