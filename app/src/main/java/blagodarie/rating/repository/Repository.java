package blagodarie.rating.repository;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.paging.PagedList;

import org.json.JSONException;

import java.io.IOException;
import java.util.UUID;

import blagodarie.rating.model.IAnyTextInfo;
import blagodarie.rating.model.IDisplayOperation;
import blagodarie.rating.model.IProfileInfo;
import blagodarie.rating.model.IWish;
import blagodarie.rating.model.entities.OperationToAnyText;
import blagodarie.rating.model.entities.OperationToUser;
import blagodarie.rating.server.HttpException;

public interface Repository {

    LiveData<PagedList<IWish>> getUserWishes (@NonNull final UUID userId);

    LiveData<PagedList<IDisplayOperation>> getUserOperations (@NonNull final UUID userId);

    LiveData<PagedList<IDisplayOperation>> getAnyTextOperations (@NonNull final UUID anyTextId);

    void insertOperationToUser (
            @NonNull final OperationToUser operation
    ) throws JSONException, IOException, HttpException;

    void insertOperationToAnyText (
            @NonNull final OperationToAnyText operation,
            @NonNull final String anyText
    ) throws JSONException, IOException, HttpException;

    @Nullable
    IProfileInfo getProfileInfo(
            @NonNull final UUID userId
    ) throws JSONException, IOException, HttpException;

    @Nullable
    IAnyTextInfo getAnyTextInfo(
            @NonNull final String anyText
    ) throws JSONException, IOException, HttpException;
}
