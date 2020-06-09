package org.blagodarie.repository.java;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Query;

import java.util.List;

/**
 * Класс определяющий DAO для сущности {@link LikeKey}.
 *
 * @author sergeGabrus
 */
@Dao
abstract class LikeKeyDao
        extends SynchronizableDao<LikeKey> {

    @Override
    @Query ("SELECT * " +
            "FROM tbl_like_key " +
            "WHERE id = :id")
    abstract LikeKey getById (final long id);

    @Override
    @Query ("SELECT * " +
            "FROM tbl_like_key")
    abstract List<LikeKey> getAll ();

    @Override
    @Query ("SELECT id " +
            "FROM tbl_like_key " +
            "WHERE server_id = :serverId")
    public abstract Long getIdByServerId (final long serverId);

    @Override
    @Query ("SELECT server_id " +
            "FROM tbl_like_key " +
            "WHERE id = :id")
    public abstract Long getServerIdById (final long id);

    @Query ("SELECT * " +
            "FROM tbl_like_key " +
            "WHERE like_id = :likeId " +
            "AND key_id = :keyId")
    public abstract LikeKey getByLikeIdAndKeyId (final long likeId, final long keyId);


    @Query ("UPDATE tbl_like_key " +
            "SET vague = 1, " +
            "need_sync = 1 " +
            "WHERE EXISTS (SELECT * " +
            "              FROM tbl_key " +
            "              WHERE tbl_key.id = tbl_like_key.key_id" +
            "              AND vague = 1)")
    public abstract void markVagueForVagueKey ();

    @Query ("SELECT lk.* " +
            "FROM tbl_like_key lk " +
            "LEFT JOIN tbl_like l ON l.id = lk.like_id " +
            "LEFT JOIN tbl_user u ON u.id = l.owner_id " +
            "LEFT JOIN tbl_key k ON k.id = lk.key_id " +
            "WHERE u.id = :userId " +
            "AND u.server_id IS NOT NULL " +
            "AND k.server_id IS NOT NULL " +
            "AND l.server_id IS NULL " +
            "AND lk.key_id IS NOT NULL " +
            "AND lk.server_id IS NULL")
    public abstract List<LikeKey> getForAddLike (final long userId);


    @Query ("SELECT lk.* " +
            "FROM tbl_like_key lk " +
            "LEFT JOIN tbl_like l ON l.id = lk.like_id " +
            "LEFT JOIN tbl_user u ON u.id = l.owner_id " +
            "LEFT JOIN tbl_key k ON k.id = lk.key_id " +
            "WHERE u.id = :userId " +
            "AND u.server_id IS NOT NULL " +
            "AND k.server_id IS NOT NULL " +
            "AND l.server_id IS NOT NULL " +
            "AND lk.key_id IS NOT NULL " +
            "AND (lk.server_id IS NULL " +//несинхронизирован
            "  OR (lk.need_sync = 1 AND lk.vague = 0))")//либо стал определенным
    public abstract List<LikeKey> getForGetOrCreate (long userId);


    @Query ("SELECT COUNT(*) " +
            "FROM tbl_like_key lk " +
            "LEFT JOIN tbl_like l ON l.id = lk.like_id " +
            "LEFT JOIN tbl_user u ON u.id = l.owner_id " +
            "LEFT JOIN tbl_key k ON k.id = lk.key_id " +
            "WHERE u.id = :userId " +
            "AND u.server_id IS NOT NULL " +
            "AND k.server_id IS NOT NULL " +
            "AND l.server_id IS NOT NULL " +
            "AND lk.key_id IS NOT NULL " +
            "AND (lk.server_id IS NULL " +//несинхронизирован
            "  OR (lk.need_sync = 1 AND lk.vague = 0))")//либо стал определенным
    public abstract LiveData<Boolean> isExistsForGetOrCreate (long userId);

    /**
     * Помечает для удаления связи благодарность-ключ, для благодарностей, принадлежащим заданным
     * контактам.
     * Примечание: небезопасная функция, использует запрос типа ...WHERE value IN (:values)...
     * Необходимо использовать оберточную функцию {@link this#markDeleted(List)}.
     *
     * @param contactIds Список идентификаторов контактов.
     */
    @Query ("UPDATE tbl_like_key " +
            "SET deleted = 1 " +
            "WHERE EXISTS (SELECT * " +
            "              FROM tbl_like " +
            "              WHERE tbl_like.id = tbl_like_key.like_id" +
            "              AND contact_id IN (:contactIds)) ")
    abstract void __markDeleted (final List<Long> contactIds);

    /**
     * Помечает для удаления связи благодарность-ключ, для благодарностей, принадлежащим заданным
     * контактам.
     * Примечание: безопасная функция, оберточная функция для {@link this#__markDeleted(List)}.
     *
     * @param contactIds Список идентификаторов контактов.
     */
    void markDeleted (final List<Long> contactIds) {
        splitByLoop(
                contactIds,
                this::__markDeleted
        );
    }

    /**
     * Помечает для удаления связи благодарность-ключ, для ключей не принадлежащих заданным контактам.
     * Примечание: небезопасная функция, использует запрос типа ...WHERE value IN (:values)...
     * Необходимо использовать оберточную функцию {@link this#markDeleteByContactIds(List)}.
     *
     * @param contactIds Список идентификаторов контактов.
     */
    @Query ("UPDATE tbl_like_key " +
            "SET deleted = 1 " +
            "WHERE id IN (SELECT lk.id " +
            "             FROM tbl_like_key lk " +
            "             LEFT JOIN tbl_like l ON l.id = lk.like_id " +
            "             LEFT JOIN tbl_contact c ON c.id = l.contact_id " +
            "             WHERE c.id IN (:contactIds) " +
            "             AND NOT EXISTS (SELECT * " +
            "                             FROM tbl_contact_key ck " +
            "                             WHERE ck.contact_id = c.id" +
            "                             AND ck.key_id = lk.key_id))")
    abstract void __markDeleteByContactIds (final List<Long> contactIds);

    /**
     * Помечает для удаления связи благодарность-ключ, для ключей не принадлежащих заданным контактам.
     * Примечание: безопасная функция, оберточная функция для {@link this#__markDeleteByContactIds(List)}.
     *
     * @param contactIds Список идентификаторов контактов.
     */
    void markDeleteByContactIds (final List<Long> contactIds) {
        splitByLoop(
                contactIds,
                this::__markDeleteByContactIds
        );
    }

    @Query ("SELECT lk.* " +
            "FROM tbl_like_key lk " +
            "LEFT JOIN tbl_like l ON l.id = lk.like_id " +
            "LEFT JOIN tbl_user u ON u.id = l.owner_id " +
            "WHERE l.owner_id = :ownerId " +
            "AND u.server_id IS NOT NULL " +
            "AND l.server_id IS NOT NULL " +
            "AND lk.server_id IS NOT NULL " +
            "AND ((lk.need_sync = 0 AND lk.deleted = 1) " +//либо удален
            "  OR (lk.need_sync = 1 AND lk.vague = 1))")//либо неопределен
    public abstract List<LikeKey> getForDelete (final long ownerId);

    @Query ("SELECT COUNT(*) " +
            "FROM tbl_like_key lk " +
            "LEFT JOIN tbl_like l ON l.id = lk.like_id " +
            "LEFT JOIN tbl_user u ON u.id = l.owner_id " +
            "WHERE l.owner_id = :ownerId " +
            "AND u.server_id IS NOT NULL " +
            "AND l.server_id IS NOT NULL " +
            "AND lk.server_id IS NOT NULL " +
            "AND ((lk.need_sync = 0 AND lk.deleted = 1) " +//либо удален
            "  OR (lk.need_sync = 1 AND lk.vague = 1))")//либо неопределен
    public abstract LiveData<Boolean> isExistsForDelete (final long ownerId);

    @Query ("SELECT * " +
            "FROM tbl_like_key " +
            "WHERE deleted = 0")
    abstract public List<LikeKey> getNotDeleted ();
}
