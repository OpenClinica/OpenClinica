package org.akaza.openclinica.dao.core;

import org.akaza.openclinica.bean.extract.ExtractPropertyBean;
import org.akaza.openclinica.bean.service.PdfProcessingFunction;
import org.akaza.openclinica.bean.service.SasProcessingFunction;
import org.akaza.openclinica.bean.service.SqlProcessingFunction;
import org.akaza.openclinica.exception.OpenClinicaSystemException;
import org.aspectj.weaver.patterns.ThisOrTargetAnnotationPointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.context.ResourceLoaderAware;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Properties;
import java.util.StringTokenizer;

import javax.servlet.ServletContext;

public class CoreResources implements ResourceLoaderAware   {

    private ResourceLoader resourceLoader;
    public static String PROPERTIES_DIR;
    private static String DB_NAME;
    private static Properties DATAINFO;
    private static Properties EXTRACTINFO;

    private Properties dataInfo;
    private Properties extractInfo;

    private static String webapp;
    protected final static  Logger logger = LoggerFactory.getLogger("org.akaza.openclinica.dao.core.CoreResources");
    // private MessageSource messageSource;
    private static ArrayList<ExtractPropertyBean> extractProperties;
    
//TODO:Clean up all system outs
    //default no arg constructor
    public CoreResources()
    {
    	
    }
   /**
    * TODO: Delete me! 
    * @param dataInfoProps
    * @throws IOException
    */
    public CoreResources(Properties dataInfoProps) throws IOException
    {
    	this.dataInfo = dataInfoProps;
    	if(resourceLoader==null)
    		resourceLoader = new DefaultResourceLoader();
    	webapp = getWebAppName(resourceLoader.getResource("/").getURI().getPath());
    	
    }
    
    
    public void setResourceLoader(ResourceLoader resourceLoader)  {
        this.resourceLoader = resourceLoader;
        try {
            // setPROPERTIES_DIR();
        	webapp = getWebAppName(resourceLoader.getResource("/").getURI().getPath());
        	logMe("is web app name null?"+webapp);
        	
        	
            String dbName = dataInfo.getProperty("dbType");
           
            DATAINFO = dataInfo;
        dataInfo =  setDataInfoProperties();//weird, but there are references to dataInfo...MainMenuServlet  for instance
          //  setDataInfoPath();
            EXTRACTINFO = extractInfo;

            DB_NAME = dbName;
            SQLFactory factory = SQLFactory.getInstance();
            factory.run(dbName, resourceLoader);
            copyBaseToDest(resourceLoader);
            extractProperties = findExtractProperties();
           
        } catch (OpenClinicaSystemException e) {
        	logger.debug(e.getMessage());
        	logger.debug(e.toString());
             throw new OpenClinicaSystemException(e.getMessage(), e.fillInStackTrace());  
        } catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }

   /**
    * For changing values which are applicable to all properties, for ex webapp name can be used in any properties 
    */
       private void setDataInfoVals() {
	
    	Enumeration<String> properties =  (Enumeration<String>) DATAINFO.propertyNames();
    	String vals,key;
    	while(properties.hasMoreElements()){
		key = properties.nextElement();
    		vals = DATAINFO.getProperty(key);
    		//replacePaths(vals);
    		logMe("key:"+key+"vals:"+vals);
    		vals = replaceWebapp(vals);
    		vals = replaceCatHome(vals);
    		logMe("key:"+key+"vals:"+vals);
    		DATAINFO.setProperty(key, vals);
    	}
    	
	}
    private static String replaceWebapp(String value)
    {
    	logMe(value);
    	
    	if(value.contains("${WEBAPP}")){
    		value = value.replace("${WEBAPP}",webapp );
    	}
   
    	else if(value.contains("${WEBAPP.lower}")){
    		value = value.replace("${WEBAPP.lower}", webapp.toLowerCase());
    	  }
    	if(value.contains("$WEBAPP.lower")){
    		value = value.replace("$WEBAPP.lower",webapp.toLowerCase() );
    	}
    	else if (value.contains("$WEBAPP")){
    		value = value.replace("$WEBAPP",webapp );
    	}
    	
    	return value;
    }
    private static String replaceCatHome(String value)
    {
    	logMe(value);
    	
    	if(value.contains("${catalina.home}")){
    		value = value.replace("${catalina.home}",System.getenv("CATALINA_HOME") );
    	}
   
    	
    	if(value.contains("$catalina.home")){
    		value = value.replace("$catalina.home",System.getenv("CATALINA_HOME") );
    	}
    	
    	return value;
    }
  

