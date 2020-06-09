package com.vsdrozd.blagodarie.server.api;

import androidx.annotation.NonNull;

import com.ex.diagnosticlib.Diagnostic;
import com.vsdrozd.blagodarie.DataRepository;
import com.vsdrozd.blagodarie.db.scheme.Like;
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

public final class DeleteLikes
        extends SyncDataApi {

    private static volatile DeleteLikes INSTANCE;

    private DeleteLikes () {
        super(DeleteLikes.class.getSimpleName().toLowerCase());
    }

    public static DeleteLikes getInstance () {
        synchronized (DeleteLikes.class) {
            if (INSTANCE == null) {
                INSTANCE = new DeleteLikes();
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
            final List<Like> likeList = dataIn.getDataRepository().getLikeForDelete(dataIn.getUserId());
            if (!likeList.isEmpty()) {
                final String content = createRequestContent(likeList);
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
                        likeList
                );
            }
        } catch (Throwable throwable) {
            apiResult.setThrowable(throwable);
        }
        return apiResult;
    }

    private static String createRequestContent (
            @NonNull final List<Like> likesList
    ) {
        final StringBuilder content = new StringBuilder();
        content.append("{\"ids\":[");

        boolean isFirst = true;
        for (Like like : likesList) {
            if (!isFirst) {
                content.append(',');
            } else {
                isFirst = false;
            }
            content.append(like.getServerId());
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
                dataRepository.deleteLikes(likeList);
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
