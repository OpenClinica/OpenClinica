package org.akaza.openclinica.controller;

import org.akaza.openclinica.bean.extract.ExtractPropertyBean;
import org.akaza.openclinica.bean.login.UserAccountBean;
import org.akaza.openclinica.controller.healthcheck.DatabaseHealthCheck;
import org.akaza.openclinica.dao.core.CoreResources;
import org.akaza.openclinica.dao.login.UserAccountDAO;
import org.akaza.openclinica.i18n.util.ResourceBundleProvider;
import org.akaza.openclinica.service.JobTriggerService;
import org.akaza.openclinica.service.rule.RuleSetService;
import org.akaza.openclinica.service.rule.expression.ExpressionService;
import org.apache.commons.dbcp.BasicDataSource;
import org.apache.commons.io.FileSystemUtils;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import com.codahale.metrics.ConsoleReporter;
import com.codahale.metrics.Meter;
import com.codahale.metrics.Metric;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.health.HealthCheck;
import com.codahale.metrics.health.HealthCheckRegistry;
import com.codahale.metrics.jvm.BufferPoolMetricSet;
import com.codahale.metrics.jvm.FileDescriptorRatioGauge;
import com.codahale.metrics.jvm.GarbageCollectorMetricSet;
import com.codahale.metrics.jvm.MemoryUsageGaugeSet;
import com.codahale.metrics.jvm.ThreadStatesGaugeSet;

import javax.management.ObjectName;
import javax.servlet.ServletContext;

import java.io.File;
import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;
import java.sql.DatabaseMetaData;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Map.Entry;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

@Controller
@RequestMapping(value = "/healthcheck")
@ResponseStatus(value = org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR)
public class ReportController {

	// Add in Spring Cor files /healthcheck path to avoid firewall
	@Autowired
	@Qualifier("dataSource")
	private BasicDataSource dataSource;

	@Autowired
	ExpressionService expressionService;

	@Autowired
	RuleSetService ruleSetService;

	protected final Logger logger = LoggerFactory.getLogger(getClass().getName());
    
    
	
	
	
	
	/**
	 * @api {post} /pages/healthcheck/runonschedule Evaluate runOnSchedule behavior
	 * @apiName ruleTrigger
	 * @apiPermission admin
	 * @apiVersion 3.8.0
	 * @apiParam {String} serverZoneId Server TimeZone.
	 * @apiParam {String} ssZoneId Study Subject TimeZone .
	 * @apiParam {String} runTime Scheduled Run Time .
	 * @apiParam {String} serverTime Server Time .
	 * @apiGroup Rule
	 * @apiDescription Evaluate runOnSchedule behavior taking into consideration different time zones the subject and the server could be on.
	 * @apiParamExample {json} Request-Example:
	 *
	 *                  {
	 *                  "serverZoneId" :"America/New_York",
	 *                  "ssZoneId" :"America/New_York",
	 *                  "runTime" :"11",
	 *                  "serverTime" :"12"
	 *                  }
	 * @apiErrorExample {json} Error-Response:
	 *                  HTTP/1.1 400 Bad Request
	 *                  {
	 *                  "result": false
	 *                  }
	 * @apiSuccessExample {json} Success-Response:
	 *                    HTTP/1.1 200 OK
	 *                    {
	 *                    "result": true
	 *                    }
	 */

	@RequestMapping(value = "/runonschedule", method = RequestMethod.POST)
	public ResponseEntity<HashMap> ruleTrigger(@RequestBody HashMap<String, String> hashMap) throws Exception {

		String serverZoneId = hashMap.get("serverZoneId");
		String ssZoneId = hashMap.get("ssZoneId");
		String runTime = hashMap.get("runTime");
		String serverTime = hashMap.get("serverTime");

		HashMap<String, Boolean> map = new HashMap<>();
		ResourceBundleProvider.updateLocale(new Locale("en_US"));
		System.out.println("I'm in rest call");
		Boolean result = ruleSetService.calculateTimezoneDiff(TimeZone.getTimeZone(serverZoneId), TimeZone.getTimeZone(ssZoneId), Integer.valueOf(runTime), Integer.valueOf(serverTime));
		map.put("result", result);

		if (result) {
			return new ResponseEntity<HashMap>(map, org.springframework.http.HttpStatus.OK);
		} else {
			return new ResponseEntity<HashMap>(map, org.springframework.http.HttpStatus.BAD_REQUEST);
		}

	}

