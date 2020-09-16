package blagodarie.rating.server;

import androidx.annotation.NonNull;

import java.util.UUID;

public final class AddOperationToAnyTextResponse
        extends _ServerApiResponse {

    @NonNull
    private final UUID mAnyTextId;

    public AddOperationToAnyTextResponse (
            @NonNull final UUID anyTextId) {
        mAnyTextId = anyTextId;
    }

    @NonNull
    public final UUID getAnyTextId () {
        return mAnyTextId;
    }
}
