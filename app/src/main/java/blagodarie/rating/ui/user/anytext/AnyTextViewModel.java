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

import blagodarie.rating.model.IAnyTextInfo;
import blagodarie.rating.model.IProfileInfo;
import blagodarie.rating.model.entities.AnyTextInfo;
import blagodarie.rating.model.entities.ProfileInfo;
import blagodarie.rating.server.GetThanksUsersResponse;

public final class AnyTextViewModel
        extends ViewModel {

    @NonNull
    private final ObservableField<IAnyTextInfo> mAnyTextInfo = new ObservableField<>(AnyTextInfo.EMPTY_ANY_TEXT);

    @NonNull
    private final ObservableField<String> mAnyText = new ObservableField<>("");

    @NonNull
    private final ObservableField<Bitmap> mQrCode = new ObservableField<>();

    @NonNull
    private final ObservableBoolean mDownloadInProgress = new ObservableBoolean(false);

    @NonNull
    private final ObservableBoolean mHaveAccount = new ObservableBoolean(false);

    @NonNull
    private final MutableLiveData<List<GetThanksUsersResponse.ThanksUser>> mThanksUsers = new MutableLiveData<>();

    @NonNull
    public final ObservableField<IAnyTextInfo> getAnyTextInfo () {
        return mAnyTextInfo;
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
