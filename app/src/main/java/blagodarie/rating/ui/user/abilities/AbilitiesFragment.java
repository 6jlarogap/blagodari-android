package blagodarie.rating.ui.user.abilities;

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
import androidx.recyclerview.widget.LinearLayoutManager;

import org.jetbrains.annotations.NotNull;

import java.util.UUID;

import blagodarie.rating.AppExecutors;
import blagodarie.rating.databinding.AbilitiesFragmentBinding;
import blagodarie.rating.model.entities.Ability;
import blagodarie.rating.repository.AsyncRepository;
import blagodarie.rating.repository.AsyncServerRepository;
import blagodarie.rating.ui.user.keys.KeysFragment;
import blagodarie.rating.ui.user.profile.ProfileFragmentArgs;

public final class AbilitiesFragment
        extends Fragment {

    public interface FragmentCommunicator {
        void toEditAbility (@Nullable final Ability ability);
    }

    public interface UserActionListener {
        void onAddAbilityClick ();
    }

    private static final String TAG = AbilitiesFragment.class.getSimpleName();

    private AbilitiesViewModel mViewModel;

    private AbilitiesFragmentBinding mBinding;

    private AbilitiesAdapter mAbilitiesAdapter;

    private Account mAccount;

    private UUID mUserId;

    private final AsyncRepository mAsyncRepository = new AsyncServerRepository(AppExecutors.getInstance().networkIO(), AppExecutors.getInstance().mainThread());

    private final UserActionListener mUserActionListener = new UserActionListener() {
        @Override
        public void onAddAbilityClick () {
            mFragmentCommunicator.toEditAbility(null);
        }
    };

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

        final ProfileFragmentArgs args = ProfileFragmentArgs.fromBundle(requireArguments());

        mUserId = args.getUserId();
        mAccount = args.getAccount();
    }

    @Override
    public void onActivityCreated (@Nullable final Bundle savedInstanceState) {
        Log.d(TAG, "onActivityCreated");
        super.onActivityCreated(savedInstanceState);
        try {
            mFragmentCommunicator = (FragmentCommunicator) requireActivity();
        } catch (ClassCastException e) {
            Log.e(TAG, requireActivity().getClass().getName() + " must implement " + KeysFragment.FragmentCommunicator.class.getName());
            throw new ClassCastException(requireActivity().getClass().getName() + " must implement " + KeysFragment.FragmentCommunicator.class.getName());
        }

        initOperationsAdapter();
        initViewModel();
        setupBinding();
    }

    @Override
    public void onStart () {
        Log.d(TAG, "onStart");
        super.onStart();
        refreshAbilities();
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
        mBinding = AbilitiesFragmentBinding.inflate(inflater, container, false);
    }

    private void initViewModel () {
        Log.d(TAG, "initViewModel");
        mViewModel = new ViewModelProvider(requireActivity()).get(AbilitiesViewModel.class);
        mViewModel.isOwnProfile().set(mAccount != null && mAccount.name.equals(mUserId.toString()));
    }

    private void refreshAbilities () {
        Log.d(TAG, "refreshAbilities");
        mViewModel.setAbilities(mAsyncRepository.getLiveDataPagedListFromDataSource(new AbilitiesDataSource.AbilitiesDataSourceFactory(mUserId)));
        mViewModel.getAbilities().observe(requireActivity(), mAbilitiesAdapter::submitList);
    }

    private void setupBinding () {
        mBinding.setViewModel(mViewModel);
        mBinding.setUserActionListener(mUserActionListener);
        mBinding.rvAbilities.setLayoutManager(new LinearLayoutManager(requireContext()));
        mBinding.rvAbilities.setAdapter(mAbilitiesAdapter);
        mBinding.srlRefreshProfileInfo.setOnRefreshListener(() -> {
            mViewModel.getDownloadInProgress().set(true);
            refreshAbilities();
            mViewModel.getDownloadInProgress().set(false);
        });
    }

    private void initOperationsAdapter () {
        mAbilitiesAdapter = new AbilitiesAdapter();
    }

}
