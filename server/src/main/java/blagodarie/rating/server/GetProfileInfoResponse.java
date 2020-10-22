package blagodarie.rating.server;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public final class GetProfileInfoResponse
        extends _ServerApiResponse {

    @NonNull
    private final String mPhoto;

    @NonNull
    private final String mFirstName;

    @NonNull
    private final String mMiddleName;

    @NonNull
    private final String mLastName;

    @NonNull
    private final String mCardNumber;

    private final int mFame;

    private final int mSumThanksCount;

    private final int mTrustCount;

    private final int mMistrustCount;

    @Nullable
    private final Integer mThanksCount;

    @Nullable
    private final Boolean mIsTrust;

    @NonNull
    private final List<GetThanksUsersResponse.ThanksUser> mThanksUsers;

    public GetProfileInfoResponse (
            @NonNull final String photo,
            @NonNull final String firstName,
            @NonNull final String middleName,
            @NonNull final String lastName,
            @NonNull final String cardNumber,
            final int fame,
            final int sumThanksCount,
            final int trustCount,
            final int mistrustCount,
            @Nullable final Integer thanksCount,
            @Nullable final Boolean isTrust,
            @NonNull final List<GetThanksUsersResponse.ThanksUser> thanksUsers
    ) {
        mPhoto = photo;
        mFirstName = firstName;
        mMiddleName = middleName;
        mLastName = lastName;
        mCardNumber = cardNumber;
        mFame = fame;
        mSumThanksCount = sumThanksCount;
        mTrustCount = trustCount;
        mMistrustCount = mistrustCount;
        mThanksCount = thanksCount;
        mIsTrust = isTrust;
        mThanksUsers = Collections.unmodifiableList(thanksUsers);
    }

    @NonNull
    public String getPhoto () {
        return mPhoto;
    }

    @NonNull
    public final String getFirstName () {
        return mFirstName;
    }

    @NonNull
    public final String getMiddleName () {
        return mMiddleName;
    }

    @NonNull
    public final String getLastName () {
        return mLastName;
    }

    @NonNull
    public final String getCardNumber () {
        return mCardNumber;
    }

    public final int getFame () {
        return mFame;
    }

    public final int getSumThanksCount () {
        return mSumThanksCount;
    }

    public final int getTrustCount () {
        return mTrustCount;
    }

    public final int getMistrustCount () {
        return mMistrustCount;
    }

    @Nullable
    public final Integer getThanksCount () {
        return mThanksCount;
    }

    @Nullable
    public final Boolean getIsTrust () {
        return mIsTrust;
    }

    @NonNull
    public final List<GetThanksUsersResponse.ThanksUser> getThanksUsers () {
        return mThanksUsers;
    }
}
