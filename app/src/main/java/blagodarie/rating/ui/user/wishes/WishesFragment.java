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

import blagodarie.rating.R;
import blagodarie.rating.databinding.WishesFragmentBinding;
import blagodarie.rating.model.IWish;
import blagodarie.rating.model.entities.Wish;
import blagodarie.rating.repository.Repository;
import blagodarie.rating.repository.ServerRepository;
import blagodarie.rating.ui.user.profile.ProfileFragmentArgs;
import blagodarie.rating.ui.wishes.EditWishActivity;
import io.reactivex.disposables.CompositeDisposable;

public final class WishesFragment
        extends Fragment {

    private static final String TAG = WishesFragment.class.getSimpleName();

    private WishesViewModel mViewModel;

    private WishesFragmentBinding mBinding;

    private WishesAdapter mWishesAdapter;

    private Account mAccount;

    private UUID mUserId;

    private Repository mRepository = new ServerRepository();

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
        mDisposables.clear();
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
        mViewModel.setWishes(mRepository.getUserWishes(mUserId));
        mViewModel.getWishes().observe(requireActivity(), mWishesAdapter::submitList);
    }

    private void setupBinding () {
        mBinding.setViewModel(mViewModel);
        mBinding.setUserActionListener(this::onAddWishClick);
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
        i.setData(Uri.parse(getString(R.string.url_wish, wish.getUuid())));
        startActivity(i);
    }

    public void onAddWishClick () {
        final Intent intent = EditWishActivity.createSelfIntent(requireContext(), new Wish(UUID.randomUUID(), mUserId, "", new Date()), mAccount);
        startActivity(intent);
    }
}
