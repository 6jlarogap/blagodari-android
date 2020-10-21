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
import androidx.databinding.DataBindingUtil;
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

import blagodarie.rating.OperationToUserManager;
import blagodarie.rating.OperationType;
import blagodarie.rating.R;
import blagodarie.rating.auth.AccountGeneral;
import blagodarie.rating.databinding.ProfileFragmentBinding;
import blagodarie.rating.databinding.ThanksUserItemBinding;
import blagodarie.rating.server.GetProfileInfoRequest;
import blagodarie.rating.server.GetProfileInfoResponse;
import blagodarie.rating.server.ServerApiClient;
import blagodarie.rating.ui.AccountProvider;
import blagodarie.rating.ui.user.GridAutofitLayoutManager;
import blagodarie.rating.ui.user.ThanksUserAdapter;
import blagodarie.rating.ui.user.UserViewModel;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;

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

    private ThanksUserAdapter mThanksUserAdapter;

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
        mThanksUserAdapter = new ThanksUserAdapter(this::onThanksUserClick);
    }

    private void initViewModel () {
        Log.d(TAG, "initViewModel");
        mViewModel = new ViewModelProvider(requireActivity()).get(ProfileViewModel.class);
        mViewModel.isHaveAccount().set(mAccount != null);
        mViewModel.isOwnProfile().set(mAccount != null && mAccount.name.equals(mUserId.toString()));
        mViewModel.getThanksUsers().observe(requireActivity(), mThanksUserAdapter::setData);
        mViewModel.getQrCode().set(createQrCodeBitmap());
    }

    private void setupBinding () {
        Log.d(TAG, "setupBinding");
        mBinding.setUserActionListener(this);
        mBinding.srlRefreshProfileInfo.setOnRefreshListener(this::refreshProfileData);
        mBinding.rvThanksUsers.setLayoutManager(new GridAutofitLayoutManager(requireContext(), (int) ((getResources().getDimension(R.dimen.thanks_user_photo_width) + (getResources().getDimension(R.dimen.thanks_user_photo_margin) * 2)))));
        mBinding.rvThanksUsers.setAdapter(mThanksUserAdapter);
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

    private void onThanksUserClick (@NonNull final View view) {
        Log.d(TAG, "onThanksUserClick");
        final ThanksUserItemBinding thanksUserItemBinding = DataBindingUtil.findBinding(view);
        if (thanksUserItemBinding != null) {
            final String userId = thanksUserItemBinding.getThanksUser().getUserId().toString();
            final Intent i = new Intent(Intent.ACTION_VIEW);
            i.setData(Uri.parse(getString(R.string.url_profile, userId)));
            startActivity(i);
        }
    }

    private void downloadProfileData (
            @Nullable final String authToken
    ) {
        Log.d(TAG, "downloadProfileData");
        mViewModel.getDownloadInProgress().set(true);

        final ServerApiClient apiClient = new ServerApiClient();
        apiClient.setAuthToken(authToken);
        final GetProfileInfoRequest getProfileInfoRequest = new GetProfileInfoRequest(mUserId.toString());
        mDisposables.add(
                Observable.
                        fromCallable(() -> apiClient.execute(getProfileInfoRequest)).
                        subscribeOn(Schedulers.io()).
                        observeOn(AndroidSchedulers.mainThread()).
                        subscribe(
                                getProfileInfoResponse -> {
                                    mViewModel.getDownloadInProgress().set(false);
                                    handleGetProfileInfoResponse(getProfileInfoResponse);
                                },
                                throwable -> {
                                    mViewModel.getDownloadInProgress().set(false);
                                    Toast.makeText(requireActivity(), throwable.getMessage(), Toast.LENGTH_LONG).show();
                                }
                        )
        );
    }

    private void handleGetProfileInfoResponse (
            @NonNull final GetProfileInfoResponse getProfileInfoResponse
    ) {
        Log.d(TAG, "handleGetProfileInfoResponse");
        mViewModel.getPhoto().set(getProfileInfoResponse.getPhoto());
        if (mViewModel.isOwnProfile().get()) {
            AccountManager.get(requireContext()).setUserData(mAccount, AccountGeneral.USER_DATA_PHOTO, getProfileInfoResponse.getPhoto());
            new ViewModelProvider(requireActivity()).get(UserViewModel.class).getOwnAccountPhotoUrl().setValue(getProfileInfoResponse.getPhoto());
        }
        mViewModel.getFirstName().set(getProfileInfoResponse.getFirstName());
        mViewModel.getMiddleName().set(getProfileInfoResponse.getMiddleName());
        mViewModel.getLastName().set(getProfileInfoResponse.getLastName());
        mViewModel.getCardNumber().set(getProfileInfoResponse.getCardNumber());
        mViewModel.getFame().set(getProfileInfoResponse.getFame());
        mViewModel.getSumThanksCount().set(getProfileInfoResponse.getSumThanksCount());
        mViewModel.getTrustCount().set(getProfileInfoResponse.getTrustCount());
        mViewModel.getMistrustCount().set(getProfileInfoResponse.getMistrustCount());
        mViewModel.getThanksCount().set((getProfileInfoResponse.getThanksCount() != null ? getProfileInfoResponse.getThanksCount() : 0));
        mViewModel.getIsTrust().set(getProfileInfoResponse.getIsTrust());
        mViewModel.getThanksUsers().setValue(getProfileInfoResponse.getThanksUsers());
    }

    @BindingAdapter({"imageUrl"})
    public static void loadImage (ImageView view, String url) {
        if (url != null && !url.isEmpty()) {
            Picasso.get().load(url).into(view);
        }
    }

    @BindingAdapter({"imageBitmap"})
    public static void loadImage (ImageView view, Bitmap bitmap) {
        view.setImageBitmap(bitmap);
    }

    @Override
    public void onShareProfile () {
        final Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_SEND);
        sendIntent.putExtra(Intent.EXTRA_TEXT, getString(R.string.url_profile, mUserId.toString()));
        sendIntent.setType("text/plain");
        startActivity(Intent.createChooser(sendIntent, "Поделиться"));
    }

    @Override
    public void onAddOperation (@NonNull final OperationType operationType) {
        Log.d(TAG, "onAddOperation");
        if (mAccount != null) {
            new OperationToUserManager().
                    createOperationToUser(
                            requireActivity(),
                            mDisposables,
                            mAccount,
                            mUserId,
                            operationType,
                            this::refreshProfileData
                    );
        } else {
            AccountProvider.createAccount(
                    requireActivity(),
                    account -> {
                        if (account != null) {
                            mAccount = account;
                            mViewModel.isHaveAccount().set(true);
                            mViewModel.isOwnProfile().set(mAccount.name.equals(mUserId.toString()));
                            if (!mViewModel.isOwnProfile().get()) {
                                new OperationToUserManager().
                                        createOperationToUser(
                                                requireActivity(),
                                                mDisposables,
                                                mAccount,
                                                mUserId,
                                                operationType,
                                                this::refreshProfileData
                                        );
                            }
                        }
                    }
            );
        }
    }

    @Override
    public void onOperations () {
        mFragmentCommunicator.toOperationsFromProfile();
    }

    @Override
    public void onWishes () {
        mFragmentCommunicator.toWishes();
    }

    @Override
    public void onAbilities () {
        mFragmentCommunicator.toAbilities();
    }

    @Override
    public void onKeys () {
        mFragmentCommunicator.toKeysFromProfile();
    }

    @Override
    public void onSocialGraph () {
        mFragmentCommunicator.toGraph();
        /*
        final Intent i = new Intent(Intent.ACTION_VIEW);
        i.setData(Uri.parse(String.format(getString(R.string.url_social_graph), mAccount.name, mUserId)));
        startActivity(i);
        */
    }

}
