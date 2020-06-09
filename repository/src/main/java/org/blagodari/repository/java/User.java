package org.blagodari.repository.java;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;

import java.util.Locale;

/**
 * Класс, определяющий сущность Пользователь.
 * <p>
 * Название таблицы - tbl_user
 * <p>
 * Индексы:
 * - наследует родительские {@link SynchronizableEntity};
 *
 * @author sergeGabrus
 */
@Entity (
        tableName = "tbl_user",
        inheritSuperIndices = true
)
public final class User
        extends SynchronizableEntity {

    /**
     * Время последней синхронизации данных с сервером в микросекундах.
     * Не может быть пустым, по умолчанию равно 0.
     * Название столбца таблицы - sync_timestamp.
     */
    @NonNull
    @ColumnInfo (name = "sync_timestamp", defaultValue = "0")
    private Long SyncTimestamp = 0L;

    /**
     * Конструктор, создает новый объект.
     */
    User (@NonNull final Long Id) {
        super(Id, null);
    }

    /**
     * Возвращает время последней синхронизации данных с сервером.
     *
     * @return Время последней синхронизации данных с сервером.
     */
    @NonNull
    final Long getSyncTimestamp () {
        return this.SyncTimestamp;
    }

    /**
     * Устанавливает время последней синхронизации данных с сервером.
     *
     * @param syncTimestamp Время последней синхронизации данных с сервером.
     */
    final void setSyncTimestamp (@NonNull final Long syncTimestamp) {
        this.SyncTimestamp = syncTimestamp;
    }

    /**
     * Метод представляет сущность в виде строки, содержащей поля, которые входят в таблицу базы данных.
     *
     * @return Возвращает строковое представление сущности
     */
    @Override
    @NonNull
    public String toString () {
        //установить формат строки
        final String format = "@[SyncTimestamp=%d]";
        //установить массив значений
        final Object[] args = {this.SyncTimestamp};
        //вернуть конкатенацию строки суперкласса и сформированной строки текущего класса
        return super.toString() +
                String.format(
                        Locale.ENGLISH,//устанавливаем Locale.ENGLISH, чтобы разделителем в числах с плавающей точкой была точка, а не запятая (2.343 а не 2,343)
                        format,
                        args);
    }

}
