package org.akaza.openclinica.controller;

import static org.jmesa.facade.TableFacadeFactory.createTableFacade;

import org.akaza.openclinica.bean.core.Role;
import org.akaza.openclinica.bean.login.StudyUserRoleBean;
import org.akaza.openclinica.bean.login.UserAccountBean;
import org.akaza.openclinica.bean.managestudy.StudyBean;
import org.akaza.openclinica.bean.managestudy.StudySubjectBean;
import org.akaza.openclinica.bean.submit.EventCRFBean;
import org.akaza.openclinica.controller.helper.SdvFilterDataBean;
import org.akaza.openclinica.controller.helper.table.SubjectSDVContainer;
import org.akaza.openclinica.dao.managestudy.StudyDAO;
import org.akaza.openclinica.dao.managestudy.StudySubjectDAO;
import org.akaza.openclinica.i18n.core.LocaleResolver;
import org.akaza.openclinica.i18n.util.ResourceBundleProvider;
import org.akaza.openclinica.view.StudyInfoPanel;
import org.akaza.openclinica.web.table.sdv.SDVUtil;
import org.akaza.openclinica.web.table.sdv.SubjectIdSDVFactory;
import org.jmesa.facade.TableFacade;
import org.jmesa.view.html.component.HtmlColumn;
import org.jmesa.view.html.component.HtmlRow;
import org.jmesa.view.html.component.HtmlTable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.propertyeditors.CustomDateEditor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.ServletRequestDataBinder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.sql.DataSource;


/**
 * Implement the functionality for displaying a table of Event CRFs for Source Data
 * Verification. This is an autowired, multiaction Controller.
 */
@Controller("sdvController")
public class SDVController {
    public final static String SUBJECT_SDV_TABLE_ATTRIBUTE = "sdvTableAttribute";
    @Autowired
    @Qualifier("dataSource")
    private DataSource dataSource;

    @Autowired
    @Qualifier("sdvUtil")
    private SDVUtil sdvUtil;

    @Autowired
    @Qualifier("sdvFactory")
    private SubjectIdSDVFactory sdvFactory;

    //Autowire the class that handles the sidebar structure with a configured
    //bean named "sidebarInit"
    @Autowired
    @Qualifier("sidebarInit")
    private SidebarInit sidebarInit;

    public SDVController() {
    }

    @RequestMapping("/viewSubjectAggregate")
    public ModelMap viewSubjectAggregateHandler(HttpServletRequest request, HttpServletResponse response, @RequestParam("studyId") int studyId) {
		if(!mayProceed(request)){
            try{
                response.sendRedirect(request.getContextPath() + "/MainMenu?message=authentication_failed");
            }catch (Exception e){
                e.printStackTrace();
            }
            return null;
        }
        ModelMap gridMap = new ModelMap();
        HttpSession session = request.getSession();
        boolean showMoreLink = false;
        if(session.getAttribute("sSdvRestore") != null && session.getAttribute("sSdvRestore") == "false") {
            session.setAttribute("sSdvRestore", "true");
            showMoreLink = true;
        }else if(request.getParameter("showMoreLink")!=null){
            showMoreLink = Boolean.parseBoolean(request.getParameter("showMoreLink").toString());
        }else if(session.getAttribute("s_sdv_showMoreLink")!=null) {
            showMoreLink = Boolean.parseBoolean(session.getAttribute("s_sdv_showMoreLink")+"");
        }else {
            showMoreLink = true;
        }
        request.setAttribute("showMoreLink", showMoreLink+"");
        session.setAttribute("s_sdv_showMoreLink", showMoreLink+"");

        request.setAttribute("studyId", studyId);
        String restore = (String)request.getAttribute("s_sdv_restore");
        restore = restore != null && restore.length()>0 ? restore : "false";
        request.setAttribute("s_sdv_restore", restore);
        // request.setAttribute("studySubjectId",studySubjectId);
        /*SubjectIdSDVFactory tableFactory = new SubjectIdSDVFactory();
        * @RequestParam("studySubjectId") int studySubjectId,*/
        request.setAttribute("imagePathPrefix", "../");

        ArrayList<String> pageMessages = (ArrayList<String>) request.getAttribute("pageMessages");
        if (pageMessages == null) {
            pageMessages = new ArrayList<String>();
        }

        request.setAttribute("pageMessages", pageMessages);
        sdvFactory.showMoreLink = showMoreLink;
        TableFacade facade = sdvFactory.createTable(request, response);
        String sdvMatrix = facade.render();
        gridMap.addAttribute(SUBJECT_SDV_TABLE_ATTRIBUTE, sdvMatrix);
        return gridMap;
    }

