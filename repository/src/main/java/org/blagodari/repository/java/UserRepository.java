package org.blagodari.repository.java;

import androidx.annotation.NonNull;

public final class UserRepository
        extends SynchronizableRepository<User, UserDao> {

    private static volatile UserRepository INSTANCE;

    private UserRepository (@NonNull final UserDao dao) {
        super(dao);
    }

    static UserRepository getInstance (@NonNull final UserDao dao) {
        synchronized (UserRepository.class) {
            if (INSTANCE == null) {
                INSTANCE = new UserRepository(dao);
            }
        }
        return INSTANCE;
    }

    private static User create (@NonNull final Long userId) {
        return new User(userId);
    }

    private User createAndInsert(
            @NonNull final Long userId
    ) {
        final User user = create(userId);
        insert(user);
        return user;
    }

    public User getOrCreate (@NonNull final Long userId) {
        User user = getDao().getById(userId);
        if (user == null){
            user = createAndInsert(userId);
        }
        return user;
    }
}
