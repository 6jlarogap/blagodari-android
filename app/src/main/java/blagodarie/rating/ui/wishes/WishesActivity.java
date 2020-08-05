package blagodarie.rating.ui.wishes;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.Context;
import android.content.Intent;
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
import androidx.recyclerview.widget.LinearLayoutManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import blagodarie.rating.R;
import blagodarie.rating.auth.AccountGeneral;
import blagodarie.rating.databinding.WishesActivityBinding;
import blagodarie.rating.server.ServerApiResponse;
import blagodarie.rating.server.ServerConnector;
import blagodarie.rating.ui.AccountProvider;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;


public final class WishesActivity
        extends AppCompatActivity {

    private static final String TAG = WishesActivity.class.getSimpleName();

    private static final String EXTRA_OWNER_ID = "blagodarie.rating.ui.wishes.WishesActivity.OWNER_ID";

    private static final int EDIT_WISH_ACTIVITY_REQUEST_CODE = 1;

    private UUID mOwnerId;

    private WishesViewModel mViewModel;

    private WishesActivityBinding mActivityBinding;

    private WishesAdapter mWishesAdapter;

    private AccountManager mAccountManager;

    private Account mAccount;

    private CompositeDisposable mCompositeDisposable = new CompositeDisposable();

    @Override
    protected void onCreate (@Nullable final Bundle savedInstanceState) {
        Log.d(TAG, "onCreate");
        super.onCreate(savedInstanceState);

        mAccountManager = AccountManager.get(this);

        mOwnerId = (UUID) getIntent().getSerializableExtra(EXTRA_OWNER_ID);

        if (mOwnerId != null) {
            initViewModel();
            initBinding();
            initThanksUserAdapter();
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
                            mViewModel.isSelfProfile().set(mOwnerId.toString().equals(mAccountManager.getUserData(mAccount, AccountGeneral.USER_DATA_USER_ID)));
                        }
                    }
            );
        } else {
            Toast.makeText(this, R.string.err_msg_missing_profile_user_id, Toast.LENGTH_LONG).show();
            finish();
        }
    }

    @Override
    protected void onStart () {
        super.onStart();
        if (mOwnerId != null) {
            downloadUserWishes();
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
        mViewModel = new ViewModelProvider(this).get(WishesViewModel.class);
        List<Wish> wishes = new ArrayList<>();
        mViewModel.getWishes().setValue(wishes);
    }

    private void initBinding (
    ) {
        Log.d(TAG, "initBinding");
        mActivityBinding = DataBindingUtil.setContentView(this, R.layout.wishes_activity);
        mActivityBinding.setViewModel(mViewModel);
        mActivityBinding.srlRefreshProfileInfo.setOnRefreshListener(this::downloadUserWishes);
    }

    private void downloadUserWishes () {
        Log.d(TAG, "downloadUserWishes");
        mViewModel.getDownloadInProgress().set(true);
        mCompositeDisposable.add(
                Observable.
                        fromCallable(() -> ServerConnector.sendRequestAndGetResponse("getuserwishes?uuid=" + mOwnerId)).
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

    private void extractDataFromServerApiResponse (
            @NonNull final ServerApiResponse serverApiResponse
    ) {
        Log.d(TAG, "extractDataFromServerApiResponse");
        if (serverApiResponse.getCode() == 200) {
            if (serverApiResponse.getBody() != null) {
                final String responseBody = serverApiResponse.getBody();
                try {
                    final JSONArray userWishesJSON = new JSONObject(responseBody).getJSONArray("wishes");
                    final List<Wish> userWishes = new ArrayList<>();
                    for (int i = 0; i < userWishesJSON.length(); i++) {
                        final JSONObject jsonWish = userWishesJSON.getJSONObject(i);
                        final String uuidString = jsonWish.getString("uuid");
                        final UUID uuid = UUID.fromString(uuidString);
                        final String text = jsonWish.getString("text");
                        final long timestamp = jsonWish.getLong("last_edit");
                        userWishes.add(new Wish(uuid, mOwnerId, text, new Date(timestamp)));
                    }
                    Collections.sort(userWishes, (w1, w2) -> w2.getTimestamp().compareTo(w1.getTimestamp()));
                    mViewModel.getWishes().setValue(userWishes);
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

    private void initThanksUserAdapter () {
        mViewModel.getWishes().observe(this, wishes -> {
            if (mWishesAdapter == null) {
                mWishesAdapter = new WishesAdapter(this::onWishClick);
                mActivityBinding.rvWishes.setLayoutManager(new LinearLayoutManager(this));
                mActivityBinding.rvWishes.setAdapter(mWishesAdapter);
            }
            mWishesAdapter.setData(wishes);
        });
    }

    public void onAddWishClick (@NonNull final View view) {
        final Intent intent = EditWishActivity.createSelfIntent(this, new Wish(UUID.randomUUID(), mOwnerId, "", new Date()), mAccount);
        startActivityForResult(intent, EDIT_WISH_ACTIVITY_REQUEST_CODE);
    }

    private void onWishClick (@NonNull final Wish wish) {
        final Intent i = new Intent(Intent.ACTION_VIEW);
        i.setData(Uri.parse(getString(R.string.url_wish, wish.getUuid())));
        startActivity(i);
    }

    public static Intent createSelfIntent (
            @NonNull final Context context,
            @NonNull final UUID ownerId
    ) {
        Log.d(TAG, "createSelfIntent");
        final Intent intent = new Intent(context, WishesActivity.class);
        intent.putExtra(EXTRA_OWNER_ID, ownerId);
        return intent;
    }
}
