/*
 * OpenClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).
 *
 * For details see: http://www.openclinica.org/license
 */
package org.akaza.openclinica.control.submit;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

import javax.servlet.http.HttpSession;

import org.akaza.openclinica.bean.core.DiscrepancyNoteType;
import org.akaza.openclinica.bean.core.NumericComparisonOperator;
import org.akaza.openclinica.bean.core.ResolutionStatus;
import org.akaza.openclinica.bean.core.Status;
import org.akaza.openclinica.bean.core.SubjectEventStatus;
import org.akaza.openclinica.bean.login.UserAccountBean;
import org.akaza.openclinica.bean.managestudy.DiscrepancyNoteBean;
import org.akaza.openclinica.bean.managestudy.StudyBean;
import org.akaza.openclinica.bean.managestudy.StudyEventBean;
import org.akaza.openclinica.bean.managestudy.StudySubjectBean;
import org.akaza.openclinica.bean.submit.EventCRFBean;
import org.akaza.openclinica.bean.submit.ItemBean;
import org.akaza.openclinica.bean.submit.ItemDataBean;
import org.akaza.openclinica.bean.submit.SectionBean;
import org.akaza.openclinica.control.core.SecureController;
import org.akaza.openclinica.control.form.FormDiscrepancyNotes;
import org.akaza.openclinica.control.form.FormProcessor;
import org.akaza.openclinica.control.form.Validator;
import org.akaza.openclinica.core.EmailEngine;
import org.akaza.openclinica.dao.login.UserAccountDAO;
import org.akaza.openclinica.dao.managestudy.DiscrepancyNoteDAO;
import org.akaza.openclinica.dao.managestudy.StudyDAO;
import org.akaza.openclinica.dao.managestudy.StudyEventDAO;
import org.akaza.openclinica.dao.managestudy.StudySubjectDAO;
import org.akaza.openclinica.dao.submit.EventCRFDAO;
import org.akaza.openclinica.dao.submit.ItemDAO;
import org.akaza.openclinica.dao.submit.ItemDataDAO;
import org.akaza.openclinica.dao.submit.SectionDAO;
import org.akaza.openclinica.view.Page;
import org.akaza.openclinica.web.InsufficientPermissionException;
import org.akaza.openclinica.web.SQLInitServlet;

/**
 * Create a discrepancy note
 * 
 */
public class CreateOneDiscrepancyNoteServlet extends SecureController {

    Locale locale;
    // < ResourceBundleresexception,respage;

    public static final String DIS_TYPES = "discrepancyTypes";
    public static final String RES_STATUSES = "resolutionStatuses";
    public static final String ENTITY_ID = "id";
    public static final String SUBJECT_ID = "subjectId";
    public static final String ITEM_ID = "itemId";
    public static final String PARENT_ID = "parentId";// parent note id
    public static final String ENTITY_TYPE = "name";
    public static final String ENTITY_COLUMN = "column";
    public static final String ENTITY_FIELD = "field";
    public static final String FORM_DISCREPANCY_NOTES_NAME = "fdnotes";
    public static final String DIS_NOTE = "discrepancyNote";
    public static final String RES_STATUS_ID = "resStatusId";
    public static final String USER_ACCOUNTS = "userAccounts";//a list of user accounts
    public static final String SUBMITTED_USER_ACCOUNT_ID = "userAccountId";
    public static final String PRESET_USER_ACCOUNT_ID = "preUserAccountId";
    public static final String EMAIL_USER_ACCOUNT = "sendEmail";
    public static final String BOX_DN_MAP = "boxDNMap";
    public static final String BOX_TO_SHOW = "boxToShow";
    public static final String CLOSE_WINDOW = "closeWindow";

    /*
     * (non-Javadoc)
     * 
     * @see org.akaza.openclinica.control.core.SecureController#mayProceed()
     */
    @Override
    protected void mayProceed() throws InsufficientPermissionException {
        checkStudyLocked(Page.MENU_SERVLET, respage.getString("current_study_locked"));
        locale = request.getLocale();

        String exceptionName = resexception.getString("no_permission_to_create_discrepancy_note");
        String noAccessMessage = respage.getString("you_may_not_create_discrepancy_note") + respage.getString("change_study_contact_sysadmin");

        if (SubmitDataServlet.mayViewData(ub, currentRole)) {
            return;
        }

        addPageMessage(noAccessMessage);
        throw new InsufficientPermissionException(Page.MENU, exceptionName, "1");
    }

