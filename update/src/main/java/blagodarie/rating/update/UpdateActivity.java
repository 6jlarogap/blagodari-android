package blagodarie.rating.update;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.ViewModelProvider;

import java.io.File;

import blagodarie.rating.update.databinding.UpdateActivityBinding;

public final class UpdateActivity
        extends AppCompatActivity
        implements UpdateLoader.ProgressListener {

    private static final String TAG = UpdateActivity.class.getSimpleName();

    private static final String FILE_BASE_PATH = "file://";

    private static final String EXTRA_LATEST_VERSION_NAME = "blagodarie.health.ui.update.LATEST_VERSION_NAME";
    private static final String EXTRA_LATEST_VERSION_URI = "blagodarie.health.ui.update.LATEST_VERSION_URI";
    private static final String EXTRA_FILE_PROVIDER_AUTHORITIES = "blagodarie.health.ui.update.FILE_PROVIDER_AUTHORITIES";

    private UpdateViewModel mViewModel;

    private String mLatestVersionName;

    private Uri mLatestVersionUri;

    private String mFileProviderAuthorities;

    private String mFileName;

    private File mFile;

    @Override
    protected void onCreate (@Nullable final Bundle savedInstanceState) {
        Log.d(TAG, "onCreate");
        super.onCreate(savedInstanceState);

        mLatestVersionName = getIntent().getStringExtra(EXTRA_LATEST_VERSION_NAME);
        mLatestVersionUri = getIntent().getParcelableExtra(EXTRA_LATEST_VERSION_URI);
        mFileProviderAuthorities = getIntent().getStringExtra(EXTRA_FILE_PROVIDER_AUTHORITIES);
        mFileName = getString(R.string.file_name, mLatestVersionName);
        mFile = new File(getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), mFileName);

        initViewModel();

        final UpdateActivityBinding mainActivityBinding = DataBindingUtil.setContentView(this, R.layout.update_activity);
        mainActivityBinding.setViewModel(mViewModel);

        UpdateLoader.getInstance().setProgressListener(this);

        if (mFile.exists()) {
            if (UpdateLoader.getInstance().getDownloadStatus() != UpdateLoader.DownloadStatus.RUN) {
                startInstall();
            }
        } else {
            UpdateLoader.getInstance().startDownload(
                    getApplicationContext(),
                    mFile,
                    mLatestVersionUri
            );
        }
    }

    private void initViewModel () {
        Log.d(TAG, "initViewModel");
        //создаем фабрику
        final UpdateViewModel.Factory factory = new UpdateViewModel.Factory(mLatestVersionName);

        //создаем UpdateViewModel
        mViewModel = new ViewModelProvider(this, factory).get(UpdateViewModel.class);
    }

    private void startInstall () {
        Log.d(TAG, "startInstall");
        final File externalFilesDir = getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS);
        String destination = null;
        if (externalFilesDir != null) {
            destination = externalFilesDir.toString() + "/" + mFileName;
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            Uri contentUri = FileProvider.getUriForFile(
                    this,
                    mFileProviderAuthorities,
                    new File(destination)
            );
            final Intent install = new Intent(Intent.ACTION_VIEW);
            install.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            install.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            install.putExtra(Intent.EXTRA_NOT_UNKNOWN_SOURCE, true);
            install.setData(contentUri);
            startActivity(install);
        } else {
            final Uri uri = Uri.parse(FILE_BASE_PATH + destination);
            final Intent install = new Intent(Intent.ACTION_VIEW);
            install.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            install.setDataAndType(
                    uri,
                    UpdateLoader.MIME_TYPE
            );
            startActivity(install);
        }
        finish();
    }

    private void showRepeatDialog () {
        Log.d(TAG, "showRepeatDialog");
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        /*builder.setTitle(R.string.info_msg_download_failed);
        builder.setMessage(getString(R.string.qstn_want_repeat));
        builder.setPositiveButton(R.string.btn_repeat, (dialog, which) -> UpdateLoader.getInstance().startDownload(
                getApplicationContext(),
                mFile,
                mLatestVersionUri
        ));
        builder.setNegativeButton(R.string.btn_finish, (dialog, which) -> finish());*/
        builder.create();
        builder.show();
    }

    public static Intent createSelfIntent (
            @NonNull final Context context,
            @NonNull final String fileProviderAuthorities,
            @NonNull final String versionName,
            @NonNull final Uri latestVersionUri
    ) {
        Log.d(TAG, "createSelfIntent");
        final Intent intent = new Intent(context, UpdateActivity.class);
        intent.putExtra(EXTRA_FILE_PROVIDER_AUTHORITIES, fileProviderAuthorities);
        intent.putExtra(EXTRA_LATEST_VERSION_NAME, versionName);
        intent.putExtra(EXTRA_LATEST_VERSION_URI, latestVersionUri);
        return intent;
    }

    @Override
    public void onNext (long total, long downloaded) {
        Log.d(TAG, "onNext");
        mViewModel.getTotalBytes().set((float) total / 1000000F);
        mViewModel.getDownloadedBytes().set((float) downloaded / 1000000F);
        mViewModel.getProgress().set((int) ((downloaded * 100L) / total));
    }

    @Override
    public void onSuccess () {
        Log.d(TAG, "onSuccess");
        mViewModel.getProgress().set(100);
        mViewModel.getDownloadedBytes().set(mViewModel.getTotalBytes().get());
        startInstall();
    }

    @Override
    public void onFail () {
        Log.d(TAG, "onFail");
        showRepeatDialog();
    }
}
