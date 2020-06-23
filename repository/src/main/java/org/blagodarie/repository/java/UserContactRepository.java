package org.blagodarie.repository.java;

import androidx.annotation.NonNull;

final class UserContactRepository
        extends BaseRepository<UserContact, UserContactDao> {

    private static volatile UserContactRepository INSTANCE;

    private UserContactRepository (@NonNull final UserContactDao dao) {
        super(dao);
    }

    static UserContactRepository getInstance (@NonNull final UserContactDao dao) {
        synchronized (UserContactRepository.class) {
            if (INSTANCE == null) {
                INSTANCE = new UserContactRepository(dao);
            }
        }
        return INSTANCE;
    }

    static UserContact create (
            @NonNull final Long userId,
            @NonNull final Long contactId
    ) {
        return new UserContact(userId, contactId);
    }

    final UserContact createAndInsert(
            @NonNull final Long userId,
            @NonNull final Long contactId
    ) {
        final UserContact userContact = create(userId, contactId);
        insert(userContact);
        return userContact;
    }

    final UserContact createAndInsertAndSetId(
            @NonNull final Long userId,
            @NonNull final Long contactId
    ) {
        final UserContact userContact = create(userId, contactId);
        insertAndSetId(userContact);
        return userContact;
    }
}
