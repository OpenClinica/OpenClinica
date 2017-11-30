package org.akaza.openclinica.dao.hibernate;

import java.util.List;

import org.akaza.openclinica.domain.datamap.RepeatCount;
import org.hibernate.query.Query;

public class RepeatCountDao extends AbstractDomainDao<RepeatCount> {

    @Override
    Class<RepeatCount> domainClass() {
        // TODO Auto-generated method stub
        return RepeatCount.class;
    }

    public List<RepeatCount> findAllByEventCrfId(int eventCrfId) {
        String query = "from " + getDomainClassName() + " rc where rc.eventCrf.eventCrfId = :eventCrfId ";
        Query q = getCurrentSession().createQuery(query);
        q.setInteger("eventCrfId", eventCrfId);
        return (List<RepeatCount>) q.list();
    }

    public RepeatCount findByEventCrfIdAndRepeatName(int eventCrfId, String name) {
        String query = "from " + getDomainClassName() + " rc  where rc.eventCrf.eventCrfId = :eventCrfId  and rc.groupName = :name";
        Query q = getCurrentSession().createQuery(query);
        q.setInteger("eventCrfId", eventCrfId);
        q.setString("name", name);
        return (RepeatCount) q.uniqueResult();
    }
}
