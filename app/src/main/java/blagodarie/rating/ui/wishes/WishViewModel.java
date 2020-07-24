package blagodarie.rating.ui.wishes;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.ObservableBoolean;
import androidx.databinding.ObservableField;
import androidx.lifecycle.ViewModel;

public final class WishViewModel
        extends ViewModel {

    @NonNull
    private final ObservableField<String> mWishText = new ObservableField<>("");

    @NonNull
    private final ObservableBoolean mSelfProfile = new ObservableBoolean(false);

    @NonNull
    private final ObservableBoolean mDownloadInProgress = new ObservableBoolean(false);

    @NonNull
    public final ObservableField<String> getWishText () {
        return mWishText;
    }

    @NonNull
    public final ObservableBoolean isSelfProfile () {
        return mSelfProfile;
    }

    @NonNull
    public final ObservableBoolean getDownloadInProgress () {
        return mDownloadInProgress;
    }
}
