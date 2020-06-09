package org.blagodari.repository.java;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.util.Locale;

/**
 * Базовый абстрактный класс для всех сущностей базы данных.
 * Содержит общие для всех сущностей поля и методы.
 *
 * @author sergeGabrus
 */
@Entity
abstract class BaseEntity {

    /**
     * Идентификатор сущности.
     * Первичный ключ для всех сущностей, генерируется автоматически.
     * Название столбца таблицы - id.
     */
    @PrimaryKey (autoGenerate = true)
    @ColumnInfo (name = "id")
    private Long Id;

    BaseEntity (final Long id) {
        this.Id = id;
    }

    /**
     * Возвращает идентификатор сущности.
     *
     * @return Идентификатор сущности.
     */
    public final Long getId () {
        return this.Id;
    }

    /**
     * Устанавливает идентификатор сущности.
     *
     * @param id Идентификатор сущности.
     */
    final void setId (final Long id) {
        this.Id = id;
    }

    /**
     * Метод представляет сущность в виде строки, содержащей поля, которые входят в таблицу базы данных.
     *
     * @return Возвращает строковое представление сущности.
     */
    @NonNull
    @Override
    public String toString () {
        //установить формат строки
        final String format = "@[Id=%d]";
        //установить массив значений
        final Object[] args = {this.Id};
        //вернуть конкатенацию названия класса и сформированной строки текущего класса
        return getClass().getSimpleName() +
                String.format(
                        Locale.ENGLISH,//устанавливаем Locale.ENGLISH, чтобы разделителем в числах с плавающей точкой была точка, а не запятая (2.343 а не 2,343)
                        format,
                        args);
    }
}
