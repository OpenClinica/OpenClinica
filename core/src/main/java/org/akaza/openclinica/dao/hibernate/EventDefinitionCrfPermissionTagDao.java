package org.akaza.openclinica.dao.hibernate;

import org.akaza.openclinica.core.form.StringUtil;
import org.akaza.openclinica.domain.datamap.EventDefinitionCrf;
import org.akaza.openclinica.domain.datamap.EventDefinitionCrfPermissionTag;
import org.akaza.openclinica.domain.datamap.RepeatCount;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.hibernate.query.Query;

import java.util.List;

public class EventDefinitionCrfPermissionTagDao extends AbstractDomainDao<EventDefinitionCrfPermissionTag> {

    @Override
    Class<EventDefinitionCrfPermissionTag> domainClass() {
        // TODO Auto-generated method stub
        return EventDefinitionCrfPermissionTag.class;
    }


    public List<EventDefinitionCrfPermissionTag> findByEdcIdTagId(int edcId, int edcParentId, List<String> permissionTags) {
        String query;
        Query q = null;
        query = "from " + getDomainClassName() + " do where (do.eventDefinitionCrf.eventDefinitionCrfId = :eventDefinitionCrfId or do.eventDefinitionCrf.eventDefinitionCrfId = :eventDefinitionCrfParentId) ";
        if (CollectionUtils.isNotEmpty(permissionTags)) {
            query = query + " and do.permissionTagId not in ( :permissionTags) ";
            q = getCurrentSession().createQuery(query);
            q.setParameter("eventDefinitionCrfId", edcId);
            q.setParameter("eventDefinitionCrfParentId", edcParentId);
            q.setParameterList ("permissionTags", permissionTags);
        } else {
            q = getCurrentSession().createQuery(query);
            q.setParameter("eventDefinitionCrfId", edcId);
            q.setParameter("eventDefinitionCrfParentId", edcParentId);
        }

        return (List<EventDefinitionCrfPermissionTag>) q.list();
    }
    public void delete(EventDefinitionCrfPermissionTag tag)

    {
        String query = " delete from " + getDomainClassName() + "  where id =:id ";
        Query q = getCurrentSession().createQuery(query);
        q.setParameter("id", tag.getId());
        q.executeUpdate();
    }
    public List<String> findTagsForEDC(EventDefinitionCrf edc) {
        String query = "select permissionTagId from " + getDomainClassName() + " do where do.eventDefinitionCrf.eventDefinitionCrfId = :ecdId";
        Query q = getCurrentSession().createQuery(query);
        q.setParameter("ecdId", edc.getEventDefinitionCrfId());
        return q.list();
    }

}
