package blagodarie.rating.server;

import android.util.Log;

import androidx.annotation.NonNull;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;
import java.util.Locale;
import java.util.UUID;

import blagodarie.rating.model.entities.Wish;
import okhttp3.Request;
import okhttp3.Response;

public final class GetWishInfoRequest
        extends ServerApiRequest<GetWishInfoResponse> {

    private static final String TAG = GetWishInfoRequest.class.getSimpleName();

    @NonNull
    private final UUID mWishId;

    public GetWishInfoRequest (
            @NonNull final UUID wishId
    ) {
        super(String.format(Locale.ENGLISH, "getwishinfo?uuid=%s", wishId.toString()));
        mWishId = wishId;
    }

    @NonNull
    @Override
    Request.Builder getRequestBuilder () {
        return createDefaultRequestBuilder();
    }

    @Override
    protected GetWishInfoResponse parseOkResponse (
            @NonNull final String responseBody
    ) throws JSONException {
        Log.d(TAG, "parseOkResponse responseBody=" + responseBody);
        final JSONObject json = new JSONObject(responseBody);
        final String ownerUuidString = json.getString("owner_id");
        final UUID ownerUuid = UUID.fromString(ownerUuidString);
        final String text = json.getString("text");
        final long timestamp = json.getLong("last_edit");
        return new GetWishInfoResponse(new Wish(mWishId, ownerUuid, text, new Date(timestamp)));
    }


    @Override
    protected GetWishInfoResponse parse400Response (
            @NonNull final Response response
    ) {
        return new GetWishInfoResponse(null);
    }
}
