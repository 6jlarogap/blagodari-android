package blagodarie.rating.model.entities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.UUID;

import blagodarie.rating.model.IProfile;

public final class Profile
        implements IProfile {

    public static final Profile EMPTY_PROFILE = new Profile(UUID.fromString("00000000-0000-0000-0000-000000000000"), "", "", null, 0, 0, 0, 0, 0, null);

    @NonNull
    private final UUID mId;

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

    private final int mThanksCount;

    @Nullable
    private final Boolean mIsTrust;

    public Profile (
            @NonNull final UUID id,
            @NonNull final String firstName,
            @NonNull final String lastName,
            @Nullable final String photo,
            final int fame,
            final int trustCount,
            final int mistrustCount,
            final int sumThanksCount,
            final int thanksCount,
            @Nullable final Boolean isTrust
    ) {
        mId = id;
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
    public UUID getId () {
        return mId;
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

    @Override
    public int getThanksCount () {
        return mThanksCount;
    }

    @Nullable
    @Override
    public Boolean isTrust () {
        return mIsTrust;
    }

}
