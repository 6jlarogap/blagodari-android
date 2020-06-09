package org.blagodari.repository.java;

import androidx.annotation.NonNull;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class ContactKeyTest
        extends EntityTest {

    @Test
    public void testConstructor(){
        final Long contactId = 1L;
        final Long keyId = 2L;

        final ContactKey contactKey = new ContactKey(contactId, keyId);

        check(
                contactKey,
                getDefaultId(),
                contactId,
                keyId
        );
    }

    @Test
    @Override
    public void testSetId () {
        final Long contactId = 1L;
        final Long keyId = 2L;

        final ContactKey contactKey = new ContactKey(contactId, keyId);

        final Long newId = 21L;

        contactKey.setId(newId);

        check(
                contactKey,
                newId,
                contactId,
                keyId
        );
    }

    private static void check(
            @NonNull final ContactKey contactKey,
            final Long id,
            final Long contactId,
            final Long keyId
    ){
        System.out.println(contactKey);

        assertEquals(contactKey.getId(), id);
        assertEquals(contactKey.getContactId(), contactId);
        assertEquals(contactKey.getKeyId(), keyId);
    }
}
