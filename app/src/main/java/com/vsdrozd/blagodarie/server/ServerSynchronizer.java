package com.vsdrozd.blagodarie.server;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;

import com.ex.diagnosticlib.Diagnostic;
import com.vsdrozd.blagodarie.DataRepository;
import com.vsdrozd.blagodarie.server.api.AddLikes;
import com.vsdrozd.blagodarie.server.api.CancelLike;
import com.vsdrozd.blagodarie.server.api.DeleteLikeKeyz;
import com.vsdrozd.blagodarie.server.api.DeleteLikes;
import com.vsdrozd.blagodarie.server.api.GetOrCreateKeyz;
import com.vsdrozd.blagodarie.server.api.GetOrCreateLikeKeyz;
import com.vsdrozd.blagodarie.server.api.GetOrCreateUser;
import com.vsdrozd.blagodarie.server.api.SyncDataApi;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Completable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

public final class ServerSynchronizer {

    private static volatile ServerSynchronizer INSTANCE;

    public interface ApiListener {
        void onStart (@NonNull final Class c);

        void onSuccess ();

        void onFailed (@NonNull final Throwable throwable);
    }

    private LiveData<Boolean> mGetOrCreateUserListener;
    private LiveData<Boolean> mGetOrCreateKeyzListener;
    private LiveData<Boolean> mGetOrCreateLikeKeyz;
    private LiveData<Boolean> mAddLikesListener;
    private LiveData<Boolean> mCancelLikeListener;
    private LiveData<Boolean> mDeleteLikeListener;
    private LiveData<Boolean> mDeleteLikeKeyzListener;

    private Observer<Boolean> mGetOrCreateUserObserver;
    private Observer<Boolean> mGetOrCreateKeyzObserver;
    private Observer<Boolean> mGetOrCreateLikeKeyzObserver;
    private Observer<Boolean> mAddLikesObserver;
    private Observer<Boolean> mCancelLikeObserver;
    private Observer<Boolean> mDeleteLikeObserver;
    private Observer<Boolean> mDeleteLikeKeyzObserver;

    private final List<ApiListener> mSyncDataApiListeners = new ArrayList<>();

    private ServerSynchronizer () {
    }

    public static ServerSynchronizer getInstance () {
        synchronized (ServerSynchronizer.class) {
            if (INSTANCE == null) {
                INSTANCE = new ServerSynchronizer();
            }
        }
        return INSTANCE;
    }

    public void start (
            @NonNull final DataRepository dataRepository,
            @NonNull final Long userId
    ) {
        Diagnostic.i();

        this.mGetOrCreateUserListener = dataRepository.isAuthorizedNotSyncedUser(userId);
        this.mGetOrCreateUserObserver = input -> {
            if (input) {
                startSyncDataApis(new SyncDataApi.DataIn(dataRepository, userId), GetOrCreateUser.getInstance());
            }
        };
        this.mGetOrCreateUserListener.observeForever(mGetOrCreateUserObserver);

        this.mGetOrCreateKeyzListener = dataRepository.isExistsKeyzForGetOrCreate(userId);
        this.mGetOrCreateKeyzObserver = input -> {
            if (input) {
                startSyncDataApis(new SyncDataApi.DataIn(dataRepository, userId), GetOrCreateKeyz.getInstance());
            }
        };
        this.mGetOrCreateKeyzListener.observeForever(mGetOrCreateKeyzObserver);

        this.mGetOrCreateLikeKeyz = dataRepository.isExistsLikeKeyzForGetOrCreate(userId);
        this.mGetOrCreateLikeKeyzObserver = input -> {
            if (input) {
                startSyncDataApis(new SyncDataApi.DataIn(dataRepository, userId), GetOrCreateLikeKeyz.getInstance());
            }
        };
        this.mGetOrCreateLikeKeyz.observeForever(mGetOrCreateLikeKeyzObserver);

        this.mAddLikesListener = dataRepository.isExistsLikeForAdd(userId);
        this.mAddLikesObserver = input -> {
            if (input) {
                startSyncDataApis(new SyncDataApi.DataIn(dataRepository, userId), AddLikes.getInstance());
            }
        };
        this.mAddLikesListener.observeForever(mAddLikesObserver);

        this.mCancelLikeListener = dataRepository.isExistsLikeForCancel(userId);
        this.mCancelLikeObserver = input -> {
            if (input) {
                startSyncDataApis(new SyncDataApi.DataIn(dataRepository, userId), CancelLike.getInstance());
            }
        };
        this.mCancelLikeListener.observeForever(mCancelLikeObserver);

        this.mDeleteLikeListener = dataRepository.isExistsLikeForDelete(userId);
        this.mDeleteLikeObserver = input -> {
            if (input) {
                startSyncDataApis(new SyncDataApi.DataIn(dataRepository, userId), DeleteLikes.getInstance());
            }
        };
        this.mDeleteLikeListener.observeForever(mDeleteLikeObserver);

        this.mDeleteLikeKeyzListener = dataRepository.isExistsLikeKeyzForDelete(userId);
        this.mDeleteLikeKeyzObserver = input -> {
            if (input) {
                startSyncDataApis(new SyncDataApi.DataIn(dataRepository, userId), DeleteLikeKeyz.getInstance());
            }
        };
        this.mDeleteLikeKeyzListener.observeForever(mDeleteLikeKeyzObserver);
    }

