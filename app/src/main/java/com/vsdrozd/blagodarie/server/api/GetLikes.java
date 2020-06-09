package com.vsdrozd.blagodarie.server.api;

import androidx.annotation.NonNull;
import androidx.collection.LongSparseArray;

import com.ex.diagnosticlib.Diagnostic;
import com.vsdrozd.blagodarie.DataRepository;
import com.vsdrozd.blagodarie.db.addent.ContactWithKeyz;
import com.vsdrozd.blagodarie.db.addent.KeyzWithContacts;
import com.vsdrozd.blagodarie.db.scheme.Contact;
import com.vsdrozd.blagodarie.db.scheme.Keyz;
import com.vsdrozd.blagodarie.db.scheme.Like;
import com.vsdrozd.blagodarie.db.scheme.LikeKeyz;
import com.vsdrozd.blagodarie.db.scheme.User;
import com.vsdrozd.blagodarie.server.ResponseBodyException;
import com.vsdrozd.blagodarie.server.ResponseException;
import com.vsdrozd.blagodarie.server.ServerException;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;

import okhttp3.Request;
import okhttp3.Response;

public final class GetLikes
        extends SyncDataApi {

    private static volatile GetLikes INSTANCE;

    private GetLikes () {
        super(GetLikes.class.getSimpleName().toLowerCase());
    }

    public static GetLikes getInstance () {
        synchronized (GetLikes.class) {
            if (INSTANCE == null) {
                INSTANCE = new GetLikes();
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
            if (dataIn.getDataRepository().isUserSynced(dataIn.getUserId())) {
                final User currentUser = dataIn.getDataRepository().getUser(dataIn.getUserId());
                final Request request = new Request.Builder()
                        .url(API_URL + String.format(Locale.ENGLISH, "?ownerid=%d&synctimestamp=%d", currentUser.getServerId(), currentUser.getSyncTimestamp() / 1000))
                        .build();

                currentUser.setSyncTimestamp(System.currentTimeMillis());

                final Response response = sendRequestAndGetResponse(request);

                handleResponse(dataIn.getDataRepository(), response, currentUser);
            }
        } catch (Throwable throwable) {
            apiResult.setThrowable(throwable);
        }
        return apiResult;
    }

    //
    private static void handleResponse (
            @NonNull final DataRepository dataRepository,
            @NonNull final Response response,
            @NonNull final User currentUser
    ) throws IOException, JSONException, ServerException, ResponseException, ResponseBodyException {
        if (response.body() != null) {
            final String responseBody = response.body().string();
            Diagnostic.i("responseBody", responseBody);
            if (response.code() == 200) {
                final JSONArray likeKeyzJSONArray = new JSONObject(responseBody).getJSONArray("likekeyz");
                final LongSparseArray<List<Keyz>> keyzByLikeServerId = createKeyzByLikeIdFromJson(likeKeyzJSONArray);
                final LongSparseArray<List<LikeKeyz>> likeKeyzByLikeServerId = createLikeKeyzByLikeIdFromJson(likeKeyzJSONArray);

                final JSONArray likesJSONArray = new JSONObject(responseBody).getJSONArray("likes");
                final List<Like> likeList = createLikeListFromJson(currentUser.getId(), likesJSONArray);

                for (Like like : likeList) {
                    final List<Keyz> keyzList = keyzByLikeServerId.get(like.getServerId());
                    if (keyzList != null) {
                        dataRepository.insertKeyz(keyzList);
                        like.setContactId(dataRepository.findContactId(keyzList));

                        dataRepository.insertLike(like);

                        if (like.getContactId() != null) {
                            ContactWithKeyz contactWithKeyz = dataRepository.getContactWithKeyzByContactId(like.getContactId());
                            final List<LikeKeyz> likeKeyzList = new ArrayList<>();
                            for (Keyz keyz : contactWithKeyz.getKeyzSet()) {
                                likeKeyzList.add(new LikeKeyz(like.getId(), keyz.getId()));
                            }
                            dataRepository.insertLikeKeyz(likeKeyzList);
                            for (LikeKeyz likeKeyz : likeKeyzList) {
                                if (likeKeyz.getId() == null) {
                                    likeKeyz.setDeleted(false);
                                }
                            }
                            dataRepository.updateLikeKeyz(likeKeyzList);
                        } else {
                            final List<LikeKeyz> likeKeyzWithServerIdList = likeKeyzByLikeServerId.get(like.getServerId());
                            if (likeKeyzWithServerIdList != null) {
                                final List<LikeKeyz> likeKeyzWithLocalIdList = new ArrayList<>();
                                for (LikeKeyz likeKeyz : likeKeyzWithServerIdList) {
                                    likeKeyzWithLocalIdList.add(new LikeKeyz(
                                            dataRepository.getLikeIdByServerId(likeKeyz.getLikeId()),
                                            dataRepository.getKeyzIdByServerId(likeKeyz.getKeyzId())
                                    ));
                                }
                                dataRepository.insertLikeKeyz(likeKeyzWithLocalIdList);
                            }
                        }
                    } else {
                        like.setDeleted(true);
                        dataRepository.insertLike(like);
                    }

                }
                dataRepository.updateUser(currentUser);
                dataRepository.calcLikeCount();

            } else if (response.code() == 400) {
                throw new ServerException(new JSONObject(responseBody).getString("message"));
            } else {
                throw new ResponseException();
            }
        } else {
            throw new ResponseBodyException();
        }
    }

    private static LongSparseArray<List<Keyz>> createKeyzByLikeIdFromJson (@NonNull final JSONArray likeKeyzJSONArray) throws JSONException {
        final LongSparseArray<List<Keyz>> keyzByLikeServerId = new LongSparseArray<>();
        for (int i = 0; i < likeKeyzJSONArray.length(); i++) {
            final long likeServerId = likeKeyzJSONArray.getJSONObject(i).getLong("like_id");
            final Long keyzServerId = likeKeyzJSONArray.getJSONObject(i).getLong("keyz_id");
            final String value = likeKeyzJSONArray.getJSONObject(i).getString("value");
            final Long typeId = likeKeyzJSONArray.getJSONObject(i).getLong("type_id");
            final Keyz keyz = new Keyz(value, typeId);
            keyz.setServerId(keyzServerId);
            List<Keyz> keyzList = keyzByLikeServerId.get(likeServerId);
            if (keyzList == null) {
                keyzList = new ArrayList<>();
                keyzByLikeServerId.put(likeServerId, keyzList);
            }
            keyzList.add(keyz);
        }
        return keyzByLikeServerId;
    }

    private static LongSparseArray<List<LikeKeyz>> createLikeKeyzByLikeIdFromJson (@NonNull final JSONArray likeKeyzJSONArray) throws JSONException {
        final LongSparseArray<List<LikeKeyz>> LikeKeyzByLikeServerId = new LongSparseArray<>();
        for (int i = 0; i < likeKeyzJSONArray.length(); i++) {
            final Long serverId = likeKeyzJSONArray.getJSONObject(i).getLong("server_id");
            final long likeServerId = likeKeyzJSONArray.getJSONObject(i).getLong("like_id");
            final Long keyzServerId = likeKeyzJSONArray.getJSONObject(i).getLong("keyz_id");
            final LikeKeyz likeKeyz = new LikeKeyz(likeServerId, keyzServerId);
            likeKeyz.setServerId(serverId);
            List<LikeKeyz> likeKeyzList = LikeKeyzByLikeServerId.get(likeServerId);
            if (likeKeyzList == null) {
                likeKeyzList = new ArrayList<>();
                LikeKeyzByLikeServerId.put(likeServerId, likeKeyzList);
            }
            likeKeyzList.add(likeKeyz);
        }
        return LikeKeyzByLikeServerId;
    }

    private static List<Like> createLikeListFromJson (
            @NonNull final Long userId,
            @NonNull final JSONArray likesJSONArray
    ) throws JSONException {
        final List<Like> likeList = new ArrayList<>();
        for (int i = 0; i < likesJSONArray.length(); i++) {
            final Long serverId = likesJSONArray.getJSONObject(i).getLong("server_id");
            final Long createTimestamp = likesJSONArray.getJSONObject(i).getLong("create_timestamp") * 1000;
            final Long cancelTimestamp = likesJSONArray.getJSONObject(i).isNull("cancel_timestamp") ? null : likesJSONArray.getJSONObject(i).getLong("cancel_timestamp") * 1000;
            final Like like = new Like(userId, createTimestamp);
            like.setServerId(serverId);
            like.setCancelTimestamp(cancelTimestamp);
            likeList.add(like);
        }
        return likeList;
    }
}
