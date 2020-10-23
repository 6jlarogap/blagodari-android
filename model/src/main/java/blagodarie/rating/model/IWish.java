package blagodarie.rating.model;

import androidx.annotation.NonNull;

import java.util.Date;
import java.util.UUID;

public interface IWish {

    @NonNull
    UUID getUuid ();

    @NonNull
    UUID getOwnerUuid ();

    @NonNull
    String getText ();

    @NonNull
    Date getLastEdit ();
}
