package org.blagodari;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.blagodari.db.scheme.User;

import org.junit.Before;
import org.junit.runner.RunWith;

@RunWith (AndroidJUnit4.class)
public abstract class TestWithNotAuthorizedUserAndKeyzTypes
        extends TestWithExistingKeyzTypes {

    private final User mUser = new User();

    TestWithNotAuthorizedUserAndKeyzTypes () {
        super();
    }

    @Before
    public final void insertUser () {
        //вставить пользователя
        getDatabase().getUserDao().insertAndSetId(this.mUser);
    }

    protected final User getUser () {
        return this.mUser;
    }
}
