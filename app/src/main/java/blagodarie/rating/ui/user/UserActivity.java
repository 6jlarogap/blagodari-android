package blagodarie.rating.ui.user;

import android.Manifest;
import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
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
import androidx.navigation.NavController;
import androidx.navigation.NavDirections;
import androidx.navigation.Navigation;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.squareup.picasso.Picasso;

import java.util.UUID;

import blagodarie.rating.R;
import blagodarie.rating.auth.AccountGeneral;
import blagodarie.rating.databinding.NavHeaderLayoutBinding;
import blagodarie.rating.databinding.UserActivityBinding;
import blagodarie.rating.ui.AccountProvider;
import blagodarie.rating.ui.splash.SplashActivity;
import blagodarie.rating.ui.user.profile.ProfileFragment;
import blagodarie.rating.ui.user.profile.ProfileFragmentDirections;
import blagodarie.rating.ui.wishes.WishesActivity;

public final class UserActivity
        extends AppCompatActivity
        implements ProfileFragment.FragmentCommunicator {

    private static final String TAG = UserActivity.class.getSimpleName();

    private AccountManager mAccountManager;

    private Account mAccount;

    private UUID mUserId;

    private UserViewModel mViewModel;

    private UserActivityBinding mActivityBinding;

    private DrawerLayout mDrawerLayout;

    private NavController mNavController;

    private AppCompatImageView ivOwnAccountPhoto;

    @Override
    protected void onCreate (@Nullable final Bundle savedInstanceState) {
        Log.d(TAG, "onCreate");
        super.onCreate(savedInstanceState);

        mAccountManager = AccountManager.get(this);

        final Uri data = getIntent().getData();

        if (data != null) {
            final String userId = data.getQueryParameter("id");
            try {
                mUserId = UUID.fromString(userId);
            } catch (IllegalArgumentException e) {
                Toast.makeText(this, R.string.err_msg_incorrect_user_id, Toast.LENGTH_LONG).show();
                finish();
            }
            if (mUserId != null) {
                initViewModel();
                initBinding();
                mNavController = Navigation.findNavController(this, blagodarie.rating.auth.R.id.nav_host_fragment);
                setupToolbar();
                setupNavigationDrawer();
                AccountProvider.getAccount(
                        this,
                        this::onAccountSelected
                );
            }
        } else {
            Toast.makeText(this, R.string.err_msg_missing_user_id, Toast.LENGTH_LONG).show();
            finish();
        }
/*
        final Uri data = getIntent().getData();

        if (data != null) {
            final String userId = data.getQueryParameter("id");
            try {
                mUserId = UUID.fromString(userId);
                if (mUserId != null) {
                    initViewModel();
                    mViewModel.isProfile().set(true);
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
                                    mActivityBinding.nvNavigation.getMenu().findItem(R.id.miLogout).setEnabled(mAccount != null);
                                    mViewModel.getIsSelfProfile().set(mUserId.toString().equals(mAccountManager.getUserData(mAccount, AccountGeneral.USER_DATA_USER_ID)));
                                    getAuthTokenAndDownloadProfileData();
                                }
                            }
                    );
                } else {
                    Toast.makeText(this, R.string.err_msg_missing_profile_user_id, Toast.LENGTH_LONG).show();
                    finish();
                }
            } catch (IllegalArgumentException e) {
                Toast.makeText(this, R.string.err_msg_incorrect_user_id, Toast.LENGTH_LONG).show();
                finish();
            }

        } else {
            mJustText = getIntent().getStringExtra("text");
            initViewModel();
            mViewModel.isProfile().set(false);
            mViewModel.getLastName().set(mJustText);
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
                            mActivityBinding.nvNavigation.getMenu().findItem(R.id.miLogout).setEnabled(mAccount != null);
                            mViewModel.getIsSelfProfile().set((mUserId != null ? mUserId.toString() : mJustText).equals(mAccountManager.getUserData(mAccount, AccountGeneral.USER_DATA_USER_ID)));
                            getAuthTokenAndDownloadProfileData();
                        }
                    }
            );
        }*/
    }

    @Override
    public void onBackPressed () {
        Log.d(TAG, "onBackPressed");
        if (mNavController.getCurrentDestination() != null) {
            if (mNavController.getCurrentDestination().getId() == R.id.profileFragment) {
                finish();
            } else {
                super.onBackPressed();
            }
        }
    }

    public void onAccountSelected (@Nullable final Account account) {
        if (account != null) {
            mAccount = account;
            mActivityBinding.nvNavigation.getMenu().findItem(R.id.miLogout).setEnabled(mAccount != null);
            mViewModel.getOwnAccountPhotoUrl().setValue(mAccountManager.getUserData(mAccount, AccountGeneral.USER_DATA_PHOTO));
        }
        toProfile();
    }

    void toProfile () {
        Log.d(TAG, "toProfile");
        final NavDirections action = StartFragmentDirections.actionStartFragment2ToProfileFragment(mUserId, mAccount);
        mNavController.navigate(action);
    }

    private void initViewModel () {
        Log.d(TAG, "initViewModel");
        mViewModel = new ViewModelProvider(this).get(UserViewModel.class);
    }

    private void initBinding () {
        Log.d(TAG, "initBinding");
        mActivityBinding = DataBindingUtil.setContentView(this, R.layout.user_activity);
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
        getMenuInflater().inflate(R.menu.user_activity, menu);
        if (mAccount != null) {
            final MenuItem miMyAccount = menu.findItem(R.id.miOwnAccount);
            miMyAccount.setVisible(true);
            ivOwnAccountPhoto = miMyAccount.getActionView().findViewById(R.id.ivOwnAccountPhoto);
            mViewModel.getOwnAccountPhotoUrl().observe(this, s -> Picasso.get().load(s).into(ivOwnAccountPhoto));
            ivOwnAccountPhoto.setOnClickListener(view -> {
                /*if (mViewModel.getIsSelfProfile().get()) {
                    //getAuthTokenAndDownloadProfileData();
                } else {
                    final Intent i = new Intent(Intent.ACTION_VIEW);
                    i.setData(Uri.parse(getString(R.string.url_profile, mAccountManager.getUserData(mAccount, AccountGeneral.USER_DATA_USER_ID))));
                    startActivity(i);
                }*/
            });
        }
        return true;
    }

    private void setupNavigationDrawer () {
        Log.d(TAG, "setupNavigationDrawer");
        mDrawerLayout = findViewById(R.id.drawer_layout);

        final NavHeaderLayoutBinding binding = DataBindingUtil.inflate(getLayoutInflater(), R.layout.nav_header_layout, null, false);
        mActivityBinding.nvNavigation.addHeaderView(binding.getRoot());
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
            case R.id.miOwnAccount:
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
                    final String content = result.getContents();
                    try {
                        final Uri uri = Uri.parse(content);
                        if (uri.getPath() != null) {
                            if (uri.getPath().equals("/profile")) {
                                final String profileUserIdString = uri.getQueryParameter("id");
                                UUID profileUserId = UUID.fromString(profileUserIdString);
                                final Intent i = new Intent(Intent.ACTION_VIEW);
                                i.setData(Uri.parse(getString(R.string.url_profile, profileUserId)));
                                startActivity(i);
                            } else if (uri.getPath().equals("/wish")) {
                                final String profileUserIdString = uri.getQueryParameter("id");
                                UUID profileUserId = UUID.fromString(profileUserIdString);
                                final Intent i = new Intent(Intent.ACTION_VIEW);
                                i.setData(Uri.parse(getString(R.string.url_wish, profileUserId)));
                                startActivity(i);
                            }
                        }

                    } catch (Exception e) {
                        final Intent i = new Intent(this, UserActivity.class);
                        i.putExtra("text", content);
                        startActivity(i);
                    }
                }
                break;
            }
            default:
                break;
        }
    }

    @Override
    public void onRequestPermissionsResult (
            final int requestCode,
            @NonNull final String[] permissions,
            @NonNull final int[] grantResults
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case 1:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    scanQRCode();
                }
                break;
        }
    }

    @Override
    protected void onDestroy () {
        Log.d(TAG, "onDestroy");
        super.onDestroy();
    }

    @Override
    public void toThanks () {
        Log.d(TAG, "toThanks");
        final NavDirections action = ProfileFragmentDirections.actionProfileFragmentToOperationsFragment(mUserId, mAccount);
        mNavController.navigate(action);
    }

    @Override
    public void toWishes () {
        final Intent intent = WishesActivity.createSelfIntent(this, mUserId);
        startActivity(intent);
    }

    @Override
    public void toAbilities () {
        Toast.makeText(this, getString(R.string.info_msg_function_in_developing), Toast.LENGTH_LONG).show();
    }

}
