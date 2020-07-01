package blagodarie.rating.ui.profile;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AccountManagerFuture;
import android.accounts.AuthenticatorException;
import android.accounts.OperationCanceledException;
import android.app.AlertDialog;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.databinding.DataBindingUtil;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.lifecycle.ViewModelProvider;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Arrays;

import blagodarie.rating.R;
import blagodarie.rating.RatingApp;
import blagodarie.rating.auth.AccountGeneral;
import blagodarie.rating.databinding.ProfileActivityBinding;
import blagodarie.rating.server.ServerApiResponse;
import blagodarie.rating.server.ServerConnector;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;

public final class ProfileActivity
        extends AppCompatActivity
        implements ProfileEditor {

    private static final String TAG = ProfileActivity.class.getSimpleName();

    private CompositeDisposable mCompositeDisposable = new CompositeDisposable();

    private ProfileViewModel mViewModel;

    private ProfileActivityBinding mActivityBinding;

    private AccountManager mAccountManager;

    private Account mAccount;

    private String mProfileUserId;

    private DrawerLayout mDrawerLayout;

    @Override
    protected void onCreate (@Nullable final Bundle savedInstanceState) {
        Log.d(TAG, "onCreate");
        super.onCreate(savedInstanceState);

        final Uri data = getIntent().getData();

        if (data != null) {
            mProfileUserId = data.getQueryParameter("id");
            if (mProfileUserId != null && !mProfileUserId.isEmpty()) {
                initViewModel();
                initBinding();
                setupToolbar();
                setupNavigationDrawer();
                chooseAccount();
            } else {
                Toast.makeText(this, R.string.err_msg_missing_profile_user_id, Toast.LENGTH_LONG).show();
                finish();
            }
        } else {
            Toast.makeText(this, R.string.err_msg_missing_profile_user_id, Toast.LENGTH_LONG).show();
            finish();
        }
    }

    private void initViewModel (
    ) {
        Log.d(TAG, "initViewModel");
        mViewModel = new ViewModelProvider(this).get(ProfileViewModel.class);
        mViewModel.getProfileUserId().set(mProfileUserId);
    }

    private void initBinding (
    ) {
        Log.d(TAG, "initBinding");
        mActivityBinding = DataBindingUtil.setContentView(this, R.layout.profile_activity);
        mActivityBinding.setViewModel(mViewModel);
        mActivityBinding.setProfileEditor(this);

        final QRCodeWriter writer = new QRCodeWriter();
        try {
            final BitMatrix bitMatrix = writer.encode(getString(R.string.url_profile, mProfileUserId), BarcodeFormat.QR_CODE, 512, 512);
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
        return true;
    }

    private void setupNavigationDrawer () {
        Log.d(TAG, "setupNavigationDrawer");
        mDrawerLayout = findViewById(R.id.drawer_layout);
    }

    @Override
    public boolean onOptionsItemSelected (final MenuItem item) {
        Log.d(TAG, "onOptionsItemSelected item=" + item);
        switch (item.getItemId()) {
            case android.R.id.home:
                mDrawerLayout.openDrawer(GravityCompat.START);
                return true;
            case R.id.miQRCodeScan:
                return true;
        }
        return super.onOptionsItemSelected(item);
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

    private void onUpdateDataComplete (@NonNull final ServerApiResponse serverApiResponse) {
        Log.d(TAG, "onUpdateDataComplete serverApiResponse=" + serverApiResponse);
        if (serverApiResponse.getCode() == 200) {
            mViewModel.getCardNumber().set(mActivityBinding.etCardNumber.getText().toString());
            Toast.makeText(this, R.string.info_msg_update_data_complete, Toast.LENGTH_LONG).show();
        } else {
            mViewModel.getCardNumber().notifyChange();
            Toast.makeText(this, R.string.err_msg_update_data_failed, Toast.LENGTH_LONG).show();
        }
    }

    private void chooseAccount () {
        Log.d(TAG, "chooseAccount");

        mAccountManager = AccountManager.get(this);

        final String accountType = getString(R.string.account_type);
        final Account[] accounts = mAccountManager.getAccountsByType(accountType);
        if (accounts.length == 0) {
            downloadProfileData(null);
        } else if (accounts.length == 1) {
            mAccount = accounts[0];
            mViewModel.getIsEnableEdit().set(mProfileUserId.equals(mAccountManager.getUserData(accounts[0], AccountGeneral.USER_DATA_USER_ID)));
            getAuthTokenAndDownloadProfileData();
        } else {
            showAccountPicker(accounts);
        }
    }

    private void showAccountPicker (
            @NonNull final Account[] accounts
    ) {
        Log.d(TAG, "showAccountPicker accounts=" + Arrays.toString(accounts));
        final String[] names = new String[accounts.length];
        for (int i = 0; i < accounts.length; i++) {
            names[i] = accounts[i].name;
        }

        new AlertDialog.
                Builder(this).
                setTitle(R.string.rqst_choose_account).
                setCancelable(false).
                setAdapter(
                        new ArrayAdapter<>(
                                getBaseContext(),
                                android.R.layout.simple_list_item_1, names),
                        (dialog, which) -> {
                            mAccount = accounts[which];
                            mViewModel.getIsEnableEdit().set(mProfileUserId.equals(mAccountManager.getUserData(mAccount, AccountGeneral.USER_DATA_USER_ID)));
                            getAuthTokenAndDownloadProfileData();
                        }
                ).
                create().
                show();
    }

    private void getAuthTokenAndDownloadProfileData () {
        Log.d(TAG, "getAuthTokenAndDownloadProfileData");
        RatingApp.getAuthToken(
                this,
                mAccount,
                this::onGetAuthTokenForDownloadProfileDataComplete);
    }

    private void getAuthTokenAndUpdateProfileData (@NonNull final String cardNumber) {
        Log.d(TAG, "getAuthTokenAndUpdateProfileData");
        RatingApp.getAuthToken(
                this,
                mAccount,
                future -> onGetAuthTokenForUpdateProfileDataComplete(future, cardNumber)
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

    private void extractDataFromServerApiResponse (ServerApiResponse serverApiResponse) {
        Log.d(TAG, "extractDataFromServerApiResponse");
        if (serverApiResponse.getCode() == 200) {
            if (serverApiResponse.getBody() != null) {
                final String responseBody = serverApiResponse.getBody();
                try {
                    final JSONObject userJSON = new JSONObject(responseBody);

                    final String photo = userJSON.getString("photo");
                    mViewModel.getLastName().set(photo);
                    Picasso.get().load(photo).into(mActivityBinding.ivPhoto);

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
        if (cardNumber.length() == 16) {
            getAuthTokenAndUpdateProfileData(cardNumber);
        } else {
            Toast.makeText(this, getString(blagodarie.rating.auth.R.string.err_msg_incorrect_card_number), Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onCancel () {
        Log.d(TAG, "onCancel");
        mViewModel.getCardNumber().notifyChange();
        mViewModel.setCurrentMode(ProfileViewModel.Mode.VIEW);
    }

}
