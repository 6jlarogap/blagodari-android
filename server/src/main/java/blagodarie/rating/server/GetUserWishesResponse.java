package blagodarie.rating.server;

import androidx.annotation.NonNull;

import java.util.Collections;
import java.util.List;

import blagodarie.rating.model.IWish;

public final class GetUserWishesResponse
        extends _ServerApiResponse {

    @NonNull
    private final List<IWish> mWishes;

    GetUserWishesResponse (
            @NonNull final List<IWish> wishes
    ) {
        mWishes = Collections.unmodifiableList(wishes);
    }

    @NonNull
    public final List<IWish> getWishes () {
        return mWishes;
    }
}
