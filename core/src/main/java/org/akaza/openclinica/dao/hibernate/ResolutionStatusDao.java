package org.akaza.openclinica.dao.hibernate;

import org.akaza.openclinica.domain.datamap.ResolutionStatus;

public class ResolutionStatusDao extends AbstractDomainDao<ResolutionStatus> {

    @Override
    public Class<ResolutionStatus> domainClass() {
        return ResolutionStatus.class;
    }
    public ResolutionStatus findByResolutionStatusId(Integer resolutionStatusId) {
        String query = "from " + getDomainClassName() + " do  where do.resolutionStatusId = :resolutionstatusid";
        org.hibernate.Query q = getCurrentSession().createQuery(query);
        q.setInteger("resolutionstatusid", resolutionStatusId);
        return (ResolutionStatus) q.uniqueResult();
    }

}
