package blagodarie.rating.ui.operations;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AccountManagerFuture;
import android.accounts.AuthenticatorException;
import android.accounts.OperationCanceledException;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.ViewModelProvider;
import androidx.paging.LivePagedListBuilder;
import androidx.paging.PagedList;
import androidx.recyclerview.widget.LinearLayoutManager;

import java.io.IOException;
import java.util.Locale;
import java.util.UUID;
import java.util.concurrent.Executors;

import blagodarie.rating.OnOperationListener;
import blagodarie.rating.R;
import blagodarie.rating.auth.AccountGeneral;
import blagodarie.rating.databinding.EnterOperationCommentDialogBinding;
import blagodarie.rating.databinding.OperationsActivityBinding;
import blagodarie.rating.server.ServerApiResponse;
import blagodarie.rating.server.ServerConnector;
import blagodarie.rating.ui.AccountProvider;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;

public final class OperationsActivity
        extends AppCompatActivity
        implements OnOperationListener {

    private static final String TAG = OperationsActivity.class.getSimpleName();

    private static final String EXTRA_USER_ID = "blagodarie.rating.ui.operations.OperationsActivity.USER_ID";

    private UUID mUserId;

    private OperationsViewModel mViewModel;

    private OperationsActivityBinding mActivityBinding;

    private OperationAdapter mOperationsAdapter;

    private AccountManager mAccountManager;

    private Account mAccount;

    private CompositeDisposable mCompositeDisposable = new CompositeDisposable();

    @Override
    protected void onCreate (@Nullable final Bundle savedInstanceState) {
        Log.d(TAG, "onCreate");
        super.onCreate(savedInstanceState);

        mAccountManager = AccountManager.get(this);

        mUserId = (UUID) getIntent().getSerializableExtra(EXTRA_USER_ID);
        if (mUserId != null) {
            initViewModel();
            initBinding();
            initOperationsAdapter();
            AccountProvider.getAccount(
                    this,
                    account -> {
                        if (account != null) {
                            mAccount = account;
                            mViewModel.isSelfProfile().set(mUserId.toString().equals(mAccountManager.getUserData(mAccount, AccountGeneral.USER_DATA_USER_ID)));
                        } else {
                            mViewModel.isSelfProfile().set(false);
                        }
                    }
            );
        } else {
            Toast.makeText(this, R.string.err_msg_missing_user_id, Toast.LENGTH_LONG).show();
            finish();
        }
    }

    @Override
    protected void onDestroy () {
        Log.d(TAG, "onDestroy");
        super.onDestroy();
        mCompositeDisposable.dispose();
    }

    private void initViewModel () {
        Log.d(TAG, "initViewModel");
        mViewModel = new ViewModelProvider(this).get(OperationsViewModel.class);
    }

    private void initBinding () {
        Log.d(TAG, "initBinding");
        mActivityBinding = DataBindingUtil.setContentView(this, R.layout.operations_activity);
        mActivityBinding.setViewModel(mViewModel);
        mActivityBinding.setOnOperationListener(this);
    }

    private void initOperationsAdapter () {
        //if (mOperationsAdapter == null) {
        OperationDataSource.OperationSourceFactory sourceFactory = new OperationDataSource.OperationSourceFactory(mUserId);

        PagedList.Config config = new PagedList.Config.Builder()
                .setEnablePlaceholders(false)
                .setPageSize(10)
                .build();

        mViewModel.setOperations(
                new LivePagedListBuilder<>(sourceFactory, config).
                        setFetchExecutor(Executors.newSingleThreadExecutor()).
                        build()
        );

        mOperationsAdapter = new OperationAdapter();
        mViewModel.getOperations().observe(this, mOperationsAdapter::submitList);

        mActivityBinding.rvOperations.setLayoutManager(new LinearLayoutManager(this));
        mActivityBinding.rvOperations.setAdapter(mOperationsAdapter);
        //}
    }

    public static Intent createSelfIntent (
            @NonNull final Context context,
            @NonNull final UUID userId
    ) {
        Log.d(TAG, "createSelfIntent");
        final Intent intent = new Intent(context, OperationsActivity.class);
        intent.putExtra(EXTRA_USER_ID, userId);
        return intent;
    }

    @Override
    public void onThanks () {
        addOperationComment(1);
    }

    @Override
    public void onTrustless () {

    }

    @Override
    public void onTrustlessCancel () {

    }

    private void addOperationComment (final int operationTypeId) {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        final EnterOperationCommentDialogBinding binding = DataBindingUtil.inflate(LayoutInflater.from(this), R.layout.enter_operation_comment_dialog, null, false);
        new AlertDialog.
                Builder(this).
                setCancelable(false).
                setTitle(R.string.txt_comment).
                setView(binding.getRoot()).
                setNegativeButton(android.R.string.cancel, (dialogInterface, i) -> {
                    imm.hideSoftInputFromWindow(binding.etOperationComment.getWindowToken(), 0);
                }).
                setPositiveButton(android.R.string.ok,
                        (dialogInterface, i) -> {
                            imm.hideSoftInputFromWindow(binding.etOperationComment.getWindowToken(), 0);
                            final String operationComment = binding.etOperationComment.getText().toString();
                            if (mAccount != null) {
                                getAuthTokenAndAddOperation(operationTypeId, operationComment);
                            } else {
                                mAccountManager.addAccount(
                                        getString(R.string.account_type),
                                        getString(R.string.token_type),
                                        null,
                                        null,
                                        this,
                                        accountManagerFuture -> onAddAccountFinished(accountManagerFuture, 1, operationComment),
                                        null
                                );
                            }
                        }).
                create().
                show();
        binding.etOperationComment.requestFocus();
        imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY);
    }


    public void onAddAccountFinished (
            final AccountManagerFuture<Bundle> result,
            final int operationTypeId,
            final String operationComment
    ) {
        Log.d(TAG, "onAddAccountFinish");
        try {
            final Bundle bundle = result.getResult();
            mAccount = new Account(
                    bundle.getString(AccountManager.KEY_ACCOUNT_NAME),
                    bundle.getString(AccountManager.KEY_ACCOUNT_TYPE)
            );
            getAuthTokenAndAddOperation(operationTypeId, operationComment);
        } catch (OperationCanceledException e) {
            Log.e(TAG, Log.getStackTraceString(e));
            Toast.makeText(this, getString(R.string.err_msg_account_not_created), Toast.LENGTH_LONG).show();
            finish();
        } catch (AuthenticatorException e) {
            Log.e(TAG, Log.getStackTraceString(e));
            Toast.makeText(this, getString(R.string.err_msg_authentication_error), Toast.LENGTH_LONG).show();
            finish();
        } catch (IOException e) {
            Log.e(TAG, Log.getStackTraceString(e));
            finish();
        }
    }

    private void getAuthTokenAndAddOperation (
            final int operationTypeId,
            final String operationComment
    ) {
        Log.d(TAG, "getAuthTokenAndAddOperation");
        /*AccountProvider.getAuthToken(
                this,
                mAccount,
                accountManagerFuture -> onGetAuthTokenForAddOperationComplete(accountManagerFuture, operationTypeId, operationComment)
        );*/
    }


    private void onGetAuthTokenForAddOperationComplete (
            @NonNull final AccountManagerFuture<Bundle> future,
            final int operationTypeId,
            final String operationComment
    ) {
        Log.d(TAG, "onGetAuthTokenForUpdateProfileDataComplete");
        try {
            final Bundle bundle = future.getResult();
            if (bundle != null) {
                final String authToken = bundle.getString(AccountManager.KEY_AUTHTOKEN);
                if (authToken != null) {
                    addOperation(authToken, operationTypeId, operationComment);
                }
            }
        } catch (AuthenticatorException | IOException | OperationCanceledException e) {
            e.printStackTrace();
        }
    }

    private void addOperation (
            @NonNull final String authToken,
            final int operationTypeId,
            final String operationComment
    ) {
        Log.d(TAG, "addOperation");

        final String content = String.format(Locale.ENGLISH, "{\"user_id_to\":\"%s\",\"operation_type_id\":%d,\"timestamp\":%d,\"comment\":\"%s\"}", mUserId.toString(), operationTypeId, System.currentTimeMillis(), operationComment);

        mCompositeDisposable.add(
                Observable.
                        fromCallable(() -> ServerConnector.sendAuthRequestAndGetResponse("addoperation", authToken, content)).
                        subscribeOn(Schedulers.io()).
                        observeOn(AndroidSchedulers.mainThread()).
                        subscribe(
                                serverApiResponse -> {
                                    Log.d(TAG, serverApiResponse.toString());
                                    onAddOperationComplete(serverApiResponse, operationTypeId);
                                },
                                throwable -> {
                                    Log.e(TAG, Log.getStackTraceString(throwable));
                                    Toast.makeText(this, throwable.getMessage(), Toast.LENGTH_LONG).show();
                                }
                        )
        );
    }


    private void onAddOperationComplete (
            @NonNull final ServerApiResponse serverApiResponse,
            final int operationTypeId
    ) {
        Log.d(TAG, "onUpdateDataComplete serverApiResponse=" + serverApiResponse);
        if (serverApiResponse.getCode() == 200) {
            switch (operationTypeId) {
                case 1: {
                    Toast.makeText(this, R.string.info_msg_add_thanks_complete, Toast.LENGTH_LONG).show();
                    break;
                }
                case 2: {
                    Toast.makeText(this, R.string.info_msg_trust_is_lost, Toast.LENGTH_LONG).show();
                    break;
                }
                case 3: {
                    Toast.makeText(this, R.string.info_msg_trust_restored, Toast.LENGTH_LONG).show();
                    break;
                }
            }
            initOperationsAdapter();
        } else {
            Toast.makeText(this, R.string.err_msg_add_thanks_failed, Toast.LENGTH_LONG).show();
        }
    }
}
