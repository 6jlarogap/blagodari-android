package com.vsdrozd.blagodarie.server.api;

import androidx.annotation.NonNull;

import com.ex.diagnosticlib.Diagnostic;
import com.vsdrozd.blagodarie.DataRepository;
import com.vsdrozd.blagodarie.db.addent.ContactWithKeyz;
import com.vsdrozd.blagodarie.db.scheme.Keyz;
import com.vsdrozd.blagodarie.db.scheme.Like;
import com.vsdrozd.blagodarie.server.ResponseBodyException;
import com.vsdrozd.blagodarie.server.ResponseException;
import com.vsdrozd.blagodarie.server.ServerException;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public final class GetLikesByKeyzId extends GetAllLikesApi {

    private static volatile GetLikesByKeyzId INSTANCE;

    private GetLikesByKeyzId () {
        super(GetLikesByKeyzId.class.getSimpleName().toLowerCase());
    }

    public static GetLikesByKeyzId getInstance () {
        synchronized (GetLikesByKeyzId.class) {
            if (INSTANCE == null) {
                INSTANCE = new GetLikesByKeyzId();
            }
        }
        return INSTANCE;
    }

    @NotNull
    @Override
    public Result execute (@NonNull final DataIn dataIn) {
        Diagnostic.i("start");
        final Result apiResult = new Result();
        try {
        Diagnostic.i("contactId", dataIn.getContactId());
        List<Like> allLikes = new ArrayList<>();
        final ContactWithKeyz contactWithKeyz = dataIn.getDataRepository().getContactWithKeyzByContactId(dataIn.getContactId());
        if (contactWithKeyz.getKeyzSet().size() > 0) {
            final String content = createRequestContent(contactWithKeyz);
            Diagnostic.i("content", content);
            final RequestBody body = RequestBody.create(content, JSON_TYPE);
            final Request request = new Request.Builder()
                    .url(API_URL)
                    .post(body)
                    .build();

            final Response response = sendRequestAndGetResponse(request);

            allLikes = handleResponse(response);
        }
        } catch (Throwable throwable) {
            apiResult.setThrowable(throwable);
        }
        return apiResult;
    }

    private static String createRequestContent (@NonNull final ContactWithKeyz contactWithKeyz) {
        final StringBuilder content = new StringBuilder();
        content.append("{\"keyz_ids\":[");

        boolean isFirst = true;
        for (Keyz keyz : contactWithKeyz.getKeyzSet()) {
            if (!isFirst) {
                content.append(',');
            } else {
                isFirst = false;
            }
            content.append(keyz.getServerId());
        }
        content.append("]}");
        return content.toString();
    }

    private static List<Like> handleResponse (
            @NonNull final Response response
    ) throws IOException, JSONException, ServerException, ResponseException, ResponseBodyException {
        List<Like> allLikes;
        if (response.body() != null) {
            final String responseBody = response.body().string();
            Diagnostic.i("responseBody", responseBody);
            if (response.code() == 200) {
                final JSONArray likesJSONArray = new JSONObject(responseBody).getJSONArray("likes");
                allLikes = createLikeListFromJson(likesJSONArray);
            } else if (response.code() == 400) {
                throw new ServerException(new JSONObject(responseBody).getString("message"));
            } else {
                throw new ResponseException();
            }
        } else {
            throw new ResponseBodyException();
        }
        return allLikes;
    }

    private static List<Like> createLikeListFromJson (
            @NonNull final JSONArray likesJSONArray
    ) throws JSONException {
        final List<Like> likeList = new ArrayList<>();
        for (int i = 0; i < likesJSONArray.length(); i++) {
            final Long serverId = likesJSONArray.getJSONObject(i).getLong("server_id");
            final Long ownerId = likesJSONArray.getJSONObject(i).getLong("owner_id");
            final Long createTimestamp = likesJSONArray.getJSONObject(i).getLong("create_timestamp") * 1000;
            final Long cancelTimestamp = likesJSONArray.getJSONObject(i).isNull("cancel_timestamp") ? null : likesJSONArray.getJSONObject(i).getLong("cancel_timestamp") * 1000;
            final Like like = new Like(ownerId, createTimestamp);
            like.setServerId(serverId);
            like.setCancelTimestamp(cancelTimestamp);
            likeList.add(like);
        }
        return likeList;
    }
}
