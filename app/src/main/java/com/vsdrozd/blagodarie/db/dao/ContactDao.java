package com.vsdrozd.blagodarie.db.dao;

import androidx.lifecycle.LiveData;
import androidx.paging.DataSource;
import androidx.room.Dao;
import androidx.room.Query;
import androidx.room.Transaction;

import com.vsdrozd.blagodarie.db.addent.ContactWithKeyz;
import com.vsdrozd.blagodarie.db.scheme.Contact;

import java.util.Collection;
import java.util.List;

/**
 * Класс, определяющий DAO для сущности {@link Contact}.
 *
 * @author sergeGabrus
 */
@Dao
public abstract class ContactDao
        extends BaseDao<Contact> {

    @Query ("SELECT * " +
            "FROM tbl_contact " +
            "WHERE id = :id")
    public abstract Contact get (final long id);

    @Query ("SELECT * " +
            "FROM tbl_contact")
    public abstract List<Contact> getAll ();

    @Transaction
    @Query ("SELECT * " +
            "FROM tbl_contact " +
            "WHERE id = :id")
    public abstract ContactWithKeyz getContactWithKeyz (final long id);

    @Transaction
    @Query ("SELECT * " +
            "FROM tbl_contact " +
            "WHERE id IN (:ids)")
    public abstract List<ContactWithKeyz> getContactsWithKeyz (final Collection<Long> ids);

    /**
     * Удаляет контакты с заданными идентификаторами.
     * Примечание: небезопасная функция, использует запрос типа ...WHERE value IN (:values)...
     * Необходимо использовать оберточную функцию {@link this#deleteByContactIds(List)}.
     *
     * @param ids Список идентификаторов контактов.
     */
    @Query ("DELETE FROM tbl_contact " +
            "WHERE id IN (:ids)")
    abstract void __deleteByContactIds (final Collection<Long> ids);

    /**
     * Удаляет контакты с заданными идентификаторами.
     * Примечание: безопасная функция, оберточная функция для {@link this#__deleteByContactIds(Collection)}.
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
    public abstract List<ContactWithKeyz> getContactsWithKeyzByUser (final long userId);

    @Query ("SELECT c.* " +
            "FROM tbl_contact c " +
            "LEFT JOIN tbl_user_contact uc ON uc.contact_id = c.id " +
            "WHERE uc.user_id = :userId")
    public abstract List<Contact> getByUser (final long userId);

    @Query ("SELECT c.* " +
            "FROM tbl_contact c " +
            "LEFT JOIN tbl_user_contact uc ON uc.contact_id = c.id " +
            "WHERE uc.user_id = :userId")
    public abstract LiveData<List<Contact>> getByUserId (final long userId);

    @Query ("SELECT DISTINCT c.* " +
            "FROM tbl_contact c " +
            "LEFT JOIN tbl_user_contact uc ON uc.contact_id = c.id " +
            "WHERE uc.user_id = :userId " +
            "AND c.title LIKE :filter " +
            "ORDER BY c.title ASC, c.fame DESC, c.sum_like_count DESC")
    public abstract DataSource.Factory<Integer, Contact> getByUserOrderByName (
            final long userId,
            final String filter
    );

    @Query ("SELECT DISTINCT c.* " +
            "FROM tbl_contact c " +
            "LEFT JOIN tbl_user_contact uc ON uc.contact_id = c.id " +
            "WHERE uc.user_id = :userId " +
            "AND c.title LIKE :filter " +
            "ORDER BY c.fame DESC, c.sum_like_count DESC, c.title ASC")
    public abstract DataSource.Factory<Integer, Contact> getByUserOrderByFame (
            final long userId,
            final String filter
    );

    @Query ("SELECT DISTINCT c.* " +
            "FROM tbl_contact c " +
            "LEFT JOIN tbl_user_contact uc ON uc.contact_id = c.id " +
            "WHERE uc.user_id = :userId " +
            "AND c.title LIKE :filter " +
            "ORDER BY c.like_count DESC, c.fame DESC, c.sum_like_count DESC")
    public abstract DataSource.Factory<Integer, Contact> getByUserOrderByLikeCount (
            final long userId,
            final String filter
    );

    @Query ("SELECT DISTINCT c.* " +
            "FROM tbl_contact c " +
            "LEFT JOIN tbl_user_contact uc ON uc.contact_id = c.id " +
            "WHERE uc.user_id = :userId " +
            "AND c.title LIKE :filter " +
            "ORDER BY c.sum_like_count DESC, c.fame DESC, c.title ASC")
    public abstract DataSource.Factory<Integer, Contact> getByUserOrderBySumLikeCount (
            final long userId,
            final String filter
    );

    @Query ("SELECT DISTINCT c.* " +
            "FROM tbl_contact c " +
            "LEFT JOIN tbl_user_contact uc ON uc.contact_id = c.id " +
            "WHERE uc.user_id = :userId " +
            "AND c.title LIKE :filter " +
            "ORDER BY (SELECT MAX(create_timestamp) " +
            "          FROM tbl_like l " +
            "          WHERE l.contact_id = c.id) DESC, c.fame DESC, c.sum_like_count DESC")
    public abstract DataSource.Factory<Integer, Contact> getByUserOrderByTime (
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
            "                  WHERE l.contact_id = tbl_contact.id " +
            "                  AND cancel_timestamp IS NULL " +
            "                  AND deleted = 0)")
    public abstract void calcLikeCount ();

    @Query ("UPDATE tbl_contact " +
            "SET like_count = (SELECT COUNT(*) " +
            "                  FROM tbl_like l " +
            "                  WHERE l.contact_id = tbl_contact.id" +
            "                  AND cancel_timestamp IS NULL " +
            "                  AND deleted = 0) " +
            "WHERE id = :id")
    public abstract void calcLikeCount (final long id);

    @Query ("SELECT * " +
            "FROM tbl_contact " +
            "WHERE id = :id")
    public abstract LiveData<Contact> getLiveData (final long id);

    @Transaction
    @Query ("SELECT * " +
            "FROM tbl_contact c1 " +
            "WHERE EXISTS (SELECT * " +
            "              FROM tbl_contact c2 " +
            "              WHERE c2.title = c1.title " +
            "              AND c2.id != c1.id)")
    public abstract LiveData<List<ContactWithKeyz>> getNamesakeContactsWithKeyz ();

    @Query ("SELECT COUNT(*) " +
            "FROM tbl_contact c1 " +
            "WHERE id = :id " +
            "AND EXISTS (SELECT * " +
            "            FROM tbl_contact c2 " +
            "            WHERE c2.id != c1.id" +
            "            AND c2.title = c1.title)")
    public abstract Boolean isHaveNamesake (final long id);
}
