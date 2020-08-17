package blagodarie.rating.ui.user.wishes;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.paging.DataSource;
import androidx.paging.PositionalDataSource;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import blagodarie.rating.server.ServerApiResponse;
import blagodarie.rating.server.ServerConnector;

public class WishesDataSource
        extends PositionalDataSource<Wish> {
    private static final String TAG = WishesDataSource.class.getSimpleName();

    @NonNull
    private final UUID mUserId;

    WishesDataSource (@NonNull final UUID userId) {
        Log.d(TAG, "OperationDataSource");
        mUserId = userId;
    }

    @Override
    public void loadInitial (@NonNull PositionalDataSource.LoadInitialParams params, @NonNull PositionalDataSource.LoadInitialCallback<Wish> callback) {
        Log.d(TAG, "loadInitial from=" + params.requestedStartPosition + ", pageSize=" + params.pageSize);
        try {
            final ServerApiResponse serverApiResponse = ServerConnector.sendRequestAndGetResponse(String.format(Locale.ENGLISH, "/getuserwishes?uuid=%s&from=%d&count=%d", mUserId.toString(), params.requestedStartPosition, params.pageSize));
            if (serverApiResponse.getCode() == 200) {
                if (serverApiResponse.getBody() != null) {
                    final String responseBody = serverApiResponse.getBody();
                    Log.d(TAG, "responseBody=" + responseBody);
                    try {
                        final JSONArray jsonOperations = new JSONObject(responseBody).getJSONArray("wishes");
                        final List<Wish> wishes = new ArrayList<>();
                        for (int i = 0; i < jsonOperations.length(); i++) {
                            final JSONObject operationJsonObject = jsonOperations.getJSONObject(i);
                            final UUID id = UUID.fromString(operationJsonObject.getString("uuid"));
                            final String text = operationJsonObject.getString("text");
                            final Date lastEdit = new Date(operationJsonObject.getLong("last_edit"));
                            wishes.add(new Wish(id, mUserId, text, lastEdit));
                        }
                        callback.onResult(wishes, 0);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void loadRange (@NonNull PositionalDataSource.LoadRangeParams params, @NonNull PositionalDataSource.LoadRangeCallback<Wish> callback) {
        Log.d(TAG, "loadRange startPosition=" + params.startPosition + ", loadSize=" + params.loadSize);
        try {
            final ServerApiResponse serverApiResponse = ServerConnector.sendRequestAndGetResponse(String.format(Locale.ENGLISH, "/getuserwishes?uuid=%s&from=%d&count=%d", mUserId.toString(), params.startPosition, params.loadSize));
            if (serverApiResponse.getCode() == 200) {
                if (serverApiResponse.getBody() != null) {
                    final String responseBody = serverApiResponse.getBody();
                    Log.d(TAG, "responseBody=" + responseBody);
                    try {
                        final JSONArray jsonOperations = new JSONObject(responseBody).getJSONArray("wishes");
                        final List<Wish> wishes = new ArrayList<>();
                        for (int i = 0; i < jsonOperations.length(); i++) {
                            final JSONObject operationJsonObject = jsonOperations.getJSONObject(i);
                            final UUID id = UUID.fromString(operationJsonObject.getString("uuid"));
                            final String text = operationJsonObject.getString("text");
                            final Date lastEdit = new Date(operationJsonObject.getLong("last_edit"));
                            wishes.add(new Wish(id, mUserId, text, lastEdit));
                        }
                        callback.onResult(wishes);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    static class WishesDataSourceFactory
            extends DataSource.Factory<Integer, Wish> {

        @NonNull
        private final UUID mUserId;

        WishesDataSourceFactory (@NonNull final UUID userId) {
            mUserId = userId;
        }

        @Override
        public DataSource<Integer, Wish> create () {
            return new WishesDataSource(mUserId);
        }

    }
}
