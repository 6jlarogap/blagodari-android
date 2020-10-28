package blagodarie.rating.model;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.UUID;

public interface IProfile
        extends Identifiable<UUID> {

    @NonNull
    default String getFullName () {
        return getLastName() + " " + getFirstName();
    }

    @NonNull
    String getFirstName ();

    @NonNull
    String getLastName ();

    @Nullable
    String getPhoto ();

    int getSumThanksCount ();

    int getThanksCount ();

    int getFame ();

    int getTrustCount ();

    int getMistrustCount ();

    @Nullable
    Boolean isTrust ();
}
