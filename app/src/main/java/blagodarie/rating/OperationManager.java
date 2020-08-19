package blagodarie.rating;

import android.accounts.Account;
import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.databinding.DataBindingUtil;

import java.util.Locale;
import java.util.UUID;

import blagodarie.rating.databinding.EnterOperationCommentDialogBinding;
import blagodarie.rating.server.ServerApiResponse;
import blagodarie.rating.server.ServerConnector;
import blagodarie.rating.ui.AccountProvider;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;

public final class OperationManager {

    private static final String TAG = OperationManager.class.getSimpleName();

    public interface OnAddOperationCompleteListener {
        void onAddOperationComplete ();
    }

    @NonNull
    private final OnAddOperationCompleteListener mOnAddOperationCompleteListener;

    public OperationManager (@NonNull final OnAddOperationCompleteListener onAddOperationCompleteListener) {
        mOnAddOperationCompleteListener = onAddOperationCompleteListener;
    }

    public void createOperation (
            @NonNull final Activity activity,
            @NonNull final CompositeDisposable disposables,
            @NonNull final Account account,
            @NonNull final UUID userIdTo,
            @NonNull final OperationType operationType
    ) {
        Log.d(TAG, "createOperation");
        requireOperationComment(activity, disposables, account, userIdTo, operationType);
    }

    private void requireOperationComment (
            @NonNull final Activity activity,
            @NonNull final CompositeDisposable disposables,
            @NonNull final Account account,
            @NonNull final UUID userIdTo,
            @NonNull final OperationType operationType
    ) {
        Log.d(TAG, "requireOperationComment");
        final InputMethodManager imm = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
        final EnterOperationCommentDialogBinding binding = DataBindingUtil.inflate(LayoutInflater.from(activity), R.layout.enter_operation_comment_dialog, null, false);
        new AlertDialog.
                Builder(activity).
                setCancelable(false).
                setTitle(R.string.txt_comment).
                setView(binding.getRoot()).
                setNegativeButton(android.R.string.cancel, (dialogInterface, i) -> imm.hideSoftInputFromWindow(binding.etOperationComment.getWindowToken(), 0)).
                setPositiveButton(android.R.string.ok,
                        (dialogInterface, i) -> {
                            imm.hideSoftInputFromWindow(binding.etOperationComment.getWindowToken(), 0);
                            final String operationComment = binding.etOperationComment.getText().toString();
                            AccountProvider.getAuthToken(activity, account, authToken -> {
                                if (authToken != null) {
                                    addOperation(activity, disposables, authToken, userIdTo, operationType, operationComment);
                                }
                            });
                        }).
                create().
                show();
        binding.etOperationComment.requestFocus();
        imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY);
    }


    private void addOperation (
            @NonNull final Context context,
            @NonNull final CompositeDisposable disposables,
            @NonNull final String authToken,
            @NonNull final UUID userIdTo,
            @NonNull final OperationType operationType,
            final String operationComment
    ) {
        Log.d(TAG, "addOperation");

        final String content = String.format(Locale.ENGLISH, "{\"user_id_to\":\"%s\",\"operation_type_id\":%d,\"timestamp\":%d,\"comment\":\"%s\"}", userIdTo.toString(), operationType.getId(), System.currentTimeMillis(), operationComment);

        disposables.add(
                Observable.
                        fromCallable(() -> ServerConnector.sendAuthRequestAndGetResponse("addoperation", authToken, content)).
                        subscribeOn(Schedulers.io()).
                        observeOn(AndroidSchedulers.mainThread()).
                        subscribe(
                                serverApiResponse -> {
                                    Log.d(TAG, serverApiResponse.toString());
                                    onAddOperationComplete(context, serverApiResponse, operationType);
                                },
                                throwable -> {
                                    Log.e(TAG, Log.getStackTraceString(throwable));
                                    Toast.makeText(context, throwable.getMessage(), Toast.LENGTH_LONG).show();
                                }
                        )
        );
    }

    private void onAddOperationComplete (
            @NonNull final Context context,
            @NonNull final ServerApiResponse serverApiResponse,
            @NonNull final OperationType operationType
    ) {
        Log.d(TAG, "onAddOperationComplete serverApiResponse=" + serverApiResponse);
        if (serverApiResponse.getCode() == 200) {
            switch (operationType) {
                case THANKS: {
                    Toast.makeText(context, R.string.info_msg_add_thanks_complete, Toast.LENGTH_LONG).show();
                    break;
                }
                case MISTRUST: {
                    Toast.makeText(context, R.string.info_msg_trust_is_lost, Toast.LENGTH_LONG).show();
                    break;
                }
                case MISTRUST_CANCEL: {
                    Toast.makeText(context, R.string.info_msg_trust_restored, Toast.LENGTH_LONG).show();
                    break;
                }
            }
            mOnAddOperationCompleteListener.onAddOperationComplete();
        } else {
            Toast.makeText(context, R.string.err_msg_add_thanks_failed, Toast.LENGTH_LONG).show();
        }
    }
}
