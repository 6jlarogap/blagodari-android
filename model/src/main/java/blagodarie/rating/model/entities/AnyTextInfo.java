package blagodarie.rating.model.entities;

import androidx.annotation.Nullable;

import java.util.UUID;

import blagodarie.rating.model.IAnyTextInfo;

public final class AnyTextInfo
        implements IAnyTextInfo {

    public static final AnyTextInfo EMPTY_ANY_TEXT = new AnyTextInfo(null, 0,0,0,0, null, null);

    @Nullable
    private final UUID mAnyTextId;

    private final int mFame;

    private final int mTrustCount;

    private final int mMistrustCount;

    private final int mSumThanksCount;

    @Nullable
    private final Integer mThanksCount;

    @Nullable
    private final Boolean mIsTrust;

    public AnyTextInfo (
            @Nullable final UUID anyTextId,
            final int fame,
            final int trustCount,
            final int mistrustCount,
            final int sumThanksCount,
            @Nullable final Integer thanksCount,
            @Nullable final Boolean isTrust
    ) {
        mAnyTextId = anyTextId;
        mFame = fame;
        mTrustCount = trustCount;
        mMistrustCount = mistrustCount;
        mSumThanksCount = sumThanksCount;
        mThanksCount = thanksCount;
        mIsTrust = isTrust;
    }

    @Nullable
    @Override
    public UUID getAnyTextId () {
        return mAnyTextId;
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
