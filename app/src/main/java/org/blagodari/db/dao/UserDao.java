package org.blagodari.db.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Query;

import org.blagodari.db.scheme.User;

/**
 * Класс, определяющий DAO для сущности {@link User}.
 *
 * @author sergeGabrus
 */
@Dao
public abstract class UserDao
        extends BaseDao<User> {

    /**
     * Выполняет запрос в БД и возвращает объект {@link User} по его id.
     *
     * @param id Идентификатор пользователя.
     * @return Пользователь. Если пользователь с заданным идентификатором не найден - {@code null}.
     */
    @Query ("SELECT * " +
            "FROM tbl_user " +
            "WHERE id = :id")
    public abstract User get (final long id);

    @Query ("SELECT COUNT(*) " +
            "FROM tbl_user " +
            "WHERE id = :id")
    public abstract Boolean isExists (final long id);

    /**
     * Выполняет запрос в БД и возвращает серверный идентификатор пользователя по его id.
     *
     * @param id Идентификатор пользователя.
     * @return Серверный идентификатор.
     */
    @Query ("SELECT server_id " +
            "FROM tbl_user " +
            "WHERE id = :id")
    public abstract Long getServerId (final long id);

    /**
     * Определяет, что пользователь авторизован, но его ключ требует синхронизации.
     *
     * @param id Идентификатор пользователя.
     * @return Если пользователь авторизован, но не синхронизирован - возвращает 1, иначе - 0.
     */
    /*Выбираем количество пользователей, с заданным id, для которых существует ключ типа
    GoogleAccountId, но этот ключ не синхронизирован с сервером*/
    @Query ("SELECT COUNT(*) " +
            "FROM tbl_user u " +
            "WHERE u.id = :id " +
            "AND EXISTS (" +
            "   SELECT * " +
            "   FROM tbl_keyz k " +
            "   WHERE k.owner_id = u.id " +
            "   AND k.type_id = 3 " +
            "   AND k.server_id IS NULL" +
            ")")
    public abstract LiveData<Boolean> isAuthorizedNotSynced (final long id);

    /**
     * Определяет синхронизирован ли пользователь.
     *
     * @param id Идентификатор пользователя.
     * @return Если пользователь синхронизирован с сервером - {@code true}, иначе - {@code false}.
     */
    @Query ("SELECT COUNT(*) " +
            "FROM tbl_user " +
            "WHERE id = :id " +
            "AND server_id IS NOT NULL")
    public abstract Boolean isSynced (final long id);
}
