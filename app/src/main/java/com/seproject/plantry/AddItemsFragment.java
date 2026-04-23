package com.seproject.plantry;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.seproject.plantry.database.PantryItem;
import com.seproject.plantry.utils.PantryViewModel;

/// The fragment for any logic pertaining to adding items to the db
public class AddItemsFragment extends Fragment {

    public AddItemsFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_add_items, container, false);
    }

    private PantryViewModel viewmod;

//    @Override
//    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState){
//        viewmod=new ViewModelProvider(requireActivity()).get(PantryViewModel.class);
//        //Change to actual names as needed
//        view.findViewById(R.id.add_item_button).setOnClickListener(v->{
//            String name=nameInput.getText().toString();
//            int qty = Integer.parseInt(qtyInput.getText().toString());
//            String buyDate=buyDateInput.getText().toString();
//            String expDate=expDateInput.getText().toString();
//
//            PantryItem newItem=new PantryItem(name,qty,buyDate,expDate);
//
//            //Background thread because Room db operations require it.
//            new Thread(() -> viewmod.addItem(newItem)).start();
//        });
//        }
    }
