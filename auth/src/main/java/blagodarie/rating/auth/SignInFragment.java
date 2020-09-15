package blagodarie.rating.auth;

import android.accounts.AccountManager;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;

import java.util.UUID;

import blagodarie.rating.server.ServerApiClient;
import blagodarie.rating.server.SignInRequest;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;

import static android.app.Activity.RESULT_OK;

public final class SignInFragment
        extends Fragment {

    private static final String TAG = SignInFragment.class.getSimpleName();

    private UUID mUserId;

    private CompositeDisposable mDisposables = new CompositeDisposable();

    @Override
    public View onCreateView (
            @NonNull final LayoutInflater inflater,
            final ViewGroup container,
            final Bundle savedInstanceState
    ) {
        Log.d(TAG, "onCreateView");
        final View view = inflater.inflate(R.layout.sign_in_fragment, container, false);
        initViews(view);
        return view;
    }

    @Override
    public void onViewCreated (
            @NonNull final View view,
            @Nullable final Bundle savedInstanceState
    ) {
        Log.d(TAG, "onViewCreated");
        super.onViewCreated(view, savedInstanceState);
        final SignInFragmentArgs args = SignInFragmentArgs.fromBundle(requireArguments());

        mUserId = args.getUserId();
    }

    @Override
    public void onActivityCreated (@Nullable final Bundle savedInstanceState) {
        Log.d(TAG, "onActivityCreated");
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onDestroy () {
        Log.d(TAG, "onDestroy");
        super.onDestroy();
        mDisposables.dispose();
    }


    private void initViews (@NonNull final View view) {
        Log.d(TAG, "initViews");
        view.findViewById(R.id.btnSignIn).setOnClickListener(
                v -> AuthenticationActivity.googleSignIn(
                        requireActivity(),
                        this,
                        getString(R.string.oauth2_client_id)
                )
        );
    }

    @Override
    public final void onActivityResult (
            final int requestCode,
            final int resultCode,
            @Nullable final Intent data
    ) {
        Log.d(TAG, "onActivityResult");
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == AuthenticationActivity.ACTIVITY_REQUEST_CODE_GOGGLE_SIGN_IN) {
            final Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                final GoogleSignInAccount account = task.getResult(ApiException.class);
                if (account != null &&
                        account.getIdToken() != null) {
                    startSignIn(account.getIdToken());
                }
            } catch (ApiException e) {
                Log.e(TAG, Log.getStackTraceString(e));
                Toast.makeText(requireActivity(), e.getMessage(), Toast.LENGTH_LONG).show();
            }
        }
    }

    private void startSignIn (
            @NonNull final String googleTokenId
    ) {
        Log.d(TAG, "startSignIn");
        final ServerApiClient apiClient = new ServerApiClient();
        final SignInRequest signInRequest = new SignInRequest(googleTokenId, mUserId.toString());
        mDisposables.add(
                Observable.
                        fromCallable(() -> apiClient.execute(signInRequest)).
                        subscribeOn(Schedulers.io()).
                        observeOn(AndroidSchedulers.mainThread()).
                        subscribe(
                                signInResponse -> finishSignIn(signInResponse.getAuthToken()),
                                throwable -> Toast.makeText(requireActivity(), throwable.getMessage(), Toast.LENGTH_LONG).show()
                        )
        );
    }

    private void finishSignIn (
            @NonNull final String authToken
    ) {
        Log.d(TAG, "finishSignIn");
        final Bundle bundle = new Bundle();
        bundle.putString(AccountManager.KEY_ACCOUNT_NAME, mUserId.toString());
        bundle.putString(AccountManager.KEY_ACCOUNT_TYPE, getString(R.string.account_type));
        bundle.putString(AccountManager.KEY_AUTHTOKEN, authToken);

        final Intent res = new Intent();
        res.putExtras(bundle);

        ((AuthenticationActivity) requireActivity()).setAccountAuthenticatorResult(bundle);
        requireActivity().setResult(RESULT_OK, res);
        requireActivity().finish();
    }

}
