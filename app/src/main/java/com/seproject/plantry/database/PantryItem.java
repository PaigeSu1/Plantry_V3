package com.seproject.plantry.database;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * The type for a particular item or set of items with the same buy date and expiration date.
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

    public int getExpirationPriority() {
        // Defaults to safe (2)
        if (isDefaultDate || expirationDate == null || expirationDate.isEmpty()) {
            return 2;
        }

        try {
            SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy", Locale.US);
            Date expiry = sdf.parse(expirationDate);

            // Get today's date at midnight for comparison
            Calendar cal = Calendar.getInstance();
            cal.set(Calendar.HOUR_OF_DAY, 0);
            cal.set(Calendar.MINUTE, 0);
            cal.set(Calendar.SECOND, 0);
            cal.set(Calendar.MILLISECOND, 0);
            Date today = cal.getTime();

            //Yells about a potential null pointer, but if expiration date is empty/null, it's caught earlier in the function.
            if (expiry.before(today)) return 0;

            long diffInMs = expiry.getTime() - today.getTime();
            long diffInDays = TimeUnit.MILLISECONDS.toDays(diffInMs);

            //If expiring within 3 days, 1 (expiring soon). Otherwise, 2 (safe).
            return (diffInDays <= 3) ? 1 : 2;
        } catch (ParseException e) {
            return 2; // Assumes safe if there's a formatting problem or something.
        }
    }


}
