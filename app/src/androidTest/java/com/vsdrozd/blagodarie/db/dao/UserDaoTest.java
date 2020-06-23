package com.vsdrozd.blagodarie.db.dao;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.vsdrozd.blagodarie.db.scheme.User;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.List;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertNotNull;
import static org.junit.Assert.assertNull;

@RunWith (AndroidJUnit4.class)
public class UserDaoTest
        extends DaoTest {

    private UserDao userDao;

    @Override
    public void createDao () {
        userDao = getDatabase().getUserDao();
    }

    @Test
    public void setIdTest () {
        //создать объект
        User user = new User ();

        //вставить объект в БД
        userDao.insertAndSetId(user);

        //убедиться что id проставлен
        assertNotNull(user.getId());

        //убедиться, что id имеет ожидаемое значение
        assertEquals(user.getId().longValue(), 1L);
    }

    @Test
    public void notSetIdTest () {
        //создать объект
        User user = new User ();
        user.setId(1L);

        //вставить объект в БД два раза
        userDao.insertAndSetId(user);
        userDao.insertAndSetId(user);

        //убедиться что id не проставлен
        assertNull(user.getId());
    }

    @Test
    public void setIdsTest () {
        //количество объектов для теста
        int testUsersCount = 100;

        //список объектов
        List<User> users = new ArrayList<>(testUsersCount);

        //массив ожидаемых идентификаторов
        long[] expectedIds = new long[testUsersCount];

        //заполнить список объектов и массив ожидаемых идентификаторов
        for (int i = 0; i < testUsersCount; i++) {
            users.add(new User());
            expectedIds[i] = (long)i + 1L;
        }

        //вставить список объектов в БД
        userDao.insertAndSetIds(users);

        //проверить что id проставились
        for (User u : users) {
            assertNotNull(u.getId());
        }

        //проверить что идентификаторы соответствуют ожидаемым
        for (int i = 0; i < testUsersCount; i++) {
            assertEquals(users.get(i).getId().longValue(), expectedIds[i]);
        }
    }

    @Test
    public void notSetIdsTest () {
        //количество объектов для теста
        int testUsersCount = 100;

        //список объектов
        List<User> users = new ArrayList<>(testUsersCount);

        //заполнить список объектов
        for (int i = 0; i < testUsersCount; i++) {
            users.add(new User());
        }

        //вставить список объектов в БД два раза
        userDao.insertAndSetIds(users);
        userDao.insertAndSetIds(users);

        //проверить что id не проставились
        for (User u : users) {
            assertNull(u.getId());
        }
    }

}
