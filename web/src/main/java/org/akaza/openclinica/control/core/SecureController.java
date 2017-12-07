/*
 * OpenClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: http://www.openclinica.org/license
 * copyright 2003-2005 Akaza Research
 */

package org.akaza.openclinica.control.core;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.StringTokenizer;

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

import org.akaza.openclinica.bean.admin.CRFBean;
import org.akaza.openclinica.bean.core.DiscrepancyNoteType;
import org.akaza.openclinica.bean.core.Role;
import org.akaza.openclinica.bean.core.Status;
import org.akaza.openclinica.bean.extract.ArchivedDatasetFileBean;
import org.akaza.openclinica.bean.login.StudyUserRoleBean;
import org.akaza.openclinica.bean.login.UserAccountBean;
import org.akaza.openclinica.bean.managestudy.DiscrepancyNoteBean;
import org.akaza.openclinica.bean.managestudy.StudyBean;
import org.akaza.openclinica.bean.managestudy.StudyEventBean;
import org.akaza.openclinica.bean.managestudy.StudyEventDefinitionBean;
import org.akaza.openclinica.bean.managestudy.StudyGroupClassBean;
import org.akaza.openclinica.bean.managestudy.StudySubjectBean;
import org.akaza.openclinica.bean.submit.EventCRFBean;
import org.akaza.openclinica.bean.submit.ItemBean;
import org.akaza.openclinica.bean.submit.ItemDataBean;
import org.akaza.openclinica.control.SpringServletAccess;
import org.akaza.openclinica.core.CRFLocker;
import org.akaza.openclinica.core.EmailEngine;
import org.akaza.openclinica.core.SessionManager;
import org.akaza.openclinica.core.form.StringUtil;
import org.akaza.openclinica.dao.admin.CRFDAO;
import org.akaza.openclinica.dao.core.AuditableEntityDAO;
import org.akaza.openclinica.dao.core.CoreResources;
import org.akaza.openclinica.dao.extract.ArchivedDatasetFileDAO;
import org.akaza.openclinica.dao.hibernate.UserAccountDao;
import org.akaza.openclinica.dao.login.UserAccountDAO;
import org.akaza.openclinica.dao.managestudy.StudyDAO;
import org.akaza.openclinica.dao.managestudy.StudyEventDAO;
import org.akaza.openclinica.dao.managestudy.StudyEventDefinitionDAO;
import org.akaza.openclinica.dao.managestudy.StudyGroupClassDAO;
import org.akaza.openclinica.dao.managestudy.StudyGroupDAO;
import org.akaza.openclinica.dao.managestudy.StudySubjectDAO;
import org.akaza.openclinica.dao.service.StudyConfigService;
import org.akaza.openclinica.dao.service.StudyParameterValueDAO;
import org.akaza.openclinica.dao.submit.EventCRFDAO;
import org.akaza.openclinica.dao.submit.ItemDAO;
import org.akaza.openclinica.dao.submit.ItemDataDAO;
import org.akaza.openclinica.exception.OpenClinicaException;
import org.akaza.openclinica.i18n.core.LocaleResolver;
import org.akaza.openclinica.i18n.util.I18nFormatUtil;
import org.akaza.openclinica.i18n.util.ResourceBundleProvider;
import org.akaza.openclinica.service.StudyBuildService;
import org.akaza.openclinica.service.StudyBuildServiceImpl;
import org.akaza.openclinica.service.pmanage.Authorization;
import org.akaza.openclinica.service.pmanage.ParticipantPortalRegistrar;
import org.akaza.openclinica.view.BreadcrumbTrail;
import org.akaza.openclinica.view.Page;
import org.akaza.openclinica.view.StudyInfoPanel;
import org.akaza.openclinica.view.StudyInfoPanelLine;
import org.akaza.openclinica.web.InconsistentStateException;
import org.akaza.openclinica.web.InsufficientPermissionException;
import org.akaza.openclinica.web.SQLInitServlet;
import org.akaza.openclinica.web.bean.EntityBeanTable;
import org.apache.commons.lang.StringUtils;
import org.quartz.JobKey;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.quartz.TriggerKey;
import org.quartz.impl.StdScheduler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

