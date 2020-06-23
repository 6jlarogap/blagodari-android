package com.vsdrozd.blagodarie.server.api;

import androidx.annotation.NonNull;
import androidx.collection.LongSparseArray;

import com.vsdrozd.blagodarie.DataRepository;
import com.vsdrozd.blagodarie.db.scheme.LikeKeyz;
import com.vsdrozd.blagodarie.server.ResponseBodyException;
import com.vsdrozd.blagodarie.server.ResponseException;
import com.vsdrozd.blagodarie.server.ServerException;

import org.json.JSONException;

import java.io.IOException;
import java.util.List;

abstract public class SyncDataApi
        extends Api<SyncDataApi.DataIn, Api.Result> {

    SyncDataApi (String relativeURL) {
        super(relativeURL);
    }

    static LongSparseArray<LikeKeyz> createLikeKeyzLongSparseArrayById (@NonNull final List<LikeKeyz> likeKeyzList) {
        final LongSparseArray<LikeKeyz> likeKeyzLongSparseArray = new LongSparseArray<>();
        for (LikeKeyz likeKeyz : likeKeyzList) {
            likeKeyzLongSparseArray.put(likeKeyz.getId(), likeKeyz);
        }
        return likeKeyzLongSparseArray;
    }

    public static final class DataIn
            extends Api.DataIn {

        @NonNull
        private final DataRepository mDataRepository;

        @NonNull
        private final Long mUserId;

        public DataIn (
                @NonNull final DataRepository dataRepository,
                @NonNull final Long userId
        ) {
            this.mDataRepository = dataRepository;
            this.mUserId = userId;
        }

        @NonNull
        final DataRepository getDataRepository () {
            return this.mDataRepository;
        }

        @NonNull
        final Long getUserId () {
            return this.mUserId;
        }
    }
}
