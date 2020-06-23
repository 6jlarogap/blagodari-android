package org.blagodarie.repository.java;

abstract class SynchronizableDao<EntityType extends SynchronizableEntity>
        extends BaseDao<EntityType> {

    abstract Long getIdByServerId (final long serverId);

    abstract Long getServerIdById (final long Id);
}