    @RequestMapping("/viewAllSubjectSDV")
    public ModelMap viewSubjectHandler(HttpServletRequest request, @RequestParam("studySubjectId") int studySubjectId, @RequestParam("studyId") int studyId) {

        ModelMap gridMap = new ModelMap();
        /*EventCRFDAO eventCRFDAO = new EventCRFDAO(dataSource);
        List<EventCRFBean> eventCRFBeans = eventCRFDAO.findAllByStudySubject(studySubjectId);*/

        request.setAttribute("studyId", studyId);
        request.setAttribute("studySubjectId", studySubjectId);
        //  request.setAttribute("isViewSubjectRequest","y");
        request.setAttribute("imagePathPrefix", "../");

        ArrayList<String> pageMessages = (ArrayList<String>) request.getAttribute("pageMessages");
        if (pageMessages == null) {
            pageMessages = new ArrayList<String>();
        }

        request.setAttribute("pageMessages", pageMessages);

        String sdvMatrix = sdvUtil.renderSubjectsTableWithLimit(request, studyId, studySubjectId);
        gridMap.addAttribute(SUBJECT_SDV_TABLE_ATTRIBUTE, sdvMatrix);
        return gridMap;
    }

    @RequestMapping("/viewAllSubjectSDVtmp")
    public ModelMap viewAllSubjectHandler(HttpServletRequest request, @RequestParam("studyId") int studyId, HttpServletResponse response) {

        if(!mayProceed(request)){
            try{
                response.sendRedirect(request.getContextPath() + "/MainMenu?message=authentication_failed");
            }catch (Exception e){
                e.printStackTrace();
            }
            return null;
        }
    ResourceBundleProvider.updateLocale(LocaleResolver.getLocale(request));
        // Reseting the side info panel set by SecureControler Mantis Issue: 8680.
        // Todo need something to reset panel from all the Spring Controllers
        StudyInfoPanel panel = new StudyInfoPanel();
        panel.reset();
        HttpSession session = request.getSession();
        request.getSession().setAttribute("panel", panel);

        ModelMap gridMap = new ModelMap();
        //set up request attributes for sidebar
        //Not necessary when using old page design...
        // setUpSidebar(request);
        boolean showMoreLink = false;
        if(session.getAttribute("tableFacadeRestore") != null && session.getAttribute("tableFacadeRestore") == "false") {
            session.setAttribute("tableFacadeRestore","true");
            session.setAttribute("sSdvRestore", "false");
            showMoreLink = true;
        }else if(request.getParameter("showMoreLink")!=null){
            showMoreLink = Boolean.parseBoolean(request.getParameter("showMoreLink").toString());
        }else if(session.getAttribute("sdv_showMoreLink")!=null) {
            showMoreLink = Boolean.parseBoolean(session.getAttribute("sdv_showMoreLink")+"");
        } else {
            showMoreLink = true;
        }
        request.setAttribute("showMoreLink", showMoreLink+"");
        session.setAttribute("sdv_showMoreLink", showMoreLink+"");
        request.setAttribute("studyId", studyId);
        String restore = (String)request.getAttribute("sdv_restore");
        restore = restore != null && restore.length()>0 ? restore : "false";
        request.setAttribute("sdv_restore", restore);
        //request.setAttribute("imagePathPrefix","../");
        //We need a study subject id for the first tab;
        Integer studySubjectId = (Integer) request.getAttribute("studySubjectId");
        studySubjectId = studySubjectId == null || studySubjectId == 0 ? 0 : studySubjectId;
        request.setAttribute("studySubjectId", studySubjectId);

        //set up the elements for the view's filter box
        // sdvUtil.prepareSDVSelectElements(request,studyBean);

        ArrayList<String> pageMessages = (ArrayList<String>) request.getAttribute("pageMessages");
        if (pageMessages == null) {
            pageMessages = new ArrayList<String>();
        }

        request.setAttribute("pageMessages", pageMessages);

        String sdvMatrix = sdvUtil.renderEventCRFTableWithLimit(request, studyId, "../");

        gridMap.addAttribute(SUBJECT_SDV_TABLE_ATTRIBUTE, sdvMatrix);
        return gridMap;
    }

