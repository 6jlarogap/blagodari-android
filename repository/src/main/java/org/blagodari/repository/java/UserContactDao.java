package org.blagodari.repository.java;

import androidx.room.Dao;
import androidx.room.Query;

import java.util.List;

/**
 * Класс, определяющий DAO для сущности {@link UserContact}.
 *
 * @author sergeGabrus
 */
@Dao
abstract class UserContactDao
        extends BaseDao<UserContact> {

    @Override
    @Query ("SELECT * " +
            "FROM tbl_user_contact " +
            "WHERE id = :id")
    abstract UserContact getById (final long id);

    @Override
    @Query ("SELECT * " +
            "FROM tbl_user_contact")
    abstract List<UserContact> getAll ();

    @Query ("SELECT * " +
            "FROM tbl_user_contact " +
            "WHERE user_id = :userId")
    public abstract List<UserContact> getByUserId (final long userId);

    /**
     * Удаляет связи пользователь-контакт с заданными идентификаторами контактов.
     * Примечание: небезопасная функция, использует запрос типа ...WHERE value IN (:values)...
     * Необходимо использовать оберточную функцию {@link this#deleteByUserIdAndContactIds(long, List)}.
     *
     * @param userId     Идентификатор пользователя
     * @param contactIds Список идентификаторов контактов.
     */
    @Query ("DELETE FROM tbl_user_contact " +
            "WHERE user_id = :userId " +
            "AND contact_id IN (:contactIds)")
    abstract void __deleteByUserIdAndContactIds (
            final long userId,
            final List<Long> contactIds
    );

    /**
     * Удаляет связи пользователь-контакт с заданными идентификаторами контактов.
     * Примечание: безопасная функция, оберточная функция для {@link this#__deleteByUserIdAndContactIds(long, List)}.
     *
     * @param userId     Идентификатор пользователя
     * @param contactIds Список идентификаторов контактов.
     */
    public void deleteByUserIdAndContactIds (
            final long userId,
            final List<Long> contactIds
    ) {
        splitByLoop(
                contactIds,
                (Looper<Long>) ids -> __deleteByUserIdAndContactIds(userId, ids)
        );
    }
}
