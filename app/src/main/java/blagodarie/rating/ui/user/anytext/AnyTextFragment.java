package blagodarie.rating.ui.user.anytext;

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
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

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
import blagodarie.rating.R;
import blagodarie.rating.operations.OperationToAnyTextManager;
import blagodarie.rating.databinding.AnyTextFragmentBinding;
import blagodarie.rating.databinding.ThanksUserItemBinding;
import blagodarie.rating.model.entities.AnyTextInfo;
import blagodarie.rating.model.entities.OperationType;
import blagodarie.rating.repository.AsyncServerRepository;
import blagodarie.rating.server.BadAuthorizationTokenException;
import blagodarie.rating.ui.AccountProvider;
import blagodarie.rating.ui.user.GridAutofitLayoutManager;
import blagodarie.rating.ui.user.ThanksUserAdapter;
import io.reactivex.disposables.CompositeDisposable;

public final class AnyTextFragment
        extends Fragment
        implements AnyTextUserActionListener {

    public interface FragmentCommunicator {
        void toOperationsFromAnyText (@NonNull final UUID anyTextId);
    }

    private static final String TAG = AnyTextFragment.class.getSimpleName();

    private AnyTextViewModel mViewModel;

    private AnyTextFragmentBinding mBinding;

    private ThanksUserAdapter mThanksUserAdapter;

    private Account mAccount;

    private String mAnyText;

    private UUID mAnyTextId;

    @NonNull
    private CompositeDisposable mDisposables = new CompositeDisposable();

    private FragmentCommunicator mFragmentCommunicator;

    @NonNull
    private final AsyncServerRepository mRepository = new AsyncServerRepository(AppExecutors.getInstance().networkIO(), AppExecutors.getInstance().mainThread());

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

        mAccount = args.getAccount();
        mAnyText = args.getAnyText();
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

        refreshAnyTextData();
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
        mBinding = AnyTextFragmentBinding.inflate(inflater, container, false);
    }

    private void initThanksUserAdapter () {
        Log.d(TAG, "initThanksUserAdapter");
        mThanksUserAdapter = new ThanksUserAdapter(this::onThanksUserClick);
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

    private void initViewModel () {
        Log.d(TAG, "initViewModel");
        mViewModel = new ViewModelProvider(requireActivity()).get(AnyTextViewModel.class);
        mViewModel.getAnyText().set(mAnyText);
        mViewModel.isHaveAccount().set(mAccount != null);
        mViewModel.getThanksUsers().observe(requireActivity(), mThanksUserAdapter::setData);
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
        if (mAccount != null) {
            AccountProvider.getAuthToken(requireActivity(), mAccount, this::downloadAnyTextData);
        } else {
            downloadAnyTextData(null);
        }
    }

    private void downloadAnyTextData (
            @Nullable final String authToken
    ) {
        Log.d(TAG, "downloadAnyTextData");
        mViewModel.getDownloadInProgress().set(true);

        mRepository.setAuthToken(authToken);
        mRepository.getAnyTextInfo(
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
                    mRepository.setAuthToken(authToken);
                    new OperationToAnyTextManager().
                            createOperationToAnyText(
                                    requireActivity(),
                                    UUID.fromString(mAccount.name),
                                    mAnyTextId,
                                    operationType,
                                    mAnyText,
                                    mRepository,
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
                            attemptToAddOperation(operationType);
                        }
                    }
            );
        }
    }

    @Override
    public void onOperationsClick () {
        mFragmentCommunicator.toOperationsFromAnyText(mAnyTextId);
    }

}
