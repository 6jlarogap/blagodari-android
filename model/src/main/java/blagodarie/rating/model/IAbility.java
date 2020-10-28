package blagodarie.rating.model;

import androidx.annotation.NonNull;

import java.util.Date;
import java.util.UUID;

public interface IAbility
        extends Identifiable<UUID> {

    @NonNull
    UUID getOwnerUuid ();

    @NonNull
    String getText ();

    void setText (@NonNull final String text);

    @NonNull
    Date getLastEdit ();

    void setLastEdit (@NonNull final Date lastEdit);
}
