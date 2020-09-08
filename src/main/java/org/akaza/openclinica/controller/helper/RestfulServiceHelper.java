package org.akaza.openclinica.controller.helper;

import com.google.common.io.Files;
import core.org.akaza.openclinica.bean.core.Role;
import core.org.akaza.openclinica.bean.login.StudyUserRoleBean;
import core.org.akaza.openclinica.bean.login.UserAccountBean;
import core.org.akaza.openclinica.core.form.StringUtil;
import core.org.akaza.openclinica.dao.hibernate.StudyDao;
import core.org.akaza.openclinica.domain.datamap.Study;
import core.org.akaza.openclinica.service.StudyBuildService;
import core.org.akaza.openclinica.service.StudyBuildServiceImpl;
import org.akaza.openclinica.control.SpringServletAccess;
import org.akaza.openclinica.control.submit.ImportCRFInfoSummary;
import core.org.akaza.openclinica.dao.core.CoreResources;
import core.org.akaza.openclinica.dao.login.UserAccountDAO;
import core.org.akaza.openclinica.exception.OpenClinicaException;
import core.org.akaza.openclinica.exception.OpenClinicaSystemException;
import core.org.akaza.openclinica.logic.importdata.PipeDelimitedDataHelper;
import org.akaza.openclinica.service.CsvFileConverterServiceImpl;
import org.akaza.openclinica.service.ExcelFileConverterServiceImpl;
import org.akaza.openclinica.service.SasFileConverterServiceImpl;
import org.akaza.openclinica.web.restful.errors.ErrorConstants;
import org.apache.commons.dbcp2.BasicDataSource;
import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.context.annotation.Configuration;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.validation.Errors;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.sql.DataSource;
import java.io.*;
import java.sql.Connection;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;

import static org.akaza.openclinica.control.core.SecureController.USER_BEAN_NAME;

@Configurable
@Service("serviceHelper")
public class RestfulServiceHelper {

    private final static Logger log = LoggerFactory.getLogger("RestfulServiceHelper");

    //CSV file header
    private static final String SAS_FILE_EXTENSION = "sas7bdat";
    private static final String XLSX_FILE_EXTENSION = "xlsx";
    private static final String CSV_FILE_EXTENSION = "csv";
    private static final String TXT_FILE_EXTENSION = "txt";

    private DataSource dataSource;
    private StudyDao studyDao;
    private StudyBuildService studyBuildService;
    private UserAccountDAO userAccountDAO;
    private PipeDelimitedDataHelper importDataHelper;
    private MessageLogger messageLogger;
    private SasFileConverterServiceImpl sasFileConverterService;
    private ExcelFileConverterServiceImpl excelFileConverterService;
    private CsvFileConverterServiceImpl csvFileConverterService;

    public RestfulServiceHelper() {

    }

    public RestfulServiceHelper(DataSource dataSource, StudyBuildService studyBuildService, StudyDao studyDao) {
        this.dataSource = dataSource;
        this.studyBuildService = studyBuildService;
        this.studyDao = studyDao;
    }

    public RestfulServiceHelper(DataSource dataSource,
                                StudyBuildService studyBuildService,
                                StudyDao studyDao,
                                SasFileConverterServiceImpl sasFileConverterService,
                                ExcelFileConverterServiceImpl excelFileConverterService,
                                CsvFileConverterServiceImpl csvFileConverterService) {
        this.dataSource = dataSource;
        this.studyBuildService = studyBuildService;
        this.studyDao = studyDao;
        this.sasFileConverterService = sasFileConverterService;
        this.excelFileConverterService = excelFileConverterService;
        this.csvFileConverterService = csvFileConverterService;
    }

    /**
     * @param studyOid
     * @param request
     * @return
     * @throws Exception
     */
    public Study setSchema(String studyOid, HttpServletRequest request) throws OpenClinicaSystemException {
        // first time, the default DB schema for restful service is public
        Study study = studyDao.findPublicStudy(studyOid);

        Connection con;
        String schemaNm = "";

        if (study == null) {
            throw new OpenClinicaSystemException("errorCode.studyNotExist", "The study identifier you provided:" + studyOid + " is not valid.");

        } else {
            schemaNm = study.getSchemaName();
        }
        request.setAttribute("requestSchema", schemaNm);
        // get correct study from the right DB schema
        study = studyDao.findByOcOID(studyOid);

        return study;
    }

