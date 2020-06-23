package com.vsdrozd.blagodarie.db.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;

import com.vsdrozd.blagodarie.db.scheme.BaseEntity;

import java.util.Collection;
import java.util.List;

@Dao
abstract class AbstractDao<EntityType extends BaseEntity> {
    /**
     * Вставляет объект сущности в соответствующую ей таблицу. Если объект конфликтует с уже
     * существующей в таблице записью, то объект не вставляется. Не рекомендуется использовать
     * для внешних вызовов. Для внешних вызовов рекомендуется использовать метод
     * {@link BaseDao#insertAndSetId(BaseEntity)}.
     *
     * @param object Объект для вставки.
     * @return Если объект вставлен, возвращает его идентификатор, если нет - значение -1.
     */
    @Insert (onConflict = OnConflictStrategy.IGNORE)
    abstract long __insert (final EntityType object);

    /**
     * Вставляет список объектов сущности в соответствующую ей таблицу. Если объект конфликтует
     * с уже существующей в таблице записью, то объект не вставляется. Не рекомендуется использовать
     * для внешних вызовов. Для внешних вызовов рекомендуется использовать метод
     * {@link BaseDao#insertAndSetIds(Collection)}.
     *
     * @param objects Список объектов для вставки.
     * @return Массив идентификаторов, соответствующий списку объектов. Если объект не вставлен
     * значение соответствующего ему идентификатора будет равно -1.
     */
    @Insert (onConflict = OnConflictStrategy.IGNORE)
    abstract long[] __insert (final Collection<EntityType> objects);

}
