package org.akaza.openclinica.web.job;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

import javax.sql.DataSource;

import org.akaza.openclinica.bean.admin.TriggerBean;
import org.akaza.openclinica.bean.core.DataEntryStage;
import org.akaza.openclinica.bean.core.DiscrepancyNoteType;
import org.akaza.openclinica.bean.core.ResolutionStatus;
import org.akaza.openclinica.bean.core.Status;
import org.akaza.openclinica.bean.login.UserAccountBean;
import org.akaza.openclinica.bean.managestudy.DiscrepancyNoteBean;
import org.akaza.openclinica.bean.managestudy.StudyBean;
import org.akaza.openclinica.bean.managestudy.StudyEventBean;
import org.akaza.openclinica.bean.managestudy.StudySubjectBean;
import org.akaza.openclinica.bean.rule.XmlSchemaValidationHelper;
import org.akaza.openclinica.bean.submit.DisplayItemBean;
import org.akaza.openclinica.bean.submit.DisplayItemBeanWrapper;
import org.akaza.openclinica.bean.submit.EventCRFBean;
import org.akaza.openclinica.bean.submit.ItemBean;
import org.akaza.openclinica.bean.submit.ItemDataBean;
import org.akaza.openclinica.bean.submit.crfdata.ODMContainer;
import org.akaza.openclinica.bean.submit.crfdata.SubjectDataBean;
import org.akaza.openclinica.bean.submit.crfdata.SummaryStatsBean;
import org.akaza.openclinica.control.submit.ImportCRFInfoContainer;
import org.akaza.openclinica.core.OpenClinicaMailSender;
import org.akaza.openclinica.dao.admin.AuditEventDAO;
import org.akaza.openclinica.dao.core.CoreResources;
import org.akaza.openclinica.dao.login.UserAccountDAO;
import org.akaza.openclinica.dao.managestudy.DiscrepancyNoteDAO;
import org.akaza.openclinica.dao.managestudy.StudyDAO;
import org.akaza.openclinica.dao.managestudy.StudySubjectDAO;
import org.akaza.openclinica.dao.submit.EventCRFDAO;
import org.akaza.openclinica.dao.submit.ItemDAO;
import org.akaza.openclinica.dao.submit.ItemDataDAO;
import org.akaza.openclinica.exception.OpenClinicaException;
import org.akaza.openclinica.exception.OpenClinicaSystemException;
import org.akaza.openclinica.i18n.util.ResourceBundleProvider;
import org.akaza.openclinica.logic.rulerunner.ExecutionMode;
import org.akaza.openclinica.logic.rulerunner.ImportDataRuleRunnerContainer;
import org.akaza.openclinica.service.rule.RuleSetServiceInterface;
import org.akaza.openclinica.web.SQLInitServlet;
import org.akaza.openclinica.web.crfdata.ImportCRFDataService;
import org.apache.commons.lang.StringUtils;
import org.exolab.castor.mapping.Mapping;
import org.exolab.castor.xml.Unmarshaller;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.SchedulerException;
import org.quartz.SimpleTrigger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.scheduling.quartz.QuartzJobBean;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;

/**
 * Import Spring Job, a job running asynchronously on the Tomcat server using Spring and Quartz.
 * 
 * @author thickerson, 04/2009
 * 
 */
public class ImportSpringJob extends QuartzJobBean {

    protected final Logger logger = LoggerFactory.getLogger(getClass().getName());

    XmlSchemaValidationHelper schemaValidator = new XmlSchemaValidationHelper();
    public static final String DIR_PATH = "scheduled_data_import";

    ResourceBundle respage;
    ResourceBundle resword;

    Locale locale;
    /*
     * variables to be pulled out of the JobDataMap. Note that these are stored in binary format in the database.
     */
    public static final String DIRECTORY = "filePathDir";
    public static final String EMAIL = "contactEmail";
    public static final String USER_ID = "user_id";
    public static final String STUDY_NAME = "study_name";
    public static final String STUDY_OID = "study_oid";
    public static final String DEST_DIR = "Event_CRF_Data";

    // below is the directory where we copy the files to, our target
    private static final String IMPORT_DIR = SQLInitServlet.getField("filePath") + DIR_PATH + File.separator; // +

    public static final String IMPORT_DIR_2 = SQLInitServlet.getField("filePath") + DEST_DIR + File.separator;

    private DataSource dataSource;
    private OpenClinicaMailSender mailSender;
    private ImportCRFDataService dataService;
    private ItemDataDAO itemDataDao;// = new ItemDataDAO(sm.getDataSource());
    private EventCRFDAO eventCrfDao;// = new EventCRFDAO(sm.getDataSource());
    private AuditEventDAO auditEventDAO;
    private TriggerService triggerService;

    @Override
    protected void executeInternal(final JobExecutionContext context) throws JobExecutionException {
        ApplicationContext appContext;
        try {
            appContext = (ApplicationContext) context.getScheduler().getContext().get("applicationContext");
            TransactionTemplate transactionTemplate = (TransactionTemplate) appContext.getBean("sharedTransactionTemplate");
            transactionTemplate.execute(new TransactionCallbackWithoutResult() {
                @Override
                protected void doInTransactionWithoutResult(TransactionStatus status) {
                    executeInternalInTransaction(context);
                }
            });
        } catch (SchedulerException e) {
            throw new JobExecutionException(e);
        }
    }

