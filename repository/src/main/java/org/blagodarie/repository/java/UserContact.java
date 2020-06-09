package org.blagodarie.repository.java;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;

import java.util.Locale;

/**
 * Класс, определяющий сущность, реализующую связь многие ко многим между Контактами {@link Contact} и Пользователями {@link User}.
 * <p>
 * Название таблицы - tbl_contact_user
 * <p>
 * Индексы:
 * - идентификатор контакта и идентификатор пользователя - уникальны;
 * - идентификатор контакта;
 * - идентификатор пользователя.
 * <p>
 * Внешние ключи:
 * - идентификатор контакта;
 * - идентификатор пользователя.
 *
 * @author sergeGabrus
 */
@Entity (tableName = "tbl_user_contact",
        indices = {
                @Index (value = {"user_id", "contact_id"}, unique = true),
                @Index ("user_id"),
                @Index ("contact_id")
        },
        foreignKeys = {
                @ForeignKey (
                        entity = User.class,
                        parentColumns = "id",
                        childColumns = "user_id"
                ),
                @ForeignKey (
                        entity = Contact.class,
                        parentColumns = "id",
                        childColumns = "contact_id"
                )
        }
)
final class UserContact
        extends BaseEntity {

    /**
     * Идентификатор пользователя.
     * Не может быть пустым.
     * Название столбца таблицы - user_id.
     */
    @NonNull
    @ColumnInfo (name = "user_id")
    private final Long UserId;

    /**
     * Идентификатор контакта.
     * Не может быть пустым.
     * Название столбца таблицы - contact_id
     */
    @NonNull
    @ColumnInfo (name = "contact_id")
    private final Long ContactId;

    /**
     * Конструктор, создает новый объект.
     *
     * @param UserId    Идентификатор пользователя.
     * @param ContactId Идентификатор контакта.
     */
    UserContact (
            @NonNull final Long UserId,
            @NonNull final Long ContactId
    ) {
        super(null);
        this.UserId = UserId;
        this.ContactId = ContactId;
    }

    /**
     * Возвращает идентификатор пользователя.
     *
     * @return Идентификатор пользователя.
     */
    @NonNull
    final Long getUserId () {
        return this.UserId;
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
     * Метод представляет сущность в виде строки, содержащей поля, которые входят в таблицу базы данных
     *
     * @return Возвращает строковое представление сущности
     */
    @NonNull
    @Override
    public String toString () {
        //установить формат строки
        final String format = "@[UserId=%d,ContactId=%d]";
        //установить массив значений
        final Object[] args = {this.UserId, this.ContactId};
        //вернуть конкатенацию строки суперкласса и сформированной строки текущего класса
        return super.toString() +
                String.format(
                        Locale.ENGLISH,//устанавливаем Locale.ENGLISH, чтобы разделителем в числах с плавающей точкой была точка, а не запятая (2.343 а не 2,343)
                        format,
                        args);
    }
}
