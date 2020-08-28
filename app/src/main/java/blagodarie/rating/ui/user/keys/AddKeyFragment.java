package blagodarie.rating.ui.user.keys;

import android.accounts.Account;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Locale;

import blagodarie.rating.R;
import blagodarie.rating.databinding.AddKeyFragmentBinding;
import blagodarie.rating.server.ServerApiResponse;
import blagodarie.rating.server.ServerConnector;
import blagodarie.rating.server.ServerException;
import blagodarie.rating.ui.AccountProvider;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;

public final class AddKeyFragment
        extends Fragment {

    public interface FragmentCommunicator {
        void onKeyAdded ();
    }

    private static final String TAG = AddKeyFragment.class.getSimpleName();

    private AddKeyFragmentBinding mBinding;

    private Account mAccount;

    @NonNull
    private CompositeDisposable mDisposables = new CompositeDisposable();

    private FragmentCommunicator mFragmentCommunicator;

    @NotNull
    @Override
    public View onCreateView (
            @NonNull final LayoutInflater inflater,
            @Nullable final ViewGroup container,
            @Nullable final Bundle savedInstanceState
    ) {
        Log.d(TAG, "onCreateView");
        initBinding(inflater, container);
        return mBinding.getRoot();
    }

    @Override
    public void onViewCreated (
            @NonNull final View view,
            @Nullable final Bundle savedInstanceState
    ) {
        Log.d(TAG, "onViewCreated");
        super.onViewCreated(view, savedInstanceState);

        final AddKeyFragmentArgs args = AddKeyFragmentArgs.fromBundle(requireArguments());

        mAccount = args.getAccount();
    }

    @Override
    public void onActivityCreated (@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        try {
            mFragmentCommunicator = (FragmentCommunicator) requireActivity();
        } catch (ClassCastException e) {
            Log.e(TAG, requireActivity().getClass().getName() + " must implement " + FragmentCommunicator.class.getName());
            throw new ClassCastException(requireActivity().getClass().getName() + " must implement " + FragmentCommunicator.class.getName());
        }

        setupBinding();
    }

    @Override
    public void onStart () {
        Log.d(TAG, "onStart");
        super.onStart();
    }

    @Override
    public void onDestroy () {
        Log.d(TAG, "onDestroy");
        super.onDestroy();
        mDisposables.clear();
        mBinding = null;
    }

    private void initBinding (
            @NonNull final LayoutInflater inflater,
            @Nullable final ViewGroup container
    ) {
        Log.d(TAG, "initBinding");
        mBinding = AddKeyFragmentBinding.inflate(inflater, container, false);
    }

    private void setupBinding () {
        Log.d(TAG, "setupBinding");
        mBinding.btnSave.setOnClickListener(view -> AccountProvider.getAuthToken(requireActivity(), mAccount, this::addKey));
    }

    private void addKey (
            @NonNull final String authToken
    ) {
        Log.d(TAG, "downloadProfileData");
        KeyType keyType = KeyType.PHONE;
        if (mBinding.rbEmail.isChecked()) {
            keyType = KeyType.EMAIL;
        } else if (mBinding.rbCreditCard.isChecked()) {
            keyType = KeyType.CREDIT_CARD;
        } else if (mBinding.rbLink.isChecked()) {
            keyType = KeyType.LINK;
        }
        final String content = String.format(Locale.ENGLISH, "{\"value\":\"%s\",\"type_id\":%d}", mBinding.etValue.getText().toString(), keyType.getId());
        mDisposables.add(
                Observable.
                        fromCallable(() -> ServerConnector.sendAuthRequestAndGetResponse("addkey", authToken, content)).
                        subscribeOn(Schedulers.io()).
                        observeOn(AndroidSchedulers.mainThread()).
                        subscribe(
                                this::extractDataFromServerApiResponse,
                                throwable -> Toast.makeText(requireContext(), throwable.getMessage(), Toast.LENGTH_LONG).show()
                        )
        );
    }

    private void extractDataFromServerApiResponse (
            @NonNull final ServerApiResponse serverApiResponse) {
        Log.d(TAG, "extractDataFromServerApiResponse");
        if (serverApiResponse.getCode() == 200) {
            Toast.makeText(requireContext(), R.string.info_msg_key_saved, Toast.LENGTH_LONG).show();
            mFragmentCommunicator.onKeyAdded();
        } else {
            if (serverApiResponse.getBody() != null) {
                try {
                    final JSONObject jsonObject = new JSONObject(serverApiResponse.getBody());
                    String errorMessage = jsonObject.getString("message");
                    Toast.makeText(requireContext(), errorMessage, Toast.LENGTH_LONG).show();
                } catch (JSONException e) {
                }
            }
        }
    }
}
