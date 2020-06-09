package org.blagodari.db.dao;

import org.blagodari.db.scheme.KeyzType;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static junit.framework.TestCase.assertNotNull;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class KeyzTypeDaoTest
        extends DaoTest {

    private KeyzTypeDao keyzTypeDao;

    @Override
    public void createDao () {
        keyzTypeDao = getDatabase().getKeyzTypeDao();
    }

    @Test
    public void setIdTest () {
        //создать объект
        Long id = 1L;
        String title = "testTitle";
        KeyzType keyzType = new KeyzType(id, title);

        //вставить объект в БД
        keyzTypeDao.insertAndSetId(keyzType);

        //убедиться что id проставлен
        assertNotNull(keyzType.getId());

        //убедиться, что id имеет ожидаемое значение
        assertEquals(keyzType.getId().longValue(), 1L);
    }

    @Test
    public void notSetIdTest () {
        //создать объект
        Long id = 1L;
        String title = "testTitle";
        KeyzType keyzType = new KeyzType(id, title);

        //вставить объект в БД два раза
        keyzTypeDao.insertAndSetId(keyzType);
        keyzTypeDao.insertAndSetId(keyzType);

        //убедиться что id не проставлен
        assertNull(keyzType.getId());
    }

    @Test
    public void setIdsTest () {
        //количество объектов для теста
        int testKeyzTypesCount = 100;

        //список объектов
        List<KeyzType> keyzTypes = new ArrayList<>(testKeyzTypesCount);

        //массив ожидаемых идентификаторов
        long[] expectedIds = new long[testKeyzTypesCount];

        //заполнить список объектов и массив ожидаемых идентификаторов
        Long id;
        String title;
        for (int i = 0; i < testKeyzTypesCount; i++) {
            id = (long) i + 1L;
            title = "testTitle" + i;
            keyzTypes.add(new KeyzType(id, title));
            expectedIds[i] = (long) i + 1L;
        }

        //вставить список объектов в БД
        keyzTypeDao.insertAndSetIds(keyzTypes);

        //проверить что id проставились
        for (KeyzType u : keyzTypes) {
            assertNotNull(u.getId());
        }

        //проверить что идентификаторы соответствуют ожидаемым
        for (int i = 0; i < testKeyzTypesCount; i++) {
            assertEquals(keyzTypes.get(i).getId().longValue(), expectedIds[i]);
        }
    }

    @Test
    public void notSetIdsTest () {
        //количество объектов для теста
        int testKeyzTypesCount = 100;

        //список объектов
        List<KeyzType> keyzTypes = new ArrayList<>(testKeyzTypesCount);

        //заполнить список объектов
        Long id;
        String title;
        for (int i = 0; i < testKeyzTypesCount; i++) {
            id = (long) i;
            title = "testTitle" + i;
            keyzTypes.add(new KeyzType(id, title));
        }

        //вставить список объектов в БД два раза
        keyzTypeDao.insertAndSetIds(keyzTypes);
        keyzTypeDao.insertAndSetIds(keyzTypes);

        //проверить что id не проставились
        for (KeyzType u : keyzTypes) {
            assertNull(u.getId());
        }
    }
}
