package com.vsdrozd.blagodarie.server.api;

import androidx.annotation.NonNull;

import com.ex.diagnosticlib.Diagnostic;
import com.vsdrozd.blagodarie.DataRepository;
import com.vsdrozd.blagodarie.db.scheme.Like;
import com.vsdrozd.blagodarie.server.EntityToJsonConverter;
import com.vsdrozd.blagodarie.server.ResponseBodyException;
import com.vsdrozd.blagodarie.server.ResponseException;
import com.vsdrozd.blagodarie.server.ServerException;

import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.List;

import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public final class CancelLike
        extends SyncDataApi {

    private static volatile CancelLike INSTANCE;

    private CancelLike () {
        super(CancelLike.class.getSimpleName().toLowerCase());
    }

    public static CancelLike getInstance () {
        synchronized (CancelLike.class) {
            if (INSTANCE == null) {
                INSTANCE = new CancelLike();
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
            final List<Like> likesList = dataIn.getDataRepository().getLikeForCancelLike(dataIn.getUserId());
            if (!likesList.isEmpty()) {
                final String content = createRequestContent(dataIn.getDataRepository(), likesList);
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
                        likesList
                );
            }
        } catch (Throwable throwable) {
            apiResult.setThrowable(throwable);
        }
        return apiResult;
    }

    private static String createRequestContent (
            @NonNull final DataRepository dataRepository,
            @NonNull final List<Like> likesList
    ) {
        final StringBuilder content = new StringBuilder();
        content.append("{\"likes\":[");

        boolean isFirst = true;
        for (Like like : likesList) {
            if (!isFirst) {
                content.append(',');
            } else {
                isFirst = false;
            }
            content.append(EntityToJsonConverter.likeForCancelToJson(dataRepository, like));
        }
        content.append("]}");
        return content.toString();
    }

    private static void handleResponse (
            @NonNull final DataRepository dataRepository,
            @NonNull final Response response,
            @NonNull final List<Like> likeList
    ) throws IOException, JSONException, ServerException, ResponseException, ResponseBodyException {
        if (response.body() != null) {
            final String responseBody = response.body().string();
            Diagnostic.i("responseBody", responseBody);
            if (response.code() == 200) {
                long countCanceledLikes = new JSONObject(responseBody).getLong("count_cancelled_likes");
                if (countCanceledLikes == likeList.size()) {
                    for (Like l : likeList) {
                        l.setNeedSync(false);
                    }
                    dataRepository.updateLike(likeList);
                } else {
                    throw new ServerException("Cancel likes count mismatch");
                }
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
