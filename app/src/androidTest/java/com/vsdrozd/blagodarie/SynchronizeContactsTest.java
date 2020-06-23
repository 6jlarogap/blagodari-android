package com.vsdrozd.blagodarie;

import com.vsdrozd.blagodarie.db.addent.ContactWithKeyz;
import com.vsdrozd.blagodarie.db.scheme.Contact;
import com.vsdrozd.blagodarie.db.scheme.Keyz;
import com.vsdrozd.blagodarie.db.scheme.Like;
import com.vsdrozd.blagodarie.db.scheme.LikeKeyz;
import com.vsdrozd.blagodarie.db.scheme.UserContact;
import com.vsdrozd.blagodarie.db.scheme.UserKeyz;

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class SynchronizeContactsTest
        extends TestWithAuthorizedUserAndKeyzTypes {

    private DataRepository mDataRepository;

    @Before
    public void createDataRepository () {
        this.mDataRepository = new DataRepository(getDatabase());
    }

    @Test
    public void allEmptyTest () {
        List<ContactWithKeyz> contactWithKeyzForInsert = new ArrayList<>();
        List<ContactWithKeyz> contactWithKeyzForUpdate = new ArrayList<>();
        List<ContactWithKeyz> contactWithKeyzForDelete = new ArrayList<>();

        long startTime = System.currentTimeMillis();
        this.mDataRepository.synchronizeContacts(
                getUser().getId(),
                contactWithKeyzForInsert,
                contactWithKeyzForUpdate,
                contactWithKeyzForDelete
        );
        long execTime = System.currentTimeMillis() - startTime;
        System.out.println(String.format("allEmptyTest time = %d ms", execTime));

        List<Contact> dbContacts = getDatabase().getContactDao().getAll();
        assertEquals(dbContacts.size(), 0);
    }

    @Test
    public void insertOnlyTest () {
        final int contactWithKeyzForInsertCount = 5000;
        final int maxKeyzCount = 5;
        final RandContactWithKeyz randContactWithKeyz = new RandContactWithKeyz();
        final List<ContactWithKeyz> contactWithKeyzForInsert = randContactWithKeyz.getList(contactWithKeyzForInsertCount, maxKeyzCount);
        final int keyzCount = ContactWithKeyz.extractKeyzList(contactWithKeyzForInsert).size();
        System.out.println("keyzCount = " + keyzCount);

        final List<ContactWithKeyz> contactWithKeyzForUpdate = new ArrayList<>();
        final List<ContactWithKeyz> contactWithKeyzForDelete = new ArrayList<>();

        long startTime = System.currentTimeMillis();
        this.mDataRepository.synchronizeContacts(
                getUser().getId(),
                contactWithKeyzForInsert,
                contactWithKeyzForUpdate,
                contactWithKeyzForDelete
        );
        long execTime = System.currentTimeMillis() - startTime;
        System.out.println(String.format("insertOnlyTest time = %d ms", execTime));

        //сверить количество контактов
        final List<Contact> dbContacts = getDatabase().getContactDao().getAll();
        assertEquals(dbContacts.size(), contactWithKeyzForInsertCount);

        //сверить количество связей пользователь-контакт
        final List<UserContact> dbUserContacts = getDatabase().getUserContactDao().getByUserId(getUser().getId());
        assertEquals(dbUserContacts.size(), contactWithKeyzForInsertCount);

        //сверить количество ключей
        final List<Keyz> dbKeyz = getDatabase().getKeyzDao().getByUserId(getUser().getId());
        assertEquals(dbKeyz.size(), keyzCount);

        //сверить количество связей пользователь-ключ
        final List<UserKeyz> dbUserKeyz = getDatabase().getUserKeyzDao().getNotDeletedByUserId(getUser().getId());
        assertEquals(dbUserKeyz.size(), keyzCount);

    }

    @Test
    public void insertAndDeleteContactsAndKeyzTest () {
        final int contactWithKeyzForInsertCount = 5000;
        final int maxKeyzCount = 5;
        final int contactWithKeyzForDeleteCount = 100;
        final RandContactWithKeyz randContactWithKeyz = new RandContactWithKeyz();
        final List<ContactWithKeyz> contactWithKeyzForInsert = randContactWithKeyz.getList(contactWithKeyzForInsertCount, maxKeyzCount);
        final int insertKeyzCount = ContactWithKeyz.extractKeyzList(contactWithKeyzForInsert).size();
        System.out.println("insertKeyzCount = " + insertKeyzCount);

        final List<ContactWithKeyz> contactWithKeyzForUpdate = new ArrayList<>();
        List<ContactWithKeyz> contactWithKeyzForDelete = new ArrayList<>();

        //синхронизировать
        this.mDataRepository.synchronizeContacts(
                getUser().getId(),
                contactWithKeyzForInsert,
                contactWithKeyzForUpdate,
                contactWithKeyzForDelete
        );

        //создать список для удаления
        contactWithKeyzForDelete = randContactWithKeyz.getRandSublistFromList(contactWithKeyzForInsert, contactWithKeyzForDeleteCount);
        final int deleteKeyzCount = ContactWithKeyz.extractKeyzList(contactWithKeyzForDelete).size();
        //очистить список для вставки
        contactWithKeyzForInsert.clear();
        ;

        //синхронизировать
        long startTime = System.currentTimeMillis();
        this.mDataRepository.synchronizeContacts(
                getUser().getId(),
                contactWithKeyzForInsert,
                contactWithKeyzForUpdate,
                contactWithKeyzForDelete
        );
        long execTime = System.currentTimeMillis() - startTime;
        System.out.println(String.format("insertAndDeleteContactsAndKeyzTest time = %d ms", execTime));


        //сверить количество контактов
        final List<Contact> dbContacts = getDatabase().getContactDao().getAll();
        assertEquals(dbContacts.size(), contactWithKeyzForInsertCount - contactWithKeyzForDeleteCount);

        //сверить количество связей пользователь-контакт
        final List<UserContact> dbUserContacts = getDatabase().getUserContactDao().getByUserId(getUser().getId());
        assertEquals(dbUserContacts.size(), contactWithKeyzForInsertCount - contactWithKeyzForDeleteCount);

        //сверить количество ключей
        final List<Keyz> dbKeyz = getDatabase().getKeyzDao().getByUserId(getUser().getId());
        assertEquals(dbKeyz.size(), insertKeyzCount);

        //сверить количество связей пользователь-ключ
        final List<UserKeyz> dbUserKeyz = getDatabase().getUserKeyzDao().getNotDeletedByUserId(getUser().getId());
        assertEquals(dbUserKeyz.size(), insertKeyzCount - deleteKeyzCount);

    }

    @Test
    public void insertAndDeleteLikesTest () {
        final int contactWithKeyzForInsertCount = 500;
        final int maxKeyzCount = 5;
        final int contactWithKeyzForDeleteCount = 100;
        final RandContactWithKeyz randContactWithKeyz = new RandContactWithKeyz();
        final List<ContactWithKeyz> contactWithKeyzForInsert = randContactWithKeyz.getList(contactWithKeyzForInsertCount, maxKeyzCount);
        final int insertKeyzCount = ContactWithKeyz.extractKeyzList(contactWithKeyzForInsert).size();
        System.out.println("insertKeyzCount = " + insertKeyzCount);

        final List<ContactWithKeyz> contactWithKeyzForUpdate = new ArrayList<>();
        List<ContactWithKeyz> contactWithKeyzForDelete = new ArrayList<>();

        //синхронизировать
        this.mDataRepository.synchronizeContacts(
                getUser().getId(),
                contactWithKeyzForInsert,
                contactWithKeyzForUpdate,
                contactWithKeyzForDelete
        );

        //создать список для удаления
        contactWithKeyzForDelete = randContactWithKeyz.getRandSublistFromList(contactWithKeyzForInsert, contactWithKeyzForDeleteCount);
        final int deleteKeyzCount = ContactWithKeyz.extractKeyzList(contactWithKeyzForDelete).size();
        //для каждого контакта создать благодарности
        final int likeForContactCount = 2;
        for (ContactWithKeyz contactWithKeyz : contactWithKeyzForInsert) {
            for (int i = 0; i < likeForContactCount; i++) {
                this.mDataRepository.createLikeForContact(getUser().getId(), contactWithKeyz.getContact().getId(),System.currentTimeMillis());
            }
        }
        //очистить список для вставки
        contactWithKeyzForInsert.clear();

        //синхронизировать
        long startTime = System.currentTimeMillis();
        this.mDataRepository.synchronizeContacts(
                getUser().getId(),
                contactWithKeyzForInsert,
                contactWithKeyzForUpdate,
                contactWithKeyzForDelete
        );
        long execTime = System.currentTimeMillis() - startTime;
        System.out.println(String.format("insertAndDeleteLikesTest time = %d ms", execTime));


        //сверить количество благодарностей
        final List<Like> dbLikes = getDatabase().getLikeDao().getNotDeletedByOwnerId(getUser().getId());
        assertEquals(dbLikes.size(), ((contactWithKeyzForInsertCount * likeForContactCount) - (contactWithKeyzForDeleteCount * likeForContactCount)));

        //сверить количество связей благодарность ключ
        final List<LikeKeyz> dbLikeKeyz = getDatabase().getLikeKeyzDao().getNotDeleted();
        assertEquals(dbLikeKeyz.size(), ((insertKeyzCount * likeForContactCount) - (deleteKeyzCount * likeForContactCount)));

    }
}
