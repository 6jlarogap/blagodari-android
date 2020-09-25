package blagodarie.rating.server;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.UUID;

abstract class Operation {

    @Nullable
    private final UUID mIdTo;

    private final int mOperationTypeId;

    private final long mTimestamp;

    @NonNull
    private final String mComment;

    public Operation (
            @Nullable final UUID idTo,
            final int operationTypeId,
            final long timestamp,
            @NonNull final String comment
    ) {
        mIdTo = idTo;
        mOperationTypeId = operationTypeId;
        mTimestamp = timestamp;
        mComment = comment;
    }

    @Nullable
    UUID getIdTo () {
        return mIdTo;
    }

    public final int getOperationTypeId () {
        return mOperationTypeId;
    }

    public final long getTimestamp () {
        return mTimestamp;
    }

    @NonNull
    public final String getComment () {
        return mComment;
    }
}
