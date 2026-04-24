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
import com.seproject.plantry.database.PantryGroup;
import com.seproject.plantry.database.PantryGroupDao;
import com.seproject.plantry.database.PantryDatabase;
import com.seproject.plantry.database.PantryItem;
import com.seproject.plantry.utils.PantryViewModel;

import java.util.ArrayList;

/// The fragment that allows the user to view all of the item groups in the pantry
/// Each group should fall into a category as well
public class PantryFragment extends Fragment {
    // private RecyclerView recyclerView;
    // private PantryViewModel viewModel;
    // private PantryGroupAdapter adapter;

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

        // viewModel = new ViewModelProvider(requireActivity()).get(PantryViewModel.class);

        // The recyclerview stuff was commented out in order to test the pantry item cards
        // recyclerView = root.findViewById(R.id.pantry_recycler_view);
        // recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        // adapter = new PantryGroupAdapter(new ArrayList<>());
        // recyclerView.setAdapter(adapter);

        PantryGroup testGroup = new PantryGroup("Test", "Test", true, "mm/dd/yyyy");
        PantryItem testItem = new PantryItem("Test", 1, "mm/dd/yyyy", "mm/dd/yyyy");

        // adapter.addItem(testGroup);

        return root;
    }
}