package blagodarie.rating.server;

import androidx.annotation.NonNull;

import org.json.JSONException;
import org.json.JSONObject;

import okhttp3.Request;

public final class GetRatingLatestVersionRequest
        extends ServerApiRequest<GetRatingLatestVersionResponse> {

    public GetRatingLatestVersionRequest () {
        super("getratinglatestversion");
    }

    @NonNull
    @Override
    Request.Builder getRequestBuilder () {
        return createDefaultRequestBuilder();
    }

    @Override
    protected GetRatingLatestVersionResponse parseOkResponse (
            @NonNull final String responseBody
    ) throws JSONException {
        final JSONObject json = new JSONObject(responseBody);
        final boolean ratingGooglePlayUpdate = true;//json.getBoolean("rating_google_play_update");
        final int versionCode = 100;//json.getInt("version_code");
        final String versionName = json.getString("version_name");
        final String path = json.getString("path");
        return new GetRatingLatestVersionResponse(ratingGooglePlayUpdate, versionCode, versionName, path);
    }
}