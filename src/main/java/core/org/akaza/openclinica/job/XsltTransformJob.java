package core.org.akaza.openclinica.job;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Map.Entry;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.sql.DataSource;
import javax.xml.transform.*;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import core.org.akaza.openclinica.bean.admin.TriggerBean;
import core.org.akaza.openclinica.bean.extract.ArchivedDatasetFileBean;
import core.org.akaza.openclinica.bean.extract.DatasetBean;
import core.org.akaza.openclinica.bean.extract.ExportFormatBean;
import core.org.akaza.openclinica.bean.extract.ExtractBean;
import core.org.akaza.openclinica.bean.extract.ExtractPropertyBean;
import core.org.akaza.openclinica.bean.login.UserAccountBean;
import core.org.akaza.openclinica.bean.service.ProcessingFunction;
import core.org.akaza.openclinica.bean.service.ProcessingResultType;
import core.org.akaza.openclinica.core.EmailEngine;
import core.org.akaza.openclinica.core.OpenClinicaMailSender;
import core.org.akaza.openclinica.core.util.XMLFileFilter;
import core.org.akaza.openclinica.dao.admin.AuditEventDAO;
import core.org.akaza.openclinica.dao.core.CoreResources;
import core.org.akaza.openclinica.dao.extract.ArchivedDatasetFileDAO;
import core.org.akaza.openclinica.dao.extract.DatasetDAO;
import core.org.akaza.openclinica.dao.hibernate.ArchivedDatasetFilePermissionTagDao;
import core.org.akaza.openclinica.dao.hibernate.EventDefinitionCrfPermissionTagDao;
import core.org.akaza.openclinica.dao.hibernate.StudyDao;
import core.org.akaza.openclinica.dao.login.UserAccountDAO;
import core.org.akaza.openclinica.domain.datamap.ArchivedDatasetFilePermissionTag;
import core.org.akaza.openclinica.domain.datamap.Study;
import core.org.akaza.openclinica.domain.enumsupport.JobStatus;
import core.org.akaza.openclinica.exception.OpenClinicaSystemException;
import core.org.akaza.openclinica.i18n.util.ResourceBundleProvider;
import core.org.akaza.openclinica.logic.odmExport.ClinicalDataCollector;
import core.org.akaza.openclinica.service.PermissionService;
import core.org.akaza.openclinica.service.dto.ODMFilterDTO;
import core.org.akaza.openclinica.service.extract.GenerateExtractFileService;
import core.org.akaza.openclinica.service.extract.OdmFileCreation;
import core.org.akaza.openclinica.service.extract.XsltTriggerService;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.impl.StdScheduler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.scheduling.quartz.QuartzJobBean;
import org.springframework.util.StringUtils;


/**
 * Xalan Transform Job, an XSLT transform job using the Xalan classes
 * @author thickerson
 */
public class XsltTransformJob extends QuartzJobBean {

    private static final Logger logger = LoggerFactory.getLogger(XsltTransformJob.class);

    public static final String DATASET_ID = "dsId";
    public static final String EMAIL = "contactEmail";
    public static final String USER_ID = "user_id";
    public static final String XSL_FILE_PATH = "xslFilePath";
    public static final String XML_FILE_PATH = "xmlFilePath";
    public static final String POST_FILE_PATH = "postFilePath";
    public static final String POST_FILE_NAME = "postFileName";
    public static final String EXTRACT_PROPERTY = "extractProperty";
    public static final String LOCALE = "locale";
    public static final String STUDY_ID = "studyId";
    public static final String TENANT_SCHEMA = "tenantSchema";
    public static final String ZIPPED = "zipped";
    public static final String DELETE_OLD = "deleteOld";
    public static final String XSLT_PATH = "XSLT_PATH";
    public static final String EP_BEAN = "epBean";

    private OpenClinicaMailSender mailSender;
    private GenerateExtractFileService generateFileService;
    private OdmFileCreation odmFileCreation;
    private UserAccountDAO userAccountDao;
    private ArchivedDatasetFileDAO archivedDatasetFileDao;
    private AuditEventDAO auditEventDAO;
    private DatasetDAO datasetDao;
    private StudyDao studyDao;

    @Autowired
    private PermissionService permissionService;

    @Autowired
    private ArchivedDatasetFilePermissionTagDao archivedDatasetFilePermissionTagDao;

    @Autowired
    private EventDefinitionCrfPermissionTagDao eventDefinitionCrfPermissionTagDao;

    private final TransformerFactory transformerFactory = new net.sf.saxon.TransformerFactoryImpl();

    // POST PROCESSING VARIABLES
    public static final String POST_PROC_DELETE_OLD = "postProcDeleteOld";
    public static final String POST_PROC_ZIP = "postProcZip";
    public static final String POST_PROC_LOCATION = "postProcLocation";
    public static final String POST_PROC_EXPORT_NAME = "postProcExportName";
    private static final long KILOBYTE = 1024;