    /**
     * @param file
     * @return
     * @throws IOException
     */
    public static String readFileToString(MultipartFile file) throws IOException {
        StringBuilder sb = new StringBuilder();
        try (Scanner sc = new Scanner(file.getInputStream())) {
            String currentLine;

            while (sc.hasNextLine()) {
                currentLine = sc.nextLine();
                sb.append(currentLine);
            }

        }

        return sb.toString();
    }


    public boolean verifyRole(String userName, String study_oid,
                              String site_oid, Errors e) {

        boolean hasRolePermission = true;
        // check for site role & user permission if ok -> return yes,
        //if no-> check for study permissions & role
        String studyOid = study_oid;
        String siteOid = site_oid;

        StudyUserRoleBean studyLevelRole = this.getUserAccountDAO().findTheRoleByUserNameAndStudyOid(userName, studyOid);
        if (studyLevelRole == null) {
            if (siteOid != null) {

                StudyUserRoleBean siteLevelRole = this.getUserAccountDAO().findTheRoleByUserNameAndStudyOid(userName, siteOid);
                if (siteLevelRole == null) {
                    e.reject(ErrorConstants.ERR_NO_ROLE_SETUP, "No role configured for user" + userName + " in Site " + siteOid + ".");
                    hasRolePermission = false;
                } else if (siteLevelRole.getId() == 0 || siteLevelRole.getRole().equals(Role.MONITOR)) {
                    e.reject(ErrorConstants.ERR_NO_SUFFICIENT_PRIVILEGES, "User account does not have sufficient privileges to perform this data import.");
                    hasRolePermission = false;
                }

            } else {
                e.reject(ErrorConstants.ERR_NO_ROLE_SETUP, "No role configured for user " + userName + " in study " + studyOid);
                hasRolePermission = false;
            }

        } else {
            if (studyLevelRole.getId() == 0 || studyLevelRole.getRole().equals(Role.MONITOR)) {
                e.reject(ErrorConstants.ERR_NO_SUFFICIENT_PRIVILEGES, "User account does not have sufficient privileges to perform this data import.");
                hasRolePermission = false;
            }
        }


        return hasRolePermission;

    }

    /**
     * @param userName
     * @param study_oid
     * @param site_oid
     * @return
     */
    public String verifyRole(String userName, String study_oid,
                             String site_oid) {

        String studyOid = study_oid;
        String siteOid = site_oid;
        String err_msg = null;

        StudyUserRoleBean studyLevelRole = this.getUserAccountDAO().findTheRoleByUserNameAndStudyOid(userName, studyOid);
        if (studyLevelRole == null) {
            if (siteOid != null) {

                StudyUserRoleBean siteLevelRole = this.getUserAccountDAO().findTheRoleByUserNameAndStudyOid(userName, siteOid);
                if (siteLevelRole == null) {
                    err_msg = ErrorConstants.ERR_NO_ROLE_SETUP + " You do not have any role set up for user " + userName + " in study site " + siteOid;
                } else if (siteLevelRole.getId() == 0 || siteLevelRole.getRole().equals(Role.MONITOR)) {
                    err_msg = ErrorConstants.ERR_NO_SUFFICIENT_PRIVILEGES + " You do not have sufficient privileges to proceed with this operation.";
                }

            } else {
                err_msg = ErrorConstants.ERR_NO_ROLE_SETUP + " You do not have any role set up for user " + userName + " in study " + studyOid;

            }

        } else {
            if (studyLevelRole.getId() == 0 || studyLevelRole.getRole().equals(Role.MONITOR)) {
                err_msg = ErrorConstants.ERR_NO_SUFFICIENT_PRIVILEGES + " You do not have sufficient privileges to proceed with this operation.";

            }
        }


        return err_msg;

    }

