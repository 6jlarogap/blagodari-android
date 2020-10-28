package blagodarie.rating.ui.wishes;

import android.accounts.Account;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import java.util.Date;
import java.util.Locale;

import blagodarie.rating.R;
import blagodarie.rating.databinding.EditWishActivityBinding;
import blagodarie.rating.model.entities.Wish;
import blagodarie.rating.server.ServerApiResponse;
import blagodarie.rating.server.ServerConnector;
import blagodarie.rating.ui.AccountProvider;
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
                setupToolbar();
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

    private void getAuthTokenAndAddOrUpdateWish () {
        Log.d(TAG, "getAuthTokenAndAddOrUpdateWish");
        AccountProvider.getAuthToken(this, mAccount, this::addOrUpdateWish);
    }

    private void setupToolbar () {
        Log.d(TAG, "setupToolbar");
        setSupportActionBar(findViewById(R.id.toolbar));
        final ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayShowTitleEnabled(false);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }


    @Override
    public boolean onCreateOptionsMenu (
            @NonNull final Menu menu
    ) {
        Log.d(TAG, "onCreateOptionsMenu");
        getMenuInflater().inflate(R.menu.edit_wish_activity, menu);
        return true;
    }

    private void addOrUpdateWish (
            @NonNull final String authToken
    ) {
        Log.d(TAG, "downloadProfileData");
        final String content = String.format(Locale.ENGLISH, "{\"uuid\":\"%s\",\"text\":\"%s\",\"last_edit\":%d}", mWish.getId().toString(), mWish.getText(), mWish.getLastEdit().getTime());
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

    @Override
    public boolean onOptionsItemSelected (final MenuItem item) {
        Log.d(TAG, "onOptionsItemSelected item=" + item);
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            case R.id.miSave:
                mWish.setText(mActivityBinding.etWishText.getText().toString());
                mWish.setLastEdit(new Date());
                getAuthTokenAndAddOrUpdateWish();
                return true;
        }
        return super.onOptionsItemSelected(item);
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
