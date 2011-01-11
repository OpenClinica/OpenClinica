package org.akaza.openclinica.job;

import org.akaza.openclinica.bean.extract.ArchivedDatasetFileBean;
import org.akaza.openclinica.bean.extract.DatasetBean;
import org.akaza.openclinica.bean.extract.ExportFormatBean;
import org.akaza.openclinica.bean.extract.ExtractBean;
import org.akaza.openclinica.bean.extract.ExtractPropertyBean;
import org.akaza.openclinica.bean.login.UserAccountBean;
import org.akaza.openclinica.bean.managestudy.StudyBean;
import org.akaza.openclinica.bean.service.ProcessingFunction;
import org.akaza.openclinica.bean.service.ProcessingResultType;
import org.akaza.openclinica.core.EmailEngine;
import org.akaza.openclinica.core.OpenClinicaMailSender;
import org.akaza.openclinica.dao.core.CoreResources;
import org.akaza.openclinica.dao.extract.ArchivedDatasetFileDAO;
import org.akaza.openclinica.dao.extract.DatasetDAO;
import org.akaza.openclinica.dao.hibernate.RuleSetRuleDao;
import org.akaza.openclinica.dao.login.UserAccountDAO;
import org.akaza.openclinica.dao.managestudy.StudyDAO;
import org.akaza.openclinica.exception.OpenClinicaSystemException;
import org.akaza.openclinica.i18n.util.ResourceBundleProvider;
import org.akaza.openclinica.service.extract.GenerateExtractFileService;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.SchedulerException;
import org.quartz.impl.StdScheduler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.scheduling.quartz.QuartzJobBean;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.zip.ZipEntry;

import java.util.zip.ZipOutputStream;

import javax.sql.DataSource;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

/**
 * Xalan Transform Job, an XSLT transform job using the Xalan classes
 * 
 * @author thickerson
 * 
 */
public class XsltTransformJob extends QuartzJobBean {

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
    public static final String ZIPPED = "zipped";
    public static final String DELETE_OLD = "deleteOld";
    public static final String SUCCESS_MESSAGE = "SUCCESS_MESSAGE";
    public static final String FAILURE_MESSAGE = "FAILURE_MESSAGE";
    private OpenClinicaMailSender mailSender;
    private DataSource dataSource;
    private GenerateExtractFileService generateFileService;
    private StudyDAO studyDao;
    private UserAccountDAO userAccountDao;
    private CoreResources coreResources;
    private RuleSetRuleDao ruleSetRuleDao;
    public static final String EP_BEAN = "epBean";

    // POST PROCESSING VARIABLES
    public static final String POST_PROC_DELETE_OLD = "postProcDeleteOld";
    public static final String POST_PROC_ZIP = "postProcZip";
    public static final String POST_PROC_LOCATION = "postProcLocation";
    public static final String POST_PROC_EXPORT_NAME = "postProcExportName";
    private static final long MEGABYTE = 1024L * 1024L;
    private static final long KILOBYTE = 1024;

    protected final Logger logger = LoggerFactory.getLogger(getClass().getName());

