package com.vsdrozd.blagodarie.db.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Query;

import com.vsdrozd.blagodarie.db.scheme.LikeKeyz;

import java.util.List;

/**
 * Класс определяющий DAO для сущности {@link LikeKeyz}.
 *
 * @author sergeGabrus
 */
@Dao
public abstract class LikeKeyzDao
        extends BaseDao<LikeKeyz> {

    @Query ("SELECT * " +
            "FROM tbl_like_keyz " +
            "WHERE id = :id")
    public abstract LikeKeyz get (final long id);

    @Query ("SELECT * " +
            "FROM tbl_like_keyz " +
            "WHERE like_id = :likeId")
    public abstract List<LikeKeyz> getByLikeId (final long likeId);

    @Query ("SELECT * " +
            "FROM tbl_like_keyz " +
            "WHERE like_id = :likeId " +
            "AND keyz_id = :keyzId")
    public abstract LikeKeyz getByLikeIdAndKeyzId (final long likeId, final long keyzId);

    @Query ("SELECT * " +
            "FROM tbl_like_keyz")
    public abstract List<LikeKeyz> getAll ();

    @Query ("UPDATE tbl_like_keyz " +
            "SET vague = 1, " +
            "need_sync = 1 " +
            "WHERE EXISTS (SELECT * " +
            "              FROM tbl_keyz " +
            "              WHERE tbl_keyz.id = tbl_like_keyz.keyz_id" +
            "              AND vague = 1)")
    public abstract void markVagueForVagueKeyz ();

    @Query ("SELECT lk.* " +
            "FROM tbl_like_keyz lk " +
            "LEFT JOIN tbl_like l ON l.id = lk.like_id " +
            "LEFT JOIN tbl_user u ON u.id = l.owner_id " +
            "LEFT JOIN tbl_keyz k ON k.id = lk.keyz_id " +
            "WHERE u.id = :userId " +
            "AND u.server_id IS NOT NULL " +
            "AND k.server_id IS NOT NULL " +
            "AND l.server_id IS NULL " +
            "AND lk.keyz_id IS NOT NULL " +
            "AND lk.server_id IS NULL")
    public abstract List<LikeKeyz> getForAddLike (final long userId);


    @Query ("SELECT lk.* " +
            "FROM tbl_like_keyz lk " +
            "LEFT JOIN tbl_like l ON l.id = lk.like_id " +
            "LEFT JOIN tbl_user u ON u.id = l.owner_id " +
            "LEFT JOIN tbl_keyz k ON k.id = lk.keyz_id " +
            "WHERE u.id = :userId " +
            "AND u.server_id IS NOT NULL " +
            "AND k.server_id IS NOT NULL " +
            "AND l.server_id IS NOT NULL " +
            "AND lk.keyz_id IS NOT NULL " +
            "AND (lk.server_id IS NULL " +//несинхронизирован
            "  OR (lk.need_sync = 1 AND lk.vague = 0))")//либо стал определенным
    public abstract List<LikeKeyz> getForGetOrCreate (long userId);


    @Query ("SELECT COUNT(*) " +
            "FROM tbl_like_keyz lk " +
            "LEFT JOIN tbl_like l ON l.id = lk.like_id " +
            "LEFT JOIN tbl_user u ON u.id = l.owner_id " +
            "LEFT JOIN tbl_keyz k ON k.id = lk.keyz_id " +
            "WHERE u.id = :userId " +
            "AND u.server_id IS NOT NULL " +
            "AND k.server_id IS NOT NULL " +
            "AND l.server_id IS NOT NULL " +
            "AND lk.keyz_id IS NOT NULL " +
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
    @Query ("UPDATE tbl_like_keyz " +
            "SET deleted = 1 " +
            "WHERE EXISTS (SELECT * " +
            "              FROM tbl_like " +
            "              WHERE tbl_like.id = tbl_like_keyz.like_id" +
            "              AND contact_id IN (:contactIds)) ")
    abstract void __markDeleted (final List<Long> contactIds);

    /**
     * Помечает для удаления связи благодарность-ключ, для благодарностей, принадлежащим заданным
     * контактам.
     * Примечание: безопасная функция, оберточная функция для {@link this#__markDeleted(List)}.
     *
     * @param contactIds Список идентификаторов контактов.
     */
    public void markDeleted (final List<Long> contactIds) {
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
    @Query ("UPDATE tbl_like_keyz " +
            "SET deleted = 1 " +
            "WHERE id IN (SELECT lk.id " +
            "             FROM tbl_like_keyz lk " +
            "             LEFT JOIN tbl_like l ON l.id = lk.like_id " +
            "             LEFT JOIN tbl_contact c ON c.id = l.contact_id " +
            "             WHERE c.id IN (:contactIds) " +
            "             AND NOT EXISTS (SELECT * " +
            "                             FROM tbl_contact_keyz ck " +
            "                             WHERE ck.contact_id = c.id" +
            "                             AND ck.keyz_id = lk.keyz_id))")
    abstract void __markDeleteByContactIds (final List<Long> contactIds);

    /**
     * Помечает для удаления связи благодарность-ключ, для ключей не принадлежащих заданным контактам.
     * Примечание: безопасная функция, оберточная функция для {@link this#__markDeleteByContactIds(List)}.
     *
     * @param contactIds Список идентификаторов контактов.
     */
    public void markDeleteByContactIds (final List<Long> contactIds) {
        splitByLoop(
                contactIds,
                this::__markDeleteByContactIds
        );
    }

    /**
     * Помечает для удаления связи благодарность-ключ, для ключей не принадлежащих заданному контакту.
     *
     * @param contactId Идентификатор контакта.
     */
    @Query ("UPDATE tbl_like_keyz " +
            "SET deleted = 1 " +
            "WHERE id IN (SELECT lk.id " +
            "             FROM tbl_like_keyz lk " +
            "             LEFT JOIN tbl_like l ON l.id = lk.like_id " +
            "             LEFT JOIN tbl_contact c ON c.id = l.contact_id " +
            "             WHERE c.id = :contactId " +
            "             AND NOT EXISTS (SELECT * " +
            "                             FROM tbl_contact_keyz ck " +
            "                             WHERE ck.contact_id = c.id" +
            "                             AND ck.keyz_id = lk.keyz_id))")
    public abstract void markDeleteByContactId (final long contactId);

    @Query ("SELECT lk.* " +
            "FROM tbl_like_keyz lk " +
            "LEFT JOIN tbl_like l ON l.id = lk.like_id " +
            "LEFT JOIN tbl_user u ON u.id = l.owner_id " +
            "WHERE l.owner_id = :ownerId " +
            "AND u.server_id IS NOT NULL " +
            "AND l.server_id IS NOT NULL " +
            "AND lk.server_id IS NOT NULL " +
            "AND ((lk.need_sync = 0 AND lk.deleted = 1) " +//либо удален
            "  OR (lk.need_sync = 1 AND lk.vague = 1))")//либо неопределен
    public abstract List<LikeKeyz> getForDelete (final long ownerId);

    @Query ("SELECT COUNT(*) " +
            "FROM tbl_like_keyz lk " +
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
            "FROM tbl_like_keyz " +
            "WHERE deleted = 0")
    abstract public List<LikeKeyz> getNotDeleted ();
}
