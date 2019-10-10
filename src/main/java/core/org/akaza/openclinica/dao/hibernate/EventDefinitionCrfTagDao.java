package core.org.akaza.openclinica.dao.hibernate;

import core.org.akaza.openclinica.domain.datamap.EventDefinitionCrfTag;

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

    public EventDefinitionCrfTag findByCrfPathAndTagId(int tagId, String path) {
        String query = "from " + getDomainClassName() + " where path = '" + path + "' and tagId=" + tagId;
        org.hibernate.Query q = getCurrentSession().createQuery(query);
        return (EventDefinitionCrfTag) q.uniqueResult();

    }
}
