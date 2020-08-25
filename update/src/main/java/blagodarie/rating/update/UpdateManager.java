package blagodarie.rating.update;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import androidx.annotation.NonNull;

import org.json.JSONException;
import org.json.JSONObject;

import blagodarie.rating.server.ServerApiResponse;
import blagodarie.rating.server.ServerConnector;
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
        return Observable.
                fromCallable(() -> ServerConnector.sendRequestAndGetResponse("getratinglatestversion")).
                subscribeOn(Schedulers.io()).
                observeOn(AndroidSchedulers.mainThread()).
                subscribe(
                        serverApiResponse -> handleCheckUpdateResponse(currentCodeVersion, serverApiResponse, onCheckUpdateListener, onErrorListener),
                        onErrorListener::onError
                );
    }

    private static void handleCheckUpdateResponse (
            final int currentCodeVersion,
            @NonNull final ServerApiResponse serverApiResponse,
            @NonNull final OnCheckUpdateListener onCheckUpdateListener,
            @NonNull final OnErrorListener onErrorListener
    ) {
        if (serverApiResponse.getBody() != null) {
            final String responseBody = serverApiResponse.getBody();
            switch (serverApiResponse.getCode()) {
                case 200:
                    try {
                        final JSONObject json = new JSONObject(responseBody);
                        final boolean ratingGooglePlayUpdate = json.getBoolean("rating_google_play_update");
                        if (!ratingGooglePlayUpdate) {
                            final int versionCode = json.getInt("version_code");
                            if (versionCode > currentCodeVersion) {
                                final String versionName = json.getString("version_name");
                                final String path = json.getString("path");
                                onCheckUpdateListener.onHaveUpdate(new NewVersionInfo(versionCode, versionName, Uri.parse(path)));
                            } else {
                                onCheckUpdateListener.onNothingUpdate();
                            }
                        } else {
                            onCheckUpdateListener.onUpdateFromMarket();
                        }
                    } catch (JSONException e) {
                        onErrorListener.onError(e);
                    }
                    break;
                case 400:
                    try {
                        final JSONObject json = new JSONObject(responseBody);
                        final String message = json.getString("message");
                        onErrorListener.onError(new Exception(message));
                    } catch (JSONException e) {
                        onErrorListener.onError(e);
                    }
                    break;
            }

        } else {
            onErrorListener.onError(new IllegalArgumentException("Response body is null"));
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
