package org.akaza.openclinica.controller;

import org.akaza.openclinica.bean.managestudy.EventDefinitionCRFBean;
import org.akaza.openclinica.bean.managestudy.StudyBean;
import org.akaza.openclinica.bean.managestudy.StudyEventBean;
import org.akaza.openclinica.bean.managestudy.StudyEventDefinitionBean;
import org.akaza.openclinica.bean.managestudy.StudySubjectBean;
import org.akaza.openclinica.bean.submit.CRFVersionBean;
import org.akaza.openclinica.bean.submit.EventCRFBean;
import org.akaza.openclinica.bean.submit.ItemBean;
import org.akaza.openclinica.bean.submit.ItemDataBean;
import org.akaza.openclinica.bean.submit.ItemGroupMetadataBean;
import org.akaza.openclinica.dao.admin.AuditDAO;
import org.akaza.openclinica.dao.admin.CRFDAO;
import org.akaza.openclinica.dao.core.CoreResources;
import org.akaza.openclinica.dao.hibernate.EventCrfDao;
import org.akaza.openclinica.dao.login.UserAccountDAO;
import org.akaza.openclinica.dao.managestudy.EventDefinitionCRFDAO;
import org.akaza.openclinica.dao.managestudy.StudyDAO;
import org.akaza.openclinica.dao.managestudy.StudyEventDAO;
import org.akaza.openclinica.dao.managestudy.StudyEventDefinitionDAO;
import org.akaza.openclinica.dao.managestudy.StudySubjectDAO;
import org.akaza.openclinica.dao.submit.CRFVersionDAO;
import org.akaza.openclinica.dao.submit.EventCRFDAO;
import org.akaza.openclinica.dao.submit.ItemDAO;
import org.akaza.openclinica.dao.submit.ItemGroupMetadataDAO;
import org.akaza.openclinica.domain.datamap.EventCrf;
import org.akaza.openclinica.domain.rule.action.NotificationActionProcessor;
import org.akaza.openclinica.i18n.util.ResourceBundleProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.web.HttpSessionRequiredException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.servlet.ModelAndView;
import org.akaza.openclinica.view.StudyInfoPanel;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.sql.Connection;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.sql.DataSource;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;

import org.akaza.openclinica.bean.admin.CRFBean;
import org.akaza.openclinica.bean.core.Role;
import org.akaza.openclinica.bean.core.Status;
import org.akaza.openclinica.bean.core.SubjectEventStatus;
import org.akaza.openclinica.bean.login.StudyUserRoleBean;
import org.akaza.openclinica.bean.login.UserAccountBean;
import org.akaza.openclinica.bean.login.UserDTO;
import org.akaza.openclinica.controller.helper.ReportLog;
import org.akaza.openclinica.core.EmailEngine;
import org.akaza.openclinica.core.OpenClinicaMailSender;
import org.apache.commons.io.IOUtils;
import org.cdisc.ns.odm.v130_api.ODM;

import com.sun.jersey.api.core.HttpRequestContext;

/**
 * Implement the functionality for displaying a table of Event CRFs for Source Data
 * Verification. This is an autowired, multiaction Controller.
 */
@Controller
public class BatchCRFMigrationController implements Runnable {

    @Autowired
    private DataSource dataSource;

    @Autowired
    CoreResources coreResources;

    @Autowired
    JavaMailSenderImpl mailSender;

    @Autowired
    OpenClinicaMailSender openClinicaMailSender;

    protected final Logger logger = LoggerFactory.getLogger(getClass().getName());

    ResourceBundle resterms;
    ArrayList<EventCRFBean> crfMigrationReportList;
    CRFVersionBean sourceCrfVersionBean;
    CRFVersionBean targetCrfVersionBean;
    ReportLog reportLog;
    StudyBean stBean;
    CRFBean cBean;
    UserAccountBean userAccountBean;
    HttpServletRequest request;
    String urlBase;

    public BatchCRFMigrationController() {
        super();
    }

    public BatchCRFMigrationController(ArrayList<EventCRFBean> crfMigrationReportList, CRFVersionBean sourceCrfVersionBean,
            CRFVersionBean targetCrfVersionBean, ReportLog reportLog, StudyBean stBean, CRFBean cBean, HttpServletRequest request, DataSource dataSource,
            UserAccountBean userAccountBean, ResourceBundle resterms, String urlBase, OpenClinicaMailSender openClinicaMailSender) {
        this.crfMigrationReportList = crfMigrationReportList;
        this.sourceCrfVersionBean = sourceCrfVersionBean;
        this.targetCrfVersionBean = targetCrfVersionBean;
        this.reportLog = reportLog;
        this.stBean = stBean;
        this.cBean = cBean;
        this.request = request;
        this.dataSource = dataSource;
        this.userAccountBean = userAccountBean;
        this.resterms = resterms;
        this.urlBase = urlBase;
        this.openClinicaMailSender = openClinicaMailSender;
    }

