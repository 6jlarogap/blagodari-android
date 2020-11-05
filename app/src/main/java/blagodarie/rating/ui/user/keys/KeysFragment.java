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
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import org.jetbrains.annotations.NotNull;

import java.util.UUID;

import blagodarie.rating.AppExecutors;
import blagodarie.rating.R;
import blagodarie.rating.databinding.KeysFragmentBinding;
import blagodarie.rating.model.IKey;
import blagodarie.rating.repository.AsyncServerRepository;
import blagodarie.rating.server.BadAuthorizationTokenException;
import blagodarie.rating.ui.AccountProvider;

public final class KeysFragment
        extends Fragment
        implements KeysAdapter.AdapterCommunicator {

    public interface FragmentCommunicator {
        void toAddKey ();
    }

    public interface UserActionListener {
        void onAddKeyClick ();
    }

    private static final String TAG = KeysFragment.class.getSimpleName();

    private KeysViewModel mViewModel;

    private KeysFragmentBinding mBinding;

    private KeysAdapter mKeysAdapter;

    private Account mAccount;

    private UUID mUserId;

    private FragmentCommunicator mFragmentCommunicator;

    private final AsyncServerRepository mAsyncRepository = new AsyncServerRepository(AppExecutors.getInstance().networkIO(), AppExecutors.getInstance().mainThread());

    private final UserActionListener mUserActionListener = new UserActionListener() {
        @Override
        public void onAddKeyClick () {
            mFragmentCommunicator.toAddKey();
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

        final KeysFragmentArgs args = KeysFragmentArgs.fromBundle(requireArguments());

        mUserId = args.getUserId();
    }

    @Override
    public void onActivityCreated (@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        /*try {
            mFragmentCommunicator = (FragmentCommunicator) requireActivity();
        } catch (ClassCastException e) {
            Log.e(TAG, requireActivity().getClass().getName() + " must implement " + FragmentCommunicator.class.getName());
            throw new ClassCastException(requireActivity().getClass().getName() + " must implement " + FragmentCommunicator.class.getName());
        }*/

        initKeysAdapter();
        initViewModel();
        setupBinding();
    }

    @Override
    public void onStart () {
        Log.d(TAG, "onStart");
        super.onStart();
        refreshKeys();
    }

    @Override
    public void onDestroy () {
        Log.d(TAG, "onDestroy");
        super.onDestroy();
        mBinding = null;
    }

    private void initKeysAdapter () {
        Log.d(TAG, "initKeysAdapter");
        mKeysAdapter = new KeysAdapter((mAccount != null && mAccount.name.equals(mUserId.toString())), this);
    }

    private void initBinding (
            @NonNull final LayoutInflater inflater,
            @Nullable final ViewGroup container
    ) {
        Log.d(TAG, "initBinding");
        mBinding = KeysFragmentBinding.inflate(inflater, container, false);
    }

    private void initViewModel () {
        Log.d(TAG, "initViewModel");
        mViewModel = new ViewModelProvider(requireActivity()).get(KeysViewModel.class);
        mViewModel.isHaveAccount().set(mAccount != null);
        mViewModel.isOwnProfile().set(mAccount != null && mUserId != null && mAccount.name.equals(mUserId.toString()));
    }

    private void setupBinding () {
        Log.d(TAG, "setupBinding");
        mBinding.setViewModel(mViewModel);
        mBinding.setUserActionListener(mUserActionListener);
        mBinding.rvKeys.setLayoutManager(new LinearLayoutManager(requireContext()));
        mBinding.rvKeys.setAdapter(mKeysAdapter);
        mBinding.srlRefreshProfileInfo.setOnRefreshListener(() -> {
            mViewModel.getDownloadInProgress().set(true);
            refreshKeys();
            mViewModel.getDownloadInProgress().set(false);
        });
    }

    private void refreshKeys () {
        Log.d(TAG, "refreshKeys");
        mViewModel.setOperations(mAsyncRepository.getLiveDataPagedListFromDataSource(new KeysDataSource.KeysDataSourceFactory(mUserId)));
        mViewModel.getKeys().observe(requireActivity(), mKeysAdapter::submitList);
    }

    @Override
    public void onEditKey (@NonNull final IKey key) {
        attemptToEditKey(key);
    }

    @Override
    public void onDeleteKey (@NonNull final IKey key) {
        attemptToDeleteKey(key);
    }

    private void attemptToEditKey (
            @NonNull final IKey key
    ) {
        AccountProvider.getAuthToken(requireActivity(), mAccount, authToken -> editKey(authToken, key));
    }

    private void attemptToDeleteKey (
            @NonNull final IKey key
    ) {
        AccountProvider.getAuthToken(requireActivity(), mAccount, authToken -> deleteKey(authToken, key));
    }

    public void editKey (
            @Nullable final String authToken,
            @NonNull final IKey key
    ) {
        if (authToken != null) {
            mAsyncRepository.setAuthToken(authToken);
            mAsyncRepository.updateKey(
                    key,
                    () -> {
                        Toast.makeText(requireContext(), R.string.info_msg_key_saved, Toast.LENGTH_LONG).show();
                        refreshKeys();
                    },
                    throwable -> {
                        if (throwable instanceof BadAuthorizationTokenException) {
                            AccountManager.get(requireContext()).invalidateAuthToken(getString(R.string.account_type), authToken);
                            attemptToEditKey(key);
                        } else {
                            Log.e(TAG, Log.getStackTraceString(throwable));
                            Toast.makeText(requireContext(), throwable.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    });
        } else {
            Toast.makeText(requireContext(), R.string.info_msg_need_log_in, Toast.LENGTH_LONG).show();
        }
    }

    private void deleteKey (
            @Nullable final String authToken,
            @NonNull final IKey key
    ) {
        if (authToken != null) {
            mAsyncRepository.setAuthToken(authToken);
            mAsyncRepository.deleteKey(
                    key,
                    () -> {
                        Toast.makeText(requireContext(), R.string.info_msg_key_deleted, Toast.LENGTH_LONG).show();
                        refreshKeys();
                    },
                    throwable -> {
                        if (throwable instanceof BadAuthorizationTokenException) {
                            AccountManager.get(requireContext()).invalidateAuthToken(getString(R.string.account_type), authToken);
                            attemptToEditKey(key);
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
