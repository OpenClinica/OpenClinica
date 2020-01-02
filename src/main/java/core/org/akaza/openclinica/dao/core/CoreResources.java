package core.org.akaza.openclinica.dao.core;

import core.org.akaza.openclinica.bean.core.KeyCloakConfiguration;
import core.org.akaza.openclinica.bean.extract.ExtractPropertyBean;
import core.org.akaza.openclinica.bean.login.UserAccountBean;
import core.org.akaza.openclinica.bean.service.PdfProcessingFunction;
import core.org.akaza.openclinica.bean.service.SasProcessingFunction;
import core.org.akaza.openclinica.bean.service.SqlProcessingFunction;
import core.org.akaza.openclinica.dao.hibernate.StudyDao;
import core.org.akaza.openclinica.dao.login.UserAccountDAO;
import core.org.akaza.openclinica.domain.datamap.Study;
import core.org.akaza.openclinica.exception.OpenClinicaSystemException;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.keycloak.authorization.client.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.sql.DataSource;
import java.io.*;
import java.net.URL;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;
import java.util.regex.Pattern;

import static core.org.akaza.openclinica.dao.hibernate.multitenant.CurrentTenantIdentifierResolverImpl.CURRENT_TENANT_ID;

@Component("coreResources")
public class CoreResources implements InitializingBean {

    private ResourceLoader resourceLoader;
    public static String PROPERTIES_DIR;
    private static String DB_NAME;
    public static ThreadLocal<String> tenantSchema = new ThreadLocal<>();
    private static Properties DATAINFO;
    private static Properties EXTRACTINFO;
    private static KeyCloakConfiguration KEYCLOAKCONFIG;

    public static final Integer PDF_ID = 10;
    public static final Integer TAB_ID = 8;
    public static final Integer CDISC_ODM_1_2_ID = 5;
    public static final Integer CDISC_ODM_1_2_EXTENSION_ID = 4;
    public static final Integer CDISC_ODM_1_3_ID = 3;
    public static final Integer CDISC_ODM_1_3_EXTENSION_ID = 2;
    public static final Integer SPSS_ID = 9;
    private static final String DATA_INFO_FILE_NAME = "datainfo.properties";
    private static final String EXTRACT_INFO_FILE_NAME = "extract.properties";
    private static final String EXTERNAL_PROPERTY_DIRECTORY = System.getProperty("user.home") + "/runtime-config/";

    private static String webapp;
    protected final static Logger logger = LoggerFactory.getLogger("core.org.akaza.openclinica.dao.core.CoreResources");
    // private MessageSource messageSource;
    private static ArrayList<ExtractPropertyBean> extractProperties;

    public static String ODM_MAPPING_DIR;
    // TODO:Clean up all system outs
    // default no arg constructor
    public CoreResources() {

    }

    /**
     * TODO: Delete me!
     *
     * @param dataInfoProps
     * @throws IOException
     */
    public CoreResources(Properties dataInfoProps) throws IOException {
        DATAINFO = dataInfoProps;
        if (resourceLoader == null)
            resourceLoader = new DefaultResourceLoader();
        webapp = getWebAppName(resourceLoader.getResource("/").getURI().getPath());

    }

    public void reportUrl() {
        String contHome = System.getProperty("catalina.home");
        Properties pros = System.getProperties();
        Enumeration proEnum = pros.propertyNames();
        for (; proEnum.hasMoreElements(); ) {
            // Get property name
            String propName = (String) proEnum.nextElement();

            // Get property value
            String propValue = (String) pros.get(propName);
        }
    }

    private void extractKeyCloakConfig() {
        KEYCLOAKCONFIG = new KeyCloakConfiguration();
        KEYCLOAKCONFIG.setRealm(DATAINFO.getProperty("keycloak.realm"));
        KEYCLOAKCONFIG.setAuthServerUrl(DATAINFO.getProperty("keycloak.auth-server-url"));
        String secretKey = "secret";
        String secretValue = DATAINFO.getProperty("keycloak.credentials.secret");
        Map<String, Object> credentials = new TreeMap<String, Object>() {{
            put(secretKey, secretValue);
        }};
        KEYCLOAKCONFIG.setCredentials(credentials);
    }

    public static Configuration getKeyCloakConfig() {
        return KEYCLOAKCONFIG;
    }

    public static void overwriteExternalPropOnInternalProp(Properties internalProp, Properties externalProp) {
        if (externalProp != null && !externalProp.isEmpty()) {
            Set<String> externalKeys = externalProp.stringPropertyNames();
            externalKeys.forEach(key -> internalProp.setProperty(key, externalProp.getProperty(key)));
        }
    }

    public static UserAccountBean setRootUserAccountBean(HttpServletRequest request, DataSource dataSource) {
        UserAccountDAO userAccountDAO = new UserAccountDAO(dataSource);
        UserAccountBean ub = (UserAccountBean) userAccountDAO.findByUserName("root");
        if (ub.getId() != 0) {
            request.getSession().setAttribute("userBean", ub);
        }
        return ub;
    }

    /**
     * For changing values which are applicable to all properties, for ex webapp name can be used in any properties
     */
    private void setDataInfoVals() {

        Enumeration<String> properties = (Enumeration<String>) DATAINFO.propertyNames();
        String vals, key;
        while (properties.hasMoreElements()) {
            key = properties.nextElement();
            vals = DATAINFO.getProperty(key);
            // replacePaths(vals);
            vals = replaceWebapp(vals);
            vals = replaceCatHome(vals);
            DATAINFO.setProperty(key, vals);
        }

    }