    @RequestMapping("/viewAllSubjectSDVform")
    public ModelMap viewAllSubjectFormHandler(HttpServletRequest request, HttpServletResponse response, @RequestParam("studyId") int studyId) {

        ModelMap gridMap = new ModelMap();
        StudyDAO studyDAO = new StudyDAO(dataSource);
        // StudyEventDAO studyEventDAO = new StudyEventDAO(dataSource);
        StudyBean studyBean = (StudyBean) studyDAO.findByPK(studyId);
        String pattern = "MM/dd/yyyy";
        SimpleDateFormat sdf = new SimpleDateFormat(pattern);

        //  List<StudyEventBean> studyEventBeans = studyEventDAO.findAllByStudy(studyBean);
        //  List<EventCRFBean> eventCRFBeans = sdvUtil.getAllEventCRFs(studyEventBeans);

        //set up the parameters to take part in filtering
        ServletRequestDataBinder dataBinder = new ServletRequestDataBinder(new SdvFilterDataBean());
        dataBinder.setAllowedFields(new String[] { "study_subject_id", "studyEventDefinition", "studyEventStatus", "eventCRFStatus", "sdvRequirement",
            "eventcrfSDVStatus", "startUpdatedDate", "endDate", "eventCRFName" });

        dataBinder.registerCustomEditor(java.util.Date.class, new CustomDateEditor(sdf, true));
        dataBinder.bind(request);
        BindingResult bindingResult = dataBinder.getBindingResult();
        //  eventCRFBeans = sdvUtil.filterEventCRFs(eventCRFBeans,bindingResult);

        //set up request attributes for sidebar
        //Not necessary when using old page design...
        // setUpSidebar(request);

        request.setAttribute("studyId", studyId);
        //We need a study subject id for the first tab; take it somewhat arbitrarily from the first study event bean

        /* int studySubjectId = 0;

        StudyEventBean studyBeanUrl = studyEventBeans.get(0);
        if(studyBeanUrl != null) {
            studySubjectId= studyBeanUrl.getStudySubjectId();
        }
        request.setAttribute("studySubjectId",studySubjectId);*/

        //set up the elements for the view's filter box
        /*sdvUtil.prepareSDVSelectElements(request,studyBean);*/

        ArrayList<String> pageMessages = (ArrayList<String>) request.getAttribute("pageMessages");
        if (pageMessages == null) {
            pageMessages = new ArrayList<String>();
        }

        request.setAttribute("pageMessages", pageMessages);
        String sdvMatrix = sdvUtil.renderEventCRFTableWithLimit(request, studyId, "");
        gridMap.addAttribute(SUBJECT_SDV_TABLE_ATTRIBUTE, sdvMatrix);
        return gridMap;
    }

    /*  @RequestMapping("/viewSubjectAggregateSDV")
    public ModelMap viewSubjectAggregateHandler(HttpServletRequest request,
                                                @RequestParam("studyId") int studyId) {

        ModelMap gridMap = new ModelMap();

        //set up request attributes for sidebar
        setUpSidebar(request);
        String sdvMatrix = sdvUtil.renderSubjectsAggregateTable(studyId,request);
        gridMap.addAttribute(SUBJECT_SDV_TABLE_ATTRIBUTE,sdvMatrix);
        return gridMap;
    }*/

