package org.akaza.openclinica.controller;

import core.org.akaza.openclinica.bean.core.Role;
import core.org.akaza.openclinica.bean.login.StudyUserRoleBean;
import core.org.akaza.openclinica.bean.login.UserAccountBean;
import core.org.akaza.openclinica.bean.service.StudyParameterValueBean;
import core.org.akaza.openclinica.dao.admin.CRFDAO;
import core.org.akaza.openclinica.dao.core.CoreResources;
import core.org.akaza.openclinica.dao.hibernate.StudyDao;
import core.org.akaza.openclinica.dao.hibernate.StudyModuleStatusDao;
import core.org.akaza.openclinica.dao.login.UserAccountDAO;
import core.org.akaza.openclinica.dao.managestudy.EventDefinitionCRFDAO;
import core.org.akaza.openclinica.dao.managestudy.StudyEventDefinitionDAO;
import core.org.akaza.openclinica.dao.managestudy.StudyGroupClassDAO;
import core.org.akaza.openclinica.dao.rule.RuleDAO;
import core.org.akaza.openclinica.dao.service.StudyParameterValueDAO;
import core.org.akaza.openclinica.domain.Status;
import core.org.akaza.openclinica.domain.datamap.Study;
import core.org.akaza.openclinica.domain.managestudy.StudyModuleStatus;
import core.org.akaza.openclinica.i18n.core.LocaleResolver;
import core.org.akaza.openclinica.i18n.util.ResourceBundleProvider;
import core.org.akaza.openclinica.service.pmanage.Authorization;
import core.org.akaza.openclinica.service.pmanage.ParticipantPortalRegistrar;
import core.org.akaza.openclinica.service.pmanage.RandomizationRegistrar;
import core.org.akaza.openclinica.service.pmanage.SeRandomizationDTO;
import core.org.akaza.openclinica.service.rule.RuleSetServiceInterface;
import org.akaza.openclinica.view.StudyInfoPanel;
import org.apache.commons.dbcp2.BasicDataSource;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.web.HttpSessionRequiredException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.support.SessionStatus;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;

/**
 * @author: sshamim Date: Jan 22, 2009 Time: 6:52:16 PM Manages the Study creation process
 */
@Controller("studyModuleController") @RequestMapping("/studymodule") @SessionAttributes("studyModuleStatus") public class StudyModuleController {
    public static final String REG_MESSAGE = "regMessages";
    public static ResourceBundle respage;
    protected final Logger logger = LoggerFactory.getLogger(getClass().getName());
    @Autowired CoreResources coreResources;
    @Autowired @Qualifier("sidebarInit") private SidebarInit sidebarInit;
    @Autowired @Qualifier("studyModuleStatusDao") private StudyModuleStatusDao studyModuleStatusDao;
    @Autowired @Qualifier("ruleSetService") private RuleSetServiceInterface ruleSetService;
    @Autowired @Qualifier("dataSource") private BasicDataSource dataSource;
    private EventDefinitionCRFDAO eventDefinitionCRFDao;
    private StudyEventDefinitionDAO studyEventDefinitionDao;
    private CRFDAO crfDao;
    private StudyGroupClassDAO studyGroupClassDao;
    private UserAccountDAO userDao;
    private core.org.akaza.openclinica.dao.rule.RuleDAO ruleDao;
    @Autowired private JavaMailSenderImpl mailSender;
    @Autowired
    private StudyDao studyDao;
    public StudyModuleController() {

    }

    @RequestMapping(value = "/{study}/deactivate", method = RequestMethod.GET) public String deactivateParticipate(@PathVariable("study") String studyOid,
            HttpServletRequest request) throws Exception {
        Study study = studyDao.findByOcOID(studyOid);
        StudyParameterValueDAO spvdao = new StudyParameterValueDAO(dataSource);
        StudyParameterValueBean spv = spvdao.findByHandleAndStudy(study.getStudyId(), "participantPortal");
        spv.setStudyId(study.getStudyId());
        spv.setParameter("participantPortal");
        spv.setValue("disabled");

        if (spv.getId() > 0)
            spvdao.update(spv);
        else
            spvdao.create(spv);
        Study currentStudy = (Study) request.getSession().getAttribute("study");
        currentStudy.setParticipantPortal("disabled");

        return "redirect:/pages/studymodule";
    }

