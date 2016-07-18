/*
 * OpenClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: http://www.openclinica.org/license
 * copyright 2003-2005 Akaza Research
 */
package org.akaza.openclinica.control.submit;

import org.akaza.openclinica.bean.admin.CRFBean;
import org.akaza.openclinica.bean.core.DiscrepancyNoteType;
import org.akaza.openclinica.bean.core.ResolutionStatus;
import org.akaza.openclinica.bean.core.Role;
import org.akaza.openclinica.bean.login.UserAccountBean;
import org.akaza.openclinica.bean.managestudy.*;
import org.akaza.openclinica.bean.submit.*;
import org.akaza.openclinica.control.core.SecureController;
import org.akaza.openclinica.control.form.FormDiscrepancyNotes;
import org.akaza.openclinica.control.form.FormProcessor;
import org.akaza.openclinica.core.form.StringUtil;
import org.akaza.openclinica.dao.admin.AuditDAO;
import org.akaza.openclinica.dao.admin.CRFDAO;
import org.akaza.openclinica.dao.login.UserAccountDAO;
import org.akaza.openclinica.dao.managestudy.*;
import org.akaza.openclinica.dao.submit.*;
import org.akaza.openclinica.i18n.core.LocaleResolver;
import org.akaza.openclinica.view.Page;
import org.akaza.openclinica.web.InsufficientPermissionException;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * @author jxu
 * 
 *         View the detail of a discrepancy note on the data entry page
 */
public class ViewDiscrepancyNoteServlet extends SecureController {

    Locale locale;
    // < ResourceBundleresexception,respage;

    public static final String ENTITY_ID = "id";
    public static final String PARENT_ID = "parentId";
    public static final String ENTITY_TYPE = "name";
    public static final String ENTITY_COLUMN = "column";
    public static final String ENTITY_FIELD = "field";
    public static final String DIS_NOTES = "discrepancyNotes";
    public static final String DIS_NOTE = "discrepancyNote";
    public static final String LOCKED_FLAG = "isLocked";// if an event crf is
    public static final String RES_STATUSES = "resolutionStatuses";
    public static final String RES_STATUSES2 = "resolutionStatuses2";
    public static final String DIS_TYPES = "discrepancyTypes";
    public static final String DIS_TYPES2 = "discrepancyTypes2";
    public static final String WHICH_RES_STATUSES = "whichResStatus";
    public static final String USER_ACCOUNTS = "userAccounts";
    public static final String BOX_DN_MAP = "boxDNMap";
    public static final String AUTOVIEWS = "autoViews";
    public static final String BOX_TO_SHOW = "boxToShow";
    public static final String VIEW_DN_LINK = "viewDNLink";
    public static final String IS_REASON_FOR_CHANGE = "isRfc";
    public static final String CAN_MONITOR = "canMonitor";
    public static final String ERROR_FLAG = "errorFlag";
    public static final String FROM_BOX = "fromBox";

    public String exceptionName = resexception.getString("no_permission_to_create_discrepancy_note");
    public String noAccessMessage = respage.getString("you_may_not_create_discrepancy_note") + respage.getString("change_study_contact_sysadmin");

    // locked, so don't
    // allow to enter notes

    /*
     * (non-Javadoc)
     * 
     * @see org.akaza.openclinica.control.core.SecureController#mayProceed()
     */
    @Override
    protected void mayProceed() throws InsufficientPermissionException {

        locale = LocaleResolver.getLocale(request);
        // <
        // resexception=ResourceBundle.getBundle("org.akaza.openclinica.i18n.exceptions",locale);
        // < respage =
        // ResourceBundle.getBundle("org.akaza.openclinica.i18n.page_messages",locale);

        if (SubmitDataServlet.mayViewData(ub, currentRole)) {
            return;
        }

        addPageMessage(noAccessMessage);
        throw new InsufficientPermissionException(Page.MENU_SERVLET, exceptionName, "1");
    }

