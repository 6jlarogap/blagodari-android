package blagodarie.rating.repository;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.paging.PagedList;

import org.json.JSONException;

import java.io.IOException;
import java.util.UUID;

import blagodarie.rating.model.IWish;
import blagodarie.rating.model.entities.OperationToUser;
import blagodarie.rating.server.HttpException;

public interface Repository {

    LiveData<PagedList<IWish>> getUserWishes (@NonNull final UUID userId);

    void insertOperationToUser (@NonNull final OperationToUser operation) throws JSONException, IOException, HttpException;

}
