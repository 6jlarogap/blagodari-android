package blagodarie.rating.server;

import android.annotation.SuppressLint;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.json.JSONException;

import java.io.IOException;
import java.security.cert.CertificateException;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public final class ServerApiClient {

    private static final String TAG = ServerApiClient.class.getSimpleName();

    @Nullable
    private final String mAuthToken;

    public ServerApiClient () {
        this(null);
    }

    public ServerApiClient (
            @Nullable final String mAuthToken
    ) {
        this.mAuthToken = mAuthToken;
    }

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

    private static Response sendRequestAndGetResponse (
            @NonNull final Request request
    ) throws IOException {
        return generateDefaultOkHttp().newCall(request).execute();
    }

    private boolean isAuthorized () {
        return mAuthToken != null && !mAuthToken.isEmpty();
    }

    public <T extends _ServerApiResponse> T execute (
            @NonNull final ServerApiRequest<T> apiRequest
    ) throws IOException, JSONException, HttpException {
        final Request.Builder builder = apiRequest.getRequestBuilder();

        if (isAuthorized()) {
            builder.addHeader("Authorization", String.format("Token %s", mAuthToken));
        }

        final Request request = builder.build();
        Log.d(TAG, "execute request=" + request.toString());
        return apiRequest.parseResponse(sendRequestAndGetResponse(request));
    }
}
