package blagodarie.rating;

import android.os.Handler;
import android.os.Looper;

import androidx.annotation.NonNull;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public final class AppExecutors {

    private static AppExecutors mInstance;

    private final Executor mDiskIO;

    private final Executor mNetworkIO;

    private final Executor mMainThread;

    public static AppExecutors getInstance () {
        if (mInstance == null) {
            synchronized (AppExecutors.class) {
                if (mInstance == null) {
                    mInstance = new AppExecutors();
                }
            }
        }
        return mInstance;
    }

    private AppExecutors (
            @NonNull final Executor diskIO,
            @NonNull final Executor networkIO,
            @NonNull final Executor mainThread
    ) {
        mDiskIO = diskIO;
        mNetworkIO = networkIO;
        mMainThread = mainThread;
    }

    public AppExecutors () {
        this(Executors.newSingleThreadExecutor(), Executors.newFixedThreadPool(3),
                new MainThreadExecutor());
    }

    public Executor diskIO () {
        return mDiskIO;
    }

    public Executor networkIO () {
        return mNetworkIO;
    }

    public Executor mainThread () {
        return mMainThread;
    }

    private static class MainThreadExecutor
            implements Executor {

        private final Handler mMainThreadHandler = new Handler(Looper.getMainLooper());

        @Override
        public void execute (
                @NonNull final Runnable command
        ) {
            mMainThreadHandler.post(command);
        }

    }
}