    @Override
    @SuppressWarnings("unchecked")
    protected void processRequest() throws Exception {
        FormProcessor fp = new FormProcessor(request);

        int eventCRFId = fp.getInt(CreateDiscrepancyNoteServlet.EVENT_CRF_ID);
        request.setAttribute(CreateDiscrepancyNoteServlet.EVENT_CRF_ID, new Integer(eventCRFId));
        
        request.setAttribute(DIS_TYPES, DiscrepancyNoteType.list);
        if (currentRole.getRole().equals(Role.RESEARCHASSISTANT) ||currentRole.getRole().equals(Role.RESEARCHASSISTANT2) || currentRole.getRole().equals(Role.INVESTIGATOR)) {
            ArrayList<ResolutionStatus> resStatuses = new ArrayList();
            resStatuses.add(ResolutionStatus.UPDATED);
            resStatuses.add(ResolutionStatus.RESOLVED);
            request.setAttribute(RES_STATUSES, resStatuses);
            // it's for parentDNId is null or 0
            request.setAttribute(WHICH_RES_STATUSES, "22");
            ArrayList<ResolutionStatus> resStatuses2 = new ArrayList<ResolutionStatus>();
            resStatuses2.add(ResolutionStatus.OPEN);
            resStatuses2.add(ResolutionStatus.RESOLVED);
            request.setAttribute(RES_STATUSES2, resStatuses2);
            List<DiscrepancyNoteType> types2 = new ArrayList<DiscrepancyNoteType>(DiscrepancyNoteType.list);
            types2.remove(DiscrepancyNoteType.QUERY);
            request.setAttribute(DIS_TYPES2, types2);
        } else if (currentRole.getRole().equals(Role.MONITOR)) {
            ArrayList<ResolutionStatus> resStatuses = new ArrayList();
            resStatuses.add(ResolutionStatus.OPEN);
            resStatuses.add(ResolutionStatus.UPDATED);
            resStatuses.add(ResolutionStatus.CLOSED);
            request.setAttribute(RES_STATUSES, resStatuses);
            request.setAttribute(WHICH_RES_STATUSES, "1");
            ArrayList<DiscrepancyNoteType> types2 = new ArrayList<DiscrepancyNoteType>();
            types2.add(DiscrepancyNoteType.QUERY);
            request.setAttribute(DIS_TYPES2, types2);
        } else {// Role.STUDYDIRECTOR Role.COORDINATOR
            List<ResolutionStatus> resStatuses = new ArrayList<ResolutionStatus>(ResolutionStatus.list);
            resStatuses.remove(ResolutionStatus.NOT_APPLICABLE);
            request.setAttribute(RES_STATUSES, resStatuses);
            ;
            // it's for parentDNId is null or 0 and FVC
            request.setAttribute(WHICH_RES_STATUSES, "2");
            ArrayList<ResolutionStatus> resStatuses2 = new ArrayList<ResolutionStatus>();
            resStatuses2.add(ResolutionStatus.OPEN);
            resStatuses2.add(ResolutionStatus.RESOLVED);
            request.setAttribute(RES_STATUSES2, resStatuses2);
        }
        // logic from CreateDiscrepancyNoteServlet
        request.setAttribute("unlock", "0");
        fp.getBoolean(IS_REASON_FOR_CHANGE);
        fp.getBoolean(ERROR_FLAG);
        String monitor = fp.getString("monitor");
        // String enterData = fp.getString("enterData");
        // request.setAttribute("enterData", enterData);
        // boolean enteringData = false;
        // if (enterData != null && "1".equalsIgnoreCase(enterData)) {
        // variables are not set in JSP, so not from viewing data and from
        // entering data
        // request.setAttribute(CAN_MONITOR, "1");
        // request.setAttribute("monitor", monitor);
        // enteringData = true;
        // } else if ("1".equalsIgnoreCase(monitor)) {// change to allow user to
        if ("1".equalsIgnoreCase(monitor)) {// change to allow user to
            // enter note for all items,
            // not just blank items
            request.setAttribute(CAN_MONITOR, "1");
            request.setAttribute("monitor", monitor);
        } else {
            request.setAttribute(CAN_MONITOR, "0");
        }

        Boolean fromBox = fp.getBoolean(FROM_BOX);
        if (fromBox == null || !fromBox) {
            session.removeAttribute(BOX_TO_SHOW);
            session.removeAttribute(BOX_DN_MAP);
            session.removeAttribute(AUTOVIEWS);
        }

        Boolean refresh = fp.getBoolean("refresh");
        request.setAttribute("refresh", refresh + "");
        String ypos = fp.getString("y");
        if (ypos == null || ypos.length() == 0) {
            ypos = "0";
        }

        request.setAttribute("y", ypos);

        DiscrepancyNoteDAO dndao = new DiscrepancyNoteDAO(sm.getDataSource());
        int entityId = fp.getInt(ENTITY_ID, true);
        String name = fp.getString(ENTITY_TYPE, true);

        String column = fp.getString(ENTITY_COLUMN, true);

        String field = fp.getString(ENTITY_FIELD, true);

        String isLocked = fp.getString(LOCKED_FLAG);

        if (!StringUtil.isBlank(isLocked) && "yes".equalsIgnoreCase(isLocked)) {

            request.setAttribute(LOCKED_FLAG, "yes");
        } else {
            request.setAttribute(LOCKED_FLAG, "no");
        }

        DateFormat dateFormatter = DateFormat.getDateInstance(DateFormat.DEFAULT, locale);

        int subjectId = fp.getInt(CreateDiscrepancyNoteServlet.SUBJECT_ID, true);
        int itemId = fp.getInt(CreateDiscrepancyNoteServlet.ITEM_ID, true);

        StudySubjectBean ssub = new StudySubjectBean();
        if (subjectId > 0) {
            StudySubjectDAO ssdao = new StudySubjectDAO(sm.getDataSource());
            ssub = (StudySubjectBean) ssdao.findByPK(subjectId);
            request.setAttribute("noteSubject", ssub);
        }
        ItemBean item = new ItemBean();
        if (itemId > 0) {
            ItemDAO idao = new ItemDAO(sm.getDataSource());
            item = (ItemBean) idao.findByPK(itemId);
            request.setAttribute("item", item);
            request.setAttribute("entityName", item.getName());
        }
        ItemDataBean itemData = new ItemDataBean();
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MMM-yyyy");
        int preUserId = 0;
        if (!StringUtil.isBlank(name)) {
            if ("itemData".equalsIgnoreCase(name)) {
                ItemDataDAO iddao = new ItemDataDAO(sm.getDataSource());
                itemData = (ItemDataBean) iddao.findByPK(entityId);
                request.setAttribute("entityValue", itemData.getValue());
                request.setAttribute("entityName", item.getName());

                EventCRFDAO ecdao = new EventCRFDAO(sm.getDataSource());
                EventCRFBean ec = (EventCRFBean) ecdao.findByPK(itemData.getEventCRFId());
                preUserId = ec.getOwnerId() > 0 ? ec.getOwnerId() : 0;
                request.setAttribute("entityCreatedDate", sdf.format(ec.getCreatedDate()));

                StudyEventDAO sed = new StudyEventDAO(sm.getDataSource());
                StudyEventBean se = (StudyEventBean) sed.findByPK(ec.getStudyEventId());

                StudyEventDefinitionDAO seddao = new StudyEventDefinitionDAO(sm.getDataSource());
                StudyEventDefinitionBean sedb = (StudyEventDefinitionBean) seddao.findByPK(se.getStudyEventDefinitionId());

                se.setName(sedb.getName());
                request.setAttribute("studyEvent", se);

                CRFVersionDAO cvdao = new CRFVersionDAO(sm.getDataSource());
                CRFVersionBean cv = (CRFVersionBean) cvdao.findByPK(ec.getCRFVersionId());

                CRFDAO cdao = new CRFDAO(sm.getDataSource());
                CRFBean crf = (CRFBean) cdao.findByPK(cv.getCrfId());
                request.setAttribute("crf", crf);

            } else if ("studySub".equalsIgnoreCase(name)) {
                StudySubjectDAO ssdao = new StudySubjectDAO(sm.getDataSource());
                ssub = (StudySubjectBean) ssdao.findByPK(entityId);
                SubjectDAO sdao = new SubjectDAO(sm.getDataSource());
                SubjectBean sub = (SubjectBean) sdao.findByPK(ssub.getSubjectId());
                request.setAttribute("noteSubject", ssub);

                if (!StringUtil.isBlank(column)) {
                    if ("enrollment_date".equalsIgnoreCase(column)) {
                        if (ssub.getEnrollmentDate() != null) {
                            request.setAttribute("entityValue", dateFormatter.format(ssub.getEnrollmentDate()));
                        }
                        request.setAttribute("entityName", resword.getString("enrollment_date"));
                    } else if ("gender".equalsIgnoreCase(column)) {
                        request.setAttribute("entityValue", sub.getGender() + "");
                        request.setAttribute("entityName", resword.getString("gender"));
                    } else if ("date_of_birth".equalsIgnoreCase(column)) {
                        if (sub.getDateOfBirth() != null) {
                            request.setAttribute("entityValue", dateFormatter.format(sub.getDateOfBirth()));
                        }
                        request.setAttribute("entityName", resword.getString("date_of_birth"));
                    } else if ("unique_identifier".equalsIgnoreCase(column)) {
                        if (sub.getUniqueIdentifier() != null) {
                            request.setAttribute("entityValue", sub.getUniqueIdentifier());
                        }
                        request.setAttribute("entityName", resword.getString("unique_identifier"));
                    }
                }
                preUserId = ssub.getOwnerId() > 0 ? ssub.getOwnerId() : 0;
                request.setAttribute("entityCreatedDate", sdf.format(ssub.getCreatedDate()));

            } else if ("subject".equalsIgnoreCase(name)) {

                SubjectDAO sdao = new SubjectDAO(sm.getDataSource());
                SubjectBean sub = (SubjectBean) sdao.findByPK(entityId);
                // be caution: here for subject, noteSubject is SubjectBean and
                // label is unique_identifier
                sub.setLabel(sub.getUniqueIdentifier());
                request.setAttribute("noteSubject", sub);

                if (!StringUtil.isBlank(column)) {
                    if ("gender".equalsIgnoreCase(column)) {
                        request.setAttribute("entityValue", ssub.getGender() + "");
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
                preUserId = sub.getOwnerId() > 0 ? sub.getOwnerId() : 0;
                request.setAttribute("entityCreatedDate", sdf.format(sub.getCreatedDate()));

            } else if ("studyEvent".equalsIgnoreCase(name)) {

                StudyEventDAO sed = new StudyEventDAO(sm.getDataSource());
                StudyEventBean se = (StudyEventBean) sed.findByPK(entityId);
                StudyEventDefinitionDAO seddao = new StudyEventDefinitionDAO(sm.getDataSource());
                StudyEventDefinitionBean sedb = (StudyEventDefinitionBean) seddao.findByPK(se.getStudyEventDefinitionId());

                se.setName(sedb.getName());
                request.setAttribute("studyEvent", se);
                request.setAttribute("noteSubject", new StudySubjectDAO(sm.getDataSource()).findByPK(se.getStudySubjectId()));

                if (!StringUtil.isBlank(column)) {
                    if ("location".equalsIgnoreCase(column)) {
                        request.setAttribute("entityValue", se.getLocation());
                        request.setAttribute("entityName", resword.getString("location"));
                    } else if ("start_date".equalsIgnoreCase(column)) {
                        if (se.getDateStarted() != null) {
                            request.setAttribute("entityValue", dateFormatter.format(se.getDateStarted()));
                        }
                        request.setAttribute("entityName", resword.getString("start_date"));

                    } else if ("end_date".equalsIgnoreCase(column)) {
                        if (se.getDateEnded() != null) {
                            request.setAttribute("entityValue", dateFormatter.format(se.getDateEnded()));
                        }
                        request.setAttribute("entityName", resword.getString("end_date"));

                    }
                }
                preUserId = se.getOwnerId() > 0 ? se.getOwnerId() : 0;
                request.setAttribute("entityCreatedDate", sdf.format(se.getCreatedDate()));

            } else if ("eventCrf".equalsIgnoreCase(name)) {
                EventCRFDAO ecdao = new EventCRFDAO(sm.getDataSource());
                EventCRFBean ec = (EventCRFBean) ecdao.findByPK(entityId);
                if (!StringUtil.isBlank(column)) {
                    if ("date_interviewed".equals(column)) {
                        if (ec.getDateInterviewed() != null) {
                            request.setAttribute("entityValue", dateFormatter.format(ec.getDateInterviewed()));
                        }
                        request.setAttribute("entityName", resword.getString("date_interviewed"));
                    } else if ("interviewer_name".equals(column)) {
                        request.setAttribute("entityValue", ec.getInterviewerName());
                        request.setAttribute("entityName", resword.getString("interviewer_name"));
                    }
                }

                setupStudyEventCRFAttributes(ec);

                preUserId = ec.getOwnerId() > 0 ? ec.getOwnerId() : 0;
                request.setAttribute("entityCreatedDate", sdf.format(ec.getCreatedDate()));
            }

        }
        boolean writeToDB = fp.getBoolean(CreateDiscrepancyNoteServlet.WRITE_TO_DB, true);

        HashMap<Integer, Integer> autoviews = (HashMap<Integer, Integer>) session.getAttribute(AUTOVIEWS);
        autoviews = autoviews == null ? new HashMap<Integer, Integer>() : autoviews;
        HashMap<Integer, DiscrepancyNoteBean> boxDNMap = (HashMap<Integer, DiscrepancyNoteBean>) session.getAttribute(BOX_DN_MAP);
        if (boxDNMap == null || !boxDNMap.containsKey(0)) {
            boxDNMap = new HashMap<Integer, DiscrepancyNoteBean>();
            // initialize dn for a new thread
            DiscrepancyNoteBean dnb = new DiscrepancyNoteBean();
            if (currentRole.getRole().equals(Role.RESEARCHASSISTANT) ||currentRole.getRole().equals(Role.RESEARCHASSISTANT2) || currentRole.getRole().equals(Role.INVESTIGATOR)) {
                dnb.setDiscrepancyNoteTypeId(DiscrepancyNoteType.ANNOTATION.getId());
                dnb.setResolutionStatusId(ResolutionStatus.NOT_APPLICABLE.getId());
                autoviews.put(0, 0);
                // request.setAttribute("autoView", "0");
            } else {
                dnb.setDiscrepancyNoteTypeId(DiscrepancyNoteType.QUERY.getId());
                dnb.setAssignedUserId(preUserId);
                autoviews.put(0, 1);
                // request.setAttribute("autoView", "1");
            }
            boxDNMap.put(0, dnb);
        } else if (boxDNMap.containsKey(0)) {
            int dnTypeId = boxDNMap.get(0).getDiscrepancyNoteTypeId();
            autoviews.put(0, dnTypeId == 3 ? 1 : 0);
        }
        if (boxDNMap.containsKey(0)) {
            int dnTypeId0 = boxDNMap.get(0).getDiscrepancyNoteTypeId();
            if (dnTypeId0 == 2 || dnTypeId0 == 4) {
                request.setAttribute("typeID0", dnTypeId0 + "");
            }
        }

        // request.setAttribute("enterData", enterData);
        request.setAttribute("monitor", monitor);
        request.setAttribute(ENTITY_ID, entityId + "");
        request.setAttribute(ENTITY_TYPE, name);
        request.setAttribute(ENTITY_FIELD, field);
        request.setAttribute(ENTITY_COLUMN, column);

        request.setAttribute(CreateDiscrepancyNoteServlet.WRITE_TO_DB, writeToDB ? "1" : "0");

        ArrayList notes = (ArrayList) dndao.findAllByEntityAndColumn(name, entityId, column);

        if (notes.size() > 0) {
            notes.get(0);
            // @pgawade 21-May-2011 Corrected the condition to throw no access
            // error
            StudyDAO studyDAO = new StudyDAO(sm.getDataSource());
            int parentStudyForNoteSub = 0;
            // @pgawade #9801: 07-June-2011 corrected the way to get study
            // subject id associated with discrepancy note
            // int noteSubId = note.getOwnerId();
            StudySubjectDAO ssdao = new StudySubjectDAO(sm.getDataSource());
            // StudySubjectBean notessub = (StudySubjectBean)
            // ssdao.findByPK(noteSubId);

            StudySubjectBean notessub = (StudySubjectBean) ssdao.findByPK(subjectId);
            StudyBean studyBeanSub = (StudyBean) studyDAO.findByPK(notessub.getStudyId());
            if (null != studyBeanSub) {
                parentStudyForNoteSub = studyBeanSub.getParentStudyId();
            }

            if (notessub.getStudyId() != currentStudy.getId() && currentStudy.getId() != parentStudyForNoteSub) {
                addPageMessage(noAccessMessage);
                throw new InsufficientPermissionException(Page.MENU_SERVLET, exceptionName, "1");
            }
        }

        FormDiscrepancyNotes newNotes = (FormDiscrepancyNotes) session.getAttribute(AddNewSubjectServlet.FORM_DISCREPANCY_NOTES_NAME);

        Map<Integer, DiscrepancyNoteBean> noteTree = new LinkedHashMap<Integer, DiscrepancyNoteBean>();

        String session_key = eventCRFId+"_"+field;
        ArrayList newFieldNotes=null;
        if (newNotes != null && (!newNotes.getNotes(field).isEmpty() || 
        		!newNotes.getNotes(session_key).isEmpty() ))
        
        {
            newFieldNotes = newNotes.getNotes(field);
            if (newFieldNotes == null || newFieldNotes.size() == 0){
            	newFieldNotes = newNotes.getNotes(session_key);
            }
            // System.out.println("how many notes:" + newFieldNotes.size());
            for (int i = 0; i < newFieldNotes.size(); i++) {
                DiscrepancyNoteBean note = (DiscrepancyNoteBean) newFieldNotes.get(i);
                note.setLastUpdator(ub);
                note.setLastDateUpdated(new Date());
                note.setDisType(DiscrepancyNoteType.get(note.getDiscrepancyNoteTypeId()));
                note.setResStatus(ResolutionStatus.get(note.getResolutionStatusId()));
                note.setSaved(false);
                if (itemId > 0) {
                    note.setEntityName(item.getName());
                    note.setEntityValue(itemData.getValue());
                }
                note.setSubjectName(ssub.getName());
                note.setEntityType(name);

                int pId = note.getParentDnId();
                if (pId == 0) {// we can only keep one unsaved note because
                    // note.id == 0
                    noteTree.put(note.getId(), note);
                }
            }
            for (int i = 0; i < newFieldNotes.size(); i++) {
                DiscrepancyNoteBean note = (DiscrepancyNoteBean) newFieldNotes.get(i);
                int pId = note.getParentDnId();
                if (pId > 0) {
                    note.setSaved(false);
                    note.setLastUpdator(ub);
                    note.setLastDateUpdated(new Date());

                    note.setEntityName(item.getName());
                    note.setSubjectName(ssub.getName());
                    note.setEntityType(name);

                    note.setDisType(DiscrepancyNoteType.get(note.getDiscrepancyNoteTypeId()));
                    note.setResStatus(ResolutionStatus.get(note.getResolutionStatusId()));
                    DiscrepancyNoteBean parent = noteTree.get(new Integer(pId));
                    if (parent != null) {
                        parent.getChildren().add(note);
                    }
                }
            }

        }

        UserAccountDAO udao = new UserAccountDAO(sm.getDataSource());
        HashMap<Integer, String> fvcInitAssigns = new HashMap<Integer, String>();
        for (int i = 0; i < notes.size(); i++) {
            DiscrepancyNoteBean note = (DiscrepancyNoteBean) notes.get(i);
            note.setColumn(column);
            note.setEntityId(entityId);
            note.setEntityType(name);
            note.setField(field);
            Date lastUpdatedDate = note.getCreatedDate();
            UserAccountBean lastUpdator = (UserAccountBean) udao.findByPK(note.getOwnerId());
            note.setLastUpdator(lastUpdator);
            note.setLastDateUpdated(lastUpdatedDate);
            int pId = note.getParentDnId();
            note.setDisType(DiscrepancyNoteType.get(note.getDiscrepancyNoteTypeId()));
            note.setResStatus(ResolutionStatus.get(note.getResolutionStatusId()));
            if (pId == 0) {
                noteTree.put(new Integer(note.getId()), note);
            }
        }

        for (int i = 0; i < notes.size(); i++) {
            DiscrepancyNoteBean note = (DiscrepancyNoteBean) notes.get(i);
            int pId = note.getParentDnId();

            if (itemId > 0) {
                note.setEntityName(item.getName());
                note.setEntityValue(itemData.getValue());
            }
            note.setSubjectName(ssub.getName());
            note.setEntityType(name);

            Date lastUpdatedDate = note.getCreatedDate();
            UserAccountBean lastUpdator = (UserAccountBean) udao.findByPK(note.getOwnerId());
            note.setLastUpdator(lastUpdator);
            note.setLastDateUpdated(lastUpdatedDate);
            note.setDisType(DiscrepancyNoteType.get(note.getDiscrepancyNoteTypeId()));
            note.setResStatus(ResolutionStatus.get(note.getResolutionStatusId()));
            if (pId > 0) {
                DiscrepancyNoteBean parent = noteTree.get(new Integer(pId));
                if (parent != null) {
                    parent.getChildren().add(note);
                    if (!note.getCreatedDate().before(parent.getLastDateUpdated())) {
                        parent.setLastDateUpdated(note.getCreatedDate());
                    }

                    if (note.getDiscrepancyNoteTypeId() == 1 && note.getAssignedUserId() > 0) {
                        int ownerId = note.getOwnerId();
                        if (fvcInitAssigns.containsKey(pId)) {
                            String f = fvcInitAssigns.get(pId);
                            String fn = note.getId() + "." + ownerId;
                            if (fn.compareTo(f) < 0) {
                                fvcInitAssigns.put(pId, fn);
                            }
                        } else {
                            fvcInitAssigns.put(pId, note.getId() + "." + ownerId);
                        }
                    }
                }
            }
        }

        Set parents = noteTree.keySet();
        Iterator it = parents.iterator();
        while (it.hasNext()) {
            Integer key = (Integer) it.next();
            DiscrepancyNoteBean note = noteTree.get(key);
            note.setNumChildren(note.getChildren().size());
            note.setEntityType(name);

            if (!boxDNMap.containsKey(key)) {
                DiscrepancyNoteBean dn = new DiscrepancyNoteBean();
                dn.setId(key);
                int dnTypeId = note.getDiscrepancyNoteTypeId();
                dn.setDiscrepancyNoteTypeId(dnTypeId);
                if (dnTypeId == 3) {// Query
                    dn.setAssignedUserId(note.getOwnerId());
                } else if (dnTypeId == 1) {// FVC
                    if (fvcInitAssigns.containsKey(key)) {
                        String[] s = fvcInitAssigns.get(key).split("\\.");
                        int i = Integer.parseInt(s.length == 2 ? s[1].trim() : "0");
                        dn.setAssignedUserId(i);
                    }
                }
                Role r = currentRole.getRole();
                // if (currentRole.getRole().equals(Role.RESEARCHASSISTANT) &&
                // currentStudy.getId() != currentStudy.getParentStudyId()) {
                if (r.equals(Role.RESEARCHASSISTANT) ||r.equals(Role.RESEARCHASSISTANT2) || r.equals(Role.INVESTIGATOR)) {
                    if (dn.getDiscrepancyNoteTypeId() == DiscrepancyNoteType.QUERY.getId() && note.getResStatus().getId() == ResolutionStatus.UPDATED.getId()) {
                        dn.setResolutionStatusId(ResolutionStatus.UPDATED.getId());
                    } else {
                        dn.setResolutionStatusId(ResolutionStatus.RESOLVED.getId());
                    }
                    if (dn.getAssignedUserId() > 0) {
                        autoviews.put(key, 1);
                    } else {
                        autoviews.put(key, 0);
                    }
                    // copied from CreateDiscrepancyNoteServlet
                    // request.setAttribute("autoView", "0");
                    // hide the panel, tbh
                } else {
                    if (note.getResStatus().getId() == ResolutionStatus.RESOLVED.getId()) {
                        dn.setResolutionStatusId(ResolutionStatus.CLOSED.getId());
                    } else if (note.getResStatus().getId() == ResolutionStatus.CLOSED.getId()) {
                        dn.setResolutionStatusId(ResolutionStatus.UPDATED.getId());
                    } else if (r.equals(Role.MONITOR)) {
                        dn.setResolutionStatusId(ResolutionStatus.UPDATED.getId());
                    } else if (dn.getDiscrepancyNoteTypeId() == 1) {
                        dn.setResolutionStatusId(ResolutionStatus.RESOLVED.getId());
                    } else {
                        dn.setResolutionStatusId(ResolutionStatus.UPDATED.getId());
                    }
                    autoviews.put(key, 1);
                    if (dn.getAssignedUserId() > 0) {
                    } else {
                        dn.setAssignedUserId(preUserId);
                    }
                }
                boxDNMap.put(key, dn);
            }
        }
        session.setAttribute(BOX_DN_MAP, boxDNMap);
        session.setAttribute(AUTOVIEWS, autoviews);
        // noteTree is a Hashmap mapping note id to a parent note, with all the
        // child notes
        // stored in the children List.
        // BWP 3029>>make sure the parent note has an updated resolution status
        // and
        // updated date
        fixStatusUpdatedDate(noteTree);
        request.setAttribute(DIS_NOTES, noteTree);

        // copied from CreatediscrepancyNoteServlet generateUserAccounts
        StudyDAO studyDAO = new StudyDAO(sm.getDataSource());
        StudyBean subjectStudy = studyDAO.findByStudySubjectId(subjectId);
        int studyId = currentStudy.getId();
        ArrayList<UserAccountBean> userAccounts = new ArrayList();
        if (currentStudy.getParentStudyId() > 0) {
            userAccounts = udao.findAllUsersByStudyOrSite(studyId, currentStudy.getParentStudyId(), subjectId);
        } else if (subjectStudy.getParentStudyId() > 0) {
            userAccounts = udao.findAllUsersByStudyOrSite(subjectStudy.getId(), subjectStudy.getParentStudyId(), subjectId);
        } else {
            userAccounts = udao.findAllUsersByStudyOrSite(studyId, 0, subjectId);
        }
        request.setAttribute(USER_ACCOUNTS, userAccounts);
        request.setAttribute(VIEW_DN_LINK, this.getPageServletFileName());

        // audit log items (from ViewItemAuditLogServlet.java)
        AuditDAO adao = new AuditDAO(sm.getDataSource());
        if (name.equalsIgnoreCase("studysub")) {
            name = "study_subject";
        } else if (name.equalsIgnoreCase("eventcrf")) {
            name = "event_crf";
        } else if (name.equalsIgnoreCase("studyevent")) {
            name = "study_event";
        } else if (name.equalsIgnoreCase("itemdata")) {
            name = "item_data";
        }
        ArrayList itemAuditEvents = adao.findItemAuditEvents(entityId, name);
        request.setAttribute("itemAudits", itemAuditEvents);

        forwardPage(Page.VIEW_DISCREPANCY_NOTE);
    }

    private void setupStudyEventCRFAttributes(EventCRFBean eventCRFBean) {
        StudyEventDAO sed = new StudyEventDAO(sm.getDataSource());
        StudyEventBean studyEventBean = (StudyEventBean) sed.findByPK(eventCRFBean.getStudyEventId());

        StudyEventDefinitionDAO seddao = new StudyEventDefinitionDAO(sm.getDataSource());
        StudyEventDefinitionBean sedb = (StudyEventDefinitionBean) seddao.findByPK(studyEventBean.getStudyEventDefinitionId());

        studyEventBean.setName(sedb.getName());
        request.setAttribute("studyEvent", studyEventBean);

        CRFVersionDAO cvdao = new CRFVersionDAO(sm.getDataSource());
        CRFVersionBean cv = (CRFVersionBean) cvdao.findByPK(eventCRFBean.getCRFVersionId());

        CRFDAO cdao = new CRFDAO(sm.getDataSource());
        CRFBean crf = (CRFBean) cdao.findByPK(cv.getCrfId());
        request.setAttribute("crf", crf);
    }

    /**
     * Update a parent DiscrepancyNoteBean's resolution status and updated date
     * to that of the latest child DiscrepancyNoteBean.
     * 
     * @param noteTree
     *            A HashMap of an Integer representing the DiscrepancyNoteBean,
     *            pointing to a parent DiscrepancyNoteBean.
     */
    private void fixStatusUpdatedDate(Map<Integer, DiscrepancyNoteBean> noteTree) {
        if (noteTree == null || noteTree.isEmpty()) {
            return;
        }
        // foreach parent stored in the Map
        DiscrepancyNoteBean lastChild = null;
        ArrayList<DiscrepancyNoteBean> children;
        for (DiscrepancyNoteBean parent : noteTree.values()) {
            // The parent bean will contain in its children ArrayList property
            // the "automatic" child that is generated in the
            // database at creation, plus any "real" child notes.
            // first sort the beans so we can grab the last child
            Collections.sort(parent.getChildren());
            children = parent.getChildren();
            // tbh 02/2009 fixing an error where a monitor accesses a note
            if (children.size() > 0) {
                // >> tbh
                lastChild = children.get(children.size() - 1);
                if (lastChild != null) {
                    Date lastUpdatedDate = lastChild.getCreatedDate();
                    UserAccountDAO userDAO = new UserAccountDAO(sm.getDataSource());
                    UserAccountBean lastUpdator = (UserAccountBean) userDAO.findByPK(lastChild.getOwnerId());
                    parent.setLastUpdator(lastUpdator);
                    parent.setLastDateUpdated(lastUpdatedDate);
                    // parent.setResolutionStatusId(lastChild.getResolutionStatusId());
                    // parent.setResStatus(ResolutionStatus.get(lastChild.getResolutionStatusId()));
                }
            }
        }

        // Sorting parent notes according to the last child being updated. The
        // parent who has the most recently updated child gets into the top.
        List<DiscrepancyNoteBean> parentNotes = new ArrayList<DiscrepancyNoteBean>(noteTree.values());
        Collections.sort(parentNotes, new Comparator<DiscrepancyNoteBean>() {
            @Override
            public int compare(DiscrepancyNoteBean dn1, DiscrepancyNoteBean dn2) {
                ArrayList<DiscrepancyNoteBean> cn1 = dn1.getChildren();
                DiscrepancyNoteBean child1 = cn1.size() > 0 ? cn1.get(cn1.size() - 1) : dn1;
                ArrayList<DiscrepancyNoteBean> cn2 = dn2.getChildren();
                DiscrepancyNoteBean child2 = cn2.size() > 0 ? cn2.get(cn2.size() - 1) : dn2;
                return child1.getId() > child2.getId() ? -1 : 1;
                // return
                // ((DiscrepancyNoteBean)o1).getLastDateUpdated().after(((DiscrepancyNoteBean)o2).getLastDateUpdated())
                // ? -1 : 1;
            }
        });
        noteTree.clear();
        for (DiscrepancyNoteBean dn : parentNotes) {
            noteTree.put(dn.getId(), dn);
        }
    }

}
