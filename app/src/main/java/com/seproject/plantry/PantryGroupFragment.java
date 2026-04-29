package com.seproject.plantry;

import android.app.AlertDialog;
import android.os.Bundle;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.PopupMenu;
import android.widget.TextView;

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
import com.google.android.material.datepicker.MaterialDatePicker;
import com.seproject.plantry.adapter.PantryItemAdapter;
import com.seproject.plantry.database.PantryItem;
import com.seproject.plantry.utils.PantryViewModel;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;

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
                showChangeExpirationDateDialog(item);
                return true;
            } else if (id == R.id.action_delete) {
                viewModel.deleteItem(item);
                //Reminds the view that the expiration data is outdated.
                viewModel.updateGroupStatus(item.name);
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
                item.quantity = Integer.parseInt(input.getText().toString());
                viewModel.updateItem(item);
            } catch (NumberFormatException e) {
                // Ignore invalid input
            }
        });
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());

        builder.show();
    }

    private void showChangeExpirationDateDialog(PantryItem item) {
        MaterialDatePicker<Long> picker = MaterialDatePicker.Builder.datePicker().setTitleText("Select a date").setSelection(item.expirationDate != null ? item.expirationDate.toEpochDay() : MaterialDatePicker.todayInUtcMilliseconds()).build();

        picker.show(requireActivity().getSupportFragmentManager(), "MATERIAL_DATE_PICKER");

        picker.addOnPositiveButtonClickListener(selection -> {
            LocalDate selectedDate = Instant.ofEpochMilli(selection).atZone(ZoneId.systemDefault()).toLocalDate();

            if (selectedDate != null) {
                item.expirationDate = selectedDate;
                viewModel.updateItem(item);
                viewModel.updateGroupStatus(groupName);
            }
        });
    }
}
