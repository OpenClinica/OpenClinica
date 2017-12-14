package org.akaza.openclinica.dao.hibernate;

import org.akaza.openclinica.domain.datamap.EventDefinitionCrfTag;

public class EventDefinitionCrfTagDao extends AbstractDomainDao<EventDefinitionCrfTag> {

    @Override
    Class<EventDefinitionCrfTag> domainClass() {
        // TODO Auto-generated method stub
        return EventDefinitionCrfTag.class;
    }

    public EventDefinitionCrfTag findByCrfPath(int tagId, String path, boolean active) {
        String query = "from " + getDomainClassName() + " where path = :path and tagId= :tagId and active= :active ";
        org.hibernate.Query q = getCurrentSession().createQuery(query);
        q.setInteger("tagId", tagId);
        q.setString("path", path);
        q.setBoolean("active", active);
        return (EventDefinitionCrfTag) q.uniqueResult();

    }

}
