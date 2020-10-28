package blagodarie.rating.server;

import android.util.Log;

import androidx.annotation.NonNull;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import blagodarie.rating.model.IDisplayOperation;
import blagodarie.rating.model.entities.DisplayOperation;
import blagodarie.rating.model.entities.OperationType;
import okhttp3.Request;
import okhttp3.RequestBody;

public final class GetUserOperationsRequest
        extends ServerApiRequest<GetOperationsResponse> {

    private static final String TAG = GetUserOperationsRequest.class.getSimpleName();

    @NonNull
    private final UUID mUserId;

    private final int mFrom;

    private final int mCount;

    public GetUserOperationsRequest (
            @NonNull final UUID userId,
            final int from,
            final int count
    ) {
        super("/getuseroperations");
        mUserId = userId;
        mFrom = from;
        mCount = count;
    }

    @NonNull
    @Override
    Request.Builder getRequestBuilder () {
        final RequestBody body = RequestBody.create(JSON_TYPE, createContent());
        return createDefaultRequestBuilder().post(body);
    }

    @Override
    protected GetOperationsResponse parseOkResponse (
            @NonNull final String responseBody
    ) throws JSONException {
        Log.d(TAG, "parseOkResponse responseBody=" + responseBody);
        final JSONArray json = new JSONObject(responseBody).getJSONArray("operations");
        final List<IDisplayOperation> operations = new ArrayList<>();
        for (int i = 0; i < json.length(); i++) {
            final JSONObject jsonArrayElement = json.getJSONObject(i);
            final UUID userIdFrom = UUID.fromString(jsonArrayElement.getString("user_id_from"));
            final int operationTypeId = jsonArrayElement.getInt("operation_type_id");
            final Date timestamp = new Date(jsonArrayElement.getLong("timestamp"));
            String comment = jsonArrayElement.getString("comment");
            if (comment.equals("null")) {
                comment = null;
            }
            String photo = jsonArrayElement.getString("photo");
            if (photo.equals("null")) {
                photo = null;
            }
            final String lastName = jsonArrayElement.getString("last_name");
            final String firstName = jsonArrayElement.getString("first_name");
            final OperationType operationType = OperationType.getById(operationTypeId);
            if (operationType != null) {
                operations.add(new DisplayOperation(userIdFrom, mUserId, photo, lastName, firstName, operationType, comment, timestamp));
            }
        }
        return new GetOperationsResponse(operations);
    }

    private String createContent () {
        return String.format(Locale.ENGLISH, "{\"uuid\":\"%s\",\"from\":%d,\"count\":%d}", mUserId, mFrom, mCount);
    }
}
