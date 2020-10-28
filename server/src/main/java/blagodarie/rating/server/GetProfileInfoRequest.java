package blagodarie.rating.server;

import android.util.Log;

import androidx.annotation.NonNull;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.UUID;

import blagodarie.rating.model.entities.Profile;
import okhttp3.Request;
import okhttp3.Response;

public final class GetProfileInfoRequest
        extends ServerApiRequest<GetProfileInfoResponse> {

    private static final String TAG = GetProfileInfoRequest.class.getSimpleName();

    @NonNull
    private final UUID mUserId;

    public GetProfileInfoRequest (
            @NonNull final UUID userId
    ) {
        super(String.format("getprofileinfo?uuid=%s", userId));
        mUserId = userId;
    }

    @NonNull
    @Override
    Request.Builder getRequestBuilder () {
        return createDefaultRequestBuilder();
    }

    @Override
    protected GetProfileInfoResponse parseOkResponse (
            @NonNull final String responseBody
    ) throws JSONException {
        Log.d(TAG, "parseOkResponse responseBody=" + responseBody);
        final JSONObject json = new JSONObject(responseBody);

        final String photo = json.getString("photo");
        final String firstName = json.getString("first_name");
        final String lastName = json.getString("last_name");
        final int fame = json.getInt("fame");
        final int sumThanksCount = json.getInt("sum_thanks_count");
        final int mistrustCount = json.getInt("trustless_count");
        final int trustCount = fame - mistrustCount;
        int thanksCount;
        try {
            thanksCount = json.getInt("thanks_count");
        } catch (JSONException e) {
            thanksCount = 0;
        }
        Boolean isTrust;
        try {
            isTrust = json.getBoolean("is_trust");
        } catch (JSONException e) {
            isTrust = null;
        }
        return new GetProfileInfoResponse(
                new Profile(
                        mUserId,
                        firstName,
                        lastName,
                        photo,
                        fame,
                        trustCount,
                        mistrustCount,
                        sumThanksCount,
                        thanksCount,
                        isTrust
                )
        );
    }

    @Override
    protected GetProfileInfoResponse parse400Response (
            @NonNull final Response response
    ) {
        return new GetProfileInfoResponse(null);
    }
}
