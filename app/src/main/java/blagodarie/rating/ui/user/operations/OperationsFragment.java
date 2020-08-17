package blagodarie.rating.ui.user.operations;

import android.accounts.Account;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.paging.LivePagedListBuilder;
import androidx.paging.PagedList;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import org.jetbrains.annotations.NotNull;

import java.util.Locale;
import java.util.UUID;
import java.util.concurrent.Executors;

import blagodarie.rating.OperationType;
import blagodarie.rating.R;
import blagodarie.rating.databinding.EnterOperationCommentDialogBinding;
import blagodarie.rating.databinding.OperationsFragmentBinding;
import blagodarie.rating.server.ServerApiResponse;
import blagodarie.rating.server.ServerConnector;
import blagodarie.rating.ui.AccountProvider;
import blagodarie.rating.ui.user.profile.ProfileFragmentArgs;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;

public final class OperationsFragment
        extends Fragment {

    private static final String TAG = OperationsFragment.class.getSimpleName();

    private OperationsViewModel mViewModel;

    private OperationsFragmentBinding mBinding;

    private OperationsAdapter mOperationsAdapter;

    private Account mAccount;

    private UUID mUserId;

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

        final ProfileFragmentArgs args = ProfileFragmentArgs.fromBundle(requireArguments());

        mUserId = args.getUserId();
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
        mViewModel.isOwnProfile().set(mAccount != null && mAccount.name.equals(mUserId.toString()));
    }

    private void refreshOperations () {
        final OperationsDataSource.OperationsDataSourceFactory sourceFactory = new OperationsDataSource.OperationsDataSourceFactory(mUserId);

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
        mBinding.setUserActionsListener(this::addOperationComment);
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

    private void addOperationComment (@NonNull final OperationType operationType) {
        Log.d(TAG, "addOperationComment");
        InputMethodManager imm = (InputMethodManager) requireContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        final EnterOperationCommentDialogBinding binding = DataBindingUtil.inflate(LayoutInflater.from(requireContext()), R.layout.enter_operation_comment_dialog, null, false);
        new AlertDialog.
                Builder(requireContext()).
                setCancelable(false).
                setTitle(R.string.txt_comment).
                setView(binding.getRoot()).
                setNegativeButton(android.R.string.cancel, (dialogInterface, i) -> imm.hideSoftInputFromWindow(binding.etOperationComment.getWindowToken(), 0)).
                setPositiveButton(android.R.string.ok,
                        (dialogInterface, i) -> {
                            imm.hideSoftInputFromWindow(binding.etOperationComment.getWindowToken(), 0);
                            final String operationComment = binding.etOperationComment.getText().toString();
                            if (mAccount != null) {
                                AccountProvider.getAuthToken(requireActivity(), mAccount, authToken -> {
                                    if (authToken != null) {
                                        addOperation(authToken, operationType, operationComment);
                                    }
                                });
                            }/* else {
                                mAccountManager.addAccount(
                                        getString(R.string.account_type),
                                        getString(R.string.token_type),
                                        null,
                                        null,
                                        this,
                                        accountManagerFuture -> onAddAccountFinished(accountManagerFuture, 1, operationComment),
                                        null
                                );
                            }*/
                        }).
                create().
                show();
        binding.etOperationComment.requestFocus();
        imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY);
    }

    private void addOperation (
            @NonNull final String authToken,
            @NonNull final OperationType operationType,
            final String operationComment
    ) {
        Log.d(TAG, "addOperation");

        final String content = String.format(Locale.ENGLISH, "{\"user_id_to\":\"%s\",\"operation_type_id\":%d,\"timestamp\":%d,\"comment\":\"%s\"}", mUserId.toString(), operationType.getId(), System.currentTimeMillis(), operationComment);

        mDisposables.add(
                Observable.
                        fromCallable(() -> ServerConnector.sendAuthRequestAndGetResponse("addoperation", authToken, content)).
                        subscribeOn(Schedulers.io()).
                        observeOn(AndroidSchedulers.mainThread()).
                        subscribe(
                                serverApiResponse -> {
                                    Log.d(TAG, serverApiResponse.toString());
                                    onAddOperationComplete(serverApiResponse, operationType);
                                },
                                throwable -> {
                                    Log.e(TAG, Log.getStackTraceString(throwable));
                                    Toast.makeText(requireContext(), throwable.getMessage(), Toast.LENGTH_LONG).show();
                                }
                        )
        );
    }

    private void onAddOperationComplete (
            @NonNull final ServerApiResponse serverApiResponse,
            @NonNull final OperationType operationType
    ) {
        Log.d(TAG, "onAddOperationComplete serverApiResponse=" + serverApiResponse);
        if (serverApiResponse.getCode() == 200) {
            switch (operationType) {
                case THANKS: {
                    Toast.makeText(requireContext(), R.string.info_msg_add_thanks_complete, Toast.LENGTH_LONG).show();
                    break;
                }
                case MISSTRUST: {
                    Toast.makeText(requireContext(), R.string.info_msg_trust_is_lost, Toast.LENGTH_LONG).show();
                    break;
                }
                case MISSTRUST_CANCEL: {
                    Toast.makeText(requireContext(), R.string.info_msg_trust_restored, Toast.LENGTH_LONG).show();
                    break;
                }
            }
            refreshOperations();
        } else {
            Toast.makeText(requireContext(), R.string.err_msg_add_thanks_failed, Toast.LENGTH_LONG).show();
        }
    }

}
