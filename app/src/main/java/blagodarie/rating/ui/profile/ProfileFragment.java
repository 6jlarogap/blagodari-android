package blagodarie.rating.ui.profile;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavDirections;
import androidx.navigation.fragment.NavHostFragment;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

import org.jetbrains.annotations.NotNull;

import java.util.EnumMap;
import java.util.Map;
import java.util.UUID;

import blagodarie.rating.AppExecutors;
import blagodarie.rating.MainActivityDirections;
import blagodarie.rating.R;
import blagodarie.rating.databinding.ProfileFragmentBinding;
import blagodarie.rating.model.entities.OperationType;
import blagodarie.rating.operations.OperationToUserManager;
import blagodarie.rating.repository.AsyncServerRepository;
import blagodarie.rating.server.BadAuthorizationTokenException;
import blagodarie.rating.ui.AccountProvider;
import blagodarie.rating.ui.AccountSource;
import blagodarie.rating.ui.GridAutofitLayoutManager;

public final class ProfileFragment
        extends Fragment
        implements ProfileUserActionListener {

    private static final String TAG = ProfileFragment.class.getSimpleName();

    private ProfileViewModel mViewModel;

    private ProfileFragmentBinding mBinding;

    private ThanksUsersAdapter mThanksUsersAdapter;

    private UUID mUserId;

    private Account mLastAccount;

    private final AsyncServerRepository mAsyncRepository = new AsyncServerRepository(AppExecutors.getInstance().networkIO(), AppExecutors.getInstance().mainThread());

    @NotNull
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
        super.onViewCreated(view, savedInstanceState);
        initUserId();
    }

    @Override
    public void onActivityCreated (@Nullable final Bundle savedInstanceState) {
        Log.d(TAG, "onActivityCreated");
        super.onActivityCreated(savedInstanceState);

        AccountSource.INSTANCE.getAccount(
                requireActivity(),
                false,
                account -> {
                    mLastAccount = account;
                    if (account != null && account.name.equals(mUserId.toString())) {
                        NavHostFragment.findNavController(this).popBackStack();
                        NavHostFragment.findNavController(this).navigate(R.id.global_action_to_user);
                    } else {
                        initThanksUserAdapter();
                        initViewModel();
                        setupBinding();
                        refreshProfileData();
                    }
                }
        );
    }

    private void initUserId () {
        Log.d(TAG, "initUserId");
        final ProfileFragmentArgs args = ProfileFragmentArgs.fromBundle(requireArguments());
        mUserId = UUID.fromString(args.getUserUuid());
    }

    @Override
    public void onResume () {
        super.onResume();
        AccountSource.INSTANCE.getAccount(
                requireActivity(),
                false,
                account -> {
                    if (account != null && account.name.equals(mUserId.toString())) {
                        NavHostFragment.findNavController(this).popBackStack();
                        NavHostFragment.findNavController(this).navigate(R.id.global_action_to_user);
                    } else {
                        if (account == null && mLastAccount != null ||
                                account != null && !account.equals(mLastAccount)) {
                            mLastAccount = account;
                            refreshProfileData();
                        }
                    }
                }
        );
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
        mBinding = ProfileFragmentBinding.inflate(inflater, container, false);
    }

    private void initThanksUserAdapter () {
        Log.d(TAG, "initThanksUserAdapter");
        mThanksUsersAdapter = new ThanksUsersAdapter(this::onThanksUserClick);
    }

    private void initViewModel () {
        Log.d(TAG, "initViewModel");
        mViewModel = new ViewModelProvider(requireActivity()).get(ProfileViewModel.class);
        mViewModel.discardValues();
        mViewModel.getQrCode().set(createQrCodeBitmap());
    }

    private void setupBinding () {
        Log.d(TAG, "setupBinding");
        mBinding.setUserActionListener(this);
        mBinding.srlRefreshProfileInfo.setOnRefreshListener(this::refreshProfileData);
        mBinding.rvThanksUsers.setLayoutManager(new GridAutofitLayoutManager(requireContext(), (int) ((getResources().getDimension(R.dimen.thanks_user_photo_width) + (getResources().getDimension(R.dimen.thanks_user_photo_margin) * 2)))));
        mBinding.rvThanksUsers.setAdapter(mThanksUsersAdapter);
        mBinding.setViewModel(mViewModel);
    }

    @Override
    public void onCreateOptionsMenu (
            @NonNull final Menu menu,
            @NonNull final MenuInflater inflater
    ) {
        Log.d(TAG, "onCreateOptionsMenu");
        super.onCreateOptionsMenu(menu, inflater);
        requireActivity().getMenuInflater().inflate(R.menu.profile_fragment, menu);
    }

    @Override
    public boolean onOptionsItemSelected (MenuItem item) {
        boolean result;
        if (item.getItemId() == R.id.miShare) {
            share();
            result = true;
        } else {
            result = super.onOptionsItemSelected(item);
        }
        return result;
    }

    @NonNull
    private Bitmap createQrCodeBitmap () {
        Log.d(TAG, "createQrCodeBitmap");
        final int width = 500;
        final int height = 500;
        Bitmap result = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        final QRCodeWriter writer = new QRCodeWriter();
        final Map<EncodeHintType, Object> hints = new EnumMap<>(EncodeHintType.class);
        hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");
        hints.put(EncodeHintType.MARGIN, 0); // default = 4
        try {
            final BitMatrix bitMatrix = writer.encode(
                    getString(R.string.url_profile, mUserId.toString()),
                    BarcodeFormat.QR_CODE,
                    width,
                    height,
                    hints
            );
            for (int x = 0; x < width; x++) {
                for (int y = 0; y < height; y++) {
                    result.setPixel(x, y, bitMatrix.get(x, y) ? Color.BLACK : Color.TRANSPARENT);
                }
            }
        } catch (WriterException e) {
            Log.e(TAG, Log.getStackTraceString(e));
        }
        return result;
    }

    public final void refreshProfileData () {
        Log.d(TAG, "refreshProfileData");
        AccountSource.INSTANCE.getAccount(
                requireActivity(),
                false,
                account -> {
                    mLastAccount = account;
                    if (account != null) {
                        AccountProvider.getAuthToken(requireActivity(), account, this::downloadProfileData);
                    } else {
                        downloadProfileData(null);
                    }
                }
        );
    }

    private void onThanksUserClick (@NonNull final UUID userId) {
        Log.d(TAG, "onThanksUserClick");
        NavHostFragment.findNavController(this).navigate(Uri.parse(getString(R.string.url_profile, userId)));
    }

    private void downloadProfileData (
            @Nullable final String authToken
    ) {
        Log.d(TAG, "downloadProfileData");
        mViewModel.getDownloadInProgress().set(true);

        mAsyncRepository.setAuthToken(authToken);
        mAsyncRepository.getProfileInfo(
                mUserId,
                profileInfo -> {
                    mViewModel.getDownloadInProgress().set(false);
                    mViewModel.getProfileInfo().set(profileInfo);
                },
                throwable -> {
                    mViewModel.getDownloadInProgress().set(false);
                    Toast.makeText(requireActivity(), throwable.getMessage(), Toast.LENGTH_LONG).show();
                });

        refreshThanksUsers();
    }

    private void refreshThanksUsers () {
        Log.d(TAG, "refreshThanksUsers");
        mViewModel.setThanksUsers(mAsyncRepository.getLiveDataPagedListFromDataSource(new ThanksUsersDataSource.ThanksUserDataSourceFactory(mUserId)));
        mViewModel.getThanksUsers().observe(requireActivity(), mThanksUsersAdapter::submitList);
    }

    public void share () {
        Log.d(TAG, "share");
        final Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_SEND);
        sendIntent.putExtra(Intent.EXTRA_TEXT, getString(R.string.url_profile, mUserId.toString()));
        sendIntent.setType("text/plain");
        startActivity(Intent.createChooser(sendIntent, "Поделиться"));
    }

    @Override
    public void onTrustClick () {
        Log.d(TAG, "onTrustClick");
        attemptToAddOperation(OperationType.TRUST);
    }

    @Override
    public void onMistrustClick () {
        Log.d(TAG, "onMistrustClick");
        attemptToAddOperation(OperationType.MISTRUST);
    }

    @Override
    public void onThanksClick () {
        Log.d(TAG, "onThanksClick");
        attemptToAddOperation(OperationType.THANKS);
    }

    public void attemptToAddOperation (@NonNull final OperationType operationType) {
        Log.d(TAG, "attemptToAddOperation");
        AccountSource.INSTANCE.getAccount(
                requireActivity(),
                true,
                account -> {
                    mLastAccount = account;
                    if (account != null) {
                        if (account.name.equals(mUserId.toString())) {
                            Toast.makeText(requireContext(), R.string.info_msg_cant_thanks_yourself, Toast.LENGTH_LONG).show();
                        } else {
                            AccountProvider.getAuthToken(requireActivity(), account, authToken -> {
                                if (authToken != null) {
                                    mAsyncRepository.setAuthToken(authToken);
                                    new OperationToUserManager().
                                            createOperationToUser(
                                                    requireActivity(),
                                                    UUID.fromString(account.name),
                                                    mUserId,
                                                    operationType,
                                                    mAsyncRepository,
                                                    () -> {
                                                        Toast.makeText(requireContext(), R.string.info_msg_saved, Toast.LENGTH_LONG).show();
                                                        refreshProfileData();
                                                    },
                                                    throwable -> {
                                                        Log.e(TAG, Log.getStackTraceString(throwable));
                                                        if (throwable instanceof BadAuthorizationTokenException) {
                                                            AccountManager.get(requireContext()).invalidateAuthToken(getString(R.string.account_type), authToken);
                                                            attemptToAddOperation(operationType);
                                                        } else {
                                                            Toast.makeText(requireContext(), R.string.err_msg_not_saved, Toast.LENGTH_LONG).show();
                                                        }
                                                    });
                                } else {
                                    Toast.makeText(requireContext(), R.string.info_msg_need_log_in, Toast.LENGTH_LONG).show();
                                }
                            });
                        }
                    }
                }
        );
    }

    @Override
    public void onOperationsClick () {
        Log.d(TAG, "onOperationsClick");
        final NavDirections action = MainActivityDirections.actionGlobalOperationsFragment().setUserId(mUserId);
        NavHostFragment.findNavController(this).navigate(action);
    }

    @Override
    public void onWishesClick () {
        Log.d(TAG, "onWishesClick");
        final NavDirections action = MainActivityDirections.actionGlobalWishesFragment(mUserId);
        NavHostFragment.findNavController(this).navigate(action);
    }

    @Override
    public void onAbilitiesClick () {
        Log.d(TAG, "onAbilitiesClick");
        final NavDirections action = MainActivityDirections.actionGlobalAbilitiesFragment(mUserId);
        NavHostFragment.findNavController(this).navigate(action);
    }

    @Override
    public void onKeysClick () {
        Log.d(TAG, "onKeysClick");
        final NavDirections action = MainActivityDirections.actionGlobalKeysFragment(mUserId);
        NavHostFragment.findNavController(this).navigate(action);
    }

    @Override
    public void onSocialGraphClick () {
        Log.d(TAG, "onSocialGraphClick");
        final NavDirections action = ProfileFragmentDirections.actionGlobalGraphFragment().setUserId(mUserId);
        NavHostFragment.findNavController(this).navigate(action);
    }

}
