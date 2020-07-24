package blagodarie.rating.ui.wishes;

import androidx.annotation.NonNull;
import androidx.databinding.ObservableBoolean;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.List;

public final class WishesViewModel
        extends ViewModel {

    @NonNull
    private final MutableLiveData<List<Wish>> mWishes = new MutableLiveData<>();

    @NonNull
    private final ObservableBoolean mSelfProfile = new ObservableBoolean(false);

    @NonNull
    public MutableLiveData<List<Wish>> getWishes () {
        return mWishes;
    }

    @NonNull
    public final ObservableBoolean isSelfProfile () {
        return mSelfProfile;
    }

}
