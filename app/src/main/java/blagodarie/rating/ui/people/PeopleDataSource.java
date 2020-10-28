package blagodarie.rating.ui.people;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.paging.DataSource;
import androidx.paging.PositionalDataSource;

import blagodarie.rating.model.IProfile;
import blagodarie.rating.server.GetUsersRequest;
import blagodarie.rating.server.GetUsersResponse;
import blagodarie.rating.server.ServerApiClient;

public class PeopleDataSource
        extends PositionalDataSource<IProfile> {

    private static final String TAG = PeopleDataSource.class.getSimpleName();

    @NonNull
    private final String mFilter;

    PeopleDataSource (
            @NonNull final String filter
    ) {
        Log.d(TAG, "PeopleDataSource");
        mFilter = filter;
    }

    @Override
    public void loadInitial (
            @NonNull final LoadInitialParams params,
            @NonNull final LoadInitialCallback<IProfile> callback
    ) {
        Log.d(TAG, "loadInitial from=" + params.requestedStartPosition + ", pageSize=" + params.pageSize);
        final ServerApiClient client = new ServerApiClient();
        final GetUsersRequest request = new GetUsersRequest(mFilter, params.requestedStartPosition, params.pageSize);
        try {
            final GetUsersResponse response = client.execute(request);
            callback.onResult(response.getUsers(), 0);
        } catch (Exception e) {
            Log.e(TAG, Log.getStackTraceString(e));
        }
    }

    @Override
    public void loadRange (
            @NonNull  final LoadRangeParams params,
            @NonNull final LoadRangeCallback<IProfile> callback
    ) {
        Log.d(TAG, "loadInitial from=" + params.startPosition + ", pageSize=" + params.loadSize);
        final ServerApiClient client = new ServerApiClient();
        final GetUsersRequest request = new GetUsersRequest(mFilter, params.startPosition, params.loadSize);
        try {
            final GetUsersResponse response = client.execute(request);
            callback.onResult(response.getUsers());
        } catch (Exception e) {
            Log.e(TAG, Log.getStackTraceString(e));
        }
    }

    static class UserOperationsDataSourceFactory
            extends Factory<Integer, IProfile> {

        @NonNull
        private final String mFilter;

        UserOperationsDataSourceFactory (
                @NonNull final String filter
        ) {
            mFilter = filter;
        }

        @Override
        public DataSource<Integer, IProfile> create () {
            return new PeopleDataSource(mFilter);
        }

    }
}