package blagodarie.rating.ui.wishes;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.ViewModelProvider;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;
import java.util.EnumMap;
import java.util.Map;
import java.util.UUID;

import blagodarie.rating.R;
import blagodarie.rating.auth.AccountGeneral;
import blagodarie.rating.databinding.WishActivityBinding;
import blagodarie.rating.server.ServerApiResponse;
import blagodarie.rating.server.ServerConnector;
import blagodarie.rating.ui.AccountProvider;
import blagodarie.rating.ui.user.wishes.Wish;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;

public final class WishActivity
        extends AppCompatActivity {

    private static final String TAG = WishActivity.class.getSimpleName();

    private CompositeDisposable mCompositeDisposable = new CompositeDisposable();

    private WishViewModel mViewModel;

    private WishActivityBinding mActivityBinding;

    private AccountManager mAccountManager;

    private Account mAccount;

    private UUID mWishId;

    private Wish mWish;

    @Override
    protected void onCreate (@Nullable final Bundle savedInstanceState) {
        Log.d(TAG, "onCreate");
        super.onCreate(savedInstanceState);

        mAccountManager = AccountManager.get(this);

        final Uri data = getIntent().getData();

        if (data != null) {
            final String wishId = data.getQueryParameter("id");
            try {
                mWishId = UUID.fromString(wishId);
                if (mWishId != null) {
                    initViewModel();
                    initBinding();
                    downloadWishData();
                    AccountProvider.getAccount(
                            this,
                            account -> {
                                if (account != null) {
                                    mAccount = account;
                                } else {
                                    mViewModel.isSelfWish().set(false);
                                }
                            }
                    );
                } else {
                    Toast.makeText(this, R.string.err_msg_missing_user_id, Toast.LENGTH_LONG).show();
                    finish();
                }
            } catch (IllegalArgumentException e) {
                Toast.makeText(this, R.string.err_msg_incorrect_user_id, Toast.LENGTH_LONG).show();
                finish();
            }

        } else {
            Toast.makeText(this, R.string.err_msg_missing_user_id, Toast.LENGTH_LONG).show();
            finish();
        }
    }

    @Override
    protected void onDestroy () {
        Log.d(TAG, "onDestroy");
        super.onDestroy();
        mCompositeDisposable.clear();
    }

    private void initViewModel () {
        Log.d(TAG, "initViewModel");
        mViewModel = new ViewModelProvider(this).get(WishViewModel.class);
    }

    private void initBinding (
    ) {
        Log.d(TAG, "initBinding");
        mActivityBinding = DataBindingUtil.setContentView(this, R.layout.wish_activity);
        mActivityBinding.setViewModel(mViewModel);
        mActivityBinding.srlRefreshProfileInfo.setOnRefreshListener(this::downloadWishData);

        final QRCodeWriter writer = new QRCodeWriter();
        final Map<EncodeHintType, Object> hints = new EnumMap<>(EncodeHintType.class);
        hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");
        hints.put(EncodeHintType.MARGIN, 0); /* default = 4 */
        try {
            final BitMatrix bitMatrix = writer.encode(getString(R.string.url_wish, mWishId.toString()), BarcodeFormat.QR_CODE, 500, 500, hints);
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

    private void downloadWishData (
    ) {
        Log.d(TAG, "downloadProfileData");
        mViewModel.getDownloadInProgress().set(true);
        mCompositeDisposable.add(
                Observable.
                        fromCallable(() -> ServerConnector.sendRequestAndGetResponse("getwishinfo?uuid=" + mWishId)).
                        subscribeOn(Schedulers.io()).
                        observeOn(AndroidSchedulers.mainThread()).
                        subscribe(
                                serverApiResponse -> {
                                    mViewModel.getDownloadInProgress().set(false);
                                    extractWishFromServerApiResponse(serverApiResponse);
                                },
                                throwable -> {
                                    mViewModel.getDownloadInProgress().set(false);
                                    Toast.makeText(this, throwable.getMessage(), Toast.LENGTH_LONG).show();
                                }
                        )
        );
    }

    private void extractWishFromServerApiResponse (
            @NonNull final ServerApiResponse serverApiResponse
    ) {
        Log.d(TAG, "extractDataFromServerApiResponse");
        if (serverApiResponse.getCode() == 200) {
            if (serverApiResponse.getBody() != null) {
                final String responseBody = serverApiResponse.getBody();
                try {
                    final JSONObject wishJSON = new JSONObject(responseBody);
                    final String ownerUuidString = wishJSON.getString("owner_id");
                    final UUID ownerUuid = UUID.fromString(ownerUuidString);
                    final String text = wishJSON.getString("text");
                    final long timestamp = wishJSON.getLong("last_edit");
                    mWish = new Wish(mWishId, ownerUuid, text, new Date(timestamp));
                    mViewModel.getWishText().set(mWish.getText());
                    if (mAccount != null) {
                        mViewModel.isSelfWish().set(ownerUuid.toString().equals(mAccountManager.getUserData(mAccount, AccountGeneral.USER_DATA_USER_ID)));
                    } else {
                        mViewModel.isSelfWish().set(false);
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
    protected void onActivityResult (
            final int requestCode,
            final int resultCode,
            @Nullable final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case 1:
                if (resultCode == Activity.RESULT_OK) {
                    downloadWishData();
                }
                break;
            default:
                break;
        }
    }

    public void onEditWishClick (@NonNull final View view) {
        final Intent intent = EditWishActivity.createSelfIntent(this, mWish, mAccount);
        startActivityForResult(intent, 1);
    }

    public void onDeleteWishClick (View view) {
        getAuthTokenAndDeleteWish();
    }

    private void getAuthTokenAndDeleteWish () {
        Log.d(TAG, "getAuthTokenAndDeleteWish");
        AccountProvider.getAuthToken(
                this,
                mAccount,
                this::deleteWish);
    }

    private void deleteWish (
            @NonNull final String authToken
    ) {
        Log.d(TAG, "deleteWish");
        mCompositeDisposable.add(
                Observable.
                        fromCallable(() -> ServerConnector.sendAuthRequestAndGetResponse("deletewish?uuid=" + mWishId, authToken)).
                        subscribeOn(Schedulers.io()).
                        observeOn(AndroidSchedulers.mainThread()).
                        subscribe(
                                this::onDeleteComplete,
                                throwable -> Toast.makeText(this, throwable.getMessage(), Toast.LENGTH_LONG).show()
                        )
        );
    }

    private void onDeleteComplete (
            @NonNull final ServerApiResponse serverApiResponse) {
        Log.d(TAG, "extractDataFromServerApiResponse");
        if (serverApiResponse.getCode() == 200) {
            Toast.makeText(this, R.string.info_msg_wish_deleted, Toast.LENGTH_LONG).show();
            finish();
        } else {
            Toast.makeText(this, R.string.err_msg_delete_wish_failed, Toast.LENGTH_LONG).show();
        }
    }

    public void onDonateWishClick (View view) {
        Toast.makeText(this, R.string.info_msg_function_in_developing, Toast.LENGTH_LONG).show();
    }
}
