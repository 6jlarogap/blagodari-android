package blagodarie.rating.ui.people;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavDirections;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import java.util.UUID;

import blagodarie.rating.AppExecutors;
import blagodarie.rating.R;
import blagodarie.rating.databinding.PeopleFragmentBinding;
import blagodarie.rating.repository.AsyncRepository;
import blagodarie.rating.repository.AsyncServerRepository;

public final class PeopleFragment
        extends Fragment {

    public interface UserActionListener {
        void onSwipeRefresh ();
    }

    private static final String TAG = PeopleFragment.class.getSimpleName();

    private PeopleViewModel mViewModel;

    private PeopleFragmentBinding mBinding;

    private PeopleAdapter mPeopleAdapter;

    private SearchView mSearchView;

    private final SearchView.OnQueryTextListener mOnQueryTextListener = new SearchView.OnQueryTextListener() {
        @Override
        public boolean onQueryTextSubmit (String query) {
            return false;
        }

        @Override
        public boolean onQueryTextChange (String newText) {
            mViewModel.getSearchText().setValue(newText);
            return false;
        }
    };

    private final Observer<String> mSearchTextObserver = o -> refreshPeople();

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
        setHasOptionsMenu(true);
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
    public void onResume () {
        Log.d(TAG, "onResume");
        super.onResume();
        if(mSearchView != null) {
            mSearchView.setOnQueryTextListener(mOnQueryTextListener);
            mSearchView.setQuery(mViewModel.getSearchText().getValue(), false);
        }
    }

    @Override
    public void onPause () {
        Log.d(TAG, "onPause");
        super.onPause();
        mSearchView.setOnQueryTextListener(null);
    }

    @Override
    public void onDestroyView () {
        Log.d(TAG, "onDestroyView");
        super.onDestroyView();
        mViewModel.getSearchText().removeObserver(mSearchTextObserver);
    }

    @Override
    public void onDestroy () {
        Log.d(TAG, "onDestroy");
        super.onDestroy();
        mBinding = null;
    }

    @Override
    public void onCreateOptionsMenu (
            @NonNull final Menu menu,
            @NonNull final MenuInflater inflater
    ) {
        Log.d(TAG, "onCreateOptionsMenu");
        super.onCreateOptionsMenu(menu, inflater);
        requireActivity().getMenuInflater().inflate(R.menu.people_fragment, menu);

        final MenuItem searchMenuItem = menu.findItem(R.id.miSearch);
        if (mViewModel.getSearchText().getValue() != null &&
                !mViewModel.getSearchText().getValue().isEmpty()) {
            searchMenuItem.expandActionView();
        }
        mSearchView = (SearchView) searchMenuItem.getActionView();
        mSearchView.setOnQueryTextListener(mOnQueryTextListener);
        mSearchView.setQuery(mViewModel.getSearchText().getValue(), false);
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
        mViewModel.getSearchText().observe(requireActivity(), mSearchTextObserver);
    }

    private void refreshPeople () {
        Log.d(TAG, "refreshPeople");
        mViewModel.setPeople(mAsyncRepository.getLiveDataPagedListFromDataSource(new PeopleDataSource.PeopleDataSourceFactory(mViewModel.getSearchText().getValue() != null ? mViewModel.getSearchText().getValue() : "")));
        mViewModel.getPeople().observe(requireActivity(), mPeopleAdapter::submitList);
    }

    private void setupBinding () {
        mBinding.setViewModel(mViewModel);
        mBinding.setUserActionListener(mUserActionListener);
        mBinding.rvPeople.setLayoutManager(new LinearLayoutManager(requireContext()));
        mBinding.rvPeople.setAdapter(mPeopleAdapter);
    }

    private void initOperationsAdapter () {
        mPeopleAdapter = new PeopleAdapter(this::onProfileClick);
    }

    private void onProfileClick (@NonNull final UUID userId) {
        /*final NavDirections action = PeopleFragmentDirections.actionPeopleFragmentToProfileFragment(userId.toString());
        NavHostFragment.findNavController(this).navigate(action);*/
    }
}
