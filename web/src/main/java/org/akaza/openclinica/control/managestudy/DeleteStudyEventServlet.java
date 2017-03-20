package org.akaza.openclinica.control.managestudy;

import java.util.ArrayList;
import java.util.Date;

import org.akaza.openclinica.bean.core.Role;
import org.akaza.openclinica.bean.core.SubjectEventStatus;
import org.akaza.openclinica.bean.managestudy.DisplayStudyEventBean;
import org.akaza.openclinica.bean.managestudy.StudyBean;
import org.akaza.openclinica.bean.managestudy.StudyEventBean;
import org.akaza.openclinica.bean.managestudy.StudyEventDefinitionBean;
import org.akaza.openclinica.bean.managestudy.StudySubjectBean;
import org.akaza.openclinica.control.core.SecureController;
import org.akaza.openclinica.control.form.FormProcessor;
import org.akaza.openclinica.core.EmailEngine;
import org.akaza.openclinica.dao.managestudy.EventDefinitionCRFDAO;
import org.akaza.openclinica.dao.managestudy.StudyDAO;
import org.akaza.openclinica.dao.managestudy.StudyEventDAO;
import org.akaza.openclinica.dao.managestudy.StudyEventDefinitionDAO;
import org.akaza.openclinica.dao.managestudy.StudySubjectDAO;
import org.akaza.openclinica.dao.submit.EventCRFDAO;
import org.akaza.openclinica.view.Page;
import org.akaza.openclinica.web.InsufficientPermissionException;

public class DeleteStudyEventServlet extends SecureController{
    @Override
    public void mayProceed() throws InsufficientPermissionException {
        checkStudyLocked(Page.LIST_STUDY_SUBJECTS, respage.getString("current_study_locked"));
        checkStudyFrozen(Page.LIST_STUDY_SUBJECTS, respage.getString("current_study_frozen"));
        
        if (ub.isSysAdmin()) {
            return;
        }

        if (currentRole.getRole().equals(Role.STUDYDIRECTOR) || currentRole.getRole().equals(Role.COORDINATOR)) {
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

        StudyEventDAO sedao = new StudyEventDAO(sm.getDataSource());
        StudySubjectDAO subdao = new StudySubjectDAO(sm.getDataSource());

        if (studyEventId == 0) {
            addPageMessage(respage.getString("please_choose_a_SE_to_remove"));
            request.setAttribute("id", new Integer(studySubId).toString());
            forwardPage(Page.VIEW_STUDY_SUBJECT_SERVLET);
        } else {

            StudyEventBean event = (StudyEventBean) sedao.findByPK(studyEventId);

            StudySubjectBean studySub = (StudySubjectBean) subdao.findByPK(studySubId);
            request.setAttribute("studySub", studySub);

            StudyEventDefinitionDAO seddao = new StudyEventDefinitionDAO(sm.getDataSource());
            StudyEventDefinitionBean sed = (StudyEventDefinitionBean) seddao.findByPK(event.getStudyEventDefinitionId());
            event.setStudyEventDefinition(sed);

            StudyDAO studydao = new StudyDAO(sm.getDataSource());
            StudyBean study = (StudyBean) studydao.findByPK(studySub.getStudyId());

            String action = request.getParameter("action");
            if ("confirm".equalsIgnoreCase(action)) {

                EventDefinitionCRFDAO edcdao = new EventDefinitionCRFDAO(sm.getDataSource());
                // find all crfs in the definition
                ArrayList eventDefinitionCRFs = (ArrayList) edcdao.findAllByEventDefinitionId(study, sed.getId());

                EventCRFDAO ecdao = new EventCRFDAO(sm.getDataSource());
                // construct info needed on view study event page
                DisplayStudyEventBean de = new DisplayStudyEventBean();
                de.setStudyEvent(event);

                request.setAttribute("displayEvent", de);
//                request.setAttribute("crfs", eventDefinitionCRFs);

                forwardPage(Page.DELETE_STUDY_EVENT);
            } else {
                logger.info("submit to delete the event from study");
                // delete event from study

                event.setSubjectEventStatus(SubjectEventStatus.NOT_SCHEDULED);
                event.setUpdater(ub);
                event.setUpdatedDate(new Date());
                sedao.update(event);
                String emailBody =
                    respage.getString("the_event") + " " + event.getStudyEventDefinition().getName() + " "
                        + respage.getString("has_been_removed_from_the_subject_record_for") + " " + studySub.getLabel() + " "
                        + respage.getString("in_the_study") + " " + study.getName() + ".";

                addPageMessage(emailBody);
//                sendEmail(emailBody);
                request.setAttribute("id", new Integer(studySubId).toString());
                forwardPage(Page.VIEW_STUDY_SUBJECT_SERVLET);
            }
        }
    }


    /**
     * Send email to director and administrator
     *
     */
    private void sendEmail(String emailBody) throws Exception {

        logger.info("Sending email...");
        // to study director
        sendEmail(ub.getEmail().trim(), respage.getString("remove_event_from_study"), emailBody, false);
        sendEmail(EmailEngine.getAdminEmail(), respage.getString("remove_event_from_study"), emailBody, false, false);
        logger.info("Sending email done..");
    }
    
}
