package com.seproject.plantry;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.ListPopupWindow;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.checkbox.MaterialCheckBox;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.textfield.TextInputEditText;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.mlkit.vision.barcode.BarcodeScanner;
import com.google.mlkit.vision.barcode.BarcodeScannerOptions;
import com.google.mlkit.vision.barcode.BarcodeScanning;
import com.google.mlkit.vision.barcode.common.Barcode;
import com.google.mlkit.vision.common.InputImage;
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
    private View manualContainer;
    private View scanningPlaceholder;
    private PreviewView previewView;
    private ActivityResultLauncher<String> cameraPermissionLauncher;

    public AddItemsFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        cameraPermissionLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestPermission(),
                isGranted -> {
                    if (isGranted) {
                        startCamera();
                    } else {
                        Toast.makeText(requireContext(), "Camera permission is required for scanning", Toast.LENGTH_SHORT).show();
                    }
                }
        );
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
        MaterialButton bulkAddButton = view.findViewById(R.id.bulk_add_button);
        manualContainer = view.findViewById(R.id.manual_add_container);
        scanningPlaceholder = view.findViewById(R.id.scanning_placeholder);
        previewView = view.findViewById(R.id.preview_view);
        com.google.android.material.tabs.TabLayout tabLayout = view.findViewById(R.id.tab_layout);

        viewModel = new ViewModelProvider(requireActivity()).get(PantryViewModel.class);

        setupNameDropdown();
        setupCategoryDropdown();
        setupDatePicker(buyDateInput);
        setupDatePicker(expirationDateInput);

        tabLayout.addOnTabSelectedListener(new com.google.android.material.tabs.TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(com.google.android.material.tabs.TabLayout.Tab tab) {
                if (tab.getPosition() == 0) {
                    manualContainer.setVisibility(View.VISIBLE);
                    scanningPlaceholder.setVisibility(View.GONE);
                    previewView.setVisibility(View.GONE);
                } else {
                    manualContainer.setVisibility(View.GONE);
                    scanningPlaceholder.setVisibility(View.VISIBLE);
                    previewView.setVisibility(View.VISIBLE);
                    requestCameraAndStart();
                }
            }

            @Override
            public void onTabUnselected(com.google.android.material.tabs.TabLayout.Tab tab) {}

            @Override
            public void onTabReselected(com.google.android.material.tabs.TabLayout.Tab tab) {}
        });

        bulkAddButton.setOnClickListener(v -> {
            Navigation.findNavController(view).navigate(R.id.action_addItemsFragment_to_bulkAddFragment);
        });

        // Reflect the status of has expiration date visually
        expiryCheckbox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            expirationDateInput.setEnabled(isChecked);
        });

        saveFab.setOnClickListener(v -> saveItem());
        cancelFab.setOnClickListener(v -> clearFields());
    }

    @Override
    public void onCreateOptionsMenu(@NonNull android.view.Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.add_item_menu, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.action_bulk_add) {
            Navigation.findNavController(requireView()).navigate(R.id.action_addItemsFragment_to_bulkAddFragment);
            return true;
        }
        return super.onOptionsItemSelected(item);
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

    private void requestCameraAndStart() {
        if (ContextCompat.checkSelfPermission(requireContext(), android.Manifest.permission.CAMERA)
                == android.content.pm.PackageManager.PERMISSION_GRANTED) {
            startCamera();
        } else {
            cameraPermissionLauncher.launch(android.Manifest.permission.CAMERA);
        }
    }

    private void startCamera() {
        ListenableFuture<ProcessCameraProvider> cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext());

        cameraProviderFuture.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();

                Preview preview = new Preview.Builder().build();
                preview.setSurfaceProvider(previewView.getSurfaceProvider());

                BarcodeScannerOptions options = new BarcodeScannerOptions.Builder()
                        .setBarcodeFormats(Barcode.FORMAT_ALL_FORMATS)
                        .build();
                BarcodeScanner scanner = BarcodeScanning.getClient(options);

                ImageAnalysis imageAnalysis = new ImageAnalysis.Builder()
                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                        .build();

                imageAnalysis.setAnalyzer(ContextCompat.getMainExecutor(requireContext()), image -> {
                    @SuppressWarnings("UnsafeOptInUsageError")
                    android.media.Image mediaImage = image.getImage();
                    if (mediaImage != null) {
                        InputImage inputImage = InputImage.fromMediaImage(mediaImage, image.getImageInfo().getRotationDegrees());
                        scanner.process(inputImage)
                                .addOnSuccessListener(barcodes -> {
                                    for (Barcode barcode : barcodes) {
                                        String rawValue = barcode.getRawValue();
                                        if (rawValue != null) {
                                            handleBarcode(rawValue);
                                            // Optional: Stop scanning after first successful find
                                            cameraProvider.unbindAll();
                                            break;
                                        }
                                    }
                                })
                                .addOnCompleteListener(task -> image.close());
                    } else {
                        image.close();
                    }
                });

                CameraSelector cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA;

                cameraProvider.unbindAll();
                cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageAnalysis);

            } catch (Exception e) {
                Toast.makeText(requireContext(), "Error starting camera: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }, ContextCompat.getMainExecutor(requireContext()));
    }

    private void handleBarcode(String barcode) {
        Toast.makeText(requireContext(), "Scanned: " + barcode, Toast.LENGTH_LONG).show();
        // In a real app, you would look up the product by barcode.
        // For now, let's just populate the name and switch back to manual tab.
        nameInput.setText("Item " + barcode);
        
        com.google.android.material.tabs.TabLayout tabLayout = requireView().findViewById(R.id.tab_layout);
        if (tabLayout != null) {
            tabLayout.getTabAt(0).select();
        }
    }
}
