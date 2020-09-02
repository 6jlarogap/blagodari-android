package blagodarie.rating.server;

import androidx.annotation.NonNull;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.UUID;

import okhttp3.Request;
import okhttp3.RequestBody;

public final class SignUpRequest
        extends ServerApiRequest<SignUpResponse> {

    @NonNull
    private final String mGoogleToken;

    public SignUpRequest (
            @NonNull final String googleToken
    ) {
        super("auth/signup");
        mGoogleToken = googleToken;
    }

    @NonNull
    @Override
    final Request.Builder getRequestBuilder () {
        final RequestBody body = RequestBody.create(JSON_TYPE, createContent());
        return createDefaultRequestBuilder().post(body);
    }

    @Override
    protected SignUpResponse parseOkResponse (
            @NonNull final String responseBody
    ) throws JSONException {
        final JSONObject userJSON = new JSONObject(responseBody);
        final String userIdString = userJSON.getString("user_uuid");
        final UUID userId = UUID.fromString(userIdString);
        final String firstName = userJSON.getString("first_name");
        final String middleName = userJSON.getString("middle_name");
        final String lastName = userJSON.getString("last_name");
        final String photo = userJSON.getString("photo");
        final String authToken = userJSON.getString("token");
        return new SignUpResponse(
                userId,
                firstName,
                middleName,
                lastName,
                photo,
                authToken
        );
    }


    private String createContent () {
        return String.format("{\"oauth\":{\"provider\":\"google\",\"token\":\"%s\"}}", mGoogleToken);
    }
}