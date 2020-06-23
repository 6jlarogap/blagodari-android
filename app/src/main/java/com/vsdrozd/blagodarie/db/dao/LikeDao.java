package com.vsdrozd.blagodarie.db.dao;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.Transformations;
import androidx.room.Dao;
import androidx.room.Query;
import androidx.room.Transaction;

import com.vsdrozd.blagodarie.db.addent.LikeWithKeyz;
import com.vsdrozd.blagodarie.db.scheme.Like;

import java.util.Collection;
import java.util.List;

/**
 * Класс определяющий DAO для сущности {@link Like}.
 *
 * @author sergeGabrus
 */
@Dao
public abstract class LikeDao
        extends BaseDao<Like> {

    @Query ("SELECT * " +
            "FROM tbl_like")
    public abstract List<Like> getAll ();

    @Query ("SELECT id " +
            "FROM tbl_like " +
            "WHERE server_id = :serverId")
    public abstract Long getIdByServerId (final long serverId);

    @Query ("SELECT server_id " +
            "FROM tbl_like " +
            "WHERE id = :id")
    public abstract Long getServerId (final long id);

    @Query ("SELECT * " +
            "FROM tbl_like " +
            "WHERE contact_id = :contactId")
    public abstract List<Like> getByContactId (final long contactId);

    /**
     * Разрывает связь между благодарностями и заданными контактами.
     * Примечание: небезопасная функция, использует запрос типа ...WHERE value IN (:values)...
     * Необходимо использовать оберточную функцию {@link this#relateOffFromContacts(List)}.
     *
     * @param contactIds Список идентификаторов контактов.
     */
    @Query ("UPDATE tbl_like " +
            "SET contact_id = null " +
            "WHERE contact_id IN (:contactIds)")
    abstract void __relateOffFromContacts (final Collection<Long> contactIds);

    /**
     * Разрывает связь между благодарностями и заданными контактами.
     * Примечание: безопасная функция, оберточная функция для {@link this#__relateOffFromContacts(Collection)}.
     *
     * @param contactIds Список идентификаторов контактов.
     */
    public void relateOffFromContacts (final List<Long> contactIds) {
        splitByLoop(
                contactIds,
                this::__relateOffFromContacts
        );
    }

    /**
     * Помечает для удаления благодарности, привязанные к заданным контактам.
     * Примечание: небезопасная функция, использует запрос типа ...WHERE value IN (:values)...
     * Необходимо использовать оберточную функцию {@link this#markForDeletedByContactIds(List)}.
     *
     * @param contactIds Список идентификаторов контактов.
     */
    @Query ("UPDATE tbl_like " +
            "SET deleted = 1 " +
            "WHERE contact_id IN (:contactIds)")
    abstract void __markForDeletedByContactIds (final Collection<Long> contactIds);

    /**
     * Помечает для удаления благодарности, привязанные к заданным контактам.
     * Примечание: безопасная функция, оберточная функция для {@link this#__markForDeletedByContactIds(Collection)}.
     *
     * @param contactIds Список идентификаторов контактов.
     */
    public void markForDeletedByContactIds (final List<Long> contactIds) {
        splitByLoop(
                contactIds,
                this::__markForDeletedByContactIds
        );
    }

    @Transaction
    public void markDeletedAndRelateOffFromContacts (final List<Long> contactsIds) {
        markForDeletedByContactIds(contactsIds);
        relateOffFromContacts(contactsIds);
    }

    @Query ("SELECT l.* " +
            "FROM tbl_like l " +
            "LEFT JOIN tbl_user u ON u.Id = l.owner_id " +
            "WHERE l.owner_id = :ownerId " +
            "AND u.server_id IS NOT NULL " +
            "AND l.server_id IS NULL")
    public abstract List<Like> getForAdd (final long ownerId);

    @Query ("SELECT COUNT(*) " +
            "FROM tbl_like l " +
            "LEFT JOIN tbl_user u ON u.Id = l.owner_id " +
            "WHERE l.owner_id = :ownerId " +
            "AND u.server_id IS NOT NULL " +
            "AND l.server_id IS NULL")
    public abstract LiveData<Boolean> isExistsForAdd (long ownerId);

    @Query ("SELECT l.* " +
            "FROM tbl_like l " +
            "LEFT JOIN tbl_user u ON u.Id = l.owner_id " +
            "WHERE l.owner_id = :ownerId " +
            "AND u.server_id IS NOT NULL " +
            "AND l.server_id IS NOT NULL " +
            "AND l.cancel_timestamp IS NOT NULL " +
            "AND l.need_sync = 1")
    public abstract List<Like> getForCancelLike (long ownerId);

    @Query ("SELECT COUNT(*) " +
            "FROM tbl_like l " +
            "LEFT JOIN tbl_user u ON u.Id = l.owner_id " +
            "WHERE l.owner_id = :ownerId " +
            "AND u.server_id IS NOT NULL " +
            "AND l.server_id IS NOT NULL " +
            "AND l.cancel_timestamp IS NOT NULL " +
            "AND l.need_sync = 1")
    abstract LiveData<Integer> getCountForCancelLike (long ownerId);

    public final LiveData<Boolean> isExistsForCancelLike (final long ownerId) {
        return Transformations.map(
                getCountForCancelLike(ownerId),
                count -> count > 0);
    }

    @Query ("UPDATE tbl_like " +
            "SET contact_id = null " +
            "WHERE EXISTS (SELECT * " +
            "              FROM tbl_like_keyz lk" +
            "              WHERE lk.like_id = tbl_like.id " +
            "              AND lk.vague = 1)")
    public abstract void relateOffFromContactsForVagueKeyz ();

    @Query ("SELECT l.* " +
            "FROM tbl_like l " +
            "LEFT JOIN tbl_user u ON u.id = l.owner_id " +
            "WHERE l.owner_id = :ownerId " +
            "AND u.server_id IS NOT NULL " +
            "AND l.server_id IS NOT NULL " +
            "AND l.need_sync = 0 " +
            "AND l.deleted = 1 " +
            "AND NOT EXISTS (SELECT * FROM tbl_like_keyz WHERE like_id = l.id)")
    public abstract List<Like> getForDelete (final long ownerId);

    @Query ("SELECT COUNT(*) " +
            "FROM tbl_like l " +
            "LEFT JOIN tbl_user u ON u.id = l.owner_id " +
            "WHERE l.owner_id = :ownerId " +
            "AND u.server_id IS NOT NULL " +
            "AND l.server_id IS NOT NULL " +
            "AND l.need_sync = 0 " +
            "AND l.deleted = 1 " +
            "AND NOT EXISTS (SELECT * FROM tbl_like_keyz WHERE like_id = l.id)")
    public abstract LiveData<Boolean> isExistsForDelete (final long ownerId);

    @Query ("SELECT * " +
            "FROM tbl_like " +
            "WHERE owner_id = :ownerId " +
            "AND deleted = 0")
    public abstract List<Like> getNotDeletedByOwnerId (final long ownerId);


    @Query ("SELECT * " +
            "FROM tbl_like " +
            "WHERE contact_id = :contactId " +
            "AND owner_id = :ownerId " +
            "ORDER BY create_timestamp DESC")
    public abstract LiveData<List<Like>> getLiveDataByContactIdAndOwnerId (
            final long contactId,
            final long ownerId
    );

    @Transaction
    @Query ("SELECT * " +
            "FROM tbl_like l " +
            "WHERE contact_id IS NULL " +
            "AND deleted = 0 " +
            "AND (SELECT COUNT(DISTINCT ck.contact_id) " +
            "     FROM tbl_contact_keyz ck " +
            "     LEFT JOIN tbl_like_keyz lk ON lk.keyz_id = ck.keyz_id " +
            "     LEFT JOIN tbl_keyz k ON k.id = lk.keyz_id " +
            "     WHERE lk.like_id = l.id " +
            "     AND lk.deleted = 0 " +
            "     AND k.vague = 0) > 1")
    public abstract LiveData<List<LikeWithKeyz>> getVagueLikeWithKeyz ();

    @Transaction
    @Query ("SELECT * " +
            "FROM tbl_like l " +
            "WHERE contact_id IS NULL " +
            "AND deleted = 0 " +
            "AND (SELECT COUNT(DISTINCT ck.contact_id) " +
            "     FROM tbl_contact_keyz ck " +
            "     LEFT JOIN tbl_like_keyz lk ON lk.keyz_id = ck.keyz_id " +
            "     LEFT JOIN tbl_keyz k ON k.id = lk.keyz_id " +
            "     WHERE lk.like_id = l.id " +
            "     AND lk.deleted = 0 " +
            "     AND k.vague = 0) = 0")
    public abstract LiveData<List<LikeWithKeyz>> getLikeWithMissingKeyz ();

    @Transaction
    @Query ("SELECT * " +
            "FROM tbl_like " +
            "WHERE contact_id IS NULL")
    public abstract List<LikeWithKeyz> getWithEmptyContactId ();
}
