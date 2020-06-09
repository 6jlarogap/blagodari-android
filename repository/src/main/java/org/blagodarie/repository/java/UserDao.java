package org.blagodarie.repository.java;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Query;

import java.util.List;


/**
 * Класс, определяющий DAO для сущности {@link User}.
 *
 * @author sergeGabrus
 */
@Dao
abstract class UserDao
        extends SynchronizableDao<User> {

    @Override
    @Query ("SELECT * " +
            "FROM tbl_user " +
            "WHERE id = :id")
    abstract User getById (final long id);

    @Override
    @Query ("SELECT * " +
            "FROM tbl_user")
    abstract List<User> getAll ();

    @Override
    @Query ("SELECT id " +
            "FROM tbl_user " +
            "WHERE server_id = :serverId")
    public abstract Long getIdByServerId (final long serverId);

    @Override
    @Query ("SELECT server_id " +
            "FROM tbl_user " +
            "WHERE id = :id")
    public abstract Long getServerIdById (final long id);






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
            "   FROM tbl_key k " +
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
