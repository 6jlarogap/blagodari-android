package blagodarie.rating.repository;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.concurrent.Executor;

import blagodarie.rating.model.entities.OperationToAnyText;
import blagodarie.rating.model.entities.OperationToUser;

public final class AsyncServerRepository
        implements AsyncRepository {

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

    public final void setAuthToken (@Nullable final String mAuthToken) {
        mServerRepository.setAuthToken(mAuthToken);
    }

    @Override
    public void insertOperationToUser (
            @NonNull OperationToUser operation,
            @NonNull final OnCompleteListener<Void> onCompleteListener,
            @NonNull final OnErrorListener onErrorListener
    ) {
        mExecutor.execute(() -> {
            try {
                mServerRepository.insertOperationToUser(operation);

                mMainThreadExecutor.execute(() -> onCompleteListener.onComplete(null));
            } catch (Throwable throwable) {
                mMainThreadExecutor.execute(() -> onErrorListener.onError(throwable));
            }
        });
    }

    @Override
    public void insertOperationToAnyText (@NonNull OperationToAnyText operation, @Nullable String anyText) {

    }
}
