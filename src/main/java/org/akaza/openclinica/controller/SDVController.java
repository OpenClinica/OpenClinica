package org.akaza.openclinica.controller;

import static org.jmesa.facade.TableFacadeFactory.createTableFacade;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
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

import core.org.akaza.openclinica.bean.core.Role;
import core.org.akaza.openclinica.bean.login.StudyUserRoleBean;
import core.org.akaza.openclinica.bean.login.UserAccountBean;
import core.org.akaza.openclinica.bean.managestudy.StudySubjectBean;
import core.org.akaza.openclinica.bean.submit.EventCRFBean;
import core.org.akaza.openclinica.dao.hibernate.StudyDao;
import core.org.akaza.openclinica.domain.datamap.Study;
import io.swagger.annotations.ApiParam;
import org.akaza.openclinica.controller.dto.SdvDTO;
import org.akaza.openclinica.controller.helper.SdvFilterDataBean;
import org.akaza.openclinica.controller.helper.table.SubjectSDVContainer;
import core.org.akaza.openclinica.dao.hibernate.EventCrfDao;
import core.org.akaza.openclinica.dao.managestudy.StudySubjectDAO;
import core.org.akaza.openclinica.domain.datamap.EventCrf;
import core.org.akaza.openclinica.i18n.core.LocaleResolver;
import core.org.akaza.openclinica.i18n.util.ResourceBundleProvider;
import core.org.akaza.openclinica.service.PermissionService;
import org.akaza.openclinica.domain.enumsupport.SdvStatus;
import org.akaza.openclinica.view.Page;
import org.akaza.openclinica.view.StudyInfoPanel;
import core.org.akaza.openclinica.web.table.sdv.SDVUtil;
import core.org.akaza.openclinica.web.table.sdv.SubjectIdSDVFactory;
import org.jmesa.facade.TableFacade;
import org.jmesa.view.html.component.HtmlColumn;
import org.jmesa.view.html.component.HtmlRow;
import org.jmesa.view.html.component.HtmlTable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.propertyeditors.CustomDateEditor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.ServletRequestDataBinder;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;


/**
 * Implement the functionality for displaying a table of Event CRFs for Source Data
 * Verification. This is an autowired, multiaction Controller.
 */
@Controller("sdvController")
public class SDVController {
    protected final Logger logger = LoggerFactory.getLogger(getClass().getName());
    public static final String ORIGINATING_PAGE = "originatingPage";

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

    @Autowired
    private EventCrfDao eventCrfDao;

    @Autowired
    private PermissionService permissionService;

    public SDVController() {
    }

