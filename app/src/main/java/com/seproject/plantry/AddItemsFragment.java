package com.seproject.plantry;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import com.seproject.plantry.database.PantryGroup;
import com.seproject.plantry.database.PantryItem;
import com.seproject.plantry.utils.PantryViewModel;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class AddItemsFragment extends Fragment {

    private TextInputEditText nameInput, quantityInput, buyDateInput;
    private AutoCompleteTextView categoryInput;
    private PantryViewModel viewModel;
    private final Calendar calendar = Calendar.getInstance();

    public AddItemsFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_add_items, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        nameInput = view.findViewById(R.id.nameInput);
        quantityInput = view.findViewById(R.id.quantityInput);
        categoryInput = view.findViewById(R.id.categoryInput);
        buyDateInput = view.findViewById(R.id.buyDateInput);
        FloatingActionButton saveFab = view.findViewById(R.id.saveFab);
        FloatingActionButton cancelFab = view.findViewById(R.id.cancelFab);

        viewModel = new ViewModelProvider(requireActivity()).get(PantryViewModel.class);

        setupCategoryDropdown();
        setupDatePicker();

        saveFab.setOnClickListener(v -> saveItem());
        cancelFab.setOnClickListener(v -> clearFields());
    }

    private void setupCategoryDropdown() {
        viewModel.getGroups().observe(getViewLifecycleOwner(), groups -> {
            List<String> categories = new ArrayList<>();
            categories.add(getString(R.string.fruit));
            categories.add(getString(R.string.meat));
            categories.add(getString(R.string.vegetable));

            if (groups != null) {
                for (PantryGroup group : groups) {
                    if (group.category != null && !categories.contains(group.category)) {
                        categories.add(group.category);
                    }
                }
            }
            ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(),
                    android.R.layout.simple_dropdown_item_1line, categories);
            categoryInput.setAdapter(adapter);
        });
    }

    private void setupDatePicker() {
        DatePickerDialog.OnDateSetListener dateSetListener = (view, year, month, dayOfMonth) -> {
            calendar.set(Calendar.YEAR, year);
            calendar.set(Calendar.MONTH, month);
            calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
            updateLabel();
        };

        View.OnClickListener clickListener = v -> new DatePickerDialog(requireContext(), dateSetListener,
                calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)).show();

        buyDateInput.setOnClickListener(clickListener);
    }

    private void updateLabel() {
        String myFormat = "MM/dd/yyyy";
        SimpleDateFormat dateFormat = new SimpleDateFormat(myFormat, Locale.US);
        buyDateInput.setText(dateFormat.format(calendar.getTime()));
    }

    private void saveItem() {
        String name = nameInput.getText().toString().trim();
        String qtyStr = quantityInput.getText().toString().trim();
        String category = categoryInput.getText().toString().trim();
        String date = buyDateInput.getText().toString().trim();

        // If no date was picked, use the current day's date as default
        if (date.isEmpty()) {
            String myFormat = "MM/dd/yyyy";
            SimpleDateFormat dateFormat = new SimpleDateFormat(myFormat, Locale.US);
            date = dateFormat.format(Calendar.getInstance().getTime());
        }

        if (name.isEmpty() || qtyStr.isEmpty() || category.isEmpty()) {
            Toast.makeText(getContext(), "Please fill in all required fields", Toast.LENGTH_SHORT).show();
            return;
        }

        int quantity;
        try {
            quantity = Integer.parseInt(qtyStr);
        } catch (NumberFormatException e) {
            Toast.makeText(getContext(), "Invalid quantity", Toast.LENGTH_SHORT).show();
            return;
        }

        // Save the item
        PantryItem newItem = new PantryItem(name, quantity, date, date); // Using same date for expiry as placeholder
        viewModel.addItem(newItem);

        // Save the group/category if it's new
        PantryGroup newGroup = new PantryGroup(name, category, "safe");
        viewModel.addGroup(newGroup);

        Toast.makeText(getContext(), "Item saved successfully", Toast.LENGTH_SHORT).show();
        clearFields();
        
        // Go back to the pantry view
        if (getActivity() != null) {
            getActivity().onBackPressed();
        }
    }

    private void clearFields() {
        nameInput.setText("");
        quantityInput.setText("");
        categoryInput.setText("");
        buyDateInput.setText("");
    }
}
