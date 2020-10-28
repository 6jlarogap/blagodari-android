package blagodarie.rating.ui.user.operations;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.paging.DataSource;
import androidx.paging.PositionalDataSource;

import java.util.UUID;

import blagodarie.rating.model.IDisplayOperation;
import blagodarie.rating.server.GetUserOperationsRequest;
import blagodarie.rating.server.GetOperationsResponse;
import blagodarie.rating.server.ServerApiClient;

final class UserOperationsDataSource
        extends PositionalDataSource<IDisplayOperation> {

    private static final String TAG = UserOperationsDataSource.class.getSimpleName();

    @NonNull
    private final UUID mUserId;

    UserOperationsDataSource (
            @NonNull final UUID userId
    ) {
        Log.d(TAG, "UserOperationsDataSource");
        mUserId = userId;
    }

    @Override
    public void loadInitial (
            @NonNull final LoadInitialParams params,
            @NonNull final LoadInitialCallback<IDisplayOperation> callback
    ) {
        Log.d(TAG, "loadInitial from=" + params.requestedStartPosition + ", pageSize=" + params.pageSize);
        final ServerApiClient client = new ServerApiClient();
        final GetUserOperationsRequest request = new GetUserOperationsRequest(mUserId, params.requestedStartPosition, params.pageSize);
        try {
            final GetOperationsResponse response = client.execute(request);
            callback.onResult(response.getOperations(), 0);
        } catch (Exception e) {
            Log.e(TAG, Log.getStackTraceString(e));
        }
    }

    @Override
    public void loadRange (
            @NonNull final LoadRangeParams params,
            @NonNull final LoadRangeCallback<IDisplayOperation> callback
    ) {
        Log.d(TAG, "loadRange startPosition=" + params.startPosition + ", loadSize=" + params.loadSize);
        final ServerApiClient client = new ServerApiClient();
        final GetUserOperationsRequest request = new GetUserOperationsRequest(mUserId, params.startPosition, params.loadSize);
        try {
            final GetOperationsResponse response = client.execute(request);
            callback.onResult(response.getOperations());
        } catch (Exception e) {
            Log.e(TAG, Log.getStackTraceString(e));
        }
    }

    static class UserOperationsDataSourceFactory
            extends Factory<Integer, IDisplayOperation> {

        @NonNull
        private final UUID mUserId;


        UserOperationsDataSourceFactory (
                @NonNull final UUID userId
        ) {
            mUserId = userId;
        }

        @Override
        public DataSource<Integer, IDisplayOperation> create () {
            return new UserOperationsDataSource(mUserId);
        }

    }
}
