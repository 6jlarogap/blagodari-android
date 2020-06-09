package org.blagodari;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.blagodari.db.scheme.Keyz;
import org.blagodari.db.scheme.KeyzType;

import org.junit.Before;
import org.junit.runner.RunWith;

@RunWith (AndroidJUnit4.class)
public abstract class TestWithAuthorizedUserAndKeyzTypes
        extends TestWithNotAuthorizedUserAndKeyzTypes {

    private final Keyz mAuthorizingKeyz;

    public TestWithAuthorizedUserAndKeyzTypes () {
        this.mAuthorizingKeyz = new Keyz("0123456789", KeyzType.Types.GOOGLE_ACCOUNT_ID.getId());
        this.mAuthorizingKeyz.setOwnerId(getUser().getId());
    }

    @Before
    public final void insertUserAuthorizingKeyz () {
        getDatabase().getKeyzDao().insertAndSetId(this.mAuthorizingKeyz);
    }
}
