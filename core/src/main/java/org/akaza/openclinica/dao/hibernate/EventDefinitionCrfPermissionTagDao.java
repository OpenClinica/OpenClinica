package org.akaza.openclinica.dao.hibernate;

import org.akaza.openclinica.domain.datamap.EventDefinitionCrf;
import org.akaza.openclinica.domain.datamap.EventDefinitionCrfPermissionTag;
import org.akaza.openclinica.domain.datamap.RepeatCount;
import org.hibernate.query.Query;

import java.util.List;

public class EventDefinitionCrfPermissionTagDao extends AbstractDomainDao<EventDefinitionCrfPermissionTag> {

    @Override
    Class<EventDefinitionCrfPermissionTag> domainClass() {
        // TODO Auto-generated method stub
        return EventDefinitionCrfPermissionTag.class;
    }

    public List<EventDefinitionCrfPermissionTag> findByEdcId(int edcId , int edcParentId) {
        String query = "from " + getDomainClassName() + " do where do.eventDefinitionCrf.eventDefinitionCrfId = :eventDefinitionCrfId or do.eventDefinitionCrf.eventDefinitionCrfId = :eventDefinitionCrfParentId ";
        org.hibernate.Query q = getCurrentSession().createQuery(query);
        q.setInteger("eventDefinitionCrfId", edcId);
        q.setInteger("eventDefinitionCrfParentId", edcParentId);

        return (List<EventDefinitionCrfPermissionTag>) q.list();
    }
    public void delete(EventDefinitionCrfPermissionTag tag) {
        String query = " delete from " + getDomainClassName() + "  where id =:id ";
        org.hibernate.Query q = getCurrentSession().createQuery(query);
        q.setInteger("id", tag.getId());
        q.executeUpdate();
    }

}