    @Override
    protected void executeInternal(JobExecutionContext context) {
        logger.info("Job " + context.getJobDetail().getDescription() + " started.");
        JobDataMap dataMap = context.getMergedJobDataMap();
        initDependencies(context.getScheduler(), dataMap);
        // need to generate a Locale for emailing users with i18n
        // TODO make dynamic?
        Locale locale = new Locale("en-US");
        ResourceBundleProvider.updateLocale(locale);
        ResourceBundle pageMessages = ResourceBundleProvider.getPageMessagesBundle();
        List<File> markForDelete = new LinkedList<>();
        Boolean zipped = true;
        Boolean deleteOld = true;
        Boolean exceptions = false;
        String localeStr = dataMap.getString(LOCALE);
        String[] doNotDeleteUntilExtract = new String[4];
        int cnt = dataMap.getInt("count");
        DatasetBean datasetBean = null;
        if (localeStr != null) {
            locale = new Locale(localeStr);
            ResourceBundleProvider.updateLocale(locale);
            pageMessages = ResourceBundleProvider.getPageMessagesBundle();
        }
        java.io.InputStream in = null;
        FileOutputStream endFileStream = null;
        UserAccountBean userBean = null;
        // get the file information from the job
        String alertEmail = dataMap.getString(EMAIL);
        int userAccountId = dataMap.getInt(USER_ID);
        logger.debug("found userAccountId: " + userAccountId);
        int studyId = dataMap.getInt(STUDY_ID);
        String outputPath = dataMap.getString(POST_FILE_PATH);
        String generalFileDir = dataMap.getString(XML_FILE_PATH);
        int dsId = dataMap.getInt(DATASET_ID);
        int archivedDatasetFileBeanId = dataMap.getInt(XsltTriggerService.ARCHIVED_DATASET_FILE_BEAN_ID);
        logger.debug("found archivedDatasetFileBeanId: " + dsId);
        ExtractPropertyBean epBean = (ExtractPropertyBean) dataMap.get(EP_BEAN);
        String jobName = dataMap.getString(XsltTriggerService.JOB_NAME);
        ArchivedDatasetFileBean archivedDatasetFileBean = (ArchivedDatasetFileBean) archivedDatasetFileDao.findByPK(archivedDatasetFileBeanId);
        if (archivedDatasetFileBean == null) {
            logger.error("Could not find archivedDatasetFile for id: " + archivedDatasetFileBeanId);
        } else {
            logger.debug("Found archivedDatasetFile in the db.");
        }

        try {
            boolean isScheduled = archivedDatasetFileBean.getJobType().contentEquals("Scheduled");
            //Create a new row in archived dataset file db if previous one is completed (for export triggers)
            if (archivedDatasetFileBean.getStatus().equals(JobStatus.COMPLETED.name())) {
                ArchivedDatasetFileBean newArchivedDatasetFileBean = new ArchivedDatasetFileBean();
                newArchivedDatasetFileBean.setStatus(JobStatus.IN_QUEUE.name());
                newArchivedDatasetFileBean.setFormat(archivedDatasetFileBean.getFormat());
                newArchivedDatasetFileBean.setOwner(archivedDatasetFileBean.getOwner());
                newArchivedDatasetFileBean.setOwnerId(archivedDatasetFileBean.getOwnerId());
                newArchivedDatasetFileBean.setDatasetId(archivedDatasetFileBean.getDatasetId());
                newArchivedDatasetFileBean.setDateCreated(new Date());
                newArchivedDatasetFileBean.setExportFormatId(1);
                newArchivedDatasetFileBean.setFileReference(null);
                newArchivedDatasetFileBean.setJobUuid(archivedDatasetFileBean.getJobUuid());
                newArchivedDatasetFileBean.setJobExecutionUuid(UUID.randomUUID().toString());
                newArchivedDatasetFileBean.setJobType(archivedDatasetFileBean.getJobType());
                archivedDatasetFileBean = (ArchivedDatasetFileBean) archivedDatasetFileDao.create(newArchivedDatasetFileBean);

                //create a new file for this export instead of overwrite the previous one
                int id = dataMap.getInt(EXTRACT_PROPERTY);
                String fileName;
                if (id == 9 || id == 10) { //for SAS and SPSS
                    fileName = CoreResources.getExtractFields("extract." + id + ".zipName")[0];
                    String simpleDatePattern = "yyyy-MM-dd-HHmmssSSS";
                    SimpleDateFormat sdfDir = new SimpleDateFormat(simpleDatePattern);
                    String currentDate = sdfDir.format(new java.util.Date());
                    fileName = fileName.replace("$dateTime", currentDate).replace("$datasetName", epBean.getDatasetName());
                    epBean.setZipName(fileName);
                } else {
                    fileName = CoreResources.getExtractFields("extract." + id + ".exportname")[0];
                    String simpleDatePattern = "yyyy-MM-dd-HHmmssSSS";
                    SimpleDateFormat sdfDir = new SimpleDateFormat(simpleDatePattern);
                    String currentDate = sdfDir.format(new java.util.Date());
                    fileName = fileName.replace("$dateTime", currentDate).replace("$datasetName", epBean.getDatasetName());
                    String[] exportFileName = {fileName};
                    epBean.setExportFileName(exportFileName);
                    epBean.setDoNotDelFiles(exportFileName);
                }
                dataMap.put(POST_FILE_NAME, fileName);
            }

            archivedDatasetFileBean.setStatus(JobStatus.IN_PROGRESS.name());
            archivedDatasetFileBean.setDateCreated(new Date());
            archivedDatasetFileBean = (ArchivedDatasetFileBean) archivedDatasetFileDao.update(archivedDatasetFileBean);

            // JN: Change from earlier versions, cannot get static reference as
            // static references don't work. Reason being for example there could be
            // datasetId as a variable which is different for each dataset and
            // that needs to be loaded dynamically

            File doNotDelDir = new File(generalFileDir);
            if (doNotDelDir.isDirectory()) {
                doNotDeleteUntilExtract = doNotDelDir.list();
            }

            zipped = epBean.getZipFormat();

            deleteOld = epBean.getDeleteOld();

            //always delete old file references for manually ran jobs.
            if (!isScheduled) {
                deleteOld = true;
            }

            long sysTimeBegin = System.currentTimeMillis();
            userBean = (UserAccountBean) userAccountDao.findByPK(userAccountId);

            Study currentStudy = studyDao.findByPK(studyId);
            Study parentStudy = studyDao.findByPK(currentStudy.checkAndGetParentStudyId());
            String successMsg = epBean.getSuccessMessage();
            String failureMsg = epBean.getFailureMessage();
            final long start = System.currentTimeMillis();

            datasetBean = (DatasetBean) datasetDao.findByPK(dsId);
            ExtractBean eb = generateFileService.generateExtractBean(datasetBean, currentStudy, parentStudy);

            // generate file directory for file service
            datasetBean.setName(datasetBean.getName().replaceAll(" ", "_"));
            logger.debug("--> job starting: ");

            String userUuid = userBean.getUserUuid();
            String permissionTagsString = permissionService.getPermissionTagsString(currentStudy, userUuid);
            String[] permissionTagsStringArray = permissionService.getPermissionTagsStringArray(currentStudy, userUuid);
            ODMFilterDTO odmFilter = new ODMFilterDTO();


            Set<Integer> edcSet = new HashSet<>();
            ArchivedDatasetFileBean fbFinal = null;
            ClinicalDataCollector.datasetFiltered = "NO";

            HashMap<String, Integer> answerMap =
                    odmFileCreation.createODMFile(epBean.getFormat(), sysTimeBegin, generalFileDir, datasetBean,
                            currentStudy, "", eb, currentStudy.getStudyId(), currentStudy.checkAndGetParentStudyId(), "99",
                            (Boolean) dataMap.get(ZIPPED), false, (Boolean) dataMap.get(DELETE_OLD), epBean.getOdmType(),
                            userBean, odmFilter, permissionTagsString, permissionTagsStringArray, edcSet);

            // won't save a record of the XML to db
            // won't be a zipped file, so that we can submit it for
            // transformation
            // this will have to be toggled by the export data format? no, the
            // export file will have to be zipped/not zipped
            String ODMXMLFileName = "";
            int fId = 0;
            Iterator<Entry<String, Integer>> it = answerMap.entrySet().iterator();
            while (it.hasNext()) {
                JobTerminationMonitor.check();

                Entry<String, Integer> entry = it.next();
                String key = entry.getKey();
                Integer value = entry.getValue();
                ODMXMLFileName = key;// JN: Since there is a logic to
                // delete all the intermittent
                // files, this file could be a zip
                // file.
                Integer fileID = value;
                fId = fileID.intValue();
                logger.debug("found " + fId + " and " + ODMXMLFileName);
            }
            logger.info("Finished ODM generation of job " + context.getJobDetail().getDescription());

            // create dirs
            File output = new File(outputPath);
            if (!output.isDirectory()) {
                output.mkdirs();
            }

            int numXLS = epBean.getFileName().length;
            int fileCntr = 0;

            String xmlFilePath = new File(generalFileDir + ODMXMLFileName).toURI().toURL().toExternalForm();
            String endFile = null;
            File oldFilesPath = new File(generalFileDir);
            while (fileCntr < numXLS) {
                JobTerminationMonitor.check();

                String xsltPath = dataMap.getString(XSLT_PATH) + File.separator + epBean.getFileName()[fileCntr];
                in = new java.io.FileInputStream(xsltPath);

                Transformer transformer = transformerFactory.newTransformer(new StreamSource(in));

                endFile = outputPath + File.separator + epBean.getExportFileName()[fileCntr];

                endFileStream = new FileOutputStream(endFile);
                transformer.transform(new StreamSource(xmlFilePath), new StreamResult(endFileStream));

                // JN...CLOSE THE STREAM...HMMMM
                in.close();
                endFileStream.close();

                fileCntr++;

                JobTerminationMonitor.check();
            }
            if (oldFilesPath.isDirectory()) {
                markForDelete = Arrays.asList(oldFilesPath.listFiles());
                // logic to prevent deleting the file being created.
            }
            final double done = setFormat(new Double(System.currentTimeMillis() - start) / 1000);
            logger.info("--> job completed in " + done + " ms");
            // run post processing

            ProcessingFunction function = epBean.getPostProcessing();
            String subject = "";
            StringBuffer emailBuffer = new StringBuffer("");
            emailBuffer.append("<p>" + pageMessages.getString("email_header_1") + " " + EmailEngine.getAdminEmail() + " "
                    + pageMessages.getString("email_header_2") + " Job Execution " + pageMessages.getString("email_header_3") + "</p>");
            emailBuffer.append("<P>Dataset: " + datasetBean.getName() + "</P>");
            emailBuffer.append("<P>Study: " + currentStudy.getName() + "</P>");
            if (function != null && function.getClass().equals(core.org.akaza.openclinica.bean.service.SqlProcessingFunction.class)) {
                String dbUrl = ((core.org.akaza.openclinica.bean.service.SqlProcessingFunction) function).getDatabaseUrl();
                int lastIndex = dbUrl.lastIndexOf('/');
                String schemaName = dbUrl.substring(lastIndex);
                int HostIndex = dbUrl.substring(0, lastIndex).indexOf("//");
                String Host = dbUrl.substring(HostIndex, lastIndex);
                emailBuffer.append("<P>Database: " + ((core.org.akaza.openclinica.bean.service.SqlProcessingFunction) function).getDatabaseType() + "</P>");
                emailBuffer.append("<P>Schema: " + schemaName.replace("/", "") + "</P>");
                emailBuffer.append("<P>Host: " + Host.replace("//", "") + "</P>");

            }
            emailBuffer.append("<p>" + pageMessages.getString("html_email_body_1") + datasetBean.getName() + pageMessages.getString("html_email_body_2_2")
                    + "</p>");
            if (function != null) {
                function.setTransformFileName(outputPath + File.separator + dataMap.getString(POST_FILE_NAME));
                function.setODMXMLFileName(endFile);
                function.setXslFileName(dataMap.getString(XSL_FILE_PATH));
                function.setDeleteOld((Boolean) dataMap.get(POST_PROC_DELETE_OLD));
                function.setZip((Boolean) dataMap.get(POST_PROC_ZIP));
                function.setLocation(dataMap.getString(POST_PROC_LOCATION));
                function.setExportFileName(dataMap.getString(POST_PROC_EXPORT_NAME));
                File oldFiles[] = getOldFiles(outputPath, dataMap.getString(POST_PROC_LOCATION));
                function.setOldFiles(oldFiles);
                File intermediateFiles[] = getInterFiles(dataMap.getString(POST_FILE_PATH));
                ProcessingResultType message = function.run();

                // Delete these files only in case when there is no failure
                if (message.getCode().intValue() != 2) {
                    deleteOldFiles(intermediateFiles);
                }
                final long done2 = System.currentTimeMillis() - start;
                logger.info("--> postprocessing completed in " + done2 + " ms, found result type " + message.getCode());
                if (!function.getClass().equals(core.org.akaza.openclinica.bean.service.SqlProcessingFunction.class)) {
                    String archivedFile = dataMap.getString(POST_FILE_NAME) + "." + function.getFileType();
                    // JN: if the properties is set to zip the output file,
                    // download the zip file
                    if (function.isZip()) {
                        archivedFile = archivedFile + ".zip";
                    }

                    // JN: The above 2 lines code is useless, it should be
                    // removed..added it only for the sake of custom processing
                    // but it will produce erroneous results in case of custom
                    // post processing as well.
                    if (function.getClass().equals(core.org.akaza.openclinica.bean.service.PdfProcessingFunction.class)) {
                        archivedFile = function.getArchivedFileName();
                    }

                    fbFinal =
                            generateFileRecord(archivedFile, outputPath, done, new File(outputPath + File.separator + archivedFile).length(),
                                    ExportFormatBean.PDFFILE, edcSet, archivedDatasetFileBean);

                    if (successMsg.contains("$linkURL")) {
                        successMsg =
                                successMsg.replace("$linkURL", "<a href=\"" + CoreResources.getField("sysURL.base") + "AccessFile?fileId=" + fbFinal.getId()
                                        + "\">" + CoreResources.getField("sysURL.base") + "AccessFile?fileId=" + fbFinal.getId()
                                        + " </a>");
                    }
                    emailBuffer.append("<p>" + successMsg + "</p>");
                    logger.debug("System time begining.." + sysTimeBegin);
                    logger.debug("System time end.." + System.currentTimeMillis());
                    double sysTimeEnd = setFormat((System.currentTimeMillis() - sysTimeBegin) / 1000);
                    logger.debug("difference" + sysTimeEnd);

                    if (fbFinal != null) {
                        fbFinal.setFileSize((int) bytesToKilo(new File(archivedFile).length()));
                        fbFinal.setRunTime(sysTimeEnd);
                    }

                }
                // otherwise don't do it
                if (message.getCode().intValue() == 1) {
                    if (jobName != null) {
                        subject = "Success: " + jobName;
                    } else {
                        subject = "Success: " + datasetBean.getName();
                    }
                } else if (message.getCode().intValue() == 2) {
                    if (jobName != null) {
                        subject = "Failure: " + jobName;
                    } else {
                        subject = "Failure: " + datasetBean.getName();
                    }
                    if (failureMsg != null && !failureMsg.isEmpty()) {
                        emailBuffer.append(failureMsg);
                    }
                    emailBuffer.append("<P>").append(message.getDescription());
                    postErrorMessage(message.getDescription(), context);
                } else if (message.getCode().intValue() == 3) {
                    if (jobName != null) {
                        subject = "Update: " + jobName;
                    } else {
                        subject = "Update: " + datasetBean.getName();
                    }
                }

            } else {
                // extract ran but no post-processing - we send an email with
                // success and url to link to
                // generate archived dataset file bean here, and use the id to
                // build the URL
                String archivedFilename = dataMap.getString(POST_FILE_NAME);
                // JN: if the properties is set to zip the output file, download
                // the zip file
                if (zipped) {
                    archivedFilename = dataMap.getString(POST_FILE_NAME) + ".zip";
                }

                List<File> intermediateFiles = generateFileService.getOldFiles();
                String[] dontDelFiles = epBean.getDoNotDelFiles();
                // The following is the code for zipping up the files, in case of more than one xsl being provided (i.e. SPSS and SAS)
                if (dontDelFiles.length > 1 && zipped) {
                    logger.debug("count =====" + cnt + "dontDelFiles length==---" + dontDelFiles.length);

                    logger.debug("Entering this?" + cnt + "dontDelFiles" + dontDelFiles);
                    String path = outputPath + File.separator;
                    logger.debug("path = " + path);
                    logger.debug("zipName?? = " + epBean.getZipName());

                    String zipName =
                            epBean.getZipName() == null || epBean.getZipName().isEmpty() ? endFile + ".zip" : path + epBean.getZipName() + ".zip";

                    archivedFilename = new File(zipName).getName();
                    zipAll(path, epBean.getDoNotDelFiles(), zipName);
                    String[] tempArray = {archivedFilename};
                    dontDelFiles = tempArray;
                    endFile = archivedFilename;


                } else if (zipped) {
                    markForDelete = zipxmls(markForDelete, endFile);
                    endFile = endFile + ".zip";

                    String[] temp = new String[dontDelFiles.length];
                    int i = 0;
                    while (i < dontDelFiles.length) {
                        temp[i] = dontDelFiles[i] + ".zip";
                        i++;
                    }
                    dontDelFiles = temp;

                    // Actually deleting all the xml files which are produced
                    // since its zipped
                    FilenameFilter xmlFilter = new XMLFileFilter();
                    File tempFile = new File(generalFileDir);
                    deleteOldFiles(tempFile.listFiles(xmlFilter));
                }

                fbFinal = generateFileRecord(archivedFilename, outputPath, done, new File(outputPath + File.separator + archivedFilename).length(),
                        ExportFormatBean.TXTFILE, edcSet, archivedDatasetFileBean);


                if (jobName != null) {
                    subject = "Job Ran: " + jobName;
                } else {
                    subject = "Job Ran: " + datasetBean.getName();
                }

                Study study = studyDao.findByPK(datasetBean.getStudyId());

                if (successMsg == null || successMsg.isEmpty()) {
                    logger.info("email buffer??" + emailBuffer);

                } else {
                    if (successMsg.contains("$linkURL")) {
                        successMsg =
                                successMsg.replace("$linkURL", "<a href=\"" + CoreResources.getField("sysURL.base") + "AccessFile?study_oid=" + study.getOc_oid().toLowerCase() + "&fileId=" + fbFinal.getId()
                                        + "\"> [Click Here] </a>");
                    }
                    emailBuffer.append("<p>" + successMsg + "</p>");
                }

                if (deleteOld) {
                    deleteIntermFiles(intermediateFiles, endFile, dontDelFiles);
                    deleteIntermFiles(markForDelete, endFile, dontDelFiles);
                }

                //Deleting old file_references from archived_dataset_file
                if (isScheduled) {
                    //for SAS and SPSS delete the unzipped files
                    if (dataMap.getInt(EXTRACT_PROPERTY) == 9 || dataMap.getInt(EXTRACT_PROPERTY) == 10) {
                        deleteNonZippedFilesInFolder(generalFileDir);
                    }
                    deleteOldFileReferences(dataMap, archivedDatasetFileBean);
                }

            }
            // email the message to the user
            emailBuffer.append("<p>" + pageMessages.getString("html_email_body_5") + "</p>");
            try {

                // @pgawade 19-April-2011 Log the event into audit_event table
                if (null != dataMap.get("job_type") && ((String) dataMap.get("job_type")).equalsIgnoreCase("exportJob")) {
                    String extractName = (String) dataMap.get(XsltTriggerService.JOB_NAME);
                    TriggerBean triggerBean = new TriggerBean();
                    triggerBean.setDataset(datasetBean);
                    triggerBean.setUserAccount(userBean);
                    triggerBean.setFullName(extractName);
                    String actionMsg =
                            "You may access the " + (String) dataMap.get(XsltTriggerService.EXPORT_FORMAT) + " file by changing your study/site to "
                                    + currentStudy.getName() + " and selecting the Export Data icon for " + datasetBean.getName()
                                    + " dataset on the View Datasets page.";
                    auditEventDAO.createRowForExtractDataJobSuccess(triggerBean, actionMsg);
                }
                mailSender.sendEmail(alertEmail, EmailEngine.getAdminEmail(), subject, emailBuffer.toString(), true);

                logger.info("just sent email to " + alertEmail + ", from " + EmailEngine.getAdminEmail());
                if (successMsg == null) {
                    successMsg = " ";
                }

            } catch (OpenClinicaSystemException ose) {
                // Do Nothing, In the future we might want to have an email
                // status added to system.
                logger.info("exception sending mail: " + ose.getMessage());
                logger.error("exception sending mail: " + ose.getMessage());
            }

            postSuccessMessage(successMsg, context);

        } catch (JobInterruptedException e) {
            logger.info("Job was cancelled by the user");
            exceptions = true;
        } catch (TransformerConfigurationException e) {
            sendErrorEmail(e.getMessage(), context, alertEmail);
            postErrorMessage(e.getMessage(), context);
            logger.error("Error executing extract", e);
            exceptions = true;
        } catch (FileNotFoundException e) {
            sendErrorEmail(e.getMessage(), context, alertEmail);
            postErrorMessage(e.getMessage(), context);
            logger.error("Error executing extract", e);
            exceptions = true;
        } catch (TransformerFactoryConfigurationError e) {
            sendErrorEmail(e.getMessage(), context, alertEmail);
            postErrorMessage(e.getMessage(), context);
            logger.error("Error executing extract", e);
            exceptions = true;
        } catch (TransformerException e) {
            sendErrorEmail(e.getMessage(), context, alertEmail);
            postErrorMessage(e.getMessage(), context);
            logger.error("Error executing extract", e);
            exceptions = true;
        } catch (Exception ee) {
            sendErrorEmail(ee.getMessage() + " for " + jobName, context, alertEmail);
            postErrorMessage(ee.getMessage(), context);
            logger.error("Error executing extract", ee);
            exceptions = true;

            if (null != dataMap.get("job_type") && ((String) dataMap.get("job_type")).equalsIgnoreCase("exportJob")) {
                TriggerBean triggerBean = new TriggerBean();
                triggerBean.setUserAccount(userBean);
                if (dataMap != null) {
                    triggerBean.setFullName((String) dataMap.get(XsltTriggerService.JOB_NAME));
                }
                archivedDatasetFileBean.setStatus("FAILED");
                archivedDatasetFileDao.update(archivedDatasetFileBean);
            }

        } finally {

            if (in != null)
                try {
                    in.close();
                } catch (IOException e) {
                    logger.error("Error executing extract", e);
                }
            if (endFileStream != null)
                try {
                    endFileStream.close();
                } catch (IOException e) {
                    logger.error("Error executing extract", e);
                }

            if (exceptions) {
                logger.debug("EXCEPTIONS... EVEN THEN DELETING OFF OLD FILES");
                generalFileDir = dataMap.getString(XML_FILE_PATH);
                File oldFilesPath = new File(generalFileDir);

                if (oldFilesPath.isDirectory()) {

                    markForDelete = Arrays.asList(oldFilesPath.listFiles());

                }
                logger.debug("deleting the old files reference from archive dataset");

                if (deleteOld) {
                    deleteIntermFiles(markForDelete, "", doNotDeleteUntilExtract);
                }

            }
            if (datasetBean != null)
                resetArchiveDataset(datasetBean.getId());

            logger.info("Job " + context.getJobDetail().getDescription() + " finished.");
        }
    }