    @RequestMapping("/viewAllSubjectSDVtmp")
    public ModelMap viewAllSubjectHandler(HttpServletRequest request, @RequestParam("studyId") int studyId,
                                          @RequestParam(value = "sdv_restore", required = false) String restore,
                                          HttpServletResponse response) {

        request.setAttribute("studyId", studyId);
        HttpSession session = request.getSession();


        String[] permissionTags = permissionService.getPermissionTagsStringArray(request);
        if (!mayProceed(request)) {
            try {
                response.sendRedirect(request.getContextPath() + "/MainMenu?message=authentication_failed");
            } catch (Exception e) {
                logger.error("Error while redirecting to MainMenu: ", e);
            }
            return null;
        }


        ResourceBundleProvider.updateLocale(LocaleResolver.getLocale(request));
        // Reseting the side info panel set by SecureControler Mantis Issue: 8680.
        // Todo need something to reset panel from all the Spring Controllers
        StudyInfoPanel panel = new StudyInfoPanel();
        panel.reset();
        request.getSession().setAttribute("panel", panel);
        request.setAttribute("iconInfoShownSDV", true);


        ModelMap gridMap = new ModelMap();
        //set up request attributes for sidebar
        //Not necessary when using old page design...
        // setUpSidebar(request);
        boolean showMoreLink = false;
        if (session.getAttribute("tableFacadeRestore") != null && session.getAttribute("tableFacadeRestore") == "false") {
            session.setAttribute("tableFacadeRestore", "true");
            session.setAttribute("sSdvRestore", "false");
            showMoreLink = true;
        } else if (request.getParameter("showMoreLink") != null) {
            showMoreLink = Boolean.parseBoolean(request.getParameter("showMoreLink").toString());
        } else if (session.getAttribute("sdv_showMoreLink") != null) {
            showMoreLink = Boolean.parseBoolean(session.getAttribute("sdv_showMoreLink") + "");
        } else {
            showMoreLink = true;
        }

        if (request.getParameter("studyJustChanged") != null)
            request.setAttribute("studyJustChanged", request.getParameter("studyJustChanged"));
        request.setAttribute("showMoreLink", showMoreLink + "");
        session.setAttribute("sdv_showMoreLink", showMoreLink + "");
        //String restore = (String)request.getAttribute("sdv_restore");
        restore = restore != null && restore.length() > 0 ? restore : "false";
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
        String returnTo = request.getRequestURL().toString() + "?sdv_restore=true&studyId=" + studyId;
        try {
            request.setAttribute("currentPageUrl", URLEncoder.encode(returnTo, "UTF-8"));
        } catch (UnsupportedEncodingException e) {
            logger.error("Encoding exception:" + e);
        }

        String sdvMatrix = sdvUtil.renderEventCRFTableWithLimit(request, studyId, "../", permissionTags);

        gridMap.addAttribute(SUBJECT_SDV_TABLE_ATTRIBUTE, sdvMatrix);
        return gridMap;
    }

