package com.vsdrozd.blagodarie.db.dao;

import com.vsdrozd.blagodarie.db.addent.ContactWithKeyz;
import com.vsdrozd.blagodarie.db.scheme.Contact;
import com.vsdrozd.blagodarie.db.scheme.ContactKeyz;
import com.vsdrozd.blagodarie.db.scheme.UserContact;
import com.vsdrozd.blagodarie.db.scheme.Keyz;
import com.vsdrozd.blagodarie.db.scheme.KeyzType;
import com.vsdrozd.blagodarie.db.scheme.User;

import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class ContactDaoTest
        extends DaoTest {

    private ContactDao contactDao;

    @Override
    public void createDao () {
        contactDao = getDatabase().getContactDao();
    }

    @Test
    public void getForUserTest () {
        //вставить тип ключа
        KeyzType keyzType = new KeyzType(1L, "testType");
        getDatabase().getKeyzTypeDao().insertAndSetId(keyzType);

        //вставить пользователей
        User user1 = new User();
        getDatabase().getUserDao().insertAndSetId(user1);

        User user2 = new User();
        getDatabase().getUserDao().insertAndSetId(user2);

        //вставить контакты
        Contact contact1 = new Contact("contact1");
        contactDao.insertAndSetId(contact1);

        Contact contact2 = new Contact("contact2");
        contactDao.insertAndSetId(contact2);

        Contact contact3 = new Contact("contact3");
        contactDao.insertAndSetId(contact3);

        Contact contact4 = new Contact("contact4");
        contactDao.insertAndSetId(contact4);

        //связать пользователей и контакты
        UserContact userContact1 = new UserContact(user1.getId(), contact1.getId());
        getDatabase().getUserContactDao().insertAndSetId(userContact1);

        UserContact userContact2 = new UserContact(user1.getId(), contact2.getId());
        getDatabase().getUserContactDao().insertAndSetId(userContact2);

        UserContact userContact3 = new UserContact(user2.getId(), contact3.getId());
        getDatabase().getUserContactDao().insertAndSetId(userContact3);

        UserContact userContact4 = new UserContact(user2.getId(), contact4.getId());
        getDatabase().getUserContactDao().insertAndSetId(userContact4);

        //вставить ключи
        Keyz keyz1 = new Keyz("keyz1", keyzType.getId());
        getDatabase().getKeyzDao().insertAndSetId(keyz1);

        Keyz keyz2 = new Keyz("keyz2", keyzType.getId());
        getDatabase().getKeyzDao().insertAndSetId(keyz2);

        Keyz keyz3 = new Keyz("keyz3", keyzType.getId());
        getDatabase().getKeyzDao().insertAndSetId(keyz3);

        Keyz keyz4 = new Keyz("keyz4", keyzType.getId());
        getDatabase().getKeyzDao().insertAndSetId(keyz4);

        Keyz keyz5 = new Keyz("keyz5", keyzType.getId());
        getDatabase().getKeyzDao().insertAndSetId(keyz5);

        Keyz keyz6 = new Keyz("keyz6", keyzType.getId());
        getDatabase().getKeyzDao().insertAndSetId(keyz6);

        Keyz keyz7 = new Keyz("keyz7", keyzType.getId());
        getDatabase().getKeyzDao().insertAndSetId(keyz7);

        Keyz keyz8 = new Keyz("keyz8", keyzType.getId());
        getDatabase().getKeyzDao().insertAndSetId(keyz8);

        //связать ключи и контакты
        ContactKeyz contactKeyz1 = new ContactKeyz(contact1.getId(), keyz1.getId());
        getDatabase().getContactKeyzDao().insertAndSetId(contactKeyz1);

        ContactKeyz contactKeyz2 = new ContactKeyz(contact1.getId(), keyz2.getId());
        getDatabase().getContactKeyzDao().insertAndSetId(contactKeyz2);

        ContactKeyz contactKeyz3 = new ContactKeyz(contact2.getId(), keyz3.getId());
        getDatabase().getContactKeyzDao().insertAndSetId(contactKeyz3);

        ContactKeyz contactKeyz4 = new ContactKeyz(contact2.getId(), keyz4.getId());
        getDatabase().getContactKeyzDao().insertAndSetId(contactKeyz4);

        ContactKeyz contactKeyz5 = new ContactKeyz(contact3.getId(), keyz5.getId());
        getDatabase().getContactKeyzDao().insertAndSetId(contactKeyz5);

        ContactKeyz contactKeyz6 = new ContactKeyz(contact3.getId(), keyz6.getId());
        getDatabase().getContactKeyzDao().insertAndSetId(contactKeyz6);

        ContactKeyz contactKeyz7 = new ContactKeyz(contact4.getId(), keyz7.getId());
        getDatabase().getContactKeyzDao().insertAndSetId(contactKeyz7);

        ContactKeyz contactKeyz8 = new ContactKeyz(contact4.getId(), keyz8.getId());
        getDatabase().getContactKeyzDao().insertAndSetId(contactKeyz8);

        List<ContactWithKeyz> contactWithKeyzs1 = contactDao.getContactsWithKeyzByUser(user1.getId());
        List<ContactWithKeyz> contactWithKeyzs2 = contactDao.getContactsWithKeyzByUser(user2.getId());

        assertEquals(contactWithKeyzs1.size(), 2);
        assertEquals(contactWithKeyzs2.size(), 2);
        assertEquals(contactWithKeyzs1.get(0).getKeyzList().size(), 2);
        assertEquals(contactWithKeyzs2.get(1).getKeyzList().size(), 2);
    }

}
