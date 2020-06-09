package org.blagodari.server.api;

import androidx.annotation.NonNull;

import com.ex.diagnosticlib.Diagnostic;
import org.blagodari.server.ResponseBodyException;
import org.blagodari.server.ResponseException;
import org.blagodari.server.ServerException;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Request;
import okhttp3.Response;

public final class GetLatestVersion
        extends Api<Api.DataIn, GetLatestVersion.Result> {

    private static volatile GetLatestVersion INSTANCE;

    private GetLatestVersion () {
        super(GetLatestVersion.class.getSimpleName().toLowerCase());
    }

    public static GetLatestVersion getInstance () {
        synchronized (GetLatestVersion.class) {
            if (INSTANCE == null) {
                INSTANCE = new GetLatestVersion();
            }
        }
        return INSTANCE;
    }

    @NonNull
    @Override
    public Result execute (@NonNull final DataIn dataIn) {
        Diagnostic.i("start");
        final Result apiResult = new Result();
        try {
            final Request request = new Request.Builder()
                    .url(API_URL)
                    .build();
            final Response response = sendRequestAndGetResponse(request);
            handleResponse(response, apiResult);
        } catch (Throwable throwable) {
            apiResult.setThrowable(throwable);
        }
        return apiResult;
    }

    private static void handleResponse (
            @NonNull final Response response,
            @NonNull final Result apiResult
    ) throws IOException, JSONException, ServerException, ResponseException, ResponseBodyException {
        if (response.body() != null) {
            final String responseBody = response.body().string();
            Diagnostic.i("responseBody", responseBody);
            if (response.code() == 200) {
                final JSONObject rootJSON = new JSONObject(responseBody);
                int versionCode = rootJSON.getInt("version_code");
                String versionName = rootJSON.getString("version_name");
                String url = rootJSON.getString("url");
                apiResult.setVersionCode(versionCode);
                apiResult.setVersionName(versionName);
                apiResult.setUrl(url);
            } else if (response.code() == 400) {
                throw new ServerException(new JSONObject(responseBody).getString("message"));
            } else {
                throw new ResponseException();
            }
        } else {
            throw new ResponseBodyException();
        }
    }

    public static final class Result
            extends Api.Result {

        private int versionCode;
        private String versionName;
        private String url;

        public int getVersionCode () {
            return versionCode;
        }

        void setVersionCode (int versionCode) {
            this.versionCode = versionCode;
        }

        void setVersionName (String versionName) {
            this.versionName = versionName;
        }

        public void setUrl (String url) {
            this.url = url;
        }

        public String getVersionName () {
            return versionName;
        }

        public String getUrl () {
            return url;
        }
    }
}
