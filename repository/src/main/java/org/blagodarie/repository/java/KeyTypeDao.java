package org.blagodarie.repository.java;

import androidx.room.Dao;
import androidx.room.Query;

import java.util.List;

/**
 * Класс, определяющий DAO для сущности {@link KeyType}.
 *
 * @author sergeGabrus
 */
@Dao
abstract class KeyTypeDao
        extends BaseDao<KeyType> {

    @Override
    @Query ("SELECT * " +
            "FROM tbl_key_type " +
            "WHERE id = :id")
    abstract KeyType getById (final long id);

    @Override
    @Query ("SELECT * " +
            "FROM tbl_key_type")
    abstract List<KeyType> getAll ();
}
