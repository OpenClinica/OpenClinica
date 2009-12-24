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
import org.akaza.openclinica.bean.login.UserAccountBean;
import org.akaza.openclinica.bean.managestudy.DiscrepancyNoteBean;
import org.akaza.openclinica.bean.managestudy.StudyEventBean;
import org.akaza.openclinica.bean.managestudy.StudyEventDefinitionBean;
import org.akaza.openclinica.bean.managestudy.StudySubjectBean;
import org.akaza.openclinica.bean.submit.CRFVersionBean;
import org.akaza.openclinica.bean.submit.EventCRFBean;
import org.akaza.openclinica.bean.submit.ItemBean;
import org.akaza.openclinica.bean.submit.ItemDataBean;
import org.akaza.openclinica.bean.submit.SubjectBean;
import org.akaza.openclinica.control.core.SecureController;
import org.akaza.openclinica.control.form.FormDiscrepancyNotes;
import org.akaza.openclinica.control.form.FormProcessor;
import org.akaza.openclinica.core.form.StringUtil;
import org.akaza.openclinica.dao.admin.CRFDAO;
import org.akaza.openclinica.dao.login.UserAccountDAO;
import org.akaza.openclinica.dao.managestudy.DiscrepancyNoteDAO;
import org.akaza.openclinica.dao.managestudy.StudyEventDAO;
import org.akaza.openclinica.dao.managestudy.StudyEventDefinitionDAO;
import org.akaza.openclinica.dao.managestudy.StudySubjectDAO;
import org.akaza.openclinica.dao.submit.CRFVersionDAO;
import org.akaza.openclinica.dao.submit.EventCRFDAO;
import org.akaza.openclinica.dao.submit.ItemDAO;
import org.akaza.openclinica.dao.submit.ItemDataDAO;
import org.akaza.openclinica.dao.submit.SubjectDAO;
import org.akaza.openclinica.service.DiscrepancyNoteUtil;
import org.akaza.openclinica.view.Page;
import org.akaza.openclinica.web.InsufficientPermissionException;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * @author jxu
 * 
 * View the detail of a discrepancy note on the data entry page
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

    // locked, so don't
    // allow to enter notes

    /*
     * (non-Javadoc)
     * 
     * @see org.akaza.openclinica.control.core.SecureController#mayProceed()
     */
    @Override
    protected void mayProceed() throws InsufficientPermissionException {

        locale = request.getLocale();
        // <
        // resexception=ResourceBundle.getBundle("org.akaza.openclinica.i18n.exceptions",locale);
        // < respage =
        // ResourceBundle.getBundle("org.akaza.openclinica.i18n.page_messages",locale);

        String exceptionName = resexception.getString("no_permission_to_create_discrepancy_note");
        String noAccessMessage = respage.getString("you_may_not_create_discrepancy_note") + respage.getString("change_study_contact_sysadmin");

        if (SubmitDataServlet.mayViewData(ub, currentRole)) {
            return;
        }

        addPageMessage(noAccessMessage);
        throw new InsufficientPermissionException(Page.MENU, exceptionName, "1");
    }

    @Override
    @SuppressWarnings("unchecked")
    protected void processRequest() throws Exception {
        FormProcessor fp = new FormProcessor(request);

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
        if (!StringUtil.isBlank(name)) {
            if ("itemData".equalsIgnoreCase(name)) {
                ItemDataDAO iddao = new ItemDataDAO(sm.getDataSource());
                itemData = (ItemDataBean) iddao.findByPK(entityId);
                request.setAttribute("entityValue", itemData.getValue());
                request.setAttribute("entityName", item.getName());

                EventCRFDAO ecdao = new EventCRFDAO(sm.getDataSource());
                EventCRFBean ec = (EventCRFBean) ecdao.findByPK(itemData.getEventCRFId());

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
                    } else if ("date_start".equalsIgnoreCase(column)) {
                        if (se.getDateStarted() != null) {
                            request.setAttribute("entityValue", dateFormatter.format(se.getDateStarted()));
                        }
                        request.setAttribute("entityName", resword.getString("start_date"));

                    } else if ("date_end".equalsIgnoreCase(column)) {
                        if (se.getDateEnded() != null) {
                            request.setAttribute("entityValue", dateFormatter.format(se.getDateEnded()));
                        }
                        request.setAttribute("entityName", resword.getString("end_date"));

                    }
                }

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

            }

        }
        boolean writeToDB = fp.getBoolean(CreateDiscrepancyNoteServlet.WRITE_TO_DB, true);

        String monitor = fp.getString("monitor");
        String enterData = fp.getString("enterData");

        request.setAttribute("enterData", enterData);
        request.setAttribute("monitor", monitor);
        request.setAttribute(ENTITY_ID, entityId + "");
        request.setAttribute(ENTITY_TYPE, name);
        request.setAttribute(ENTITY_FIELD, field);
        request.setAttribute(ENTITY_COLUMN, column);

        request.setAttribute(CreateDiscrepancyNoteServlet.WRITE_TO_DB, writeToDB ? "1" : "0");

        ArrayList notes = (ArrayList) dndao.findAllByEntityAndColumn(name, entityId, column);
        // BWP 5/13/2009 3468 WHO; update the resolution status of parent disc
        // notes based
        // on the status of child notes
        new DiscrepancyNoteUtil().updateStatusOfParents(notes, sm.getDataSource(), currentStudy);

        FormDiscrepancyNotes newNotes = (FormDiscrepancyNotes) session.getAttribute(CreateDiscrepancyNoteServlet.FORM_DISCREPANCY_NOTES_NAME);

        HashMap<Integer, DiscrepancyNoteBean> noteTree = new HashMap<Integer, DiscrepancyNoteBean>();

        if (newNotes != null && !newNotes.getNotes(field).isEmpty()) {
            ArrayList newFieldNotes = newNotes.getNotes(field);
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
            // if (note.getAssignedUserId() > 0) {
            // System.out.println("found assigned user id: " +
            // note.getAssignedUserId());
            // UserAccountBean assignedUser = (UserAccountBean)
            // udao.findByPK(note.getAssignedUserId());
            // note.setAssignedUser(assignedUser);
            // // setting it twice? tbh
            // }
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
        }
        // noteTree is a Hashmap mapping note id to a parent note, with all the
        // child notes
        // stored in the children List.
        // BWP 3029>>make sure the parent note has an updated resolution status
        // and
        // updated date
        fixStatusUpdatedDate(noteTree);
        request.setAttribute(DIS_NOTES, noteTree);
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
                    parent.setResolutionStatusId(lastChild.getResolutionStatusId());
                    parent.setResStatus(ResolutionStatus.get(lastChild.getResolutionStatusId()));
                }
            }
        }
    }

}
