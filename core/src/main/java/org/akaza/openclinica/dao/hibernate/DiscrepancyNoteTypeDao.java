package org.akaza.openclinica.dao.hibernate;

import org.akaza.openclinica.domain.datamap.DiscrepancyNoteType;

public class DiscrepancyNoteTypeDao extends AbstractDomainDao<DiscrepancyNoteType> {

    @Override
    public Class<DiscrepancyNoteType> domainClass() {
        return DiscrepancyNoteType.class;
    }
    public DiscrepancyNoteType findByDiscrepancyNoteTypeId(Integer discrepancyNoteTypeId) {
        String query = "from " + getDomainClassName() + " do  where do.discrepancyNoteTypeId = :discrepancynotetypeid";
        org.hibernate.Query q = getCurrentSession().createQuery(query);
        q.setInteger("discrepancynotetypeid", discrepancyNoteTypeId);
        return (DiscrepancyNoteType) q.uniqueResult();
    }

}
