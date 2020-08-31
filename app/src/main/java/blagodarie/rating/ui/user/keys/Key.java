package blagodarie.rating.ui.user.keys;

import androidx.annotation.NonNull;

import java.util.UUID;

public final class Key {

    private final long mId;

    @NonNull
    private final UUID mOwnerId;

    @NonNull
    private final String mValue;

    @NonNull
    private final KeyType mKeyType;

    Key (
            final long id,
            @NonNull final UUID ownerId,
            @NonNull final String value,
            @NonNull final KeyType keyType
    ) {
        mId = id;
        mOwnerId = ownerId;
        mValue = value;
        mKeyType = keyType;
    }

    public final long getId () {
        return mId;
    }

    @NonNull
    public final UUID getOwnerId () {
        return mOwnerId;
    }

    @NonNull
    public final String getValue () {
        return mValue;
    }

    @NonNull
    public final KeyType getKeyType () {
        return mKeyType;
    }
}
