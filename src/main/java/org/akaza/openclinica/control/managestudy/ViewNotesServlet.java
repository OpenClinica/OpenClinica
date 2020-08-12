package org.akaza.openclinica.control.managestudy;

import core.org.akaza.openclinica.bean.core.DiscrepancyNoteType;
import core.org.akaza.openclinica.bean.core.ResolutionStatus;
import core.org.akaza.openclinica.bean.managestudy.CustomColumn;
import core.org.akaza.openclinica.bean.managestudy.DiscrepancyNoteBean;
import core.org.akaza.openclinica.bean.managestudy.StudySubjectBean;
import core.org.akaza.openclinica.dao.hibernate.*;
import core.org.akaza.openclinica.dao.login.UserAccountDAO;
import core.org.akaza.openclinica.dao.managestudy.*;
import core.org.akaza.openclinica.dao.service.StudyParameterValueDAO;
import core.org.akaza.openclinica.dao.submit.EventCRFDAO;
import core.org.akaza.openclinica.dao.submit.SubjectDAO;
import core.org.akaza.openclinica.dao.submit.SubjectGroupMapDAO;
import core.org.akaza.openclinica.service.DiscrepancyNoteUtil;
import core.org.akaza.openclinica.service.DiscrepancyNotesSummary;
import core.org.akaza.openclinica.service.PermissionService;
import core.org.akaza.openclinica.service.managestudy.ViewNotesService;
import core.org.akaza.openclinica.web.InsufficientPermissionException;
import org.akaza.openclinica.control.SpringServletAccess;
import org.akaza.openclinica.control.core.SecureController;
import org.akaza.openclinica.control.form.FormProcessor;
import org.akaza.openclinica.control.submit.ListNotesTableFactory;
import org.akaza.openclinica.control.submit.SubmitDataUtil;
import org.akaza.openclinica.service.Component;
import org.akaza.openclinica.service.UserService;
import org.akaza.openclinica.service.ViewStudySubjectService;
import org.akaza.openclinica.view.Page;
import org.apache.commons.lang3.StringUtils;
import org.jmesa.facade.TableFacade;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.context.support.WebApplicationContextUtils;

