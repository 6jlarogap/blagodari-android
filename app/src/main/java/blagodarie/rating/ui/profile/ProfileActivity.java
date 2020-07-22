package blagodarie.rating.ui.profile;

import android.Manifest;
import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AccountManagerCallback;
import android.accounts.AccountManagerFuture;
import android.accounts.AuthenticatorException;
import android.accounts.OperationCanceledException;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.core.view.GravityCompat;
import androidx.databinding.DataBindingUtil;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.google.zxing.qrcode.QRCodeWriter;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import blagodarie.rating.OnOperationListener;
import blagodarie.rating.R;
import blagodarie.rating.auth.AccountGeneral;
import blagodarie.rating.databinding.NavHeaderLayoutBinding;
import blagodarie.rating.databinding.ProfileActivityBinding;
import blagodarie.rating.databinding.ThanksUserItemBinding;
import blagodarie.rating.server.ServerApiResponse;
import blagodarie.rating.server.ServerConnector;
import blagodarie.rating.ui.AccountProvider;
import blagodarie.rating.ui.splash.SplashActivity;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;

public final class ProfileActivity
        extends AppCompatActivity
        implements ProfileEditor,
        OnOperationListener {

    private static final String TAG = ProfileActivity.class.getSimpleName();

    private CompositeDisposable mCompositeDisposable = new CompositeDisposable();

    private ProfileViewModel mViewModel;

    private ProfileActivityBinding mActivityBinding;

    private AccountManager mAccountManager;

    private Account mAccount;

    private String mProfileUserId;

    private DrawerLayout mDrawerLayout;

    private ThanksUserAdapter mThanksUserAdapter;

    private AppCompatImageView mIvMyAccount;

    @Override
    protected void onCreate (@Nullable final Bundle savedInstanceState) {
        Log.d(TAG, "onCreate");
        super.onCreate(savedInstanceState);

        mAccountManager = AccountManager.get(this);

        final Uri data = getIntent().getData();

        if (data != null) {
            mProfileUserId = data.getQueryParameter("id");
            if (mProfileUserId != null && !mProfileUserId.isEmpty()) {
                initViewModel();
                initBinding();
                setupToolbar();
                setupNavigationDrawer();
                initThanksUserAdapter();
                AccountProvider.getAccount(
                        this,
                        new AccountProvider.OnAccountSelectListener() {
                            @Override
                            public void onNoAccount () {
                                downloadProfileData(null);
                            }

                            @Override
                            public void onAccountSelected (@NonNull final Account account) {
                                mAccount = account;
                                mViewModel.getIsSelfProfile().set(mProfileUserId.equals(mAccountManager.getUserData(mAccount, AccountGeneral.USER_DATA_USER_ID)));
                                getAuthTokenAndDownloadProfileData();
                            }
                        }
                );
            } else {
                Toast.makeText(this, R.string.err_msg_missing_profile_user_id, Toast.LENGTH_LONG).show();
                finish();
            }
        } else {
            Toast.makeText(this, R.string.err_msg_missing_profile_user_id, Toast.LENGTH_LONG).show();
            finish();
        }
    }

    private void initViewModel () {
        Log.d(TAG, "initViewModel");
        mViewModel = new ViewModelProvider(this).get(ProfileViewModel.class);
    }

    private void initBinding (
    ) {
        Log.d(TAG, "initBinding");
        mActivityBinding = DataBindingUtil.setContentView(this, R.layout.profile_activity);
        mActivityBinding.setViewModel(mViewModel);
        mActivityBinding.srlRefreshProfileInfo.setOnRefreshListener(this::getAuthTokenAndDownloadProfileData);
        mActivityBinding.setProfileEditor(this);
        mActivityBinding.setOnOperationListener(this);

        final QRCodeWriter writer = new QRCodeWriter();
        final Map<EncodeHintType, Object> hints = new EnumMap<EncodeHintType, Object>(EncodeHintType.class);
        hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");
        hints.put(EncodeHintType.MARGIN, 0); /* default = 4 */
        try {
            final BitMatrix bitMatrix = writer.encode(getString(R.string.url_profile, mProfileUserId), BarcodeFormat.QR_CODE, 500, 500, hints);
            final int width = bitMatrix.getWidth();
            final int height = bitMatrix.getHeight();
            final Bitmap bmp = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
            for (int x = 0; x < width; x++) {
                for (int y = 0; y < height; y++) {
                    bmp.setPixel(x, y, bitMatrix.get(x, y) ? Color.BLACK : Color.TRANSPARENT);
                }
            }
            mActivityBinding.ivQRCode.setImageBitmap(bmp);
        } catch (WriterException e) {
            e.printStackTrace();
        }
    }

    private void initThanksUserAdapter () {
        mViewModel.getThanksUsers().observe(this, displayThanksUsers -> {
            if (mThanksUserAdapter == null) {
                mThanksUserAdapter = new ThanksUserAdapter(this::onThanksUserClick);
                mActivityBinding.rvThanksUsers.setLayoutManager(new GridLayoutManager(this, calcSpanCount()));
                mActivityBinding.rvThanksUsers.setAdapter(mThanksUserAdapter);
            }
            mThanksUserAdapter.setData(displayThanksUsers);
        });
    }

    private int calcSpanCount () {
        int layoutWidthDp = (int) (mActivityBinding.rvThanksUsers.getWidth() / getResources().getDisplayMetrics().density);
        int itemWidthDp = (int) ((getResources().getDimension(R.dimen.thanks_user_photo_width) + (getResources().getDimension(R.dimen.thanks_user_photo_margin) * 2)) / getResources().getDisplayMetrics().density);
        return layoutWidthDp / itemWidthDp;
    }

    private void onThanksUserClick (@NonNull final View view) {
        final ThanksUserItemBinding thanksUserItemBinding = DataBindingUtil.findBinding(view);
        if (thanksUserItemBinding != null) {
            final String userId = thanksUserItemBinding.getThanksUser().getUserUUID();
            final Intent i = new Intent(Intent.ACTION_VIEW);
            i.setData(Uri.parse(getString(R.string.url_profile, userId)));
            startActivity(i);
        }
    }

    private void setupToolbar () {
        Log.d(TAG, "setupToolbar");
        setSupportActionBar(findViewById(R.id.toolbar));
        final ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayShowTitleEnabled(false);
            actionBar.setHomeAsUpIndicator(R.drawable.ic_menu);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    @Override
    public boolean onCreateOptionsMenu (
            @NonNull final Menu menu
    ) {
        Log.d(TAG, "onCreateOptionsMenu");
        getMenuInflater().inflate(R.menu.profile_activity, menu);
        if (mAccount != null && !mViewModel.getIsSelfProfile().get()) {
            final MenuItem miMyAccount = menu.findItem(R.id.miMyAccount);
            miMyAccount.setVisible(true);
            mIvMyAccount = (AppCompatImageView) miMyAccount.getActionView().findViewById(R.id.ivAccountPhoto);
            mIvMyAccount.setOnClickListener(view -> {
                final Intent i = new Intent(Intent.ACTION_VIEW);
                i.setData(Uri.parse(getString(R.string.url_profile, mAccountManager.getUserData(mAccount, AccountGeneral.USER_DATA_USER_ID))));
                startActivity(i);
            });
        }
        return true;
    }

    private void setupNavigationDrawer () {
        Log.d(TAG, "setupNavigationDrawer");
        mDrawerLayout = findViewById(R.id.drawer_layout);

        final NavHeaderLayoutBinding binding = DataBindingUtil.inflate(getLayoutInflater(), R.layout.nav_header_layout, null, false);
        mActivityBinding.nvNavigation.addHeaderView(binding.getRoot());
        mActivityBinding.nvNavigation.getMenu().findItem(R.id.miLogout).setEnabled(mAccount != null);
        mActivityBinding.nvNavigation.setNavigationItemSelectedListener(menuItem -> {
            switch (menuItem.getItemId()) {
                case R.id.miLogout:
                    logout();
                    break;
                default:
                    break;
            }
            mDrawerLayout.closeDrawers();
            return true;
        });
    }

    private void logout () {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
            mAccountManager.removeAccount(
                    mAccount,
                    this,
                    accountManagerFuture -> {
                        startActivity(SplashActivity.createSelfIntent(this));
                        finish();
                    },
                    null);
        } else {
            mAccountManager.removeAccount(
                    mAccount,
                    accountManagerFuture -> {
                        startActivity(SplashActivity.createSelfIntent(this));
                        finish();
                    },
                    null);
        }
    }

    @Override
    public boolean onOptionsItemSelected (final MenuItem item) {
        Log.d(TAG, "onOptionsItemSelected item=" + item);
        switch (item.getItemId()) {
            case android.R.id.home:
                mDrawerLayout.openDrawer(GravityCompat.START);
                return true;
            case R.id.miMyAccount:
                final Intent i = new Intent(Intent.ACTION_VIEW);
                i.setData(Uri.parse(getString(R.string.url_profile, mAccountManager.getUserData(mAccount, AccountGeneral.USER_DATA_USER_ID))));
                startActivity(i);
                return true;
            case R.id.miQRCodeScan:
                tryScanQRCode();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void tryScanQRCode () {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                scanQRCode();
            } else {
                requestPermissions(new String[]{Manifest.permission.CAMERA}, 1);
            }
        }
    }

    private void scanQRCode () {
        new IntentIntegrator(this).
                setPrompt(getString(R.string.rqst_scan_qr_code)).
                setDesiredBarcodeFormats(IntentIntegrator.QR_CODE_TYPES).
                setOrientationLocked(true).
                initiateScan();
    }

    @Override
    protected void onActivityResult (int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case IntentIntegrator.REQUEST_CODE: {
                IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
                if (result.getContents() != null) {
                    final String url = result.getContents();
                    final Intent i = new Intent(Intent.ACTION_VIEW);
                    i.setData(Uri.parse(url));
                    if (i.resolveActivity(getPackageManager()) != null) {
                        startActivity(i);
                    } else {
                        Toast.makeText(this, R.string.err_msg_incorrect_link, Toast.LENGTH_LONG).show();
                    }
                }
                break;
            }
            default:
                break;
        }
    }

    @Override
    public void onRequestPermissionsResult (int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case 1:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    scanQRCode();
                }
                break;
        }
    }

    private void share () {
        final Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_SEND);
        sendIntent.putExtra(Intent.EXTRA_TEXT, getString(R.string.url_profile, mProfileUserId));
        sendIntent.setType("text/plain");
        startActivity(Intent.createChooser(sendIntent, "Поделиться"));
    }

    @Override
    protected void onDestroy () {
        Log.d(TAG, "onDestroy");
        super.onDestroy();
        mCompositeDisposable.dispose();
    }

    private void downloadProfileData (
            @Nullable final String authToken
    ) {
        Log.d(TAG, "downloadProfileData");
        mViewModel.getDownloadInProgress().set(true);
        mCompositeDisposable.add(
                Observable.
                        fromCallable(() -> {
                            if (authToken != null) {
                                return ServerConnector.sendAuthRequestAndGetResponse("getprofileinfo?uuid=" + mProfileUserId, authToken);
                            } else {
                                return ServerConnector.sendRequestAndGetResponse("getprofileinfo?uuid=" + mProfileUserId);
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
                                    Toast.makeText(this, throwable.getMessage(), Toast.LENGTH_LONG).show();
                                }
                        )
        );
    }

    private void updateProfileData (
            @NonNull final String authToken,
            @NonNull final String cardNumber
    ) {
        Log.d(TAG, "updateProfileData");

        final String content = String.format("{\"credit_card\":\"%s\"}", cardNumber);

        mCompositeDisposable.add(
                Observable.
                        fromCallable(() -> ServerConnector.sendAuthRequestAndGetResponse("updateprofileinfo", authToken, content)).
                        subscribeOn(Schedulers.io()).
                        observeOn(AndroidSchedulers.mainThread()).
                        subscribe(
                                serverApiResponse -> {
                                    Log.d(TAG, serverApiResponse.toString());
                                    onUpdateDataComplete(serverApiResponse);
                                },
                                throwable -> {
                                    Log.e(TAG, Log.getStackTraceString(throwable));
                                    Toast.makeText(this, throwable.getMessage(), Toast.LENGTH_LONG).show();
                                }
                        )
        );
    }

    private void addOperation (
            @NonNull final String authToken,
            final int operationTypeId
    ) {
        Log.d(TAG, "addOperation");

        final String content = String.format(Locale.ENGLISH, "{\"user_id_to\":\"%s\",\"operation_type_id\":%d,\"timestamp\":%d}", mProfileUserId, operationTypeId, System.currentTimeMillis());

        mCompositeDisposable.add(
                Observable.
                        fromCallable(() -> ServerConnector.sendAuthRequestAndGetResponse("addoperation", authToken, content)).
                        subscribeOn(Schedulers.io()).
                        observeOn(AndroidSchedulers.mainThread()).
                        subscribe(
                                serverApiResponse -> {
                                    Log.d(TAG, serverApiResponse.toString());
                                    onAddOperationComplete(serverApiResponse, operationTypeId);
                                },
                                throwable -> {
                                    Log.e(TAG, Log.getStackTraceString(throwable));
                                    Toast.makeText(this, throwable.getMessage(), Toast.LENGTH_LONG).show();
                                }
                        )
        );
    }

    private void onUpdateDataComplete (
            @NonNull final ServerApiResponse serverApiResponse
    ) {
        Log.d(TAG, "onUpdateDataComplete serverApiResponse=" + serverApiResponse);
        if (serverApiResponse.getCode() == 200) {
            mViewModel.getCardNumber().set(mActivityBinding.etCardNumber.getText().toString());
            Toast.makeText(this, R.string.info_msg_update_data_complete, Toast.LENGTH_LONG).show();
        } else {
            mViewModel.getCardNumber().notifyChange();
            Toast.makeText(this, R.string.err_msg_update_data_failed, Toast.LENGTH_LONG).show();
        }
    }

    private void onAddOperationComplete (
            @NonNull final ServerApiResponse serverApiResponse,
            final int operationTypeId
    ) {
        Log.d(TAG, "onUpdateDataComplete serverApiResponse=" + serverApiResponse);
        if (serverApiResponse.getCode() == 200) {
            switch (operationTypeId) {
                case 1: {
                    Toast.makeText(this, R.string.info_msg_add_thanks_complete, Toast.LENGTH_LONG).show();
                    break;
                }
                case 2: {
                    Toast.makeText(this, R.string.info_msg_trust_is_lost, Toast.LENGTH_LONG).show();
                    break;
                }
                case 3: {
                    Toast.makeText(this, R.string.info_msg_trust_restored, Toast.LENGTH_LONG).show();
                    break;
                }
            }
            getAuthTokenAndDownloadProfileData();
        } else {
            Toast.makeText(this, R.string.err_msg_add_thanks_failed, Toast.LENGTH_LONG).show();
        }
    }

    private void getAuthTokenAndDownloadProfileData () {
        Log.d(TAG, "getAuthTokenAndDownloadProfileData");
        AccountProvider.getAuthToken(
                this,
                mAccount,
                this::onGetAuthTokenForDownloadProfileDataComplete);
    }

    private void getAuthTokenAndUpdateProfileData (@NonNull final String cardNumber) {
        Log.d(TAG, "getAuthTokenAndUpdateProfileData");
        AccountProvider.getAuthToken(
                this,
                mAccount,
                future -> onGetAuthTokenForUpdateProfileDataComplete(future, cardNumber)
        );
    }

    private void getAuthTokenAndAddOperation (final int operationTypeId) {
        Log.d(TAG, "getAuthTokenAndAddOperation");
        AccountProvider.getAuthToken(
                this,
                mAccount,
                accountManagerFuture -> onGetAuthTokenForAddOperationComplete(accountManagerFuture, operationTypeId)
        );
    }

    private void onGetAuthTokenForDownloadProfileDataComplete (@NonNull final AccountManagerFuture<Bundle> future) {
        Log.d(TAG, "onGetAuthTokenForDownloadProfileDataComplete");
        try {
            final Bundle bundle = future.getResult();
            if (bundle != null) {
                final String authToken = bundle.getString(AccountManager.KEY_AUTHTOKEN);
                downloadProfileData(authToken);
            }
        } catch (AuthenticatorException | IOException | OperationCanceledException e) {
            e.printStackTrace();
        }
    }

    private void onGetAuthTokenForUpdateProfileDataComplete (
            @NonNull final AccountManagerFuture<Bundle> future,
            @NonNull final String cardNumber
    ) {
        Log.d(TAG, "onGetAuthTokenForUpdateProfileDataComplete");
        try {
            final Bundle bundle = future.getResult();
            if (bundle != null) {
                final String authToken = bundle.getString(AccountManager.KEY_AUTHTOKEN);
                if (authToken != null) {
                    updateProfileData(authToken, cardNumber);
                }
            }
        } catch (AuthenticatorException | IOException | OperationCanceledException e) {
            e.printStackTrace();
        }
    }

    private void onGetAuthTokenForAddOperationComplete (
            @NonNull final AccountManagerFuture<Bundle> future,
            final int operationTypeId
    ) {
        Log.d(TAG, "onGetAuthTokenForUpdateProfileDataComplete");
        try {
            final Bundle bundle = future.getResult();
            if (bundle != null) {
                final String authToken = bundle.getString(AccountManager.KEY_AUTHTOKEN);
                if (authToken != null) {
                    addOperation(authToken, operationTypeId);
                }
            }
        } catch (AuthenticatorException | IOException | OperationCanceledException e) {
            e.printStackTrace();
        }
    }

    private void extractDataFromServerApiResponse (ServerApiResponse serverApiResponse) {
        Log.d(TAG, "extractDataFromServerApiResponse");
        if (serverApiResponse.getCode() == 200) {
            if (serverApiResponse.getBody() != null) {
                final String responseBody = serverApiResponse.getBody();
                try {
                    final JSONObject userJSON = new JSONObject(responseBody);

                    final String photo = userJSON.getString("photo");
                    Picasso.get().load(photo).into(mActivityBinding.ivPhoto);

                    if (mAccount != null && !mViewModel.getIsSelfProfile().get()) {
                        if (mViewModel.getIsSelfProfile().get()) {
                            mAccountManager.setUserData(mAccount, AccountGeneral.USER_DATA_PHOTO, photo);
                            Picasso.get().load(photo).into(mIvMyAccount);
                        } else {
                            Picasso.get().load(mAccountManager.getUserData(mAccount, AccountGeneral.USER_DATA_PHOTO)).into(mIvMyAccount);
                        }
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
                    Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
                } catch (IllegalArgumentException e) {
                    Log.e(TAG, Log.getStackTraceString(e));
                    Toast.makeText(this, getString(blagodarie.rating.auth.R.string.err_msg_incorrect_user_id), Toast.LENGTH_LONG).show();
                }
            }

        }
    }

    @Override
    public void onEdit () {
        Log.d(TAG, "onEdit");
        mViewModel.setCurrentMode(ProfileViewModel.Mode.EDIT);
    }

    @Override
    public void onSave () {
        Log.d(TAG, "onSave");
        mViewModel.setCurrentMode(ProfileViewModel.Mode.VIEW);

        final String cardNumber = mActivityBinding.etCardNumber.getText().toString();
        if (cardNumber.isEmpty() || cardNumber.length() == 16) {
            getAuthTokenAndUpdateProfileData(cardNumber);
        } else {
            mViewModel.getCardNumber().notifyChange();
            Toast.makeText(this, getString(blagodarie.rating.auth.R.string.err_msg_incorrect_card_number), Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onCancel () {
        Log.d(TAG, "onCancel");
        mViewModel.getCardNumber().notifyChange();
        mViewModel.setCurrentMode(ProfileViewModel.Mode.VIEW);
    }

    @Override
    public void onThanks () {
        if (mAccount != null) {
            getAuthTokenAndAddOperation(1);
        } else {
            mAccountManager.addAccount(
                    getString(R.string.account_type),
                    getString(R.string.token_type),
                    null,
                    null,
                    this,
                    accountManagerFuture -> onAddAccountFinished(accountManagerFuture, 1),
                    null
            );
        }
    }

    @Override
    public void onTrustless () {
        if (mAccount != null) {
            getAuthTokenAndAddOperation(2);
        } else {
            mAccountManager.addAccount(
                    getString(R.string.account_type),
                    getString(R.string.token_type),
                    null,
                    null,
                    this,
                    accountManagerFuture -> onAddAccountFinished(accountManagerFuture, 2),
                    null
            );
        }
    }

    @Override
    public void onTrustlessCancel () {
        if (mAccount != null) {
            getAuthTokenAndAddOperation(3);
        } else {
            mAccountManager.addAccount(
                    getString(R.string.account_type),
                    getString(R.string.token_type),
                    null,
                    null,
                    this,
                    accountManagerFuture -> onAddAccountFinished(accountManagerFuture, 3),
                    null
            );
        }
    }

    public void onAddAccountFinished (final AccountManagerFuture<Bundle> result, final int operationTypeId) {
        Log.d(TAG, "onAddAccountFinish");
        try {
            final Bundle bundle = result.getResult();
            mAccount = new Account(
                    bundle.getString(AccountManager.KEY_ACCOUNT_NAME),
                    bundle.getString(AccountManager.KEY_ACCOUNT_TYPE)
            );
            getAuthTokenAndAddOperation(operationTypeId);
        } catch (OperationCanceledException e) {
            Log.e(TAG, Log.getStackTraceString(e));
            Toast.makeText(this, getString(R.string.err_msg_account_not_created), Toast.LENGTH_LONG).show();
            finish();
        } catch (AuthenticatorException e) {
            Log.e(TAG, Log.getStackTraceString(e));
            Toast.makeText(this, getString(R.string.err_msg_authentication_error), Toast.LENGTH_LONG).show();
            finish();
        } catch (IOException e) {
            Log.e(TAG, Log.getStackTraceString(e));
            finish();
        }
    }

    public void onCopyClick (@NonNull final View view) {
        final ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        final ClipData clip = ClipData.newPlainText(getText(R.string.txt_card_number), mActivityBinding.etCardNumber.getText().toString());
        if (clipboard != null) {
            clipboard.setPrimaryClip(clip);
            Toast.makeText(this, R.string.info_msg_copied_to_clipboard, Toast.LENGTH_SHORT).show();
        }
    }
}
