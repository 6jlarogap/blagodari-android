package org.blagodari.repository.java;

import androidx.annotation.NonNull;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

public class ContactTest
        extends EntityTest {

    private static final String DEFAULT_PHOTO_URI = null;
    private static final Long DEFAULT_FAME = 1L;
    private static final Long DEFAULT_LIKE_COUNT = 0L;
    private static final Long DEFAULT_SUM_LIKE_COUNT = 0L;

    @Test
    public void testMinimalConstructor () {
        final String title = "testTitle";

        final Contact contact = new Contact(title);

        check(
                contact,
                getDefaultId(),
                title,
                DEFAULT_PHOTO_URI,
                DEFAULT_FAME,
                DEFAULT_LIKE_COUNT,
                DEFAULT_SUM_LIKE_COUNT
        );
    }

    @Test
    public void testFullContstructor () {
        final Long id = 1L;
        final String title = "testTitle";
        final String photoUri = "testUri";
        final Long fame = 3L;
        final Long likeCount = 4L;
        final Long sumLikeCount = 5L;

        final Contact contact = new Contact(
                id,
                title,
                photoUri,
                fame,
                likeCount,
                sumLikeCount
        );

        check(
                contact,
                id,
                title,
                photoUri,
                fame,
                likeCount,
                sumLikeCount
        );
    }

    @Test
    @Override
    public void testSetId () {
        final String title = "testTitle";

        final Contact contact = new Contact(title);

        final Long newId = 13L;

        contact.setId(newId);

        check(
                contact,
                newId,
                title,
                DEFAULT_PHOTO_URI,
                DEFAULT_FAME,
                DEFAULT_LIKE_COUNT,
                DEFAULT_SUM_LIKE_COUNT
        );
    }

    @Test
    public void testSetTitle () {
        final String title = "testTitle";

        final Contact contact = new Contact(title);

        final String newTitle = "newTitle";

        contact.setTitle(newTitle);

        check(
                contact,
                getDefaultId(),
                newTitle,
                DEFAULT_PHOTO_URI,
                DEFAULT_FAME,
                DEFAULT_LIKE_COUNT,
                DEFAULT_SUM_LIKE_COUNT
        );
    }

    @Test
    public void testSetPhotoUri () {
        final String title = "testTitle";

        final Contact contact = new Contact(title);

        final String newPhotoUri = "newPhotoUri";

        contact.setPhotoUri(newPhotoUri);

        check(
                contact,
                getDefaultId(),
                title,
                newPhotoUri,
                DEFAULT_FAME,
                DEFAULT_LIKE_COUNT,
                DEFAULT_SUM_LIKE_COUNT
        );
    }

    @Test
    public void testSetFame () {
        final String title = "testTitle";

        final Contact contact = new Contact(title);

        final Long newFame = 123L;

        contact.setFame(newFame);

        check(
                contact,
                getDefaultId(),
                title,
                DEFAULT_PHOTO_URI,
                newFame,
                DEFAULT_LIKE_COUNT,
                DEFAULT_SUM_LIKE_COUNT
        );
    }

    @Test
    public void testSetLikeCount () {
        final String title = "testTitle";

        final Contact contact = new Contact(title);

        final Long newLikeCount = 123L;

        contact.setLikeCount(newLikeCount);

        check(
                contact,
                getDefaultId(),
                title,
                DEFAULT_PHOTO_URI,
                DEFAULT_FAME,
                newLikeCount,
                DEFAULT_SUM_LIKE_COUNT
        );
    }

    @Test
    public void testSetSumLikeCount () {
        final String title = "testTitle";

        final Contact contact = new Contact(title);

        final Long newSumLikeCount = 123L;

        contact.setSumLikeCount(newSumLikeCount);

        check(
                contact,
                getDefaultId(),
                title,
                DEFAULT_PHOTO_URI,
                DEFAULT_FAME,
                DEFAULT_LIKE_COUNT,
                newSumLikeCount
        );
    }

    @Test
    public void testEquals () {
        final Contact contact1 = new Contact(1L, "title 1", "photoUri 1", 1L, 0L, 0L);
        final Contact contact2 = new Contact(1L, "title 1", "photoUri 1", 1L, 0L, 0L);
        final Contact contact3 = new Contact(1L, "title 1", "photoUri 1", 1L, 0L, 0L);
        final Contact contact4 = new Contact(2L, "title 1", "photoUri 1", 1L, 0L, 0L);
        final Contact contact5 = new Contact(1L, "title 2", "photoUri 1", 1L, 0L, 0L);
        final Contact contact7 = new Contact(1L, "title 1", "photoUri 2", 1L, 0L, 0L);
        final Contact contact8 = new Contact(1L, "title 1", "photoUri 1", 2L, 0L, 0L);
        final Contact contact9 = new Contact(1L, "title 1", "photoUri 1", 1L, 1L, 0L);
        final Contact contact10 = new Contact(1L, "title 1", "photoUri 1", 1L, 0L, 1L);

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
        final Contact contact1 = new Contact(1L, "title 1", "photoUri 1", 1L, 0L, 0L);
        final Contact contact2 = new Contact(1L, "title 1", "photoUri 1", 2L, 0L, 0L);
        final Contact contact3 = new Contact(1L, "title 1", "photoUri 1", 1L, 1L, 0L);

        assertEquals(contact1.hashCode(), contact2.hashCode());
        assertEquals(contact1.hashCode(), contact3.hashCode());
    }

    private static void check (
            @NonNull final Contact contact,
            final Long id,
            final String title,
            final String photoUri,
            final Long fame,
            final Long likeCount,
            final Long sumLikeCount
    ) {
        System.out.print(contact);

        assertEquals(contact.getId(), id);
        assertEquals(contact.getTitle(), title);
        assertEquals(contact.getPhotoUri(), photoUri);
        assertEquals(contact.getFame(), fame);
        assertEquals(contact.getLikeCount(), likeCount);
        assertEquals(contact.getSumLikeCount(), sumLikeCount);
    }
}
