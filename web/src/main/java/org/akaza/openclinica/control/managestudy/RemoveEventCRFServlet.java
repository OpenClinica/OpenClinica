/*
 * OpenClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: http://www.openclinica.org/license
 * copyright 2003-2005 Akaza Research
 */
package org.akaza.openclinica.control.managestudy;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.akaza.openclinica.bean.admin.CRFBean;
import org.akaza.openclinica.bean.core.ResolutionStatus;
import org.akaza.openclinica.bean.core.Role;
import org.akaza.openclinica.bean.core.Status;
import org.akaza.openclinica.bean.login.UserAccountBean;
import org.akaza.openclinica.bean.managestudy.DiscrepancyNoteBean;
import org.akaza.openclinica.bean.managestudy.EventDefinitionCRFBean;
import org.akaza.openclinica.bean.managestudy.StudyBean;
import org.akaza.openclinica.bean.managestudy.StudyEventBean;
import org.akaza.openclinica.bean.managestudy.StudyEventDefinitionBean;
import org.akaza.openclinica.bean.managestudy.StudySubjectBean;
import org.akaza.openclinica.bean.submit.CRFVersionBean;
import org.akaza.openclinica.bean.submit.DisplayEventCRFBean;
import org.akaza.openclinica.bean.submit.EventCRFBean;
import org.akaza.openclinica.bean.submit.ItemDataBean;
import org.akaza.openclinica.control.core.SecureController;
import org.akaza.openclinica.control.form.FormProcessor;
import org.akaza.openclinica.core.LockInfo;
import org.akaza.openclinica.dao.admin.CRFDAO;
import org.akaza.openclinica.dao.login.UserAccountDAO;
import org.akaza.openclinica.dao.managestudy.DiscrepancyNoteDAO;
import org.akaza.openclinica.dao.managestudy.EventDefinitionCRFDAO;
import org.akaza.openclinica.dao.managestudy.StudyDAO;
import org.akaza.openclinica.dao.managestudy.StudyEventDAO;
import org.akaza.openclinica.dao.managestudy.StudyEventDefinitionDAO;
import org.akaza.openclinica.dao.managestudy.StudySubjectDAO;
import org.akaza.openclinica.dao.submit.CRFVersionDAO;
import org.akaza.openclinica.dao.submit.EventCRFDAO;
import org.akaza.openclinica.dao.submit.ItemDataDAO;
import org.akaza.openclinica.view.Page;
import org.akaza.openclinica.web.InsufficientPermissionException;

/**
 * Removes an Event CRF
 * 
 * @author jxu
 * 
 */
public class RemoveEventCRFServlet extends SecureController {
    /**
     * 
     */
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
        int eventCRFId = fp.getInt("id");// eventCRFId
        int studySubId = fp.getInt("studySubId");// studySubjectId
        checkStudyLocked("ViewStudySubject?id" + studySubId, respage.getString("current_study_locked"));
        StudyEventDAO sedao = new StudyEventDAO(sm.getDataSource());
        StudySubjectDAO subdao = new StudySubjectDAO(sm.getDataSource());
        EventCRFDAO ecdao = new EventCRFDAO(sm.getDataSource());
        StudyDAO sdao = new StudyDAO(sm.getDataSource());

