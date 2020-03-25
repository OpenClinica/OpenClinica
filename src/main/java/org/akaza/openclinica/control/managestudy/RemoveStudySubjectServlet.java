/*
 * OpenClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: http://www.openclinica.org/license
 * copyright 2003-2005 Akaza Research
 */
package org.akaza.openclinica.control.managestudy;

import core.org.akaza.openclinica.bean.core.ResolutionStatus;
import core.org.akaza.openclinica.bean.core.Role;
import core.org.akaza.openclinica.bean.core.Status;
import core.org.akaza.openclinica.bean.login.UserAccountBean;
import core.org.akaza.openclinica.bean.managestudy.DiscrepancyNoteBean;
import core.org.akaza.openclinica.bean.managestudy.StudyEventBean;
import core.org.akaza.openclinica.bean.managestudy.StudySubjectBean;
import core.org.akaza.openclinica.bean.managestudy.DisplayStudyEventBean;
import core.org.akaza.openclinica.bean.submit.EventCRFBean;
import core.org.akaza.openclinica.bean.submit.ItemDataBean;
import core.org.akaza.openclinica.bean.submit.SubjectBean;
import core.org.akaza.openclinica.dao.hibernate.StudyDao;
import core.org.akaza.openclinica.dao.managestudy.DiscrepancyNoteDAO;
import core.org.akaza.openclinica.domain.datamap.DiscrepancyNote;
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
 * Removes a study subject and all the related data
 */
public class RemoveStudySubjectServlet extends SecureController {

