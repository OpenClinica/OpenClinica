/*
 * OpenClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: http://www.openclinica.org/license
 * copyright 2003-2005 Akaza Research
 */
package org.akaza.openclinica.control.managestudy;

import core.org.akaza.openclinica.bean.core.Role;
import core.org.akaza.openclinica.bean.core.Status;
import core.org.akaza.openclinica.bean.managestudy.StudyEventBean;
import core.org.akaza.openclinica.bean.managestudy.StudySubjectBean;
import core.org.akaza.openclinica.bean.managestudy.DisplayStudyEventBean;
import core.org.akaza.openclinica.bean.submit.EventCRFBean;
import core.org.akaza.openclinica.bean.submit.ItemDataBean;
import core.org.akaza.openclinica.bean.submit.SubjectBean;
import core.org.akaza.openclinica.dao.hibernate.StudyDao;
import core.org.akaza.openclinica.domain.datamap.Study;
import org.akaza.openclinica.control.core.SecureController;
import core.org.akaza.openclinica.core.EmailEngine;
import core.org.akaza.openclinica.core.form.StringUtil;
import core.org.akaza.openclinica.dao.managestudy.StudyEventDAO;
import core.org.akaza.openclinica.dao.managestudy.StudySubjectDAO;
import core.org.akaza.openclinica.dao.submit.EventCRFDAO;
import core.org.akaza.openclinica.dao.submit.ItemDataDAO;
import core.org.akaza.openclinica.dao.submit.SubjectDAO;
import core.org.akaza.openclinica.service.UserStatus;
import org.akaza.openclinica.view.Page;
import core.org.akaza.openclinica.web.InsufficientPermissionException;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.Date;

/**
 * @author jxu
 *
 * Restores a removed subject to a study
 */
public class RestoreStudySubjectServlet extends SecureController {
    /**
     *
     */
    @Override
    public void mayProceed() throws InsufficientPermissionException {
        checkStudyLocked(Page.LIST_STUDY_SUBJECTS_SERVLET, respage.getString("current_study_locked"));
        checkStudyFrozen(Page.LIST_STUDY_SUBJECTS_SERVLET, respage.getString("current_study_frozen"));
        if (ub.isSysAdmin()) {
            return;
        }

        if (currentRole.getRole().equals(Role.STUDYDIRECTOR) || currentRole.getRole().equals(Role.COORDINATOR)
            || currentRole.getRole().equals(Role.INVESTIGATOR)) {
            return;
        }

        addPageMessage(respage.getString("no_have_correct_privilege_current_study") + respage.getString("change_study_contact_sysadmin"));
        throw new InsufficientPermissionException(Page.LIST_DEFINITION_SERVLET, resexception.getString("not_study_director"), "1");

    }

