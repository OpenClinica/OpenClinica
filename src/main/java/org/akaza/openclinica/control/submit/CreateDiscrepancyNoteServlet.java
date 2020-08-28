/*
 * OpenClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: http://www.openclinica.org/license
 * copyright 2003-2012 Akaza Research
 */
package org.akaza.openclinica.control.submit;

import java.text.DateFormat;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import javax.servlet.http.HttpSession;

import core.org.akaza.openclinica.bean.admin.CRFBean;
import core.org.akaza.openclinica.bean.core.DiscrepancyNoteType;
import core.org.akaza.openclinica.bean.core.ResolutionStatus;
import core.org.akaza.openclinica.bean.core.Role;
import core.org.akaza.openclinica.bean.core.Status;
import core.org.akaza.openclinica.bean.core.SubjectEventStatus;
import core.org.akaza.openclinica.bean.login.UserAccountBean;
import core.org.akaza.openclinica.bean.managestudy.DiscrepancyNoteBean;
import core.org.akaza.openclinica.bean.managestudy.StudyEventBean;
import core.org.akaza.openclinica.bean.managestudy.StudyEventDefinitionBean;
import core.org.akaza.openclinica.bean.managestudy.StudySubjectBean;
import core.org.akaza.openclinica.bean.submit.CRFVersionBean;
import core.org.akaza.openclinica.bean.submit.EventCRFBean;
import core.org.akaza.openclinica.bean.submit.ItemBean;
import core.org.akaza.openclinica.bean.submit.ItemDataBean;
import core.org.akaza.openclinica.bean.submit.SectionBean;
import core.org.akaza.openclinica.bean.submit.SubjectBean;
import core.org.akaza.openclinica.dao.hibernate.StudyDao;
import core.org.akaza.openclinica.domain.datamap.Study;
import org.akaza.openclinica.control.core.SecureController;
import org.akaza.openclinica.control.form.FormDiscrepancyNotes;
import org.akaza.openclinica.control.form.FormProcessor;
import org.akaza.openclinica.control.form.Validator;
import core.org.akaza.openclinica.core.EmailEngine;
import core.org.akaza.openclinica.core.form.StringUtil;
import core.org.akaza.openclinica.dao.admin.CRFDAO;
import core.org.akaza.openclinica.dao.core.CoreResources;
import core.org.akaza.openclinica.dao.login.UserAccountDAO;
import core.org.akaza.openclinica.dao.hibernate.DiscrepancyNoteDao;
import core.org.akaza.openclinica.dao.managestudy.DiscrepancyNoteDAO;
import core.org.akaza.openclinica.dao.managestudy.StudyEventDAO;
import core.org.akaza.openclinica.dao.managestudy.StudyEventDefinitionDAO;
import core.org.akaza.openclinica.dao.managestudy.StudySubjectDAO;
import core.org.akaza.openclinica.dao.submit.CRFVersionDAO;
import core.org.akaza.openclinica.dao.submit.EventCRFDAO;
import core.org.akaza.openclinica.dao.submit.ItemDAO;
import core.org.akaza.openclinica.dao.submit.ItemDataDAO;
import core.org.akaza.openclinica.dao.submit.SectionDAO;
import core.org.akaza.openclinica.dao.submit.SubjectDAO;
import core.org.akaza.openclinica.i18n.core.LocaleResolver;
import org.akaza.openclinica.domain.enumsupport.SdvStatus;
import org.akaza.openclinica.view.Page;
import core.org.akaza.openclinica.web.InsufficientPermissionException;
import core.org.akaza.openclinica.web.SQLInitServlet;
import org.apache.commons.lang.StringUtils;

/**
 * Create a discrepancy note for a data entity
 *
 * @author jxu
 *
 */
public class CreateDiscrepancyNoteServlet extends SecureController {

    Locale locale;
    // < ResourceBundleresexception,respage;

    public static final String DIS_TYPES = "discrepancyTypes";

    public static final String RES_STATUSES = "resolutionStatuses";

    public static final String ENTITY_ID = "id";

    public static final String SUBJECT_ID = "subjectId";

    public static final String ITEM_ID = "itemId";


    public static final String IS_GROUP_ITEM = "isGroup";

    public static final String PARENT_ID = "parentId";// parent note id

    public static final String ENTITY_TYPE = "name";

    public static final String ENTITY_COLUMN = "column";

    public static final String ENTITY_FIELD = "field";


    public static final String DIS_NOTE = "discrepancyNote";

    public static final String WRITE_TO_DB = "writeToDB";

    public static final String IS_REASON_FOR_CHANGE = "isRfc";

    public static final String PRESET_RES_STATUS = "strResStatus";

    public static final String CAN_MONITOR = "canMonitor";

    public static final String NEW_NOTE = "new";

    public static final String RES_STATUS_ID = "resStatusId";

    public static final String ERROR_FLAG = "errorFlag";// use to determine
    // discrepany note type,
    // whether a note's item
    // has validation error
    // or not

    public static final String USER_ACCOUNTS = "userAccounts"; // use to provide
    // a list of user
    // accounts

    public static final String USER_ACCOUNT_ID = "strUserAccountId"; // use to
    // provide a
    // single user
    // id, to whom
    // the query
    // is
    // assigned,
    // tbh 02/2009

    public static final String SUBMITTED_USER_ACCOUNT_ID = "userAccountId";

    public static final String PRESET_USER_ACCOUNT_ID = "preUserAccountId";

    public static final String EMAIL_USER_ACCOUNT = "sendEmail";

    public static final String WHICH_RES_STATUSES = "whichResStatus";

    public static final String EVENT_CRF_ID = "eventCRFId";
    public static final String PARENT_ROW_COUNT = "rowCount";

    public String exceptionName = resexception.getString("no_permission_to_create_discrepancy_note");
    public String noAccessMessage = respage.getString("you_may_not_create_discrepancy_note") + respage.getString("change_study_contact_sysadmin");

    public static final String FLAG_DISCREPANCY_RFC ="flagDNRFC";

    public static final String NOTE_SUBMITTED = "note_submitted";

