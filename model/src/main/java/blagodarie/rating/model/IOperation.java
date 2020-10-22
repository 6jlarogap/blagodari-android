package blagodarie.rating.model;

import java.util.Date;
import java.util.UUID;

import blagodarie.rating.model.entities.OperationType;

public interface IOperation {

    UUID getUserIdFrom ();

    UUID getIdTo ();

    OperationType getOperationType ();

    Date getTimestamp ();

    String getComment ();

}
