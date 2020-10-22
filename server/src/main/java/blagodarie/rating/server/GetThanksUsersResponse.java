package blagodarie.rating.server;

import androidx.annotation.NonNull;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class GetThanksUsersResponse
        extends _ServerApiResponse {

    public static final class ThanksUser {

        @NonNull
        private final UUID mUserId;

        @NonNull
        private final String mPhoto;

        public ThanksUser (
                @NonNull final UUID userId,
                @NonNull final String photo
        ) {
            mUserId = userId;
            mPhoto = photo;
        }

        @NonNull
        public UUID getUserId () {
            return mUserId;
        }

        @NonNull
        public String getPhoto () {
            return mPhoto;
        }
    }

    @NonNull
    private final List<ThanksUser> mThanksUsers;

    public GetThanksUsersResponse (
            @NonNull final List<ThanksUser> thanksUsers
    ) {
        mThanksUsers = Collections.unmodifiableList(thanksUsers);
    }

    @NonNull
    public final List<ThanksUser> getThanksUsers () {
        return mThanksUsers;
    }
}
