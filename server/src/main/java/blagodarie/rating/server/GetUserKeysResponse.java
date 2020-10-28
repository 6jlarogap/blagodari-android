package blagodarie.rating.server;

import androidx.annotation.NonNull;

import java.util.Collections;
import java.util.List;

import blagodarie.rating.model.IKey;

public final class GetUserKeysResponse
        extends _ServerApiResponse {

    @NonNull
    private final List<IKey> mKeys;

    public GetUserKeysResponse (
            @NonNull final List<IKey> keys
    ) {
        mKeys = Collections.unmodifiableList(keys);
    }

    @NonNull
    public final List<IKey> getKeys () {
        return mKeys;
    }

}
