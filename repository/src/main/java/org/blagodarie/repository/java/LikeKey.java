package org.blagodarie.repository.java;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;

import org.jetbrains.annotations.NotNull;

import java.util.Locale;

/**
 * Класс, определяющий сущность, реализующей взаимосвязь многие ко многим между Благодарностями {@link Like} и Ключами {@link Key}.
 * <p>
 * Название таблицы - tbl_like_key.
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
        tableName = "tbl_like_key",
        inheritSuperIndices = true,
        indices = {
                @Index (value = {"like_id", "key_id"}, unique = true),
                @Index (value = {"like_id"}),
                @Index (value = {"key_id"})
        },
        foreignKeys = {
                @ForeignKey (
                        entity = Like.class,
                        parentColumns = "id",
                        childColumns = "like_id"
                ),
                @ForeignKey (
                        entity = Key.class,
                        parentColumns = "id",
                        childColumns = "key_id"
                )
        }
)
public final class LikeKey
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
     * Название столбца таблицы - key_id.
     */
    @NonNull
    @ColumnInfo (name = "key_id")
    private final Long KeyId;

    /**
     * Флаг определенности связи. Связь считается неопределенной, если ключ, к которому она привязана,
     * принадлежит нескольким контактам.
     * Не может быть пустым, по умолчанию равно false.
     * Название столбца таблицы - vague.
     */
    @NonNull
    @ColumnInfo (name = "vague", defaultValue = "0")
    private Boolean Vague = false;

    /**
     * Флаг необходимости синхронизации данной записи с сервером.
     * Не может быть пустым, по умолчанию равно false.
     * Название столбца таблицы - need_sync.
     */
    @NonNull
    @ColumnInfo (name = "need_sync", defaultValue = "0")
    private Boolean NeedSync = false;

    /**
     * Флаг необходимости удаления записи с сервера.
     * Не может быть пустым, по умолчанию равно false.
     * Название столбца таблицы - deleted.
     */
    @NonNull
    @ColumnInfo (name = "deleted", defaultValue = "0")
    private Boolean Deleted = false;

    /**
     * Конструктор, устанавливающий все поля. (Требует Room)
     *
     * @param Id       Идентификатор.
     * @param ServerId Серверный идентификатор.
     * @param LikeId   Идентификатор благодарности.
     * @param KeyId   Идентификатор ключа.
     * @param Vague    Флаг неопределенности.
     * @param NeedSync Флаг необходимости синхронизации.
     * @param Deleted  Флаг необходимости удаления записи с сервера.
     */
    LikeKey (
            final Long Id,
            final Long ServerId,
            @NonNull final Long LikeId,
            @NonNull final Long KeyId,
            @NonNull final Boolean Vague,
            @NonNull final Boolean NeedSync,
            @NonNull final Boolean Deleted
    ) {
        super(Id, ServerId);
        this.LikeId = LikeId;
        this.KeyId = KeyId;
        this.Vague = Vague;
        this.NeedSync = NeedSync;
        this.Deleted = Deleted;
    }

    /**
     * Конструктор, устанавливающий только необходимые поля.
     *
     * @param likeId Идентификатор благодарности.
     * @param keyId Идентификатор ключа.
     */
    LikeKey (
            @NonNull final Long likeId,
            @NonNull final Long keyId
    ) {
        super(null, null);
        this.LikeId = likeId;
        this.KeyId = keyId;
    }

    /**
     * Возвращает идентификатор благодарности.
     *
     * @return Идентификатор благодарности.
     */
    @NonNull
    final Long getLikeId () {
        return this.LikeId;
    }

    /**
     * Возвращает идентификатор ключа.
     *
     * @return Идентификатор ключа.
     */
    @NotNull
    final Long getKeyId () {
        return this.KeyId;
    }

    /**
     * Возвращает флаг определенности связи.
     *
     * @return Флаг определенности связи.
     */
    @NonNull
    final Boolean getVague () {
        return this.Vague;
    }

    /**
     * Возвращает флаг необходимости синхронизации с сервером.
     *
     * @return Флаг необходимости синхронизации с сервером.
     */
    @NonNull
    final Boolean getNeedSync () {
        return this.NeedSync;
    }

    /**
     * Устанавливает флаг необходимости синхронизации с сервером.
     *
     * @param needSync Флаг необходимости синхронизации с сервером.
     */
    final void setNeedSync (@NonNull final Boolean needSync) {
        this.NeedSync = needSync;
    }

    /**
     * Возвращает флаг необходимости удаления.
     *
     * @return Флаг необходимости удаления.
     */
    @NonNull
    final Boolean getDeleted () {
        return this.Deleted;
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
        final String format = "@[LikeId=%d,KeyId=%d,Vague=%s,NeedSync=%s,Deleted=%s]";
        //установить массив значений
        final Object[] args = {this.LikeId, this.KeyId, this.Vague, this.NeedSync, this.Deleted};
        //вернуть конкатенацию строки суперкласса и сформированной строки текущего класса
        return super.toString() +
                String.format(
                        Locale.ENGLISH,//устанавливаем Locale.ENGLISH, чтобы разделителем в числах с плавающей точкой была точка, а не запятая (2.343 а не 2,343)
                        format,
                        args);
    }

}
