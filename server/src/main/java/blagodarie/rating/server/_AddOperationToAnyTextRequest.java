package blagodarie.rating.server;

import android.util.Log;

import androidx.annotation.NonNull;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Locale;
import java.util.UUID;

import okhttp3.Request;
import okhttp3.RequestBody;

public class _AddOperationToAnyTextRequest
        extends ServerApiRequest<AddOperationToAnyTextResponse> {

    private static final String TAG = _AddOperationToAnyTextRequest.class.getSimpleName();

    @NonNull
    private final blagodarie.rating.model.entities.OperationToAnyText mOperation;

    @NonNull
    private final String mAnyText;

    public _AddOperationToAnyTextRequest (
            @NonNull final blagodarie.rating.model.entities.OperationToAnyText operation,
            @NonNull final String anyText
    ) {
        super("addtextoperation");
        mOperation = operation;
        mAnyText = anyText;
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
                "{" + (mOperation.getIdTo() != null ? "\"text_id_to\":\"%s\"" : "\"text\":\"%s\"") + ",\"operation_type_id\":%d,\"timestamp\":%d,\"comment\":\"%s\"}",
                (mOperation.getIdTo() != null ? mOperation.getIdTo().toString() : mAnyText),
                mOperation.getOperationType().getId(),
                mOperation.getTimestamp().getTime(),
                mOperation.getComment()
        );
    }
}
