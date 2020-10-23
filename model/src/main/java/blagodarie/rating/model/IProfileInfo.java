package blagodarie.rating.model;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public interface IProfileInfo {

    @NonNull
    default String getFullName(){
        return getLastName() + " " + getFirstName();
    }

    @NonNull
    String getFirstName ();

    @NonNull
    String getLastName ();

    @Nullable
    String getPhoto ();

    int getSumThanksCount ();

    @Nullable
    Integer getThanksCount ();

    int getFame ();

    int getTrustCount ();

    int getMistrustCount ();

    @Nullable
    Boolean isTrust ();
}
