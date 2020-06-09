package org.blagodari.db.dao;

import org.blagodari.TestWithAuthorizedUserAndKeyzTypes;
import org.blagodari.db.addent.ContactWithKeyz;
import org.blagodari.db.scheme.Contact;
import org.blagodari.db.scheme.ContactKeyz;
import org.blagodari.db.scheme.Keyz;
import org.blagodari.db.scheme.KeyzType;
import org.blagodari.db.scheme.UserContact;
import org.blagodari.db.scheme.UserKeyz;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertNotNull;

public class UserKeyzDaoTest
        extends TestWithAuthorizedUserAndKeyzTypes {

    @Test
    public void testRelateOffFromKeyzByContacts () {
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
        ContactKeyz contactKeyz2 = new ContactKeyz(contact1.getId(), keyz2.getId());
        getDatabase().getContactKeyzDao().insertAndSetId(contactKeyz2);
        ContactKeyz contactKeyz3 = new ContactKeyz(contact2.getId(), keyz3.getId());
        getDatabase().getContactKeyzDao().insertAndSetId(contactKeyz3);
        ContactKeyz contactKeyz4 = new ContactKeyz(contact2.getId(), keyz4.getId());
        getDatabase().getContactKeyzDao().insertAndSetId(contactKeyz4);

        //связать пользователя с контактами
        UserContact userContact1 = new UserContact(getUser().getId(), contact1.getId());
        getDatabase().getUserContactDao().insertAndSetId(userContact1);
        UserContact userContact2 = new UserContact(getUser().getId(), contact2.getId());
        getDatabase().getUserContactDao().insertAndSetId(userContact2);

        //связать пользователя с ключами
        UserKeyz userKeyz1 = new UserKeyz(getUser().getId(), keyz1.getId());
        getDatabase().getUserKeyzDao().insertAndSetId(userKeyz1);
        UserKeyz userKeyz2 = new UserKeyz(getUser().getId(), keyz2.getId());
        getDatabase().getUserKeyzDao().insertAndSetId(userKeyz2);
        UserKeyz userKeyz3 = new UserKeyz(getUser().getId(), keyz3.getId());
        getDatabase().getUserKeyzDao().insertAndSetId(userKeyz3);
        UserKeyz userKeyz4 = new UserKeyz(getUser().getId(), keyz4.getId());
        getDatabase().getUserKeyzDao().insertAndSetId(userKeyz4);

        List<Contact> contactList = new ArrayList<>();
        contactList.add(contact1);
        contactList.add(contact2);

        //отвязать пользователя от ключей через контакты
        getDatabase().getUserKeyzDao().markForDeleteByUserIdAndContactIds(getUser().getId(), ContactWithKeyz.extractContactIds(contactList));;

        List<UserKeyz> dbUserKeyz = getDatabase().getUserKeyzDao().getAll();
        assertNotNull(dbUserKeyz);
    }
}
