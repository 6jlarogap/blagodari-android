package blagodarie.rating.server;

import androidx.annotation.NonNull;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.Response;

public abstract class ServerApiRequest<ApiResponseType extends _ServerApiResponse> {

    protected static final MediaType JSON_TYPE = MediaType.parse("application/json; charset=utf-8");
    private final static String API_BASE_URL = BuildConfig.DEBUG ? "https://api.dev.благодарие.рф/api/" : "https://api.благодарие.рф/api/";

    @NonNull
    private final String mRelateUrl;

    ServerApiRequest (
            @NonNull final String relateUrl
    ) {
        mRelateUrl = relateUrl;
    }

    @NonNull
    private String getUrl () {
        return API_BASE_URL + mRelateUrl;
    }

    @NonNull
    protected Request.Builder createDefaultRequestBuilder () {
        return new Request.Builder().url(getUrl());
    }

    @NonNull
    abstract Request.Builder getRequestBuilder ();

    @NonNull
    ApiResponseType parseResponse (
            @NonNull final Response response
    ) throws ServerException, JSONException, IOException, EmptyBodyException {
        if (response.code() == 200) {
            if (response.body() != null) {
                return parseOkResponse(response.body().string());
            } else {
                throw new EmptyBodyException();
            }
        } else if (response.code() == 401) {
            throw new UnauthorizedException();
        } else {
            String errorMessage = null;
            if (response.body() != null) {
                final JSONObject jsonObject = new JSONObject(response.body().string());
                errorMessage = jsonObject.getString("message");
            }
            throw new ServerException(errorMessage, response.code());
        }
    }

    protected abstract ApiResponseType parseOkResponse (
            @NonNull final String responseBody
    ) throws JSONException;
}
