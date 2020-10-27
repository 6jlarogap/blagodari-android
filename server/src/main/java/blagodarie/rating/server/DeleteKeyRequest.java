package blagodarie.rating.server;

import android.util.Log;

import androidx.annotation.NonNull;

import org.json.JSONException;

import java.util.Locale;

import blagodarie.rating.model.IKey;
import okhttp3.Request;

public class DeleteKeyRequest
        extends ServerApiRequest<DeleteKeyResponse> {

    private static final String TAG = DeleteKeyRequest.class.getSimpleName();

    public DeleteKeyRequest (
            @NonNull final IKey key
    ) {
        super(String.format(Locale.ENGLISH, "deletekey?id=%d", key.getId()));
    }

    @NonNull
    @Override
    Request.Builder getRequestBuilder () {
        return createDefaultRequestBuilder();
    }

    @Override
    protected DeleteKeyResponse parseOkResponse (
            @NonNull final String responseBody
    ) throws JSONException {
        Log.d(TAG, "parseOkResponse responseBody=" + responseBody);
        return new DeleteKeyResponse();
    }

}
