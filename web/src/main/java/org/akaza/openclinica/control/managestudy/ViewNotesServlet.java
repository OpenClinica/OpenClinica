/*
 * OpenClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: http://www.openclinica.org/license
 * copyright 2003-2005 Akaza Research
 *
 * Created on Sep 23, 2005
 */
package org.akaza.openclinica.control.managestudy;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.akaza.openclinica.bean.admin.CRFBean;
import org.akaza.openclinica.bean.core.AuditableEntityBean;
import org.akaza.openclinica.bean.core.ResolutionStatus;
import org.akaza.openclinica.bean.managestudy.DiscrepancyNoteBean;
import org.akaza.openclinica.bean.managestudy.StudyBean;
import org.akaza.openclinica.bean.managestudy.StudyEventBean;
import org.akaza.openclinica.bean.managestudy.StudyEventDefinitionBean;
import org.akaza.openclinica.bean.managestudy.StudySubjectBean;
import org.akaza.openclinica.bean.submit.CRFVersionBean;
import org.akaza.openclinica.bean.submit.EventCRFBean;
import org.akaza.openclinica.bean.submit.ItemBean;
import org.akaza.openclinica.bean.submit.ItemDataBean;
import org.akaza.openclinica.control.core.SecureController;
import org.akaza.openclinica.control.form.FormProcessor;
import org.akaza.openclinica.control.submit.ListNotesTableFactory;
import org.akaza.openclinica.control.submit.SubmitDataServlet;
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
import org.akaza.openclinica.dao.submit.ItemDAO;
import org.akaza.openclinica.dao.submit.ItemDataDAO;
import org.akaza.openclinica.dao.submit.SubjectDAO;
import org.akaza.openclinica.service.DiscrepancyNoteUtil;
import org.akaza.openclinica.view.Page;
import org.akaza.openclinica.web.InsufficientPermissionException;
import org.akaza.openclinica.web.bean.DiscrepancyNoteRow;

/**
 * 
 * View a list of all discrepancy notes in current study
 * 
 * @author ssachs
 * @author jxu
 */
public class ViewNotesServlet extends SecureController {
    public static final String PRINT = "print";
    public static final String RESOLUTION_STATUS = "resolutionStatus";
    public static final String TYPE = "discNoteType";
    public static final String WIN_LOCATION = "window_location";
    public static final String NOTES_TABLE = "notesTable";
    public static final String DISCREPANCY_NOTE_TYPE = "discrepancyNoteType";
    private boolean showMoreLink;

