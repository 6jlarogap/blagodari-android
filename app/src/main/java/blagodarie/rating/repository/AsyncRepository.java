package blagodarie.rating.repository;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.paging.DataSource;
import androidx.paging.PagedList;

import java.util.UUID;

import blagodarie.rating.model.IAbility;
import blagodarie.rating.model.IAnyTextInfo;
import blagodarie.rating.model.IKey;
import blagodarie.rating.model.IKeyPair;
import blagodarie.rating.model.IProfile;
import blagodarie.rating.model.IWish;
import blagodarie.rating.model.entities.OperationToAnyText;
import blagodarie.rating.model.entities.OperationToUser;

public interface AsyncRepository {

    interface OnCompleteListener {
        void onComplete ();
    }

    interface OnLoadListener<T> {
        void onLoad (@Nullable final T value);
    }

    interface OnErrorListener {
        void onError (@NonNull final Throwable throwable);
    }

    void insertOperationToUser (
            @NonNull final OperationToUser operation,
            @NonNull final OnCompleteListener onCompleteListener,
            @NonNull final OnErrorListener onErrorListener
    );

    void insertOperationToAnyText (
            @NonNull final OperationToAnyText operation,
            @NonNull final String anyText,
            @NonNull final OnCompleteListener onCompleteListener,
            @NonNull final OnErrorListener onErrorListener
    );

    void getProfileInfo (
            @NonNull final UUID userId,
            @NonNull final OnLoadListener<IProfile> onLoadListener,
            @NonNull final OnErrorListener onErrorListener
    );

    void getAnyTextInfo (
            @NonNull final String anyText,
            @NonNull final OnLoadListener<IAnyTextInfo> onLoadListener,
            @NonNull final OnErrorListener onErrorListener
    );

    void getWish (
            @NonNull final UUID wishId,
            @NonNull final OnLoadListener<IWish> onLoadListener,
            @NonNull final OnErrorListener onErrorListener
    );

    void upsertAbility (
            @NonNull final IAbility ability,
            @NonNull final OnCompleteListener onCompleteListener,
            @NonNull final OnErrorListener onErrorListener
    );

    void upsertWish (
            @NonNull final IWish wish,
            @NonNull final OnCompleteListener onCompleteListener,
            @NonNull final OnErrorListener onErrorListener
    );

    void insertKey (
            @NonNull final IKeyPair keyPair,
            @NonNull final OnCompleteListener onCompleteListener,
            @NonNull final OnErrorListener onErrorListener
    );

    void updateKey (
            @NonNull final IKey key,
            @NonNull final OnCompleteListener onCompleteListener,
            @NonNull final OnErrorListener onErrorListener
    );

    void deleteKey (
            @NonNull final IKey key,
            @NonNull final OnCompleteListener onCompleteListener,
            @NonNull final OnErrorListener onErrorListener
    );

    void deleteWish (
            @NonNull final UUID wishId,
            @NonNull final OnCompleteListener onCompleteListener,
            @NonNull final OnErrorListener onErrorListener
    );

    @NonNull
    <T> LiveData<PagedList<T>> getLiveDataPagedListFromDataSource (
            @NonNull final DataSource.Factory<Integer, T> dataSourceFactory
    );
}