    /**
     * Helper Method to get the user account
     * @return UserAccountBean
     */
    public UserAccountBean getUserAccount(HttpServletRequest request) {
        UserAccountBean userBean;

        if (request.getSession() != null && request.getSession().getAttribute("userBean") != null) {
            userBean = (UserAccountBean) request.getSession().getAttribute("userBean");

        } else {
            Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            String username = null;
            if (principal instanceof UserDetails) {
                username = ((UserDetails) principal).getUsername();
            } else {
                username = principal.toString();
            }

            String schema = CoreResources.getRequestSchema();
            CoreResources.setRequestSchemaToPublic();
            UserAccountDAO userAccountDAO = new UserAccountDAO(dataSource);
            userBean = (UserAccountBean) userAccountDAO.findByUserName(username);
            CoreResources.setRequestSchema(schema);

        }

        return userBean;

    }


    public File getXSDFile(HttpServletRequest request, String fileNm) {
        HttpSession session = request.getSession();
        ServletContext context = session.getServletContext();

        return new File(SpringServletAccess.getPropertiesDir(context) + fileNm);
    }

    public UserAccountDAO getUserAccountDAO() {
        userAccountDAO = userAccountDAO != null ? userAccountDAO : new UserAccountDAO(dataSource);
        return userAccountDAO;
    }

