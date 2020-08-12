/*
 * OpenClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: http://www.openclinica.org/license
 * copyright 2003-2005 Akaza Research
 */
package org.akaza.openclinica.control.managestudy;

import core.org.akaza.openclinica.bean.core.Role;
import core.org.akaza.openclinica.bean.managestudy.DisplayStudyEventBean;
import core.org.akaza.openclinica.bean.managestudy.StudyEventBean;
import core.org.akaza.openclinica.bean.managestudy.StudyEventDefinitionBean;
import core.org.akaza.openclinica.bean.managestudy.StudySubjectBean;
import core.org.akaza.openclinica.dao.managestudy.EventDefinitionCRFDAO;
import core.org.akaza.openclinica.domain.datamap.Study;
import core.org.akaza.openclinica.service.StudyEventService;
import core.org.akaza.openclinica.web.InsufficientPermissionException;
import org.akaza.openclinica.control.core.SecureController;
import org.akaza.openclinica.control.form.FormProcessor;
import org.akaza.openclinica.view.Page;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;

/**
 * @author jxu
 *
 *         Removes a study event and all its related event CRFs, items
 */
public class RemoveStudyEventServlet extends SecureController {

    @Autowired
    private StudyEventService studyEventService;
    @Autowired
    EventDefinitionCRFDAO eventDefinitionCRFDAO;

    @Override
    public void mayProceed() throws InsufficientPermissionException {
        checkStudyLocked(Page.LIST_STUDY_SUBJECTS, respage.getString("current_study_locked"));
        checkStudyFrozen(Page.LIST_STUDY_SUBJECTS, respage.getString("current_study_frozen"));

        if (ub.isSysAdmin()) {
            return;
        }

        if (!currentRole.getRole().equals(Role.MONITOR) ){
            return;
        }

        addPageMessage(respage.getString("no_have_correct_privilege_current_study") + respage.getString("change_study_contact_sysadmin"));
        throw new InsufficientPermissionException(Page.MENU_SERVLET, resexception.getString("not_study_director"), "1");

    }

    @Override
    public void processRequest() throws Exception {
        FormProcessor fp = new FormProcessor(request);
        int studyEventId = fp.getInt("id");// studyEventId
        int studySubId = fp.getInt("studySubId");// studySubjectId

        if (studyEventId == 0) {
            addPageMessage(respage.getString("please_choose_a_SE_to_remove"));
            request.setAttribute("id", Integer.toString(studySubId));
            forwardPage(Page.VIEW_STUDY_SUBJECT_SERVLET);
        } else {

            StudyEventBean studyEvent = (StudyEventBean) studyEventDAO.findByPK(studyEventId);
            studyEvent.getStudyEventDefinition().getOid();

            StudySubjectBean studySubject = (StudySubjectBean) studySubjectDAO.findByPK(studySubId);
            request.setAttribute("studySub", studySubject);

            StudyEventDefinitionBean sed = (StudyEventDefinitionBean) studyEventDefinitionDAO.findByPK(studyEvent.getStudyEventDefinitionId());
            studyEvent.setStudyEventDefinition(sed);

            Study study = (Study) getStudyDao().findByPK(studySubject.getStudyId());

            request.setAttribute("study", study);

            String action = request.getParameter("action");
            if ("confirm".equalsIgnoreCase(action)) {

                // find all crfs in the definition
                ArrayList eventDefinitionCRFs = (ArrayList) eventDefinitionCRFDAO.findAllByEventDefinitionId(study, sed.getId());

                ArrayList eventCRFs = eventCRFDAO.findAllByStudyEvent(studyEvent);

                // construct info needed on view study event page
                DisplayStudyEventBean displayEvent = new DisplayStudyEventBean();
                displayEvent.setStudyEvent(studyEvent);

                ArrayList displayEventCrfs = studyEventService.getDisplayEventCRFs(eventCRFs, eventDefinitionCRFs, currentRole, ub);
                displayEvent.setDisplayEventCRFs(displayEventCrfs);

                request.setAttribute("displayEvent", displayEvent);

                forwardPage(Page.REMOVE_STUDY_EVENT);
            } else {
                logger.info("submit to remove the event from study");
                // remove event from study

                studyEventService.removeStudyEvent(studySubject, studyEvent, ub);

                String alertMessage = respage.getString("the_event") + " " + studyEvent.getStudyEventDefinition().getName() + " "
                        + respage.getString("has_been_removed_from_the_subject_record_for") + " " + studySubject.getLabel() + " "
                        + respage.getString("in_the_study") + " " + study.getName() + ".";

                addPageMessage(alertMessage);

                request.setAttribute("id", Integer.toString(studySubId));
                forwardPage(Page.VIEW_STUDY_SUBJECT_SERVLET);
            }
        }
    }

}
