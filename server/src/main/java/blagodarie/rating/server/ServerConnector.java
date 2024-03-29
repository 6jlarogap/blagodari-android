package blagodarie.rating.server;

import android.annotation.SuppressLint;

import androidx.annotation.NonNull;

import java.io.IOException;
import java.security.cert.CertificateException;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public final class ServerConnector {

    public static final MediaType JSON_TYPE = MediaType.parse("application/json; charset=utf-8");

    private final static String API_BASE_URL = BuildConfig.DEBUG ? "https://api.dev.благодарие.рф/api/" : "https://api.благодарие.рф/api/";

    @NonNull
    private static OkHttpClient generateDefaultOkHttp () {
        final OkHttpClient.Builder builder = new OkHttpClient.Builder();
        try {
            // Create a trust manager that does not validate certificate chains
            final TrustManager[] trustAllCerts = new TrustManager[]{
                    new X509TrustManager() {
                        @SuppressLint("TrustAllX509TrustManager")
                        @Override
                        public void checkClientTrusted (java.security.cert.X509Certificate[] chain, String authType) throws CertificateException {
                        }

                        @SuppressLint("TrustAllX509TrustManager")
                        @Override
                        public void checkServerTrusted (java.security.cert.X509Certificate[] chain, String authType) throws CertificateException {
                        }

                        @Override
                        public java.security.cert.X509Certificate[] getAcceptedIssuers () {
                            return new java.security.cert.X509Certificate[]{};
                        }
                    }
            };

            // Install the all-trusting trust manager
            final SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(null, trustAllCerts, new java.security.SecureRandom());
            // Create an ssl socket factory with our all-trusting manager
            final SSLSocketFactory sslSocketFactory = sslContext.getSocketFactory();


            builder.sslSocketFactory(sslSocketFactory, (X509TrustManager) trustAllCerts[0]);
            builder.hostnameVerifier(new HostnameVerifier() {
                @SuppressLint("BadHostnameVerifier")
                @Override
                public boolean verify (String hostname, SSLSession session) {
                    return true;
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
        builder.connectTimeout(60, TimeUnit.SECONDS)
                .readTimeout(60, TimeUnit.SECONDS)
                .writeTimeout(60, TimeUnit.SECONDS)
                .retryOnConnectionFailure(true);
        return builder.build();
    }

    private static Request createRequest (
            @NonNull final String relativeUrl,
            @NonNull final String content
    ) {
        final RequestBody body = RequestBody.create(JSON_TYPE, content);
        return new Request.Builder().
                url(API_BASE_URL + relativeUrl).
                post(body).
                build();
    }

    private static Request createAuthRequest (
            @NonNull final String relativeUrl,
            @NonNull final String authToken,
            @NonNull final String content
    ) {
        final RequestBody body = RequestBody.create(JSON_TYPE, content);
        return new Request.Builder().
                url(API_BASE_URL + relativeUrl).
                header("Authorization", String.format("Token %s", authToken)).
                post(body).
                build();
    }

    private static Request createRequest (
            @NonNull final String relativeUrl
    ) {
        return new Request.Builder().
                url(API_BASE_URL + relativeUrl).
                build();
    }

    private static Request createAuthRequest (
            @NonNull final String relativeUrl,
            @NonNull final String authToken
    ) {
        return new Request.Builder().
                url(API_BASE_URL + relativeUrl).
                header("Authorization", String.format("Token %s", authToken)).
                build();
    }

    private static Response sendRequestAndGetRespone (
            @NonNull final Request request
    ) throws IOException {
        return generateDefaultOkHttp().newCall(request).execute();
    }

    public static ServerApiResponse sendRequestAndGetResponse (
            @NonNull final String relativeUrl,
            @NonNull final String content
    ) throws IOException {
        final Request request = createRequest(relativeUrl, content);
        final Response response = sendRequestAndGetRespone(request);
        return new ServerApiResponse(response.code(), response.body() != null ? response.body().string() : null);
    }

    public static ServerApiResponse sendAuthRequestAndGetResponse (
            @NonNull final String relativeUrl,
            @NonNull final String authToken,
            @NonNull final String content
    ) throws IOException {
        final Request request = createAuthRequest(relativeUrl, authToken, content);
        final Response response = sendRequestAndGetRespone(request);
        return new ServerApiResponse(response.code(), response.body() != null ? response.body().string() : null);
    }

    public static ServerApiResponse sendRequestAndGetResponse (
            @NonNull final String relativeUrl
    ) throws IOException {
        final Request request = createRequest(relativeUrl);
        final Response response = sendRequestAndGetRespone(request);
        return new ServerApiResponse(response.code(), response.body() != null ? response.body().string() : null);
    }

    public static ServerApiResponse sendAuthRequestAndGetResponse (
            @NonNull final String relativeUrl,
            @NonNull final String authToken
    ) throws IOException {
        final Request request = createAuthRequest(relativeUrl, authToken);
        final Response response = sendRequestAndGetRespone(request);
        return new ServerApiResponse(response.code(), response.body() != null ? response.body().string() : null);
    }

}
