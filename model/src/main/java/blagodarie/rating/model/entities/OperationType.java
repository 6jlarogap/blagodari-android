package blagodarie.rating.model.entities;

import androidx.annotation.Nullable;

import blagodarie.rating.model.R;

public enum OperationType {

    THANKS(1, R.string.operation_type_name_thanks),
    MISTRUST(2, R.string.operation_type_name_mistrust),
    TRUST(3, R.string.operation_type_name_trust),
    NULLIFY_TRUST(4, R.string.operation_type_name_nullify_trust);

    private final int mId;

    private final int mNameResId;

    OperationType (
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

    @Nullable
    public static OperationType getById (final int id) {
        OperationType operationType;
        switch (id) {
            case 1:
                operationType = THANKS;
                break;
            case 2:
                operationType = MISTRUST;
                break;
            case 3:
                operationType = TRUST;
                break;
            case 4:
                operationType = NULLIFY_TRUST;
                break;
            default:
                operationType = null;
        }
        return operationType;
    }
}
