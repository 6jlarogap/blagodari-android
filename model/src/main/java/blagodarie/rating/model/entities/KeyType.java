package blagodarie.rating.model.entities;

import androidx.annotation.NonNull;

import blagodarie.rating.model.R;

public enum KeyType {

    PHONE(1, R.string.key_type_phone),
    EMAIL(2, R.string.key_type_email),
    CREDIT_CARD(4, R.string.key_type_credit_card),
    LINK(5, R.string.key_type_link);

    private final int mId;

    private final int mNameResId;

    KeyType (
            final int id,
            final int nameResId
    ) {
        mId = id;
        mNameResId = nameResId;
    }

    public final int getId () {
        return mId;
    }

    public int getNameResId () {
        return mNameResId;
    }

    @NonNull
    public static KeyType getById (final int id) {
        KeyType keyType;
        switch (id) {
            case 1:
                keyType = PHONE;
                break;
            case 2:
                keyType = EMAIL;
                break;
            case 4:
                keyType = CREDIT_CARD;
                break;
            case 5:
                keyType = LINK;
                break;
            default:
                throw new IllegalArgumentException("Unknown key type");
        }
        return keyType;
    }
}