    /**
     * this will call OC Restful API directly:
     * ${remoteAddress}/OpenClinica/pages/auth/api/clinicaldata/
     * @param files
     * @param request
     * @return
     * @throws Exception
     */
    public ImportCRFInfoSummary sendOneDataRowPerRequestByHttpClient(List<File> files, HttpServletRequest request, HashMap hm) throws Exception {
        String remoteAddress = this.getBasePath(request);
        String importDataWSUrl = remoteAddress + "/OpenClinica/pages/auth/api/clinicaldata/";
        ImportCRFInfoSummary importCRFInfoSummary = new ImportCRFInfoSummary();
        ArrayList<File> tempODMFileList = new ArrayList<>();
        String studyOID = null;

        /**
         *  prepare mapping file
         */
        File mappingFile = null;
        String mappingpartNm = null;
        for (File file : files) {

            if (file.getName().toLowerCase().endsWith(".properties")) {
                mappingFile = file;
                mappingpartNm = "uploadedData";
                studyOID = this.getImportDataHelper().getStudyOidFromMappingFile(file);
                Study publicStudy = null;
                if (StringUtils.isEmpty(studyOID))
                    publicStudy = studyDao.findPublicStudy(studyOID);
                if (publicStudy != null)
                    CoreResources.setRequestSchema(publicStudy.getSchemaName());
                break;
            }

        }

        // prepare log file
        String logFileName = null;
        logFileName = (String) request.getAttribute("logFileName");

        int i = 1;
        for (File file : files) {
            // skip mapping file
            if (file.getName().toLowerCase().endsWith(".properties")) {
            } else {
                ArrayList<File> dataFileList = splitDataFileAndProcesDataRowbyRow(mappingFile, file, studyOID);
                setDataFileNameAndTimeStampForLog(request, file);

                Iterator dataFilesIt = dataFileList.iterator();
                File rowFile = null;
                while (dataFilesIt.hasNext()) {
                    try {
                        rowFile = (File) dataFilesIt.next();
                        //validate studySubject in datafile
                        String participantLabel = this.getImportDataHelper().getStudySubject(mappingFile, rowFile);
                        HttpPost post = new HttpPost(importDataWSUrl);
                        post.setHeader("participantLabel", participantLabel);
                        /**
                         *  add header Authorization
                         */
                        String accessToken = (String) request.getSession().getAttribute("accessToken");
                        post.setHeader("Authorization", "Bearer " + accessToken);

                        String basePath = getBasePath(request);
                        post.setHeader("OCBasePath", basePath);

                        //PIPETEXT
                        post.setHeader("PIPETEXT", "PIPETEXT");

                        //SkipMatchCriteria
                        String skipMatchCriteria = this.getImportDataHelper().getSkipMatchCriteria(rowFile, mappingFile);
                        post.setHeader("SkipMatchCriteria", skipMatchCriteria);

                        post.setHeader("Accept",
                                "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
                        post.setHeader("Accept-Language", "en-US,en;q=0.5");
                        post.setHeader("Connection", "keep-alive");

                        String originalFileName = rowFile.getName();
                        post.setHeader("originalFileName", originalFileName);
                        post.setHeader("logFileName", logFileName);

                        MultipartEntityBuilder builder = MultipartEntityBuilder.create();
                        builder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);
                        String partNm = null;
                        /**
                         *  Here will only send ODM XML to OC API
                         *
                         */
                        String dataStr = this.getImportDataHelper().transformTextToODMxml(mappingFile, rowFile, hm);
                        File odmXmlFile = this.getImportDataHelper().saveDataToFile(dataStr, originalFileName, studyOID);
                        tempODMFileList.add(odmXmlFile);

                        FileBody fileBody = new FileBody(odmXmlFile, ContentType.TEXT_PLAIN);
                        partNm = "uploadedData" + i;
                        builder.addPart(partNm, fileBody);
                        builder.addBinaryBody("file", odmXmlFile);


                        HttpEntity entity = builder.build();
                        post.setEntity(entity);

                        CloseableHttpClient httpClient = HttpClients.createDefault();
                        HttpResponse response = httpClient.execute(post);

                        //print result
                        int responseCode = response.getStatusLine().getStatusCode();

                        //System.out.println("\nSending 'POST' request to URL : " + importDataWSUrl);
                        //System.out.println("Response Code : " + responseCode);

                        BufferedReader rd = new BufferedReader(
                                new InputStreamReader(response.getEntity().getContent()));

                        StringBuffer result = new StringBuffer();
                        String line = "";
                        while ((line = rd.readLine()) != null) {
                            result.append(line);
                        }

                        String responseStr = result.toString();
                        if (responseStr != null && responseStr.toLowerCase().indexOf("error") > -1) {
                            importCRFInfoSummary.setFailCnt(importCRFInfoSummary.getFailCnt() + 1);
                        } else {
                            importCRFInfoSummary.setPassCnt(importCRFInfoSummary.getPassCnt() + 1);
                        }

                        importCRFInfoSummary.getDetailMessages().add(responseStr);
                        //System.out.println(responseStr);

                        //TimeUnit.MILLISECONDS.sleep(5);
                    } catch (OpenClinicaSystemException e) {
                        /*
                         *  write error to log file            	 *
                         */
                        String originalFileName = rowFile.getName();
                        String recordNum = null;
                        String participantID = "";
                        if (!e.getErrorCode().equals("errorCode.participantIdHeaderNotMatchingMappingFile"))
                            participantID = this.getImportDataHelper().getParticipantID(mappingFile, rowFile);
                        if (originalFileName != null) {
                            recordNum = originalFileName.substring(originalFileName.lastIndexOf("_") + 1, originalFileName.indexOf("."));
                            originalFileName = originalFileName.substring(0, originalFileName.lastIndexOf("_"));
                        }
                        String msg = recordNum + "," + participantID + ",FAILED," + e.getMessage();
                        this.getImportDataHelper().writeToMatchAndSkipLog(originalFileName, msg, request);

                    }

                }
                this.getImportDataHelper().addSummaryAndMappingFileInLog(logFileName, mappingFile, request, dataFileList.size());
                // after sent, then delete from disk
                dataFilesIt = dataFileList.iterator();
                while (dataFilesIt.hasNext()) {
                    rowFile = (File) dataFilesIt.next();
                    this.getImportDataHelper().deleteTempImportFile(rowFile, studyOID);

                }

                dataFilesIt = tempODMFileList.iterator();
                while (dataFilesIt.hasNext()) {
                    File tempODMFile = (File) dataFilesIt.next();
                    this.getImportDataHelper().deleteTempImportFile(tempODMFile, studyOID);

                }

            }

            i++;
        }
        // not save original data
        //this.getImportDataHelper().saveFileToImportFolder(files,studyOID);

        return importCRFInfoSummary;
    }

