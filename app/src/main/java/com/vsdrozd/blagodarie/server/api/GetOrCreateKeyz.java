package com.vsdrozd.blagodarie.server.api;

import android.util.LongSparseArray;

import androidx.annotation.NonNull;

import com.ex.diagnosticlib.Diagnostic;
import com.vsdrozd.blagodarie.DataRepository;
import com.vsdrozd.blagodarie.db.scheme.Keyz;
import com.vsdrozd.blagodarie.db.scheme.UserKeyz;
import com.vsdrozd.blagodarie.server.EntityToJsonConverter;
import com.vsdrozd.blagodarie.server.ResponseBodyException;
import com.vsdrozd.blagodarie.server.ResponseException;
import com.vsdrozd.blagodarie.server.ServerException;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public final class GetOrCreateKeyz
        extends SyncDataApi {

    private static volatile GetOrCreateKeyz INSTANCE;

    private GetOrCreateKeyz () {
        super(GetOrCreateKeyz.class.getSimpleName().toLowerCase());
    }

    public static GetOrCreateKeyz getInstance () {
        synchronized (GetOrCreateKeyz.class) {
            if (INSTANCE == null) {
                INSTANCE = new GetOrCreateKeyz();
            }
        }
        return INSTANCE;
    }

    @NotNull
    @Override
    public final synchronized Result execute (
            @NonNull final DataIn dataIn
    ) {
        Diagnostic.i("start");
        final Result apiResult = new Result();
        try {
            final List<Keyz> keyzList = dataIn.getDataRepository().getKeyzForGetOrCreate(dataIn.getUserId());
            if (!keyzList.isEmpty()) {
                final String content = createRequestContent(dataIn.getDataRepository(), dataIn.getUserId(), keyzList);
                Diagnostic.i("content", content);
                final RequestBody body = RequestBody.create(content, JSON_TYPE);
                final Request request = new Request.
                        Builder().
                        url(API_URL).
                        post(body).
                        build();

                final Response response = sendRequestAndGetResponse(request);

                handleResponse(
                        dataIn.getDataRepository(),
                        response,
                        keyzList,
                        dataIn.getUserId()
                );
            }
        } catch (Throwable throwable) {
            apiResult.setThrowable(throwable);
        }
        return apiResult;
    }

    private static String createRequestContent (
            @NonNull final DataRepository dataRepository,
            @NonNull final Long userId,
            @NonNull final List<Keyz> keyzList
    ) {
        final StringBuilder content = new StringBuilder();
        final Long userServerId = dataRepository.getUserServerId(userId);
        content.append(String.format(Locale.ENGLISH, "{\"user_id\":%d,\"keyz\":[", userServerId));

        boolean isFirst = true;
        for (Keyz keyz : keyzList) {
            if (!isFirst) {
                content.append(',');
            } else {
                isFirst = false;
            }
            content.append(EntityToJsonConverter.keyzForGetOrCreateToJson(dataRepository, keyz));
        }
        content.append("]}");
        return content.toString();
    }

    private static void handleResponse (
            @NonNull final DataRepository dataRepository,
            @NonNull final Response response,
            @NonNull final List<Keyz> keyzList,
            @NonNull final Long userId
    ) throws IOException, JSONException, ServerException, ResponseException, ResponseBodyException {
        if (response.body() != null) {
            final String responseBody = response.body().string();
            Diagnostic.i("responseBody", responseBody);
            if (response.code() == 200) {
                final List<UserKeyz> userKeyzList = dataRepository.getUserKeyzForGetOrCreate(userId);
                final LongSparseArray<Keyz> keyzById = createLongSparseArrayById(keyzList);
                final LongSparseArray<UserKeyz> userKeyzByKeyzId = createLongSparseArrayByKeyzId(userKeyzList);

                final JSONObject dataJson = new JSONObject(responseBody);
                final JSONArray keyzsArray = dataJson.getJSONArray("keyz");
                for (int i = 0; i < keyzsArray.length(); i++) {
                    final long id = keyzsArray.getJSONObject(i).getLong("id");
                    final Long serverId = keyzsArray.getJSONObject(i).getLong("server_id");
                    final Keyz keyz = keyzById.get(id);
                    if (keyz != null) {
                        keyz.setServerId(serverId);
                    }
                }
                final JSONArray userKeyzsArray = dataJson.getJSONArray("user_keyz");
                for (int i = 0; i < userKeyzsArray.length(); i++) {
                    final long keyzId = userKeyzsArray.getJSONObject(i).getLong("keyz_id");
                    final Long serverId = userKeyzsArray.getJSONObject(i).getLong("server_id");
                    final UserKeyz userKeyz = userKeyzByKeyzId.get(keyzId);
                    if (userKeyz != null) {
                        userKeyz.setServerId(serverId);
                    }
                }
                dataRepository.updateKeyz(keyzList);
                dataRepository.updateUserKeyz(userKeyzList);
                dataRepository.determineLikeContactId();
            } else if (response.code() == 400) {
                throw new ServerException(new JSONObject(responseBody).getString("message"));
            } else {
                throw new ResponseException();
            }
        } else {
            throw new ResponseBodyException();
        }
    }

    private static LongSparseArray<Keyz> createLongSparseArrayById (@NonNull final List<Keyz> keyzList) {
        final LongSparseArray<Keyz> keyzLongSparseArray = new LongSparseArray<>();
        for (Keyz keyz : keyzList) {
            keyzLongSparseArray.put(keyz.getId(), keyz);
        }
        return keyzLongSparseArray;
    }

    private static LongSparseArray<UserKeyz> createLongSparseArrayByKeyzId (@NonNull final List<UserKeyz> userKeyzList) {
        final LongSparseArray<UserKeyz> userKeyzLongSparseArray = new LongSparseArray<>();
        for (UserKeyz userKeyz : userKeyzList) {
            userKeyzLongSparseArray.put(userKeyz.getKeyzId(), userKeyz);
        }
        return userKeyzLongSparseArray;
    }
}