    /**
     * Initializes the dependencies of this job with the components from the Spring application context.
     * @param scheduler
     * @param dataMap
     */
    private void initDependencies(Scheduler scheduler, JobDataMap dataMap) {
        try {
            ApplicationContext ctx = (ApplicationContext) scheduler.getContext().get("applicationContext");
            DataSource dataSource = ctx.getBean(DataSource.class);
            studyDao = (StudyDao) ctx.getBean("studyDaoDomain");
            if (StringUtils.isEmpty(dataMap.getString(TENANT_SCHEMA)))
                CoreResources.tenantSchema.set(scheduler.getSchedulerName());
            else CoreResources.tenantSchema.set(dataMap.getString(TENANT_SCHEMA));
            mailSender = ctx.getBean(OpenClinicaMailSender.class);
            auditEventDAO = ctx.getBean(AuditEventDAO.class);
            datasetDao = ctx.getBean(DatasetDAO.class);
            userAccountDao = ctx.getBean(UserAccountDAO.class);
            archivedDatasetFileDao = ctx.getBean(ArchivedDatasetFileDAO.class);
            generateFileService = ctx.getBean(GenerateExtractFileService.class);
            odmFileCreation = ctx.getBean(OdmFileCreation.class);
        } catch (SchedulerException e) {
            throw new IllegalStateException("Could not load dependencies from scheduler context", e);
        }

    }