    public ImportCRFInfoSummary sendOneDataRowPerRequestByHttpClient(List<File> files, MockHttpServletRequest request, boolean ismock, HashMap hm) throws Exception {

        String importDataWSUrl = (String) request.getAttribute("importDataWSUrl");
        String accessToken = (String) request.getAttribute("accessToken");
        String basePath = (String) request.getAttribute("basePath");
        UserAccountBean ub = (UserAccountBean) request.getSession().getAttribute(USER_BEAN_NAME);


        ImportCRFInfoSummary importCRFInfoSummary = new ImportCRFInfoSummary();
        ArrayList<File> tempODMFileList = new ArrayList<>();
        String studyOID = null;

        /**
         *  prepare mapping file
         */
        File mappingFile = null;
        String mappingpartNm = null;
        for (File file : files) {

            if (file.getName().toLowerCase().endsWith(".properties")) {
                mappingFile = file;
                mappingpartNm = "uploadedData";
                studyOID = this.getImportDataHelper().getStudyOidFromMappingFile(file);
                Study publicStudy = null;
                if (!StringUtils.isEmpty(studyOID))
                    publicStudy = studyDao.findPublicStudy(studyOID);
                if (publicStudy != null)
                    CoreResources.setRequestSchema(publicStudy.getSchemaName());
                break;
            }

        }

        int i = 1;
        for (File file : files) {
            // skip mapping file
            if (file.getName().toLowerCase().endsWith(".properties")) {
                ;
            } else {
                ArrayList<File> dataFileList = splitDataFileAndProcesDataRowbyRow(mappingFile, file, studyOID);
                setDataFileNameAndTimeStampForLog(request, file);
                // prepare log file
                String logFileName = null;
                logFileName = buildLogFile(file.getName(), request);
                request.setAttribute("logFileName", logFileName);

                Iterator dataFilesIt = dataFileList.iterator();
                File rowFile = null;
                String skipMatchCriteria = null;
                while (dataFilesIt.hasNext()) {
                    try {
                        rowFile = (File) dataFilesIt.next();
                        //validate studySubject in datafile
                        String participantLabel = this.getImportDataHelper().getStudySubject(mappingFile, rowFile);
                        HttpPost post = new HttpPost(importDataWSUrl);
                        post.setHeader("participantLabel", participantLabel);
                        /**
                         *  add header Authorization
                         */
                        post.setHeader("Authorization", "Bearer " + accessToken);
                        post.setHeader("OCBasePath", basePath);
                        //PIPETEXT
                        post.setHeader("PIPETEXT", "PIPETEXT");

                        //SkipMatchCriteria
                        if (skipMatchCriteria == null) {
                            skipMatchCriteria = this.getImportDataHelper().getSkipMatchCriteria(rowFile, mappingFile);
                        }
                        post.setHeader("SkipMatchCriteria", skipMatchCriteria);

                        post.setHeader("Accept",
                                "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
                        post.setHeader("Accept-Language", "en-US,en;q=0.5");
                        post.setHeader("Connection", "keep-alive");

                        String originalFileName = rowFile.getName();
                        post.setHeader("originalFileName", originalFileName);
                        post.setHeader("logFileName", logFileName);

                        MultipartEntityBuilder builder = MultipartEntityBuilder.create();
                        builder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);
                        String partNm = null;
                        /**
                         *  Here will only send ODM XML to OC API
                         *
                         */
                        String dataStr = this.getImportDataHelper().transformTextToODMxml(mappingFile, rowFile, hm);
                        File odmXmlFile = this.getImportDataHelper().saveDataToFile(dataStr, originalFileName, studyOID);
                        tempODMFileList.add(odmXmlFile);

                        FileBody fileBody = new FileBody(odmXmlFile, ContentType.TEXT_PLAIN);
                        partNm = "uploadedData" + i;
                        builder.addPart(partNm, fileBody);
                        builder.addBinaryBody("file", odmXmlFile);


                        HttpEntity entity = builder.build();
                        post.setEntity(entity);

                        CloseableHttpClient httpClient = HttpClients.createDefault();
                        HttpResponse response = httpClient.execute(post);

                        //print result
                        int responseCode = response.getStatusLine().getStatusCode();

                        //System.out.println("\nSending 'POST' request to URL : " + importDataWSUrl);
                        //System.out.println("Response Code : " + responseCode);

                        BufferedReader rd = new BufferedReader(
                                new InputStreamReader(response.getEntity().getContent()));

                        StringBuffer result = new StringBuffer();
                        String line = "";
                        while ((line = rd.readLine()) != null) {
                            result.append(line);
                        }

                        String responseStr = result.toString();
                        if (responseStr != null && responseStr.toLowerCase().indexOf("error") > -1) {
                            importCRFInfoSummary.setFailCnt(importCRFInfoSummary.getFailCnt() + 1);
                        } else {
                            importCRFInfoSummary.setPassCnt(importCRFInfoSummary.getPassCnt() + 1);
                        }

                        importCRFInfoSummary.getDetailMessages().add(responseStr);
                        //System.out.println(responseStr);

                        //TimeUnit.MILLISECONDS.sleep(1);
                    } catch (OpenClinicaSystemException e) {
                        String originalFileName = rowFile.getName();
                        String recordNum = null;
                        String participantID = "";
                        if (!e.getErrorCode().equals("errorCode.participantIdHeaderNotMatchingMappingFile"))
                            participantID = this.getImportDataHelper().getParticipantID(mappingFile, rowFile);
                        if (originalFileName != null) {
                            recordNum = originalFileName.substring(originalFileName.lastIndexOf("_") + 1, originalFileName.indexOf("."));
                            originalFileName = originalFileName.substring(0, originalFileName.lastIndexOf("_"));
                        }
                        String msg = recordNum + "," + participantID + ",FAILED," + e.getMessage();
                        this.getImportDataHelper().writeToMatchAndSkipLog(originalFileName, msg, request);

                    }

                }
                this.getImportDataHelper().addSummaryAndMappingFileInLog(logFileName, mappingFile, request, dataFileList.size());
                // after sent, then delete from disk
                dataFilesIt = dataFileList.iterator();
                while (dataFilesIt.hasNext()) {
                    rowFile = (File) dataFilesIt.next();
                    this.getImportDataHelper().deleteTempImportFile(rowFile, studyOID);

                }

                dataFilesIt = tempODMFileList.iterator();
                while (dataFilesIt.hasNext()) {
                    File tempODMFile = (File) dataFilesIt.next();
                    this.getImportDataHelper().deleteTempImportFile(tempODMFile, studyOID);

                }

            }

            i++;
        }
        // not save original data
        //this.getImportDataHelper().saveFileToImportFolder(files,studyOID);

        return importCRFInfoSummary;
    }

