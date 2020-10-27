package blagodarie.rating.server;

import android.util.Log;

import androidx.annotation.NonNull;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import blagodarie.rating.model.IKey;
import blagodarie.rating.model.entities.Key;
import blagodarie.rating.model.entities.KeyType;
import okhttp3.Request;

public final class GetUserKeysRequest
        extends ServerApiRequest<GetUserKeysResponse> {

    private static final String TAG = GetUserKeysRequest.class.getSimpleName();

    @NonNull
    private final UUID mUserId;

    public GetUserKeysRequest (
            @NonNull final UUID userId,
            final int from,
            final int count
    ) {
        super(String.format(Locale.ENGLISH, "/getuserkeys?uuid=%s&from=%d&count=%d", userId.toString(), from, count));
        mUserId = userId;
    }

    @NonNull
    @Override
    Request.Builder getRequestBuilder () {
        return createDefaultRequestBuilder();
    }

    @Override
    protected GetUserKeysResponse parseOkResponse (
            @NonNull final String responseBody
    ) throws JSONException {
        Log.d(TAG, "parseOkResponse responseBody=" + responseBody);
        final JSONArray jsonArray = new JSONObject(responseBody).getJSONArray("keys");
        final List<IKey> keys = new ArrayList<>();
        for (int i = 0; i < jsonArray.length(); i++) {
            final JSONObject jsonArrayElement = jsonArray.getJSONObject(i);
            final long id = jsonArrayElement.getLong("id");
            final String value = jsonArrayElement.getString("value");
            final int typeId = jsonArrayElement.getInt("type_id");
            keys.add(new Key(id, mUserId, value, KeyType.getById(typeId)));
        }
        return new GetUserKeysResponse(keys);
    }
}