    @RequestMapping(value = "/batchmigration/{filename}/downloadLogFile")
    public void getLogFile(@PathVariable("filename") String fileName, HttpServletResponse response) throws Exception {
        try {
            String logFileName = getFilePath() + File.separator + fileName;

            File fileToDownload = new File(logFileName);
            InputStream inputStream = new FileInputStream(fileToDownload);
            response.setContentType("application/force-download");
            response.setHeader("Content-Disposition", "attachment; filename=" + fileName);
            IOUtils.copy(inputStream, response.getOutputStream());
            response.flushBuffer();
            inputStream.close();
        } catch (Exception e) {
            logger.debug("Request could not be completed at this moment. Please try again.");
            e.printStackTrace();
        }

    }

    /**
     * @api {post} /pages/auth/api/v1/batchmigration/process CRF Version Migration Execution
     * @apiName runAuthBatchProcess
     * @apiPermission Authenticate using api-key. admin
     * @apiVersion 3.8.0
     * @apiParam {String} studyOid is the Target Study Oid.
     * @apiParam {String} sourceCrfVersion CRF Version Oid From.
     * @apiParam {String} targetCrfVersion CRF Version Oid To.
     * @apiParam {String} studyEventDefnlist List Of Event Definitions , when left blank, implies all events within
     *           target study.
     * @apiParam {String} sitelist List Of Sites , when left blank, implies all sites including target study
     * @apiGroup Form
     * @apiDescription This api will execute crf version migration and return an email with link of a file that include
     *                 details of the transaction.
     * @apiParamExample {json} Request-Example:
     *                  {
     *                  "studyOid" : "S_BL101" ,
     *                  "sourceCrfVersion" : "F_GROUPS_ADVER_V221" ,
     *                  "targetCrfVersion" : "F_GROUPS_ADVER_V22" ,
     *                  "studyEventDefnlist" : [] ,
     *                  "sitelist" : []
     *                  }
     * 
     * @apiErrorExample {json} Error-Response:
     *                  HTTP/1.1 406 NOT ACCEPTABLE
     *                  {
     *                  "errorList": ["Current CRF version and New CRF version can not be same."],
     *                  "reportSummary": null,
     *                  "subjectCount": 0,
     *                  "eventCrfCount": 0,
     *                  "migrationCanNotPerformList": [],
     *                  "reportLogList": []
     *                  }
     * 
     * @apiSuccessExample {json} Success-Response:
     *                    HTTP/1.1 200 OK
     *                    {
     *                    "errorList": [],
     *                    "reportSummary":
     *                    "Batch CRF version migration is running. You will receive an email once the process is complete"
     *                    ,
     *                    "subjectCount": 8,
     *                    "eventCrfCount": 12,
     *                    "migrationCanNotPerformList": [],
     *                    "reportLogList": []
     *                    }
     */

    @RequestMapping(value = "/auth/api/v1/batchmigration/process", method = RequestMethod.POST)
    public ResponseEntity<ReportLog> runAuthBatchProcess(@RequestBody HashMap<String, Object> hashMap, HttpServletRequest request) throws Exception {

        ResponseEntity<HashMap<String, Object>> res = runBatchCrfVersionMigrationSummaryReport(hashMap, request);
        HashMap<String, Object> map = res.getBody();
        ArrayList<EventCRFBean> crfMigrationReportList = (ArrayList<EventCRFBean>) map.get("crfMigrationReportList");
        CRFVersionBean sourceCrfVersionBean = (CRFVersionBean) map.get("sourceCrfVersionBean");
        CRFVersionBean targetCrfVersionBean = (CRFVersionBean) map.get("targetCrfVersionBean");
        ReportLog reportLog = (ReportLog) map.get("reportLog");
        StudyBean stBean = (StudyBean) map.get("stBean");
        CRFBean cBean = (CRFBean) map.get("cBean");
        UserAccountBean userAccountBean = (UserAccountBean) map.get("userAccountBean");
        // ResourceBundle resterms = (ResourceBundle) map.get("resterms");
        String urlBase = coreResources.getDataInfo().getProperty("sysURL").split("/MainMenu")[0];
        Locale locale = request.getLocale();
        resterms = ResourceBundleProvider.getTermsBundle(locale);

        // run migration execution process using Thread
        String str = "";
        if (reportLog.getSubjectCount() != 0 && reportLog.getEventCrfCount() != 0 && reportLog.getErrorList().size() == 0) {

            BatchCRFMigrationController bcmController = new BatchCRFMigrationController(crfMigrationReportList, sourceCrfVersionBean, targetCrfVersionBean,
                    reportLog, stBean, cBean, request, dataSource, userAccountBean, resterms, urlBase, openClinicaMailSender);
            Thread thread = new Thread(bcmController);
            thread.start();
            str = resterms.getString("Batch_CRF_version_migration_is_running_You_will_receive_an_email_once_the_process_is_complete");
            reportLog.setReportSummary(str);
            return new ResponseEntity<ReportLog>(reportLog, org.springframework.http.HttpStatus.OK);
        } else {
            if (reportLog.getErrorList().size() > 0) {
                return new ResponseEntity<ReportLog>(reportLog, org.springframework.http.HttpStatus.NOT_ACCEPTABLE);
            } else {
                str = resterms.getString("Migration_did_not_run_due_to_no_affected_subject");
                reportLog.setReportSummary(str);
                return new ResponseEntity<ReportLog>(reportLog, org.springframework.http.HttpStatus.OK);
            }
        }
    }