    @RequestMapping(value = "/{study}/deactivaterandomization", method = RequestMethod.GET) public String deactivateRandomization(
            @PathVariable("study") String studyOid, HttpServletRequest request) throws Exception {
        Study study = studyDao.findByOcOID(studyOid);
        StudyParameterValueDAO spvdao = new StudyParameterValueDAO(dataSource);
        StudyParameterValueBean spv = spvdao.findByHandleAndStudy(study.getStudyId(), "randomization");
        spv.setStudyId(study.getStudyId());
        spv.setParameter("randomization");
        spv.setValue("disabled");

        if (spv.getId() > 0)
            spvdao.update(spv);
        else
            spvdao.create(spv);
        Study currentStudy = (Study) request.getSession().getAttribute("study");
        currentStudy.setRandomization("disabled");

        return "redirect:/pages/studymodule";
    }

    @RequestMapping(value = "/{study}/reactivate", method = RequestMethod.GET) public String reactivateParticipate(@PathVariable("study") String studyOid,
            HttpServletRequest request) throws Exception {
        Study study = studyDao.findByOcOID(studyOid);
        StudyParameterValueDAO spvdao = new StudyParameterValueDAO(dataSource);
        StudyParameterValueBean spv = spvdao.findByHandleAndStudy(study.getStudyId(), "participantPortal");
        spv.setStudyId(study.getStudyId());
        spv.setParameter("participantPortal");
        spv.setValue("enabled");

        if (spv.getId() > 0)
            spvdao.update(spv);
        else
            spvdao.create(spv);
        Study currentStudy = (Study) request.getSession().getAttribute("study");
        currentStudy.setParticipantPortal("enabled");

        return "redirect:/pages/studymodule";
    }

    @RequestMapping(value = "/{study}/reactivaterandomization", method = RequestMethod.GET) public String reactivateRandomization(
            @PathVariable("study") String studyOid, HttpServletRequest request) throws Exception {
        Study study = studyDao.findByOcOID(studyOid);
        StudyParameterValueDAO spvdao = new StudyParameterValueDAO(dataSource);
        StudyParameterValueBean spv = spvdao.findByHandleAndStudy(study.getStudyId(), "randomization");
        spv.setStudyId(study.getStudyId());
        spv.setParameter("randomization");
        spv.setValue("enabled");

        if (spv.getId() > 0)
            spvdao.update(spv);
        else
            spvdao.create(spv);
        Study currentStudy = (Study) request.getSession().getAttribute("study");
        currentStudy.setRandomization("enabled");

        return "redirect:/pages/studymodule";
    }