    DiscrepancyNoteDAO dnDao;

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
            addPageMessage(respage.getString("please_choose_a_study_subject_to_remove"));
            forwardPage(Page.LIST_STUDY_SUBJECTS_SERVLET);
        } else {
            int studyId = Integer.valueOf(studyIdString.trim()).intValue();
            int studySubId = Integer.valueOf(studySubIdString.trim()).intValue();
            int subjectId = Integer.valueOf(subIdString.trim()).intValue();

            SubjectBean subject = (SubjectBean) sdao.findByPK(subjectId);

            StudySubjectBean studySub = (StudySubjectBean) subdao.findByPK(studySubId);

            Study study = (Study) getStudyDao().findByPK(studyId);

            checkRoleByUserAndStudy(ub, study);

            // find study events
            StudyEventDAO sedao = new StudyEventDAO(sm.getDataSource());
//            ArrayList events = sedao.findAllByStudyAndStudySubjectId(study, studySubId);
            ArrayList<DisplayStudyEventBean> displayEvents = ViewStudySubjectServlet.getDisplayStudyEventsForStudySubject(studySub, sm.getDataSource(), ub, currentRole, getStudyDao());
            String action = request.getParameter("action");
            if ("confirm".equalsIgnoreCase(action)) {
                if (!studySub.getStatus().equals(Status.AVAILABLE)) {
                    addPageMessage(respage.getString("this_subject_is_not_available_for_this_study") + " "
                        + respage.getString("please_contact_sysadmin_for_more_information"));
                    forwardPage(Page.LIST_STUDY_SUBJECTS_SERVLET);
                    return;
                }

                request.setAttribute("subject", subject);
                request.setAttribute("subjectStudy", study);
                request.setAttribute("studySub", studySub);
                request.setAttribute("events", displayEvents);

                forwardPage(Page.REMOVE_STUDY_SUBJECT);
            } else {
                logger.info("submit to remove the subject from study");
                // remove subject from study
                studySub.setStatus(Status.DELETED);
                studySub.setUpdater(ub);
                studySub.setUpdatedDate(new Date());
                studySub.setUserStatus(UserStatus.INACTIVE);
                subdao.update(studySub);

                dnDao = new DiscrepancyNoteDAO(sm.getDataSource());
                // OC-9585 OC4 Auto close queries when a Participant is removed
                // parentDiscrepancyNoteList is the list of the parent DNs records only
                ArrayList<DiscrepancyNoteBean> parentDiscrepancyNoteList = dnDao.findParentNotesBySubject(studySubId);
                for (DiscrepancyNoteBean parentDiscrepancyNote : parentDiscrepancyNoteList) {
                    String description = resword.getString("dn_auto-closed_description");
                    String detailedNotes = resword.getString("dn_auto_closed_detailed_notes_due_to_participant");
                    // create new DN record , new DN Map record , also update the parent record
                    createDiscrepancyNoteBean(description, detailedNotes, parentDiscrepancyNote.getItemId(), study, ub, parentDiscrepancyNote);
                }

                EventCRFDAO ecdao = new EventCRFDAO(sm.getDataSource());

                for (int j = 0; j < displayEvents.size(); j++) {
                    DisplayStudyEventBean dispEvent = displayEvents.get(j);
                    StudyEventBean event = dispEvent.getStudyEvent();

                        ArrayList eventCRFs = ecdao.findAllByStudyEvent(event);

                        ItemDataDAO iddao = new ItemDataDAO(sm.getDataSource());
                        for (int k = 0; k < eventCRFs.size(); k++) {
                            EventCRFBean eventCRF = (EventCRFBean) eventCRFs.get(k);
                                ArrayList itemDatas = iddao.findAllByEventCRFId(eventCRF.getId());
                                for (int a = 0; a < itemDatas.size(); a++) {
                                    ItemDataBean item = (ItemDataBean) itemDatas.get(a);


                                }

                        }

                }

                String emailBody =
                    respage.getString("the_subject") + " " + studySub.getName() + " " + respage.getString("has_been_removed_from_the_study") + study.getName()
                        + ".";

                addPageMessage(emailBody);
//                try{
//                    sendEmail(emailBody);    
//                }catch(Exception ex){
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
        boolean messageSent = sendEmail(ub.getEmail().trim(), respage.getString("remove_event_from_study"), emailBody, false);
        // to admin
        if(messageSent){
            sendEmail(EmailEngine.getAdminEmail(), respage.getString("remove_event_from_study"), emailBody, false);
        }

        logger.info("Sending email done..");
    }

    private void createDiscrepancyNoteBean(String description, String detailedNotes, int itemDataId, Study studyBean, UserAccountBean ub,
                                           DiscrepancyNoteBean parentDiscrepancyNote) {
        DiscrepancyNoteBean dnb = new DiscrepancyNoteBean();
        dnb.setEntityId(itemDataId); // this is needed for DN Map object
        dnb.setStudyId(studyBean.getStudyId());
        dnb.setEntityType(DiscrepancyNoteBean.ITEM_DATA);
        dnb.setDescription(description);
        dnb.setDetailedNotes(detailedNotes);
        dnb.setDiscrepancyNoteTypeId(parentDiscrepancyNote.getDiscrepancyNoteTypeId()); // set to parent DN Type Id
        dnb.setResolutionStatusId(ResolutionStatus.CLOSED_MODIFIED.getId()); // set to closed Modified
        dnb.setColumn("value"); // this is needed for DN Map object
        dnb.setAssignedUser(null);
        dnb.setOwner(ub);
        dnb.setParentDnId(parentDiscrepancyNote.getId());
        dnb.setActivated(false);
        dnb.setThreadUuid(parentDiscrepancyNote.getThreadUuid());
        dnb = (DiscrepancyNoteBean) getDnDao().create(dnb); // create child DN
        getDnDao().createMapping(dnb); // create DN mapping

        DiscrepancyNoteBean itemParentNote = (DiscrepancyNoteBean) getDnDao().findByPK(dnb.getParentDnId());
        itemParentNote.setResolutionStatusId(ResolutionStatus.CLOSED_MODIFIED.getId()); // set to closed Modified
        itemParentNote.setAssignedUser(null);
        itemParentNote.setOwner(ub);
        itemParentNote.setDetailedNotes(detailedNotes);
        getDnDao().update(itemParentNote); // update parent DN
        getDnDao().updateAssignedUserToNull(itemParentNote); // update parent DN assigned user
    }

    public DiscrepancyNoteDAO getDnDao() {
        return dnDao;
    }

}
