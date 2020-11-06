package blagodarie.rating.model;

import androidx.annotation.NonNull;

import java.util.Date;
import java.util.UUID;

public interface IWish
        extends Identifiable<UUID> {

    @NonNull
    UUID getOwnerUuid ();

    @NonNull
    String getText ();

    void setText (@NonNull final String text);

    @NonNull
    Date getLastEdit ();
}
