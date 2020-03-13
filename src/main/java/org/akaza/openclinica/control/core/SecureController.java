/*
 * OpenClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: http://www.openclinica.org/license
 * copyright 2003-2005 Akaza Research
 */

package org.akaza.openclinica.control.core;
import core.org.akaza.openclinica.bean.admin.CRFBean;
import core.org.akaza.openclinica.bean.core.*;
import core.org.akaza.openclinica.bean.extract.ArchivedDatasetFileBean;
import core.org.akaza.openclinica.bean.login.StudyUserRoleBean;
import core.org.akaza.openclinica.bean.login.UserAccountBean;
import core.org.akaza.openclinica.bean.managestudy.*;
import core.org.akaza.openclinica.bean.submit.EventCRFBean;
import core.org.akaza.openclinica.bean.submit.ItemBean;
import core.org.akaza.openclinica.bean.submit.ItemDataBean;
import core.org.akaza.openclinica.domain.datamap.Study;
import org.akaza.openclinica.config.StudyParamNames;
import org.akaza.openclinica.control.SpringServletAccess;
import org.akaza.openclinica.controller.KeycloakController;
import core.org.akaza.openclinica.core.EmailEngine;
import core.org.akaza.openclinica.core.EventCRFLocker;
import core.org.akaza.openclinica.core.SessionManager;
import core.org.akaza.openclinica.core.form.StringUtil;
import core.org.akaza.openclinica.dao.admin.CRFDAO;
import core.org.akaza.openclinica.dao.core.AuditableEntityDAO;
import core.org.akaza.openclinica.dao.core.CoreResources;
import core.org.akaza.openclinica.dao.extract.ArchivedDatasetFileDAO;
import core.org.akaza.openclinica.dao.hibernate.*;
import core.org.akaza.openclinica.dao.login.UserAccountDAO;
import core.org.akaza.openclinica.dao.managestudy.*;
import core.org.akaza.openclinica.dao.service.StudyConfigService;
import core.org.akaza.openclinica.dao.service.StudyParameterValueDAO;
import core.org.akaza.openclinica.dao.submit.EventCRFDAO;
import core.org.akaza.openclinica.dao.submit.ItemDAO;
import core.org.akaza.openclinica.dao.submit.ItemDataDAO;
import core.org.akaza.openclinica.domain.datamap.EventCrf;
import core.org.akaza.openclinica.domain.datamap.StudyParameterValue;
import core.org.akaza.openclinica.exception.OpenClinicaException;
import core.org.akaza.openclinica.i18n.core.LocaleResolver;
import core.org.akaza.openclinica.i18n.util.I18nFormatUtil;
import core.org.akaza.openclinica.i18n.util.ResourceBundleProvider;
import core.org.akaza.openclinica.service.*;
import core.org.akaza.openclinica.service.crfdata.EnketoUrlService;
import org.akaza.openclinica.service.UserService;
import org.akaza.openclinica.service.ValidateService;
import org.akaza.openclinica.view.BreadcrumbTrail;
import org.akaza.openclinica.view.Page;
import org.akaza.openclinica.view.StudyInfoPanel;
import org.akaza.openclinica.view.StudyInfoPanelLine;
import core.org.akaza.openclinica.web.InconsistentStateException;
import core.org.akaza.openclinica.web.InsufficientPermissionException;
import core.org.akaza.openclinica.web.SQLInitServlet;
import core.org.akaza.openclinica.web.bean.EntityBeanTable;
import org.apache.commons.lang.StringUtils;
import org.quartz.JobKey;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.quartz.TriggerKey;
import org.quartz.impl.StdScheduler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import javax.mail.MessagingException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.SingleThreadModel;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.sql.DataSource;
import java.io.*;
import java.net.MalformedURLException;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

/**
 * This class enhances the Controller in several ways.
 *
 * <ol>
 * <li>The method mayProceed, for which the class is named, is declared abstract and is called before processRequest.
 * This
 * method indicates whether the user may proceed with the action he wishes to perform (as indicated by various
 * attributes or
 * parameters in request or session). Note, howeveer, that the method has a void return, and throws
 * InsufficientPermissionException. The intention is that if the user may not proceed with his desired action, the
 * method
 * should throw an exception. InsufficientPermissionException will accept a Page object which indicates where the user
 * should
 * be redirected in order to be informed that he has insufficient permission, and the process method enforces this
 * redirection
 * by catching an InsufficientPermissionException object.
 *
 * <li>Four new members, session, request, response, and the UserAccountBean object ub have been declared protected, and
 * are
 * set in the process method. This allows developers to avoid passing these objects between methods, and moreover it
 * accurately encodes the fact that these objects represent the state of the servlet.
 *
 * <br/>
 * In particular, please note that it is no longer necessary to generate a bean for the session manager, the current
 * user or
 * the current study.
 *
 * <li>The method processRequest has been declared abstract. This change is unlikely to affect most code, since by
 * custom
 * processRequest is declared in each subclass anyway.
 *
 * <li>The standard try-catch block within most processRequest methods has been included in the process method, which
 * calls
 * the processRequest method. Therefore, subclasses may throw an Exception in the processRequest method without having
 * to
 * handle it.
 *
 * <li>The addPageMessage method has been declared to streamline the process of setting page-level messages. The
 * accompanying
 * showPageMessages.jsp file in jsp/include/ automatically displays all of the page messages; the developer need only
 * include
 * this file in the jsp.
 *
 * <li>The addEntityList method makes it easy to add a Collection of EntityBeans to the request. Note that this method
 * should
 * only be used for Collections from which one EntityBean must be selected by the user. If the Collection is empty, this
 * method will throw an InconsistentStateException, taking the user to an error page and settting a page message
 * indicating
 * that the user may not proceed because no entities are present. Note that the error page and the error message must be
 * specified.
 * </ol>
 *
 * @author ssachs
 */
public abstract class SecureController extends HttpServlet implements SingleThreadModel {
    protected ServletContext context;
    protected SessionManager sm;
    private final static String STUDY_ENV_UUID = "studyEnvUuid";
    private final static String FORCE_RENEW_AUTH = "forceRenewAuth";

    // protected final Logger logger =
    // LoggerFactory.getLogger(getClass().getName());
    protected static final Logger logger = LoggerFactory.getLogger(SecureController.class);
    protected String logDir;
    protected String logLevel;
    protected HttpSession session;
    protected HttpServletRequest request;
    protected HttpServletResponse response;
    protected UserAccountBean ub;
    protected Study currentStudy;
    protected Study currentPublicStudy;
    protected StudyUserRoleBean currentRole;
    protected HashMap errors = new HashMap();
    protected UserAccountDao userDaoDomain;
    private static String SCHEDULER = "schedulerFactoryBean";
    public static final String ENABLED = "enabled";
    public static final String DISABLED = "disabled";
    public static final String QUERY_SUFFIX = "form-queries.xml";
    public static final String BIND_OC_EXTERNAL = "bind::oc:external";
    public static final String OC_CONTACTDATA = "oc:contactdata";
    public static final String CONTACTDATA = "contactdata";

    protected UserService userService;

    private StdScheduler scheduler;
    /**
     * local_df is set to the client locale in each request.
     */
    protected SimpleDateFormat local_df = new SimpleDateFormat("MM/dd/yyyy");
    public static ResourceBundle resadmin, resaudit, resexception, resformat, respage, resterm, restext, resword, resworkflow;

    protected StudyInfoPanel panel = new StudyInfoPanel();

    public static final String PAGE_MESSAGE = "pageMessages";// for showing
    // page
    // wide message

    public static final String INPUT_MESSAGES = "formMessages"; // for showing
    // input-specific
    // messages

    public static final String PRESET_VALUES = "presetValues"; // for setting
    // preset values

    public static final String ADMIN_SERVLET_CODE = "admin";

    public static final String BEAN_TABLE = "table";

    public static final String STUDY_INFO_PANEL = "panel"; // for setting the
    // side panel

    public static final String BREADCRUMB_TRAIL = "breadcrumbs";

    public static final String POP_UP_URL = "popUpURL";

