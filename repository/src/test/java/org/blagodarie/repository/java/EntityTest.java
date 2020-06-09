package org.blagodarie.repository.java;

abstract class EntityTest {

    private static final Long DEFAULT_ID = null;

    static Long getDefaultId () {
        return DEFAULT_ID;
    }

    public abstract void testSetId ();

}
