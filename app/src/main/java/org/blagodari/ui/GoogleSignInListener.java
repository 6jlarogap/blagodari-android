package org.blagodari.ui;

import android.app.Activity;
import android.content.Intent;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.ex.diagnosticlib.Diagnostic;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import org.blagodari.DataRepository;
import org.blagodari.db.scheme.KeyzType;

import io.reactivex.schedulers.Schedulers;

public final class GoogleSignInListener {

    public static final int ACTIVITY_REQUEST_CODE_GOGGLE_SIGN_IN = 1;

    private static volatile GoogleSignInListener INSTANCE;

    public static GoogleSignInListener getInstance () {
        synchronized (GoogleSignInListener.class) {
            if (INSTANCE == null) {
                INSTANCE = new GoogleSignInListener();
            }
        }
        return INSTANCE;
    }

    private GoogleSignInListener () {
    }

    public final void signIn (@NonNull final Activity activity) {
        Diagnostic.i();
        final GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();
        final GoogleSignInClient mGoogleSignInClient = GoogleSignIn.getClient(activity, gso);
        final Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        activity.startActivityForResult(signInIntent, ACTIVITY_REQUEST_CODE_GOGGLE_SIGN_IN);
    }

    public final boolean handleSignInResult (
            @Nullable final Intent data,
            @NonNull final Long userId,
            @NonNull final DataRepository repository
    ) {
        Diagnostic.i();
        boolean result;
        final Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
        try {
            GoogleSignInAccount account = task.getResult(ApiException.class);
            if (account != null &&
                    account.getId() != null) {
                createGoogleAccountIdKeyz(account.getId(), userId, repository);
            }
            result = true;
        } catch (ApiException e) {
            Diagnostic.e(e);
            result = false;
        }
        return result;
    }

    private void createGoogleAccountIdKeyz (
            @NonNull final String value,
            @NonNull final Long userId,
            @NonNull final DataRepository repository
    ) {
        Diagnostic.i();
        repository.
                createKeyz(userId, value, KeyzType.Types.GOOGLE_ACCOUNT_ID.getId()).
                subscribeOn(Schedulers.io()).
                subscribe();
    }
}
