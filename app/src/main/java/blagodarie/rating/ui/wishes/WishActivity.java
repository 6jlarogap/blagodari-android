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

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import blagodarie.rating.R;
import blagodarie.rating.auth.AccountGeneral;
import blagodarie.rating.databinding.WishActivityBinding;
import blagodarie.rating.ui.AccountProvider;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;

public final class WishActivity
        extends AppCompatActivity {

    private static final String TAG = WishActivity.class.getSimpleName();

    public static final String EXTRA_WISH = "blagodarie.rating.ui.wishes.EditWishActivity.WISH";

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
                    /*AccountProvider.getAccount(
                            this,
                            new AccountProvider.OnAccountSelectListener() {
                                @Override
                                public void onNoAccount () {
                                    downloadProfileData(null);
                                }

                                @Override
                                public void onAccountSelected (@NonNull final Account account) {
                                    mAccount = account;
                                    mActivityBinding.nvNavigation.getMenu().findItem(R.id.miLogout).setEnabled(mAccount != null);
                                    mViewModel.getIsSelfProfile().set(mProfileUserId.toString().equals(mAccountManager.getUserData(mAccount, AccountGeneral.USER_DATA_USER_ID)));
                                    getAuthTokenAndDownloadProfileData();
                                }
                            }
                    );*/
                } else {
                    Toast.makeText(this, R.string.err_msg_missing_profile_user_id, Toast.LENGTH_LONG).show();
                    finish();
                }
            } catch (IllegalArgumentException e) {
                Toast.makeText(this, R.string.err_msg_incorrect_user_id, Toast.LENGTH_LONG).show();
                finish();
            }

        } else {
            Toast.makeText(this, R.string.err_msg_missing_profile_user_id, Toast.LENGTH_LONG).show();
            finish();
        }
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
        //mActivityBinding.srlRefreshProfileInfo.setOnRefreshListener(this::getAuthTokenAndDownloadProfileData);

        final QRCodeWriter writer = new QRCodeWriter();
        final Map<EncodeHintType, Object> hints = new EnumMap<>(EncodeHintType.class);
        hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");
        hints.put(EncodeHintType.MARGIN, 0); /* default = 4 */
        try {
            final BitMatrix bitMatrix = writer.encode(getString(R.string.url_profile, mWishId.toString()), BarcodeFormat.QR_CODE, 500, 500, hints);
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
                        fromCallable(() -> (Wish) getIntent().getSerializableExtra(EXTRA_WISH)).
                        subscribeOn(Schedulers.io()).
                        observeOn(AndroidSchedulers.mainThread()).
                        subscribe(
                                wish -> {
                                    mWish = wish;
                                    mViewModel.getDownloadInProgress().set(false);
                                    mViewModel.getWishText().set(wish.getText());
                                    AccountProvider.getAccount(
                                            this,
                                            new AccountProvider.OnAccountSelectListener() {
                                                @Override
                                                public void onNoAccount () {
                                                    mViewModel.isSelfProfile().set(false);
                                                }

                                                @Override
                                                public void onAccountSelected (@NonNull final Account account) {
                                                    mAccount = account;
                                                    mViewModel.isSelfProfile().set(wish.getOwnerUuid().toString().equals(mAccountManager.getUserData(mAccount, AccountGeneral.USER_DATA_USER_ID)));
                                                }
                                            });
                                },
                                throwable -> {
                                    mViewModel.getDownloadInProgress().set(false);
                                    Toast.makeText(this, throwable.getMessage(), Toast.LENGTH_LONG).show();
                                }
                        )
        );
    }

    @Override
    protected void onActivityResult (
            final int requestCode,
            final int resultCode,
            @Nullable final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case 1:
                if (resultCode == Activity.RESULT_OK &&
                        data != null) {
                    final Wish wish = (Wish) data.getSerializableExtra(EditWishActivity.EXTRA_WISH);
                    if (wish != null) {
                        mViewModel.getWishText().set(wish.getText());
                    }
                }
                break;
            default:
                break;
        }
    }

    public void onEditWishClick (@NonNull final View view) {
        final Intent intent = EditWishActivity.createSelfIntent(this, mWish);
        startActivityForResult(intent, 1);
    }

    public void onDeleteWishClick (View view) {
        Toast.makeText(this, R.string.info_msg_function_in_developing, Toast.LENGTH_LONG).show();
    }
}
