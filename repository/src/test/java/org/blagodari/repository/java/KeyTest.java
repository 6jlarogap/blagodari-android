package org.blagodari.repository.java;

import androidx.annotation.NonNull;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

public class KeyTest
        extends SynchronizableEntityTest {

    private static final Long DEFAULT_OWNER_ID = null;
    private static final Boolean DEFAULT_VAGUE = false;

    @Test
    public void testMinimalConstructor () {
        final String value = "testValue";
        final Long typeId = 1L;

        final Key key = new Key(value, typeId);

        check(
                key,
                getDefaultId(),
                getDefaultServerId(),
                DEFAULT_OWNER_ID,
                value,
                typeId,
                DEFAULT_VAGUE
        );
    }

    @Test
    public void testFullConstructor () {
        final Long id = 1L;
        final Long serverId = 2L;
        final Long ownerId = 3L;
        final String value = "testValue";
        final Long typeId = 4L;
        final Boolean vague = true;

        final Key key = new Key(
                id,
                serverId,
                ownerId,
                value,
                typeId,
                vague
        );

        check(
                key,
                id,
                serverId,
                ownerId,
                value,
                typeId,
                vague
        );
    }

    @Test
    @Override
    public void testSetId () {
        final String value = "testValue";
        final Long typeId = 1L;

        final Key key = new Key(value, typeId);
        
        final Long newKeyId = 12L;
        
        key.setId(newKeyId);

        check(
                key,
                newKeyId,
                getDefaultServerId(),
                DEFAULT_OWNER_ID,
                value,
                typeId,
                DEFAULT_VAGUE
        );
    }

    @Test
    @Override
    public void testSetServerId () {
        final String value = "testValue";
        final Long typeId = 1L;

        final Key key = new Key(value, typeId);

        final Long newServerId = 12L;

        key.setServerId(newServerId);

        check(
                key,
                getDefaultId(),
                newServerId,
                DEFAULT_OWNER_ID,
                value,
                typeId,
                DEFAULT_VAGUE
        );
    }

    @Test
    public void testSetOwnerId () {
        final String value = "testValue1";
        final Long typeId = 1L;

        final Key key = new Key(value, typeId);

        final Long newOwnerId = 12L;

        key.setOwnerId(newOwnerId);

        check(
                key,
                getDefaultId(),
                getDefaultServerId(),
                newOwnerId,
                value,
                typeId,
                DEFAULT_VAGUE
        );
    }

    @Test
    public void testEquals () {
        Key key1 = new Key(1L, 2L, 3L, "val1", 4L, false);
        Key key2 = new Key(5L, 6L, 7L, "val1", 4L, false);
        Key key3 = new Key(8L, 9L, 10L, "val1", 4L, false);
        Key key4 = new Key(8L, 9L, 10L, "val2", 4L, false);
        Key key5 = new Key(8L, 9L, 10L, "val1", 5L, false);

        //Рефлексивность
        assertEquals(key1, key1);

        //Симметрия
        assertEquals(key1, key2);
        assertEquals(key2, key1);

        //Транзитивность
        assertEquals(key1, key2);
        assertEquals(key2, key3);
        assertEquals(key1, key3);

        assertNotEquals(key1, key4);
        assertNotEquals(key1, key5);
    }

    @Test
    public void testHashCode () {
        Key key1 = new Key(1L, 2L, 3L, "val1", 4L, false);
        Key key2 = new Key(1L, 2L, 3L, "val1", 4L, false);
        Key key3 = new Key(2L, 3L, 4L, "val1", 4L, false);

        assertEquals(key1.hashCode(), key2.hashCode());
        assertEquals(key1.hashCode(), key3.hashCode());
    }

    private static void check (
            @NonNull final Key key,
            final Long id,
            final Long serverId,
            final Long ownerId,
            final String value,
            final Long typeId,
            final Boolean vague
    ) {
        System.out.println(key);

        assertEquals(key.getId(), id);
        assertEquals(key.getServerId(), serverId);
        assertEquals(key.getOwnerId(), ownerId);
        assertEquals(key.getValue(), value);
        assertEquals(key.getTypeId(), typeId);
        assertEquals(key.getVague(), vague);
    }
}
