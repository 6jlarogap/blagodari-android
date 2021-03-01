package blagodarie.rating.server;

import android.util.Log;

import androidx.annotation.NonNull;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.Response;

public abstract class ServerApiRequest<ApiResponseType extends _ServerApiResponse> {

    private static final String TAG = ServerApiRequest.class.getSimpleName();

    protected static final MediaType JSON_TYPE = MediaType.parse("application/json; charset=utf-8");
    private final static String API_BASE_URL = BuildConfig.DEBUG ? "https://api.dev.blagodarie.org/api/" : "https://api.blagodarie.org/api/";

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
    ) throws JSONException, IOException, EmptyResponseException, HttpException {
        Log.d(TAG, "parseResponse response=" + response.toString());
        if (response.code() == 200) {
            if (response.body() != null) {
                return parseOkResponse(response.body().string());
            } else {
                throw new EmptyResponseException();
            }
        } else if (response.code() == 400) {
            return parse400Response(response);
        } else if (response.code() == 401) {
            throw new BadAuthorizationTokenException();
        } else if (response.code() >= 402 && response.code() <= 499) {
            String errorMessage = null;
            if (response.body() != null) {
                final JSONObject jsonObject = new JSONObject(response.body().string());
                errorMessage = jsonObject.getString("message");
            }
            throw new BadRequestException(response.code(), response.message() + " - " + errorMessage);
        } else if (response.code() >= 500 && response.code() <= 599) {
            throw new ServerInternalException(response.code(), response.message());
        } else {
            throw new HttpException(response.code(), response.message());
        }
    }

    protected ApiResponseType parse400Response (
            @NonNull final Response response
    ) throws JSONException, IOException, BadRequestException {
        String errorMessage = null;
        if (response.body() != null) {
            final JSONObject jsonObject = new JSONObject(response.body().string());
            errorMessage = jsonObject.getString("message");
        }
        throw new BadRequestException(response.code(), response.message() + " - " + errorMessage);
    }

    protected abstract ApiResponseType parseOkResponse (
            @NonNull final String responseBody
    ) throws JSONException;
}
