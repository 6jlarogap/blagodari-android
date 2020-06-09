package org.blagodari.db.scheme;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.TypeConverters;

import org.blagodari.db.converter.BooleanConverter;

import java.util.Locale;

/**
 * Класс, определяющий сущность, реализующую взаимосвязь многие ко многим между Пользователями {@link User} и Ключами {@link Keyz}.
 * <p>
 * Название таблицы - tbl_user_keyz.
 * <p>
 * Индексы:
 * - наследует родительские {@link SynchronizableEntity}
 * - идентификатор пользователя и идентификатор ключа - уникальны;
 * - идентификатор пользователя;
 * - идентификатор ключа.
 * <p>
 * Внешние ключи:
 * - идентификатор пользователя {@link User};
 * - идентификатор ключа {@link Keyz}.
 *
 * @author sergeGabrus
 */
@Entity (tableName = "tbl_user_keyz",
        inheritSuperIndices = true,
        indices = {
                @Index (value = {"user_id", "keyz_id"}, unique = true),
                @Index (value = {"user_id"}),
                @Index (value = {"keyz_id"})
        },
        foreignKeys = {
                @ForeignKey (
                        entity = User.class,
                        parentColumns = "id",
                        childColumns = "user_id"
                ),
                @ForeignKey (
                        entity = Keyz.class,
                        parentColumns = "id",
                        childColumns = "keyz_id"
                )
        }
)
public final class UserKeyz
        extends SynchronizableEntity {

    /**
     * Идентификатор пользователя.
     * Не может быть пустым.
     * Название столбца таблицы - user_id.
     */
    @NonNull
    @ColumnInfo (name = "user_id")
    private final Long UserId;

    /**
     * Идентификатор ключа.
     * Не может быть пустым.
     * Название столбца таблицы - keyz_id.
     */
    @NonNull
    @ColumnInfo (name = "keyz_id")
    private final Long KeyzId;

    /**
     * Флаг необходимости удаления записи с сервера.
     * Не может быть пустым, по умолчанию равно false.
     * Название столбца таблицы - deleted.
     */
    @NonNull
    @ColumnInfo (name = "deleted", defaultValue = "0")
    @TypeConverters ({BooleanConverter.class})
    private Boolean Deleted = false;

    /**
     * Конструктор, устанавливающий все поля. (Требует Room)
     *
     * @param Id       Идентификатор.
     * @param ServerId Серверный идентификатор.
     * @param UserId   Идентификатор пользователя.
     * @param KeyzId   Идентификатор ключа.
     * @param Deleted  Флаг необходимости удаления записи с сервера.
     */
    public UserKeyz (
            final Long Id,
            final Long ServerId,
            @NonNull final Long UserId,
            @NonNull final Long KeyzId,
            @NonNull final Boolean Deleted
    ) {
        setId(Id);
        setServerId(ServerId);
        this.UserId = UserId;
        this.KeyzId = KeyzId;
        this.Deleted = Deleted;
    }

    /**
     * Конструктор, устанавливающий только необходимые поля.
     *
     * @param userId Идентификатор пользователя.
     * @param keyzId Идентификатор ключа.
     */
    public UserKeyz (
            @NonNull final Long userId,
            @NonNull final Long keyzId
    ) {
        this.UserId = userId;
        this.KeyzId = keyzId;
    }

    /**
     * Возвращает идентификатор пользователя.
     *
     * @return Идентификатор пользователя.
     */
    @NonNull
    public final Long getUserId () {
        return this.UserId;
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
     * Возвращает флаг необходимости удаления.
     *
     * @return Флаг необходимости удаления.
     */
    @NonNull
    public final Boolean getDeleted () {
        return this.Deleted;
    }

    /**
     * Функция сравнения текущей связи с переданной. Две связи считаются равными, если равны их
     * идентификатор пользователя и идентификаторы ключа.
     *
     * @param obj Объект, с которым требуется сравнить
     * @return {@code true} если ключи равны, иначе {@code false}
     */
    @Override
    public final boolean equals (@Nullable Object obj) {
        //если текущий и переданный объект ссылаются на одно и то же
        if (this == obj) {
            //вернуть true
            return true;
        }
        //если переданным объект пуст или типы текущего и переданного объектов не совпадают
        if ((obj == null) ||
                (getClass() != obj.getClass())) {
            //вернуть false
            return false;
        }
        //преобразовать переданный объект в тип UserKeyz
        UserKeyz uk = (UserKeyz) obj;
        //если идентификатор пользователя и идентификатор ключа текущей и переданной связи равны, то они равны, иначе - не равны
        return this.UserId.equals(uk.UserId) &&
                this.KeyzId.equals(uk.KeyzId);
    }

    /**
     * Метод представляет сущность в виде строки, содержащей поля, которые входят в таблицу базы данных
     *
     * @return Возвращает строковое представление сущности
     */
    @Override
    @NonNull
    public final String toString () {
        //установить формат строки
        final String format = "@[UserId=%d,KeyzId=%d,Deleted=%s]";
        //установить массив значений
        final Object[] args = {this.UserId, this.KeyzId, this.Deleted};
        //вернуть конкатенацию строки суперкласса и сформированной строки текущего класса
        return super.toString() +
                String.format(
                        Locale.ENGLISH,//устанавливаем Locale.ENGLISH, чтобы разделителем в числах с плавающей точкой была точка, а не запятая (2.343 а не 2,343)
                        format,
                        args);
    }

}
