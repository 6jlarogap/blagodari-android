package blagodarie.rating.model.entities;

import androidx.annotation.Keep;
import androidx.annotation.NonNull;

import java.io.Serializable;
import java.util.Date;
import java.util.UUID;

import blagodarie.rating.model.IAbility;

@Keep
public final class Ability
        implements IAbility, Serializable {

    @NonNull
    private final UUID mUuid;

    @NonNull
    private final UUID mOwnerUuid;

    @NonNull
    private String mText;

    @NonNull
    private Date mLastEdit;

    public Ability (
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

}
