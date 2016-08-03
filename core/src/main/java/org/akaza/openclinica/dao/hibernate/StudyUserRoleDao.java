package org.akaza.openclinica.dao.hibernate;

import java.util.ArrayList;

import org.akaza.openclinica.domain.datamap.StudyUserRole;
import org.akaza.openclinica.domain.user.UserAccount;

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

}
