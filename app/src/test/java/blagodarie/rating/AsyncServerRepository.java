package blagodarie.rating;

import androidx.annotation.NonNull;

import java.util.List;
import java.util.concurrent.Executor;

public final class AsyncServerRepository
        implements IAsyncRepository {

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

    void getValues (OnCompleteListener<List<String>> onCompleteListener, OnErrorListener onErrorListener) {
        mExecutor.execute(() -> {
            int a = 2 / 0;
        });
    }
}
