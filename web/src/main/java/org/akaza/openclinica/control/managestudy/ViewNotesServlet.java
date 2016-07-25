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
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.akaza.openclinica.bean.core.DiscrepancyNoteType;
import org.akaza.openclinica.bean.core.ResolutionStatus;
import org.akaza.openclinica.bean.managestudy.DiscrepancyNoteBean;
import org.akaza.openclinica.bean.managestudy.StudySubjectBean;
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
import org.akaza.openclinica.service.DiscrepancyNotesSummary;
import org.akaza.openclinica.service.managestudy.ViewNotesService;
import org.akaza.openclinica.view.Page;
import org.akaza.openclinica.web.InsufficientPermissionException;
import org.jmesa.facade.TableFacade;
import org.springframework.web.context.support.WebApplicationContextUtils;

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
    private ViewNotesService viewNotesService;

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
        factory.setViewNotesService(resolveViewNotesService());
        //factory.setResolutionStatusIds(resolutionStatusIds);
        TableFacade tf = factory.createTable(request, response);

        Map<String, Map<String, String>> stats = generateDiscrepancyNotesSummary(factory.getNotesSummary());
        Map<String,String> totalMap = generateDiscrepancyNotesTotal(stats);

        int grandTotal = 0;
        for (String typeName: totalMap.keySet()) {
            String total = totalMap.get(typeName);
            grandTotal = total.equals("--") ? grandTotal + 0 : grandTotal + Integer.parseInt(total);
        }
        request.setAttribute("summaryMap", stats);

        tf.setTotalRows(grandTotal);
        String viewNotesHtml = tf.render();

        request.setAttribute("viewNotesHtml", viewNotesHtml);
        String viewNotesURL = this.getPageURL();
        session.setAttribute("viewNotesURL", viewNotesURL);
        String viewNotesPageFileName = this.getPageServletFileName();
        session.setAttribute("viewNotesPageFileName", viewNotesPageFileName);

        request.setAttribute("mapKeys", ResolutionStatus.getMembers());
        request.setAttribute("typeNames", DiscrepancyNoteUtil.getTypeNames());
        request.setAttribute("typeKeys", totalMap);
        request.setAttribute("grandTotal", grandTotal);

        if ("yes".equalsIgnoreCase(fp.getString(PRINT))) {
        	List<DiscrepancyNoteBean> allNotes = factory.findAllNotes(tf);
            request.setAttribute("allNotes", allNotes);
            forwardPage(Page.VIEW_DISCREPANCY_NOTES_IN_STUDY_PRINT);
        } else {
            forwardPage(Page.VIEW_DISCREPANCY_NOTES_IN_STUDY);
        }
    }

    /**
     * @param stats
     * @return
     */
    private Map<String, String> generateDiscrepancyNotesTotal(Map<String, Map<String, String>> stats) {
        Map<String, String> result = new HashMap<String, String>(stats.size());

        int totals[] = new int[DiscrepancyNoteType.list.size() + 1]; // The "invalid" type is not part of this list

        for (String resStatus : stats.keySet()) {
            Map<String, String> dnTypeMap = stats.get(resStatus);
            for (String dnType: dnTypeMap.keySet()) {
                String stringVal = dnTypeMap.get(dnType);
                int val = (stringVal.equals("--") ? 0 : Integer.parseInt(stringVal));
                totals[DiscrepancyNoteType.getByName(dnType).getId()] += val;
            }
        }

        for (int i = 1; i < totals.length; i++) { // Discarding i = 0 ("Invalid" DiscrepancyNoteType)
            String dnType = DiscrepancyNoteType.get(i).getName();
            result.put(dnType, (totals[i] == 0 ? "--" : Integer.toString(totals[i])));
        }

        return result;
    }

    /**
     * @param resolveViewNotesService
     * @return
     */
    private Map<String, Map<String, String>> generateDiscrepancyNotesSummary(DiscrepancyNotesSummary summary) {
        Map<String, Map<String, String>> result = new HashMap<String, Map<String, String>>();
        for (ResolutionStatus resStatus : ResolutionStatus.list) {
            Map<String, String> resStatusMap = new HashMap<String, String>(DiscrepancyNoteType.list.size());
            for (DiscrepancyNoteType dnType : DiscrepancyNoteType.list) {
                int val = summary.getSum(resStatus, dnType);
                resStatusMap.put(dnType.getName(), (val == 0 ? "--" : Integer.toString(val)));
            }
            int acc = summary.getSum(resStatus);
            resStatusMap.put("Total", (acc == 0 ? "--" : Integer.toString(acc)));
            result.put(resStatus.getName(), resStatusMap);
        }
        return result;
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

    protected ViewNotesService resolveViewNotesService() {
        if (viewNotesService == null) {
            viewNotesService = (ViewNotesService) WebApplicationContextUtils.getWebApplicationContext(
                    getServletContext()).getBean("viewNotesService");
        }
        return viewNotesService;

    }

}
