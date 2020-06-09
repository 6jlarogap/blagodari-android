package org.blagodari.repository.java;

import androidx.annotation.NonNull;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class UserContactTest
        extends EntityTest {

    @Test
    public void testConstructor(){
        final Long userId = 1L;
        final Long contactId = 2L;

        final UserContact userContact = new UserContact(userId, contactId);

        check(
                userContact,
                getDefaultId(),
                userId,
                contactId
        );
    }

    @Test
    @Override
    public void testSetId () {
        final Long userId = 1L;
        final Long contactId = 2L;

        final UserContact userContact = new UserContact(userId, contactId);

        final Long newId = 21L;

        userContact.setId(newId);

        check(
                userContact,
                newId,
                userId,
                contactId
        );
    }

    private static void check(
            @NonNull final UserContact userContact,
            final Long id,
            final Long userId,
            final Long contactId
    ){
        System.out.println(userContact);

        assertEquals(userContact.getId(), id);
        assertEquals(userContact.getUserId(), userId);
        assertEquals(userContact.getContactId(), contactId);
    }
}
