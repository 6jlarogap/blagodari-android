package blagodarie.rating.model;

import java.util.Date;
import java.util.UUID;

public interface IWish {

    UUID getUuid ();

    UUID getOwnerUuid ();

    String getText ();

    Date getLastEdit ();
}
