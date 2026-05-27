package com.seproject.plantry.utils;

import androidx.room.TypeConverter;

public class ExpirationStatusConverter {
    @TypeConverter
    public static String fromExpirationStatus(ExpirationStatus status) {
        return status == null ? ExpirationStatus.NONE.toString() : status.name();
    }

    @TypeConverter
    public static ExpirationStatus toExpirationStatus(String value) {
        return value == null ? ExpirationStatus.NONE : ExpirationStatus.valueOf(value);
    }
}
