package org.blagodari.db.scheme;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class LikeKeyzEntityTest {

    @Test
    public void testFullConstructor () {
        final Long id = 1L;
        final Long serverId = 2L;
        final Long likeId = 3L;
        final Long keyzId = 4L;
        final Boolean vague = false;
        final Boolean needSync = true;
        final Boolean deleted = false;

        LikeKeyz likeKeyz = new LikeKeyz(
                id,
                serverId,
                likeId,
                keyzId,
                vague,
                needSync,
                deleted
        );

        assertEquals(likeKeyz.getId(), id);
        assertEquals(likeKeyz.getServerId(), serverId);
        assertEquals(likeKeyz.getLikeId(), likeId);
        assertEquals(likeKeyz.getKeyzId(), keyzId);
        assertEquals(likeKeyz.getVague(), vague);
        assertEquals(likeKeyz.getNeedSync(), needSync);
        assertEquals(likeKeyz.getDeleted(), deleted);

        System.out.println("toString" + likeKeyz.toString());
    }

    @Test
    public void testMinimalConstructor () {
        final Long likeId = 1L;
        final Long keyzId = 2L;

        LikeKeyz likeKeyz = new LikeKeyz(likeId, keyzId);

        assertNull(likeKeyz.getId());
        assertNull(likeKeyz.getServerId());
        assertEquals(likeKeyz.getLikeId(), likeId);
        assertEquals(likeKeyz.getKeyzId(), keyzId);
        assertFalse(likeKeyz.getVague());
        assertFalse(likeKeyz.getNeedSync());

        System.out.println("toString" + likeKeyz.toString());
    }

    @Test
    public void testSetters () {
        final Long id = 1L;
        final Long serverId = 2L;
        final Long likeId = 3L;
        final Long keyzId = 4L;
        final Boolean vague = false;
        final Boolean needSync = true;

        LikeKeyz likeKeyz = new LikeKeyz(likeId, keyzId);

        assertNull(likeKeyz.getId());
        assertNull(likeKeyz.getServerId());
        assertEquals(likeKeyz.getLikeId(), likeId);
        assertEquals(likeKeyz.getKeyzId(), keyzId);
        assertFalse(likeKeyz.getVague());
        assertFalse(likeKeyz.getNeedSync());

        likeKeyz.setId(id);
        likeKeyz.setServerId(serverId);
        likeKeyz.setVague(vague);
        likeKeyz.setNeedSync(needSync);

        assertEquals(likeKeyz.getId(), id);
        assertEquals(likeKeyz.getServerId(), serverId);
        assertEquals(likeKeyz.getVague(), vague);
        assertEquals(likeKeyz.getNeedSync(), needSync);

        System.out.println("toString" + likeKeyz.toString());
    }
}
