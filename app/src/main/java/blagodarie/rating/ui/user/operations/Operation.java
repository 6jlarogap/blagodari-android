package blagodarie.rating.ui.user.operations;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.Date;
import java.util.UUID;

import blagodarie.rating.model.entities.OperationType;

public final class Operation {

    @NonNull
    private final UUID mUserIdFrom;

    @NonNull
    private final UUID mUserIdTo;

    @Nullable
    private final String mPhoto;

    @NonNull
    private final String mLastName;

    @NonNull
    private final String mFirstName;

    @NonNull
    private final OperationType mOperationType;

    @Nullable
    private final String mComment;

    @NonNull
    private final Date mTimestamp;

    public Operation (
            @NonNull final UUID userIdFrom,
            @NonNull final UUID userIdTo,
            @Nullable final String photo,
            @NonNull final String lastName,
            @NonNull final String firstName,
            @NonNull final OperationType operationType,
            @Nullable final String comment,
            @NonNull final Date timestamp
    ) {
        mUserIdFrom = userIdFrom;
        mUserIdTo = userIdTo;
        mPhoto = photo;
        mLastName = lastName;
        mFirstName = firstName;
        mOperationType = operationType;
        mComment = comment;
        mTimestamp = timestamp;
    }

    @NonNull
    public final UUID getUserIdFrom () {
        return mUserIdFrom;
    }

    @NonNull
    public final UUID getUserIdTo () {
        return mUserIdTo;
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

    public final OperationType getOperationType () {
        return mOperationType;
    }

    @Nullable
    public final String getComment () {
        return mComment;
    }

    @NonNull
    public final Date getTimestamp () {
        return mTimestamp;
    }
}
