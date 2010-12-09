package org.akaza.openclinica.dao.core;

import org.akaza.openclinica.bean.extract.ExtractPropertyBean;
import org.akaza.openclinica.bean.service.PdfProcessingFunction;
import org.akaza.openclinica.bean.service.SasProcessingFunction;
import org.akaza.openclinica.bean.service.SqlProcessingFunction;
import org.akaza.openclinica.exception.OpenClinicaSystemException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.context.ResourceLoaderAware;
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
    protected final static Logger logger = LoggerFactory.getLogger("CoreResources");
    // private MessageSource messageSource;
    private static ArrayList<ExtractPropertyBean> extractProperties;
//TODO:Clean up all system outs
    //default no arg constructor
    public CoreResources()
    {
    	
    }
     
    public void setResourceLoader(ResourceLoader resourceLoader)  {
        this.resourceLoader = resourceLoader;
        try {
            // setPROPERTIES_DIR();
        	webapp = getWebAppName(resourceLoader.getResource("/").getURI().getPath());
        
            String dbName = dataInfo.getProperty("dataBase");
           
            DATAINFO = dataInfo;
            setDataInfoProperties();
          //  setDataInfoPath();
            //JN:TODO undo-comment after the datainfo part is done. 
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

    
    //For future
    private void setDataInfoPath() {
	
    	Enumeration<String> properties =  (Enumeration<String>) DATAINFO.propertyNames();
    	String vals,key;
    	while(properties.hasMoreElements()){
		key = properties.nextElement();
    		vals = DATAINFO.getProperty(key);
    		replacePaths(vals);
    	
    	DATAINFO.setProperty(key, vals);
    	}
    	
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
		String database = DATAINFO.getProperty("dataBase");
    	logger.debug("DataInfo..."+DATAINFO);
    	if(DATAINFO.getProperty("filePath").contains("${catalina.home}"))
		{
			
			filePath = filePath.replace("${catalina.home}", System.getenv("catalina.home"));
			DATAINFO.setProperty("filePath", filePath);
		}
    	if(DATAINFO.getProperty("filePath").contains("${WEBAPP}"))
		{
			
			filePath = filePath.replace("${WEBAPP}", webapp);
			DATAINFO.setProperty("filePath", filePath);
		}
    	else if(DATAINFO.getProperty("filePath").contains("${WEBAPP.lower}")){
    		filePath = filePath.replace("${WEBAPP.lower}", webapp.toLowerCase());
    		DATAINFO.setProperty("filePath", filePath);
    	}
    	
    	filePath = replacePaths(DATAINFO.getProperty("filePath"));
    	DATAINFO.setProperty("filePath", filePath);
    	
    	String sysURL = DATAINFO.getProperty("sysURL");

    	if(sysURL.contains("${WEBAPP}"))
		{
			
			sysURL = sysURL.replace("${WEBAPP}", webapp);
			DATAINFO.setProperty("sysURL", sysURL);
		}
    	
    	
    	//sysURL.base 
    	String sysURLBase  = DATAINFO.getProperty("sysURL").replace("/MainMenu","");
    	DATAINFO.setProperty("sysURL.base", sysURLBase);
    	
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
     	DATAINFO.setProperty("org.quartz.threadPool.threadCount","1");
     	DATAINFO.setProperty("org.quartz.threadPool.threadPriority", "5");
     	
    	logger.debug("DataInfo..."+DATAINFO);
		return DATAINFO;
	}

	private void replaceVars(String var){
		
		
	}
	private void copyBaseToDest(ResourceLoader resourceLoader)  {
    //	System.out.println("Properties directory?"+resourceLoader.getResource("properties/xslt"));
    
    	ByteArrayInputStream listSrcFiles[] = new ByteArrayInputStream[10];
    	String[] fileNames =  {"odm_spss_dat.xsl","ODMToTAB.xsl","odm_to_html.xsl","odm_to_xslfo.xsl","ODMToCSV.xsl","ODM-XSLFO-Stylesheet.xsl","odm_spss_sps.xsl","copyXML.xsl"};
    	try{
    listSrcFiles[0] = (ByteArrayInputStream) resourceLoader.getResource("classpath:properties"+File.separator+"xslt"+File.separator+fileNames[0]).getInputStream();
    listSrcFiles[1] = (ByteArrayInputStream)resourceLoader.getResource("classpath:properties"+File.separator+"xslt"+File.separator+fileNames[1]).getInputStream();
    listSrcFiles[2] = (ByteArrayInputStream)resourceLoader.getResource("classpath:properties"+File.separator+"xslt"+File.separator+fileNames[2]).getInputStream();
    listSrcFiles[3] = (ByteArrayInputStream)resourceLoader.getResource("classpath:properties"+File.separator+"xslt"+File.separator+fileNames[3]).getInputStream();
    listSrcFiles[4] = (ByteArrayInputStream)resourceLoader.getResource("classpath:properties"+File.separator+"xslt"+File.separator+fileNames[4]).getInputStream();
    listSrcFiles[5] = (ByteArrayInputStream)resourceLoader.getResource("classpath:properties"+File.separator+"xslt"+File.separator+fileNames[5]).getInputStream();
    listSrcFiles[6] = (ByteArrayInputStream)resourceLoader.getResource("classpath:properties"+File.separator+"xslt"+File.separator+fileNames[6]).getInputStream();
    listSrcFiles[7] = (ByteArrayInputStream)resourceLoader.getResource("classpath:properties"+File.separator+"xslt"+File.separator+fileNames[7]).getInputStream();

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
        logger.debug("key??"+key+"value = "+value);
        if (value != null) {
            value = value.trim();
        }
        return value.split(",");
    }
    public static ExtractPropertyBean findExtractPropertyBeanById(int id) {
        for (ExtractPropertyBean epbean : extractProperties) {
            if (epbean.getId() == id) {
                return epbean;
            }
        }
        return null;
    }

    public Properties getDataInfo() {
        return dataInfo;
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
    
    //getters and setters for web application context name
//    public void setWebAppContextName(ServletContext webAppContext)
//    {
//    	logger.debug("Web application context"+webAppContext);
//    	System.out.println("Web application context"+webAppContext);
//    	this.webAppContext = webAppContext;
//    }
//    public ServletContext getWebAppContextName(){
//    	return webAppContext;
//    }

}
