/*
 * LibreClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: https://libreclinica.org/license
 * LibreClinica, copyright (C) 2020
 */
package org.akaza.openclinica.dao.hibernate;

import java.util.ArrayList;

import org.akaza.openclinica.domain.datamap.StudyUserRole;
import org.akaza.openclinica.domain.user.UserAccount;
import org.hibernate.query.Query;

public class StudyUserRoleDao extends CompositeIdAbstractDomainDao<StudyUserRole> {

    @Override
    public Class<StudyUserRole> domainClass() {
        return StudyUserRole.class;
    }

    @SuppressWarnings("unchecked")
    public ArrayList<StudyUserRole> findAllUserRolesByUserAccount(UserAccount userAccount, int studyId, int parentStudyId) {
        String query = "from " + getDomainClassName()
                + "   where   user_name=:username  AND  status_id=1  AND  ( study_id=:studyId OR study_id=:parentStudyId) ";
        Query<StudyUserRole> q = getCurrentSession().createQuery(query);
        q.setParameter("username", userAccount.getUserName());
        q.setParameter("studyId", studyId);
        q.setParameter("parentStudyId", parentStudyId);
        return new ArrayList<StudyUserRole>(q.list());
    }

}
