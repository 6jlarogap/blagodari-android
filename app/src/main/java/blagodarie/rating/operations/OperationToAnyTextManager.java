package blagodarie.rating.operations;

import android.app.Activity;
import android.util.Log;

import androidx.annotation.NonNull;

import java.util.Date;
import java.util.UUID;

import blagodarie.rating.model.entities.OperationType;
import blagodarie.rating.repository.AsyncRepository;

public final class OperationToAnyTextManager
        extends OperationManager {

    private static final String TAG = OperationToAnyTextManager.class.getSimpleName();

    public void createOperationToAnyText (
            @NonNull final Activity activity,
            @NonNull final UUID userIdFrom,
            @NonNull final UUID anyTextIdTo,
            @NonNull final OperationType operationType,
            @NonNull final String anyText,
            @NonNull final AsyncRepository repository,
            @NonNull final AsyncRepository.OnCompleteListener onCompleteListener,
            @NonNull final AsyncRepository.OnErrorListener onErrorListener
    ) {
        Log.d(TAG, "createOperation");
        showOperationCommentDialog(activity, comment -> {
            final blagodarie.rating.model.entities.OperationToAnyText operation = new blagodarie.rating.model.entities.OperationToAnyText(userIdFrom, anyTextIdTo, operationType, comment, new Date());
            repository.insertOperationToAnyText(operation, anyText, onCompleteListener, onErrorListener);
        });
    }

}