    @RequestMapping(value = "/api/v1/batchmigration/process", method = RequestMethod.POST)
    public @ResponseBody String runBatchProcess(HttpServletRequest request, HttpServletResponse response) throws Exception {

        HashMap<String, Object> hashMap = getUIComponents(request);
        String crfId = request.getParameter("crfId");

        ResponseEntity<HashMap<String, Object>> res = runBatchCrfVersionMigrationSummaryReport(hashMap, request);
        HashMap<String, Object> map = res.getBody();
        ArrayList<EventCRFBean> crfMigrationReportList = (ArrayList<EventCRFBean>) map.get("crfMigrationReportList");
        CRFVersionBean sourceCrfVersionBean = (CRFVersionBean) map.get("sourceCrfVersionBean");
        CRFVersionBean targetCrfVersionBean = (CRFVersionBean) map.get("targetCrfVersionBean");
        ReportLog reportLog = (ReportLog) map.get("reportLog");
        StudyBean stBean = (StudyBean) map.get("stBean");
        CRFBean cBean = (CRFBean) map.get("cBean");
        UserAccountBean userAccountBean = (UserAccountBean) map.get("userAccountBean");
        // ResourceBundle resterms = (ResourceBundle) map.get("resterms");
        String urlBase = coreResources.getDataInfo().getProperty("sysURL").split("/MainMenu")[0];
        Locale locale = request.getLocale();
        resterms = ResourceBundleProvider.getTermsBundle(locale);

        String pageMessages = null;
        if (reportLog.getSubjectCount() != 0 && reportLog.getEventCrfCount() != 0 && reportLog.getErrorList().size() == 0) {
            // run migration process using Thread
            BatchCRFMigrationController bcmController = new BatchCRFMigrationController(crfMigrationReportList, sourceCrfVersionBean, targetCrfVersionBean,
                    reportLog, stBean, cBean, request, dataSource, userAccountBean, resterms, urlBase, openClinicaMailSender);
            Thread thread = new Thread(bcmController);
            thread.start();

            pageMessages = resterms.getString("Batch_CRF_version_migration_is_running_You_will_receive_an_email_once_the_process_is_complete");
            return (String) redirect(request, response, "/ListCRF?module=manage" + "&isFromCRFVersionBatchChange=" + pageMessages);
        } else {
            pageMessages = resterms.getString("Error_in_Running_Migration_Please_try_again");
            return (String) redirect(request, response, "/BatchCRFMigration?module=manage&crfId=" + crfId + "&isFromCRFVersionBatchChange=" + pageMessages);
        }
    }

