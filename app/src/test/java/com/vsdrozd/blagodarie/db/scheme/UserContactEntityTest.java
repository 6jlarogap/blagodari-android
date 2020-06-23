package com.vsdrozd.blagodarie.db.scheme;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class UserContactEntityTest {

    @Test
    public void testFullConstructor () {
        Long contactId = 1L;
        Long userId = 2L;

        UserContact userContact = new UserContact(userId, contactId);

        assertNull(userContact.getId());
        assertEquals(userContact.getContactId(), contactId);
        assertEquals(userContact.getUserId(), userId);

        System.out.println("toString" + userContact.toString());
    }
}
