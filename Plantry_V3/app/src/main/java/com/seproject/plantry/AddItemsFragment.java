package com.seproject.plantry;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.ListPopupWindow;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.checkbox.MaterialCheckBox;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.textfield.TextInputEditText;
import com.seproject.plantry.database.PantryGroup;
import com.seproject.plantry.database.PantryItem;
import com.seproject.plantry.utils.PantryViewModel;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

import nl.dionsegijn.konfetti.core.PartyFactory;
import nl.dionsegijn.konfetti.core.emitter.Emitter;
import nl.dionsegijn.konfetti.core.emitter.EmitterConfig;
import nl.dionsegijn.konfetti.core.models.Shape;
import nl.dionsegijn.konfetti.xml.KonfettiView;

public class AddItemsFragment extends Fragment {
    private TextInputEditText nameInput, categoryInput, quantityInput, buyDateInput, expirationDateInput;
    private MaterialCheckBox expiryCheckbox;
    private KonfettiView konfettiView;
    private PantryViewModel viewModel;

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

        nameInput = view.findViewById(R.id.name_text);
        quantityInput = view.findViewById(R.id.quantity_text);
        categoryInput = view.findViewById(R.id.category_text);
        expiryCheckbox = view.findViewById(R.id.expiry_checkbox);
        buyDateInput = view.findViewById(R.id.buy_date_text);
        expirationDateInput = view.findViewById(R.id.expiration_date_text);
        konfettiView = view.findViewById(R.id.konfetti_view);
        MaterialButton saveFab = view.findViewById(R.id.save_item_button);
        MaterialButton cancelFab = view.findViewById(R.id.cancel_item_button);

        viewModel = new ViewModelProvider(requireActivity()).get(PantryViewModel.class);

        setupNameDropdown();
        setupCategoryDropdown();
        setupDatePicker(buyDateInput);
        setupDatePicker(expirationDateInput);

