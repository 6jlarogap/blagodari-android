package blagodarie.rating.repository;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import blagodarie.rating.model.IOperation;
import blagodarie.rating.model.entities.OperationToAnyText;
import blagodarie.rating.model.entities.OperationToUser;

public interface AsyncRepository {

    interface OnCompleteListener<T> {
        void onComplete (@NonNull final T value);
    }

    interface OnErrorListener {
        void onError (@NonNull final Throwable throwable);
    }

    void insertOperationToUser (
            @NonNull final OperationToUser operation,
            final OnCompleteListener<Void> onCompleteListener,
            final OnErrorListener onErrorListener
    );

    void insertOperationToAnyText (
            @NonNull final OperationToAnyText operation,
            @Nullable final String anyText
    );

}
