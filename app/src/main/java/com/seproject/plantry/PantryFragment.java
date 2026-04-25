package com.seproject.plantry;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.seproject.plantry.adapter.PantryGroupAdapter;
import com.seproject.plantry.utils.PantryViewModel;

import java.util.ArrayList;

/// The fragment that allows the user to view all of the item groups in the pantry
/// Each group should fall into a category as well
public class PantryFragment extends Fragment {
    private RecyclerView recyclerView;
    private PantryViewModel viewModel;
    private PantryGroupAdapter adapter;

    public PantryFragment() {
        super(R.layout.fragment_pantry);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.fragment_pantry, container, false);

        viewModel = new ViewModelProvider(requireActivity()).get(PantryViewModel.class);

        recyclerView = root.findViewById(R.id.pantry_recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        adapter = new PantryGroupAdapter(new ArrayList<>(), group -> {
            Bundle bundle = new Bundle();
            bundle.putString("groupName", group.name);
            androidx.navigation.fragment.NavHostFragment.findNavController(this)
                    .navigate(R.id.action_pantryFragment_to_pantryGroupFragment, bundle);
        });
        recyclerView.setAdapter(adapter);

        android.widget.EditText searchBar = root.findViewById(R.id.search_bar);
        searchBar.addTextChangedListener(new android.text.TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                adapter.filter(s.toString());
            }

            @Override
            public void afterTextChanged(android.text.Editable s) {}
        });

        // Observe the database and update the list when data changes
        viewModel.getGroups().observe(getViewLifecycleOwner(), groups -> {
            if (groups != null) {
                adapter.setAllGroups(groups);
                updatePagination(root);
            }
        });

        root.findViewById(R.id.prev_page).setOnClickListener(v -> {
            int currentPage = adapter.getCurrentPage();
            if (currentPage > 0) {
                adapter.setCurrentPage(currentPage - 1);
                updatePagination(root);
            }
        });

        root.findViewById(R.id.next_page).setOnClickListener(v -> {
            int currentPage = adapter.getCurrentPage();
            if (currentPage < adapter.getPageCount() - 1) {
                adapter.setCurrentPage(currentPage + 1);
                updatePagination(root);
            }
        });

        return root;
    }

    private void updatePagination(View root) {
        android.widget.TextView pageNum = root.findViewById(R.id.page_number);
        pageNum.setText(String.valueOf(adapter.getCurrentPage() + 1));
        
        root.findViewById(R.id.prev_page).setAlpha(adapter.getCurrentPage() == 0 ? 0.3f : 1.0f);
        root.findViewById(R.id.next_page).setAlpha(adapter.getCurrentPage() >= adapter.getPageCount() - 1 ? 0.3f : 1.0f);
    }
    @Override
    public void onResume(){
        super.onResume();
        viewModel.refreshStatus();
    }
}
