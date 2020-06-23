package org.blagodarie.repository.java;

import androidx.room.Dao;
import androidx.room.Query;

import java.util.List;

/**
 * Класс, определяющий DAO для сущности {@link ContactKey}.
 *
 * @author sergeGabrus
 */
@Dao
abstract class ContactKeyDao
        extends BaseDao<ContactKey> {

    @Override
    @Query ("SELECT * " +
            "FROM tbl_contact_key " +
            "WHERE id = :id")
    abstract ContactKey getById (final long id);

    @Override
    @Query ("SELECT * " +
            "FROM tbl_contact_key")
    abstract List<ContactKey> getAll ();

    /**
     * Получает список связей контактов с ключами по заданному идентификатору контакта.
     *
     * @param contactId Идентификатор контакта.
     * @return Список связей контак-ключ.
     */
    @Query ("SELECT * " +
            "FROM tbl_contact_key " +
            "WHERE contact_id = :contactId")
    public abstract List<ContactKey> getByContactId (final long contactId);

    /**
     * Удаляет связи {@link ContactKey} между контактами и ключами, для заданных идентификаторов
     * контактов.
     * Примечание: небезопасная функция, использует запрос типа ...WHERE value IN (:values)... Необходимо
     * использовать оберточную функцию {@link this#deleteByContactIds(List)}.
     *
     * @param contactIds Список идентификаторов контактов.
     */
    @Query ("DELETE FROM tbl_contact_key " +
            "WHERE contact_id IN (:contactIds)")
    abstract void __deleteByContactIds (final List<Long> contactIds);

    /**
     * Удаляет связи {@link ContactKey} между контактами и ключами, для заданных идентификаторов
     * контактов.
     * Примечание: безопасная функция, оберточная функция для {@link this#__deleteByContactIds(List)}.
     *
     * @param contactIds Список идентификаторов контактов.
     */
    public void deleteByContactIds (final List<Long> contactIds) {
        splitByLoop(
                contactIds,
                this::__deleteByContactIds
        );
    }
}
