package com.vsdrozd.blagodarie.ui.contacts;

import androidx.annotation.NonNull;
import androidx.databinding.ObservableBoolean;
import androidx.databinding.ObservableField;
import androidx.databinding.ObservableInt;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;
import androidx.paging.LivePagedListBuilder;
import androidx.paging.PagedList;

import com.ex.diagnosticlib.Diagnostic;
import com.vsdrozd.blagodarie.DataRepository;
import com.vsdrozd.blagodarie.contacts.ContactSynchronizer;
import com.vsdrozd.blagodarie.db.scheme.Contact;
import com.vsdrozd.blagodarie.server.ServerSynchronizer;
import com.vsdrozd.blagodarie.server.api.GetLikes;
import com.vsdrozd.blagodarie.server.api.SyncDataApi;
import com.vsdrozd.blagodarie.ui.warning.Warning;
import com.vsdrozd.blagodarie.ui.warning.WarningContainer;

import java.util.List;

import io.reactivex.disposables.CompositeDisposable;

public final class ContactsViewModel
        extends ViewModel {

    private static final int PAGE_SIZE = 20;

    public final ObservableBoolean mNeedAuth = new ObservableBoolean(false);
    public final ObservableBoolean mEmptyContacts = new ObservableBoolean(true);
    public final ObservableBoolean mSyncContactsInProgress = new ObservableBoolean(false);
    public final ObservableBoolean mGetDataInProgress = new ObservableBoolean(false);
    public final ObservableBoolean mSyncDataInProgress = new ObservableBoolean(false);
    public final ObservableField<String> mSyncDataApiClass = new ObservableField<>("");
    public final ObservableInt mWarningsCount = new ObservableInt(0);
    public final ObservableField<String> mSyncContactsStatus = new ObservableField<>("");
    private final MutableLiveData<String> mInfoMessage = new MutableLiveData<>();
    private final ServerSynchronizer.ApiListener mServerSynchronizerApiListener = new ServerSynchronizer.ApiListener() {
        @Override
        public void onStart (@NonNull final Class c) {
            mSyncDataInProgress.set(true);
            mSyncDataApiClass.set(c.getSimpleName());
        }

        @Override
        public void onSuccess () {
            mSyncDataInProgress.set(false);
        }

        @Override
        public void onFailed (@NonNull final Throwable throwable) {
            mSyncDataInProgress.set(false);
            mInfoMessage.setValue(throwable.getMessage());
        }
    };

    @NonNull
    private final CompositeDisposable mDisposables = new CompositeDisposable();

    @NonNull
    private final DataRepository mDataRepository;

    @NonNull
    private final Long mUserId;

    @NonNull
    private final MediatorLiveData<PagedList<Contact>> mContacts = new MediatorLiveData<>();

    @NonNull
    private final WarningContainer mWarningContainer;

    @NonNull
    private final MutableLiveData<ContactsOrder> mContactsOrder;

    @NonNull
    private final MutableLiveData<String> mFilter = new MutableLiveData<>("");

    private ContactsViewModel (
            @NonNull final DataRepository repository,
            @NonNull final Long userId,
            @NonNull final ContactsOrder contactsOrder
    ) {
        super();
        Diagnostic.i();

        this.mDataRepository = repository;
        this.mUserId = userId;
        this.mContactsOrder = new MutableLiveData<>(contactsOrder);
        this.mWarningContainer = new WarningContainer(this.mDataRepository);

        setupContacts();
    }

    private void setupContacts () {
        this.mContacts.addSource(Transformations.switchMap(this.mContactsOrder, input -> createContacts()), this.mContacts::setValue);
        this.mContacts.addSource(Transformations.switchMap(this.mFilter, input -> createContacts()), this.mContacts::setValue);
    }

    final LiveData<List<Warning>> getWarnings () {
        return this.mWarningContainer.getWarnings();
    }

    @Override
    protected final void onCleared () {
        super.onCleared();
        mDisposables.dispose();
    }

    final ContactsOrder getContactsOrder () {
        return this.mContactsOrder.getValue();
    }

    final void setContactsOrder (@NonNull final ContactsOrder contactsOrder) {
        Diagnostic.i();
        this.mContactsOrder.setValue(contactsOrder);
    }

    final String getFilter () {
        return this.mFilter.getValue();
    }

    final void setFilter (@NonNull final String filter) {
        this.mFilter.setValue(filter);
    }

    @NonNull
    final Long getUserId () {
        return this.mUserId;
    }

    @NonNull
    final LiveData<PagedList<Contact>> getContacts () {
        return this.mContacts;
    }

    private LiveData<PagedList<Contact>> createContacts () {
        Diagnostic.i();
        LiveData<PagedList<Contact>> result;
        String filter = "%" + getFilter() + "%";
        switch (getContactsOrder()) {
            case NAME:
                result = new LivePagedListBuilder<>(mDataRepository.getContactsByUserOrderByName(mUserId, filter), PAGE_SIZE).build();
                break;
            case UPDATE_TIMESTAMP:
                result = new LivePagedListBuilder<>(mDataRepository.getContactsByUserOrderByTime(mUserId, filter), PAGE_SIZE).build();
                break;
            case FAME:
                result = new LivePagedListBuilder<>(mDataRepository.getContactsByUserOrderByFame(mUserId, filter), PAGE_SIZE).build();
                break;
            case LIKES_COUNT:
                result = new LivePagedListBuilder<>(mDataRepository.getContactsByUserOrderByLikeCount(mUserId, filter), PAGE_SIZE).build();
                break;
            case SUM_LIKES_COUNT:
                result = new LivePagedListBuilder<>(mDataRepository.getContactsByUserOrderBySumLikeCount(mUserId, filter), PAGE_SIZE).build();
                break;
            default:
                throw new IllegalArgumentException("Unknown contact sorting type: " + getContactsOrder().toString());
        }
        return result;
    }

    final MutableLiveData<String> getInfoMessage () {
        return this.mInfoMessage;
    }

    final ServerSynchronizer.ApiListener getServerSynchronizerListener () {
        return this.mServerSynchronizerApiListener;
    }

    final ContactSynchronizer.ProgressListener createContactSynchronizerListener (
            @NonNull final String onGetDataStringPattern,
            @NonNull final String onProcessingContactsStringPattern,
            @NonNull final String onDatabaseWriteStringPattern,
            @NonNull final String onContactRepositoryWriteStringPattern
    ) {
        return new ContactSynchronizer.ProgressListener() {
            @Override
            public void onGetData (final int index, final int count) {
                mSyncContactsInProgress.set(true);
                mSyncContactsStatus.set(String.format(onGetDataStringPattern, index, count));
            }

            @Override
            public void onProcessingContacts (final int indexFrom, final int indexTo, final int count) {
                mSyncContactsStatus.set(String.format(onProcessingContactsStringPattern, indexFrom, indexTo, count));
            }

            @Override
            public void onDatabaseWrite (final int indexFrom, final int indexTo, final int count) {
                mSyncContactsStatus.set(String.format(onDatabaseWriteStringPattern, indexFrom, indexTo, count));
            }

            @Override
            public void onContactRepositoryWrite (
                    final int indexFrom,
                    final int indexto,
                    final int size
            ) {
                mSyncContactsStatus.set(String.format(onContactRepositoryWriteStringPattern, indexFrom, indexto, size));
            }

            @Override
            public void onFinish () {
                mSyncContactsInProgress.set(false);
                mSyncContactsStatus.set("");
                updateContactInfo();
            }
        };
    }

    final void updateContactInfo () {
        Diagnostic.i();
        final ServerSynchronizer.ApiListener apiListener = new ServerSynchronizer.ApiListener() {
            @Override
            public void onStart (@NonNull final Class c) {
                mGetDataInProgress.set(true);
            }

            @Override
            public void onSuccess () {
                mGetDataInProgress.set(false);
            }

            @Override
            public void onFailed (@NonNull Throwable throwable) {
                getInfoMessage().setValue(throwable.getMessage());
                mGetDataInProgress.set(false);
            }
        };
        ServerSynchronizer.getInstance().startSyncDataApis(apiListener, new SyncDataApi.DataIn(this.mDataRepository, this.mUserId), GetLikes.getInstance());
    }

    static final class Factory
            extends ViewModelProvider.NewInstanceFactory {

        @NonNull
        private final DataRepository mRepository;

        @NonNull
        private final Long mUserId;

        @NonNull
        private final ContactsOrder mContactsOrder;

        Factory (
                @NonNull final DataRepository repository,
                @NonNull final Long userId,
                @NonNull final ContactsOrder contactsOrder
        ) {
            this.mRepository = repository;
            this.mUserId = userId;
            this.mContactsOrder = contactsOrder;
        }

        @Override
        @NonNull
        public <T extends ViewModel> T create (@NonNull Class<T> modelClass) {
            return (T) new ContactsViewModel(
                    this.mRepository,
                    this.mUserId,
                    this.mContactsOrder);
        }
    }
}
