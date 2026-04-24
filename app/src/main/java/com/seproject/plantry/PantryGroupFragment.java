package com.seproject.plantry;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.os.Bundle;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.PopupMenu;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.MaterialToolbar;
import com.seproject.plantry.adapter.PantryItemAdapter;
import com.seproject.plantry.database.PantryItem;
import com.seproject.plantry.utils.PantryViewModel;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

public class PantryGroupFragment extends Fragment {

    private PantryViewModel viewModel;
    private PantryItemAdapter adapter;
    private String groupName;

    public PantryGroupFragment() {
        super(R.layout.fragment_group_pantry);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.fragment_group_pantry, container, false);
        
        if (getArguments() != null) {
            groupName = getArguments().getString("groupName");
        }

        MaterialToolbar toolbar = root.findViewById(R.id.pantry_group_toolbar);
        // Ensure the title is the name of the food (e.g., Apple)
        toolbar.setTitle(groupName != null ? groupName : "Item Details");
        
        NavController navController = NavHostFragment.findNavController(this);
        NavigationUI.setupWithNavController(toolbar, navController);

        RecyclerView recyclerView = root.findViewById(R.id.pantry_group_recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        viewModel = new ViewModelProvider(requireActivity()).get(PantryViewModel.class);

        adapter = new PantryItemAdapter(new ArrayList<>(), this::showItemMenu);
        recyclerView.setAdapter(adapter);

        if (groupName != null) {
            viewModel.getItemsByName(groupName).observe(getViewLifecycleOwner(), items -> {
                adapter.setItems(items);
            });
        }

        return root;
    }

    private void showItemMenu(View view, PantryItem item) {
        PopupMenu popup = new PopupMenu(getContext(), view);
        popup.getMenuInflater().inflate(R.menu.menu_item_options, popup.getMenu());
        
        popup.setOnMenuItemClickListener(menuItem -> {
            int id = menuItem.getItemId();
            if (id == R.id.action_edit_qty) {
                showUpdateQuantityDialog(item);
                return true;
            } else if (id == R.id.action_edit_date) {
                showChangeDateDialog(item);
                return true;
            } else if (id == R.id.action_delete) {
                viewModel.deleteItem(item);
                return true;
            }
            return false;
        });
        popup.show();
    }

    private void showUpdateQuantityDialog(PantryItem item) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Update Quantity");

        final EditText input = new EditText(getContext());
        input.setInputType(InputType.TYPE_CLASS_NUMBER);
        input.setText(String.valueOf(item.quantity));
        builder.setView(input);

        builder.setPositiveButton("Update", (dialog, which) -> {
            try {
                int newQty = Integer.parseInt(input.getText().toString());
                item.quantity = newQty;
                viewModel.updateItem(item);
            } catch (NumberFormatException e) {
                // Ignore invalid input
            }
        });
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());

        builder.show();
    }

    private void showChangeDateDialog(PantryItem item) {
        final Calendar calendar = Calendar.getInstance();
        
        DatePickerDialog.OnDateSetListener dateSetListener = (view, year, month, dayOfMonth) -> {
            calendar.set(Calendar.YEAR, year);
            calendar.set(Calendar.MONTH, month);
            calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
            
            String myFormat = "MM/dd/yyyy";
            SimpleDateFormat dateFormat = new SimpleDateFormat(myFormat, Locale.US);
            item.expirationDate = dateFormat.format(calendar.getTime());
            item.isDefaultDate = false; // User manually picked a date now
            viewModel.updateItem(item);
        };

        new DatePickerDialog(requireContext(), dateSetListener,
                calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)).show();
    }
}
