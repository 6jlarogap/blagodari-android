package com.vsdrozd.blagodarie.db.dao;

import androidx.room.Room;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import com.vsdrozd.blagodarie.TestWithEmptyDatabase;
import com.vsdrozd.blagodarie.db.BlagodarieDatabase;

import org.junit.After;
import org.junit.Before;
import org.junit.runner.RunWith;

@RunWith (AndroidJUnit4.class)
public abstract class DaoTest extends TestWithEmptyDatabase {

    @Before
    abstract public void createDao ();

}
