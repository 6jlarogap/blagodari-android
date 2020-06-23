package com.vsdrozd.blagodarie.db.scheme;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.TypeConverters;

import com.vsdrozd.blagodarie.db.converter.BooleanConverter;

import org.jetbrains.annotations.NotNull;

import java.util.Locale;

/**
 * Класс, определяющий сущность, реализующей взаимосвязь многие ко многим между Благодарностями {@link Like} и Ключами {@link Keyz}.
 * <p>
 * Название таблицы - tbl_like_keyz.
 * <p>
 * Индексы:
 * - наследует родительские {@link SynchronizableEntity}
 * - идентификатор благодарности и идентификатор ключа - уникальны;
 * - идентификатор благодарности;
 * - идентификатор ключа;
 * <p>
 * Внешние ключи;
 * - идентификатор благодарности;
 * - идентификатор ключа;
 *
 * @author sergeGabrus
 */
@Entity (
        tableName = "tbl_like_keyz",
        inheritSuperIndices = true,
        indices = {
                @Index (value = {"like_id", "keyz_id"}, unique = true),
                @Index (value = {"like_id"}),
                @Index (value = {"keyz_id"})
        },
        foreignKeys = {
                @ForeignKey (
                        entity = Like.class,
                        parentColumns = "id",
                        childColumns = "like_id"
                ),
                @ForeignKey (
                        entity = Keyz.class,
                        parentColumns = "id",
                        childColumns = "keyz_id"
                )
        }
)
public final class LikeKeyz
        extends SynchronizableEntity {

    /**
     * Идентфикатор благодарности.
     * Не может быть пустым.
     * Название столбца таблицы - like_id
     */
    @NonNull
    @ColumnInfo (name = "like_id")
    private final Long LikeId;

    /**
     * Идентификатор ключа.
     * Название столбца таблицы - keyz_id.
     */
    @NonNull
    @ColumnInfo (name = "keyz_id")
    private final Long KeyzId;

    /**
     * Флаг определенности связи. Связь считается неопределенной, если ключ, к которому она привязана,
     * принадлежит нескольким контактам.
     * Не может быть пустым, по умолчанию равно false.
     * Название столбца таблицы - vague.
     */
    @NonNull
    @ColumnInfo (name = "vague", defaultValue = "0")
    @TypeConverters ({BooleanConverter.class})
    private Boolean Vague = false;

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
     * @param Id       Идентификатор.
     * @param ServerId Серверный идентификатор.
     * @param LikeId   Идентификатор благодарности.
     * @param KeyzId   Идентификатор ключа.
     * @param Vague    Флаг неопределенности.
     * @param NeedSync Флаг необходимости синхронизации.
     * @param Deleted  Флаг необходимости удаления записи с сервера.
     */
    public LikeKeyz (
            final Long Id,
            final Long ServerId,
            @NonNull final Long LikeId,
            @NonNull final Long KeyzId,
            @NonNull final Boolean Vague,
            @NonNull final Boolean NeedSync,
            @NonNull final Boolean Deleted
    ) {
        setId(Id);
        setServerId(ServerId);
        this.LikeId = LikeId;
        this.KeyzId = KeyzId;
        this.Vague = Vague;
        this.NeedSync = NeedSync;
        this.Deleted = Deleted;
    }

    /**
     * Конструктор, устанавливающий только необходимые поля.
     *
     * @param likeId Идентификатор благодарности.
     * @param keyzId Идентификатор ключа.
     */
    public LikeKeyz (
            @NonNull final Long likeId,
            @NonNull final Long keyzId
    ) {
        this.LikeId = likeId;
        this.KeyzId = keyzId;
    }

    /**
     * Возвращает идентификатор благодарности.
     *
     * @return Идентификатор благодарности.
     */
    @NonNull
    public final Long getLikeId () {
        return this.LikeId;
    }

    /**
     * Возвращает идентификатор ключа.
     *
     * @return Идентификатор ключа.
     */
    @NotNull
    public final Long getKeyzId () {
        return this.KeyzId;
    }

    /**
     * Возвращает флаг неопределенности.
     *
     * @return Флаг неопределенности связи.
     */
    @NonNull
    public final Boolean getVague () {
        return this.Vague;
    }

    /**
     * Устанавливает флаг неопределенности связи.
     *
     * @param vague Флаг неопределенности связи.
     */
    public void setVague (@NonNull final Boolean vague) {
        this.Vague = vague;
    }

    /**
     * Возвращает флаг необходимости синхронизации с сервером.
     *
     * @return Флаг необходимости синхронизации с сервером.
     */
    @NonNull
    public final Boolean getNeedSync () {
        return this.NeedSync;
    }

    /**
     * Устанавливает флаг необходимости синхронизации с сервером.
     *
     * @param needSync Флаг необходимости синхронизации с сервером.
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
        final String format = "@[LikeId=%d,KeyzId=%d,Vague=%s,NeedSync=%s,Deleted=%s]";
        //установить массив значений
        final Object[] args = {this.LikeId, this.KeyzId, this.Vague, this.NeedSync, this.Deleted};
        //вернуть конкатенацию строки суперкласса и сформированной строки текущего класса
        return super.toString() +
                String.format(
                        Locale.ENGLISH,//устанавливаем Locale.ENGLISH, чтобы разделителем в числах с плавающей точкой была точка, а не запятая (2.343 а не 2,343)
                        format,
                        args);
    }

}
