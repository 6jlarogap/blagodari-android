package com.vsdrozd.blagodarie.db.generators;

import com.vsdrozd.blagodarie.db.scheme.BaseEntity;

import java.util.List;

interface Generator<EntityType extends BaseEntity> {

    EntityType get();

    List<EntityType> getList(final int count);

}
