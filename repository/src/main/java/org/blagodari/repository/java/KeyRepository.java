package org.blagodari.repository.java;

import androidx.annotation.NonNull;

import java.util.Collection;

public final class KeyRepository
        extends SynchronizableRepository<Key, KeyDao> {

    private static volatile KeyRepository INSTANCE;

    private KeyRepository (@NonNull final KeyDao dao) {
        super(dao);
    }

    static KeyRepository getInstance (@NonNull final KeyDao dao) {
        synchronized (KeyRepository.class) {
            if (INSTANCE == null) {
                INSTANCE = new KeyRepository(dao);
            }
        }
        return INSTANCE;
    }

    public static Key create (
            @NonNull final String value,
            @NonNull final KeyType.Type type
    ) {
        return new Key(value, type.getKeyType().getId());
    }

    final Key createAndInsert(
            @NonNull final String value,
            @NonNull final KeyType.Type type
    ) {
        final Key key = create(value, type);
        insert(key);
        return key;
    }

    final Key createAndInsertAndSetId(
            @NonNull final String value,
            @NonNull final KeyType.Type type
    ) {
        final Key key = create(value, type);
        insertAndSetId(key);
        return key;
    }

    final Integer getCountByOwnerIdAndTypeId(
            @NonNull final Long userId,
            @NonNull final KeyType.Type keyType
    ){
        return getDao().getCountByOwnerIdAndTypeId(userId, keyType.getKeyType().getId());
    }

    final void insertAndSetIdsOrGetIdsFromDB(@NonNull final Collection<Key> keyCollection){
        getDao().insertAndSetIdsOrGetIdsFromDB(keyCollection);
    }
}
