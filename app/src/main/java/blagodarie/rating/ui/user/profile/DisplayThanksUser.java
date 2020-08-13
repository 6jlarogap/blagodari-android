package blagodarie.rating.ui.user.profile;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public final class DisplayThanksUser {

    @Nullable
    private final String mPhoto;

    @NonNull
    private final String mUserUUID;

    DisplayThanksUser (
            @Nullable final String photo,
            @NonNull final String userUUID
    ) {
        mPhoto = photo;
        mUserUUID = userUUID;
    }

    @Nullable
    public final String getPhoto () {
        return mPhoto;
    }

    @NonNull
    public final String getUserUUID () {
        return mUserUUID;
    }

    @Override
    public String toString () {
        return "DisplayThanksUser{" +
                "mPhoto='" + mPhoto + '\'' +
                ", mUserUUID=" + mUserUUID +
                '}';
    }
}
