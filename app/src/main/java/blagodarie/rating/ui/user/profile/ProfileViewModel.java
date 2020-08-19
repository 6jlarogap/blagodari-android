package blagodarie.rating.ui.user.profile;

import android.graphics.Bitmap;

import androidx.annotation.NonNull;
import androidx.databinding.ObservableBoolean;
import androidx.databinding.ObservableField;
import androidx.databinding.ObservableInt;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.List;

import blagodarie.rating.ui.user.DisplayThanksUser;

public final class ProfileViewModel
        extends ViewModel {

    public enum Mode {
        VIEW, EDIT
    }

    @NonNull
    private final ObservableField<Mode> mCurrentMode = new ObservableField<>(Mode.VIEW);

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
    private final ObservableInt mTrustlessCount = new ObservableInt(0);

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

    @NonNull
    private final MutableLiveData<List<DisplayThanksUser>> mThanksUsers = new MutableLiveData<>();

    public ProfileViewModel () {
    }

    @NonNull
    public final ObservableField<Mode> getCurrentMode () {
        return mCurrentMode;
    }

    public final void setCurrentMode (@NonNull final Mode mode) {
        mCurrentMode.set(mode);
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
    public final ObservableInt getTrustlessCount () {
        return mTrustlessCount;
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
    public MutableLiveData<List<DisplayThanksUser>> getThanksUsers () {
        return mThanksUsers;
    }

}