    @RequestMapping(value = "/{study}/register", method = RequestMethod.POST) public String registerParticipate(@PathVariable("study") String studyOid,
            HttpServletRequest request) throws Exception {
        Study study = studyDao.findByOcOID(studyOid);
        StudyParameterValueDAO spvdao = new StudyParameterValueDAO(dataSource);
        StudyParameterValueBean spv = spvdao.findByHandleAndStudy(study.getStudyId(), "participantPortal");
        ParticipantPortalRegistrar registrar = new ParticipantPortalRegistrar();

        Locale locale = LocaleResolver.getLocale(request);
        ResourceBundleProvider.updateLocale(locale);
        respage = ResourceBundleProvider.getPageMessagesBundle(locale);

        // Check if desired hostName is available. If so, send OCUI registration request
        String hostName = request.getParameter("hostName");
        if (hostName == null || hostName.equals("")) {
            addRegMessage(request, respage.getString("participate_hostname_invalid"));
            return "redirect:/pages/studymodule";
        }
        String status = "";
        String nameAvailability = registrar.getHostNameAvailability(hostName);
        if (nameAvailability.equals(ParticipantPortalRegistrar.UNAVAILABLE)) {
            addRegMessage(request, respage.getString("participate_hostname_not_available"));
            return "redirect:/pages/studymodule";
        } else if (nameAvailability.equals(ParticipantPortalRegistrar.UNKNOWN)) {
            addRegMessage(request, respage.getString("participate_not_available"));
            return "redirect:/pages/studymodule";
        } else if (nameAvailability.equals(ParticipantPortalRegistrar.INVALID)) {
            addRegMessage(request, respage.getString("participate_hostname_invalid"));
            return "redirect:/pages/studymodule";
        } else {
            // Returned status was 'available'. Proceed with registration.
            status = registrar.registerStudy(study.getOc_oid(), hostName, study.getUniqueIdentifier());
        }

        // If status == "", that indicates the request to OCUI failed. Post an error message and don't update study
        // parameter.
        if (status.equals("")) {
            addRegMessage(request, respage.getString("participate_not_available"));
        } else {
            // Update OC Study configuration
            spv.setStudyId(study.getStudyId());
            spv.setParameter("participantPortal");
            spv.setValue("enabled");
            if (spv.getId() > 0)
                spvdao.update(spv);
            else
                spvdao.create(spv);
            Study currentStudy = (Study) request.getSession().getAttribute("study");
            currentStudy.setParticipantPortal("enabled");
        }

        return "redirect:/pages/studymodule";
    }

