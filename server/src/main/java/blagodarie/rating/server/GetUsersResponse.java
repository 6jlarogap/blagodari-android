package blagodarie.rating.server;

import androidx.annotation.NonNull;

import java.util.List;

import blagodarie.rating.model.IProfile;

public class GetUsersResponse
        extends _ServerApiResponse {

    @NonNull
    private final List<IProfile> mUsers;

    GetUsersResponse (@NonNull final List<IProfile> users) {
        mUsers = users;
    }

    @NonNull
    public List<IProfile> getUsers () {
        return mUsers;
    }
}
