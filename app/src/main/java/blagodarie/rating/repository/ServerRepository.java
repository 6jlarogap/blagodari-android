package blagodarie.rating.repository;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.json.JSONException;

import java.io.IOException;
import java.util.UUID;

import blagodarie.rating.model.IAnyTextInfo;
import blagodarie.rating.model.IProfileInfo;
import blagodarie.rating.model.entities.OperationToAnyText;
import blagodarie.rating.model.entities.OperationToUser;
import blagodarie.rating.server.GetAnyTextInfoRequest;
import blagodarie.rating.server.GetProfileInfoRequest;
import blagodarie.rating.server.HttpException;
import blagodarie.rating.server.ServerApiClient;
import blagodarie.rating.server._AddOperationToAnyTextRequest;
import blagodarie.rating.server._AddOperationToUserRequest;

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
        final _AddOperationToUserRequest request = new _AddOperationToUserRequest(operation);
        client.execute(request);
    }

    @Override
    public void insertOperationToAnyText (
            @NonNull final OperationToAnyText operation,
            @NonNull final String anyText
    ) throws JSONException, IOException, HttpException {
        final ServerApiClient client = new ServerApiClient(mAuthToken);
        final _AddOperationToAnyTextRequest request = new _AddOperationToAnyTextRequest(operation, anyText);
        client.execute(request);
    }

    @Nullable
    @Override
    public IProfileInfo getProfileInfo (
            @NonNull final UUID userId
    ) throws JSONException, IOException, HttpException {
        final ServerApiClient client = new ServerApiClient(mAuthToken);
        final GetProfileInfoRequest request = new GetProfileInfoRequest(userId.toString());
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
}
