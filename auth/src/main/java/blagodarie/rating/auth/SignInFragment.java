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

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Locale;

import blagodarie.rating.server.ServerApiResponse;
import blagodarie.rating.server.ServerConnector;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;

import static android.app.Activity.RESULT_CANCELED;
import static android.app.Activity.RESULT_OK;

public final class SignInFragment
        extends Fragment {

    private static final String TAG = SignInFragment.class.getSimpleName();

    static final Long DEFAULT_USER_ID = Long.MIN_VALUE;

    private Long mUserId;

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
        if (getArguments() != null) {
            mUserId = SignInFragmentArgs.fromBundle(getArguments()).getUserId();
            if (mUserId.equals(DEFAULT_USER_ID)) {
                Toast.makeText(requireActivity(), getString(R.string.err_msg_no_user_id), Toast.LENGTH_LONG).show();
                requireActivity().setResult(RESULT_CANCELED);
                requireActivity().finish();
            }
        } else {
            Toast.makeText(requireActivity(), getString(R.string.err_msg_no_user_id), Toast.LENGTH_LONG).show();
            requireActivity().setResult(RESULT_CANCELED);
            requireActivity().finish();
        }
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
        final String content = String.format(Locale.ENGLISH, "{\"oauth\":{\"provider\":\"google\",\"token\":\"%s\"},\"user_id\":%d}", googleTokenId, mUserId);
        Log.d(TAG, "content=" + content);

        mDisposables.add(
                Observable.
                        fromCallable(() -> ServerConnector.sendRequestAndGetResponse("auth/signin", content)).
                        subscribeOn(Schedulers.io()).
                        observeOn(AndroidSchedulers.mainThread()).
                        subscribe(
                                this::extractDataFromServerApiResponse,
                                throwable -> Toast.makeText(requireActivity(), throwable.getMessage(), Toast.LENGTH_LONG).show()
                        )
        );
    }

    private void extractDataFromServerApiResponse (
            @NonNull final ServerApiResponse serverApiResponse
    ) {
        if (serverApiResponse.getCode() == 200) {
            if (serverApiResponse.getBody() != null) {
                final String responseBody = serverApiResponse.getBody();
                try {
                    final JSONObject userJSON = new JSONObject(responseBody);
                    final String authToken = userJSON.getString("token");
                    finishSignIn(authToken);
                } catch (JSONException e) {
                    Log.e(TAG, Log.getStackTraceString(e));
                    Toast.makeText(requireActivity(), e.getMessage(), Toast.LENGTH_LONG).show();
                }
            }
        }
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
