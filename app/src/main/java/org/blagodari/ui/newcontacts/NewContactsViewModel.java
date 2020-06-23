package org.blagodari.ui.newcontacts;

import androidx.annotation.NonNull;
import androidx.databinding.ObservableBoolean;
import androidx.databinding.ObservableField;
import androidx.databinding.ObservableInt;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import org.blagodari.db.addent.ContactWithKeyz;

import java.util.Collection;
import java.util.Comparator;
import java.util.TreeSet;

public final class NewContactsViewModel
        extends ViewModel {

    public final ContactProvider.ProgressListener contactProgressListener = new ContactProvider.ProgressListener() {
        @Override
        public void onStart () {
            mSyncContactsInProgress.set(true);
        }

        @Override
        public void onGetData (int index, int size) {
            mSyncContactsStatus.set(String.format("Чтение контактов %d из %d", index, size));
        }

        @Override
        public void onFinish () {
            mSyncContactsInProgress.set(false);
        }
    };

    @NonNull
    private final ObservableBoolean mNeedAuth = new ObservableBoolean(false);

    @NonNull
    private final ObservableBoolean mHaveContacts = new ObservableBoolean(false);

    public final ObservableBoolean mSyncContactsInProgress = new ObservableBoolean(false);

    public final ObservableField<String> mSyncContactsStatus = new ObservableField<>("");

    public final ObservableBoolean mSyncDataInProgress = new ObservableBoolean(false);
    public final ObservableField<String> mSyncDataApiClass = new ObservableField<>("");
    public final ObservableInt mGetInfoContactsCount = new ObservableInt(0);

    Comparator<ContactWithKeyz> contactWithKeyzComparator = new Comparator<ContactWithKeyz>() {
        @Override
        public int compare (ContactWithKeyz o1, ContactWithKeyz o2) {
            int result = 0;
            result = -o1.getContact().getFame().compareTo(o2.getContact().getFame());
            if (result == 0) {
                result = -o1.getContact().getSumLikeCount().compareTo(o2.getContact().getSumLikeCount());
            }
            if (result == 0) {
                result = o1.getContact().getTitle().compareTo(o2.getContact().getTitle());
            }
            return result;
        }
    };
    @NonNull
    private final MutableLiveData<Collection<ContactWithKeyz>> mContacts = new MutableLiveData<>(new TreeSet<>(contactWithKeyzComparator));

    private NewContactsViewModel () {
        this.mContacts.observeForever(input -> this.mHaveContacts.set(!input.isEmpty()));
    }

    @NonNull
    public final ObservableBoolean isNeedAuth () {
        return this.mNeedAuth;
    }

    @NonNull
    public final ObservableBoolean isHaveContacts () {
        return this.mHaveContacts;
    }

    @NonNull
    public final MutableLiveData<Collection<ContactWithKeyz>> getContacts () {
        return this.mContacts;
    }

    static final class Factory
            extends ViewModelProvider.NewInstanceFactory {


        Factory (
        ) {
        }

        @Override
        @NonNull
        public <T extends ViewModel> T create (@NonNull final Class<T> modelClass) {
            return (T) new NewContactsViewModel();
        }
    }
}
