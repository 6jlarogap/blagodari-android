package blagodarie.rating.ui.operations;

import androidx.annotation.NonNull;
import androidx.databinding.ObservableBoolean;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;
import androidx.paging.PagedList;

public final class OperationsViewModel
        extends ViewModel {

    private LiveData<PagedList<Operation>> mOperations;

    @NonNull
    private final ObservableBoolean mDownloadInProgress = new ObservableBoolean(false);

    @NonNull
    private final ObservableBoolean mSelfProfile = new ObservableBoolean(false);

    @NonNull
    public ObservableBoolean getDownloadInProgress () {
        return mDownloadInProgress;
    }

    @NonNull
    public LiveData<PagedList<Operation>> getOperations () {
        return mOperations;
    }

    public void setOperations (@NonNull final LiveData<PagedList<Operation>> operations) {
        mOperations = operations;
    }

    @NonNull
    public final ObservableBoolean isSelfProfile () {
        return mSelfProfile;
    }
}
