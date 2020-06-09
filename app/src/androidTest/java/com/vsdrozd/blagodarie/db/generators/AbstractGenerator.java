package com.vsdrozd.blagodarie.db.generators;

import com.vsdrozd.blagodarie.db.scheme.BaseEntity;

import java.util.ArrayList;
import java.util.List;

public abstract class AbstractGenerator<EntityType extends BaseEntity>
        implements Generator<EntityType> {

    protected abstract void incrementNumber ();

    protected abstract long getNumber ();

    protected abstract EntityType generate ();

    private EntityType incrementNumberAndGenerate () {
        incrementNumber();
        return generate();
    }


    @Override
    public final EntityType get () {
        return incrementNumberAndGenerate();
    }

    @Override
    public final List<EntityType> getList (final int count) {
        List<EntityType> entities = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            entities.add(get());
        }
        return entities;
    }

}
