package org.blagodari;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.runner.RunWith;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@RunWith (AndroidJUnit4.class)
public class AddContactsTest
        extends TestWithAuthorizedUserAndKeyzTypes {
/*
    private DataRepository mDataRepository;
    private List<ContactWithKeyz> mContactsWithKeyzList = new ArrayList<>();

    public AddContactsTest () {
        final int contactsCount = 1000;
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
    public void testInsertContacts () {
        //извлечь список контактов
        List<Contact> contactsForInsert = ContactWithKeyz.extractContactList(this.mContactsWithKeyzList);

        //вставить контакты
        mDataRepository.insertContacts(contactsForInsert);

        //извлечь контакты из БД
        List<Contact> dbContacts = getDatabase().getContactDao().getAll();

        //сверить
        assertNotNull(dbContacts);
        assertEquals(dbContacts.size(), contactsForInsert.size());

    }

    @Test
    public void testInsertKeyzWithoutExistsKeyz () {
        //извлечь список ключей
        List<Keyz> keyzForInsert = ContactWithKeyz.extractKeyzList(this.mContactsWithKeyzList);

        //вставить ключи
        mDataRepository.insertKeyz(keyzForInsert);

        //извлечь контакты из БД
        List<Keyz> dbKeyz = getDatabase().getKeyzDao().getAll();

        //сверить
        assertNotNull(dbKeyz);
        assertEquals(dbKeyz.size(), keyzForInsert.size() + 1);
    }

    @Test
    public void testInsertKeyzWithExistsKeyz () {
        //извлечь список ключей
        List<Keyz> keyzForInsert = ContactWithKeyz.extractKeyzList(this.mContactsWithKeyzList);

        //заранее вставляем ключи
        getDatabase().getKeyzDao().insertAndSetId(keyzForInsert.get(0));
        getDatabase().getKeyzDao().insertAndSetId(keyzForInsert.get(1));

        //вставить ключи
        mDataRepository.insertKeyz(keyzForInsert);

        //извлечь контакты из БД
        List<Keyz> dbKeyz = getDatabase().getKeyzDao().getAll();

        //сверить
        assertNotNull(dbKeyz);
        assertEquals(dbKeyz.size(), keyzForInsert.size() + 1);
    }

    @Test
    public void testRelateContactsToKeyz () {
        //извлечь список контактов
        List<Contact> contactsForInsert = ContactWithKeyz.extractContactList(this.mContactsWithKeyzList);
        //извлечь список ключей
        List<Keyz> keyzForInsert = ContactWithKeyz.extractKeyzList(this.mContactsWithKeyzList);

        //вставить контакты
        mDataRepository.insertContacts(contactsForInsert);
        //вставить ключи
        mDataRepository.insertKeyz(keyzForInsert);

        //связать ключи с контактами
        mDataRepository.relateContactsToKeyz(this.mContactsWithKeyzList);

        //извлечь связи контакт-ключ из БД
        List<ContactKeyz> dbContactKeyz = getDatabase().getContactKeyzDao().getAll();

        //сверить
        assertNotNull(dbContactKeyz);
        assertEquals(dbContactKeyz.size(), keyzForInsert.size());
    }

    @Test
    public void testRelateUserToContacts () {
        //извлечь список контактов
        List<Contact> contactsForInsert = ContactWithKeyz.extractContactList(this.mContactsWithKeyzList);

        //вставить контакты
        mDataRepository.insertContacts(contactsForInsert);
        //связать пользователя с контактами
        mDataRepository.relateUserToContacts(getUser().getId(), contactsForInsert);

        //извлечь связи пользователь-контакт из БД
        List<UserContact> dbUserContact = getDatabase().getUserContactDao().getAll();

        //сверить
        assertNotNull(dbUserContact);
        assertEquals(dbUserContact.size(), contactsForInsert.size());
    }

    @Test
    public void testCreateAndInsertUserKeyz () {
        //извлечь список ключей
        List<Keyz> keyzForInsert = ContactWithKeyz.extractKeyzList(this.mContactsWithKeyzList);

        //вставить ключи
        getDatabase().getKeyzDao().insertAndSetIds(keyzForInsert);

        //извлечь идентификаторы ключей
        List<Long> keyzIds = ContactWithKeyz.extractKeyzIds(keyzForInsert);

        //связать пользователя с ключами
        mDataRepository.relateUserToKeyz(getUser().getId(), keyzIds);

        //извлечь связи из БД
        List<UserKeyz> dbUserKeyz = getDatabase().getUserKeyzDao().getAll();

        //сверить
        assertNotNull(dbUserKeyz);
        assertEquals(dbUserKeyz.size(), keyzForInsert.size());
    }

    @Test
    public void testRelateUserToKeyzWithoutExists () {
        //извлечь список ключей
        List<Keyz> keyzForInsert = ContactWithKeyz.extractKeyzList(this.mContactsWithKeyzList);

        //вставить ключи
        getDatabase().getKeyzDao().insertAndSetIds(keyzForInsert);

        //извлечь идентификаторы ключей
        List<Long> keyzIds = ContactWithKeyz.extractKeyzIds(keyzForInsert);

        //связать пользователя с ключами
        mDataRepository.relateUserToKeyz(getUser().getId(), keyzIds);

        //извлечь связи из БД
        List<UserKeyz> dbUserKeyz = getDatabase().getUserKeyzDao().getAll();

        //сверить
        assertNotNull(dbUserKeyz);
        assertEquals(dbUserKeyz.size(), keyzForInsert.size());
    }

    @Test
    public void testRelateUserToKeyzWithExists () {
        //извлечь список ключей
        List<Keyz> keyzForInsert = ContactWithKeyz.extractKeyzList(this.mContactsWithKeyzList);

        //вставить ключи
        getDatabase().getKeyzDao().insertAndSetIds(keyzForInsert);

        //извлечь идентификаторы ключей
        List<Long> keyzIds = ContactWithKeyz.extractKeyzIds(keyzForInsert);

        //половину ключей связывать заранее и деактуализировать связи
        List<Keyz> keyzForRelate = keyzForInsert.subList(0, keyzForInsert.size() / 2);
        List<UserKeyz> userKeyzList = new ArrayList<>();
        for (Keyz keyz : keyzForRelate) {
            UserKeyz userKeyz = new UserKeyz(getUser().getId(), keyz.getId());
            userKeyzList.add(userKeyz);
        }
        getDatabase().getUserKeyzDao().insertAndSetIds(userKeyzList);

        //связать пользователя с ключами
        mDataRepository.relateUserToKeyz(getUser().getId(), keyzIds);

        //извлечь связи из БД
        List<UserKeyz> dbUserKeyz = getDatabase().getUserKeyzDao().getAll();

        //сверить
        assertNotNull(dbUserKeyz);
        assertEquals(dbUserKeyz.size(), keyzForInsert.size());
    }

    @Test
    public void testInsertContactsForUser () {
        //извлечь список ключей
        List<Keyz> keyzForInsert = ContactWithKeyz.extractKeyzList(this.mContactsWithKeyzList);

        //вставить контакты
        this.mDataRepository.insertContactsForUser(getUser().getId(), this.mContactsWithKeyzList);

        //получить контакты из БД
        List<Contact> dbContacts = getDatabase().getContactDao().getByUser(getUser().getId());

        //сверить
        assertNotNull(dbContacts);
        assertEquals(dbContacts.size(), this.mContactsWithKeyzList.size());

        //получить ключи из бд
        List<Keyz> dbKeyz = getDatabase().getKeyzDao().getByUserId(getUser().getId());

        //сверить
        assertNotNull(dbKeyz);
        assertEquals(dbKeyz.size(), keyzForInsert.size());

        //для каждого контакта
        for (ContactWithKeyz contactWithKeyz : this.mContactsWithKeyzList) {
            //проверить, что проставился идентификатор
            assertNotNull(contactWithKeyz.getContact().getId());
            //проверить, что ключам проставился идентификатор
            for (Keyz keyz : contactWithKeyz.getKeyzList()) {
                assertNotNull(keyz.getId());
            }

            //получить его ключи из БД
            List<Keyz> dbKeyzForContact = getDatabase().getKeyzDao().getByContactId(contactWithKeyz.getContact().getId());

            //сверить
            assertNotNull(dbKeyzForContact);
            assertEquals(dbKeyzForContact.size(), contactWithKeyz.getKeyzList().size());
        }


    }*/
}