    @RequestMapping(method = RequestMethod.GET) public ModelMap handleMainPage(HttpServletRequest request, HttpServletResponse response) {
        ModelMap map = new ModelMap();
        // Todo need something to reset panel from all the Spring Controllers
        StudyInfoPanel panel = new StudyInfoPanel();
        UserAccountBean userBean = (UserAccountBean) request.getSession().getAttribute("userBean");
        if (!mayProceed(request)) {
            try {
                response.sendRedirect(request.getContextPath() + "/MainMenu?message=authentication_failed");
            } catch (Exception e) {
                logger.error("Error while redirecting to MainMenu: ",e);
            }
            return null;
        }
        panel.reset();
        request.getSession().setAttribute("panel", panel);

        // setUpSidebar(request);
        ResourceBundleProvider.updateLocale(LocaleResolver.getLocale(request));

        Study currentStudy = (Study) request.getSession().getAttribute("study");
        Study currentPublicStudy = (Study) request.getSession().getAttribute("publicStudy");
        eventDefinitionCRFDao = new EventDefinitionCRFDAO(dataSource);
        studyEventDefinitionDao = new StudyEventDefinitionDAO(dataSource);
        crfDao = new CRFDAO(dataSource);
        studyGroupClassDao = new StudyGroupClassDAO(dataSource);
        userDao = new UserAccountDAO(dataSource);
        ruleDao = new RuleDAO(dataSource);

        StudyModuleStatus sms = studyModuleStatusDao.findByStudyId(currentStudy.getStudyId());
        if (sms == null) {
            sms = new StudyModuleStatus();
            sms.setStudyId(currentStudy.getStudyId());
        }

        int crfCount = crfDao.findAllByStudy(currentStudy.getStudyId()).size();
        int crfWithEventDefinition = crfDao.findAllActiveByDefinitions(currentStudy.getStudyId()).size();
        int totalCrf = crfCount + crfWithEventDefinition;
        // int eventDefinitionCount = eventDefinitionCRFDao.findAllActiveByStudy(currentStudy).size();
        int eventDefinitionCount = studyEventDefinitionDao.findAllActiveByStudy(currentStudy).size();

        int subjectGroupCount = studyGroupClassDao.findAllActiveByStudy(currentStudy).size();

        // List<RuleSetBean> ruleSets = ruleSetService.getRuleSetsByStudy(currentStudy);
        // ruleSets = ruleSetService.filterByStatusEqualsAvailableOnlyRuleSetRules(ruleSets);

        int ruleCount = ruleSetService.getCountByStudy(currentStudy);

        int siteCount = studyDao.findOlnySiteIdsByStudy(currentStudy).size();
        String tenantSchema = (String) request.getAttribute("requestSchema");
        request.setAttribute("requestSchema", "public");
        int userCount = userDao.findAllUsersByStudy(currentPublicStudy.getStudyId()).size();
        Collection childStudies = studyDao.findAllByParent(currentPublicStudy.getStudyId());
        Map childStudyUserCount = new HashMap();
        for (Object sb : childStudies) {
            Study childStudy = (Study) sb;
            childStudyUserCount.put(childStudy.getName(), userDao.findAllUsersByStudy(childStudy.getStudyId()).size());
        }
        request.setAttribute("requestSchema", tenantSchema);

        if (sms.getCrf() == 0) {
            sms.setCrf(StudyModuleStatus.NOT_STARTED);
        }
        if (sms.getCrf() != 3 && totalCrf > 0) {
            sms.setCrf(StudyModuleStatus.IN_PROGRESS);
        }

        if (sms.getEventDefinition() == 0) {
            sms.setEventDefinition(StudyModuleStatus.NOT_STARTED);
        }
        if (sms.getEventDefinition() != 3 && eventDefinitionCount > 0) {
            sms.setEventDefinition(StudyModuleStatus.IN_PROGRESS);
        }

        if (sms.getSubjectGroup() == 0) {
            sms.setSubjectGroup(StudyModuleStatus.NOT_STARTED);
        }
        if (sms.getSubjectGroup() != 3 && subjectGroupCount > 0) {
            sms.setSubjectGroup(StudyModuleStatus.IN_PROGRESS);
        }

        if (sms.getRule() == 0) {
            sms.setRule(StudyModuleStatus.NOT_STARTED);
        }
        if (sms.getRule() != 3 && ruleCount > 0) {
            sms.setRule(StudyModuleStatus.IN_PROGRESS);
        }

        if (sms.getSite() == 0) {
            sms.setSite(StudyModuleStatus.NOT_STARTED);
        }
        if (sms.getSite() != 3 && siteCount > 0) {
            sms.setSite(StudyModuleStatus.IN_PROGRESS);
        }

        if (sms.getUsers() == 0) {
            sms.setUsers(StudyModuleStatus.NOT_STARTED);
        }
        if (sms.getUsers() != 3 && userCount > 0) {
            sms.setUsers(StudyModuleStatus.IN_PROGRESS);
        }

        map.addAttribute(sms);
        map.addAttribute("crfCount", totalCrf);
        map.addAttribute("eventDefinitionCount", eventDefinitionCount);
        map.addAttribute("subjectGroupCount", subjectGroupCount);
        map.addAttribute("ruleCount", ruleCount);
        map.addAttribute("siteCount", siteCount);
        map.addAttribute("userCount", userCount);
        map.addAttribute("childStudyUserCount", childStudyUserCount);
        map.addAttribute("studyId", currentStudy.getStudyId());
        map.addAttribute("currentStudy", currentStudy);

        // Load Participate registration information
        String portalURL = CoreResources.getField("portalURL");
        map.addAttribute("portalURL", portalURL);
        if (portalURL != null && !portalURL.equals("")) {
            String participateOCStatus = currentStudy.getParticipantPortal();
            ParticipantPortalRegistrar registrar = new ParticipantPortalRegistrar();
            Authorization pManageAuthorization = registrar.getAuthorization(currentStudy.getOc_oid());
            String participateStatus = "";
            String url = "";
            try {
                URL pManageUrl = new URL(portalURL);
                if (pManageAuthorization != null && pManageAuthorization.getAuthorizationStatus() != null
                        && pManageAuthorization.getAuthorizationStatus().getStatus() != null)
                    participateStatus = pManageAuthorization.getAuthorizationStatus().getStatus();
                map.addAttribute("participateURL", pManageUrl);
                map.addAttribute("participateOCStatus", participateOCStatus);
                map.addAttribute("participateStatus", participateStatus);

                if (pManageAuthorization != null && pManageAuthorization.getStudy() != null && pManageAuthorization.getStudy().getHost() != null
                        && !pManageAuthorization.getStudy().getHost().equals("")) {
                    url = pManageUrl.getProtocol() + "://" + pManageAuthorization.getStudy().getHost() + "." + pManageUrl.getHost() + ((pManageUrl.getPort()
                            > 0) ? ":" + String.valueOf(pManageUrl.getPort()) : "");

                }
            } catch (MalformedURLException e) {
                logger.error(e.getMessage());
                logger.error(ExceptionUtils.getStackTrace(e));
            }
            map.addAttribute("participateURLDisplay", url);
            map.addAttribute("participateURLFull", url + "/#/login");
        }

        // Load Randomization  information
        String moduleManager = CoreResources.getField("moduleManager");
        map.addAttribute("moduleManager", moduleManager);
        if (moduleManager != null && !moduleManager.equals("")) {

            String randomizationOCStatus = currentStudy.getRandomization();
            RandomizationRegistrar randomizationRegistrar = new RandomizationRegistrar();
            SeRandomizationDTO randomization = null;
            try {
                randomization = randomizationRegistrar.getCachedRandomizationDTOObject(currentStudy.getOc_oid(), true);
            } catch (Exception e1) {
                // TODO Auto-generated catch block
                logger.error("Error while accessing randomization registrar: ",e1);
            }
        }

        // @pgawade 13-April-2011- #8877: Added the rule designer URL
        if (null != coreResources) {
            map.addAttribute("ruleDesignerURL", coreResources.getField("designer.url"));
            map.addAttribute("contextPath", getContextPath(request));
            logMe("before checking getHostPath url = " + request.getRequestURL());
            // JN: for the eclinicalhosting the https is not showing up in the request path, going for a fix of taking
            // the hostpath from sysurl
            // map.addAttribute("hostPath", getHostPath(request));
            map.addAttribute("hostPath", getHostPathFromSysUrl(coreResources.getField("sysURL.base"), request.getContextPath()));
            map.addAttribute("path", "pages/studymodule");
        }
        // UserAccountBean userBean = (UserAccountBean) request.getSession().getAttribute("userBean");
        request.setAttribute("userBean", userBean);
        ArrayList statusMap = Status.toStudyUpdateMembersList();
        // statusMap.add(Status.PENDING);
        request.setAttribute("statusMap", statusMap);

        if (currentStudy.isSite()) {
            Study parentStudy = (Study) studyDao.findByPK(currentStudy.getStudy().getStudyId());
            request.setAttribute("parentStudy", parentStudy);
        }

        ArrayList pageMessages = new ArrayList();
        if (request.getSession().getAttribute("pageMessages") != null) {
            pageMessages.addAll((ArrayList) request.getSession().getAttribute("pageMessages"));
            request.setAttribute("pageMessages", pageMessages);
            request.getSession().removeAttribute("pageMessages");
        }

        ArrayList regMessages = new ArrayList();
        if (request.getSession().getAttribute(REG_MESSAGE) != null) {
            regMessages.addAll((ArrayList) request.getSession().getAttribute(REG_MESSAGE));
            request.setAttribute(REG_MESSAGE, regMessages);
            request.getSession().removeAttribute(REG_MESSAGE);
        }

        return map;
    }

