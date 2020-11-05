package blagodarie.rating.ui.user.operations;

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

import org.jetbrains.annotations.NotNull;

import java.util.UUID;

import blagodarie.rating.AppExecutors;
import blagodarie.rating.R;
import blagodarie.rating.databinding.OperationsFragmentBinding;
import blagodarie.rating.model.entities.OperationType;
import blagodarie.rating.operations.OperationToAnyTextManager;
import blagodarie.rating.operations.OperationToUserManager;
import blagodarie.rating.repository.AsyncServerRepository;
import blagodarie.rating.server.BadAuthorizationTokenException;
import blagodarie.rating.ui.AccountProvider;
import blagodarie.rating.ui.OperationsFragmentArgs;

public final class OperationsFragment
        extends Fragment {

    public interface UserActionListener {
        void onThanksClick ();
    }

    private static final String TAG = OperationsFragment.class.getSimpleName();

    private OperationsViewModel mViewModel;

    private OperationsFragmentBinding mBinding;

    private OperationsAdapter mOperationsAdapter;

    private Account mAccount;

    private UUID mUserId;

    private UUID mAnyTextId;

    @NonNull
    private final AsyncServerRepository mAsyncRepository = new AsyncServerRepository(AppExecutors.getInstance().networkIO(), AppExecutors.getInstance().mainThread());

    @NonNull
    private final UserActionListener mUserActionListener = new UserActionListener() {
        @Override
        public void onThanksClick () {
            attemptToAddOperation(OperationType.THANKS, mUserId);
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

        final OperationsFragmentArgs args = OperationsFragmentArgs.fromBundle(requireArguments());

        mUserId = args.getUserId();
        mAnyTextId = args.getAnyTextId();
    }

    @Override
    public void onActivityCreated (@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        initViewModel();
        initOperationsAdapter();
        setupBinding();
    }

    @Override
    public void onResume () {
        Log.d(TAG, "onResume");
        super.onResume();
        refreshOperations();
    }

    @Override
    public void onDestroy () {
        Log.d(TAG, "onDestroy");
        super.onDestroy();
        mBinding = null;
    }

    private void initOperationsAdapter () {
        mOperationsAdapter = new OperationsAdapter();
    }

    private void initBinding (
            @NonNull final LayoutInflater inflater,
            @Nullable final ViewGroup container
    ) {
        Log.d(TAG, "initBinding");
        mBinding = OperationsFragmentBinding.inflate(inflater, container, false);
    }

    private void initViewModel () {
        mViewModel = new ViewModelProvider(requireActivity()).get(OperationsViewModel.class);
        mViewModel.isHaveAccount().set(mAccount != null);
        mViewModel.isOwnProfile().set(mAccount != null && mUserId != null && mAccount.name.equals(mUserId.toString()));
    }

    private void setupBinding () {
        /*mBinding.setViewModel(mViewModel);
        mBinding.setUserActionsListener(mUserActionListener);
        mBinding.rvOperations.setLayoutManager(new LinearLayoutManager(requireContext()));
        mBinding.rvOperations.setAdapter(mOperationsAdapter);
        mBinding.srlRefreshProfileInfo.setOnRefreshListener(this::refreshOperations);*/
    }

    private void refreshOperations () {
        mViewModel.getDownloadInProgress().set(true);
        Log.d(TAG, "refreshOperations");
        mViewModel.setOperations(
                mUserId != null ?
                        mAsyncRepository.getLiveDataPagedListFromDataSource(new UserOperationsDataSource.UserOperationsDataSourceFactory(mUserId)) :
                        mAsyncRepository.getLiveDataPagedListFromDataSource(new AnyTextOperationsDataSource.AnyTextOperationsDataSourceFactory(mAnyTextId))
        );
        mViewModel.getOperations().observe(requireActivity(), operations -> {
            mViewModel.getDownloadInProgress().set(false);
            mOperationsAdapter.submitList(operations);
        });
    }

    private void attemptToAddOperation (
            @NonNull final OperationType operationType,
            @Nullable final UUID userIdTo
    ) {
        Log.d(TAG, "onAddOperation");
        if (mAccount != null) {
            AccountProvider.getAuthToken(requireActivity(), mAccount, authToken -> {
                if (authToken != null) {
                    mAsyncRepository.setAuthToken(authToken);
                    if (userIdTo != null) {
                        new OperationToUserManager().
                                createOperationToUser(
                                        requireActivity(),
                                        UUID.fromString(mAccount.name),
                                        userIdTo,
                                        operationType,
                                        mAsyncRepository,
                                        () -> {
                                            Toast.makeText(requireContext(), R.string.info_msg_saved, Toast.LENGTH_LONG).show();
                                            refreshOperations();
                                        },
                                        throwable -> {
                                            Log.e(TAG, Log.getStackTraceString(throwable));
                                            if (throwable instanceof BadAuthorizationTokenException) {
                                                AccountManager.get(requireContext()).invalidateAuthToken(getString(R.string.account_type), authToken);
                                                attemptToAddOperation(operationType, userIdTo);
                                            } else {
                                                Toast.makeText(requireContext(), R.string.err_msg_not_saved, Toast.LENGTH_LONG).show();
                                            }
                                        });
                    } else {
                        new OperationToAnyTextManager().
                                createOperationToAnyText(
                                        requireActivity(),
                                        UUID.fromString(mAccount.name),
                                        mAnyTextId,
                                        operationType,
                                        "",
                                        mAsyncRepository,
                                        () -> {
                                            Toast.makeText(requireContext(), R.string.info_msg_saved, Toast.LENGTH_LONG).show();
                                            refreshOperations();
                                        },
                                        throwable -> {
                                            Log.e(TAG, Log.getStackTraceString(throwable));
                                            if (throwable instanceof BadAuthorizationTokenException) {
                                                AccountManager.get(requireContext()).invalidateAuthToken(getString(R.string.account_type), authToken);
                                                attemptToAddOperation(operationType, userIdTo);
                                            } else {
                                                Toast.makeText(requireContext(), R.string.err_msg_not_saved, Toast.LENGTH_LONG).show();
                                            }
                                        });
                    }
                } else {
                    Toast.makeText(requireContext(), R.string.info_msg_need_log_in, Toast.LENGTH_LONG).show();
                }
            });
        } else {
            AccountProvider.createAccount(
                    requireActivity(),
                    account -> {
                        if (account != null) {
                            mAccount = account;
                            mViewModel.isHaveAccount().set(true);
                            mViewModel.isOwnProfile().set(mAccount.name.equals(mUserId.toString()));
                            if (!mViewModel.isOwnProfile().get()) {
                                attemptToAddOperation(operationType, userIdTo);
                            } else {
                                Toast.makeText(requireContext(), R.string.info_msg_cant_add_operation_to_own_profile, Toast.LENGTH_LONG).show();
                            }
                        }
                    }
            );
        }
    }

}