    @Override
    public void processRequest() throws Exception {
        String studySubIdString = request.getParameter("id");// studySubjectId
        String subIdString = request.getParameter("subjectId");
        String studyIdString = request.getParameter("studyId");

        SubjectDAO sdao = new SubjectDAO(sm.getDataSource());
        StudySubjectDAO subdao = new StudySubjectDAO(sm.getDataSource());

        if (StringUtil.isBlank(studySubIdString) || StringUtil.isBlank(subIdString) || StringUtil.isBlank(studyIdString)) {
            addPageMessage(respage.getString("please_choose_study_subject_to_restore"));
            forwardPage(Page.LIST_STUDY_SUBJECTS_SERVLET);
        } else {
            int studyId = Integer.valueOf(studyIdString.trim()).intValue();
            int studySubId = Integer.valueOf(studySubIdString.trim()).intValue();
            int subjectId = Integer.valueOf(subIdString.trim()).intValue();

            SubjectBean subject = (SubjectBean) sdao.findByPK(subjectId);

            StudySubjectBean studySub = (StudySubjectBean) subdao.findByPK(studySubId);

            Study study = (Study) getStudyDao().findByPK(studyId);
            if(study.getStudy() ==null)
                study.setStudy(new Study());
            study.getStudy().setName(((Study) getStudyDao().findByPK(study.checkAndGetParentStudyId())).getName());
            // find study events
            StudyEventDAO sedao = new StudyEventDAO(sm.getDataSource());
//            ArrayList events = sedao.findAllByStudyAndStudySubjectId(study, studySubId);
            ArrayList<DisplayStudyEventBean> displayEvents = ViewStudySubjectServlet.getDisplayStudyEventsForStudySubject(studySub, sm.getDataSource(), ub, currentRole);
            String action = request.getParameter("action");
            if ("confirm".equalsIgnoreCase(action)) {
                if (studySub.getStatus().equals(Status.AVAILABLE)) {
                    addPageMessage(respage.getString("this_subject_is_already_available_for_study") + " "
                        + respage.getString("please_contact_sysadmin_for_more_information"));
                    forwardPage(Page.LIST_STUDY_SUBJECTS_SERVLET);
                    return;
                }

                request.setAttribute("subject", subject);
                request.setAttribute("study", study);
                request.setAttribute("studySub", studySub);
                request.setAttribute("events", displayEvents);

                forwardPage(Page.RESTORE_STUDY_SUBJECT);
            } else {
                logger.info("submit to restore the subject from study");
                // restore subject from study
                studySub.setStatus(Status.AVAILABLE);
                studySub.setUpdater(ub);
                studySub.setUpdatedDate(new Date());
                studySub.setUserStatus(UserStatus.ACTIVE);
                subdao.update(studySub);


                // restore all study events
                // restore all event crfs
                EventCRFDAO ecdao = new EventCRFDAO(sm.getDataSource());

                for (int j = 0; j < displayEvents.size(); j++) {
                    DisplayStudyEventBean dispEvent = displayEvents.get(j);
                    StudyEventBean event = dispEvent.getStudyEvent();
                    if (event.getStatus().equals(Status.AUTO_DELETED)) {
                        event.setStatus(Status.AVAILABLE);
                        event.setUpdater(ub);
                        event.setUpdatedDate(new Date());
                        sedao.update(event);
                    }

                    ArrayList eventCRFs = ecdao.findAllByStudyEvent(event);

                    ItemDataDAO iddao = new ItemDataDAO(sm.getDataSource());
                    for (int k = 0; k < eventCRFs.size(); k++) {
                        EventCRFBean eventCRF = (EventCRFBean) eventCRFs.get(k);
                        if (eventCRF.getStatus().equals(Status.AUTO_DELETED)) {
                            eventCRF.setStatus(Status.AVAILABLE);
                            eventCRF.setUpdater(ub);
                            eventCRF.setUpdatedDate(new Date());
                            ecdao.update(eventCRF);
                            // remove all the item data
                            ArrayList itemDatas = iddao.findAllByEventCRFId(eventCRF.getId());
                            for (int a = 0; a < itemDatas.size(); a++) {
                                ItemDataBean item = (ItemDataBean) itemDatas.get(a);
                                if (item.getStatus().equals(Status.AUTO_DELETED)) {
                                    item.setStatus(Status.AVAILABLE);
                                    item.setUpdater(ub);
                                    item.setUpdatedDate(new Date());
                                    iddao.update(item);
                                }
                            }
                        }
                    }
                }

                String emailBody =
                    respage.getString("the_subject") + " " + studySub.getName() + " " + respage.getString("has_been_restored_to_the_study") + " "
                        + study.getName() + ".";
                    addPageMessage(emailBody);
//                try{
//                    sendEmail(emailBody);
//                }catch (Exception ex){
//                    addPageMessage(respage.getString("mail_cannot_be_sent_to_admin"));
//                }
                forwardPage(Page.LIST_STUDY_SUBJECTS_SERVLET);
            }
        }
    }

    /**
     * Send email to director and administrator
     *
     * @param request
     * @param response
     */
    private void sendEmail(String emailBody) throws Exception {

        logger.info("Sending email...");
        // to study director
        boolean messageSent = sendEmail(ub.getEmail().trim(), respage.getString("restore_subject_to_study"), emailBody, false);
        if(messageSent){
            sendEmail(EmailEngine.getAdminEmail(), respage.getString("restore_subject_to_study"), emailBody, false);
        }
        logger.info("Sending email done..");
    }

}