    //method = RequestMethod.POST
    @RequestMapping("/handleSDVPost")
    public String sdvAllSubjectsFormHandler(HttpServletRequest request, HttpServletResponse response, @RequestParam("studyId") int studyId,
            @RequestParam("redirection") String redirection, ModelMap model) {

        //The application is POSTing parameters with the name "sdvCheck_" plus the
        //Event CRF id, so the parameter is sdvCheck_534.

        Enumeration paramNames = request.getParameterNames();
        Map<String, String> parameterMap = new HashMap<String, String>();
        String tmpName = "";
        for (; paramNames.hasMoreElements();) {
            tmpName = (String) paramNames.nextElement();
            if (tmpName.contains(SDVUtil.CHECKBOX_NAME)) {
                parameterMap.put(tmpName, request.getParameter(tmpName));
            }
        }
        request.setAttribute("sdv_restore", "true");

        //For the messages that appear in the left column of the results page
        ArrayList<String> pageMessages = new ArrayList<String>();

        //In this case, no checked event CRFs were submitted
        if (parameterMap.isEmpty()) {
            pageMessages.add("None of the Event CRFs were selected for SDV.");
            request.setAttribute("pageMessages", pageMessages);
            sdvUtil.forwardRequestFromController(request, response, "/pages/" + redirection);

        }
        List<Integer> eventCRFIds = sdvUtil.getListOfSdvEventCRFIds(parameterMap.keySet());
        boolean updateCRFs = sdvUtil.setSDVerified(eventCRFIds, getCurrentUser(request).getId(), true);

        if (updateCRFs) {
            pageMessages.add("The Event CRFs have been source data verified.");
        } else {

            pageMessages
                    .add("There was a problem with submitting the Event CRF verification to the database. Is it possible that the database system is down temporarily?");

        }
        request.setAttribute("pageMessages", pageMessages);

        //model.addAttribute("allParams",parameterMap);
        //model.addAttribute("verified",updateCRFs);
        sdvUtil.forwardRequestFromController(request, response, "/pages/" + redirection);

        //The name of the view, as in allSdvResult.jsp
        return null;

    }

    @RequestMapping("/handleSDVGet")
    public String sdvOneCRFFormHandler(HttpServletRequest request, HttpServletResponse response, @RequestParam("crfId") int crfId,
            @RequestParam("redirection") String redirection, ModelMap model) {


			 if(!mayProceed(request)){
            try{
                response.sendRedirect(request.getContextPath() + "/MainMenu?message=authentication_failed");
            }catch (Exception e){
                e.printStackTrace();
            }
            return null;
        }
        //For the messages that appear in the left column of the results page
        ArrayList<String> pageMessages = new ArrayList<String>();

        List<Integer> eventCRFIds = new ArrayList<Integer>();
        eventCRFIds.add(crfId);
        boolean updateCRFs = sdvUtil.setSDVerified(eventCRFIds, getCurrentUser(request).getId(), true);

        if (updateCRFs) {
            pageMessages.add("The Event CRFs have been source data verified.");
        } else {

            pageMessages
                    .add("There was a problem with submitting the Event CRF verification to the database. Is it possible that the database system is down temporarily?");

        }
        request.setAttribute("pageMessages", pageMessages);

        request.setAttribute("sdv_restore", "true");

        //model.addAttribute("allParams",parameterMap);
        //model.addAttribute("verified",updateCRFs);
        sdvUtil.forwardRequestFromController(request, response, "/pages/" + redirection);

        //The name of the view, as in allSdvResult.jsp
        return null;

    }

    @RequestMapping("/handleSDVRemove")
    public String changeSDVHandler(HttpServletRequest request, HttpServletResponse response, @RequestParam("crfId") int crfId,
            @RequestParam("redirection") String redirection, ModelMap model) {

        //For the messages that appear in the left column of the results page
        ArrayList<String> pageMessages = new ArrayList<String>();

        List<Integer> eventCRFIds = new ArrayList<Integer>();
        eventCRFIds.add(crfId);
        boolean updateCRFs = sdvUtil.setSDVerified(eventCRFIds, getCurrentUser(request).getId(), false);

        if (updateCRFs) {
            pageMessages.add("The application has unset SDV for the Event CRF.");
        } else {

            pageMessages
                    .add("There was a problem with submitting the Event CRF verification to the database. Is it possible that the database system is down temporarily?");

        }
        request.setAttribute("pageMessages", pageMessages);
        request.setAttribute("sdv_restore", "true");

        //model.addAttribute("allParams",parameterMap);
        //model.addAttribute("verified",updateCRFs);
        sdvUtil.forwardRequestFromController(request, response, "/pages/" + redirection);

        //The name of the view, as in allSdvResult.jsp
        return null;

    }

