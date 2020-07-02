package blagodarie.rating.ui.main;

import androidx.annotation.NonNull;
import androidx.databinding.ObservableField;
import androidx.lifecycle.ViewModel;

public final class MainViewModel
        extends ViewModel {

    @NonNull
    private final ObservableField<String> mProfileUrl = new ObservableField<>("");

    public MainViewModel () {
    }

    @NonNull
    public final ObservableField<String> getProfileUrl () {
        return mProfileUrl;
    }

}
