package blagodarie.rating.model.entities;

import androidx.annotation.NonNull;

import blagodarie.rating.model.IKeyPair;

public class KeyPair
        implements IKeyPair {

    @NonNull
    private final String mValue;

    @NonNull
    private final KeyType mKeyType;

    public KeyPair (
            @NonNull final String value,
            @NonNull final KeyType keyType
    ) {
        mValue = value;
        mKeyType = keyType;
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
