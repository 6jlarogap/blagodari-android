package org.blagodari.db.scheme;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

public class LikeEntityTest {

    @Test
    public void testFullContstructor () {
        final Long id = 1L;
        final Long serverId = 2L;
        final Long ownerId = 3L;
        final Long contactId = 4L;
        final Long createTimestamp = 5L;
        final Long cancelTimestamp = 6L;
        final Boolean needSync = false;
        final Boolean deleted = false;

        Like like = new Like(
                id,
                serverId,
                ownerId,
                contactId,
                createTimestamp,
                cancelTimestamp,
                needSync,
                deleted
        );

        assertEquals(like.getId(), id);
        assertEquals(like.getServerId(), serverId);
        assertEquals(like.getOwnerId(), ownerId);
        assertEquals(like.getContactId(), contactId);
        assertEquals(like.getCreateTimestamp(), createTimestamp);
        assertEquals(like.getCancelTimestamp(), cancelTimestamp);
        assertEquals(like.getNeedSync(), needSync);

        System.out.println("toString" + like.toString());
    }

    @Test
    public void testMinimalConstructor () {
        final Long ownerId = 3L;
        final Long contactId = 4L;
        final Long createTimestamp = 5L;

        final Like like = new Like(ownerId, createTimestamp);
        like.setContactId(contactId);

        assertNull(like.getId());
        assertNotNull(like.getContactId());
        assertNull(like.getServerId());
        assertEquals(like.getOwnerId(), ownerId);
        assertEquals(like.getContactId(), contactId);
        assertEquals(like.getCreateTimestamp(), createTimestamp);
        assertNull(like.getCancelTimestamp());
        assertFalse(like.getNeedSync());

        System.out.println("toString" + like.toString());
    }

    @Test
    public void testSetters () {
        final Long id = 1L;
        final Long serverId = 2L;
        final Long ownerId = 3L;
        final Long contactId = 4L;
        final Long createTimestamp = 5L;
        final Long cancelTimestamp = 6L;
        final Boolean needSync = false;

        Like like = new Like(ownerId, createTimestamp);
        like.setContactId(contactId);
        like.setId(id);
        like.setServerId(serverId);
        like.setCancelTimestamp(cancelTimestamp);
        like.setNeedSync(needSync);

        assertEquals(like.getId(), id);
        assertEquals(like.getContactId(), contactId);
        assertEquals(like.getServerId(), serverId);
        assertEquals(like.getCancelTimestamp(), cancelTimestamp);
        assertEquals(like.getNeedSync(), needSync);

        System.out.println("toString" + like.toString());
    }

}
