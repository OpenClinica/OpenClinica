package core.org.akaza.openclinica.dao.hibernate;

import core.org.akaza.openclinica.domain.user.UserAccount;
import org.hibernate.query.Query;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public class UserAccountDao extends AbstractDomainDao<UserAccount> {

    @Override
    public Class<UserAccount> domainClass() {
        return UserAccount.class;
    }

    public UserAccount findByUserName(String userName) {
        getSessionFactory().getStatistics().logSummary();
        String query = "from " + getDomainClassName() + " do  where do.userName = :user_name";
        Query q = getCurrentSession().createQuery(query);
        q.setParameter("user_name", userName);
        return (UserAccount) q.uniqueResult();
    }

    @Transactional
    public UserAccount findByUserId(Integer userId) {
        getSessionFactory().getStatistics().logSummary();
        String query = "from " + getDomainClassName() + " do  where do.userId = :user_id";
        Query q = getCurrentSession().createQuery(query);
        q.setParameter("user_id", userId);
        return (UserAccount) q.uniqueResult();
    }

    public UserAccount findByUserUuid(String uuid) {
        getSessionFactory().getStatistics().logSummary();
        String query = "from " + getDomainClassName() + " do  where do.userUuid = :uuid";
        Query q = getCurrentSession().createQuery(query);
        q.setParameter("uuid", uuid);
        return (UserAccount) q.uniqueResult();
    }
    public List<UserAccount> findNonRootNonParticipateUsersByStudyId(Integer studyId, Integer parentStudyId) {
        getSessionFactory().getStatistics().logSummary();
        String query = "";
        Query q;
        if (parentStudyId != null && parentStudyId > 0) {
            query = "select distinct ua.* from user_account ua join study_user_role sur on ua.user_name = sur.user_name " +
                    "where ua.enabled = true and ua.institutional_affiliation != 'PFORM' and ua.user_name != 'root' and ua.status_id!=5 " +
                    "and ua.status_id!=7 and ua.user_type_id!=4 and sur.status_id!=5 and sur.status_id!=7  "
                    + " and (sur.study_id = :studyId or  sur.study_id = :parentStudyId) order by ua.last_name";
            q = getCurrentSession().createNativeQuery(query).addEntity(UserAccount.class);
            q.setParameter("parentStudyId", parentStudyId);
        } else {
            query = "select distinct ua.* from user_account ua join study_user_role sur on ua.user_name = sur.user_name " +
                    "where ua.enabled = true and ua.institutional_affiliation != 'PFORM' and ua.user_name != 'root' " +
                    "and ua.status_id!=5 " +
                    "and ua.status_id!=7 and ua.user_type_id!=4 and sur.status_id!=5 and sur.status_id!=7  "
                    + " and sur.study_id = :studyId order by ua.last_name";
            q = getCurrentSession().createNativeQuery(query).addEntity(UserAccount.class);
        }
        q.setParameter("studyId", studyId);
        return (List<UserAccount>) q.list();
    }

}
