package blagodarie.rating.server;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.UUID;

public final class SignUpResponse
        extends _ServerApiResponse {

    @NonNull
    private final UUID mUserId;

    @NonNull
    private final String mFirstName;

    @NonNull
    private final String mMiddleName;

    @NonNull
    private final String mLastName;

    @NonNull
    private final String mPhoto;

    @NonNull
    private final String mAuthToken;

    public SignUpResponse (
            @NonNull final UUID userId,
            @NonNull final String firstName,
            @NonNull final String middleName,
            @NonNull final String lastName,
            @NonNull final String photo,
            @NonNull final String authToken) {
        mUserId = userId;
        mFirstName = firstName;
        mMiddleName = middleName;
        mLastName = lastName;
        mPhoto = photo;
        mAuthToken = authToken;
    }

    @NonNull
    public final UUID getUserId () {
        return mUserId;
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
    public final String getPhoto () {
        return mPhoto;
    }

    @NonNull
    public final String getAuthToken () {
        return mAuthToken;
    }
}
