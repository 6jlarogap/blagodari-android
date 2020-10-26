package blagodarie.rating.server;

import android.util.Log;

import androidx.annotation.NonNull;

import java.util.Locale;

import okhttp3.Request;
import okhttp3.RequestBody;

public final class AddOperationToUserRequest
        extends ServerApiRequest<AddOperationToUserResponse> {

    private static final String TAG = AddOperationToUserRequest.class.getSimpleName();

    @NonNull
    private final blagodarie.rating.model.entities.OperationToUser mOperation;

    public AddOperationToUserRequest (
            @NonNull final blagodarie.rating.model.entities.OperationToUser operation
    ) {
        super("addoperation");
        mOperation = operation;
    }

    @NonNull
    @Override
    Request.Builder getRequestBuilder () {
        final RequestBody body = RequestBody.create(JSON_TYPE, createContent());
        return createDefaultRequestBuilder().post(body);
    }

    @Override
    protected AddOperationToUserResponse parseOkResponse (
            @NonNull final String responseBody
    ) {
        Log.d(TAG, "parseOkResponse responseBody=" + responseBody);
        return new AddOperationToUserResponse();
    }

    private String createContent () {
        return String.format(
                Locale.ENGLISH,
                "{\"user_id_to\":\"%s\",\"operation_type_id\":%d,\"timestamp\":%d,\"comment\":\"%s\"}",
                mOperation.getIdTo().toString(),
                mOperation.getOperationType().getId(),
                mOperation.getTimestamp().getTime(),
                mOperation.getComment()
        );
    }
}
