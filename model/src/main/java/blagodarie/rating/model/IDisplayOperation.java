package blagodarie.rating.model;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public interface IDisplayOperation
        extends IOperation {

    @Nullable
    String getPhoto ();

    @NonNull
    String getLastName ();

    @NonNull
    String getFirstName ();

}
