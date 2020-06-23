package com.vsdrozd.blagodarie.db.addent;

import androidx.annotation.NonNull;
import androidx.room.Embedded;
import androidx.room.Junction;
import androidx.room.Relation;

import com.vsdrozd.blagodarie.db.scheme.Keyz;
import com.vsdrozd.blagodarie.db.scheme.Like;
import com.vsdrozd.blagodarie.db.scheme.LikeKeyz;

import java.util.Set;

public class LikeWithKeyz {
    @NonNull
    @Embedded
    private final Like Like;

    @NonNull
    @Relation (
            parentColumn = "id",
            entity = Keyz.class,
            entityColumn = "id",
            associateBy = @Junction (
                    value = LikeKeyz.class,
                    parentColumn = "like_id",
                    entityColumn = "keyz_id"
            )
    )
    private final Set<Keyz> KeyzSet;

    public LikeWithKeyz (
            @NonNull final Like Like,
            @NonNull final Set<Keyz> KeyzSet
    ) {
        this.Like = Like;
        this.KeyzSet = KeyzSet;
    }

    @NonNull
    public final Like getLike () {
        return this.Like;
    }

    @NonNull
    public final Set<Keyz> getKeyzSet () {
        return this.KeyzSet;
    }
}
