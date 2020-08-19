package blagodarie.rating.ui.user.operations;

import android.accounts.Account;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.paging.LivePagedListBuilder;
import androidx.paging.PagedList;
import androidx.recyclerview.widget.LinearLayoutManager;

import org.jetbrains.annotations.NotNull;

import java.util.UUID;
import java.util.concurrent.Executors;

import blagodarie.rating.OperationManager;
import blagodarie.rating.OperationType;
import blagodarie.rating.databinding.OperationsFragmentBinding;
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

        initOperationsAdapter();
        initViewModel();
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

    private void refreshOperations () {
        final OperationsDataSource.OperationsDataSourceFactory sourceFactory = new OperationsDataSource.OperationsDataSourceFactory(mUserId, mAnyTextId);

        final PagedList.Config config = new PagedList.Config.Builder()
                .setEnablePlaceholders(false)
                .setPageSize(10)
                .build();

        mViewModel.setOperations(
                new LivePagedListBuilder<>(sourceFactory, config).
                        setFetchExecutor(Executors.newSingleThreadExecutor()).
                        build()
        );
        mViewModel.getOperations().observe(requireActivity(), mOperationsAdapter::submitList);
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

    private void initOperationsAdapter () {
        mOperationsAdapter = new OperationsAdapter();
    }

    @Override
    public void onAddOperation (@NonNull final OperationType operationType) {
        Log.d(TAG, "onAddOperation");
        if (mAccount != null) {
            new OperationManager(this::refreshOperations).
                    createOperation(
                            requireActivity(),
                            mDisposables,
                            mAccount,
                            mUserId,
                            operationType
                    );
        } else {
            AccountProvider.createAccount(
                    requireActivity(),
                    account -> {
                        if (account != null) {
                            mAccount = account;
                            mViewModel.isHaveAccount().set(true);
                            mViewModel.isOwnProfile().set(mAccount.name.equals(mUserId.toString()));
                            if (!mViewModel.isOwnProfile().get()) {
                                new OperationManager(this::refreshOperations).
                                        createOperation(
                                                requireActivity(),
                                                mDisposables,
                                                mAccount,
                                                mUserId,
                                                operationType
                                        );
                            }
                        }
                    }
            );
        }
    }

}
