package org.blagodari.db.scheme;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNull;

public class KeyzEntityTest {

    @Test
    public void testFullContstructor () {
        final Long id = 1L;
        final Long serverId = 2L;
        final Long ownerId = 3L;
        final String value = "testValue";
        final Long keyzTypeId = 4L;
        final Boolean vague = false;

        Keyz keyz = new Keyz(
                id,
                serverId,
                ownerId,
                value,
                keyzTypeId,
                vague
        );

        assertEquals(keyz.getId(), id);
        assertEquals(keyz.getServerId(), serverId);
        assertEquals(keyz.getOwnerId(), ownerId);
        assertEquals(keyz.getValue(), value);
        assertEquals(keyz.getTypeId(), keyzTypeId);
        assertEquals(keyz.getVague(), vague);

        System.out.println("toString" + keyz.toString());
    }

    @Test
    public void testMinimalConstructor () {
        final String value = "testValue";
        final Long keyzTypeId = 4L;

        Keyz keyz = new Keyz(value, keyzTypeId);

        assertNull(keyz.getId());
        assertNull(keyz.getServerId());
        assertNull(keyz.getOwnerId());
        assertEquals(keyz.getValue(), value);
        assertEquals(keyz.getTypeId(), keyzTypeId);

        System.out.println("toString" + keyz.toString());
    }

    @Test
    public void testSetters () {
        final Long id = 1L;
        final Long serverId = 2L;
        final Long ownerId = 3L;
        final String value = "testValue";
        final Long keyzTypeId = 4L;

        Keyz keyz = new Keyz(value, keyzTypeId);

        assertNull(keyz.getId());
        assertNull(keyz.getServerId());
        assertNull(keyz.getOwnerId());
        assertEquals(keyz.getValue(), value);
        assertEquals(keyz.getTypeId(), keyzTypeId);

        keyz.setId(id);
        keyz.setServerId(serverId);
        keyz.setOwnerId(ownerId);

        assertEquals(keyz.getId(), id);
        assertEquals(keyz.getServerId(), serverId);
        assertEquals(keyz.getOwnerId(), ownerId);
    }

    @Test
    public void testEquals () {
        Keyz keyz1 = new Keyz(1L, 2L, 3L, "val1", 4L, false);
        Keyz keyz2 = new Keyz(5L, 6L, 7L, "val1", 4L, false);
        Keyz keyz3 = new Keyz(8L, 9L, 10L, "val1", 4L, false);
        Keyz keyz4 = new Keyz(8L, 9L, 10L, "val2", 4L, false);
        Keyz keyz5 = new Keyz(8L, 9L, 10L, "val1", 5L, false);

        //Рефлексивность
        assertEquals(keyz1, keyz1);

        //Симметрия
        assertEquals(keyz1, keyz2);
        assertEquals(keyz2, keyz1);

        //Транзитивность
        assertEquals(keyz1, keyz2);
        assertEquals(keyz2, keyz3);
        assertEquals(keyz1, keyz3);

        assertNotEquals(keyz1, keyz4);
        assertNotEquals(keyz1, keyz5);
    }

    @Test
    public void testHashCode () {
        Keyz keyz1 = new Keyz(1L, 2L, 3L, "val1", 4L, false);
        Keyz keyz2 = new Keyz(1L, 2L, 3L, "val1", 4L, false);
        Keyz keyz3 = new Keyz(2L, 3L, 4L, "val1", 4L, false);

        assertEquals(keyz1.hashCode(), keyz2.hashCode());
        assertEquals(keyz1.hashCode(), keyz3.hashCode());
    }
}
