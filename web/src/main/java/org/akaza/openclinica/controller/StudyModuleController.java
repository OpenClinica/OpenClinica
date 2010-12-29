package org.akaza.openclinica.controller;

import org.akaza.openclinica.bean.core.Status;
import org.akaza.openclinica.bean.login.UserAccountBean;
import org.akaza.openclinica.bean.managestudy.StudyBean;
import org.akaza.openclinica.dao.admin.CRFDAO;
import org.akaza.openclinica.dao.hibernate.RuleSetDao;
import org.akaza.openclinica.dao.hibernate.StudyModuleStatusDao;
import org.akaza.openclinica.dao.login.UserAccountDAO;
import org.akaza.openclinica.dao.managestudy.EventDefinitionCRFDAO;
import org.akaza.openclinica.dao.managestudy.StudyDAO;
import org.akaza.openclinica.dao.managestudy.StudyEventDefinitionDAO;
import org.akaza.openclinica.dao.managestudy.StudyGroupClassDAO;
import org.akaza.openclinica.dao.rule.RuleDAO;
import org.akaza.openclinica.domain.managestudy.StudyModuleStatus;
import org.akaza.openclinica.service.rule.RuleSetServiceInterface;
import org.akaza.openclinica.i18n.util.ResourceBundleProvider;
import org.apache.commons.dbcp.BasicDataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.SessionAttributes;
import org.springframework.web.bind.support.SessionStatus;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

/**
 * @author: sshamim
 * Date: Jan 22, 2009
 * Time: 6:52:16 PM
 * Manages the Study creation process
 */
@Controller("studyModuleController")
@RequestMapping("/studymodule")
@SessionAttributes("studyModuleStatus")
public class StudyModuleController {
    @Autowired
    @Qualifier("sidebarInit")
    private SidebarInit sidebarInit;

    @Autowired
    @Qualifier("studyModuleStatusDao")
    private StudyModuleStatusDao studyModuleStatusDao;

    @Autowired
    @Qualifier("ruleSetService")
    private RuleSetServiceInterface ruleSetService;

    @Autowired
    @Qualifier("ruleSetDao")
    private RuleSetDao ruleSetDao;

    @Autowired
    @Qualifier("dataSource")
    private BasicDataSource dataSource;

    private EventDefinitionCRFDAO eventDefinitionCRFDao;
    private StudyEventDefinitionDAO studyEventDefinitionDao;
    private CRFDAO crfDao;
    private StudyGroupClassDAO studyGroupClassDao;
    private StudyDAO studyDao;
    private UserAccountDAO userDao;
    private org.akaza.openclinica.dao.rule.RuleDAO ruleDao;

    public StudyModuleController() {

    }

    @RequestMapping(method = RequestMethod.GET)
    public ModelMap handleMainPage(HttpServletRequest request) {
        ModelMap map = new ModelMap();
        //setUpSidebar(request);
        ResourceBundleProvider.updateLocale(request.getLocale());

        StudyBean currentStudy = (StudyBean) request.getSession().getAttribute("study");

        eventDefinitionCRFDao = new EventDefinitionCRFDAO(dataSource);
        studyEventDefinitionDao = new StudyEventDefinitionDAO(dataSource);
        crfDao = new CRFDAO(dataSource);
        studyGroupClassDao = new StudyGroupClassDAO(dataSource);
        studyDao = new StudyDAO(dataSource);
        userDao = new UserAccountDAO(dataSource);
        ruleDao = new RuleDAO(dataSource);

        StudyModuleStatus sms = studyModuleStatusDao.findByStudyId(currentStudy.getId());
        if (sms == null) {
            sms = new StudyModuleStatus();
            sms.setStudyId(currentStudy.getId());
        }

        int crfCount = crfDao.findAllByStudy(currentStudy.getId()).size();
        int crfWithEventDefinition = crfDao.findAllActiveByDefinitions(currentStudy.getId()).size();
        int totalCrf = crfCount + crfWithEventDefinition;
        //int eventDefinitionCount = eventDefinitionCRFDao.findAllActiveByStudy(currentStudy).size();
        int eventDefinitionCount = studyEventDefinitionDao.findAllActiveByStudy(currentStudy).size();

        int subjectGroupCount = studyGroupClassDao.findAllActiveByStudy(currentStudy).size();

        //List<RuleSetBean> ruleSets = ruleSetService.getRuleSetsByStudy(currentStudy);
        //ruleSets = ruleSetService.filterByStatusEqualsAvailableOnlyRuleSetRules(ruleSets);

        int ruleCount = ruleSetDao.getCountByStudy(currentStudy).intValue();

        int siteCount = studyDao.findOlnySiteIdsByStudy(currentStudy).size();
        int userCount = userDao.findAllUsersByStudy(currentStudy.getId()).size();
        Collection childStudies = studyDao.findAllByParent(currentStudy.getId());
        Map childStudyUserCount = new HashMap();
        for (Object sb : childStudies) {
            StudyBean childStudy = (StudyBean) sb;
            childStudyUserCount.put(childStudy.getName(), userDao.findAllUsersByStudy(childStudy.getId()).size());
        }

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

        map.addObject(sms);
        map.addAttribute("crfCount", totalCrf);
        map.addAttribute("eventDefinitionCount", eventDefinitionCount);
        map.addAttribute("subjectGroupCount", subjectGroupCount);
        map.addAttribute("ruleCount", ruleCount);
        map.addAttribute("siteCount", siteCount);
        map.addAttribute("userCount", userCount);
        map.addAttribute("childStudyUserCount", childStudyUserCount);
        map.addAttribute("studyId", currentStudy.getId());
        map.addAttribute("currentStudy", currentStudy);

        UserAccountBean userBean = (UserAccountBean) request.getSession().getAttribute("userBean");
        request.setAttribute("userBean", userBean);
        ArrayList statusMap = Status.toStudyUpdateMembersList();
//        statusMap.add(Status.PENDING);
        request.setAttribute("statusMap", statusMap);

        if(currentStudy.getParentStudyId() > 0){
            StudyBean parentStudy = (StudyBean)studyDao.findByPK(currentStudy.getParentStudyId());
            request.setAttribute("parentStudy", parentStudy);
        }


        ArrayList pageMessages = new ArrayList();
        if (request.getSession().getAttribute("pageMessages") != null) {
            pageMessages.addAll((ArrayList) request.getSession().getAttribute("pageMessages"));
            request.setAttribute("pageMessages", pageMessages);
            request.getSession().removeAttribute("pageMessages");
        }
        return map;
    }

    @RequestMapping(method = RequestMethod.POST)
    public String processSubmit(@ModelAttribute("studyModuleStatus") StudyModuleStatus studyModuleStatus, BindingResult result, SessionStatus status,
            HttpServletRequest request) {
        StudyBean currentStudy = (StudyBean) request.getSession().getAttribute("study");
        if (request.getParameter("saveStudyStatus") == null){
            studyModuleStatusDao.saveOrUpdate(studyModuleStatus);
            status.setComplete();
        } else {
            currentStudy.setOldStatus(currentStudy.getStatus());
            currentStudy.setStatus(Status.get(studyModuleStatus.getStudyStatus()));
            if (currentStudy.getParentStudyId() > 0) {
                studyDao.updateStudyStatus(currentStudy);
            } else {
                studyDao.updateStudyStatus(currentStudy);
            }

            ArrayList siteList = (ArrayList) studyDao.findAllByParent(currentStudy.getId());
            if (siteList.size() > 0) {
                studyDao.updateSitesStatus(currentStudy);
            }
        }
        return "redirect:studymodule";
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
}
