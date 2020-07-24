package blagodarie.rating.ui.wishes;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Activity;
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
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import blagodarie.rating.R;
import blagodarie.rating.auth.AccountGeneral;
import blagodarie.rating.databinding.WishesActivityBinding;
import blagodarie.rating.ui.AccountProvider;


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

    private void initViewModel () {
        Log.d(TAG, "initViewModel");
        mViewModel = new ViewModelProvider(this).get(WishesViewModel.class);
        List<Wish> wishes = new ArrayList<>();
        wishes.add(new Wish(UUID.randomUUID(), mOwnerId, "Желание 1", new Date()));
        wishes.add(new Wish(UUID.randomUUID(), mOwnerId, "Желание 2", new Date()));
        wishes.add(new Wish(UUID.randomUUID(), mOwnerId, "Желание 3", new Date()));
        mViewModel.getWishes().setValue(wishes);
    }

    private void initBinding (
    ) {
        Log.d(TAG, "initBinding");
        mActivityBinding = DataBindingUtil.setContentView(this, R.layout.wishes_activity);
        mActivityBinding.setViewModel(mViewModel);
    }

    @Override
    protected void onActivityResult (
            final int requestCode,
            final int resultCode,
            @Nullable final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case EDIT_WISH_ACTIVITY_REQUEST_CODE:
                if (resultCode == Activity.RESULT_OK &&
                        data != null) {
                    final Wish wish = (Wish) data.getSerializableExtra(EditWishActivity.EXTRA_WISH);
                    List<Wish> wishes = mViewModel.getWishes().getValue();
                    if (wishes == null) {
                        wishes = new ArrayList<>();
                        wishes.add(wish);
                        mViewModel.getWishes().setValue(wishes);
                    } else {
                        if (!wishes.contains(wish)) {
                            wishes.add(wish);
                        } else {
                            wishes.set(wishes.indexOf(wish), wish);
                        }
                        Collections.sort(wishes, (w1, w2) -> w2.getTimestamp().compareTo(w1.getTimestamp()));
                        mViewModel.getWishes().setValue(wishes);
                    }
                }
                break;
            default:
                break;
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
        final Intent intent = EditWishActivity.createSelfIntent(this, new Wish(UUID.randomUUID(), mOwnerId, "", new Date()));
        startActivityForResult(intent, EDIT_WISH_ACTIVITY_REQUEST_CODE);
    }

    private void onWishClick (@NonNull final Wish wish) {
        final Intent i = new Intent(Intent.ACTION_VIEW);
        i.putExtra(WishActivity.EXTRA_WISH, wish);
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