    @Override
    protected void processRequest() throws Exception {
        FormProcessor fp = new FormProcessor(request);
        DiscrepancyNoteDAO dndao = new DiscrepancyNoteDAO(sm.getDataSource());

        int parentId = fp.getInt(PARENT_ID);
        DiscrepancyNoteBean parent = parentId > 0 ? (DiscrepancyNoteBean) dndao.findByPK(parentId) : new DiscrepancyNoteBean();
        HashMap<Integer, DiscrepancyNoteBean> boxDNMap = (HashMap<Integer, DiscrepancyNoteBean>) session.getAttribute(BOX_DN_MAP);
        DiscrepancyNoteBean dn =
            boxDNMap.size() > 0 && boxDNMap.containsKey(Integer.valueOf(parentId)) ? boxDNMap.get(Integer.valueOf(parentId)) : new DiscrepancyNoteBean();
        int entityId = fp.getInt(ENTITY_ID, true);
        entityId = entityId > 0 ? entityId : parent.getEntityId();
        if (entityId == 0) {
            Validator.addError(errors, "newChildAdded" + parentId, respage.getString("note_cannot_be_saved"));
            logger.info("entityId is 0. Note saving can not be started.");
        }
        String entityType = fp.getString(ENTITY_TYPE, true);
        
        FormDiscrepancyNotes noteTree = (FormDiscrepancyNotes) session.getAttribute(FORM_DISCREPANCY_NOTES_NAME);
        if (noteTree == null) {
            noteTree = new FormDiscrepancyNotes();
        }
        session.removeAttribute(CLOSE_WINDOW);

        String description = fp.getString("description" + parentId);
        int typeId = fp.getInt("typeId" + parentId);
        String detailedDes = fp.getString("detailedDes" + parentId);
        int resStatusId = fp.getInt(RES_STATUS_ID + parentId);
        int assignedUserAccountId = fp.getInt(SUBMITTED_USER_ACCOUNT_ID + parentId);
        String viewNoteLink = fp.getString("viewDNLink" + parentId);

        Validator v = new Validator(request);
        v.addValidation("description" + parentId, Validator.NO_BLANKS);
        v.addValidation("description" + parentId, Validator.LENGTH_NUMERIC_COMPARISON, NumericComparisonOperator.LESS_THAN_OR_EQUAL_TO, 255);
        v.addValidation("detailedDes" + parentId, Validator.LENGTH_NUMERIC_COMPARISON, NumericComparisonOperator.LESS_THAN_OR_EQUAL_TO, 1000);
        v.addValidation("typeId" + parentId, Validator.NO_BLANKS);
        HashMap errors = v.validate();

        dn.setParentDnId(parentId);
        dn.setDescription(description);
        dn.setDiscrepancyNoteTypeId(typeId);
        dn.setDetailedNotes(detailedDes);
        dn.setResolutionStatusId(resStatusId);
        // dn.setAssignedUserId(assignedUserAccountId);
        // logic from CreateDiscrepancyNoteServlet
        if (typeId != DiscrepancyNoteType.ANNOTATION.getId() && typeId != DiscrepancyNoteType.FAILEDVAL.getId()
            && typeId != DiscrepancyNoteType.REASON_FOR_CHANGE.getId()) {
            if (assignedUserAccountId > 0) {
                dn.setAssignedUserId(assignedUserAccountId);
            } else {
                dn.setAssignedUserId(parent.getOwnerId());
            }
        }
        if (DiscrepancyNoteType.ANNOTATION.getId() == dn.getDiscrepancyNoteTypeId()) {
            updateStudyEvent(entityType, entityId);
            updateStudySubjectStatus(entityType, entityId);
        }
        if (DiscrepancyNoteType.ANNOTATION.getId() == dn.getDiscrepancyNoteTypeId()
            || DiscrepancyNoteType.REASON_FOR_CHANGE.getId() == dn.getDiscrepancyNoteTypeId()) {
            dn.setResStatus(ResolutionStatus.NOT_APPLICABLE);
            dn.setResolutionStatusId(ResolutionStatus.NOT_APPLICABLE.getId());
        }
        if (DiscrepancyNoteType.FAILEDVAL.getId() == dn.getDiscrepancyNoteTypeId()
            || DiscrepancyNoteType.QUERY.getId() == dn.getDiscrepancyNoteTypeId()) {
            if (ResolutionStatus.NOT_APPLICABLE.getId() == dn.getResolutionStatusId()) {
                Validator.addError(errors, RES_STATUS_ID + parentId, restext.getString("not_valid_res_status"));
            }
        }
        //
        

        if (errors.isEmpty()) {
            HashMap<String, ArrayList<String>> results = new HashMap<String, ArrayList<String>>();
            ArrayList<String> mess = new ArrayList<String>();

            String column = fp.getString(ENTITY_COLUMN, true);
            String field = fp.getString(ENTITY_FIELD, true);

            dn.setOwner(ub);
            dn.setStudyId(currentStudy.getId());
            dn.setEntityId(entityId);
            dn.setEntityType(entityType);
            dn.setColumn(column);
            dn.setField(field);

            if(parentId > 0) {
                //dn.setOwnerId(parent.getOwnerId());
                if (dn.getDiscrepancyNoteTypeId() == parent.getDiscrepancyNoteTypeId()) {
                    if (dn.getResolutionStatusId() != parent.getResolutionStatusId()) {
                        parent.setResolutionStatusId(dn.getResolutionStatusId());
                        dndao.update(parent);
                        if(!parent.isActive()) {
                            logger.info("Failed to update resolution status ID for the parent dn ID = " + parentId + ". ");
                        }
                    }
                    if (dn.getAssignedUserId() != parent.getAssignedUserId() && dn.getAssignedUserId() != 0) {
                        parent.setAssignedUserId(dn.getAssignedUserId());
                        dndao.updateAssignedUser(parent);
                        if(!parent.isActive()) {
                            logger.info("Failed to update assigned user ID for the parent dn ID= " + parentId + ". ");
                        }
                    }
                }
            }
            
            dn = (DiscrepancyNoteBean)dndao.create(dn);
            boolean success = dn.getId()>0 ? true : false;
            if(success) {
                dndao.createMapping(dn);
                success = dndao.isQuerySuccessful();
                if(success == false) {
                    mess.add(restext.getString("failed_create_dn_mapping_for_dnId") + dn.getId()+". ");
                } 
                noteTree.addNote(field, dn);
                noteTree.addIdNote(dn.getEntityId(), field);
                session.setAttribute(FORM_DISCREPANCY_NOTES_NAME, noteTree);
                if (dn.getParentDnId() == 0) {
                    // see issue 2659 this is a new thread, we will create
                    // two notes in this case,
                    // This way one can be the parent that updates as the
                    // status changes, but one also stays as New.
                    dn.setParentDnId(dn.getId());
                    dn = (DiscrepancyNoteBean) dndao.create(dn);
                    if(dn.getId()>0) {
                        dndao.createMapping(dn);
                        if(!dndao.isQuerySuccessful()) {
                            mess.add(restext.getString("failed_create_dn_mapping_for_dnId")+dn.getId()+". ");
                        }
                        noteTree.addNote(field, dn);
                        noteTree.addIdNote(dn.getEntityId(), field);
                        session.setAttribute(FORM_DISCREPANCY_NOTES_NAME, noteTree);
                    } else {
                        mess.add(restext.getString("failed_create_child_dn_for_new_parent_dnId")+dn.getId()+". ");
                    }
                }
            }else {
                mess.add(restext.getString("failed_create_new_dn")+". ");
            }

            if(success) {
                if (boxDNMap.size() > 0 && boxDNMap.containsKey(parentId)) {
                    boxDNMap.remove(parentId);
                }
                session.removeAttribute(BOX_TO_SHOW);
                if(parentId == dn.getParentDnId()) {
                    mess.add(restext.getString("a_new_child_dn_added")+". ");
                    results.put("newChildAdded" + parentId, mess);
                } else {
                    mess.add(restext.getString("a_new_dn_thread_added")+". ");
                    results.put("newChildAdded" + dn.getParentDnId(), mess);
                }
                setInputMessages(results);
                String close = fp.getString("close"+parentId);
                session.setAttribute(CLOSE_WINDOW, "true".equals(close)?"true":"");
                /*
                 * Copied from CreateDiscrepancyNoteServlet
                 * Setting a marker to check
                 * later while saving administrative edited data. This is needed to
                 * make sure the system flags error while changing data for items
                 * which already has a DiscrepanyNote
                 */
                manageReasonForChangeState(session, entityId);
                String email = fp.getString(EMAIL_USER_ACCOUNT+parentId);
                if (dn.getAssignedUserId() > 0 && "1".equals(email.trim()) && DiscrepancyNoteType.QUERY.getId() == dn.getDiscrepancyNoteTypeId()) {
                    logger.info("++++++ found our way here");
                    // generate email for user here
                    StringBuffer message = new StringBuffer();
                    int sectionId = fp.getInt("sectionId");
                    String groupLabel = fp.getString("groupLabel");
                    // generate message here
                    EmailEngine em = new EmailEngine(EmailEngine.getSMTPHost());
                    UserAccountDAO userAccountDAO = new UserAccountDAO(sm.getDataSource());
                    ItemDAO itemDAO = new ItemDAO(sm.getDataSource());
                    ItemDataDAO iddao = new ItemDataDAO(sm.getDataSource());
                    ItemDataBean itemData = (ItemDataBean) iddao.findByPK(dn.getEntityId());
                    StudyDAO studyDAO = new StudyDAO(sm.getDataSource());
                    UserAccountBean assignedUser = (UserAccountBean) userAccountDAO.findByPK(dn.getAssignedUserId());
                    String alertEmail = assignedUser.getEmail();
                    message.append(MessageFormat.format(respage.getString("mailDNHeader"), assignedUser.getFirstName(), assignedUser.getLastName(), dn
                            .getDescription()));
                    StudyBean study = (StudyBean) studyDAO.findByPK(dn.getStudyId());
                    message.append(MessageFormat.format(respage.getString("mailDNParameters1"), study.getName(), dn.getSubjectName(), dn.getCrfName(), dn
                            .getEventName()));
                    // plus
                    // repeating
                    // number
                    // ?
                    SectionDAO sectionDAO = new SectionDAO(sm.getDataSource());
                    SectionBean section = (SectionBean) sectionDAO.findByPK(sectionId);
                    message.append(MessageFormat.format(respage.getString("mailDNParameters2"), section.getName(), groupLabel));
                    // plus
                    // repeating
                    // number?
                    ItemBean item = (ItemBean) itemDAO.findByPK(itemData.getItemId());
                    message.append(MessageFormat.format(respage.getString("mailDNParameters3"), item.getName(), dn.getDescription(), dn.getDetailedNotes()));
    
                    /*
                     * Please select the link below to view the information
                     * provided. You may need to login to OpenClinica_testbed with
                     * your user name and password after selecting the link. If you
                     * receive a page cannot be displayed message, please make sure
                     * to select the Change Study/Site link in the upper right table
                     * of the page, select the study referenced above, and select
                     * the link again.
                     * 
                     * https://openclinica.s-3.com/OpenClinica_testbed/
                     * ViewSectionDataEntry ?ecId=117&sectionId=142&tabId=2
                     */
                    message.append("<P>" + respage.getString("please_select_the_link_below_dn") + "</P>");
                    message.append("<A HREF='" + SQLInitServlet.getField("sysURL.base") + "'>" + SQLInitServlet.getField("sysURL.base") + "</A><BR/>");
                    message.append("<P>" + respage.getString("mailDNThanks") + "</P>");
                    message.append("<P><P>" + respage.getString("email_footer"));
                    String emailBodyString = message.toString();
                    sendEmail(alertEmail.trim(), EmailEngine.getAdminEmail(), MessageFormat.format(respage.getString("mailDNSubject"), dn.getDescription()),
                            emailBodyString, true, null, null, true);
                }
            } else {
                session.setAttribute(BOX_TO_SHOW, parentId+"");
            }

        } else {
            setInputMessages(errors);
            boxDNMap.put(Integer.valueOf(parentId), dn);
            session.setAttribute(BOX_TO_SHOW, parentId+"");
        }
        session.setAttribute(BOX_DN_MAP, boxDNMap);
        forwardPage(Page.setNewPage(viewNoteLink+"&fromBox=1", Page.VIEW_DISCREPANCY_NOTE.getTitle()));
    }



