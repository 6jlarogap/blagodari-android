package org.blagodari.db.dao;

import androidx.room.Dao;
import androidx.room.Query;

import org.blagodari.db.scheme.KeyzType;

/**
 * Класс, определяющий DAO для сущности {@link KeyzType}.
 *
 * @author sergeGabrus
 */
@Dao
public abstract class KeyzTypeDao
        extends BaseDao<KeyzType> {
    @Query ("SELECT * " +
            "FROM tbl_keyztype " +
            "WHERE id = :id")
    public abstract KeyzType get (final long id);
}
