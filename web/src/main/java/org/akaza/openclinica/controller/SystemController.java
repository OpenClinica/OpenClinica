package org.akaza.openclinica.controller;

import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.FileOwnerAttributeView;
import java.nio.file.attribute.UserPrincipal;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.ResourceBundle;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import javax.naming.Context;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.akaza.openclinica.bean.extract.ExtractPropertyBean;
import org.akaza.openclinica.bean.login.UserAccountBean;
import org.akaza.openclinica.bean.managestudy.StudyBean;
import org.akaza.openclinica.bean.service.StudyParameterValueBean;
import org.akaza.openclinica.core.EmailEngine;
import org.akaza.openclinica.dao.core.CoreResources;
import org.akaza.openclinica.dao.login.UserAccountDAO;
import org.akaza.openclinica.dao.managestudy.StudyDAO;
import org.akaza.openclinica.dao.service.StudyParameterValueDAO;
import org.akaza.openclinica.exception.OpenClinicaSystemException;
import org.akaza.openclinica.i18n.util.ResourceBundleProvider;
import org.akaza.openclinica.service.pmanage.Authorization;
import org.akaza.openclinica.service.pmanage.ParticipantPortalRegistrar;
import org.akaza.openclinica.service.pmanage.RandomizationRegistrar;
import org.akaza.openclinica.service.pmanage.SeRandomizationDTO;
import org.apache.commons.dbcp.BasicDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.ldap.core.ContextSource;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.security.ldap.SpringSecurityLdapTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseStatus;

@Controller
@RequestMapping(value = "/auth/api/v1/system")
@ResponseStatus(value = org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR)
public class SystemController {

    // Add in Spring Cor files /healthcheck path to avoid firewall
    @Autowired
    @Qualifier("dataSource")
    private BasicDataSource dataSource;
    @Autowired
    private JavaMailSenderImpl mailSender;

    @Autowired
    private ContextSource contextSource;

    private SpringSecurityLdapTemplate ldapTemplate;

    protected final Logger logger = LoggerFactory.getLogger(getClass().getName());
    private HttpSession session;

