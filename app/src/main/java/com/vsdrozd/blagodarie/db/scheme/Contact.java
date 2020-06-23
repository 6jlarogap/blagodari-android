package com.vsdrozd.blagodarie.db.scheme;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Index;

import java.util.Locale;

/**
 * Класс, определяющий сущность Контакт. Контакт - объект адресной книги пользователя,
 * имеет название и набор ключей {@link Keyz}, с которыми связывается посредством сущностей {@link ContactKeyz}.
 * Может иметь связь многие ко многим.
 * Пользователь может благодарить свои контакты, при этом создается объект Благодарность {@link Like}
 * привязанный к определенному контакту и посредством сущности {@link LikeKeyz}, привязывается к
 * Ключам Контакта.
 * <p>
 * Название таблицы - tbl_contact.
 * <p>
 * Индексы:
 * - название.
 *
 * @author sergeGabrus
 */
@Entity (
        tableName = "tbl_contact",
        indices = {
                @Index (value = {"title"})
        }
)
public final class Contact
        extends BaseEntity {

    /**
     * Название.
     * Не может быть пустым.
     * Название столбца таблицы - title.
     */
    @NonNull
    @ColumnInfo (name = "title")
    private String Title;

    /**
     * Uri изображения.
     * Название столбца таблицы - photo_uri.
     */
    @ColumnInfo (name = "photo_uri")
    private String PhotoUri;

    /**
     * Известность.
     * Не может быть пустым, по умолчанию равно 1.
     * Название столбца таблицы - fame
     */
    @NonNull
    @ColumnInfo (name = "fame", defaultValue = "1")
    private Long Fame = 1L;

    /**
     * Количество благодарностей пользователя.
     * Не может быть пустым, по умолчанию равно 0.
     * Название столбца таблицы - like_count
     */
    @NonNull
    @ColumnInfo (name = "like_count", defaultValue = "0")
    private Long LikeCount = 0L;

    /**
     * Общее количество благодарностей.
     * Не может быть пустым, по умолчанию равно 0.
     * Название столбца таблицы - sum_like_count
     */
    @NonNull
    @ColumnInfo (name = "sum_like_count", defaultValue = "0")
    private Long SumLikeCount = 0L;

    /**
     * Время последней синхронизации.
     * Не может быть пусты, по умолчанию равно 0.
     * Название столбца таблицы - sync_timestamp.
     */
    @NonNull
    @ColumnInfo (name = "sync_timestamp", defaultValue = "0")
    private Long SyncTimestamp = 0L;

    /**
     * Конструктор, устанавливающий все поля. (Требует Room)
     *
     * @param Id           Идентификатор.
     * @param Title        Название.
     * @param PhotoUri     Uri фото.
     * @param Fame         Известность.
     * @param LikeCount    Количество лайков пользователя.
     * @param SumLikeCount Общее количество лайков.
     */
    public Contact (
            final Long Id,
            @NonNull final String Title,
            final String PhotoUri,
            @NonNull final Long Fame,
            @NonNull final Long LikeCount,
            @NonNull final Long SumLikeCount,
            @NonNull final Long SyncTimestamp
    ) {
        setId(Id);
        this.Title = Title;
        this.PhotoUri = PhotoUri;
        this.Fame = Fame;
        this.LikeCount = LikeCount;
        this.SumLikeCount = SumLikeCount;
        this.SyncTimestamp = SyncTimestamp;
    }

    /**
     * Конструктор, устанавливающий только необходимые поля.
     *
     * @param title Название.
     */
    public Contact (
            @NonNull final String title
    ) {
        this.Title = title;
    }

    /**
     * Возвращает название контакта.
     *
     * @return Название контакта.
     */
    @NonNull
    public final String getTitle () {
        return this.Title;
    }

    /**
     * Задает название контакта.
     *
     * @param title Название контакта.
     */
    public final void setTitle (@NonNull final String title) {
        this.Title = title;
    }

    /**
     * Возвращаеть путь к изображению.
     *
     * @return Путь к изображению.
     */
    public final String getPhotoUri () {
        return this.PhotoUri;
    }

    /**
     * Задает путь к изображению.
     *
     * @param photoUri Пусть к изображению.
     */
    public void setPhotoUri (final String photoUri) {
        this.PhotoUri = photoUri;
    }

    /**
     * Возвращает известность.
     *
     * @return Известность.
     */
    @NonNull
    public final Long getFame () {
        return this.Fame;
    }

    /**
     * Задает известность.
     *
     * @param fame Известность.
     */
    public final void setFame (@NonNull final Long fame) {
        Fame = fame;
    }

    /**
     * Возвращает количество благодарностей.
     *
     * @return Количество благодарностей.
     */
    @NonNull
    public final Long getLikeCount () {
        return this.LikeCount;
    }

    /**
     * Устанавливает количество благодарностей.
     *
     * @param likeCount Количество благодарностей.
     */
    public final void setLikeCount (@NonNull final Long likeCount) {
        this.LikeCount = likeCount;
    }

    /**
     * Возвращает общее количество благодарностей.
     *
     * @return Общее количество благодарностей.
     */
    @NonNull
    public final Long getSumLikeCount () {
        return this.SumLikeCount;
    }

    /**
     * Устанавливает общее количество благодарностей.
     *
     * @param sumLikeCount Общее количество благодарностей.
     */
    public final void setSumLikeCount (@NonNull final Long sumLikeCount) {
        this.SumLikeCount = sumLikeCount;
    }

    /**
     * Возвращает время последней синхронизации.
     *
     * @return Время последней синхронизации.
     */
    @NonNull
    public final Long getSyncTimestamp () {
        return this.SyncTimestamp;
    }

    /**
     * Устанавливает время последней синхронизации.
     *
     * @param syncTimestamp Время последней синхронизации.
     */
    public final void setSyncTimestamp (@NonNull final Long syncTimestamp) {
        this.SyncTimestamp = syncTimestamp;
    }

    /**
     * Функция сравнения текущего контакта с переданным. Два ключа считаются равными, если равны их
     * идентификаторы, названия и фотографии.
     *
     * @param obj Объект, с которым требуется сравнить.
     * @return {@code true} если контакты равны, иначе {@code false}.
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
        //преобразовать переданный объект в тип Contact
        final Contact c = (Contact) obj;
        //если идентификаторы, имя и фото текущего и переданного контактов равны, то они равны, иначе - не равны
        return (getId() == null ? c.getId() == null : getId().equals(c.getId())) &&
                this.Title.equals(c.Title) &&
                (this.PhotoUri == null ? c.PhotoUri == null : this.PhotoUri.equals(c.PhotoUri));
    }

    /**
     * Возвращает хэш-код объекта.
     *
     * @return Хэш-код объекта.
     */
    @Override
    public int hashCode () {
        int result = 13;
        result = (47 * result) + (getId() == null ? 0 : getId().hashCode());
        result = (47 * result) + this.Title.hashCode();
        result = (47 * result) + (this.PhotoUri == null ? 0 : this.PhotoUri.hashCode());
        return result;
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
        final String format = "@[Title=%s,PhotoUri=%s,Fame=%d,LikeCount=%d,SumLikeCount=%d]";
        //установить массив значений
        final Object[] args = {this.Title, this.PhotoUri, this.Fame, this.LikeCount, this.SumLikeCount};
        //вернуть конкатенацию строки суперкласса и сформированной строки текущего класса
        return super.toString() +
                String.format(
                        Locale.ENGLISH,//устанавливаем Locale.ENGLISH, чтобы разделителем в числах с плавающей точкой была точка, а не запятая (2.343 а не 2,343)
                        format,
                        args);
    }
}
