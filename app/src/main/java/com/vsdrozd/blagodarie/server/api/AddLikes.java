package com.vsdrozd.blagodarie.server.api;

import androidx.annotation.NonNull;
import androidx.collection.LongSparseArray;

import com.ex.diagnosticlib.Diagnostic;
import com.vsdrozd.blagodarie.DataRepository;
import com.vsdrozd.blagodarie.db.scheme.Like;
import com.vsdrozd.blagodarie.db.scheme.LikeKeyz;
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

import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public final class AddLikes
        extends SyncDataApi {

    private static volatile AddLikes INSTANCE;

    private AddLikes () {
        super(AddLikes.class.getSimpleName().toLowerCase());
    }

    public static AddLikes getInstance () {
        synchronized (AddLikes.class) {
            if (INSTANCE == null) {
                INSTANCE = new AddLikes();
            }
        }
        return INSTANCE;
    }

    @NotNull
    @Override
    public final synchronized Result execute (@NonNull final DataIn dataIn) {
        Diagnostic.i("start");
        final Result apiResult = new Result();
        try {
            final List<Like> likeList = dataIn.getDataRepository().getLikesForAddLike(dataIn.getUserId());
            if (!likeList.isEmpty()) {
                final List<LikeKeyz> likeKeyzList = dataIn.getDataRepository().getLikeKeyzForAddLike(dataIn.getUserId());
                final String content = createRequestContent(dataIn.getDataRepository(), likeList, likeKeyzList);
                Diagnostic.i("content", content);
                final RequestBody body = RequestBody.create(content, JSON_TYPE);
                final Request request = new Request.Builder()
                        .url(API_URL)
                        .post(body)
                        .build();

                final Response response = sendRequestAndGetResponse(request);

                handleResponse(
                        dataIn.getDataRepository(),
                        response,
                        likeList,
                        likeKeyzList);

            }
        } catch (Throwable throwable) {
            apiResult.setThrowable(throwable);
        }
        return apiResult;
    }

    private static String createRequestContent (
            @NonNull final DataRepository dataRepository,
            @NonNull final List<Like> likeList,
            @NonNull final List<LikeKeyz> likeKeyzList
    ) {
        final StringBuilder content = new StringBuilder();
        content.append("{\"likes\":[");

        boolean isFirst = true;
        for (Like like : likeList) {
            if (!isFirst) {
                content.append(',');
            } else {
                isFirst = false;
            }
            content.append(EntityToJsonConverter.likeForAddToJson(dataRepository, like));
        }

        content.append("],\"likekeyz\":[");
        isFirst = true;
        for (LikeKeyz likeKeyz : likeKeyzList) {
            if (!isFirst) {
                content.append(',');
            } else {
                isFirst = false;
            }
            content.append(EntityToJsonConverter.likeKeyzForAddToJson(dataRepository, likeKeyz));
        }
        content.append("]}");
        return content.toString();
    }

    private static void handleResponse (
            @NonNull final DataRepository dataRepository,
            @NonNull final Response response,
            @NonNull final List<Like> likeList,
            @NonNull final List<LikeKeyz> likeKeyzList
    ) throws IOException, JSONException, ServerException, ResponseException, ResponseBodyException {
        if (response.body() != null) {
            final String responseBody = response.body().string();
            Diagnostic.i("responseBody", responseBody);
            if (response.code() == 200) {
                final LongSparseArray<Like> likeById = createLikeLongSparseArrayById(likeList);
                final LongSparseArray<LikeKeyz> likeKeyzById = createLikeKeyzLongSparseArrayById(likeKeyzList);

                final JSONObject dataJson = new JSONObject(responseBody);
                final JSONArray likeArray = dataJson.getJSONArray("likes");
                for (int i = 0; i < likeArray.length(); i++) {
                    final long id = likeArray.getJSONObject(i).getLong("id");
                    final Long serverId = likeArray.getJSONObject(i).getLong("server_id");
                    final Like like = likeById.get(id);
                    if (like != null) {
                        like.setServerId(serverId);
                    }
                }
                final JSONArray likeKeyzArray = dataJson.getJSONArray("likekeyz");
                for (int i = 0; i < likeKeyzArray.length(); i++) {
                    final long id = likeKeyzArray.getJSONObject(i).getLong("id");
                    final Long serverId = likeKeyzArray.getJSONObject(i).getLong("server_id");
                    final LikeKeyz LikeKeyz = likeKeyzById.get(id);
                    if (LikeKeyz != null) {
                        LikeKeyz.setServerId(serverId);
                    }
                }
                dataRepository.updateLike(likeList);
                dataRepository.updateLikeKeyz(likeKeyzList);
            } else if (response.code() == 400) {
                throw new ServerException(new JSONObject(responseBody).getString("message"));
            } else {
                throw new ResponseException();
            }
        } else {
            throw new ResponseBodyException();
        }
    }

    private static LongSparseArray<Like> createLikeLongSparseArrayById (@NonNull final List<Like> likeList) {
        final LongSparseArray<Like> likeLongSparseArray = new LongSparseArray<>();
        for (Like like : likeList) {
            likeLongSparseArray.put(like.getId(), like);
        }
        return likeLongSparseArray;
    }
}