    private static String replaceWebapp(String value) {

        if (value.contains("${WEBAPP}")) {
            value = value.replace("${WEBAPP}", webapp);
        } else if (value.contains("${WEBAPP.lower}")) {
            value = value.replace("${WEBAPP.lower}", webapp.toLowerCase());
        }
        if (value.contains("$WEBAPP.lower")) {
            value = value.replace("$WEBAPP.lower", webapp.toLowerCase());
        } else if (value.contains("$WEBAPP")) {
            value = value.replace("$WEBAPP", webapp);
        }

        return value;
    }

    private static String replaceCatHome(String value) {
        String catalina = null;
        if (catalina == null) {
            catalina = System.getProperty("CATALINA_HOME");
        }

        if (catalina == null) {
            catalina = System.getProperty("catalina.home");
        }

        if (catalina == null) {
            catalina = System.getenv("CATALINA_HOME");
        }

        if (catalina == null) {
            catalina = System.getenv("catalina.home");
        }
        // logMe("catalina home - " + value);
        // logMe("CATALINA_HOME system variable is " + System.getProperty("CATALINA_HOME"));
        // logMe("CATALINA_HOME system env variable is " + System.getenv("CATALINA_HOME"));
        // logMe(" -Dcatalina.home system property variable is"+System.getProperty(" -Dcatalina.home"));
        // logMe("CATALINA.HOME system env variable is"+System.getenv("catalina.home"));
        // logMe("CATALINA_BASE system env variable is"+System.getenv("CATALINA_BASE"));
        // Map<String, String> env = System.getenv();
        // for (String envName : env.keySet()) {
        // logMe("%s=%s%n"+ envName+ env.get(envName));
        // }

        if (value.contains("${catalina.home}") && catalina != null) {
            value = value.replace("${catalina.home}", catalina);
        }

        if (value.contains("$catalina.home") && catalina != null) {
            value = value.replace("$catalina.home", catalina);
        }

        return value;
    }

    private static String replacePaths(String vals) {
        if (vals != null) {
            if (vals.contains("/")) {
                vals = vals.replace("/", File.separator);
            } else if (vals.contains("\\")) {
                vals = vals.replace("\\", File.separator);
            } else if (vals.contains("\\\\")) {
                vals = vals.replace("\\\\", File.separator);
            }
        }
        return vals;
    }

