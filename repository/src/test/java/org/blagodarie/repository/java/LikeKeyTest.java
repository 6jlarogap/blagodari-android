package org.blagodarie.repository.java;

import androidx.annotation.NonNull;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class LikeKeyTest
        extends SynchronizableEntityTest {

    private static final Boolean DEFAULT_VAGUE = false;
    private static final Boolean DEFAULT_NEED_SYNC = false;
    private static final Boolean DEFAULT_DELETED = false;

    @Test
    public void testMinimalConstructor () {
        final Long likeId = 1L;
        final Long keyId = 2L;

        final LikeKey likeKey = new LikeKey(likeId, keyId);

        check(
                likeKey,
                getDefaultId(),
                getDefaultServerId(),
                likeId,
                keyId,
                DEFAULT_VAGUE,
                DEFAULT_NEED_SYNC,
                DEFAULT_DELETED
        );
    }

    @Test
    public void testFullConstructor () {
        final Long id = 1L;
        final Long serverId = 2L;
        final Long likeId = 1L;
        final Long keyId = 2L;
        final Boolean vague = true;
        final Boolean needSync = true;
        final Boolean deleted = true;

        final LikeKey likeKey = new LikeKey(
                id,
                serverId,
                likeId,
                keyId,
                vague,
                needSync,
                deleted
        );

        check(
                likeKey,
                id,
                serverId,
                likeId,
                keyId,
                vague,
                needSync,
                deleted
        );
    }

    @Test
    @Override
    public void testSetId () {
        final Long likeId = 1L;
        final Long keyId = 2L;

        final LikeKey likeKey = new LikeKey(likeId, keyId);

        final Long newId = 33L;

        likeKey.setId(newId);

        check(
                likeKey,
                newId,
                getDefaultServerId(),
                likeId,
                keyId,
                DEFAULT_VAGUE,
                DEFAULT_NEED_SYNC,
                DEFAULT_DELETED
        );
    }

    @Test
    @Override
    public void testSetServerId () {
        final Long likeId = 1L;
        final Long keyId = 2L;

        final LikeKey likeKey = new LikeKey(likeId, keyId);

        final Long newServerId = 33L;

        likeKey.setServerId(newServerId);

        check(
                likeKey,
                getDefaultId(),
                newServerId,
                likeId,
                keyId,
                DEFAULT_VAGUE,
                DEFAULT_NEED_SYNC,
                DEFAULT_DELETED
        );
    }

    @Test
    public void testSetNeedSync () {
        final Long likeId = 1L;
        final Long keyId = 2L;

        final LikeKey likeKey = new LikeKey(likeId, keyId);

        final Boolean newNeedSync = true;

        likeKey.setNeedSync(newNeedSync);

        check(
                likeKey,
                getDefaultId(),
                getDefaultServerId(),
                likeId,
                keyId,
                DEFAULT_VAGUE,
                newNeedSync,
                DEFAULT_DELETED
        );
    }

    private static void check (
            @NonNull final LikeKey likeKey,
            final Long id,
            final Long serverId,
            final Long likeId,
            final Long keyId,
            final Boolean vague,
            final Boolean needSync,
            final Boolean deleted
    ) {
        System.out.println(likeKey);

        assertEquals(likeKey.getId(), id);
        assertEquals(likeKey.getServerId(), serverId);
        assertEquals(likeKey.getLikeId(), likeId);
        assertEquals(likeKey.getKeyId(), keyId);
        assertEquals(likeKey.getVague(), vague);
        assertEquals(likeKey.getNeedSync(), needSync);
        assertEquals(likeKey.getDeleted(), deleted);
    }
}
