package blagodarie.rating.server;

import android.util.Log;

import androidx.annotation.NonNull;

import org.json.JSONException;

import java.util.Locale;

import blagodarie.rating.model.IKey;
import okhttp3.Request;
import okhttp3.RequestBody;

public final class UpdateKeyRequest
        extends ServerApiRequest<UpdateKeyResponse> {

    private static final String TAG = UpdateKeyRequest.class.getSimpleName();

    @NonNull
    private final IKey mKey;

    public UpdateKeyRequest (
            @NonNull final IKey key
    ) {
        super("updatekey");
        mKey = key;
    }

    @NonNull
    @Override
    Request.Builder getRequestBuilder () {
        final RequestBody body = RequestBody.create(JSON_TYPE, createContent());
        return createDefaultRequestBuilder().post(body);
    }

    @Override
    protected UpdateKeyResponse parseOkResponse (
            @NonNull final String responseBody
    ) throws JSONException {
        Log.d(TAG, "parseOkResponse responseBody=" + responseBody);
        return new UpdateKeyResponse();
    }

    private String createContent () {
        return String.format(Locale.ENGLISH, "{\"id\":%d,\"value\":\"%s\",\"type_id\":%d}", mKey.getId(), mKey.getValue(), mKey.getKeyType().getId());
    }
}
