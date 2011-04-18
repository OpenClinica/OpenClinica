/*
 * OpenClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: http://www.openclinica.org/license
 * copyright 2003-2005 Akaza Research
 */
package org.akaza.openclinica.control.submit;

import org.akaza.openclinica.bean.admin.CRFBean;
import org.akaza.openclinica.bean.core.DiscrepancyNoteType;
import org.akaza.openclinica.bean.core.NumericComparisonOperator;
import org.akaza.openclinica.bean.core.ResolutionStatus;
import org.akaza.openclinica.bean.core.Role;
import org.akaza.openclinica.bean.core.Status;
import org.akaza.openclinica.bean.core.SubjectEventStatus;
import org.akaza.openclinica.bean.login.UserAccountBean;
import org.akaza.openclinica.bean.managestudy.DiscrepancyNoteBean;
import org.akaza.openclinica.bean.managestudy.StudyBean;
import org.akaza.openclinica.bean.managestudy.StudyEventBean;
import org.akaza.openclinica.bean.managestudy.StudyEventDefinitionBean;
import org.akaza.openclinica.bean.managestudy.StudySubjectBean;
import org.akaza.openclinica.bean.submit.EventCRFBean;
import org.akaza.openclinica.bean.submit.ItemBean;
import org.akaza.openclinica.bean.submit.ItemDataBean;
import org.akaza.openclinica.bean.submit.SectionBean;
import org.akaza.openclinica.bean.submit.SubjectBean;
import org.akaza.openclinica.control.core.SecureController;
import org.akaza.openclinica.control.form.FormDiscrepancyNotes;
import org.akaza.openclinica.control.form.FormProcessor;
import org.akaza.openclinica.control.form.Validator;
import org.akaza.openclinica.core.EmailEngine;
import org.akaza.openclinica.core.form.StringUtil;
import org.akaza.openclinica.dao.admin.CRFDAO;
import org.akaza.openclinica.dao.login.UserAccountDAO;
import org.akaza.openclinica.dao.managestudy.DiscrepancyNoteDAO;
import org.akaza.openclinica.dao.managestudy.StudyDAO;
import org.akaza.openclinica.dao.managestudy.StudyEventDAO;
import org.akaza.openclinica.dao.managestudy.StudyEventDefinitionDAO;
import org.akaza.openclinica.dao.managestudy.StudySubjectDAO;
import org.akaza.openclinica.dao.submit.EventCRFDAO;
import org.akaza.openclinica.dao.submit.ItemDAO;
import org.akaza.openclinica.dao.submit.ItemDataDAO;
import org.akaza.openclinica.dao.submit.SectionDAO;
import org.akaza.openclinica.dao.submit.SubjectDAO;
import org.akaza.openclinica.view.Page;
import org.akaza.openclinica.web.InsufficientPermissionException;
import org.akaza.openclinica.web.SQLInitServlet;

import java.text.DateFormat;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

