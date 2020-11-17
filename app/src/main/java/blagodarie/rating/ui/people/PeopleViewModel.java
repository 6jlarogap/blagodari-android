package blagodarie.rating.ui.people;

import androidx.annotation.NonNull;
import androidx.databinding.ObservableBoolean;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import androidx.paging.PagedList;

import blagodarie.rating.model.IProfile;

public final class PeopleViewModel
        extends ViewModel {

    private LiveData<PagedList<IProfile>> mPeople;

    @NonNull
    private final ObservableBoolean mDownloadInProgress = new ObservableBoolean(false);

    @NonNull
    private final MutableLiveData<String> mSearchText = new MutableLiveData<>();

    @NonNull
    public ObservableBoolean getDownloadInProgress () {
        return mDownloadInProgress;
    }

    public final LiveData<PagedList<IProfile>> getPeople () {
        return mPeople;
    }

    public final void setPeople (@NonNull final LiveData<PagedList<IProfile>> people) {
        mPeople = people;
    }

    @NonNull
    public final MutableLiveData<String> getSearchText () {
        return mSearchText;
    }
}
