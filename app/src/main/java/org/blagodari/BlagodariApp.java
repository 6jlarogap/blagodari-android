package org.blagodari;

import android.app.Application;
import android.content.Intent;
import android.net.Uri;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import androidx.core.content.FileProvider;

import com.ex.diagnosticlib.Diagnostic;

import org.blagodari.db.BlagodariDatabase;

import java.io.File;

public final class BlagodariApp
        extends Application {

    private static DataRepository mDataRepository;

    @Override
    public void onCreate () {
        super.onCreate();

        Diagnostic.init(this, getSharedPreferences("pref", MODE_PRIVATE).getBoolean("isLog", true), BuildConfig.DEBUG);
        Diagnostic.i("start app");

        Thread.setDefaultUncaughtExceptionHandler(this::handleUncaughtException);

    }

    public BlagodariDatabase getDatabase () {
        return BlagodariDatabase.getInstance(this);
    }

    public DataRepository getRepository () {
        if (mDataRepository == null) {
            mDataRepository = new DataRepository(getDatabase());
        }
        return mDataRepository;
    }

    public void handleUncaughtException (Thread thread, Throwable e) {
        Diagnostic.e(e);
        new Thread() {
            @Override
            public void run () {
                Looper.prepare();
                Toast.makeText(getApplicationContext(), R.string.crash_toast_text, Toast.LENGTH_SHORT).show();
                Looper.loop();
            }
        }.start();

        final String stackTrace = Log.getStackTraceString(e);
        final String message = e.getMessage();

        final Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("vnd.android.cursor.dir/email2");
        intent.putExtra(Intent.EXTRA_EMAIL, new String[]{"blagodarie.developer@gmail.com"});
        intent.putExtra(Intent.EXTRA_SUBJECT, "Crash: " + message);
        intent.putExtra(Intent.EXTRA_TEXT, stackTrace);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        File logfile = new File(this.getFilesDir(), "logfile.log");
        Uri uri = FileProvider.getUriForFile(getApplicationContext(), BuildConfig.APPLICATION_ID + ".fileprovider", logfile);

        intent .putExtra(Intent.EXTRA_STREAM, uri);

        startActivity(intent);

    }
}
