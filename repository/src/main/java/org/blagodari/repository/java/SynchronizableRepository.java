package org.blagodari.repository.java;

import androidx.annotation.NonNull;

abstract class SynchronizableRepository<EntityType extends SynchronizableEntity, DaoType extends SynchronizableDao<EntityType>>
        extends BaseRepository<EntityType, DaoType> {

    SynchronizableRepository (@NonNull final DaoType dao) {
        super(dao);
    }

    public final Long getIdByServerId (@NonNull final Long serverId){
        return getDao().getIdByServerId(serverId);
    }

    public final Long getServerIdById (@NonNull final Long id){
        return getDao().getServerIdById(id);
    }
}
