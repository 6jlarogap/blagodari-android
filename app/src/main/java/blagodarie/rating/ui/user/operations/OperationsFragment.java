package blagodarie.rating.ui.user.operations;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.Intent;
import android.net.Uri;
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
import blagodarie.rating.operations.OperationToAnyTextManager;
import blagodarie.rating.operations.OperationToUserManager;
import blagodarie.rating.model.entities.OperationType;
import blagodarie.rating.R;
import blagodarie.rating.databinding.OperationsFragmentBinding;
import blagodarie.rating.repository.AsyncServerRepository;
import blagodarie.rating.repository.ServerRepository;
import blagodarie.rating.server.BadAuthorizationTokenException;
import blagodarie.rating.ui.AccountProvider;
import io.reactivex.disposables.CompositeDisposable;

public final class OperationsFragment
        extends Fragment
        implements OperationsUserActionListener {

    private static final String TAG = OperationsFragment.class.getSimpleName();

    private OperationsViewModel mViewModel;

    private OperationsFragmentBinding mBinding;

    private OperationsAdapter mOperationsAdapter;

    private Account mAccount;

    private UUID mUserId;

    private UUID mAnyTextId;

    @NonNull
    private CompositeDisposable mDisposables = new CompositeDisposable();

    @NonNull
    private final AsyncServerRepository mAsyncRepository = new AsyncServerRepository(AppExecutors.getInstance().networkIO(), AppExecutors.getInstance().mainThread());

    @NonNull
    private final ServerRepository mRepository = new ServerRepository();

    @NonNull
    private final OperationsAdapter.OnItemClickListener mOnOperationClickListener = new OperationsAdapter.OnItemClickListener() {
        @Override
        public void onOperationClick (@NonNull final UUID userId) {
            final Intent i = new Intent(Intent.ACTION_VIEW);
            i.setData(Uri.parse(getString(R.string.url_profile, userId.toString())));
            startActivity(i);
        }

        @Override
        public void onThanksClick (@NonNull final UUID userIdTo) {
            attemptToAddOperation(OperationType.THANKS, userIdTo);
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
        mAccount = args.getAccount();
    }

    @Override
    public void onActivityCreated (@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        initViewModel();
        initOperationsAdapter();
        setupBinding();
    }

    @Override
    public void onStart () {
        Log.d(TAG, "onStart");
        super.onStart();
        refreshOperations();
    }

    @Override
    public void onDestroy () {
        Log.d(TAG, "onDestroy");
        super.onDestroy();
        mDisposables.clear();
        mBinding = null;
    }

    private void initOperationsAdapter () {
        mOperationsAdapter = new OperationsAdapter(mOnOperationClickListener, mViewModel);
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
        mBinding.setViewModel(mViewModel);
        mBinding.setUserActionsListener(this);
        mBinding.rvOperations.setLayoutManager(new LinearLayoutManager(requireContext()));
        mBinding.rvOperations.setAdapter(mOperationsAdapter);
        mBinding.srlRefreshProfileInfo.setOnRefreshListener(() -> {
            mViewModel.getDownloadInProgress().set(true);
            refreshOperations();
            mViewModel.getDownloadInProgress().set(false);
        });
    }

    private void refreshOperations () {
        Log.d(TAG, "refreshOperations");
        mViewModel.setOperations(mUserId != null ? mRepository.getUserOperations(mUserId) : mRepository.getAnyTextOperations(mAnyTextId));
        mViewModel.getOperations().observe(requireActivity(), mOperationsAdapter::submitList);
    }

    @Override
    public void onThanksClick () {
        attemptToAddOperation(OperationType.THANKS, mUserId);
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
