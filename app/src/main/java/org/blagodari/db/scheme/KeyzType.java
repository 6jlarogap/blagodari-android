package org.blagodari.db.scheme;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Index;
import androidx.annotation.NonNull;

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
        tableName = "tbl_keyztype",
        indices = {
                @Index (value = {"title"}, unique = true)
        }
)
public final class KeyzType
        extends BaseEntity {

    /**
     * Перечисление Типов Ключей. Типы Ключей, перечисленные здесь, должны быть автоматически
     * добавлены в таблицу tbl_keyztype, после создания базы данных.
     */
    public enum Types {
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
         * Идентификатор типа.
         */
        private final Long id;
        /**
         * Наименование типа.
         */
        private final String title;

        /**
         * Конструктор, создает новый объект
         *
         * @param id    Идентификатор типа
         * @param title Наименование типа
         */
        Types (final Long id, final String title) {
            this.id = id;
            this.title = title;
        }

        /**
         * Возвращает идентификатор типа.
         *
         * @return Идентификатор типа
         */
        public final Long getId () {
            return id;
        }

        /**
         * Возвращает наименование типа.
         *
         * @return Наименование типа.
         */
        public final String getTitle () {
            return title;
        }

        /**
         * Метод создает сущность {@link KeyzType}
         *
         * @return Возвращает сущность Тип Ключа
         */
        public final KeyzType createKeyzType () {
            return new KeyzType(id, title);
        }
    }

    /**
     * Наименование
     * Не может быть пустым
     * Название столбца таблицы - title
     */
    @NonNull
    @ColumnInfo (name = "title")
    private final String Title;

    /**
     * Конструктор, создает новый объект
     *
     * @param Id    Идентификатор типа ключа
     * @param Title Наименование типа ключа
     */
    public KeyzType (
            @NonNull final Long Id,
            @NonNull final String Title
    ) {
        setId(Id);
        this.Title = Title;
    }

    /**
     * Метод получения значения поля {@link KeyzType#Title}
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