	/**
	 * @api {post} /pages/healthcheck/rulecurrentdate Evaluate currentDate behavior
	 * @apiName getSSZone
	 * @apiPermission admin
	 * @apiVersion 3.8.0
	 * @apiParam {String} serverZoneId Server TimeZone.
	 * @apiParam {String} ssZoneId Study Subject TimeZone .
	 * @apiGroup Rule
	 * @apiDescription Evaluate current date taking into consideration different time zones the subject and the server could be on.
	 * @apiParamExample {json} Request-Example:
	 *
	 *                  {
	 *                  "serverZoneId" :"America/New_York",
	 *                  "ssZoneId" :"America/New_York"
	 *                  }
	 * @apiSuccessExample {json} Success-Response:
	 *                    HTTP/1.1 200 OK
	 *                    {
	 *                    "ssDate": "2015-10-07",
	 *                    "serverZoneId": "America/New_York",
	 *                    "serverDate": "2015-10-07"
	 *                    }
	 */

	@RequestMapping(value = "/rulecurrentdate", method = RequestMethod.POST)
	public ResponseEntity<HashMap> getSSZone(@RequestBody HashMap<String, String> hashMap) throws Exception {
		ResourceBundleProvider.updateLocale(new Locale("en_US"));
		String ssZoneId = hashMap.get("ssZoneId");
		String serverZoneId = hashMap.get("serverZoneId");
		System.out.println("I'm in rest call");
		HashMap<String, String> map = expressionService.getSSDate(ssZoneId, serverZoneId);
		return new ResponseEntity<HashMap>(map, org.springframework.http.HttpStatus.OK);

	}

	/**
	 * @api {post} /pages/healthcheck/runtime Retrieve runOnSchedule default runTime
	 * @apiName getRunTime
	 * @apiPermission admin
	 * @apiVersion 3.8.0
	 * @apiGroup Rule
	 * @apiDescription Retrieves the default runOnSchedule runtime for rules. The runOnSchedule when configured, allows you to run rules on a schedule.
	 * @apiSuccessExample {json} Success-Response:
	 *                    HTTP/1.1 200 OK
	 *                    {
	 *                    "result": 20
	 *                    }
	 */

	@RequestMapping(value = "/runtime", method = RequestMethod.POST)
	public ResponseEntity<HashMap> getRunTime() throws Exception {
		ResourceBundleProvider.updateLocale(new Locale("en_US"));
		HashMap<String, Integer> map = new HashMap<>();
		System.out.println("I'm in rest call for RunTime");
		int result = ruleSetService.getRunTimeWhenTimeIsNotSet();
		map.put("result", result);
		return new ResponseEntity<HashMap>(map, org.springframework.http.HttpStatus.OK);

	}


	   @RequestMapping(value = "/systemstatus", method = RequestMethod.POST)
	    public ResponseEntity<HashMap> getSystemStatus() throws Exception {
	        ResourceBundleProvider.updateLocale(new Locale("en_US"));
	        HashMap<String, String> map = new HashMap<>();
	        System.out.println("I'm in rest call for System Status");
	        
/*            map.put("dbType", CoreResources.getField("dbType"));
            map.put("dbUser", CoreResources.getField("dbUser"));
            map.put("dbPass", CoreResources.getField("dbPass"));
            map.put("db", CoreResources.getField("db"));
            map.put("dbPort", CoreResources.getField("dbPort"));
            map.put("dbHost", CoreResources.getField("dbHost"));
*/
	        
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
                System.out.format("%s=%s%n",
                                  envName,
                                  env.get(envName));
            }
            
            DatabaseMetaData metaData = dataSource.getConnection().getMetaData();  
            System.out.println (metaData.getSchemas());
            System.out.println (metaData.getUserName());
            System.out.println (metaData.getCatalogs());

//            metaData.getTablePrivileges("pg_catalog", schemaPattern, tableNamePattern)
            
            
  //          metaData.getTablePrivileges();
    //        metaData.getColumnPrivileges();
            
            
            
	        try {
                UserAccountDAO udao = new UserAccountDAO(dataSource);
                UserAccountBean uBean = (UserAccountBean) udao.findByPK(1);
                
                if(uBean.getFirstName().equals("Root") && uBean.getLastName().equals("User")) {            
                map.put("Root User Account First And Last Name", uBean.getFirstName() + " " + uBean.getLastName());
                map.put ("Database Connection" ,"PASS");
                }else{
                    map.put ("Database Connection" ,"FAIL");
                }
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            
            // Send Email
            //
            
            
            
	        return new ResponseEntity<HashMap>(map, org.springframework.http.HttpStatus.OK);

	    }


