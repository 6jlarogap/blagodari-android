package org.blagodari.server;

import androidx.annotation.NonNull;

import org.blagodari.DataRepository;
import org.blagodari.db.addent.ContactWithKeyz;
import org.blagodari.db.scheme.Keyz;
import org.blagodari.db.scheme.Like;
import org.blagodari.db.scheme.LikeKeyz;

import java.util.Locale;
import java.util.Objects;

public final class EntityToJsonConverter {

    private static final String OPENING_BRACKET = "{";

    private static final String CLOSING_BRACKET = "}";

    private static final String COMMA = ",";

    private EntityToJsonConverter () {
    }

    public static String keyzForGetOrCreateToJson (
            @NonNull final DataRepository dataRepository,
            @NonNull final Keyz keyz
    ) {
        return OPENING_BRACKET +
                toJsonUnit("id", keyz.getId()) +
                COMMA +
                toJsonUnit("owner_id", (keyz.getOwnerId() != null ? dataRepository.getUserServerId(keyz.getOwnerId()) : null)) +
                COMMA +
                toJsonUnit("value", keyz.getValue()) +
                COMMA +
                toJsonUnit("type_id", keyz.getTypeId()) +
                CLOSING_BRACKET;
    }

    public static String likeForAddToJson (
            @NonNull final DataRepository dataRepository,
            @NonNull final Like like
    ) {
        return OPENING_BRACKET +
                toJsonUnit("id", like.getId()) +
                COMMA +
                toJsonUnit("owner_id", dataRepository.getUserServerId(like.getOwnerId())) +
                COMMA +
                toJsonUnit("create_timestamp", (like.getCreateTimestamp() / 1000L)) +
                CLOSING_BRACKET;
    }

    public static String likeForCancelToJson (
            @NonNull final DataRepository dataRepository,
            @NonNull final Like like
    ) {
        return OPENING_BRACKET +
                toJsonUnit("owner_id", dataRepository.getUserServerId(like.getOwnerId())) +
                COMMA +
                toJsonUnit("server_id", like.getServerId()) +
                COMMA +
                toJsonUnit("cancel_timestamp", (like.getCancelTimestamp() / 1000L)) +
                CLOSING_BRACKET;
    }

    public static String likeKeyzForAddToJson (
            @NonNull final DataRepository dataRepository,
            @NonNull final LikeKeyz likeKeyz
    ) {
        return OPENING_BRACKET +
                toJsonUnit("id", likeKeyz.getId()) +
                COMMA +
                toJsonUnit("like_id", likeKeyz.getLikeId()) +
                COMMA +
                toJsonUnit("keyz_id", dataRepository.getKeyzServerId(likeKeyz.getKeyzId())) +
                CLOSING_BRACKET;
    }

    public static String likeKeyzForGetOrCreateToJson (
            @NonNull final DataRepository dataRepository,
            @NonNull final LikeKeyz likeKeyz
    ) {

        return OPENING_BRACKET +
                toJsonUnit("id", likeKeyz.getId()) +
                COMMA +
                toJsonUnit("like_id", dataRepository.getLikeServerId(likeKeyz.getLikeId())) +
                COMMA +
                toJsonUnit("keyz_id", dataRepository.getKeyzServerId(likeKeyz.getKeyzId())) +
                CLOSING_BRACKET;
    }

    public static String contactWithKeyzToJson (@NonNull final ContactWithKeyz contactWithKeyz) {
        final StringBuilder keyzJson = new StringBuilder();
        boolean isFirst = true;
        if (!contactWithKeyz.getKeyzSet().isEmpty()) {
            for (Keyz keyz : contactWithKeyz.getKeyzSet()) {
                if (!isFirst) {
                    keyzJson.append(',');
                } else {
                    isFirst = false;
                }
                keyzJson.append(keyzForGetContactSumInfoToJson(keyz));
            }
        }
        return String.format(Locale.ENGLISH, "{\"id\":%d,\"keyz\":[%s]}", contactWithKeyz.getContact().getId(), keyzJson.toString());
    }

    public static String likeKeyzForSetVagueToJson (@NonNull final LikeKeyz likeKeyz) {
        return OPENING_BRACKET +
                toJsonUnit("server_id", likeKeyz.getServerId()) +
                COMMA +
                toJsonUnit("vague", likeKeyz.getVague()) +
                CLOSING_BRACKET;
    }

    public static String keyzForGetContactSumInfoToJson (@NonNull final Keyz keyz) {
        return OPENING_BRACKET +
                toJsonUnit("value", keyz.getValue()) +
                COMMA +
                toJsonUnit("type_id", keyz.getTypeId()) +
                CLOSING_BRACKET;
    }

    private static String toJsonUnit (
            @NonNull final String name,
            final String value
    ) {
        return String.format("\"%s\":%s", name, value != null ? "\"" + value.replace("\"", "\\\"") + "\"" : "null");
    }

    private static String toJsonUnit (
            @NonNull final String name,
            final Long value
    ) {
        return String.format(Locale.ENGLISH, "\"%s\":%s", name, Objects.toString(value, "null"));
    }

    private static String toJsonUnit (
            @NonNull final String name,
            @NonNull final Boolean value
    ) {
        return String.format("\"%s\":%s", name, Objects.toString(value, "null"));
    }
}

