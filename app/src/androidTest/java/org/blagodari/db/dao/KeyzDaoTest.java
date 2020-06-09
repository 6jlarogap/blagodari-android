package org.blagodari.db.dao;

import org.blagodari.db.scheme.Keyz;
import org.blagodari.db.scheme.KeyzType;

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static junit.framework.TestCase.assertNotNull;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class KeyzDaoTest
        extends DaoTest {

    private KeyzDao keyzDao;

    @Override
    public void createDao () {
        keyzDao = getDatabase().getKeyzDao();
    }

    @Before
    public void createKeyzType () {
        KeyzType keyzType = new KeyzType(1L, "phone");
        getDatabase().getKeyzTypeDao().insertAndSetId(keyzType);
    }

    @Test
    public void setIdTest () {
        //создать объект
        String value = "testValue";
        Long keyzTypeId = 1L;
        Keyz keyz = new Keyz (value, keyzTypeId);

        //вставить объект в БД
        keyzDao.insertAndSetId(keyz);

        //убедиться что id проставлен
        assertNotNull(keyz.getId());

        //убедиться, что id имеет ожидаемое значение
        assertEquals(keyz.getId().longValue(), 1L);
    }

    @Test
    public void notSetIdTest () {
        //создать объект
        String value = "testValue";
        Long keyzTypeId = 1L;
        Keyz keyz = new Keyz (value, keyzTypeId);
        keyz.setId(1L);

        //вставить объект в БД два раза
        keyzDao.insertAndSetId(keyz);
        keyzDao.insertAndSetId(keyz);

        //убедиться что id не проставлен
        assertNull(keyz.getId());
    }

    @Test
    public void setIdsTest () {
        //количество объектов для теста
        int testKeyzsCount = 100;

        //список объектов
        List<Keyz> keyzs = new ArrayList<>(testKeyzsCount);

        //массив ожидаемых идентификаторов
        long[] expectedIds = new long[testKeyzsCount];

        //заполнить список объектов и массив ожидаемых идентификаторов
        String value;
        Long keyzTypeId = 1L;
        for (int i = 0; i < testKeyzsCount; i++) {
            value = "testValue" + i;
            keyzs.add(new Keyz (value, keyzTypeId));
            expectedIds[i] = (long)i + 1L;
        }

        //вставить список объектов в БД
        keyzDao.insertAndSetIds(keyzs);

        //проверить что id проставились
        for (Keyz u : keyzs) {
            assertNotNull(u.getId());
        }

        //проверить что идентификаторы соответствуют ожидаемым
        for (int i = 0; i < testKeyzsCount; i++) {
            assertEquals(keyzs.get(i).getId().longValue(), expectedIds[i]);
        }
    }

    @Test
    public void notSetIdsTest () {
        //количество объектов для теста
        int testKeyzsCount = 100;

        //список объектов
        List<Keyz> keyzs = new ArrayList<>(testKeyzsCount);

        //заполнить список объектов
        String value;
        Long keyzTypeId = 1L;
        for (int i = 0; i < testKeyzsCount; i++) {
            value = "testValue" + i;
            keyzs.add(new Keyz (value, keyzTypeId));
        }

        //вставить список объектов в БД два раза
        keyzDao.insertAndSetIds(keyzs);
        keyzDao.insertAndSetIds(keyzs);

        //проверить что id не проставились
        for (Keyz u : keyzs) {
            assertNull(u.getId());
        }
    }
}
