package blagodarie.rating.ui.user.wishes;

import android.accounts.Account;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import org.jetbrains.annotations.NotNull;

import java.util.Date;
import java.util.UUID;

import blagodarie.rating.AppExecutors;
import blagodarie.rating.R;
import blagodarie.rating.databinding.WishesFragmentBinding;
import blagodarie.rating.model.IWish;
import blagodarie.rating.model.entities.Wish;
import blagodarie.rating.repository.AsyncRepository;
import blagodarie.rating.repository.AsyncServerRepository;
import blagodarie.rating.ui.user.profile.ProfileFragmentArgs;
import blagodarie.rating.ui.wishes.EditWishActivity;
import io.reactivex.disposables.CompositeDisposable;

public final class WishesFragment
        extends Fragment {

    public interface UserActionListener {
        void onAddWishClick ();
    }

    private static final String TAG = WishesFragment.class.getSimpleName();

    private WishesViewModel mViewModel;

    private WishesFragmentBinding mBinding;

    private WishesAdapter mWishesAdapter;

    private Account mAccount;

    private UUID mUserId;

    @NonNull
    private final AsyncRepository mAsyncRepository = new AsyncServerRepository(AppExecutors.getInstance().networkIO(), AppExecutors.getInstance().mainThread());

    @NonNull
    private final UserActionListener mUserActionListener = new UserActionListener() {
        @Override
        public void onAddWishClick () {
            final Intent intent = EditWishActivity.createSelfIntent(requireContext(), new Wish(UUID.randomUUID(), mUserId, "", new Date()), mAccount);
            startActivity(intent);
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

        final ProfileFragmentArgs args = ProfileFragmentArgs.fromBundle(requireArguments());

        mUserId = args.getUserId();
        mAccount = args.getAccount();
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
        refreshWishes();
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
        mBinding = WishesFragmentBinding.inflate(inflater, container, false);
    }

    private void initViewModel () {
        Log.d(TAG, "initViewModel");
        mViewModel = new ViewModelProvider(requireActivity()).get(WishesViewModel.class);
        mViewModel.isOwnProfile().set(mAccount != null && mAccount.name.equals(mUserId.toString()));
    }

    private void refreshWishes () {
        Log.d(TAG, "refreshWishes");
        mViewModel.setWishes(mAsyncRepository.getLiveDataPagedListFromDataSource(new WishesDataSource.WishesDataSourceFactory(mUserId)));
        mViewModel.getWishes().observe(requireActivity(), mWishesAdapter::submitList);
    }

    private void setupBinding () {
        mBinding.setViewModel(mViewModel);
        mBinding.setUserActionListener(mUserActionListener);
        mBinding.rvWishes.setLayoutManager(new LinearLayoutManager(requireContext()));
        mBinding.rvWishes.setAdapter(mWishesAdapter);
        mBinding.srlRefreshProfileInfo.setOnRefreshListener(() -> {
            mViewModel.getDownloadInProgress().set(true);
            refreshWishes();
            mViewModel.getDownloadInProgress().set(false);
        });
    }

    private void initOperationsAdapter () {
        mWishesAdapter = new WishesAdapter(this::onWishClick);
    }

    private void onWishClick (@NonNull final IWish wish) {
        final Intent i = new Intent(Intent.ACTION_VIEW);
        i.setData(Uri.parse(getString(R.string.url_wish, wish.getId())));
        startActivity(i);
    }

}
