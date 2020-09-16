package blagodarie.rating;

import android.accounts.Account;
import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.UUID;

import blagodarie.rating.server.AddOperationToAnyTextRequest;
import blagodarie.rating.server.OperationToAnyText;
import blagodarie.rating.server.ServerApiClient;
import blagodarie.rating.ui.AccountProvider;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;

public final class OperationToAnyTextManager
        extends OperationManager {

    private static final String TAG = OperationToAnyTextManager.class.getSimpleName();

    public interface OnAddOperationCompleteListener {
        void onAddOperationComplete (@Nullable final UUID textId);
    }

    public void createOperationToAnyText (
            @NonNull final Activity activity,
            @NonNull final CompositeDisposable disposables,
            @NonNull final Account account,
            @Nullable final UUID anyTextIdTo,
            @NonNull final String anyText,
            @NonNull final OperationType operationType,
            @NonNull final OnAddOperationCompleteListener onAddOperationCompleteListener
    ) {
        Log.d(TAG, "createOperation");
        requireOperationComment(activity, comment -> {
            final OperationToAnyText operationToAnyText = new OperationToAnyText(anyText, anyTextIdTo, operationType.getId(), System.currentTimeMillis(), comment);
            AccountProvider.getAuthToken(activity, account, authToken -> {
                if (authToken != null) {
                    addOperation(activity, disposables, authToken, new AddOperationToAnyTextRequest(operationToAnyText), onAddOperationCompleteListener);
                }
            });
        });
    }

    void addOperation (
            @NonNull final Context context,
            @NonNull final CompositeDisposable disposables,
            @NonNull final String authToken,
            @NonNull final AddOperationToAnyTextRequest addOperationToAnyTextRequest,
            @NonNull final OnAddOperationCompleteListener onAddOperationCompleteListener
    ) {
        Log.d(TAG, "addOperation");

        final ServerApiClient serverApiClient = new ServerApiClient();
        serverApiClient.setAuthToken(authToken);

        disposables.add(
                Observable.
                        fromCallable(() -> serverApiClient.execute(addOperationToAnyTextRequest)).
                        subscribeOn(Schedulers.io()).
                        observeOn(AndroidSchedulers.mainThread()).
                        subscribe(
                                addOperationResponse -> {
                                    onAddOperationCompleteListener.onAddOperationComplete(addOperationResponse.getAnyTextId());
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
