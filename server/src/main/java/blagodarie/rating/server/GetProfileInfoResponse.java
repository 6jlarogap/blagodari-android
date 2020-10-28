package blagodarie.rating.server;

import androidx.annotation.Nullable;

import blagodarie.rating.model.IProfile;

public final class GetProfileInfoResponse
        extends _ServerApiResponse {

    @Nullable
    private final IProfile mProfileInfo;

    public GetProfileInfoResponse (
            @Nullable final IProfile profileInfo
    ) {
        mProfileInfo = profileInfo;
    }

    @Nullable
    public final IProfile getProfileInfo () {
        return mProfileInfo;
    }

}
