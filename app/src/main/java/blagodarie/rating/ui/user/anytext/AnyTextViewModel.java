package blagodarie.rating.ui.user.anytext;

import android.graphics.Bitmap;

import androidx.annotation.NonNull;
import androidx.databinding.ObservableBoolean;
import androidx.databinding.ObservableField;
import androidx.databinding.ObservableInt;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.List;
import java.util.UUID;

import blagodarie.rating.server.GetProfileInfoResponse;
import blagodarie.rating.server.GetThanksUsersResponse;

public final class AnyTextViewModel
        extends ViewModel {

    @NonNull
    private final ObservableField<UUID> mAnyTextId = new ObservableField<>();

    @NonNull
    private final ObservableField<String> mAnyText = new ObservableField<>("");

    @NonNull
    private final ObservableField<Bitmap> mQrCode = new ObservableField<>();

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
    public final ObservableField<UUID> getAnyTextId () {
        return mAnyTextId;
    }

    @NonNull
    public final ObservableField<String> getAnyText () {
        return mAnyText;
    }

    @NonNull
    public final ObservableField<Bitmap> getQrCode () {
        return mQrCode;
    }

    @NonNull
    private final MutableLiveData<List<GetThanksUsersResponse.ThanksUser>> mThanksUsers = new MutableLiveData<>();

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

    public final ObservableBoolean isHaveAccount () {
        return mHaveAccount;
    }

    @NonNull
    public MutableLiveData<List<GetThanksUsersResponse.ThanksUser>> getThanksUsers () {
        return mThanksUsers;
    }
}
