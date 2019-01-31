/*
 * OpenClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: http://www.openclinica.org/license
 * copyright 2003-2005 Akaza Research
 */
package org.akaza.openclinica.control;

import org.akaza.openclinica.bean.core.Utils;
import org.akaza.openclinica.bean.login.StudyUserRoleBean;
import org.akaza.openclinica.bean.login.UserAccountBean;
import org.akaza.openclinica.bean.managestudy.StudyBean;
import org.akaza.openclinica.bean.service.StudyParameterValueBean;
import org.akaza.openclinica.control.admin.EventStatusStatisticsTableFactory;
import org.akaza.openclinica.control.admin.SiteStatisticsTableFactory;
import org.akaza.openclinica.control.admin.StudyStatisticsTableFactory;
import org.akaza.openclinica.control.admin.StudySubjectStatusStatisticsTableFactory;
import org.akaza.openclinica.control.core.SecureController;
import org.akaza.openclinica.control.form.FormProcessor;
import org.akaza.openclinica.control.submit.ListStudySubjectTableFactory;
import org.akaza.openclinica.dao.core.CoreResources;
import org.akaza.openclinica.dao.login.UserAccountDAO;
import org.akaza.openclinica.dao.managestudy.*;
import org.akaza.openclinica.dao.service.StudyConfigService;
import org.akaza.openclinica.dao.service.StudyParameterValueDAO;
import org.akaza.openclinica.dao.submit.EventCRFDAO;
import org.akaza.openclinica.dao.submit.SubjectDAO;
import org.akaza.openclinica.dao.submit.SubjectGroupMapDAO;
import org.akaza.openclinica.domain.datamap.Study;
import org.akaza.openclinica.i18n.core.LocaleResolver;
import org.akaza.openclinica.service.StudyBuildService;
import org.akaza.openclinica.service.StudyBuildServiceImpl;
import org.akaza.openclinica.view.Page;
import org.akaza.openclinica.web.InsufficientPermissionException;
import org.akaza.openclinica.web.table.sdv.SDVUtil;
import org.apache.commons.lang.StringUtils;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import javax.servlet.ServletContext;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * The main controller servlet for all the work behind study sites for
 * OpenClinica.
 *
 * @author jxu
 */
public class MainMenuServlet extends SecureController {

    private final static String STUDY_ENV_UUID = "studyEnvUuid";
    private final static String FORCE_RENEW_AUTH = "forceRenewAuth";

    Locale locale;
    private StudyEventDefinitionDAO studyEventDefinitionDAO;
    private SubjectDAO subjectDAO;
    private StudySubjectDAO studySubjectDAO;
    private StudyEventDAO studyEventDAO;
    private StudyGroupClassDAO studyGroupClassDAO;
    private SubjectGroupMapDAO subjectGroupMapDAO;
    private StudyDAO studyDAO;
    private EventCRFDAO eventCRFDAO;
    private EventDefinitionCRFDAO eventDefintionCRFDAO;
    private StudyGroupDAO studyGroupDAO;
    private DiscrepancyNoteDAO discrepancyNoteDAO;
    private StudyParameterValueDAO studyParameterValueDAO;

    // ResourceBundle respage;

    @Override public void mayProceed() throws InsufficientPermissionException {
        locale = LocaleResolver.getLocale(request);
        // < respage =
        // ResourceBundle.getBundle("org.akaza.openclinica.i18n.page_messages",locale);
    }

    public String getQueryStrCookie(HttpServletRequest request, HttpServletResponse response) {
        String queryStr = "";
        Cookie[] cookies = request.getCookies();
        for (Cookie cookie : cookies) {
            if (cookie.getName().equalsIgnoreCase("queryStr")) {
                try {
                    queryStr = URLDecoder.decode(cookie.getValue(), "UTF-8");
                } catch (UnsupportedEncodingException e) {
                    logger.error("Error decoding redirect URL from queryStr cookie:" + e.getMessage());
                }
                cookie.setValue(null);
                cookie.setMaxAge(0);
                cookie.setPath("/");
                if (response != null)
                    response.addCookie(cookie);
                break;
            }
        }
        return queryStr;
    }

