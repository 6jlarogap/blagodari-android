package blagodarie.rating.ui.user.operations;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.paging.DataSource;
import androidx.paging.PositionalDataSource;

import java.util.UUID;

import blagodarie.rating.model.IDisplayOperation;
import blagodarie.rating.server.GetAnyTextOperationsRequest;
import blagodarie.rating.server.GetUserOperationsRequest;
import blagodarie.rating.server.GetOperationsResponse;
import blagodarie.rating.server.ServerApiClient;

public final class AnyTextOperationsDataSource
        extends PositionalDataSource<IDisplayOperation> {

    private static final String TAG = AnyTextOperationsDataSource.class.getSimpleName();

    @NonNull
    private final UUID mAnyTextId;

    public AnyTextOperationsDataSource (
            @NonNull final UUID anyTextId
    ) {
        Log.d(TAG, "AnyTextOperationsDataSource");
        mAnyTextId = anyTextId;
    }

    @Override
    public void loadInitial (@NonNull LoadInitialParams params, @NonNull LoadInitialCallback<IDisplayOperation> callback) {
        Log.d(TAG, "loadInitial from=" + params.requestedStartPosition + ", pageSize=" + params.pageSize);
        final ServerApiClient client = new ServerApiClient();
        final GetAnyTextOperationsRequest request = new GetAnyTextOperationsRequest(mAnyTextId, params.requestedStartPosition, params.pageSize);
        try {
            final GetOperationsResponse response = client.execute(request);
            callback.onResult(response.getOperations(), 0);
        } catch (Exception e) {
            Log.e(TAG, Log.getStackTraceString(e));
        }
    }

    @Override
    public void loadRange (@NonNull LoadRangeParams params, @NonNull LoadRangeCallback<IDisplayOperation> callback) {
        Log.d(TAG, "loadRange startPosition=" + params.startPosition + ", loadSize=" + params.loadSize);
        final ServerApiClient client = new ServerApiClient();
        final GetUserOperationsRequest request = new GetUserOperationsRequest(mAnyTextId, params.startPosition, params.loadSize);
        try {
            final GetOperationsResponse response = client.execute(request);
            callback.onResult(response.getOperations());
        } catch (Exception e) {
            Log.e(TAG, Log.getStackTraceString(e));
        }
    }

    public static class AnyTextOperationsDataSourceFactory
            extends Factory<Integer, IDisplayOperation> {

        @NonNull
        private final UUID mUserId;


        public AnyTextOperationsDataSourceFactory (
                @NonNull final UUID userId
        ) {
            mUserId = userId;
        }

        @Override
        public DataSource<Integer, IDisplayOperation> create () {
            return new AnyTextOperationsDataSource(mUserId);
        }

    }
}