/**
 * This class enhances the Controller in several ways.
 *
 * <ol>
 * <li>The method mayProceed, for which the class is named, is declared abstract and is called before processRequest. This
 * method indicates whether the user may proceed with the action he wishes to perform (as indicated by various attributes or
 * parameters in request or session). Note, howeveer, that the method has a void return, and throws
 * InsufficientPermissionException. The intention is that if the user may not proceed with his desired action, the method
 * should throw an exception. InsufficientPermissionException will accept a Page object which indicates where the user should
 * be redirected in order to be informed that he has insufficient permission, and the process method enforces this redirection
 * by catching an InsufficientPermissionException object.
 *
 * <li>Four new members, session, request, response, and the UserAccountBean object ub have been declared protected, and are
 * set in the process method. This allows developers to avoid passing these objects between methods, and moreover it
 * accurately encodes the fact that these objects represent the state of the servlet.
 *
 * <br/>
 * In particular, please note that it is no longer necessary to generate a bean for the session manager, the current user or
 * the current study.
 *
 * <li>The method processRequest has been declared abstract. This change is unlikely to affect most code, since by custom
 * processRequest is declared in each subclass anyway.
 *
 * <li>The standard try-catch block within most processRequest methods has been included in the process method, which calls
 * the processRequest method. Therefore, subclasses may throw an Exception in the processRequest method without having to
 * handle it.
 *
 * <li>The addPageMessage method has been declared to streamline the process of setting page-level messages. The accompanying
 * showPageMessages.jsp file in jsp/include/ automatically displays all of the page messages; the developer need only include
 * this file in the jsp.
 *
 * <li>The addEntityList method makes it easy to add a Collection of EntityBeans to the request. Note that this method should
 * only be used for Collections from which one EntityBean must be selected by the user. If the Collection is empty, this
 * method will throw an InconsistentStateException, taking the user to an error page and settting a page message indicating
 * that the user may not proceed because no entities are present. Note that the error page and the error message must be
 * specified.
 * </ol>
 *
 * @author ssachs
 */
public abstract class SecureController extends HttpServlet implements SingleThreadModel {
    protected ServletContext context;
    protected SessionManager sm;
    // protected final Logger logger =
    // LoggerFactory.getLogger(getClass().getName());
    protected final Logger logger = LoggerFactory.getLogger(getClass().getName());
    protected String logDir;
    protected String logLevel;
    protected HttpSession session;
    protected HttpServletRequest request;
    protected HttpServletResponse response;
    protected UserAccountBean ub;
    protected StudyBean currentStudy;
    protected StudyBean currentPublicStudy;
    protected StudyUserRoleBean currentRole;
    protected HashMap errors = new HashMap();
    protected UserAccountDao userDaoDomain;
    private static String SCHEDULER = "schedulerFactoryBean";

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

