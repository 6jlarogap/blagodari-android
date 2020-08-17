package blagodarie.rating.ui.user;

import androidx.annotation.NonNull;
import androidx.databinding.ObservableBoolean;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class UserViewModel
        extends ViewModel {

    @NonNull
    private MutableLiveData<String> mOwnAccountPhotoUrl = new MutableLiveData<>();

    @NonNull
    private final ObservableBoolean mOwnProfile = new ObservableBoolean(false);

    @NonNull
    public MutableLiveData<String> getOwnAccountPhotoUrl () {
        return mOwnAccountPhotoUrl;
    }

    @NonNull
    public final ObservableBoolean isOwnProfile () {
        return mOwnProfile;
    }
}
