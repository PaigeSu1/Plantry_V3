package com.seproject.plantry;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.search.SearchBar;
import com.google.android.material.search.SearchView;
import com.seproject.plantry.adapter.PantryAdapter;
import com.seproject.plantry.database.PantryDao;
import com.seproject.plantry.database.PantryDatabase;
import com.seproject.plantry.database.PantryItem;

import java.util.ArrayList;
import java.util.List;

public class PantryFragment extends Fragment {
    private RecyclerView recyclerView;
    private PantryAdapter adapter;

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

        // The recyclerview stuff was commented out in order to test the pantry item cards
        // recyclerView = root.findViewById(R.id.pantry_container_view);
        // recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        adapter = new PantryAdapter(new ArrayList<>());
        // recyclerView.setAdapter(adapter);

        PantryDatabase db = PantryDatabase.getInstance(getContext());
        PantryDao dao = db.pantryDao();

        dao.getAllItems().observe(getViewLifecycleOwner(), pantryItems -> {
            adapter.setItems(pantryItems);
        });

        PantryItem testItem = new PantryItem("Test", 1, 1);
        adapter.addItem(testItem);

        return root;
    }
}