    /**
     * @api {post} /pages/auth/api/v1/batchmigration/summaryreport CRF Version Migration Summary Report
     * @apiName runAuthSummaryReport
     * @apiPermission Authenticate using api-key. admin
     * @apiVersion 3.8.0
     * @apiParam {String} studyOid is the Target Study Oid.
     * @apiParam {String} sourceCrfVersion CRF Version Oid From.
     * @apiParam {String} targetCrfVersion CRF Version Oid To.
     * @apiParam {String} studyEventDefnlist List Of Event Definitions , when left blank, implies all events within
     *           target study.
     * @apiParam {String} sitelist List Of Sites , when left blank, implies all sites including target study
     * @apiGroup Form
     * @apiDescription This api is a summary report for crf version migration and returns json object of report log.
     * @apiParamExample {json} Request-Example:
     *                  {
     *                  "studyOid" : "S_BL101" ,
     *                  "sourceCrfVersion" : "F_GROUPS_ADVER_V221" ,
     *                  "targetCrfVersion" : "F_GROUPS_ADVER_V22" ,
     *                  "studyEventDefnlist" : [] ,
     *                  "sitelist" : []
     *                  }
     * 
     * @apiErrorExample {json} Error-Response:
     *                  HTTP/1.1 406 NOT ACCEPTABLE
     *                  {
     *                  "errorList": ["The OID of the Target Study that you provided is invalid."],
     *                  "reportSummary": null,
     *                  "subjectCount": 0,
     *                  "eventCrfCount": 0,
     *                  "migrationCanNotPerformList": [],
     *                  "reportLogList": []
     *                  }
     * 
     * @apiSuccessExample {json} Success-Response:
     *                    HTTP/1.1 200 OK
     *                    {
     *                    "errorList": [],
     *                    "reportSummary": null,
     *                    "subjectCount": 8,
     *                    "eventCrfCount": 12,
     *                    "migrationCanNotPerformList": [],
     *                    "reportLogList": []
     *                    }
     */
    @RequestMapping(value = "/auth/api/v1/batchmigration/summaryreport", method = RequestMethod.POST)
    public ResponseEntity<ReportLog> runAuthSummaryReport(@RequestBody HashMap<String, Object> hashMap, HttpServletRequest request) throws Exception {
        boolean dryrun = true;

        ResponseEntity<HashMap<String, Object>> res = runBatchCrfVersionMigrationSummaryReport(hashMap, request);
        HashMap<String, Object> map = res.getBody();
        // ArrayList<EventCRFBean> crfMigrationReportList= (ArrayList<EventCRFBean>) map.get("crfMigrationReportList");
        // CRFVersionBean sourceCrfVersionBean = (CRFVersionBean) map.get("sourceCrfVersionBean");
        // CRFVersionBean targetCrfVersionBean = (CRFVersionBean) map.get("targetCrfVersionBean");
        ReportLog reportLog = (ReportLog) map.get("reportLog");

        return new ResponseEntity<ReportLog>(reportLog, org.springframework.http.HttpStatus.OK);

    }

    @RequestMapping(value = "/api/v1/batchmigration/summaryreport", method = RequestMethod.POST)
    public @ResponseBody ReportLog runSummaryReport(HttpServletRequest request, HttpServletResponse response) throws Exception {
        HashMap<String, Object> hashMap = getUIComponents(request);

        ResponseEntity<HashMap<String, Object>> res = runBatchCrfVersionMigrationSummaryReport(hashMap, request);
        HashMap<String, Object> map = res.getBody();
        // ArrayList<EventCRFBean> crfMigrationReportList= (ArrayList<EventCRFBean>) map.get("crfMigrationReportList");
        // CRFVersionBean sourceCrfVersionBean = (CRFVersionBean) map.get("sourceCrfVersionBean");
        // CRFVersionBean targetCrfVersionBean = (CRFVersionBean) map.get("targetCrfVersionBean");
        ReportLog reportLog = (ReportLog) map.get("reportLog");

        reportLog.setReportSummary(toStringHtmlFormat(reportLog, resterms));
        return reportLog;
    }