    public String getTimeoutReturnToCookie(HttpServletRequest request, HttpServletResponse response) {
        String queryStr = "";
        if (ub == null || StringUtils.isEmpty(ub.getName()))
            return queryStr;

        Cookie[] cookies = request.getCookies();
        for (Cookie cookie : cookies) {
            if (cookie.getName().equalsIgnoreCase("bridgeTimeoutReturn-" + ub.getName())) {
                try {
                    queryStr = URLDecoder.decode(cookie.getValue(), "UTF-8");
                } catch (UnsupportedEncodingException e) {
                    logger.error("Error decoding redirect URL from queryStr cookie:" + e.getMessage());
                }
                cookie.setValue(null);
                cookie.setMaxAge(0);
                cookie.setPath("/");
                if (response != null)
                    response.addCookie(cookie);
                break;
            }
        }
        return queryStr;
    }

    /**
     * The parameter separators are being URL encoded hence not being interpreted as separate parameters
     * eg. MainMenu?studyEnvUuid%3D3d3a3d9e-8dc8-49ce-9800-810e665f062c%26forceRenewAuth%3Dtrue
     * @param request
     * @param parameterName
     * @return parameter value
     */
    private String getParameter(HttpServletRequest request, String parameterName){
        String paramValue = request.getParameter(parameterName);
        if (paramValue == null){
            ArrayList<String> parameters = extractParametersAsListFromParameterNames(request.getParameterNames());
            paramValue = parameters.indexOf(parameterName) > -1 ? parameters.get(parameters.indexOf(parameterName) + 1) : null;
        }
        logger.info("Getting parameter name: " + parameterName + " value: " + paramValue);
        return paramValue;
    }

    private ArrayList<String> extractParametersAsListFromParameterNames(Enumeration<String> parameterNames){
        ArrayList<String> parameters = new ArrayList<>();
        if (parameterNames.hasMoreElements()) {
            parameters = new ArrayList<>(Arrays.asList(request.getParameterNames().nextElement().split("=|&")));
        }
        return parameters;
    }

    public boolean processSpecificStudyEnvUuid() throws Exception {
        logger.info("MainMenuServlet processSpecificStudyEnvUuid:%%%%%%%%" + session.getAttribute("firstLoginCheck"));
        boolean isRenewAuth = false;
        String studyEnvUuid = getParameter(request, STUDY_ENV_UUID);
        if (StringUtils.isEmpty(studyEnvUuid)) {
            return isRenewAuth;
        }
        String forceRenewAuth = getParameter(request, FORCE_RENEW_AUTH);
        if (processForceRenewAuth(forceRenewAuth))
            return true;
        ServletContext context = getServletContext();
        WebApplicationContext ctx =
                WebApplicationContextUtils
                        .getWebApplicationContext(context);
        String currentSchema = CoreResources.getRequestSchema(request);
        CoreResources.setRequestSchema(request, "public");
        StudyBuildService studyService = ctx.getBean("studyBuildService", StudyBuildService.class);

        studyService.updateStudyUserRoles(request, studyService.getUserAccountObject(ub), ub.getActiveStudyId(), studyEnvUuid, false);
        UserAccountDAO userAccountDAO = new UserAccountDAO(sm.getDataSource());

        ArrayList userRoleBeans = (ArrayList) userAccountDAO.findAllRolesByUserName(ub.getName());
        ub.setRoles(userRoleBeans);
        session.setAttribute(SecureController.USER_BEAN_NAME, ub);
        StudyDAO sd = getStudyDAO();
        StudyBean tmpPublicStudy = sd.findByStudyEnvUuid(studyEnvUuid);

        if (tmpPublicStudy == null) {
            CoreResources.setRequestSchema(request,currentSchema);
            return isRenewAuth;
        }

        StudyUserRoleBean role = ub.getRoleByStudy(tmpPublicStudy.getId());

        if (role.getStudyId() == 0) {
            logger.error("You have no roles for this study." + studyEnvUuid + " currentStudy is:" + tmpPublicStudy.getName() + " schema:" + tmpPublicStudy.getSchemaName());
            logger.error("Creating an invalid role, ChangeStudy page will be shown");
            currentRole = new StudyUserRoleBean();
            session.setAttribute("userRole", currentRole);
        } else {
            currentPublicStudy = tmpPublicStudy;
            CoreResources.setRequestSchema(request, currentPublicStudy.getSchemaName());
            currentStudy = sd.findByStudyEnvUuid(studyEnvUuid);
            if (currentStudy.getParentStudyId() > 0) {
                currentStudy.setParentStudyName(sd.findByPK(currentStudy.getParentStudyId()).getName());
                currentPublicStudy.setParentStudyName(currentStudy.getParentStudyName());
            }
            StudyConfigService scs = new StudyConfigService(sm.getDataSource());
            scs.setParametersForStudy(currentStudy);

            session.setAttribute("publicStudy", currentPublicStudy);
            session.setAttribute("study", currentStudy);
            currentRole = role;
            session.setAttribute("userRole", role);
            logger.info("Found role for this study:" + role.getRoleName());
            if (ub.getActiveStudyId() == currentPublicStudy.getId())
                return isRenewAuth;
            ub.setActiveStudyId(currentPublicStudy.getId());
            session.setAttribute("customUserRole", session.getAttribute("altCustomUserRole"));
        }

        return isRenewAuth;
    }

