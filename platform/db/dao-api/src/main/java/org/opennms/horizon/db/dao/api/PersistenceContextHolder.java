package org.opennms.horizon.db.dao.api;

import javax.persistence.EntityManager;

public interface PersistenceContextHolder {
    EntityManager getEntityManager();
}
