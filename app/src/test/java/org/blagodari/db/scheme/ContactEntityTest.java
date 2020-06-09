package org.blagodari.db.scheme;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNull;

public class ContactEntityTest {
/*
    @Test
    public void testFullContstructor () {
        final Long id = 1L;
        final String title = "testTitle";
        final String photoUri = "testUri";
        final Long fame = 3L;
        final Long likeCount = 4L;
        final Long sumLikeCount = 5L;

        Contact contact = new Contact(
                id,
                title,
                photoUri,
                fame,
                likeCount,
                sumLikeCount
        );

        assertEquals(contact.getId(), id);
        assertEquals(contact.getTitle(), title);
        assertEquals(contact.getPhotoUri(), photoUri);
        assertEquals(contact.getFame(), fame);
        assertEquals(contact.getLikeCount(), likeCount);
        assertEquals(contact.getSumLikeCount(), sumLikeCount);

        System.out.print("toString:" + contact.toString());
    }

    @Test
    public void testMinimalConstructor () {
        final String title = "testTitle";

        Contact contact = new Contact(title);

        assertNull(contact.getId());
        assertEquals(contact.getTitle(), title);
        assertNull(contact.getPhotoUri());
        assertEquals(contact.getFame().longValue(), 1L);
        assertEquals(contact.getLikeCount().longValue(), 0L);
        assertEquals(contact.getSumLikeCount().longValue(), 0L);

        System.out.print("toString:" + contact.toString());
    }

    @Test
    public void testSetters () {
        final Long id = 1L;
        final String title = "testTitle";
        final String photoUri = "testUri";
        final Long fame = 3L;
        final Long likeCount = 4L;
        final Long sumLikeCount = 5L;

        Contact contact = new Contact(title);
        contact.setId(id);
        contact.setPhotoUri(photoUri);
        contact.setFame(fame);
        contact.setLikeCount(likeCount);
        contact.setSumLikeCount(sumLikeCount);

        assertEquals(contact.getId(), id);
        assertEquals(contact.getPhotoUri(), photoUri);
        assertEquals(contact.getFame(), fame);
        assertEquals(contact.getLikeCount(), likeCount);
        assertEquals(contact.getSumLikeCount(), sumLikeCount);

        System.out.print("toString:" + contact.toString());
    }

    @Test
    public void testEquals () {
        Contact contact1 = new Contact(1L, "title 1", "photoUri 1", 1L, 0L, 0L);
        Contact contact2 = new Contact(1L, "title 1", "photoUri 1", 1L, 0L, 0L);
        Contact contact3 = new Contact(1L, "title 1", "photoUri 1", 1L, 0L, 0L);
        Contact contact4 = new Contact(2L, "title 1", "photoUri 1", 1L, 0L, 0L);
        Contact contact5 = new Contact(1L, "title 2", "photoUri 1", 1L, 0L, 0L);
        Contact contact7 = new Contact(1L, "title 1", "photoUri 2", 1L, 0L, 0L);
        Contact contact8 = new Contact(1L, "title 1", "photoUri 1", 2L, 0L, 0L);
        Contact contact9 = new Contact(1L, "title 1", "photoUri 1", 1L, 1L, 0L);
        Contact contact10 = new Contact(1L, "title 1", "photoUri 1", 1L, 0L, 1L);

        //Рефлексивность
        assertEquals(contact1, contact1);

        //Симметрия
        assertEquals(contact1, contact2);
        assertEquals(contact2, contact1);

        //Транзитивность
        assertEquals(contact1, contact2);
        assertEquals(contact2, contact3);
        assertEquals(contact1, contact3);

        assertNotEquals(contact1, contact4);
        assertNotEquals(contact1, contact5);
        assertNotEquals(contact1, contact7);

        assertEquals(contact1, contact8);
        assertEquals(contact1, contact9);
        assertEquals(contact1, contact10);
    }

    @Test
    public void testHashCode () {
        Contact contact1 = new Contact(1L, "title 1", "photoUri 1", 1L, 0L, 0L);
        Contact contact2 = new Contact(1L, "title 1", "photoUri 1", 2L, 0L, 0L);
        Contact contact3 = new Contact(1L, "title 1", "photoUri 1", 1L, 1L, 0L);

        assertEquals(contact1.hashCode(), contact2.hashCode());
        assertEquals(contact1.hashCode(), contact3.hashCode());
    }*/
}
