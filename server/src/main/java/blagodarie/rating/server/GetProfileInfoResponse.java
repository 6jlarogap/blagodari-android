package blagodarie.rating.server;

import androidx.annotation.Nullable;

import blagodarie.rating.model.IProfileInfo;

public final class GetProfileInfoResponse
        extends _ServerApiResponse {

    @Nullable
    private final IProfileInfo mProfileInfo;

    public GetProfileInfoResponse (
            @Nullable final IProfileInfo profileInfo
    ) {
        mProfileInfo = profileInfo;
    }

    @Nullable
    public final IProfileInfo getProfileInfo () {
        return mProfileInfo;
    }

}
