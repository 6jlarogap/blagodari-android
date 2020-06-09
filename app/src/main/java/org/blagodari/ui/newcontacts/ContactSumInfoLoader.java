package org.blagodari.ui.newcontacts;

import android.annotation.SuppressLint;

import androidx.annotation.NonNull;
import androidx.databinding.ObservableBoolean;
import androidx.databinding.ObservableField;

import com.ex.diagnosticlib.Diagnostic;
import org.blagodari.db.addent.ContactWithKeyz;
import org.blagodari.db.scheme.Contact;
import org.blagodari.server.EntityToJsonConverter;
import org.blagodari.server.ResponseBodyException;
import org.blagodari.server.ResponseException;
import org.blagodari.server.ServerException;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.security.cert.CertificateException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
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

public class ContactSumInfoLoader {

    @NonNull
    private final Long mUserId;

/*
    @NonNull
    private final ContactWithKeyz mContactWithKeyz;*/

    ContactSumInfoLoader (
            @NonNull final Long userId
    ) {
        this.mUserId = userId;
        //this.mContactWithKeyz = contactWithKeyz;
    }


    public synchronized ContactWithKeyz call (ContactWithKeyz mContactWithKeyz, ObservableBoolean b, ObservableField<String> s) throws Exception {
        b.set(true);
        s.set(String.format("Получение данных о %s", mContactWithKeyz.getContact().getTitle()));
        List<ContactWithKeyz> contactWithKeyzList = new ArrayList<>();
        contactWithKeyzList.add(mContactWithKeyz);
        final String content = createRequestContent(contactWithKeyzList);
        Diagnostic.i("content", content);
        final MediaType JSON_TYPE = MediaType.parse("application/json; charset=utf-8");
        final RequestBody body = RequestBody.create(content, JSON_TYPE);
        final Request request = new Request.Builder()
                .url("https://api.dev.благодарие.рф/api/getcontactsuminfo")
                .post(body)
                .build();

        Response response = null;
        int attemptNumber = 1;
        while (response == null) {
            try {
                response = sendRequestAndGetResponse(request);
            } catch (SocketTimeoutException e) {
                s.set("Ждем 5 сек.");
                Thread.sleep(5000);
                s.set(String.format("Получение данных о %s (%d попытка)", mContactWithKeyz.getContact().getTitle(), attemptNumber++));
            }
        }
        if (response.body() != null) {
            final String responseBody = response.body().string();
            Diagnostic.i("responseBody", responseBody);
            if (response.code() == 200) {
                final JSONObject dataJson = new JSONObject(responseBody);
                final JSONArray contactArray = dataJson.getJSONArray("contacts");

                for (int i = 0; i < contactArray.length(); i++) {
                    final long contactId = contactArray.getJSONObject(i).getLong("id");
                    final long fame = contactArray.getJSONObject(i).getLong("fame");
                    final long likeCount = contactArray.getJSONObject(i).getLong("likes_count");
                    final long sumLikeCount = contactArray.getJSONObject(i).getLong("sum_likes_count");
                    final Contact contact = mContactWithKeyz.getContact();
                    contact.setFame(Math.max(fame, 1L));
                    contact.setLikeCount(likeCount);
                    contact.setSumLikeCount(sumLikeCount);
                }
            } else if (response.code() == 400) {
                b.set(false);
                throw new ServerException(new JSONObject(responseBody).getString("message"));
            } else {
                b.set(false);
                throw new ResponseException();
            }
        } else {
            b.set(false);
            throw new ResponseBodyException();
        }
        b.set(false);
        return mContactWithKeyz;
    }

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

    private String createRequestContent (@NonNull final List<ContactWithKeyz> contactWithKeyzList) {
        final StringBuilder content = new StringBuilder();
        content.append(String.format(Locale.ENGLISH,"{\"user_id\":%d,\"contacts\":[", this.mUserId));


        boolean isFirst = true;
        for (ContactWithKeyz contactWithKeyz : contactWithKeyzList) {
            if (!isFirst) {
                content.append(',');
            } else {
                isFirst = false;
            }
            content.append(EntityToJsonConverter.contactWithKeyzToJson(contactWithKeyz));
        }
        content.append("]}");
        return content.toString();
    }
}