        // Reflect the status of has expiration date visually
        expiryCheckbox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            expirationDateInput.setEnabled(isChecked);
        });

        saveFab.setOnClickListener(v -> saveItem());
        cancelFab.setOnClickListener(v -> clearFields());
    }

    private void setupNameDropdown() {
        viewModel.getGroups().observe(getViewLifecycleOwner(), groups -> {
            List<String> names = new ArrayList<>();

            if (groups != null) {
                for (PantryGroup group : groups) {
                    if (!names.contains(group.name)) {
                        names.add(group.name);
                    }
                }
            }

            createDropdown(nameInput, names);
        });
    }

    private void setupCategoryDropdown() {
        viewModel.getGroups().observe(getViewLifecycleOwner(), groups -> {
            List<String> categories = new ArrayList<>();

            if (groups != null) {
                for (PantryGroup group : groups) {
                    if (group.category != null && !categories.contains(group.category)) {
                        categories.add(group.category);
                    }
                }
            }

            createDropdown(categoryInput, categories);
        });
    }

    /// Make a dropdown to display under a TextInput
    private void createDropdown(TextInputEditText anchor, List<String> data) {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_dropdown_item_1line, data);
        ListPopupWindow popup = new ListPopupWindow(requireContext());
        popup.setAnchorView(anchor);
        popup.setAdapter(adapter);
        popup.setWidth(anchor.getWidth());
        popup.setModal(false);


        popup.setOnItemClickListener((parent, view, position, id) -> {
            String value = adapter.getItem(position);

            anchor.setText(value);
            anchor.setSelection(value != null ? value.length() : 0);
            anchor.clearFocus();

            popup.dismiss();
        });

        anchor.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {
                popup.dismiss();
            }
        });

        anchor.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (!anchor.hasFocus()) {
                    return;
                }

                String query = s.toString().toLowerCase();
                List<String> filteredList = new ArrayList<>();

                for (String item : data) {
                    if (item.toLowerCase().contains(query)) {
                        filteredList.add(item);
                    }
                }

                adapter.clear();
                adapter.addAll(filteredList);
                adapter.notifyDataSetChanged();

                if (!popup.isShowing() && anchor.hasFocus()) {
                    popup.show();
                }
            }
        });
    }

    private void setupDatePicker(TextInputEditText input) {
        MaterialDatePicker<Long> picker = MaterialDatePicker.Builder.datePicker().setTitleText("Select a date").setSelection(MaterialDatePicker.todayInUtcMilliseconds()).build();

        input.setOnClickListener(v -> picker.show(requireActivity().getSupportFragmentManager(), "MATERIAL_DATE_PICKER" + input.getId()));

        picker.addOnPositiveButtonClickListener(selection -> {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/dd/yyyy");
            LocalDate selectedDate = Instant.ofEpochMilli(selection).atOffset(ZoneOffset.UTC).toLocalDate();

            if (selectedDate != null) {
                input.setText(selectedDate.format(formatter));
            } else {
                input.setText("");
            }
        });
    }

    private void saveItem() {
        String name = nameInput.getText().toString().trim();
        String quantityString = quantityInput.getText().toString().trim();
        String category = categoryInput.getText().toString().trim();
        String buyDateString = buyDateInput.getText().toString().trim();
        String expirationDateString = expirationDateInput.getText().toString().trim();
        boolean hasExpirationDate = expiryCheckbox.isChecked();

        // If any required fields are empty, warn the user and prevent saving
        // Check this BEFORE doing anything else!!
        if (name.isEmpty() || quantityString.isEmpty() || category.isEmpty() || buyDateString.isEmpty() || (hasExpirationDate && expirationDateString.isEmpty())) {
            Toast.makeText(getContext(), "Please fill in all required fields", Toast.LENGTH_SHORT).show();
            return;
        }

        LocalDate buyDate, expirationDate;

        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/dd/yyyy");

            // guaranteed to be non-empty by this point (but not necessarily valid)
            buyDate = LocalDate.parse(buyDateString, formatter);

            // Validate the expiration date
            if (hasExpirationDate) {
                expirationDate = LocalDate.parse(expirationDateString, formatter);
            } else {
                expirationDate = null;
            }
        } catch (DateTimeParseException e) {
            Toast.makeText(getContext(), "Enter a valid mm/dd/yyyy date", Toast.LENGTH_SHORT).show();
            return;
        }

        int quantity;
        try {
            quantity = Integer.parseInt(quantityString);
        } catch (NumberFormatException e) {
            Toast.makeText(getContext(), "Invalid quantity", Toast.LENGTH_SHORT).show();
            return;
        }

        PantryItem newItem = new PantryItem(name, quantity, buyDate, expirationDate);
        viewModel.addItem(newItem);

        // Save the group/category if it's new
        PantryGroup newGroup = new PantryGroup(name, category, newItem.getExpirationStatus());
        viewModel.addGroup(newGroup);

        Toast.makeText(getContext(), "Item saved successfully", Toast.LENGTH_SHORT).show();
        explodeConfetti();
        clearFields();
    }

    private void explodeConfetti() {
        EmitterConfig emitterConfig = new Emitter(100L, java.util.concurrent.TimeUnit.MILLISECONDS).max(100);
        konfettiView.start(
                new PartyFactory(emitterConfig)
                        .spread(360)
                        .shapes(java.util.Arrays.asList(Shape.Square.INSTANCE, Shape.Circle.INSTANCE))
                        .colors(java.util.Arrays.asList(0xfce18a, 0xff726d, 0xf4306d, 0xb48def))
                        .setSpeedBetween(0f, 30f)
                        .position(0.5, 0.5)
                        .build()
        );
    }

    private void clearFields() {
        nameInput.setText("");
        quantityInput.setText("");
        categoryInput.setText("");
        buyDateInput.setText("");
        expirationDateInput.setText("");
    }
}
