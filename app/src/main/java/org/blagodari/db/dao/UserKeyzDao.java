package org.blagodari.db.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Query;

import org.blagodari.db.scheme.UserKeyz;

import java.util.List;

/**
 * Класс, определяющий DAO для сущности {@link UserKeyz}.
 *
 * @author sergeGabrus
 */
@Dao
public abstract class UserKeyzDao
        extends BaseDao<UserKeyz> {

    @Query ("SELECT * " +
            "FROM tbl_user_keyz")
    public abstract List<UserKeyz> getAll ();

    @Query ("SELECT * " +
            "FROM tbl_user_keyz " +
            "WHERE user_id = :userId " +
            "AND deleted = 0")
    public abstract List<UserKeyz> getNotDeletedByUserId (final long userId);

    /**
     * Выполняет запрос в БД и возвращает объект {@link UserKeyz} по его id.
     *
     * @param id Идентификатор.
     * @return Связь Пользователь-Ключ. Если связь с заданным идентификатором не найдена - {@code null}.
     */
    @Query ("SELECT * " +
            "FROM tbl_user_keyz " +
            "WHERE id = :id")
    public abstract UserKeyz get (final long id);

    /**
     * Выполняет запрос в БД и возвращает серверный идентификатор связи по ее id.
     *
     * @param id Идентификатор.
     * @return Серверный идентификатор.
     */
    @Query ("SELECT server_id " +
            "FROM tbl_user_keyz " +
            "WHERE id = :id")
    public abstract Long getServerId (final long id);

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
            "FROM tbl_user_keyz uk " +
            "LEFT JOIN tbl_user u ON u.id = uk.user_id " +
            "LEFT JOIN tbl_keyz k ON k.id = uk.keyz_id " +
            "WHERE u.id = :userId " +
            "AND u.server_id IS NOT NULL " +
            "AND k.server_id IS NULL " +
            "AND uk.server_id IS NULL")
    public abstract List<UserKeyz> getForGetOrCreate (final long userId);

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
            "FROM tbl_user_keyz " +
            "WHERE user_id = :userId " +
            "AND server_id IS NOT NULL " +
            "AND deleted = 1")
    public abstract List<UserKeyz> getForDelete (final long userId);

    /**
     * Определяет существуют ли записи, которые необходимо удалить с сервера.
     *
     * @param userId Идентификатор пользователя.
     * @return Объект {@link LiveData} для количества связей.
     */
    @Query ("SELECT COUNT(*) " +
            "FROM tbl_user_keyz " +
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
     * @param keyzIds Массив идентификаторов ключей.
     */
    @Query ("UPDATE tbl_user_keyz " +
            "SET deleted = 1 " +
            "WHERE user_id = :userId " +
            "AND keyz_id IN (:keyzIds) ")
    abstract void __markForDelete (
            final long userId,
            final List<Long> keyzIds
    );

    /**
     * Помечает для удаления связи, соответствующие заданным пользователю и набору ключей.
     * Примечание: безопасная функция, оберточная функция для {@link this#__markForDelete(long, List)}.
     *
     * @param userId  Идентификатор пользователя.
     * @param keyzIds Массив идентификаторов ключей.
     */
    public void markForDelete (
            final long userId,
            final List<Long> keyzIds
    ) {
        splitByLoop(
                keyzIds,
                ids -> __markForDelete(userId, ids)
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
    @Query ("UPDATE tbl_user_keyz " +
            "SET deleted = 1 " +
            "WHERE user_id = :userId " +
            "AND keyz_id IN (SELECT keyz_id " +
            "                FROM tbl_contact_keyz ck " +
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
    public void markForDeleteByUserIdAndContactIds (
            final long userId,
            final List<Long> contactIds
    ) {
        splitByLoop(
                contactIds,
                ids -> __markForDeleteByUserIdAndContactIds(userId, ids)
        );
    }

    /**
     * Помечает для удаления связи пользователь-ключ, для заданного пользователя и контакта.
     *
     * @param userId     Идентификатор пользователя.
     * @param contactId  Идентификатор контакта.
     */
    @Query ("UPDATE tbl_user_keyz " +
            "SET deleted = 1 " +
            "WHERE user_id = :userId " +
            "AND keyz_id IN (SELECT keyz_id " +
            "                FROM tbl_contact_keyz ck " +
            "                LEFT JOIN tbl_user_contact uc ON uc.contact_id = ck.contact_id " +
            "                WHERE ck.contact_id = :contactId)")
    public abstract void markForDeleteByUserIdAndContactId (
            final long userId,
            final long contactId
    );

}