    protected void executeInternalInTransaction(JobExecutionContext context) {
        locale = new Locale("en-US");
        ResourceBundleProvider.updateLocale(locale);
        respage = ResourceBundleProvider.getPageMessagesBundle();
        resword = ResourceBundleProvider.getWordsBundle();
        triggerService = new TriggerService();

        JobDataMap dataMap = context.getMergedJobDataMap();
        SimpleTrigger trigger = (SimpleTrigger) context.getTrigger();
        TriggerBean triggerBean = new TriggerBean();
        triggerBean.setFullName(trigger.getKey().getName());
        String contactEmail = dataMap.getString(EMAIL);
        logger.debug("=== starting to run trigger " + trigger.getKey().getName() + " ===");
        try {
            ApplicationContext appContext = (ApplicationContext) context.getScheduler().getContext().get("applicationContext");
            dataSource = (DataSource) appContext.getBean("dataSource");
            mailSender = (OpenClinicaMailSender) appContext.getBean("openClinicaMailSender");
            RuleSetServiceInterface ruleSetService = (RuleSetServiceInterface) appContext.getBean("ruleSetService");

            itemDataDao = new ItemDataDAO(dataSource);
            eventCrfDao = new EventCRFDAO(dataSource);
            auditEventDAO = new AuditEventDAO(dataSource);

            int userId = dataMap.getInt(USER_ID);
            UserAccountDAO userAccountDAO = new UserAccountDAO(dataSource);

            UserAccountBean ub = (UserAccountBean) userAccountDAO.findByPK(userId);
            triggerBean.setUserAccount(ub);

            String directory = dataMap.getString(DIRECTORY);
            String studyName = dataMap.getString(STUDY_NAME);
            String studyOid = dataMap.getString(STUDY_OID);
            String localeStr = dataMap.getString(ExampleSpringJob.LOCALE);
            if (localeStr != null) {
                locale = new Locale(localeStr);
                ResourceBundleProvider.updateLocale(locale);
                respage = ResourceBundleProvider.getPageMessagesBundle();
                resword = ResourceBundleProvider.getWordsBundle();
            }
            StudyDAO studyDAO = new StudyDAO(dataSource);
            StudyBean studyBean;
            if (studyOid != null) {
                studyBean = studyDAO.findByOid(studyOid);
            } else {
                studyBean = (StudyBean) studyDAO.findByName(studyName);
            }
            // might also need study id here for the data service?
            File fileDirectory = new File(SQLInitServlet.getField("filePath") + DIR_PATH + File.separator);
            // File fileDirectory = new File(IMPORT_DIR);
            if ("".equals(directory)) { // avoid NPEs
                // do nothing here?
            } else {
                // there is a separator at the end of IMPORT_DIR already...
                // fileDirectory = new File(IMPORT_DIR + directory +
                // File.separator);
                fileDirectory = new File(SQLInitServlet.getField("filePath") + DIR_PATH + File.separator + directory + File.separator);
            }
            if (!fileDirectory.isDirectory()) {
                fileDirectory.mkdirs();
            }
            // this is necessary the first time this is run, tbh
            // File destDirectory = new File(IMPORT_DIR_2);
            File destDirectory = new File(SQLInitServlet.getField("filePath") + DEST_DIR + File.separator);
            if (!destDirectory.isDirectory()) {
                destDirectory.mkdirs();
            }
            // look at directory, if there are new files, move them over and
            // read them
            // File fileDirectory = new File(directory);
            String[] files = fileDirectory.list();
            logger.debug("found " + files.length + " files under directory " + SQLInitServlet.getField("filePath") + DIR_PATH + File.separator + directory);
            File[] target = new File[files.length];
            File[] destination = new File[files.length];
            for (int i = 0; i < files.length; i++) {
                // hmm
                if (!new File(fileDirectory + File.separator + files[i]).isDirectory()) {
                    File f = new File(fileDirectory + File.separator + files[i]);
                    if (f == null || f.getName() == null) {
                        logger.debug("found a null file");
                    } else if (f.getName().indexOf(".xml") < 0 && f.getName().indexOf(".XML") < 0) {
                        logger.debug("does not seem to be an xml file");

                        // we need a place holder to avoid 'gaps' in the file
                        // list
                    } else {
                        logger.debug("adding: " + f.getName());
                        target[i] = f;// new File(IMPORT_DIR +
                        // directory +
                        // File.separator + files[i]);
                        // destination[i] = new File(IMPORT_DIR_2 + files[i]);
                        destination[i] = new File(SQLInitServlet.getField("filePath") + DEST_DIR + File.separator + files[i]);
                    }
                }
            }
            if (target.length > 0 && destination.length > 0) {
                cutAndPaste(target, destination);
                // @pgawade 28-June-2012: Fix for issue #13964 - Remove the null
                // elements from destination array of files
                // which might be created because of presense of sub-directories
                // or non-xml files under scheduled_data_import directory
                // which are non-usable files for import.
                destination = removeNullElements(destination);
                // do everything else here with 'destination'
                ArrayList<String> auditMessages = processData(destination, dataSource, respage, resword, ub, studyBean, destDirectory, triggerBean,
                        ruleSetService);

                auditEventDAO.createRowForExtractDataJobSuccess(triggerBean, auditMessages.get(1));
                try {
                    if (contactEmail != null && !"".equals(contactEmail)) {
                        mailSender.sendEmail(contactEmail, respage.getString("job_ran_for") + " " + triggerBean.getFullName(),
                                generateMsg(auditMessages.get(0), contactEmail), true);
                        logger.debug("email body: " + auditMessages.get(1));
                    }
                } catch (OpenClinicaSystemException e) {
                    // Do nothing
                    logger.error("=== throw an ocse === " + e.getMessage());
                    e.printStackTrace();
                }

            } else {
                logger.debug("no real files found");
                auditEventDAO.createRowForExtractDataJobSuccess(triggerBean, respage.getString("job_ran_but_no_files"));
                // no email here, tbh
            }

            // use the business logic to go through each one and import that
            // data

            // check to see if they were imported before?

            // using the four methods:
            // importCRFDataServce.validateStudyMetadata,
            // service.lookupValidationErrors, service.fetchEventCRFBeans(?),
            // and
            // service.generateSummaryStatsBean(for the email we send out later)
        } catch (Exception e) {
            // more detailed reporting here
            logger.error("found a fail exception: " + e.getMessage());
            e.printStackTrace();
            auditEventDAO.createRowForExtractDataJobFailure(triggerBean, e.getMessage());
            try {
                mailSender.sendEmail(contactEmail, respage.getString("job_failure_for") + " " + triggerBean.getFullName(), e.getMessage(), true);
            } catch (OpenClinicaSystemException ose) {
                // Do nothing
                logger.error("=== throw an ocse: " + ose.getMessage());

            }
        }
    }

