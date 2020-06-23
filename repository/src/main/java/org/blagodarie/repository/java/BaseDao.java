package org.blagodarie.repository.java;

import androidx.annotation.NonNull;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Update;

import com.ex.diagnosticlib.Diagnostic;

import java.util.Collection;
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
abstract class BaseDao<EntityType extends BaseEntity> {

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
     * @see ContactKeyDao#deleteByContactIds(List)
     * @see ContactKeyDao#__deleteByContactIds(List)
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
    private static final short NOT_INSERTED_OBJECT_ID = -1;

    /**
     * Вставляет объект сущности в соответствующую ей таблицу. Если объект конфликтует с уже
     * существующей в таблице записью, то объект не вставляется. Не рекомендуется использовать
     * для внешних вызовов. Для внешних вызовов рекомендуется использовать метод
     * {@link BaseDao#insertAndSetId(BaseEntity)}.
     *
     * @param obj Объект для вставки.
     * @return Если объект вставлен, возвращает его идентификатор, если нет - значение -1.
     */
    @Insert (onConflict = OnConflictStrategy.IGNORE)
    abstract long insert (final EntityType obj);

    /**
     * Вставляет список объектов сущности в соответствующую ей таблицу. Если объект конфликтует
     * с уже существующей в таблице записью, то объект не вставляется. Не рекомендуется использовать
     * для внешних вызовов. Для внешних вызовов рекомендуется использовать метод
     * {@link BaseDao#insertAndSetIds(Collection)}.
     *
     * @param objs Список объектов для вставки.
     * @return Массив идентификаторов, соответствующий списку объектов. Если объект не вставлен
     * значение соответствующего ему идентификатора будет равно {@link this#NOT_INSERTED_OBJECT_ID}.
     */
    @Insert (onConflict = OnConflictStrategy.IGNORE)
    abstract long[] insert (final Collection<EntityType> objs);

    /**
     * Вставляет объект сущности в соответствующую ей таблицу, а также устанавливает объекту
     * идентификатор, заданный при вставке. Если объект не удалось вставить, идентификатор
     * устанавливается в значение {@code null}.
     *
     * @param obj Объект для вставки.
     */
    final void insertAndSetId (@NonNull final EntityType obj) {
        //вставляем объект и получаем его id
        long id = insert(obj);

        //если объект вставлен, установить ему id
        if (id != NOT_INSERTED_OBJECT_ID) {
            obj.setId(id);
        } else {
            //иначе установить id в значение null
            obj.setId(null);
        }
    }

    /**
     * Вставляет объекты сущности в соответствующую ей таблицу, а также устанавливает объектам
     * идентификаторы, заданные при вставке. Если объект не удалось вставить, идентификатор
     * устанавливается в значение {@code null}.
     *
     * @param objs Объекты для вставки.
     */
    final void insertAndSetIds (@NonNull final Collection<EntityType> objs) {
        //вставляем объекты и получаем массив id
        long[] ids = insert(objs);

        //количество идентификаторов должно быть равно количеству объектов
        Diagnostic.Assert(
                ids.length == objs.size(),
                "The number of ids is not equal to the number of objects"
        );

        int i = 0;
        //для всех идентификаторов
        for (EntityType obj : objs) {
            long id = ids[i];
            //если объект вставлен
            if (id != NOT_INSERTED_OBJECT_ID) {
                //установить ему id
                obj.setId(id);
                Diagnostic.i(String.format(Locale.ENGLISH, "Object %s inserted", obj));
            } else {
                //иначе установить id в значение null
                obj.setId(null);
                Diagnostic.i(String.format(Locale.ENGLISH, "Object %s not inserted", obj));
            }
            i++;
        }
    }

    /**
     * Обновляет запись объекта в БД.
     *
     * @param obj Объект для обновления.
     */
    @Update
    abstract void update (final EntityType obj);

    /**
     * Обновляет записи объектов в БД.
     *
     * @param objs Объекты для обновления.
     */
    @Update
    abstract void update (final Collection<EntityType> objs);

    /**
     * Удаляет записи объектов из БД.
     *
     * @param objs Объекты для удаления.
     */
    @Delete
    abstract void delete (final Collection<EntityType> objs);

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
        int indexFrom = 0;
        int indexTo = Math.min(SQLITE_MAX_VARIABLE_NUMBER, ids.size());
        while (indexFrom != indexTo) {
            looper.loop(ids.subList(indexFrom, indexTo));
            indexFrom = indexTo;
            indexTo = Math.min(indexTo + SQLITE_MAX_VARIABLE_NUMBER, ids.size());
        }

    }

    /**
     * Возвращает объект по его идентификатору.
     *
     * @param id Идентификатор объекта
     * @return Объект из таблицы.
     */
    abstract EntityType getById (final long id);

    /**
     * Возвращает все объекты из соотвестсвующей его типу таблицы.
     *
     * @return Список объектов.
     */
    abstract List<EntityType> getAll ();
}
