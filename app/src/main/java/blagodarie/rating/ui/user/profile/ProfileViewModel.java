package blagodarie.rating.ui.user.profile;

import android.graphics.Bitmap;

import androidx.annotation.NonNull;
import androidx.databinding.ObservableBoolean;
import androidx.databinding.ObservableField;
import androidx.databinding.ObservableInt;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import androidx.paging.PagedList;

import java.util.List;

import blagodarie.rating.server.GetThanksUsersResponse;

public final class ProfileViewModel
        extends ViewModel {

    @NonNull
    private final ObservableField<String> mLastName = new ObservableField<>("");

    @NonNull
    private final ObservableField<String> mFirstName = new ObservableField<>("");

    @NonNull
    private final ObservableField<String> mMiddleName = new ObservableField<>("");

    @NonNull
    private final ObservableField<String> mPhoto = new ObservableField<>("");

    @NonNull
    private final ObservableField<Bitmap> mQrCode = new ObservableField<>();

    @NonNull
    private final ObservableField<String> mCardNumber = new ObservableField<>("");

    @NonNull
    private final ObservableInt mFame = new ObservableInt(0);

    @NonNull
    private final ObservableInt mSumThanksCount = new ObservableInt(0);

    @NonNull
    private final ObservableInt mTrustCount = new ObservableInt(0);

    @NonNull
    private final ObservableInt mMistrustCount = new ObservableInt(0);

    @NonNull
    private final ObservableField<Integer> mThanksCount = new ObservableField<>();

    @NonNull
    private final ObservableField<Boolean> mIsTrust = new ObservableField<>();

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
    public final ObservableField<String> getLastName () {
        return mLastName;
    }

    @NonNull
    public final ObservableField<String> getFirstName () {
        return mFirstName;
    }

    @NonNull
    public final ObservableField<String> getMiddleName () {
        return mMiddleName;
    }

    @NonNull
    public final ObservableField<String> getPhoto () {
        return mPhoto;
    }

    @NonNull
    public final ObservableField<Bitmap> getQrCode () {
        return mQrCode;
    }

    @NonNull
    public final ObservableField<String> getCardNumber () {
        return mCardNumber;
    }

    @NonNull
    public final ObservableInt getSumThanksCount () {
        return mSumThanksCount;
    }

    @NonNull
    public final ObservableInt getFame () {
        return mFame;
    }

    @NonNull
    public final ObservableInt getTrustCount () {
        return mTrustCount;
    }

    @NonNull
    public final ObservableInt getMistrustCount () {
        return mMistrustCount;
    }

    @NonNull
    public final ObservableField<Integer> getThanksCount () {
        return mThanksCount;
    }

    @NonNull
    public final ObservableField<Boolean> getIsTrust () {
        return mIsTrust;
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