    private void updateStudySubjectStatus(String entityType, int entityId) {
        if ("itemData".equalsIgnoreCase(entityType)) {
            int itemDataId = entityId;
            ItemDataDAO iddao = new ItemDataDAO(sm.getDataSource());
            ItemDataBean itemData = (ItemDataBean) iddao.findByPK(itemDataId);
            EventCRFDAO ecdao = new EventCRFDAO(sm.getDataSource());
            StudyEventDAO svdao = new StudyEventDAO(sm.getDataSource());
            StudySubjectDAO studySubjectDAO = new StudySubjectDAO(sm.getDataSource());
            EventCRFBean ec = (EventCRFBean) ecdao.findByPK(itemData.getEventCRFId());
            StudyEventBean event = (StudyEventBean) svdao.findByPK(ec.getStudyEventId());
            StudySubjectBean studySubject = (StudySubjectBean) studySubjectDAO.findByPK(event.getStudySubjectId());
            if (studySubject.getStatus() != null && studySubject.getStatus().equals(Status.SIGNED)) {
                studySubject.setStatus(Status.AVAILABLE);
                studySubject.setUpdater(ub);
                studySubject.setUpdatedDate(new Date());
                studySubjectDAO.update(studySubject);
            }
            if (ec.isSdvStatus()) {
                studySubject.setStatus(Status.AVAILABLE);
                studySubject.setUpdater(ub);
                studySubject.setUpdatedDate(new Date());
                studySubjectDAO.update(studySubject);
                ec.setSdvStatus(false);
                ecdao.update(ec);
            }

        }
    }

