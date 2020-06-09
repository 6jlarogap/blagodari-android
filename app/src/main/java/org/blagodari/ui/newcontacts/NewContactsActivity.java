package org.blagodari.ui.newcontacts;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.databinding.DataBindingUtil;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.lifecycle.ViewModelProvider;

import com.ex.diagnosticlib.Diagnostic;
import com.google.android.material.navigation.NavigationView;
import org.blagodari.BuildConfig;
import org.blagodari.R;
import org.blagodari.databinding.NewContactsActivityBinding;

import org.blagodari.ui.GoogleSignInListener;

public final class NewContactsActivity
        extends AppCompatActivity {
    /**
     * Название поля Extra для идентификатора пользователя.
     *
     * @link https://developer.android.com/guide/components/intents-filters?hl=ru#Building
     */
    @NonNull
    public static final String EXTRA_USER_ID = "org.blagodarie.ui.newcontacts.USER_ID";

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
