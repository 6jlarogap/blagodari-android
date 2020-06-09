package org.blagodari.db.scheme;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class ContactKeyzEntityTest {

    @Test
    public void testFullConstructor () {
        Long contactId = 1L;
        Long keyzId = 2L;

        ContactKeyz contactKeyz = new ContactKeyz(contactId, keyzId);

        assertNull(contactKeyz.getId());
        assertEquals(contactKeyz.getContactId(), contactId);
        assertEquals(contactKeyz.getKeyzId(), keyzId);

        System.out.println("toString" + contactKeyz.toString());
    }
}
