package blagodarie.rating.update;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import androidx.annotation.NonNull;

import blagodarie.rating.server.GetRatingLatestVersionRequest;
import blagodarie.rating.server.GetRatingLatestVersionResponse;
import blagodarie.rating.server.ServerApiClient;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public final class UpdateManager {

    public interface OnCheckUpdateListener {
        void onHaveUpdate (@NonNull final NewVersionInfo newVersionInfo);

        void onNothingUpdate ();

        void onUpdateFromMarket ();
    }

    @FunctionalInterface
    public interface OnErrorListener {
        void onError (@NonNull final Throwable throwable);
    }

    public static Disposable checkUpdate (
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
                        response -> handleCheckUpdateResponse(currentCodeVersion, response, onCheckUpdateListener),
                        onErrorListener::onError
                );
    }

    private static void handleCheckUpdateResponse (
            final int currentCodeVersion,
            @NonNull final GetRatingLatestVersionResponse response,
            @NonNull final OnCheckUpdateListener onCheckUpdateListener
    ) {
        if (!response.isRatingGooglePlayUpdate()) {
            if (response.getVersionCode() > currentCodeVersion) {
                onCheckUpdateListener.onHaveUpdate(new NewVersionInfo(response.getVersionCode(), response.getVersionName(), Uri.parse(response.getPath())));
            } else {
                onCheckUpdateListener.onNothingUpdate();
            }
        } else {
            onCheckUpdateListener.onUpdateFromMarket();
        }
    }

    public static void startUpdate (
            @NonNull final Context context,
            @NonNull final String fileProviderAuthorities,
            @NonNull final NewVersionInfo newVersionInfo
    ) {
        final Intent intent = UpdateActivity.createSelfIntent(context, fileProviderAuthorities, newVersionInfo.getVersionName(), newVersionInfo.getPath());
        context.startActivity(intent);
    }
}
