package org.blagodarie.repository.java;

import androidx.annotation.NonNull;

final class LikeRepository
        extends SynchronizableRepository<Like, LikeDao> {

    private static volatile LikeRepository INSTANCE;

    private LikeRepository (@NonNull final LikeDao dao) {
        super(dao);
    }

    static LikeRepository getInstance (@NonNull final LikeDao dao) {
        synchronized (LikeRepository.class) {
            if (INSTANCE == null) {
                INSTANCE = new LikeRepository(dao);
            }
        }
        return INSTANCE;
    }

    static Like create (
            @NonNull final Long ownerId,
            @NonNull final Long createTimestamp
    ) {
        return new Like(ownerId, createTimestamp);
    }
}
