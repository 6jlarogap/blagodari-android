package org.blagodari.db.dao;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.blagodari.TestWithAuthorizedUserAndKeyzTypes;
import org.blagodari.db.addent.ContactWithKeyz;
import org.blagodari.db.scheme.Contact;
import org.blagodari.db.scheme.ContactKeyz;
import org.blagodari.db.scheme.Keyz;
import org.blagodari.db.scheme.KeyzType;
import org.blagodari.db.scheme.Like;
import org.blagodari.db.scheme.LikeKeyz;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

@RunWith (AndroidJUnit4.class)
public class LikeKeyzDaoTest
        extends TestWithAuthorizedUserAndKeyzTypes {

    @Test
    public void testMarkDeletedForKeyzByContacts () {
        //добавить контакты
        Contact contact1 = new Contact("contact1");
        getDatabase().getContactDao().insertAndSetId(contact1);
        Contact contact2 = new Contact("contact2");
        getDatabase().getContactDao().insertAndSetId(contact2);

        //добавить ключи
        Keyz keyz1 = new Keyz("value1", KeyzType.Types.PHONE_NUMBER.getId());
        getDatabase().getKeyzDao().insertAndSetId(keyz1);
        Keyz keyz2 = new Keyz("value2", KeyzType.Types.PHONE_NUMBER.getId());
        getDatabase().getKeyzDao().insertAndSetId(keyz2);
        Keyz keyz3 = new Keyz("value3", KeyzType.Types.PHONE_NUMBER.getId());
        getDatabase().getKeyzDao().insertAndSetId(keyz3);
        Keyz keyz4 = new Keyz("value4", KeyzType.Types.PHONE_NUMBER.getId());
        getDatabase().getKeyzDao().insertAndSetId(keyz4);

        //связать контакты с ключами
        ContactKeyz contactKeyz1 = new ContactKeyz(contact1.getId(), keyz1.getId());
        getDatabase().getContactKeyzDao().insertAndSetId(contactKeyz1);
        ContactKeyz contactKeyz2 = new ContactKeyz(contact2.getId(), keyz3.getId());
        getDatabase().getContactKeyzDao().insertAndSetId(contactKeyz2);

        //добавить благодарности
        Like like1 = new Like(getUser().getId(), System.currentTimeMillis());
        like1.setContactId(contact1.getId());
        getDatabase().getLikeDao().insertAndSetId(like1);
        Like like2 = new Like(getUser().getId(), System.currentTimeMillis());
        like2.setContactId(contact2.getId());
        getDatabase().getLikeDao().insertAndSetId(like2);
        Like like3 = new Like(getUser().getId(), System.currentTimeMillis());
        like3.setContactId(contact2.getId());
        getDatabase().getLikeDao().insertAndSetId(like3);

        //связать благодарности с ключами
        LikeKeyz likeKeyz1 = new LikeKeyz(like1.getId(), keyz1.getId());
        getDatabase().getLikeKeyzDao().insertAndSetId(likeKeyz1);
        LikeKeyz likeKeyz2 = new LikeKeyz(like1.getId(), keyz2.getId());
        getDatabase().getLikeKeyzDao().insertAndSetId(likeKeyz2);
        LikeKeyz likeKeyz3 = new LikeKeyz(like2.getId(), keyz3.getId());
        getDatabase().getLikeKeyzDao().insertAndSetId(likeKeyz3);
        LikeKeyz likeKeyz4 = new LikeKeyz(like2.getId(), keyz4.getId());
        getDatabase().getLikeKeyzDao().insertAndSetId(likeKeyz4);
        LikeKeyz likeKeyz5 = new LikeKeyz(like3.getId(), keyz3.getId());
        getDatabase().getLikeKeyzDao().insertAndSetId(likeKeyz5);
        LikeKeyz likeKeyz6 = new LikeKeyz(like3.getId(), keyz4.getId());
        getDatabase().getLikeKeyzDao().insertAndSetId(likeKeyz6);

        List<Contact> contactList = new ArrayList<>();
        contactList.add(contact1);
        contactList.add(contact2);

        //пометить для удаления
        getDatabase().getLikeKeyzDao().markDeleteByContactIds(ContactWithKeyz.extractContactIds(contactList));

        //сверить
        LikeKeyz dbLikeKeyz1 = getDatabase().getLikeKeyzDao().get(likeKeyz1.getId());
        assertFalse(dbLikeKeyz1.getDeleted());
        LikeKeyz dbLikeKeyz2 = getDatabase().getLikeKeyzDao().get(likeKeyz2.getId());
        assertTrue(dbLikeKeyz2.getDeleted());
        LikeKeyz dbLikeKeyz3 = getDatabase().getLikeKeyzDao().get(likeKeyz3.getId());
        assertFalse(dbLikeKeyz3.getDeleted());
        LikeKeyz dbLikeKeyz4 = getDatabase().getLikeKeyzDao().get(likeKeyz4.getId());
        assertTrue(dbLikeKeyz4.getDeleted());
        LikeKeyz dbLikeKeyz5 = getDatabase().getLikeKeyzDao().get(likeKeyz5.getId());
        assertFalse(dbLikeKeyz5.getDeleted());
        LikeKeyz dbLikeKeyz6 = getDatabase().getLikeKeyzDao().get(likeKeyz6.getId());
        assertTrue(dbLikeKeyz6.getDeleted());
    }
}
