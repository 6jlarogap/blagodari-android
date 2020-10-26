package blagodarie.rating.server;

import android.util.Log;

import androidx.annotation.NonNull;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.UUID;

import blagodarie.rating.model.entities.AnyTextInfo;
import okhttp3.Request;
import okhttp3.Response;

public final class GetAnyTextInfoRequest
        extends ServerApiRequest<GetAnyTextInfoResponse> {

    private static final String TAG = GetAnyTextInfoRequest.class.getSimpleName();

    public GetAnyTextInfoRequest (
            @NonNull final String anyText
    ) {
        super(String.format("gettextinfo?text=%s", anyText));
    }

    @NonNull
    @Override
    Request.Builder getRequestBuilder () {
        return createDefaultRequestBuilder();
    }

    @Override
    protected GetAnyTextInfoResponse parseOkResponse (
            @NonNull final String responseBody
    ) throws JSONException {
        Log.d(TAG, "parseOkResponse responseBody=" + responseBody);
        final JSONObject json = new JSONObject(responseBody);

        final UUID anyTextId = UUID.fromString(json.getString("uuid"));
        final int fame = json.getInt("fame");
        final int sumThanksCount = json.getInt("sum_thanks_count");
        final int mistrustCount = json.getInt("trustless_count");
        final int trustCount = fame - mistrustCount;
        Integer thanksCount;
        try {
            thanksCount = json.getInt("thanks_count");
        } catch (JSONException e) {
            thanksCount = null;
        }
        Boolean isTrust;
        try {
            isTrust = json.getBoolean("is_trust");
        } catch (JSONException e) {
            isTrust = null;
        }
        return new GetAnyTextInfoResponse(
                new AnyTextInfo(
                        anyTextId,
                        fame,
                        trustCount,
                        mistrustCount,
                        sumThanksCount,
                        thanksCount,
                        isTrust)
        );
    }

    @Override
    protected GetAnyTextInfoResponse parse400Response (
            @NonNull final Response response
    ) {
        return new GetAnyTextInfoResponse(null);
    }
}
