package blagodarie.rating.update;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;

import com.google.android.play.core.appupdate.AppUpdateInfo;
import com.google.android.play.core.appupdate.AppUpdateManager;
import com.google.android.play.core.appupdate.AppUpdateManagerFactory;
import com.google.android.play.core.tasks.Task;

import blagodarie.rating.server.GetRatingLatestVersionRequest;
import blagodarie.rating.server.GetRatingLatestVersionResponse;
import blagodarie.rating.server.ServerApiClient;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public enum UpdateManager {

    INSTANCE;

    private static final String TAG = UpdateManager.class.getSimpleName();

    private static final String NEW_VERSION_NOTIFICATION_PREFERENCE = "blagodarie.rating.update.UpdateManager.newVersionNotification";

    @FunctionalInterface
    public interface OnCheckUpdateListener {
        void onHaveUpdate ();
    }

    @FunctionalInterface
    public interface OnErrorListener {
        void onError (@NonNull final Throwable throwable);
    }

    private GetRatingLatestVersionResponse mLastResponse;

    private Integer mLastVersionCodeOnMarket;

    UpdateManager (
    ) {
    }

    public Disposable checkUpdate (
            @NonNull final Context context,
            final int currentCodeVersion,
            @NonNull final OnCheckUpdateListener onCheckUpdateListener,
            @NonNull final OnErrorListener onErrorListener
    ) {
        final ServerApiClient client = new ServerApiClient();
        final GetRatingLatestVersionRequest request = new GetRatingLatestVersionRequest();
        return Observable.
                fromCallable(() -> client.execute(request)).
                subscribeOn(Schedulers.io()).
                observeOn(AndroidSchedulers.mainThread()).
                subscribe(
                        response -> {
                            mLastResponse = response;
                            handleCheckUpdateResponse(context, currentCodeVersion, onCheckUpdateListener);
                        },
                        onErrorListener::onError
                );
    }

    private void handleCheckUpdateResponse (
            @NonNull final Context context,
            final int currentCodeVersion,
            @NonNull final OnCheckUpdateListener onCheckUpdateListener
    ) {
        if (!mLastResponse.isRatingGooglePlayUpdate()) {
            if (mLastResponse.getVersionCode() > currentCodeVersion) {
                onCheckUpdateListener.onHaveUpdate();
                if (!context.getSharedPreferences(NEW_VERSION_NOTIFICATION_PREFERENCE, Context.MODE_PRIVATE).contains(String.valueOf(mLastResponse.getVersionCode()))) {
                    showUpdateDialog(
                            context,
                            (dialogInterface, i) -> toUpdate(context)
                    );
                }
            }
        } else {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                final AppUpdateManager appUpdateManager = AppUpdateManagerFactory.create(context);

                final Task<AppUpdateInfo> appUpdateInfoTask = appUpdateManager.getAppUpdateInfo();

                appUpdateInfoTask.addOnSuccessListener(appUpdateInfo -> {
                    Log.d(TAG, "appUpdateInfo=" + appUpdateInfo.toString());
                    mLastVersionCodeOnMarket = appUpdateInfo.availableVersionCode();
                    if (appUpdateInfo.availableVersionCode() > BuildConfig.VERSION_CODE) {
                        if (!context.getSharedPreferences(NEW_VERSION_NOTIFICATION_PREFERENCE, Context.MODE_PRIVATE).contains(String.valueOf(appUpdateInfo.availableVersionCode()))) {
                            showUpdateDialog(
                                    context,
                                    (dialogInterface, i) -> toUpdate(context)
                            );
                        }
                    }
                });
            }
        }
    }

    public void toUpdate (
            @NonNull final Context context
    ) {
        if (mLastResponse.isRatingGooglePlayUpdate()) {
            final Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse(context.getString(R.string.url_play_market)));
            context.startActivity(intent);
        } else {
            startUpdate(context);
        }
    }

    private void showUpdateDialog (
            @NonNull final Context context,
            @NonNull final DialogInterface.OnClickListener onOkClickListener
    ) {
        new AlertDialog.
                Builder(context).
                setTitle(R.string.info_msg_update_available).
                setMessage(context.getString(R.string.qstn_want_load_new_version, mLastResponse.isRatingGooglePlayUpdate() ? mLastVersionCodeOnMarket : mLastResponse.getVersionCode())).
                setPositiveButton(
                        R.string.btn_update,
                        onOkClickListener).
                setNegativeButton(
                        android.R.string.cancel,
                        null).
                create().
                show();
        context.getSharedPreferences(NEW_VERSION_NOTIFICATION_PREFERENCE, Context.MODE_PRIVATE).
                edit().
                putInt(String.valueOf(mLastResponse.getVersionCode()), mLastResponse.getVersionCode()).
                apply();
    }

    private void startUpdate (
            @NonNull final Context context
    ) {
        final Intent intent = UpdateActivity.createSelfIntent(context, mLastResponse.getVersionName(), Uri.parse(mLastResponse.getPath()));
        context.startActivity(intent);
    }
}
