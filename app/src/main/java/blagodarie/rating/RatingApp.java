package blagodarie.rating;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AccountManagerCallback;
import android.accounts.AuthenticatorException;
import android.accounts.OperationCanceledException;
import android.app.Activity;
import android.app.Application;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;

import java.io.IOException;

import blagodarie.rating.auth.AccountGeneral;
import blagodarie.rating.ui.profile.ProfileActivity;

public final class RatingApp
        extends Application {

    private static final String TAG = ProfileActivity.class.getSimpleName();

    @Override
    public final void onCreate () {
        Log.d(TAG, "start RatingApp");
        super.onCreate();
    }

    public static void getAuthToken (
            @NonNull final Activity activity,
            @NonNull final Account account,
            @NonNull final AccountManagerCallback<Bundle> callback
    ) {
        Log.d(TAG, "getAuthToken");
        AccountManager.get(activity).getAuthToken(
                account,
                activity.getString(R.string.token_type),
                null,
                activity,
                callback,
                null
        );
    }
}