       @RequestMapping(value = "/systemstatus1", method = RequestMethod.POST)
       public ResponseEntity<HashMap> getSystemStatus1() throws Exception {
           ResourceBundleProvider.updateLocale(new Locale("en_US"));
           HashMap<String, String> map = new HashMap<>();
        //   System.out.println("I'm in rest call for System Status 1");
        
           HealthCheckRegistry healthChecks = new HealthCheckRegistry();
           healthChecks.register("postgres", new DatabaseHealthCheck(dataSource));
           
         MetricRegistry metricRegistry = new MetricRegistry();  
     //    metricRegistry.register("PROP_METRIC_REG_JVM_MEMORY", new MemoryUsageGaugeSet( ));
     //     metricRegistry.register("PROP_METRIC_REG_JVM_GARBAGE", new GarbageCollectorMetricSet());
     //   metricRegistry.register("PROP_METRIC_REG_JVM_THREADS", new ThreadStatesGaugeSet());
     //    metricRegistry.register("PROP_METRIC_REG_JVM_FILES", new FileDescriptorRatioGauge());
     //    metricRegistry.register("PROP_METRIC_REG_JVM_BUFFERS", new BufferPoolMetricSet(ManagementFactory.getPlatformMBeanServer()));

      //     System.out.println();
         Meter requests = metricRegistry.meter("requests");
         requests.mark();
         
         
           ConsoleReporter reporter = ConsoleReporter.forRegistry(metricRegistry)
                   .convertRatesTo(TimeUnit.SECONDS)
                   .convertDurationsTo(TimeUnit.MILLISECONDS)
                   .build();
               reporter.start(1, TimeUnit.SECONDS);

           
           
           final Map<String, HealthCheck.Result> results = healthChecks.runHealthChecks();
           
           for (Entry<String, HealthCheck.Result> entry : results.entrySet()) {
               if (entry.getValue().isHealthy()) {
                   System.out.println(entry.getKey() + " is healthy");
                   map.put(entry.getKey(), " is healthy");
               } else {
                   System.err.println(entry.getKey() + " is UNHEALTHY: " + entry.getValue().getMessage());
                   map.put(entry.getKey(), " is UNHEALTHY: " + entry.getValue().getMessage());
                   final Throwable e = entry.getValue().getError();
                   if (e != null) {
                       e.printStackTrace();
                   }
               }
           }


           
           return new ResponseEntity<HashMap>(map, org.springframework.http.HttpStatus.OK);

       }


       @RequestMapping(value = "/configprops", method = RequestMethod.GET)
       public ResponseEntity<HashMap> getConfigProps() throws Exception {
           ResourceBundleProvider.updateLocale(new Locale("en_US"));
           HashMap<String, Object> map = new HashMap<>();
           System.out.println("I'm in rest call for ConfigProps");
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
           extractDatamart.put("db1.username", CoreResources.getExtractField("db1.username"));
           extractDatamart.put("db1.url", CoreResources.getExtractField("db1.url"));
           extractDatamart.put("db1.dataBase", CoreResources.getExtractField("db1.dataBase"));

           HashMap<String, String> extractNumber = new HashMap<>();
           extractNumber.put("extract.number", CoreResources.getExtractField("extract.number"));

           extractMap.put("extract.number", extractNumber);
           extractMap.put("DataMart", extractDatamart);

           map.put("extract.properties", extractMap);

           return new ResponseEntity<HashMap>(map, org.springframework.http.HttpStatus.OK);

       }

       @RequestMapping(value = "/health", method = RequestMethod.GET)
       public ResponseEntity<HashMap> getHealth() throws Exception {
           ResourceBundleProvider.updateLocale(new Locale("en_US"));
           HashMap<String, Object> map = new HashMap<>();
           System.out.println("I'm in rest call for Health");
          
           String filePath =  CoreResources.getField("filePath");
           
           float size = FileUtils.sizeOfDirectory(new File(filePath));
           
         //  FileSystemUtils.freeSpace(filePath);
           System.out.println("Folder Size: " + size + " bytes");
           System.out.println("Folder Size: " + size/1024 /1024 + " MegaBytes");
           
           System.out.println("Free Space: " +  (FileSystemUtils.freeSpace(filePath))/1024 /1024 + " MegaBytes");
           
           
           
           File file = new File(filePath);
           long totalSpace = file.getTotalSpace(); //total disk space in bytes.
           long usableSpace = file.getUsableSpace(); ///unallocated / free disk space in bytes.
           long freeSpace = file.getFreeSpace(); //unallocated / free disk space in bytes.
           
           System.out.println(" === Partition Detail ===");
           
           System.out.println(" === bytes ===");
           System.out.println("Total size : " + totalSpace + " bytes");
           System.out.println("Space free : " + usableSpace + " bytes");
           System.out.println("Space free : " + freeSpace + " bytes");
           
           System.out.println(" === mega bytes ===");
           System.out.println("Total size : " + totalSpace /1024 /1024 + " mb");
           System.out.println("Space free : " + usableSpace /1024 /1024 + " mb");
           System.out.println("Space free : " + freeSpace /1024 /1024 + " mb");

           

           return new ResponseEntity<HashMap>(map, org.springframework.http.HttpStatus.OK);

       }

       	
}
