package org.akaza.openclinica.dao.hibernate;

import org.akaza.openclinica.domain.user.UserAccount;

public class UserAccountDao extends AbstractDomainDao<UserAccount> {
	
    @Override
    public Class<UserAccount> domainClass() {
        return UserAccount.class;
    }
    
    public UserAccount findByUserName(String userName) {
        getSessionFactory().getStatistics().logSummary();
        String query = "from " + getDomainClassName() + " do  where do.userName = :user_name";
        org.hibernate.Query q = getCurrentSession().createQuery(query);
        q.setString("user_name", userName);
        return (UserAccount) q.uniqueResult();
    }

    public UserAccount findByUserId(Integer userId) {
        getSessionFactory().getStatistics().logSummary();
        String query = "from " + getDomainClassName() + " do  where do.user_id = :user_id";
        org.hibernate.Query q = getCurrentSession().createQuery(query);
        q.setInteger("user_id", userId);
        return (UserAccount) q.uniqueResult();
    }

}
