package blagodarie.rating.ui.user.profile;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.paging.DataSource;
import androidx.paging.PositionalDataSource;

import java.util.UUID;

import blagodarie.rating.server.GetThanksUsersRequest;
import blagodarie.rating.server.GetThanksUsersResponse;
import blagodarie.rating.server.ServerApiClient;

final class ThanksUsersDataSource
        extends PositionalDataSource<GetThanksUsersResponse.ThanksUser> {

    private static final String TAG = ThanksUsersDataSource.class.getSimpleName();

    @NonNull
    private final UUID mUserId;

    ThanksUsersDataSource (
            @NonNull final UUID userId
    ) {
        Log.d(TAG, "ThanksUsersDataSource");
        mUserId = userId;
    }

    @Override
    public void loadInitial (
            @NonNull final LoadInitialParams params,
            @NonNull final LoadInitialCallback<GetThanksUsersResponse.ThanksUser> callback
    ) {
        Log.d(TAG, "loadInitial from=" + params.requestedStartPosition + ", pageSize=" + params.pageSize);
        final ServerApiClient client = new ServerApiClient();
        final GetThanksUsersRequest request = new GetThanksUsersRequest(mUserId.toString(), params.requestedStartPosition, params.pageSize);
        try {
            final GetThanksUsersResponse response = client.execute(request);
            callback.onResult(response.getThanksUsers(), 0);
        } catch (Exception e) {
            Log.e(TAG, Log.getStackTraceString(e));
        }
    }

    @Override
    public void loadRange (
            @NonNull final LoadRangeParams params,
            @NonNull final LoadRangeCallback<GetThanksUsersResponse.ThanksUser> callback
    ) {
        Log.d(TAG, "loadRange startPosition=" + params.startPosition + ", loadSize=" + params.loadSize);
        final ServerApiClient client = new ServerApiClient();
        final GetThanksUsersRequest request = new GetThanksUsersRequest(mUserId.toString(), params.startPosition, params.loadSize);
        try {
            final GetThanksUsersResponse response = client.execute(request);
            callback.onResult(response.getThanksUsers());
        } catch (Exception e) {
            Log.e(TAG, Log.getStackTraceString(e));
        }
    }

    static class ThanksUserDataSourceFactory
            extends Factory<Integer, GetThanksUsersResponse.ThanksUser> {

        @NonNull
        private final UUID mUserId;

        ThanksUserDataSourceFactory (
                @NonNull final UUID userId
        ) {
            mUserId = userId;
        }

        @Override
        public DataSource<Integer, GetThanksUsersResponse.ThanksUser> create () {
            return new ThanksUsersDataSource(mUserId);
        }

    }
}