    private Properties setDataInfoProperties() {
        // getPropertiesSource();

        String filePath = DATAINFO.getProperty("filePath");
        if (filePath == null || filePath.isEmpty())
            filePath = "$catalina.home/$WEBAPP.lower.data";
        String database = DATAINFO.getProperty("dbType");

        setDatabaseProperties(database);

        setDataInfoVals();
        if (DATAINFO.getProperty("filePath") == null || DATAINFO.getProperty("filePath").length() <= 0)
            DATAINFO.setProperty("filePath", filePath);
        DATAINFO.setProperty("changeLogFile", "migration/master.xml");
        // sysURL.base
        String sysURLBase = DATAINFO.getProperty("sysURL").replace("MainMenu", "");
        DATAINFO.setProperty("sysURL.base", sysURLBase);

        if (DATAINFO.getProperty("org.quartz.jobStore.misfireThreshold") == null)
            DATAINFO.setProperty("org.quartz.jobStore.misfireThreshold", "60000");
        DATAINFO.setProperty("org.quartz.jobStore.class", "core.org.akaza.openclinica.dao.core.MultiSchemaJobStoreTx");// "org.quartz.impl.jdbcjobstore.JobStoreTX");

        if (database.equalsIgnoreCase("oracle")) {
            DATAINFO.setProperty("org.quartz.jobStore.driverDelegateClass", "org.quartz.impl.jdbcjobstore.oracle.OracleDelegate");
        } else if (database.equalsIgnoreCase("postgres")) {
            DATAINFO.setProperty("org.quartz.jobStore.driverDelegateClass", "org.quartz.impl.jdbcjobstore.PostgreSQLDelegate");
        }

        DATAINFO.setProperty("org.quartz.jobStore.useProperties", "false");
        DATAINFO.setProperty("org.quartz.jobStore.tablePrefix", "oc_qrtz_");
        if (DATAINFO.getProperty("org.quartz.threadPool.threadCount") == null)
            DATAINFO.setProperty("org.quartz.threadPool.threadCount", "1");
        if (DATAINFO.getProperty("org.quartz.threadPool.threadPriority") == null)
            DATAINFO.setProperty("org.quartz.threadPool.threadPriority", "5");

        String attached_file_location = DATAINFO.getProperty("attached_file_location");
        if (attached_file_location == null || attached_file_location.isEmpty()) {
            attached_file_location = DATAINFO.getProperty("filePath") + "attached_files" + File.separator;
            DATAINFO.setProperty("attached_file_location", attached_file_location);
        }

        String change_passwd_required = DATAINFO.getProperty("change_passwd_required");
        if (change_passwd_required == null || change_passwd_required.isEmpty()) {
            change_passwd_required = "1";
            DATAINFO.setProperty("change_passwd_required", change_passwd_required);

        }
        setMailProps();
        setRuleDesignerProps();
        if (DATAINFO.getProperty("crfFileExtensions") != null)
            DATAINFO.setProperty("crf_file_extensions", DATAINFO.getProperty("crfFileExtensions"));
        if (DATAINFO.getProperty("crfFileExtensionSettings") != null)
            DATAINFO.setProperty("crf_file_extension_settings", DATAINFO.getProperty("crfFileExtensionSettings"));

        String dataset_file_delete = DATAINFO.getProperty("dataset_file_delete");
        if (dataset_file_delete == null)
            DATAINFO.setProperty("dataset_file_delete", "true");
        ;// TODO:Revisit me!
        String password_expiration_time = DATAINFO.getProperty("passwdExpirationTime");
        if (password_expiration_time != null)
            DATAINFO.setProperty("passwd_expiration_time", password_expiration_time);

        if (DATAINFO.getProperty("maxInactiveInterval") != null)
            DATAINFO.setProperty("max_inactive_interval", DATAINFO.getProperty("maxInactiveInterval"));

        DATAINFO.setProperty("ra", "Data_Entry_Person");
        DATAINFO.setProperty("ra2", "site_Data_Entry_Person2");
        DATAINFO.setProperty("participate", "site_Data_Entry_Participant");
        DATAINFO.setProperty("investigator", "Investigator");
        DATAINFO.setProperty("director", "Study_Director");

        DATAINFO.setProperty("coordinator", "Study_Coordinator");
        DATAINFO.setProperty("monitor", "Monitor");

        String rss_url = DATAINFO.getProperty("rssUrl");
        if (rss_url == null || rss_url.isEmpty())
            rss_url = "http://blog.openclinica.com/feed/";
        DATAINFO.setProperty("rss.url", rss_url);
        String rss_more = DATAINFO.getProperty("rssMore");
        if (rss_more == null || rss_more.isEmpty())
            rss_more = "http://blog.openclinica.com/";
        DATAINFO.setProperty("rss.more", rss_more);

        String supportURL = DATAINFO.getProperty("supportURL");
        if (supportURL == null || supportURL.isEmpty())
            supportURL = "https://www.openclinica.com/support";
        DATAINFO.setProperty("supportURL", supportURL);

        String walkmeURL = DATAINFO.getProperty("walkme.url");
        if (walkmeURL == null || walkmeURL.isEmpty())
            walkmeURL = "https://ineedawalkme.url";
        DATAINFO.setProperty("walkmeURL", walkmeURL);

        String piwikURL = DATAINFO.getProperty("piwik.url");
        if (piwikURL == null || piwikURL.isEmpty())
            piwikURL = "https://ineedapiwik.url";
        DATAINFO.setProperty("piwikURL", piwikURL);

        DATAINFO.setProperty("show_unique_id", "1");

        DATAINFO.setProperty("auth_mode", "password");
        logger.debug("DataInfo..." + DATAINFO);

        String designerURL = DATAINFO.getProperty("designerURL");
        if (designerURL == null || designerURL.isEmpty()) {
            DATAINFO.setProperty("designer.url", designerURL);
        }

        String xformEnabled = DATAINFO.getProperty("xformEnabled");
        if (xformEnabled == null || xformEnabled.isEmpty())
            DATAINFO.setProperty("xformEnabled", "");

        String portalURL = DATAINFO.getProperty("portalURL");
        if (portalURL == null || portalURL.isEmpty()) {
            DATAINFO.setProperty("portal.url", "");
            logger.debug(" Portal URL NOT Defined in datainfo ");
        } else {
            logger.debug("Portal URL IS Defined in datainfo:  " + portalURL);
        }
        String moduleManager = DATAINFO.getProperty("moduleManager");
        if (moduleManager == null || moduleManager.isEmpty()) {
            DATAINFO.setProperty("moduleManager.url", "");
            logger.debug(" Module Manager URL NOT Defined in datainfo ");
        } else {
            logger.debug("Module Manager URL IS Defined in datainfo:  " + moduleManager);
        }
        return DATAINFO;

    }

    private void setMailProps() {

        DATAINFO.setProperty("mail.host", DATAINFO.getProperty("mailHost"));
        DATAINFO.setProperty("mail.port", DATAINFO.getProperty("mailPort"));
        DATAINFO.setProperty("mail.protocol", DATAINFO.getProperty("mailProtocol"));
        DATAINFO.setProperty("mail.username", DATAINFO.getProperty("mailUsername"));
        DATAINFO.setProperty("mail.password", DATAINFO.getProperty("mailPassword"));
        DATAINFO.setProperty("mail.smtp.auth", DATAINFO.getProperty("mailSmtpAuth"));
        DATAINFO.setProperty("mail.smtp.starttls.enable", DATAINFO.getProperty("mailSmtpStarttls.enable"));
        DATAINFO.setProperty("mail.smtps.auth", DATAINFO.getProperty("mailSmtpsAuth"));
        DATAINFO.setProperty("mail.smtps.starttls.enable", DATAINFO.getProperty("mailSmtpsStarttls.enable"));
        DATAINFO.setProperty("mail.smtp.connectiontimeout", DATAINFO.getProperty("mailSmtpConnectionTimeout"));
        DATAINFO.setProperty("mail.errormsg", DATAINFO.getProperty("mailErrorMsg"));

    }

    private void setRuleDesignerProps() {

        DATAINFO.setProperty("designer.url", DATAINFO.getProperty("designerURL"));
    }

    public static String getStudyManager() {
        return DATAINFO.getProperty("SBSBaseUrl") + "/#/account-study";
    }

    public static void setSchema(Connection conn) throws SQLException {
        Statement statement = conn.createStatement();
        String schema = null;

        schema = handleMultiSchemaConnection(conn);

        logger.debug("Using schema in CoreResources:schema:" + schema);
        if (StringUtils.isEmpty(schema) || conn.getSchema().equalsIgnoreCase(schema))
            return;
        try {
            statement.execute("set search_path to '" + schema + "'");
        } finally {
            statement.close();
        }
    }


