package blagodarie.rating;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.paging.LivePagedListBuilder;
import androidx.paging.PagedList;

import java.util.UUID;
import java.util.concurrent.Executors;

import blagodarie.rating.model.entities.Wish;
import blagodarie.rating.ui.wishes.WishesDataSource;

public final class ServerRepository
        implements IRepository {

    @Override
    public LiveData<PagedList<Wish>> getUserWishes (@NonNull final UUID userId) {
        final WishesDataSource.WishesDataSourceFactory sourceFactory = new WishesDataSource.WishesDataSourceFactory(userId);

        final PagedList.Config config = new PagedList.Config.Builder()
                .setEnablePlaceholders(false)
                .setPageSize(10)
                .build();

        return new LivePagedListBuilder<>(sourceFactory, config).
                setFetchExecutor(Executors.newSingleThreadExecutor()).
                build();
    }
}
