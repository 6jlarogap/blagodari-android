package com.vsdrozd.blagodarie;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.vsdrozd.blagodarie.db.generators.KeyzTypeGenerator;
import com.vsdrozd.blagodarie.db.generators.UserGenerator;
import com.vsdrozd.blagodarie.db.scheme.Keyz;
import com.vsdrozd.blagodarie.db.scheme.KeyzType;
import com.vsdrozd.blagodarie.db.scheme.User;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.List;

import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.functions.Consumer;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;

@RunWith (AndroidJUnit4.class)
public class DataRepositoryTest
        extends TestWithEmptyDatabase {

    private DataRepository mDataRepository;
    private CompositeDisposable mDisposables = new CompositeDisposable();

    @Before
    public void createDataRepository () {
        mDataRepository = new DataRepository(getDatabase());
    }

    @After
    public void dispose () {
        mDisposables.dispose();
    }

    @Test
    public void testGetOrCreateUser () {
        long userId = 1L;
        //вставить
        mDisposables.add(
                mDataRepository.
                        getOrCreateUser(userId).
                        subscribe(user -> {
                            User dbUser = getDatabase().getUserDao().get(userId);
                            assertEquals(user.getId(), dbUser.getId());
                            assertEquals(user.getServerId(), dbUser.getServerId());
                            assertEquals(user.getSyncTimestamp(), dbUser.getSyncTimestamp());
                        })
        );

        //выбрать
        mDisposables.add(
                mDataRepository.
                        getOrCreateUser(userId).
                        subscribe(user -> {
                            User dbUser = getDatabase().getUserDao().get(userId);
                            assertEquals(user.getId(), dbUser.getId());
                            assertEquals(user.getServerId(), dbUser.getServerId());
                            assertEquals(user.getSyncTimestamp(), dbUser.getSyncTimestamp());
                        })
        );

    }

    @Test
    public void testIsAuthorizedUser () {
        //создать пользователя
        final User user = UserGenerator.getInstance().get();
        getDatabase().getUserDao().insertAndSetId(user);

        //должен быть неавторизован
        mDisposables.add(
                mDataRepository.
                        isAuthorizedUser(user.getId()).
                        subscribe((Consumer<Boolean>) Assert::assertFalse)
        );

        //создать три типа ключей
        List<KeyzType> keyzTypeList = KeyzTypeGenerator.getInstance().getList(3);
        getDatabase().getKeyzTypeDao().insertAndSetIds(keyzTypeList);

        final String value = "testValue";

        //создать для пользователя ключ с идентификатором типа GOOGLE_ACCOUNT_ID
        mDisposables.add(
                mDataRepository.
                        createKeyz(user.getId(), value, KeyzType.Types.GOOGLE_ACCOUNT_ID.getId()).
                        subscribe()
        );


        //должен быть авторизован
        mDisposables.add(
                mDataRepository.
                        isAuthorizedUser(user.getId()).
                        subscribe((Consumer<Boolean>) Assert::assertTrue)
        );
    }

    @Test
    public void testCreateKeyz () {
        final User user = UserGenerator.getInstance().get();
        getDatabase().getUserDao().insertAndSetId(user);

        final KeyzType keyzType = KeyzTypeGenerator.getInstance().get();
        getDatabase().getKeyzTypeDao().insertAndSetId(keyzType);

        final String value = "testValue";

        mDisposables.add(
                mDataRepository.
                        createKeyz(user.getId(), value, keyzType.getId()).
                        subscribe(() -> {
                                    List<Keyz> dbKeyz = getDatabase().getKeyzDao().getByOwnerIdAndTypeId(user.getId(), keyzType.getId());
                                    assertEquals(dbKeyz.size(), 1);
                                    assertEquals(dbKeyz.get(0).getId().longValue(), 1L);
                                    assertNull(dbKeyz.get(0).getServerId());
                                    assertEquals(dbKeyz.get(0).getOwnerId(), user.getId());
                                    assertEquals(dbKeyz.get(0).getValue(), value);
                                    assertEquals(dbKeyz.get(0).getTypeId(), keyzType.getId());
                                    assertFalse(dbKeyz.get(0).getVague());
                                }
                        )
        );
    }
}
