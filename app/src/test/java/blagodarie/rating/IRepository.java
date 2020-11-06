package blagodarie.rating;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.paging.PagedList;

import java.util.UUID;

public interface IRepository {

    LiveData<PagedList<Wish>> getUserWishes (@NonNull final UUID userId);

}
