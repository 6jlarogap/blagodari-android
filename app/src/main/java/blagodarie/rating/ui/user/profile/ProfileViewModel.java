package blagodarie.rating.ui.user.profile;

import android.accounts.Account;
import android.graphics.Bitmap;

import androidx.annotation.NonNull;
import androidx.databinding.ObservableBoolean;
import androidx.databinding.ObservableField;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;
import androidx.paging.PagedList;

import blagodarie.rating.model.IProfile;
import blagodarie.rating.model.entities.Profile;
import blagodarie.rating.server.GetThanksUsersResponse;

public final class ProfileViewModel
        extends ViewModel {

    @NonNull
    private final ObservableField<IProfile> mProfileInfo = new ObservableField<>(Profile.EMPTY_PROFILE);

    @NonNull
    private final ObservableField<Bitmap> mQrCode = new ObservableField<>();

    @NonNull
    private final ObservableBoolean mDownloadInProgress = new ObservableBoolean(false);

    private LiveData<PagedList<GetThanksUsersResponse.ThanksUser>> mThanksUsers;

    public ProfileViewModel () {
    }

    @NonNull
    public final ObservableField<IProfile> getProfileInfo () {
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
    public LiveData<PagedList<GetThanksUsersResponse.ThanksUser>> getThanksUsers () {
        return mThanksUsers;
    }

    public void setThanksUsers (
            @NonNull final LiveData<PagedList<GetThanksUsersResponse.ThanksUser>> thanksUsers
    ) {
        mThanksUsers = thanksUsers;
    }

}
