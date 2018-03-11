package com.danlls.daniel.sendlink.db;

import android.arch.persistence.room.TypeConverter;

import java.util.Date;

/**
 * Created by danieL on 1/23/2018.
 */

public class DateConverter {

    @TypeConverter
    public static Date toDate(Long timestamp) {
        return timestamp == null ? null : new Date(timestamp);
    }

    @TypeConverter
    public static Long toTimestamp(Date date) {
        return date == null ? null : date.getTime();
    }
}
