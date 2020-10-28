package blagodarie.rating.ui.user.profile;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.BindingAdapter;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.squareup.picasso.Picasso;

import org.jetbrains.annotations.NotNull;

import java.util.EnumMap;
import java.util.Map;
import java.util.UUID;

import blagodarie.rating.AppExecutors;
import blagodarie.rating.R;
import blagodarie.rating.auth.AccountGeneral;
import blagodarie.rating.databinding.ProfileFragmentBinding;
import blagodarie.rating.model.entities.OperationType;
import blagodarie.rating.operations.OperationToUserManager;
import blagodarie.rating.repository.AsyncServerRepository;
import blagodarie.rating.server.BadAuthorizationTokenException;
import blagodarie.rating.ui.AccountProvider;
import blagodarie.rating.ui.user.GridAutofitLayoutManager;
import blagodarie.rating.ui.user.UserViewModel;
import io.reactivex.disposables.CompositeDisposable;

public final class ProfileFragment
        extends Fragment
        implements ProfileUserActionListener {

    public interface FragmentCommunicator {
        void toOperationsFromProfile ();

        void toWishes ();

        void toAbilities ();

        void toKeysFromProfile ();

        void toGraph ();
    }

    private static final String TAG = ProfileFragment.class.getSimpleName();

    private ProfileViewModel mViewModel;

    private ProfileFragmentBinding mBinding;

    private ThanksUsersAdapter mThanksUsersAdapter;

    private Account mAccount;

    private UUID mUserId;

    @NonNull
    private CompositeDisposable mDisposables = new CompositeDisposable();

    private FragmentCommunicator mFragmentCommunicator;

    @NonNull
    private final AsyncServerRepository mAsyncRepository = new AsyncServerRepository(AppExecutors.getInstance().networkIO(), AppExecutors.getInstance().mainThread());

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

        mAccount = args.getAccount();
        mUserId = args.getUserId();
    }

    @Override
    public void onActivityCreated (@Nullable final Bundle savedInstanceState) {
        Log.d(TAG, "onActivityCreated");
        super.onActivityCreated(savedInstanceState);
        try {
            mFragmentCommunicator = (FragmentCommunicator) requireActivity();
        } catch (ClassCastException e) {
            Log.e(TAG, requireActivity().getClass().getName() + " must implement " + FragmentCommunicator.class.getName());
            throw new ClassCastException(requireActivity().getClass().getName() + " must implement " + FragmentCommunicator.class.getName());
        }

        initThanksUserAdapter();
        initViewModel();
        setupBinding();

        refreshProfileData();
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
        mBinding = ProfileFragmentBinding.inflate(inflater, container, false);
    }

    private void initThanksUserAdapter () {
        Log.d(TAG, "initThanksUserAdapter");
        mThanksUsersAdapter = new ThanksUsersAdapter(this::onThanksUserClick);
    }

    private void initViewModel () {
        Log.d(TAG, "initViewModel");
        mViewModel = new ViewModelProvider(requireActivity()).get(ProfileViewModel.class);
        mViewModel.isHaveAccount().set(mAccount != null);
        mViewModel.isOwnProfile().set(mAccount != null && mAccount.name.equals(mUserId.toString()));
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
        if (mAccount != null) {
            AccountProvider.getAuthToken(requireActivity(), mAccount, this::downloadProfileData);
        } else {
            downloadProfileData(null);
        }
    }

    private void onThanksUserClick (@NonNull final UUID userId) {
        Log.d(TAG, "onThanksUserClick");
        final Intent i = new Intent(Intent.ACTION_VIEW);
        i.setData(Uri.parse(getString(R.string.url_profile, userId)));
        startActivity(i);
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
                    if (profileInfo != null &&
                            profileInfo.getPhoto() != null &&
                            mViewModel.isOwnProfile().get()) {
                        AccountManager.get(requireContext()).setUserData(mAccount, AccountGeneral.USER_DATA_PHOTO, profileInfo.getPhoto());
                        new ViewModelProvider(requireActivity()).get(UserViewModel.class).getOwnAccountPhotoUrl().setValue(profileInfo.getPhoto());
                    }
                },
                throwable -> {
                    mViewModel.getDownloadInProgress().set(false);
                    Toast.makeText(requireActivity(), throwable.getMessage(), Toast.LENGTH_LONG).show();
                });

        refreshThanksUsers();
    }

    private void refreshThanksUsers () {
        mViewModel.setThanksUsers(mAsyncRepository.getLiveDataPagedListFromDataSource(new ThanksUsersDataSource.ThanksUserDataSourceFactory(mUserId)));
        mViewModel.getThanksUsers().observe(requireActivity(), mThanksUsersAdapter::submitList);
    }

    @Override
    public void onShareClick () {
        final Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_SEND);
        sendIntent.putExtra(Intent.EXTRA_TEXT, getString(R.string.url_profile, mUserId.toString()));
        sendIntent.setType("text/plain");
        startActivity(Intent.createChooser(sendIntent, "Поделиться"));
    }

    @Override
    public void onTrustClick () {
        attemptToAddOperation(OperationType.TRUST);
    }

    @Override
    public void onMistrustClick () {
        attemptToAddOperation(OperationType.MISTRUST);
    }

    @Override
    public void onThanksClick () {
        attemptToAddOperation(OperationType.THANKS);
    }

    public void attemptToAddOperation (@NonNull final OperationType operationType) {
        Log.d(TAG, "onAddOperation");
        if (mAccount != null) {
            AccountProvider.getAuthToken(requireActivity(), mAccount, authToken -> {
                if (authToken != null) {
                    mAsyncRepository.setAuthToken(authToken);
                    new OperationToUserManager().
                            createOperationToUser(
                                    requireActivity(),
                                    UUID.fromString(mAccount.name),
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
        } else {
            AccountProvider.createAccount(
                    requireActivity(),
                    account -> {
                        if (account != null) {
                            mAccount = account;
                            mViewModel.isHaveAccount().set(true);
                            mViewModel.isOwnProfile().set(mAccount.name.equals(mUserId.toString()));
                            if (!mViewModel.isOwnProfile().get()) {
                                attemptToAddOperation(operationType);
                            } else {
                                Toast.makeText(requireContext(), R.string.info_msg_cant_add_operation_to_own_profile, Toast.LENGTH_LONG).show();
                            }
                        }
                    }
            );
        }
    }

    @Override
    public void onOperationsClick () {
        mFragmentCommunicator.toOperationsFromProfile();
    }

    @Override
    public void onWishesClick () {
        mFragmentCommunicator.toWishes();
    }

    @Override
    public void onAbilitiesClick () {
        mFragmentCommunicator.toAbilities();
    }

    @Override
    public void onKeysClick () {
        mFragmentCommunicator.toKeysFromProfile();
    }

    @Override
    public void onSocialGraphClick () {
        mFragmentCommunicator.toGraph();
        /*
        final Intent i = new Intent(Intent.ACTION_VIEW);
        i.setData(Uri.parse(String.format(getString(R.string.url_social_graph), mAccount.name, mUserId)));
        startActivity(i);
        */
    }

}