    private void setDataFileNameAndTimeStampForLog(HttpServletRequest request, File dataFile) {
        request.setAttribute("dataFileName", dataFile.getName());
        SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyy HH:mm:ss Z");
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        request.setAttribute("startTimestamp", sdf.format(new Date()));
    }

    public ArrayList<File> splitDataFileAndProcesDataRowbyRow(File mappingFile, File dataFile, String studyOID) throws IOException, OpenClinicaException {
        String importFileDir = this.getImportDataHelper().getImportFileDir(studyOID);
        String fileType = Files.getFileExtension(dataFile.getAbsolutePath());
        if (fileType.equals(SAS_FILE_EXTENSION)) {
            // convert sas to pipe-delimited
            dataFile = sasFileConverterService.convert(dataFile);
        } else if (fileType.equals(XLSX_FILE_EXTENSION)) {
            // convert xlsx to pipe-delimited
            dataFile = excelFileConverterService.convert(dataFile);
        } else if (fileType.equals(CSV_FILE_EXTENSION)) {
            // convert csv to pipe-delimited
            dataFile = csvFileConverterService.convert(dataFile);
        } else if (fileType.equals(TXT_FILE_EXTENSION)) {
            Properties mappingProperties = readMappingProperties(mappingFile);
            String delimiter = mappingProperties.getProperty(PipeDelimitedDataHelper.DELIMITER_PROPERTY);
            if (delimiter != null) {
                if (delimiter.length() != 1) {
                    throw new OpenClinicaException("Invalid delimiter character", ErrorConstants.INVALID_DELIMITER);
                }
                dataFile = csvFileConverterService.convert(dataFile, delimiter.charAt(0));
            }
        }
        ArrayList<File> fileList = new ArrayList<>();
        BufferedReader reader;

        try {
            int count = 1;

            File splitFile;

            reader = new BufferedReader(new FileReader(dataFile));

            String orginalFileName = dataFile.getName();
            int pos = orginalFileName.indexOf(".");
            if (pos > 0) {
                orginalFileName = orginalFileName.substring(0, pos);
            }


            String columnLine = reader.readLine();
            String line = columnLine;

            BufferedWriter bw = null;
            FileOutputStream fos = null;

            while (line != null) {

                // read next line
                line = reader.readLine();

                if (line != null) {
                    splitFile = new File(importFileDir + orginalFileName + "_" + count + ".txt");
                    fos = new FileOutputStream(splitFile);
                    bw = new BufferedWriter(new OutputStreamWriter(fos));

                    bw.write(columnLine);
                    bw.write("\r");

                    bw.write(line);
                    fileList.add(splitFile);
                }

                if (bw != null) {
                    bw.close();
                }


                count++;

            }
            reader.close();


        } catch (Exception e) {
            log.error("Error while accessing the process the data: ", e);
        }

        return fileList;
    }

