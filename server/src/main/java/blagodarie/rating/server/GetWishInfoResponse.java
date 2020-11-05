package blagodarie.rating.server;

import androidx.annotation.Nullable;

import blagodarie.rating.model.IWish;

public final class GetWishInfoResponse
        extends _ServerApiResponse {
    @Nullable
    private final IWish mWish;

    GetWishInfoResponse (
            @Nullable final IWish wish
    ) {
        mWish = wish;
    }

    @Nullable
    public IWish getWish () {
        return mWish;
    }
}
