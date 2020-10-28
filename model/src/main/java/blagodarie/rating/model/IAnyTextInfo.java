package blagodarie.rating.model;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.UUID;

public interface IAnyTextInfo {

    @Nullable
    UUID getAnyTextId ();

    int getSumThanksCount ();

    int getThanksCount ();

    int getFame ();

    int getTrustCount ();

    int getMistrustCount ();

    @Nullable
    Boolean isTrust ();

}