    // public static String DATASET_HOME_DIR = "OpenClinica";

    // Use this variable as the key for the support url
    public static final String SUPPORT_URL = "supportURL";

    // Use this variable as the key for the walkme url
    public static final String WALKME_URL = "walkmeURL";

    // Use this variable as the key for the piwik url
    public static final String PIWIK_URL = "piwikURL";

    public static final String MODULE = "module";// to determine which module

    private EventCRFLocker eventCrfLocker;

    private final String COMMON = "common";

    public static final String ORIGINATING_PAGE = "originatingPage";

    // user is in

    // for setting the breadcrumb trail
    // protected HashMap errors = new HashMap();//error messages on the page

    protected void addPageMessage(String message) {
        ArrayList pageMessages = (ArrayList) request.getAttribute(PAGE_MESSAGE);

        if (pageMessages == null) {
            pageMessages = new ArrayList();
        }

        pageMessages.add(message);
        logger.debug(message);
        request.setAttribute(PAGE_MESSAGE, pageMessages);
    }

    protected void resetPanel() {
        panel.reset();
    }

    protected void setToPanel(String title, String info) {
        if (panel.isOrderedData()) {
            ArrayList data = panel.getUserOrderedData();
            data.add(new StudyInfoPanelLine(title, info));
            panel.setUserOrderedData(data);
        } else {
            panel.setData(title, info);
        }
        request.setAttribute(STUDY_INFO_PANEL, panel);
    }

    protected void setInputMessages(HashMap messages) {
        request.setAttribute(INPUT_MESSAGES, messages);
    }

    protected void setPresetValues(HashMap presetValues) {
        request.setAttribute(PRESET_VALUES, presetValues);
    }

    protected void setTable(EntityBeanTable table) {
        request.setAttribute(BEAN_TABLE, table);
    }

    @Override
    public void init() throws ServletException {
        context = getServletContext();
        eventCrfLocker = SpringServletAccess.getApplicationContext(context).getBean(EventCRFLocker.class);

    }

    /**
     * Process request
     *
     * @throws Exception
     */
    protected abstract void processRequest() throws Exception;

    protected abstract void mayProceed() throws InsufficientPermissionException;

    public static final String USER_BEAN_NAME = "userBean";

    public void passwdTimeOut() {
        Date lastChangeDate = ub.getPasswdTimestamp();
        if (!ub.isLdapUser() && lastChangeDate == null) {
            // @pgawade 18-Sep-2012: fix for issue #14506
            // (https://issuetracker.openclinica.com/view.php?id=14506#c58197)
            // addPageMessage(respage.getString("welcome") + " " + ub.getFirstName() + " " + ub.getLastName() + ". " +
            // respage.getString("password_set"));
            // + "<a href=\"UpdateProfile\">" + respage.getString("user_profile") + " </a>");
            int pwdChangeRequired = new Integer(SQLInitServlet.getField("change_passwd_required")).intValue();
            /*
             * if (pwdChangeRequired == 1) {
             * addPageMessage(respage.getString("welcome") + " " + ub.getFirstName() + " " + ub.getLastName() + ". " +
             * respage.getString("password_set"));
             * request.setAttribute("mustChangePass", "yes");
             * forwardPage(Page.RESET_PASSWORD);
             * }
             */
        }
    }

    private void pingJobServer(HttpServletRequest request) {
        String jobName = (String) request.getSession().getAttribute("jobName");
        String groupName = (String) request.getSession().getAttribute("groupName");
        Integer datasetId = (Integer) request.getSession().getAttribute("datasetId");
        try {
            if (jobName != null && groupName != null) {

                Trigger.TriggerState triggerState = getScheduler(request).getTriggerState(new TriggerKey(jobName, groupName));
                org.quartz.JobDetail details = getScheduler(request).getJobDetail(new JobKey(jobName, groupName));
                List contexts = getScheduler(request).getCurrentlyExecutingJobs();
                // will we get the above, even if its completed running?
                // ProcessingResultType message = null;
                // for (int i = 0; i < contexts.size(); i++) {
                // org.quartz.JobExecutionContext context = (org.quartz.JobExecutionContext) contexts.get(i);
                // if (context.getJobDetail().getName().equals(jobName) &&
                // context.getJobDetail().getGroup().equals(groupName)) {
                // message = (ProcessingResultType) context.getResult();
                // System.out.println("found message " + message.getDescription());
                // }
                // }
                // ProcessingResultType message = (ProcessingResultType) details.getResult();
                org.quartz.JobDataMap dataMap = details.getJobDataMap();
                String failMessage = dataMap.getString("failMessage");
                if (triggerState == Trigger.TriggerState.NONE || triggerState == Trigger.TriggerState.COMPLETE) {
                    // add the message here that your export is done
                    // TODO make absolute paths in the message, for example a link from /pages/* would break
                    // TODO i18n
                    if (failMessage != null) {
                        // The extract data job failed with the message:
                        // ERROR: relation "demographics" already exists
                        // More information may be available in the log files.
                        addPageMessage("The extract data job failed with the message: <br/><br/>" + failMessage
                                + "<br/><br/>More information may be available in the log files.");
                        request.getSession().removeAttribute("jobName");
                        request.getSession().removeAttribute("groupName");
                        request.getSession().removeAttribute("datasetId");
                    } else {
                        String successMsg = dataMap.getString("SUCCESS_MESSAGE");
                        String success = dataMap.getString("successMsg");
                        if (success != null) {

                            if (successMsg.contains("$linkURL")) {
                                successMsg = decodeLINKURL(successMsg, datasetId);
                            }

                            if (successMsg != null && !successMsg.isEmpty()) {
                                addPageMessage(successMsg);
                            } else {
                                addPageMessage("Your Extract is now completed. Please go to review them at <a href='ExportDataset?datasetId=" + datasetId
                                        + "'> Here </a>.");
                            }
                            request.getSession().removeAttribute("jobName");
                            request.getSession().removeAttribute("groupName");
                            request.getSession().removeAttribute("datasetId");
                        }
                    }

                } else {

                }
            }
        } catch (SchedulerException se) {
            logger.error("Pinging job server is failing due to ",se);
        }

    }

    private String decodeLINKURL(String successMsg, Integer datasetId) {

        ArchivedDatasetFileDAO asdfDAO = new ArchivedDatasetFileDAO(sm.getDataSource());

        ArrayList<ArchivedDatasetFileBean> fileBeans = asdfDAO.findByDatasetId(datasetId);

        successMsg = successMsg.replace("$linkURL",
                "<a href=\"" + CoreResources.getField("sysURL.base") + "AccessFile?fileId=" + fileBeans.get(0).getId() + "\">here </a>");

        return successMsg;
    }

    private StdScheduler getScheduler(HttpServletRequest request) {
        scheduler = this.scheduler != null ? scheduler
                : (StdScheduler) SpringServletAccess.getApplicationContext(request.getSession().getServletContext()).getBean(SCHEDULER);
        return scheduler;
    }