    private void updateStudyEvent(String entityType, int entityId) {
        if ("itemData".equalsIgnoreCase(entityType)) {
            int itemDataId = entityId;
            ItemDataDAO iddao = new ItemDataDAO(sm.getDataSource());
            ItemDataBean itemData = (ItemDataBean) iddao.findByPK(itemDataId);
            EventCRFDAO ecdao = new EventCRFDAO(sm.getDataSource());
            StudyEventDAO svdao = new StudyEventDAO(sm.getDataSource());
            EventCRFBean ec = (EventCRFBean) ecdao.findByPK(itemData.getEventCRFId());
            StudyEventBean event = (StudyEventBean) svdao.findByPK(ec.getStudyEventId());
            if (event.getSubjectEventStatus().equals(SubjectEventStatus.SIGNED)) {
                event.setSubjectEventStatus(SubjectEventStatus.COMPLETED);
                event.setUpdater(ub);
                event.setUpdatedDate(new Date());
                svdao.update(event);
            }
        } else if ("eventCrf".equalsIgnoreCase(entityType)) {
            int eventCRFId = entityId;
            EventCRFDAO ecdao = new EventCRFDAO(sm.getDataSource());
            StudyEventDAO svdao = new StudyEventDAO(sm.getDataSource());

            EventCRFBean ec = (EventCRFBean) ecdao.findByPK(eventCRFId);
            StudyEventBean event = (StudyEventBean) svdao.findByPK(ec.getStudyEventId());
            if (event.getSubjectEventStatus().equals(SubjectEventStatus.SIGNED)) {
                event.setSubjectEventStatus(SubjectEventStatus.COMPLETED);
                event.setUpdater(ub);
                event.setUpdatedDate(new Date());
                svdao.update(event);
            }
        }
    }

    private void manageReasonForChangeState(HttpSession session, Integer itemDataBeanId) {
        HashMap<Integer, Boolean> noteSubmitted = (HashMap<Integer, Boolean>) session.getAttribute(DataEntryServlet.NOTE_SUBMITTED);
        if (noteSubmitted == null) {
            noteSubmitted = new HashMap<Integer, Boolean>();
        }
        noteSubmitted.put(itemDataBeanId, Boolean.TRUE);
        session.setAttribute(DataEntryServlet.NOTE_SUBMITTED, noteSubmitted);
    }

}
