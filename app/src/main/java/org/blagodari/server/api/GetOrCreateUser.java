package org.blagodari.server.api;

import androidx.annotation.NonNull;

import com.ex.diagnosticlib.Diagnostic;
import org.blagodari.DataRepository;
import org.blagodari.db.scheme.Keyz;
import org.blagodari.db.scheme.KeyzType;
import org.blagodari.db.scheme.User;
import org.blagodari.server.ResponseBodyException;
import org.blagodari.server.ResponseException;
import org.blagodari.server.ServerException;

import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Locale;

import okhttp3.Request;
import okhttp3.Response;

public final class GetOrCreateUser
        extends SyncDataApi {

    private static volatile GetOrCreateUser INSTANCE;

    private GetOrCreateUser () {
        super(GetOrCreateUser.class.getSimpleName().toLowerCase());
    }

    public static GetOrCreateUser getInstance () {
        synchronized (GetOrCreateUser.class) {
            if (INSTANCE == null) {
                INSTANCE = new GetOrCreateUser();
            }
        }
        return INSTANCE;
    }

    @NotNull
    @Override
    public final synchronized Result execute (
            @NonNull final DataIn dataIn
    ) {
        Diagnostic.i("start");
        final Result apiResult = new Result();
        try {
            final Keyz keyz = dataIn.getDataRepository().getKeyzByOwnerIdAndTypeId(dataIn.getUserId(), KeyzType.Types.GOOGLE_ACCOUNT_ID.getId()).get(0);

            final Request request = new Request.Builder()
                    .url(API_URL + String.format(Locale.ENGLISH, "?googleaccountid=%s", keyz.getValue()))
                    .build();
            final Response response = sendRequestAndGetResponse(request);
            handleResponse(
                    dataIn.getDataRepository(),
                    response,
                    dataIn.getUserId(),
                    keyz);
        } catch (Throwable throwable) {
            apiResult.setThrowable(throwable);
        }
        return apiResult;
    }

    private static void handleResponse (
            @NonNull final DataRepository dataRepository,
            @NonNull final Response response,
            @NonNull final Long userId,
            @NonNull final Keyz keyz
    ) throws IOException, JSONException, ServerException, ResponseException, ResponseBodyException {
        final User currentUser = dataRepository.getUser(userId);
        if (response.body() != null) {
            final String responseBody = response.body().string();
            Diagnostic.i("responseBody", responseBody);
            if (response.code() == 200) {
                final JSONObject rootJSON = new JSONObject(responseBody);
                final JSONObject userJSON = rootJSON.getJSONObject("user");
                final JSONObject keyzJSON = rootJSON.getJSONObject("keyz");
                currentUser.setServerId(userJSON.getLong("server_id"));
                keyz.setServerId(keyzJSON.getLong("server_id"));
                dataRepository.updateUser(currentUser);
                dataRepository.updateKeyz(keyz);
            } else if (response.code() == 400) {
                throw new ServerException(new JSONObject(responseBody).getString("message"));
            } else {
                throw new ResponseException();
            }
        } else {
            throw new ResponseBodyException();
        }
    }

}
