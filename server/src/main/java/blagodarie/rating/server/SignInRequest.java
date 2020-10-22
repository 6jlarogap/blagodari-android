package blagodarie.rating.server;

import android.util.Log;

import androidx.annotation.NonNull;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Locale;

import okhttp3.Request;
import okhttp3.RequestBody;

public final class SignInRequest
        extends ServerApiRequest<SignInResponse> {

    private static final String TAG = SignInRequest.class.getSimpleName();

    @NonNull
    private final String mGoogleToken;

    @NonNull
    private final String mUserId;

    public SignInRequest (
            @NonNull final String googleToken,
            @NonNull final String userId
    ) {
        super("auth/signin");
        mGoogleToken = googleToken;
        mUserId = userId;
    }

    @NonNull
    @Override
    Request.Builder getRequestBuilder () {
        final RequestBody body = RequestBody.create(JSON_TYPE, createContent());
        return createDefaultRequestBuilder().post(body);
    }

    @Override
    protected SignInResponse parseOkResponse (
            @NonNull final String responseBody
    ) throws JSONException {
        Log.d(TAG, "parseOkResponse responseBody=" + responseBody);
        final JSONObject json = new JSONObject(responseBody);
        final String authToken = json.getString("token");
        return new SignInResponse(authToken);
    }

    private String createContent () {
        return String.format(Locale.ENGLISH, "{\"oauth\":{\"provider\":\"google\",\"token\":\"%s\"},\"user_id\":%s}", mGoogleToken, mUserId);
    }
}
