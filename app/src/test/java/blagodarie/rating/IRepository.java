package blagodarie.rating;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.paging.PagedList;

import java.util.UUID;

import blagodarie.rating.model.entities.Wish;

public interface IRepository {

    LiveData<PagedList<Wish>> getUserWishes (@NonNull final UUID userId);

}
