package org.blagodari.contacts;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class CompareAndGroupTest {
/*
    private static final ContactSynchronizer.ProgressListener M_PROGRESS_LISTENER = new ContactSynchronizer.ProgressListener() {
        @Override
        public void onGetData (int index, int count) {
            System.out.println("onGetData");
        }

        @Override
        public void onProcessingContacts (int index, int count, int size) {
            System.out.println(String.format("onProcessingContacts %d of %d", index, count));
        }

        @Override
        public void onDatabaseWrite (int index, int count, int size) {
            System.out.println("onDatabaseWrite");
        }

        @Override
        public void onContactRepositoryWrite (int index, int count, int size) {
            System.out.println("onContactRepositoryWrite");
        }

        @Override
        public void onFinish () {
            System.out.println("onFinish");
        }
    };

    @Test
    public final void noChangeTest () {
        final LongSparseArray<ContactWithKeyz> dbContacts = createContactsLongSparseArray();
        final List<ContactWithKeyz> contentContacts = createContactsList();

        //создать список контактов с ключами для добавления в БД
        final List<ContactWithKeyz> contactsWithKeyzForInsert = new ArrayList<>();
        //создать список контактов с ключами для обновления в БД
        final List<ContactWithKeyz> contactsWithKeyzForUpdate = new ArrayList<>();
        //создать список контактов с ключами для удаления из БД
        final List<ContactWithKeyz> contactsWithKeyzForDelete = new ArrayList<>();
        long startTime = System.currentTimeMillis();
        ContactSynchronizer.groupContacts(
                contentContacts,
                dbContacts,
                M_PROGRESS_LISTENER,
                contactsWithKeyzForInsert,
                contactsWithKeyzForUpdate,
                contactsWithKeyzForDelete
        );
        long execTime = System.currentTimeMillis() - startTime;
        System.out.println(String.format("compare and group time of %d elements - %d ms", contentContacts.size(), execTime));

        assertEquals(contactsWithKeyzForInsert.size(), 0);
        assertEquals(contactsWithKeyzForUpdate.size(), 0);
        assertEquals(contactsWithKeyzForDelete.size(), 0);
    }

    @Test
    public final void addKeyzTest () {
        final LongSparseArray<ContactWithKeyz> dbContacts = createContactsLongSparseArray();
        final List<ContactWithKeyz> contentContacts = createContactsList();

        final ContactWithKeyz changedContactWithKeyz = contentContacts.get(0);
        final Long changedContactId = changedContactWithKeyz.getContact().getId();
        final Keyz newKeyz = new Keyz("new keyz", KeyzType.Types.PHONE_NUMBER.getId());
        changedContactWithKeyz.getKeyzSet().add(newKeyz);

        //создать список контактов с ключами для добавления в БД
        final List<ContactWithKeyz> contactsWithKeyzForInsert = new ArrayList<>();
        //создать список контактов с ключами для обновления в БД
        final List<ContactWithKeyz> contactsWithKeyzForUpdate = new ArrayList<>();
        //создать список контактов с ключами для удаления из БД
        final List<ContactWithKeyz> contactsWithKeyzForDelete = new ArrayList<>();
        long startTime = System.currentTimeMillis();
        ContactSynchronizer.groupContacts(
                contentContacts,
                dbContacts,
                M_PROGRESS_LISTENER,
                contactsWithKeyzForInsert,
                contactsWithKeyzForUpdate,
                contactsWithKeyzForDelete
        );
        long execTime = System.currentTimeMillis() - startTime;
        System.out.println(String.format("compare and group time of %d elements - %d ms", contentContacts.size(), execTime));

        assertEquals(contactsWithKeyzForInsert.size(), 0);
        assertEquals(contactsWithKeyzForUpdate.size(), 1);
        assertEquals(contactsWithKeyzForUpdate.get(0).getContact().getId(), changedContactId);
        assertEquals(contactsWithKeyzForDelete.size(), 0);
    }

    @Test
    public final void removeKeyzTest () {
        final LongSparseArray<ContactWithKeyz> dbContacts = createContactsLongSparseArray();
        final List<ContactWithKeyz> contentContacts = createContactsList();

        final ContactWithKeyz changedContactWithKeyz = contentContacts.get(0);
        final Long changedContactId = changedContactWithKeyz.getContact().getId();
        changedContactWithKeyz.getKeyzList().remove(0);

        //создать список контактов с ключами для добавления в БД
        final List<ContactWithKeyz> contactsWithKeyzForInsert = new ArrayList<>();
        //создать список контактов с ключами для обновления в БД
        final List<ContactWithKeyz> contactsWithKeyzForUpdate = new ArrayList<>();
        //создать список контактов с ключами для удаления из БД
        final List<ContactWithKeyz> contactsWithKeyzForDelete = new ArrayList<>();
        long startTime = System.currentTimeMillis();
        ContactSynchronizer.groupContacts(
                contentContacts,
                dbContacts,
                M_PROGRESS_LISTENER,
                contactsWithKeyzForInsert,
                contactsWithKeyzForUpdate,
                contactsWithKeyzForDelete
        );
        long execTime = System.currentTimeMillis() - startTime;
        System.out.println(String.format("compare and group time of %d elements - %d ms", contentContacts.size(), execTime));

        assertEquals(contactsWithKeyzForInsert.size(), 0);
        assertEquals(contactsWithKeyzForUpdate.size(), 1);
        assertEquals(contactsWithKeyzForUpdate.get(0).getContact().getId(), changedContactId);
        assertEquals(contactsWithKeyzForDelete.size(), 0);
    }

    @Test
    public final void changeKeyzTest () {
        final LongSparseArray<ContactWithKeyz> dbContacts = createContactsLongSparseArray();
        final List<ContactWithKeyz> contentContacts = createContactsList();

        final ContactWithKeyz changedContactWithKeyz = contentContacts.get(0);
        final Long changedContactId = changedContactWithKeyz.getContact().getId();
        final Keyz newKeyz = new Keyz("new keyz", KeyzType.Types.PHONE_NUMBER.getId());
        changedContactWithKeyz.getKeyzList().add(newKeyz);
        changedContactWithKeyz.getKeyzList().remove(0);

        //создать список контактов с ключами для добавления в БД
        final List<ContactWithKeyz> contactsWithKeyzForInsert = new ArrayList<>();
        //создать список контактов с ключами для обновления в БД
        final List<ContactWithKeyz> contactsWithKeyzForUpdate = new ArrayList<>();
        //создать список контактов с ключами для удаления из БД
        final List<ContactWithKeyz> contactsWithKeyzForDelete = new ArrayList<>();
        long startTime = System.currentTimeMillis();
        ContactSynchronizer.groupContacts(
                contentContacts,
                dbContacts,
                M_PROGRESS_LISTENER,
                contactsWithKeyzForInsert,
                contactsWithKeyzForUpdate,
                contactsWithKeyzForDelete
        );
        long execTime = System.currentTimeMillis() - startTime;
        System.out.println(String.format("compare and group time of %d elements - %d ms", contentContacts.size(), execTime));

        assertEquals(contactsWithKeyzForInsert.size(), 0);
        assertEquals(contactsWithKeyzForUpdate.size(), 1);
        assertEquals(contactsWithKeyzForUpdate.get(0).getContact().getId(), changedContactId);
        assertEquals(contactsWithKeyzForDelete.size(), 0);
    }

    @Test
    public final void changeContactTitleTest () {
        final LongSparseArray<ContactWithKeyz> dbContacts = createContactsLongSparseArray();
        final List<ContactWithKeyz> contentContacts = createContactsList();

        final ContactWithKeyz changedContactWithKeyz = contentContacts.get(0);
        final Long changedContactId = changedContactWithKeyz.getContact().getId();
        changedContactWithKeyz.getContact().setTitle("new title");

        //создать список контактов с ключами для добавления в БД
        final List<ContactWithKeyz> contactsWithKeyzForInsert = new ArrayList<>();
        //создать список контактов с ключами для обновления в БД
        final List<ContactWithKeyz> contactsWithKeyzForUpdate = new ArrayList<>();
        //создать список контактов с ключами для удаления из БД
        final List<ContactWithKeyz> contactsWithKeyzForDelete = new ArrayList<>();
        long startTime = System.currentTimeMillis();
        ContactSynchronizer.groupContacts(
                contentContacts,
                dbContacts,
                M_PROGRESS_LISTENER,
                contactsWithKeyzForInsert,
                contactsWithKeyzForUpdate,
                contactsWithKeyzForDelete
        );
        long execTime = System.currentTimeMillis() - startTime;
        System.out.println(String.format("compare and group time of %d elements - %d ms", contentContacts.size(), execTime));

        assertEquals(contactsWithKeyzForInsert.size(), 0);
        assertEquals(contactsWithKeyzForUpdate.size(), 1);
        assertEquals(contactsWithKeyzForUpdate.get(0).getContact().getId(), changedContactId);
        assertEquals(contactsWithKeyzForDelete.size(), 0);
    }

    @Test
    public final void changeContactPhotoTest () {
        final LongSparseArray<ContactWithKeyz> dbContacts = createContactsLongSparseArray();
        final List<ContactWithKeyz> contentContacts = createContactsList();

        final ContactWithKeyz changedContactWithKeyz = contentContacts.get(0);
        final Long changedContactId = changedContactWithKeyz.getContact().getId();
        changedContactWithKeyz.getContact().setPhotoUri("new photoUri");

        //создать список контактов с ключами для добавления в БД
        final List<ContactWithKeyz> contactsWithKeyzForInsert = new ArrayList<>();
        //создать список контактов с ключами для обновления в БД
        final List<ContactWithKeyz> contactsWithKeyzForUpdate = new ArrayList<>();
        //создать список контактов с ключами для удаления из БД
        final List<ContactWithKeyz> contactsWithKeyzForDelete = new ArrayList<>();
        long startTime = System.currentTimeMillis();
        ContactSynchronizer.groupContacts(
                contentContacts,
                dbContacts,
                M_PROGRESS_LISTENER,
                contactsWithKeyzForInsert,
                contactsWithKeyzForUpdate,
                contactsWithKeyzForDelete
        );
        long execTime = System.currentTimeMillis() - startTime;
        System.out.println(String.format("compare and group time of %d elements - %d ms", contentContacts.size(), execTime));

        assertEquals(contactsWithKeyzForInsert.size(), 0);
        assertEquals(contactsWithKeyzForUpdate.size(), 1);
        assertEquals(contactsWithKeyzForUpdate.get(0).getContact().getId(), changedContactId);
        assertEquals(contactsWithKeyzForDelete.size(), 0);
    }

    @Test
    public final void addContactTest () {
        final LongSparseArray<ContactWithKeyz> dbContacts = createContactsLongSparseArray();
        final List<ContactWithKeyz> contentContacts = createContactsList();

        final Contact newContact = new Contact("new contact");
        final List<Keyz> newContactKeyzList = new ArrayList<>();
        newContactKeyzList.add(new Keyz("new phone", KeyzType.Types.PHONE_NUMBER.getId()));
        newContactKeyzList.add(new Keyz("new email", KeyzType.Types.PHONE_NUMBER.getId()));
        final ContactWithKeyz newContactWithKeyz = new ContactWithKeyz(newContact, newContactKeyzList);
        contentContacts.add(newContactWithKeyz);

        //создать список контактов с ключами для добавления в БД
        final List<ContactWithKeyz> contactsWithKeyzForInsert = new ArrayList<>();
        //создать список контактов с ключами для обновления в БД
        final List<ContactWithKeyz> contactsWithKeyzForUpdate = new ArrayList<>();
        //создать список контактов с ключами для удаления из БД
        final List<ContactWithKeyz> contactsWithKeyzForDelete = new ArrayList<>();
        long startTime = System.currentTimeMillis();
        ContactSynchronizer.groupContacts(
                contentContacts,
                dbContacts,
                M_PROGRESS_LISTENER,
                contactsWithKeyzForInsert,
                contactsWithKeyzForUpdate,
                contactsWithKeyzForDelete
        );
        long execTime = System.currentTimeMillis() - startTime;
        System.out.println(String.format("compare and group time of %d elements - %d ms", contentContacts.size(), execTime));

        assertEquals(contactsWithKeyzForInsert.size(), 1);
        assertNull(contactsWithKeyzForInsert.get(0).getContact().getId());
        assertEquals(contactsWithKeyzForInsert.get(0).getKeyzList().size(), 2);
        assertEquals(contactsWithKeyzForUpdate.size(), 0);
        assertEquals(contactsWithKeyzForDelete.size(), 0);
    }

    @Test
    public final void removeContactTest () {
        final LongSparseArray<ContactWithKeyz> dbContacts = createContactsLongSparseArray();
        final List<ContactWithKeyz> contentContacts = createContactsList();

        final ContactWithKeyz removeContactWithKeyz = contentContacts.remove(0);
        final Long removeContactId = removeContactWithKeyz.getContact().getId();

        //создать список контактов с ключами для добавления в БД
        final List<ContactWithKeyz> contactsWithKeyzForInsert = new ArrayList<>();
        //создать список контактов с ключами для обновления в БД
        final List<ContactWithKeyz> contactsWithKeyzForUpdate = new ArrayList<>();
        //создать список контактов с ключами для удаления из БД
        final List<ContactWithKeyz> contactsWithKeyzForDelete = new ArrayList<>();
        long startTime = System.currentTimeMillis();
        ContactSynchronizer.groupContacts(
                contentContacts,
                dbContacts,
                M_PROGRESS_LISTENER,
                contactsWithKeyzForInsert,
                contactsWithKeyzForUpdate,
                contactsWithKeyzForDelete
        );
        long execTime = System.currentTimeMillis() - startTime;
        System.out.println(String.format("compare and group time of %d elements - %d ms", contentContacts.size(), execTime));

        assertEquals(contactsWithKeyzForInsert.size(), 0);
        assertEquals(contactsWithKeyzForUpdate.size(), 0);
        assertEquals(contactsWithKeyzForDelete.size(), 1);
        assertEquals(contactsWithKeyzForDelete.get(0).getContact().getId(), removeContactId);
    }

    private LongSparseArray<ContactWithKeyz> createContactsLongSparseArray () {
        final List<ContactWithKeyz> dbContactsWithKeyz = createContactsList();
        final LongSparseArray<ContactWithKeyz> dbContactWithKeyzMap = new LongSparseArray<>();

        for (ContactWithKeyz contactWithKeyz : dbContactsWithKeyz) {
            dbContactWithKeyzMap.put(contactWithKeyz.getContact().getId(), contactWithKeyz);
        }

        return dbContactWithKeyzMap;
    }

    private List<ContactWithKeyz> createContactsList () {
        final List<ContactWithKeyz> dbContactsWithKeyz = new ArrayList<>();
        final int contactsCount = 100;
        for (int i = 0; i < contactsCount; i++) {
            final Contact contact = new Contact("contact " + i);
            contact.setId((long) i);
            contact.setPhotoUri("photo " + i);

            final List<Keyz> keyzList = new ArrayList<>();
            keyzList.add(new Keyz("phone " + i, KeyzType.Types.PHONE_NUMBER.getId()));
            keyzList.add(new Keyz("email " + i, KeyzType.Types.EMAIL.getId()));

            final ContactWithKeyz contactWithKeyz = new ContactWithKeyz(contact, keyzList);
            dbContactsWithKeyz.add(contactWithKeyz);
        }
        return dbContactsWithKeyz;
    }*/
}
