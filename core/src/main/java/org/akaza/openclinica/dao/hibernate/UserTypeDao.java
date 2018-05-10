package org.akaza.openclinica.dao.hibernate;

import org.akaza.openclinica.domain.user.UserType;

public class UserTypeDao extends AbstractDomainDao<UserType> {
	
    @Override
    public Class<UserType> domainClass() {
        return UserType.class;
    }
    
    public UserType findByUserTypeId(Integer userTypeId) {
        getSessionFactory().getStatistics().logSummary();
        String query = "from " + getDomainClassName() + " do  where do.userTypeId = :user_type_id";
        org.hibernate.Query q = getCurrentSession().createQuery(query);
        q.setInteger("user_type_id", userTypeId);
        return (UserType) q.uniqueResult();
    }

}