import java.util.*;

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

    @Autowired
    private EventCRFDAO eventCRFDAO;
    @Autowired
    private StudyEventDefinitionDAO studyEventDefinitionDAO;
    @Autowired
    private ViewStudySubjectService viewStudySubjectService;
    @Autowired
    private PermissionService permissionService;
    @Autowired
    private ItemDao itemDao;
    @Autowired
    private ItemDataDao itemDataDao;
    @Autowired
    private ItemFormMetadataDao itemFormMetadataDao;
    @Autowired
    private EventCrfDao eventCrfDao;
    @Autowired
    private StudyEventDao studyEventDao;
    @Autowired
    private CrfDao crfDao;
    @Autowired
    private CrfVersionDao crfVersionDao;
    @Autowired
    private EventDefinitionCrfDao eventDefinitionCrfDao;
    @Autowired
    private StudyEventDefinitionDao studyEventDefinitionDao;
    @Autowired
    private EventDefinitionCrfPermissionTagDao permissionTagDao;

    /*
     * (non-Javadoc)
     *
     * @see org.akaza.openclinica.control.core.SecureController#processRequest()
     */
    @Override
    protected void processRequest() throws Exception {
        String module = request.getParameter("module");
        String moduleStr = "manage";
        String study_oid = request.getParameter("study_oid");
        if (!StringUtils.isEmpty(study_oid)) {
            changeStudy(study_oid);
        }
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
        if (fp.getString("showMoreLink").equals("")) {
            showMoreLink = true;
        } else {
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

        boolean hasAResolutionStatus = resolutionStatus >= 1 && resolutionStatus <= 6;
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
        SubjectDAO sdao = new SubjectDAO(sm.getDataSource());

        UserAccountDAO uadao = new UserAccountDAO(sm.getDataSource());
        EventDefinitionCRFDAO eventDefinitionCRFDao = new EventDefinitionCRFDAO(sm.getDataSource());

        ListNotesTableFactory factory = new ListNotesTableFactory(showMoreLink, getPermissionTagsList());
        factory.setSubjectDao(sdao);
        factory.setStudySubjectDao(subdao);
        factory.setUserAccountDao(uadao);
        factory.setCurrentStudy(currentStudy);
        factory.setDiscrepancyNoteDao(dndao);
        factory.setCrfDao(crfDao);
        factory.setCrfVersionDao(crfVersionDao);
        factory.setStudyEventDao(studyEventDao);
        factory.setStudyEventDefinitionDao(studyEventDefinitionDAO);
        factory.setEventDefinitionCRFDao(eventDefinitionCRFDao);
        factory.setEventCRFDao(eventCRFDAO);
        factory.setModule(moduleStr);
        factory.setDiscNoteType(discNoteType);
        factory.setResolutionStatus(resolutionStatus);
        factory.setViewNotesService(resolveViewNotesService());

        factory.setViewStudySubjectService(viewStudySubjectService);
        factory.setPermissionService(getPermissionService());
        factory.setItemDao(itemDao);
        factory.setItemDataDao(itemDataDao);
        factory.setEventCrfDao(eventCrfDao);
        factory.setEventDefinitionCrfDao(eventDefinitionCrfDao);
        factory.setItemFormMetadataDao(itemFormMetadataDao);
        factory.setPermissionTagDao(permissionTagDao);
        factory.setStudyEventDefinitionHibDao(studyEventDefinitionDao);

        List<Component> components = viewStudySubjectService.getPageComponents(ListNotesTableFactory.PAGE_NAME);
        if (components != null) {
            for (Component component : components) {
                if (component.getColumns() != null) {
                    List<String> permissionTags = permissionService.getPermissionTagsList(request);
                    request.getSession().setAttribute("userPermissionTags", permissionTags);
                    break;
                }
            }
        }


        // factory.setResolutionStatusIds(resolutionStatusIds);
        TableFacade tf = factory.createTable(request, response);
        Map<String, Map<String, String>> stats = generateDiscrepancyNotesSummary(factory.getNotesSummary());
        Map<String, String> totalSummary = generateDiscrepancyNotesTotal(stats);
        Map<String, String> totalMap = generateDiscrepancyNotesTotal(generateDiscrepancyNotesSummary(factory.getNotesSummary()));

        int grandTotal = 0;
        for (String typeName : totalMap.keySet()) {
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


        int columnCount = factory.getNetCountCustomColumns(currentStudy, request);
        if ("yes".equalsIgnoreCase(fp.getString(PRINT))) {
            List<DiscrepancyNoteBean> allNotes = factory.findAllNotes(tf);
            for (DiscrepancyNoteBean note : allNotes) {
                if (note.getEntityType().equals(ListNotesTableFactory.ITEM_DATA)) {
                    note.setCustomColumns(factory.getCustomColumns(note, currentStudy, request));
                } else if (note.getEntityType().equals(ListNotesTableFactory.STUDY_EVENT)) {
                    List<CustomColumn> customColumns = new ArrayList<>();
                    for (int i = 0; i < columnCount; i++) {
                        CustomColumn customColumn = new CustomColumn();
                        customColumns.add(customColumn);
                    }
                    note.setCustomColumns(customColumns);
                }
            }
            request.setAttribute("allNotes", allNotes);
            forwardPage(Page.VIEW_DISCREPANCY_NOTES_IN_STUDY_PRINT);
        } else {
            getEventCrfLocker().unlockAllForUser(ub.getId());
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
            for (String dnType : dnTypeMap.keySet()) {
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
        if (SubmitDataUtil.mayViewData(ub, currentRole)) {
            return;
        }

        addPageMessage(respage.getString("no_permission_to_view_discrepancies") + respage.getString("change_study_contact_sysadmin"));
        throw new InsufficientPermissionException(Page.MENU_SERVLET, resexception.getString("not_study_director_or_study_cordinator"), "1");
    }

    protected ViewNotesService resolveViewNotesService() {
        if (viewNotesService == null) {
            viewNotesService = (ViewNotesService) WebApplicationContextUtils.getWebApplicationContext(getServletContext()).getBean("viewNotesService");
        }
        return viewNotesService;

    }

}