    public static String getRequestSchema() {
        ServletRequestAttributes requestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (requestAttributes != null && requestAttributes.getRequest() != null) {
            HttpServletRequest request = requestAttributes.getRequest();
            if (request.getAttribute("requestSchema") != null) {
                return (String) request.getAttribute("requestSchema");
            }
        }
        String schema = null;
        if (tenantSchema.get() != null) {
            schema = tenantSchema.get();
        } else
            schema = DATAINFO.getProperty("schema");
        return schema;

    }

    public static String getRequestSchema(HttpServletRequest request) {
        return (String) request.getAttribute("requestSchema");
    }

    public static boolean setRequestSchema(String schema) {
        ServletRequestAttributes requestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (requestAttributes != null && requestAttributes.getRequest() != null) {
            HttpServletRequest request = requestAttributes.getRequest();
            request.setAttribute("requestSchema", schema);
            return true;
        } else {
            CoreResources.tenantSchema.set(schema);
        }

        return false;
    }

    public static void setRequestSchema(HttpServletRequest request, String schema) {
        request.setAttribute("requestSchema", schema);
    }

    public static HttpServletRequest getRequest() {
        ServletRequestAttributes requestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (requestAttributes != null && requestAttributes.getRequest() != null) {
            HttpServletRequest request = requestAttributes.getRequest();
            return request;
        }
        return null;
    }

    private static String handleMultiSchemaConnection(Connection conn) throws SQLException {
        String schema = null;
        if (tenantSchema.get() == null)
            tenantSchema.set(conn.getSchema());

        ServletRequestAttributes requestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (requestAttributes != null && requestAttributes.getRequest() != null) {
            HttpServletRequest request = requestAttributes.getRequest();
            HttpSession session = requestAttributes.getRequest().getSession();
            if (request.getAttribute("changeStudySchema") != null) {
                schema = (String) request.getAttribute("changeStudySchema");
                logger.debug("request.getAttribute(\"changeStudySchema\"):" + schema);
                if (session != null) {
                    session.setAttribute(CURRENT_TENANT_ID, schema);
                }
            } else if (request.getAttribute("requestSchema") != null) {
                schema = (String) request.getAttribute("requestSchema");
                logger.debug("request.getAttribute(\"requestSchema\"):" + schema);
            } else if (session != null) {
                schema = (String) session.getAttribute(CURRENT_TENANT_ID);
                logger.debug("Session schema:" + schema);
            } else {
                schema = (String) request.getAttribute(CURRENT_TENANT_ID);
                logger.debug("Request schema:" + schema);
            }
            if (StringUtils.isNotEmpty(schema))
                tenantSchema.set(schema);
        }
        if (StringUtils.isEmpty(schema)) {
            if (tenantSchema.get() != null) {
                schema = tenantSchema.get();
            } else
                schema = DATAINFO.getProperty("schema");
        }
        logger.debug("Current thread schema:" + tenantSchema.get());
        logger.debug("Current thread:" + Thread.currentThread().getId() + " Current schema for JDBC connections:" + schema);
        return schema;
    }

    private void setDatabaseProperties(String database) {

        DATAINFO.setProperty("username", DATAINFO.getProperty("dbUser"));
        DATAINFO.setProperty("password", DATAINFO.getProperty("dbPass"));
        String dbSSLsetting = String.valueOf(DATAINFO.getOrDefault("dbSSL", "false"));

        String url = null, driver = null, hibernateDialect = null;
        if (database.equalsIgnoreCase("postgres")) {
            url = "jdbc:postgresql:" + "//" + DATAINFO.getProperty("dbHost") + ":" + DATAINFO.getProperty("dbPort") + "/" + DATAINFO.getProperty("db");
            driver = "org.postgresql.Driver";
            hibernateDialect = "org.hibernate.dialect.PostgreSQL94Dialect";
            if (dbSSLsetting.equals("true")) {
                url = url + "?ssl=true&sslfactory=org.postgresql.ssl.NonValidatingFactory";
            }
        } else if (database.equalsIgnoreCase("oracle")) {
            url = "jdbc:oracle:thin:" + "@" + DATAINFO.getProperty("dbHost") + ":" + DATAINFO.getProperty("dbPort") + ":" + DATAINFO.getProperty("db");
            driver = "oracle.jdbc.driver.OracleDriver";
            hibernateDialect = "org.hibernate.dialect.OracleDialect";
        }

        DATAINFO.setProperty("dataBase", database);
        DATAINFO.setProperty("url", url);
        DATAINFO.setProperty("hibernate.dialect", hibernateDialect);
        DATAINFO.setProperty("driver", driver);

    }

