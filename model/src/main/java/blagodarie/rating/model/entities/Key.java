package blagodarie.rating.model.entities;

import androidx.annotation.NonNull;

import java.util.UUID;

import blagodarie.rating.model.IKey;

public final class Key
        extends KeyPair
        implements IKey {

    @NonNull
    private final Long mId;

    @NonNull
    private final UUID mOwnerId;

    public Key (
            @NonNull final Long id,
            @NonNull final UUID ownerId,
            @NonNull final String value,
            @NonNull final KeyType keyType
    ) {
        super(value, keyType);
        mId = id;
        mOwnerId = ownerId;
    }

    @NonNull
    public final Long getId () {
        return mId;
    }

    @NonNull
    public final UUID getOwnerId () {
        return mOwnerId;
    }

}
