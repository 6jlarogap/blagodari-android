package org.blagodarie.repository.java;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Index;

import java.util.Locale;

/**
 * Класс, определяющий сущность Тип Ключа
 * <p>
 * Название таблицы - tbl_keyztype
 * <p>
 * Индексы:
 * - наименование ключа - уникально;
 *
 * @author sergeGabrus
 */
@Entity (
        tableName = "tbl_key_type",
        indices = {
                @Index (value = {"title"}, unique = true)
        }
)
public final class KeyType
        extends BaseEntity {

    /**
     * Перечисление Типов Ключей. Типы Ключей, перечисленные здесь, должны быть автоматически
     * добавлены в таблицу tbl_keyztype, после создания базы данных.
     */
    public enum Type {
        /**
         * Номер телефона.
         */
        PHONE_NUMBER(1L, "PhoneNumber"),
        /**
         * Адрес электронной почты.
         */
        EMAIL(2L, "Email"),
        /**
         * Идентификатор аккаунта Google.
         */
        GOOGLE_ACCOUNT_ID(3L, "GoogleAccountId");

        /**
         * Тип ключа.
         */
        @NonNull
        private final KeyType mKeyType;

        /**
         * Конструктор, создает новый объект.
         *
         * @param id    Идентификатор типа.
         * @param title Наименование типа.
         */
        Type (
                @NonNull final Long id,
                @NonNull final String title
        ) {
            this.mKeyType = new KeyType(id, title);
        }

        /**
         * Метод создает сущность {@link KeyType}.
         *
         * @return Возвращает сущность Тип Ключа.
         */
        @NonNull
        public final KeyType getKeyType () {
            return this.mKeyType;
        }
    }

    /**
     * Наименование.
     * Не может быть пустым.
     * Название столбца таблицы - title.
     */
    @NonNull
    @ColumnInfo (name = "title")
    private final String Title;

    /**
     * Конструктор, создает новый объект.
     *
     * @param Id    Идентификатор типа ключа.
     * @param Title Наименование типа ключа.
     */
    KeyType (
            @NonNull final Long Id,
            @NonNull final String Title
    ) {
        super(Id);
        this.Title = Title;
    }

    /**
     * Метод получения значения поля {@link KeyType#Title}
     *
     * @return Возвращает наименование типа ключа
     */
    @NonNull
    public final String getTitle () {
        return this.Title;
    }

    /**
     * Метод представляет Тип Ключа в виде строки, содержащей поля, которые входят в таблицу базы данных
     *
     * @return Возвращает строковое представление Типа Ключа
     */
    @NonNull
    @Override
    public final String toString () {
        //установить формат строки
        final String format = "@[Title=%s]";
        //установить массив значений
        final Object[] args = {this.Title};
        //вернуть конкатенацию строки суперкласса и сформированной строки текущего класса
        return super.toString() +
                String.format(
                        Locale.ENGLISH,//устанавливаем Locale.ENGLISH, чтобы разделителем в числах с плавающей точкой была точка, а не запятая (2.343 а не 2,343)
                        format,
                        args);
    }
}
