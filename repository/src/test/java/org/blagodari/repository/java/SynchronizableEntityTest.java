package org.blagodari.repository.java;

abstract class SynchronizableEntityTest
        extends EntityTest {

    private static final Long DEFAULT_SERVER_ID = null;

    static Long getDefaultServerId () {
        return DEFAULT_SERVER_ID;
    }

    public abstract void testSetServerId ();

}