    private static String replacePaths(String vals)
    {
    	if(vals!=null){
		if(vals.contains("/"))
		{
		vals =	vals.replace("/", File.separator);
		}
		else if (vals.contains("\\")){
		vals = 	vals.replace("\\", File.separator);
		}
		else if(vals.contains("\\\\")){
		vals = 	vals.replace("\\\\", File.separator);
		}
	}
    return vals;	
    }
	private Properties setDataInfoProperties() {
   	String filePath = DATAINFO.getProperty("filePath");
		if(filePath==null|| filePath.isEmpty()) filePath="$catalina.home/$WEBAPP.lower.data";
    	String database = DATAINFO.getProperty("dbType");
		
    	setDatabaseProperties(database);
    	
    	logMe("DataInfo..."+DATAINFO);
    	logMe("filePath = "+filePath);
    	 	setDataInfoVals();
    	
    	DATAINFO.setProperty("changeLogFile", "src/main/resources/migration/master.xml");
    	//sysURL.base 
    	String sysURLBase  = DATAINFO.getProperty("sysURL").replace("MainMenu","");
    	DATAINFO.setProperty("sysURL.base", sysURLBase);
    	
    	
    	
    	if(DATAINFO.getProperty("org.quartz.jobStore.misfireThreshold")==null)
    	DATAINFO.setProperty("org.quartz.jobStore.misfireThreshold", "60000");
    	DATAINFO.setProperty("org.quartz.jobStore.class","org.quartz.impl.jdbcjobstore.JobStoreTX");
   
    	if(database.equalsIgnoreCase("oracle"))
    	{
    		DATAINFO.setProperty("org.quartz.jobStore.driverDelegateClass","org.quartz.impl.jdbcjobstore.oracle.OracleDelegate");
    	}
    	else if(database.equalsIgnoreCase("postgres"))
    	{
    		DATAINFO.setProperty("org.quartz.jobStore.driverDelegateClass","org.quartz.impl.jdbcjobstore.PostgreSQLDelegate");
    	}
    		
    	
    	
     	DATAINFO.setProperty("org.quartz.jobStore.useProperties", "false");
     	DATAINFO.setProperty("org.quartz.jobStore.tablePrefix", "oc_qrtz_");
     	if(DATAINFO.getProperty("org.quartz.threadPool.threadCount")==null)
     	DATAINFO.setProperty("org.quartz.threadPool.threadCount","1");
     	if(DATAINFO.getProperty("org.quartz.threadPool.threadPriority")==null)
     	DATAINFO.setProperty("org.quartz.threadPool.threadPriority", "5");
     	
    	
     	String attached_file_location= DATAINFO.getProperty("attached_file_location");
     	if(attached_file_location == null || attached_file_location.isEmpty())
     		{
     			attached_file_location = DATAINFO.getProperty(filePath)+File.separator+"attached_files";
     			DATAINFO.setProperty("attached_file_location",attached_file_location);
     		}
     	
     	String change_passwd_required = DATAINFO.getProperty("change_passwd_required");
     	if(change_passwd_required==null || change_passwd_required.isEmpty())
     		{
     			change_passwd_required = "1";
     			DATAINFO.setProperty("change_passwd_required",change_passwd_required);
     			
     		}
     	setMailProps();
     	if(DATAINFO.getProperty("crfFileExtensions")!=null)
     	DATAINFO.setProperty("crf_file_extensions",DATAINFO.getProperty("crfFileExtensions"));
     	if(DATAINFO.getProperty("crfFileExtensionSettings")!=null)
     	DATAINFO.setProperty("crf_file_extension_settings",DATAINFO.getProperty("crfFileExtensionSettings"));
     	
     	String dataset_file_delete = DATAINFO.getProperty("dataset_file_delete");
     	if(dataset_file_delete==null)
     		DATAINFO.setProperty("dataset_file_delete", "true");;//TODO:Revisit me!
     	String password_expiration_time = DATAINFO.getProperty("passwdExpirationTime");
     	if(password_expiration_time!=null)
     	DATAINFO.setProperty("passwd_expiration_time",password_expiration_time);
     	
     	if(DATAINFO.getProperty("maxInactiveInterval")!=null)
     	DATAINFO.setProperty("max_inactive_interval",DATAINFO.getProperty("maxInactiveInterval"));
     	
     	DATAINFO.setProperty("ra", "Data_Entry_Person");
     	DATAINFO.setProperty("investigator","Investigator");
     	DATAINFO.setProperty("director","Study_Director");
     	
     	DATAINFO.setProperty("coordinator","Study_Coordinator");
     	DATAINFO.setProperty("monitor","Monitor");
     	DATAINFO.setProperty("ccts.waitBeforeCommit","6000");
     	
     	
     	String rss_url = DATAINFO.getProperty("rssUrl");
     	if(rss_url==null ||rss_url.isEmpty())
     		rss_url = "http://clinicalresearch.wordpress.com/feed/";
     	DATAINFO.setProperty("rss.url", rss_url);
     	String rss_more = DATAINFO.getProperty("rssMore");
     	if(rss_more==null || rss_more.isEmpty())
     		rss_more = "http://clinicalresearch.wordpress.com/";
     	DATAINFO.setProperty("rss.more",rss_more);
     	
     	String supportURL = DATAINFO.getProperty("supportURL");
     	if(supportURL==null||supportURL.isEmpty())
     		supportURL = "http://www.openclinica.org/OpenClinica/3.0/support/";
     	DATAINFO.setProperty("supportURL", supportURL);
     	
     	DATAINFO.setProperty("show_unique_id","1");
     	
     	DATAINFO.setProperty("auth_mode","password");
     	if(DATAINFO.getProperty("userAccountNotification")!=null)
     	DATAINFO.setProperty("user_account_notification", DATAINFO.getProperty("userAccountNotification"));
     	logger.debug("DataInfo..."+DATAINFO);
		return DATAINFO;
	}