    private ImportCRFDataService getImportCRFDataService(DataSource dataSource) {
        // TODO dynamic locale?
        // Locale locale = new Locale("en-US");
        dataService = this.dataService != null ? dataService : new ImportCRFDataService(dataSource);
        return dataService;
    }

    private String generateMsg(String msg, String contactEmail) {
        // @pgawade 07-Sept-2012: fix for https://issuetracker.openclinica.com/view.php?id=13261#c57418
        String returnMe = respage.getString("html_email_header_1") + " " + contactEmail + respage.getString("your_job_ran_success_html") + "  "
                + respage.getString("please_review_the_data_html") + msg;
        return returnMe;
    }

    /*
     * processData, a method which should take in all XML files, check to see if they were imported previously, ? insert
     * them into the database if not, and return a message which will go to audit and to the end user.
     */
    private ArrayList<String> processData(File[] dest, DataSource dataSource, ResourceBundle respage, ResourceBundle resword, UserAccountBean ub,
            StudyBean studyBean, File destDirectory, TriggerBean triggerBean, RuleSetServiceInterface ruleSetService) throws Exception {
        StringBuffer msg = new StringBuffer();
        StringBuffer auditMsg = new StringBuffer();
        Mapping myMap = new Mapping();

        String propertiesPath = CoreResources.PROPERTIES_DIR;

        new File(propertiesPath + File.separator + "ODM1-3-0.xsd");
        File xsdFile2 = new File(propertiesPath + File.separator + "ODM1-2-1.xsd");
        // @pgawade 18-April-2011 Fix for issue 8394
        String ODM_MAPPING_DIR_path = CoreResources.ODM_MAPPING_DIR;
        myMap.loadMapping(ODM_MAPPING_DIR_path + File.separator + "cd_odm_mapping.xml");

        Unmarshaller um1 = new Unmarshaller(myMap);
        ODMContainer odmContainer = new ODMContainer();
        // BufferedWriter out = new BufferedWriter(new FileWriter(new
        // File("log.txt")));
        for (File f : dest) {
            // >> tbh
            boolean fail = false;
            String regex = "\\s+"; // all whitespace, one or more times
            String replacement = "_"; // replace with underscores
            String pattern = "yyyy" + File.separator + "MM" + File.separator + "dd" + File.separator + "HHmmssSSS" + File.separator;
            SimpleDateFormat sdfDir = new SimpleDateFormat(pattern);
            String generalFileDir = sdfDir.format(new java.util.Date());
            File logDestDirectory = new File(destDirectory + File.separator + generalFileDir + f.getName().replaceAll(regex, replacement) + ".log.txt");
            if (!logDestDirectory.isDirectory()) {
                logger.debug("creating new dir: " + logDestDirectory.getAbsolutePath());
                logDestDirectory.mkdirs();
            }
            File newFile = new File(logDestDirectory, "log.txt");
            // FileOutputStream out = new FileOutputStream(new
            // File(logDestDirectory, "log.txt"));
            // BufferedWriter out = null;
            // wrap the below in a try-catch?
            BufferedWriter out = new BufferedWriter(new FileWriter(newFile));

            // TODO add more info here, like a timestamp

            // << tbh 06/2010
            if (f != null) {
                String firstLine = "<P>" + f.getName() + ": ";
                msg.append(firstLine);
                out.write(firstLine);
                auditMsg.append(firstLine);

            } else {
                msg.append("<P>" + respage.getString("unreadable_file") + ": ");
                out.write("<P>" + respage.getString("unreadable_file") + ": ");
                auditMsg.append("<P>" + respage.getString("unreadable_file") + ": ");
            }

            try {

                // schemaValidator.validateAgainstSchema(f, xsdFile);
                odmContainer = (ODMContainer) um1.unmarshal(new FileReader(f));

                logger.debug("Found crf data container for study oid: " + odmContainer.getCrfDataPostImportContainer().getStudyOID());
                logger.debug("found length of subject list: " + odmContainer.getCrfDataPostImportContainer().getSubjectData().size());
            } catch (Exception me1) {
                // fail against one, try another
                try {
                    schemaValidator.validateAgainstSchema(f, xsdFile2);
                    // for backwards compatibility, we also try to validate vs
                    // 1.2.1 ODM 06/2008
                    odmContainer = (ODMContainer) um1.unmarshal(new FileReader(f));
                } catch (Exception me2) {
                    // not sure if we want to report me2

                    MessageFormat mf = new MessageFormat("");
                    mf.applyPattern(respage.getString("your_xml_is_not_well_formed"));
                    Object[] arguments = { me1.getMessage() };
                    msg.append(mf.format(arguments) + "<br/>");
                    auditMsg.append(mf.format(arguments) + "<br/>");
                    // break here with an exception
                    logger.error("found an error with XML: " + msg.toString());
                    // throw new Exception(msg.toString());
                    // instead of breaking the entire operation, we should
                    // continue looping
                    continue;
                }
            }
            // next: check, then import
            List<String> errors = getImportCRFDataService(dataSource).validateStudyMetadata(odmContainer, studyBean.getId(), locale);
            // this needs to be replaced with the study name from the job, since
            // the user could be in any study ...
            if (errors != null) {
                // add to session
                // forward to another page

                if (errors.size() > 0) {
                    out.write("<P>Errors:<br/>");
                    for (String error : errors) {
                        out.write(error + "<br/>");
                    }
                    out.write("</P>");
                    // fail = true;
                    // forwardPage(Page.IMPORT_CRF_DATA);
                    // break here with an exception
                    // throw new Exception("Your XML in the file " + f.getName()
                    // + " was well formed, but generated metadata errors: " +
                    // errors.toString());
                    // msg.append("Your XML in the file " + f.getName() +
                    // " was well formed, but generated metadata errors: " +
                    // errors.toString());
                    MessageFormat mf = new MessageFormat("");
                    mf.applyPattern(respage.getString("your_xml_in_the_file"));
                    Object[] arguments = { f.getName(), errors.size() };
                    auditMsg.append(mf.format(arguments) + "<br/>");
                    msg.append(mf.format(arguments) + "<br/>");
                    auditMsg.append("You can see the log file <a href='" + SQLInitServlet.getField("sysURL.base") + "ViewLogMessage?n=" + generalFileDir
                            + f.getName() + "&tn=" + triggerBean.getName() + "&gn=1'>here</a>.<br/>");
                    msg.append("You can see the log file <a href='" + SQLInitServlet.getField("sysURL.base") + "ViewLogMessage?n=" + generalFileDir
                            + f.getName() + "&tn=" + triggerBean.getName() + "&gn=1'>here</a>.<br/>");
                    // auditMsg.append("Your XML in the file " + f.getName() +
                    // " was well formed, but generated " + errors.size() +
                    // " metadata errors." + "<br/>");
                    out.close();
                    continue;
                } else {
                    msg.append(respage.getString("passed_study_check") + "<br/>");
                    msg.append(respage.getString("passed_oid_metadata_check") + "<br/>");
                    auditMsg.append(respage.getString("passed_study_check") + "<br/>");
                    auditMsg.append(respage.getString("passed_oid_metadata_check") + "<br/>");
                }

            }
            ImportCRFInfoContainer importCrfInfo = new ImportCRFInfoContainer(odmContainer, dataSource);
            // validation errors, the same as in the ImportCRFDataServlet. DRY?
            // create a 'fake' request to generate the validation errors
            // here, tbh 05/2009

            MockHttpServletRequest request = new MockHttpServletRequest();
            HashMap fetchEventCRFBeansResult = getImportCRFDataService(dataSource).fetchEventCRFBeans(odmContainer, ub, Boolean.FALSE,request);
            
            List<EventCRFBean> eventCRFBeans = (List<EventCRFBean>) fetchEventCRFBeansResult.get("eventCRFBeans");
            ArrayList<StudyEventBean> newStudyEventBeans = (ArrayList<StudyEventBean>) fetchEventCRFBeansResult.get("studyEventBeans");
           
            List<EventCRFBean> permittedEventCRFs = new ArrayList<EventCRFBean>();
            Boolean eventCRFStatusesValid = getImportCRFDataService(dataSource).eventCRFStatusesValid(odmContainer, ub);
            List<DisplayItemBeanWrapper> displayItemBeanWrappers = new ArrayList<DisplayItemBeanWrapper>();
            HashMap<String, String> totalValidationErrors = new HashMap<String, String>();
            HashMap<String, String> hardValidationErrors = new HashMap<String, String>();
            // The following map is used for setting the EventCRF status post import.
            HashMap<String, String> importedCRFStatuses = getImportCRFDataService(dataSource).fetchEventCRFStatuses(odmContainer);

            // -- does the event already exist? if not, fail
            if (eventCRFBeans == null) {
                fail = true;
                msg.append(respage.getString("no_event_status_matching"));
                out.write(respage.getString("no_event_status_matching"));
                out.close();
                continue;

            } else if (!eventCRFBeans.isEmpty()) {
                logger.debug("found a list of eventCRFBeans: " + eventCRFBeans.toString());
                for (EventCRFBean eventCRFBean : eventCRFBeans) {
                    DataEntryStage dataEntryStage = eventCRFBean.getStage();
                    Status eventCRFStatus = eventCRFBean.getStatus();

                    logger.debug("Event CRF Bean: id " + eventCRFBean.getId() + ", data entry stage " + dataEntryStage.getName() + ", status "
                            + eventCRFStatus.getName());
                    if (eventCRFStatus.equals(Status.AVAILABLE)
                            || dataEntryStage.equals(DataEntryStage.INITIAL_DATA_ENTRY)
                            || dataEntryStage.equals(DataEntryStage.COMPLETE)
                            || dataEntryStage.equals(DataEntryStage.INITIAL_DATA_ENTRY_COMPLETE)
                            || dataEntryStage.equals(DataEntryStage.DOUBLE_DATA_ENTRY_COMPLETE) || dataEntryStage.equals(DataEntryStage.DOUBLE_DATA_ENTRY)) {

                        permittedEventCRFs.add(eventCRFBean);
                    } else {
                        // break out here with an exception

                        // throw new
                        // Exception("Your listed Event CRF in the file " +
                        // f.getName() +
                        // " does not exist, or has already been locked for import."
                        // );
                        MessageFormat mf = new MessageFormat("");
                        mf.applyPattern(respage.getString("your_listed_crf_in_the_file"));
                        Object[] arguments = { f.getName() };
                        msg.append(mf.format(arguments) + "<br/>");
                        auditMsg.append(mf.format(arguments) + "<br/>");
                        out.write(mf.format(arguments) + "<br/>");
                        out.close();
                        continue;
                    }
                }

                if (eventCRFBeans.size() >= permittedEventCRFs.size()) {
                    msg.append(respage.getString("passed_event_crf_status_check") + "<br/>");
                    auditMsg.append(respage.getString("passed_event_crf_status_check") + "<br/>");
                } else {
                    fail = true;
                    msg.append(respage.getString("the_event_crf_not_correct_status") + "<br/>");
                    auditMsg.append(respage.getString("the_event_crf_not_correct_status") + "<br/>");
                }

               
                // Locale locale = new Locale("en-US");
                request.addPreferredLocale(locale);
                try {
                    List<DisplayItemBeanWrapper> tempDisplayItemBeanWrappers = new ArrayList<DisplayItemBeanWrapper>();
                    tempDisplayItemBeanWrappers = getImportCRFDataService(dataSource).lookupValidationErrors(request, odmContainer, ub, totalValidationErrors,
                            hardValidationErrors, permittedEventCRFs, null);
                    logger.debug("size of total validation errors: " + totalValidationErrors.size());
                    displayItemBeanWrappers.addAll(tempDisplayItemBeanWrappers);
                } catch (NullPointerException npe1) {
                    // what if you have 2 event crfs but the third is a fake?
                    npe1.printStackTrace();
                    fail = true;
                    logger.debug("threw a NPE after calling lookup validation errors");
                    msg.append(respage.getString("an_error_was_thrown_while_validation_errors") + "<br/>");
                    auditMsg.append(respage.getString("an_error_was_thrown_while_validation_errors") + "<br/>");
                    out.write(respage.getString("an_error_was_thrown_while_validation_errors") + "<br/>");
                    logger.debug("=== threw the null pointer, import ===");
                } catch (OpenClinicaException oce1) {
                    fail = true;
                    logger.error("threw an OCE after calling lookup validation errors " + oce1.getOpenClinicaMessage());
                    msg.append(oce1.getOpenClinicaMessage() + "<br/>");
                    // auditMsg.append(oce1.getOpenClinicaMessage() + "<br/>");
                    out.write(oce1.getOpenClinicaMessage() + "<br/>");

                }
            } else if (!eventCRFStatusesValid) {
                fail = true;
                msg.append(respage.getString("the_event_crf_not_correct_status"));
                out.write(respage.getString("the_event_crf_not_correct_status"));
                out.close();
                continue;
            } else {
                // fail = true;
                // break here with an exception
                msg.append(respage.getString("no_event_crfs_matching_the_xml_metadata") + "<br/>");
                // auditMsg.append(respage.getString("no_event_crfs_matching_the_xml_metadata")
                // + "<br/>");
                out.write(respage.getString("no_event_crfs_matching_the_xml_metadata") + "<br/>");
                // throw new Exception(msg.toString());
                out.close();
                continue;
            }

            ArrayList<SubjectDataBean> subjectData = odmContainer.getCrfDataPostImportContainer().getSubjectData();

            if (!hardValidationErrors.isEmpty()) {

                String messageHardVals = triggerService.generateHardValidationErrorMessage(subjectData, hardValidationErrors, false);
                // byte[] messageHardValsBytes = messageHardVals.getBytes();
                out.write(messageHardVals);
                msg.append(respage.getString("file_generated_hard_validation_error"));
                // here we create a file and append the data, tbh 06/2010
                fail = true;
            } else {
                if (!totalValidationErrors.isEmpty()) {
                    String totalValErrors = triggerService.generateHardValidationErrorMessage(subjectData, totalValidationErrors, false);
                    out.write(totalValErrors);
                    // here we also append data to the file, tbh 06/2010
                }
                String validMsgs = triggerService.generateValidMessage(subjectData, totalValidationErrors);
                out.write(validMsgs);
                // third place to append data to the file? tbh 06/2010
            }
            // << tbh 05/2010, bug #5110, leave off the detailed reports
            out.close();

            if (fail) {
                // forwardPage(Page.IMPORT_CRF_DATA);
                // break here with an exception
                // throw new Exception("Problems encountered with file " +
                // f.getName() + ": " + msg.toString());
                MessageFormat mf = new MessageFormat("");
                mf.applyPattern(respage.getString("problems_encountered_with_file"));
                Object[] arguments = { f.getName(), msg.toString() };
                msg = new StringBuffer(mf.format(arguments) + "<br/>");
                out.close();
                auditMsg.append("You can see the log file <a href='" + SQLInitServlet.getField("sysURL.base") + "ViewLogMessage?n=" + generalFileDir
                        + f.getName() + "&tn=" + triggerBean.getName() + "&gn=1'>here</a>.<br/>");
                msg.append("You can see the log file <a href='" + SQLInitServlet.getField("sysURL.base") + "ViewLogMessage?n=" + generalFileDir + f.getName()
                        + "&tn=" + triggerBean.getName() + "&gn=1'>here</a>.<br/>");
                // msg.append("Problems encountered with file " + f.getName() +
                // ": " + msg.toString() + "<br/>");
                continue;
            } else {
                msg.append(respage.getString("passing_crf_edit_checks") + "<br/>");
                auditMsg.append(respage.getString("passing_crf_edit_checks") + "<br/>");
                // session.setAttribute("importedData",
                // displayItemBeanWrappers);
                // session.setAttribute("validationErrors",
                // totalValidationErrors);
                // session.setAttribute("hardValidationErrors",
                // hardValidationErrors);
                // above are to be sent to the user, but what kind of message
                // can we make of them here?

                // if hard validation errors are present, we only generate one
                // table
                // otherwise, we generate the other two: validation errors and
                // valid data
                logger.debug("found total validation errors: " + totalValidationErrors.size());
                SummaryStatsBean ssBean = getImportCRFDataService(dataSource).generateSummaryStatsBean(odmContainer, displayItemBeanWrappers, importCrfInfo);
                // msg.append("===+");
                // the above is a special key that we will use to split the
                // message into two parts
                // a shorter version for the audit and
                // a longer version for the email
                msg.append(triggerService.generateSummaryStatsMessage(ssBean, respage) + "<br/>");
                // session.setAttribute("summaryStats", ssBean);
                // will have to set hard edit checks here as well
                // session.setAttribute("subjectData",
                // ArrayList<SubjectDataBean> subjectData =
                // odmContainer.getCrfDataPostImportContainer().getSubjectData();
                // forwardPage(Page.VERIFY_IMPORT_SERVLET);
                // instead of forwarding, go ahead and save it all, sending a
                // message at the end

                msg.append(triggerService.generateSkippedCRFMessage(importCrfInfo, resword) + "<br/>");

                // setup ruleSets to run if applicable
                List<ImportDataRuleRunnerContainer> containers = this.ruleRunSetup(dataSource, studyBean, ub, ruleSetService, odmContainer);

                CrfBusinessLogicHelper crfBusinessLogicHelper = new CrfBusinessLogicHelper(dataSource);
                for (DisplayItemBeanWrapper wrapper : displayItemBeanWrappers) {

                    boolean resetSDV = false;
                    int eventCrfBeanId = -1;
                    EventCRFBean eventCrfBean = new EventCRFBean();

                    logger.debug("right before we check to make sure it is savable: " + wrapper.isSavable());
                    if (wrapper.isSavable()) {
                        logger.debug("wrapper problems found : " + wrapper.getValidationErrors().toString());
                        itemDataDao.setFormatDates(false);
                        for (DisplayItemBean displayItemBean : wrapper.getDisplayItemBeans()) {
                            eventCrfBeanId = displayItemBean.getData().getEventCRFId();
                            eventCrfBean = (EventCRFBean) eventCrfDao.findByPK(eventCrfBeanId);
                            logger.debug("found value here: " + displayItemBean.getData().getValue());
                            logger.debug("found status here: " + eventCrfBean.getStatus().getName());
                            ItemDataBean itemDataBean = new ItemDataBean();
                            itemDataBean = itemDataDao.findByItemIdAndEventCRFIdAndOrdinal(displayItemBean.getItem().getId(), eventCrfBean.getId(),
                                    displayItemBean.getData().getOrdinal());
                            if (wrapper.isOverwrite() && itemDataBean.getStatus() != null) {
                                logger.debug("just tried to find item data bean on item name " + displayItemBean.getItem().getName());
                                if (!itemDataBean.getValue().equals(displayItemBean.getData().getValue()))
                                    resetSDV = true;
                                itemDataBean.setUpdatedDate(new Date());
                                itemDataBean.setUpdater(ub);
                                itemDataBean.setValue(displayItemBean.getData().getValue());
                                // set status?
                                itemDataDao.update(itemDataBean);
                                logger.debug("updated: " + itemDataBean.getItemId());
                                // need to set pk here in order to create dn
                                displayItemBean.getData().setId(itemDataBean.getId());
                            } else {
                                resetSDV = true;
                                itemDataDao.create(displayItemBean.getData());
                                logger.debug("created: " + displayItemBean.getData().getItemId());
                                ItemDataBean itemDataBean2 = itemDataDao.findByItemIdAndEventCRFIdAndOrdinal(displayItemBean.getItem().getId(),
                                        eventCrfBean.getId(), displayItemBean.getData().getOrdinal());
                                logger.debug("found: id " + itemDataBean2.getId() + " name " + itemDataBean2.getName());
                                displayItemBean.getData().setId(itemDataBean2.getId());
                            }
                            ItemDAO idao = new ItemDAO(dataSource);
                            ItemBean ibean = (ItemBean) idao.findByPK(displayItemBean.getData().getItemId());
                            logger.debug("*** checking for validation errors: " + ibean.getName());
                            String itemOid = displayItemBean.getItem().getOid() + "_" + wrapper.getStudyEventRepeatKey() + "_"
                                    + displayItemBean.getData().getOrdinal() + "_" + wrapper.getStudySubjectOid();
                            if (wrapper.getValidationErrors().containsKey(itemOid)) {
                                ArrayList messageList = (ArrayList) wrapper.getValidationErrors().get(itemOid);
                                for (int iter = 0; iter < messageList.size(); iter++) {
                                    String message = (String) messageList.get(iter);

                                    DiscrepancyNoteBean parentDn = createDiscrepancyNote(ibean, message, eventCrfBean, displayItemBean, null, ub, dataSource,
                                            studyBean);
                                    createDiscrepancyNote(ibean, message, eventCrfBean, displayItemBean, parentDn.getId(), ub, dataSource, studyBean);
                                    logger.debug("*** created disc note with message: " + message);
                                    // displayItemBean);
                                }
                            }
                            // Update CRF status
                            String eventCRFStatus = importedCRFStatuses
                                    .get(eventCrfBean.getStudySubjectId() + "-" + eventCrfBean.getStudyEventId() + "-" + eventCrfBean.getFormLayoutId());
                            if (eventCRFStatus != null && eventCRFStatus.equals(DataEntryStage.INITIAL_DATA_ENTRY.getName())
                                    && eventCrfBean.getStatus().isAvailable()) {
                                crfBusinessLogicHelper.markCRFStarted(eventCrfBean, ub);
                            } else {
                                crfBusinessLogicHelper.markCRFComplete(eventCrfBean, ub);
                            }
                            logger.debug("*** just updated event crf bean: " + eventCrfBean.getId());

                        }
                        itemDataDao.setFormatDates(true);
                        // Reset the SDV status if item data has been changed or added
                        if (eventCrfBean != null && resetSDV)
                            eventCrfDao.setSDVStatus(false, ub.getId(), eventCrfBean.getId());
                    }
                }
                // msg.append("===+");
                msg.append(respage.getString("data_has_been_successfully_import") + "<br/>");
                auditMsg.append(respage.getString("data_has_been_successfully_import") + "<br/>");

                // MessageFormat mf = new MessageFormat("");
                String linkMessage = respage.getString("you_can_review_the_data") + SQLInitServlet.getField("sysURL.base")
                        + respage.getString("you_can_review_the_data_2") + SQLInitServlet.getField("sysURL.base")
                        + respage.getString("you_can_review_the_data_3") + generalFileDir + f.getName() + "&tn=" + triggerBean.getFullName() + "&gn=1"
                        + respage.getString("you_can_review_the_data_4") + "<br/>";
                // mf.applyPattern(respage.getString("you_can_review_the_data"));
                // Object[] arguments = {
                // SQLInitServlet.getField("sysURL.base"),
                // SQLInitServlet.getField("sysURL.base"), f.getName() };
                msg.append(linkMessage);
                auditMsg.append(linkMessage);

                // was here but is now moved up, tbh
                // String finalLine =
                // "<p>You can review the entered data <a href='" +
                // SQLInitServlet.getField("sysURL.base") +
                // "ListStudySubjects'>here</a>.";
                // >> tbh additional message
                // "you can review the validation messages here" <-- where
                // 'here' is a link to view an external file
                // i.e. /ViewExternal?n=file_name.txt
                // << tbh 06/2010
                // msg.append(finalLine);
                // auditMsg.append(finalLine);
                auditMsg.append(this.runRules(studyBean, ub, containers, ruleSetService, ExecutionMode.SAVE));
            }
        } // end for loop
          // is the writer still not closed? try to close it

        ArrayList<String> retList = new ArrayList<String>();
        retList.add(msg.toString());
        retList.add(auditMsg.toString());
        return retList;// msg.toString();

    }

