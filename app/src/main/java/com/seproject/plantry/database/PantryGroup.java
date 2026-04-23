package com.seproject.plantry.database;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.util.Objects;

/// The type used for pantry groups (i.e., Apple, Potato, etc.) as opposed to a specific item (e.g., apples purchased on a particular date)
/// This maps to the database containing records with a name, perishability, and expiration date
@Entity(tableName = "pantry_groups")
public class PantryGroup {
    @NonNull
    @PrimaryKey
    public String name;
    public String category;
    /// The current state of the items contained within the group
    /// Should have one of four values: 'none', 'safe', 'soon', or 'expired'
    /// 'none' is used for non-perishable goods
    /// 'safe' should be used when there are no items that are expiring soon or have done so already
    /// 'soon' should be used if there is at least one item expiring soon and none that have already expired
    /// 'expired' should be used if there is at least one expired item
    public String expiryState;

    public PantryGroup(@NonNull String name, String category, String expiryState) {
        this.name = name;
        this.category = category;
        this.expiryState = expiryState;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        PantryGroup r = (PantryGroup) o;
        return Objects.equals(name, r.name) && Objects.equals(category, r.category) && Objects.equals(expiryState, r.expiryState);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, category, expiryState);
    }
}
