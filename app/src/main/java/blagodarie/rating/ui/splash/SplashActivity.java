package blagodarie.rating.ui.splash;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AccountManagerFuture;
import android.accounts.AuthenticatorException;
import android.accounts.OperationCanceledException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import java.io.IOException;

import blagodarie.rating.R;
import blagodarie.rating.auth.AccountGeneral;
import blagodarie.rating.ui.AccountProvider;

public final class SplashActivity
        extends AppCompatActivity
        implements AccountProvider.OnAccountSelectListener {

    private static final String TAG = _SplashActivity.class.getSimpleName();

    private AccountManager mAccountManager;

    @Override
    protected final void onCreate (
            @Nullable final Bundle savedInstanceState
    ) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate");

        mAccountManager = AccountManager.get(this);
    }

    @Override
    protected void onResume () {
        super.onResume();
        Log.d(TAG, "onResume");
        AccountProvider.getAccount(
                this,
                this
        );
    }

    @Override
    public void onAccountSelected (@Nullable final Account account) {
        if (account != null) {
            toProfile(account);
        } else {
            addNewAccount(getString(R.string.account_type));
        }
    }

    private void addNewAccount (
            @NonNull final String accountType
    ) {
        Log.d(TAG, "addNewAccount accountType=" + accountType);
        mAccountManager.addAccount(
                accountType,
                getString(R.string.token_type),
                null,
                null,
                this,
                this::onAddAccountFinished,
                null
        );
    }

    public void onAddAccountFinished (final AccountManagerFuture<Bundle> result) {
        Log.d(TAG, "onAddAccountFinish");
        try {
            final Bundle bundle = result.getResult();
            final Account account = new Account(
                    bundle.getString(AccountManager.KEY_ACCOUNT_NAME),
                    bundle.getString(AccountManager.KEY_ACCOUNT_TYPE)
            );
            toProfile(account);
        } catch (OperationCanceledException e) {
            Log.e(TAG, Log.getStackTraceString(e));
            Toast.makeText(this, getString(R.string.err_msg_account_not_created), Toast.LENGTH_LONG).show();
            finish();
        } catch (AuthenticatorException e) {
            Log.e(TAG, Log.getStackTraceString(e));
            Toast.makeText(this, getString(R.string.err_msg_authentication_error), Toast.LENGTH_LONG).show();
            finish();
        } catch (IOException e) {
            Log.e(TAG, Log.getStackTraceString(e));
            finish();
        }
    }

    private void toProfile (
            @NonNull final Account account
    ) {
        Log.d(TAG, "toProfile account=" + account);
        final String userId = mAccountManager.getUserData(account, AccountGeneral.USER_DATA_USER_ID);
        final Intent i = new Intent(Intent.ACTION_VIEW);
        i.setData(Uri.parse(getString(R.string.url_profile, userId)));
        startActivity(i);
        finish();
    }

    public static Intent createSelfIntent (
            @NonNull final Context context
    ) {
        Log.d(TAG, "createSelfIntent");
        return new Intent(context, SplashActivity.class);
    }
}
