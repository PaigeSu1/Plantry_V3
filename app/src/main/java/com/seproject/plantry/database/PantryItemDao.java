package com.seproject.plantry.database;

//Database function libaries

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface PantryItemDao {

    // How to talk to the database through functions
    @Insert
    void insert(PantryItem item); // saves all items

    @Update
    void update(PantryItem item);

    @Delete
    void delete(PantryItem item);

    @Query("SELECT * FROM pantry_items WHERE name = :name")
    LiveData<List<PantryItem>> getItemsByName(String name);

    @Query("DELETE FROM pantry_items WHERE id = :id")
    void deleteById(int id);
}