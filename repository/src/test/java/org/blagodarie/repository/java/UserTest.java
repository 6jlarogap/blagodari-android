package org.blagodarie.repository.java;

import androidx.annotation.NonNull;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class UserTest
        extends SynchronizableEntityTest {

    private static final Long DEFAULT_SYNC_TYMESTAMP = 0L;

    @Test
    public void testConstructor () {
        final Long id = 10L;

        final User user = new User(id);

        check(
                user,
                id,
                getDefaultServerId(),
                DEFAULT_SYNC_TYMESTAMP
        );
    }

    @Test
    @Override
    public void testSetId () {
        final Long id = 10L;

        final User user = new User(id);

        final Long newId = 11L;

        user.setId(newId);

        check(
                user,
                newId,
                getDefaultServerId(),
                DEFAULT_SYNC_TYMESTAMP
        );
    }

    @Test
    @Override
    public void testSetServerId () {
        final Long id = 10L;

        final User user = new User(id);

        final Long newServerId = 123L;

        user.setServerId(newServerId);

        check(
                user,
                id,
                newServerId,
                DEFAULT_SYNC_TYMESTAMP
        );
    }

    @Test
    public void testSetSyncTimestamp () {
        final Long id = 10L;

        final User user = new User(id);

        final Long newSyncTimestamp = 100500L;

        user.setSyncTimestamp(newSyncTimestamp);

        check(
                user,
                id,
                getDefaultServerId(),
                newSyncTimestamp
        );
    }

    private static void check(
            @NonNull final User user,
            final Long id,
            final Long serverId,
            final Long syncTimestamp
    ){
        System.out.print(user);

        assertEquals(user.getId(), id);
        assertEquals(user.getServerId(), serverId);
        assertEquals(user.getSyncTimestamp(), syncTimestamp);
    }
}
