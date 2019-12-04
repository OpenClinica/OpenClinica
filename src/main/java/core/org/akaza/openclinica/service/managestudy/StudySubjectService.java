/*
 * OpenClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: http://www.openclinica.org/license
 * copyright 2003-2005 Akaza Research
 */
package core.org.akaza.openclinica.service.managestudy;

import java.util.List;

import core.org.akaza.openclinica.bean.login.StudyUserRoleBean;
import core.org.akaza.openclinica.bean.login.UserAccountBean;
import core.org.akaza.openclinica.bean.managestudy.DisplayStudyEventBean;
import core.org.akaza.openclinica.bean.managestudy.StudySubjectBean;
import core.org.akaza.openclinica.domain.datamap.Study;

/**
 * @author Doug Rodrigues (douglas.rodrigues@openclinica.com)
 *
 */
public interface StudySubjectService {

    /**
     *
     * @param studySubject
     * @param userAccount
     * @param currentRole
     * @return
     */
    List<DisplayStudyEventBean> getDisplayStudyEventsForStudySubject(StudySubjectBean studySubject,
                                                                     UserAccountBean userAccount, StudyUserRoleBean currentRole, Study study);

}
