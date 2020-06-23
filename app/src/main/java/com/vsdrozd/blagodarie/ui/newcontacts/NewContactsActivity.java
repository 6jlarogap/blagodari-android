package com.vsdrozd.blagodarie.ui.newcontacts;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.view.Menu;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.util.Pair;
import androidx.core.view.GravityCompat;
import androidx.databinding.DataBindingUtil;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.lifecycle.ViewModelProvider;

import com.ex.diagnosticlib.Diagnostic;
import com.google.android.material.navigation.NavigationView;
import com.vsdrozd.blagodarie.BuildConfig;
import com.vsdrozd.blagodarie.R;
import com.vsdrozd.blagodarie.databinding.NewContactsActivityBinding;
import com.vsdrozd.blagodarie.databinding.WarningBadgeBinding;
import com.vsdrozd.blagodarie.db.addent.ContactWithKeyz;
import com.vsdrozd.blagodarie.db.scheme.Contact;
import com.vsdrozd.blagodarie.db.scheme.Keyz;
import com.vsdrozd.blagodarie.db.scheme.KeyzType;
import com.vsdrozd.blagodarie.server.EntityToJsonConverter;
import com.vsdrozd.blagodarie.server.ResponseBodyException;
import com.vsdrozd.blagodarie.server.ResponseException;
import com.vsdrozd.blagodarie.server.ServerException;
import com.vsdrozd.blagodarie.ui.GoogleSignInListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.security.cert.CertificateException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public final class NewContactsActivity
        extends AppCompatActivity {
    /**
     * Название поля Extra для идентификатора пользователя.
     *
     * @link https://developer.android.com/guide/components/intents-filters?hl=ru#Building
     */
    @NonNull
    public static final String EXTRA_USER_ID = "com.vsdrozd.blagodarie.ui.newcontacts.USER_ID";

    /**
     * Идентификатор запроса на разрешение чтения контактов.
     */
    static final int PERMISSION_REQUEST_READ_CONTACTS = 1;

    private NewContactsViewModel mContactsViewModel;

    /**
     * Боковое меню.
     */
    private DrawerLayout mDrawerLayout;

    private Long mUserId;

    @Override
    protected void onCreate (@Nullable final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.mUserId = getIntent().getLongExtra(EXTRA_USER_ID, -1L);

        initViewModel();

        ((NewContactsActivityBinding) DataBindingUtil.setContentView(this, R.layout.new_contacts_activity)).setViewModel(mContactsViewModel);

        setupToolbar();

        setupNavigationDrawer();

        setupSignInListener();

        //setupSnackbar();


        //mContactsViewModel.contacts.observe(this, input -> mContactsViewModel.existsContacts.set(!input.isEmpty()));

        NewContactsAdapter newContactsAdapter = new NewContactsAdapter();
        //binding.rvContacts.setAdapter(newContactsAdapter);

        //mContactsViewModel.contacts.observe(this, newContactsAdapter::setData);

    }

    private void initViewModel(){
        //создаем фабрику
        final NewContactsViewModel.Factory factory = new NewContactsViewModel.Factory(
        );

        //создаем ContactsViewModel
        this.mContactsViewModel = new ViewModelProvider(this, factory).get(NewContactsViewModel.class);
    }

    @Override
    public boolean onCreateOptionsMenu (@NonNull Menu menu) {
        Diagnostic.i();
        getMenuInflater().inflate(R.menu.activity_contacts, menu);
        return true;
    }

    private void setupToolbar () {
        Diagnostic.i();
        setSupportActionBar(findViewById(R.id.toolbar));
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setHomeAsUpIndicator(R.drawable.ic_menu);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }
    private void setupSignInListener () {
        Diagnostic.i();
        findViewById(R.id.btnSignIn).setOnClickListener(view -> GoogleSignInListener.getInstance().signIn(this));
    }

    @Override
    public boolean onOptionsItemSelected (MenuItem item) {
        Diagnostic.i();
        switch (item.getItemId()) {
            case android.R.id.home:
                this.mDrawerLayout.openDrawer(GravityCompat.START);
                return true;
            case R.id.miShare:
                //onShare();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void setupNavigationDrawer () {
        Diagnostic.i();
        this.mDrawerLayout = findViewById(R.id.drawer_layout);
        this.mDrawerLayout.setStatusBarBackground(R.color.colorPrimaryDark);
        final NavigationView navigationView = findViewById(R.id.nvNavigation);

        /*final MenuItem warningsMenuItem = navigationView.getMenu().findItem(R.id.miWarnings);
        final WarningBadgeBinding warningBadgeBinding = WarningBadgeBinding.bind(warningsMenuItem.getActionView());
        warningBadgeBinding.setViewModel(mContactsViewModel);*/

        if (BuildConfig.DEBUG) {
            navigationView.getMenu().findItem(R.id.miSendLog).setVisible(true);
        }

        navigationView.setNavigationItemSelectedListener(menuItem -> {
            switch (menuItem.getItemId()) {
                case R.id.miWarnings:
                    //toWarnings();
                    break;
                case R.id.miCommunication:
                    //communicate();
                    break;
                case R.id.miCheckForUpdates:
                    //checkLatestVersion(true);
                    break;
                case R.id.miAbout:
                    //showAbout();
                    break;
                case R.id.miShare:
                    //onShare();
                    return true;
                case R.id.miSendLog:
                    Diagnostic.sendLog(this, BuildConfig.APPLICATION_ID);
                    return true;
                default:
                    break;
            }
            mDrawerLayout.closeDrawers();
            return true;
        });
    }

    public static Intent createIntent (
            @NonNull final Context context,
            @NonNull final Long userId
    ) {
        final Intent intent = new Intent(context, NewContactsActivity.class);
        intent.putExtra(EXTRA_USER_ID, userId);
        return intent;
    }
}
