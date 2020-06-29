package blagodarie.rating.ui.main;

import android.Manifest;
import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.ViewModelProvider;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

import java.util.UUID;
import java.util.logging.Logger;

import blagodarie.rating.R;
import blagodarie.rating.auth.AccountGeneral;
import blagodarie.rating.databinding.MainActivityBinding;

public final class MainActivity
        extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();

    private static final String EXTRA_ACCOUNT = "blagodarie.rating.ui.main.ACCOUNT";

    @Override
    protected void onCreate (@Nullable final Bundle savedInstanceState) {
        Log.d(TAG, "onCreate");
        super.onCreate(savedInstanceState);
        Account account = getIntent().getParcelableExtra(EXTRA_ACCOUNT);


        QRCodeWriter writer = new QRCodeWriter();
        UUID userId = UUID.fromString(AccountManager.get(this).getUserData(account, AccountGeneral.USER_DATA_USER_ID));

        MainViewModel viewModel = new ViewModelProvider(this).get(MainViewModel.class);
        viewModel.getProfileUrl().set(getString(R.string.url_profile, userId));
        MainActivityBinding activityBinding = DataBindingUtil.setContentView(this, R.layout.main_activity);
        activityBinding.setViewModel(viewModel);
        try {
            BitMatrix bitMatrix = writer.encode(getString(R.string.url_profile, userId), BarcodeFormat.QR_CODE, 512, 512);
            int width = bitMatrix.getWidth();
            int height = bitMatrix.getHeight();
            Bitmap bmp = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
            for (int x = 0; x < width; x++) {
                for (int y = 0; y < height; y++) {
                    bmp.setPixel(x, y, bitMatrix.get(x, y) ? Color.BLACK : Color.WHITE);
                }
            }
            activityBinding.ivQRCode.setImageBitmap(bmp);
        } catch (WriterException e) {
            e.printStackTrace();
        }
    }

    public static Intent createSelfIntent (
            @NonNull final Context context,
            @NonNull final Account account
    ) {
        Log.d(TAG, "createSelfIntent");
        final Intent intent = new Intent(context, MainActivity.class);
        intent.putExtra(EXTRA_ACCOUNT, account);
        return intent;
    }

}
