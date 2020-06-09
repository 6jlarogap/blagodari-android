package org.blagodari.server.api;

import androidx.annotation.NonNull;
import androidx.collection.LongSparseArray;

import com.ex.diagnosticlib.Diagnostic;
import org.blagodari.DataRepository;
import org.blagodari.db.addent.ContactWithKeyz;
import org.blagodari.db.scheme.Contact;
import org.blagodari.server.EntityToJsonConverter;
import org.blagodari.server.ResponseBodyException;
import org.blagodari.server.ResponseException;
import org.blagodari.server.ServerException;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Collection;
import java.util.List;

import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;


public final class GetContactSumInfo
        extends Api<GetContactSumInfo.DataIn, Api.Result> {

    private static volatile GetContactSumInfo INSTANCE;

    private GetContactSumInfo () {
        super(GetContactSumInfo.class.getSimpleName().toLowerCase());
    }

    public static GetContactSumInfo getInstance () {
        synchronized (GetContactSumInfo.class) {
            if (INSTANCE == null) {
                INSTANCE = new GetContactSumInfo();
            }
        }
        return INSTANCE;
    }

    @NotNull
    @Override
    public Result execute (@NonNull final DataIn data) {
        Diagnostic.i("start");
        final Result apiResult = new Result();
        try {
            final List<ContactWithKeyz> contactWithKeyzList = data.mDataRepository.getContactsWithKeyzByContactIds(data.mContactIds);
            if (!contactWithKeyzList.isEmpty()) {
                final String content = createRequestContent(contactWithKeyzList);
                Diagnostic.i("content", content);
                final RequestBody body = RequestBody.create(content, JSON_TYPE);
                final Request request = new Request.Builder()
                        .url(API_URL)
                        .post(body)
                        .build();

                final Response response = sendRequestAndGetResponse(request);

                handleResponse(data.mDataRepository, response, contactWithKeyzList);
            }
        } catch (Throwable throwable) {
            apiResult.setThrowable(throwable);
        }
        return apiResult;
    }

    private static String createRequestContent (@NonNull final List<ContactWithKeyz> contactWithKeyzList) {
        final StringBuilder content = new StringBuilder();
        content.append("{\"contacts\":[");


        boolean isFirst = true;
        for (ContactWithKeyz contactWithKeyz : contactWithKeyzList) {
            if (!isFirst) {
                content.append(',');
            } else {
                isFirst = false;
            }
            content.append(EntityToJsonConverter.contactWithKeyzToJson(contactWithKeyz));
        }
        content.append("]}");
        return content.toString();
    }

    private static void handleResponse (
            @NonNull final DataRepository dataRepository,
            @NonNull final Response response,
            @NonNull final List<ContactWithKeyz> contactWithKeyzList
    ) throws IOException, JSONException, ServerException, ResponseException, ResponseBodyException {
        if (response.body() != null) {
            final String responseBody = response.body().string();
            Diagnostic.i("responseBody", responseBody);
            if (response.code() == 200) {
                final LongSparseArray<Contact> contactsById = createContactLongSparseArrayById(contactWithKeyzList);

                final JSONObject dataJson = new JSONObject(responseBody);
                final JSONArray contactArray = dataJson.getJSONArray("contacts");

                for (int i = 0; i < contactArray.length(); i++) {
                    final long contactId = contactArray.getJSONObject(i).getLong("id");
                    final long fame = contactArray.getJSONObject(i).getLong("fame");
                    final long sumLikeCount = contactArray.getJSONObject(i).getLong("sum_likes_count");
                    final Contact contact = contactsById.get(contactId);
                    if (contact != null) {
                        contact.setFame(Math.max(fame, 1L));
                        contact.setSumLikeCount(sumLikeCount);
                    }
                    dataRepository.updateContact(ContactWithKeyz.extractContactList(contactWithKeyzList));
                }
            } else if (response.code() == 400) {
                throw new ServerException(new JSONObject(responseBody).getString("message"));
            } else {
                throw new ResponseException();
            }
        } else {
            throw new ResponseBodyException();
        }
    }

    private static LongSparseArray<Contact> createContactLongSparseArrayById (@NonNull final List<ContactWithKeyz> contactWithKeyzList) {
        final LongSparseArray<Contact> contactLongSparseArray = new LongSparseArray<>();
        for (ContactWithKeyz contactWithKeyz : contactWithKeyzList) {
            contactLongSparseArray.put(contactWithKeyz.getContact().getId(), contactWithKeyz.getContact());
        }
        return contactLongSparseArray;
    }

    public static class DataIn
            extends Api.DataIn {

        @NonNull
        private final DataRepository mDataRepository;

        @NonNull
        private final Collection<Long> mContactIds;

        public DataIn (
                @NonNull final DataRepository dataRepository,
                @NonNull final Collection<Long> contactIds
        ) {
            this.mDataRepository = dataRepository;
            this.mContactIds = contactIds;
        }
    }
}
