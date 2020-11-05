package blagodarie.rating.server;

import android.util.Log;

import androidx.annotation.NonNull;

import org.json.JSONException;

import java.util.Locale;

import blagodarie.rating.model.IWish;
import okhttp3.Request;
import okhttp3.RequestBody;

public class AddOrUpdateWishRequest
        extends ServerApiRequest<AddOrUpdateWishResponse> {

    private static final String TAG = AddOrUpdateAbilityRequest.class.getSimpleName();

    @NonNull
    private final IWish mWish;

    public AddOrUpdateWishRequest (
            @NonNull final IWish wish
    ) {
        super("addorupdatewish");
        mWish = wish;
    }

    @NonNull
    @Override
    Request.Builder getRequestBuilder () {
        final RequestBody body = RequestBody.create(JSON_TYPE, createContent());
        return createDefaultRequestBuilder().post(body);
    }

    @Override
    protected AddOrUpdateWishResponse parseOkResponse (
            @NonNull final String responseBody
    ) throws JSONException {
        Log.d(TAG, "parseOkResponse responseBody=" + responseBody);
        return new AddOrUpdateWishResponse();
    }


    private String createContent () {
        return String.format(Locale.ENGLISH, "{\"uuid\":\"%s\",\"text\":\"%s\",\"last_edit\":\"%d\"}", mWish.getId().toString(), mWish.getText(), mWish.getLastEdit().getTime());
    }
}