    /*
     * (non-Javadoc)
     *
     * @see org.akaza.openclinica.control.core.SecureController#mayProceed()
     */
    @Override
    protected void mayProceed() throws InsufficientPermissionException {
        checkStudyLocked(Page.MENU_SERVLET, respage.getString("current_study_locked"));
        locale = LocaleResolver.getLocale(request);

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
        List<DiscrepancyNoteType> types = DiscrepancyNoteType.list;

        request.setAttribute(DIS_TYPES, types);
        request.setAttribute(RES_STATUSES, ResolutionStatus.list);

        boolean writeToDB = fp.getBoolean(WRITE_TO_DB, true); //this should be set based on a new property of DisplayItemBean
        boolean isReasonForChange = fp.getBoolean(IS_REASON_FOR_CHANGE);
        int entityId = fp.getInt(ENTITY_ID);
        // subjectId has to be added to the database when disc notes area saved
        // as entity_type 'subject'
        int subjectId = fp.getInt(SUBJECT_ID);
        int itemId = fp.getInt(ITEM_ID);
        String entityType = fp.getString(ENTITY_TYPE);

        String field = fp.getString(ENTITY_FIELD);
        String column = fp.getString(ENTITY_COLUMN);
        int parentId = fp.getInt(PARENT_ID);


        //patch for repeating groups and RFC on empty fields

        int isGroup = fp.getInt(IS_GROUP_ITEM);
        //  request.setAttribute(IS_GROUP_ITEM, new Integer(isGroup));
        int eventCRFId = fp.getInt(EVENT_CRF_ID);
        request.setAttribute(EVENT_CRF_ID, new Integer(eventCRFId));
        int rowCount = fp.getInt(PARENT_ROW_COUNT);
        // run only once: try to recalculate writeToDB
        if (!StringUtils.isBlank(entityType) && "itemData".equalsIgnoreCase(entityType)
                && isGroup !=0 && eventCRFId != 0 ){
            //  request.setAttribute(PARENT_ROW_COUNT, new Integer(eventCRFId));
            int ordinal_for_repeating_group_field = calculateOrdinal( isGroup, field,eventCRFId,rowCount);
            int writeToDBStatus = isWriteToDB( isGroup,  field,  entityId,itemId,  ordinal_for_repeating_group_field, eventCRFId);
            writeToDB = (writeToDBStatus == -1)? false : (( writeToDBStatus==1)? true:writeToDB);
        }
        boolean isInError = fp.getBoolean(ERROR_FLAG);

        boolean isNew = fp.getBoolean(NEW_NOTE);
        request.setAttribute(NEW_NOTE, isNew ? "1" : "0");

        String strResStatus = fp.getString(PRESET_RES_STATUS);
        if (!strResStatus.equals("")) {
            request.setAttribute(PRESET_RES_STATUS, strResStatus);
        }

        String monitor = fp.getString("monitor");
        String enterData = fp.getString("enterData");
        request.setAttribute("enterData", enterData);

        boolean enteringData = false;
        if (enterData != null && "1".equalsIgnoreCase(enterData)) {
            // variables are not set in JSP, so not from viewing data and from
            // entering data
            request.setAttribute(CAN_MONITOR, "1");
            request.setAttribute("monitor", monitor);

            enteringData = true;
        } else if ("1".equalsIgnoreCase(monitor)) {// change to allow user to
            // enter note for all items,
            // not just blank items

            request.setAttribute(CAN_MONITOR, "1");
            request.setAttribute("monitor", monitor);

        } else {
            request.setAttribute(CAN_MONITOR, "0");

        }

        if ("itemData".equalsIgnoreCase(entityType) && enteringData) {
            request.setAttribute("enterItemData", "yes");
        }

        DateFormat dateFormatter = DateFormat.getDateInstance(DateFormat.DEFAULT, locale);
        int preUserId = 0;
        if (!StringUtils.isBlank(entityType)) {
            if ("itemData".equalsIgnoreCase(entityType)||"itemdata".equalsIgnoreCase(entityType)) {
                ItemBean item = (ItemBean) new ItemDAO(sm.getDataSource()).findByPK(itemId);
                ItemDataBean itemData = (ItemDataBean) new ItemDataDAO(sm.getDataSource()).findByPK(entityId);
                request.setAttribute("entityValue", itemData.getValue());
                request.setAttribute("entityName", item.getName());
                EventCRFDAO ecdao = new EventCRFDAO(sm.getDataSource());
                EventCRFBean ec = (EventCRFBean) ecdao.findByPK(itemData.getEventCRFId());
                preUserId = ec.getOwnerId();
            } else if ("studySub".equalsIgnoreCase(entityType)) {
                StudySubjectBean ssub = (StudySubjectBean) new StudySubjectDAO(sm.getDataSource()).findByPK(entityId);
                SubjectBean sub = (SubjectBean)new SubjectDAO(sm.getDataSource()).findByPK(ssub.getSubjectId());
                preUserId = ssub.getOwnerId();
                if (!StringUtils.isBlank(column)) {
                    if ("enrollment_date".equalsIgnoreCase(column)) {
                        if (ssub.getEnrollmentDate() != null) {
                            request.setAttribute("entityValue", dateFormatter.format(ssub.getEnrollmentDate()));
                        } else {
                            request.setAttribute("entityValue", resword.getString("N/A"));
                        }
                        request.setAttribute("entityName", resword.getString("enrollment_date"));
                    } else if ("gender".equalsIgnoreCase(column)) {
                        request.setAttribute("entityValue", sub.getGender() + "");
                        request.setAttribute("entityName", resword.getString("gender"));
                    } else if ("date_of_birth".equalsIgnoreCase(column)) {
                        if (sub.getDateOfBirth() != null) {
                            request.setAttribute("entityValue", dateFormatter.format(sub.getDateOfBirth()));
                        } else {
                            request.setAttribute("entityValue", resword.getString("N/A"));
                        }
                        request.setAttribute("entityName", resword.getString("date_of_birth"));
                    } else if ("unique_identifier".equalsIgnoreCase(column)) {
                        if (sub.getUniqueIdentifier() != null) {
                            request.setAttribute("entityValue", sub.getUniqueIdentifier());
                        }
                        request.setAttribute("entityName", resword.getString("unique_identifier"));
                    }
                }
            } else if ("subject".equalsIgnoreCase(entityType)) {
                SubjectBean sub = (SubjectBean) new SubjectDAO(sm.getDataSource()).findByPK(entityId);
                preUserId = sub.getOwnerId();
                if (!StringUtils.isBlank(column)) {
                    if ("gender".equalsIgnoreCase(column)) {
                        request.setAttribute("entityValue", sub.getGender() + "");
                        request.setAttribute("entityName", resword.getString("gender"));
                    } else if ("date_of_birth".equalsIgnoreCase(column)) {
                        if (sub.getDateOfBirth() != null) {
                            request.setAttribute("entityValue", dateFormatter.format(sub.getDateOfBirth()));
                        }
                        request.setAttribute("entityName", resword.getString("date_of_birth"));
                    } else if ("unique_identifier".equalsIgnoreCase(column)) {
                        request.setAttribute("entityValue", sub.getUniqueIdentifier());
                        request.setAttribute("entityName", resword.getString("unique_identifier"));
                    }
                }
            } else if ("studyEvent".equalsIgnoreCase(entityType)) {
                StudyEventBean se = (StudyEventBean)new StudyEventDAO(sm.getDataSource()).findByPK(entityId);
                preUserId = se.getOwnerId();
                if (!StringUtils.isBlank(column)) {
                    if ("location".equalsIgnoreCase(column)) {
                        request.setAttribute("entityValue", se.getLocation().equals("") || se.getLocation() == null ? resword.getString("N/A") : se.getLocation());
                        request.setAttribute("entityName", resword.getString("location"));
                    } else if ("start_date".equalsIgnoreCase(column)) {
                        if (se.getDateStarted() != null) {
                            request.setAttribute("entityValue", dateFormatter.format(se.getDateStarted()));
                        } else {
                            request.setAttribute("entityValue", resword.getString("N/A"));
                        }
                        request.setAttribute("entityName", resword.getString("start_date"));
                    } else if ("end_date".equalsIgnoreCase(column)) {
                        if (se.getDateEnded() != null) {
                            request.setAttribute("entityValue", dateFormatter.format(se.getDateEnded()));
                        } else {
                            request.setAttribute("entityValue", resword.getString("N/A"));
                        }
                        request.setAttribute("entityName", resword.getString("end_date"));
                    }
                }
            } else if ("eventCrf".equalsIgnoreCase(entityType)) {
                EventCRFBean ec = (EventCRFBean) new EventCRFDAO(sm.getDataSource()).findByPK(entityId);
                preUserId = ec.getOwnerId();
                if (!StringUtils.isBlank(column)) {
                    if ("date_interviewed".equals(column)) {
                        if (ec.getDateInterviewed() != null) {
                            request.setAttribute("entityValue", dateFormatter.format(ec.getDateInterviewed()));
                        } else {
                            request.setAttribute("entityValue", resword.getString("N/A"));
                        }
                        request.setAttribute("entityName", resword.getString("date_interviewed"));
                    } else if ("interviewer_name".equals(column)) {
                        request.setAttribute("entityValue", ec.getInterviewerName());
                        request.setAttribute("entityName", resword.getString("interviewer_name"));
                    }
                }
            }

        }

        // finds all the related notes
        ArrayList notes = (ArrayList) dndao.findAllByEntityAndColumn(entityType, entityId, column);

        DiscrepancyNoteBean parent = new DiscrepancyNoteBean();
        if (parentId > 0) {
            dndao.setFetchMapping(true);
            parent = (DiscrepancyNoteBean) dndao.findByPK(parentId);
            if (parent.isActive()) {
                request.setAttribute("parent", parent);
            }
            dndao.setFetchMapping(false);
        }
        FormDiscrepancyNotes newNotes = (FormDiscrepancyNotes) session.getAttribute(AddNewSubjectServlet.FORM_DISCREPANCY_NOTES_NAME);

        if (newNotes == null) {
            newNotes = new FormDiscrepancyNotes();
        }
        boolean isNotesExistInSession = (!newNotes.getNotes(field).isEmpty())? true : (!newNotes.getNotes(eventCRFId+"_"+field).isEmpty())? true:false;
        if (!notes.isEmpty() || isNotesExistInSession) {
            request.setAttribute("hasNotes", "yes");
        } else {
            request.setAttribute("hasNotes", "no");
            logger.debug("has notes:" + "no");
        }

        if(currentRole.getRole().equals(Role.MONITOR)){
            ArrayList<ResolutionStatus> resStatuses = new ArrayList();
            resStatuses.add(ResolutionStatus.OPEN);
            resStatuses.add(ResolutionStatus.UPDATED);
            resStatuses.add(ResolutionStatus.CLOSED);
            request.setAttribute(RES_STATUSES, resStatuses);
            request.setAttribute(WHICH_RES_STATUSES, "1");
            ArrayList<DiscrepancyNoteType> types2 = new ArrayList<DiscrepancyNoteType>();
            types2.add(DiscrepancyNoteType.QUERY);
            request.setAttribute(DIS_TYPES, types2);
        } else {//Role.STUDYDIRECTOR Role.COORDINATOR
            List<ResolutionStatus> resStatuses = new ArrayList<ResolutionStatus>(ResolutionStatus.list);
            resStatuses.remove(ResolutionStatus.NOT_APPLICABLE);
            request.setAttribute(RES_STATUSES, resStatuses); ;
            request.setAttribute(WHICH_RES_STATUSES, "2");
        }

        if (!fp.isSubmitted()) {
            DiscrepancyNoteBean dnb = new DiscrepancyNoteBean();

            if (subjectId > 0) {
                // BWP: this doesn't seem correct, because the SubjectId should
                // be the id for
                // the SubjectBean, different from StudySubjectBean
                StudySubjectDAO ssdao = new StudySubjectDAO(sm.getDataSource());
                StudySubjectBean ssub = (StudySubjectBean) ssdao.findByPK(subjectId);
                dnb.setSubjectName(ssub.getName());
                dnb.setSubjectId(ssub.getId());
                dnb.setStudySub(ssub);
                int parentStudyForSubject = 0;
                Study studyBeanSub = (Study) getStudyDao().findByPK(ssub.getStudyId());
                if (null != studyBeanSub) {
                    parentStudyForSubject = studyBeanSub.checkAndGetParentStudyId();
                }
                if (ssub.getStudyId() != currentStudy.getStudyId() && currentStudy.getStudyId() != parentStudyForSubject) {
                    addPageMessage(noAccessMessage);
                    throw new InsufficientPermissionException(Page.MENU_SERVLET, exceptionName, "1");
                }

            }
            if (itemId > 0) {
                ItemBean item = (ItemBean) new ItemDAO(sm.getDataSource()).findByPK(itemId);
                dnb.setEntityName(item.getName());
                request.setAttribute("item", item);
            }
            dnb.setEntityType(entityType);
            dnb.setColumn(column);
            dnb.setEntityId(entityId);
            dnb.setField(field);
            dnb.setParentDnId(parent.getId());
            dnb.setCreatedDate(new Date());

            // When a user is performing Data Entry, Initial Data Entry or
            // Double Data Entry and
            // have not received any validation warnings or messages for a
            // particular item,
            // they will see Annotation as the default type in the Add
            // Discrepancy note window.

            // When a user is performing Data Entry, Initial Data Entry or
            // Double Data Entry and they
            // have received a validation warning or message for a particular
            // item,
            // they can click on the flag and the default type should be Failed
            // Validation Check

            // When a user is viewing a CRF and they click on the flag icon, the
            // default type should be query.

            // when the type is query, we should also get the user id for the
            // person who completed data entry

            /* Mantis issue: tbh 08/31/2009
             * 0004092: CRCs and Investigators allowed to close a JsonQuery
                CRCs and Investigators are allowed to choose Closed for a JsonQuery. they are also allowed to choose New.

                They should only be allowed to choose Updated or Resolution Proposed.
             */

            // above extra business rule here, tbh

            if (parent.getId() == 0 || isNew) {// no parent, new note thread
                if (enteringData) {
                    if (isInError) {
                        dnb.setDiscrepancyNoteTypeId(DiscrepancyNoteType.FAILEDVAL.getId());
                    } else {
                        dnb.setDiscrepancyNoteTypeId(DiscrepancyNoteType.ANNOTATION.getId());
                        dnb.setResolutionStatusId(ResolutionStatus.NOT_APPLICABLE.getId());
                        // >> tbh WHO bug: set an assigned user for the parent
                        // note
                        // dnb.setAssignedUser(ub);
                        // dnb.setAssignedUserId(ub.getId());
                        // << tbh 08/2009
                    }
                    if (isReasonForChange) {
                        dnb.setDiscrepancyNoteTypeId(DiscrepancyNoteType.REASON_FOR_CHANGE.getId());
                        dnb.setResolutionStatusId(ResolutionStatus.NOT_APPLICABLE.getId());
                    }
                    // << tbh 02/2010, trumps failed evaluation error checks
                    // can we put this in admin editing
                    request.setAttribute("autoView", "0");
                    // above set to automatically open up the user panel
                } else {
                    // when the user is a CRC and is adding a note to the thread
                    // it should default to Resolution Proposed,
                    // and the assigned should be the user who logged the query,
                    // NOT the one who is proposing the solution, tbh 02/2009
                    // if (currentRole.getRole().equals(Role.COORDINATOR)) {
                    // dnb.setDiscrepancyNoteTypeId(DiscrepancyNoteType.
                    // REASON_FOR_CHANGE.getId());
                    // request.setAttribute("autoView", "1");
                    // // above set to automatically open up the user panel
                    // } else {
                    dnb.setDiscrepancyNoteTypeId(DiscrepancyNoteType.QUERY.getId());
                    // remove this option for CRCs and Investigators
                    //if (currentRole.getRole().equals(Role.RESEARCHASSISTANT) && currentStudy.getId() != currentStudy.getParentStudyId()

                    request.setAttribute("autoView", "1");
                    dnb.setAssignedUserId(preUserId);

                    // above set to automatically open up the user panel
                    // }
                }

            }

            else if (parent.getDiscrepancyNoteTypeId() > 0) {
                dnb.setDiscrepancyNoteTypeId(parent.getDiscrepancyNoteTypeId());

                // if it is a CRC then we should automatically propose a
                // solution, tbh

                // adding second rule here, tbh 08/2009
                if ((currentRole.getRole().equals(Role.RESEARCHASSISTANT) || currentRole.getRole().equals(Role.RESEARCHASSISTANT2)) && currentStudy.getStudyId() != currentStudy.checkAndGetParentStudyId()) {
                    dnb.setResolutionStatusId(ResolutionStatus.RESOLVED.getId());
                    request.setAttribute("autoView", "0");
                    // hide the panel, tbh
                } else {
                    dnb.setResolutionStatusId(ResolutionStatus.UPDATED.getId());
                }

            }
            dnb.setOwnerId(parent.getOwnerId());
            String detailedDes = fp.getString("strErrMsg");
            if (detailedDes != null) {
                dnb.setDetailedNotes(detailedDes);
                logger.debug("found strErrMsg: " + fp.getString("strErrMsg"));
            }
            // #4346 TBH 10/2009

            //If the data entry form has not been saved yet, collecting info from parent page.
            dnb = getNoteInfo(dnb);// populate note infos

            if (dnb.getEventName() == null || dnb.getEventName().equals("")) {
                if (!fp.getString("eventName").equals("")){
                    dnb.setEventName(fp.getString("eventName"));
                }else{
                    dnb.setEventName(getStudyEventDefinition(eventCRFId).getName());
                }
            }
            if (dnb.getEventStart() == null) {
                if (fp.getDate("eventDate")!=null){
                    dnb.setEventStart(fp.getDate("eventDate"));
                }else{
                    dnb.setEventStart(getStudyEvent(eventCRFId).getDateStarted());
                }

            }
            if (dnb.getCrfName() == null || dnb.getCrfName().equals("")) {
                if(!fp.getString("crfName").equals("")){
                    dnb.setCrfName(fp.getString("crfName"));
                }else{
                    dnb.setCrfName(getCrf(eventCRFId).getName());
                }

            }
            //            // #4346 TBH 10/2009
            request.setAttribute(DIS_NOTE, dnb);
            request.setAttribute("unlock", "0");
            request.setAttribute(WRITE_TO_DB, writeToDB ? "1" : "0");//this should go from UI & here
            ArrayList userAccounts = this.generateUserAccounts(ub.getActiveStudyId(), subjectId);
            request.setAttribute(USER_ACCOUNTS, userAccounts);

            // ideally should be only two cases
            if ((currentRole.getRole().equals(Role.RESEARCHASSISTANT) || currentRole.getRole().equals(Role.RESEARCHASSISTANT2)) && currentStudy.getStudyId() != currentStudy.checkAndGetParentStudyId()) {             // assigning back to OP, tbh
                request.setAttribute(USER_ACCOUNT_ID,  Integer.valueOf(parent.getOwnerId()).toString());
                logger.debug("assigned owner id: " + parent.getOwnerId());
            } else if (dnb.getEventCRFId() > 0) {
                logger.debug("found a event crf id: " + dnb.getEventCRFId());
                EventCRFDAO eventCrfDAO = new EventCRFDAO(sm.getDataSource());
                EventCRFBean eventCrfBean = new EventCRFBean();
                eventCrfBean = (EventCRFBean) eventCrfDAO.findByPK(dnb.getEventCRFId());
                request.setAttribute(USER_ACCOUNT_ID,   Integer.valueOf(eventCrfBean.getOwnerId()).toString());
                logger.debug("assigned owner id: " + eventCrfBean.getOwnerId());
            } else {
                // the end case

            }

            // set the user account id for the user who completed data entry
            forwardPage(Page.ADD_DISCREPANCY_NOTE);

        } else {
            FormDiscrepancyNotes noteTree = (FormDiscrepancyNotes) session.getAttribute(AddNewSubjectServlet.FORM_DISCREPANCY_NOTES_NAME);
            FormDiscrepancyNotes noteTree_RFC_REPEAT = (FormDiscrepancyNotes) session.getAttribute(FLAG_DISCREPANCY_RFC);;

            if(noteTree_RFC_REPEAT == null)
                noteTree_RFC_REPEAT = new FormDiscrepancyNotes();

            if (noteTree == null) {
                noteTree = new FormDiscrepancyNotes();
                logger.debug("No note tree initailized in session");
            }


            Validator v = new Validator(request);
            //String description = fp.getString("description");
            int typeId = fp.getInt("typeId");
            int assignedUserAccountId = fp.getInt(SUBMITTED_USER_ACCOUNT_ID);
            int resStatusId = fp.getInt(RES_STATUS_ID);
            String detailedDes = fp.getString("detailedDes");
            int sectionId = fp.getInt("sectionId");
            DiscrepancyNoteBean note = new DiscrepancyNoteBean();
            v.addValidation("detailedDes", Validator.NO_BLANKS);
            // v.addValidation("description", Validator.LENGTH_NUMERIC_COMPARISON, NumericComparisonOperator.LESS_THAN_OR_EQUAL_TO, 255);
            // v.addValidation("detailedDes", Validator.LENGTH_NUMERIC_COMPARISON, NumericComparisonOperator.LESS_THAN_OR_EQUAL_TO, 1000);

            v.addValidation("typeId", Validator.NO_BLANKS);

            HashMap errors = v.validate();
            //note.setDescription(description);
            note.setDetailedNotes(detailedDes);
            note.setOwner(ub);
            note.setOwnerId(ub.getId());
            note.setCreatedDate(new Date());
            note.setResolutionStatusId(resStatusId);
            note.setDiscrepancyNoteTypeId(typeId);
            note.setParentDnId(parent.getId());

            if (typeId != DiscrepancyNoteType.ANNOTATION.getId() && typeId != DiscrepancyNoteType.FAILEDVAL.getId()
                    && typeId != DiscrepancyNoteType.REASON_FOR_CHANGE.getId()) {
                if (assignedUserAccountId > 0) {
                    note.setAssignedUserId(assignedUserAccountId);
                    logger.debug("^^^ found assigned user id: " + assignedUserAccountId);

                } else {
                    // a little bit of a workaround, should ideally be always from
                    // the form
                    note.setAssignedUserId(parent.getOwnerId());
                    logger.debug("found user assigned id, in the PARENT OWNER ID: " + parent.getOwnerId() + " note that user assgined id did not work: "
                            + assignedUserAccountId);
                }
            }

            note.setField(field);
            if (DiscrepancyNoteType.ANNOTATION.getId() == note.getDiscrepancyNoteTypeId()) {
                updateStudyEvent(entityType, entityId);
                updateStudySubjectStatus(entityType, entityId);
            }
            if (DiscrepancyNoteType.ANNOTATION.getId() == note.getDiscrepancyNoteTypeId()
                    || DiscrepancyNoteType.REASON_FOR_CHANGE.getId() == note.getDiscrepancyNoteTypeId()) {
                note.setResStatus(ResolutionStatus.NOT_APPLICABLE);
                note.setResolutionStatusId(ResolutionStatus.NOT_APPLICABLE.getId());
            }
            if (DiscrepancyNoteType.FAILEDVAL.getId() == note.getDiscrepancyNoteTypeId()
                    || DiscrepancyNoteType.QUERY.getId() == note.getDiscrepancyNoteTypeId()) {
                if (ResolutionStatus.NOT_APPLICABLE.getId() == note.getResolutionStatusId()) {
                    Validator.addError(errors, RES_STATUS_ID, restext.getString("not_valid_res_status"));
                }
            }

            if (!parent.isActive()) {
                note.setEntityId(entityId);
                note.setEntityType(entityType);
                note.setColumn(column);
            } else {
                note.setEntityId(parent.getEntityId());
                note.setEntityType(parent.getEntityType());
                if (!StringUtils.isBlank(parent.getColumn())) {
                    note.setColumn(parent.getColumn());
                } else {
                    note.setColumn(column);
                }
                note.setParentDnId(parent.getId());
            }

            note.setStudyId(currentStudy.getStudyId());

            note = getNoteInfo(note);// populate note infos

            request.setAttribute(DIS_NOTE, note);
            request.setAttribute(WRITE_TO_DB, writeToDB ? "1" : "0");//this should go from UI & here
            ArrayList userAccounts = this.generateUserAccounts(ub.getActiveStudyId(), subjectId);

            request.setAttribute(USER_ACCOUNT_ID,   Integer.valueOf(note.getAssignedUserId()).toString());
            // formality more than anything else, we should go to say the note
            // is done

            Role r = currentRole.getRole();
            if (r.equals(Role.MONITOR) || r.equals(Role.INVESTIGATOR) || r.equals(Role.RESEARCHASSISTANT) || r.equals(Role.RESEARCHASSISTANT2) || r.equals(Role.COORDINATOR)) { // investigator
                request.setAttribute("unlock", "1");
                logger.debug("set UNLOCK to ONE");
            } else {
                request.setAttribute("unlock", "0");
                logger.debug("set UNLOCK to ZERO");
            }

            request.setAttribute(USER_ACCOUNTS, userAccounts);

            if (errors.isEmpty()) {

                if (!writeToDB) {
                    noteTree.addNote(field, note);
                    noteTree.addIdNote(note.getEntityId(), field);
                    noteTree_RFC_REPEAT.addNote(EVENT_CRF_ID+"_"+field, note);
                    noteTree_RFC_REPEAT.addIdNote(note.getEntityId(), field);

                    //-> catcher                //   FORM_DISCREPANCY_NOTES_NAME
                    session.setAttribute(AddNewSubjectServlet.FORM_DISCREPANCY_NOTES_NAME, noteTree);
                    session.setAttribute(FLAG_DISCREPANCY_RFC, noteTree_RFC_REPEAT);

                    //
                    /*Setting a marker to check later while saving administrative edited data. This is needed to make
                    * sure the system flags error while changing data for items which already has a DiscrepanyNote*/
                    manageReasonForChangeState(session, eventCRFId+"_"+field);
                    forwardPage(Page.ADD_DISCREPANCY_NOTE_DONE);
                } else {
                    // if not creating a new thread(note), update exsiting notes
                    // if necessary
                    //if ("itemData".equalsIgnoreCase(entityType) && !isNew) {
                    int pdnId = note!=null?note.getParentDnId():0;
                    if(pdnId > 0) {
                        logger.debug("Create:find parent note for item data:" + note.getEntityId());

                        DiscrepancyNoteBean pNote = (DiscrepancyNoteBean) dndao.findByPK(pdnId);

                        logger.debug("setting DN owner id: " + pNote.getOwnerId());

                        note.setOwnerId(pNote.getOwnerId());

                        if (note.getDiscrepancyNoteTypeId() == pNote.getDiscrepancyNoteTypeId()) {

                            if (note.getResolutionStatusId() != pNote.getResolutionStatusId()) {
                                pNote.setResolutionStatusId(note.getResolutionStatusId());
                                dndao.update(pNote);
                            }

                            if (note.getAssignedUserId() != pNote.getAssignedUserId()) {
                                pNote.setAssignedUserId(note.getAssignedUserId());
                                if(pNote.getAssignedUserId()>0) {
                                    dndao.updateAssignedUser(pNote);
                                } else {
                                    dndao.updateAssignedUserToNull(pNote);
                                }
                            }

                        }

                    }
                    String entityName ="";
                    if (!StringUtils.isBlank(note.getEntityType()) && !"itemData".equalsIgnoreCase(note.getEntityType())
                            && !StringUtils.isBlank((String)request.getAttribute("entityName"))) {
                        entityName = (String)request.getAttribute("entityName");
                    } else {
                        entityName=note.getEntityName();
                    }

                    note = (DiscrepancyNoteBean) dndao.create(note);
                    dndao.createMapping(note);

                    request.setAttribute(DIS_NOTE, note);

                    if (note.getParentDnId() == 0) {
                        // see issue 2659 this is a new thread, we will create
                        // two notes in this case,
                        // This way one can be the parent that updates as the
                        // status changes, but one also stays as New.
                        note.setParentDnId(note.getId());
                        note = (DiscrepancyNoteBean) dndao.create(note);
                        dndao.createMapping(note);
                    }

                    /*Setting a marker to check later while saving administrative edited data. This is needed to make
                    * sure the system flags error while changing data for items which already has a DiscrepanyNote*/
                    //session.setAttribute(DataEntryServlet.NOTE_SUBMITTED, true);
                    //session.setAttribute(DataEntryServlet.NOTE_SUBMITTED, true);
                    // String field_id_for_RFC_hash = buildDiscrepancyNoteIdForRFCHash(eventCRFId,entityId, isGroup, field, ordinal_for_repeating_group_field);
                    String field_id_for_RFC_hash =  eventCRFId+"_"+field;

                    manageReasonForChangeState(session, field_id_for_RFC_hash);

                    logger.debug("found resolution status: " + note.getResolutionStatusId());

                    String email = fp.getString(EMAIL_USER_ACCOUNT);

                    logger.debug("found email: " + email);
                    if (note.getAssignedUserId() > 0 && "1".equals(email.trim()) && DiscrepancyNoteType.QUERY.getId() == note.getDiscrepancyNoteTypeId()) {

                        logger.debug("++++++ found our way here: " + note.getDiscrepancyNoteTypeId() + " id number and " + note.getDisType().getName());
                        // generate email for user here
                        StringBuffer message = new StringBuffer();

                        // generate message here
                        UserAccountDAO userAccountDAO = new UserAccountDAO(sm.getDataSource());
                        ItemDAO itemDAO = new ItemDAO(sm.getDataSource());
                        ItemDataDAO iddao = new ItemDataDAO(sm.getDataSource());
                        ItemBean item = new ItemBean();
                        ItemDataBean itemData = new ItemDataBean();
                        SectionBean section = new SectionBean();

                        UserAccountBean assignedUser = (UserAccountBean) userAccountDAO.findByPK(note.getAssignedUserId());
                        String alertEmail = assignedUser.getEmail();
                        message.append(MessageFormat.format(respage.getString("mailDNHeader"), assignedUser.getFirstName(),assignedUser.getLastName()));
                        message.append("<A HREF='" + SQLInitServlet.getField("sysURL.base")
                                + "ViewNotes?module=submit&listNotes_f_discrepancyNoteBean.user=" + assignedUser.getName()
                                + "&listNotes_f_entityName=" + note.getEntityName()
                                + "'>" + SQLInitServlet.getField("sysURL.base") + "</A><BR/>");
                        message.append(respage.getString("you_received_this_from"));
                        Study study = (Study) getStudyDao().findByPK(note.getStudyId());
                        SectionDAO sectionDAO = new SectionDAO(sm.getDataSource());

                        if ("itemData".equalsIgnoreCase(entityType)) {
                            itemData = (ItemDataBean) iddao.findByPK(note.getEntityId());
                            item = (ItemBean) itemDAO.findByPK(itemData.getItemId());
                            if (sectionId > 0) {
                                section = (SectionBean) sectionDAO.findByPK(sectionId);
                            } else {
                                //Todo section should be initialized when sectionId = 0
                            }
                        }

                        message.append(respage.getString("email_body_separator"));
                        message.append(respage.getString("disc_note_info"));
                        message.append(respage.getString("email_body_separator"));
                        DiscrepancyNoteBean parentDn= (DiscrepancyNoteBean) dndao.findByPK(note.getParentDnId());
                        message.append(
                                MessageFormat.format(respage.getString("mailDNParameters1"), String.valueOf(parentDn.getThreadNumber()),note.getDetailedNotes(), ub.getName()));
                        message.append(respage.getString("email_body_separator"));
                        message.append(respage.getString("entity_information"));
                        message.append(respage.getString("email_body_separator"));
                        message.append(MessageFormat.format(respage.getString("mailDNParameters2"), study.getName(), note.getSubjectName()));

                        if (!("studySub".equalsIgnoreCase(entityType)
                                || "subject".equalsIgnoreCase(entityType))) {
                            message.append(MessageFormat.format(respage.getString("mailDNParameters3"), note.getEventName()));
                            if (!"studyEvent".equalsIgnoreCase(note.getEntityType())) {
                                message.append(MessageFormat.format(respage.getString("mailDNParameters4"), note.getCrfName()));
                                if (!"eventCrf".equalsIgnoreCase(note.getEntityType())) {
                                    if (sectionId > 0) {
                                        message.append(MessageFormat.format(respage.getString("mailDNParameters5"), section.getName()));
                                    }
                                    message.append(MessageFormat.format(respage.getString("mailDNParameters6"), item.getName()));
                                }
                            }
                            else {
                                String description = getDiscrepancyNoteDao().findByPK(note.getId()).getDnStudyEventMaps().get(0).getDnStudyEventMapId().getColumnName();
                                description = description.equals("start_date") ? "Event Start Date" : "Event End Date";
                                message.append(MessageFormat.format(respage.getString("mailDNParameters7"), description));
                            }
                        }

                        message.append(respage.getString("email_body_separator"));
                        message.append(MessageFormat.format(respage.getString("mailDNThanks"), study.getName()));
                        message.append(respage.getString("email_body_separator"));
                        message.append(respage.getString("disclaimer"));
                        message.append(respage.getString("email_body_separator"));
                        message.append(respage.getString("email_footer"));

                        String emailBodyString = message.toString();
                        sendEmail(alertEmail.trim(), EmailEngine.getAdminEmail(), MessageFormat.format(respage.getString("mailDNSubject"),study.getName(), entityName), emailBodyString, true, null,
                                null, true);

                    } else {
                        logger.debug("did not send email, but did save DN");
                    }
                    // addPageMessage(
                    // "Your discrepancy note has been saved into database.");
                    addPageMessage(respage.getString("note_saved_into_db"));
                    addPageMessage(respage.getString("page_close_automatically"));
                    forwardPage(Page.ADD_DISCREPANCY_NOTE_SAVE_DONE);
                }

            } else {
                if(parentId>0) {
                    if(note.getResolutionStatusId() == ResolutionStatus.NOT_APPLICABLE.getId()) {
                        request.setAttribute("autoView", "0");
                    }
                }else {
                    if(note.getDiscrepancyNoteTypeId() == DiscrepancyNoteType.QUERY.getId()) {
                        request.setAttribute("autoView", "1");
                    } else {
                        request.setAttribute("autoView", "0");
                    }
                }
                setInputMessages(errors);
                forwardPage(Page.ADD_DISCREPANCY_NOTE);
            }

        }

    }

