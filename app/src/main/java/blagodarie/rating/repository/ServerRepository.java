package blagodarie.rating.repository;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.paging.LivePagedListBuilder;
import androidx.paging.PagedList;

import org.json.JSONException;

import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.Executors;

import blagodarie.rating.model.IWish;
import blagodarie.rating.model.entities.OperationToUser;
import blagodarie.rating.server.HttpException;
import blagodarie.rating.server.ServerApiClient;
import blagodarie.rating.server._AddOperationToUserRequest;
import blagodarie.rating.ui.user.wishes.WishesDataSource.WishesDataSourceFactory;

public final class ServerRepository
        implements Repository {

    @Nullable
    private String mAuthToken;

    public final void setAuthToken (@Nullable final String mAuthToken) {
        this.mAuthToken = mAuthToken;
    }

    @Override
    public LiveData<PagedList<IWish>> getUserWishes (@NonNull final UUID userId) {
        final WishesDataSourceFactory sourceFactory = new WishesDataSourceFactory(userId);

        final PagedList.Config config = new PagedList.Config.Builder()
                .setEnablePlaceholders(false)
                .setPageSize(10)
                .build();

        return new LivePagedListBuilder<>(sourceFactory, config).
                setFetchExecutor(Executors.newSingleThreadExecutor()).
                build();
    }

    @Override
    public void insertOperationToUser (
            @NonNull final OperationToUser operation
    ) throws JSONException, IOException, HttpException {
        final ServerApiClient client = new ServerApiClient(mAuthToken);
        final _AddOperationToUserRequest request = new _AddOperationToUserRequest(operation);
        client.execute(request);
    }
}