    private boolean
    processForceRenewAuth(String renewAuth) throws IOException {
        logger.info("forceRenewAuth is true");
        boolean isRenewAuth = false;
        if (StringUtils.isNotEmpty(renewAuth)) {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null) {
                auth.setAuthenticated(false);
                SecurityContextHolder.clearContext();
            }
            return true;
        }
        return isRenewAuth;
    }

    @Override public void processRequest() throws Exception {

        FormProcessor fp = new FormProcessor(request);
        session.setAttribute(USER_BEAN_NAME, ub);
        request.setAttribute("iconInfoShown", true);
        request.setAttribute("closeInfoShowIcons", false);

        if (ub == null || ub.getId() == 0) {// in case database connection is
            // broken
            forwardPage(Page.MENU, false);
            return;
        }

        // a flag tells whether users are required to change pwd upon the first
        // time log in or pwd expired
        // update last visit date to current date
        UserAccountDAO udao = new UserAccountDAO(sm.getDataSource());
        if (processSpecificStudyEnvUuid()) {
            /*
            TODO: Do we need this logic? Might come in handy if URL passed in contains more params
            Map<String, String[]> targetMap = new ConcurrentHashMap<>(request.getParameterMap());
            targetMap.remove("forceRenewAuth");
            String paramStr = Utils.getParamsString(targetMap);
            logger.info("Sending redirect to:" + request.getRequestURI() + "?" + paramStr);
            */
            session.removeAttribute("userRole");
            response.sendRedirect(request.getRequestURI() + "?" + STUDY_ENV_UUID  + "=" +  getParameter(request,STUDY_ENV_UUID));
            return;
        }

        ub.setLastVisitDate(new Date(System.currentTimeMillis()));
        // have to actually set the above to a timestamp? tbh
        udao.update(ub);

        if (!currentRole.isActive()) {
            String paramStr = Utils.getParamsString(request.getParameterMap());
            request.setAttribute("prevPageParams", paramStr);
            forwardPage(Page.CHANGE_STUDY_SERVLET, false);
            return;
        }

        // Use study Id in JSPs
        if (currentStudy != null) {
            request.setAttribute("studyId", currentStudy.getId());
            // Event Definition list and Group Class list for add suybject window.
            // request.setAttribute("allDefsArray", super.getEventDefinitionsByCurrentStudy());
            request.setAttribute("studyGroupClasses", super.getStudyGroupClassesByCurrentStudy());
        }

        logger.info("is ub a ldapuser??" + ub.isLdapUser());

        if (currentStudy == null) {
            forwardPage(Page.MENU);
            return;
        }

        ////Integer assignedDiscrepancies = getDiscrepancyNoteDAO().countAllItemDataByStudyAndUser(currentStudy, ub);
        //Integer assignedDiscrepancies = getDiscrepancyNoteDAO().getViewNotesCountWithFilter(" AND dn.assigned_user_id ="
        //  + ub.getId() + " AND (dn.resolution_status_id=1 OR dn.resolution_status_id=2 OR dn.resolution_status_id=3)", currentStudy);
        //Yufang code added by Jamuna, to optimize the query on MainMenu

        Integer assignedDiscrepancies = getDiscrepancyNoteDAO().getViewNotesCountWithFilter(ub.getId(), currentStudy.getId());
        request.setAttribute("assignedDiscrepancies", assignedDiscrepancies == null ? 0 : assignedDiscrepancies);

        int parentStudyId = currentStudy.getParentStudyId() > 0 ? currentStudy.getParentStudyId() : currentStudy.getId();
        StudyParameterValueDAO spvdao = new StudyParameterValueDAO(sm.getDataSource());
        StudyParameterValueBean parentSPV = spvdao.findByHandleAndStudy(parentStudyId, "subjectIdGeneration");
        currentStudy.getStudyParameterConfig().setSubjectIdGeneration(parentSPV.getValue());
        String idSetting = parentSPV.getValue();
        if (idSetting.equals("auto editable") || idSetting.equals("auto non-editable")) {
            //Shaoyu Su
            //int nextLabel = this.getStudySubjectDAO().findTheGreatestLabel() + 1;
            //request.setAttribute("label", new Integer(nextLabel).toString());
            request.setAttribute("label", resword.getString("id_generated_Save_Add"));
            //@pgawade 27-June-2012 fix for issue 13477: set label to "ID will be generated on Save or Add" in case of auto generated subject id
            fp.addPresetValue("label", resword.getString("id_generated_Save_Add"));
        }
        setPresetValues(fp.getPresetValues());

        if (currentRole.isInvestigator() || currentRole.isResearchAssistant() || currentRole.isResearchAssistant2()) {
            forwardPage(Page.LIST_STUDY_SUBJECTS_SERVLET);
            return;
        }
        if (currentRole.isMonitor()) {
            response.sendRedirect(request.getContextPath() + "/pages/viewAllSubjectSDVtmp?sdv_restore=true&studyId=" + currentStudy.getId());
            return;
        } else if (currentRole.isCoordinator() || currentRole.isDirector()) {
            setupStudySiteStatisticsTable();
            setupSubjectEventStatusStatisticsTable();
            setupStudySubjectStatusStatisticsTable();
            if (currentStudy.getParentStudyId() == 0) {
                setupStudyStatisticsTable();
            }

        }
        logger.info("Current Role:" + currentRole.getRole().getName());
        StudyUserRoleBean userRole = (StudyUserRoleBean) session.getAttribute("userRole");
        logger.info("User Role:" + userRole.getName());

        forwardPage(Page.MENU);
    }

    private void setupSubjectSDVTable() {

   //     request.setAttribute("studyId", currentStudy.getId());
   //     request.setAttribute("showMoreLink", "true");
   //     String sdvMatrix = getSDVUtil().renderEventCRFTableWithLimit(request, currentStudy.getId(), "");
   //     request.setAttribute("sdvMatrix", sdvMatrix);
    }

    private void setupStudySubjectStatusStatisticsTable() {

        StudySubjectStatusStatisticsTableFactory factory = new StudySubjectStatusStatisticsTableFactory();
        factory.setStudySubjectDao(getStudySubjectDAO());
        factory.setCurrentStudy(currentStudy);
        factory.setStudyDao(getStudyDAO());

        String studySubjectStatusStatistics = factory.createTable(request, response).render();
        request.setAttribute("studySubjectStatusStatistics", studySubjectStatusStatistics);
    }

    private void setupSubjectEventStatusStatisticsTable() {

        EventStatusStatisticsTableFactory factory = new EventStatusStatisticsTableFactory();
        factory.setStudySubjectDao(getStudySubjectDAO());
        factory.setCurrentStudy(currentStudy);
        factory.setStudyEventDao(getStudyEventDAO());
        factory.setStudyDao(getStudyDAO());
        String subjectEventStatusStatistics = factory.createTable(request, response).render();
        request.setAttribute("subjectEventStatusStatistics", subjectEventStatusStatistics);
    }

    private void setupStudySiteStatisticsTable() {

        SiteStatisticsTableFactory factory = new SiteStatisticsTableFactory();
        factory.setStudySubjectDao(getStudySubjectDAO());
        factory.setCurrentStudy(currentStudy);
        factory.setStudyDao(getStudyDAO());
        String studySiteStatistics = factory.createTable(request, response).render();
        request.setAttribute("studySiteStatistics", studySiteStatistics);

    }

    private void setupStudyStatisticsTable() {

        StudyStatisticsTableFactory factory = new StudyStatisticsTableFactory();
        factory.setStudySubjectDao(getStudySubjectDAO());
        factory.setCurrentStudy(currentPublicStudy);
        factory.setStudyDao(getStudyDAO());
        String studyStatistics = factory.createTable(request, response).render();
        request.setAttribute("studyStatistics", studyStatistics);

    }

    private void setupListStudySubjectTable() {

        ListStudySubjectTableFactory factory = new ListStudySubjectTableFactory(true);
        factory.setStudyEventDefinitionDao(getStudyEventDefinitionDao());
        factory.setSubjectDAO(getSubjectDAO());
        factory.setStudySubjectDAO(getStudySubjectDAO());
        factory.setStudyEventDAO(getStudyEventDAO());
        factory.setStudyBean(currentStudy);
        factory.setStudyGroupClassDAO(getStudyGroupClassDAO());
        factory.setSubjectGroupMapDAO(getSubjectGroupMapDAO());
        factory.setStudyDAO(getStudyDAO());
        factory.setCurrentRole(currentRole);
        factory.setCurrentUser(ub);
        factory.setEventCRFDAO(getEventCRFDAO());
        factory.setEventDefintionCRFDAO(getEventDefinitionCRFDAO());
        factory.setStudyGroupDAO(getStudyGroupDAO());
        factory.setStudyParameterValueDAO(getStudyParameterValueDAO());
        String findSubjectsHtml = factory.createTable(request, response).render();
        request.setAttribute("findSubjectsHtml", findSubjectsHtml);
    }

    public StudyParameterValueDAO getStudyParameterValueDAO() {
        studyParameterValueDAO = this.studyParameterValueDAO == null ? new StudyParameterValueDAO(sm.getDataSource()) : studyParameterValueDAO;
        return studyParameterValueDAO;
    }

    public void setStudyParameterValueDAO(StudyParameterValueDAO studyParameterValueDAO) {
        studyParameterValueDAO = studyParameterValueDAO;
    }

    public StudyEventDefinitionDAO getStudyEventDefinitionDao() {
        studyEventDefinitionDAO = studyEventDefinitionDAO == null ? new StudyEventDefinitionDAO(sm.getDataSource()) : studyEventDefinitionDAO;
        return studyEventDefinitionDAO;
    }

    public SubjectDAO getSubjectDAO() {
        subjectDAO = this.subjectDAO == null ? new SubjectDAO(sm.getDataSource()) : subjectDAO;
        return subjectDAO;
    }

    public StudySubjectDAO getStudySubjectDAO() {
        studySubjectDAO = this.studySubjectDAO == null ? new StudySubjectDAO(sm.getDataSource()) : studySubjectDAO;
        return studySubjectDAO;
    }

    public StudyGroupClassDAO getStudyGroupClassDAO() {
        studyGroupClassDAO = this.studyGroupClassDAO == null ? new StudyGroupClassDAO(sm.getDataSource()) : studyGroupClassDAO;
        return studyGroupClassDAO;
    }

    public SubjectGroupMapDAO getSubjectGroupMapDAO() {
        subjectGroupMapDAO = this.subjectGroupMapDAO == null ? new SubjectGroupMapDAO(sm.getDataSource()) : subjectGroupMapDAO;
        return subjectGroupMapDAO;
    }

    public StudyEventDAO getStudyEventDAO() {
        studyEventDAO = this.studyEventDAO == null ? new StudyEventDAO(sm.getDataSource()) : studyEventDAO;
        return studyEventDAO;
    }

    public StudyDAO getStudyDAO() {
        studyDAO = this.studyDAO == null ? new StudyDAO(sm.getDataSource()) : studyDAO;
        return studyDAO;
    }

    public EventCRFDAO getEventCRFDAO() {
        eventCRFDAO = this.eventCRFDAO == null ? new EventCRFDAO(sm.getDataSource()) : eventCRFDAO;
        return eventCRFDAO;
    }

    public EventDefinitionCRFDAO getEventDefinitionCRFDAO() {
        eventDefintionCRFDAO = this.eventDefintionCRFDAO == null ? new EventDefinitionCRFDAO(sm.getDataSource()) : eventDefintionCRFDAO;
        return eventDefintionCRFDAO;
    }

    public StudyGroupDAO getStudyGroupDAO() {
        studyGroupDAO = this.studyGroupDAO == null ? new StudyGroupDAO(sm.getDataSource()) : studyGroupDAO;
        return studyGroupDAO;
    }

    public DiscrepancyNoteDAO getDiscrepancyNoteDAO() {
        discrepancyNoteDAO = this.discrepancyNoteDAO == null ? new DiscrepancyNoteDAO(sm.getDataSource()) : discrepancyNoteDAO;
        return discrepancyNoteDAO;
    }

    public SDVUtil getSDVUtil() {
        return (SDVUtil) SpringServletAccess.getApplicationContext(context).getBean("sdvUtil");
    }

}
