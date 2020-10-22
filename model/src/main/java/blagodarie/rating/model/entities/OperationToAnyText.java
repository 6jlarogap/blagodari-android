package blagodarie.rating.model.entities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.Date;
import java.util.UUID;

public final class OperationToAnyText
        extends AbstractOperation {

    @Nullable
    private final UUID mAnyTextIdTo;

    public OperationToAnyText (
            @NonNull final UUID userIdFrom,
            @Nullable final UUID anyTextIdTo,
            @NonNull final OperationType operationType,
            @Nullable final String comment,
            @NonNull final Date timestamp
    ) {
        super(userIdFrom, operationType, comment, timestamp);
        mAnyTextIdTo = anyTextIdTo;
    }

    @Nullable
    public final UUID getIdTo () {
        return mAnyTextIdTo;
    }
}