    private void zipAll(String path, String[] files, String zipname) throws IOException {
        final int BUFFER = 2048;
        BufferedInputStream orgin = null;

        FileInputStream fis = null;
        FileOutputStream fos = null;
        ZipOutputStream zos = null;
        try {
            fos = new FileOutputStream(zipname);
            zos = new ZipOutputStream(fos);
            byte data[] = new byte[BUFFER];

            for (String file : files) {
                logger.debug("Path = " + path + "zipName = " + zipname);
                fis = new FileInputStream(path + file);

                orgin = new BufferedInputStream(fis, BUFFER);
                ZipEntry entry = new ZipEntry(file);
                zos.putNextEntry(entry);
                int cnt = 0;
                while ((cnt = orgin.read(data, 0, BUFFER)) != -1) {
                    zos.write(data, 0, cnt);
                }
                fis.close();
            }

        } catch (IOException ioe) {
            throw new IllegalStateException("Error zipping XML files", ioe);
        } finally {
            if (fis != null) {
                fis.close();
            }
            if (orgin != null) {
                orgin.close();
            }
            if (zos != null) {
                zos.close();
            }
            if (fos != null) {
                fos.close();
            }
        }
        for (String file : files) {
            File f = new File(path + file);
            f.delete();
        }
    }

    /**
     * To go through all the existing archived datasets and delete off the
     * records whose file references do not exist any more.
     * @param datasetId
     */
    private void resetArchiveDataset(int datasetId) {

        ArrayList<ArchivedDatasetFileBean> al = archivedDatasetFileDao.findByDatasetId(datasetId);
        for (ArchivedDatasetFileBean fbExisting : al) {
            logger.debug("The file to checking?" + fbExisting.getFileReference() + "Does the file exist?" + new File(fbExisting.getFileReference()).exists());
            logger.debug("check if it still exists in archive set before" + archivedDatasetFileDao.findByDatasetId(fbExisting.getDatasetId()).size());
            if (!fbExisting.getFileReference().equals("") && !new File(fbExisting.getFileReference()).exists() && !fbExisting.getJobType().contentEquals("Scheduled")) {
                logger.debug(fbExisting.getFileReference() + "Doesnt exist,deleting it from archiveset data");

                deleteArchivedDataset(fbExisting);
            }
            logger.debug("check if it still exists in archive set after" + archivedDatasetFileDao.findByDatasetId(fbExisting.getDatasetId()).size());
        }

    }