    //method = RequestMethod.POST
    // POST method for verifying multiple eventCrfs
    @RequestMapping("/handleSDVPost")
    public String sdvAllSubjectsFormHandler(HttpServletRequest request, HttpServletResponse response, @RequestParam("studyId") int studyId,
                                            @RequestParam("redirection") String redirection, ModelMap model) {

        //The application is POSTing parameters with the name "sdvCheck_" plus the
        //Event CRF id, so the parameter is sdvCheck_534.

        Enumeration paramNames = request.getParameterNames();
        Map<String, String> parameterMap = new HashMap<String, String>();
        String tmpName = "";
        for (; paramNames.hasMoreElements(); ) {
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
            sdvUtil.forwardRequestFromController(request, response, redirection);

        }
        List<Integer> eventCRFIds = sdvUtil.getListOfSdvEventCRFIds(parameterMap.keySet());
        List<Integer> filteredEventCRFIds = new ArrayList<>();
        for (Integer eventCrfId : eventCRFIds) {
            if (hasFormAccess(eventCrfId, request) == true) {
                filteredEventCRFIds.add(eventCrfId);
            }
        }
        if (filteredEventCRFIds.size() == 0) {
            forwardToNoAccessPage(request, response, redirection);
            return null;
        }


        boolean updateCRFs = sdvUtil.setSDVerified(filteredEventCRFIds, getCurrentUser(request).getId(), SdvStatus.VERIFIED);

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

    //Request method for verifying single eventCrf
    @RequestMapping("/handleSDVGet")
    public String sdvOneCRFFormHandler(HttpServletRequest request, HttpServletResponse response, @RequestParam("crfId") int crfId,
                                       @RequestParam("redirection") String redirection, @RequestParam("studyId") int studyId, ModelMap model) {


        if (!mayProceed(request)) {
            try {
                response.sendRedirect(request.getContextPath() + "/MainMenu?message=authentication_failed");
            } catch (Exception e) {
                logger.error("Error while redirecting to MainMenu: ", e);
            }
            return null;
        }

        ArrayList<String> pageMessages = new ArrayList<String>();

        if (hasFormAccess(crfId, request) != true) {
            forwardToNoAccessPage(request, response, redirection);
            return null;
        }


        //For the messages that appear in the left column of the results page

        List<Integer> eventCRFIds = new ArrayList<Integer>();
        eventCRFIds.add(crfId);
        boolean updateCRFs = sdvUtil.setSDVerified(eventCRFIds, getCurrentUser(request).getId(), SdvStatus.VERIFIED);

        if (updateCRFs) {
            pageMessages.add("The Event CRFs have been source data verified.");
            request.setAttribute("sdv_restore", "true");
        } else {

            pageMessages
                    .add("There was a problem with submitting the Event CRF verification to the database. Is it possible that the database system is down temporarily?");

        }
        request.setAttribute("pageMessages", pageMessages);

        //model.addAttribute("allParams",parameterMap);
        //model.addAttribute("verified",updateCRFs);
        try {
            response.sendRedirect(redirection);
        } catch (Exception e) {
            logger.error("Error while redirecting: ", e);
        }

        //The name of the view, as in allSdvResult.jsp
        return null;
    }

    //Request method for un-verifying single eventCrf
    @RequestMapping("/handleSDVRemove")
    public String changeSDVHandler(HttpServletRequest request, HttpServletResponse response, @RequestParam("crfId") int crfId,
                                   @RequestParam("redirection") String redirection) {

        //For the messages that appear in the left column of the results page
        if (hasFormAccess(crfId, request) != true) {
            forwardToNoAccessPage(request, response, redirection);
            return null;
        }

        ArrayList<String> pageMessages = new ArrayList<String>();
        List<Integer> eventCRFIds = new ArrayList<Integer>();
        eventCRFIds.add(crfId);
        boolean updateCRFs = sdvUtil.setSDVerified(eventCRFIds, getCurrentUser(request).getId(), SdvStatus.NOT_VERIFIED);

        if (updateCRFs) {
            pageMessages.add("The application has unset SDV for the Event CRF.");
        } else {
            pageMessages.add("There was a problem with submitting the Event CRF verification to the database. Is it possible that the database system is down temporarily?");
        }

        request.setAttribute("pageMessages", pageMessages);
        request.setAttribute("sdv_restore", "true");

        //model.addAttribute("allParams",parameterMap);
        //model.addAttribute("verified",updateCRFs);
        try {
            response.sendRedirect(redirection);
        } catch (Exception e) {
            logger.error("Error while redirecting: ", e);
        }

        //The name of the view, as in allSdvResult.jsp
        return null;
    }

    private UserAccountBean getCurrentUser(HttpServletRequest request) {
        UserAccountBean ub = (UserAccountBean) request.getSession().getAttribute("userBean");
        return ub;
    }

    private boolean mayProceed(HttpServletRequest request) {
        HttpSession session = request.getSession();
        StudyUserRoleBean currentRole = (StudyUserRoleBean) session.getAttribute("userRole");
        UserAccountBean ub = (UserAccountBean) request.getSession().getAttribute("userBean");
        if (currentRole == null || currentRole.getId() <= 0) {
            if (ub.getId() > 0) {
                currentRole = ub.getRoleByStudy(ub.getActiveStudyId());
                session.setAttribute("userRole", currentRole);
            }
        }
        if (currentRole == null || currentRole.getId() <= 0) {
            logger.error("No role found for user:" + ub.getName());
        }
        Role r = currentRole.getRole();

        if (r.equals(Role.STUDYDIRECTOR) || r.equals(Role.COORDINATOR) || r.equals(Role.MONITOR)) {
            return true;
        }
        return false;
    }

    public boolean hasFormAccess(int ecId, HttpServletRequest request) {
        EventCrf ec = eventCrfDao.findById(ecId);
        return permissionService.hasFormAccess(ec, ec.getFormLayout().getFormLayoutId(), ec.getStudyEvent().getStudyEventId(), request);
    }

    private void forwardToNoAccessPage(HttpServletRequest request, HttpServletResponse response, String originatingPage) {
        Page page1 = Page.valueOf(Page.NO_ACCESS.name());
        String temp = page1.getFileName();
        request.setAttribute(ORIGINATING_PAGE, originatingPage);
        sdvUtil.forwardRequestFromController(request, response, temp);
    }
}
