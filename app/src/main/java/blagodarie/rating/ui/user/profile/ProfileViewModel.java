package blagodarie.rating.ui.user.profile;

import android.graphics.Bitmap;

import androidx.annotation.NonNull;
import androidx.databinding.ObservableBoolean;
import androidx.databinding.ObservableField;
import androidx.databinding.ObservableInt;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;
import androidx.paging.PagedList;

import blagodarie.rating.model.IProfileInfo;
import blagodarie.rating.model.entities.ProfileInfo;
import blagodarie.rating.server.GetThanksUsersResponse;

public final class ProfileViewModel
        extends ViewModel {

    @NonNull
    private final ObservableField<IProfileInfo> mProfileInfo = new ObservableField<>(ProfileInfo.EMPTY_PROFILE);

    @NonNull
    private final ObservableField<Bitmap> mQrCode = new ObservableField<>();

    @NonNull
    private final ObservableBoolean mDownloadInProgress = new ObservableBoolean(false);

    @NonNull
    private final ObservableBoolean mHaveAccount = new ObservableBoolean(false);

    @NonNull
    private final ObservableBoolean mOwnProfile = new ObservableBoolean(false);

    private LiveData<PagedList<GetThanksUsersResponse.ThanksUser>> mThanksUsers;

    public ProfileViewModel () {
    }

    @NonNull
    public final ObservableField<IProfileInfo> getProfileInfo () {
        return mProfileInfo;
    }

    @NonNull
    public final ObservableField<Bitmap> getQrCode () {
        return mQrCode;
    }

    @NonNull
    public final ObservableBoolean getDownloadInProgress () {
        return mDownloadInProgress;
    }

    @NonNull
    public final ObservableBoolean isHaveAccount () {
        return mHaveAccount;
    }

    public final ObservableBoolean isOwnProfile () {
        return mOwnProfile;
    }

    @NonNull
    public LiveData<PagedList<GetThanksUsersResponse.ThanksUser>> getThanksUsers () {
        return mThanksUsers;
    }

    public void setThanksUsers (
            @NonNull final LiveData<PagedList<GetThanksUsersResponse.ThanksUser>> thanksUsers
    ) {
        mThanksUsers = thanksUsers;
    }

}
