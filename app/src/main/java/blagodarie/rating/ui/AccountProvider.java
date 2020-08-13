package blagodarie.rating.ui;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AuthenticatorException;
import android.accounts.OperationCanceledException;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.io.IOException;
import java.util.Arrays;

import blagodarie.rating.R;

public final class AccountProvider {

    private static final String TAG = AccountProvider.class.getSimpleName();

    public interface OnAccountSelectListener {
        void onAccountSelected (@Nullable final Account account);
    }

    public interface OnGetTokenListener {
        void onGetToken (@Nullable final String authToken);
    }

    public interface OnTokenErrorListener {
        void onError (@NonNull final Throwable throwable);
    }

    private AccountProvider (
    ) {
    }

    public static void getAccount (
            @NonNull final Context context,
            @NonNull final OnAccountSelectListener onAccountSelectListener
    ) {
        Log.d(TAG, "chooseAccount");

        final AccountManager accountManager = AccountManager.get(context);

        final Account[] accounts = accountManager.getAccountsByType(context.getString(R.string.account_type));
        if (accounts.length == 0) {
            onAccountSelectListener.onAccountSelected(null);
        } else if (accounts.length == 1) {
            onAccountSelectListener.onAccountSelected(accounts[0]);
        } else {
            showAccountPicker(context, accounts, onAccountSelectListener);
        }
    }

    private static void showAccountPicker (
            @NonNull final Context context,
            @NonNull final Account[] accounts,
            @NonNull final OnAccountSelectListener onAccountSelectListener
    ) {
        Log.d(TAG, "showAccountPicker accounts=" + Arrays.toString(accounts));
        final String[] names = new String[accounts.length];
        for (int i = 0; i < accounts.length; i++) {
            names[i] = accounts[i].name;
        }

        new AlertDialog.
                Builder(context).
                setTitle(R.string.rqst_choose_account).
                setCancelable(false).
                setAdapter(
                        new ArrayAdapter<>(
                                context,
                                android.R.layout.simple_list_item_1, names),
                        (dialog, which) -> onAccountSelectListener.onAccountSelected(accounts[which])
                ).
                create().
                show();
    }

    public static void getAuthToken (
            @NonNull final Activity activity,
            @NonNull final Account account,
            @NonNull final OnGetTokenListener onGetTokenListener
    ) {
        Log.d(TAG, "getAuthToken");
        AccountManager.get(activity).getAuthToken(
                account,
                activity.getString(R.string.token_type),
                null,
                activity,
                accountManagerFuture -> {
                    try {
                        final Bundle bundle = accountManagerFuture.getResult();
                        if (bundle != null) {
                            onGetTokenListener.onGetToken(bundle.getString(AccountManager.KEY_AUTHTOKEN));
                        } else {
                            onGetTokenListener.onGetToken(null);
                        }
                    } catch (AuthenticatorException | IOException | OperationCanceledException e) {
                        Log.e(TAG, Log.getStackTraceString(e));
                    }
                },
                null
        );
    }

    public static void getAuthToken (
            @NonNull final Activity activity,
            @NonNull final Account account,
            @NonNull final OnGetTokenListener onGetTokenListener,
            @NonNull final OnTokenErrorListener onTokenErrorListener
    ) {
        Log.d(TAG, "getAuthToken");
        AccountManager.get(activity).getAuthToken(
                account,
                activity.getString(R.string.token_type),
                null,
                activity,
                accountManagerFuture -> {
                    try {
                        final Bundle bundle = accountManagerFuture.getResult();
                        if (bundle != null) {
                            onGetTokenListener.onGetToken(bundle.getString(AccountManager.KEY_AUTHTOKEN));
                        } else {
                            onGetTokenListener.onGetToken(null);
                        }
                    } catch (AuthenticatorException | IOException | OperationCanceledException e) {
                        Log.e(TAG, Log.getStackTraceString(e));
                        onTokenErrorListener.onError(e);
                    }
                },
                null
        );
    }
}
