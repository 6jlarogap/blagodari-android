package com.vsdrozd.blagodarie.db.scheme;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.TypeConverters;

import com.vsdrozd.blagodarie.db.converter.BooleanConverter;

import java.util.Locale;

/**
 * Класс, определяющий сущность Ключ. Ключ - последовательность символов, имеющая определенный
 * тип (телефон, емэйл и т. д.) {@link KeyzType}. Ключ может иметь владельца {@link Keyz#OwnerId},
 * а может и не иметь.
 * <p>
 * Название таблицы - tbl_keyz
 * <p>
 * Индексы:
 * - наследует родительские {@link SynchronizableEntity};
 * - пара полей значение-тип - уникальна;
 * - владелец ключа;
 * - тип ключа;
 * - значение;
 * <p>
 * Внешние ключи:
 * - идентификатор владельца ключа {@link User}
 * - идентификатор типа ключа {@link KeyzType}
 *
 * @author sergeGabrus
 */
@Entity (
        tableName = "tbl_keyz",
        inheritSuperIndices = true,
        indices = {
                @Index (value = {"value", "type_id"}, unique = true),
                @Index (value = {"owner_id"}),
                @Index (value = {"type_id"}),
                @Index (value = {"value"})
        },
        foreignKeys = {
                @ForeignKey (
                        entity = User.class,
                        parentColumns = "id",
                        childColumns = "owner_id"
                ),
                @ForeignKey (
                        entity = KeyzType.class,
                        parentColumns = "id",
                        childColumns = "type_id"
                )
        }
)
public final class Keyz
        extends SynchronizableEntity {

    /**
     * Идентификатор владельца ключа.
     * Название столбца таблицы - owner_id.
     */
    @ColumnInfo (name = "owner_id")
    private Long OwnerId;

    /**
     * Значение ключа.
     * Не может быть пустым.
     * Название столбца таблицы - value.
     */
    @NonNull
    @ColumnInfo (name = "value")
    private final String Value;

    /**
     * Идентификатор типа ключа.
     * Не может быть пустым.
     * Название столбца таблицы - type_id.
     */
    @NonNull
    @ColumnInfo (name = "type_id")
    private final Long TypeId;

    /**
     * Флаг неопределенности ключа. Ключ считается неопределенным если принадлежит нескольким контактам.
     */
    @NonNull
    @ColumnInfo (name = "vague", defaultValue = "0")
    @TypeConverters ({BooleanConverter.class})
    private Boolean Vague = false;

    /**
     * Конструктор, устанавливающий все поля. (Требует Room)
     *
     * @param Id       Идентификатор
     * @param ServerId Серверный индентификатор
     * @param OwnerId  Идентификатор владельца ключа
     * @param Value    Значение ключа
     * @param TypeId   Идентификатор типа ключа
     * @param Vague    Флаг неопределенности ключа
     */
    public Keyz (
            final Long Id,
            final Long ServerId,
            final Long OwnerId,
            @NonNull final String Value,
            @NonNull final Long TypeId,
            @NonNull final Boolean Vague
    ) {
        setId(Id);
        setServerId(ServerId);
        this.OwnerId = OwnerId;
        this.Value = Value;
        this.TypeId = TypeId;
        this.Vague = Vague;
    }

    /**
     * Конструктор,  устанавливающий только необходимые поля.
     *
     * @param value  Значение ключа.
     * @param typeId Идентификатор типа ключа.
     */
    public Keyz (
            @NonNull final String value,
            @NonNull final Long typeId
    ) {
        this.Value = value;
        this.TypeId = typeId;
    }

    /**
     * Возвращает идентификатор владельца ключа.
     *
     * @return Идентификатор владельца ключа.
     */
    public final Long getOwnerId () {
        return this.OwnerId;
    }

    /**
     * Устанавливает идентификатор владельца ключа.
     *
     * @param ownerId Идентификатор владельца ключа.
     */
    public final void setOwnerId (final Long ownerId) {
        this.OwnerId = ownerId;
    }

    /**
     * Возвращает значение ключа.
     *
     * @return Значение ключа.
     */
    @NonNull
    public String getValue () {
        return this.Value;
    }

    /**
     * Возвращает идентификатор типа ключа.
     *
     * @return Идентификатор типа ключа.
     */
    @NonNull
    public Long getTypeId () {
        return this.TypeId;
    }

    /**
     * Возвращает флаг неопределенности ключа.
     *
     * @return Флаг неопределенности ключа.
     */
    @NonNull
    public final Boolean getVague () {
        return this.Vague;
    }

    /**
     * Функция сравнения текущего ключа с переданным. Два ключа считаются равными, если равны их
     * значения и идентификаторы типов.
     *
     * @param obj Объект, с которым требуется сравнить.
     * @return {@code true} если ключи равны, иначе {@code false}.
     */
    @Override
    public boolean equals (@Nullable final Object obj) {
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
        //преобразовать переданный объект в тип Keyz
        Keyz k = (Keyz) obj;
        //если значения и типы текущего и переданного ключей равны, то они равны, иначе - не равны
        return this.Value.equals(k.Value) &&
                this.TypeId.equals(k.TypeId);
    }

    /**
     * Возвращает хэш-код объекта.
     *
     * @return Хэш-код объекта.
     */
    @Override
    public int hashCode () {
        int result = 13;
        result = (47 * result) + this.Value.hashCode();
        result = (47 * result) + this.TypeId.hashCode();
        return result;
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
        final String format = "@[OwnerId=%d,Value=%s,TypeId=%d]";
        //установить массив значений
        final Object[] args = {this.OwnerId, this.Value, this.TypeId};
        //вернуть конкатенацию строки суперкласса и сформированной строки текущего класса
        return super.toString() +
                String.format(
                        Locale.ENGLISH,//устанавливаем Locale.ENGLISH, чтобы разделителем в числах с плавающей точкой была точка, а не запятая (2.343 а не 2,343)
                        format,
                        args);
    }

}
