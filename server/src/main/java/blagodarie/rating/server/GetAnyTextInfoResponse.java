package blagodarie.rating.server;

import androidx.annotation.Nullable;

import blagodarie.rating.model.IAnyTextInfo;

public final class GetAnyTextInfoResponse
        extends _ServerApiResponse {

    @Nullable
    private final IAnyTextInfo mAnyTextInfo;

    public GetAnyTextInfoResponse (
            @Nullable final IAnyTextInfo anyTextInfo
    ) {
        mAnyTextInfo = anyTextInfo;
    }

    @Nullable
    public final IAnyTextInfo getAnyTextInfo () {
        return mAnyTextInfo;
    }

}
