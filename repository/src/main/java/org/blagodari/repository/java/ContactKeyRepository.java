package org.blagodari.repository.java;

import androidx.annotation.NonNull;

final class ContactKeyRepository
        extends BaseRepository<ContactKey, ContactKeyDao> {

    private static volatile ContactKeyRepository INSTANCE;

    private ContactKeyRepository (@NonNull final ContactKeyDao dao) {
        super(dao);
    }

    static ContactKeyRepository getInstance (@NonNull final ContactKeyDao dao) {
        synchronized (ContactKeyRepository.class) {
            if (INSTANCE == null) {
                INSTANCE = new ContactKeyRepository(dao);
            }
        }
        return INSTANCE;
    }

    static ContactKey create (
            @NonNull final Long contactId,
            @NonNull final Long keyId
    ) {
        return new ContactKey(contactId, keyId);
    }

    final ContactKey createAndInsert(
            @NonNull final Long contactId,
            @NonNull final Long keyId
    ) {
        final ContactKey contactKey = create(contactId, keyId);
        insert(contactKey);
        return contactKey;
    }

    final ContactKey createAndInsertAndSetId(
            @NonNull final Long contactId,
            @NonNull final Long keyId
    ) {
        final ContactKey contactKey = create(contactId, keyId);
        insertAndSetId(contactKey);
        return contactKey;
    }
}
