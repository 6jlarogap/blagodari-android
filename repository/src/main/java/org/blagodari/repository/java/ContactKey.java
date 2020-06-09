package org.blagodari.repository.java;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;

import java.util.Locale;

/**
 * Класс, определяющий сущность, реализующую связь многие ко многим между Контактами {@link Contact} и Ключами {@link Key}.
 * <p>
 * Название таблицы - tbl_contact_key
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
@Entity (tableName = "tbl_contact_key",
        indices = {
                @Index (value = {"contact_id", "key_id"}, unique = true),
                @Index ("contact_id"),
                @Index ("key_id")
        },
        foreignKeys = {
                @ForeignKey (
                        entity = Contact.class,
                        parentColumns = "id",
                        childColumns = "contact_id"
                ),
                @ForeignKey (
                        entity = Key.class,
                        parentColumns = "id",
                        childColumns = "key_id"
                )
        }
)
final class ContactKey
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
     * Название столбца таблицы - key_id.
     */
    @NonNull
    @ColumnInfo (name = "key_id")
    private final Long KeyId;

    /**
     * Конструктор, создает новый объект.
     *
     * @param ContactId Идентификатор контакта.
     * @param KeyId    Идентификатор ключа.
     */
    ContactKey (
            @NonNull final Long ContactId,
            @NonNull final Long KeyId
    ) {
        super(null);
        this.ContactId = ContactId;
        this.KeyId = KeyId;
    }

    /**
     * Возвращает идентификатор контакта.
     *
     * @return Идентификатор контакта.
     */
    @NonNull
    final Long getContactId () {
        return this.ContactId;
    }

    /**
     * Возвращает идентификатор ключа.
     *
     * @return Идентификатор ключа.
     */
    @NonNull
    final Long getKeyId () {
        return this.KeyId;
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
        final String format = "@[ContactId=%d,KeyId=%d]";
        //установить массив значений
        final Object[] args = {this.ContactId, this.KeyId};
        //вернуть конкатенацию строки суперкласса и сформированной строки текущего класса
        return super.toString() +
                String.format(
                        Locale.ENGLISH,//устанавливаем Locale.ENGLISH, чтобы разделителем в числах с плавающей точкой была точка, а не запятая (2.343 а не 2,343)
                        format,
                        args);
    }
}
