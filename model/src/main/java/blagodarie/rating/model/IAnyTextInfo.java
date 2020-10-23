package blagodarie.rating.model;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.UUID;

public interface IAnyTextInfo {

    @Nullable
    UUID getAnyTextId ();

    int getSumThanksCount ();

    @Nullable
    Integer getThanksCount ();

    int getFame ();

    int getTrustCount ();

    int getMistrustCount ();

    @Nullable
    Boolean isTrust ();

}