    // zips up the resultant xml file
    private List<File> zipxmls(List<File> deleteFilesList, String endFile) throws IOException {
        final int BUFFER = 2048;
        BufferedInputStream orgin = null;
        File EndFile = new File(endFile);
        FileInputStream fis = null;
        FileOutputStream fos = null;
        ZipOutputStream zos = null;
        try {
            fis = new FileInputStream(EndFile);

            fos = new FileOutputStream(endFile + ".zip");
            zos = new ZipOutputStream(fos);

            byte data[] = new byte[BUFFER];
            orgin = new BufferedInputStream(fis, BUFFER);
            ZipEntry entry = new ZipEntry(new ZipEntry(EndFile.getName()));
            zos.putNextEntry(entry);
            int cnt = 0;
            while ((cnt = orgin.read(data, 0, BUFFER)) != -1) {
                zos.write(data, 0, cnt);
            }

        } catch (IOException ioe) {
            throw new IllegalStateException("Error zipping XML files", ioe);
        } finally {
            if (fis != null) {
                fis.close();
            }
            if (orgin != null) {
                orgin.close();
            }
            if (zos != null) {
                zos.close();
            }
            if (fos != null) {
                fos.close();
            }
        }

        // since zip is successful, deleting the endfile.
        logger.info("About to delete file: " + EndFile.getName());
        boolean deleted = EndFile.delete();
        logger.info("deleted? " + deleted);
        logger.info("Does the file exist still? " + EndFile.exists());
        return deleteFilesList;

    }

