package blagodarie.rating.model;

import androidx.annotation.NonNull;

import java.util.UUID;

public interface IKey
        extends IKeyPair,
        Identifiable<Long> {

    @NonNull
    UUID getOwnerId ();

}
