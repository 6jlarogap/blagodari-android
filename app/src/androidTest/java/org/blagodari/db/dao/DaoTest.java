package org.blagodari.db.dao;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.blagodari.TestWithEmptyDatabase;

import org.junit.Before;
import org.junit.runner.RunWith;

@RunWith (AndroidJUnit4.class)
public abstract class DaoTest extends TestWithEmptyDatabase {

    @Before
    abstract public void createDao ();

}
