package blagodarie.rating.operations;

import android.app.Activity;
import android.util.Log;

import androidx.annotation.NonNull;

import java.util.Date;
import java.util.UUID;

import blagodarie.rating.model.entities.OperationType;
import blagodarie.rating.repository.AsyncRepository;

public final class OperationToUserManager
        extends OperationManager {

    private static final String TAG = OperationToUserManager.class.getSimpleName();

    public void createOperationToUser (
            @NonNull final Activity activity,
            @NonNull final UUID userIdFrom,
            @NonNull final UUID userIdTo,
            @NonNull final OperationType operationType,
            @NonNull final AsyncRepository repository,
            @NonNull final AsyncRepository.OnCompleteListener onCompleteListener,
            @NonNull final AsyncRepository.OnErrorListener onErrorListener
    ) {
        Log.d(TAG, "createOperation");
        showOperationCommentDialog(activity, comment -> {
            final blagodarie.rating.model.entities.OperationToUser operationToUser = new blagodarie.rating.model.entities.OperationToUser(userIdFrom, userIdTo, operationType, comment, new Date());
            repository.insertOperationToUser(operationToUser, onCompleteListener, onErrorListener);
        });
    }

}
