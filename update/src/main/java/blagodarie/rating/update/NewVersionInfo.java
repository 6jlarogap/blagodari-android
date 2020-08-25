package blagodarie.rating.update;

import android.net.Uri;

import androidx.annotation.NonNull;

public final class NewVersionInfo {

    private final int mVersionCode;

    @NonNull
    private final String mVersionName;

    @NonNull
    private final Uri mPath;

    NewVersionInfo (
            final int versionCode,
            @NonNull final String versionName,
            @NonNull final Uri path
    ) {
        mVersionCode = versionCode;
        mVersionName = versionName;
        mPath = path;
    }

    public final int getVersionCode () {
        return mVersionCode;
    }

    @NonNull
    public final String getVersionName () {
        return mVersionName;
    }

    @NonNull
    public final Uri getPath () {
        return mPath;
    }
}