    @RequestMapping(method = RequestMethod.POST) public String processSubmit(@ModelAttribute("studyModuleStatus") StudyModuleStatus studyModuleStatus,
            BindingResult result, SessionStatus status, HttpServletRequest request) {
        Study currentStudy = (Study) request.getSession().getAttribute("study");
        if (request.getParameter("saveStudyStatus") == null) {
            studyModuleStatusDao.saveOrUpdate(studyModuleStatus);
            status.setComplete();
        } else {
            currentStudy.setOldStatusId(currentStudy.getStatus().getCode());
            currentStudy.setStatus(Status.getByCode(studyModuleStatus.getStudyStatus()));
            studyDao.updateStudyStatus(currentStudy);
            ArrayList siteList = (ArrayList) studyDao.findAllByParent(currentStudy.getStudyId());
            if (siteList.size() > 0) {
                studyDao.updateSitesStatus(currentStudy);
            }
        }
        return "redirect:studymodule";
    }

    @ExceptionHandler(HttpSessionRequiredException.class) public String handleSessionRequiredException(HttpSessionRequiredException ex,
            HttpServletRequest request) {
        return "redirect:/MainMenu";
    }

    @ExceptionHandler(NullPointerException.class) public String handleNullPointerException(NullPointerException ex, HttpServletRequest request,
            HttpServletResponse response) {
        Study currentStudy = (Study) request.getSession().getAttribute("study");
        if (currentStudy == null) {
            return "redirect:/MainMenu";
        }
        throw ex;
    }

