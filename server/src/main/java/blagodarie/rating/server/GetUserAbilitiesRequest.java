package blagodarie.rating.server;

import android.util.Log;

import androidx.annotation.NonNull;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import blagodarie.rating.model.IAbility;
import blagodarie.rating.model.entities.Ability;
import okhttp3.Request;

public final class GetUserAbilitiesRequest
        extends ServerApiRequest<GetUserAbilitiesResponse> {


    private static final String TAG = GetUserAbilitiesRequest.class.getSimpleName();

    @NonNull
    private final UUID mUserId;

    public GetUserAbilitiesRequest (
            @NonNull final UUID userId,
            final int from,
            final int count
    ) {
        super(String.format(Locale.ENGLISH, "/getuserabilities?uuid=%s&from=%d&count=%d", userId.toString(), from, count));
        mUserId = userId;
    }

    @NonNull
    @Override
    Request.Builder getRequestBuilder () {
        return createDefaultRequestBuilder();
    }

    @Override
    protected GetUserAbilitiesResponse parseOkResponse (
            @NonNull final String responseBody
    ) throws JSONException {
        Log.d(TAG, "parseOkResponse responseBody=" + responseBody);
        final JSONArray json = new JSONObject(responseBody).getJSONArray("abilities");
        final List<IAbility> abilities = new ArrayList<>();
        for (int i = 0; i < json.length(); i++) {
            final JSONObject operationJsonObject = json.getJSONObject(i);
            final UUID id = UUID.fromString(operationJsonObject.getString("uuid"));
            final String text = operationJsonObject.getString("text");
            final Date lastEdit = new Date(operationJsonObject.getLong("last_edit"));
            abilities.add(new Ability(id, mUserId, text, lastEdit));
        }
        return new GetUserAbilitiesResponse(abilities);
    }
}
