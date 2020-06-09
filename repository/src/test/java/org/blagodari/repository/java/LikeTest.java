package org.blagodari.repository.java;

import androidx.annotation.NonNull;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class LikeTest
        extends SynchronizableEntityTest {

    private static final Long DEFAULT_CONTACT_ID = null;
    private static final Long DEFAULT_CANCEL_TIMESTAMP = null;
    private static final Boolean DEFAULT_NEED_SYNC = false;
    private static final Boolean DEFAULT_DELETED = false;

    @Test
    public void testMinimalConstructor(){
        final Long ownerId = 12L;
        final Long createTimestamp = 1234L;

        final Like like = new Like(ownerId, createTimestamp);

        check(
                like,
                getDefaultId(),
                getDefaultServerId(),
                ownerId,
                DEFAULT_CONTACT_ID,
                createTimestamp,
                DEFAULT_CANCEL_TIMESTAMP,
                DEFAULT_NEED_SYNC,
                DEFAULT_DELETED
        );
    }

    @Test
    public void testFullConstructor(){
        final Long id = 1L;
        final Long serverId = 2L;
        final Long ownerId = 3L;
        final Long contactId = 4L;
        final Long createTimestamp = 5L;
        final Long cancelTimestamp = 6L;
        final Boolean needSync = true;
        final Boolean deleted = true;

        final Like like = new Like(
                id,
                serverId,
                ownerId,
                contactId,
                createTimestamp,
                cancelTimestamp,
                needSync,
                deleted);

        check(
                like,
                id,
                serverId,
                ownerId,
                contactId,
                createTimestamp,
                cancelTimestamp,
                needSync,
                deleted
        );
    }

    @Test
    @Override
    public void testSetId () {
        final Long ownerId = 12L;
        final Long createTimestamp = 1234L;

        final Like like = new Like(ownerId, createTimestamp);

        final Long newId = 1L;

        like.setId(newId);

        check(
                like,
                newId,
                getDefaultServerId(),
                ownerId,
                DEFAULT_CONTACT_ID,
                createTimestamp,
                DEFAULT_CANCEL_TIMESTAMP,
                DEFAULT_NEED_SYNC,
                DEFAULT_DELETED
        );
    }

    @Test
    @Override
    public void testSetServerId () {
        final Long ownerId = 12L;
        final Long createTimestamp = 1234L;

        final Like like = new Like(ownerId, createTimestamp);

        final Long newServerId = 1L;

        like.setServerId(newServerId);

        check(
                like,
                getDefaultId(),
                newServerId,
                ownerId,
                DEFAULT_CONTACT_ID,
                createTimestamp,
                DEFAULT_CANCEL_TIMESTAMP,
                DEFAULT_NEED_SYNC,
                DEFAULT_DELETED
        );
    }

    @Test
    public void testSetContactId () {
        final Long ownerId = 12L;
        final Long createTimestamp = 1234L;

        final Like like = new Like(ownerId, createTimestamp);

        final Long newContactId = 1L;

        like.setContactId(newContactId);

        check(
                like,
                getDefaultId(),
                getDefaultServerId(),
                ownerId,
                newContactId,
                createTimestamp,
                DEFAULT_CANCEL_TIMESTAMP,
                DEFAULT_NEED_SYNC,
                DEFAULT_DELETED
        );
    }

    @Test
    public void testSetCancelTimestamp () {
        final Long ownerId = 12L;
        final Long createTimestamp = 1234L;

        final Like like = new Like(ownerId, createTimestamp);

        final Long newCancelTimestamp = 1L;

        like.setCancelTimestamp(newCancelTimestamp);

        check(
                like,
                getDefaultId(),
                getDefaultServerId(),
                ownerId,
                DEFAULT_CONTACT_ID,
                createTimestamp,
                newCancelTimestamp,
                DEFAULT_NEED_SYNC,
                DEFAULT_DELETED
        );
    }

    @Test
    public void testSetNeedSync () {
        final Long ownerId = 12L;
        final Long createTimestamp = 1234L;

        final Like like = new Like(ownerId, createTimestamp);

        final Boolean newNeedSync = true;

        like.setNeedSync(newNeedSync);

        check(
                like,
                getDefaultId(),
                getDefaultServerId(),
                ownerId,
                DEFAULT_CONTACT_ID,
                createTimestamp,
                DEFAULT_CANCEL_TIMESTAMP,
                newNeedSync,
                DEFAULT_DELETED
        );
    }

    private static void check(
            @NonNull final Like like,
            final Long id,
            final Long serverId,
            final Long ownerId,
            final Long contactId,
            final Long createTimestamp,
            final Long cancelTimestamp,
            final Boolean needSync,
            final Boolean deleted
    ){
        System.out.println(like);

        assertEquals(like.getId(), id);
        assertEquals(like.getServerId(), serverId);
        assertEquals(like.getOwnerId(), ownerId);
        assertEquals(like.getContactId(), contactId);
        assertEquals(like.getCreateTimestamp(), createTimestamp);
        assertEquals(like.getCancelTimestamp(), cancelTimestamp);
        assertEquals(like.getNeedSync(), needSync);
        assertEquals(like.getDeleted(), deleted);
    }
}
