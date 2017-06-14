/*
 * OpenClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: http://www.openclinica.org/license
 * copyright 2003-2005 Akaza Research
 */
package org.akaza.openclinica.control.admin;

import java.util.ArrayList;
import java.util.List;

import org.akaza.openclinica.bean.admin.CRFBean;
import org.akaza.openclinica.bean.core.Status;
import org.akaza.openclinica.bean.core.SubjectEventStatus;
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
import org.akaza.openclinica.bean.submit.ItemFormMetadataBean;
import org.akaza.openclinica.control.SpringServletAccess;
import org.akaza.openclinica.control.core.SecureController;
import org.akaza.openclinica.control.form.FormProcessor;
import org.akaza.openclinica.dao.admin.CRFDAO;
import org.akaza.openclinica.dao.hibernate.DynamicsItemFormMetadataDao;
import org.akaza.openclinica.dao.hibernate.DynamicsItemGroupMetadataDao;
import org.akaza.openclinica.dao.hibernate.RuleActionRunLogDao;
import org.akaza.openclinica.dao.managestudy.DiscrepancyNoteDAO;
import org.akaza.openclinica.dao.managestudy.EventDefinitionCRFDAO;
import org.akaza.openclinica.dao.managestudy.StudyDAO;
import org.akaza.openclinica.dao.managestudy.StudyEventDAO;
import org.akaza.openclinica.dao.managestudy.StudyEventDefinitionDAO;
import org.akaza.openclinica.dao.managestudy.StudySubjectDAO;
import org.akaza.openclinica.dao.submit.CRFVersionDAO;
import org.akaza.openclinica.dao.submit.EventCRFDAO;
import org.akaza.openclinica.dao.submit.ItemDataDAO;
import org.akaza.openclinica.dao.submit.ItemFormMetadataDAO;
import org.akaza.openclinica.dao.submit.ItemGroupMetadataDAO;
import org.akaza.openclinica.domain.rule.action.RuleActionRunLogBean;
import org.akaza.openclinica.view.Page;
import org.akaza.openclinica.web.InsufficientPermissionException;

/**
 * @author jxu
 * 
 *         TODO To change the template for this generated type comment go to Window - Preferences - Java - Code Style -
 *         Code Templates
 */
public class DeleteEventCRFServlet extends SecureController {
    public static String STUDY_SUB_ID = "ssId";

    public static String EVENT_CRF_ID = "ecId";
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
        if (ub.isSysAdmin()) {
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
        StudyDAO sdao = new StudyDAO(sm.getDataSource());

        if (eventCRFId == 0) {
            addPageMessage(respage.getString("please_choose_an_event_CRF_to_delete"));
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
            dnDao = new DiscrepancyNoteDAO(sm.getDataSource());
            ArrayList<ItemDataBean> itemData = iddao.findAllByEventCRFId(eventCRF.getId());
            request.setAttribute("items", itemData);

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
                    itemdata.setOldStatus(itemdata.getStatus());
                    itemdata.setOwner(ub);
                    itemdata.setStatus(Status.AVAILABLE);
                    itemdata.setUpdater(ub);
                    iddao.updateUser(itemdata);
                    iddao.update(itemdata);

                }
                // OC-6291 event_crf status change

                eventCRF.setOldStatus(eventCRF.getStatus());
                eventCRF.setStatus(Status.AVAILABLE);
                eventCRF.setUpdater(ub);
                ecdao.update(eventCRF);

                if (event.getSubjectEventStatus().isCompleted() || event.getSubjectEventStatus().isSigned()) {
                    event.setSubjectEventStatus(SubjectEventStatus.DATA_ENTRY_STARTED);
                    event.setUpdater(ub);
                    sedao = new StudyEventDAO(sm.getDataSource());
                    sedao.update(event);
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

    private void createDiscrepancyNoteBean(String description, String detailedNotes, int itemDataId, StudyBean studyBean, UserAccountBean ub,
            DiscrepancyNoteBean parentDiscrepancyNote) {
        DiscrepancyNoteBean dnb = new DiscrepancyNoteBean();
        dnb.setEntityId(itemDataId); // this is needed for DN Map object
        dnb.setStudyId(studyBean.getId());
        dnb.setEntityType(DiscrepancyNoteBean.ITEM_DATA);
        dnb.setDescription(description);
        dnb.setDetailedNotes(detailedNotes);
        dnb.setDiscrepancyNoteTypeId(parentDiscrepancyNote.getDiscrepancyNoteTypeId()); // set to parent DN Type Id
        dnb.setResolutionStatusId(6); // set to closed-modified
        dnb.setColumn("value"); // this is needed for DN Map object
        dnb.setAssignedUser(null);
        dnb.setOwner(ub);
        dnb.setParentDnId(parentDiscrepancyNote.getId());
        dnb.setActivated(false);
        dnb = (DiscrepancyNoteBean) getDnDao().create(dnb); // create child DN
        getDnDao().createMapping(dnb); // create DN mapping

        DiscrepancyNoteBean itemParentNote = (DiscrepancyNoteBean) getDnDao().findByPK(dnb.getParentDnId());
        itemParentNote.setResolutionStatusId(6); // set to closed-modified
        itemParentNote.setAssignedUser(null);
        itemParentNote.setOwner(ub);
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