    private void copyBaseToDest(ResourceLoader resourceLoader) {
        ResourcePatternResolver resolver = new PathMatchingResourcePatternResolver(resourceLoader);
        Resource[] resources;
        try {
            /*
             * Use classpath* to search for resources that match this pattern in ALL of the jars in the application
             * class path. See:
             * http://static.springsource.org/spring/docs/3.0.x/spring-framework-reference/html/resources
             * .html#resources-classpath-wildcards
             */
            resources = resolver.getResources("classpath*:core/properties/xslt/*.xsl");

        } catch (IOException ioe) {
            logger.debug(ioe.getMessage(), ioe);
            throw new OpenClinicaSystemException("Unable to read source files", ioe);
        }

        File dest = new File(getField("filePath") + "xslt");
        if (!dest.exists()) {
            if (!dest.mkdirs()) {
                throw new OpenClinicaSystemException("Copying files, Could not create direcotry: " + dest.getAbsolutePath() + ".");
            }
        }

        for (Resource r : resources) {
            File f = new File(dest, r.getFilename());
            try {

                FileOutputStream out = new FileOutputStream(f);
                IOUtils.copy(r.getInputStream(), out);
                out.close();

            } catch (IOException ioe) {
                logger.debug(ioe.getMessage(), ioe);
                throw new OpenClinicaSystemException("Unable to copy file: " + r.getFilename() + " to " + f.getAbsolutePath(), ioe);

            }
        }
    }

    private void copyImportRulesFiles() throws IOException {
        ByteArrayInputStream listSrcFiles[] = new ByteArrayInputStream[3];
        ResourcePatternResolver resolver = new PathMatchingResourcePatternResolver(resourceLoader);
        String[] fileNames = {"rules.xsd", "rules_template.xml", "rules_template_with_notes.xml"};
        Resource[] resources = null;
        Resource[] resourcesTemplate = null;
        Resource[] resourcesPipeDelimitedTemplate = null;
        FileOutputStream out = null;

        resources = resolver.getResources("classpath*:core/properties/rules_template*.xml");
        resourcesTemplate = resolver.getResources("classpath*:core/properties/import_template*.xml");
        resourcesPipeDelimitedTemplate = resolver.getResources("classpath*:core/properties/template_pipe*.txt");

        File dest = new File(getField("filePath") + "rules");
        if (!dest.exists()) {
            if (!dest.mkdirs()) {
                throw new OpenClinicaSystemException("Copying files, Could not create direcotry: " + dest.getAbsolutePath() + ".");
            }
        }
        for (Resource r : resources) {
            File f = new File(dest, r.getFilename());

            out = new FileOutputStream(f);
            IOUtils.copy(r.getInputStream(), out);
            out.close();

        }
        for (Resource r : resourcesTemplate) {
            File f = new File(dest, r.getFilename());

            out = new FileOutputStream(f);
            IOUtils.copy(r.getInputStream(), out);
            out.close();

        }

        for (Resource r : resourcesPipeDelimitedTemplate) {
            File f = new File(dest, r.getFilename());

            out = new FileOutputStream(f);
            IOUtils.copy(r.getInputStream(), out);
            out.close();

        }
        Resource[] r1 = resolver.getResources("classpath*:core/properties/" + fileNames[0]);
        File f1 = new File(dest, r1[0].getFilename());
        out = new FileOutputStream(f1);
        IOUtils.copy(r1[0].getInputStream(), out);
        out.close();

    }

    private void copyConfig() throws IOException {
        ResourcePatternResolver resolver = new PathMatchingResourcePatternResolver(resourceLoader);
        Resource[] resources = null;
        FileOutputStream out = null;
        Resource resource1 = null;
        Resource resource2 = null;

        resource1 = resolver.getResource("classpath:datainfo.properties");
        resource2 = resolver.getResource("classpath:extract.properties");

        String filePath = "$catalina.home/$WEBAPP.lower.config";

        filePath = replaceWebapp(filePath);
        filePath = replaceCatHome(filePath);

        File dest = new File(filePath);
        if (!dest.exists()) {
            if (!dest.mkdirs()) {
                throw new OpenClinicaSystemException("Copying files, Could not create directory: " + dest.getAbsolutePath() + ".");
            }
        }

        File f1 = new File(dest, resource1.getFilename());
        File f2 = new File(dest, resource2.getFilename());
        if (!f1.exists()) {
            out = new FileOutputStream(f1);
            IOUtils.copy(resource1.getInputStream(), out);
            out.close();
        }
        if (!f2.exists()) {
            out = new FileOutputStream(f2);
            IOUtils.copy(resource2.getInputStream(), out);
            out.close();
        }

        /*
         *
         * for (Resource r: resources) { File f = new File(dest, r.getFilename()); if(!f.exists()){ out = new
         * FileOutputStream(f); IOUtils.copy(r.getInputStream(), out); out.close(); } }
         */
    }