    private void process(HttpServletRequest request, HttpServletResponse response) throws OpenClinicaException, UnsupportedEncodingException {

        logger.debug("Metric0 {}"+new Date());
        this.request = request;
        this.response = response;
        request.setCharacterEncoding("UTF-8");
        // OC-10389
        request.setAttribute("participantIDVerification", CoreResources.getField("participantIDVerification.enabled"));
//        checkPermissions();
        session = request.getSession();
        // BWP >> 1/8/2008
        try {
            // YW 10-03-2007 <<
            // Since we are managing the session on our own, disable Tomcat session timeout
            session.setMaxInactiveInterval(Integer.parseInt(SQLInitServlet.getField("max_inactive_interval")));
            String sbsUrl = CoreResources.getField("SBSBaseUrl");
            if (StringUtils.isNotEmpty(sbsUrl)) {

                String crossStorageURL = sbsUrl + "/hub/hub.html";
                session.setAttribute("crossStorageURL", crossStorageURL);
            }
            // YW >>
        } catch (NumberFormatException nfe) {
            // BWP>>3600 is the datainfo.properties maxInactiveInterval on
            // 1/8/2008
            session.setMaxInactiveInterval(3600);
        }

        // If the session already has a value with key SUPPORT_URL don't reset
        if (session.getAttribute(SUPPORT_URL) == null) {
            session.setAttribute(SUPPORT_URL, SQLInitServlet.getSupportURL());
        }

        // If the session already has a value with key WALKME_URL don't reset
        if (session.getAttribute(WALKME_URL) == null) {
            session.setAttribute(WALKME_URL, CoreResources.getField("walkme.url"));
        }

        // If the session already has a value with key PIWIK_URL don't reset
        if (session.getAttribute(PIWIK_URL) == null) {
            session.setAttribute(PIWIK_URL, CoreResources.getField("piwik.url"));
        }

        ub = (UserAccountBean) session.getAttribute(USER_BEAN_NAME);
        currentStudy = (Study) session.getAttribute("study");
        currentPublicStudy = (Study) session.getAttribute("publicStudy");



        // Set current language preferences
        Locale locale = LocaleResolver.getLocale(request);
        ResourceBundleProvider.updateLocale(locale);
        resadmin = ResourceBundleProvider.getAdminBundle(locale);
        resaudit = ResourceBundleProvider.getAuditEventsBundle(locale);
        resexception = ResourceBundleProvider.getExceptionsBundle(locale);
        resformat = ResourceBundleProvider.getFormatBundle(locale);
        restext = ResourceBundleProvider.getTextsBundle(locale);
        resterm = ResourceBundleProvider.getTermsBundle(locale);
        resword = ResourceBundleProvider.getWordsBundle(locale);
        respage = ResourceBundleProvider.getPageMessagesBundle(locale);
        resworkflow = ResourceBundleProvider.getWorkflowBundle(locale);

        local_df = I18nFormatUtil.getDateFormat(locale);

        try {
            String userName = request.getRemoteUser();
            // BWP 1/8/08<< the sm variable may already be set with a mock
            // object,
            // from the perspective of
            // JUnit servlets tests
            /*
             * if(sm==null && (!StringUtil.isBlank(userName))) {//check if user logged in, then create a new
             * sessionmanger to
             * get ub //create a new sm in order to get a new ub object sm = new SessionManager(ub, userName); }
             */
            // BWP 01/08 >>
            // sm = new SessionManager(ub, userName);
            sm = new SessionManager(ub, userName, SpringServletAccess.getApplicationContext(context));
            WebApplicationContext webApplicationContext = WebApplicationContextUtils.getWebApplicationContext(getServletContext());
            KeycloakController controller = (KeycloakController) webApplicationContext .getBean("keycloakController");

            String ocUserUuid = null;
            logger.debug("Metric1 {}",new Date());

            try {
                ocUserUuid = controller.getOcUserUuid(request);
                ub = (UserAccountBean) session.getAttribute(USER_BEAN_NAME);
            } catch (CustomRuntimeException e) {
                forwardPage(Page.ERROR);
                return;
            }
            logger.debug("Metric2: {}",new Date());

            if (ocUserUuid != null) {
                if (ub == null || StringUtils.isEmpty(ub.getName())) {
                    UserAccountDAO uDAO = new UserAccountDAO(sm.getDataSource());
                    ub = (UserAccountBean) uDAO.findByUserUuid(ocUserUuid);
                }
            }
            if (ub == null || StringUtils.isEmpty(ub.getName())) {
                if(session != null || request.isRequestedSessionIdValid() ) {
                    session.invalidate();
                    SecurityContextHolder.clearContext();
                }
                String authorizeUrl = controller.buildAuthorizeUrl(request);
                logger.info("Secure" +
                        "" +
                        "" +
                        "Controller In login_required:%%%%%%%%" + authorizeUrl);
                response.sendRedirect(authorizeUrl);
                return;
            }
            request.setAttribute("userBean", ub);
            if (processSpecificStudyEnvUuid()) {
                /* this handles the scenario when forceRenewAuth is true */
                session.removeAttribute("userRole");
                response.sendRedirect(request.getRequestURI() + "?" + STUDY_ENV_UUID  + "=" +  getParameter(request,STUDY_ENV_UUID) +  "&firstLoginCheck=true");
                return;
            }

            if (currentPublicStudy == null || currentPublicStudy.getStudyId() <= 0) {
                UserAccountDAO uDAO = new UserAccountDAO(sm.getDataSource());
                ub = (UserAccountBean) uDAO.findByUserName(ub.getName());
                session.setAttribute(USER_BEAN_NAME, ub);
                if (ub.getId() > 0 && ub.getActiveStudyId() > 0) {
                    CoreResources.setRequestSchema(request, "public");
//                    StudyParameterValueDAO spvdao = new StudyParameterValueDAO(sm.getDataSource());
                    currentPublicStudy = getStudyDao().findByPK(ub.getActiveStudyId());

//                    ArrayList studyParameters = spvdao.findParamConfigByStudy(currentPublicStudy);

//                    currentPublicStudy.setStudyParameters(studyParameters);

                    // set up the panel here, tbh
                    panel.reset();
                    /*
                     * panel.setData("Study", currentPublicStudy.getName()); panel.setData("Summary",
                     * currentPublicStudy.getSummary());
                     * panel.setData("Start Date", sdf.format(currentPublicStudy.getDatePlannedStart()));
                     * panel.setData("End Date",
                     * sdf.format(currentPublicStudy.getDatePlannedEnd())); panel.setData("Principal Investigator",
                     * currentPublicStudy.getPrincipalInvestigator());
                     */
                    session.setAttribute(STUDY_INFO_PANEL, panel);
                } else {
                    currentPublicStudy = new Study();
                }
                session.setAttribute("publicStudy", currentPublicStudy);
                request.setAttribute("requestSchema", currentPublicStudy.getSchemaName());
                if (StringUtils.isEmpty(currentPublicStudy.getUniqueIdentifier())) {
                    logger.error("No study assigned to this user:" + ub.getName() + " uuid:" + ub.getUserUuid());
                    forwardPage(Page.ERROR);
                    return;
                }
                if(currentStudy == null || currentStudy.getStudyId() == 0 )
                    currentStudy = (Study) getStudyDao().findStudyWithSPVByUniqueId(currentPublicStudy.getUniqueIdentifier());
                if (currentStudy != null) {
                    if(currentPublicStudy != null && currentPublicStudy.getStudy() != null)
                    {
                        if(currentStudy.getStudy() == null)
                            currentStudy.setStudy(getStudyDao().findStudyWithSPVByUniqueId(currentPublicStudy.getStudy().getUniqueIdentifier()));
                    }
                }
                session.setAttribute("study", currentStudy);
            }
            else {
                request.setAttribute("requestSchema", currentPublicStudy.getSchemaName());
                currentStudy = (Study) getStudyDao().findStudyWithSPVByUniqueId(currentPublicStudy.getUniqueIdentifier());
                session.setAttribute("study", currentStudy);
            }
            request.setAttribute("requestSchema", "public");
            currentRole = (StudyUserRoleBean) session.getAttribute("userRole");

            if (currentRole == null || !currentRole.isActive() || currentRole.getId() <= 0) {
                refreshUserRole(request,ub,currentPublicStudy);
                currentRole = (StudyUserRoleBean) session.getAttribute("userRole");
            }
            // YW << For the case that current role is not "invalid" but current
            // active study has been removed.
            else if (currentPublicStudy == null || currentPublicStudy.getStudyId() == 0)
                throw new Exception("No study assigned to this user");
            else if (currentRole.getId() > 0
                    && (currentPublicStudy.getStatus().equals(Status.DELETED) || currentPublicStudy.getStatus().equals(Status.AUTO_DELETED))) {
                currentRole.setRole(Role.INVALID);
                currentRole.setStatus(Status.DELETED);
                session.setAttribute("userRole", currentRole);
            }

            if (currentPublicStudy.isSite()) {
                /*
                 * The Role decription will be set depending on whether the user logged in at study lever or site level.
                 * issue-2422
                 */
                List roles = Role.toArrayList();
                for (Iterator it = roles.iterator(); it.hasNext();) {
                    Role role = (Role) it.next();
                    switch (role.getId()) {
                    case 2:
                        role.setDescription("site_Study_Coordinator");
                        break;
                    case 3:
                        role.setDescription("site_Study_Director");
                        break;
                    case 4:
                        role.setDescription("site_investigator");
                        break;
                    case 5:
                        role.setDescription("site_Data_Entry_Person");
                        break;
                    case 6:
                        role.setDescription("site_monitor");
                        break;
                    case 7:
                        role.setDescription("site_Data_Entry_Person2");
                        break;
                    case 8:
                        role.setDescription("site_Data_Entry_Participant");
                        break;
                    default:
                        // logger.info("No role matched when setting role description");
                    }
                }
            } else {
                /*
                 * If the current study is a site, we will change the role description. issue-2422
                 */
                List roles = Role.toArrayList();
                for (Iterator it = roles.iterator(); it.hasNext();) {
                    Role role = (Role) it.next();
                    switch (role.getId()) {
                    case 2:
                        role.setDescription("Study_Coordinator");
                        break;
                    case 3:
                        role.setDescription("Study_Director");
                        break;
                    case 4:
                        role.setDescription("Investigator");
                        break;
                    case 5:
                        role.setDescription("Data_Entry_Person");
                        break;
                    case 6:
                        role.setDescription("Monitor");
                        break;
                    default:
                        // logger.info("No role matched when setting role description");
                    }
                }
            }



            // YW 06-19-2007 >>

            request.setAttribute("isAdminServlet", getAdminServlet());

            request.setAttribute("advsearchStatus", isContactsModuleEnabled());

            this.request = request;
            this.response = response;

            // java.util.Enumeration en_session = session.getAttributeNames();
            // java.util.Enumeration en_request = request.getAttributeNames();
            //
            // // logging added to find problems with adding subjects, tbh
            // 102007
            // String ss_names = "session names: ";
            // while (en_session.hasMoreElements()) {
            // ss_names += " - " + en_session.nextElement();
            // }
            // logger.info(ss_names);
            //
            // // also added tbh, 102007
            // String rq_names = "request names: ";
            // while (en_request.hasMoreElements()) {
            // rq_names += " - " + en_request.nextElement();
            // }
            // logger.info(rq_names);
            if (!request.getRequestURI().endsWith("ResetPassword")) {
                passwdTimeOut();
            }
            request.setAttribute("enrollmentCapped", isEnrollmentCapped());
            request.setAttribute("requestSchema", getRequestSchema(request));
            mayProceed();
            // pingJobServer(request);
            // Set if enrollment is capped. Used by navBar.jsp to hide "Add Participant" link in the menu
            processRequest();
        } catch (InconsistentStateException ise) {
            logger.warn("InconsistentStateException: org.akaza.openclinica.control.SecureController: ", ise);
            addPageMessage(ise.getOpenClinicaMessage());
            forwardPage(ise.getGoTo());
        } catch (InsufficientPermissionException ipe) {
            logger.warn("InsufficientPermissionException: org.akaza.openclinica.control.SecureController: ", ipe);
            // addPageMessage(ipe.getOpenClinicaMessage());
            forwardPage(ipe.getGoTo());
        } catch (OutOfMemoryError ome) {
            logger.error("Memmory full in the process: ", ome);
            long heapSize = Runtime.getRuntime().totalMemory();
            session.setAttribute("ome", "yes");
        } catch (Exception e) {
            logger.error("Process is throwing exception: ", e);

            forwardPage(Page.ERROR);
        }
        logger.debug("Metric4 {}",new Date());
    }

