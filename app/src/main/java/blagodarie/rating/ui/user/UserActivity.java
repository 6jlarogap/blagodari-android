package blagodarie.rating.ui.user;

import android.Manifest;
import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.Context;
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
import androidx.appcompat.app.AlertDialog;
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

import blagodarie.rating.BuildConfig;
import blagodarie.rating.R;
import blagodarie.rating.auth.AccountGeneral;
import blagodarie.rating.databinding.NavHeaderLayoutBinding;
import blagodarie.rating.databinding.UserActivityBinding;
import blagodarie.rating.ui.AccountProvider;
import blagodarie.rating.ui.splash.SplashActivity;
import blagodarie.rating.ui.user.anytext.AnyTextFragment;
import blagodarie.rating.ui.user.anytext.AnyTextFragmentDirections;
import blagodarie.rating.ui.user.profile.ProfileFragment;
import blagodarie.rating.ui.user.profile.ProfileFragmentDirections;
import blagodarie.rating.update.NewVersionInfo;
import blagodarie.rating.update.UpdateManager;
import io.reactivex.disposables.CompositeDisposable;

public final class UserActivity
        extends AppCompatActivity
        implements ProfileFragment.FragmentCommunicator,
        AnyTextFragment.FragmentCommunicator,
        UpdateManager.OnCheckUpdateListener {

    private static final String TAG = UserActivity.class.getSimpleName();

    private static final String EXTRA_ANY_TEXT = "blagodarie.rating.ui.user.UserActivity.ANY_TEXT";

    private static final String NEW_VERSION_NOTIFICATION_PREFERENCE = "blagodarie.rating.ui.user.UserActivity.newVersionNotification";

    private AccountManager mAccountManager;

    private Account mAccount;

    private UUID mUserId;

    private String mAnyText;

    private UserViewModel mViewModel;

    private UserActivityBinding mActivityBinding;

    private DrawerLayout mDrawerLayout;

    private NavController mNavController;

    private AppCompatImageView ivOwnAccountPhoto;

    private CompositeDisposable mDisposables = new CompositeDisposable();

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
        } else {
            mAnyText = getIntent().getStringExtra(EXTRA_ANY_TEXT);
        }

        if (mUserId != null || mAnyText != null) {
            initViewModel();
            initBinding();
            mNavController = Navigation.findNavController(this, R.id.nav_host_fragment);
            setupToolbar();
            setupNavigationDrawer();
            AccountProvider.getAccount(
                    this,
                    this::onAccountSelected
            );
            mDisposables.add(
                    UpdateManager.checkUpdate(
                            BuildConfig.VERSION_CODE,
                            this,
                            throwable -> Toast.makeText(this, throwable.getLocalizedMessage(), Toast.LENGTH_LONG).show()
                    )
            );
        } else {
            Toast.makeText(this, R.string.err_msg_missing_data, Toast.LENGTH_LONG).show();
            finish();
        }
    }

    @Override
    public void onHaveUpdate (@NonNull final NewVersionInfo newVersionInfo) {
        Log.d(TAG, "onHaveUpdate");

        if (!getSharedPreferences(NEW_VERSION_NOTIFICATION_PREFERENCE, Context.MODE_PRIVATE).contains(newVersionInfo.getVersionName())) {
            new AlertDialog.
                    Builder(this).
                    setTitle(R.string.info_msg_update_available).
                    setMessage(String.format(getString(R.string.qstn_want_load_new_version), newVersionInfo.getVersionName())).
                    setPositiveButton(
                            R.string.btn_update,
                            (dialogInterface, i) -> UpdateManager.startUpdate(
                                    this,
                                    getString(R.string.file_provider_authorities),
                                    newVersionInfo)).
                    setNegativeButton(android.R.string.cancel, null).
                    create().
                    show();
            getSharedPreferences(NEW_VERSION_NOTIFICATION_PREFERENCE, Context.MODE_PRIVATE).
                    edit().
                    putInt(newVersionInfo.getVersionName(), newVersionInfo.getVersionCode()).
                    apply();
        }
    }

    @Override
    public void onNothingUpdate () {
        Log.d(TAG, "onNothingUpdate");
        //do nothing
    }

    @Override
    public void onUpdateFromMarket () {
        Log.d(TAG, "onUpdateFromMarket");
        //do nothing
    }

    @Override
    public void onBackPressed () {
        Log.d(TAG, "onBackPressed");
        if (mNavController.getCurrentDestination() != null) {
            if (mNavController.getCurrentDestination().getId() == R.id.profileFragment ||
                    mNavController.getCurrentDestination().getId() == R.id.anyTextFragment) {
                finish();
            } else {
                super.onBackPressed();
            }
        }
    }

    public void onAccountSelected (@Nullable final Account account) {
        Log.d(TAG, "onAccountSelected account=" + (account != null ? account.toString() : "null"));
        if (account != null) {
            mAccount = account;
            mViewModel.getOwnAccountPhotoUrl().setValue(mAccountManager.getUserData(mAccount, AccountGeneral.USER_DATA_PHOTO));
        }
        mActivityBinding.nvNavigation.getMenu().findItem(R.id.miLogout).setEnabled(mAccount != null);
        mViewModel.isOwnProfile().set(mAccount != null && mUserId != null && mAccount.name.equals(mUserId.toString()));
        if (mUserId != null) {
            toProfile();
        } else if (mAnyText != null) {
            toAnyText();
        }
    }

    void toProfile () {
        Log.d(TAG, "toProfile");
        final NavDirections action = StartFragmentDirections.actionStartFragment2ToProfileFragment(mUserId, mAccount);
        mNavController.navigate(action);
    }

    void toAnyText () {
        Log.d(TAG, "toAnyText");
        final NavDirections action = StartFragmentDirections.actionStartFragment2ToAnyTextFragment(mAnyText, mAccount);
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
                if (mViewModel.isOwnProfile().get()) {
                    if (mNavController.getCurrentDestination() != null) {
                        if (mNavController.getCurrentDestination().getId() == R.id.profileFragment) {
                            ((ProfileFragment) getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment).getChildFragmentManager().getFragments().get(0)).refreshProfileData();
                        } else {
                            onBackPressed();
                        }
                    }
                } else {
                    final Intent i = new Intent(Intent.ACTION_VIEW);
                    i.setData(Uri.parse(getString(R.string.url_profile, mAccountManager.getUserData(mAccount, AccountGeneral.USER_DATA_USER_ID))));
                    startActivity(i);
                }
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
                final IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
                if (result.getContents() != null) {
                    final String content = result.getContents();
                    final Uri uri = Uri.parse(content);
                    if (uri.getHost() != null &&
                            uri.getHost().equals(getString(R.string.host)) &&
                            uri.getPath() != null &&
                            (uri.getPath().equals("/profile") || uri.getPath().equals("/wish")) &&
                            uri.getQueryParameter("id") != null) {
                        final String idString = uri.getQueryParameter("id");

                        try {
                            final UUID id = UUID.fromString(idString);

                            if (uri.getPath().equals("/profile")) {
                                final Intent i = new Intent(Intent.ACTION_VIEW);
                                i.setData(Uri.parse(getString(R.string.url_profile, id)));
                                startActivity(i);
                            } else if (uri.getPath().equals("/wish")) {
                                final Intent i = new Intent(Intent.ACTION_VIEW);
                                i.setData(Uri.parse(getString(R.string.url_wish, id)));
                                startActivity(i);
                            }
                        } catch (IllegalArgumentException e) {
                            toAnyText(content);
                        }
                    } else {
                        toAnyText(content);
                    }
                }

            }
            break;
        }
    }


    private void toAnyText (@NonNull final String text) {
        final Intent i = new Intent(this, UserActivity.class);
        i.putExtra(EXTRA_ANY_TEXT, text);
        startActivity(i);
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
        mDisposables.clear();
    }

    @Override
    public void toOperationsFromProfile () {
        Log.d(TAG, "toOperationsFromProfile");
        final NavDirections action = ProfileFragmentDirections.actionProfileFragmentToOperationsFragment(mUserId, null, mAccount);
        mNavController.navigate(action);
    }

    @Override
    public void toOperationsFromAnyText (@NonNull final UUID anyTextId) {
        Log.d(TAG, "toOperationsFromAnyText");
        final NavDirections action = AnyTextFragmentDirections.actionAnyTextFragmentToOperationsFragment(mUserId, anyTextId, mAccount);
        mNavController.navigate(action);
    }

    @Override
    public void toWishes () {
        Log.d(TAG, "toWishes");
        final NavDirections action = ProfileFragmentDirections.actionProfileFragmentToWishesFragment(mUserId, mAccount);
        mNavController.navigate(action);
    }

    @Override
    public void toAbilities () {
        Toast.makeText(this, getString(R.string.info_msg_function_in_developing), Toast.LENGTH_LONG).show();
    }

}
