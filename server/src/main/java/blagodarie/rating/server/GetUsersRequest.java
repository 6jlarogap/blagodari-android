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

import blagodarie.rating.model.IProfile;
import blagodarie.rating.model.entities.Profile;
import okhttp3.Request;
import okhttp3.RequestBody;

public class GetUsersRequest
        extends ServerApiRequest<GetUsersResponse> {

    private static final String TAG = GetUsersRequest.class.getSimpleName();

    @NonNull
    private final String mFilter;

    private final int mFrom;

    private final int mCount;

    public GetUsersRequest (
            @NonNull final String filter,
            final int from,
            final int count
    ) {
        super("getusers");
        mFilter = filter;
        mFrom = from;
        mCount = count;
    }

    @NonNull
    @Override
    Request.Builder getRequestBuilder () {
        final RequestBody body = RequestBody.create(JSON_TYPE, createContent());
        return createDefaultRequestBuilder().post(body);
    }

    @Override
    protected GetUsersResponse parseOkResponse (
            @NonNull final String responseBody
    ) throws JSONException {
        Log.d(TAG, "parseOkResponse responseBody=" + responseBody);
        final JSONArray json = new JSONObject(responseBody).getJSONArray("users");
        final List<IProfile> users = new ArrayList<>();
        for (int i = 0; i < json.length(); i++) {
            final JSONObject jsonArrayElement = json.getJSONObject(i);
            final UUID id = UUID.fromString(jsonArrayElement.getString("uuid"));
            final String lastName = jsonArrayElement.getString("last_name");
            final String firstName = jsonArrayElement.getString("first_name");
            final String photo = jsonArrayElement.getString("photo");
            final int fame = jsonArrayElement.getInt("fame");
            final int sumThanksCount = jsonArrayElement.getInt("sum_thanks_count");
            final int mistrustCount = jsonArrayElement.getInt("trustless_count");
            final int trustCount = fame - mistrustCount;
            users.add(new Profile(id, lastName, firstName, photo, fame, trustCount, mistrustCount, sumThanksCount, 0, null));
        }
        return new GetUsersResponse(users);
    }


    private String createContent () {
        return String.format(Locale.ENGLISH, "{\"filter\":{\"text\":\"%s\"},\"from\":%d,\"count\":%d}", mFilter, mFrom, mCount);
    }
}