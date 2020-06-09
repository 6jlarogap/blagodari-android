package org.blagodari.contacts;

import androidx.room.Room;
import androidx.test.platform.app.InstrumentationRegistry;

import org.blagodari.db.BlagodariDatabase;

import org.junit.Before;
import org.junit.Test;

public class ContactSynchronizerTest {

    private BlagodariDatabase DB;

    @Before
    public void createDb() {
        this.DB = Room.inMemoryDatabaseBuilder(
                InstrumentationRegistry.getInstrumentation().getContext(),
                BlagodariDatabase.class)
                .build();
    }

    @Test
    public void test () {
        //ContactSynchronizer.syncAll(1L, InstrumentationRegistry.getInstrumentation().getContext().getContentResolver(), DB);
    }
}
