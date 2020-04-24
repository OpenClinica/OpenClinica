/*
 * OpenClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: http://www.openclinica.org/license
 * copyright 2003-2005 Akaza Research
 */
package org.akaza.openclinica.control.admin;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import core.org.akaza.openclinica.bean.admin.CRFBean;
import core.org.akaza.openclinica.bean.core.ResolutionStatus;
import core.org.akaza.openclinica.bean.core.Role;
import core.org.akaza.openclinica.bean.core.Status;
import core.org.akaza.openclinica.bean.login.UserAccountBean;
import core.org.akaza.openclinica.bean.managestudy.DiscrepancyNoteBean;
import core.org.akaza.openclinica.bean.managestudy.EventDefinitionCRFBean;
import core.org.akaza.openclinica.bean.managestudy.StudyEventBean;
import core.org.akaza.openclinica.bean.managestudy.StudyEventDefinitionBean;
import core.org.akaza.openclinica.bean.managestudy.StudySubjectBean;
import core.org.akaza.openclinica.bean.submit.*;
import core.org.akaza.openclinica.dao.hibernate.*;
import core.org.akaza.openclinica.domain.datamap.Study;
import org.akaza.openclinica.control.SpringServletAccess;
import org.akaza.openclinica.control.core.SecureController;
import org.akaza.openclinica.control.form.FormProcessor;
import core.org.akaza.openclinica.core.LockInfo;
import core.org.akaza.openclinica.dao.admin.CRFDAO;
import core.org.akaza.openclinica.dao.login.UserAccountDAO;
import core.org.akaza.openclinica.dao.managestudy.DiscrepancyNoteDAO;
import core.org.akaza.openclinica.dao.managestudy.EventDefinitionCRFDAO;
import core.org.akaza.openclinica.dao.managestudy.StudyEventDAO;
import core.org.akaza.openclinica.dao.managestudy.StudyEventDefinitionDAO;
import core.org.akaza.openclinica.dao.managestudy.StudySubjectDAO;
import core.org.akaza.openclinica.dao.submit.*;
import core.org.akaza.openclinica.domain.datamap.EventCrf;
import core.org.akaza.openclinica.domain.rule.action.RuleActionRunLogBean;
import org.akaza.openclinica.domain.enumsupport.EventCrfWorkflowStatusEnum;
import org.akaza.openclinica.domain.enumsupport.StudyEventWorkflowStatusEnum;
import org.akaza.openclinica.view.Page;
import core.org.akaza.openclinica.web.InsufficientPermissionException;
import org.apache.commons.lang.BooleanUtils;

/**
 * @author jxu
 * 
 *         TODO To change the template for this generated type comment go to Window - Preferences - Java - Code Style -
 *         Code Templates
 */
public class DeleteEventCRFServlet extends SecureController {

    public static String STUDY_SUB_ID = "ssId";

    public static String EVENT_CRF_ID = "eventCrfId";
    DiscrepancyNoteDAO dnDao;
    RuleActionRunLogDao ruleActionRunLogDao;
    DynamicsItemFormMetadataDao dynamicsItemFormMetadataDao;
    DynamicsItemGroupMetadataDao dynamicsItemGroupMetadataDao;
    ItemFormMetadataDAO ifmdao;
    ItemDataDAO iddao;
    ItemGroupMetadataDAO igmdao;
    StudyEventDAO sedao;

    /**
     * 
     */
    @Override
    public void mayProceed() throws InsufficientPermissionException {
        if (!currentRole.getRole().equals(Role.MONITOR)) {
            return;
        }

        addPageMessage(respage.getString("no_have_correct_privilege_current_study") + respage.getString("change_study_contact_sysadmin"));
        throw new InsufficientPermissionException(Page.LIST_STUDY_SUBJECTS, resexception.getString("not_admin"), "1");

    }

