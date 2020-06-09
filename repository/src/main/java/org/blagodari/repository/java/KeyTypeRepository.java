package org.blagodari.repository.java;

import androidx.annotation.NonNull;

final class KeyTypeRepository
        extends BaseRepository<KeyType, KeyTypeDao> {

    private static volatile KeyTypeRepository INSTANCE;

    private KeyTypeRepository (@NonNull final KeyTypeDao keyTypeDao) {
        super(keyTypeDao);
    }

    static KeyTypeRepository getInstance (@NonNull final KeyTypeDao keyTypeDao) {
        synchronized (KeyTypeRepository.class) {
            if (INSTANCE == null) {
                INSTANCE = new KeyTypeRepository(keyTypeDao);
            }
        }
        return INSTANCE;
    }

}
