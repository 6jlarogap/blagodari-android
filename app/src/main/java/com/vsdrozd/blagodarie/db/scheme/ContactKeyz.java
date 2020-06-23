package com.vsdrozd.blagodarie.db.scheme;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;

import java.util.Locale;

/**
 * Класс, определяющий сущность, реализующую связь многие ко многим между Контактами {@link Contact} и Ключами {@link Keyz}.
 * <p>
 * Название таблицы - tbl_contact_keyz
 * <p>
 * Индексы:
 * - идентификатор контакта и идентификатор ключа - уникальны;
 * - идентификатор контакта;
 * - идентификатор ключа.
 * <p>
 * Внешние ключи:
 * - идентификатор контакта;
 * - идентификатор ключа.
 *
 * @author sergeGabrus
 */
@Entity (tableName = "tbl_contact_keyz",
        indices = {
                @Index (value = {"contact_id", "keyz_id"}, unique = true),
                @Index ("contact_id"),
                @Index ("keyz_id")
        },
        foreignKeys = {
                @ForeignKey (
                        entity = Contact.class,
                        parentColumns = "id",
                        childColumns = "contact_id"
                ),
                @ForeignKey (
                        entity = Keyz.class,
                        parentColumns = "id",
                        childColumns = "keyz_id"
                )
        }
)
public final class ContactKeyz
        extends BaseEntity {

    /**
     * Идентификатор контакта.
     * Не может быть пустым.
     * Название столбца таблицы - contact_id
     */
    @NonNull
    @ColumnInfo (name = "contact_id")
    private final Long ContactId;

    /**
     * Идентификатор ключа.
     * Не может быть пустым.
     * Название столбца таблицы - keyz_id.
     */
    @NonNull
    @ColumnInfo (name = "keyz_id")
    private final Long KeyzId;

    /**
     * Конструктор, создает новый объект.
     *
     * @param ContactId Идентификатор контакта.
     * @param KeyzId    Идентификатор ключа.
     */
    public ContactKeyz (
            @NonNull final Long ContactId,
            @NonNull final Long KeyzId
    ) {
        this.ContactId = ContactId;
        this.KeyzId = KeyzId;
    }

    /**
     * Возвращает идентификатор контакта.
     *
     * @return Идентификатор контакта.
     */
    @NonNull
    public final Long getContactId () {
        return this.ContactId;
    }

    /**
     * Возвращает идентификатор ключа.
     *
     * @return Идентификатор ключа.
     */
    @NonNull
    public final Long getKeyzId () {
        return this.KeyzId;
    }

    /**
     * Метод представляет сущность в виде строки, содержащей поля, которые входят в таблицу базы данных
     *
     * @return Возвращает строковое представление сущности
     */
    @NonNull
    @Override
    public String toString () {
        //установить формат строки
        final String format = "@[ContactId=%d,KeyzId=%d]";
        //установить массив значений
        final Object[] args = {this.ContactId, this.KeyzId};
        //вернуть конкатенацию строки суперкласса и сформированной строки текущего класса
        return super.toString() +
                String.format(
                        Locale.ENGLISH,//устанавливаем Locale.ENGLISH, чтобы разделителем в числах с плавающей точкой была точка, а не запятая (2.343 а не 2,343)
                        format,
                        args);
    }
}
