package blagodarie.rating.ui.user.keys;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.paging.DataSource;
import androidx.paging.PositionalDataSource;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import blagodarie.rating.model.IKey;
import blagodarie.rating.model.entities.Key;
import blagodarie.rating.server.GetUserKeysRequest;
import blagodarie.rating.server.GetUserKeysResponse;
import blagodarie.rating.server.GetUserWishesRequest;
import blagodarie.rating.server.GetUserWishesResponse;
import blagodarie.rating.server.ServerApiClient;
import blagodarie.rating.server.ServerApiResponse;
import blagodarie.rating.server.ServerConnector;

public final class KeysDataSource
        extends PositionalDataSource<IKey> {

    private static final String TAG = KeysDataSource.class.getSimpleName();

    @NonNull
    private final UUID mUserId;

    KeysDataSource (
            @NonNull final UUID userId
    ) {
        Log.d(TAG, "KeysDataSource");
        mUserId = userId;
    }

    @Override
    public void loadInitial (
            @NonNull final PositionalDataSource.LoadInitialParams params,
            @NonNull final PositionalDataSource.LoadInitialCallback<IKey> callback
    ) {
        Log.d(TAG, "loadInitial from=" + params.requestedStartPosition + ", pageSize=" + params.pageSize);
        final ServerApiClient client = new ServerApiClient();
        final GetUserKeysRequest request = new GetUserKeysRequest(mUserId, params.requestedStartPosition, params.pageSize);
        try {
            final GetUserKeysResponse response = client.execute(request);
            callback.onResult(response.getKeys(), 0);
        } catch (Exception e) {
            Log.e(TAG, Log.getStackTraceString(e));
        }
    }

    @Override
    public void loadRange (
            @NonNull final PositionalDataSource.LoadRangeParams params,
            @NonNull final PositionalDataSource.LoadRangeCallback<IKey> callback
    ) {
        Log.d(TAG, "loadInitial from=" + params.startPosition + ", pageSize=" + params.loadSize);
        final ServerApiClient client = new ServerApiClient();
        final GetUserKeysRequest request = new GetUserKeysRequest(mUserId, params.startPosition, params.loadSize);
        try {
            final GetUserKeysResponse response = client.execute(request);
            callback.onResult(response.getKeys());
        } catch (Exception e) {
            Log.e(TAG, Log.getStackTraceString(e));
        }
    }


    static class KeysDataSourceFactory
            extends DataSource.Factory<Integer, IKey> {

        @NonNull
        private final UUID mUserId;

        KeysDataSourceFactory (@NonNull final UUID userId) {
            mUserId = userId;
        }

        @Override
        public DataSource<Integer, IKey> create () {
            return new KeysDataSource(mUserId);
        }

    }
}
