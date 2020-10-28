package blagodarie.rating.server;

import androidx.annotation.NonNull;

public final class SignInResponse
        extends _ServerApiResponse {

    @NonNull
    private final String mAuthToken;

    SignInResponse (
            @NonNull final String authToken
    ) {
        mAuthToken = authToken;
    }

    @NonNull
    public String getAuthToken () {
        return mAuthToken;
    }
}