    @RequestMapping("/sdvStudySubject")
    public String sdvStudySubjectHandler(HttpServletRequest request, HttpServletResponse response, @RequestParam("theStudySubjectId") int studySubjectId,
            @RequestParam("redirection") String redirection, ModelMap model) {

        //For the messages that appear in the left column of the results page
        ArrayList<String> pageMessages = new ArrayList<String>();

        List<Integer> studySubjectIds = new ArrayList<Integer>();
        studySubjectIds.add(studySubjectId);
        boolean updateCRFs = sdvUtil.setSDVStatusForStudySubjects(studySubjectIds, getCurrentUser(request).getId(), true);

        if (updateCRFs) {
            pageMessages.add("The Subject has been source data verified.");
        } else {

            pageMessages
                    .add("There was a problem with submitting the Event CRF verification to the database. Is it possible that the database system is down temporarily?");

        }
        request.setAttribute("pageMessages", pageMessages);
        request.setAttribute("s_sdv_restore", "true");
        sdvUtil.forwardRequestFromController(request, response, "/pages/" + redirection);
        return null;
    }

    @RequestMapping("/unSdvStudySubject")
    public String unSdvStudySubjectHandler(HttpServletRequest request, HttpServletResponse response, @RequestParam("theStudySubjectId") int studySubjectId,
            @RequestParam("redirection") String redirection, ModelMap model) {

        ArrayList<String> pageMessages = new ArrayList<String>();
        List<Integer> studySubjectIds = new ArrayList<Integer>();

        studySubjectIds.add(studySubjectId);
        boolean updateCRFs = sdvUtil.setSDVStatusForStudySubjects(studySubjectIds, getCurrentUser(request).getId(), false);

        if (updateCRFs) {
            pageMessages.add("The application has unset SDV for the Event CRF.");
        } else {
            pageMessages
                    .add("There was a problem with submitting the Event CRF verification to the database. Is it possible that the database system is down temporarily?");
        }
        request.setAttribute("pageMessages", pageMessages);
        request.setAttribute("s_sdv_restore", "true");
        sdvUtil.forwardRequestFromController(request, response, "/pages/" + redirection);
        return null;

    }

    @RequestMapping("/sdvStudySubjects")
    public String sdvStudySubjectsHandler(HttpServletRequest request, HttpServletResponse response, @RequestParam("studyId") int studyId,
            @RequestParam("redirection") String redirection, ModelMap model) {

        //The application is POSTing parameters with the name "sdvCheck_" plus the
        //Event CRF id, so the parameter is sdvCheck_534.

        Enumeration paramNames = request.getParameterNames();
        Map<String, String> parameterMap = new HashMap<String, String>();
        String tmpName = "";
        for (; paramNames.hasMoreElements();) {
            tmpName = (String) paramNames.nextElement();
            if (tmpName.contains(SDVUtil.CHECKBOX_NAME)) {
                parameterMap.put(tmpName, request.getParameter(tmpName));
            }
        }
        request.setAttribute("s_sdv_restore", "true");

        //For the messages that appear in the left column of the results page
        ArrayList<String> pageMessages = new ArrayList<String>();

        //In this case, no checked event CRFs were submitted
        if (parameterMap.isEmpty()) {
            pageMessages.add("None of the Study Subjects were selected for SDV.");
            request.setAttribute("pageMessages", pageMessages);
            sdvUtil.forwardRequestFromController(request, response, "/pages/" + redirection);

        }
        List<Integer> studySubjectIds = sdvUtil.getListOfStudySubjectIds(parameterMap.keySet());
        boolean updateCRFs = sdvUtil.setSDVStatusForStudySubjects(studySubjectIds, getCurrentUser(request).getId(), true);

        if (updateCRFs) {
            pageMessages.add("The Event CRFs have been source data verified.");
        } else {

            pageMessages
                    .add("There was a problem with submitting the Event CRF verification to the database. Is it possible that the database system is down temporarily?");

        }
        request.setAttribute("pageMessages", pageMessages);

        //model.addAttribute("allParams",parameterMap);
        //model.addAttribute("verified",updateCRFs);
        sdvUtil.forwardRequestFromController(request, response, "/pages/" + redirection);

        //The name of the view, as in allSdvResult.jsp
        return null;

    }

