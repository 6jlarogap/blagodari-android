package com.vsdrozd.blagodarie.db.scheme;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Index;

import java.util.Locale;

/**
 * Абстрактный класс для сущностей, синхронизируемых с сервером.
 * Содержит поля и методы, общие для сущностей базы данных, которые необходимо синхронизировать с сервером.
 * <p>
 * Индексы:
 * - серверный идентификатор - уникальный;
 *
 * @author sergeGabrus
 */
@Entity (
        indices = {
                @Index (value = {"server_id"}, unique = true)
        }
)
public abstract class SynchronizableEntity
        extends BaseEntity {

    /**
     * Серверный идетификатор
     * Название столбца таблицы - server_id
     */
    @ColumnInfo (name = "server_id")
    private Long ServerId;

    /**
     * Возвращает серверный идентификатор сущности
     *
     * @return Серверный идентификатор
     */
    public final Long getServerId () {
        return this.ServerId;
    }

    /**
     * Устанавливает серверный идентификатор сущности
     *
     * @param serverId Серверный идентификатор
     */
    public final void setServerId (final Long serverId) {
        this.ServerId = serverId;
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
        final String format = "@[ServerId=%d]";
        //установить массив значений
        final Object[] args = {this.ServerId};
        //вернуть конкатенацию строки суперкласса и сформированной строки текущего класса
        return super.toString() +
                String.format(
                        Locale.ENGLISH,//устанавливаем Locale.ENGLISH, чтобы разделителем в числах с плавающей точкой была точка, а не запятая (2.343 а не 2,343)
                        format,
                        args);
    }
}