    /*
     * public static final Map<Integer,String> TYPES = new HashMap<Integer,String>();
     * static{ TYPES.put(1,"Failed Validation Check");
     * TYPES.put(2,"Incomplete"); TYPES.put(3,"Unclear/Unreadable");
     * TYPES.put(4,"Annotation"); TYPES.put(5,"Other"); TYPES.put(6,"Query");
     * TYPES.put(7,"Reason for Change"); }
     */
    /*
     * (non-Javadoc)
     * 
     * @see org.akaza.openclinica.control.core.SecureController#processRequest()
     */
    @Override
    protected void processRequest() throws Exception {
        String module = request.getParameter("module");
        String moduleStr = "manage";
        if (module != null && module.trim().length() > 0) {
            if ("submit".equals(module)) {
                request.setAttribute("module", "submit");
                moduleStr = "submit";
            } else if ("admin".equals(module)) {
                request.setAttribute("module", "admin");
                moduleStr = "admin";
            } else {
                request.setAttribute("module", "manage");
            }
        }

        FormProcessor fp = new FormProcessor(request);
        if(fp.getString("showMoreLink").equals("")){
            showMoreLink = true;
        }else {
            showMoreLink = Boolean.parseBoolean(fp.getString("showMoreLink"));
        }
        
        int oneSubjectId = fp.getInt("id");
        // BWP 11/03/2008 3029: This session attribute in removed in
        // ResolveDiscrepancyServlet.mayProceed() >>
        session.setAttribute("subjectId", oneSubjectId);
        // >>

        int resolutionStatusSubj = fp.getInt(RESOLUTION_STATUS);
        int discNoteType = 0;
        try {
            discNoteType = Integer.parseInt(request.getParameter("type"));
        } catch (NumberFormatException nfe) {
            // Show all DN's
            discNoteType = -1;
        }
        request.setAttribute(DISCREPANCY_NOTE_TYPE, discNoteType);

        boolean removeSession = fp.getBoolean("removeSession");

        // BWP 11/03/2008 3029: This session attribute in removed in
        // ResolveDiscrepancyServlet.mayProceed() >>
        session.setAttribute("module", module);
        // >>

        // Do we only want to view the notes for 1 subject?
        String viewForOne = fp.getString("viewForOne");
        boolean isForOneSubjectsNotes = "y".equalsIgnoreCase(viewForOne);

        DiscrepancyNoteDAO dndao = new DiscrepancyNoteDAO(sm.getDataSource());
        StudyDAO studyDAO = new StudyDAO(sm.getDataSource());
        dndao.setFetchMapping(true);

        int resolutionStatus = 0;
        try {
            resolutionStatus = Integer.parseInt(request.getParameter("resolutionStatus"));
        } catch (NumberFormatException nfe) {
            // Show all DN's
            resolutionStatus = -1;
        }

        if (removeSession) {
            session.removeAttribute(WIN_LOCATION);
            session.removeAttribute(NOTES_TABLE);
        }

        // after resolving a note, user wants to go back to view notes page, we
        // save the current URL
        // so we can go back later
        session.setAttribute(WIN_LOCATION, "ViewNotes?viewForOne=" + viewForOne + "&id=" + oneSubjectId + "&module=" + module + " &removeSession=1");

        boolean hasAResolutionStatus = resolutionStatus >= 1 && resolutionStatus <= 5;
        Set<Integer> resolutionStatusIds = (HashSet) session.getAttribute(RESOLUTION_STATUS);
        // remove the session if there is no resolution status
        if (!hasAResolutionStatus && resolutionStatusIds != null) {
            session.removeAttribute(RESOLUTION_STATUS);
            resolutionStatusIds = null;
        }
        if (hasAResolutionStatus) {
            if (resolutionStatusIds == null) {
                resolutionStatusIds = new HashSet<Integer>();
            }
            resolutionStatusIds.add(resolutionStatus);
            session.setAttribute(RESOLUTION_STATUS, resolutionStatusIds);
        }

        DiscrepancyNoteUtil discNoteUtil = new DiscrepancyNoteUtil();
        Map stats = discNoteUtil.generateDiscNoteSummaryRefactored(sm.getDataSource(), currentStudy, resolutionStatusIds, discNoteType);
        request.setAttribute("summaryMap", stats);
        Set mapKeys = stats.keySet();
        request.setAttribute("mapKeys", mapKeys);


        StudySubjectDAO subdao = new StudySubjectDAO(sm.getDataSource());
        StudyDAO studyDao = new StudyDAO(sm.getDataSource());

        SubjectDAO sdao = new SubjectDAO(sm.getDataSource());

        UserAccountDAO uadao = new UserAccountDAO(sm.getDataSource());
        CRFVersionDAO crfVersionDao = new CRFVersionDAO(sm.getDataSource());
        CRFDAO crfDao = new CRFDAO(sm.getDataSource());
        StudyEventDAO studyEventDao = new StudyEventDAO(sm.getDataSource());
        StudyEventDefinitionDAO studyEventDefinitionDao = new StudyEventDefinitionDAO(sm.getDataSource());
        EventDefinitionCRFDAO eventDefinitionCRFDao = new EventDefinitionCRFDAO(sm.getDataSource());
        ItemDataDAO itemDataDao = new ItemDataDAO(sm.getDataSource());
        ItemDAO itemDao = new ItemDAO(sm.getDataSource());
        EventCRFDAO eventCRFDao = new EventCRFDAO(sm.getDataSource());

        ListNotesTableFactory factory = new ListNotesTableFactory(showMoreLink);
        factory.setSubjectDao(sdao);
        factory.setStudySubjectDao(subdao);
        factory.setUserAccountDao(uadao);
        factory.setStudyDao(studyDao);
        factory.setCurrentStudy(currentStudy);
        factory.setDiscrepancyNoteDao(dndao);
        factory.setCrfDao(crfDao);
        factory.setCrfVersionDao(crfVersionDao);
        factory.setStudyEventDao(studyEventDao);
        factory.setStudyEventDefinitionDao(studyEventDefinitionDao);
        factory.setEventDefinitionCRFDao(eventDefinitionCRFDao);
        factory.setItemDao(itemDao);
        factory.setItemDataDao(itemDataDao);
        factory.setEventCRFDao(eventCRFDao);
        factory.setModule(moduleStr);
        factory.setDiscNoteType(discNoteType);
        factory.setResolutionStatus(resolutionStatus);
        //factory.setResolutionStatusIds(resolutionStatusIds);


        String viewNotesHtml = factory.createTable(request, response).render();
        request.setAttribute("viewNotesHtml", viewNotesHtml);
        
        String viewNotesURL = this.getPageURL();
        session.setAttribute("viewNotesURL", viewNotesURL);
        String viewNotesPageFileName = this.getPageServletFileName();
        session.setAttribute("viewNotesPageFileName", viewNotesPageFileName);
        ArrayList allNotes = ListNotesTableFactory.getNotesForPrintPop();
        factory.populateDataInNote(allNotes);
        session.setAttribute("allNotes", allNotes);
        if ("yes".equalsIgnoreCase(fp.getString(PRINT))) {
            request.setAttribute("allNotes", allNotes);
            forwardPage(Page.VIEW_DISCREPANCY_NOTES_IN_STUDY_PRINT);
        } else {
            forwardPage(Page.VIEW_DISCREPANCY_NOTES_IN_STUDY);
        }
    }

