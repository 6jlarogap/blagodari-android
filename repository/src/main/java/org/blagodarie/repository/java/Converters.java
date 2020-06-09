package org.blagodarie.repository.java;

import androidx.room.TypeConverter;

final class Converters {
    @TypeConverter
    public Boolean toBoolean(Integer i) {
        return i == 1;
    }

    @TypeConverter
    public Integer fromBoolean(Boolean b) {
        return b ? 1 : 0;
    }
}
