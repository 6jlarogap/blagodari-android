package blagodarie.rating.repository;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.paging.PagedList;

import org.json.JSONException;

import java.io.IOException;
import java.util.UUID;

import blagodarie.rating.model.IAbility;
import blagodarie.rating.model.IAnyTextInfo;
import blagodarie.rating.model.IDisplayOperation;
import blagodarie.rating.model.IKey;
import blagodarie.rating.model.IKeyPair;
import blagodarie.rating.model.IProfileInfo;
import blagodarie.rating.model.IWish;
import blagodarie.rating.model.entities.OperationToAnyText;
import blagodarie.rating.model.entities.OperationToUser;
import blagodarie.rating.server.HttpException;

public interface Repository {

    void insertOperationToUser (
            @NonNull final OperationToUser operation
    ) throws JSONException, IOException, HttpException;

    void insertOperationToAnyText (
            @NonNull final OperationToAnyText operation,
            @NonNull final String anyText
    ) throws JSONException, IOException, HttpException;

    @Nullable
    IProfileInfo getProfileInfo (
            @NonNull final UUID userId
    ) throws JSONException, IOException, HttpException;

    @Nullable
    IAnyTextInfo getAnyTextInfo (
            @NonNull final String anyText
    ) throws JSONException, IOException, HttpException;

    void upsertAbility (
            @NonNull final IAbility ability
    ) throws JSONException, IOException, HttpException;

    void insertKey(
            @NonNull final IKeyPair keyPair
    ) throws JSONException, IOException, HttpException;

    void updateKey (
            @NonNull final IKey key
    ) throws JSONException, IOException, HttpException;

    void deleteKey (
            @NonNull final IKey key
    ) throws JSONException, IOException, HttpException;
}
