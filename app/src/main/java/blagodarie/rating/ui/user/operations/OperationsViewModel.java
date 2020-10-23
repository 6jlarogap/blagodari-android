package blagodarie.rating.ui.user.operations;

import androidx.annotation.NonNull;
import androidx.databinding.ObservableBoolean;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;
import androidx.paging.PagedList;

import blagodarie.rating.model.IDisplayOperation;
import blagodarie.rating.model.entities.DisplayOperation;

public final class OperationsViewModel
        extends ViewModel {

    private LiveData<PagedList<IDisplayOperation>> mOperations;

    @NonNull
    private final ObservableBoolean mHaveAccount = new ObservableBoolean(false);

    @NonNull
    private final ObservableBoolean mOwnProfile = new ObservableBoolean(false);

    @NonNull
    private final ObservableBoolean mDownloadInProgress = new ObservableBoolean(false);

    @NonNull
    public LiveData<PagedList<IDisplayOperation>> getOperations () {
        return mOperations;
    }

    public void setOperations (@NonNull final LiveData<PagedList<IDisplayOperation>> operations) {
        mOperations = operations;
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
