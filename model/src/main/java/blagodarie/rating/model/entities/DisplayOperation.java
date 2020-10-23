package blagodarie.rating.model.entities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.Date;
import java.util.UUID;

import blagodarie.rating.model.IDisplayOperation;

public final class DisplayOperation
        extends AbstractOperation
        implements IDisplayOperation {

    @NonNull
    private final UUID mIdTo;

    @Nullable
    private final String mPhoto;

    @NonNull
    private final String mLastName;

    @NonNull
    private final String mFirstName;

    public DisplayOperation (
            @NonNull final UUID userIdFrom,
            @NonNull final UUID idTo,
            @Nullable final String photo,
            @NonNull final String lastName,
            @NonNull final String firstName,
            @NonNull final OperationType operationType,
            @Nullable final String comment,
            @NonNull final Date timestamp
    ) {
        super(userIdFrom, operationType, comment, timestamp);
        mIdTo = idTo;
        mPhoto = photo;
        mLastName = lastName;
        mFirstName = firstName;
    }


    @NonNull
    public final UUID getIdTo () {
        return mIdTo;
    }

    @Nullable
    public String getPhoto () {
        return mPhoto;
    }

    @NonNull
    public String getLastName () {
        return mLastName;
    }

    @NonNull
    public String getFirstName () {
        return mFirstName;
    }

}
