package org.akaza.openclinica.dao.hibernate;

import java.util.List;

import org.akaza.openclinica.domain.datamap.EventDefinitionCrf;
import org.akaza.openclinica.domain.datamap.EventDefinitionCrfTag;

public class EventDefinitionCrfTagDao extends AbstractDomainDao<EventDefinitionCrfTag> {

    @Override
    Class<EventDefinitionCrfTag> domainClass() {
        // TODO Auto-generated method stub
        return EventDefinitionCrfTag.class;
    }

    public EventDefinitionCrfTag findByCrfPath(int tagId, String path, boolean active) {
        String query = "from " + getDomainClassName() + " where path = '" + path + "' and tagId=" + tagId + "and active=" + active;
        org.hibernate.Query q = getCurrentSession().createQuery(query);
        return (EventDefinitionCrfTag) q.uniqueResult();

    }

}
