/*
 * OpenClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: http://www.openclinica.org/license
 * copyright 2003-2005 Akaza Research
 */
package core.org.akaza.openclinica.service.managestudy;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import core.org.akaza.openclinica.bean.admin.CRFBean;
import core.org.akaza.openclinica.bean.login.StudyUserRoleBean;
import core.org.akaza.openclinica.bean.login.UserAccountBean;
import core.org.akaza.openclinica.bean.managestudy.DisplayStudyEventBean;
import core.org.akaza.openclinica.bean.managestudy.StudyEventBean;
import core.org.akaza.openclinica.bean.managestudy.StudySubjectBean;
import core.org.akaza.openclinica.bean.submit.DisplayEventCRFBean;
import core.org.akaza.openclinica.bean.submit.FormLayoutBean;
import core.org.akaza.openclinica.core.SessionManager;
import core.org.akaza.openclinica.dao.hibernate.StudyDao;
import core.org.akaza.openclinica.domain.datamap.Study;
import org.akaza.openclinica.domain.enumsupport.StudyEventWorkflowStatusEnum;

import javax.sql.DataSource;

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

    ArrayList<DisplayStudyEventBean> getDisplayStudyEventsForStudySubject(StudySubjectBean studySub, DataSource ds, UserAccountBean ub,
                                                                          StudyUserRoleBean currentRole, StudyDao studyDao);

    DisplayStudyEventBean getDisplayStudyEventsForStudySubject(StudySubjectBean studySub, StudyEventBean event, DataSource ds,
                                                               UserAccountBean ub, StudyUserRoleBean currentRole, Study study);

    List<DisplayEventCRFBean> getDisplayEventCRFs(List eventCRFs, UserAccountBean ub, StudyUserRoleBean currentRole, StudyEventWorkflowStatusEnum status,
                                                          Study study, Set<Integer> nonEmptyEventCrf, Map<Integer, FormLayoutBean> formLayoutById, Map<Integer, CRFBean> crfById,
                                                          Integer studyEventDefinitionId, List eventDefinitionCRFs);

    ArrayList getDisplayEventCRFs(DataSource ds, ArrayList eventCRFs, ArrayList eventDefinitionCRFs, UserAccountBean ub,
                                  StudyUserRoleBean currentRole, StudyEventWorkflowStatusEnum workflowStatus, Study study);

    ArrayList getUncompletedCRFs(DataSource ds, ArrayList eventDefinitionCRFs, ArrayList eventCRFs, StudyEventWorkflowStatusEnum workflowStatus, int studyEventId);

    ArrayList getUncompletedCRFs(SessionManager sm, ArrayList eventDefinitionCRFs, ArrayList eventCRFs, int studyEventId);

    void populateUncompletedCRFsWithCRFAndVersions(DataSource ds, ArrayList uncompletedEventDefinitionCRFs);

    Boolean isSignable(int studySubjectId);

}
