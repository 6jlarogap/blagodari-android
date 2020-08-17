package blagodarie.rating.ui.user.wishes;

import androidx.annotation.NonNull;
import androidx.databinding.ObservableBoolean;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;
import androidx.paging.PagedList;

public final class WishesViewModel
        extends ViewModel {

    private LiveData<PagedList<Wish>> mWishes;

    @NonNull
    private final ObservableBoolean mOwnProfile = new ObservableBoolean(false);

    @NonNull
    private final ObservableBoolean mDownloadInProgress = new ObservableBoolean(false);

    @NonNull
    public LiveData<PagedList<Wish>> getWishes () {
        return mWishes;
    }

    public void setWishes (@NonNull final LiveData<PagedList<Wish>> wishes) {
        mWishes = wishes;
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