    private void deleteIntermFiles(List<File> intermediateFiles, String dontDeleteFile, String[] dontDelFiles) {

        Iterator<File> fileIt = intermediateFiles.iterator();
        File temp;
        File dontDelFile = new File(dontDeleteFile);
        int i;
        boolean del;
        while (fileIt.hasNext()) {
            temp = fileIt.next();
            if (!temp.getName().equals(dontDelFile.getName())) {
                i = 0;
                del = true;
                logger.debug("File Name?" + temp.getName());

                while (i < dontDelFiles.length && del) {
                    if (temp.getName().equals(dontDelFiles[i])) {
                        logger.debug("file to deleted:" + temp.getName() + "File Not to deleted:" + dontDelFiles[i]);
                        del = false;// file name contained in doNotDelete list,
                        // break;
                    }
                    i++;
                }
                if (del)
                    temp.delete();

            }
        }
    }

    // Utility method, might be useful in the future to convert to kilobytes.
    public float bytesToKilo(long bytes) {
        logger.info("output bytes?" + bytes + "divided by 1024" + bytes / KILOBYTE);
        logger.info("output bytes?" + bytes + "divided by 1024" + (float) bytes / KILOBYTE);
        return (float) bytes / KILOBYTE;
    }

    // A stub to delete old files.
    private void deleteOldFiles(File[] oldFiles) {
        for (File oldFile : oldFiles) {
            if (oldFile.exists())
                oldFile.delete();
        }

    }

