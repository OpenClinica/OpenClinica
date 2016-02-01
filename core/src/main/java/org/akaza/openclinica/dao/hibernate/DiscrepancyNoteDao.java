package org.akaza.openclinica.dao.hibernate;

import java.util.List;

import org.akaza.openclinica.domain.datamap.DiscrepancyNote;
import org.hibernate.Criteria;
import org.hibernate.criterion.Restrictions;

public class DiscrepancyNoteDao extends AbstractDomainDao<DiscrepancyNote> {

    @Override
    Class<DiscrepancyNote> domainClass() {
        return DiscrepancyNote.class;
    }

    public DiscrepancyNote findByParentId(int discrepancyNoteId) {
        String query = "from " + getDomainClassName() + " do where do.parentDnId = :discrepancynoteid ";
        org.hibernate.Query q = getCurrentSession().createQuery(query);
        q.setInteger("discrepancynoteid", discrepancyNoteId);
        return (DiscrepancyNote) q.uniqueResult();
    }

    public List<DiscrepancyNote> findParentNotesByItemData(Integer itemDataId) {
        //String query = "from " + getDomainClassName() + " do where do.itemData.itemDataId = :itemdataid ";
        //org.hibernate.Query q = getCurrentSession().createQuery(query);
        //q.setInteger("itemdataid", itemDataId);
        //return (List<DiscrepancyNote>) q.list();

        Criteria criteria = getCurrentSession().createCriteria(getDomainClassName());
        criteria.add(Restrictions.isNull("parentDnId"));
        criteria.add(Restrictions.eq("itemData.itemDataId", itemDataId));
        return (List<DiscrepancyNote>) criteria.list();
    }


}
