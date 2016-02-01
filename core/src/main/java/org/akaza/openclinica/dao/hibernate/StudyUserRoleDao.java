package org.akaza.openclinica.dao.hibernate;

import org.akaza.openclinica.domain.datamap.StudyUserRole;

public class StudyUserRoleDao extends AbstractDomainDao<StudyUserRole> {
	
    @Override
    public Class<StudyUserRole> domainClass() {
        return StudyUserRole.class;
    }
    
    public StudyUserRole findByUserTypeId(Integer userTypeId) {
        getSessionFactory().getStatistics().logSummary();
        String query = "from " + getDomainClassName() + " do  where do.user_id = :user_type_id";
        org.hibernate.Query q = getCurrentSession().createQuery(query);
        q.setInteger("user_type_id", userTypeId);
        return (StudyUserRole) q.uniqueResult();
    }

}
