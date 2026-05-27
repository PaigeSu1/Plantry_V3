package com.seproject.plantry.database;

import static androidx.core.content.ContentProviderCompat.requireContext;

import android.content.SharedPreferences;

import androidx.annotation.Nullable;
import androidx.preference.PreferenceManager;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import com.seproject.plantry.utils.ExpirationStatus;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
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
    @Nullable
    public LocalDate buyDate;
    @Nullable
    public LocalDate expirationDate; // represents the time stamp

    public PantryItem(String name, int quantity, @Nullable LocalDate buyDate, @Nullable LocalDate expirationDate) {
        this.name = name;
        this.quantity = quantity;
        this.buyDate = buyDate;
        this.expirationDate = expirationDate;
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

    public ExpirationStatus getExpirationStatus() {

        // Defaults to safe (2)
        if (expirationDate == null) {
            return ExpirationStatus.SAFE;
        }

        LocalDate today = LocalDate.now();

        // Yells about a potential null pointer, but if expiration date is empty/null, it's caught earlier in the function.
        if (expirationDate.isBefore(today)) return ExpirationStatus.EXPIRED;

        // If expiring within 3 days, 1 (expiring soon). Otherwise, 2 (safe).
        return (ChronoUnit.DAYS.between(today, expirationDate) <= 3) ? ExpirationStatus.SOON : ExpirationStatus.SAFE;
    }


}
