package org.blagodari.server.api;

import androidx.annotation.NonNull;

import org.blagodari.DataRepository;
import org.blagodari.db.scheme.Like;

import java.util.ArrayList;
import java.util.Collection;

public abstract class GetAllLikesApi
        extends Api<GetAllLikesApi.DataIn, GetAllLikesApi.Result> {

    GetAllLikesApi (@NonNull final String relativeURL) {
        super(relativeURL);
    }

    public static class DataIn
            extends Api.DataIn {

        @NonNull
        private final DataRepository mDataRepository;

        @NonNull
        private final Long mContactId;

        public DataIn (
                @NonNull final DataRepository dataRepository,
                @NonNull final Long contactId
        ) {
            this.mDataRepository = dataRepository;
            this.mContactId = contactId;
        }

        @NonNull
        public DataRepository getDataRepository () {
            return this.mDataRepository;
        }

        @NonNull
        public Long getContactId () {
            return this.mContactId;
        }
    }

    public static class Result
            extends Api.Result {

        @NonNull
        private Collection<Like> mLikes = new ArrayList<>();

        void setLikes (@NonNull final Collection<Like> likes) {
            this.mLikes = likes;
        }

        @NonNull
        public Collection<Like> getLikes () {
            return this.mLikes;
        }
    }
}
