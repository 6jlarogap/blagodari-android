package blagodarie.rating.repository;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.paging.DataSource;
import androidx.paging.LivePagedListBuilder;
import androidx.paging.PagedList;

import java.util.UUID;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import blagodarie.rating.model.IAnyTextInfo;
import blagodarie.rating.model.IProfileInfo;
import blagodarie.rating.model.entities.OperationToAnyText;
import blagodarie.rating.model.entities.OperationToUser;

public final class AsyncServerRepository
        implements AsyncRepository,
        Authenticable {

    @NonNull
    private final ServerRepository mServerRepository = new ServerRepository();

    @NonNull
    private final Executor mExecutor;

    @NonNull
    private final Executor mMainThreadExecutor;

    public AsyncServerRepository (
            @NonNull final Executor executor,
            @NonNull final Executor mainThreadExecutor
    ) {
        mExecutor = executor;
        mMainThreadExecutor = mainThreadExecutor;
    }

    @Override
    public final void setAuthToken (@Nullable final String mAuthToken) {
        mServerRepository.setAuthToken(mAuthToken);
    }

    @Override
    public void insertOperationToUser (
            @NonNull OperationToUser operation,
            @NonNull final OnCompleteListener onCompleteListener,
            @NonNull final OnErrorListener onErrorListener
    ) {
        mExecutor.execute(() -> {
            try {
                mServerRepository.insertOperationToUser(operation);

                mMainThreadExecutor.execute(onCompleteListener::onComplete);
            } catch (Throwable throwable) {
                mMainThreadExecutor.execute(() -> onErrorListener.onError(throwable));
            }
        });
    }

    @Override
    public void insertOperationToAnyText (
            @NonNull final OperationToAnyText operation,
            @NonNull final String anyText,
            @NonNull final OnCompleteListener onCompleteListener,
            @NonNull final OnErrorListener onErrorListener
    ) {
        mExecutor.execute(() -> {
            try {
                mServerRepository.insertOperationToAnyText(operation, anyText);

                mMainThreadExecutor.execute(onCompleteListener::onComplete);
            } catch (Throwable throwable) {
                mMainThreadExecutor.execute(() -> onErrorListener.onError(throwable));
            }
        });
    }

    @Override
    public void getProfileInfo (
            @NonNull final UUID userId,
            @NonNull final OnLoadListener<IProfileInfo> onLoadListener,
            @NonNull final OnErrorListener onErrorListener
    ) {
        mExecutor.execute(() -> {
            try {
                final IProfileInfo profileInfo = mServerRepository.getProfileInfo(userId);

                mMainThreadExecutor.execute(() -> onLoadListener.onLoad(profileInfo));
            } catch (Throwable throwable) {
                mMainThreadExecutor.execute(() -> onErrorListener.onError(throwable));
            }
        });
    }

    @Override
    public void getAnyTextInfo (
            @NonNull final String anyText,
            @NonNull final OnLoadListener<IAnyTextInfo> onLoadListener,
            @NonNull final OnErrorListener onErrorListener
    ) {
        mExecutor.execute(() -> {
            try {
                final IAnyTextInfo anyTextInfo = mServerRepository.getAnyTextInfo(anyText);

                mMainThreadExecutor.execute(() -> onLoadListener.onLoad(anyTextInfo));
            } catch (Throwable throwable) {
                mMainThreadExecutor.execute(() -> onErrorListener.onError(throwable));
            }
        });
    }

    @NonNull
    @Override
    public <T> LiveData<PagedList<T>> getLiveDataPagedListFromDataSource(
            @NonNull final DataSource.Factory<Integer, T> dataSourceFactory
    ){
        final PagedList.Config config = new PagedList.Config.Builder()
                .setEnablePlaceholders(false)
                .setPageSize(10)
                .build();

        return new LivePagedListBuilder<>(dataSourceFactory, config).
                setFetchExecutor(mExecutor).
                build();
    }
}
