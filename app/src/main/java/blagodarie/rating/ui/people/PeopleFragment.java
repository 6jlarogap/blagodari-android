package blagodarie.rating.ui.people;

import android.accounts.Account;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import java.util.UUID;

import blagodarie.rating.AppExecutors;
import blagodarie.rating.R;
import blagodarie.rating.databinding.PeopleFragmentBinding;
import blagodarie.rating.repository.AsyncRepository;
import blagodarie.rating.repository.AsyncServerRepository;
import blagodarie.rating.ui.user.wishes.WishesFragment;

public class PeopleFragment
        extends Fragment {

    public interface UserActionListener {
        void onSwipeRefresh ();
    }

    private static final String TAG = WishesFragment.class.getSimpleName();

    private PeopleViewModel mViewModel;

    private PeopleFragmentBinding mBinding;

    private PeopleAdapter mPeopleAdapter;

    private Account mAccount;

    private UUID mUserId;

    @NonNull
    private final AsyncRepository mAsyncRepository = new AsyncServerRepository(AppExecutors.getInstance().networkIO(), AppExecutors.getInstance().mainThread());

    @NonNull
    private final UserActionListener mUserActionListener = new UserActionListener() {
        @Override
        public void onSwipeRefresh () {
            mViewModel.getDownloadInProgress().set(true);
            refreshPeople();
            mViewModel.getDownloadInProgress().set(false);
        }
    };

    @NonNull
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
    }

    @Override
    public void onActivityCreated (@Nullable final Bundle savedInstanceState) {
        Log.d(TAG, "onActivityCreated");
        super.onActivityCreated(savedInstanceState);

        initOperationsAdapter();
        initViewModel();
        setupBinding();
    }

    @Override
    public void onStart () {
        Log.d(TAG, "onStart");
        super.onStart();
        refreshPeople();
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
        mBinding = PeopleFragmentBinding.inflate(inflater, container, false);
    }

    private void initViewModel () {
        Log.d(TAG, "initViewModel");
        mViewModel = new ViewModelProvider(requireActivity()).get(PeopleViewModel.class);
        //mViewModel.isOwnProfile().set(mAccount != null && mAccount.name.equals(mUserId.toString()));
    }

    private void refreshPeople () {
        Log.d(TAG, "refreshPeople");
        mViewModel.setPeople(mAsyncRepository.getLiveDataPagedListFromDataSource(new PeopleDataSource.PeopleDataSourceFactory(mBinding.etFilter.getText().toString())));
        mViewModel.getPeople().observe(requireActivity(), mPeopleAdapter::submitList);
    }

    private void setupBinding () {
        mBinding.setViewModel(mViewModel);
        mBinding.setUserActionListener(mUserActionListener);
        mBinding.rvPeople.setLayoutManager(new LinearLayoutManager(requireContext()));
        mBinding.rvPeople.setAdapter(mPeopleAdapter);
        mBinding.etFilter.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged (CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged (CharSequence s, int start, int before, int count) {
                refreshPeople();
            }

            @Override
            public void afterTextChanged (Editable s) {

            }
        });
    }

    private void initOperationsAdapter () {
        mPeopleAdapter = new PeopleAdapter(this::onProfileClick);
    }

    private void onProfileClick (@NonNull final UUID userId) {
        final Intent i = new Intent(Intent.ACTION_VIEW);
        i.setData(Uri.parse(getString(R.string.url_profile, userId)));
        startActivity(i);
    }
}
