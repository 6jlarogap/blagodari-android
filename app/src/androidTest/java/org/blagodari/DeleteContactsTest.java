package org.blagodari;

import org.blagodari.db.addent.ContactWithKeyz;
import org.blagodari.db.scheme.Contact;
import org.blagodari.db.scheme.ContactKeyz;
import org.blagodari.db.scheme.Keyz;
import org.blagodari.db.scheme.KeyzType;
import org.blagodari.db.scheme.Like;
import org.blagodari.db.scheme.LikeKeyz;
import org.blagodari.db.scheme.UserContact;
import org.blagodari.db.scheme.UserKeyz;

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class DeleteContactsTest
        extends TestWithAuthorizedUserAndKeyzTypes {

    private DataRepository mDataRepository;
    private List<ContactWithKeyz> mContactsWithKeyzList = new ArrayList<>();

    public DeleteContactsTest () {
        final int contactsCount = 2000;
        //создать контакты
        for (int i = 0; i < contactsCount; i++) {
            Contact contact = new Contact("contact " + i);
            List<Keyz> keyz = new ArrayList<>();
            keyz.add(new Keyz("phone " + i, KeyzType.Types.PHONE_NUMBER.getId()));
            keyz.add(new Keyz("email " + i, KeyzType.Types.EMAIL.getId()));
            this.mContactsWithKeyzList.add(new ContactWithKeyz(contact, keyz));
        }
    }

    @Before
    public void createDataRepository () {
        this.mDataRepository = new DataRepository(getDatabase());
    }

    @Test
    public void testDeleteContacts () {
        //вставить контакты
        insertContacts();
        //вставить ключи
        insertKeyz();
        //связать пользователя с контактами
        insertUserContacts();
        //связать пользователя с ключами
        insertUserKeyz();
        //вставить благодарности и связать с ключами
        insertLikeAndLikeKeyz();

        //удалить контакты
        this.mDataRepository.deleteContacts(getUser().getId(), this.mContactsWithKeyzList);

        //извлечь из БД и сверить
        List<Contact> dbContacts = getDatabase().getContactDao().getAll();
        assertNotNull(dbContacts);
        assertEquals(dbContacts.size(), 0L);

        List<ContactKeyz> dbContactKeyz = getDatabase().getContactKeyzDao().getAll();
        assertNotNull(dbContactKeyz);
        assertEquals(dbContactKeyz.size(), 0L);

        List<UserKeyz> dbUserKeyz = getDatabase().getUserKeyzDao().getAll();
        assertNotNull(dbUserKeyz);
        List<Keyz> keyzList = ContactWithKeyz.extractKeyzList(this.mContactsWithKeyzList);
        assertEquals(dbUserKeyz.size(), keyzList.size());
        for (UserKeyz userKeyz : dbUserKeyz) {
            assertTrue(userKeyz.getDeleted());
        }

        List<UserContact> dbUserContacts = getDatabase().getUserContactDao().getAll();
        assertNotNull(dbUserContacts);
        assertEquals(dbUserContacts.size(), 0L);

        List<Like> dbLikes = getDatabase().getLikeDao().getAll();
        assertNotNull(dbLikes);
        assertEquals(dbLikes.size(), this.mContactsWithKeyzList.size());
        for (Like like : dbLikes) {
            assertNull(like.getContactId());
            assertTrue(like.getDeleted());
        }

        List<LikeKeyz> dbLikeKeyz = getDatabase().getLikeKeyzDao().getAll();
        assertNotNull(dbLikeKeyz);
        assertEquals(dbLikeKeyz.size(), keyzList.size());
        for (LikeKeyz likeKeyz : dbLikeKeyz) {
            assertTrue(likeKeyz.getDeleted());
        }
    }

    private void insertLikeAndLikeKeyz () {
        List<LikeKeyz> likeKeyzList = new ArrayList<>();
        for (ContactWithKeyz contactWithKeyz : this.mContactsWithKeyzList) {
            Like like = new Like(getUser().getId(), System.currentTimeMillis());
            like.setContactId(contactWithKeyz.getContact().getId());
            getDatabase().getLikeDao().insertAndSetId(like);
            for (Keyz keyz : contactWithKeyz.getKeyzList()) {
                likeKeyzList.add(new LikeKeyz(like.getId(), keyz.getId()));
            }
        }
        getDatabase().getLikeKeyzDao().insertAndSetIds(likeKeyzList);
    }

    private void insertLikes () {
        final List<Like> likeList = new ArrayList<>();
        for (ContactWithKeyz contactWithKeyz : this.mContactsWithKeyzList) {
            final Like like = new Like(getUser().getId(), System.currentTimeMillis());
            like.setContactId(contactWithKeyz.getContact().getId());
            likeList.add(like);

        }
        getDatabase().getLikeDao().insertAndSetIds(likeList);
    }

    private void insertUserKeyz () {
        //связать пользователя с ключами
        final List<UserKeyz> userKeyzList = new ArrayList<>();
        for (ContactWithKeyz contactWithKeyz : this.mContactsWithKeyzList) {
            for (Keyz keyz : contactWithKeyz.getKeyzList()) {
                userKeyzList.add(new UserKeyz(getUser().getId(), keyz.getId()));
            }
        }
        getDatabase().getUserKeyzDao().insertAndSetIds(userKeyzList);
    }

    private void insertUserContacts () {
        //связать пользователя с контактами
        final List<UserContact> userContactsForInsert = new ArrayList<>();
        for (ContactWithKeyz contactWithKeyz : this.mContactsWithKeyzList) {
            userContactsForInsert.add(new UserContact(getUser().getId(), contactWithKeyz.getContact().getId()));
        }
        getDatabase().getUserContactDao().insertAndSetIds(userContactsForInsert);
    }

    private void insertContactKeyz () {
        //связать ключи с контактами
        List<ContactKeyz> contactKeyzList = new ArrayList<>();
        for (ContactWithKeyz contactWithKeyz : this.mContactsWithKeyzList) {
            for (Keyz keyz : contactWithKeyz.getKeyzList()) {
                contactKeyzList.add(new ContactKeyz(contactWithKeyz.getContact().getId(), keyz.getId()));
            }
        }
        getDatabase().getContactKeyzDao().insertAndSetIds(contactKeyzList);
    }

    private void insertContacts () {
        //извлечь список контактов
        List<Contact> contacts = ContactWithKeyz.extractContactList(this.mContactsWithKeyzList);
        //вставить контакты
        getDatabase().getContactDao().insertAndSetIds(contacts);
    }

    private void insertKeyz () {
        //извлечь список ключей
        List<Keyz> keyzList = ContactWithKeyz.extractKeyzList(this.mContactsWithKeyzList);
        //вставить ключи
        getDatabase().getKeyzDao().insertAndSetIds(keyzList);
    }
}
