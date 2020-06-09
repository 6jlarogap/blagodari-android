package org.blagodari.repository.java;

import androidx.annotation.NonNull;

final class LikeKeyRepository
        extends SynchronizableRepository<LikeKey, LikeKeyDao> {

    private static volatile LikeKeyRepository INSTANCE;

    private LikeKeyRepository (@NonNull final LikeKeyDao dao) {
        super(dao);
    }

    static LikeKeyRepository getInstance (@NonNull final LikeKeyDao dao) {
        synchronized (LikeKeyRepository.class) {
            if (INSTANCE == null) {
                INSTANCE = new LikeKeyRepository(dao);
            }
        }
        return INSTANCE;
    }

    public static LikeKey create (
            @NonNull final Long likeId,
            @NonNull final Long keyId
    ) {
        return new LikeKey(likeId, keyId);
    }

    final LikeKey createAndInsert(
            @NonNull final Long likeId,
            @NonNull final Long keyId
    ) {
        final LikeKey likeKey = create(likeId, keyId);
        insert(likeKey);
        return likeKey;
    }

    final LikeKey createAndInsertAndSetId(
            @NonNull final Long likeId,
            @NonNull final Long keyId
    ) {
        final LikeKey likeKey = create(likeId, keyId);
        insertAndSetId(likeKey);
        return likeKey;
    }
}
