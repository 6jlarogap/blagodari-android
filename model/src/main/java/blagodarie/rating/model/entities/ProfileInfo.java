package blagodarie.rating.model.entities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import blagodarie.rating.model.IProfileInfo;

public final class ProfileInfo
        implements IProfileInfo {

    public static final ProfileInfo EMPTY_PROFILE = new ProfileInfo("","", null, 0,0,0,0, null, null);

    @NonNull
    private final String mFirstName;

    @NonNull
    private final String mLastName;

    @Nullable
    private final String mPhoto;

    private final int mFame;

    private final int mTrustCount;

    private final int mMistrustCount;

    private final int mSumThanksCount;

    @Nullable
    private final Integer mThanksCount;

    @Nullable
    private final Boolean mIsTrust;

    public ProfileInfo (
            @NonNull final String firstName,
            @NonNull final String lastName,
            @Nullable final String photo,
            final int fame,
            final int trustCount,
            final int mistrustCount,
            final int sumThanksCount,
            @Nullable final Integer thanksCount,
            @Nullable final Boolean isTrust
    ) {
        mPhoto = photo;
        mFirstName = firstName;
        mLastName = lastName;
        mFame = fame;
        mTrustCount = trustCount;
        mMistrustCount = mistrustCount;
        mSumThanksCount = sumThanksCount;
        mThanksCount = thanksCount;
        mIsTrust = isTrust;
    }

    @NonNull
    @Override
    public String getFirstName () {
        return mFirstName;
    }

    @NonNull
    @Override
    public String getLastName () {
        return mLastName;
    }

    @Nullable
    @Override
    public String getPhoto () {
        return mPhoto;
    }

    @Override
    public int getSumThanksCount () {
        return mSumThanksCount;
    }

    @Override
    public int getFame () {
        return mFame;
    }

    @Override
    public int getTrustCount () {
        return mTrustCount;
    }

    @Override
    public int getMistrustCount () {
        return mMistrustCount;
    }

    @Nullable
    @Override
    public Integer getThanksCount () {
        return mThanksCount;
    }

    @Nullable
    @Override
    public Boolean isTrust () {
        return mIsTrust;
    }

}
