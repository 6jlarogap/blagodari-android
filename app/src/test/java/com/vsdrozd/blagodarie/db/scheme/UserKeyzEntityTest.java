package com.vsdrozd.blagodarie.db.scheme;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class UserKeyzEntityTest {

    @Test
    public void testFullConstructor () {
        final Long id = 1L;
        final Long serverId = 2L;
        final Long userId = 3L;
        final Long keyzId = 4L;
        final Boolean deleted = false;

        UserKeyz userKeyz = new UserKeyz(
                id,
                serverId,
                userId,
                keyzId,
                deleted
        );

        assertEquals(userKeyz.getId(), id);
        assertEquals(userKeyz.getServerId(), serverId);
        assertEquals(userKeyz.getUserId(), userId);
        assertEquals(userKeyz.getKeyzId(), keyzId);
        assertFalse(userKeyz.getDeleted());

        System.out.println("toString" + userKeyz.toString());
    }
}
