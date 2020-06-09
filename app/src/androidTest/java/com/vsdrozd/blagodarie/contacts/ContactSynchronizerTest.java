package com.vsdrozd.blagodarie.contacts;

import androidx.room.Room;
import androidx.test.platform.app.InstrumentationRegistry;

import com.vsdrozd.blagodarie.db.BlagodarieDatabase;

import org.junit.Before;
import org.junit.Test;

public class ContactSynchronizerTest {

    private BlagodarieDatabase DB;

    @Before
    public void createDb() {
        this.DB = Room.inMemoryDatabaseBuilder(
                InstrumentationRegistry.getInstrumentation().getContext(),
                BlagodarieDatabase.class)
                .build();
    }

    @Test
    public void test () {
        //ContactSynchronizer.syncAll(1L, InstrumentationRegistry.getInstrumentation().getContext().getContentResolver(), DB);
    }
}
