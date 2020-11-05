package blagodarie.rating.ui.user.abilities;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.paging.DataSource;
import androidx.paging.PositionalDataSource;

import java.util.UUID;

import blagodarie.rating.model.IAbility;
import blagodarie.rating.server.GetUserAbilitiesRequest;
import blagodarie.rating.server.GetUserAbilitiesResponse;
import blagodarie.rating.server.ServerApiClient;

public final class AbilitiesDataSource
        extends PositionalDataSource<IAbility> {

    private static final String TAG = AbilitiesDataSource.class.getSimpleName();

    @NonNull
    private final UUID mUserId;

    public AbilitiesDataSource (
            @NonNull final UUID userId
    ) {
        Log.d(TAG, "OperationDataSource");
        mUserId = userId;
    }

    @Override
    public void loadInitial (
            @NonNull final LoadInitialParams params,
            @NonNull final LoadInitialCallback<IAbility> callback
    ) {
        Log.d(TAG, "loadInitial from=" + params.requestedStartPosition + ", pageSize=" + params.pageSize);
        final ServerApiClient client = new ServerApiClient();
        final GetUserAbilitiesRequest request = new GetUserAbilitiesRequest(mUserId, params.requestedStartPosition, params.pageSize);
        try {
            final GetUserAbilitiesResponse response = client.execute(request);
            callback.onResult(response.getAbilities(), 0);
        } catch (Exception e) {
            Log.e(TAG, Log.getStackTraceString(e));
        }
    }

    @Override
    public void loadRange (
            @NonNull final LoadRangeParams params,
            @NonNull final LoadRangeCallback<IAbility> callback
    ) {
        Log.d(TAG, "loadRange startPosition=" + params.startPosition + ", loadSize=" + params.loadSize);
        final ServerApiClient client = new ServerApiClient();
        final GetUserAbilitiesRequest request = new GetUserAbilitiesRequest(mUserId, params.startPosition, params.loadSize);
        try {
            final GetUserAbilitiesResponse response = client.execute(request);
            callback.onResult(response.getAbilities());
        } catch (Exception e) {
            Log.e(TAG, Log.getStackTraceString(e));
        }
    }

    public static class AbilitiesDataSourceFactory
            extends Factory<Integer, IAbility> {

        @NonNull
        private final UUID mUserId;

        public AbilitiesDataSourceFactory (
                @NonNull final UUID userId
        ) {
            mUserId = userId;
        }

        @Override
        public DataSource<Integer, IAbility> create () {
            return new AbilitiesDataSource(mUserId);
        }

    }
}
