package com.vsdrozd.blagodarie.db.dao;

import androidx.annotation.NonNull;
import androidx.room.Dao;
import androidx.room.Query;

import com.vsdrozd.blagodarie.db.scheme.User;
import com.vsdrozd.blagodarie.db.scheme.UserContact;

import java.util.List;

/**
 * Класс, определяющий DAO для сущности {@link UserContact}.
 *
 * @author sergeGabrus
 */
@Dao
public abstract class UserContactDao
        extends BaseDao<UserContact> {

    @Query ("SELECT * " +
            "FROM tbl_user_contact")
    public abstract List<UserContact> getAll ();

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