    public ArrayList<DiscrepancyNoteBean> filterForOneSubject(ArrayList<DiscrepancyNoteBean> allNotes, int subjectId, int resolutionStatus) {

        if (allNotes == null || allNotes.isEmpty() || subjectId == 0)
            return allNotes;
        // Are the D Notes filtered by resolution?
        boolean filterByRes = resolutionStatus >= 1 && resolutionStatus <= 5;

        ArrayList<DiscrepancyNoteBean> filteredNotes = new ArrayList<DiscrepancyNoteBean>();
        StudySubjectDAO subjectDao = new StudySubjectDAO(sm.getDataSource());
        StudySubjectBean studySubjBean = (StudySubjectBean) subjectDao.findByPK(subjectId);

        for (DiscrepancyNoteBean discBean : allNotes) {
            if (discBean.getSubjectName().equalsIgnoreCase(studySubjBean.getLabel())) {
                if (!filterByRes) {
                    filteredNotes.add(discBean);
                } else {
                    if (discBean.getResolutionStatusId() == resolutionStatus) {
                        filteredNotes.add(discBean);
                    }
                }
            }
        }

        return filteredNotes;
    }

    private void populateRowsWithAttachedData(ArrayList noteRows) {
        DiscrepancyNoteDAO dndao = new DiscrepancyNoteDAO(sm.getDataSource());

        for (int i = 0; i < noteRows.size(); i++) {
            DiscrepancyNoteRow dnr = (DiscrepancyNoteRow) noteRows.get(i);
            DiscrepancyNoteBean dnb = (DiscrepancyNoteBean) dnr.getBean();

            // get study properties
            dnr.setPartOfSite(dnb.getStudyId() == currentStudy.getId());
            if (dnr.isPartOfSite()) {
                StudyDAO sdao = new StudyDAO(sm.getDataSource());
                StudyBean sb = (StudyBean) sdao.findByPK(dnb.getStudyId());
            }

            if (dnb.getParentDnId() == 0) {
                ArrayList children = dndao.findAllByStudyAndParent(currentStudy, dnb.getId());
                dnr.setNumChildren(children.size());
                dnb.setNumChildren(children.size());

                for (int j = 0; j < children.size(); j++) {
                    DiscrepancyNoteBean child = (DiscrepancyNoteBean) children.get(j);

                    if (child.getResolutionStatusId() > dnb.getResolutionStatusId()) {
                        dnr.setStatus(ResolutionStatus.get(child.getResolutionStatusId()));
                        dnb.setResStatus(ResolutionStatus.get(child.getResolutionStatusId()));
                    }
                }
            }

            String entityType = dnb.getEntityType();

            if (dnb.getEntityId() > 0 && !entityType.equals("")) {
                AuditableEntityBean aeb = dndao.findEntity(dnb);
                // dnr.setEntityName(aeb.getName());
                if (entityType.equalsIgnoreCase("eventCRF")) {
                    EventCRFBean ecb = (EventCRFBean) aeb;
                    CRFVersionDAO cvdao = new CRFVersionDAO(sm.getDataSource());
                    CRFDAO cdao = new CRFDAO(sm.getDataSource());
                    CRFVersionBean cvb = (CRFVersionBean) cvdao.findByPK(ecb.getCRFVersionId());
                    CRFBean cb = (CRFBean) cdao.findByPK(cvb.getCrfId());
                    dnb.setStageId(ecb.getStage().getId());
                    // dnr.setEntityName(cb.getName() + " (" + cvb.getName() +
                    // ")");
                }
                /*
                 * else if (entityType.equalsIgnoreCase("studyEvent")) {
                 * StudyEventDAO sed = new StudyEventDAO(sm.getDataSource());
                 * StudyEventBean se = (StudyEventBean)
                 * sed.findByPK(dnb.getEntityId());
                 * 
                 * StudyEventDefinitionDAO seddao = new
                 * StudyEventDefinitionDAO(sm.getDataSource());
                 * StudyEventDefinitionBean sedb = (StudyEventDefinitionBean)
                 * seddao.findByPK(se.getStudyEventDefinitionId());
                 * 
                 * //dnr.setEntityName(sedb.getName()); }
                 */
                else if (entityType.equalsIgnoreCase("itemData")) {
                    // ItemDataBean idb = (ItemDataBean) aeb;
                    ItemDataDAO iddao = new ItemDataDAO(sm.getDataSource());
                    ItemDataBean idb = (ItemDataBean) iddao.findByPK(dnb.getEntityId());

                    ItemDAO idao = new ItemDAO(sm.getDataSource());
                    ItemBean ib = (ItemBean) idao.findByPK(idb.getItemId());

                    EventCRFDAO ecdao = new EventCRFDAO(sm.getDataSource());
                    EventCRFBean ec = (EventCRFBean) ecdao.findByPK(idb.getEventCRFId());
                    dnb.setStageId(ec.getStage().getId());

                    dnr.setEntityName(ib.getName());

                    StudyEventDAO sed = new StudyEventDAO(sm.getDataSource());
                    StudyEventBean se = (StudyEventBean) sed.findByPK(ec.getStudyEventId());

                    StudyEventDefinitionDAO seddao = new StudyEventDefinitionDAO(sm.getDataSource());
                    StudyEventDefinitionBean sedb = (StudyEventDefinitionBean) seddao.findByPK(se.getStudyEventDefinitionId());

                    se.setName(sedb.getName());

                    StudySubjectDAO ssdao = new StudySubjectDAO(sm.getDataSource());
                    StudySubjectBean ssub = (StudySubjectBean) ssdao.findByPK(ec.getStudySubjectId());

                }
            }
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.akaza.openclinica.control.core.SecureController#mayProceed()
     */
    @Override
    protected void mayProceed() throws InsufficientPermissionException {
        /*
         * if (currentRole.getRole().equals(Role.STUDYDIRECTOR) ||
         * currentRole.getRole().equals(Role.COORDINATOR)) { return; }
         */
        if (SubmitDataServlet.mayViewData(ub, currentRole)) {
            return;
        }

        addPageMessage(respage.getString("no_permission_to_view_discrepancies") + respage.getString("change_study_contact_sysadmin"));
        throw new InsufficientPermissionException(Page.MENU_SERVLET, resexception.getString("not_study_director_or_study_cordinator"), "1");
    }

    private void addUpdatedDateToNotes(List<DiscrepancyNoteBean> discrepancyNoteBeans) {
        if (discrepancyNoteBeans == null || discrepancyNoteBeans.isEmpty()) {
            return;
        }

        DiscrepancyNoteDAO discrepancyNoteDAO = new DiscrepancyNoteDAO(sm.getDataSource());

        // The updated date is the creation date for notes with no parents or
        // children
        for (DiscrepancyNoteBean dnBean : discrepancyNoteBeans) {
            Date createDate = dnBean.getCreatedDate();
            if (isUnaffiliatedDNote(dnBean, discrepancyNoteDAO) && createDate != null) {
                dnBean.setUpdatedDate(createDate);
                continue;
            }

            // The bean is part of a thread, either as a parent or child.
            // First check whether it's a parent, and if so, get the creation
            // date of the newest child
            List<DiscrepancyNoteBean> childNoteBeans = discrepancyNoteDAO.findAllByParent(dnBean);

            DiscrepancyNoteBean tempBean;
            int listSize;

            if (!childNoteBeans.isEmpty()) {
                listSize = childNoteBeans.size();
                // get the last child's CreatedDate
                tempBean = childNoteBeans.get(listSize - 1);
                dnBean.setUpdatedDate(tempBean.getCreatedDate());
                continue;
            }

            // The disc bean must be a child of a parent bean. Get the
            // parent bean, then the last child's CreatedDate
            DiscrepancyNoteBean parentBean;
            int parentId = dnBean.getParentDnId();
            if (parentId == 0) {
                // There is no reason to move on if there is not a valid parent
                // id
                continue;
            }
            parentBean = (DiscrepancyNoteBean) discrepancyNoteDAO.findByPK(parentId);

            if (parentBean != null) {
                childNoteBeans = discrepancyNoteDAO.findAllByParent(dnBean);

                if (!childNoteBeans.isEmpty()) {
                    listSize = childNoteBeans.size();
                    // get the last child's CreatedDate
                    tempBean = childNoteBeans.get(listSize - 1);
                    dnBean.setUpdatedDate(tempBean.getCreatedDate());
                }

            }
        }

    }

    private boolean isUnaffiliatedDNote(DiscrepancyNoteBean dnBean, DiscrepancyNoteDAO discrepancyNoteDAO) {

        if (dnBean == null || discrepancyNoteDAO == null)
            return true;

        List<DiscrepancyNoteBean> discrepancyNoteBeans = discrepancyNoteDAO.findAllByParent(dnBean);

        boolean isChildless = discrepancyNoteBeans.isEmpty();

        boolean isOrphan = dnBean.getParentDnId() == 0;

        if (isChildless && isOrphan)
            return true;

        return false;
    }
}
