package blagodarie.rating.auth;

import android.accounts.Account;
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

import blagodarie.rating.server.ServerApiResponse;
import blagodarie.rating.server.ServerConnector;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;

import static android.app.Activity.RESULT_OK;

public final class SignUpFragment
        extends Fragment {

    private static final String TAG = SignUpFragment.class.getSimpleName();

    private static final int ACTIVITY_REQUEST_CODE_GOGGLE_SIGN_IN = 1;

    private CompositeDisposable mDisposables = new CompositeDisposable();

    @Override
    public View onCreateView (
            @NonNull final LayoutInflater inflater,
            final ViewGroup container,
            final Bundle savedInstanceState
    ) {
        Log.d(TAG, "onCreateView");
        final View view = inflater.inflate(R.layout.sign_up_fragment, container, false);
        initViews(view);
        return view;
    }


    @Override
    public void onActivityCreated (@Nullable final Bundle savedInstanceState) {
        Log.d(TAG, "onActivityCreated");
        super.onActivityCreated(savedInstanceState);
    }

    private void initViews (final View view) {
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
    public void onDestroy () {
        Log.d(TAG, "onDestroy");
        super.onDestroy();
        mDisposables.dispose();
    }

    @Override
    public void onActivityResult (
            final int requestCode,
            final int resultCode,
            @Nullable final Intent data
    ) {
        Log.d(TAG, "onActivityResult");
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == ACTIVITY_REQUEST_CODE_GOGGLE_SIGN_IN) {
            final Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                final GoogleSignInAccount account = task.getResult(ApiException.class);
                if (account != null &&
                        account.getIdToken() != null) {
                    startSignUp(account.getIdToken());
                }
            } catch (ApiException e) {
                Log.e(TAG, Log.getStackTraceString(e));
            }
        }
    }

    private void startSignUp (
            @NonNull final String googleTokenId
    ) {
        Log.d(TAG, "startSignUp");
        final String content = String.format("{\"oauth\":{\"provider\":\"google\",\"token\":\"%s\"}}", googleTokenId);
        Log.d(TAG, "content=" + content);

        mDisposables.add(
                Observable.
                        fromCallable(() -> ServerConnector.sendRequestAndGetResponse("auth/signup", content)).
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
                    final long userId = userJSON.getLong("user_id");
                    // TODO: 26.06.2020 Заглушка
                    final String first_name = "Иван";//userJSON.getString("first_name");
                    final String middleName = "Иванович";//userJSON.getString("middle_name");
                    final String lastName = "Иванов";//userJSON.getString("last_name");
                    final String authToken = userJSON.getString("token");
                    createAccount(userId, first_name, middleName, lastName, authToken);
                } catch (JSONException e) {
                    Log.e(TAG, Log.getStackTraceString(e));
                    Toast.makeText(requireActivity(), e.getMessage(), Toast.LENGTH_LONG).show();
                }
            }
        }
    }

    private void createAccount (
            @NonNull final Long userId,
            @NonNull final String firstName,
            @NonNull final String middleName,
            @NonNull final String lastName,
            @NonNull final String authToken
    ) {
        Log.d(TAG, "createAccount");
        final String accountName = lastName + " " + firstName + " " + middleName;
        final AccountManager accountManager = AccountManager.get(getContext());
        final Account account = new Account(accountName, getString(R.string.account_type));
        final Bundle userData = new Bundle();
        userData.putString(AccountGeneral.USER_DATA_USER_ID, userId.toString());
        accountManager.addAccountExplicitly(account, "", userData);
        accountManager.setAuthToken(account, getString(R.string.token_type), authToken);

        final Bundle bundle = new Bundle();
        bundle.putString(AccountManager.KEY_ACCOUNT_NAME, accountName);
        bundle.putString(AccountManager.KEY_ACCOUNT_TYPE, getString(R.string.account_type));
        bundle.putString(AccountManager.KEY_AUTHTOKEN, authToken);

        final Intent res = new Intent();
        res.putExtras(bundle);

        ((AuthenticationActivity) requireActivity()).setAccountAuthenticatorResult(bundle);
        requireActivity().setResult(RESULT_OK, res);
        requireActivity().finish();
    }

}
