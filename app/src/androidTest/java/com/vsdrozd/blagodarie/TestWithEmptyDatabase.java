package com.vsdrozd.blagodarie;

import androidx.room.Room;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import com.vsdrozd.blagodarie.db.BlagodarieDatabase;

import org.junit.After;
import org.junit.Before;
import org.junit.runner.RunWith;

@RunWith (AndroidJUnit4.class)
public abstract class TestWithEmptyDatabase {

    private BlagodarieDatabase mDatabase;
    @Before
    public final void createDb() {
        this.mDatabase = Room.inMemoryDatabaseBuilder(
                InstrumentationRegistry.getInstrumentation().getContext(),
                BlagodarieDatabase.class)
                .build();
    }

    @After
    public final void closeDb() {
        this.mDatabase.close();
        this.mDatabase = null;
    }

    protected final BlagodarieDatabase getDatabase () {
        return this.mDatabase;
    }
}
