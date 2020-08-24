package blagodarie.rating;

import androidx.annotation.Nullable;

public enum OperationType {

    THANKS(1, R.string.operation_type_name_thanks),
    MISTRUST(2, R.string.operation_type_name_trustless),
    MISTRUST_CANCEL(3, R.string.operation_type_name_trustless_cancel);

    final int mId;

    final int mNameResId;

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
                operationType = MISTRUST_CANCEL;
                break;
            default:
                operationType = null;
        }
        return operationType;
    }
}