    public void stop () {
        Diagnostic.i();

        this.mGetOrCreateUserListener.removeObserver(mGetOrCreateUserObserver);
        this.mGetOrCreateUserListener = null;

        this.mGetOrCreateKeyzListener.removeObserver(mGetOrCreateKeyzObserver);
        this.mGetOrCreateKeyzListener = null;

        this.mAddLikesListener.removeObserver(mAddLikesObserver);
        this.mAddLikesListener = null;

        this.mCancelLikeListener.removeObserver(mCancelLikeObserver);
        this.mCancelLikeListener = null;

        this.mGetOrCreateLikeKeyz.removeObserver(mGetOrCreateLikeKeyzObserver);
        this.mGetOrCreateLikeKeyz = null;

        this.mDeleteLikeListener.removeObserver(mDeleteLikeObserver);
        this.mDeleteLikeListener = null;

        this.mDeleteLikeKeyzListener.removeObserver(mDeleteLikeKeyzObserver);
        this.mDeleteLikeKeyzListener = null;

        //this.mDisposables.dispose();
        //this.mDisposables.clear();
    }

    public final void startSyncDataApis (
            @Nullable final ApiListener listener,
            @NonNull final SyncDataApi.DataIn dataIn,
            @NonNull final SyncDataApi... apis
    ) {
        Completable.
                fromAction(() -> execApis(listener, dataIn, apis)).
                subscribeOn(Schedulers.io()).
                observeOn(AndroidSchedulers.mainThread()).
                subscribe();
    }

    public final void startSyncDataApis (
            @NonNull final SyncDataApi.DataIn dataIn,
            @NonNull final SyncDataApi... apis
    ) {
        startSyncDataApis(null, dataIn, apis);
    }

    private synchronized void execApis (
            @Nullable final ApiListener apiListener,
            @NonNull final SyncDataApi.DataIn dataIn,
            @NonNull final SyncDataApi... apis
    ) {
        for (SyncDataApi api : apis) {
            if (apiListener != null) {
                apiListener.onStart(api.getClass());
            }
            sendOnStart(api.getClass());
            try {
                api.execute(dataIn);
                if (apiListener != null) {
                    apiListener.onSuccess();
                }
                sendOnSuccess();
            } catch (Throwable throwable) {
                if (apiListener != null) {
                    apiListener.onFailed(throwable);
                }
                sendOnFailed(throwable);
            }
        }
    }

    private void sendOnStart (@NonNull final Class c) {
        for (ApiListener apiListener : this.mSyncDataApiListeners) {
            apiListener.onStart(c);
        }
    }

    private void sendOnSuccess () {
        for (ApiListener apiListener : this.mSyncDataApiListeners) {
            apiListener.onSuccess();
        }
    }

    private void sendOnFailed (Throwable throwable) {
        for (ApiListener apiListener : this.mSyncDataApiListeners) {
            apiListener.onFailed(throwable);
        }
    }

    public final void addApiListener (@NonNull final ApiListener apiListener) {
        this.mSyncDataApiListeners.add(apiListener);
    }

    public final void removeApiListener (@NonNull final ApiListener apiListener) {
        this.mSyncDataApiListeners.remove(apiListener);
    }
}
