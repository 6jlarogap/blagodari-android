package blagodarie.rating.model;

import androidx.annotation.NonNull;

import blagodarie.rating.model.entities.KeyType;

public interface IKeyPair {

    @NonNull
    KeyType getKeyType ();

    @NonNull
    String getValue ();

}