    public void executeMigrationAction(EventCRFBean eventCRFBEan, CRFVersionBean targetCrfVersionBean, HttpServletRequest request, DataSource dataSource,
            UserAccountBean userAccountBean) {
        try {
            EventCRFDAO event_crf_dao = new EventCRFDAO(dataSource);
            StudyEventDAO sedao = new StudyEventDAO(dataSource);

            EventCRFBean ev_bean = (EventCRFBean) event_crf_dao.findByPK(eventCRFBEan.getId());
            StudyEventBean st_event_bean = (StudyEventBean) sedao.findByPK(ev_bean.getStudyEventId());

            Connection con = dataSource.getConnection();
            con.setAutoCommit(false);
            event_crf_dao.updateCRFVersionID(eventCRFBEan.getId(), targetCrfVersionBean.getId(), userAccountBean.getId(), null);

            String status_before_update = null;
            SubjectEventStatus eventStatus = null;
            Status subjectStatus = null;

            // event signed, check if subject is signed as well
            StudySubjectBean studySubBean = (StudySubjectBean) ssdao().findByPK(st_event_bean.getStudySubjectId());
            if (studySubBean.getStatus().isSigned()) {
                status_before_update = auditDao().findLastStatus("study_subject", studySubBean.getId(), "8");
                if (status_before_update != null && status_before_update.length() == 1) {
                    int subject_status = Integer.parseInt(status_before_update);
                    subjectStatus = Status.get(subject_status);
                    studySubBean.setStatus(subjectStatus);
                }
                studySubBean.setUpdater(userAccountBean);
                ssdao().update(studySubBean, null);
            }
            st_event_bean.setUpdater(userAccountBean);
            st_event_bean.setUpdatedDate(new Date());

            status_before_update = auditDao().findLastStatus("study_event", st_event_bean.getId(), "8");
            if (status_before_update != null && status_before_update.length() == 1) {
                int status = Integer.parseInt(status_before_update);
                eventStatus = SubjectEventStatus.get(status);
                st_event_bean.setSubjectEventStatus(eventStatus);
            }
            sedao.update(st_event_bean, null);

            con.commit();
            con.setAutoCommit(true);
            con.close();
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    public ResponseEntity<HashMap<String, Object>> runBatchCrfVersionMigrationSummaryReport(HashMap<String, Object> hashMap, HttpServletRequest request)
            throws Exception {
        // ResourceBundleProvider.updateLocale(new Locale("en_US"));
        HashMap<String, Object> hashObject = new HashMap<String, Object>();

        Locale locale = request.getLocale();
        resterms = ResourceBundleProvider.getTermsBundle(locale);
        UserAccountBean userAccountBean = getCurrentUser(request);
        ReportLog reportLog = new ReportLog();

        String studyOid = (String) hashMap.get("studyOid");
        String sourceCrfVersion = (String) hashMap.get("sourceCrfVersion");
        String targetCrfVersion = (String) hashMap.get("targetCrfVersion");
        ArrayList<String> studyEventDefnlist = (ArrayList<String>) hashMap.get("studyEventDefnlist");
        ArrayList<String> sitelist = (ArrayList<String>) hashMap.get("sitelist");

        CRFVersionBean sourceCrfVersionBean = cvdao().findByOid(sourceCrfVersion);
        CRFVersionBean targetCrfVersionBean = cvdao().findByOid(targetCrfVersion);

        StudyBean stBean = sdao().findByOid(studyOid);
        if (stBean == null || stBean.getStatus().isUnavailable() || stBean.getParentStudyId() != 0) {
            reportLog.getErrorList().add(resterms.getString("The_OID_of_the_Target_Study_that_you_provided_is_invalid"));
            hashObject.put("reportLog", reportLog);
            return new ResponseEntity<HashMap<String, Object>>(hashObject, org.springframework.http.HttpStatus.NOT_ACCEPTABLE);
        }
        StudyUserRoleBean suRole = uadao().findRoleByUserNameAndStudyId(userAccountBean.getName(), stBean.getId());
        Role r = suRole.getRole();
        if (suRole == null || !(r.equals(Role.STUDYDIRECTOR) || r.equals(Role.COORDINATOR))) {
            reportLog.getErrorList().add(resterms.getString("You_do_not_have_permission_to_perform_CRF_version_migration_in_this_study"));
            hashObject.put("reportLog", reportLog);
            return new ResponseEntity<HashMap<String, Object>>(hashObject, org.springframework.http.HttpStatus.NOT_ACCEPTABLE);
        }
        if (sourceCrfVersionBean == null || targetCrfVersionBean == null) {
            if (sourceCrfVersion.equals("-1") || targetCrfVersion.equals("-1")) {
                reportLog.getErrorList().add(resterms.getString("Current_CRF_version_and_New_CRF_version_should_be_selected"));
            } else {
                reportLog.getErrorList().add(resterms.getString("The_OID_of_the_CRF_Version_that_you_provided_is_invalid"));
            }
            hashObject.put("reportLog", reportLog);
            return new ResponseEntity<HashMap<String, Object>>(hashObject, org.springframework.http.HttpStatus.NOT_ACCEPTABLE);
        }
        if (sourceCrfVersionBean.getId() == targetCrfVersionBean.getId()) {
            reportLog.getErrorList().add(resterms.getString("Current_CRF_version_and_New_CRF_version_can_not_be_same"));
            hashObject.put("reportLog", reportLog);
            return new ResponseEntity<HashMap<String, Object>>(hashObject, org.springframework.http.HttpStatus.NOT_ACCEPTABLE);
        }
        if (sourceCrfVersionBean.getCrfId() != targetCrfVersionBean.getCrfId() || sourceCrfVersionBean.getStatus().isUnavailable()
                || targetCrfVersionBean.getStatus().isUnavailable()) {
            reportLog.getErrorList().add(resterms.getString("The_OID_of_the_CRF_Version_that_you_provided_is_invalid"));
            hashObject.put("reportLog", reportLog);
            return new ResponseEntity<HashMap<String, Object>>(hashObject, org.springframework.http.HttpStatus.NOT_ACCEPTABLE);
        }

        CRFBean cBean = (CRFBean) cdao().findByPK(sourceCrfVersionBean.getCrfId());

        if (sitelist.size() == 0) {
            ArrayList<StudyBean> listOfSites = (ArrayList<StudyBean>) sdao().findAllByParent(stBean.getId());
            sitelist.add(stBean.getOid());
            for (StudyBean s : listOfSites) {
                if (s.getStatus().isAvailable()) {
                    sitelist.add(s.getOid());
                }
            }
        } else {
            for (String site : sitelist) {
                StudyBean siteBean = (StudyBean) sdao().findByOid(site);
                if (siteBean == null || siteBean.getStatus().isUnavailable() || getParentStudy(siteBean).getId() != stBean.getId()) {
                    reportLog.getErrorList().add(resterms.getString("The_OID_of_the_Site_that_you_provided_is_invalid"));
                    hashObject.put("reportLog", reportLog);
                    return new ResponseEntity<HashMap<String, Object>>(hashObject, org.springframework.http.HttpStatus.NOT_ACCEPTABLE);
                }
            }
        }

        if (studyEventDefnlist.size() == 0) {
            ArrayList<StudyEventDefinitionBean> listOfDefn = (ArrayList<StudyEventDefinitionBean>) seddao().findAllByStudy(stBean);
            for (StudyEventDefinitionBean d : listOfDefn) {
                if (d.getStatus().isAvailable()) {
                    studyEventDefnlist.add(d.getOid());
                }
            }
        } else {
            for (String studyEventDefn : studyEventDefnlist) {
                StudyEventDefinitionBean sedefnBean = (StudyEventDefinitionBean) seddao().findByOid(studyEventDefn);
                if (sedefnBean == null || sedefnBean.getStatus().isUnavailable() || sedefnBean.getStudyId() != stBean.getId()) {
                    reportLog.getErrorList().add(resterms.getString("The_OID_of_the_Event_that_you_provided_is_invalid"));
                    hashObject.put("reportLog", reportLog);
                    return new ResponseEntity<HashMap<String, Object>>(hashObject, org.springframework.http.HttpStatus.NOT_ACCEPTABLE);
                }
            }
        }

        // ---------
        int eventCrfCount = ssdao().getTotalEventCrfCountForCrfMigration(sourceCrfVersionBean, targetCrfVersionBean, studyEventDefnlist, sitelist);
        reportLog.setEventCrfCount(eventCrfCount);

        int subjectCount = ssdao().getTotalCountStudySubjectForCrfMigration(sourceCrfVersionBean, targetCrfVersionBean, studyEventDefnlist, sitelist);
        reportLog.setSubjectCount(subjectCount);

        ArrayList<EventDefinitionCRFBean> crfMigrationDoesNotPerformList = edcdao().findAllCrfMigrationDoesNotPerform(sourceCrfVersionBean,
                targetCrfVersionBean, studyEventDefnlist, sitelist);
        for (EventDefinitionCRFBean crfMigrationDoesNotPerform : crfMigrationDoesNotPerformList) {
            StudyEventDefinitionBean seddBean = (StudyEventDefinitionBean) seddao().findByPK(crfMigrationDoesNotPerform.getStudyEventDefinitionId());
            StudyBean sssBean = (StudyBean) sdao().findByPK(crfMigrationDoesNotPerform.getStudyId());
            reportLog.getMigrationCanNotPerformList().add(
                    resterms.getString("CRF_Version_Migration_cannot_be_performed_for") + " " + sssBean.getName() + " " + seddBean.getName() + ". "
                            + resterms.getString("Both_CRF_versions_are_not_available_at_the_Site"));
        }

        ArrayList<EventCRFBean> crfMigrationReportList = ecdao().findAllCRFMigrationReportList(sourceCrfVersionBean, targetCrfVersionBean, studyEventDefnlist,
                sitelist);

        hashObject.put("reportLog", reportLog);
        hashObject.put("stBean", stBean);
        hashObject.put("cBean", cBean);
        hashObject.put("crfMigrationReportList", crfMigrationReportList);
        hashObject.put("sourceCrfVersionBean", sourceCrfVersionBean);
        hashObject.put("targetCrfVersionBean", targetCrfVersionBean);
        hashObject.put("userAccountBean", userAccountBean);
        hashObject.put("resterms", resterms);

        return new ResponseEntity<HashMap<String, Object>>(hashObject, org.springframework.http.HttpStatus.OK);
    }

    private StudyBean getParentStudy(StudyBean study) {
        if (study.getParentStudyId() == 0) {
            return study;
        } else {
            StudyBean parentStudy = (StudyBean) sdao().findByPK(study.getParentStudyId());
            return parentStudy;
        }
    }

    private StudyDAO sdao() {
        return new StudyDAO(dataSource);
    }

    private EventCRFDAO ecdao() {
        return new EventCRFDAO(dataSource);
    }

    private StudyEventDAO sedao() {
        return new StudyEventDAO(dataSource);
    }

    private StudyEventDefinitionDAO seddao() {
        return new StudyEventDefinitionDAO(dataSource);
    }

    private StudySubjectDAO ssdao() {
        return new StudySubjectDAO(dataSource);
    }

    private EventDefinitionCRFDAO edcdao() {
        return new EventDefinitionCRFDAO(dataSource);
    }

    private UserAccountDAO uadao() {
        return new UserAccountDAO(dataSource);
    }

    private CRFDAO cdao() {
        return new CRFDAO(dataSource);
    }

    private CRFVersionDAO cvdao() {
        return new CRFVersionDAO(dataSource);
    }

    private AuditDAO auditDao() {
        return new AuditDAO(dataSource);
    }

    private UserAccountBean getCurrentUser(HttpServletRequest request) {
        UserAccountBean ub = (UserAccountBean) request.getSession().getAttribute("userBean");
        return ub;
    }

    private File createLogFile(String fileName) {
        new File(getFilePath()).mkdir();
        String logFileName = getFilePath() + File.separator + fileName;
        File logFile = new File(logFileName);
        return logFile;
    }

    private String getFilePath() {
        String versionMigrationFilePath = CoreResources.getField("filePath") + "crf_version_migration_batch_log_file";
        return versionMigrationFilePath;
    }

    private PrintWriter openFile(File file) throws FileNotFoundException, UnsupportedEncodingException {
        PrintWriter writer = new PrintWriter(file.getPath(), "UTF-8");
        return writer;
    }

    private void closeFile(PrintWriter writer) {
        writer.close();
    }

    private String getReportUrl(String filename, String urlBase) {
        String reportUrl = urlBase + "/pages/batchmigration/" + filename + "/downloadLogFile";
        return reportUrl;
    }

    public HashMap<String, Object> getUIComponents(HttpServletRequest request) {
        String selectedSourceVersion = request.getParameter("selectedSourceVersion");
        String selectedTargetVersion = request.getParameter("selectedTargetVersion");
        String selectedSites = request.getParameter("selectedSites");
        String selectedEvents = request.getParameter("selectedEvents");
        String studyOid = request.getParameter("studyOid");

        List<String> selectedSiteList = Arrays.asList(selectedSites.split(","));
        List<String> selectedEventList = Arrays.asList(selectedEvents.split(","));
        ArrayList<String> selectedSiteArrayList = new ArrayList<String>(selectedSiteList);
        ArrayList<String> selectedEventArrayList = new ArrayList<String>(selectedEventList);

        if (selectedSiteArrayList.contains("-1"))
            selectedSiteArrayList.clear();
        if (selectedEventArrayList.contains("-1"))
            selectedEventArrayList.clear();

        HashMap<String, Object> hashMap = new HashMap();
        hashMap.put("studyOid", studyOid);
        hashMap.put("sourceCrfVersion", selectedSourceVersion);
        hashMap.put("targetCrfVersion", selectedTargetVersion);
        hashMap.put("sitelist", selectedSiteArrayList);
        hashMap.put("studyEventDefnlist", selectedEventArrayList);

        return hashMap;
    }

    public String toStringTextFormat(ReportLog reportLog, ResourceBundle resterms) {

        StringBuffer text1 = new StringBuffer();
        for (String migrationPerform : reportLog.getMigrationCanNotPerformList()) {
            text1.append(migrationPerform.toString()).append('\n');
        }
        StringBuffer text2 = new StringBuffer();
        for (String error : reportLog.getErrorList()) {
            text2.append(error.toString()).append('\n');
        }

        StringBuffer text3 = new StringBuffer();
        for (String log : reportLog.getReportLogList()) {
            text3.append(log.toString()).append('\n');
        }
        String str = resterms.getString("Migration_Summary") + ":\n" + resterms.getString("Number_of_Subjects_affected_by_migration") + ": "
                + reportLog.getSubjectCount() + "\n";

        str = str + resterms.getString("Number_of_Event_CRF_affected_by_migration") + ": " + reportLog.getEventCrfCount() + "\n";

        str = str + text1.toString() + "\n";

        if (reportLog.getErrorList().size() != 0) {
            str = str + resterms.getString("Errors") + ":\n" + text2.toString() + "\n";
        }

        str = str + resterms.getString("Report_Log") + ":\n"
                + resterms.getString("CRF_Name__Origin_Version__Target_Version__Subject_ID__Site__Event__Event_Ordinal") + "\n" + text3.toString();
        return str;
    }

    public String toStringHtmlFormat(ReportLog reportLog, ResourceBundle resterms) {

        StringBuffer text1 = new StringBuffer();
        for (String migrationPerform : reportLog.getMigrationCanNotPerformList()) {
            text1.append(migrationPerform.toString()).append("<br>");
        }
        StringBuffer text2 = new StringBuffer();
        for (String error : reportLog.getErrorList()) {
            text2.append(error.toString()).append("<br>");
        }

        StringBuffer text3 = new StringBuffer();
        for (String log : reportLog.getReportLogList()) {
            text3.append(log.toString()).append("<br>");
        }
        StringBuilder sb = new StringBuilder();
        // sb.append("<font color=\"color1\">");
        // sb.append(c);
        // sb.append("</font>");
        // String str = "<br>";

        sb.append("<br>");
        sb.append("<font size=\"3\" color=\"#D4A718\"><b>");
        sb.append(resterms.getString("Migration_Summary") + ":");
        sb.append("</b></font>");
        sb.append("<br>");

        sb.append(resterms.getString("Number_of_Subjects_to_be_affected_by_migration") + ": " + reportLog.getSubjectCount() + "<br>");
        sb.append(resterms.getString("Number_of_Event_CRF_to_be_affected_by_migration") + ": " + reportLog.getEventCrfCount() + "<br>");
        sb.append("<br>");
        sb.append(text1.toString() + "<br>");

        if (reportLog.getErrorList().size() != 0) {
            sb.append("<font size=\"3\" color=\"#D4A718\" ><b>");
            sb.append(resterms.getString("Errors") + ":");
            sb.append("</b></font>");
            sb.append("<br>");

            sb.append("<font color=\"red\"><b>");
            sb.append(text2.toString());
            sb.append("</b></font>");
            sb.append("<br>");
        }

        return sb.toString();
    }

    private Object redirect(HttpServletRequest request, HttpServletResponse response, String location) {
        try {
            response.sendRedirect(request.getContextPath() + location);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;

    }

    // public void runMigration(ArrayList<EventCRFBean> crfMigrationReportList, CRFVersionBean sourceCrfVersionBean,
    // CRFVersionBean targetCrfVersionBean,
    // ReportLog reportLog, StudyBean stBean, CRFBean cBean, HttpServletRequest request) {
    @Override
    public void run() {
        // TODO Auto-generated method stub

        for (EventCRFBean crfMigrationReport : crfMigrationReportList) {
            executeMigrationAction(crfMigrationReport, targetCrfVersionBean, request, dataSource, userAccountBean);

            StudySubjectBean ssBean = (StudySubjectBean) ssdao().findByPK(crfMigrationReport.getStudySubjectId());
            StudyBean sBean = (StudyBean) sdao().findByPK(ssBean.getStudyId());
            StudyEventBean seBean = (StudyEventBean) sedao().findByPK(crfMigrationReport.getStudyEventId());
            StudyEventDefinitionBean sedBean = (StudyEventDefinitionBean) seddao().findByPK(seBean.getStudyEventDefinitionId());
            reportLog.getReportLogList().add(
                    cBean.getName() + "," + sourceCrfVersionBean.getName() + "," + targetCrfVersionBean.getName() + "," + ssBean.getLabel() + ","
                            + sBean.getName() + "," + sedBean.getName() + "," + seBean.getSampleOrdinal());
        }

        String fileName = new SimpleDateFormat("_yyyy-MM-dd-hhmmssSaa'.txt'").format(new Date());
        fileName = "logFile" + fileName;
        File file = createLogFile(fileName);
        PrintWriter writer = null;
        try {
            writer = openFile(file);
        } catch (FileNotFoundException | UnsupportedEncodingException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        writer.print(toStringTextFormat(reportLog, resterms));
        closeFile(writer);
        String reportUrl = getReportUrl(fileName, urlBase);
        System.out.println(reportUrl);
        String fullName = userAccountBean.getFirstName() + " " + userAccountBean.getLastName();
        String body = resterms.getString("Dear") + " " + fullName + ",<br><br>" + resterms.getString("Batch_CRF_version_migration_for") + " "
                + stBean.getName() + " " + resterms.getString("has_completed_running") + "<br><br>"
                + resterms.getString("A_summary_report_of_the_migration_is_available_here") + ":<br>" + reportUrl;
        System.out.println(body);
        openClinicaMailSender.sendEmail(userAccountBean.getEmail(), EmailEngine.getAdminEmail(), resterms.getString("Batch_Migration_Complete"), body, true);

    }

}
