package blagodarie.rating.model;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.Date;
import java.util.UUID;

import blagodarie.rating.model.entities.OperationType;

public interface IOperation {

    @NonNull
    UUID getUserIdFrom ();

    @Nullable
    UUID getIdTo ();

    @NonNull
    OperationType getOperationType ();

    @NonNull
    Date getTimestamp ();

    @NonNull
    String getComment ();

}
