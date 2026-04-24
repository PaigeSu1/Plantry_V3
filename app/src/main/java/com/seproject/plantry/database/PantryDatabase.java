package com.seproject.plantry.database;

// Android / Room imports
import android.content.Context;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

/// The database for all data in the app
@Database(entities = {PantryItem.class, PantryGroup.class}, version = 2)
public abstract class PantryDatabase extends RoomDatabase {
    private static PantryDatabase instance;
    public abstract PantryItemDao pantryItemDao();
    public abstract PantryGroupDao pantryGroupDao();

    public static synchronized PantryDatabase getInstance(Context context) {
        if (instance == null) {
            instance = Room.databaseBuilder(
                            context.getApplicationContext(),
                            PantryDatabase.class,
                            "pantry-db"
                    ).fallbackToDestructiveMigration()
                    .build();
        }
        return instance;
    }
}