    private void addRegMessage(HttpServletRequest request, String message) {
        ArrayList regMessages = (ArrayList) request.getSession().getAttribute(REG_MESSAGE);
        if (regMessages == null) {
            regMessages = new ArrayList();
        }

        regMessages.add(message);
        logger.debug(message);
        request.getSession().setAttribute(REG_MESSAGE, regMessages);
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

    public SidebarInit getSidebarInit() {
        return sidebarInit;
    }

    public void setSidebarInit(SidebarInit sidebarInit) {
        this.sidebarInit = sidebarInit;
    }

    public StudyModuleStatusDao getStudyModuleStatusDao() {
        return studyModuleStatusDao;
    }

    public BasicDataSource getDataSource() {
        return dataSource;
    }

    public String getContextPath(HttpServletRequest request) {
        String contextPath = request.getContextPath().replaceAll("/", "");
        return contextPath;
    }

    public String getRequestURLMinusServletPath(HttpServletRequest request) {
        String requestURLMinusServletPath = request.getRequestURL().toString().replaceAll(request.getServletPath(), "");
        logMe("processing.." + requestURLMinusServletPath);
        return requestURLMinusServletPath;
    }

    public String getHostPath(HttpServletRequest request) {
        logMe("into the getHostPath/....URL = " + request.getRequestURL() + "URI=" + request.getRequestURI() + "PROTOCOL=");
        String requestURLMinusServletPath = getRequestURLMinusServletPath(request);
        String hostPath = "";

        if (null != requestURLMinusServletPath) {
            String tmpPath = requestURLMinusServletPath.substring(0, requestURLMinusServletPath.lastIndexOf("/"));
            logMe("processing2..." + tmpPath);
            hostPath = tmpPath.substring(0, tmpPath.lastIndexOf("/"));
            logMe("processing2..." + hostPath);
        }
        logMe("after all the stripping returning" + hostPath);
        return hostPath;
    }

    public String getWebAppName(String servletCtxRealPath) {
        String webAppName = null;
        if (null != servletCtxRealPath) {
            String[] tokens = servletCtxRealPath.split("\\\\");
            webAppName = tokens[tokens.length - 1].trim();
        }
        return webAppName;
    }

    private boolean mayProceed(HttpServletRequest request) {
        HttpSession session = request.getSession();
        StudyUserRoleBean currentRole = (StudyUserRoleBean) session.getAttribute("userRole");

        Role r = currentRole.getRole();
        if (r.equals(Role.ADMIN) || r.equals(Role.STUDYDIRECTOR) || r.equals(Role.COORDINATOR)) {
            return true;
        }

        return false;
    }

    private String getHostPathFromSysUrl(String sysURL, String contextPath) {
        return sysURL.replaceAll(contextPath + "/", "");
    }

    private void logMe(String msg) {
        logger.debug(msg);
    }

}
