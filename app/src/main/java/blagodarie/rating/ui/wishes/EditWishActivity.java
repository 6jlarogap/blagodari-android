package blagodarie.rating.ui.wishes;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AccountManagerFuture;
import android.accounts.AuthenticatorException;
import android.accounts.OperationCanceledException;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import java.io.IOException;
import java.util.Date;
import java.util.Locale;

import blagodarie.rating.R;
import blagodarie.rating.databinding.EditWishActivityBinding;
import blagodarie.rating.server.ServerApiResponse;
import blagodarie.rating.server.ServerConnector;
import blagodarie.rating.ui.AccountProvider;
import blagodarie.rating.ui.user.wishes.Wish;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;

public final class EditWishActivity
        extends AppCompatActivity {

    private static final String TAG = EditWishActivity.class.getSimpleName();

    public static final String EXTRA_WISH = "blagodarie.rating.ui.wishes.EditWishActivity.WISH";

    public static final String EXTRA_ACCOUNT = "blagodarie.rating.ui.wishes.EditWishActivity.ACCOUNT";

    private CompositeDisposable mCompositeDisposable = new CompositeDisposable();

    private Account mAccount;

    private Wish mWish;

    private EditWishActivityBinding mActivityBinding;

    @Override
    protected void onCreate (@Nullable final Bundle savedInstanceState) {
        Log.d(TAG, "onCreate");
        super.onCreate(savedInstanceState);
        mAccount = (Account) getIntent().getParcelableExtra(EXTRA_ACCOUNT);

        if (mAccount != null) {
            mWish = (Wish) getIntent().getSerializableExtra(EXTRA_WISH);
            if (mWish != null) {
                initBinding();
            } else {
                Toast.makeText(this, R.string.err_msg_missing_wish, Toast.LENGTH_SHORT).show();
                setResult(RESULT_CANCELED);
                finish();
            }
        } else {
            Toast.makeText(this, R.string.err_msg_missing_account, Toast.LENGTH_SHORT).show();
            setResult(RESULT_CANCELED);
            finish();
        }
    }

    @Override
    protected void onDestroy () {
        Log.d(TAG, "onDestroy");
        super.onDestroy();
        mCompositeDisposable.clear();
    }

    private void initBinding (
    ) {
        Log.d(TAG, "initBinding");
        mActivityBinding = DataBindingUtil.setContentView(this, R.layout.edit_wish_activity);
        mActivityBinding.setWish(mWish);
    }

    public void onSaveWishClick (@NonNull final View view) {
        Log.d(TAG, "onSaveWishClick");
        mWish.setText(mActivityBinding.etWishText.getText().toString());
        mWish.setTimestamp(new Date());
        getAuthTokenAndAddOrUpdateWish();
    }

    private void getAuthTokenAndAddOrUpdateWish () {
        Log.d(TAG, "getAuthTokenAndAddOrUpdateWish");
        AccountProvider.getAuthToken(this, mAccount, this::addOrUpdateWish);
    }

    private void addOrUpdateWish (
            @NonNull final String authToken
    ) {
        Log.d(TAG, "downloadProfileData");
        final String content = String.format(Locale.ENGLISH, "{\"uuid\":\"%s\",\"text\":\"%s\",\"last_edit\":%d}", mWish.getUuid().toString(), mWish.getText(), mWish.getTimestamp().getTime());
        mCompositeDisposable.add(
                Observable.
                        fromCallable(() -> ServerConnector.sendAuthRequestAndGetResponse("addorupdatewish", authToken, content)).
                        subscribeOn(Schedulers.io()).
                        observeOn(AndroidSchedulers.mainThread()).
                        subscribe(
                                this::extractDataFromServerApiResponse,
                                throwable -> {
                                    Toast.makeText(this, throwable.getMessage(), Toast.LENGTH_LONG).show();
                                }
                        )
        );
    }

    private void extractDataFromServerApiResponse (
            @NonNull final ServerApiResponse serverApiResponse) {
        Log.d(TAG, "extractDataFromServerApiResponse");
        if (serverApiResponse.getCode() == 200) {
            Toast.makeText(this, R.string.info_msg_wish_saved, Toast.LENGTH_LONG).show();
            setResult(RESULT_OK);
            finish();
        } else {
            Toast.makeText(this, R.string.err_msg_save_wish_failed, Toast.LENGTH_LONG).show();
        }
    }

    public static Intent createSelfIntent (
            @NonNull final Context context,
            @NonNull final Wish wish,
            @NonNull final Account account
    ) {
        Log.d(TAG, "createSelfIntent");
        final Intent intent = new Intent(context, EditWishActivity.class);
        intent.putExtra(EXTRA_WISH, wish);
        intent.putExtra(EXTRA_ACCOUNT, account);
        return intent;
    }
}
