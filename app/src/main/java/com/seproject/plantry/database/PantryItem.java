package com.seproject.plantry.database;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.util.Objects;

/**
 * The type for a particular item or set of items with the same buy date and expiration date.
 *
 * This maps to the database containing records with id, name, quantity, buyDate, and expirationDate
 */
@Entity(tableName = "pantry_items")
public class PantryItem {
    // Create the database table
    // id               auto number
    // name             item name
    // barcode          scanned code
    // expirationDate   the expiration date (for reminders)

    @PrimaryKey(autoGenerate = true)
    public int id;
    public String name;
    public int quantity;
    public String buyDate;
    public String expirationDate; // represents the time stamp
    public boolean isDefaultDate;

    public PantryItem(String name, int quantity, String buyDate, String expirationDate, boolean isDefaultDate) {
        this.name = name;
        this.quantity = quantity;
        this.buyDate = buyDate;
        this.expirationDate = expirationDate;
        this.isDefaultDate = isDefaultDate;
    }

    public int getId() {
        return id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        PantryItem r = (PantryItem) o;
        return id == r.id && quantity == r.quantity && Objects.equals(buyDate, r.buyDate) && Objects.equals(expirationDate, r.expirationDate);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, quantity, buyDate, expirationDate);
    }
}
