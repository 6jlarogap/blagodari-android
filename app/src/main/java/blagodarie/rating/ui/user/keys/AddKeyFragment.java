package blagodarie.rating.ui.user.keys;

import android.accounts.Account;
import android.accounts.AccountManager;
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

import blagodarie.rating.AppExecutors;
import blagodarie.rating.R;
import blagodarie.rating.databinding.AddKeyFragmentBinding;
import blagodarie.rating.model.entities.KeyPair;
import blagodarie.rating.model.entities.KeyType;
import blagodarie.rating.repository.AsyncServerRepository;
import blagodarie.rating.server.BadAuthorizationTokenException;
import blagodarie.rating.ui.AccountProvider;

public final class AddKeyFragment
        extends Fragment {

    public interface FragmentCommunicator {
        void onKeySaved ();
    }

    public interface UserActionListener {
        void onSaveClick ();
    }

    private static final String TAG = AddKeyFragment.class.getSimpleName();

    private AddKeyFragmentBinding mBinding;

    private Account mAccount;

    private FragmentCommunicator mFragmentCommunicator;

    private final AsyncServerRepository mAsyncRepository = new AsyncServerRepository(AppExecutors.getInstance().networkIO(), AppExecutors.getInstance().mainThread());

    private final UserActionListener mUserActionListener = new UserActionListener() {
        @Override
        public void onSaveClick () {
            final String value = mBinding.etValue.getText().toString();
            if (!value.isEmpty()) {
                KeyType keyType = KeyType.PHONE;
                if (mBinding.rbEmail.isChecked()) {
                    keyType = KeyType.EMAIL;
                } else if (mBinding.rbCreditCard.isChecked()) {
                    keyType = KeyType.CREDIT_CARD;
                } else if (mBinding.rbLink.isChecked()) {
                    keyType = KeyType.LINK;
                }
                final KeyPair keyPair = new KeyPair(value, keyType);
                attemptToInsertKey(keyPair);
            } else {
                mBinding.etValue.setError(getString(R.string.err_msg_required_to_fill));
            }
        }
    };

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
        try {
            mFragmentCommunicator = (FragmentCommunicator) requireActivity();
        } catch (ClassCastException e) {
            Log.e(TAG, requireActivity().getClass().getName() + " must implement " + FragmentCommunicator.class.getName());
            throw new ClassCastException(requireActivity().getClass().getName() + " must implement " + FragmentCommunicator.class.getName());
        }

        final AddKeyFragmentArgs args = AddKeyFragmentArgs.fromBundle(requireArguments());

        mAccount = args.getAccount();
    }

    @Override
    public void onActivityCreated (@Nullable final Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        setupBinding();
    }

    @Override
    public void onDestroy () {
        Log.d(TAG, "onDestroy");
        super.onDestroy();
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
        mBinding.setUserActionListener(mUserActionListener);
    }

    private void attemptToInsertKey (@NonNull final KeyPair keyPair) {
        AccountProvider.getAuthToken(requireActivity(), mAccount, authToken -> insertKey(authToken, keyPair));
    }

    private void insertKey (
            @Nullable final String authToken,
            @NonNull final KeyPair keyPair
    ) {
        if (authToken != null) {
            mAsyncRepository.setAuthToken(authToken);
            mAsyncRepository.insertKey(
                    keyPair,
                    () -> {
                        Toast.makeText(requireContext(), R.string.info_msg_key_saved, Toast.LENGTH_LONG).show();
                        mFragmentCommunicator.onKeySaved();
                    },
                    throwable -> {
                        if (throwable instanceof BadAuthorizationTokenException) {
                            AccountManager.get(requireContext()).invalidateAuthToken(getString(R.string.account_type), authToken);
                            attemptToInsertKey(keyPair);
                        } else {
                            Log.e(TAG, Log.getStackTraceString(throwable));
                            Toast.makeText(requireContext(), throwable.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    });
        } else {
            Toast.makeText(requireContext(), R.string.info_msg_need_log_in, Toast.LENGTH_LONG).show();
        }
    }

}