    public static DiscrepancyNoteBean createDiscrepancyNote(ItemBean itemBean, String message, EventCRFBean eventCrfBean, DisplayItemBean displayItemBean,
            Integer parentId, UserAccountBean uab, DataSource ds, StudyBean study) {
        // DisplayItemBean displayItemBean) {
        DiscrepancyNoteBean note = new DiscrepancyNoteBean();
        StudySubjectDAO ssdao = new StudySubjectDAO(ds);
        note.setDescription(message);
        note.setDetailedNotes("Failed Validation Check");
        note.setOwner(uab);
        note.setCreatedDate(new Date());
        note.setResolutionStatusId(ResolutionStatus.OPEN.getId());
        note.setDiscrepancyNoteTypeId(DiscrepancyNoteType.FAILEDVAL.getId());
        if (parentId != null) {
            note.setParentDnId(parentId);
        }

        note.setField(itemBean.getName());
        note.setStudyId(study.getId());
        note.setEntityName(itemBean.getName());
        note.setEntityType(DiscrepancyNoteBean.ITEM_DATA);
        note.setEntityValue(displayItemBean.getData().getValue());

        note.setEventName(eventCrfBean.getName());
        note.setEventStart(eventCrfBean.getCreatedDate());
        note.setCrfName(displayItemBean.getEventDefinitionCRF().getCrfName());

        StudySubjectBean ss = (StudySubjectBean) ssdao.findByPK(eventCrfBean.getStudySubjectId());
        note.setSubjectName(ss.getName());

        note.setEntityId(displayItemBean.getData().getId());
        note.setColumn("value");

        DiscrepancyNoteDAO dndao = new DiscrepancyNoteDAO(ds);
        note = (DiscrepancyNoteBean) dndao.create(note);
        // so that the below method works, need to set the entity above
        // System.out.println("trying to create mapping with " + note.getId() +
        // " " + note.getEntityId() + " " + note.getColumn() + " " +
        // note.getEntityType());
        dndao.createMapping(note);
        return note;
    }

