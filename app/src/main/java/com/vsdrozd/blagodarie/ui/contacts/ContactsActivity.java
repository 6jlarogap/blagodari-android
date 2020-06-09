package com.vsdrozd.blagodarie.ui.contacts;

import android.Manifest;
import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.databinding.DataBindingUtil;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.ex.diagnosticlib.Diagnostic;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.snackbar.Snackbar;
import com.vsdrozd.blagodarie.AccountGeneral;
import com.vsdrozd.blagodarie.BuildConfig;
import com.vsdrozd.blagodarie.R;
import com.vsdrozd.blagodarie.contacts.ContactRepository;
import com.vsdrozd.blagodarie.databinding.ContactsActivityBinding;
import com.vsdrozd.blagodarie.databinding.WarningBadgeBinding;
import com.vsdrozd.blagodarie.server.ServerSynchronizer;
import com.vsdrozd.blagodarie.server.api.Api;
import com.vsdrozd.blagodarie.server.api.GetLatestVersion;
import com.vsdrozd.blagodarie.ui.BaseActivity;
import com.vsdrozd.blagodarie.ui.GoogleSignInListener;
import com.vsdrozd.blagodarie.ui.contactdetail.ContactDetailActivity;

import java.io.File;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;


public final class ContactsActivity
        extends BaseActivity<ContactsViewModel>
        implements ContactItemClickListener, ContactsUserActionListener {

    /**
     * Название поля Extra для идентификатора пользователя.
     *
     * @link https://developer.android.com/guide/components/intents-filters?hl=ru#Building
     */
    @NonNull
    public static final String EXTRA_USER_ID = "com.vsdrozd.blagodarie.ui.contacts.USER_ID";

    /**
     * Название поля в настройках для сохранения порядка сортировки.
     */
    @NonNull
    static final String PREF_ORDER_BY = "orderBy";

    /**
     * Идентификатор запроса на разрешение чтения контактов.
     */
    static final int PERMISSION_REQUEST_READ_CONTACTS = 1;

    /**
     * Боковое меню.
     */
    private DrawerLayout mDrawerLayout;

    /**
     * Источник контактов.
     */
    private ContactRepository mContactRepository;


    private final CompositeDisposable mDisposables = new CompositeDisposable();
    private NavController navController;

    private long downloadID;
    private BroadcastReceiver onDownloadComplete = new BroadcastReceiver() {
        @Override
        public void onReceive (Context context, Intent intent) {
            long id = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1);
            if (downloadID == id) {
                getViewModel().getInfoMessage().setValue("Загрузка завершена");
            }
        }
    };

    /**
     * Вызывается перед первым созданием Activity.
     *
     * @param savedInstanceState Данные после предыдущего выключения.
     */
    @Override
    protected final void onCreate (Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Diagnostic.i();

        initContactRepository();

        ((ContactsActivityBinding) DataBindingUtil.setContentView(this, R.layout.contacts_activity)).setViewModel(getViewModel());

        navController = Navigation.findNavController(this, R.id.nav_host_fragment);

        setupToolbar();

        setupNavigationDrawer();

        setupSignInListener();

        setupSnackbar();

        registerReceiver(onDownloadComplete, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));

    }

    private void initContactRepository () {
        final Account account = new Account(AccountGeneral.ACCOUNT_NAME, AccountGeneral.ACCOUNT_TYPE);
        final AccountManager accountManager = (AccountManager) getApplicationContext().getSystemService(ACCOUNT_SERVICE);
        this.mContactRepository = new ContactRepository(getContentResolver(), accountManager, account);
    }

    @Override
    protected final ContactsViewModel createViewModel () {
        //получаем идентификатор пользователя из Intent
        final Long userId = getIntent().getLongExtra(EXTRA_USER_ID, -1L);

        /*получаем из сохраненных настроек порядок сортировки контактов,
        если в настройках нет записи о порядке - сортируем по умолчанию*/
        final ContactsOrder contactsOrder = ContactsOrder.
                valueOf(
                        getSharedPreferences("pref", MODE_PRIVATE).
                                getString(PREF_ORDER_BY, ContactsOrder.getDefault().name())
                );

        //создаем фабрику
        final ContactsViewModel.Factory factory = new ContactsViewModel.Factory(
                getDataRepository(),
                userId,
                contactsOrder
        );

        //создаем ContactsViewModel
        final ContactsViewModel contactsViewModel = new ViewModelProvider(this, factory).get(ContactsViewModel.class);

        contactsViewModel.getContacts().observe(this, contacts -> contactsViewModel.mEmptyContacts.set(contacts.isEmpty()));
        contactsViewModel.getWarnings().observe(this, warnings -> contactsViewModel.mWarningsCount.set(warnings.size()));

        return contactsViewModel;
    }

    @Override
    public void onStart () {
        super.onStart();
        Diagnostic.i();
        checkPermissionReadContacts();
        ServerSynchronizer.getInstance().addApiListener(getViewModel().getServerSynchronizerListener());
        ServerSynchronizer.getInstance().start(getDataRepository(), getViewModel().getUserId());
        checkLatestVersion(false);
    }

    @Override
    protected void onStop () {
        super.onStop();
        ServerSynchronizer.getInstance().removeApiListener(getViewModel().getServerSynchronizerListener());
        ServerSynchronizer.getInstance().stop();
    }

    @Override
    protected void onDestroy () {
        super.onDestroy();
        Diagnostic.i();
        ContactsChangeObserver.getInstance().unregister(getContentResolver());
        this.mDisposables.dispose();
    }

    @Override
    protected void onActivityResult (int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Diagnostic.i();
        if (requestCode == GoogleSignInListener.ACTIVITY_REQUEST_CODE_GOGGLE_SIGN_IN) {
            GoogleSignInListener.getInstance().handleSignInResult(data, getViewModel().getUserId(), getDataRepository());
        }
    }

    @Override
    public void onRequestPermissionsResult (int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        Diagnostic.i();
        if (requestCode == PERMISSION_REQUEST_READ_CONTACTS) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                registerContactsObserverAndSynchronizeContacts();
            }
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
                mDrawerLayout.openDrawer(GravityCompat.START);
                return true;
            case R.id.miShare:
                onShare();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void setupNavigationDrawer () {
        Diagnostic.i();
        mDrawerLayout = findViewById(R.id.drawer_layout);
        mDrawerLayout.setStatusBarBackground(R.color.colorPrimaryDark);
        final NavigationView navigationView = findViewById(R.id.nvNavigation);

        final MenuItem warningsMenuItem = navigationView.getMenu().findItem(R.id.miWarnings);
        final WarningBadgeBinding warningBadgeBinding = WarningBadgeBinding.bind(warningsMenuItem.getActionView());
        warningBadgeBinding.setViewModel(getViewModel());

        if (BuildConfig.DEBUG) {
            navigationView.getMenu().findItem(R.id.miSendLog).setVisible(true);
        }

        navigationView.setNavigationItemSelectedListener(menuItem -> {
            switch (menuItem.getItemId()) {
                case R.id.miWarnings:
                    toWarnings();
                    break;
                case R.id.miCommunication:
                    communicate();
                    break;
                case R.id.miCheckForUpdates:
                    checkLatestVersion(true);
                    break;
                case R.id.miAbout:
                    showAbout();
                    break;
                case R.id.miShare:
                    onShare();
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

    private void communicate () {
        final Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("message/rfc822");
        intent.putExtra(Intent.EXTRA_EMAIL, new String[]{"blagodarie.developer@gmail.com"});
        intent.putExtra(Intent.EXTRA_SUBJECT, "Обратная связь");
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivity(intent);
        }
    }

    private void showAbout () {
        final View messageView = DataBindingUtil.inflate(getLayoutInflater(), R.layout.about, null, false).getRoot();
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.app_name);
        builder.setView(messageView);
        builder.create();
        builder.show();
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

    private void checkPermissionReadContacts () {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
            Diagnostic.i("Request permission");
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_CONTACTS, Manifest.permission.WRITE_CONTACTS}, PERMISSION_REQUEST_READ_CONTACTS);
        } else {
            Diagnostic.i("Permission granted");
            registerContactsObserverAndSynchronizeContacts();
        }
    }

    private void registerContactsObserverAndSynchronizeContacts () {
        ContactsChangeObserver.getInstance().registerIfNeedAndSynchronizeContactsIfNeed(
                getViewModel().getUserId(),
                getContentResolver(),
                this.mContactRepository,
                getDataRepository(),
                getViewModel().createContactSynchronizerListener(
                        getString(R.string.on_get_data),
                        getString(R.string.on_processing_contacts),
                        getString(R.string.on_database_write),
                        getString(R.string.on_contact_repository_write)
                )
        );
    }


    private void setupSnackbar () {
        Diagnostic.i();
        getViewModel().getInfoMessage().observe(this, s -> Snackbar.make(findViewById(android.R.id.content), s, Snackbar.LENGTH_LONG).show());
    }

    @Override
    public void onContactItemClick (
            @NonNull final View view,
            @NonNull final Long contactId
    ) {
        Diagnostic.i();
        Bundle bundle = new Bundle();
        bundle.putLong(ContactDetailActivity.EXTRA_USER_ID, getViewModel().getUserId());
        bundle.putLong(ContactDetailActivity.EXTRA_CONTACT_ID, contactId);
        Navigation.findNavController(view).navigate(R.id.action_contactsFragment_to_profileActivity, bundle);
    }

    @Override
    public void onBackPressed () {
        Diagnostic.i();
        finish();
    }

    @Override
    public void onShare () {
        final Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_SEND);
        sendIntent.putExtra(Intent.EXTRA_TEXT, BuildConfig.DEBUG ? "Приложение Благодарие, скачивай отсюда - https://api.dev.благодарие.рф/media/download/apk/blagodarie-" + BuildConfig.VERSION_NAME + "-debug.apk" : "Приложение Благодарие, скачивай отсюда - https://api.благодарие.рф/media/download/apk/blagodarie-" + BuildConfig.VERSION_NAME + "-release.apk");
        sendIntent.putExtra(Intent.EXTRA_SUBJECT, "Приглашение в Благодарие");
        sendIntent.setType("text/plain");
        startActivity(Intent.createChooser(sendIntent, "Поделиться"));
    }

    void checkLatestVersion (boolean showIsActualVersion) {
        Diagnostic.i();
        this.mDisposables.add(
                Observable.fromCallable(() -> GetLatestVersion.getInstance().execute(new Api.DataIn()))
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(input -> {
                            if (input.getVersionCode() > BuildConfig.VERSION_CODE) {
                                showNewVersionDialog(input.getVersionName(), input.getUrl());
                            } else if (showIsActualVersion) {
                                getViewModel().getInfoMessage().setValue(getApplication().getString(R.string.txt_no_updates));
                            }
                        })
        );
    }

    private void showNewVersionDialog (String versionName, String url) {
        Diagnostic.i();
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.txt_update_available);
        builder.setMessage(String.format(getString(R.string.txt_want_load_new_version), versionName));
        builder.setPositiveButton(R.string.btn_yes, (dialog, which) -> loadNewVersion(versionName, url));
        builder.setNegativeButton(R.string.btn_no, null);
        builder.create();
        builder.show();
    }

    private void loadNewVersion (String versionName, String url) {
        Diagnostic.i("url", url);
        File file = new File(getExternalFilesDir(null), "Dummy");
        /*
        Create a DownloadManager.Request with all the information necessary to start the download
         */
        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url))
                .setTitle(String.format("%s %s.apk", getApplication().getString(R.string.app_name), versionName))// Title of the Download Notification
                .setDescription("Загрузка")// Description of the Download Notification
                .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)// Visibility of the download Notification
                .setDestinationUri(Uri.fromFile(file))// Uri of the destination file
                .setAllowedOverMetered(true)// Set if download is allowed on Mobile network
                .setAllowedOverRoaming(true);// Set if download is allowed on roaming network
        DownloadManager downloadManager = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);
        downloadID = downloadManager.enqueue(request);// enqueue puts the download request in the queue.

    }

    private void toWarnings () {
        navController.navigate(R.id.action_contactsFragment_to_warningActivity);
    }

    public static Intent createIntent (
            @NonNull final Context context,
            @NonNull final Long userId
    ) {
        final Intent intent = new Intent(context, ContactsActivity.class);
        intent.putExtra(ContactsActivity.EXTRA_USER_ID, userId);
        return intent;
    }
}
