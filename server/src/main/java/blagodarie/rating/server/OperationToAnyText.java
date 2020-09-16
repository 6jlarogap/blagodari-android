package blagodarie.rating.server;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.UUID;

public final class OperationToAnyText
        extends Operation {

    @NonNull
    private final String mAnyText;

    public OperationToAnyText (
            @NonNull final String anyText,
            @Nullable final UUID idTo,
            final int operationTypeId,
            final long timestamp,
            @NonNull final String comment) {
        super(idTo, operationTypeId, timestamp, comment);
        mAnyText = anyText;
    }

    @NonNull
    public final String getAnyText () {
        return mAnyText;
    }

    @Nullable
    public final UUID getAnyTextIdTo () {
        return getIdTo();
    }
}
