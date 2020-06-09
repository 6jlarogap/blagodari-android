package org.blagodari.repository.java;

import androidx.annotation.NonNull;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class UserKeyTest
        extends SynchronizableEntityTest {

    private static final Boolean DEFAULT_DELETED = false;

    @Test
    public void testMinimalConstructor(){
        final Long userId = 1L;
        final Long keyId = 2L;

        final UserKey userKey = new UserKey(userId, keyId);

        check(
                userKey,
                getDefaultId(),
                getDefaultServerId(),
                userId,
                keyId,
                DEFAULT_DELETED
        );
    }

    @Test
    @Override
    public void testSetId () {
        final Long userId = 1L;
        final Long keyId = 2L;

        final UserKey userKey = new UserKey(userId, keyId);

        final Long newId = 23L;

        userKey.setId(newId);

        check(
                userKey,
                newId,
                getDefaultServerId(),
                userId,
                keyId,
                DEFAULT_DELETED
        );
    }

    @Test
    @Override
    public void testSetServerId () {
        final Long userId = 1L;
        final Long keyId = 2L;

        final UserKey userKey = new UserKey(userId, keyId);

        final Long newServerId = 23L;

        userKey.setServerId(newServerId);

        check(
                userKey,
                getDefaultId(),
                newServerId,
                userId,
                keyId,
                DEFAULT_DELETED
        );
    }

    private static void check(
            @NonNull final UserKey userKey,
            final Long id,
            final Long serverId,
            final Long userId,
            final Long keyId,
            final Boolean deleted
    ){
        System.out.println(userKey);

        assertEquals(userKey.getId(), id);
        assertEquals(userKey.getServerId(), serverId);
        assertEquals(userKey.getUserId(), userId);
        assertEquals(userKey.getKeyId(), keyId);
        assertEquals(userKey.getDeleted(), deleted);
    }
}