    private void cutAndPaste(File[] tar, File[] dest) throws IOException {
        InputStream in = null;
        OutputStream out = null;
        // @pgawade 07-Sept-2012: fix for https://issuetracker.openclinica.com/view.php?id=13261#c57418
        // int fle_count = 0;
        int fle_count = -1;

        for (File cur_file : tar) {
            if (cur_file == null) {
                fle_count++;
                continue;
            }
            try {
                in = new FileInputStream(cur_file);
                fle_count++;
                out = new FileOutputStream(dest[fle_count]);
                byte[] buf = new byte[1024];
                int len = 0;

                while ((len = in.read(buf)) > 0) {
                    out.write(buf, 0, len);
                }
                in.close();
                out.close();
                cur_file.delete();
            } catch (NullPointerException npe) {
                logger.debug("found Npe: " + npe.getMessage());
                // maybe no need to catch, but leaving as is not to change too
                // much in code
            } finally {
                if (in != null) {
                    in.close();
                }
                if (out != null) {
                    out.close();
                }
            }

        }
    }

    @Transactional
    private List<ImportDataRuleRunnerContainer> ruleRunSetup(DataSource dataSource, StudyBean studyBean, UserAccountBean userBean,
            RuleSetServiceInterface ruleSetService, ODMContainer odmContainer) {
        List<ImportDataRuleRunnerContainer> containers = new ArrayList<ImportDataRuleRunnerContainer>();
        if (odmContainer != null) {
            ArrayList<SubjectDataBean> subjectDataBeans = odmContainer.getCrfDataPostImportContainer().getSubjectData();
            if (ruleSetService.getCountByStudy(studyBean) > 0) {
                ImportDataRuleRunnerContainer container;
                for (SubjectDataBean subjectDataBean : subjectDataBeans) {
                    container = new ImportDataRuleRunnerContainer();
                    container.initRuleSetsAndTargets(dataSource, studyBean, subjectDataBean, ruleSetService);
                    if (container.getShouldRunRules())
                        containers.add(container);
                }
                if (containers != null && !containers.isEmpty())
                    ruleSetService.runRulesInImportData(containers, studyBean, userBean, ExecutionMode.DRY_RUN);
            }
        }
        return containers;
    }

    @Transactional
    private StringBuffer runRules(StudyBean studyBean, UserAccountBean userBean, List<ImportDataRuleRunnerContainer> containers,
            RuleSetServiceInterface ruleSetService, ExecutionMode executionMode) {
        StringBuffer messages = new StringBuffer();
        if (containers != null && !containers.isEmpty()) {
            HashMap<String, ArrayList<String>> summary = ruleSetService.runRulesInImportData(containers, studyBean, userBean, executionMode);
            messages = extractRuleActionWarnings(summary);
        }
        return messages;
    }

    private StringBuffer extractRuleActionWarnings(HashMap<String, ArrayList<String>> summaryMap) {
        StringBuffer messages = new StringBuffer();
        if (summaryMap != null && !summaryMap.isEmpty()) {
            for (String key : summaryMap.keySet()) {
                messages.append(key);
                messages.append(" : ");
                messages.append(StringUtils.join(summaryMap.get(key), ", "));
            }
        }
        return messages;
    }

    private File[] removeNullElements(File[] source) {

        ArrayList<File> list = new ArrayList<File>();
        for (File f : source) {
            if (f != null) {
                list.add(f);
            }
        }
        return list.toArray(new File[list.size()]);
    }
}
