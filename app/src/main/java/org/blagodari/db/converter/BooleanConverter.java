package org.blagodari.db.converter;

import androidx.room.TypeConverter;

public class BooleanConverter {
    @TypeConverter
    public Boolean toBoolean(Integer i) {
        return i == 1;
    }

    @TypeConverter
    public Integer fromBoolean(Boolean b) {
        return b ? 1 : 0;
    }
}
