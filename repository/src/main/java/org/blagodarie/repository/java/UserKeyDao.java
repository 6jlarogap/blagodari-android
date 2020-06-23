package org.blagodarie.repository.java;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Query;

import java.util.List;

/**
 * Класс, определяющий DAO для сущности {@link UserKey}.
 *
 * @author sergeGabrus
 */
@Dao
abstract class UserKeyDao
        extends SynchronizableDao<UserKey> {

    @Override
    @Query ("SELECT * " +
            "FROM tbl_user_key " +
            "WHERE id = :id")
    abstract UserKey getById (final long id);

    @Override
    @Query ("SELECT * " +
            "FROM tbl_user_key")
    abstract List<UserKey> getAll ();

    @Override
    @Query ("SELECT id " +
            "FROM tbl_user_key " +
            "WHERE server_id = :serverId")
    public abstract Long getIdByServerId (final long serverId);

    @Override
    @Query ("SELECT server_id " +
            "FROM tbl_user_key " +
            "WHERE id = :id")
    public abstract Long getServerIdById (final long id);





    @Query ("SELECT * " +
            "FROM tbl_user_key " +
            "WHERE user_id = :userId " +
            "AND deleted = 0")
    public abstract List<UserKey> getNotDeletedByUserId (final long userId);

    /**
     * Выполняет запрос в БД и возвращает список связей Пользователь-Ключ для определенного
     * пользователя, которые необходимо отправить на сервер, для получения оттуда серверного
     * идентификатора.
     *
     * @param userId Идентификатор пользователя.
     * @return Список связей Пользователь-Ключ.
     */
    /*Выбираем все связи Пользователь-Ключ для пользователя с идентификатором userId и пустым
    серверным идентификатором. Пользователь должен быть синхронизирован, а ключи нет.*/
    @Query ("SELECT uk.* " +
            "FROM tbl_user_key uk " +
            "LEFT JOIN tbl_user u ON u.id = uk.user_id " +
            "LEFT JOIN tbl_key k ON k.id = uk.key_id " +
            "WHERE u.id = :userId " +
            "AND u.server_id IS NOT NULL " +
            "AND k.server_id IS NULL " +
            "AND uk.server_id IS NULL")
    public abstract List<UserKey> getForGetOrCreate (final long userId);

    /**
     * Выполняет запрос в БД и возвращает список записей, актуальность которых необходимо
     * синхронизировать с сервером.
     *
     * @param userId Идентификатор пользователя.
     * @return Список связей Пользователь-Ключ.
     */
    /*Выбираем связи для заданного пользователя, которые имеют серверный идентификатор и
    установленный флаг необходимости синхронизации.*/
    @Query ("SELECT * " +
            "FROM tbl_user_key " +
            "WHERE user_id = :userId " +
            "AND server_id IS NOT NULL " +
            "AND deleted = 1")
    public abstract List<UserKey> getForDelete (final long userId);

    /**
     * Определяет существуют ли записи, которые необходимо удалить с сервера.
     *
     * @param userId Идентификатор пользователя.
     * @return Объект {@link LiveData} для количества связей.
     */
    @Query ("SELECT COUNT(*) " +
            "FROM tbl_user_key " +
            "WHERE user_id = :userId " +
            "AND server_id IS NOT NULL " +
            "AND deleted = 1")
    public abstract LiveData<Boolean> isExistsForDelete (final long userId);

    /**
     * Помечает для удаления связи, соответствующие заданным пользователю и набору ключей.
     * Примечание: небезопасная функция, использует запрос типа ...WHERE value IN (:values)...
     * Необходимо использовать оберточную функцию {@link this#markForDelete(long, List)}.
     *
     * @param userId  Идентификатор пользователя.
     * @param keyIds Массив идентификаторов ключей.
     */
    @Query ("UPDATE tbl_user_key " +
            "SET deleted = 1 " +
            "WHERE user_id = :userId " +
            "AND key_id IN (:keyIds) ")
    abstract void __markForDelete (
            final long userId,
            final List<Long> keyIds
    );

    /**
     * Помечает для удаления связи, соответствующие заданным пользователю и набору ключей.
     * Примечание: безопасная функция, оберточная функция для {@link this#__markForDelete(long, List)}.
     *
     * @param userId  Идентификатор пользователя.
     * @param keyIds Массив идентификаторов ключей.
     */
    public void markForDelete (
            final long userId,
            final List<Long> keyIds
    ) {
        splitByLoop(
                keyIds,
                (Looper<Long>) ids -> __markForDelete(userId, ids)
        );
    }

    /**
     * Помечает для удаления связи пользователь-ключ, для заданного пользователя и набора контактов.
     * Примечание: небезопасная функция, использует запрос типа ...WHERE value IN (:values)...
     * Необходимо использовать оберточную функцию {@link this#markForDeleteByUserIdAndContactIds(long, List)}.
     *
     * @param userId     Идентификатор пользователя.
     * @param contactIds Список идентификаторов контактов.
     */
    @Query ("UPDATE tbl_user_key " +
            "SET deleted = 1 " +
            "WHERE user_id = :userId " +
            "AND key_id IN (SELECT key_id " +
            "                FROM tbl_contact_key ck " +
            "                LEFT JOIN tbl_user_contact uc ON uc.contact_id = ck.contact_id " +
            "                WHERE ck.contact_id IN (:contactIds))")
    abstract void __markForDeleteByUserIdAndContactIds (
            final long userId,
            final List<Long> contactIds
    );

    /**
     * Помечает для удаления связи пользователь-ключ, для заданного пользователя и набора контактов.
     * Примечание: безопасная функция, оберточная функция для {@link this#__markForDeleteByUserIdAndContactIds(long, List)}.
     *
     * @param userId     Идентификатор пользователя.
     * @param contactIds Список идентификаторов контактов.
     */
    void markForDeleteByUserIdAndContactIds (
            final long userId,
            final List<Long> contactIds
    ) {
        splitByLoop(
                contactIds,
                (Looper<Long>) ids -> __markForDeleteByUserIdAndContactIds(userId, ids)
        );
    }
}