    /**
     * @deprecated. ByteArrayInputStream keeps the whole file in memory needlessly. Use Commons IO's
     * {@link IOUtils#copy(java.io.InputStream, java.io.OutputStream)} instead.
     */
    @Deprecated
    private void copyFiles(ByteArrayInputStream fis, File dest) {
        FileOutputStream fos = null;
        byte[] buffer = new byte[512]; // Buffer 4K at a time (you can change this).
        int bytesRead;
        logger.debug("fis?" + fis);
        try {
            fos = new FileOutputStream(dest);
            while ((bytesRead = fis.read(buffer)) >= 0) {
                fos.write(buffer, 0, bytesRead);
            }
        } catch (IOException ioe) {// error while copying files
            OpenClinicaSystemException oe = new OpenClinicaSystemException(
                    "Unable to copy file: " + fis + "to" + dest.getAbsolutePath() + "." + dest.getAbsolutePath() + ".");
            oe.initCause(ioe);
            oe.setStackTrace(ioe.getStackTrace());
            throw oe;
        } finally { // Ensure that the files are closed (if they were open).
            if (fis != null) {
                try {
                    fis.close();
                } catch (IOException ioe) {
                    OpenClinicaSystemException oe = new OpenClinicaSystemException(
                            "Unable to copy file: " + fis + "to" + dest.getAbsolutePath() + "." + dest.getAbsolutePath() + ".");
                    oe.initCause(ioe);
                    oe.setStackTrace(ioe.getStackTrace());
                    logger.debug(ioe.getMessage());
                    throw oe;

                }
            }
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException ioe) {
                    OpenClinicaSystemException oe = new OpenClinicaSystemException(
                            "Unable to copy file: " + fis + "to" + dest.getAbsolutePath() + "." + dest.getAbsolutePath() + ".");
                    oe.initCause(ioe);
                    oe.setStackTrace(ioe.getStackTrace());
                    logger.debug(ioe.getMessage());
                    throw oe;

                }
            }
        }
    }

    private void copyODMMappingXMLtoResources(ResourceLoader resourceLoader) {
        ResourcePatternResolver resolver = new PathMatchingResourcePatternResolver(resourceLoader);
        String[] fileNames = {"cd_odm_mapping.xml"};
        Resource[] resources;
        try {
            resources = resolver.getResources("classpath*:core/properties/cd_odm_mapping.xml");
        } catch (IOException ioe) {
            OpenClinicaSystemException oe = new OpenClinicaSystemException("Unable to read source files");
            oe.initCause(ioe);
            oe.setStackTrace(ioe.getStackTrace());
            logger.debug(ioe.getMessage());
            throw oe;
        }

        File dest = null;
        try {
            dest = new File(getField("filePath"));
            if (!dest.exists()) {
                if (!dest.mkdirs()) {
                    throw new OpenClinicaSystemException("Copying files, Could not create direcotry: " + dest.getAbsolutePath() + ".");
                }
            }
            File f = new File(dest, resources[0].getFilename());
            FileOutputStream out = new FileOutputStream(f);
            IOUtils.copy(resources[0].getInputStream(), out);
            out.close();

        } catch (IOException ioe) {
            OpenClinicaSystemException oe = new OpenClinicaSystemException("Unable to get web app base path");
            oe.initCause(ioe);
            oe.setStackTrace(ioe.getStackTrace());
            throw oe;
        }

    }

    public ResourceLoader getResourceLoader() {
        return resourceLoader;
    }

    public static ArrayList<ExtractPropertyBean> getExtractProperties() {
        return extractProperties;
    }

    public void setExtractProperties(ArrayList extractProperties) {
        this.extractProperties = extractProperties;
    }

    private ArrayList<ExtractPropertyBean> findExtractProperties() throws OpenClinicaSystemException {
        ArrayList<ExtractPropertyBean> ret = new ArrayList<ExtractPropertyBean>();

        // ExtractPropertyBean epbean = new ExtractPropertyBean();
        int i = 1;
        int maxExtractOption = getMaxExtractCounterValue();
        while (i <= maxExtractOption) {
            if (!getExtractField("extract." + i + ".file").equals("")) {
                ExtractPropertyBean epbean = new ExtractPropertyBean();
                epbean.setId(i);
                // we will implement a find by id function in the front end

                // check to make sure the file exists, if not throw an exception and system will abort to start.
                checkForFile(getExtractFields("extract." + i + ".file"));
                epbean.setFileName(getExtractFields("extract." + i + ".file"));
                // file name of the xslt stylesheet
                epbean.setFiledescription(getExtractField("extract." + i + ".fileDescription"));
                // description of the choice of format
                epbean.setHelpText(getExtractField("extract." + i + ".helpText"));
                // help text, currently in the alt-text of the link
                epbean.setLinkText(getExtractField("extract." + i + ".linkText"));
                // link text of the choice of format
                // epbean.setRolesAllowed(getExtractField("xsl.allowed." + i).split(","));
                // which roles are allowed to see the choice?
                epbean.setFileLocation(getExtractField("extract." + i + ".location"));
                // destination of the copied files
                // epbean.setFormat(getExtractField("xsl.format." + i));
                // if (("").equals(epbean.getFormat())) {
                // }
                // formatting choice. currently permenantly set at oc1.3
                /*
                 * String clinica = getExtractField("extract."+i+".odmType"); if(clinica!=null) {
                 * if(clinica.equalsIgnoreCase("clinical_data")) epbean.setFormat("occlinical_data"); else
                 * epbean.setFormat("oc1.3"); } else
                 */

                epbean.setOdmType(getExtractField("extract." + i + ".odmType"));

                epbean.setFormat("oc1.3");

                // destination file name of the copied files
                epbean.setExportFileName(getExtractFields("extract." + i + ".exportname"));
                // post-processing event after the creation
                String whichFunction = getExtractField("extract." + i + ".post").toLowerCase();
                // added by JN: Zipformat comes from extract properties returns true by default
                epbean.setZipFormat(getExtractFieldBoolean("extract." + i + ".zip"));
                epbean.setDeleteOld(getExtractFieldBoolean("extract." + i + ".deleteOld"));
                epbean.setSuccessMessage(getExtractField("extract." + i + ".success"));
                epbean.setFailureMessage(getExtractField("extract." + i + ".failure"));
                epbean.setZipName(getExtractField("extract." + i + ".zipName"));
                epbean.setFormatDescription(getExtractField("extract." + i + ".formatDescription"));
                if (epbean.getFileName().length != epbean.getExportFileName().length)
                    throw new OpenClinicaSystemException(
                            "The comma seperated values of file names and export file names should correspond 1 on 1 for the property number" + i);

                if ("sql".equals(whichFunction)) {
                    // set the bean within, so that we can access the file locations etc
                    SqlProcessingFunction function = new SqlProcessingFunction(epbean);
                    String whichSettings = getExtractField("xsl.post." + i + ".sql");
                    if (!"".equals(whichSettings)) {
                        function.setDatabaseType(getExtractFieldNoRep(whichSettings + ".dataBase").toLowerCase());
                        function.setDatabaseUrl(getExtractFieldNoRep(whichSettings + ".url"));
                        function.setDatabaseUsername(getExtractFieldNoRep(whichSettings + ".username"));
                        function.setDatabasePassword(getExtractFieldNoRep(whichSettings + ".password"));
                    } else {
                        // set default db settings here
                        function.setDatabaseType(getField("dataBase"));
                        function.setDatabaseUrl(getField("url"));
                        function.setDatabaseUsername(getField("username"));
                        function.setDatabasePassword(getField("password"));
                    }
                    // also pre-set the database connection stuff
                    epbean.setPostProcessing(function);
                    // System.out.println("found db password: " + function.getDatabasePassword());
                } else if ("pdf".equals(whichFunction)) {
                    // TODO add other functions here
                    epbean.setPostProcessing(new PdfProcessingFunction());
                } else if ("sas".equals(whichFunction)) {
                    epbean.setPostProcessing(new SasProcessingFunction());
                } else if (!whichFunction.isEmpty()) {
                    String postProcessorName = getExtractField(whichFunction + ".postProcessor");
                    if (postProcessorName.equals("pdf")) {
                        epbean.setPostProcessing(new PdfProcessingFunction());
                        epbean.setPostProcDeleteOld(getExtractFieldBoolean(whichFunction + ".deleteOld"));
                        epbean.setPostProcZip(getExtractFieldBoolean(whichFunction + ".zip"));
                        epbean.setPostProcLocation(getExtractField(whichFunction + ".location"));
                        epbean.setPostProcExportName(getExtractField(whichFunction + ".exportname"));
                    }
                    // since the database is the last option TODO: think about custom post processing options
                    else {
                        SqlProcessingFunction function = new SqlProcessingFunction(epbean);

                        function.setDatabaseType(getExtractFieldNoRep(whichFunction + ".dataBase").toLowerCase());
                        function.setDatabaseUrl(getExtractFieldNoRep(whichFunction + ".url"));
                        function.setDatabaseUsername(getExtractFieldNoRep(whichFunction + ".username"));
                        function.setDatabasePassword(getExtractFieldNoRep(whichFunction + ".password"));
                        epbean.setPostProcessing(function);
                    }

                } else {
                    // add a null here
                    epbean.setPostProcessing(null);
                }
                ret.add(epbean);
            }
            i++;
        }
        // tbh change to print out properties

        // System.out.println("found " + ret.size() + " records in extract.properties");
        return ret;
    }

    private int getMaxExtractCounterValue() {
        Set<String> properties = EXTRACTINFO.stringPropertyNames();
        int numExtractTypes = 0;
        for (String property : properties) {
            if (property.split(Pattern.quote(".")).length == 3 && property.startsWith("extract.") && property.endsWith(".file")) {
                try {
                    int value = Integer.parseInt(property.split(Pattern.quote("."))[1]);
                    if (value > numExtractTypes)
                        numExtractTypes = value;
                } catch (Exception e) {
                    // Wasn't a number. Do nothing.
                }
            }
        }
        return numExtractTypes;
    }

    private String getExtractFieldNoRep(String key) {
        String value = EXTRACTINFO.getProperty(key);
        if (value != null) {
            value = value.trim();
        }

        return value == null ? "" : value;
    }

    private void checkForFile(String[] extractFields) throws OpenClinicaSystemException {

        int cnt = extractFields.length;
        int i = 0;
        // iterate through all comma separated file names
        while (i < cnt) {

            File f = new File(getField("filePath") + "xslt" + File.separator + extractFields[i]);
            // System.out.println(getField("filePath") + "xslt" + File.separator + extractFields[i]);
            if (!f.exists())
                throw new OpenClinicaSystemException("FileNotFound -- Please make sure" + extractFields[i] + "exists");

            i++;

        }

    }

    public InputStream getInputStream(String fileName) throws IOException {
        return resourceLoader.getResource("classpath:core/properties/" + fileName).getInputStream();
    }

    public URL getURL(String fileName) throws IOException {
        return resourceLoader.getResource("classpath:core/properties/" + fileName).getURL();
    }

    /**
     * @deprecated Use {@link #getFile(String, String)} instead
     */
    @Deprecated
    public File getFile(String fileName) {
        return getFile(fileName, "filePath");
    }

    public File getFile(String fileName, String relDirectory) {
        try {

            InputStream inputStream = getInputStream(fileName);

            File f = new File(getField("filePath") + relDirectory + fileName);

            /*
             * OutputStream outputStream = new FileOutputStream(f); byte buf[] = new byte[1024]; int len; try { while
             * ((len = inputStream.read(buf)) > 0) outputStream.write(buf, 0, len); } finally { outputStream.close();
             * inputStream.close(); }
             */
            return f;

        } catch (IOException e) {
            throw new OpenClinicaSystemException(e.getMessage(), e.fillInStackTrace());
        }
    }

    public void setPROPERTIES_DIR() {
        String resource = "classpath:core/properties/placeholder.properties";
        // System.out.println("Resource " + resource);
        Resource scr = resourceLoader.getResource(resource);
        String absolutePath = null;
        try {
            // System.out.println("Resource" + resource);
            absolutePath = scr.getFile().getAbsolutePath();
            // System.out.println("Resource" + ((ClassPathResource) scr).getPath());
            // System.out.println("Resource" + resource);
            PROPERTIES_DIR = absolutePath.replaceAll("placeholder.properties", "");
            // System.out.println("Resource " + PROPERTIES_DIR);
        } catch (IOException e) {
            throw new OpenClinicaSystemException(e.getMessage(), e.fillInStackTrace());
        }

    }

    /**
     * @pgawade 18-April-2011 - Fix for issue 8394 Method to set the absolute file path value to point to "odm_mapping"
     * in resources. cd_odm_mapping.xml file used by Castor API during CRF data import will be copied to this
     * location during application initialization
     */
    public void setODM_MAPPING_DIR() {
        ODM_MAPPING_DIR = getField("filePath");
    }

    public static String getDBName() {
        if (null == DB_NAME)
            return "postgres";
        return DB_NAME;
    }

    public static String getField(String key) {
        String value = DATAINFO.getProperty(key);
        if (value != null) {
            value = value.trim();
        }
        return value == null ? "" : value;
    }

    public static String getSBSFieldFormservice() {
        String value = getField("SBSBaseUrl");
        return value.concat("/form-service/api");
    }

    // TODO internationalize
    public static String getExtractField(String key) {
        String value = EXTRACTINFO.getProperty(key);
        if (value != null) {
            value = value.trim();
        }
        value = replacePaths(value);
        return value == null ? "" : value;
    }

    // JN:The following method returns default of true when converting from string
    public static boolean getExtractFieldBoolean(String key) {
        String value = EXTRACTINFO.getProperty(key);
        if (value != null) {
            value = value.trim();
        }
        if (value == null)
            return true;// Defaulting to true
        if (value.equalsIgnoreCase("false"))
            return false;
        else
            return true;// defaulting to true

    }

    public static String[] getExtractFields(String key) {
        String value = EXTRACTINFO.getProperty(key);

        // System.out.println("key? " + key + " value = " + value);

        if (value != null) {
            value = value.trim();
        }
        return value.split(",");
    }

    // JN: by using static when u click same export link from 2 different datasets the first one stays in tact and is
    // saved in
    // there.

    /**
     *
     */
    public ExtractPropertyBean findExtractPropertyBeanById(int id, String datasetId) {
        boolean notDone = true;
        ArrayList<ExtractPropertyBean> epBeans = findExtractProperties();
        ExtractPropertyBean returnBean = null;
        for (ExtractPropertyBean epbean : epBeans) {

            if (epbean.getId() == id) {
                epbean.setDatasetId(datasetId);
                notDone = false;
                // returnBean = epbean;
                return epbean;
            }

        }
        return returnBean;
    }

    public Properties getDataInfo() {
        return DATAINFO;
    }

    // Pradnya G code added by Jamuna
    public static String getWebAppName(String servletCtxRealPath) {
        String webAppName = null;
        if (null != servletCtxRealPath) {
            String[] tokens = servletCtxRealPath.split("/");
            webAppName = tokens[(tokens.length - 3)].trim();
        }
        return webAppName;
    }
    public Properties getDATAINFO(){
        return DATAINFO;
    }

    @Override
    public void afterPropertiesSet() {
        try {
            this.resourceLoader = new DefaultResourceLoader();
            webapp = getWebAppName(resourceLoader.getResource("/").getURI().getPath());
            loadAllProperties();
            String dbName = DATAINFO.getProperty("dbType");
            setDataInfoProperties();
            tenantSchema.set(DATAINFO.getProperty("schema"));
            DB_NAME = dbName;
            SQLFactory factory = SQLFactory.getInstance();
            factory.run(dbName, resourceLoader);
            setODM_MAPPING_DIR();
            if (EXTRACTINFO != null) {
                copyBaseToDest(resourceLoader);
                copyODMMappingXMLtoResources(resourceLoader);
                extractProperties = findExtractProperties();
                // JN: this is in for junits to run without extract props
                copyImportRulesFiles();
                // copyConfig();
            }
            extractKeyCloakConfig();
        } catch (OpenClinicaSystemException e) {
            logger.debug(e.getMessage());
            logger.debug(e.toString());
            throw new OpenClinicaSystemException(e.getMessage(), e.fillInStackTrace());
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public static void loadAllProperties() {
        if (DATAINFO == null)
            DATAINFO = loadProperties(DATA_INFO_FILE_NAME);
        if (EXTRACTINFO == null)
            EXTRACTINFO = loadProperties(EXTRACT_INFO_FILE_NAME);
    }

    public static Properties loadProperties(String fileProps) {
        Properties internalProp = null;
        InputStream inpStream;
        Properties externalProp = null;
        try {
            inpStream = CoreResources.class.getClassLoader().getResourceAsStream(fileProps);
            internalProp = extractPropertiesFromFile(inpStream);
            InputStream externalInpStream = new FileInputStream(EXTERNAL_PROPERTY_DIRECTORY + fileProps);
            externalProp = extractPropertiesFromFile(externalInpStream);
            overwriteExternalPropOnInternalProp(internalProp, externalProp);
            inpStream.close();
            externalInpStream.close();
            return internalProp;
        } catch (Exception e) {
            logger.warn("Failing to load the properties {}", e.getMessage());
        }
        return internalProp;
    }

    private static Properties extractPropertiesFromFile(InputStream input) {
        Properties prop = new Properties();

        try {
            prop.load(input);
        } catch (IOException ioe) {
            prop = null;
        }
        return prop;
    }
}