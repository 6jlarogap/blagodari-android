package blagodarie.rating.server;

import androidx.annotation.NonNull;

import java.util.UUID;

public final class OperationToUser
        extends Operation {

    public OperationToUser (
            @NonNull final UUID userIdTo,
            final int operationTypeId,
            final long timestamp,
            @NonNull final String comment) {
        super(userIdTo, operationTypeId, timestamp, comment);
    }

    @NonNull
    public UUID getUserIdTo () {
        if (getIdTo() != null) {
            return getIdTo();
        } else {
            throw new IllegalArgumentException("UserIdTo is null");
        }
    }
}
