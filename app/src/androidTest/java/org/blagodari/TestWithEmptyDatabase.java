package org.blagodari;

import androidx.room.Room;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import org.blagodari.db.BlagodariDatabase;

import org.junit.After;
import org.junit.Before;
import org.junit.runner.RunWith;

@RunWith (AndroidJUnit4.class)
public abstract class TestWithEmptyDatabase {

    private BlagodariDatabase mDatabase;
    @Before
    public final void createDb() {
        this.mDatabase = Room.inMemoryDatabaseBuilder(
                InstrumentationRegistry.getInstrumentation().getContext(),
                BlagodariDatabase.class)
                .build();
    }

    @After
    public final void closeDb() {
        this.mDatabase.close();
        this.mDatabase = null;
    }

    protected final BlagodariDatabase getDatabase () {
        return this.mDatabase;
    }
}
