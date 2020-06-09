package org.blagodari.repository.java;

import androidx.lifecycle.LiveData;
import androidx.paging.DataSource;
import androidx.room.Dao;
import androidx.room.Query;
import androidx.room.Transaction;

import java.util.List;

/**
 * Класс, определяющий DAO для сущности {@link Contact}.
 *
 * @author sergeGabrus
 */
@Dao
abstract class ContactDao
        extends BaseDao<Contact> {

    @Override
    @Query ("SELECT * " +
            "FROM tbl_contact " +
            "WHERE id = :id")
    abstract Contact getById (final long id);

    @Override
    @Query ("SELECT * " +
            "FROM tbl_contact")
    abstract List<Contact> getAll ();

    @Transaction
    @Query ("SELECT * " +
            "FROM tbl_contact " +
            "WHERE id = :id")
    public abstract ContactWithKeys getContactWithKeys (final long id);

    /**
     * Удаляет контакты с заданными идентификаторами.
     * Примечание: небезопасная функция, использует запрос типа ...WHERE value IN (:values)...
     * Необходимо использовать оберточную функцию {@link this#deleteByContactIds(List)}.
     *
     * @param ids Список идентификаторов контактов.
     */
    @Query ("DELETE FROM tbl_contact " +
            "WHERE id IN (:ids)")
    abstract void __deleteByContactIds (final List<Long> ids);

    /**
     * Удаляет контакты с заданными идентификаторами.
     * Примечание: безопасная функция, оберточная функция для {@link this#__deleteByContactIds(List)}.
     *
     * @param ids Список идентификаторов контактов.
     */
    public void deleteByContactIds (final List<Long> ids) {
        splitByLoop(
                ids,
                this::__deleteByContactIds
        );
    }

    @Transaction
    @Query ("SELECT c.* " +
            "FROM tbl_contact c " +
            "LEFT JOIN tbl_user_contact uc ON uc.contact_id = c.id " +
            "WHERE uc.user_id = :userId")
    public abstract List<ContactWithKeys> getContactsWithKeyByUser (final long userId);

    @Query ("SELECT c.* " +
            "FROM tbl_contact c " +
            "LEFT JOIN tbl_user_contact uc ON uc.contact_id = c.id " +
            "WHERE uc.user_id = :userId")
    public abstract List<Contact> getByUser (final long userId);

    @Query ("SELECT DISTINCT c.* " +
            "FROM tbl_contact c " +
            "LEFT JOIN tbl_user_contact uc ON uc.contact_id = c.id " +
            "LEFT JOIN tbl_contact_key ck ON ck.contact_id = c.id " +
            "LEFT JOIN tbl_key k ON k.id = ck.key_id " +
            "WHERE uc.user_id = :userId " +
            "AND (c.title LIKE :filter " +
            "    OR k.value LIKE :filter)" +
            "ORDER BY c.title")
    public abstract DataSource.Factory<Integer, Contact> getByUserOrderByName (
            final long userId,
            final String filter
    );

    @Query ("SELECT c.* " +
            "FROM tbl_contact c " +
            "LEFT JOIN tbl_user_contact uc ON uc.contact_id = c.id " +
            "LEFT JOIN tbl_contact_key ck ON ck.contact_id = c.id " +
            "LEFT JOIN tbl_key k ON k.id = ck.key_id " +
            "WHERE uc.user_id = :userId " +
            "AND (c.title LIKE :filter " +
            "    OR k.value LIKE :filter)" +
            "ORDER BY c.id")
    public abstract DataSource.Factory<Integer, Contact> getByUserOrderByFame (
            final long userId,
            final String filter
    );

    @Query ("SELECT c.* " +
            "FROM tbl_contact c " +
            "LEFT JOIN tbl_user_contact uc ON uc.contact_id = c.id " +
            "LEFT JOIN tbl_contact_key ck ON ck.contact_id = c.id " +
            "LEFT JOIN tbl_key k ON k.id = ck.key_id " +
            "WHERE uc.user_id = :userId " +
            "AND (c.title LIKE :filter " +
            "    OR k.value LIKE :filter)" +
            "ORDER BY c.like_count")
    public abstract DataSource.Factory<Integer, Contact> getByUserOrderByLikeCount (
            final long userId,
            final String filter
    );

    @Query ("SELECT c.* " +
            "FROM tbl_contact c " +
            "LEFT JOIN tbl_user_contact uc ON uc.contact_id = c.id " +
            "LEFT JOIN tbl_contact_key ck ON ck.contact_id = c.id " +
            "LEFT JOIN tbl_key k ON k.id = ck.key_id " +
            "WHERE uc.user_id = :userId " +
            "AND (c.title LIKE :filter " +
            "    OR k.value LIKE :filter)" +
            "ORDER BY c.sum_like_count")
    public abstract DataSource.Factory<Integer, Contact> getByUserOrderBySumLikeCount (
            final long userId,
            final String filter
    );

    @Query ("UPDATE tbl_contact " +
            "SET like_count = like_count + 1 " +
            "WHERE id = :id")
    public abstract void incrementContactLikeCount (final long id);


    @Query ("UPDATE tbl_contact " +
            "SET sum_like_count = sum_like_count + 1 " +
            "WHERE id = :id")
    public abstract void incrementContactSumLikeCount (final long id);

    @Query ("UPDATE tbl_contact " +
            "SET like_count = like_count - 1 " +
            "WHERE id = :id")
    public abstract void decrementContactLikeCount (final long id);


    @Query ("UPDATE tbl_contact " +
            "SET sum_like_count = sum_like_count - 1 " +
            "WHERE id = :id")
    public abstract void decrementContactSumLikeCount (final long id);

    @Query ("UPDATE tbl_contact " +
            "SET like_count = (SELECT COUNT(*) " +
            "                  FROM tbl_like l " +
            "                  WHERE l.contact_id = tbl_contact.id" +
            "                  AND deleted = 0)")
    public abstract void calcLikeCount ();

    @Query ("SELECT * " +
            "FROM tbl_contact " +
            "WHERE id = :id")
    public abstract LiveData<Contact> getLiveData (final long id);
}
