package blagodarie.rating;

import android.accounts.Account;
import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.UUID;

import blagodarie.rating.server.AddOperationToUserRequest;
import blagodarie.rating.server.OperationToUser;
import blagodarie.rating.server.ServerApiClient;
import blagodarie.rating.ui.AccountProvider;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;

public final class OperationToUserManager
        extends OperationManager {

    private static final String TAG = OperationToUserManager.class.getSimpleName();

    public interface OnAddOperationCompleteListener {
        void onAddOperationComplete ();
    }

    public void createOperationToUser (
            @NonNull final Activity activity,
            @NonNull final CompositeDisposable disposables,
            @NonNull final Account account,
            @NonNull final UUID userIdTo,
            @NonNull final OperationType operationType,
            @NonNull final OnAddOperationCompleteListener onAddOperationCompleteListener
    ) {
        Log.d(TAG, "createOperation");
        requireOperationComment(activity, comment -> {
            final OperationToUser operationToUser = new OperationToUser(userIdTo, operationType.getId(), System.currentTimeMillis(), comment);
            AccountProvider.getAuthToken(activity, account, authToken -> {
                if (authToken != null) {
                    addOperation(activity, disposables, authToken, new AddOperationToUserRequest(operationToUser), onAddOperationCompleteListener);
                }
            });
        });
    }

    void addOperation (
            @NonNull final Context context,
            @NonNull final CompositeDisposable disposables,
            @NonNull final String authToken,
            @NonNull final AddOperationToUserRequest addOperationToUserRequest,
            @NonNull final OnAddOperationCompleteListener onAddOperationCompleteListener
    ) {
        Log.d(TAG, "addOperation");

        final ServerApiClient serverApiClient = new ServerApiClient();
        serverApiClient.setAuthToken(authToken);

        disposables.add(
                Observable.
                        fromCallable(() -> serverApiClient.execute(addOperationToUserRequest)).
                        subscribeOn(Schedulers.io()).
                        observeOn(AndroidSchedulers.mainThread()).
                        subscribe(
                                addOperationResponse -> {
                                    onAddOperationCompleteListener.onAddOperationComplete();
                                    Toast.makeText(context, R.string.info_msg_saved, Toast.LENGTH_LONG).show();
                                },
                                throwable -> {
                                    Log.e(TAG, Log.getStackTraceString(throwable));
                                    Toast.makeText(context, throwable.getMessage(), Toast.LENGTH_LONG).show();
                                }
                        )
        );
    }
}
