package org.akaza.openclinica.dao.hibernate;

import org.akaza.openclinica.domain.datamap.EventCrfFlag;

public class EventCrfFlagDao extends AbstractDomainDao<EventCrfFlag> {

    @Override
    Class<EventCrfFlag> domainClass() {
        // TODO Auto-generated method stub
        return EventCrfFlag.class;
    }

    public EventCrfFlag findByEventCrfPath(int tagId, String path) {
        String query = "from " + getDomainClassName() + " where path = '" + path + "' and tagId=" + tagId;
        org.hibernate.Query q = getCurrentSession().createQuery(query);
        return (EventCrfFlag) q.uniqueResult();

    }

}
