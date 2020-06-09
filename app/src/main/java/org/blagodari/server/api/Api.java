package org.blagodari.server.api;

import android.annotation.SuppressLint;

import androidx.annotation.NonNull;

import org.blagodari.BuildConfig;

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
import okhttp3.Response;

public abstract class Api<InputType extends Api.DataIn, OutputType extends Api.Result> {

    private static final String API_BASE_URL = BuildConfig.DEBUG ? "https://api.dev.благодарие.рф/api/" : "https://api.благодарие.рф/api/";
    protected final static String DATA_MESSAGE = "message";
    static final MediaType JSON_TYPE = MediaType.parse("application/json; charset=utf-8");
    protected final String API_URL;

    Api (@NonNull final String relativeURL) {
        API_URL = API_BASE_URL + relativeURL;
    }

    @NonNull
    public abstract OutputType execute (InputType data);

    static Response sendRequestAndGetResponse (@NonNull final Request request) throws IOException {
        return generateDefaultOkHttp().newCall(request).execute();
    }

    @NonNull
    private static OkHttpClient generateDefaultOkHttp () {
        final OkHttpClient.Builder builder = new OkHttpClient.Builder();
        try {
            // Create a trust manager that does not validate certificate chains
            final TrustManager[] trustAllCerts = new TrustManager[]{
                    new X509TrustManager() {
                        @SuppressLint ("TrustAllX509TrustManager")
                        @Override
                        public void checkClientTrusted (java.security.cert.X509Certificate[] chain, String authType) throws CertificateException {
                        }

                        @SuppressLint ("TrustAllX509TrustManager")
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
            final SSLContext sslContext = SSLContext.getInstance("SSL");
            sslContext.init(null, trustAllCerts, new java.security.SecureRandom());
            // Create an ssl socket factory with our all-trusting manager
            final SSLSocketFactory sslSocketFactory = sslContext.getSocketFactory();


            builder.sslSocketFactory(sslSocketFactory, (X509TrustManager) trustAllCerts[0]);
            builder.hostnameVerifier(new HostnameVerifier() {
                @SuppressLint ("BadHostnameVerifier")
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

    public static class DataIn {
    }

    static class Result {

        Throwable mThrowable;

        Result () {
        }

        void setThrowable (final Throwable throwable) {
            this.mThrowable = throwable;
        }
    }
}
