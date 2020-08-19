package blagodarie.rating;

import android.accounts.Account;
import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.databinding.DataBindingUtil;

import org.json.JSONException;
import org.json.JSONObject;

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

    public enum Type {
        TO_USER, TO_ANY_TEXT
    }

    public interface OnAddOperationCompleteListener {
        void onAddOperationComplete (@Nullable final String textId);
    }

    @NonNull
    private final Type mType;

    @NonNull
    private final OnAddOperationCompleteListener mOnAddOperationCompleteListener;

    public OperationManager (
            @NonNull final Type type,
            @NonNull final OnAddOperationCompleteListener onAddOperationCompleteListener
    ) {
        mType = type;
        mOnAddOperationCompleteListener = onAddOperationCompleteListener;
    }

    public void createOperation (
            @NonNull final Activity activity,
            @NonNull final CompositeDisposable disposables,
            @NonNull final Account account,
            @Nullable final UUID idTo,
            @Nullable final String textTo,
            @NonNull final OperationType operationType
    ) {
        Log.d(TAG, "createOperation");
        requireOperationComment(activity, disposables, account, idTo, textTo, operationType);
    }

    private void requireOperationComment (
            @NonNull final Activity activity,
            @NonNull final CompositeDisposable disposables,
            @NonNull final Account account,
            @Nullable final UUID idTo,
            @Nullable final String textTo,
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
                                    addOperation(activity, disposables, authToken, idTo, textTo, operationType, operationComment);
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
            @Nullable final UUID idTo,
            @Nullable final String textTo,
            @NonNull final OperationType operationType,
            final String operationComment
    ) {
        Log.d(TAG, "addOperation");

        final String content = createContent(idTo, textTo, operationType, operationComment);
        assert content != null;

        disposables.add(
                Observable.
                        fromCallable(() -> ServerConnector.sendAuthRequestAndGetResponse((mType == Type.TO_USER ? "addoperation" : "addtextoperation"), authToken, content)).
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
            String textId = null;
            if (serverApiResponse.getBody() != null){

                final String responseBody = serverApiResponse.getBody();
                try {
                    final JSONObject json = new JSONObject(responseBody);
                    textId = json.getString("text_id_to");
                } catch (JSONException e){
                    //do nothing
                }
            }
            mOnAddOperationCompleteListener.onAddOperationComplete(textId);
        } else {
            Toast.makeText(context, R.string.err_msg_add_thanks_failed, Toast.LENGTH_LONG).show();
        }
    }

    private String createContent (
            @Nullable final UUID idTo,
            @Nullable final String anyTextTo,
            @NonNull final OperationType operationType,
            @NonNull final String operationComment
    ) {
        return (mType == Type.TO_USER ?
                (idTo != null ? createContentForUserTo(idTo, operationType, operationComment) : null) :
                createContentForTextTo(idTo, anyTextTo, operationType, operationComment));
    }

    private String createContentForUserTo (
            @NonNull final UUID userIdTo,
            @NonNull final OperationType operationType,
            @NonNull final String operationComment
    ) {
        return String.format(Locale.ENGLISH, "{\"user_id_to\":\"%s\",\"operation_type_id\":%d,\"timestamp\":%d,\"comment\":\"%s\"}", userIdTo.toString(), operationType.getId(), System.currentTimeMillis(), operationComment);
    }

    private String createContentForTextTo (
            @Nullable final UUID anyTextIdTo,
            @Nullable final String anyTextTo,
            @NonNull final OperationType operationType,
            @NonNull final String operationComment
    ) {
        return String.format(Locale.ENGLISH, "{" + (anyTextIdTo != null ? "\"text_id_to\":\"%s\"" : "\"text\":\"%s\"") + ",\"operation_type_id\":%d,\"timestamp\":%d,\"comment\":\"%s\"}", (anyTextIdTo != null ? anyTextIdTo.toString() : anyTextTo), operationType.getId(), System.currentTimeMillis(), operationComment);
    }
}