        if (eventCRFId == 0) {
            addPageMessage(respage.getString("please_choose_an_event_CRF_to_remove"));
            request.setAttribute("id", new Integer(studySubId).toString());
            forwardPage(Page.VIEW_STUDY_SUBJECT_SERVLET);
        } else {
            EventCRFBean eventCRF = (EventCRFBean) ecdao.findByPK(eventCRFId);

            StudySubjectBean studySub = (StudySubjectBean) subdao.findByPK(studySubId);
            request.setAttribute("studySub", studySub);

            // construct info needed on view event crf page
            CRFDAO cdao = new CRFDAO(sm.getDataSource());
            CRFVersionDAO cvdao = new CRFVersionDAO(sm.getDataSource());

            int crfVersionId = eventCRF.getCRFVersionId();
            CRFBean cb = cdao.findByVersionId(crfVersionId);
            eventCRF.setCrf(cb);

            CRFVersionBean cvb = (CRFVersionBean) cvdao.findByPK(crfVersionId);
            eventCRF.setCrfVersion(cvb);

            // then get the definition so we can call
            // DisplayEventCRFBean.setFlags
            int studyEventId = eventCRF.getStudyEventId();

            StudyEventBean event = (StudyEventBean) sedao.findByPK(studyEventId);

            int studyEventDefinitionId = sedao.getDefinitionIdFromStudyEventId(studyEventId);
            StudyEventDefinitionDAO seddao = new StudyEventDefinitionDAO(sm.getDataSource());
            StudyEventDefinitionBean sed = (StudyEventDefinitionBean) seddao.findByPK(studyEventDefinitionId);
            event.setStudyEventDefinition(sed);
            request.setAttribute("event", event);

            EventDefinitionCRFDAO edcdao = new EventDefinitionCRFDAO(sm.getDataSource());

            StudyBean study = (StudyBean) sdao.findByPK(studySub.getStudyId());
            EventDefinitionCRFBean edc = edcdao.findByStudyEventDefinitionIdAndCRFId(study, studyEventDefinitionId, cb.getId());

            DisplayEventCRFBean dec = new DisplayEventCRFBean();
            dec.setEventCRF(eventCRF);
            dec.setFlags(eventCRF, ub, currentRole, edc.isDoubleEntry());

            // find all item data
            ItemDataDAO iddao = new ItemDataDAO(sm.getDataSource());

            ArrayList itemData = iddao.findAllByEventCRFId(eventCRF.getId());

            request.setAttribute("items", itemData);
            String action = request.getParameter("action");

            if (getEventCrfLocker().isLocked(currentPublicStudy.getSchemaName()
                    + eventCRF.getStudyEventId() + eventCRF.getFormLayoutId(), ub.getId(), request.getSession().getId())) {

                LockInfo lockInfo = getEventCrfLocker().getLockOwner(currentPublicStudy.getSchemaName()
                        + eventCRF.getStudyEventId() + eventCRF.getFormLayoutId());
                UserAccountDAO uDAO = new UserAccountDAO(sm.getDataSource());
                UserAccountBean userAccountBean = (UserAccountBean) uDAO.findByPK(lockInfo.getUserId());
                request.setAttribute("errorData", "This form is currently unavailable for this action.\\n " +
                        "User " + userAccountBean.getName() +" is currently entering data.\\n " +
                        resword.getString("CRF_perform_action") +"\\n");
                if ("confirm".equalsIgnoreCase(action)) {
                    request.setAttribute("id", new Integer(studySubId).toString());
                    forwardPage(Page.VIEW_STUDY_SUBJECT_SERVLET);
                    return;
                } else {
                    request.setAttribute("displayEventCRF", dec);
                    forwardPage(Page.REMOVE_EVENT_CRF);
                    return;
                }
            }
            if ("confirm".equalsIgnoreCase(action)) {
                if (eventCRF.getStatus().equals(Status.DELETED) || eventCRF.getStatus().equals(Status.AUTO_DELETED)) {
                    addPageMessage(respage.getString("this_event_CRF_is_removed_for_this_study") + " "
                            + respage.getString("please_contact_sysadmin_for_more_information"));
                    request.setAttribute("id", new Integer(studySubId).toString());
                    forwardPage(Page.VIEW_STUDY_SUBJECT_SERVLET);
                    return;
                }

                request.setAttribute("displayEventCRF", dec);

                forwardPage(Page.REMOVE_EVENT_CRF);
            } else {
                logger.info("submit to remove the event CRF from study");

                eventCRF.setStatus(Status.DELETED);
                eventCRF.setUpdater(ub);
                eventCRF.setUpdatedDate(new Date());
                ecdao.update(eventCRF);

                // remove all the item data
                for (int a = 0; a < itemData.size(); a++) {
                    ItemDataBean item = (ItemDataBean) itemData.get(a);
                    if (!item.getStatus().equals(Status.DELETED)) {
                        item.setStatus(Status.AUTO_DELETED);
                        item.setUpdater(ub);
                        item.setUpdatedDate(new Date());
                        iddao.update(item);
                        DiscrepancyNoteDAO dnDao = new DiscrepancyNoteDAO(sm.getDataSource());
                        List dnNotesOfRemovedItem = dnDao.findExistingNotesForItemData(item.getId());
                        if (!dnNotesOfRemovedItem.isEmpty()) {
                            DiscrepancyNoteBean itemParentNote = null;
                            for (Object obj : dnNotesOfRemovedItem) {
                                if (((DiscrepancyNoteBean) obj).getParentDnId() == 0) {
                                    itemParentNote = (DiscrepancyNoteBean) obj;
                                }
                            }
                            DiscrepancyNoteBean dnb = new DiscrepancyNoteBean();
                            if (itemParentNote != null) {
                                dnb.setParentDnId(itemParentNote.getId());
                                dnb.setDiscrepancyNoteTypeId(itemParentNote.getDiscrepancyNoteTypeId());
                            }
                            dnb.setResolutionStatusId(ResolutionStatus.CLOSED.getId());
                            dnb.setStudyId(currentStudy.getId());
                            dnb.setAssignedUserId(ub.getId());
                            dnb.setOwner(ub);
                            dnb.setEntityType(DiscrepancyNoteBean.ITEM_DATA);
                            dnb.setEntityId(item.getId());
                            dnb.setColumn("value");
                            dnb.setCreatedDate(new Date());
                            dnb.setDetailedNotes("The item has been removed, this Query has been Closed.");
                            dnDao.create(dnb);
                            dnDao.createMapping(dnb);
                            itemParentNote.setResolutionStatusId(ResolutionStatus.CLOSED.getId());
                            dnDao.update(itemParentNote);
                        }
                    }
                }

                /* OC-8797
                    Do not send email notification when data is removed
                    String emailBody = respage.getString("the_event_CRF") + " " + cb.getName() + " " + respage.getString("has_been_removed_from_the_event")
                            + event.getStudyEventDefinition().getName() + ". " + respage.getString("has_been_removed_from_the_event_cont");

                    addPageMessage(emailBody);
                    sendEmail(emailBody);
                */
                request.setAttribute("id", new Integer(studySubId).toString());
                forwardPage(Page.VIEW_STUDY_SUBJECT_SERVLET);
            }
        }
    }

    /**
     * Send email to director and administrator
     * 
     * @param emailBody
     */
    private void sendEmail(String emailBody) throws Exception {

        logger.info("Sending email...");
        // to study director

        sendEmail(ub.getEmail().trim(), respage.getString("remove_event_CRF_from_event"), emailBody, false);
        // sendEmail(EmailEngine.getAdminEmail(), respage.getString("remove_event_CRF_from_event"), emailBody, false);
        logger.info("Sending email done..");
    }

}
