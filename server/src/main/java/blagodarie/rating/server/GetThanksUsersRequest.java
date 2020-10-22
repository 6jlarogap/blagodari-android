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

import okhttp3.Request;

public class GetThanksUsersRequest
        extends ServerApiRequest<GetThanksUsersResponse> {

    private static final String TAG = GetThanksUsersRequest.class.getSimpleName();

    public GetThanksUsersRequest (
            @NonNull final String userId,
            final int from,
            final int count
    ) {
        super(String.format(Locale.ENGLISH, "/getthanksusers?uuid=%s&from=%d&count=%d", userId, from, count));
    }

    @NonNull
    @Override
    Request.Builder getRequestBuilder () {
        return createDefaultRequestBuilder();
    }

    @Override
    protected GetThanksUsersResponse parseOkResponse (
            @NonNull String responseBody
    ) throws JSONException {
        Log.d(TAG, "parseOkResponse responseBody=" + responseBody);
        final JSONObject json = new JSONObject(responseBody);
        final List<GetThanksUsersResponse.ThanksUser> thanksUsers = new ArrayList<>();
        final JSONArray thanksUsersJSONArray = json.getJSONArray("thanks_users");
        for (int i = 0; i < thanksUsersJSONArray.length(); i++) {
            final JSONObject thanksUserJSONObject = thanksUsersJSONArray.getJSONObject(i);
            final String thanksUserPhoto = thanksUserJSONObject.getString("photo");
            final String thanksUserIdString = thanksUserJSONObject.getString("user_uuid");
            final UUID thanksUserId = UUID.fromString(thanksUserIdString);
            thanksUsers.add(new GetThanksUsersResponse.ThanksUser(thanksUserId, thanksUserPhoto));
        }
        return new GetThanksUsersResponse(thanksUsers);
    }
}
