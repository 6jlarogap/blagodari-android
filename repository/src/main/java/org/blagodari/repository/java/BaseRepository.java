package org.blagodari.repository.java;

import androidx.annotation.NonNull;

import java.util.Collection;

abstract class BaseRepository<EntityType extends BaseEntity, DaoType extends BaseDao<EntityType>> {

    @NonNull
    private final DaoType mDao;

    BaseRepository (@NonNull final DaoType dao) {
        this.mDao = dao;
    }

    DaoType getDao () {
        return this.mDao;
    }

    final void insert (@NonNull final EntityType obj) {
        getDao().insert(obj);
    }

    final void insert (@NonNull final Collection<EntityType> objs) {
        getDao().insert(objs);
    }

    final void insertAndSetId (@NonNull final EntityType obj) {
        getDao().insertAndSetId(obj);
    }

    final void insertAndSetIds (@NonNull final Collection<EntityType> objs) {
        getDao().insertAndSetIds(objs);
    }
}