	private void setMailProps() {
	
		
		DATAINFO.setProperty("mail.host",DATAINFO.getProperty("mailHost"));
		DATAINFO.setProperty("mail.port",DATAINFO.getProperty("mailPort"));
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

	private void setDatabaseProperties(String database) {

	DATAINFO.setProperty("username", DATAINFO.getProperty("dbUser"));
	DATAINFO.setProperty("password", DATAINFO.getProperty("dbPass"));
	String url = null,driver = null,hibernateDialect = null;
		if(database.equalsIgnoreCase("postgres"))
		{
			url="jdbc:postgresql:"+"//"+DATAINFO.getProperty("dbHost")+":"+DATAINFO.getProperty("dbPort")+"/"+DATAINFO.getProperty("db");
			driver = "org.postgresql.Driver";
			hibernateDialect = "org.hibernate.dialect.PostgreSQLDialect"; 
		}
		else if(database.equalsIgnoreCase("oracle"))
		{
			url="jdbc:oracle:thin:"+"@"+DATAINFO.getProperty("dbHost")+":"+DATAINFO.getProperty("dbPort")+":"+DATAINFO.getProperty("db");
			driver= "oracle.jdbc.driver.OracleDriver";
			hibernateDialect = "org.hibernate.dialect.OracleDialect";
		}
		DATAINFO.setProperty("dataBase",database);
		DATAINFO.setProperty("url", url);
		DATAINFO.setProperty("hibernate.dialect", hibernateDialect);
		DATAINFO.setProperty("driver", driver);
		
	}


	private void copyBaseToDest(ResourceLoader resourceLoader)  {
    //	System.out.println("Properties directory?"+resourceLoader.getResource("properties/xslt"));
    
    	ByteArrayInputStream listSrcFiles[] = new ByteArrayInputStream[10];
    	String[] fileNames =  {"odm_spss_dat.xsl","ODMToTAB.xsl","odm_to_html.xsl","odm_to_xslfo.xsl","ODM-XSLFO-Stylesheet.xsl","odm_spss_sps.xsl","copyXML.xsl","odm1.3_to_1.2.xsl","odm1.3_to_1.2_extensions.xsl"};
    	try{
    listSrcFiles[0] = (ByteArrayInputStream) resourceLoader.getResource("classpath:properties"+File.separator+"xslt"+File.separator+fileNames[0]).getInputStream();
    listSrcFiles[1] = (ByteArrayInputStream)resourceLoader.getResource("classpath:properties"+File.separator+"xslt"+File.separator+fileNames[1]).getInputStream();
    listSrcFiles[2] = (ByteArrayInputStream)resourceLoader.getResource("classpath:properties"+File.separator+"xslt"+File.separator+fileNames[2]).getInputStream();
    listSrcFiles[3] = (ByteArrayInputStream)resourceLoader.getResource("classpath:properties"+File.separator+"xslt"+File.separator+fileNames[3]).getInputStream();
    listSrcFiles[4] = (ByteArrayInputStream)resourceLoader.getResource("classpath:properties"+File.separator+"xslt"+File.separator+fileNames[4]).getInputStream();
    listSrcFiles[5] = (ByteArrayInputStream)resourceLoader.getResource("classpath:properties"+File.separator+"xslt"+File.separator+fileNames[5]).getInputStream();
    listSrcFiles[6] = (ByteArrayInputStream)resourceLoader.getResource("classpath:properties"+File.separator+"xslt"+File.separator+fileNames[6]).getInputStream();
    listSrcFiles[7] = (ByteArrayInputStream)resourceLoader.getResource("classpath:properties"+File.separator+"xslt"+File.separator+fileNames[7]).getInputStream();
    listSrcFiles[8] = (ByteArrayInputStream)resourceLoader.getResource("classpath:properties"+File.separator+"xslt"+File.separator+fileNames[8]).getInputStream();
    

    	}catch(IOException ioe){
    		OpenClinicaSystemException oe = new OpenClinicaSystemException("Unable to read source files");
    		oe.initCause(ioe);
    		oe.setStackTrace(ioe.getStackTrace());
    		logger.debug(ioe.getMessage());
    		throw oe;
    	}
	File dest = new File(getField("filePath")+"xslt");
		if(!dest.exists()){
			if(!dest.mkdirs()){
				throw new OpenClinicaSystemException("Copying files, Could not create direcotry: " + dest.getAbsolutePath() + ".");
			}
		}

                for (int i = 0; i < fileNames.length; i++)
                {
                        File dest1 = new File(dest,fileNames[i]);
                      //  File src1 = listSrcFiles[i];
                        if(listSrcFiles[i]!=null)
                        copyFiles(listSrcFiles[i],dest1);
                }
	
		
	}

    private void copyFiles(ByteArrayInputStream fis,File dest){
    	FileOutputStream fos = null;
    	byte[] buffer = new byte[512]; //Buffer 4K at a time (you can change this).
    	int bytesRead;
    	logger.debug("fis?"+fis);
    	try{
    		fos = new FileOutputStream(dest);
    		 while (( bytesRead = fis.read(buffer)) >= 0) {
    			                                 fos.write(buffer,0,bytesRead);
    			                         }
    	}catch(IOException ioe){//error while copying files
    		OpenClinicaSystemException oe = new OpenClinicaSystemException("Unable to copy file: " +  fis + "to" + dest.getAbsolutePath()+"."  + dest.getAbsolutePath() + ".");
    		oe.initCause(ioe);
    		oe.setStackTrace(ioe.getStackTrace());
    		throw oe;
    	}
    	finally { //Ensure that the files are closed (if they were open).
    		                        if (fis != null) { try {
										fis.close();
									} catch (IOException ioe) {
										OpenClinicaSystemException oe = new OpenClinicaSystemException("Unable to copy file: " + fis  + "to" + dest.getAbsolutePath()+"."  + dest.getAbsolutePath() + ".");
							    		oe.initCause(ioe);
							    		oe.setStackTrace(ioe.getStackTrace());
							    		logger.debug(ioe.getMessage());
							    		throw oe;
										
									} }
    		                    if (fos != null) { try {
									fos.close();
								} catch (IOException ioe) {
									OpenClinicaSystemException oe = new OpenClinicaSystemException("Unable to copy file: " + fis + "to" + dest.getAbsolutePath()+"."  + dest.getAbsolutePath() + ".");
						    		oe.initCause(ioe);
						    		oe.setStackTrace(ioe.getStackTrace());
						    		logger.debug(ioe.getMessage());
						    		throw oe;
									
								} }
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

    private ArrayList<ExtractPropertyBean> findExtractProperties() throws OpenClinicaSystemException{
        ArrayList<ExtractPropertyBean> ret = new ArrayList<ExtractPropertyBean>();
        
        // ExtractPropertyBean epbean = new ExtractPropertyBean();
        int i = 1;
        while (!getExtractField("extract." + i+".file").equals("")) {
            ExtractPropertyBean epbean = new ExtractPropertyBean();
            epbean.setId(i);
            // we will implement a find by id function in the front end
            
            //check to make sure the file exists, if not throw an exception and system will abort to start.
            checkForFile(getExtractFields("extract." + i+".file"));
            epbean.setFileName(getExtractFields("extract." + i+".file"));
            // file name of the xslt stylesheet
            epbean.setFiledescription(getExtractField("extract." + i+".fileDescription"));
            // description of the choice of format
            epbean.setHelpText(getExtractField("extract." + i+".helptext"));
            // help text, currently in the alt-text of the link
            epbean.setLinkText(getExtractField("extract." + i+".linkText"));
            // link text of the choice of format
            // epbean.setRolesAllowed(getExtractField("xsl.allowed." + i).split(","));
            // which roles are allowed to see the choice?
            epbean.setFileLocation(getExtractField("extract." + i+".location"));
            // destination of the copied files
            // epbean.setFormat(getExtractField("xsl.format." + i));
            // if (("").equals(epbean.getFormat())) {
            // }
            // formatting choice. currently permenantly set at oc1.3
            String clinica =    getExtractField("extract."+i+".odmType");
            if(clinica!=null)
            {
             
                if(clinica.equalsIgnoreCase("clinical_data"))
                    epbean.setFormat("clinical_data");
                else epbean.setFormat("oc1.3");
            }
            else
            epbean.setFormat("oc1.3");
            
            // destination file name of the copied files
            epbean.setExportFileName(getExtractFields("extract." + i+".exportname"));
            // post-processing event after the creation
            // System.out.println("found post function: " + whichFunction);
            String whichFunction = getExtractField("extract."+i+".post").toLowerCase();
            //added by JN: Zipformat comes from extract properties returns true by default
            epbean.setZipFormat(getExtractFieldBoolean("extract."+i+".zip"));
            epbean.setDeleteOld(getExtractFieldBoolean("extract."+i+".deleteOld"));
            epbean.setSuccessMessage(getExtractField("extract."+i+".success"));
            epbean.setFailureMessage(getExtractField("extract."+i+".failure"));
            epbean.setZipName(getExtractField("extract."+i+".zipName"));
            if(epbean.getFileName().length!=epbean.getExportFileName().length)
            	throw new OpenClinicaSystemException("The comma seperated values of file names and export file names should correspond 1 on 1 for the property number"+i);
            
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
            }else if(!whichFunction.isEmpty()){
            	String postProcessorName = getExtractField(whichFunction+".postProcessor");
            	if(postProcessorName.equals("pdf")){
            		epbean.setPostProcessing(new PdfProcessingFunction());
            		epbean.setPostProcDeleteOld(getExtractFieldBoolean(whichFunction+".deleteOld"));
            		epbean.setPostProcZip(getExtractFieldBoolean(whichFunction+".zip"));
            		epbean.setPostProcLocation(getExtractField(whichFunction+".location"));
            		epbean.setPostProcExportName(getExtractField(whichFunction+".exportname"));
            	}
            	//since the database is the last option TODO: think about custom post processing options
            	else {
            		  SqlProcessingFunction function = new SqlProcessingFunction(epbean);
            		  
                    function.setDatabaseType(getExtractFieldNoRep(whichFunction + ".dataBase").toLowerCase());
                    function.setDatabaseUrl(getExtractFieldNoRep(whichFunction + ".url"));
                    function.setDatabaseUsername(getExtractFieldNoRep(whichFunction + ".username"));
                    function.setDatabasePassword(getExtractFieldNoRep(whichFunction + ".password"));
                    epbean.setPostProcessing(function);
            	}
            	 
            }
            else {
                // add a null here
                epbean.setPostProcessing(null);
            }
            ret.add(epbean);
            i++;
        }

        System.out.println("found " + ret.size() + " records in extract.properties");
        return ret;
    }

    private String getExtractFieldNoRep(String key) {
    	 String value = EXTRACTINFO.getProperty(key);
         if (value != null) {
             value = value.trim();
         }
       
         return value == null ? "" : value;
	}

	private void checkForFile(String[] extractFields) throws OpenClinicaSystemException{
	
		int cnt = extractFields.length;
		int i = 0;
		//iterate through all comma separated file names
		while(i<cnt){
			
				File f = new File(getField("filePath") + "xslt" + File.separator +extractFields[i]);		
				System.out.println(getField("filePath") + "xslt" + File.separator +extractFields[i]);
				if(!f.exists()) throw new OpenClinicaSystemException("FileNotFound -- Please make sure"+ extractFields[i]+ "exists");

	
			i++;
			
		}
		
	}

	public InputStream getInputStream(String fileName) throws IOException {
        return resourceLoader.getResource("classpath:properties/" + fileName).getInputStream();
    }

    public URL getURL(String fileName) throws IOException {
        return resourceLoader.getResource("classpath:properties/" + fileName).getURL();
    }

    public File getFile(String fileName) {
        try {
            InputStream inputStream = getInputStream(fileName);
            File f = new File(fileName);
            OutputStream outputStream = new FileOutputStream(f);
            byte buf[] = new byte[1024];
            int len;
            try {
                while ((len = inputStream.read(buf)) > 0)
                    outputStream.write(buf, 0, len);
            } finally {
                outputStream.close();
                inputStream.close();
            }
            return f;

        } catch (IOException e) {
            throw new OpenClinicaSystemException(e.getMessage(), e.fillInStackTrace());
        }
    }

    public void setPROPERTIES_DIR() {
        String resource = "classpath:properties/placeholder.properties";
        System.out.println("Resource" + resource);
        Resource scr = resourceLoader.getResource(resource);
        String absolutePath = null;
        try {
            // System.out.println("Resource" + resource);
            absolutePath = scr.getFile().getAbsolutePath();
            // System.out.println("Resource" + ((ClassPathResource) scr).getPath());
            // System.out.println("Resource" + resource);
            PROPERTIES_DIR = absolutePath.replaceAll("placeholder.properties", "");
            System.out.println("Resource" + PROPERTIES_DIR);
        } catch (IOException e) {
            throw new OpenClinicaSystemException(e.getMessage(), e.fillInStackTrace());
        }

    }

    public static String getDBName() {
        return DB_NAME;
    }

    public static String getField(String key) {
        String value = DATAINFO.getProperty(key);
        if (value != null) {
            value = value.trim();
        }
        return value == null ? "" : value;

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

    //JN:The following method returns default of true when converting from string
    public static boolean getExtractFieldBoolean(String key) {
        String value = EXTRACTINFO.getProperty(key);
        if (value != null) {
            value = value.trim();
        }
        if(value==null)
        	return true;//Defaulting to true
        if(value.equalsIgnoreCase("false"))
        return false;
        else
        	return true;//defaulting to true
        
    }
    public static String[] getExtractFields(String key) {
        String value = EXTRACTINFO.getProperty(key);
        
        System.out.println("key?"+key+"value = "+value);
       
        if (value != null) {
            value = value.trim();
        }
        return value.split(",");
    }
 //JN:  by using static when u click same export link from 2 different datasets the first one stays in tact and is saved in there.
    
    /**
     * 
     */
    public  ExtractPropertyBean findExtractPropertyBeanById(int id,String datasetId) {
    	boolean notDone=true;
    	ArrayList<ExtractPropertyBean> epBeans = findExtractProperties();
    	ExtractPropertyBean returnBean = null;
        for ( ExtractPropertyBean  epbean : epBeans) {
        
        	
        		if (epbean.getId() == id) {
        			epbean.setDatasetId(datasetId);
        			notDone=false;
        			//returnBean = epbean;
        			return epbean;
            	}
        	        
        }
        return returnBean;
    }

    public Properties getDataInfo() {
        return DATAINFO;
    }

    public void setDataInfo(Properties dataInfo) {
        this.dataInfo = dataInfo;
    }

    public Properties getExtractInfo() {
        return extractInfo;
    }

    public void setExtractInfo(Properties extractInfo) {
        this.extractInfo = extractInfo;
    }
  
    
    
    //Pradnya G code added by Jamuna
    public String getWebAppName(String servletCtxRealPath) {
        String webAppName = null;
        if (null != servletCtxRealPath) {
            String[] tokens = servletCtxRealPath.split("/");
            webAppName = tokens[(tokens.length - 1)].trim();
        }
        return webAppName;
    }
    
 //TODO comment out system out after dev
    private static void  logMe(String message){
    	//System.out.println(message);
    	logger.debug(message);
    }
    
    
}
