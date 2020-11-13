package blagodarie.rating.server;

import android.util.Log;

import androidx.annotation.NonNull;

import org.json.JSONException;

import java.util.Locale;
import java.util.UUID;

import okhttp3.Request;

public class DeleteWishRequest
        extends ServerApiRequest<DeleteWishResponse> {

    private static final String TAG = DeleteWishRequest.class.getSimpleName();

    public DeleteWishRequest (
            @NonNull final UUID wishId
    ) {
        super(String.format(Locale.ENGLISH, "deletewish?uuid=%s", wishId.toString()));
    }

    @NonNull
    @Override
    Request.Builder getRequestBuilder () {
        return createDefaultRequestBuilder();
    }

    @Override
    protected DeleteWishResponse parseOkResponse (
            @NonNull final String responseBody
    ) throws JSONException {
        Log.d(TAG, "parseOkResponse responseBody=" + responseBody);
        return new DeleteWishResponse();
    }
}
