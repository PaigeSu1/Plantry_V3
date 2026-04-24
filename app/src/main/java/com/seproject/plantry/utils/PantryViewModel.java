package com.seproject.plantry.utils;

import android.app.Application;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.room.Room;

import com.seproject.plantry.database.PantryDatabase;
import com.seproject.plantry.database.PantryGroup;
import com.seproject.plantry.database.PantryGroupDao;
import com.seproject.plantry.database.PantryItem;
import com.seproject.plantry.database.PantryItemDao;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/// The model. All db accessing should go in here
public class PantryViewModel extends AndroidViewModel {
    private final PantryDatabase db;
    private final PantryGroupDao groupDao;
    private final PantryItemDao itemDao;
    private final ExecutorService executorService = Executors.newFixedThreadPool(4);

    public PantryViewModel(Application application) {
        super(application);
        db = PantryDatabase.getInstance(application);
        groupDao = db.pantryGroupDao();
        itemDao = db.pantryItemDao();
    }

    public void addGroup(PantryGroup group) {
        executorService.execute(() -> groupDao.insert(group));
    }

    public LiveData<List<PantryGroup>> getGroups() {
        return groupDao.getAllItems();
    }

    public void addItem(PantryItem item) {
        executorService.execute(() -> itemDao.insert(item));
    }

    public LiveData<List<PantryItem>> getItemsByName(String name) {
        return itemDao.getItemsByName(name);
    }

    public void updateItem(PantryItem item) {
        executorService.execute(() -> itemDao.update(item));
    }

    public void deleteItem(PantryItem item) {
        executorService.execute(() -> itemDao.delete(item));
    }
}
