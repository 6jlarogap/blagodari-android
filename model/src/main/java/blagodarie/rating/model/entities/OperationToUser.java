package blagodarie.rating.model.entities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.Date;
import java.util.UUID;

public final class OperationToUser
        extends AbstractOperation {

    @NonNull
    private final UUID mUserIdTo;

    public OperationToUser (
            @NonNull final UUID userIdFrom,
            @NonNull final UUID userIdTo,
            @NonNull final OperationType operationType,
            @Nullable final String comment,
            @NonNull final Date timestamp
    ) {
        super(userIdFrom, operationType, comment, timestamp);
        mUserIdTo = userIdTo;
    }

    @NonNull
    public final UUID getIdTo () {
        return mUserIdTo;
    }
}
