package blagodarie.rating.ui.user.profile;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.databinding.BindingAdapter;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.squareup.picasso.Picasso;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

import blagodarie.rating.OperationType;
import blagodarie.rating.R;
import blagodarie.rating.auth.AccountGeneral;
import blagodarie.rating.databinding.EnterOperationCommentDialogBinding;
import blagodarie.rating.databinding.ProfileFragmentBinding;
import blagodarie.rating.databinding.ThanksUserItemBinding;
import blagodarie.rating.server.ServerApiResponse;
import blagodarie.rating.server.ServerConnector;
import blagodarie.rating.ui.AccountProvider;
import blagodarie.rating.ui.user.UserViewModel;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;

public final class ProfileFragment
        extends Fragment
        implements ProfileUserActionListener {

    public interface FragmentCommunicator {
        void toOperations ();

        void toWishes ();

        void toAbilities ();
    }

    private static class GridAutofitLayoutManager
            extends GridLayoutManager {
        private int columnWidth;
        private boolean isColumnWidthChanged = true;
        private int lastWidth;
        private int lastHeight;

        public GridAutofitLayoutManager (@NonNull final Context context, final int columnWidth) {
            /* Initially set spanCount to 1, will be changed automatically later. */
            super(context, 1);
            setColumnWidth(checkedColumnWidth(context, columnWidth));
        }

        public GridAutofitLayoutManager (
                @NonNull final Context context,
                final int columnWidth,
                final int orientation,
                final boolean reverseLayout) {

            /* Initially set spanCount to 1, will be changed automatically later. */
            super(context, 1, orientation, reverseLayout);
            setColumnWidth(checkedColumnWidth(context, columnWidth));
        }

        private int checkedColumnWidth (@NonNull final Context context, int columnWidth) {
            if (columnWidth <= 0) {
            /* Set default columnWidth value (48dp here). It is better to move this constant
            to static constant on top, but we need context to convert it to dp, so can't really
            do so. */
                columnWidth = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 48,
                        context.getResources().getDisplayMetrics());
            }
            return columnWidth;
        }

        public void setColumnWidth (final int newColumnWidth) {
            if (newColumnWidth > 0 && newColumnWidth != columnWidth) {
                columnWidth = newColumnWidth;
                isColumnWidthChanged = true;
            }
        }

        @Override
        public void onLayoutChildren (@NonNull final RecyclerView.Recycler recycler, @NonNull final RecyclerView.State state) {
            final int width = getWidth();
            final int height = getHeight();
            if (columnWidth > 0 && width > 0 && height > 0 && (isColumnWidthChanged || lastWidth != width || lastHeight != height)) {
                final int totalSpace;
                if (getOrientation() == VERTICAL) {
                    totalSpace = width - getPaddingRight() - getPaddingLeft();
                } else {
                    totalSpace = height - getPaddingTop() - getPaddingBottom();
                }
                final int spanCount = Math.max(1, totalSpace / columnWidth);
                setSpanCount(spanCount);
                isColumnWidthChanged = false;
            }
            lastWidth = width;
            lastHeight = height;
            super.onLayoutChildren(recycler, state);
        }
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
            mFragmentCommunicator = (FragmentCommunicator) getActivity();
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
        mViewModel.isProfile().set(true);
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
            final String userId = thanksUserItemBinding.getThanksUser().getUserUUID();
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
        mDisposables.add(
                Observable.
                        fromCallable(() -> {
                            if (authToken != null) {
                                return ServerConnector.sendAuthRequestAndGetResponse("getprofileinfo?uuid=" + mUserId.toString(), authToken);
                            } else {
                                return ServerConnector.sendRequestAndGetResponse("getprofileinfo?uuid=" + mUserId.toString());
                            }
                        }).
                        subscribeOn(Schedulers.io()).
                        observeOn(AndroidSchedulers.mainThread()).
                        subscribe(
                                serverApiResponse -> {
                                    mViewModel.getDownloadInProgress().set(false);
                                    extractDataFromServerApiResponse(serverApiResponse);
                                },
                                throwable -> {
                                    mViewModel.getDownloadInProgress().set(false);
                                    Toast.makeText(requireContext(), throwable.getMessage(), Toast.LENGTH_LONG).show();
                                }
                        )
        );
    }

    private void extractDataFromServerApiResponse (ServerApiResponse serverApiResponse) {
        Log.d(TAG, "extractDataFromServerApiResponse");
        if (serverApiResponse.getCode() == 200) {
            if (serverApiResponse.getBody() != null) {
                final String responseBody = serverApiResponse.getBody();
                try {
                    final JSONObject userJSON = new JSONObject(responseBody);

                    final String photo = userJSON.getString("photo");
                    mViewModel.getPhoto().set(photo);

                    if (mViewModel.isOwnProfile().get()) {
                        AccountManager.get(requireContext()).setUserData(mAccount, AccountGeneral.USER_DATA_PHOTO, photo);
                        new ViewModelProvider(requireActivity()).get(UserViewModel.class).getOwnAccountPhotoUrl().setValue(photo);
                    }

                    final String lastName = userJSON.getString("last_name");
                    mViewModel.getLastName().set(lastName);

                    final String first_name = userJSON.getString("first_name");
                    mViewModel.getFirstName().set(first_name);

                    final String middleName = userJSON.getString("middle_name");
                    mViewModel.getMiddleName().set(middleName);

                    try {
                        String cardNumber = userJSON.getString("credit_card");
                        if (cardNumber.equals("null")) {
                            cardNumber = "";
                        }
                        mViewModel.getCardNumber().set(cardNumber);
                    } catch (JSONException e) {
                        mViewModel.getCardNumber().set("");
                    }

                    final int fame = userJSON.getInt("fame");
                    mViewModel.getFame().set(fame);

                    final int sumThanksCount = userJSON.getInt("sum_thanks_count");
                    mViewModel.getSumThanksCount().set(sumThanksCount);

                    final int trustlessCount = userJSON.getInt("trustless_count");
                    mViewModel.getTrustlessCount().set(trustlessCount);

                    try {
                        final int thanksCount = userJSON.getInt("thanks_count");
                        mViewModel.getThanksCount().set(thanksCount);
                    } catch (JSONException e) {
                        mViewModel.getThanksCount().set(null);
                    }

                    try {
                        final boolean isTrust = userJSON.getBoolean("is_trust");
                        mViewModel.getIsTrust().set(isTrust);
                    } catch (JSONException e) {
                        mViewModel.getIsTrust().set(null);
                    }

                    final List<DisplayThanksUser> thanksUsers = new ArrayList<>();
                    final JSONArray thanksUsersJSONArray = userJSON.getJSONArray("thanks_users");
                    for (int i = 0; i < thanksUsersJSONArray.length(); i++) {
                        final JSONObject thanksUserJSONObject = thanksUsersJSONArray.getJSONObject(i);
                        final String thanksUserPhoto = thanksUserJSONObject.getString("photo");
                        final String thanksUserUUID = thanksUserJSONObject.getString("user_uuid");
                        thanksUsers.add(new DisplayThanksUser(thanksUserPhoto, thanksUserUUID));
                    }
                    mViewModel.getThanksUsers().setValue(thanksUsers);

                } catch (JSONException e) {
                    Log.e(TAG, Log.getStackTraceString(e));
                    Toast.makeText(requireContext(), e.getMessage(), Toast.LENGTH_LONG).show();
                } catch (IllegalArgumentException e) {
                    Log.e(TAG, Log.getStackTraceString(e));
                    Toast.makeText(requireContext(), getString(blagodarie.rating.auth.R.string.err_msg_incorrect_user_id), Toast.LENGTH_LONG).show();
                }
            }

        }
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
        addOperationComment(operationType);
    }

    @Override
    public void onCopyCardNumber () {
        Log.d(TAG, "onCopyCardNumber");
        final ClipboardManager clipboard = (ClipboardManager) requireContext().getSystemService(Context.CLIPBOARD_SERVICE);
        final ClipData clip = ClipData.newPlainText(getText(R.string.txt_card_number), mBinding.etCardNumber.getText().toString());
        if (clipboard != null) {
            clipboard.setPrimaryClip(clip);
            Toast.makeText(requireContext(), R.string.info_msg_copied_to_clipboard, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onEditCardNumber () {
        Log.d(TAG, "onEditCardNumber");
        mViewModel.setCurrentMode(ProfileViewModel.Mode.EDIT);
    }

    @Override
    public void onSaveCardNumber () {
        Log.d(TAG, "onSaveCardNumber");
        mViewModel.setCurrentMode(ProfileViewModel.Mode.VIEW);

        final String cardNumber = mBinding.etCardNumber.getText().toString();
        if (cardNumber.isEmpty() || cardNumber.length() == 16) {
            AccountProvider.getAuthToken(
                    requireActivity(),
                    mAccount,
                    authToken -> {
                        if (authToken != null) {
                            updateCardNumber(authToken, cardNumber);
                        }
                    });
        } else {
            mViewModel.getCardNumber().notifyChange();
            Toast.makeText(requireContext(), getString(blagodarie.rating.auth.R.string.err_msg_incorrect_card_number), Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onCancelEditCardNumber () {
        Log.d(TAG, "onCancelEditCardNumber");
        mViewModel.getCardNumber().notifyChange();
        mViewModel.setCurrentMode(ProfileViewModel.Mode.VIEW);
    }

    @Override
    public void onOperations () {
        mFragmentCommunicator.toOperations();
    }

    @Override
    public void onWishes () {
        mFragmentCommunicator.toWishes();
    }

    @Override
    public void onAbilities () {
        mFragmentCommunicator.toAbilities();
    }

    private void updateCardNumber (
            @NonNull final String authToken,
            @NonNull final String cardNumber
    ) {
        Log.d(TAG, "updateProfileData");

        final String content = String.format("{\"credit_card\":\"%s\"}", cardNumber);

        mDisposables.add(
                Observable.
                        fromCallable(() -> ServerConnector.sendAuthRequestAndGetResponse("updateprofileinfo", authToken, content)).
                        subscribeOn(Schedulers.io()).
                        observeOn(AndroidSchedulers.mainThread()).
                        subscribe(
                                serverApiResponse -> {
                                    Log.d(TAG, serverApiResponse.toString());
                                    onUpdateCardNumberComplete(serverApiResponse);
                                },
                                throwable -> {
                                    Log.e(TAG, Log.getStackTraceString(throwable));
                                    Toast.makeText(requireContext(), throwable.getMessage(), Toast.LENGTH_LONG).show();
                                }
                        )
        );
    }

    private void onUpdateCardNumberComplete (
            @NonNull final ServerApiResponse serverApiResponse
    ) {
        Log.d(TAG, "onUpdateCardNumberComplete serverApiResponse=" + serverApiResponse);
        if (serverApiResponse.getCode() == 200) {
            mViewModel.getCardNumber().set(mBinding.etCardNumber.getText().toString());
            Toast.makeText(requireContext(), R.string.info_msg_update_data_complete, Toast.LENGTH_LONG).show();
        } else {
            mViewModel.getCardNumber().notifyChange();
            Toast.makeText(requireContext(), R.string.err_msg_update_data_failed, Toast.LENGTH_LONG).show();
        }
    }

    private void addOperationComment (@NonNull final OperationType operationType) {
        Log.d(TAG, "addOperationComment");
        InputMethodManager imm = (InputMethodManager) requireContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        final EnterOperationCommentDialogBinding binding = DataBindingUtil.inflate(LayoutInflater.from(requireContext()), R.layout.enter_operation_comment_dialog, null, false);
        new AlertDialog.
                Builder(requireContext()).
                setCancelable(false).
                setTitle(R.string.txt_comment).
                setView(binding.getRoot()).
                setNegativeButton(android.R.string.cancel, (dialogInterface, i) -> imm.hideSoftInputFromWindow(binding.etOperationComment.getWindowToken(), 0)).
                setPositiveButton(android.R.string.ok,
                        (dialogInterface, i) -> {
                            imm.hideSoftInputFromWindow(binding.etOperationComment.getWindowToken(), 0);
                            final String operationComment = binding.etOperationComment.getText().toString();
                            if (mAccount != null) {
                                AccountProvider.getAuthToken(requireActivity(), mAccount, authToken -> {
                                    if (authToken != null) {
                                        addOperation(authToken, operationType, operationComment);
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
                                                    AccountProvider.getAuthToken(requireActivity(), account, authToken -> {
                                                        if (authToken != null) {
                                                            addOperation(authToken, operationType, operationComment);
                                                        }
                                                    });
                                                }
                                            }
                                        }
                                );
                            }
                        }).
                create().
                show();
        binding.etOperationComment.requestFocus();
        imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY);
    }

    private void addOperation (
            @NonNull final String authToken,
            @NonNull final OperationType operationType,
            final String operationComment
    ) {
        Log.d(TAG, "addOperation");

        final String content = String.format(Locale.ENGLISH, "{\"user_id_to\":\"%s\",\"operation_type_id\":%d,\"timestamp\":%d,\"comment\":\"%s\"}", mUserId.toString(), operationType.getId(), System.currentTimeMillis(), operationComment);

        mDisposables.add(
                Observable.
                        fromCallable(() -> ServerConnector.sendAuthRequestAndGetResponse("addoperation", authToken, content)).
                        subscribeOn(Schedulers.io()).
                        observeOn(AndroidSchedulers.mainThread()).
                        subscribe(
                                serverApiResponse -> {
                                    Log.d(TAG, serverApiResponse.toString());
                                    onAddOperationComplete(serverApiResponse, operationType);
                                },
                                throwable -> {
                                    Log.e(TAG, Log.getStackTraceString(throwable));
                                    Toast.makeText(requireContext(), throwable.getMessage(), Toast.LENGTH_LONG).show();
                                }
                        )
        );
    }

    private void onAddOperationComplete (
            @NonNull final ServerApiResponse serverApiResponse,
            @NonNull final OperationType operationType
    ) {
        Log.d(TAG, "onAddOperationComplete serverApiResponse=" + serverApiResponse);
        if (serverApiResponse.getCode() == 200) {
            switch (operationType) {
                case THANKS: {
                    Toast.makeText(requireContext(), R.string.info_msg_add_thanks_complete, Toast.LENGTH_LONG).show();
                    break;
                }
                case MISSTRUST: {
                    Toast.makeText(requireContext(), R.string.info_msg_trust_is_lost, Toast.LENGTH_LONG).show();
                    break;
                }
                case MISSTRUST_CANCEL: {
                    Toast.makeText(requireContext(), R.string.info_msg_trust_restored, Toast.LENGTH_LONG).show();
                    break;
                }
            }
            refreshProfileData();
        } else {
            Toast.makeText(requireContext(), R.string.err_msg_add_thanks_failed, Toast.LENGTH_LONG).show();
        }
    }
}
