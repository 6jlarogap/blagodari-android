package blagodarie.rating.repository;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.json.JSONException;

import java.io.IOException;
import java.util.UUID;

import blagodarie.rating.model.IAbility;
import blagodarie.rating.model.IAnyTextInfo;
import blagodarie.rating.model.IKey;
import blagodarie.rating.model.IKeyPair;
import blagodarie.rating.model.IProfile;
import blagodarie.rating.model.entities.OperationToAnyText;
import blagodarie.rating.model.entities.OperationToUser;
import blagodarie.rating.server.AddKeyRequest;
import blagodarie.rating.server.AddOrUpdateAbilityRequest;
import blagodarie.rating.server.DeleteKeyRequest;
import blagodarie.rating.server.GetAnyTextInfoRequest;
import blagodarie.rating.server.GetProfileInfoRequest;
import blagodarie.rating.server.HttpException;
import blagodarie.rating.server.ServerApiClient;
import blagodarie.rating.server.AddOperationToAnyTextRequest;
import blagodarie.rating.server.AddOperationToUserRequest;
import blagodarie.rating.server.UpdateKeyRequest;

public final class ServerRepository
        implements Repository,
        Authenticable {

    @Nullable
    private String mAuthToken;

    @Override
    public final void setAuthToken (@Nullable final String mAuthToken) {
        this.mAuthToken = mAuthToken;
    }

    @Override
    public void insertOperationToUser (
            @NonNull final OperationToUser operation
    ) throws JSONException, IOException, HttpException {
        final ServerApiClient client = new ServerApiClient(mAuthToken);
        final AddOperationToUserRequest request = new AddOperationToUserRequest(operation);
        client.execute(request);
    }

    @Override
    public void insertOperationToAnyText (
            @NonNull final OperationToAnyText operation,
            @NonNull final String anyText
    ) throws JSONException, IOException, HttpException {
        final ServerApiClient client = new ServerApiClient(mAuthToken);
        final AddOperationToAnyTextRequest request = new AddOperationToAnyTextRequest(operation, anyText);
        client.execute(request);
    }

    @Nullable
    @Override
    public IProfile getProfileInfo (
            @NonNull final UUID userId
    ) throws JSONException, IOException, HttpException {
        final ServerApiClient client = new ServerApiClient(mAuthToken);
        final GetProfileInfoRequest request = new GetProfileInfoRequest(userId);
        return client.execute(request).getProfileInfo();
    }

    @Nullable
    @Override
    public IAnyTextInfo getAnyTextInfo (
            @NonNull final String anyText
    ) throws JSONException, IOException, HttpException {
        final ServerApiClient client = new ServerApiClient(mAuthToken);
        final GetAnyTextInfoRequest request = new GetAnyTextInfoRequest(anyText);
        return client.execute(request).getAnyTextInfo();
    }

    @Override
    public void upsertAbility (
            @NonNull final IAbility ability
    ) throws JSONException, IOException, HttpException {
        final ServerApiClient client = new ServerApiClient(mAuthToken);
        final AddOrUpdateAbilityRequest request = new AddOrUpdateAbilityRequest(ability);
        client.execute(request);
    }

    @Override
    public void insertKey (
            @NonNull final IKeyPair keyPair
    ) throws JSONException, IOException, HttpException {
        final ServerApiClient client = new ServerApiClient(mAuthToken);
        final AddKeyRequest request = new AddKeyRequest(keyPair);
        client.execute(request);
    }

    @Override
    public void updateKey (
            @NonNull final IKey key
    ) throws JSONException, IOException, HttpException {
        final ServerApiClient client = new ServerApiClient(mAuthToken);
        final UpdateKeyRequest request = new UpdateKeyRequest(key);
        client.execute(request);
    }

    @Override
    public void deleteKey (
            @NonNull final IKey key
    ) throws JSONException, IOException, HttpException {
        final ServerApiClient client = new ServerApiClient(mAuthToken);
        final DeleteKeyRequest request = new DeleteKeyRequest(key);
        client.execute(request);
    }
}
