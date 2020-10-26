package blagodarie.rating.ui.user.abilities;

import androidx.annotation.NonNull;
import androidx.databinding.ObservableBoolean;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;
import androidx.paging.PagedList;

import blagodarie.rating.model.IAbility;

public final class AbilitiesViewModel
        extends ViewModel {

    private LiveData<PagedList<IAbility>> mAbilities;

    @NonNull
    private final ObservableBoolean mOwnProfile = new ObservableBoolean(false);

    @NonNull
    private final ObservableBoolean mDownloadInProgress = new ObservableBoolean(false);

    @NonNull
    public LiveData<PagedList<IAbility>> getAbilities () {
        return mAbilities;
    }

    public void setAbilities (@NonNull final LiveData<PagedList<IAbility>> wishes) {
        mAbilities = wishes;
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
