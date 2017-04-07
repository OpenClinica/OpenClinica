package org.akaza.openclinica.dao.hibernate;

import java.util.List;

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
        String query = "from " + getDomainClassName() + " do  where do.userId = :user_id";
        org.hibernate.Query q = getCurrentSession().createQuery(query);
        q.setInteger("user_id", userId);
        return (UserAccount) q.uniqueResult();
    }

    public List<UserAccount> findNonRootNonParticipateUsersByStudyId(Integer studyId, Integer parentStudyId) {
        getSessionFactory().getStatistics().logSummary();
        String query = "select distinct ua.* from user_account ua join study_user_role sur on ua.user_name = sur.user_name where ua.enabled = true and ua.institutional_affiliation != 'PFORM' and ua.user_name != 'root'"
                + " and (sur.study_id = " + studyId + " or  sur.study_id = " + parentStudyId + " )";
        org.hibernate.Query q = getCurrentSession().createSQLQuery(query).addEntity(UserAccount.class);
        return (List<UserAccount>) q.list();
    }

}
