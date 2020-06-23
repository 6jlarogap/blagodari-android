package org.blagodarie.repository.java;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Query;
import androidx.room.Transaction;

import java.util.Collection;
import java.util.List;

/**
 * Класс, определяющий DAO для сущности {@link Key}.
 *
 * @author sergeGabrus
 */
@Dao
abstract class KeyDao
        extends SynchronizableDao<Key> {

    @Override
    @Query ("SELECT * " +
            "FROM tbl_key " +
            "WHERE id = :id")
    abstract Key getById (final long id);

    @Override
    @Query ("SELECT * " +
            "FROM tbl_key")
    abstract List<Key> getAll ();

    @Override
    @Query ("SELECT id " +
            "FROM tbl_like " +
            "WHERE server_id = :serverId")
    public abstract Long getIdByServerId (final long serverId);

    @Override
    @Query ("SELECT server_id " +
            "FROM tbl_like " +
            "WHERE id = :id")
    public abstract Long getServerIdById (final long id);

    /**
     * Вставляет ключ в таблицу. Если ключ вставился, проставляет ему идентификатор,
     * если не вставился - получает идентификатор из БД.
     *
     * @param key Ключ.
     */
    @Transaction
    public void insertAndSetIdOrGetIdFromDB (final Key key) {
        //пытаемся вставить ключ
        insertAndSetId(key);
        //если ключ не вставился
        if (key.getId() == null) {
            //получаем идентификатор соответствующего ключа из БД
            Long keyId = getId(key.getValue(), key.getTypeId());
            //устанавливаем ключу идентификатор
            key.setId(keyId);
        }
    }

    /**
     * Вставляет ключи в таблицу. Вставленным ключам проставляет идентификатор, не вставленным -
     * проставляет идентификатор из БД.
     *
     * @param keyCollection Набор ключей.
     */
    @Transaction
    void insertAndSetIdsOrGetIdsFromDB (final Collection<Key> keyCollection) {
        //пытаемся вставить ключ
        insertAndSetIds(keyCollection);
        //если ключ не вставился
        for (Key key : keyCollection) {
            if (key.getId() == null) {
                //получаем идентификатор соответствующего ключа из БД
                Long keyId = getId(key.getValue(), key.getTypeId());
                //устанавливаем ключу идентификатор
                key.setId(keyId);
            }
        }
    }

    /**
     * Делает запрос в БД и возвращает количетво ключей, соответствующих заданному владельцу и типу
     * ключа.
     *
     * @param ownerId Идентификтор владельца ключа.
     * @param typeId  Идентификатор типа ключа.
     * @return Количество ключей.
     */
    @Query ("SELECT COUNT(*) " +
            "FROM tbl_key " +
            "WHERE owner_id = :ownerId " +
            "AND type_id = :typeId")
    public abstract Integer getCountByOwnerIdAndTypeId (final long ownerId, final long typeId);

    /**
     * Делает запрос в БД и возвращает список ключей, соответствующих заданным владельцу и типу
     * ключа.
     *
     * @param ownerId Идентификатор пользователя - владельца ключа.
     * @param typeId  Идентификатор типа ключа.
     * @return Список ключей.
     */
    @Query ("SELECT * " +
            "FROM tbl_key " +
            "WHERE owner_id = :ownerId " +
            "AND type_id = :typeId")
    public abstract List<Key> getByOwnerIdAndTypeId (final long ownerId, final long typeId);

    /**
     * Делает запрос в БД и возвращает идентификатор ключа, соответствующего заданному значению и типу.
     *
     * @param value  Значение ключа.
     * @param typeId Идентификатор типа ключа.
     * @return Идентификатор ключа.
     */
    @Query ("SELECT id " +
            "FROM tbl_key " +
            "WHERE value = :value " +
            "AND type_id = :typeId")
    public abstract Long getId (final String value, final long typeId);

    /**
     * Делает запрос в БД и возвращает серверный идентификатор ключа по его id.
     *
     * @param id Идентификатор ключа.
     * @return Серверный идентификатор.
     */
    @Query ("SELECT server_id " +
            "FROM tbl_key " +
            "WHERE id = :id")
    public abstract Long getServerId (final long id);

    /**
     * Делает запрос в БД и возвращает список ключей контактов, заданного пользователя.
     *
     * @param userId Идентификатор пользователя.
     * @return Список ключей.
     */
    @Query ("SELECT k.* " +
            "FROM tbl_key k " +
            "LEFT JOIN tbl_user_key uk ON uk.key_id = k.id " +
            "WHERE uk.user_id = :userId")
    public abstract List<Key> getByUserId (final long userId);

    /**
     * Делает запрос в БД и возвращает список ключей для заданного контакта.
     *
     * @param contactId Идентификатор контакты.
     * @return Список ключей.
     */
    @Query ("SELECT k.* " +
            "FROM tbl_key k " +
            "LEFT JOIN tbl_contact_key ck ON ck.key_id = k.id " +
            "WHERE ck.contact_id = :contactId")
    public abstract List<Key> getByContactId (final long contactId);

    /**
     * Делает запрос в БД и возвращает список ключей, которые необходимо отправить на сервер
     * для получения серверного идентификатора.
     *
     * @param userId Идентификатор пользователя.
     * @return Список ключей.
     */
    /*Выбираем ключи, связанные с заданным пользователем. Пользователь должен быть синхронизирован
    с сервером, а ключи нет.*/
    @Query ("SELECT k.* " +
            "FROM tbl_key k " +
            "LEFT JOIN tbl_user_key uk ON uk.key_id = k.Id " +
            "LEFT JOIN tbl_user u ON u.Id = uk.user_id " +
            "WHERE u.Id = :userId " +
            "AND u.server_id IS NOT NULL " +
            "AND k.server_id IS NULL " +
            "AND k.owner_id IS NULL")
    public abstract List<Key> getForGetOrCreate (final long userId);

    /**
     * Определяет существуют ли ключи, которые необходимо отправить на сервер для получения
     * серверного идентификатора.
     *
     * @param userId Идентификатор пользователя.
     * @return Количество ключей.
     */
    /*Выбираем количество ключей, связанных с заданным пользователем. Пользователь должен быть
    синхронизирован с сервером, а ключи нет.*/
    @Query ("SELECT COUNT(*) " +
            "FROM tbl_key k " +
            "LEFT JOIN tbl_user_key uk ON uk.key_id = k.Id " +
            "LEFT JOIN tbl_user u ON u.Id = uk.user_id " +
            "WHERE u.Id = :userId " +
            "AND u.server_id IS NOT NULL " +
            "AND k.server_id IS NULL " +
            "AND k.owner_id IS NULL")
    public abstract LiveData<Boolean> isExistsForGetOrCreate (final long userId);

    @Query ("UPDATE tbl_key " +
            "SET vague = (SELECT CASE " +
            "                    WHEN COUNT(*) < 2 THEN 0 " +
            "                    ELSE 1 " +
            "                    END " +
            "             FROM tbl_contact_key " +
            "             WHERE tbl_contact_key.key_id = tbl_key.id)")
    public abstract void verifyVague ();

    @Query ("SELECT k.* " +
            "FROM tbl_key k " +
            "LEFT JOIN tbl_contact_key ck ON ck.key_id = k.id " +
            "WHERE ck.contact_id = :contactId " +
            "AND k.type_id = :typeId")
    public abstract LiveData<List<Key>> getLiveDataByContactIdAndTypeId (
            final long contactId,
            final long typeId
    );

}
