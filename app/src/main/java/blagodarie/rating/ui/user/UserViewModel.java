package blagodarie.rating.ui.user;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class UserViewModel
        extends ViewModel {

    @NonNull
    private MutableLiveData<String> mOwnAccountPhotoUrl = new MutableLiveData<>();

    @NonNull
    public MutableLiveData<String> getOwnAccountPhotoUrl () {
        return mOwnAccountPhotoUrl;
    }

}
