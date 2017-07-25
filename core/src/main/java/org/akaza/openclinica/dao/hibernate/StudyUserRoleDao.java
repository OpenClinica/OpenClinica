package org.akaza.openclinica.dao.hibernate;

import java.util.ArrayList;

import org.akaza.openclinica.bean.login.UserAccountBean;
import org.akaza.openclinica.domain.datamap.Study;
import org.akaza.openclinica.domain.datamap.StudyUserRole;
import org.akaza.openclinica.domain.user.UserAccount;
import org.hibernate.query.Query;
import org.springframework.data.jpa.repository.Modifying;

public class StudyUserRoleDao extends CompositeIdAbstractDomainDao<StudyUserRole> {

    @Override
    public Class<StudyUserRole> domainClass() {
        return StudyUserRole.class;
    }

    @SuppressWarnings("unchecked")
    public ArrayList<StudyUserRole> findAllUserRolesByUserAccount(UserAccount userAccount, int studyId, int parentStudyId) {
        String query = "from " + getDomainClassName()
                + "   where   user_name=:username  AND  status_id=1  AND  ( study_id=:studyId OR study_id=:parentStudyId) ";
        org.hibernate.Query q = getCurrentSession().createQuery(query);
        q.setParameter("username", userAccount.getUserName());
        q.setInteger("studyId", studyId);
        q.setInteger("parentStudyId", parentStudyId);
        return (ArrayList<StudyUserRole>) q.list();
    }

    public ArrayList<StudyUserRole> findAllUserRolesByUserAccountBean(UserAccountBean userAccount, int studyId, int parentStudyId) {
        String query = "select s from " + getDomainClassName()
                + "  s where s.id.userName=:username  AND  s.id.statusId=1  AND  ( s.id.studyId=:studyId OR s.id.studyId=:parentStudyId)";
        Query q = getCurrentSession().createQuery(query);
        q.setParameter("username", userAccount.getName());
        q.setParameter("studyId", studyId);
        q.setParameter("parentStudyId", parentStudyId);
        return (ArrayList<StudyUserRole>) q.list();
    }
    public int updateUsername(String username, String prevUsername) {
        String queryStr = "update StudyUserRole set id.userName=:userName where id.userName=:prevUser";
        Query query = getCurrentSession().createQuery(queryStr);
        query.setParameter("userName", username);
        query.setParameter("prevUser", prevUsername);
        int modifications = query.executeUpdate();
        return modifications;
    }
}
