package org.blagodarie.repository.java;

import android.content.Context;

import androidx.annotation.NonNull;

import com.ex.diagnosticlib.BuildConfig;
import com.ex.diagnosticlib.Diagnostic;

import java.util.Set;

public final class DataRepository {

    private static volatile DataRepository INSTANCE;
    private static volatile AppDatabase mDatabase;

    @NonNull
    private volatile UserRepository mUserRepository;

    @NonNull
    private volatile KeyRepository mKeyRepository;

    @NonNull
    private volatile LikeRepository mLikeRepository;

    @NonNull
    private volatile ContactRepository mContactRepository;

    @NonNull
    private volatile UserKeyRepository mUserKeyRepository;

    @NonNull
    private volatile UserContactRepository mUserContactRepository;

    @NonNull
    private volatile ContactKeyRepository mContactKeyRepository;

    @NonNull
    private volatile LikeKeyRepository mLikeKeyRepository;

    private DataRepository (@NonNull final AppDatabase database) {
        mDatabase = database;

        this.mUserRepository = UserRepository.getInstance(mDatabase.getUserDao());
        this.mKeyRepository = KeyRepository.getInstance(mDatabase.getKeyDao());
        this.mLikeRepository = LikeRepository.getInstance(mDatabase.getLikeDao());
        this.mContactRepository = ContactRepository.getInstance(mDatabase.getContactDao());
        this.mUserKeyRepository = UserKeyRepository.getInstance(mDatabase.getUserKeyDao());
        this.mUserContactRepository = UserContactRepository.getInstance(mDatabase.getUserContactDao());
        this.mContactKeyRepository = ContactKeyRepository.getInstance(mDatabase.getContactKeyDao());
        this.mLikeKeyRepository = LikeKeyRepository.getInstance(mDatabase.getLikeKeyDao());
    }

    public static DataRepository getInstance (@NonNull final Context context) {
        synchronized (DataRepository.class) {
            if (INSTANCE == null) {
                INSTANCE = new DataRepository(AppDatabase.getInstance(context));
            }
        }
        return INSTANCE;
    }

    @NonNull
    public final UserRepository getUserRepository () {
        return this.mUserRepository;
    }

    @NonNull
    public final KeyRepository getKeyRepository () {
        return this.mKeyRepository;
    }

    @NonNull
    public final LikeRepository getLikeRepository () {
        return this.mLikeRepository;
    }

    @NonNull
    public final ContactRepository getContactRepository () {
        return this.mContactRepository;
    }

    @NonNull
    public final UserKeyRepository getUserKeyRepository () {
        return this.mUserKeyRepository;
    }

    @NonNull
    public final LikeKeyRepository getLikeKeyRepository () {
        return this.mLikeKeyRepository;
    }

    public final boolean isAuthorizedUser (@NonNull final Long userId) {
        return this.mKeyRepository.getCountByOwnerIdAndTypeId(userId, KeyType.Type.GOOGLE_ACCOUNT_ID) > 0;
    }

    /**
     * Добавляет контакт и его ключи в БД и связывает пользователя с контактом и с ключами.
     * Предусловия:
     * - контакт не должен иметь идентификатор;
     * - ключи не должны иметь идентификаторы.
     *
     * @param userId          Идентификатор пользователя.
     * @param contactWithKeys Контакт с ключами
     */
    public final void insertContactWithKeys (
            @NonNull final Long userId,
            @NonNull final ContactWithKeys contactWithKeys
    ) {
        final Contact contact = contactWithKeys.getContact();
        final Set<Key> keys = contactWithKeys.getKeySet();
        //проверка предусловий
        if(BuildConfig.DEBUG) {
            Diagnostic.Assert(
                    contact.getId() == null,
                    String.format("Contact for insert %s already has id", contact)
            );
            for (Key key : keys) {
                Diagnostic.Assert(
                        key.getId() == null,
                        String.format("Key for insert %s already has id", key)
                );
            }
        }

        //выполнить в одной транзакции
        mDatabase.runInTransaction(() -> {
            //вставить контакт
            this.mContactRepository.insertAndSetId(contact);
            //связать пользователя с контактом
            this.mUserContactRepository.createAndInsert(userId, contact.getId());
            //вставить ключи
            this.mKeyRepository.insertAndSetIdsOrGetIdsFromDB(keys);
            //связать контакт с ключами
            for(Key key: keys){
                this.mContactKeyRepository.createAndInsert(contact.getId(), key.getId());
            }
            //связать пользователя с ключами
            for(Key key: keys){
                this.mUserKeyRepository.createAndInsert(userId, key.getId());
            }
        });
    }
}
