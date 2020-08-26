package blagodarie.rating.ui.user.keys;

import androidx.annotation.NonNull;
import androidx.databinding.ObservableBoolean;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;
import androidx.paging.PagedList;

public final class KeysViewModel
        extends ViewModel {

    private LiveData<PagedList<Key>> mKeys;

    @NonNull
    private final ObservableBoolean mHaveAccount = new ObservableBoolean(false);

    @NonNull
    private final ObservableBoolean mOwnProfile = new ObservableBoolean(false);

    @NonNull
    private final ObservableBoolean mDownloadInProgress = new ObservableBoolean(false);

    @NonNull
    public LiveData<PagedList<Key>> getKeys () {
        return mKeys;
    }

    public void setOperations (@NonNull final LiveData<PagedList<Key>> keys) {
        mKeys = keys;
    }

    @NonNull
    public final ObservableBoolean isHaveAccount () {
        return mHaveAccount;
    }

    @NonNull
    public final ObservableBoolean isOwnProfile () {
        return mOwnProfile;
    }

    @NonNull
    public final ObservableBoolean getDownloadInProgress () {
        return mDownloadInProgress;
    }
}