    /**
     * Constructs a url for creating new note on 'view note list' page
     *
     * @param note
     * @param preset
     * @return
     */
    public static String getAddChildURL(DiscrepancyNoteBean note, ResolutionStatus preset, boolean toView) {
        ArrayList<String> arguments = new ArrayList<String>();

        arguments.add(ENTITY_TYPE + "=" + note.getEntityType());
        arguments.add(ENTITY_ID + "=" + note.getEntityId());
        arguments.add(WRITE_TO_DB + "=" + "1");
        arguments.add("monitor" + "=" + 1);// of course, when resolving a note,
        // we have monitor privilege

        if (preset.isActive()) {
            arguments.add(PRESET_RES_STATUS + "=" + String.valueOf(preset.getId()));
        }

        if (toView) {
            // BWP 3/19/2009 3166: <<
            String columnValue = "".equalsIgnoreCase(note.getColumn()) ? "value" : note.getColumn();
            // >>
            arguments.add(ENTITY_COLUMN + "=" + columnValue);
            arguments.add(SUBJECT_ID + "=" + note.getSubjectId());
            arguments.add(ITEM_ID + "=" + note.getItemId());
            String queryString = StringUtil.join("&", arguments);
            return "ViewDiscrepancyNote?" + queryString;
        } else {
            arguments.add(PARENT_ID + "=" + note.getId());
            String queryString = StringUtil.join("&", arguments);
            return "CreateDiscrepancyNote?" + queryString;
        }
    }