    @Override
    public void processRequest() throws Exception {
        FormProcessor fp = new FormProcessor(request);
        int studySubId = fp.getInt(STUDY_SUB_ID, true);
        int eventCRFId = fp.getInt(EVENT_CRF_ID);

        String action = request.getParameter("action");

        StudyEventDAO sedao = new StudyEventDAO(sm.getDataSource());
        StudySubjectDAO subdao = new StudySubjectDAO(sm.getDataSource());
        EventCRFDAO ecdao = new EventCRFDAO(sm.getDataSource());
        request.setAttribute("errorData", null);
        String originatingPage = request.getParameter(ORIGINATING_PAGE);
        request.setAttribute(ORIGINATING_PAGE, originatingPage);
        EventCrfDao eventCrfDao = (EventCrfDao) SpringServletAccess.getApplicationContext(context).getBean("eventCrfDao");

        if (eventCRFId == 0) {
            addPageMessage(respage.getString("please_choose_an_event_CRF_to_delete"));
            request.setAttribute("id", new Integer(studySubId).toString());
            forwardPage(Page.VIEW_STUDY_SUBJECT_SERVLET);
        } else {

            EventCRFBean eventCRF = (EventCRFBean) ecdao.findByPK(eventCRFId);
            final EventCrf ec = eventCrfDao.findById(eventCRFId);

            if (hasFormAccess(ec) != true) {
                forwardPage(Page.NO_ACCESS);
                return;
            }
            StudySubjectBean studySub = (StudySubjectBean) subdao.findByPK(studySubId);
            request.setAttribute("studySub", studySub);

            // construct info needed on view event crf page
            CRFDAO cdao = new CRFDAO(sm.getDataSource());
            FormLayoutDAO fldao = new FormLayoutDAO(sm.getDataSource());

            int crfVersionId = eventCRF.getCRFVersionId();
            int formLayoutId = eventCRF.getFormLayoutId();
            CRFBean cb = cdao.findByLayoutId(formLayoutId);
            eventCRF.setCrf(cb);

            FormLayoutBean flb = (FormLayoutBean) fldao.findByPK(formLayoutId);
            eventCRF.setFormLayout(flb);

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

            Study study = (Study) getStudyDao().findByPK(studySub.getStudyId());
            EventDefinitionCRFBean edc = edcdao.findByStudyEventDefinitionIdAndCRFId(study, studyEventDefinitionId, cb.getId());

            DisplayEventCRFBean dec = new DisplayEventCRFBean();
            dec.setEventCRF(eventCRF);
            dec.setFlags(eventCRF, ub, currentRole, edc.isDoubleEntry());

            // find all item data
            ItemDataDAO iddao = new ItemDataDAO(sm.getDataSource());
            dnDao = new DiscrepancyNoteDAO(sm.getDataSource());
            ArrayList<ItemDataBean> itemData = iddao.findAllByEventCRFId(eventCRF.getId());
            request.setAttribute("items", itemData);
            if (getEventCrfLocker().isLocked(currentPublicStudy.getSchemaName()
                    + eventCRF.getStudyEventId() + eventCRF.getFormLayoutId(), ub.getId(), request.getSession().getId())) {
                LockInfo lockInfo = getEventCrfLocker().getLockOwner(currentPublicStudy.getSchemaName()
                        + eventCRF.getStudyEventId() + eventCRF.getFormLayoutId());
                if (lockInfo != null) {
                    UserAccountDAO udao = new UserAccountDAO(sm.getDataSource());
                    UserAccountBean ubean = (UserAccountBean) udao.findByPK(lockInfo.getUserId());
                    String errorData = "This form is currently unavailable for this action.\\n " +
                            "User " + ubean.getName() +" is currently entering data.\\n " +
                            resword.getString("CRF_perform_action") +"\\n";
                    request.setAttribute("errorData", errorData);
                }
                if ("confirm".equalsIgnoreCase(action)) {
                    request.setAttribute("id", new Integer(studySubId).toString());
                    forwardPage(Page.VIEW_STUDY_SUBJECT_SERVLET);
                    return;
                } else {
                    request.setAttribute("displayEventCRF", dec);
                    forwardPage(Page.DELETE_EVENT_CRF);
                }
            }
            if ("confirm".equalsIgnoreCase(action)) {

                request.setAttribute("displayEventCRF", dec);

                forwardPage(Page.DELETE_EVENT_CRF);
            } else {
                logger.info("submit to delete the event CRF from event");

                // OC-6303 Deleting Event CRF resets Show / Hide logic
                // delete records from DynamicItemForm and DynamicItemGroup
                // getDynamicsItemFormMetadataDao().delete(eventCRFId);
                // getDynamicsItemGroupMetadataDao().delete(eventCRFId);

                eventCRF.setOldStatus(eventCRF.getStatus());
                eventCRF.setStatus(Status.RESET);
                eventCRF.setUpdater(ub);
                eventCRF.setDateCompleted(null);
                ecdao.update(eventCRF);


                for (ItemDataBean itemdata : itemData) {
                    // OC-6343 Rule behaviour must be reset if an Event CRF is deleted
                    // delete the records from ruleActionRunLogDao

                    List<RuleActionRunLogBean> ruleActionRunLog = getRuleActionRunLogDao().findAllItemData(itemdata.getId());
                    if (ruleActionRunLog.size() != 0) {
                        getRuleActionRunLogDao().delete(itemdata.getId());
                    }
                    // OC-6344 Notes & Discrepancies must be set to "closed" when event CRF is deleted
                    // parentDiscrepancyNoteList is the list of the parent DNs records only
                    ArrayList<DiscrepancyNoteBean> parentDiscrepancyNoteList = getDnDao().findParentNotesOnlyByItemData(itemdata.getId());
                    for (DiscrepancyNoteBean parentDiscrepancyNote : parentDiscrepancyNoteList) {
                        String description = resword.getString("dn_auto-closed_description");
                        String detailedNotes = resword.getString("dn_auto_closed_detailed_notes");
                        // create new DN record , new DN Map record , also update the parent record
                        createDiscrepancyNoteBean(description, detailedNotes, itemdata.getId(), study, ub, parentDiscrepancyNote);
                    }
                    iddao = new ItemDataDAO(sm.getDataSource());
                    ifmdao = new ItemFormMetadataDAO(sm.getDataSource());
                    ItemDataBean idBean = (ItemDataBean) iddao.findByPK(itemdata.getId());

                    ItemFormMetadataBean ifmBean = ifmdao.findByItemIdAndCRFVersionId(idBean.getItemId(), crfVersionId);

                    // Updating Dn_item_data_map actovated column into false for the existing DNs
                    ArrayList<DiscrepancyNoteBean> dnBeans = getDnDao().findExistingNotesForItemData(itemdata.getId());
                    if (dnBeans.size() != 0) {
                        DiscrepancyNoteBean dnBean = new DiscrepancyNoteBean();
                        dnBean.setEntityId(itemdata.getId());
                        dnBean.setActivated(false);
                        getDnDao().updateDnMapActivation(dnBean);
                    }

                    // Default Values are not addressed

                    itemdata.setValue("");
                    itemdata.setUpdatedDate(new Date());
                    itemdata.setUpdater(ub);
                    iddao.update(itemdata);

                }
                // OC-6291 event_crf status change
                eventCRF.setOldStatus(eventCRF.getStatus());
                eventCRF.setWorkflowStatus(EventCrfWorkflowStatusEnum.NOT_STARTED);
                eventCRF.setUpdater(ub);
                eventCRF.setDateCompleted(null);
                ecdao.update(eventCRF);

                if (event.getWorkflowStatus().equals(StudyEventWorkflowStatusEnum.COMPLETED) ) {
                    event.setWorkflowStatus(StudyEventWorkflowStatusEnum.DATA_ENTRY_STARTED);
                    event.setUpdater(ub);
                    event.setUpdatedDate(new Date());
                    sedao.update(event);
                }
                if ( event.isSigned()) {
                    event.setSigned(Boolean.FALSE);
                    event.setUpdater(ub);
                    event.setUpdatedDate(new Date());
                    sedao.update(event);
                }
                if(studySub.getStatus().equals(Status.SIGNED)){
                    studySub.setStatus(Status.AVAILABLE);
                    studySub.setUpdater(ub);
                    studySub.setUpdatedDate(new Date());
                    subdao.update(studySub);
                }

                String emailBody = respage.getString("the_event_CRF") + cb.getName() + respage.getString("has_been_deleted_from_the_event")
                        + event.getStudyEventDefinition().getName() + ". " + respage.getString("has_been_deleted_from_the_event_cont");

                addPageMessage(emailBody);
                // sendEmail(emailBody);
                request.setAttribute("id", new Integer(studySubId).toString());
                forwardPage(Page.VIEW_STUDY_SUBJECT_SERVLET);
            }

        }
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

    public void setDnDao(DiscrepancyNoteDAO dnDao) {
        this.dnDao = dnDao;
    }

    private RuleActionRunLogDao getRuleActionRunLogDao() {
        ruleActionRunLogDao = this.ruleActionRunLogDao != null ? ruleActionRunLogDao
                : (RuleActionRunLogDao) SpringServletAccess.getApplicationContext(context).getBean("ruleActionRunLogDao");
        return ruleActionRunLogDao;
    }

    private DynamicsItemFormMetadataDao getDynamicsItemFormMetadataDao() {
        dynamicsItemFormMetadataDao = this.dynamicsItemFormMetadataDao != null ? dynamicsItemFormMetadataDao
                : (DynamicsItemFormMetadataDao) SpringServletAccess.getApplicationContext(context).getBean("dynamicsItemFormMetadataDao");
        return dynamicsItemFormMetadataDao;
    }

    private DynamicsItemGroupMetadataDao getDynamicsItemGroupMetadataDao() {
        dynamicsItemGroupMetadataDao = this.dynamicsItemGroupMetadataDao != null ? dynamicsItemGroupMetadataDao
                : (DynamicsItemGroupMetadataDao) SpringServletAccess.getApplicationContext(context).getBean("dynamicsItemGroupMetadataDao");
        return dynamicsItemGroupMetadataDao;
    }

}