    /**
     * Stub to get the list of all old files.
     * @param outputPath
     * @param postProcLoc
     * @return
     */
    private File[] getOldFiles(String outputPath, String postProcLoc) {
        File exisitingFiles[] = null;
        File temp = null;
        if (postProcLoc != null) {
            temp = new File(postProcLoc);
            if (temp.isDirectory())
                exisitingFiles = temp.listFiles();
        } else {
            temp = new File(outputPath);
            if (temp.isDirectory())
                exisitingFiles = temp.listFiles();
        }

        return exisitingFiles;
    }

    private File[] getInterFiles(String xmlLoc) {
        File exisitingFiles[] = null;
        File temp = null;
        if (xmlLoc != null) {
            temp = new File(xmlLoc);
            if (temp.isDirectory())
                exisitingFiles = temp.listFiles();
        }

        return exisitingFiles;
    }

    private void postSuccessMessage(String message, JobExecutionContext context) {
        String SCHEDULER = "schedulerFactoryBean";
        try {
            ApplicationContext appContext = (ApplicationContext) context.getScheduler().getContext().get("applicationContext");
            StdScheduler scheduler = (StdScheduler) appContext.getBean(SCHEDULER);
            JobDetail jobDetail = context.getJobDetail();
            JobDataMap dataMap = jobDetail.getJobDataMap();
            dataMap.put("successMsg", message);
            jobDetail.getJobBuilder().usingJobData(dataMap);
            // replace the job with the extra data
            scheduler.addJob(jobDetail, true);

        } catch (SchedulerException e) {
            throw new IllegalStateException("Error processing post success message", e);

        }
    }

    private void postErrorMessage(String message, JobExecutionContext context) {
        String SCHEDULER = "schedulerFactoryBean";
        try {
            ApplicationContext appContext = (ApplicationContext) context.getScheduler().getContext().get("applicationContext");
            StdScheduler scheduler = (StdScheduler) appContext.getBean(SCHEDULER);
            JobDetail jobDetail = context.getJobDetail();
            JobDataMap dataMap = jobDetail.getJobDataMap();
            dataMap.put("failMessage", message);
            jobDetail.getJobBuilder().usingJobData(dataMap);
            // replace the job with the extra data
            scheduler.addJob(jobDetail, true);

        } catch (SchedulerException e) {
            throw new IllegalStateException("Error processing post error message", e);
        }
    }