    @RequestMapping(value = "/systemstatus", method = RequestMethod.POST)
    public ResponseEntity<HashMap> getSystemStatus() throws Exception {
        ResourceBundleProvider.updateLocale(new Locale("en_US"));
        HashMap<String, String> map = new HashMap<>();

        map.put("OpenClinica Version", CoreResources.getField("OpenClinica.version"));
        map.put("Java Version", System.getProperty("java.version"));
        map.put("Java Class Path", System.getProperty("java.class.path"));
        map.put("Java Home", System.getProperty("java.home"));
        map.put("OS Name", System.getProperty("os.name"));
        map.put("OS Version", System.getProperty("os.version"));
        map.put("OS Architecture", System.getProperty("os.arch"));
        map.put("File Separator", System.getProperty("file.separator"));
        map.put("Path Separator", System.getProperty("path.separator"));
        map.put("Line Separator", System.getProperty("line.separator"));
        map.put("User Home", System.getProperty("user.home"));
        map.put("User Directory", System.getProperty("user.dir"));
        map.put("User Name", System.getProperty("user.name"));

        Map<String, String> env = System.getenv();
        for (String envName : env.keySet()) {
            System.out.format("%s=%s%n", envName, env.get(envName));
        }

        DatabaseMetaData metaData = dataSource.getConnection().getMetaData();
        try {
            UserAccountDAO udao = new UserAccountDAO(dataSource);
            UserAccountBean uBean = (UserAccountBean) udao.findByPK(1);

            if (uBean.getFirstName().equals("Root") && uBean.getLastName().equals("User")) {
                map.put("Root User Account First And Last Name", uBean.getFirstName() + " " + uBean.getLastName());
                map.put("Database Connection", "PASS");
            } else {
                map.put("Database Connection", "FAIL");
            }
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return new ResponseEntity<HashMap>(map, org.springframework.http.HttpStatus.OK);

    }

    /**
     * @api {get} /pages/auth/api/v1/system/config Retrieve System Configuration
     * @apiName getConfiguration
     * @apiPermission Authenticate using api-key. admin
     * @apiVersion 3.8.0
     * @apiGroup System
     * @apiDescription Retrieves System Configuration Settings
     * @apiSuccessExample {json} Success-Response: HTTP/1.1 200 OK
     *                    {
     *                    "OC.Version": {
     *                    "OC Version number": "3.12",
     *                    "OC Edition": "OpenClinica Community",
     *                    "OC Version Changeset": "${changeSet} ${changeSetDate}"
     *                    },
     *                    "datainfo.properties": {
     *                    "usage statistics configuration": {
     *                    "usage.stats.host": "usage.openclinica.com",
     *                    "usage.stats.port": "514",
     *                    "OpenClinica.version": "MAINLINE-SNAPSHOT",
     *                    "collectStats": "true"
     *                    },
     *                    "extract.number": "99",
     *                    "database configuration": {
     *                    "db": "openclinica",
     *                    "dbType": "postgres"
     *                    },
     *                    ....
     *                    }
     *                    }
     */

    @RequestMapping(value = "/config", method = RequestMethod.GET)
    public ResponseEntity<HashMap> getConfig() throws Exception {
        ResourceBundleProvider.updateLocale(new Locale("en_US"));
        HashMap<String, Object> map = new HashMap<>();
        HashMap<String, Object> ocVersion = new HashMap<>();

        ResourceBundle resLicense = ResourceBundleProvider.getLicensingBundle();
        String versionRelease = resLicense.getString("Version_release");

        ocVersion.put("OC Edition", resLicense.getString("footer.edition.2"));
        ocVersion.put("OC Version number", versionRelease.substring(versionRelease.indexOf("Version") + 9, versionRelease.lastIndexOf('-') - 1));
        ocVersion.put("OC Version Changeset", versionRelease.substring(versionRelease.indexOf("- Changeset") + 13));

        map.put("OC.Version", ocVersion);

        HashMap<String, Object> datainfo = new HashMap<>();

        HashMap<String, String> databaseConfiguration = new HashMap<>();
        databaseConfiguration.put("dbType", CoreResources.getField("dbType"));
        databaseConfiguration.put("db", CoreResources.getField("db"));

        HashMap<String, String> emailSystem = new HashMap<>();
        emailSystem.put("mailHost", CoreResources.getField("mailHost"));
        emailSystem.put("mailPort", CoreResources.getField("mailPort"));
        emailSystem.put("mailProtocol", CoreResources.getField("mailProtocol"));
        emailSystem.put("mailSmtpAuth", CoreResources.getField("mailSmtpAuth"));
        emailSystem.put("mailSmtpStarttls.enable", CoreResources.getField("mailSmtpStarttls.enable"));
        emailSystem.put("mailSmtpsAuth", CoreResources.getField("mailSmtpsAuth"));
        emailSystem.put("mailSmtpsStarttls.enable", CoreResources.getField("mailSmtpsStarttls.enable"));
        emailSystem.put("mailSmtpConnectionTimeout", CoreResources.getField("mailSmtpConnectionTimeout"));
        emailSystem.put("mailErrorMsg", CoreResources.getField("mailErrorMsg"));

        HashMap<String, String> loggingConfiguration = new HashMap<>();
        loggingConfiguration.put("log.dir", CoreResources.getField("log.dir"));
        loggingConfiguration.put("logLocation", CoreResources.getField("logLocation"));
        loggingConfiguration.put("logLevel", CoreResources.getField("logLevel"));
        loggingConfiguration.put("syslog.host", CoreResources.getField("syslog.host"));
        loggingConfiguration.put("syslog.port", CoreResources.getField("syslog.port"));

        HashMap<String, String> news = new HashMap<>();
        news.put("rssUrl", CoreResources.getField("rssUrl"));
        news.put("rssMore", CoreResources.getField("rssMore"));
        news.put("about.text1", CoreResources.getField("about.text1"));
        news.put("about.text2", CoreResources.getField("about.text2"));

        HashMap<String, String> crfFileUploadConfiguration = new HashMap<>();
        crfFileUploadConfiguration.put("crfFileExtensions", CoreResources.getField("crfFileExtensions"));
        crfFileUploadConfiguration.put("crfFileExtensionSettings", CoreResources.getField("crfFileExtensionSettings"));

        HashMap<String, String> quartzSchedulerConfiguration = new HashMap<>();
        quartzSchedulerConfiguration.put("org.quartz.jobStore.misfireThreshold", CoreResources.getField("org.quartz.jobStore.misfireThreshold"));
        quartzSchedulerConfiguration.put("org.quartz.threadPool.threadCount", CoreResources.getField("org.quartz.threadPool.threadCount"));
        quartzSchedulerConfiguration.put("org.quartz.threadPool.threadPriority", CoreResources.getField("org.quartz.threadPool.threadPriority"));

        HashMap<String, String> facilityInformation = new HashMap<>();
        facilityInformation.put("FacName", CoreResources.getField("FacName"));
        facilityInformation.put("FacCity", CoreResources.getField("FacCity"));
        facilityInformation.put("FacState", CoreResources.getField("FacState"));
        facilityInformation.put("FacZIP", CoreResources.getField("FacZIP"));
        facilityInformation.put("FacCountry", CoreResources.getField("FacCountry"));
        facilityInformation.put("FacContactName", CoreResources.getField("FacContactName"));
        facilityInformation.put("FacContactDegree", CoreResources.getField("FacContactDegree"));
        facilityInformation.put("FacContactPhone", CoreResources.getField("FacContactPhone"));
        facilityInformation.put("FacContactEmail", CoreResources.getField("FacContactEmail"));

        HashMap<String, String> usageStatisticsConfiguration = new HashMap<>();
        usageStatisticsConfiguration.put("collectStats", CoreResources.getField("collectStats"));
        usageStatisticsConfiguration.put("usage.stats.host", CoreResources.getField("usage.stats.host"));
        usageStatisticsConfiguration.put("usage.stats.port", CoreResources.getField("usage.stats.port"));
        usageStatisticsConfiguration.put("OpenClinica.version", CoreResources.getField("OpenClinica.version"));

        HashMap<String, String> ldapConfiguration = new HashMap<>();
        ldapConfiguration.put("ldap.enabled", CoreResources.getField("ldap.enabled"));
        ldapConfiguration.put("ldap.host", CoreResources.getField("ldap.host"));
        ldapConfiguration.put("ldap.loginQuery", CoreResources.getField("ldap.loginQuery"));
        ldapConfiguration.put("ldap.passwordRecoveryURL", CoreResources.getField("ldap.passwordRecoveryURL"));
        ldapConfiguration.put("ldap.userSearch.baseDn", CoreResources.getField("ldap.userSearch.baseDn"));
        ldapConfiguration.put("ldap.userSearch.query", CoreResources.getField("ldap.userSearch.query"));
        ldapConfiguration.put("ldap.userData.distinguishedName", CoreResources.getField("ldap.userData.distinguishedName"));
        ldapConfiguration.put("ldap.userData.username", CoreResources.getField("ldap.userData.username"));
        ldapConfiguration.put("ldap.userData.firstName", CoreResources.getField("ldap.userData.firstName"));
        ldapConfiguration.put("ldap.userData.lastName", CoreResources.getField("ldap.userData.lastName"));
        ldapConfiguration.put("ldap.userData.email", CoreResources.getField("ldap.userData.email"));
        ldapConfiguration.put("ldap.userData.organization", CoreResources.getField("ldap.userData.organization"));

        datainfo.put("database configuration", databaseConfiguration);
        datainfo.put("filePath", CoreResources.getField("filePath"));
        datainfo.put("attachedFileLocation", CoreResources.getField("attachedFileLocation"));
        datainfo.put("userAccountNotification", CoreResources.getField("userAccountNotification"));
        datainfo.put("adminEmail", CoreResources.getField("adminEmail"));
        datainfo.put("mail protocol", emailSystem);
        datainfo.put("sysURL", CoreResources.getField("sysURL"));
        datainfo.put("maxInactiveInterval", CoreResources.getField("maxInactiveInterval"));
        datainfo.put("logging configuration", loggingConfiguration);
        datainfo.put("news", news);
        datainfo.put("crf file upload configuration", crfFileUploadConfiguration);
        datainfo.put("supportURL", CoreResources.getField("supportURL"));
        datainfo.put("quartz scheduler configuration", quartzSchedulerConfiguration);
        datainfo.put("ccts.waitBeforeCommit", CoreResources.getField("ccts.waitBeforeCommit"));
        datainfo.put("facility information", facilityInformation);
        datainfo.put("exportFilePath", CoreResources.getField("exportFilePath"));
        datainfo.put("extract.number", CoreResources.getField("extract.number"));
        datainfo.put("usage statistics configuration", usageStatisticsConfiguration);
        datainfo.put("designerURL", CoreResources.getField("designerURL"));
        datainfo.put("ldap Configuration", ldapConfiguration);
        datainfo.put("portalURL", CoreResources.getField("portalURL"));
        datainfo.put("moduleManager", CoreResources.getField("moduleManager"));
        datainfo.put("xform.enabled", CoreResources.getField("xform.enabled"));

        map.put("datainfo.properties", datainfo);

        return new ResponseEntity<HashMap>(map, org.springframework.http.HttpStatus.OK);

    }

    /**
     * @api {get} /pages/auth/api/v1/system/extract Retrieve Extract Properties
     * @apiName getExtractProperties
     * @apiPermission Authenticate using api-key. admin
     * @apiVersion 3.8.0
     * @apiGroup System
     * @apiDescription Retrieves Extract Properties
     * @apiSuccessExample {json} Success-Response: HTTP/1.1 200 OK
     *                    {
     *                    "extract.properties": {
     *                    "extract.number": {
     *                    "extract.number": "99"
     *                    },
     *                    "extract.1": {
     *                    "zip": "true",
     *                    "failure": "",
     *                    "fileDescription": "CDISC ODM XML 1.3 Full with OpenClinica extensions",
     *                    "linkText": "Run Now",
     *                    "deleteOld": "true",
     *                    "location": "$exportFilePath/$datasetName/ODM_1.3_Full",
     *                    "file": "[copyXML.xsl]",
     *                    "helpText": "CDISC ODM XML 1.3 Full with OpenClinica extensions. Includes discrepancy notes
     *                    and audit trails.",
     *                    "exportname": "[odm1.3_full$datasetName_$dateTime.xml]",
     *                    "success": "The extract completed successfully. The file is available for download $linkURL.",
     *                    "odmType": "full"
     *                    },
     *                    "extract.2": {
     *                    "zip": "true",
     *                    "failure": "",
     *                    "fileDescription": "CDISC ODM XML 1.3 Clinical Data with OpenClinica extensions",
     *                    "linkText": "Run Now",
     *                    "deleteOld": "true",
     *                    "location": "$exportFilePath/$datasetName/ODM_1.3_Extensions",
     *                    "file": "[copyXML.xsl]",
     *                    "helpText": "CDISC ODM XML 1.3 Clinical Data with OpenClinica extensions. Does not include
     *                    discrepancy notes or audit trails.",
     *                    "exportname": "[odm1.3_clinical_ext_$datasetName_$dateTime.xml]",
     *                    "success": "Your extract job completed successfully. The file is available for download
     *                    $linkURL.",
     *                    "odmType": "clinical_data"
     *                    }
     *                    ...
     *                    }
     *                    }
     */
    @RequestMapping(value = "/extract", method = RequestMethod.GET)
    public ResponseEntity<HashMap> getExtractModule() throws Exception {
        ResourceBundleProvider.updateLocale(new Locale("en_US"));
        HashMap<String, Object> map = new HashMap<>();

        ResourceBundle resLicense = ResourceBundleProvider.getLicensingBundle();

        HashMap<String, Object> extractMap = new HashMap<>();
        ArrayList<ExtractPropertyBean> extracts = CoreResources.getExtractProperties();
        int n = 0;
        for (ExtractPropertyBean extract : extracts) {
            n++;
            HashMap<String, String> extractmap = new HashMap<>();
            extractmap.put("odmType", extract.getOdmType());
            extractmap.put("file", Arrays.toString(extract.getFileName()));
            extractmap.put("fileDescription", extract.getFiledescription());
            extractmap.put("linkText", extract.getLinkText());
            extractmap.put("helpText", extract.getHelpText());
            extractmap.put("location", extract.getFileLocation());
            extractmap.put("exportname", Arrays.toString(extract.getExportFileName()));
            extractmap.put("zip", String.valueOf(extract.getZipFormat()));
            extractmap.put("deleteOld", String.valueOf(extract.getDeleteOld()));
            extractmap.put("success", extract.getSuccessMessage());
            extractmap.put("failure", extract.getFailureMessage());

            extractMap.put("extract." + n, extractmap);
        }

        HashMap<String, String> extractDatamart = new HashMap<>();
        HashMap<String, String> datamartRole = new HashMap<>();
        String username = CoreResources.getExtractField("db1.username");
        String password = CoreResources.getExtractField("db1.password");
        String url = CoreResources.getExtractField("db1.url");

        extractDatamart.put("db1.username", username);
        extractDatamart.put("db1.url", url);
        extractDatamart.put("db1.dataBase", CoreResources.getExtractField("db1.dataBase"));

        HashMap<String, String> extractNumber = new HashMap<>();
        extractNumber.put("extract.number", CoreResources.getExtractField("extract.number"));

        extractMap.put("extract.number", extractNumber);
        // extractMap.put("DataMart", extractDatamart);

        HashMap<String, String> datamartMap = new HashMap();

        try (Connection conn = DriverManager.getConnection(url, username, password)) {
            datamartRole = getDbRoleProperties(conn, datamartRole, username);
            datamartMap.put("connection", "Open");
        } catch (Exception e) {
            datamartMap.put("connection", "Close");
        }
        // map.put("Datamart Facts", datamartMap);
        map.put("extract.properties", extractMap);
        // map.put("Role Properties", datamartRole);

        return new ResponseEntity<HashMap>(map, org.springframework.http.HttpStatus.OK);

    }

    /**
     * @api {get} /pages/auth/api/v1/system/modules Retrieve All Modules Info
     * @apiName getModules
     * @apiPermission Authenticate using api-key. admin
     * @apiVersion 3.8.0
     * @apiGroup System
     * @apiDescription Retrieves All Modules Status Info Per Study
     * @apiSuccessExample {json} Success-Response: HTTP/1.1 200 OK
     *                    [{
     *                    "Modules": [{
     *                    "Participate": {
     *                    "enabled": "False",
     *                    "status": "INACTIVE",
     *                    "metadata": {}
     *                    }
     *                    }, {
     *                    "Randomize": {
     *                    "enabled": "False",
     *                    "status": "INACTIVE",
     *                    "metadata": {}
     *                    }
     *                    }, {
     *                    "Rule Designer": {
     *                    "enabled": "True",
     *                    "status": "INACTIVE",
     *                    "metadata": {
     *                    "Designer URL": "designer Url"
     *                    }
     *                    }
     *                    }, {
     *                    "Messaging": {
     *                    "enabled": "True",
     *                    "status": "ACTIVE",
     *                    "metadata": {
     *                    "mail.host": "your host",
     *                    "mail.protocol": "smtp"
     *                    }
     *                    }
     *                    }, {
     *                    "Datamart": {
     *                    "enabled": "False",
     *                    "status": "ACTIVE",
     *                    "metadata": {
     *                    "Role Properties": {
     *                    "Login": "True",
     *                    "CreateRole": "False",
     *                    "RoleName": "datamart",
     *                    "Inherit": "True",
     *                    "CreateDb": "False",
     *                    "SuperUser": "False"
     *                    },
     *                    "db1.dataBase": "postgres",
     *                    "db1.url": "jdbc:postgresql url",
     *                    "db1.username": "datamart"
     *                    }
     *                    }
     *                    }, {
     *                    "Web Service": {
     *                    "enabled": "True",
     *                    "status": "INACTIVE",
     *                    "metadata": {
     *                    "WebService URL": "web service url",
     *                    "Http Status Code": "404"
     *                    }
     *                    }
     *                    }, {
     *                    "Ldap": {
     *                    "enabled": "True",
     *                    "status": "ACTIVE",
     *                    "metadata": {
     *                    "ldap.host": "ldap server url"
     *                    }
     *                    }
     *                    }],
     *                    "Study Oid": "S_BL101"
     *                    }]
     */

    @RequestMapping(value = "/modules", method = RequestMethod.GET)
    public ResponseEntity<ArrayList<HashMap<String, Object>>> getAllModules(HttpServletRequest request) throws Exception {
        ResourceBundleProvider.updateLocale(new Locale("en_US"));
        HashMap<String, Object> map = new HashMap<>();
        ArrayList<HashMap<String, Object>> studyListMap = new ArrayList();

        HttpSession session = request.getSession();
        session.removeAttribute("ruledesigner");
        session.removeAttribute("messaging");
        session.removeAttribute("datamart");
        session.removeAttribute("webservice");
        session.removeAttribute("ldap");

        ArrayList<StudyBean> studyList = getStudyList();

        for (StudyBean studyBean : studyList) {
            ArrayList<HashMap<String, Object>> listOfModules = new ArrayList();
            HashMap<String, Object> mapParticipantModule = getParticipateModule(studyBean);
            listOfModules.add(mapParticipantModule);

            HashMap<String, Object> mapRandomizeModule = getRandomizeModule(studyBean);
            listOfModules.add(mapRandomizeModule);

            HashMap<String, Object> mapRuleDesignerModule = getRuleDesignerModuleInSession(studyBean, session);
            listOfModules.add(mapRuleDesignerModule);

            HashMap<String, Object> mapMessagingModule = getMessagingModuleInSession(studyBean, session);
            listOfModules.add(mapMessagingModule);

            HashMap<String, Object> mapDatamartModule = getDatamartModuleInSession(studyBean, session);
            listOfModules.add(mapDatamartModule);

            HashMap<String, Object> mapWebServiceModule = getWebServiceModuleInSession(studyBean, session);
            listOfModules.add(mapWebServiceModule);

            HashMap<String, Object> mapLdapModule = getLdapModuleInSession(studyBean, session);
            listOfModules.add(mapLdapModule);

            HashMap<String, Object> mapStudy = new HashMap<>();
            mapStudy.put("Modules", listOfModules);
            mapStudy.put("Study Oid", studyBean.getOid());

            studyListMap.add(mapStudy);
        }

        return new ResponseEntity<ArrayList<HashMap<String, Object>>>(studyListMap, org.springframework.http.HttpStatus.OK);
    }

    /**
     * @api {get} /pages/auth/api/v1/system/modules/participate Retrieve Participate Module Info
     * @apiName getPartcipateModule
     * @apiPermission Authenticate using api-key. admin
     * @apiVersion 3.8.0
     * @apiGroup System
     * @apiDescription Retrieves Participate Module Status Info Per Study
     * @apiSuccessExample {json} Success-Response: HTTP/1.1 200 OK
     *                    [{
     *                    "Study Oid": "S_BL101",
     *                    "Module": {
     *                    "Participate": {
     *                    "enabled": "False",
     *                    "status": "INACTIVE",
     *                    "metadata": {}
     *                    }
     *                    }
     *                    }]
     */

    @RequestMapping(value = "/modules/participate", method = RequestMethod.GET)
    public ResponseEntity<ArrayList<HashMap<String, Object>>> getParticipateModule() throws Exception {
        ResourceBundleProvider.updateLocale(new Locale("en_US"));
        ArrayList<HashMap<String, Object>> studyListMap = new ArrayList();

        ArrayList<StudyBean> studyList = getStudyList();

        for (StudyBean studyBean : studyList) {
            HashMap<String, Object> mapParticipantModule = getParticipateModule(studyBean);

            HashMap<String, Object> mapStudy = new HashMap<>();
            mapStudy.put("Module", mapParticipantModule);
            mapStudy.put("Study Oid", studyBean.getOid());
            studyListMap.add(mapStudy);
        }

        return new ResponseEntity<ArrayList<HashMap<String, Object>>>(studyListMap, org.springframework.http.HttpStatus.OK);
    }

    /**
     * @api {get} /pages/auth/api/v1/system/modules/randomize Retrieve Randomize Module Info
     * @apiName getRandomizeModule
     * @apiPermission Authenticate using api-key. admin
     * @apiVersion 3.8.0
     * @apiGroup System
     * @apiDescription Retrieves Randomize Module Status Info Per Study
     * @apiSuccessExample {json} Success-Response: HTTP/1.1 200 OK
     *                    [{
     *                    "Study Oid": "S_BL101",
     *                    "Module": {
     *                    "Randomize": {
     *                    "enabled": "False",
     *                    "status": "INACTIVE",
     *                    "metadata": {}
     *                    }
     *                    }
     *                    }]
     */

    @RequestMapping(value = "/modules/randomize", method = RequestMethod.GET)
    public ResponseEntity<ArrayList<HashMap<String, Object>>> getRandomizeModule() throws Exception {
        ResourceBundleProvider.updateLocale(new Locale("en_US"));

        ArrayList<HashMap<String, Object>> studyListMap = new ArrayList();

        ArrayList<StudyBean> studyList = getStudyList();

        for (StudyBean studyBean : studyList) {
            HashMap<String, Object> mapParticipantModule = getRandomizeModule(studyBean);

            HashMap<String, Object> mapStudy = new HashMap<>();
            mapStudy.put("Module", mapParticipantModule);
            mapStudy.put("Study Oid", studyBean.getOid());
            studyListMap.add(mapStudy);
        }

        return new ResponseEntity<ArrayList<HashMap<String, Object>>>(studyListMap, org.springframework.http.HttpStatus.OK);
    }

    /**
     * @api {get} /pages/auth/api/v1/system/modules/webservices Retrieve Web Services Module Info
     * @apiName getWebServicesModule
     * @apiPermission Authenticate using api-key. admin
     * @apiVersion 3.8.0
     * @apiGroup System
     * @apiDescription Retrieves Web Services Module Status Info Per Study
     * @apiSuccessExample {json} Success-Response: HTTP/1.1 200 OK
     *                    [{
     *                    "Study Oid": "S_BL101",
     *                    "Module": {
     *                    "Web Service": {
     *                    "enabled": "True",
     *                    "status": "INACTIVE",
     *                    "metadata": {
     *                    "WebService URL": "web service url",
     *                    "Http Status Code": "404"
     *                    }
     *                    }
     *                    }
     *                    }]
     */

    @RequestMapping(value = "/modules/webservices", method = RequestMethod.GET)
    public ResponseEntity<ArrayList<HashMap<String, Object>>> getWebServicesModule(HttpServletRequest request) throws Exception {
        ResourceBundleProvider.updateLocale(new Locale("en_US"));

        ArrayList<HashMap<String, Object>> studyListMap = new ArrayList();
        HttpSession session = request.getSession();
        session.removeAttribute("webservice");

        ArrayList<StudyBean> studyList = getStudyList();
        for (StudyBean studyBean : studyList) {
            HashMap<String, Object> mapRuleDesignerModule = getWebServiceModuleInSession(studyBean, session);

            HashMap<String, Object> mapStudy = new HashMap<>();
            mapStudy.put("Module", mapRuleDesignerModule);
            mapStudy.put("Study Oid", studyBean.getOid());
            studyListMap.add(mapStudy);
        }

        return new ResponseEntity<ArrayList<HashMap<String, Object>>>(studyListMap, org.springframework.http.HttpStatus.OK);

    }

    /**
     * @api {get} /pages/auth/api/v1/system/modules/ruledesigner Retrieve Rule Designer Module Info
     * @apiName getRuleDesignerModule
     * @apiPermission Authenticate using api-key. admin
     * @apiVersion 3.8.0
     * @apiGroup System
     * @apiDescription Retrieves Rule Designer Module Status Info Per Study
     * @apiSuccessExample {json} Success-Response: HTTP/1.1 200 OK
     *                    [{
     *                    "Study Oid": "S_BL101",
     *                    "Module": {
     *                    "Rule Designer": {
     *                    "enabled": "True",
     *                    "status": "INACTIVE",
     *                    "metadata": {
     *                    "Designer URL": "designer Url"
     *                    }
     *                    }
     *                    }
     *                    }]
     */

    @RequestMapping(value = "/modules/ruledesigner", method = RequestMethod.GET)
    public ResponseEntity<ArrayList<HashMap<String, Object>>> getRuleDesignerModule(HttpServletRequest request) throws Exception {
        ResourceBundleProvider.updateLocale(new Locale("en_US"));

        ArrayList<HashMap<String, Object>> studyListMap = new ArrayList();
        HttpSession session = request.getSession();
        session.removeAttribute("ruledesigner");

        ArrayList<StudyBean> studyList = getStudyList();
        for (StudyBean studyBean : studyList) {
            HashMap<String, Object> mapRuleDesignerModule = getRuleDesignerModuleInSession(studyBean, session);

            HashMap<String, Object> mapStudy = new HashMap<>();
            mapStudy.put("Module", mapRuleDesignerModule);
            mapStudy.put("Study Oid", studyBean.getOid());
            studyListMap.add(mapStudy);
        }

        return new ResponseEntity<ArrayList<HashMap<String, Object>>>(studyListMap, org.springframework.http.HttpStatus.OK);

    }

    /**
     * @api {get} /pages/auth/api/v1/system/modules/datamart Retrieve Datamart Module Info
     * @apiName getDatamartModule
     * @apiPermission Authenticate using api-key. admin
     * @apiVersion 3.8.0
     * @apiGroup System
     * @apiDescription Retrieves Datamart Module Status Info Per Study
     * @apiSuccessExample {json} Success-Response: HTTP/1.1 200 OK
     *                    [{
     *                    "Study Oid": "S_BL101",
     *                    "Module": {
     *                    "Datamart": {
     *                    "enabled": "False",
     *                    "status": "ACTIVE",
     *                    "metadata": {
     *                    "Role Properties": {
     *                    "Login": "True",
     *                    "CreateRole": "False",
     *                    "RoleName": "datamart",
     *                    "Inherit": "True",
     *                    "CreateDb": "False",
     *                    "SuperUser": "False"
     *                    },
     *                    "db1.dataBase": "postgres",
     *                    "db1.url": "jdbc:postgresql url",
     *                    "db1.username": "datamart"
     *                    }
     *                    }
     *                    }
     *                    }]
     */

    @RequestMapping(value = "/modules/datamart", method = RequestMethod.GET)
    public ResponseEntity<ArrayList<HashMap<String, Object>>> getDatamartModule(HttpServletRequest request) throws Exception {
        ResourceBundleProvider.updateLocale(new Locale("en_US"));

        HttpSession session = request.getSession();
        session.removeAttribute("datamart");

        ArrayList<HashMap<String, Object>> studyListMap = new ArrayList();

        ArrayList<StudyBean> studyList = getStudyList();
        for (StudyBean studyBean : studyList) {
            HashMap<String, Object> mapDatamartModule = getDatamartModuleInSession(studyBean, session);

            HashMap<String, Object> mapStudy = new HashMap<>();
            mapStudy.put("Module", mapDatamartModule);
            mapStudy.put("Study Oid", studyBean.getOid());
            studyListMap.add(mapStudy);
        }

        return new ResponseEntity<ArrayList<HashMap<String, Object>>>(studyListMap, org.springframework.http.HttpStatus.OK);

    }

    /**
     * @api {get} /pages/auth/api/v1/system/modules/auth Retrieve Ldap Module Info
     * @apiName getLdapModule
     * @apiPermission Authenticate using api-key. admin
     * @apiVersion 3.8.0
     * @apiGroup System
     * @apiDescription Retrieves Ldap Module Status Info Per Study
     * @apiSuccessExample {json} Success-Response: HTTP/1.1 200 OK
     *                    [{
     *                    "Study Oid": "S_BL101",
     *                    "Module": {
     *                    "Ldap": {
     *                    "enabled": "True",
     *                    "status": "ACTIVE",
     *                    "metadata": {
     *                    "ldap.host": "ldap server url"
     *                    }
     *                    }
     *                    }
     *                    }]
     */

    @RequestMapping(value = "/modules/auth", method = RequestMethod.GET)
    public ResponseEntity<ArrayList<HashMap<String, Object>>> getLdapModule(HttpServletRequest request) throws Exception {
        ResourceBundleProvider.updateLocale(new Locale("en_US"));

        ArrayList<HashMap<String, Object>> studyListMap = new ArrayList();
        HttpSession session = request.getSession();
        session.removeAttribute("ldap");

        ArrayList<StudyBean> studyList = getStudyList();
        for (StudyBean studyBean : studyList) {
            HashMap<String, Object> mapRuleDesignerModule = getLdapModuleInSession(studyBean, session);

            HashMap<String, Object> mapStudy = new HashMap<>();
            mapStudy.put("Module", mapRuleDesignerModule);
            mapStudy.put("Study Oid", studyBean.getOid());
            studyListMap.add(mapStudy);
        }

        return new ResponseEntity<ArrayList<HashMap<String, Object>>>(studyListMap, org.springframework.http.HttpStatus.OK);

    }

    /**
     * @api {get} /pages/auth/api/v1/system/modules/messaging Retrieve Email Module Info
     * @apiName getEmailModule
     * @apiPermission Authenticate using api-key. admin
     * @apiVersion 3.8.0
     * @apiGroup System
     * @apiDescription Retrieves Email Module Status Info Per Study
     * @apiSuccessExample {json} Success-Response: HTTP/1.1 200 OK
     *                    [{
     *                    "Study Oid": "S_BL101",
     *                    "Module": {
     *                    "Messaging": {
     *                    "enabled": "True",
     *                    "status": "ACTIVE",
     *                    "metadata": {
     *                    "mail.host": "your host",
     *                    "mail.protocol": "smtp"
     *                    }
     *                    }
     *                    }
     *                    }
     *                    ]
     */

    @RequestMapping(value = "/modules/messaging", method = RequestMethod.GET)
    public ResponseEntity<ArrayList<HashMap<String, Object>>> getMessagingModule(HttpServletRequest request) throws Exception {
        ResourceBundleProvider.updateLocale(new Locale("en_US"));

        HttpSession session = request.getSession();
        session.removeAttribute("messaging");

        ArrayList<HashMap<String, Object>> studyListMap = new ArrayList();

        ArrayList<StudyBean> studyList = getStudyList();
        for (StudyBean studyBean : studyList) {
            HashMap<String, Object> mapMessagingModule = getMessagingModuleInSession(studyBean, session);
            HashMap<String, Object> mapStudy = new HashMap<>();
            mapStudy.put("Module", mapMessagingModule);
            mapStudy.put("Study Oid", studyBean.getOid());
            studyListMap.add(mapStudy);
        }

        return new ResponseEntity<ArrayList<HashMap<String, Object>>>(studyListMap, org.springframework.http.HttpStatus.OK);

    }

    /**
     * @api {get} /pages/auth/api/v1/system/filesystem Retrieve FileSystem Info
     * @apiName getFileSystem
     * @apiPermission Authenticate using api-key. admin
     * @apiVersion 3.8.0
     * @apiGroup System
     * @apiDescription Retrieves FileSystem Status Info
     * @apiSuccessExample {json} Success-Response: HTTP/1.1 200 OK
     *                    {
     *                    "Available Disk Space on Drive": "84206.766 MB ",
     *                    "openClinica.data Directory & File Count & Size": [{
     *                    "Read Access": "Yes",
     *                    "Files Count": "1025",
     *                    "Write Access": "Yes",
     *                    "Sub Folders": [{
     *                    "Read Access": "Yes",
     *                    "Files Count": "341",
     *                    "Write Access": "Yes",
     *                    "Sub Folders": [{
     *                    "Read Access": "Yes",
     *                    "Files Count": "24",
     *                    "Write Access": "Yes",
     *                    "Folder Name": "S_01_PERFO",
     *                    "size": "2.9657898 MB"
     *                    }, {
     *                    "Read Access": "Yes",
     *                    "Files Count": "288",
     *                    "Write Access": "Yes",
     *                    "Folder Name": "S_BL101",
     *                    "size": "307.2837 MB"
     *                    }, {
     *                    "Read Access": "Yes",
     *                    "Write Access": "Yes",
     *                    "Folder Name": "work"
     *                    }],
     *                    "Folder Name": "Tomcat7"
     *                    }]
     *                    }
     *                    }
     */

    @RequestMapping(value = "/filesystem", method = RequestMethod.GET)
    public ResponseEntity<HashMap> getFileSystem() throws Exception {
        ResourceBundleProvider.updateLocale(new Locale("en_US"));
        HashMap<String, Object> map = new HashMap<>();

        String filePath = CoreResources.getField("filePath");
        File file = new File(filePath);

        filePath = filePath.substring(0, filePath.indexOf(".data"));

        String tomcatPath = filePath.substring(0, filePath.lastIndexOf("/"));
        File tomcatFile = new File(tomcatPath);
        float freeSpace = new File("/").getFreeSpace();

        map.put("Tomcat Directory Ownership", displayOwnerShipForTomcatDirectory(tomcatFile));
        map.put("Available Disk Space on Drive", freeSpace / 1024 / 1024 + " MB ");
        map.put("openClinica.data Directory & File Count & Size", displayOCDataDirectoryCountAndSize(file));
        // map.put("List Of Directory and File Names in OpenClinica.data Directory", displayDirectoryContents(file, new
        // ArrayList()));

        return new ResponseEntity<HashMap>(map, org.springframework.http.HttpStatus.OK);

    }

    /**
     * @api {get} /pages/auth/api/v1/system/database Retrieve Database HealthCheck Info
     * @apiName getDatabaseHealthCheck
     * @apiPermission Authenticate using api-key. admin
     * @apiVersion 3.8.0
     * @apiGroup System
     * @apiDescription Retrieves Database HealthCheck Info
     * @apiSuccessExample {json} Success-Response: HTTP/1.1 200 OK
     *                    {
     *                    "Database Connection": "True",
     *                    "Role Properties": {
     *                    "Login": "True",
     *                    "CreateRole": "False",
     *                    "Inherit": "True",
     *                    "CreateDb": "False",
     *                    "SuperUser": "True"
     *                    },
     *                    "Version": "8.4.22"
     *                    }
     */

    @RequestMapping(value = "/database", method = RequestMethod.GET)
    public ResponseEntity<HashMap> getDatabaseHealthCheck() throws Exception {
        ResourceBundleProvider.updateLocale(new Locale("en_US"));
        HashMap<String, Object> map = new HashMap<>();
        HashMap<String, String> mapRole = new HashMap<>();

        String username = CoreResources.getField("dbUser");
        String password = CoreResources.getField("dbPass");
        String url = CoreResources.getField("url");

        try (Connection conn = DriverManager.getConnection(url, username, password)) {
            map.put("Database Connection", "True");
            map.put("Version", String.valueOf(conn.getMetaData().getDatabaseProductVersion()));

            mapRole = getDbRoleProperties(conn, mapRole, username, true);

        } catch (Exception e) {
            map.put("connection", "False");
        }

        map.put("Role Properties", mapRole);
        return new ResponseEntity<HashMap>(map, org.springframework.http.HttpStatus.OK);

    }

    public ArrayList<String> displayDirectoryContents(File dir, ArrayList<String> list) {
        try {
            File[] files = dir.listFiles();
            for (File file : files) {
                if (file.isDirectory()) {
                    list.add(file.getCanonicalPath());
                    list = displayDirectoryContents(file, list);
                } else {
                    list.add(file.getCanonicalPath());
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return list;
    }

    public ArrayList<HashMap<String, Object>> displayOCDataDirectoryCountAndSize(File file) throws IOException {
        ArrayList<HashMap<String, Object>> listOfHashMaps = new ArrayList<>();
        HashMap<String, Object> hashMap = null;
        if (file.isDirectory()) {
            hashMap = new HashMap<String, Object>();

            hashMap.put("Read Access", getReadAccess(file));
            hashMap.put("Write Access", getWriteAccess(file));
            int count = 0;
            int fileCount = getFilesCount(file, count);
            int dirCount = getNumberOfSubFolders(file.getCanonicalPath().toString());
            long length = 0;
            float sizeInByte = getFolderSize(file, length);

            hashMap.put("Folder Name", file.getName());
            hashMap.put("Files Count", String.valueOf(fileCount));
            hashMap.put("size", String.valueOf(sizeInByte / 1024 / 1024) + " MB");

            listOfHashMaps.add(hashMap);
            if (dirCount != 0) {
                hashMap.put("Number of SubFolders", String.valueOf(dirCount));
                hashMap.put("Sub Folders", displaySubDirectoryCountAndSize(file));
            }
        }
        return listOfHashMaps;
    }

    public ArrayList<HashMap<String, Object>> displaySubDirectoryCountAndSize(File dir) throws IOException {
        ArrayList<HashMap<String, Object>> listOfHashMaps = new ArrayList<>();
        HashMap<String, Object> hashMap = null;
        File[] files = dir.listFiles();
        for (File file : files) {
            if (file.isDirectory()) {
                hashMap = new HashMap<String, Object>();

                hashMap.put("Read Access", getReadAccess(file));
                hashMap.put("Write Access", getWriteAccess(file));

                int count = 0;
                int fileCount = getFilesCount(file, count);
                int dirCount = getNumberOfSubFolders(file.getCanonicalPath().toString());
                long length = 0;
                float sizeInByte = getFolderSize(file, length);

                hashMap.put("Folder Name", file.getName());
                hashMap.put("Files Count", String.valueOf(fileCount));
                hashMap.put("size", String.valueOf(sizeInByte / 1024 / 1024) + " MB");

                listOfHashMaps.add(hashMap);
                if (dirCount != 0) {
                    hashMap.put("Number of SubFolders", String.valueOf(dirCount));
                    hashMap.put("Sub Folders", displaySubDirectoryCountAndSize(file));
                }
            }
        }
        return listOfHashMaps;
    }

    public ArrayList<HashMap<String, Object>> displayOwnerShipForTomcatDirectory(File file) throws IOException {
        ArrayList<HashMap<String, Object>> listOfHashMaps = new ArrayList<>();
        HashMap<String, Object> hashMap = null;
        if (file.isDirectory()) {
            hashMap = new HashMap<String, Object>();
            hashMap.put("Read Access", getReadAccess(file));
            hashMap.put("Write Access", getWriteAccess(file));

            Path path = Paths.get(file.getCanonicalPath());
            FileOwnerAttributeView ownerAttributeView = Files.getFileAttributeView(path, FileOwnerAttributeView.class);
            UserPrincipal owner = ownerAttributeView.getOwner();

            // hashMap.put("ownership", owner.getName());
            hashMap.put("Folder Name", file.getName());
            listOfHashMaps.add(hashMap);
            int dirCount = getNumberOfSubFolders(file.getCanonicalPath().toString());
            if (dirCount != 0) {
                hashMap.put("Sub Folders", displayOwnerShipForTomcatSubDirectories(file));
            }
        }
        return listOfHashMaps;
    }

    public ArrayList<HashMap<String, Object>> displayOwnerShipForTomcatSubDirectories(File dir) throws IOException {
        ArrayList<HashMap<String, Object>> listOfHashMaps = new ArrayList<>();
        HashMap<String, Object> hashMap = null;
        File[] files = dir.listFiles();
        for (File file : files) {
            if (file.isDirectory()) {
                hashMap = new HashMap<String, Object>();
                hashMap.put("Read Access", getReadAccess(file));
                hashMap.put("Write Access", getWriteAccess(file));

                Path path = Paths.get(file.getCanonicalPath());
                FileOwnerAttributeView ownerAttributeView = Files.getFileAttributeView(path, FileOwnerAttributeView.class);
                UserPrincipal owner = ownerAttributeView.getOwner();

                // hashMap.put("ownership", owner.getName());
                hashMap.put("Folder Name", file.getName());
                listOfHashMaps.add(hashMap);
                int dirCount = getNumberOfSubFolders(file.getCanonicalPath().toString());
                if (dirCount != 0) {
                    // hashMap.put("Sub Folders", displayOwnerShipForTomcatSubDirectories(file));
                }
            }
        }
        return listOfHashMaps;
    }

    public int getNumberOfSubFolders(String filePath) {
        File file1 = new File(filePath);
        File[] listFiles = file1.listFiles();
        int dirCount = 0;
        if (listFiles != null) {
            for (File f : listFiles) {
                if (f.isDirectory()) {
                    dirCount++;
                }
            }
        }
        return dirCount;
    }

    public int getFilesCount(File file, int count) {
        File[] files = file.listFiles();
        // int count = 0;
        if (files != null) {
            for (File f : files) {
                if (f.isDirectory()) {
                    count = getFilesCount(f, count);
                } else {
                    count++;
                }
            }
        }
        return count;
    }

    private long getFolderSize(File folder, long length) {
        if (folder.isDirectory()) {
            File[] files = folder.listFiles();
            if (files != null) {
                int count = files.length;
                for (int i = 0; i < count; i++) {
                    if (files[i].isFile()) {
                        length += files[i].length();
                    } else {
                        length = getFolderSize(files[i], length);
                    }
                }
            }
        }
        return length;
    }

    public String getWriteAccess(File file) {

        if (file.canWrite()) {
            return "Yes";
        } else {
            return "No";
        }
    }

    public String getReadAccess(File file) {
        if (file.canRead()) {
            return "Yes";
        } else {
            return "No";
        }

    }

    public String sendEmail(JavaMailSenderImpl mailSender, String emailSubject, String message) throws OpenClinicaSystemException {

        logger.info("Sending email...");
        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage);
            helper.setFrom(EmailEngine.getAdminEmail());
            helper.setTo("oc123@openclinica.com");
            helper.setSubject(emailSubject);
            helper.setText(message);

            mailSender.send(mimeMessage);
            return "ACTIVE";
        } catch (MailException me) {
            return "INACTIVE";
        } catch (MessagingException me) {
            return "INACTIVE";
        }
    }

    public HashMap<String, String> getDbRoleProperties(Connection conn, HashMap<String, String> mapRole, String username, Boolean withoutRoleName)
            throws SQLException {
        mapRole = getDbRoleProperties(conn, mapRole, username);
        if (withoutRoleName)
            mapRole.remove("RoleName");
        return mapRole;
    }

    public HashMap<String, String> getDbRoleProperties(Connection conn, HashMap<String, String> mapRole, String username) throws SQLException {
        String query = "select * from pg_roles where rolname='" + username + "'";
        ResultSet resultSet = conn.prepareStatement(query).executeQuery();

        while (resultSet.next()) {
            mapRole.put("RoleName", resultSet.getString("rolname"));
            mapRole.put("SuperUser", resultSet.getString("rolsuper").equals("t") ? "True" : "False");
            mapRole.put("Inherit", resultSet.getString("rolinherit").equals("t") ? "True" : "False");
            mapRole.put("CreateRole", resultSet.getString("rolcreaterole").equals("t") ? "True" : "False");
            mapRole.put("CreateDb", resultSet.getString("rolcreatedb").equals("t") ? "True" : "False");
            mapRole.put("Login", resultSet.getString("rolcanlogin").equals("t") ? "True" : "False");
        }
        return mapRole;
    }

    public ArrayList<StudyBean> getStudyList() {
        StudyDAO sdao = new StudyDAO(dataSource);
        ArrayList<StudyBean> sBeans = (ArrayList<StudyBean>) sdao.findAllParents();
        return sBeans;
    }

    public StudyParameterValueBean getParticipateMod(StudyBean studyBean, String value) {
        StudyParameterValueDAO spvdao = new StudyParameterValueDAO(dataSource);
        StudyParameterValueBean pStatus = spvdao.findByHandleAndStudy(studyBean.getId(), value);
        return pStatus;
    }

    public void getRandomizeMod() {
        StudyParameterValueDAO spvdao = new StudyParameterValueDAO(dataSource);

    }

    public HashMap<String, Object> getParticipateModule(StudyBean studyBean) {
        String portalURL = CoreResources.getField("portalURL");
        StudyParameterValueBean spvBean = getParticipateMod(studyBean, "participantPortal");
        String ocParticipateStatus = "";
        if (spvBean.isActive()) {
            ocParticipateStatus = spvBean.getValue().toString(); // enabled , disabled
        }
        String ocuiParticipateStatus = "";
        ParticipantPortalRegistrar participantPortalRegistrar = new ParticipantPortalRegistrar();
        if (ocParticipateStatus.equals("enabled")) {
            try {
                ocuiParticipateStatus = participantPortalRegistrar.getRegistrationStatus(studyBean.getOid());
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

        HashMap<String, String> mapMetadata = new HashMap<>();

        String url = "";
        URL pManageUrl = null;
        try {
            pManageUrl = new URL(portalURL);
        } catch (MalformedURLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        Authorization pManageAuthorization = participantPortalRegistrar.getAuthorization(studyBean.getOid());
        if (pManageAuthorization != null) {
            url = pManageUrl.getProtocol() + "://" + pManageAuthorization.getStudy().getHost() + "." + pManageUrl.getHost()
                    + ((pManageUrl.getPort() > 0) ? ":" + String.valueOf(pManageUrl.getPort()) : "");
            mapMetadata.put("Participate Url", url);
        }

        HashMap<String, Object> mapParticipate = new HashMap<>();
        mapParticipate.put("enabled", ocParticipateStatus.equals("enabled") ? "True" : "False");
        mapParticipate.put("status", ocuiParticipateStatus.equals("") ? "INACTIVE" : ocuiParticipateStatus);
        mapParticipate.put("metadata", mapMetadata);

        HashMap<String, Object> mapModule = new HashMap<>();
        mapModule.put("Participate", mapParticipate);

        return mapModule;
    }

    public HashMap<String, Object> getRandomizeModule(StudyBean studyBean) {
        StudyParameterValueBean spvBean = getParticipateMod(studyBean, "randomization");
        String ocRandomizeStatus = "";
        if (spvBean.isActive()) {
            ocRandomizeStatus = spvBean.getValue().toString(); // enabled , disabled
        }
        SeRandomizationDTO seRandomizationDTO = null;
        String ocuiRandomizeStatus = "";
        URL randomizeUrl = null;
        HashMap<String, String> mapMetadata = new HashMap<>();

        if (ocRandomizeStatus.equals("enabled")) {
            try {

                RandomizationRegistrar randomizationRegistrar = new RandomizationRegistrar();
                seRandomizationDTO = randomizationRegistrar.getRandomizationDTOObject(studyBean.getOid());
                if (seRandomizationDTO != null && seRandomizationDTO.getStatus() != null) {
                    ocuiRandomizeStatus = seRandomizationDTO.getStatus();
                    if (seRandomizationDTO.getUrl() != null) {
                        randomizeUrl = new URL(seRandomizationDTO.getUrl());
                    }
                    mapMetadata.put("Randomize URL", randomizeUrl == null ? "" : randomizeUrl.toString());
                }
            } catch (MalformedURLException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();

            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

        HashMap<String, Object> mapRandomize = new HashMap<>();
        mapRandomize.put("enabled", ocRandomizeStatus.equals("enabled") ? "True" : "False");
        mapRandomize.put("status", ocuiRandomizeStatus.equals("") ? "INACTIVE" : ocuiRandomizeStatus);
        mapRandomize.put("metadata", mapMetadata);

        HashMap<String, Object> mapModule = new HashMap<>();
        mapModule.put("Randomize", mapRandomize);

        return mapModule;
    }

    public HashMap<String, Object> getRuleDesignerModule(StudyBean studyBean) {
        String designerUrl = CoreResources.getField("designerURL");
        String result = "";
        HttpURLConnection huc = null;
        try {
            URL u = new URL(designerUrl);
            huc = (HttpURLConnection) u.openConnection();
            huc.setRequestMethod("HEAD");
            huc.connect();
            if (huc.getResponseCode() == 200) {
                result = "ACTIVE";
            } else {
                result = "INACTIVE";
            }

        } catch (Exception ex) {
            result = "INACTIVE";
            // Handle invalid URL
        }

        HashMap<String, String> mapMetadata = new HashMap<>();
        mapMetadata.put("Designer URL", designerUrl);
        try {
            mapMetadata.put("Http Status Code", String.valueOf(huc.getResponseCode()));
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        HashMap<String, Object> mapRuleDesigner = new HashMap<>();
        mapRuleDesigner.put("enabled", !designerUrl.equals("") ? "True" : "False");
        mapRuleDesigner.put("status", result);
        mapRuleDesigner.put("metadata", mapMetadata);

        HashMap<String, Object> mapModule = new HashMap<>();
        mapModule.put("Rule Designer", mapRuleDesigner);

        return mapModule;
    }

    public HashMap<String, Object> getMessagingModule(StudyBean studyBean) {

        String result = sendEmail(mailSender, "This is the Subject Of a Rest Call for Health Check", "This is the Body Of a Rest Call for Health Check");
        String mailProtocol = CoreResources.getField("mailProtocol");
        String mailPort = CoreResources.getField("mailPort");
        String mailHost = CoreResources.getField("mailHost");

        HashMap<String, String> mapMetadata = new HashMap<>();
        mapMetadata.put("mail.host", mailHost);
        mapMetadata.put("mail.protocol", mailProtocol);

        HashMap<String, Object> mapMessaging = new HashMap<>();
        mapMessaging.put("enabled", (!mailProtocol.equals("") && !mailPort.equals("") && !mailHost.equals("")) ? "True" : "False");
        mapMessaging.put("status", result);
        mapMessaging.put("metadata", mapMetadata);

        HashMap<String, Object> mapModule = new HashMap<>();
        mapModule.put("Messaging", mapMessaging);

        return mapModule;
    }

    public HashMap<String, Object> getDatamartModule(StudyBean studyBean) {

        HashMap<String, String> datamartRole = new HashMap<>();
        String username = CoreResources.getExtractField("db1.username");
        String password = CoreResources.getExtractField("db1.password");
        String url = CoreResources.getExtractField("db1.url");

        ArrayList<ExtractPropertyBean> extracts = CoreResources.getExtractProperties();
        String enabled = "False";
        for (ExtractPropertyBean extract : extracts) {

            if (extract.getFiledescription().equalsIgnoreCase("Datamart")) {
                enabled = "True";
                break;
            }
        }

        String result = "";
        try (Connection conn = DriverManager.getConnection(url, username, password)) {
            datamartRole = getDbRoleProperties(conn, datamartRole, username);
            result = "ACTIVE";
        } catch (Exception e) {
            result = "INACTIVE";
        }

        HashMap<String, Object> mapMetadata = new HashMap<>();
        mapMetadata.put("db1.username", username);
        mapMetadata.put("db1.url", url);
        mapMetadata.put("db1.dataBase", CoreResources.getExtractField("db1.dataBase"));
        mapMetadata.put("Role Properties", datamartRole);

        HashMap<String, Object> mapDatamart = new HashMap<>();
        mapDatamart.put("enabled", enabled);
        mapDatamart.put("status", result);
        mapDatamart.put("metadata", mapMetadata);

        HashMap<String, Object> mapModule = new HashMap<>();
        mapModule.put("Datamart", mapDatamart);

        return mapModule;
    }

    public HashMap<String, Object> getWebServiceModule(StudyBean studyBean) {
        String webserviceUrl = CoreResources.getField("sysURL");
        webserviceUrl = webserviceUrl.replace("/MainMenu", "-ws");

        String result = "";
        HttpURLConnection huc = null;
        try {
            URL u = new URL(webserviceUrl);
            huc = (HttpURLConnection) u.openConnection();
            huc.setRequestMethod("HEAD");
            huc.connect();
            if (huc.getResponseCode() == 200) {
                result = "ACTIVE";
            } else {
                result = "INACTIVE";
            }

        } catch (Exception ex) {
            result = "INACTIVE";
            // Handle invalid URL
        }

        HashMap<String, String> mapMetadata = new HashMap<>();
        mapMetadata.put("WebService URL", webserviceUrl);
        try {
            mapMetadata.put("Http Status Code", String.valueOf(huc.getResponseCode()));
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        HashMap<String, Object> mapWebService = new HashMap<>();
        mapWebService.put("enabled", !webserviceUrl.equals("") ? "True" : "False");
        mapWebService.put("status", result);
        mapWebService.put("metadata", mapMetadata);

        HashMap<String, Object> mapModule = new HashMap<>();
        mapModule.put("Web Service", mapWebService);

        return mapModule;
    }

    public HashMap<String, Object> getLdapModule(StudyBean studyBean) {
        String enabled = CoreResources.getField("ldap.enabled");
        String ldapHost = CoreResources.getField("ldap.host");
        String username = CoreResources.getField("ldap.userDn");
        String password = CoreResources.getField("ldap.password");

        String result = "";
        Properties env = new Properties();

        env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
        env.put(Context.PROVIDER_URL, ldapHost);
        env.put(Context.SECURITY_AUTHENTICATION, "simple");
        env.put(Context.SECURITY_PRINCIPAL, username); // replace with user DN
        env.put(Context.SECURITY_CREDENTIALS, password);

        DirContext ctx = null;
        try {
            ctx = new InitialDirContext(env);
            result = "ACTIVE";
        } catch (Exception e) {
            result = "INACTIVE";
        }

        HashMap<String, String> mapMetadata = new HashMap<>();
        mapMetadata.put("ldap.host", ldapHost);

        HashMap<String, Object> mapWebService = new HashMap<>();
        mapWebService.put("enabled", enabled.equalsIgnoreCase("true") ? "True" : "False");
        mapWebService.put("status", result);
        mapWebService.put("metadata", mapMetadata);

        HashMap<String, Object> mapModule = new HashMap<>();
        mapModule.put("Ldap", mapWebService);

        return mapModule;
    }

    public HashMap<String, Object> getRuleDesignerModuleInSession(StudyBean studyBean, HttpSession session) {

        HashMap<String, Object> mapModule = (HashMap<String, Object>) session.getAttribute("ruledesigner");
        if (mapModule == null) {
            mapModule = getRuleDesignerModule(studyBean);
            session.setAttribute("ruledesigner", mapModule);
        }
        return mapModule;
    }

    public HashMap<String, Object> getMessagingModuleInSession(StudyBean studyBean, HttpSession session) {

        HashMap<String, Object> mapModule = (HashMap<String, Object>) session.getAttribute("messaging");
        if (mapModule == null) {
            mapModule = getMessagingModule(studyBean);
            session.setAttribute("messaging", mapModule);
        }
        return mapModule;
    }

    public HashMap<String, Object> getDatamartModuleInSession(StudyBean studyBean, HttpSession session) {

        HashMap<String, Object> mapModule = (HashMap<String, Object>) session.getAttribute("datamart");
        if (mapModule == null) {
            mapModule = getDatamartModule(studyBean);
            session.setAttribute("datamart", mapModule);
        }
        return mapModule;
    }

    public HashMap<String, Object> getWebServiceModuleInSession(StudyBean studyBean, HttpSession session) {

        HashMap<String, Object> mapModule = (HashMap<String, Object>) session.getAttribute("webservice");
        if (mapModule == null) {
            mapModule = getWebServiceModule(studyBean);
            session.setAttribute("webservice", mapModule);
        }
        return mapModule;
    }

    public HashMap<String, Object> getLdapModuleInSession(StudyBean studyBean, HttpSession session) {

        HashMap<String, Object> mapModule = (HashMap<String, Object>) session.getAttribute("ldap");
        if (mapModule == null) {
            mapModule = getLdapModule(studyBean);
            session.setAttribute("ldap", mapModule);
        }
        return mapModule;
    }
}
