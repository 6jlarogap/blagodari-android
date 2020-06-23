package com.vsdrozd.blagodarie.db.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Query;
import androidx.room.Transaction;

import com.vsdrozd.blagodarie.db.addent.KeyzWithContacts;
import com.vsdrozd.blagodarie.db.scheme.Keyz;

import java.util.Collection;
import java.util.List;

/**
 * Класс, определяющий DAO для сущности {@link Keyz}.
 *
 * @author sergeGabrus
 */
@Dao
public abstract class KeyzDao
        extends BaseDao<Keyz> {

    @Query ("SELECT * " +
            "FROM tbl_keyz")
    public abstract List<Keyz> getAll ();

    /**
     * Делает запрос в БД и возвращает ключ, соответствующий заданному идентификатору.
     *
     * @param id Идентификатор.
     * @return Ключ.
     */
    @Query ("SELECT * " +
            "FROM tbl_keyz " +
            "WHERE id = :id")
    public abstract Keyz get (final long id);

    @Query ("SELECT * " +
            "FROM tbl_keyz " +
            "WHERE value = :value " +
            "AND type_id = :typeId")
    public abstract Keyz getByValueAndTypeId (
            final String value,
            final Long typeId
    );

    /**
     * Вставляет ключ в таблицу. Если ключ вставился, проставляет ему идентификатор,
     * если не вставился - получает идентификатор из БД.
     *
     * @param keyz Ключ.
     */
    @Transaction
    public void insertAndSetIdOrGetIdFromDB (final Keyz keyz) {
        //пытаемся вставить ключ
        insertAndSetId(keyz);
        //если ключ не вставился
        if (keyz.getId() == null) {
            //получаем идентификатор соответствующего ключа из БД
            final Long keyzId = getId(keyz.getValue(), keyz.getTypeId());
            //устанавливаем ключу идентификатор
            keyz.setId(keyzId);
        }
    }

    /**
     * Вставляет ключи в таблицу. Вставленным ключам проставляет идентификатор, не вставленным -
     * проставляет идентификатор из БД.
     *
     * @param keyzList Список ключей.
     */
    @Transaction
    public void insertAndSetIdsOrGetIdsFromDB (final Collection<Keyz> keyzList) {
        //пытаемся вставить ключ
        insertAndSetIds(keyzList);
        //если ключ не вставился
        for (Keyz keyz : keyzList) {
            if (keyz.getId() == null) {
                //получаем идентификатор соответствующего ключа из БД
                Long keyzId = getId(keyz.getValue(), keyz.getTypeId());
                //устанавливаем ключу идентификатор
                keyz.setId(keyzId);
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
            "FROM tbl_keyz " +
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
            "FROM tbl_keyz " +
            "WHERE owner_id = :ownerId " +
            "AND type_id = :typeId")
    public abstract List<Keyz> getByOwnerIdAndTypeId (final long ownerId, final long typeId);

    /**
     * Делает запрос в БД и возвращает идентификатор ключа, соответствующего заданному значению и типу.
     *
     * @param value  Значение ключа.
     * @param typeId Идентификатор типа ключа.
     * @return Идентификатор ключа.
     */
    @Query ("SELECT id " +
            "FROM tbl_keyz " +
            "WHERE value = :value " +
            "AND type_id = :typeId")
    public abstract Long getId (final String value, final long typeId);

    /**
     * Делает запрос в БД и возвращает идентификатор ключа, соответствующего заданному
     * серверному идентификатору.
     *
     * @param serverId Серверный идентификатор.
     * @return Идентификатор.
     */
    @Query ("SELECT id " +
            "FROM tbl_keyz " +
            "WHERE server_id = :serverId")
    public abstract Long getIdByServerId (final long serverId);

    /**
     * Делает запрос в БД и возвращает серверный идентификатор ключа по его id.
     *
     * @param id Идентификатор ключа.
     * @return Серверный идентификатор.
     */
    @Query ("SELECT server_id " +
            "FROM tbl_keyz " +
            "WHERE id = :id")
    public abstract Long getServerId (final long id);

    /**
     * Делает запрос в БД и возвращает список ключей контактов, заданного пользователя.
     *
     * @param userId Идентификатор пользователя.
     * @return Список ключей.
     */
    @Query ("SELECT k.* " +
            "FROM tbl_keyz k " +
            "LEFT JOIN tbl_user_keyz uk ON uk.keyz_id = k.id " +
            "WHERE uk.user_id = :userId")
    public abstract List<Keyz> getByUserId (final long userId);

    /**
     * Делает запрос в БД и возвращает список ключей для заданного контакта.
     *
     * @param contactId Идентификатор контакты.
     * @return Список ключей.
     */
    @Query ("SELECT k.* " +
            "FROM tbl_keyz k " +
            "LEFT JOIN tbl_contact_keyz ck ON ck.keyz_id = k.id " +
            "WHERE ck.contact_id = :contactId")
    public abstract List<Keyz> getByContactId (final long contactId);

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
            "FROM tbl_keyz k " +
            "LEFT JOIN tbl_user_keyz uk ON uk.keyz_id = k.Id " +
            "LEFT JOIN tbl_user u ON u.Id = uk.user_id " +
            "WHERE u.Id = :userId " +
            "AND u.server_id IS NOT NULL " +
            "AND k.server_id IS NULL " +
            "AND k.owner_id IS NULL")
    public abstract List<Keyz> getForGetOrCreate (final long userId);

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
            "FROM tbl_keyz k " +
            "LEFT JOIN tbl_user_keyz uk ON uk.keyz_id = k.Id " +
            "LEFT JOIN tbl_user u ON u.Id = uk.user_id " +
            "WHERE u.Id = :userId " +
            "AND u.server_id IS NOT NULL " +
            "AND k.server_id IS NULL " +
            "AND k.owner_id IS NULL")
    public abstract LiveData<Boolean> isExistsForGetOrCreate (final long userId);

    @Query ("UPDATE tbl_keyz " +
            "SET vague = (SELECT CASE " +
            "                    WHEN COUNT(*) < 2 THEN 0 " +
            "                    ELSE 1 " +
            "                    END " +
            "             FROM tbl_contact_keyz " +
            "             WHERE tbl_contact_keyz.keyz_id = tbl_keyz.id)")
    public abstract void verifyVague ();

    @Query ("SELECT k.* " +
            "FROM tbl_keyz k " +
            "LEFT JOIN tbl_contact_keyz ck ON ck.keyz_id = k.id " +
            "WHERE ck.contact_id = :contactId " +
            "AND k.type_id = :typeId")
    public abstract LiveData<List<Keyz>> getLiveDataByContactIdAndTypeId (
            final long contactId,
            final long typeId
    );

    @Transaction
    @Query ("SELECT * " +
            "FROM tbl_keyz " +
            "WHERE vague = 1")
    public abstract LiveData<List<KeyzWithContacts>> getVagueKeyzWithContacts ();

    @Transaction
    @Query ("SELECT * " +
            "FROM tbl_keyz " +
            "WHERE id = :id")
    public abstract KeyzWithContacts getKeyzWithContactsByKeyzId (final long id);

    @Query ("SELECT k.* " +
            "FROM tbl_keyz k " +
            "LEFT JOIN tbl_contact_keyz ck ON ck.keyz_id = k.id " +
            "WHERE ck.contact_id = :contactId")
    public abstract List<Keyz> getKeyzByContactId (final long contactId);
}
