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

import blagodarie.rating.server.ServerApiResponse;
import blagodarie.rating.server.ServerConnector;

public final class KeysDataSource
        extends PositionalDataSource<Key> {

    private static final String TAG = KeysDataSource.class.getSimpleName();

    @NonNull
    private final UUID mUserId;

    KeysDataSource (@NonNull final UUID userId) {
        Log.d(TAG, "KeysDataSource");
        mUserId = userId;
    }

    @Override
    public void loadInitial (
            @NonNull final PositionalDataSource.LoadInitialParams params,
            @NonNull final PositionalDataSource.LoadInitialCallback<Key> callback
    ) {
        Log.d(TAG, "loadInitial from=" + params.requestedStartPosition + ", pageSize=" + params.pageSize);
        try {
            final ServerApiResponse serverApiResponse = ServerConnector.sendRequestAndGetResponse(String.format(Locale.ENGLISH, "/getuserkeys?uuid=%s&from=%d&count=%d", mUserId.toString(), params.requestedStartPosition, params.pageSize));
            callback.onResult(extractDataFromServerApiResponse(serverApiResponse), 0);
        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void loadRange (
            @NonNull final PositionalDataSource.LoadRangeParams params,
            @NonNull final PositionalDataSource.LoadRangeCallback<Key> callback
    ) {
        Log.d(TAG, "loadRange startPosition=" + params.startPosition + ", loadSize=" + params.loadSize);
        try {
            final ServerApiResponse serverApiResponse = ServerConnector.sendRequestAndGetResponse(String.format(Locale.ENGLISH, "/getuserkeys?uuid=%s&from=%d&count=%d", mUserId.toString(), params.startPosition, params.loadSize));
            callback.onResult(extractDataFromServerApiResponse(serverApiResponse));
        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }
    }

    private List<Key> extractDataFromServerApiResponse (
            @NonNull final ServerApiResponse serverApiResponse
    ) throws JSONException {
        final List<Key> keys = new ArrayList<>();
        if (serverApiResponse.getCode() == 200) {
            if (serverApiResponse.getBody() != null) {
                final String responseBody = serverApiResponse.getBody();
                Log.d(TAG, "responseBody=" + responseBody);
                final JSONArray jsonOperations = new JSONObject(responseBody).getJSONArray("keys");
                for (int i = 0; i < jsonOperations.length(); i++) {
                    final JSONObject keysJsonObject = jsonOperations.getJSONObject(i);
                    final long id = keysJsonObject.getLong("id");
                    final String value = keysJsonObject.getString("value");
                    final int typeId = keysJsonObject.getInt("type_id");
                    keys.add(new Key(id, mUserId, value, KeyType.getById(typeId)));
                }
            }
        }
        return keys;
    }

    static class KeysDataSourceFactory
            extends DataSource.Factory<Integer, Key> {

        @NonNull
        private final UUID mUserId;

        KeysDataSourceFactory (@NonNull final UUID userId) {
            mUserId = userId;
        }

        @Override
        public DataSource<Integer, Key> create () {
            return new KeysDataSource(mUserId);
        }

    }
}
