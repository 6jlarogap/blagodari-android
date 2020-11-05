package blagodarie.rating.ui.user.abilities;

import android.accounts.AccountManager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import org.jetbrains.annotations.NotNull;

import java.util.Date;

import blagodarie.rating.AppExecutors;
import blagodarie.rating.R;
import blagodarie.rating.databinding.EditAbilityFragmentBinding;
import blagodarie.rating.model.IAbility;
import blagodarie.rating.repository.AsyncServerRepository;
import blagodarie.rating.server.BadAuthorizationTokenException;
import blagodarie.rating.ui.AccountProvider;
import blagodarie.rating.ui.AccountSource;

public final class EditAbilityFragment
        extends Fragment {

    public interface UserActionListener {
        void onSaveClick (@NonNull final IAbility ability);
    }

    private static final String TAG = EditAbilityFragment.class.getSimpleName();

    private EditAbilityFragmentBinding mBinding;

    private IAbility mAbility;

    private final AsyncServerRepository mAsyncRepository = new AsyncServerRepository(AppExecutors.getInstance().networkIO(), AppExecutors.getInstance().mainThread());

    private final UserActionListener mUserActionListener = new UserActionListener() {
        @Override
        public void onSaveClick (@NonNull final IAbility ability) {
            if (!mAbility.getText().isEmpty()) {
                mAbility.setLastEdit(new Date());
                attemptToSaveAbility();
            } else {
                mBinding.etAbilityText.setError(getString(R.string.err_msg_required_to_fill));
            }
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

        final EditAbilityFragmentArgs args = EditAbilityFragmentArgs.fromBundle(requireArguments());

        mAbility = args.getAbility();
    }

    @Override
    public void onActivityCreated (@Nullable final Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        setupBinding();
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
        mBinding = EditAbilityFragmentBinding.inflate(inflater, container, false);
    }

    private void setupBinding () {
        Log.d(TAG, "setupBinding");
        mBinding.setAbility(mAbility);
        mBinding.setUserActionListener(mUserActionListener);
    }

    private void attemptToSaveAbility () {
        AccountSource.INSTANCE.getAccount(
                requireActivity(),
                true,
                account -> {
                    if (account != null) {
                        AccountProvider.getAuthToken(
                                requireActivity(),
                                account,
                                authToken -> {
                                    if (authToken != null) {
                                        saveAbility(authToken);
                                    } else {
                                        Toast.makeText(requireContext(), R.string.info_msg_need_log_in, Toast.LENGTH_LONG).show();
                                    }
                                }
                        );
                    }
                }

        );
    }

    private void saveAbility (
            @NonNull final String authToken
    ) {
        mAsyncRepository.setAuthToken(authToken);
        mAsyncRepository.upsertAbility(
                mAbility,
                () -> {
                    Toast.makeText(requireContext(), R.string.info_msg_ability_saved, Toast.LENGTH_LONG).show();
                    requireActivity().onBackPressed();
                },
                throwable -> {
                    if (throwable instanceof BadAuthorizationTokenException) {
                        AccountManager.get(requireContext()).invalidateAuthToken(getString(R.string.account_type), authToken);
                        attemptToSaveAbility();
                    } else {
                        Log.e(TAG, Log.getStackTraceString(throwable));
                        Toast.makeText(requireContext(), throwable.getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }
}
