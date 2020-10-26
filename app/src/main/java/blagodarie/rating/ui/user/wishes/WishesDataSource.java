package blagodarie.rating.ui.user.wishes;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.paging.DataSource;
import androidx.paging.PositionalDataSource;

import java.util.UUID;

import blagodarie.rating.model.IWish;
import blagodarie.rating.server.GetUserWishesRequest;
import blagodarie.rating.server.GetUserWishesResponse;
import blagodarie.rating.server.ServerApiClient;

class WishesDataSource
        extends PositionalDataSource<IWish> {

    private static final String TAG = WishesDataSource.class.getSimpleName();

    @NonNull
    private final UUID mUserId;

    WishesDataSource (
            @NonNull final UUID userId
    ) {
        Log.d(TAG, "OperationDataSource");
        mUserId = userId;
    }

    @Override
    public void loadInitial (
            @NonNull final PositionalDataSource.LoadInitialParams params,
            @NonNull final PositionalDataSource.LoadInitialCallback<IWish> callback
    ) {
        Log.d(TAG, "loadInitial from=" + params.requestedStartPosition + ", pageSize=" + params.pageSize);
        final ServerApiClient client = new ServerApiClient();
        final GetUserWishesRequest request = new GetUserWishesRequest(mUserId, params.requestedStartPosition, params.pageSize);
        try {
            final GetUserWishesResponse response = client.execute(request);
            callback.onResult(response.getWishes(), 0);
        } catch (Exception e) {
            Log.e(TAG, Log.getStackTraceString(e));
        }
    }

    @Override
    public void loadRange (
            @NonNull final PositionalDataSource.LoadRangeParams params,
            @NonNull final PositionalDataSource.LoadRangeCallback<IWish> callback
    ) {
        Log.d(TAG, "loadRange startPosition=" + params.startPosition + ", loadSize=" + params.loadSize);
        final ServerApiClient client = new ServerApiClient();
        final GetUserWishesRequest request = new GetUserWishesRequest(mUserId, params.startPosition, params.loadSize);
        try {
            final GetUserWishesResponse response = client.execute(request);
            callback.onResult(response.getWishes());
        } catch (Exception e) {
            Log.e(TAG, Log.getStackTraceString(e));
        }
    }

    static class WishesDataSourceFactory
            extends DataSource.Factory<Integer, IWish> {

        @NonNull
        private final UUID mUserId;

        WishesDataSourceFactory (
                @NonNull final UUID userId
        ) {
            mUserId = userId;
        }

        @Override
        public DataSource<Integer, IWish> create () {
            return new WishesDataSource(mUserId);
        }

    }
}
