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
import androidx.lifecycle.ViewModelProvider;
import androidx.paging.LivePagedListBuilder;
import androidx.paging.PagedList;
import androidx.recyclerview.widget.LinearLayoutManager;

import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Locale;
import java.util.UUID;
import java.util.concurrent.Executors;

import blagodarie.rating.R;
import blagodarie.rating.databinding.KeysFragmentBinding;
import blagodarie.rating.server.ServerApiResponse;
import blagodarie.rating.server.ServerConnector;
import blagodarie.rating.ui.AccountProvider;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;

public final class KeysFragment
        extends Fragment
        implements KeysUserActionListener,
        KeysAdapter.AdapterCommunicator {

    public interface FragmentCommunicator {
        void toAddKey ();
    }

    private static final String TAG = KeysFragment.class.getSimpleName();

    private KeysViewModel mViewModel;

    private KeysFragmentBinding mBinding;

    private KeysAdapter mKeysAdapter;

    private Account mAccount;

    private UUID mUserId;

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

        final KeysFragmentArgs args = KeysFragmentArgs.fromBundle(requireArguments());

        mUserId = args.getUserId();
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
        mDisposables.clear();
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
        mBinding.setUserActionListener(this);
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
        final KeysDataSource.KeysDataSourceFactory sourceFactory = new KeysDataSource.KeysDataSourceFactory(mUserId);

        final PagedList.Config config = new PagedList.Config.Builder()
                .setEnablePlaceholders(false)
                .setPageSize(10)
                .build();

        mViewModel.setOperations(
                new LivePagedListBuilder<>(sourceFactory, config).
                        setFetchExecutor(Executors.newSingleThreadExecutor()).
                        build()
        );
        mViewModel.getKeys().observe(requireActivity(), mKeysAdapter::submitList);
    }

    @Override
    public void onAddKey () {
        mFragmentCommunicator.toAddKey();
    }


    @Override
    public void onEditKey (@NonNull final Key key) {
        AccountProvider.getAuthToken(requireActivity(), mAccount, authToken -> editKey(authToken, key));
    }

    @Override
    public void onDeleteKey (@NonNull final Key key) {
        AccountProvider.getAuthToken(requireActivity(), mAccount, authToken -> deleteKey(authToken, key));
    }

    public void editKey (@Nullable final String authToken, @NonNull final Key key) {
        if (authToken != null) {
            final String content = String.format(Locale.ENGLISH, "{\"id\":%d,\"value\":\"%s\",\"type_id\":%d}", key.getId(), key.getValue(), key.getKeyType().getId());
            mDisposables.add(
                    Observable.
                            fromCallable(() -> ServerConnector.sendAuthRequestAndGetResponse("updatekey", authToken, content)).
                            subscribeOn(Schedulers.io()).
                            observeOn(AndroidSchedulers.mainThread()).
                            subscribe(
                                    this::extractDataFromServerApiResponse,
                                    throwable -> Toast.makeText(requireContext(), throwable.getMessage(), Toast.LENGTH_LONG).show()
                            )
            );
        }
    }

    public void deleteKey (@Nullable final String authToken, @NonNull final Key key) {
        if (authToken != null) {
            mDisposables.add(
                    Observable.
                            fromCallable(() -> ServerConnector.sendAuthRequestAndGetResponse(String.format(Locale.ENGLISH, "deletekey?id=%d", key.getId()), authToken)).
                            subscribeOn(Schedulers.io()).
                            observeOn(AndroidSchedulers.mainThread()).
                            subscribe(
                                    this::extractDataFromServerApiResponse,
                                    throwable -> Toast.makeText(requireContext(), throwable.getMessage(), Toast.LENGTH_LONG).show()
                            )
            );
        }
    }

    private void extractDataFromServerApiResponse (
            @NonNull final ServerApiResponse serverApiResponse) {
        Log.d(TAG, "extractDataFromServerApiResponse");
        if (serverApiResponse.getCode() == 200) {
            Toast.makeText(requireContext(), R.string.info_msg_key_saved, Toast.LENGTH_LONG).show();
            refreshKeys();
        } else {
            if (serverApiResponse.getBody() != null) {
                try {
                    final JSONObject jsonObject = new JSONObject(serverApiResponse.getBody());
                    String errorMessage = jsonObject.getString("message");
                    Toast.makeText(requireContext(), errorMessage, Toast.LENGTH_LONG).show();
                } catch (JSONException e) {
                    Toast.makeText(requireContext(), e.getLocalizedMessage(), Toast.LENGTH_LONG).show();
                }
            }
        }
    }
}
