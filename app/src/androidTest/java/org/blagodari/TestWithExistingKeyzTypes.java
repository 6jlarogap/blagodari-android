package org.blagodari;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.blagodari.db.scheme.KeyzType;

import org.junit.Before;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.List;

@RunWith (AndroidJUnit4.class)
public abstract class TestWithExistingKeyzTypes
        extends TestWithEmptyDatabase {

    private final List<KeyzType> mKeyzTypes = new ArrayList<>();

    TestWithExistingKeyzTypes () {
        //создать ключи
        for (KeyzType.Types t : KeyzType.Types.values()) {
            this.mKeyzTypes.add(t.createKeyzType());
        }
    }

    @Before
    public final void insertKeyzTypes () {
        //вставить типы ключей
        getDatabase().getKeyzTypeDao().insertAndSetIds(this.mKeyzTypes);
    }

    protected final List<KeyzType> getKeyzTypes () {
        return this.mKeyzTypes;
    }
}