    private CRFLocker crfLocker;

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
        crfLocker = SpringServletAccess.getApplicationContext(context).getBean(CRFLocker.class);
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
        	//@pgawade 18-Sep-2012: fix for issue #14506 (https://issuetracker.openclinica.com/view.php?id=14506#c58197)
            //addPageMessage(respage.getString("welcome") + " " + ub.getFirstName() + " " + ub.getLastName() + ". " + respage.getString("password_set"));
            // + "<a href=\"UpdateProfile\">" + respage.getString("user_profile") + " </a>");
            int pwdChangeRequired = new Integer(SQLInitServlet.getField("change_passwd_required")).intValue();
            /*if (pwdChangeRequired == 1) {
            	addPageMessage(respage.getString("welcome") + " " + ub.getFirstName() + " " + ub.getLastName() + ". " + respage.getString("password_set"));
                request.setAttribute("mustChangePass", "yes");
                forwardPage(Page.RESET_PASSWORD);
            }*/
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
                if (triggerState == Trigger.TriggerState.NONE || triggerState== Trigger.TriggerState.COMPLETE) {
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
                        if (success != null ) {

                            if (successMsg.contains("$linkURL")) {
                                successMsg = decodeLINKURL(successMsg, datasetId);
                            }

                            if(successMsg!=null && !successMsg.isEmpty())
                            {
                                addPageMessage(successMsg);
                            }
                            else {
                            addPageMessage("Your Extract is now completed. Please go to review them at <a href='ExportDataset?datasetId="
                                + datasetId + "'> Here </a>." );
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
            se.printStackTrace();
        }

    }

    private String decodeLINKURL(String successMsg, Integer datasetId) {

        ArchivedDatasetFileDAO asdfDAO = new ArchivedDatasetFileDAO(sm.getDataSource());

        ArrayList<ArchivedDatasetFileBean> fileBeans = asdfDAO.findByDatasetId(datasetId);

        successMsg =
            successMsg.replace("$linkURL", "<a href=\"" + CoreResources.getField("sysURL.base") + "AccessFile?fileId=" + fileBeans.get(0).getId()
                + "\">here </a>");

        return successMsg;
    }

    private StdScheduler getScheduler(HttpServletRequest request) {
        scheduler =
            this.scheduler != null ? scheduler : (StdScheduler) SpringServletAccess.getApplicationContext(request.getSession().getServletContext()).getBean(
                    SCHEDULER);
        return scheduler;
    }

    private void process(HttpServletRequest request, HttpServletResponse response) throws OpenClinicaException, UnsupportedEncodingException {
        request.setCharacterEncoding("UTF-8");
        session = request.getSession();
        // BWP >> 1/8/2008
        try {
            // YW 10-03-2007 <<
            session.setMaxInactiveInterval(Integer.parseInt(SQLInitServlet.getField("max_inactive_interval")));
            String smURL = CoreResources.getField("smURL");
            if (StringUtils.isNotEmpty(smURL)) {

                int index = smURL.indexOf("//");
                String protocol = smURL.substring(0, index) + "//";
                String subDomain = smURL.substring(smURL.indexOf("//")  + 2,  smURL.indexOf("/", protocol.length()));
                String crossStorageURL = protocol + subDomain + "/hub/hub.html";
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
        currentStudy = (StudyBean) session.getAttribute("study");
        currentPublicStudy  = (StudyBean) session.getAttribute("publicStudy");
        currentRole = (StudyUserRoleBean) session.getAttribute("userRole");

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
             * if(sm==null && (!StringUtil.isBlank(userName))) {//check if user logged in, then create a new sessionmanger to
             * get ub //create a new sm in order to get a new ub object sm = new SessionManager(ub, userName); }
             */
            // BWP 01/08 >>
            // sm = new SessionManager(ub, userName);
            sm = new SessionManager(ub, userName, SpringServletAccess.getApplicationContext(context));
            if (ub == null || StringUtils.isEmpty(ub.getName())) {
                UserAccountDAO uDAO = new UserAccountDAO(sm.getDataSource());
                ub = (UserAccountBean) uDAO.findByEmail(userName);
                session.setAttribute("userBean", ub);
            }
            request.setAttribute("userBean", ub);
            StudyDAO sdao = new StudyDAO(sm.getDataSource());
            if (currentPublicStudy == null || currentPublicStudy.getId() <= 0) {
                UserAccountDAO uDAO = new UserAccountDAO(sm.getDataSource());
                ub = (UserAccountBean) uDAO.findByUserName(ub.getName());
                session.setAttribute(USER_BEAN_NAME, ub);
                if (ub.getId() > 0 && ub.getActiveStudyId() > 0) {
                    CoreResources.setRequestSchema(request, "public");
                    StudyParameterValueDAO spvdao = new StudyParameterValueDAO(sm.getDataSource());
                    currentPublicStudy = (StudyBean) sdao.findByPK(ub.getActiveStudyId());

                    ArrayList studyParameters = spvdao.findParamConfigByStudy(currentPublicStudy);

                    currentPublicStudy.setStudyParameters(studyParameters);

                    // set up the panel here, tbh
                    panel.reset();
                    /*
                     * panel.setData("Study", currentPublicStudy.getName()); panel.setData("Summary", currentPublicStudy.getSummary());
                     * panel.setData("Start Date", sdf.format(currentPublicStudy.getDatePlannedStart())); panel.setData("End Date",
                     * sdf.format(currentPublicStudy.getDatePlannedEnd())); panel.setData("Principal Investigator",
                     * currentPublicStudy.getPrincipalInvestigator());
                     */
                    session.setAttribute(STUDY_INFO_PANEL, panel);
                } else {
                    currentPublicStudy = new StudyBean();
                }
                session.setAttribute("publicStudy", currentPublicStudy);
                request.setAttribute("requestSchema", currentPublicStudy.getSchemaName());
                currentStudy = (StudyBean) sdao.findByUniqueIdentifier(currentPublicStudy.getIdentifier());
                if (currentStudy != null) {
                    currentStudy.setParentStudyName(currentPublicStudy.getParentStudyName());
                    StudyConfigService scs = new StudyConfigService(sm.getDataSource());
                    if (currentStudy.getParentStudyId() <= 0) {// top study
                        scs.setParametersForStudy(currentStudy);

                    } else {
                        // YW <<
                        currentStudy.setParentStudyName(((StudyBean) sdao.findByPK(currentStudy.getParentStudyId())).getName());
                        // YW >>
                        scs.setParametersForSite(currentStudy);
                    }
                }
                request.setAttribute("requestSchema", "public");
                session.setAttribute("study", currentStudy);
            } else if (currentPublicStudy.getId() > 0) {
                // YW 06-20-2007<< set site's parentstudy name when site is
                // restored
                if (currentPublicStudy.getParentStudyId() > 0) {
                    currentPublicStudy.setParentStudyName(((StudyBean) sdao.findByPK(currentPublicStudy.getParentStudyId())).getName());
                    request.setAttribute("requestSchema", currentPublicStudy.getSchemaName());
                    currentStudy.setParentStudyName(((StudyBean) sdao.findByPK(currentStudy.getParentStudyId())).getName());
                    request.setAttribute("requestSchema", "public");
                }
                // YW >>
            }

            if (currentPublicStudy.getParentStudyId() > 0) {
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

            if (currentRole == null || currentRole.getId() <= 0) {
                // if (ub.getId() > 0 && currentPublicStudy.getId() > 0) {
                // if current study has been "removed", current role will be
                // kept as "invalid" -- YW 06-21-2007
                if (ub.getId() > 0 && currentPublicStudy.getId() > 0 && !currentPublicStudy.getStatus().getName().equals("removed")) {
                    currentRole = ub.getRoleByStudy(currentPublicStudy.getId());
                    if (currentPublicStudy.getParentStudyId() > 0) {
                        // Checking if currentPublicStudy has been removed or not will
                        // ge good enough -- YW 10-17-2007
                        StudyUserRoleBean roleInParent = ub.getRoleByStudy(currentPublicStudy.getParentStudyId());
                        // inherited role from parent study, pick the higher
                        // role
                        currentRole.setRole(Role.max(currentRole.getRole(), roleInParent.getRole()));
                    }
                    // logger.info("currentRole:" + currentRole.getRoleName());
                } else {
                    currentRole = new StudyUserRoleBean();
                }
                session.setAttribute("userRole", currentRole);
            }
            // YW << For the case that current role is not "invalid" but current
            // active study has been removed.
            else if (currentRole.getId() > 0 && (currentPublicStudy.getStatus().equals(Status.DELETED) || currentPublicStudy.getStatus().equals(Status.AUTO_DELETED))) {
                currentRole.setRole(Role.INVALID);
                currentRole.setStatus(Status.DELETED);
                session.setAttribute("userRole", currentRole);
            }
            // YW 06-19-2007 >>

            request.setAttribute("isAdminServlet", getAdminServlet());

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
            request.setAttribute("requestSchema", getRequestSchema(request));
            mayProceed();
      //      pingJobServer(request);
            processRequest();
        } catch (InconsistentStateException ise) {
            ise.printStackTrace();
            logger.warn("InconsistentStateException: org.akaza.openclinica.control.SecureController: " + ise.getMessage());

            addPageMessage(ise.getOpenClinicaMessage());
            forwardPage(ise.getGoTo());
        } catch (InsufficientPermissionException ipe) {
            ipe.printStackTrace();
            logger.warn("InsufficientPermissionException: org.akaza.openclinica.control.SecureController: " + ipe.getMessage());

            // addPageMessage(ipe.getOpenClinicaMessage());
            forwardPage(ipe.getGoTo());
        } catch (OutOfMemoryError ome) {
            ome.printStackTrace();
            long heapSize = Runtime.getRuntime().totalMemory();
            session.setAttribute("ome", "yes");
        } catch (Exception e) {
            e.printStackTrace();
            logger.error(SecureController.getStackTrace(e));

            forwardPage(Page.ERROR);
        }
    }

    public String getRequestSchema(HttpServletRequest request) {
        switch(StringUtils.substringAfterLast(request.getRequestURI(), "/")) {
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
        t.printStackTrace(pw);
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
            e.printStackTrace();
        }
    }

    /**
     * Handles the HTTP <code>POST</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, java.io.IOException {
        try {
            logger.debug("Post");
            process(request, response);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * <P>
     * Forwards to a jsp page. Additions to the forwardPage() method involve checking the session for the bread crumb trail
     * and setting it, if necessary. Setting it here allows the developer to only have to update the
     * <code>BreadcrumbTrail</code> class.
     *
     * @param jspPage The page to go to.
     * @param checkTrail The command to check for, and set a trail in the session.
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

        // to load all available event from task > add subject
        request.setAttribute("allDefsArray", this.getEventDefinitionsByCurrentStudy());

        if (request.getAttribute(POP_UP_URL) == null) {
            request.setAttribute(POP_UP_URL, "");
        }

        try {
            // Added 01/19/2005 for breadcrumbs, tbh
            if (checkTrail) {
                BreadcrumbTrail bt = new BreadcrumbTrail();
                if (session != null) {// added bu jxu, fixed bug for log out
                /*    ArrayList trail = (ArrayList) session.getAttribute("trail");
                    if (trail == null) {
                        trail = bt.generateTrail(jspPage, request);
                    } else {
                        bt.setTrail(trail);
                        trail = bt.generateTrail(jspPage, request);
                    }
                    session.setAttribute("trail", trail);*/
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
/*            if ("View Notes".equals(jspPage.getTitle())) {
                String viewNotesURL = jspPage.getFileName();
                if (viewNotesURL != null && viewNotesURL.contains("listNotes_p_=")) {
                    String[] ps = viewNotesURL.split("listNotes_p_=");
                    String t = ps[1].split("&")[0];
                    int p = t.length() > 0 ? Integer.valueOf(t).intValue() : -1;
                    if (p > 1) {
                        viewNotesURL = viewNotesURL.replace("listNotes_p_=" + p, "listNotes_p_=" + (p - 1));
                        //forwardPage(Page.setNewPage(viewNotesURL, "View Notes"));
                    	try {
							getServletContext().getRequestDispatcher(viewNotesURL).forward(request, response);
						} catch (ServletException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
                    } else if (p <= 0) {
                        forwardPage(Page.VIEW_DISCREPANCY_NOTES_IN_STUDY);
                    }
                }
            }
*/          	logger.error(se.getMessage(),se);
        }
        finally {
        	page1 = null;
        	jspPage = null;
        	temp= null;
        }
    }

    protected void forwardPage(Page jspPage) {
        this.forwardPage(jspPage, true);
    }

    /**
     * This method supports functionality of the type
     * "if a list of entities is empty, then jump to some page and display an error message." This prevents users from seeing
     * empty drop-down lists and being given error messages when they can't choose an entity from the drop-down list. Use,
     * e.g.:
     * <code>addEntityList("groups", allGroups, "There are no groups to display, so you cannot add a subject to this Study.",
     * Page.SUBMIT_DATA)</code>
     *
     * @param beanName The name of the entity list as it should be stored in the request object.
     * @param list The Collection of entities.
     * @param messageIfEmpty The message to display if the collection is empty.
     * @param destinationIfEmpty The Page to go to if the collection is empty.
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
     * <p>Check if an entity with passed entity id is included in studies of current user.</p>
     *
     * <p>Note: This method called AuditableEntityDAO.findByPKAndStudy which required
     * "The subclass must define findByPKAndStudyName before calling this
     * method. Otherwise an inactive AuditableEntityBean will be returned."</p>
     * @author ywang 10-18-2007
     * @param entityId int
     * @param userName String
     * @param adao AuditableEntityDAO
     * @param ds javax.sql.DataSource
     */
    protected boolean entityIncluded(int entityId, String userName, AuditableEntityDAO adao, DataSource ds) {
        StudyDAO sdao = new StudyDAO(ds);
        ArrayList<StudyBean> studies = (ArrayList<StudyBean>) sdao.findAllByUserNotRemoved(userName);
        for (int i = 0; i < studies.size(); ++i) {
            StudyBean publicStudy = studies.get(i);
            CoreResources.setRequestSchema(request, publicStudy.getSchemaName());
            StudyBean study = sdao.findByOid(publicStudy.getOid());
            if (adao.findByPKAndStudy(entityId, study).getId() > 0) {
                return true;
            }
            // Here follow the current logic - study subjects at sites level are
            // visible to parent studies.
            if (study.getParentStudyId() <= 0) {
                ArrayList<StudyBean> sites = (ArrayList<StudyBean>) sdao.findAllByParent(study.getId());
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
            ex.printStackTrace();
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
            ex.printStackTrace();
        }

    }

    public ArrayList getEventDefinitionsByCurrentStudy() {
        StudyDAO studyDAO = new StudyDAO(sm.getDataSource());
        StudyEventDefinitionDAO studyEventDefinitionDAO = new StudyEventDefinitionDAO(sm.getDataSource());
        ArrayList allDefs = new ArrayList();
        if (currentStudy == null)
            return allDefs;
        int parentStudyId = currentStudy.getParentStudyId();
        if (parentStudyId > 0) {
            StudyBean parentStudy = (StudyBean) studyDAO.findByPK(parentStudyId);
            allDefs = studyEventDefinitionDAO.findAllActiveByStudy(parentStudy);
        } else {
            parentStudyId = currentStudy.getId();
            allDefs = studyEventDefinitionDAO.findAllActiveByStudy(currentStudy);
        }
        return allDefs;
    }

    public ArrayList getStudyGroupClassesByCurrentStudy() {
        StudyDAO studyDAO = new StudyDAO(sm.getDataSource());
        StudyGroupClassDAO studyGroupClassDAO = new StudyGroupClassDAO(sm.getDataSource());
        StudyGroupDAO studyGroupDAO = new StudyGroupDAO(sm.getDataSource());
        int parentStudyId = currentStudy.getParentStudyId();
        ArrayList studyGroupClasses = new ArrayList();
        if (parentStudyId > 0) {
            StudyBean parentStudy = (StudyBean) studyDAO.findByPK(parentStudyId);
            studyGroupClasses = studyGroupClassDAO.findAllActiveByStudy(parentStudy);
        } else {
            parentStudyId = currentStudy.getId();
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
            //@pgawade 09-Feb-2012 #issue 13201 - setting the "mail.smtp.localhost" property to localhost when java API is not able to
            //retrieve the host name
            Properties javaMailProperties = mailSender.getJavaMailProperties();
            if(null != javaMailProperties){
            	if (javaMailProperties.get("mail.smtp.localhost") == null || ((String)javaMailProperties.get("mail.smtp.localhost")).equalsIgnoreCase("") ){
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
            me.printStackTrace();
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
            ee.printStackTrace();
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
    public void checkRoleByUserAndStudy(UserAccountBean ub, StudyBean tenantStudy, StudyDAO studyDAO){
        StudyBean study = null;

        if (StringUtils.isNotEmpty(tenantStudy.getSchemaName()))
            study = tenantStudy;
        else
            study = studyDAO.getPublicStudy(tenantStudy.getOid());

        StudyUserRoleBean studyUserRole = ub.getRoleByStudy(study.getParentStudyId());
        StudyUserRoleBean siteUserRole = new StudyUserRoleBean();
        if (study.getId() != 0) {
            siteUserRole = ub.getRoleByStudy(study.getId());
        }
        if(studyUserRole.getRole().equals(Role.INVALID) && siteUserRole.getRole().equals(Role.INVALID)){
            addPageMessage(respage.getString("no_have_correct_privilege_current_study")
                    + " " + respage.getString("change_active_study_or_contact"));
            forwardPage(Page.MENU_SERVLET);
            return;
        }
    }

    protected void baseUrl() throws MalformedURLException{
        String portalURL = CoreResources.getField("portalURL");
        URL pManageUrl = new URL(portalURL);

    ParticipantPortalRegistrar registrar = new ParticipantPortalRegistrar();
    Authorization pManageAuthorization = registrar.getAuthorization(currentStudy.getOid());
    String url="";
    if (pManageAuthorization!=null)
          url = pManageUrl.getProtocol() + "://" + pManageAuthorization.getStudy().getHost() + "." + pManageUrl.getHost()
                    + ((pManageUrl.getPort() > 0) ? ":" + String.valueOf(pManageUrl.getPort()) : "");
        System.out.println("the url :  "+ url);
        request.setAttribute("participantUrl",url+"/");

    }


    /**
     * A inner class designed to allow the implementation of a JUnit test case for abstract SecureController. The inner class
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


    public CRFLocker getCrfLocker() {
        return crfLocker;
    }




}
