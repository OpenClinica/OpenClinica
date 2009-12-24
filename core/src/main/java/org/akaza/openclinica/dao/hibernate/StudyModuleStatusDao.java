package org.akaza.openclinica.dao.hibernate;

import org.akaza.openclinica.domain.managestudy.StudyModuleStatus;
import org.akaza.openclinica.domain.rule.RuleBean;

/**
 * @author: Shamim
 * Date: Feb 18, 2009
 * Time: 8:01:42 PM
 */
public class StudyModuleStatusDao extends AbstractDomainDao<StudyModuleStatus> {
    @Override
    Class<StudyModuleStatus> domainClass() {
        return StudyModuleStatus.class;
    }

    public StudyModuleStatus findByStudyId(int studyId) {
        String query = "from " + getDomainClassName() + " sms  where sms.studyId = :studyId ";
        org.hibernate.Query q = getCurrentSession().createQuery(query);
        q.setInteger("studyId", studyId);
        return (StudyModuleStatus) q.uniqueResult();
    }

}