    public static String getBasePath(HttpServletRequest request) {
        StringBuffer basePath = new StringBuffer();
        String scheme = request.getScheme();
        String domain = request.getServerName();
        int port = request.getServerPort();
        basePath.append(scheme);
        basePath.append("://");
        basePath.append(domain);
        if ("http".equalsIgnoreCase(scheme) && 80 != port) {
            basePath.append(":").append(String.valueOf(port));
        } else if ("https".equalsIgnoreCase(scheme) && port != 443) {
            basePath.append(":").append(String.valueOf(port));
        }
        return basePath.toString();
    }


    public PipeDelimitedDataHelper getImportDataHelper() {
        if (importDataHelper == null) {
            importDataHelper = new PipeDelimitedDataHelper(this.dataSource, studyBuildService, studyDao);
        }
        return importDataHelper;
    }

    public void setImportDataHelper(PipeDelimitedDataHelper importDataHelper) {
        this.importDataHelper = importDataHelper;
    }

    /**
     * @param dateTimeStr: yyyy-MM-dd
     * @return
     */
    public Date getDateTime(String dateTimeStr) throws OpenClinicaException {
        String dataFormat = "yyyy-MM-dd";
        Date result = null;
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern(dataFormat);
            LocalDate parsedDate = LocalDate.parse(dateTimeStr, formatter);

            result = Date.from(parsedDate.atStartOfDay(ZoneId.systemDefault()).toInstant());

        } catch (DateTimeParseException e) {
            String errMsg = "The input date(" + dateTimeStr + ") can't be parsed, please use the correct format " + dataFormat;
            throw new OpenClinicaException(errMsg, ErrorConstants.ERR_PARSE_DATE);
        }

        return result;
    }


    public MessageLogger getMessageLogger() {

        if (messageLogger == null) {
            messageLogger = new MessageLogger(this.dataSource);
        }

        return messageLogger;
    }


    public void setMessageLogger(MessageLogger messageLogger) {
        this.messageLogger = messageLogger;
    }

    /**
     * @param originalFileName
     * @return logFileName
     */
    public String buildLogFile(String originalFileName, HttpServletRequest request) {
        String logFileName = null;
        if (originalFileName != null) {
            originalFileName = Files.getNameWithoutExtension(originalFileName);
        }

        Date now = new Date();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd-hhmmssSSSZ");
        String timeStamp = simpleDateFormat.format(now);
        logFileName = originalFileName + "_" + timeStamp + "_log.csv";
        try {
            String importFileDir = this.getImportDataHelper().getPersonalImportFileDir(request);
            String logFileloc = importFileDir + logFileName;
            File logFile = new File(logFileloc);
            if (!logFile.exists()) {
                logFile.createNewFile();
            }
        } catch (IOException e) {
            log.error("Log file is not able to be created from pipe-delimited");
        }
        return logFileName;
    }

    private Properties readMappingProperties(File mappingFile) throws IOException {
        Properties mappingProperties = new Properties();
        mappingProperties.load(new FileReader(mappingFile));
        return mappingProperties;
    }
}
