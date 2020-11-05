package blagodarie.rating.ui.user.anytext;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.paging.DataSource;
import androidx.paging.PositionalDataSource;

import blagodarie.rating.server.GetThanksUsersForAnyTextRequest;
import blagodarie.rating.server.GetThanksUsersResponse;
import blagodarie.rating.server.ServerApiClient;

public final class ThanksUsersForAnyTextDataSource
        extends PositionalDataSource<GetThanksUsersResponse.ThanksUser> {

    private static final String TAG = ThanksUsersForAnyTextDataSource.class.getSimpleName();

    @NonNull
    private final String mAnyText;

    ThanksUsersForAnyTextDataSource (
            @NonNull final String anyText
    ) {
        Log.d(TAG, "ThanksUsersForAnyTextDataSource");
        mAnyText = anyText;
    }

    @Override
    public void loadInitial (
            @NonNull final LoadInitialParams params,
            @NonNull final LoadInitialCallback<GetThanksUsersResponse.ThanksUser> callback
    ) {
        Log.d(TAG, "loadInitial from=" + params.requestedStartPosition + ", pageSize=" + params.pageSize);
        final ServerApiClient client = new ServerApiClient();
        final GetThanksUsersForAnyTextRequest request = new GetThanksUsersForAnyTextRequest(mAnyText, params.requestedStartPosition, params.pageSize);
        try {
            final GetThanksUsersResponse response = client.execute(request);
            callback.onResult(response.getThanksUsers(), 0);
        } catch (Exception e) {
            Log.e(TAG, Log.getStackTraceString(e));
        }
    }

    @Override
    public void loadRange (
            @NonNull final LoadRangeParams params,
            @NonNull final LoadRangeCallback<GetThanksUsersResponse.ThanksUser> callback
    ) {
        Log.d(TAG, "loadRange startPosition=" + params.startPosition + ", loadSize=" + params.loadSize);
        final ServerApiClient client = new ServerApiClient();
        final GetThanksUsersForAnyTextRequest request = new GetThanksUsersForAnyTextRequest(mAnyText, params.startPosition, params.loadSize);
        try {
            final GetThanksUsersResponse response = client.execute(request);
            callback.onResult(response.getThanksUsers());
        } catch (Exception e) {
            Log.e(TAG, Log.getStackTraceString(e));
        }
    }

    public static class ThanksUserForAnyTextDataSourceFactory
            extends Factory<Integer, GetThanksUsersResponse.ThanksUser> {

        @NonNull
        private final String mAnyText;

        public ThanksUserForAnyTextDataSourceFactory (
                @NonNull final String anyText
        ) {
            mAnyText = anyText;
        }

        @Override
        public DataSource<Integer, GetThanksUsersResponse.ThanksUser> create () {
            return new ThanksUsersForAnyTextDataSource(mAnyText);
        }

    }
}
