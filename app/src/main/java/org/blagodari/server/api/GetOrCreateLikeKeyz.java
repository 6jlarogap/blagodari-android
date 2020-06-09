package org.blagodari.server.api;

import androidx.annotation.NonNull;
import androidx.collection.LongSparseArray;

import com.ex.diagnosticlib.Diagnostic;
import org.blagodari.DataRepository;
import org.blagodari.db.scheme.LikeKeyz;
import org.blagodari.server.EntityToJsonConverter;
import org.blagodari.server.ResponseBodyException;
import org.blagodari.server.ResponseException;
import org.blagodari.server.ServerException;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.List;

import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public final class GetOrCreateLikeKeyz
        extends SyncDataApi {

    private static volatile GetOrCreateLikeKeyz INSTANCE;

    private GetOrCreateLikeKeyz () {
        super(GetOrCreateLikeKeyz.class.getSimpleName().toLowerCase());
    }

    public static GetOrCreateLikeKeyz getInstance () {
        synchronized (GetOrCreateLikeKeyz.class) {
            if (INSTANCE == null) {
                INSTANCE = new GetOrCreateLikeKeyz();
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
            final List<LikeKeyz> likeKeyzList = dataIn.getDataRepository().getLikeKeyzForGetOrCreate(dataIn.getUserId());
            if (!likeKeyzList.isEmpty()) {
                final String content = createRequestContent(dataIn.getDataRepository(), likeKeyzList);
                Diagnostic.i("content", content);
                final RequestBody body = RequestBody.create(content, JSON_TYPE);
                final Request request = new Request.Builder()
                        .url(API_URL)
                        .post(body)
                        .build();

                final Response response = sendRequestAndGetResponse(request);

                handleResponse(dataIn.getDataRepository(), response, likeKeyzList);
            }
        } catch (Throwable throwable) {
            apiResult.setThrowable(throwable);
        }
        return apiResult;
    }

    private static String createRequestContent (
            @NonNull final DataRepository dataRepository,
            @NonNull final List<LikeKeyz> likeKeyzList
    ) {
        final StringBuilder content = new StringBuilder();
        content.append("{\"likekeyz\":[");

        boolean isFirst = true;
        for (LikeKeyz likeKeyz : likeKeyzList) {
            if (!isFirst) {
                content.append(',');
            } else {
                isFirst = false;
            }
            content.append(EntityToJsonConverter.likeKeyzForGetOrCreateToJson(dataRepository, likeKeyz));
        }
        content.append("]}");
        return content.toString();
    }

    private static void handleResponse (
            @NonNull final DataRepository dataRepository,
            @NonNull final Response response,
            @NonNull final List<LikeKeyz> likeKeyzList
    ) throws IOException, JSONException, ServerException, ResponseException, ResponseBodyException {
        if (response.body() != null) {
            String responseBody = response.body().string();
            Diagnostic.i("responseBody", responseBody);
            if (response.code() == 200) {
                final LongSparseArray<LikeKeyz> likeKeyzById = createLikeKeyzLongSparseArrayById(likeKeyzList);

                final JSONObject likeKeyzsJson = new JSONObject(responseBody);
                final JSONArray likeKeyzsArray = likeKeyzsJson.getJSONArray("likekeyz");
                for (int i = 0; i < likeKeyzsArray.length(); i++) {
                    final long id = likeKeyzsArray.getJSONObject(i).getLong("id");
                    final Long serverId = likeKeyzsArray.getJSONObject(i).getLong("server_id");
                    final LikeKeyz likeKeyz = likeKeyzById.get(id);
                    if (likeKeyz != null) {
                        likeKeyz.setServerId(serverId);
                        likeKeyz.setNeedSync(false);
                    }
                }
                dataRepository.updateLikeKeyz(likeKeyzList);
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

}