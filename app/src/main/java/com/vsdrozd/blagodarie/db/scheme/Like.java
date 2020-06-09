package com.vsdrozd.blagodarie.db.scheme;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.TypeConverters;

import com.vsdrozd.blagodarie.db.converter.BooleanConverter;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Класс, определяющий сущность Благодарность. Пользователь {@link User} может благодарить свои
 * Контакты {@link Contact}, при этом он становится владельцем Благодарности {@link this#OwnerId}. Также
 * пользователь может отменить свою благодарность, при этом у благодарности заполняется время отмены
 * благодарности {@link this#CancelTimestamp}. Пустое значение времени отмены благодарности
 * {@link this#CancelTimestamp} говорит об актуальности Благодарности, в противном случае -
 * что Благодарность отменена.
 * <p>
 * Название таблицы - tbl_like.
 * <p>
 * Индексы:
 * - наследует родительские {@link SynchronizableEntity};
 * - владелец благодарности.
 * <p>
 * Внешние ключи:
 * - идентификатор владельца благодарности {@link User}.
 *
 * @author sergeGabrus
 */
@Entity (
        tableName = "tbl_like",
        inheritSuperIndices = true,
        indices = {
                @Index (value = {"owner_id"})
        },
        foreignKeys = {
                @ForeignKey (
                        entity = User.class,
                        parentColumns = "id",
                        childColumns = "owner_id"
                )
        }
)
public final class Like
        extends SynchronizableEntity {

    /**
     * Идентификатор владельца благодарности.
     * Не может быть пустым.
     * Название столбца таблицы - owner_id.
     */
    @NonNull
    @ColumnInfo (name = "owner_id")
    private final Long OwnerId;

    /**
     * Идентификатор контакта.
     * Название столбца таблицы - contact_id.
     */
    @ColumnInfo (name = "contact_id")
    private Long ContactId;

    /**
     * Время создания благодарности.
     * Не может быть пустым.
     * Название столбца таблицы - create_timestamp.
     */
    @NonNull
    @ColumnInfo (name = "create_timestamp")
    private final Long CreateTimestamp;

    /**
     * Время отмены благодарности.
     * Название столбца таблицы - cancel_timestamp.
     */
    @ColumnInfo (name = "cancel_timestamp")
    private Long CancelTimestamp;

    /**
     * Флаг необходимости синхронизации данной записи с сервером.
     * Не может быть пустым, по умолчанию равно false.
     * Название столбца таблицы - need_sync.
     */
    @NonNull
    @ColumnInfo (name = "need_sync", defaultValue = "0")
    @TypeConverters ({BooleanConverter.class})
    private Boolean NeedSync = false;

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
     * @param Id              Идентификатор
     * @param ServerId        Серверный идентификатор
     * @param OwnerId         Идентификатор владельца
     * @param ContactId       Идентификатор контакта.
     * @param CreateTimestamp Время создания
     * @param CancelTimestamp Время отмены
     * @param NeedSync        Флаг необходимости синхронизации с сервером
     * @param Deleted         Флаг необходимости удаления записи с сервера
     */
    public Like (
            final Long Id,
            final Long ServerId,
            @NonNull final Long OwnerId,
            final Long ContactId,
            @NonNull final Long CreateTimestamp,
            final Long CancelTimestamp,
            @NonNull final Boolean NeedSync,
            @NonNull final Boolean Deleted
    ) {
        setId(Id);
        setServerId(ServerId);
        this.OwnerId = OwnerId;
        this.ContactId = ContactId;
        this.CreateTimestamp = CreateTimestamp;
        this.CancelTimestamp = CancelTimestamp;
        this.NeedSync = NeedSync;
        this.Deleted = Deleted;
    }

    /**
     * Конструктор, устанавливающий только необходимые поля.
     *
     * @param ownerId         Идентификатор владельца благодарности.
     * @param createTimestamp Время создания благодарности.
     */
    public Like (
            @NonNull final Long ownerId,
            @NonNull final Long createTimestamp
    ) {
        this.OwnerId = ownerId;
        this.CreateTimestamp = createTimestamp;
    }

    /**
     * Возвращает идентификатор владельца благодарности.
     *
     * @return Идентификатор владельца благодарности.
     */
    @NonNull
    public final Long getOwnerId () {
        return this.OwnerId;
    }

    /**
     * Возвращает идентификатор контакта.
     *
     * @return Идентификатор контакта.
     */
    public final Long getContactId () {
        return this.ContactId;
    }

    /**
     * Устанавливает идентификатор контакта.
     *
     * @param contactId Идентификатор контакта.
     */
    public final void setContactId (final Long contactId) {
        this.ContactId = contactId;
    }

    /**
     * Возвращает время создания благодарности в микросекундах.
     *
     * @return Время создания благодарности.
     */
    @NonNull
    public final Long getCreateTimestamp () {
        return this.CreateTimestamp;
    }

    /**
     * Возвращает время отмены благодарности в микросекундах.
     *
     * @return Время отмены благодарности.
     */
    public final Long getCancelTimestamp () {
        return this.CancelTimestamp;
    }

    /**
     * Устанавливает время отмены благодарности.
     *
     * @param cancelTimestamp Время отмены благодарности
     */
    public final void setCancelTimestamp (final Long cancelTimestamp) {
        this.CancelTimestamp = cancelTimestamp;
    }

    /**
     * Возвращает флаг необходимости синхронизации с сервером.
     *
     * @return Флаг необходимости синхронизации с сервером
     */
    @NonNull
    public final Boolean getNeedSync () {
        return this.NeedSync;
    }

    /**
     * Устанавливает флаг необходимости синхронизации с сервером.
     *
     * @param needSync Флаг необходимости синхронизации с сервером
     */
    public final void setNeedSync (@NonNull final Boolean needSync) {
        this.NeedSync = needSync;
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
     * Устанавливает флаг необходимости удаления.
     *
     * @param deleted Флаг необходимости удаления.
     */
    public final void setDeleted (@NonNull final Boolean deleted) {
        this.Deleted = deleted;
    }

    /**
     * Метод представляет сущность в виде строки, содержащей поля, которые входят в таблицу базы данных.
     *
     * @return Возвращает строковое представление сущности
     */
    @Override
    @NonNull
    public final String toString () {
        //установить формат строки
        final String format = "@[OwnerId=%d,ContactId=%d,CreateTimestamp=%d,CancelTimestamp=%d,NeedSync=%s,Deleted=%s]";
        //установить массив значений
        final Object[] args = {this.OwnerId, this.ContactId, this.CreateTimestamp, this.CancelTimestamp, this.NeedSync, this.Deleted};
        //вернуть конкатенацию строки суперкласса и сформированной строки текущего класса
        return super.toString() +
                String.format(
                        Locale.ENGLISH,//устанавливаем Locale.ENGLISH, чтобы разделителем в числах с плавающей точкой была точка, а не запятая (2.343 а не 2,343)
                        format,
                        args);
    }

    // FIXME: 08.01.2020 Возможно стоит перенести данное преобразование туда откуда оно вызывается
    public final String getTimestampLikeString () {
        return new SimpleDateFormat("HH:mm:ss\ndd MMMM yyyy", Locale.getDefault()).format(new Date(CreateTimestamp));
    }

    // FIXME: 08.01.2020 Возможно стоит перенести данное преобразование туда откуда оно вызывается
    public final String getCancelTimestampLikeString () {
        return CancelTimestamp == null ? "" : new SimpleDateFormat("HH:mm:ss\ndd MMMM yyyy", Locale.getDefault()).format(new Date(CancelTimestamp));
    }
}
