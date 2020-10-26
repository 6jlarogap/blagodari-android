package blagodarie.rating.server;

import androidx.annotation.NonNull;

import java.util.Collections;
import java.util.List;

import blagodarie.rating.model.IDisplayOperation;

public final class GetOperationsResponse
        extends _ServerApiResponse {

    @NonNull
    private final List<IDisplayOperation> mOperations;

    public GetOperationsResponse (
            @NonNull final List<IDisplayOperation> operations
    ) {
        mOperations = Collections.unmodifiableList(operations);
    }

    @NonNull
    public final List<IDisplayOperation> getOperations () {
        return mOperations;
    }
}