    private synchronized ArchivedDatasetFileBean generateFileRecord(String name, String dir, double time, long fileLength, ExportFormatBean efb,
                                                       Set<Integer> edcSet, ArchivedDatasetFileBean archivedDatasetFileBean) {

        // Updating archive dataset file that is IN_PROGRESS.

        if (ClinicalDataCollector.datasetFiltered.equals("YES")) {
            archivedDatasetFileBean.setName("filtered-" + name);
        } else {
            archivedDatasetFileBean.setName(name);
        }

        archivedDatasetFileBean.setFileReference(dir + name);
        archivedDatasetFileBean.setFileSize((int) fileLength);
        archivedDatasetFileBean.setRunTime(time);// to convert to seconds
        archivedDatasetFileBean.setExportFormatBean(efb);
        archivedDatasetFileBean.setExportFormatId(efb.getId());
        archivedDatasetFileBean.setDateCreated(new Date());
        archivedDatasetFileBean.setJobExecutionUuid(UUID.randomUUID().toString());
        archivedDatasetFileBean.setStatus(JobStatus.COMPLETED.name());

        archivedDatasetFileBean = updateArchivedDatasetFileToComplete(archivedDatasetFileBean, edcSet);
        return archivedDatasetFileBean;
    }

    private void sendErrorEmail(String message, JobExecutionContext context, String target) {
        String subject = "Warning: " + message;
        String emailBody = "An exception was thrown while running an extract job on your server, please see the logs for more details.";
        try {
            ApplicationContext appContext = (ApplicationContext) context.getScheduler().getContext().get("applicationContext");
            mailSender = (OpenClinicaMailSender) appContext.getBean("openClinicaMailSender");

            mailSender.sendEmail(target, EmailEngine.getAdminEmail(), subject, emailBody, false);
            logger.info("sending an email to " + target + " from " + EmailEngine.getAdminEmail());
        } catch (SchedulerException se) {
            logger.warn("Error sending email", se);
        } catch (OpenClinicaSystemException ose) {
            logger.warn("Error sending email", ose);
        }

    }

    // Utility method to format upto 3 decimals.
    private double setFormat(double number) {
        if (number < 1) number = 1.0;
        DecimalFormat df = new DecimalFormat("#.#");
        logger.info("Number is " + Double.parseDouble(df.format(number)));
        logger.info("Number is " + (float) Double.parseDouble(df.format(number)));
        return Double.valueOf(df.format(number));
    }

    public void persistPermissionTags(Set<Integer> edcSet, int archivedDatasetFileId) {
        List<String> permissionTagsList = new ArrayList();
        for (Integer edcId : edcSet) {
            List<String> tgs = eventDefinitionCrfPermissionTagDao.findTagsForEdcId(edcId);
            if (tgs != null) {
                permissionTagsList.addAll(tgs);
            }
        }
        Set<String> s = new LinkedHashSet<>(permissionTagsList);

        for (String sbsTag : s) {
            ArchivedDatasetFilePermissionTag adfTag = new ArchivedDatasetFilePermissionTag(archivedDatasetFileId, sbsTag);
            archivedDatasetFilePermissionTagDao.saveOrUpdate(adfTag);
        }
    }

    private void deleteArchivedDataset(ArchivedDatasetFileBean adf) {
        archivedDatasetFilePermissionTagDao.delete(adf.getId());
        archivedDatasetFileDao.deleteArchiveDataset(adf);
    }

    private ArchivedDatasetFileBean createArchivedDataset(ArchivedDatasetFileBean adf, Set<Integer> edcSet) {
        ArchivedDatasetFileBean fbFinal = (ArchivedDatasetFileBean) archivedDatasetFileDao.create(adf);
        persistPermissionTags(edcSet, fbFinal.getId());
        return fbFinal;
    }


    private ArchivedDatasetFileBean updateArchivedDatasetFileToComplete(ArchivedDatasetFileBean adf, Set<Integer> edcSet) {
        ArchivedDatasetFileBean fbFinal = (ArchivedDatasetFileBean) archivedDatasetFileDao.update(adf);
        persistPermissionTags(edcSet, fbFinal.getId());
        return fbFinal;
    }

    private void deleteOldFileReferences(JobDataMap dataMap, ArchivedDatasetFileBean archivedDatasetFileBean) {
        //Deleting old file_references from archived_dataset_file
        int filesToKeep = dataMap.getInt(XsltTriggerService.NUMBER_OF_FILES_TO_SAVE);
        List<ArchivedDatasetFileBean> archivedDatasetFileBeans = archivedDatasetFileDao.findByJobUuidWithFileReference(archivedDatasetFileBean.getJobUuid());
        int total = archivedDatasetFileBeans.size();
        int numToDelete = total - filesToKeep;

        //since it's already ordered by earliest date to later date, just delete the number needed.
        for (int i = 0; i < numToDelete; i++) {
            ArchivedDatasetFileBean adfb = archivedDatasetFileBeans.get(i);
            File fileToDelete = new File(adfb.getFileReference());
            fileToDelete.delete();
            adfb.setFileReference(null);
            archivedDatasetFileDao.update(adfb);
        }

        if (numToDelete > 0)
            logger.info("Deleted " + numToDelete + " old file references for jobUuid " + archivedDatasetFileBean.getJobUuid() + ".");
    }


    private void deleteNonZippedFilesInFolder(String generalFileDir) {
        File oldFilesPath = new File(generalFileDir);
        File[] fileList = oldFilesPath.listFiles();
        for (int i = 0; i < fileList.length; i++) {
            if (!fileList[i].getName().contains(".zip")) {
                fileList[i].delete();
            }
        }
    }

}
