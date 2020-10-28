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

import blagodarie.rating.model.IWish;
import blagodarie.rating.model.entities.Wish;
import okhttp3.Request;

public final class GetUserWishesRequest
        extends ServerApiRequest<GetUserWishesResponse> {

    private static final String TAG = GetUserWishesRequest.class.getSimpleName();

    @NonNull
    private final UUID mUserId;

    public GetUserWishesRequest (
            @NonNull final UUID userId,
            final int from,
            final int count
    ) {
        super(String.format(Locale.ENGLISH, "/getuserwishes?uuid=%s&from=%d&count=%d", userId.toString(), from, count));
        mUserId = userId;
    }

    @NonNull
    @Override
    Request.Builder getRequestBuilder () {
        return createDefaultRequestBuilder();
    }

    @Override
    protected GetUserWishesResponse parseOkResponse (
            @NonNull final String responseBody
    ) throws JSONException {
        Log.d(TAG, "parseOkResponse responseBody=" + responseBody);
        final JSONArray jsonArray = new JSONObject(responseBody).getJSONArray("wishes");
        final List<IWish> wishes = new ArrayList<>();
        for (int i = 0; i < jsonArray.length(); i++) {
            final JSONObject jsonArrayElement = jsonArray.getJSONObject(i);
            final UUID id = UUID.fromString(jsonArrayElement.getString("uuid"));
            final String text = jsonArrayElement.getString("text");
            final Date lastEdit = new Date(jsonArrayElement.getLong("last_edit"));
            wishes.add(new Wish(id, mUserId, text, lastEdit));
        }
        return new GetUserWishesResponse(wishes);
    }
}
