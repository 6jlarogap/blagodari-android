package com.vsdrozd.blagodarie.db.dao;

import androidx.room.Dao;
import androidx.room.Query;

import com.vsdrozd.blagodarie.db.scheme.ContactKeyz;

import java.util.Collection;
import java.util.List;

/**
 * Класс, определяющий DAO для сущности {@link ContactKeyz}.
 *
 * @author sergeGabrus
 */
@Dao
public abstract class ContactKeyzDao
        extends BaseDao<ContactKeyz> {

    @Query ("SELECT * " +
            "FROM tbl_contact_keyz")
    public abstract List<ContactKeyz> getAll ();

    /**
     * Получает список связей контактов с ключами по заданному идентификатору контакта.
     *
     * @param contactId Идентификатор контакта.
     * @return Список связей контак-ключ.
     */
    @Query ("SELECT * " +
            "FROM tbl_contact_keyz " +
            "WHERE contact_id = :contactId")
    public abstract List<ContactKeyz> getByContactId (final long contactId);

    /**
     * Удаляет связи {@link ContactKeyz} между контактами и ключами, для заданных идентификаторов
     * контактов.
     * Примечание: небезопасная функция, использует запрос типа ...WHERE value IN (:values)... Необходимо
     * использовать оберточную функцию {@link this#deleteByContactIds(List)}.
     *
     * @param contactIds Список идентификаторов контактов.
     */
    @Query ("DELETE FROM tbl_contact_keyz " +
            "WHERE contact_id IN (:contactIds)")
    abstract void __deleteByContactIds (final Collection<Long> contactIds);

    /**
     * Удаляет связи {@link ContactKeyz} между контактами и ключами, для заданных идентификаторов
     * контактов.
     * Примечание: безопасная функция, оберточная функция для {@link this#__deleteByContactIds(Collection)}.
     *
     * @param contactIds Список идентификаторов контактов.
     */
    public void deleteByContactIds (final List<Long> contactIds) {
        splitByLoop(
                contactIds,
                this::__deleteByContactIds
        );
    }

    /**
     * Удаляет связи {@link ContactKeyz} между заданным контактом и его ключами.
     *
     * @param contactId Идентификатор контакта.
     */
    @Query ("DELETE FROM tbl_contact_keyz " +
            "WHERE contact_id IN (:contactId)")
    public abstract void deleteByContactId (final long contactId);
}