import javax.servlet.http.HttpSession;

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

    public static final String PARENT_ID = "parentId";// parent note id

    public static final String ENTITY_TYPE = "name";

    public static final String ENTITY_COLUMN = "column";

    public static final String ENTITY_FIELD = "field";

    public static final String FORM_DISCREPANCY_NOTES_NAME = "fdnotes";

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

    /*
     * (non-Javadoc)
     *
     * @see org.akaza.openclinica.control.core.SecureController#mayProceed()
     */
    @Override
    protected void mayProceed() throws InsufficientPermissionException {
        checkStudyLocked(Page.MENU_SERVLET, respage.getString("current_study_locked"));
        locale = request.getLocale();
        // <
        // resexception=ResourceBundle.getBundle(
        // "org.akaza.openclinica.i18n.exceptions",locale);
        // < respage =
        // ResourceBundle.getBundle("org.akaza.openclinica.i18n.page_messages",
        // locale);

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
        ArrayList types = DiscrepancyNoteType.toArrayList();

        request.setAttribute(DIS_TYPES, types);
        request.setAttribute(RES_STATUSES, ResolutionStatus.toArrayList());
        // types.remove(DiscrepancyNoteType.ANNOTATION);//this for legancy data
        // only, not for new notes

        /*
         * ArrayList ress = ResolutionStatus.toArrayList(); for ( int i=0;
         * i<ress.size(); i++) { ResolutionStatus s=
         * (ResolutionStatus)ress.get(i); System.out.println("status:" +
         * s.getName()) ; }
         */

        boolean writeToDB = fp.getBoolean(WRITE_TO_DB, true);
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

            // } else if ("1".equalsIgnoreCase(monitor) &&
            // "1".equalsIgnoreCase(blank)){
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
        if (!StringUtil.isBlank(entityType)) {
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
                if (!StringUtil.isBlank(column)) {
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
                if (!StringUtil.isBlank(column)) {
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
                if (!StringUtil.isBlank(column)) {
                    if ("location".equalsIgnoreCase(column)) {
                        request.setAttribute("entityValue", (se.getLocation().equals("") || se.getLocation() == null) ? resword.getString("N/A") : se.getLocation());
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
                if (!StringUtil.isBlank(column)) {
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
        } /* 
        else {
            if (!isNew) {// not a new note, so try to find the parent note
                for (int i = 0; i < notes.size(); i++) {
                    DiscrepancyNoteBean note1 = (DiscrepancyNoteBean) notes.get(i);
                    if (note1.getParentDnId() == 0) {
                        parent = note1;
                        parent.setEntityId(note1.getEntityId());
                        parent.setColumn(note1.getColumn());
                        parent.setId(note1.getId());
                        request.setAttribute("parent", note1);
                        break;
                    }
                }
            }
        }*/
        FormDiscrepancyNotes newNotes = (FormDiscrepancyNotes) session.getAttribute(FORM_DISCREPANCY_NOTES_NAME);

        if (newNotes == null) {
            newNotes = new FormDiscrepancyNotes();
        }

        if (!notes.isEmpty() || !newNotes.getNotes(field).isEmpty()) {
            request.setAttribute("hasNotes", "yes");
            // request.setAttribute("parent", parent);
        } else {
            request.setAttribute("hasNotes", "no");
            logger.info("has notes:" + "no");
        }

        /*
        if (currentRole.getRole().equals(Role.RESEARCHASSISTANT) && currentStudy.getId() != currentStudy.getParentStudyId()
            || currentRole.getRole().equals(Role.INVESTIGATOR)) {
            if (parent.getId() > 0 && parent.getDisType().equals(DiscrepancyNoteType.QUERY)) {
                ArrayList resStatuses = new ArrayList(); // ResolutionStatus.toArrayList();
                resStatuses.add(ResolutionStatus.UPDATED);
                resStatuses.add(ResolutionStatus.RESOLVED);//resolution proposed
                request.setAttribute(RES_STATUSES, resStatuses);
                System.out.println("reset resolution status");
            } else {
                // they cant do anything to log a new query, so we remove
                // this type
                ArrayList types2 = DiscrepancyNoteType.toArrayList();
                types2.remove(DiscrepancyNoteType.QUERY);
                request.setAttribute(DIS_TYPES, types2);
                System.out.println("reset discrepancy types");
            }

        }*/
        //only for adding a new thread
        if (currentRole.getRole().equals(Role.RESEARCHASSISTANT) || currentRole.getRole().equals(Role.INVESTIGATOR)) { 
            ArrayList<ResolutionStatus> resStatuses = new ArrayList<ResolutionStatus>(); 
            resStatuses.add(ResolutionStatus.OPEN);
            resStatuses.add(ResolutionStatus.RESOLVED);
            request.setAttribute(RES_STATUSES, resStatuses); 
            ArrayList types2 = DiscrepancyNoteType.toArrayList();
            types2.remove(DiscrepancyNoteType.QUERY);
            request.setAttribute(DIS_TYPES, types2);
            request.setAttribute(WHICH_RES_STATUSES, "22");
        } else if(currentRole.getRole().equals(Role.MONITOR)){
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
            ArrayList<ResolutionStatus> resStatuses = ResolutionStatus.toArrayList();
            resStatuses.remove(ResolutionStatus.NOT_APPLICABLE);
            request.setAttribute(RES_STATUSES, resStatuses); ;   
            request.setAttribute(WHICH_RES_STATUSES, "2");
            //it's for parentDNId is null or 0 and FVC  
            //ArrayList<ResolutionStatus> resStatuses = new ArrayList<ResolutionStatus>(); 
            //resStatuses.add(ResolutionStatus.OPEN);
            //resStatuses.add(ResolutionStatus.RESOLVED);
            //request.setAttribute(RES_STATUSES, resStatuses);
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
             * 0004092: CRCs and Investigators allowed to close a Query
            	CRCs and Investigators are allowed to choose Closed for a Query. they are also allowed to choose New.

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
                        // request.setAttribute(PRESET_RES_STATUS, new Integer(ResolutionStatus.NOT_APPLICABLE.getId()).toString());
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
                    if (currentRole.getRole().equals(Role.RESEARCHASSISTANT)    
                        || currentRole.getRole().equals(Role.INVESTIGATOR)) {
                        request.setAttribute("autoView", "0");
                    } else {
                        request.setAttribute("autoView", "1");
                        dnb.setAssignedUserId(preUserId);
                    }
                    // above set to automatically open up the user panel
                    // }
                }

            }

            else if (parent.getDiscrepancyNoteTypeId() > 0) {
                dnb.setDiscrepancyNoteTypeId(parent.getDiscrepancyNoteTypeId());

                // if it is a CRC then we should automatically propose a
                // solution, tbh

                // adding second rule here, tbh 08/2009
                if (currentRole.getRole().equals(Role.RESEARCHASSISTANT) && currentStudy.getId() != currentStudy.getParentStudyId()) {
                    dnb.setResolutionStatusId(ResolutionStatus.RESOLVED.getId());
                    request.setAttribute("autoView", "0");
                    // hide the panel, tbh
                } else {
                    dnb.setResolutionStatusId(ResolutionStatus.UPDATED.getId());
                }

            }
            dnb.setOwnerId(parent.getOwnerId());
            // System.out.println("just set owner id to " + parent.getOwnerId());
            String detailedDes = fp.getString("strErrMsg");
            if (detailedDes != null) {
                dnb.setDetailedNotes(detailedDes);
                System.out.println("found strErrMsg: " + fp.getString("strErrMsg"));
            }
            // #4346 TBH 10/2009 

            dnb = getNoteInfo(dnb);// populate note infos
            //            String detailedDes = fp.getString("strErrMsg");
            //            if (detailedDes != null) {
            //            	dnb.setDetailedNotes(detailedDes);
            //            	// System.out.println("found strErrMsg: " +fp.getString("strErrMsg"));
            //            }
            //            // #4346 TBH 10/2009 
            request.setAttribute(DIS_NOTE, dnb);
            request.setAttribute("unlock", "0");
            request.setAttribute(WRITE_TO_DB, writeToDB ? "1" : "0");
            ArrayList userAccounts = this.generateUserAccounts(ub.getActiveStudyId(), subjectId);
            request.setAttribute(USER_ACCOUNTS, userAccounts);

            // ideally should be only two cases
            if (currentRole.getRole().equals(Role.RESEARCHASSISTANT) && currentStudy.getId() != currentStudy.getParentStudyId()) {
                // assigning back to OP, tbh
                request.setAttribute(USER_ACCOUNT_ID, new Integer(parent.getOwnerId()).toString());
                System.out.println("assigned owner id: " + parent.getOwnerId());
            } else if (dnb.getEventCRFId() > 0) {
                System.out.println("found a event crf id: " + dnb.getEventCRFId());
                EventCRFDAO eventCrfDAO = new EventCRFDAO(sm.getDataSource());
                EventCRFBean eventCrfBean = new EventCRFBean();
                eventCrfBean = (EventCRFBean) eventCrfDAO.findByPK(dnb.getEventCRFId());
                request.setAttribute(USER_ACCOUNT_ID, new Integer(eventCrfBean.getOwnerId()).toString());
                System.out.println("assigned owner id: " + eventCrfBean.getOwnerId());
            } else {
                // the end case

            }

            // set the user account id for the user who completed data entry
            forwardPage(Page.ADD_DISCREPANCY_NOTE);

        } else {
            System.out.println("submitted ************");

            FormDiscrepancyNotes noteTree = (FormDiscrepancyNotes) session.getAttribute(FORM_DISCREPANCY_NOTES_NAME);

            if (noteTree == null) {
                noteTree = new FormDiscrepancyNotes();
                logger.info("No note tree initailized in session");
            }

            Validator v = new Validator(request);
            String description = fp.getString("description");
            int typeId = fp.getInt("typeId");
            int assignedUserAccountId = fp.getInt(SUBMITTED_USER_ACCOUNT_ID);
            int resStatusId = fp.getInt(RES_STATUS_ID);
            String detailedDes = fp.getString("detailedDes");
            // #4346 TBH 10/2009
            // String detailedDes = fp.getString("strErrMsg");
            // System.out.println("found strErrMsg: " +fp.getString("strErrMsg") + " and detailedDes: " + fp.getString("detailedDes"));
            int sectionId = fp.getInt("sectionId");
            String groupLabel = fp.getString("groupLabel");
            DiscrepancyNoteBean note = new DiscrepancyNoteBean();
            v.addValidation("description", Validator.NO_BLANKS);
            v.addValidation("description", Validator.LENGTH_NUMERIC_COMPARISON, NumericComparisonOperator.LESS_THAN_OR_EQUAL_TO, 255);
            v.addValidation("detailedDes", Validator.LENGTH_NUMERIC_COMPARISON, NumericComparisonOperator.LESS_THAN_OR_EQUAL_TO, 1000);

            v.addValidation("typeId", Validator.NO_BLANKS);

            HashMap errors = v.validate();
            note.setDescription(description);
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
                    System.out.println("^^^ found assigned user id: " + assignedUserAccountId);

                } else {
                    // a little bit of a workaround, should ideally be always from
                    // the form
                    note.setAssignedUserId(parent.getOwnerId());
                    System.out.println("found user assigned id, in the PARENT OWNER ID: " + parent.getOwnerId() + " note that user assgined id did not work: "
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
                // System.out.println("note entity id:" + entityId);
                note.setEntityId(entityId);
                note.setEntityType(entityType);
                note.setColumn(column);
            } else {
                // System.out.println("parent entity id:" +
                // parent.getEntityId());
                note.setEntityId(parent.getEntityId());
                note.setEntityType(parent.getEntityType());
                if (!StringUtil.isBlank(parent.getColumn())) {
                    note.setColumn(parent.getColumn());
                } else {
                    note.setColumn(column);
                }
                note.setParentDnId(parent.getId());
            }

            note.setStudyId(currentStudy.getId());

            note = getNoteInfo(note);// populate note infos

            request.setAttribute(DIS_NOTE, note);
            request.setAttribute(WRITE_TO_DB, writeToDB ? "1" : "0");
            ArrayList userAccounts = this.generateUserAccounts(ub.getActiveStudyId(), subjectId);

            request.setAttribute(USER_ACCOUNT_ID, new Integer(note.getAssignedUserId()).toString());
            // formality more than anything else, we should go to say the note
            // is done

            Role r = currentRole.getRole();
            if (r.equals(Role.MONITOR) || r.equals(Role.INVESTIGATOR) || r.equals(Role.RESEARCHASSISTANT) || r.equals(Role.COORDINATOR)) { // investigator
                request.setAttribute("unlock", "1");
                System.out.println("set UNLOCK to ONE");
            } else {
                request.setAttribute("unlock", "0");
                System.out.println("set UNLOCK to ZERO");
            }

            request.setAttribute(USER_ACCOUNTS, userAccounts);

            if (errors.isEmpty()) {

                if (!writeToDB) {
                    System.out.println("*** save note into session: " + field);
                    noteTree.addNote(field, note);
                    noteTree.addIdNote(note.getEntityId(), field);
                    session.setAttribute(FORM_DISCREPANCY_NOTES_NAME, noteTree);
                    //
                    System.out.println("forwarding on line 537");
                    /*Setting a marker to check later while saving administrative edited data. This is needed to make
                    * sure the system flags error while changing data for items which already has a DiscrepanyNote*/
                    //session.setAttribute(DataEntryServlet.NOTE_SUBMITTED, true);
                    manageReasonForChangeState(session, entityId);
                    forwardPage(Page.ADD_DISCREPANCY_NOTE_DONE);
                } else {
                    // if not creating a new thread(note), update exsiting notes
                    // if necessary
                    //if ("itemData".equalsIgnoreCase(entityType) && !isNew) {
                    int pdnId = note!=null?note.getParentDnId():0;
                    if(pdnId > 0) {
                        System.out.println("Create:find parent note for item data:" + note.getEntityId());

                        DiscrepancyNoteBean pNote = (DiscrepancyNoteBean) dndao.findByPK(pdnId);

                        System.out.println("setting owner id: " + pNote.getOwnerId());

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
                    if (note.getEntityId() == 0) {
                        // addPageMessage(
                        // "Your discrepancy note for the item cannot be saved because there is no item data for this CRF in the database yet."
                        // );
                        addPageMessage(respage.getString("note_cannot_be_saved"));
                        forwardPage(Page.ADD_DISCREPANCY_NOTE_SAVE_DONE);
                        return;
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
                    manageReasonForChangeState(session, entityId);

                    System.out.println("found resolution status: " + note.getResolutionStatusId());

                    String email = fp.getString(EMAIL_USER_ACCOUNT);

                    System.out.println("found email: " + email);
                    if (note.getAssignedUserId() > 0 && "1".equals(email.trim()) && DiscrepancyNoteType.QUERY.getId() == note.getDiscrepancyNoteTypeId()) {

                        System.out.println("++++++ found our way here: " + note.getDiscrepancyNoteTypeId() + " id number and " + note.getDisType().getName());
                        logger.info("++++++ found our way here");
                        // generate email for user here
                        StringBuffer message = new StringBuffer();

                        // generate message here
                        EmailEngine em = new EmailEngine(EmailEngine.getSMTPHost());
                        UserAccountDAO userAccountDAO = new UserAccountDAO(sm.getDataSource());
                        ItemDAO itemDAO = new ItemDAO(sm.getDataSource());
                        ItemDataDAO iddao = new ItemDataDAO(sm.getDataSource());
                        ItemBean item = new ItemBean();
                        ItemDataBean itemData = new ItemDataBean();
                        SectionBean section = new SectionBean();

                        StudyDAO studyDAO = new StudyDAO(sm.getDataSource());
                        UserAccountBean assignedUser = (UserAccountBean) userAccountDAO.findByPK(note.getAssignedUserId());
                        String alertEmail = assignedUser.getEmail();
                        message.append(MessageFormat.format(respage.getString("mailDNHeader"), assignedUser.getFirstName(),assignedUser.getLastName()));
                        message.append("<A HREF='" + SQLInitServlet.getField("sysURL.base")
                                + "ViewNotes?module=submit&listNotes_f_discrepancyNoteBean.user=" + assignedUser.getName()
                                + "&listNotes_f_entityName=" + note.getEntityName()
                                + "'>" + SQLInitServlet.getField("sysURL.base") + "</A><BR/>");
                        message.append(respage.getString("you_received_this_from"));
                        StudyBean study = (StudyBean) studyDAO.findByPK(note.getStudyId());
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
                        message.append(MessageFormat.format(respage.getString("mailDNParameters1"), note.getDescription(), note.getDetailedNotes(), ub.getName()));
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
                        }

                        message.append(respage.getString("email_body_separator"));
                        message.append(MessageFormat.format(respage.getString("mailDNThanks"), study.getName()));
                        message.append(respage.getString("email_body_separator"));
                        message.append(respage.getString("disclaimer"));
                        message.append(respage.getString("email_body_separator"));
                        message.append(respage.getString("email_footer"));


                        /*
                         *
                         *
                         *
                         * Please select the link below to view the information
                         * provided. You may need to login to
                         * OpenClinica_testbed with your user name and password
                         * after selecting the link. If you receive a page
                         * cannot be displayed message, please make sure to
                         * select the Change Study/Site link in the upper right
                         * table of the page, select the study referenced above,
                         * and select the link again.
                         *
                         * https://openclinica.s-3.com/OpenClinica_testbed/
                         * ViewSectionDataEntry ?ecId=117&sectionId=142&tabId=2
                         */
//                        message.append("<P>" + respage.getString("please_select_the_link_below_dn") + "</P>");



                        String emailBodyString = message.toString();
                        sendEmail(alertEmail.trim(), EmailEngine.getAdminEmail(), MessageFormat.format(respage.getString("mailDNSubject"),study.getName(), note.getEntityName()), emailBodyString, true, null,
                                null, true);

                    } else {
                        System.out.println("did not send email, but did save DN");
                    }
                    // addPageMessage(
                    // "Your discrepancy note has been saved into database.");
                    addPageMessage(respage.getString("note_saved_into_db"));
                    addPageMessage(respage.getString("page_close_automatically"));
                    System.out.println("forwarding on line 637");
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
        ArrayList arguments = new ArrayList();

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
     * @param note
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

    private ArrayList generateUserAccounts(int studyId, int subjectId) {
        UserAccountDAO userAccountDAO = new UserAccountDAO(sm.getDataSource());
        StudyDAO studyDAO = new StudyDAO(sm.getDataSource());
        StudyBean subjectStudy = studyDAO.findByStudySubjectId(subjectId);
        // study id, tbh 03/2009
        ArrayList userAccounts = new ArrayList();
        if (currentStudy.getParentStudyId() > 0) {
            userAccounts = userAccountDAO.findAllUsersByStudyOrSite(studyId, currentStudy.getParentStudyId(), subjectId);
        } else if (subjectStudy.getParentStudyId() > 0) {
            userAccounts = userAccountDAO.findAllUsersByStudyOrSite(subjectStudy.getId(), subjectStudy.getParentStudyId(), subjectId);
        } else {
            userAccounts = userAccountDAO.findAllUsersByStudyOrSite(studyId, 0, subjectId);
        }
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

    private void manageReasonForChangeState(HttpSession session, Integer itemDataBeanId) {
        HashMap<Integer, Boolean> noteSubmitted = (HashMap<Integer, Boolean>) session.getAttribute(DataEntryServlet.NOTE_SUBMITTED);
        if (noteSubmitted == null) {
            noteSubmitted = new HashMap<Integer, Boolean>();
        }
        noteSubmitted.put(itemDataBeanId, Boolean.TRUE);
        session.setAttribute(DataEntryServlet.NOTE_SUBMITTED, noteSubmitted);
    }

}
