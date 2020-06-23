package com.vsdrozd.blagodarie.db.dao;

import androidx.annotation.NonNull;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Update;

import com.ex.diagnosticlib.Diagnostic;
import com.vsdrozd.blagodarie.db.scheme.BaseEntity;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

/**
 * Базовый абстрактный класс для всех объектов доступа к данным (DAO). Содержит общие методы для
 * всех DAO.
 *
 * @param <EntityType> Класс сущности, к которой DAO реализует доступ.
 * @author sergeGabrus
 */
@Dao
public abstract class BaseDao<EntityType extends BaseEntity>
        extends AbstractDao<EntityType> {

    /**
     * Необходимо использовать данный интерфейс для вызова небезопасной функции в цикле с помощью
     * метода {@link this#splitByLoop(List, Looper)}.
     * <p>
     * Если запрос функции имеет вид типа ...WHERE value IN (:values)..., то такая функция считается
     * небезопасной, так как ее вызов может привести к исключительной ситуации при количестве
     * :ids > 999. Имя такой функции должно начинаться с двойного символа подчеркивания
     * "__" (конвенция именования). Данную функцию необходимо изолировать и предоставить для
     * внешних вызовов оберточную функцию с таким же именем, но без двойного подчеркивания
     * (конвенция именования). Необходимо использовать данный интерфейс для вызова небезопасной
     * функции в цикле с помощью метода {@link this#splitByLoop(List, Looper)}.
     *
     * @see ContactKeyzDao#deleteByContactIds(List)
     * @see ContactKeyzDao#__deleteByContactIds(Collection)
     */
    interface Looper<T> {
        void loop (@NonNull final List<T> values);
    }

    /**
     * Максимальное количество биндингуемых переменных. SQLite разрешает в запросе 999 переменных.
     * Данное ограничение может быть нарушено в запросах с оператором IN. Например:
     * <p>
     * SELECT *
     * FROM table
     * WHERE id IN (:ids)
     * <p>
     * Если массив ids больше 999 - сгенерируется исключение.
     * Данное ограничение установлено в 900 чтобы позволить кроме массива, вносить другие переменные
     * в запрос. Например:
     * <p>
     * SELECT *
     * FROM table
     * WHERE id IN (:ids)
     * AND title = :title
     * AND name = :name ... (до 99 переменных)
     */
    private static final short SQLITE_MAX_VARIABLE_NUMBER = 900;

    /**
     * Идентификатор не вставленного объекта ({@link androidx.room.OnConflictStrategy#IGNORE}).
     */
    private static final short ID_OF_NOT_INSERTED_OBJECT = -1;

    /**
     * Вставляет объект сущности в соответствующую ей таблицу, а также устанавливает объекту
     * идентификатор, заданный при вставке. Если объект не удалось вставить, идентификатор
     * устанавливается в значение {@code null}.
     *
     * @param object Объект для вставки.
     */
    public final void insertAndSetId (@NonNull final EntityType object) {
        //вставляем объект и получаем его id
        long id = __insert(object);

        //если объект вставлен, проставляем ему id, иначе проставляем null
        if (id != ID_OF_NOT_INSERTED_OBJECT) {
            object.setId(id);
        } else {
            object.setId(null);
        }
    }

    /**
     * Вставляет объекты сущности в соответствующую ей таблицу, а также устанавливает объектам
     * идентификаторы, заданные при вставке. Если объект не удалось вставить, идентификатор
     * устанавливается в значение {@code null}.
     *
     * @param objects Объект для вставки.
     */
    public final void insertAndSetIds (@NonNull final Collection<EntityType> objects) {
        //вставляем объекты и получаем массив id
        long[] ids = __insert(objects);

        //количество идентификаторов должно быть равно количеству объектов
        Diagnostic.Assert(
                ids.length == objects.size(),
                "The number of ids is not equal to the number of objects"
        );

        int i = 0;
        //для всех объектов
        for (EntityType obj : objects){
            long id = ids[i];
            //если объект вставлен, проставляем ему id, иначе проставляем null
            if (id != ID_OF_NOT_INSERTED_OBJECT) {
                obj.setId(id);
            } else {
                Diagnostic.i(String.format(Locale.ENGLISH, "Object %s not inserted", obj));
                obj.setId(null);
            }
            i++;
        }
    }

    /**
     * Обновляет запись объекта в БД.
     *
     * @param object Объект для обновления.
     */
    @Update
    public abstract void update (EntityType object);

    /**
     * Обновляет записи объектов в БД.
     *
     * @param objects Объекты для обновления.
     */
    @Update
    public abstract void update (Collection<EntityType> objects);

    /**
     * Удаляет объект из БД.
     *
     * @param obj Объект для удаления.
     */
    @Delete
    public abstract void delete (EntityType obj);

    /**
     * Удаляет записи объектов из БД.
     *
     * @param objects Объекты для удаления.
     */
    @Delete
    public abstract void delete (Collection<EntityType> objects);

    /**
     * Если в количество идентификаторов в списке превышает максимально разрешенное
     * {@link this#SQLITE_MAX_VARIABLE_NUMBER}, то разбивает один вызов на несколько вызовов в цикле.
     *
     * @param ids    Список идентификаторов.
     * @param looper Колбэк для вызова.
     */
    <T> void splitByLoop (
            @NonNull final List<T> ids,
            @NonNull final Looper<T> looper
    ) {
        if (ids.size() <= SQLITE_MAX_VARIABLE_NUMBER) {
            looper.loop(ids);
        } else {
            int indexFrom = 0;
            int indexTo = SQLITE_MAX_VARIABLE_NUMBER;
            while (indexFrom != indexTo) {
                looper.loop(ids.subList(indexFrom, indexTo));
                indexFrom = indexTo;
                indexTo = Math.min(indexTo + SQLITE_MAX_VARIABLE_NUMBER, ids.size());
            }
        }
    }

}
