package org.blagodari.db.scheme;

import org.junit.Test;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertNull;

public class UserEntityTest {

    @Test
    public void testEmptyContsructor () {
        User user = new User();
        final Long defaultSyncTimestamp = 0L;

        assertNull(user.getId());
        assertNull(user.getServerId());
        assertEquals(user.getSyncTimestamp(), defaultSyncTimestamp);

        System.out.println("toString" + user.toString());
    }

    @Test
    public void testSetters () {
        final Long id = 1L;
        final Long serverId = 2L;
        final Long syncTimestamp = 3L;

        User user = new User();
        user.setId(id);
        user.setServerId(serverId);
        user.setSyncTimestamp(syncTimestamp);

        assertEquals(user.getId(), id);
        assertEquals(user.getServerId(), serverId);
        assertEquals(user.getSyncTimestamp(), syncTimestamp);

        System.out.println("toString" + user.toString());
    }
}
