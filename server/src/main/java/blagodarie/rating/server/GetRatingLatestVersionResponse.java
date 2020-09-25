package blagodarie.rating.server;

import androidx.annotation.NonNull;

public final class GetRatingLatestVersionResponse
        extends _ServerApiResponse {

    private final boolean mRatingGooglePlayUpdate;

    private final int mVersionCode;

    @NonNull
    private final String mVersionName;

    @NonNull
    private final String mPath;

    public GetRatingLatestVersionResponse (
            final boolean ratingGooglePlayUpdate,
            final int versionCode,
            @NonNull final String versionName,
            @NonNull final String path
    ) {
        mRatingGooglePlayUpdate = ratingGooglePlayUpdate;
        mVersionCode = versionCode;
        mVersionName = versionName;
        mPath = path;
    }

    public final boolean isRatingGooglePlayUpdate () {
        return mRatingGooglePlayUpdate;
    }

    public int getVersionCode () {
        return mVersionCode;
    }

    @NonNull
    public final String getVersionName () {
        return mVersionName;
    }

    @NonNull
    public final String getPath () {
        return mPath;
    }
}
