package blagodarie.rating.ui.anytext;

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
import blagodarie.rating.commands.CreateOperationToAnyTextCommand;
import blagodarie.rating.databinding.AnyTextFragmentBinding;
import blagodarie.rating.model.IAnyTextInfo;
import blagodarie.rating.model.entities.AnyTextInfo;
import blagodarie.rating.model.entities.OperationType;
import blagodarie.rating.repository.AsyncServerRepository;
import blagodarie.rating.server.BadAuthorizationTokenException;
import blagodarie.rating.ui.AccountProvider;
import blagodarie.rating.ui.AccountSource;
import blagodarie.rating.ui.GridAutofitLayoutManager;
import blagodarie.rating.ui.profile.ThanksUsersAdapter;

public final class AnyTextFragment
        extends Fragment
        implements AnyTextUserActionListener {

    private static final String TAG = AnyTextFragment.class.getSimpleName();

    private AnyTextViewModel mViewModel;

    private AnyTextFragmentBinding mBinding;

    private ThanksUsersAdapter mThanksUserAdapter;

    private String mAnyText;

    private UUID mAnyTextId;

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

        final AnyTextFragmentArgs args = AnyTextFragmentArgs.fromBundle(requireArguments());

        mAnyText = args.getAnyText();
    }

    @Override
    public void onActivityCreated (@Nullable final Bundle savedInstanceState) {
        Log.d(TAG, "onActivityCreated");
        super.onActivityCreated(savedInstanceState);

        initThanksUserAdapter();
        initViewModel();
        setupBinding();

        refreshAnyTextData();
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
        mBinding = AnyTextFragmentBinding.inflate(inflater, container, false);
    }

    private void initThanksUserAdapter () {
        Log.d(TAG, "initThanksUserAdapter");
        mThanksUserAdapter = new ThanksUsersAdapter(this::onThanksUserClick);
    }


    private void onThanksUserClick (@NonNull final UUID userId) {
        Log.d(TAG, "onThanksUserClick");
        NavHostFragment.findNavController(this).navigate(Uri.parse(getString(R.string.url_profile, userId)));
    }

    private void initViewModel () {
        Log.d(TAG, "initViewModel");
        mViewModel = new ViewModelProvider(requireActivity()).get(AnyTextViewModel.class);
        mViewModel.getAnyText().set(mAnyText);
        mViewModel.getQrCode().set(createQrCodeBitmap());
    }

    private void setupBinding () {
        Log.d(TAG, "setupBinding");
        mBinding.setUserActionListener(this);
        mBinding.srlRefreshProfileInfo.setOnRefreshListener(this::refreshAnyTextData);
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
                    mAnyText,
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

    public final void refreshAnyTextData () {
        Log.d(TAG, "refreshAnyTextData");
        AccountSource.INSTANCE.getAccount(
                requireContext(),
                account -> {
                    if (account != null) {
                        AccountProvider.getAuthToken(requireActivity(), account, this::downloadAnyTextData);
                    } else {
                        downloadAnyTextData(null);
                    }
                });
    }

    private void downloadAnyTextData (
            @Nullable final String authToken
    ) {
        Log.d(TAG, "downloadAnyTextData");
        mViewModel.getDownloadInProgress().set(true);

        mAsyncRepository.setAuthToken(authToken);
        mAsyncRepository.getAnyTextInfo(
                mAnyText,
                anyTextInfo -> {
                    mViewModel.getDownloadInProgress().set(false);
                    if (anyTextInfo != null) {
                        mViewModel.getAnyTextInfo().set(anyTextInfo);
                        mAnyTextId = anyTextInfo.getAnyTextId();
                    } else {
                        mViewModel.getAnyTextInfo().set(AnyTextInfo.EMPTY_ANY_TEXT);
                    }
                },
                throwable -> {
                    mViewModel.getDownloadInProgress().set(false);
                    Toast.makeText(requireActivity(), throwable.getMessage(), Toast.LENGTH_LONG).show();
                });
        refreshThanksUsers();
    }

    private void refreshThanksUsers () {
        Log.d(TAG, "refreshThanksUsers");
        mViewModel.setThanksUsers(mAsyncRepository.getLiveDataPagedListFromDataSource(new ThanksUsersForAnyTextDataSource.ThanksUserForAnyTextDataSourceFactory(mAnyText)));
        mViewModel.getThanksUsers().observe(requireActivity(), mThanksUserAdapter::submitList);
    }

    @Override
    public void onShareClick () {
        final Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_SEND);
        sendIntent.putExtra(Intent.EXTRA_TEXT, mAnyText);
        sendIntent.setType("text/plain");
        startActivity(Intent.createChooser(sendIntent, "Поделиться"));
    }

    @Override
    public void onTrustClick () {
        Log.d(TAG, "onTrustClick");
        final IAnyTextInfo anyTextInfo = mViewModel.getAnyTextInfo().get();
        if (anyTextInfo != null) {
            final Boolean isTrust = anyTextInfo.isTrust();
            attemptToAddOperation(isTrust != null && isTrust ? OperationType.NULLIFY_TRUST : OperationType.TRUST);
        }
    }

    @Override
    public void onMistrustClick () {
        Log.d(TAG, "onMistrustClick");
        final IAnyTextInfo anyTextInfo = mViewModel.getAnyTextInfo().get();
        if (anyTextInfo != null) {
            final Boolean isTrust = anyTextInfo.isTrust();
            attemptToAddOperation(isTrust != null && !isTrust ? OperationType.NULLIFY_TRUST : OperationType.MISTRUST);
        }
    }

    @Override
    public void onThanksClick () {
        attemptToAddOperation(OperationType.THANKS);
    }

    public void attemptToAddOperation (@NonNull final OperationType operationType) {
        Log.d(TAG, "onAddOperation");
        AccountSource.INSTANCE.requireAccount(
                requireActivity(),
                account -> {
                    if (account != null) {
                        AccountProvider.getAuthToken(requireActivity(), account, authToken -> {
                            if (authToken != null) {
                                mAsyncRepository.setAuthToken(authToken);
                                new CreateOperationToAnyTextCommand(
                                        requireActivity(),
                                        UUID.fromString(account.name),
                                        mAnyTextId,
                                        operationType,
                                        mAnyText,
                                        mAsyncRepository,
                                        () -> {
                                            Toast.makeText(requireContext(), R.string.info_msg_saved, Toast.LENGTH_LONG).show();
                                            refreshAnyTextData();
                                        },
                                        throwable -> {
                                            Log.e(TAG, Log.getStackTraceString(throwable));
                                            if (throwable instanceof BadAuthorizationTokenException) {
                                                AccountManager.get(requireContext()).invalidateAuthToken(getString(R.string.account_type), authToken);
                                                attemptToAddOperation(operationType);
                                            } else {
                                                Toast.makeText(requireContext(), R.string.err_msg_not_saved, Toast.LENGTH_LONG).show();
                                            }
                                        }
                                ).execute();
                            } else {
                                Toast.makeText(requireContext(), R.string.info_msg_need_log_in, Toast.LENGTH_LONG).show();
                            }
                        });
                    }
                }
        );
    }

    @Override
    public void onOperationsClick () {
        final NavDirections action = MainActivityDirections.actionGlobalOperationsFragment().setAnyTextId(mAnyTextId);
        NavHostFragment.findNavController(this).navigate(action);
    }

}