    private boolean shouldProcessUser() {
        String path = StringUtils.substringAfterLast(request.getRequestURI(), "/");
        boolean flag = false;
        switch(path) {
            case "VerifyImportedRule":
            case "UpdateRuleSetRule":
            case "ViewRuleAssignment":
                break;
            default:
                flag = true;
                break;
        }
        return flag;
    }
    public String getRequestSchema(HttpServletRequest request) {
        switch (StringUtils.substringAfterLast(request.getRequestURI(), "/")) {
        case "ChangeStudy":
        case "DeleteStudyUserRole":
        case "DeleteUser":
        case "ListStudyUser":
        case "ViewUserAccount":
        case "ListUserAccounts":
        case "CreateUserAccount":
        case "SetUserRole":
        case "ListStudy":
        case "AuditUserActivity":
        case "EditStudyUserRole":
        case "SignStudySubject":
            return "public";
        default:
            return currentPublicStudy.getSchemaName();
        }
    }

    public static String getStackTrace(Throwable t) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw, true);
        logger.error("Error throwed in the Stack trace: ", t);
        pw.flush();
        sw.flush();
        return sw.toString();
    }

    /**
     * Handles the HTTP <code>GET</code> method.
     *
     * @param request
     * @param response
     * @throws ServletException
     * @throws java.io.IOException
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, java.io.IOException {
        try {
            logger.debug("Request");
            process(request, response);
        } catch (Exception e) {
            logger.error("Error while calling the process method: ", e);
        }
    }

    /**
     * Handles the HTTP <code>POST</code> method.
     *
     * @param request
     *            servlet request
     * @param response
     *            servlet response
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, java.io.IOException {
        try {
            logger.debug("Post");
            process(request, response);
        } catch (Exception e) {
            logger.error("Error while calling the process method:", e);
        }
    }

    /**
     * <P>
     * Forwards to a jsp page. Additions to the forwardPage() method involve checking the session for the bread crumb
     * trail
     * and setting it, if necessary. Setting it here allows the developer to only have to update the
     * <code>BreadcrumbTrail</code> class.
     *
     * @param jspPage
     *            The page to go to.
     * @param checkTrail
     *            The command to check for, and set a trail in the session.
     */
    protected void forwardPage(Page jspPage, boolean checkTrail) {
        Page page1 = Page.valueOf(jspPage.name());
        String temp;

        // YW 10-03-2007 <<
        response.setHeader("Cache-Control", "no-cache");
        response.setHeader("Pragma", "no-cache");
        response.setDateHeader("Expires", -1);
        response.setHeader("Cache-Control", "no-store");
        // YW >>

        // to load all available event based on currentStudy for Task > Add Subject
        if (currentPublicStudy != null)
            request.setAttribute("requestSchema", currentPublicStudy.getSchemaName());
        request.setAttribute("allDefsArray", this.getEventDefinitionsByCurrentStudy());
        try {
            String paramsString = Utils.getParamsString(request.getParameterMap());

            request.setAttribute("currentPageUrl", URLEncoder.encode(request.getRequestURL().toString() + "?" + paramsString, "UTF-8"));
        } catch (UnsupportedEncodingException e) {
            logger.error("Error getting parameters:", e);
        }

        if (request.getAttribute(POP_UP_URL) == null) {
            request.setAttribute(POP_UP_URL, "");
        }

        try {
            // Added 01/19/2005 for breadcrumbs, tbh
            if (checkTrail) {
                BreadcrumbTrail bt = new BreadcrumbTrail();
                if (session != null) {// added bu jxu, fixed bug for log out
                    /*
                     * ArrayList trail = (ArrayList) session.getAttribute("trail");
                     * if (trail == null) {
                     * trail = bt.generateTrail(jspPage, request);
                     * } else {
                     * bt.setTrail(trail);
                     * trail = bt.generateTrail(jspPage, request);
                     * }
                     * session.setAttribute("trail", trail);
                     */
                    panel = (StudyInfoPanel) session.getAttribute(STUDY_INFO_PANEL);
                    if (panel == null) {
                        panel = new StudyInfoPanel();
                        panel.setData(jspPage, session, request);
                    } else {
                        panel.setData(jspPage, session, request);
                    }

                    session.setAttribute(STUDY_INFO_PANEL, panel);
                }
                // we are also using checkTrail to update the panel, tbh
                // 01/31/2005
            }

            temp = page1.getFileName();
            // above added 01/19/2005, tbh
            context.getRequestDispatcher(temp).forward(request, response);
        } catch (Exception se) {
            /*
             * if ("View Notes".equals(jspPage.getTitle())) {
             * String viewNotesURL = jspPage.getFileName();
             * if (viewNotesURL != null && viewNotesURL.contains("listNotes_p_=")) {
             * String[] ps = viewNotesURL.split("listNotes_p_=");
             * String t = ps[1].split("&")[0];
             * int p = t.length() > 0 ? Integer.valueOf(t).intValue() : -1;
             * if (p > 1) {
             * viewNotesURL = viewNotesURL.replace("listNotes_p_=" + p, "listNotes_p_=" + (p - 1));
             * //forwardPage(Page.setNewPage(viewNotesURL, "View Notes"));
             * try {
             * getServletContext().getRequestDispatcher(viewNotesURL).forward(request, response);
             * } catch (ServletException e) {
             * // TODO Auto-generated catch block
             * e.printStackTrace();
             * } catch (IOException e) {
             * // TODO Auto-generated catch block
             * e.printStackTrace();
             * }
             * } else if (p <= 0) {
             * forwardPage(Page.VIEW_DISCREPANCY_NOTES_IN_STUDY);
             * }
             * }
             * }
             */ logger.error(se.getMessage(), se);
        } finally {
            page1 = null;
            jspPage = null;
            temp = null;
        }
    }

    protected void forwardPage(Page jspPage) {
        this.forwardPage(jspPage, true);
    }

    /**
     * This method supports functionality of the type
     * "if a list of entities is empty, then jump to some page and display an error message." This prevents users from
     * seeing
     * empty drop-down lists and being given error messages when they can't choose an entity from the drop-down list.
     * Use,
     * e.g.:
     * <code>addEntityList("groups", allGroups, "There are no groups to display, so you cannot add a subject to this Study.",
     * Page.SUBMIT_DATA)</code>
     *
     * @param beanName
     *            The name of the entity list as it should be stored in the request object.
     * @param list
     *            The Collection of entities.
     * @param messageIfEmpty
     *            The message to display if the collection is empty.
     * @param destinationIfEmpty
     *            The Page to go to if the collection is empty.
     * @throws InconsistentStateException
     */
    protected void addEntityList(String beanName, Collection list, String messageIfEmpty, Page destinationIfEmpty) throws InconsistentStateException {
        if (list.isEmpty()) {
            throw new InconsistentStateException(destinationIfEmpty, messageIfEmpty);
        }

        request.setAttribute(beanName, list);
    }

    /**
     * @return A blank String if this servlet is not an Administer System servlet. SecureController.ADMIN_SERVLET_CODE
     *         otherwise.
     */
    protected String getAdminServlet() {
        return "";
    }

    protected void setPopUpURL(String url) {
        if (url != null && request != null) {
            request.setAttribute(POP_UP_URL, url);
            request.setAttribute("hasPopUp", 1);
            logger.info("just set pop up url: " + url);

        }
    }

    /**
     * <p>
     * Check if an entity with passed entity id is included in studies of current user.
     * </p>
     *
     * <p>
     * Note: This method called AuditableEntityDAO.findByPKAndStudy which required
     * "The subclass must define findByPKAndStudyName before calling this
     * method. Otherwise an inactive AuditableEntityBean will be returned."
     * </p>
     * 
     * @author ywang 10-18-2007
     * @param entityId
     *            int
     * @param userName
     *            String
     * @param adao
     *            AuditableEntityDAO
     * @param ds
     *            javax.sql.DataSource
     */
    protected boolean entityIncluded(int entityId, String userName, AuditableEntityDAO adao, DataSource ds) {
        ArrayList<Study> studies = (ArrayList<Study>) getStudyDao().findAllByUserNotRemoved(userName);
        for (int i = 0; i < studies.size(); ++i) {
            Study publicStudy = studies.get(i);
            CoreResources.setRequestSchema(request, publicStudy.getSchemaName());
            Study study = getStudyDao().findByOcOID(publicStudy.getOc_oid());
            if (adao.findByPKAndStudy(entityId, study).getId() > 0) {
                return true;
            }
            // Here follow the current logic - study subjects at sites level are
            // visible to parent studies.
            if (!study.isSite()) {
                ArrayList<Study> sites = (ArrayList<Study>) getStudyDao().findAllByParent(study.getStudyId());
                if (sites.size() > 0) {
                    for (int j = 0; j < sites.size(); ++j) {
                        if (adao.findByPKAndStudy(entityId, sites.get(j)).getId() > 0) {
                            return true;
                        }
                    }
                }
            }
        }

        return false;
    }

    public String getRequestURLMinusServletPath() {
        String requestURLMinusServletPath = request.getRequestURL().toString().replaceAll(request.getServletPath(), "");
        return requestURLMinusServletPath;
    }

    public String getHostPath() {
        String requestURLMinusServletPath = getRequestURLMinusServletPath();
        return requestURLMinusServletPath.substring(0, requestURLMinusServletPath.lastIndexOf("/"));
    }

    public String getContextPath() {
        String contextPath = request.getContextPath().replaceAll("/", "");
        return contextPath;
    }

    /*
     * To check if the current study is LOCKED
     */
    public void checkStudyLocked(Page page, String message) {
        if (currentStudy.getStatus().equals(Status.LOCKED)) {
            addPageMessage(message);
            forwardPage(page);
        }
    }

    public void checkStudyLocked(String url, String message) {
        try {
            if (currentStudy.getStatus().equals(Status.LOCKED)) {
                addPageMessage(message);
                response.sendRedirect(url);
            }
        } catch (Exception ex) {
            logger.error("Error while redirecting to {} ",url, ex);
        }
    }

    /*
     * To check if the current study is FROZEN
     */

    public void checkStudyFrozen(Page page, String message) {
        if (currentStudy.getStatus().equals(Status.FROZEN)) {
            addPageMessage(message);
            forwardPage(page);
        }
    }

    public void checkStudyFrozen(String url, String message) {
        try {
            if (currentStudy.getStatus().equals(Status.FROZEN)) {
                addPageMessage(message);
                response.sendRedirect(url);
            }
        } catch (Exception ex) {
            logger.error("Error while redirecting to {} ",url,ex);
        }

    }

    public ArrayList getEventDefinitionsByCurrentStudy() {
        StudyEventDefinitionDAO studyEventDefinitionDAO = new StudyEventDefinitionDAO(sm.getDataSource());
        ArrayList<StudyEventDefinitionBean> tempList = new ArrayList();
        ArrayList<StudyEventDefinitionBean> allDefs = new ArrayList();
        if (currentStudy == null)
            return allDefs;
        if (currentStudy.isSite()) {
            allDefs = studyEventDefinitionDAO.findAllActiveByStudy(currentStudy.getStudy());
        } else {
            allDefs = studyEventDefinitionDAO.findAllActiveByStudy(currentStudy);
        }
        for (StudyEventDefinitionBean studyEventDefinition : allDefs) {
            if (!studyEventDefinition.getType().equals(COMMON)) {
                tempList.add(studyEventDefinition);
            }
        }

        return tempList;
    }

    public ArrayList getStudyGroupClassesByCurrentStudy() {
        StudyGroupClassDAO studyGroupClassDAO = new StudyGroupClassDAO(sm.getDataSource());
        StudyGroupDAO studyGroupDAO = new StudyGroupDAO(sm.getDataSource());
        int parentStudyId = currentStudy.checkAndGetParentStudyId();
        ArrayList studyGroupClasses = new ArrayList();
        if (currentStudy.isSite()) {
            studyGroupClasses = studyGroupClassDAO.findAllActiveByStudy(currentStudy.getStudy());
        } else {
            parentStudyId = currentStudy.getStudyId();
            studyGroupClasses = studyGroupClassDAO.findAllActiveByStudy(currentPublicStudy);
        }

        for (int i = 0; i < studyGroupClasses.size(); i++) {
            StudyGroupClassBean sgc = (StudyGroupClassBean) studyGroupClasses.get(i);
            ArrayList groups = studyGroupDAO.findAllByGroupClass(sgc);
            sgc.setStudyGroups(groups);
        }

        return studyGroupClasses;

    }

    protected UserDetails getUserDetails() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof UserDetails) {
            return (UserDetails) principal;
        } else {
            return null;
        }
    }

    public Boolean sendEmail(String to, String subject, String body, Boolean htmlEmail, Boolean sendMessage) throws Exception {
        return sendEmail(to, EmailEngine.getAdminEmail(), subject, body, htmlEmail, respage.getString("your_message_sent_succesfully"),
                respage.getString("mail_cannot_be_sent_to_admin"), sendMessage);
    }

    public Boolean sendEmail(String to, String subject, String body, Boolean htmlEmail) throws Exception {
        return sendEmail(to, EmailEngine.getAdminEmail(), subject, body, htmlEmail, respage.getString("your_message_sent_succesfully"),
                respage.getString("mail_cannot_be_sent_to_admin"), true);
    }

    public Boolean sendEmail(String to, String from, String subject, String body, Boolean htmlEmail) throws Exception {
        return sendEmail(to, from, subject, body, htmlEmail, respage.getString("your_message_sent_succesfully"),
                respage.getString("mail_cannot_be_sent_to_admin"), true);
    }

    public Boolean sendEmail(String to, String from, String subject, String body, Boolean htmlEmail, String successMessage, String failMessage,
            Boolean sendMessage) throws Exception {
        Boolean messageSent = true;
        try {
            JavaMailSenderImpl mailSender = (JavaMailSenderImpl) SpringServletAccess.getApplicationContext(context).getBean("mailSender");
            // @pgawade 09-Feb-2012 #issue 13201 - setting the "mail.smtp.localhost" property to localhost when java API
            // is not able to
            // retrieve the host name
            Properties javaMailProperties = mailSender.getJavaMailProperties();
            if (null != javaMailProperties) {
                if (javaMailProperties.get("mail.smtp.localhost") == null || ((String) javaMailProperties.get("mail.smtp.localhost")).equalsIgnoreCase("")) {
                    javaMailProperties.put("mail.smtp.localhost", "localhost");
                }
            }

            MimeMessage mimeMessage = mailSender.createMimeMessage();

            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, htmlEmail);
            helper.setFrom(from);
            helper.setTo(processMultipleImailAddresses(to.trim()));
            helper.setSubject(subject);
            helper.setText(body, true);

            mailSender.send(mimeMessage);
            if (successMessage != null && sendMessage) {
                addPageMessage(successMessage);
            }
            logger.debug("Email sent successfully on {}", new Date());
        } catch (MailException me) {
            logger.error("Error while sending mail: ",me);
            if (failMessage != null && sendMessage) {
                addPageMessage(failMessage);
            }
            logger.debug("Email could not be sent on {} due to: {}", new Date(), me.toString());
            messageSent = false;
        }
        return messageSent;
    }

    private InternetAddress[] processMultipleImailAddresses(String to) throws MessagingException {
        ArrayList<String> recipientsArray = new ArrayList<String>();
        StringTokenizer st = new StringTokenizer(to, ",");
        while (st.hasMoreTokens()) {
            recipientsArray.add(st.nextToken());
        }

        int sizeTo = recipientsArray.size();
        InternetAddress[] addressTo = new InternetAddress[sizeTo];
        for (int i = 0; i < sizeTo; i++) {
            addressTo[i] = new InternetAddress(recipientsArray.get(i).toString());
        }
        return addressTo;

    }

    public void dowloadFile(File f, String contentType) throws Exception {

        response.setHeader("Content-disposition", "attachment; filename=\"" + f.getName() + "\";");
        response.setContentType("text/xml");
        response.setHeader("Pragma", "public");

        ServletOutputStream op = response.getOutputStream();

        DataInputStream in = null;
        try {
            response.setContentType("text/xml");
            response.setHeader("Pragma", "public");
            response.setContentLength((int) f.length());

            byte[] bbuf = new byte[(int) f.length()];
            in = new DataInputStream(new FileInputStream(f));
            int length;
            while (in != null && (length = in.read(bbuf)) != -1) {
                op.write(bbuf, 0, length);
            }

            in.close();
            op.flush();
            op.close();
        } catch (Exception ee) {
            logger.error("Error while downloading the file: ", ee);
        } finally {
            if (in != null) {
                in.close();
            }
            if (op != null) {
                op.close();
            }
        }
    }

    public String getPageServletFileName() {
        String fileName = request.getServletPath();
        String temp = request.getPathInfo();
        if (temp != null) {
            fileName += temp;
        }
        temp = request.getQueryString();
        if (temp != null && temp.length() > 0) {
            fileName += "?" + temp;
        }
        return fileName;
    }

    public String getPageURL() {
        String url = request.getRequestURL().toString();
        String query = request.getQueryString();
        if (url != null && url.length() > 0 && query != null) {
            url += "?" + query;
        }
        return url;
    }

    public DiscrepancyNoteBean getNoteInfo(DiscrepancyNoteBean note) {
        StudySubjectDAO ssdao = new StudySubjectDAO(sm.getDataSource());
        if ("itemData".equalsIgnoreCase(note.getEntityType())) {
            int itemDataId = note.getEntityId();
            ItemDataDAO iddao = new ItemDataDAO(sm.getDataSource());
            ItemDataBean itemData = (ItemDataBean) iddao.findByPK(itemDataId);
            ItemDAO idao = new ItemDAO(sm.getDataSource());
            if (StringUtil.isBlank(note.getEntityName())) {
                ItemBean item = (ItemBean) idao.findByPK(itemData.getItemId());
                note.setEntityName(item.getName());
                request.setAttribute("item", item);
            }
            EventCRFDAO ecdao = new EventCRFDAO(sm.getDataSource());
            StudyEventDAO svdao = new StudyEventDAO(sm.getDataSource());

            EventCRFBean ec = (EventCRFBean) ecdao.findByPK(itemData.getEventCRFId());
            StudyEventBean event = (StudyEventBean) svdao.findByPK(ec.getStudyEventId());

            StudyEventDefinitionDAO seddao = new StudyEventDefinitionDAO(sm.getDataSource());
            StudyEventDefinitionBean sed = (StudyEventDefinitionBean) seddao.findByPK(event.getStudyEventDefinitionId());
            note.setEventName(sed.getName());
            if (event.getDateStarted() != null)
                note.setEventStart(event.getDateStarted());

            CRFDAO cdao = new CRFDAO(sm.getDataSource());
            CRFBean crf = cdao.findByVersionId(ec.getCRFVersionId());
            note.setCrfName(crf.getName());
            note.setEventCRFId(ec.getId());

            if (StringUtil.isBlank(note.getSubjectName())) {
                StudySubjectBean ss = (StudySubjectBean) ssdao.findByPK(ec.getStudySubjectId());
                note.setSubjectName(ss.getName());
            }

            if (note.getDiscrepancyNoteTypeId() == 0) {
                note.setDiscrepancyNoteTypeId(DiscrepancyNoteType.FAILEDVAL.getId());// default
                // value
            }

        } else if ("eventCrf".equalsIgnoreCase(note.getEntityType())) {
            int eventCRFId = note.getEntityId();
            EventCRFDAO ecdao = new EventCRFDAO(sm.getDataSource());
            StudyEventDAO svdao = new StudyEventDAO(sm.getDataSource());

            EventCRFBean ec = (EventCRFBean) ecdao.findByPK(eventCRFId);
            StudyEventBean event = (StudyEventBean) svdao.findByPK(ec.getStudyEventId());

            StudyEventDefinitionDAO seddao = new StudyEventDefinitionDAO(sm.getDataSource());
            StudyEventDefinitionBean sed = (StudyEventDefinitionBean) seddao.findByPK(event.getStudyEventDefinitionId());
            note.setEventName(sed.getName());
            note.setEventStart(event.getDateStarted());

            CRFDAO cdao = new CRFDAO(sm.getDataSource());
            CRFBean crf = cdao.findByVersionId(ec.getCRFVersionId());
            note.setCrfName(crf.getName());
            StudySubjectBean ss = (StudySubjectBean) ssdao.findByPK(ec.getStudySubjectId());
            note.setSubjectName(ss.getName());
            note.setEventCRFId(ec.getId());

        } else if ("studyEvent".equalsIgnoreCase(note.getEntityType())) {
            int eventId = note.getEntityId();
            StudyEventDAO svdao = new StudyEventDAO(sm.getDataSource());
            StudyEventBean event = (StudyEventBean) svdao.findByPK(eventId);

            StudyEventDefinitionDAO seddao = new StudyEventDefinitionDAO(sm.getDataSource());
            StudyEventDefinitionBean sed = (StudyEventDefinitionBean) seddao.findByPK(event.getStudyEventDefinitionId());
            note.setEventName(sed.getName());
            note.setEventStart(event.getDateStarted());

            StudySubjectBean ss = (StudySubjectBean) ssdao.findByPK(event.getStudySubjectId());
            note.setSubjectName(ss.getName());

        } else if ("studySub".equalsIgnoreCase(note.getEntityType())) {
            int studySubjectId = note.getEntityId();
            StudySubjectBean ss = (StudySubjectBean) ssdao.findByPK(studySubjectId);
            note.setSubjectName(ss.getName());

        } else if ("Subject".equalsIgnoreCase(note.getEntityType())) {
            int subjectId = note.getEntityId();
            StudySubjectBean ss = ssdao.findBySubjectIdAndStudy(subjectId, currentPublicStudy);
            note.setSubjectName(ss.getName());
        }

        return note;
    }

    public void checkRoleByUserAndStudy(UserAccountBean ub, Study tenantStudy) {
        if (!checkRolesByUserAndStudy(ub, tenantStudy)) {
            addPageMessage(respage.getString("no_have_correct_privilege_current_study") + " " + respage.getString("change_active_study_or_contact"));
            forwardPage(Page.MENU_SERVLET);
            return;
        }
    }

    public boolean checkRolesByUserAndStudy(UserAccountBean ub, Study tenantStudy) {
        Study study = null;

        if (StringUtils.isNotEmpty(tenantStudy.getSchemaName()))
            study = tenantStudy;
        else
            study = getStudyDao().findPublicStudy(tenantStudy.getOc_oid());

        StudyUserRoleBean studyUserRole = ub.getRoleByStudy(study.checkAndGetParentStudyId());
        StudyUserRoleBean siteUserRole = new StudyUserRoleBean();
        if (study.getStudyId() != 0) {
            siteUserRole = ub.getRoleByStudy(study.getStudyId());
        }
        if (studyUserRole.getRole().equals(Role.INVALID) && siteUserRole.getRole().equals(Role.INVALID)) {
            return false;
        }
        return true;
    }


    protected void baseUrl() throws MalformedURLException {
        /*
        String portalURL = CoreResources.getField("portalURL");
        URL pManageUrl = new URL(portalURL);

        ParticipantPortalRegistrar registrar = new ParticipantPortalRegistrar();
        Authorization pManageAuthorization = registrar.getAuthorization(currentStudy.getOid());
        String url = "";
        if (pManageAuthorization != null)
            url = pManageUrl.getProtocol() + "://" + pManageAuthorization.getStudy().getHost() + "." + pManageUrl.getHost()
                    + ((pManageUrl.getPort() > 0) ? ":" + String.valueOf(pManageUrl.getPort()) : "");
        System.out.println("the url :  " + url);
        */
        request.setAttribute("participantUrl","need_to_fix");

    }

    /**
     * A inner class designed to allow the implementation of a JUnit test case for abstract SecureController. The inner
     * class
     * allows the test case to call the outer class' private process() method.
     *
     * @author Bruce W. Perry 01/2008
     */
    public class SecureControllerTestDelegate {

        public SecureControllerTestDelegate() {
            super();
        }

        public void process(HttpServletRequest request, HttpServletResponse response) throws OpenClinicaException, UnsupportedEncodingException {
            SecureController.this.process(request, response);
        }
    }

    public  EventCRFLocker getEventCrfLocker() {
        return eventCrfLocker;
    }


    private boolean isEnrollmentCapEnforced(){
        String enrollmentCapStatus=null;
        if(currentStudy.isSite()){
            enrollmentCapStatus = currentStudy.getStudy().getEnforceEnrollmentCap();
        }else {
            enrollmentCapStatus = currentStudy.getEnforceEnrollmentCap();
        }
        boolean capEnforced = Boolean.valueOf(enrollmentCapStatus);
        return capEnforced;
    }

    protected boolean isEnrollmentCapped(){

        String previousSchema = (String) request.getAttribute("requestSchema");
        request.setAttribute("requestSchema", currentPublicStudy.getSchemaName());

        boolean capIsOn = isEnrollmentCapEnforced();

        StudySubjectDAO studySubjectDAO = new StudySubjectDAO(sm.getDataSource());
        int numberOfSubjects = studySubjectDAO.getCountofActiveStudySubjects();

        Study sb = null;
        if(currentStudy.isSite()){
            sb = (Study) currentStudy.getStudy();
        }else{
             sb = (Study) currentStudy;
        }
        int  expectedTotalEnrollment = sb.getExpectedTotalEnrollment();

        request.setAttribute("requestSchema", previousSchema);

        if (numberOfSubjects >= expectedTotalEnrollment && capIsOn)
            return true;
        else
            return false;
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


    private static ArrayList<String> extractParametersAsListFromParameterNames(Enumeration<String> parameterNames, HttpServletRequest request){
        ArrayList<String> parameters = new ArrayList<>();
        if (parameterNames.hasMoreElements()) {
            parameters = new ArrayList<>(Arrays.asList(request.getParameterNames().nextElement().split("=|&")));
        }
        return parameters;
    }

    /**
     * The parameter separators are being URL encoded hence not being interpreted as separate parameters
     * eg. MainMenu?studyEnvUuid%3D3d3a3d9e-8dc8-49ce-9800-810e665f062c%26forceRenewAuth%3Dtrue
     * @param request
     * @param parameterName
     * @return parameter value
     */
    public static String getParameter(HttpServletRequest request, String parameterName){
        String paramValue = request.getParameter(parameterName);
        if (paramValue == null){
            ArrayList<String> parameters = extractParametersAsListFromParameterNames(request.getParameterNames(),request);
            paramValue = parameters.indexOf(parameterName) > -1 ? parameters.get(parameters.indexOf(parameterName) + 1) : null;
        }
        logger.info("Getting parameter name: " + parameterName + " value: " + paramValue);
        return paramValue;
    }

    public boolean processSpecificStudyEnvUuid() throws Exception {
        boolean isRenewAuth = false;

        // Only do this for MainMenuServlet
        String path = StringUtils.substringAfterLast(request.getRequestURI(), "/");

        if (!path.equalsIgnoreCase("MainMenu")) {
            return isRenewAuth;
        }
        logger.info("MainMenuServlet processSpecificStudyEnvUuid:%%%%%%%%" + session.getAttribute("firstLoginCheck"));
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

        Study tmpPublicStudy = getStudyDao().findByStudyEnvUuid(studyEnvUuid);

        if (tmpPublicStudy == null) {
            CoreResources.setRequestSchema(request,currentSchema);
            return isRenewAuth;
        }

        studyService.updateStudyUserRoles(request, studyService.getUserAccountObject(ub), tmpPublicStudy.getStudyId(), studyEnvUuid, false);
        UserAccountDAO userAccountDAO = new UserAccountDAO(sm.getDataSource());

        ArrayList userRoleBeans = (ArrayList) userAccountDAO.findAllRolesByUserName(ub.getName());
        ub.setRoles(userRoleBeans);
        session.setAttribute(SecureController.USER_BEAN_NAME, ub);

        StudyUserRoleBean role = ub.getRoleByStudy(tmpPublicStudy.getStudyId());

        if (role.getStudyId() == 0) {
            logger.error("You have no roles for this study." + studyEnvUuid + " currentStudy is:" + tmpPublicStudy.getName() + " schema:" + tmpPublicStudy.getSchemaName());
            logger.error("Creating an invalid role, ChangeStudy page will be shown");
            currentRole = new StudyUserRoleBean();
            session.setAttribute("userRole", currentRole);
        } else {
            currentPublicStudy = tmpPublicStudy;
            CoreResources.setRequestSchema(request, currentPublicStudy.getSchemaName());
            currentStudy = getStudyDao().findStudyWithSPVByStudyEnvUuid(studyEnvUuid);

            session.setAttribute("publicStudy", currentPublicStudy);
            session.setAttribute("study", currentStudy);
            currentRole = role;
            session.setAttribute("userRole", role);
            logger.info("Found role for this study:" + role.getRoleName());
            if (ub.getActiveStudyId() == currentPublicStudy.getStudyId())
                return isRenewAuth;
            ub.setActiveStudyId(currentPublicStudy.getStudyId());
            userAccountDAO.update(ub);
        }

        return isRenewAuth;
    }

    public String getPermissionTagsString() {
        PermissionService permissionService = (PermissionService) SpringServletAccess.getApplicationContext(context).getBean("permissionService");
        String permissionTags = permissionService.getPermissionTagsString(request);
        return permissionTags;
    }
    public List<String>  getPermissionTagsList() {
        PermissionService permissionService = (PermissionService) SpringServletAccess.getApplicationContext(context).getBean("permissionService");
        List<String> permissionTagsList = permissionService.getPermissionTagsList(request);
        return permissionTagsList;
    }

    public boolean hasFormAccess(EventCrf ec) {
        Integer formLayoutId = request.getParameter("formLayoutId") != null? new Integer(request.getParameter("formLayoutId")) : null;
        Integer studyEventId = request.getParameter("studyEventId") != null? new Integer(request.getParameter("studyEventId")) : null;
        PermissionService permissionService = (PermissionService) SpringServletAccess.getApplicationContext(context).getBean("permissionService");
        return permissionService.hasFormAccess(ec, formLayoutId, studyEventId, request);
    }
    protected CustomRole checkMatchingUuid(CustomRole customRole, ChangeStudyDTO changeStudyDTO, StudyEnvironmentRoleDTO s) {
        if (StringUtils.equals(changeStudyDTO.getStudyEnvUuid().toString(), s.getStudyEnvironmentUuid())) {
            customRole.studyRoleMap.put(changeStudyDTO.getStudyId(), s.getDynamicRoleName());
            customRole.siteRoleMap.put(changeStudyDTO.getStudyId(), s.getDynamicRoleName());
        }
        return customRole;
    }

    protected void populateCustomUserRoles(CustomRole customRole, String username) {
        List<ChangeStudyDTO> byUser = getStudyDao().findByUser(username);
        List<StudyEnvironmentRoleDTO> userRoles = (List<StudyEnvironmentRoleDTO>) session.getAttribute("allUserRoles");
        if (userRoles == null) {
            logger.error("******************userRoles should not be null");
            ResponseEntity<List<StudyEnvironmentRoleDTO>> responseEntity = getStudyBuildService().getUserRoles(request, true);
            userRoles = responseEntity.getBody();
        }
        if (byUser == null) {
            logger.error("byUser variable should not be null for username:" + username);
        }
        Set<CustomRole> customRoles = userRoles.stream().flatMap(s -> byUser.stream().map(r -> checkMatchingUuid(customRole, r, s))).collect(Collectors.toSet());
    }

    protected String getParticipateStatus(Study parentStudy) {

        String participateStatus = parentStudy.getParticipantPortal();
        return participateStatus;
    }
    protected UserService getUserService() {
        return userService= (UserService) SpringServletAccess.getApplicationContext(context).getBean("userService");
    }

    protected void changeParticipantAccountStatus(Study study, StudySubjectBean studySub, UserStatus userStatus) {
        // check if particiate module enabled
        Study  parentStudy = (study.isSite()) ? study.getStudy() : study;
        String participateStatus = getParticipateStatus(parentStudy);
        if (participateStatus.equals(ENABLED) && studySub.getUserId() != 0) {
            studySub.setUserStatus(userStatus);
            StudySubjectDAO sdao = new StudySubjectDAO(sm.getDataSource());
            sdao.update(studySub);
        }
    }

    public static void refreshUserRole(HttpServletRequest req, UserAccountBean ub, Study currentPublicStudy) {
        StudyUserRoleBean currentRole = new StudyUserRoleBean();
        if (ub.getId() > 0 && currentPublicStudy != null && currentPublicStudy.getStudyId() > 0 && !currentPublicStudy.getStatus().getName().equals("removed")) {
            currentRole = ub.getRoleByStudy(currentPublicStudy.getStudyId());
            if (currentPublicStudy.isSite()) {
                StudyUserRoleBean roleInParent = ub.getRoleByStudy(currentPublicStudy.getStudy().getStudyId());
                currentRole.setRole(Role.max(currentRole.getRole(), roleInParent.getRole()));
            }
        }
        logger.debug("Setting this role in session: {}" + currentRole.getRoleName());
        req.getSession().setAttribute("userRole", currentRole);
    }

    public int getSubjectCount(Study studyBean) {
        int subjectCount = 0;
        if (studyBean != null)
            subjectCount = studyBean.getSubjectCount();

        if (subjectCount == 0) {
            StudySubjectDAO ssdao = new StudySubjectDAO(sm.getDataSource());
            ArrayList ss = ssdao.findAllBySiteId(studyBean.getStudyId());
            if (ss != null) {
                subjectCount = ss.size();
            }
        }

        return subjectCount;
    }

    protected  StudyBuildService getStudyBuildService(){
        return (StudyBuildService) SpringServletAccess.getApplicationContext(context).getBean("studyBuildService");
    }
    protected CryptoConverter getCrytoConverter() {
        return (CryptoConverter) SpringServletAccess.getApplicationContext(context).getBean("cryptoConverter");
    }

    protected ValidateService getValidateService() {
        return (ValidateService) SpringServletAccess.getApplicationContext(context).getBean("validateService");
    }

    protected StudyDao getStudyDao() {
        return (StudyDao) SpringServletAccess.getApplicationContext(context).getBean("studyDaoDomain");
    }

    protected DiscrepancyNoteDao getDiscrepancyNoteDao() {
        return (DiscrepancyNoteDao) SpringServletAccess.getApplicationContext(context).getBean("discrepancyNoteDao");
    }

    protected EnketoUrlService getEnketoUrlService() {
        return (EnketoUrlService) SpringServletAccess.getApplicationContext(context).getBean("enketoUrlService");
    }

    private String isContactsModuleEnabled(){
        String contactsModuleStatus=null;
        if(currentStudy.isSite())
            contactsModuleStatus = currentStudy.getStudy().getContactsModule();
        else
            contactsModuleStatus = currentStudy.getContactsModule();
        return contactsModuleStatus.equals(ENABLED) ? ENABLED : DISABLED;
    }

    public void changeStudy(String study_oid){
        currentPublicStudy = getStudyDao().findPublicStudy(study_oid.toUpperCase());
        String schemaName =currentPublicStudy.getSchemaName();
        CoreResources.setRequestSchema(schemaName);
        if (currentPublicStudy != null) {
            // study level
            if (currentPublicStudy.getStudy() == null) {
                currentStudy = (Study) getStudyDao().findByUniqueId(currentPublicStudy.getUniqueIdentifier());
            } else {
                // Site level
                currentStudy = (Study) getStudyDao().findByUniqueId(currentPublicStudy.getUniqueIdentifier());
            }
            session.setAttribute("study", currentStudy);
            session.setAttribute("publicStudy", currentPublicStudy);
            ub.setActiveStudyId(currentPublicStudy.getStudyId());
            UserAccountDAO userAccountDAO = new UserAccountDAO(sm.getDataSource());
            ub.setUpdater(ub);
            ub.setUpdatedDate(new java.util.Date());
            userAccountDAO.update(ub);
        }
    }

 protected PermissionService getPermissionService(){
     return  (PermissionService) SpringServletAccess.getApplicationContext(context).getBean("permissionService");

 }

}
