package blagodarie.rating.server;

import androidx.annotation.NonNull;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import okhttp3.Request;

public final class GetProfileInfoRequest
        extends ServerApiRequest<GetProfileInfoResponse> {

    public GetProfileInfoRequest (
            @NonNull final String userId
    ) {
        super("getprofileinfo?uuid=" + userId);
    }

    @NonNull
    @Override
    Request.Builder getRequestBuilder () {
        return createDefaultRequestBuilder();
    }

    @Override
    protected GetProfileInfoResponse parseOkResponse (
            @NonNull final String responseBody
    ) throws JSONException {
        final JSONObject json = new JSONObject(responseBody);

        final String photo = json.getString("photo");
        final String firstName = json.getString("first_name");
        final String middleName = json.getString("middle_name");
        final String lastName = json.getString("last_name");
        String cardNumber;
        try {
            cardNumber = json.getString("credit_card");
        } catch (JSONException e) {
            cardNumber = "";
        }
        final int fame = json.getInt("fame");
        final int sumThanksCount = json.getInt("sum_thanks_count");
        final int mistrustCount = json.getInt("trustless_count");
        final int trustCount = fame - mistrustCount;//json.getInt("trust_count");
        Integer thanksCount;
        try {
            thanksCount = json.getInt("thanks_count");
        } catch (JSONException e) {
            thanksCount = null;
        }
        Boolean isTrust;
        try {
            isTrust = json.getBoolean("is_trust");
        } catch (JSONException e) {
            isTrust = null;
        }
        final List<GetProfileInfoResponse.ThanksUser> thanksUsers = new ArrayList<>();
        final JSONArray thanksUsersJSONArray = json.getJSONArray("thanks_users");
        for (int i = 0; i < thanksUsersJSONArray.length(); i++) {
            final JSONObject thanksUserJSONObject = thanksUsersJSONArray.getJSONObject(i);
            final String thanksUserPhoto = thanksUserJSONObject.getString("photo");
            final String thanksUserIdString = thanksUserJSONObject.getString("user_uuid");
            final UUID thanksUserId = UUID.fromString(thanksUserIdString);
            thanksUsers.add(new GetProfileInfoResponse.ThanksUser(thanksUserId, thanksUserPhoto));
        }
        return new GetProfileInfoResponse(
                photo,
                firstName,
                middleName,
                lastName,
                cardNumber,
                fame,
                sumThanksCount,
                trustCount,
                mistrustCount,
                thanksCount,
                isTrust,
                thanksUsers
        );
    }
}
