package blagodarie.rating.model;

import androidx.annotation.NonNull;

public interface Identifiable<T> {

    @NonNull
    T getId ();

}
