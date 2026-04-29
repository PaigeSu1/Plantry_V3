package com.seproject.plantry.database;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.seproject.plantry.utils.ExpirationStatus;

import java.util.List;


/**
 * The dao for pantry groups
 */
@Dao
public interface PantryGroupDao {
    @Insert (onConflict= OnConflictStrategy.REPLACE)
    void insert(PantryGroup group);

    @Update
    void update(PantryGroup group);

    @Delete
    void delete(PantryGroup group);

    @Query("SELECT * FROM pantry_groups")
    LiveData<List<PantryGroup>> getAllItems();

    @Query("SELECT * FROM pantry_groups WHERE EXISTS (SELECT 1 FROM pantry_items WHERE pantry_groups.name = pantry_items.name)")
    LiveData<List<PantryGroup>> getAllNonEmptyGroups();

    @Query("SELECT * FROM pantry_groups")
    List<PantryGroup>getAllGroupsSync();

    @Query("UPDATE pantry_groups SET expiryState= :status WHERE name = :name")
    void updateStatus (String name, ExpirationStatus status);
}