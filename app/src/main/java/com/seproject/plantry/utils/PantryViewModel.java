package com.seproject.plantry.utils;

import android.app.Application;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Transformations;
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

    public LiveData<List<PantryItem>> getSortedItemsByName(String name) {
        return Transformations.map(itemDao.getItemsByName(name), items -> {
            //sorts based on expiration date
            items.sort((a, b) -> {
                int p1 = a.getExpirationPriority();
                int p2 = b.getExpirationPriority();
                if (p1 != p2) return Integer.compare(p1, p2);
                // if equal priority, sort by date itself.
                return a.expirationDate.compareTo(b.expirationDate);
            });
            return items;
        });
    }

    public void updateGroupStatus(String groupName){
        executorService.execute(()-> {
            //Get items in group.
            List<PantryItem> items = itemDao.getItemsByNameSync(groupName);
            //Determine most urgent priority.
            int worst = 2;
            for (PantryItem i : items) {
                worst = Math.min(worst, i.getExpirationPriority());
            }
            //Map to status strings.
            String newStatus = "safe";
            if (worst == 0) newStatus = "expired";
            else if (worst == 1) newStatus = "soon";
            //Update in DB.
            groupDao.updateStatus(groupName, newStatus);
        });}

    public void updateItem(PantryItem item) {
        executorService.execute(() -> itemDao.update(item));
    }

    public void deleteItem(PantryItem item) {
        executorService.execute(() -> itemDao.delete(item));
    }

    public void refreshStatus(){
        executorService.execute(()-> {
            List<PantryGroup> groups=groupDao.getAllGroupsSync();
            for(PantryGroup g:groups){
                updateGroupStatus(g.name);
            }
        });}
}