    /**
     * Pulls the note related information from database according to note type
     *
     */

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
            if (ec.getSdvStatus() == SdvStatus.VERIFIED) {
                studySubject.setStatus(Status.AVAILABLE);
                studySubject.setUpdater(ub);
                studySubject.setUpdatedDate(new Date());
                studySubjectDAO.update(studySubject);
                ec.setSdvStatus(SdvStatus.CHANGED_SINCE_VERIFIED);
                ec.setSdvUpdateId(ub.getId());
                ecdao.update(ec);
            }

        }
    }

    private ArrayList generateUserAccounts(int studyId, int subjectId) {
        Study subjectStudy = getStudyDao().findByStudySubjectId(subjectId);
        String currentSchema = CoreResources.getRequestSchema(request);
        CoreResources.setRequestSchema(request, "public");

        UserAccountDAO userAccountDAO = new UserAccountDAO(sm.getDataSource());
        // study id, tbh 03/2009
        ArrayList userAccounts = new ArrayList();
        if (currentStudy.isSite()) {
            userAccounts = userAccountDAO.findAllUsersByStudyOrSite(studyId, currentStudy.getStudy().getStudyId(), subjectId);
        } else if (subjectStudy.isSite()) {
            CoreResources.setRequestSchema(request,currentSchema);
            Study publicSubjectStudy = getStudyDao().findByUniqueId(subjectStudy.getUniqueIdentifier());
            CoreResources.setRequestSchema(request, "public");
            userAccounts = userAccountDAO.findAllUsersByStudyOrSite(publicSubjectStudy.getStudyId(), publicSubjectStudy.getStudy().getStudyId(), subjectId);
        } else {
            userAccounts = userAccountDAO.findAllUsersByStudyOrSite(studyId, 0, subjectId);
        }
        CoreResources.setRequestSchema(request,currentSchema);


        return userAccounts;
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

    private void manageReasonForChangeState(HttpSession session, String itemDataBeanId) {
        @SuppressWarnings("unchecked")
        HashMap<String, Boolean> noteSubmitted = (HashMap<String, Boolean>) session.getAttribute(NOTE_SUBMITTED);
        if (noteSubmitted == null) {
            noteSubmitted = new HashMap<String, Boolean>();
        }
        noteSubmitted.put(itemDataBeanId, Boolean.TRUE);
        session.setAttribute(NOTE_SUBMITTED, noteSubmitted);
    }



    /*
     * @return 0 - not defined; 1 - yes, -1 - no
     *
     */
    private int isWriteToDB(int isGroup, String field, int item_data_id,
                            int item_id, int ordinal_for_repeating_group_field, int event_crf_id){
        if ( item_data_id > 0 && isGroup==-1){//non repeating group; coming from showItemInput.jsp
            return 1;
        }
        else if ( item_data_id < 0 && isGroup==-1){//non repeating group; coming from showItemInput.jsp
            return -1;
        }
        else if (  isGroup==1){// repeating group;
            //initial data entry or if template cell is empty (last row)
            if ( item_data_id < 0 ){        return -1;}
            if ( item_data_id > 0 ){
                // first row IG_TEST__GROUP1_6727_0input320
                // row without X     IG_TEST__GROUP1_6727_manual1input320
                // row with X           IG_TEST__GROUP1_6727_1input320
                if (field.contains("_0input") || field.contains("manual")){
                    //get ordinal
                    ItemDataDAO iddao = new ItemDataDAO(sm.getDataSource(), locale);

                    boolean isExistInDB = iddao.isItemExists(item_id,ordinal_for_repeating_group_field,event_crf_id);
                    return (isExistInDB)? 1: -1;
                }
                else if (field.contains("input")){
                    return -1;
                }
            }
        }
        return 0;
    }

    public int calculateOrdinal(int isGroup, String field_name, int event_crf_id, int rowCount){
        // first row IG_TEST__GROUP1_6727_0input320
        // row without X     IG_TEST__GROUP1_6727_manual1input320
        // row with X           IG_TEST__GROUP1_6727_1input320
        int ordinal = 0;
        int start = -1;int end=-1;
        if ( isGroup == -1){ return 1;}
        if (field_name.contains("_0input")) { return 1;}
        try{
            if ( field_name.contains("manual")){
                start =  field_name.indexOf("manual")+5;
                end =  field_name.indexOf("input");
                if ( start == 4 || end == -1){ return 0;}
                ordinal = Integer.valueOf(field_name.substring(start+1, end));
                return ordinal+1;

            }else{
                //get max ordinal from DB
                ItemDataDAO iddao = new ItemDataDAO(sm.getDataSource(), locale);
                //IG_TEST__GROUP1_6727_manual1input320
                String[] field_name_items=field_name.split("_");

                String group_oid = field_name.substring(0, field_name.indexOf(field_name_items[field_name_items.length-1])-1);
                int maxOrdinal = iddao.getMaxOrdinalForGroupByGroupOID(group_oid, event_crf_id);

                //get ordinal from field
                end =  field_name.indexOf("input");
                start =  field_name.lastIndexOf("_");
                if ( end == -1 || start ==-1){ return 0;}
                ordinal = Integer.valueOf(field_name.substring(start+1, end));
                return ordinal+maxOrdinal+rowCount;
            }
        }catch(NumberFormatException e){
            //DO NOTHING
        }


        return ordinal;

    }


    private StudyEventBean getStudyEvent(int eventCRFId) {
        StudyEventDAO sedao = new StudyEventDAO(sm.getDataSource());
        StudyEventBean event = (StudyEventBean) sedao.findByPK(getEventCrf(eventCRFId).getStudyEventId());
        return event;
    }

    private StudyEventDefinitionBean getStudyEventDefinition(int eventCRFId) {
        StudyEventDefinitionDAO seddao = new StudyEventDefinitionDAO(sm.getDataSource());
        StudyEventDefinitionBean sed = (StudyEventDefinitionBean) seddao.findByPK(getStudyEvent(eventCRFId).getStudyEventDefinitionId());
        return sed;
    }

    private CRFBean getCrf(int eventCRFId) {
        CRFVersionDAO cvdao = new CRFVersionDAO(sm.getDataSource());
        CRFDAO cdao = new CRFDAO(sm.getDataSource());
        CRFVersionBean cv = (CRFVersionBean) cvdao.findByPK(getEventCrf(eventCRFId).getCRFVersionId());
        CRFBean c = (CRFBean) cdao.findByPK(cv.getCrfId());
        return c;
    }

    private EventCRFBean getEventCrf(int eventCRFId) {
        EventCRFDAO ecdao = new EventCRFDAO(sm.getDataSource());
        EventCRFBean ec = (EventCRFBean) ecdao.findByPK(eventCRFId);
        return ec;
    }

}
