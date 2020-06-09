package org.blagodari.server.api;

import androidx.annotation.NonNull;

import com.ex.diagnosticlib.Diagnostic;
import org.blagodari.DataRepository;
import org.blagodari.db.scheme.LikeKeyz;
import org.blagodari.server.ResponseBodyException;
import org.blagodari.server.ResponseException;
import org.blagodari.server.ServerException;

import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class DeleteLikeKeyz
        extends SyncDataApi {

    private static volatile DeleteLikeKeyz INSTANCE;

    private DeleteLikeKeyz () {
        super(DeleteLikeKeyz.class.getSimpleName().toLowerCase());
    }

    public static DeleteLikeKeyz getInstance () {
        synchronized (DeleteLikeKeyz.class) {
            if (INSTANCE == null) {
                INSTANCE = new DeleteLikeKeyz();
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
            final List<LikeKeyz> likeKeyzList = dataIn.getDataRepository().getLikeKeyzForDelete(dataIn.getUserId());
            if (!likeKeyzList.isEmpty()) {
                final String content = createRequestContent(likeKeyzList);
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
                        likeKeyzList
                );
            }
        } catch (Throwable throwable) {
            apiResult.setThrowable(throwable);
        }
        return apiResult;
    }

    private static String createRequestContent (
            @NonNull final List<LikeKeyz> likeKeyzList
    ) {
        final StringBuilder content = new StringBuilder();
        content.append("{\"ids\":[");

        boolean isFirst = true;
        for (LikeKeyz likeKeyz : likeKeyzList) {
            if (!isFirst) {
                content.append(',');
            } else {
                isFirst = false;
            }
            content.append(likeKeyz.getServerId());
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
            final String responseBody = response.body().string();
            Diagnostic.i("responseBody", responseBody);
            if (response.code() == 200) {
                final List<LikeKeyz> likeKeyzForUpdate = new ArrayList<>();
                final List<LikeKeyz> likeKeyzForDelete = new ArrayList<>();
                for (LikeKeyz likeKeyz : likeKeyzList) {
                    if (likeKeyz.getDeleted()) {
                        likeKeyzForDelete.add(likeKeyz);
                    } else {
                        likeKeyz.setNeedSync(false);
                        likeKeyzForUpdate.add(likeKeyz);
                    }
                }
                dataRepository.updateLikeKeyz(likeKeyzForUpdate);
                dataRepository.deleteLikeKeyz(likeKeyzForDelete);
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
