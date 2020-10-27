package blagodarie.rating.server;

import android.util.Log;

import androidx.annotation.NonNull;

import org.json.JSONException;

import java.util.Locale;

import blagodarie.rating.model.IKeyPair;
import okhttp3.Request;
import okhttp3.RequestBody;

public final class AddKeyRequest
        extends ServerApiRequest<AddKeyResponse> {

    private static final String TAG = AddKeyRequest.class.getSimpleName();

    @NonNull
    private final IKeyPair mKeyPair;

    public AddKeyRequest (
            @NonNull final IKeyPair keyPair
    ) {
        super("addkey");
        mKeyPair = keyPair;
    }

    @NonNull
    @Override
    Request.Builder getRequestBuilder () {
        final RequestBody body = RequestBody.create(JSON_TYPE, createContent());
        return createDefaultRequestBuilder().post(body);
    }

    @Override
    protected AddKeyResponse parseOkResponse (
            @NonNull final String responseBody
    ) throws JSONException {
        Log.d(TAG, "parseOkResponse responseBody=" + responseBody);
        return new AddKeyResponse();
    }

    private String createContent () {
        return String.format(Locale.ENGLISH, "{\"value\":\"%s\",\"type_id\":%d}", mKeyPair.getValue(), mKeyPair.getKeyType().getId());
    }
}
