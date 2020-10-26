package blagodarie.rating.server;

import android.util.Log;

import androidx.annotation.NonNull;

import org.json.JSONException;

import java.util.Locale;

import blagodarie.rating.model.IAbility;
import okhttp3.Request;
import okhttp3.RequestBody;

public class AddOrUpdateAbilityRequest
        extends ServerApiRequest<AddOrUpdateAbilityResponse> {

    private static final String TAG = AddOrUpdateAbilityRequest.class.getSimpleName();

    @NonNull
    private final IAbility mAbility;

    public AddOrUpdateAbilityRequest (
            @NonNull final IAbility ability
    ) {
        super("addorupdateability");
        mAbility = ability;
    }

    @NonNull
    @Override
    Request.Builder getRequestBuilder () {
        final RequestBody body = RequestBody.create(JSON_TYPE, createContent());
        return createDefaultRequestBuilder().post(body);
    }

    @Override
    protected AddOrUpdateAbilityResponse parseOkResponse (
            @NonNull final String responseBody
    ) throws JSONException {
        Log.d(TAG, "parseOkResponse responseBody=" + responseBody);
        return new AddOrUpdateAbilityResponse();
    }


    private String createContent () {
        return String.format(Locale.ENGLISH, "{\"uuid\":\"%s\",\"text\":\"%s\",\"last_edit\":\"%d\"}", mAbility.getUuid().toString(), mAbility.getText(), mAbility.getLastEdit().getTime());
    }
}
