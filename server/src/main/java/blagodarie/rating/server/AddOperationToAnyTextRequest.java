package blagodarie.rating.server;

import android.util.Log;

import androidx.annotation.NonNull;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Locale;
import java.util.UUID;

import okhttp3.Request;
import okhttp3.RequestBody;

public final class AddOperationToAnyTextRequest
        extends ServerApiRequest<AddOperationToAnyTextResponse> {

    private static final String TAG = AddOperationToAnyTextRequest.class.getSimpleName();

    @NonNull
    private final OperationToAnyText mOperation;

    public AddOperationToAnyTextRequest (
            @NonNull final OperationToAnyText operation
    ) {
        super("addtextoperation");
        mOperation = operation;
    }

    @NonNull
    @Override
    Request.Builder getRequestBuilder () {
        final RequestBody body = RequestBody.create(JSON_TYPE, createContent());
        return createDefaultRequestBuilder().post(body);
    }

    @Override
    protected AddOperationToAnyTextResponse parseOkResponse (
            @NonNull final String responseBody
    ) throws JSONException {
        Log.d(TAG, "parseOkResponse responseBody=" + responseBody);
        final JSONObject json = new JSONObject(responseBody);
        final UUID textId = UUID.fromString(json.getString("text_id_to"));
        return new AddOperationToAnyTextResponse(textId);
    }

    private String createContent () {
        return String.format(
                Locale.ENGLISH,
                "{" + (mOperation.getAnyTextIdTo() != null ? "\"text_id_to\":\"%s\"" : "\"text\":\"%s\"") + ",\"operation_type_id\":%d,\"timestamp\":%d,\"comment\":\"%s\"}",
                (mOperation.getAnyTextIdTo() != null ? mOperation.getAnyTextIdTo().toString() : mOperation.getAnyText()),
                mOperation.getOperationTypeId(),
                mOperation.getTimestamp(),
                mOperation.getComment()
        );
    }
}
