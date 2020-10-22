package blagodarie.rating.model.entities;

import androidx.annotation.NonNull;

import java.io.Serializable;
import java.util.Date;
import java.util.Objects;
import java.util.UUID;

import blagodarie.rating.model.IWish;

public final class Wish
        implements IWish, Serializable {

    @NonNull
    private final UUID mUuid;

    @NonNull
    private final UUID mOwnerUuid;

    @NonNull
    private String mText;

    @NonNull
    private Date mLastEdit;

    public Wish (
            @NonNull final UUID uuid,
            @NonNull final UUID ownerUuid,
            @NonNull final String text,
            @NonNull final Date lastEdit
    ) {
        mUuid = uuid;
        mOwnerUuid = ownerUuid;
        mText = text;
        mLastEdit = lastEdit;
    }

    @NonNull
    public final UUID getUuid () {
        return mUuid;
    }

    @NonNull
    public final UUID getOwnerUuid () {
        return mOwnerUuid;
    }

    @NonNull
    public final String getText () {
        return mText;
    }

    @NonNull
    public final Date getLastEdit () {
        return mLastEdit;
    }

    public final void setText (@NonNull final String text) {
        mText = text;
    }

    public final void setLastEdit (@NonNull final Date lastEdit) {
        mLastEdit = lastEdit;
    }

    @Override
    public boolean equals (Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        final Wish wish = (Wish) o;
        return mUuid.equals(wish.mUuid);
    }

    @Override
    public int hashCode () {
        return Objects.hash(mUuid);
    }

    @Override
    public String toString () {
        return "Wish{" +
                "mUuid=" + mUuid +
                ", mOwnerUuid=" + mOwnerUuid +
                ", mText='" + mText + '\'' +
                ", mTimestamp=" + mLastEdit +
                '}';
    }

}
