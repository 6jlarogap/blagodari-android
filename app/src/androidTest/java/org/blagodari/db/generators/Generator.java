package org.blagodari.db.generators;

import org.blagodari.db.scheme.BaseEntity;

import java.util.List;

interface Generator<EntityType extends BaseEntity> {

    EntityType get();

    List<EntityType> getList(final int count);

}