    @Override
    protected void executeInternal(JobExecutionContext context) throws JobExecutionException {
        // need to generate a Locale for emailing users with i18n
        // TODO make dynamic?
        CoreResources cr = new CoreResources();
        Locale locale = new Locale("en-US");
        ResourceBundleProvider.updateLocale(locale);
        ResourceBundle pageMessages = ResourceBundleProvider.getPageMessagesBundle();
        List<File> markForDelete = new LinkedList<File>();
        Boolean zipped = true;
        Boolean deleteOld = true;
        Boolean exceptions = false;
        JobDataMap dataMap = context.getMergedJobDataMap();
        String localeStr = dataMap.getString(LOCALE);
        DatasetBean datasetBean = null;
        if (localeStr != null) {
            locale = new Locale(localeStr);
            ResourceBundleProvider.updateLocale(locale);
            pageMessages = ResourceBundleProvider.getPageMessagesBundle();
        }
        // get the file information from the job
        String alertEmail = dataMap.getString(EMAIL);
        java.io.InputStream in = null;
        FileOutputStream endFileStream = null;
        try {
            ApplicationContext appContext = (ApplicationContext) context.getScheduler().getContext().get("applicationContext");
            mailSender = (OpenClinicaMailSender) appContext.getBean("openClinicaMailSender");
            dataSource = (DataSource) appContext.getBean("dataSource");
            coreResources = (CoreResources) appContext.getBean("coreResources");
            ruleSetRuleDao = (RuleSetRuleDao) appContext.getBean("ruleSetRuleDao");

            DatasetDAO dsdao = new DatasetDAO(dataSource);

            // init all fields from the data map
            int userAccountId = dataMap.getInt(USER_ID);
            int studyId = dataMap.getInt(STUDY_ID);
            String outputPath = dataMap.getString(POST_FILE_PATH);
            // get all user info, generate xml
            // logger.info("found output path: " + outputPath);
            logger.debug("found output path: " + outputPath);
            String generalFileDir = dataMap.getString(XML_FILE_PATH);
            int epBeanId = dataMap.getInt(EXTRACT_PROPERTY);
            int dsId = dataMap.getInt(DATASET_ID);
            // JN: Change from earlier versions, cannot get static reference as
            // static references don't work. Reason being for example there could be
            // datasetId as a variable which is different for each dataset and
            // that needs to be loaded dynamically
            ExtractPropertyBean epBean = (ExtractPropertyBean) dataMap.get(EP_BEAN);
            zipped = epBean.getZipFormat();
            deleteOld = epBean.getDeleteOld();
            long sysTimeBegin = System.currentTimeMillis();
            userAccountDao = new UserAccountDAO(dataSource);
            UserAccountBean userBean = (UserAccountBean) userAccountDao.findByPK(userAccountId);
            generateFileService = new GenerateExtractFileService(dataSource, userBean, coreResources, ruleSetRuleDao);
            studyDao = new StudyDAO(dataSource);
            StudyBean currentStudy = (StudyBean) studyDao.findByPK(studyId);
            StudyBean parentStudy = (StudyBean) studyDao.findByPK(currentStudy.getParentStudyId());
            String successMsg = epBean.getSuccessMessage();
            String failureMsg = epBean.getFailureMessage();

            // DatasetBean dsBean = (DatasetBean)datasetDao.findByPK(new
            // Integer(datasetId).intValue());

            datasetBean = (DatasetBean) dsdao.findByPK(dsId);
            ExtractBean eb = generateFileService.generateExtractBean(datasetBean, currentStudy, parentStudy);

            // generate file directory for file service

            datasetBean.setName(datasetBean.getName().replaceAll(" ", "_"));
            logger.debug("--> job starting: ");

            HashMap answerMap =
                generateFileService.createODMFile(epBean.getFormat(), sysTimeBegin, generalFileDir, datasetBean, currentStudy, "", eb, currentStudy.getId(),
                        currentStudy.getParentStudyId(), "99", (Boolean) dataMap.get(ZIPPED), false, (Boolean) dataMap.get(DELETE_OLD));
            // won't save a record of the XML to db
            // won't be a zipped file, so that we can submit it for
            // transformation
            // this will have to be toggled by the export data format? no, the
            // export file will have to be zipped/not zipped
            String ODMXMLFileName = "";
            int fId = 0;
            for (Iterator it = answerMap.entrySet().iterator(); it.hasNext();) {
                java.util.Map.Entry entry = (java.util.Map.Entry) it.next();
                Object key = entry.getKey();
                Object value = entry.getValue();
                ODMXMLFileName = (String) key;// JN: Since there is a logic to
                                              // delete all the intermittent
                                              // files, this file could be a zip
                                              // file.
                Integer fileID = (Integer) value;
                fId = fileID.intValue();
                logger.debug("found " + fId + " and " + ODMXMLFileName);
            }

            // create dirs

            File output = new File(outputPath);
            if (!output.isDirectory()) {
                output.mkdirs();
            }

            TransformerFactory tFactory = TransformerFactory.newInstance();

            // Use the TransformerFactory to instantiate a Transformer that will
            // work with
            // the stylesheet you specify. This method call also processes the
            // stylesheet
            // into a compiled Templates object.
            in = new java.io.FileInputStream(dataMap.getString(XSL_FILE_PATH));

            Transformer transformer = tFactory.newTransformer(new StreamSource(in));

            // move xml generation here, tbh
            String xmlFilePath = generalFileDir + ODMXMLFileName;
            String endFile = outputPath + File.separator + dataMap.getString(POST_FILE_NAME);
            File oldFilesPath = new File(generalFileDir);

            if (oldFilesPath.isDirectory()) {

                markForDelete = Arrays.asList(oldFilesPath.listFiles());
                // logic to prevent deleting the file being created.

            }
            final long start = System.currentTimeMillis();
            endFileStream = new FileOutputStream(endFile);
            transformer.transform(new StreamSource(xmlFilePath), new StreamResult(endFileStream));
            // JN...CLOSE THE STREAM...HMMMM
            in.close();
            endFileStream.close();
            final long done = System.currentTimeMillis() - start;
            logger.info("--> job completed in " + done + " ms");
            // run post processing

            ProcessingFunction function = epBean.getPostProcessing();
            String subject = "";
            // String emailBody = "";
            StringBuffer emailBuffer = new StringBuffer("");
            emailBuffer.append("<p>" + pageMessages.getString("email_header_1") + " " + EmailEngine.getAdminEmail() + " "
                + pageMessages.getString("email_header_2") + " Job Execution " + pageMessages.getString("email_header_3") + "</p>");
            emailBuffer.append("<P>Dataset: " + datasetBean.getName() + "</P>");
            emailBuffer.append("<P>Study: " + currentStudy.getName() + "</P>");
            if(function!=null && function.getClass().equals(org.akaza.openclinica.bean.service.SqlProcessingFunction.class))
            {
                String dbUrl = ((org.akaza.openclinica.bean.service.SqlProcessingFunction)function).getDatabaseUrl();
                int lastIndex = dbUrl.lastIndexOf("/");
                String schemaName = dbUrl.substring(lastIndex);
                int HostIndex = dbUrl.substring(0, lastIndex).indexOf("//");
                String Host = dbUrl.substring(HostIndex,lastIndex);
                emailBuffer.append("<P>Database: " + ((org.akaza.openclinica.bean.service.SqlProcessingFunction)function).getDatabaseType() + "</P>");
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
                if (message.getCode().intValue() != 2)
                    deleteOldFiles(intermediateFiles);
                final long done2 = System.currentTimeMillis() - start;
                logger.info("--> postprocessing completed in " + done2 + " ms, found result type " + message.getCode());
                logger.info("--> postprocessing completed in " + done2 + " ms, found result type " + message.getCode());
                /*
                 * if((Boolean)dataMap.get(POST_PROC_DELETE_OLD)) {
                 * deleteOldFiles(oldFiles); }
                 */

                if (!function.getClass().equals(org.akaza.openclinica.bean.service.SqlProcessingFunction.class)) {
                    String archivedFile = dataMap.getString(POST_FILE_NAME) + "." + function.getFileType();
                    // JN: if the properties is set to zip the output file,
                    // download the zip file
                    if (function.isZip())
                        archivedFile = archivedFile + ".zip";
                    // JN: The above 2 lines code is useless, it should be
                    // removed..added it only for the sake of custom processing
                    // but it will produce erroneous results in case of custom
                    // post processing as well.
                    if (function.getClass().equals(org.akaza.openclinica.bean.service.PdfProcessingFunction.class))
                        archivedFile = function.getArchivedFileName();

                    ArchivedDatasetFileBean fbFinal =
                        generateFileRecord(archivedFile, outputPath, datasetBean, done, new File(outputPath + File.separator + archivedFile).length(),
                                ExportFormatBean.PDFFILE, userAccountId);

                    if (successMsg == null || successMsg.isEmpty()) {
                        emailBuffer.append("<p>" + pageMessages.getString("html_email_body_4") + " " + fbFinal.getName()
                            + pageMessages.getString("html_email_body_4_5") + CoreResources.getField("sysURL.base") + "AccessFile?fileId=" + fbFinal.getId()
                            + pageMessages.getString("html_email_body_3") + "</p>");

                    }

                    if (successMsg.contains("$linkURL")) {
                        successMsg =
                            successMsg.replace("$linkURL", "<a href=\"" + CoreResources.getField("sysURL.base") + "AccessFile?fileId=" + fbFinal.getId()
                                + "\">here </a>");
                    }
                    emailBuffer.append("<p>" + successMsg + "</p>");
                    long sysTimeEnd = System.currentTimeMillis() - sysTimeBegin;
                    if (fbFinal != null) {
                        fbFinal.setFileSize((int) bytesToKilo(new File(archivedFile).length()));
                        fbFinal.setRunTime((int) sysTimeEnd);
                    }

                }
                // otherwise don't do it
                if (message.getCode().intValue() == 1) {
                    subject = "Success: " + datasetBean.getName();
                } else if (message.getCode().intValue() == 2) {
                    subject = "Failure: " + datasetBean.getName();
                    if (failureMsg != null && !failureMsg.isEmpty()) {
                        emailBuffer.append(failureMsg);
                    }
                    emailBuffer.append("<P>" + message.getDescription());
                    postErrorMessage(message.getDescription(), context);
                } else if (message.getCode().intValue() == 3) {
                    subject = "Update: " + datasetBean.getName();
                }

                // subject = "" + datasetBean.getName();

            } else {
                // extract ran but no post-processing - we send an email with
                // success and url to link to
                // generate archived dataset file bean here, and use the id to
                // build the URL
                String archivedFilename = dataMap.getString(POST_FILE_NAME);
                // JN: if the properties is set to zip the output file, download
                // the zip file
                if (zipped)
                    archivedFilename = dataMap.getString(POST_FILE_NAME) + ".zip";
                // delete old files now
                List<File> intermediateFiles = generateFileService.getOldFiles();
                String[] dontDelFiles = epBean.getDoNotDelFiles();
                int cnt = dataMap.getInt("count");
                if (dontDelFiles.length > 1 && zipped) {
                    // unzip(dontDelFiles);
                    logMe("count =====" + cnt + "dontDelFiles length==---" + dontDelFiles.length);

                    if (cnt == dontDelFiles.length - 1) {
                        logMe("Entering this?" + cnt + "dontDelFiles" + dontDelFiles);
                        File temp = new File(endFile);
                        String path = outputPath + File.separator;
                        logMe("path = " + path);
                        logMe("zipName?? = " + epBean.getZipName());

                        String zipName =
                            (epBean.getZipName() == null || epBean.getZipName().isEmpty()) ? endFile + ".zip" : path + epBean.getZipName() + ".zip";

                        archivedFilename = new File(zipName).getName();
                        zipAll(path, epBean.getDoNotDelFiles(), zipName);
                        String[] tempArray = { archivedFilename };
                        dontDelFiles = tempArray;

                    }

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

                ArchivedDatasetFileBean fbFinal =
                    generateFileRecord(archivedFilename, outputPath, datasetBean, done, new File(outputPath + File.separator + archivedFilename).length(),
                            ExportFormatBean.TXTFILE, userAccountId);
                subject = "Job Ran: " + datasetBean.getName();
                // emailBody = datasetBean.getName() +
                // " has run and you can access it ";// add url here
                // emailBody = emailBody + "<a href='" +
                // CoreResources.getField("sysURL.base") +
                // "AccessFile?fileId=" +
                // fbFinal.getId() + "'>here</a>.";

                if (successMsg == null || successMsg.isEmpty()) {
                    emailBuffer.append("<p>" + pageMessages.getString("html_email_body_4") + " " + fbFinal.getName()
                        + pageMessages.getString("html_email_body_4_5") + CoreResources.getField("sysURL.base") + "AccessFile?fileId=" + fbFinal.getId()
                        + pageMessages.getString("html_email_body_3") + "</p>");
                    logger.info("email buffer??" + emailBuffer);

                } else {
                    if (successMsg.contains("$linkURL")) {
                        successMsg =
                            successMsg.replace("$linkURL", "<a href=\"" + CoreResources.getField("sysURL.base") + "AccessFile?fileId=" + fbFinal.getId()
                                + "\">here </a>");
                    }
                    emailBuffer.append("<p>" + successMsg + "</p>");
                }

                if (deleteOld) {
                    deleteIntermFiles(intermediateFiles, endFile, dontDelFiles);

                    deleteIntermFiles(markForDelete, endFile, dontDelFiles);

                }

            }
            // email the message to the user
            // String email = dataMap.getString(EMAIL);
            emailBuffer.append("<p>" + pageMessages.getString("html_email_body_5") + "</p>");
            try {
                mailSender.sendEmail(alertEmail, EmailEngine.getAdminEmail(), subject, emailBuffer.toString(), true);
            } catch (OpenClinicaSystemException ose) {
                // Do Nothing, In the future we might want to have an email
                // status added to system.
                logger.info("exception sending mail: " + ose.getMessage());
                logger.error("exception sending mail: " + ose.getMessage());
            }

            logger.info("just sent email to " + alertEmail + ", from " + EmailEngine.getAdminEmail());
            dataMap.put("successMsg", successMsg);
            logMe(emailBuffer.toString());
        } catch (TransformerConfigurationException e) {
            sendErrorEmail(e.getMessage(), context, alertEmail);
            postErrorMessage(e.getMessage(), context);
            e.printStackTrace();
            logger.error(e.getStackTrace().toString());
            exceptions = true;
        } catch (FileNotFoundException e) {
            sendErrorEmail(e.getMessage(), context, alertEmail);
            postErrorMessage(e.getMessage(), context);
            e.printStackTrace();
            logger.error(e.getStackTrace().toString());
            exceptions = true;
        } catch (TransformerFactoryConfigurationError e) {
            sendErrorEmail(e.getMessage(), context, alertEmail);
            postErrorMessage(e.getMessage(), context);
            e.printStackTrace();
            logger.error(e.getStackTrace().toString());
            exceptions = true;
        } catch (TransformerException e) {
            sendErrorEmail(e.getMessage(), context, alertEmail);
            postErrorMessage(e.getMessage(), context);
            e.printStackTrace();
            logger.error(e.getStackTrace().toString());
            exceptions = true;
        } catch (Exception ee) {
            sendErrorEmail(ee.getMessage(), context, alertEmail);
            postErrorMessage(ee.getMessage(), context);
            ee.printStackTrace();
            logger.error(ee.getStackTrace().toString());
            exceptions = true;
        } finally {

            if (in != null)
                try {
                    in.close();
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            if (endFileStream != null)
                try {
                    endFileStream.close();
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }

            if (exceptions) {
                logger.debug("EXCEPTIONS... EVEN TEHN DELETING OFF OLD FILES");
                String generalFileDir = dataMap.getString(XML_FILE_PATH);
                File oldFilesPath = new File(generalFileDir);

                String endFile = dataMap.getString(POST_FILE_PATH) + File.separator + dataMap.getString(POST_FILE_NAME);
                if (oldFilesPath.isDirectory()) {

                    markForDelete = Arrays.asList(oldFilesPath.listFiles());

                }
                logger.debug("deleting the old files reference from archive dataset");

                if (deleteOld) {
                    deleteIntermFiles(markForDelete, endFile, new String[1]);
                }

            }
            if (datasetBean != null)
                resetArchiveDataset(datasetBean.getId());

        }

    }

    private void logMe(String message) {
        logger.debug(message);
         System.out.println(message);
    }

    private void zipAll(String path, String[] files, String zipname) throws IOException {
        final int BUFFER = 2048;
        BufferedInputStream orgin = null;

        FileInputStream fis = null;
        FileOutputStream fos = null;
        ZipOutputStream zos = null;
        File tempFile = new File(zipname);
        try {
            fos = new FileOutputStream(zipname);
            zos = new ZipOutputStream(fos);
            byte data[] = new byte[BUFFER];

            for (int i = 0; i < files.length; i++) {
                logMe("Path = " + path + "zipName = " + zipname);
                fis = new FileInputStream(path + files[i]);

                orgin = new BufferedInputStream(fis, BUFFER);
                ZipEntry entry = new ZipEntry(files[i]);
                zos.putNextEntry(entry);
                int cnt = 0;
                while ((cnt = orgin.read(data, 0, BUFFER)) != -1) {
                    zos.write(data, 0, cnt);
                }
            }

        } catch (IOException ioe) {
            ioe.printStackTrace();
        } finally {
            if (fis != null)
                fis.close();
            if (orgin != null)
                orgin.close();
            if (zos != null)
                zos.close();
            if (fos != null)
                fos.close();

        }
    }

    /**
     * To go through all the existing archived datasets and delete off the
     * records whose file references do not exist any more.
     * 
     * @param datasetId
     */
    private void resetArchiveDataset(int datasetId) {
        // ArchivedDatasetFileBean fbExisting= new ArchivedDatasetFileBean();

        ArchivedDatasetFileDAO asdfDAO = new ArchivedDatasetFileDAO(dataSource);
        ArrayList<ArchivedDatasetFileBean> al = asdfDAO.findByDatasetId(datasetId);
        for (ArchivedDatasetFileBean fbExisting : al) {
            logMe("The file to checking?" + fbExisting.getFileReference() + "Does the file exist?" + new File(fbExisting.getFileReference()).exists());
            logMe("check if it still exists in archive set before" + asdfDAO.findByDatasetId(fbExisting.getDatasetId()).size());
            if (!new File(fbExisting.getFileReference()).exists()) {
                logMe(fbExisting.getFileReference() + "Doesnt exist,deleting it from archiveset data");
                asdfDAO.deleteArchiveDataset(fbExisting);

            }
            logMe("check if it still exists in archive set after" + asdfDAO.findByDatasetId(fbExisting.getDatasetId()).size());
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
        File tempFile = new File(endFile + ".zip");
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
            ioe.printStackTrace();
        } finally {
            if (fis != null)
                fis.close();
            if (orgin != null)
                orgin.close();
            if (zos != null)
                zos.close();
            if (fos != null)
                fos.close();

        }

        // since zip is successful, deleting the endfile.
        logger.info("About to delete file" + EndFile.getName());
        boolean deleted = EndFile.delete();
        logger.info("deleted?" + deleted);
        logger.info("Does the file exist still?" + EndFile.exists());
        return deleteFilesList;

    }

    private void deleteIntermFiles(List<File> intermediateFiles, String dontDeleteFile, String[] dontDelFiles) {

        Iterator<File> fileIt = intermediateFiles.iterator();
        File temp = null;
        File DontDelFile = new File(dontDeleteFile);
        int i = 0;
        boolean del = true;
        while (fileIt.hasNext()) {
            temp = fileIt.next();
            if (!temp.getName().equals(DontDelFile.getName())) {
                i = 0;
                del = true;
                logger.info("File Name?" + temp.getName());

                while (i < dontDelFiles.length && del) {

                    if (temp.getName().equals(dontDelFiles[i])) {
                        logger.info("file to deleted:" + temp.getName() + "File Not to deleted:" + dontDelFiles[i]);

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
        // File[] files = complete.listFiles();
        for (int i = 0; i < oldFiles.length; i++) {
            if (oldFiles[i].exists())
                oldFiles[i].delete();
        }

    }

    /**
     * Stub to get the list of all old files.
     * 
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

    private void postErrorMessage(String message, JobExecutionContext context) {
        String SCHEDULER = "schedulerFactoryBean";
        try {
            ApplicationContext appContext = (ApplicationContext) context.getScheduler().getContext().get("applicationContext");
            StdScheduler scheduler = (StdScheduler) appContext.getBean(SCHEDULER);
            JobDetail jobDetail = context.getJobDetail();
            JobDataMap dataMap = jobDetail.getJobDataMap();
            dataMap.put("failMessage", message);
            jobDetail.setJobDataMap(dataMap);
            // replace the job with the extra data
            scheduler.addJob(jobDetail, true);

        } catch (Exception e) {

        }
    }

    private ArchivedDatasetFileBean generateFileRecord(String name, String dir, DatasetBean datasetBean, long time, long fileLength, ExportFormatBean efb,
            int userBeanId) {
        ArchivedDatasetFileBean fbFinal = new ArchivedDatasetFileBean();

        ArchivedDatasetFileBean fbInitial = new ArchivedDatasetFileBean();
        // Deleting off the original file archive dataset file.
        ArchivedDatasetFileDAO asdfDAO = new ArchivedDatasetFileDAO(dataSource);

        fbInitial.setName(name);
        fbInitial.setFileReference(dir + name);

        // JN: the following is to convert to KB, not possible without changing
        // database schema
        // fbInitial.setFileSize( setFormat(bytesToKilo(fileLength)));

        fbInitial.setFileSize((int) (fileLength));

        // logger.info("ODM setFileSize: " + (int)newFile.length() );
        // set the above to compressed size?
        // JN: the following commented out code is to convert from milli secs to
        // secs
        // fbInitial.setRunTime(setFormat((float)time/1000));// to convert to
        // seconds
        fbInitial.setRunTime((int) time);// to convert to seconds
        // logger.info("ODM setRunTime: " + (int)time );
        // need to set this in milliseconds, get it passed from above
        // methods?
        fbInitial.setDatasetId(datasetBean.getId());
        // logger.info("ODM setDatasetid: " + ds.getId() );
        fbInitial.setExportFormatBean(efb);
        // logger.info("ODM setExportFormatBean: success" );
        fbInitial.setExportFormatId(efb.getId());
        // logger.info("ODM setExportFormatId: " + efb.getId());
        // fbInitial.setOwner(userBean);
        // logger.info("ODM setOwner: " + sm.getUserBean());
        fbInitial.setOwnerId(userBeanId);
        // logger.info("ODM setOwnerId: " + sm.getUserBean().getId() );
        fbInitial.setDateCreated(new Date(System.currentTimeMillis()));

        fbFinal = (ArchivedDatasetFileBean) asdfDAO.create(fbInitial);
        return fbFinal;
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
            se.printStackTrace();
        } catch (OpenClinicaSystemException ose) {
            ose.printStackTrace();
        }

    }

    // Utility method to format upto 3 decimals.
    private float setFormat(float number) {
        DecimalFormat df = new DecimalFormat("0.000");
        logger.info("Number is" + Double.parseDouble(df.format(number)));
        logger.info("Number is" + (float) Double.parseDouble(df.format(number)));
        return (float) Double.parseDouble(df.format(number));
    }

}
