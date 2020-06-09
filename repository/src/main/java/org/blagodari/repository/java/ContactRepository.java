package org.blagodari.repository.java;

import androidx.annotation.NonNull;

final class ContactRepository
        extends BaseRepository<Contact, ContactDao> {

    private static volatile ContactRepository INSTANCE;

    private ContactRepository (@NonNull final ContactDao dao) {
        super(dao);
    }

    static ContactRepository getInstance (@NonNull final ContactDao dao) {
        synchronized (ContactRepository.class) {
            if (INSTANCE == null) {
                INSTANCE = new ContactRepository(dao);
            }
        }
        return INSTANCE;
    }

    public static Contact create (@NonNull final String title) {
        return new Contact(title);
    }
}
