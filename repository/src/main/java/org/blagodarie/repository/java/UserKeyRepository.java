package org.blagodarie.repository.java;

import androidx.annotation.NonNull;

final class UserKeyRepository
        extends SynchronizableRepository<UserKey, UserKeyDao> {

    private static volatile UserKeyRepository INSTANCE;

    private UserKeyRepository (@NonNull final UserKeyDao dao) {
        super(dao);
    }

    static UserKeyRepository getInstance (@NonNull final UserKeyDao dao) {
        synchronized (UserKeyRepository.class) {
            if (INSTANCE == null) {
                INSTANCE = new UserKeyRepository(dao);
            }
        }
        return INSTANCE;
    }

    static UserKey create (
            @NonNull final Long userId,
            @NonNull final Long keyId
    ) {
        return new UserKey(userId, keyId);
    }

    final UserKey createAndInsert(
            @NonNull final Long userId,
            @NonNull final Long keyId
    ) {
        final UserKey userKey = create(userId, keyId);
        insert(userKey);
        return userKey;
    }

    final UserKey createAndInsertAndSetId(
            @NonNull final Long userId,
            @NonNull final Long keyId
    ) {
        final UserKey userKey = create(userId, keyId);
        insertAndSetId(userKey);
        return userKey;
    }
}
