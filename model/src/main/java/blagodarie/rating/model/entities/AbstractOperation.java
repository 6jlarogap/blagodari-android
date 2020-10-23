package blagodarie.rating.model.entities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.Date;
import java.util.UUID;

import blagodarie.rating.model.IOperation;

abstract class AbstractOperation
        implements IOperation {

    @NonNull
    private final UUID mUserIdFrom;

    @NonNull
    private final OperationType mOperationType;

    @Nullable
    private final String mComment;

    @NonNull
    private final Date mTimestamp;

    public AbstractOperation (
            @NonNull final UUID userIdFrom,
            @NonNull final OperationType operationType,
            @Nullable final String comment,
            @NonNull final Date timestamp
    ) {
        mUserIdFrom = userIdFrom;
        mOperationType = operationType;
        mComment = comment;
        mTimestamp = timestamp;
    }

    @NonNull
    public final UUID getUserIdFrom () {
        return mUserIdFrom;
    }

    @NonNull
    public final OperationType getOperationType () {
        return mOperationType;
    }

    @NonNull
    public final String getComment () {
        return mComment;
    }

    @NonNull
    public final Date getTimestamp () {
        return mTimestamp;
    }
}
