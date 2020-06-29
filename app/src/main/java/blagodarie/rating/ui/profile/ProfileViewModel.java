package blagodarie.rating.ui.profile;

import androidx.annotation.NonNull;
import androidx.databinding.ObservableField;
import androidx.lifecycle.ViewModel;

public final class ProfileViewModel
        extends ViewModel {

    @NonNull
    private final ObservableField<String> mProfileUrl = new ObservableField<>("");

    public ProfileViewModel () {
    }


    @NonNull
    public final ObservableField<String> getProfileUrl () {
        return mProfileUrl;
    }
}
