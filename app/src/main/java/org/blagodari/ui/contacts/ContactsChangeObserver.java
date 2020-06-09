package org.blagodari.ui.contacts;

import android.content.ContentResolver;
import android.database.ContentObserver;
import android.provider.ContactsContract;

import androidx.annotation.NonNull;

import com.ex.diagnosticlib.Diagnostic;
import org.blagodari.DataRepository;
import org.blagodari.contacts.ContactRepository;
import org.blagodari.contacts.ContactSynchronizer;

import io.reactivex.Completable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;

final class ContactsChangeObserver {

    private static volatile ContactsChangeObserver INSTANCE;
    private final CompositeDisposable mDisposables = new CompositeDisposable();

    /**
     * Слушатель изменения контактов. Когда контакты претерпели изменения, утанавливает флаг
     * необходимости синхронизации контактов.
     */
    private static final ContentObserver mContactsChangeObserver = new ContentObserver(null) {
        @Override
        public void onChange (boolean selfChange) {
            super.onChange(selfChange);
            Diagnostic.i();
            mNeedSyncContacts = true;
        }
    };

    /**
     * Флаг необходимости синхронизации контактов.
     */
    private static boolean mNeedSyncContacts = true;

    /**
     * Флаг запуска синхронизации контактов.
     */
    private static boolean mSyncContactsInProgress = false;

    /**
     * Флаг регистрации слушателя.
     */
    private static boolean mContactsChangeObserverRegistered = false;


    private ContactsChangeObserver () {
    }

    public static ContactsChangeObserver getInstance () {
        synchronized (ContactsChangeObserver.class) {
            if (INSTANCE == null) {
                INSTANCE = new ContactsChangeObserver();
            }
        }
        return INSTANCE;
    }

    private void registerIfNeed (@NonNull final ContentResolver contentResolver) {
        Diagnostic.i();
        if (!mContactsChangeObserverRegistered && !mSyncContactsInProgress) {
            contentResolver.
                    registerContentObserver(ContactsContract.Data.CONTENT_URI,
                            false,
                            mContactsChangeObserver);
            mContactsChangeObserverRegistered = true;
        }
    }

    final void unregister (@NonNull final ContentResolver contentResolver) {
        Diagnostic.i();
        contentResolver.unregisterContentObserver(mContactsChangeObserver);
        mContactsChangeObserverRegistered = false;
        //mDisposables.dispose();
    }

    private void synchronizeContactsIfNeed (
            @NonNull final Long userId,
            @NonNull final ContentResolver contentResolver,
            @NonNull final ContactRepository contactRepository,
            @NonNull final DataRepository dataRepository,
            @NonNull final ContactSynchronizer.ProgressListener progressListener
    ) {
        Diagnostic.i();
        if (mNeedSyncContacts) {
            unregister(contentResolver);
            mNeedSyncContacts = false;
            mSyncContactsInProgress = true;
            mDisposables.add(
                    Completable.
                            fromAction(() ->
                                    ContactSynchronizer.syncByPage(userId, contactRepository, dataRepository, progressListener, 12)
                            ).
                            subscribeOn(Schedulers.io()).
                            observeOn(AndroidSchedulers.mainThread()).
                            subscribe(() -> {
                                mSyncContactsInProgress = false;
                                registerIfNeed(contentResolver);
                            })
            );
        }
    }

    final void registerIfNeedAndSynchronizeContactsIfNeed (
            @NonNull final Long userId,
            @NonNull final ContentResolver contentResolver,
            @NonNull final ContactRepository contactRepository,
            @NonNull final DataRepository repository,
            @NonNull final ContactSynchronizer.ProgressListener progressListener
    ) {
        Diagnostic.i();
        registerIfNeed(contentResolver);
        synchronizeContactsIfNeed(userId, contentResolver, contactRepository, repository, progressListener);
    }
}