    /*
    Create a JMesa-based table for showing the event CRFs.
    */
    private String renderSubjectsTable(List<EventCRFBean> eventCRFBeans, int studySubjectId, HttpServletRequest request) {

        StudySubjectDAO studySubjectDAO = new StudySubjectDAO(dataSource);
        StudySubjectBean subjectBean = (StudySubjectBean) studySubjectDAO.findByPK(studySubjectId);

        Collection<SubjectSDVContainer> items = sdvUtil.getSubjectRows(eventCRFBeans, request);

        //The number of items represents the total number of returned rows
        int totalRowCount = 0;
        if (items != null && items.size() > 0) {
            totalRowCount = items.size();
        }

        TableFacade tableFacade = createTableFacade("sdv", request);
        //The default display for the JMesa Limit select widget is 1,50,100 rows
        //We'll change this if the subject has more than one row, and have the last choice
        //set to the total row count
        if (totalRowCount > 1) {
            tableFacade.setMaxRowsIncrements(15, 50, totalRowCount);
        }
        tableFacade.setColumnProperties("studySubjectId", "personId", "secondaryId", "eventName", "eventDate", "enrollmentDate", "subjectStatus",
                "crfNameVersion", "crfStatus", "lastUpdatedDate", "lastUpdatedBy", "sdvStatusActions");

        tableFacade.setItems(items);
        //Fix column titles
        HtmlTable table = (HtmlTable) tableFacade.getTable();
        //i18n caption; TODO: convert to Spring messages
        ResourceBundle resourceBundle = ResourceBundle.getBundle("org.akaza.openclinica.i18n.words", LocaleResolver.getLocale(request));

        String[] allTitles =
            { resourceBundle.getString("study_subject_ID"), resourceBundle.getString("person_ID"), resourceBundle.getString("secondary_ID"),
              resourceBundle.getString("event_name"), resourceBundle.getString("event_date"), resourceBundle.getString("enrollment_date"),
              resourceBundle.getString("subject_status"),resourceBundle.getString("CRF_name")+" / "+resourceBundle.getString("version"),
              resourceBundle.getString("view_CRF"),resourceBundle.getString("last_updated_date"), resourceBundle.getString("last_updated_by"),
              resourceBundle.getString("SDV_status")+" / "+resourceBundle.getString("actions") };

        setTitles(allTitles, table);

        table.getTableRenderer().setWidth("800");
        return tableFacade.render();
    }

    /* Create the titles for the HTML table's rows */
    private void setTitles(String[] allTitles, HtmlTable table) {
        HtmlRow row = table.getRow();
        HtmlColumn tempColumn = null;

        for (int i = 0; i < allTitles.length; i++) {
            tempColumn = row.getColumn(i);
            tempColumn.setTitle(allTitles[i]);
        }

    }

    private void setUpSidebar(HttpServletRequest request) {
        if (sidebarInit.getAlertsBoxSetup() == SidebarEnumConstants.OPENALERTS) {
            request.setAttribute("alertsBoxSetup", true);
        }

        if (sidebarInit.getInfoBoxSetup() == SidebarEnumConstants.OPENINFO) {
            request.setAttribute("infoBoxSetup", true);
        }
        if (sidebarInit.getInstructionsBoxSetup() == SidebarEnumConstants.OPENINSTRUCTIONS) {
            request.setAttribute("instructionsBoxSetup", true);
        }

        if (!(sidebarInit.getEnableIconsBoxSetup() == SidebarEnumConstants.DISABLEICONS)) {
            request.setAttribute("enableIconsBoxSetup", true);
        }

    }

    private UserAccountBean getCurrentUser (HttpServletRequest request){
        UserAccountBean ub = (UserAccountBean)request.getSession().getAttribute("userBean");
        return ub;
    }

    public static void main(String[] args) throws ParseException {

        String pattern = "MM/dd/yyyy";
        SimpleDateFormat sdf = new SimpleDateFormat(pattern);

        Date date = sdf.parse("01/01/2007");
        System.out.println("date = " + date);

    }

	 private boolean mayProceed(HttpServletRequest request) {
        StudyUserRoleBean currentRole = (StudyUserRoleBean)request.getSession().getAttribute("userRole");
        Role r = currentRole.getRole();

        if (r.equals(Role.STUDYDIRECTOR) || r.equals(Role.COORDINATOR) || r.equals(Role.MONITOR)) {
            return true;
        }

        return false;
    }
}
