package org.akaza.openclinica.control.submit;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.sql.SQLException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;

import javax.servlet.http.HttpServletRequest;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;


import org.akaza.openclinica.bean.core.DataEntryStage;
import org.akaza.openclinica.bean.core.Role;
import org.akaza.openclinica.bean.core.Status;
import org.akaza.openclinica.bean.login.UserAccountBean;
import org.akaza.openclinica.bean.rule.FileUploadHelper;
import org.akaza.openclinica.bean.rule.XmlSchemaValidationHelper;
import org.akaza.openclinica.bean.submit.CRFVersionBean;
import org.akaza.openclinica.bean.submit.DisplayItemBeanWrapper;
import org.akaza.openclinica.bean.submit.EventCRFBean;
import org.akaza.openclinica.bean.submit.crfdata.ODMContainer;
import org.akaza.openclinica.bean.submit.crfdata.SummaryStatsBean;
import org.akaza.openclinica.control.SpringServletAccess;
import org.akaza.openclinica.control.core.SecureController;
import org.akaza.openclinica.control.form.FormProcessor;
import org.akaza.openclinica.control.form.Validator;
import org.akaza.openclinica.controller.helper.RestfulServiceHelper;
import org.akaza.openclinica.core.SessionManager;
import org.akaza.openclinica.core.form.StringUtil;
import org.akaza.openclinica.dao.core.CoreResources;
import org.akaza.openclinica.exception.OpenClinicaException;
import org.akaza.openclinica.exception.OpenClinicaSystemException;
import org.akaza.openclinica.i18n.core.LocaleResolver;
import org.akaza.openclinica.logic.importdata.ImportDataHelper;
import org.akaza.openclinica.view.Page;
import org.akaza.openclinica.web.InsufficientPermissionException;
import org.akaza.openclinica.web.SQLInitServlet;
import org.akaza.openclinica.web.crfdata.ImportCRFDataService;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.URI;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.exolab.castor.mapping.Mapping;
import org.exolab.castor.xml.Unmarshaller;
import org.json.JSONArray;
import org.json.JSONObject;


public class UploadCRFDataToHttpServerServlet extends SecureController {

    Locale locale;
    static private String importFileDir;

    XmlSchemaValidationHelper schemaValidator = new XmlSchemaValidationHelper();
    FileUploadHelper uploadHelper = new FileUploadHelper();
    RestfulServiceHelper restfulServiceHelper;

    

    /**
     *
     */
    @Override
    public void mayProceed() throws InsufficientPermissionException {
        checkStudyLocked(Page.MENU_SERVLET, respage.getString("current_study_locked"));
        checkStudyFrozen(Page.MENU_SERVLET, respage.getString("current_study_frozen"));

        locale = LocaleResolver.getLocale(request);
        if (ub.isSysAdmin()) {
            return;
        }

        Role r = currentRole.getRole();
        if (r.equals(Role.STUDYDIRECTOR) || r.equals(Role.COORDINATOR) || r.equals(Role.INVESTIGATOR) || r.equals(Role.RESEARCHASSISTANT)
                || r.equals(Role.RESEARCHASSISTANT2)) {
            return;
        }

        addPageMessage(respage.getString("no_have_correct_privilege_current_study") + respage.getString("change_study_contact_sysadmin"));
        throw new InsufficientPermissionException(Page.MENU_SERVLET, resexception.getString("may_not_submit_data"), "1");
    }

    @Override
    public void processRequest() throws Exception {
        resetPanel();
        panel.setStudyInfoShown(false);
        panel.setOrderedData(true);

        FormProcessor fp = new FormProcessor(request);
        // checks which module the requests are from
        String module = fp.getString(MODULE);
        // keep the module in the session
        session.setAttribute(MODULE, module);

        String action = request.getParameter("action");
       
        if (StringUtil.isBlank(action)) {
            logger.info("action is blank");
          
            forwardPage(Page.UPLOAD_CRF_DATA_TO_MIRTH);
        }else if ("confirm".equalsIgnoreCase(action)) {
            String dir = SQLInitServlet.getField("filePath");
            if (!new File(dir).exists()) {
                logger.info("The filePath in datainfo.properties is invalid " + dir);
                addPageMessage(respage.getString("filepath_you_defined_not_seem_valid"));
                forwardPage(Page.UPLOAD_CRF_DATA_TO_MIRTH);
            }
            // All the uploaded files will be saved in filePath/crf/original/
            String theDir = dir + "crf" + File.separator + "original" + File.separator;
            if (!new File(theDir).isDirectory()) {
                new File(theDir).mkdirs();
                logger.info("Made the directory " + theDir);
            }
           
            List<File> files;
            File f = null;
            try {
            	// here upload one single file
               // f = uploadFile(theDir);            	
               // sendPost(f);
               //sendRequestByHttpClient(f);
            	
               // here process all uploaded files	
               files = uploadFiles(theDir);
               File mappingFile = null;
               boolean foundMappingFile = false;
               
         	   for (File file : files) {           
                   
                   if (file == null || file.getName() == null) {
                       logger.info("file is empty.");
              
                   }else {
                   	if(file.getName().toLowerCase().lastIndexOf(".properties") > -1) {
                   		mappingFile = file;
                   		foundMappingFile = true;
                   		logger.info("Found mapping file *.properties uploaded");
                  		                   		
                   		break;
                   	}
                   }
               }
         	 
         	  if(files.size() < 2) {
         		 String message = "errorCode.notCorrectFileNumber - When upload files, please select at least one data text files and one mapping file named like *.properties"; 
         		 this.addPageMessage(message);
                 
         		 forwardPage(Page.UPLOAD_CRF_DATA_TO_MIRTH);
                 return;  
         	  }
         	  
         	  if (!foundMappingFile) {            		         		
         		 String message = "errorCode.noMappingfileFound - When upload files, please include one correct mapping file and named it like *.properties "; 
         		 this.addPageMessage(message);
                 
         		 forwardPage(Page.UPLOAD_CRF_DATA_TO_MIRTH);
                 return;
         	  }
         	  else {
         		 try {
         			 this.getRestfulServiceHelper().getImportDataHelper().validateMappingFile(mappingFile); 
         		 }catch(Exception e) {
         			 String message = e.getMessage(); 
             		 this.addPageMessage(message);
                     
             		 forwardPage(Page.UPLOAD_CRF_DATA_TO_MIRTH);
                     return;
         		 }
         		
         	  }
               
               // here process all files in one request 
               //sendRequestByHttpClient(files);
               
              //sendOneFilePerRequestByHttpClient(files);
               sendOneDataRowPerRequestByHttpClient(files,request);

               String message = "The Application is processing your files, you can come back later to check the status and with detail in log file";
               this.addPageMessage(message);
            } catch (Exception e) {
                logger.warn("*** Found exception during file upload***");
                e.printStackTrace();

            }
            
            forwardPage(Page.UPLOAD_CRF_DATA_TO_MIRTH);
                    	 
        } else if ("download".equalsIgnoreCase(action)) {
            String fileName= request.getParameter("fileId");
            File file = this.getRestfulServiceHelper().getImportDataHelper().getPersonalImportLogFile(fileName,  request);
            dowloadFile(file, "text/xml");
            
        } else if ("delete".equalsIgnoreCase(action)) {
            String fileName= request.getParameter("fileId");
            this.getRestfulServiceHelper().getImportDataHelper().deletePersonalTempImportFile(fileName,request);
            
            forwardPage(Page.UPLOAD_CRF_DATA_TO_MIRTH);
        }
        
        

    }

 // HTTP POST request
 	private void sendPost(File file) throws Exception {

 		//String url = "http://10.0.11.149:80/oc_file_process/";
 		// for dev
 		//String url = "http://mirth.dev.openclinica.io:81/oc_file_process/";
 		String uploadMirthUrl = CoreResources.getField("uploadMirthUrl");
 		String USER_AGENT = "Mozilla/5.0";
 		String login ="user1";
 		String password = "password";

 		URL obj = new URL(uploadMirthUrl);
 		HttpURLConnection con = (HttpURLConnection) obj.openConnection();

 		//add request header
 		con.setRequestMethod("POST");
 		con.setRequestProperty("User-Agent", USER_AGENT);
 		con.setRequestProperty("Accept-Language", "en-US,en;q=0.5");
 		con.setRequestProperty("Content-Type", "multipart/form-data");
 		//Authorization
 	/*	String loginPassword = login+ ":" + password;
 		String encoded = new sun.misc.BASE64Encoder().encode (loginPassword.getBytes()); 		
 		con.setRequestProperty ("Authorization", "Basic " + encoded);*/
 		 String accessToken = (String) request.getSession().getAttribute("accessToken");
 		con.setRequestProperty("Authorization", "Bearer " + accessToken);
 		
 		String basePath = getBasePath(request);
 		con.setRequestProperty("OCBasePath", basePath);

 		String urlParameters = "&CONTENT=";
 		
 	
 		// Send post request
 		con.setDoOutput(true);
 		/*DataOutputStream wr = new DataOutputStream(con.getOutputStream()); 	
 		
 		wr.writeBytes(urlParameters);
 		
 		FileInputStream fis = new FileInputStream(file);
 		byte[] buffer = new byte[4096];
		
		while (fis.read(buffer) > 0) {
			wr.write(buffer);
		}
		
		fis.close();	
 		wr.flush();
 		wr.close();
 		con.connect();
		*/
 		
 		OutputStream os = con.getOutputStream();
 		OutputStreamWriter osw = new OutputStreamWriter(os, "UTF-8"); 
 		osw.write(urlParameters);
 		FileInputStream fis = new FileInputStream(file);
 		
 		//clean the data,this make mirth engine throw out errors
        try(Scanner sc = new Scanner(fis)){
        	String currentLine;
		
	       	while (sc.hasNextLine()) {
	       		 currentLine = sc.nextLine();
	       		 String strVal = currentLine.toString().replaceAll("&#0;", "");
				 osw.write(strVal);
			}		
		 }
		
		fis.close();	
 		osw.flush();
 		osw.close();
 		con.connect();
 		////////////////////////////////////////////////////
 		int responseCode = con.getResponseCode();
 		//System.out.println("\nSending 'POST' request to URL : " + uploadMirthUrl);
 		//System.out.println("Post parameters : " + urlParameters);
 		//System.out.println("Response Code : " + responseCode);

 		if(responseCode == 200) {
 			 addPageMessage("Upload and import file sucessfully!");
 		}else {
 			addPageMessage(con.getResponseMessage());
 		}
 		BufferedReader in = new BufferedReader(
 		        new InputStreamReader(con.getInputStream()));
 		String inputLine;
 		StringBuffer response = new StringBuffer();

 		while ((inputLine = in.readLine()) != null) {
 			response.append(inputLine);
 		}
 		in.close();
 		
 		//print result
 		//System.out.println(response.toString());

 
 }

    /*
     * Given the MultipartRequest extract the first File validate that it is an xml file and then return it.
     */
    private File getFirstFile() {
        File f = null;
        List<File> files = uploadHelper.returnFiles(request, context);
        for (File file : files) {
            // Enumeration files = multi.getFileNames();
            // if (files.hasMoreElements()) {
            // String name = (String) files.nextElement();
            // f = multi.getFile(name);
            f = file;
            if (f == null || f.getName() == null) {
                logger.info("file is empty.");
       
            }
        }
        return f;
    }

    private List<File> getUploadedFiles() {
        File f = null;
        boolean foundMappingFile = false;
        
        List<File> files = uploadHelper.returnFiles(request, context);
        for (File file : files) {           
            f = file;
            if (f == null || f.getName() == null) {
                logger.info("file is empty.");
       
            }else {
            	if(f.getName().equals("mapping.txt")) {
            		foundMappingFile = true;
            		logger.info("Found mapping.txt uploaded");
            		
            		break;
            	}
            }
        }
        return files;
    }

    
    /**
     * Uploads the xml file
     * 
     * @param version
     * @throws Exception
     */
    public File uploadFile(String theDir) throws Exception {

        return getFirstFile();
    }

    public List<File> uploadFiles(String theDir) throws Exception {

        return getUploadedFiles();
    }


    @Override
    protected String getAdminServlet() {
        if (ub.isSysAdmin()) {
            return SecureController.ADMIN_SERVLET_CODE;
        } else {
            return "";
        }
    }

    public void errorCheck(List<String> errors) {
        if (errors != null) {
            // add to session
            // forward to another page
            logger.info(errors.toString());
            for (String error : errors) {
                addPageMessage(error);
            }
            if (errors.size() > 0) {
                // fail = true;
                forwardPage(Page.UPLOAD_CRF_DATA_TO_MIRTH);
            } else {
                addPageMessage(respage.getString("passed_study_check"));
                addPageMessage(respage.getString("passed_oid_metadata_check"));
            }

        }

    }
    
    
 // HTTP POST request
  	private void sendRequestByHttpClient(File file) throws Exception {

  		//String url = "http://10.0.11.149:80/oc_file_process/";
  		String uploadMirthUrl = CoreResources.getField("uploadMirthUrl");
  	/*	String hostNm = "10.0.11.149";
  		int portNum = 80;
  		String searchPath = "/oc_file_process/";
  		
  		String login ="user1";
  		String password = "password";
  		
  		 URIBuilder uriBuilder = new URIBuilder();
  	    uriBuilder..setScheme("http")
  	            .setHost(hostNm)
  	            .setPort(portNum)
  	            .setPath(searchPath)
  	            .addParameter("action", "UploadFile");
  	    java.net.URI uri = uriBuilder.build();

*/  	
  		
  		HttpPost post = new HttpPost(uploadMirthUrl);
  		/**
  		 *  add header
  		 */
  	    //Authorization
 		String accessToken = (String) request.getSession().getAttribute("accessToken");
  		post.setHeader("Authorization", "Bearer " + accessToken);

 		post.setHeader("Accept", 
 	             "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
 		post.setHeader("Accept-Language", "en-US,en;q=0.5"); 		
 		post.setHeader("Connection", "keep-alive"); 		
		  		
  		FileBody fileBody = new FileBody(file, ContentType.TEXT_PLAIN);  		  	 
  		MultipartEntityBuilder builder = MultipartEntityBuilder.create();
  		builder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);
  		builder.addPart("uploadedData", fileBody);    	
  		HttpEntity entity = builder.build();   		
  		post.setEntity(entity);
  		
  		CloseableHttpClient httpClient = HttpClients.createDefault();
  		HttpResponse response = httpClient.execute(post);
  		
  	    //print result	
 		int responseCode = response.getStatusLine().getStatusCode();

 		System.out.println("\nSending 'POST' request to URL : " + uploadMirthUrl); 	
 		System.out.println("Response Code : " + responseCode);

 		BufferedReader rd = new BufferedReader(
 	                new InputStreamReader(response.getEntity().getContent()));

 		StringBuffer result = new StringBuffer();
 		String line = "";
 		while ((line = rd.readLine()) != null) {
 			result.append(line);
 		}

 		System.out.println(result.toString());

  
  }

  	private void sendRequestByHttpClient(List<File> files) throws Exception {

  		String uploadMirthUrl = CoreResources.getField("uploadMirthUrl");
  		//String url = "http://10.0.11.149:80/oc_file_process/";
  		/*String hostNm = "10.0.11.149";
  		int portNum = 80;
  		String searchPath = "/oc_file_process/";
  		
  		String login ="user1";
  		String password = "password";
  		
  		 URIBuilder uriBuilder = new URIBuilder();
  	    uriBuilder.setScheme("http")
  	            .setHost(hostNm)
  	            .setPort(portNum)
  	            .setPath(searchPath)
  	            .addParameter("action", "UploadFile");
  	    java.net.URI uri = uriBuilder.build();*/

  	    
  		HttpPost post = new HttpPost(uploadMirthUrl);
  		/**
  		 *  add header
  		 */
  	    //Authorization
 		String accessToken = (String) request.getSession().getAttribute("accessToken");
  		post.setHeader("Authorization", "Bearer " + accessToken);
  		
  		String basePath = getBasePath(request);
  		post.setHeader("OCBasePath", basePath);

 		post.setHeader("Accept", 
 	             "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
 		post.setHeader("Accept-Language", "en-US,en;q=0.5"); 		
 		post.setHeader("Connection", "keep-alive"); 		
		
 		MultipartEntityBuilder builder = MultipartEntityBuilder.create();
	  	builder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);
	  	String partNm = null;
	  	int i = 1;
	  		
 		for (File file : files) {
 			FileBody fileBody = new FileBody(file, ContentType.TEXT_PLAIN);
 			partNm = "uploadedData" + i;
 	  		builder.addPart(partNm, fileBody);
 	  		
 	  		i++;
 		}
  		  	
  		HttpEntity entity = builder.build();   		
  		post.setEntity(entity);
  		
  		CloseableHttpClient httpClient = HttpClients.createDefault();
  		HttpResponse response = httpClient.execute(post);
  		
  	    //print result	
 		int responseCode = response.getStatusLine().getStatusCode();

 		System.out.println("\nSending 'POST' request to URL : " + uploadMirthUrl); 	
 		System.out.println("Response Code : " + responseCode);

 		BufferedReader rd = new BufferedReader(
 	                new InputStreamReader(response.getEntity().getContent()));

 		StringBuffer result = new StringBuffer();
 		String line = "";
 		while ((line = rd.readLine()) != null) {
 			result.append(line);
 		}
        
 		System.out.println(result.toString());
 		String responseStr = processResponse(result.toString());
 		addPageMessage(responseStr);
 		 
 		

  
  }
  	
	private void sendOneFilePerRequestByHttpClient(List<File> files) throws Exception {

  		String uploadMirthUrl = CoreResources.getField("uploadMirthUrl");
  		
  		/**
  		 *  prepare mapping file
  		 */
  		FileBody mappingFileBody = null;
  		String  mappingpartNm = null;
  		for (File file : files) {
  			
  			if(file.getName().toLowerCase().indexOf("mapping") > -1) {
  				mappingFileBody = new FileBody(file, ContentType.TEXT_PLAIN);
  				mappingpartNm = "uploadedData";  	 	  		
  	 	  		
  	 	  		break;
  			}
 			
 		}
  		
	  	int i = 1;	  		
 		for (File file : files) {
 			// skip mapping file
 			if(file.getName().toLowerCase().indexOf("mapping") > -1) {
 				;
 			}else {
 				HttpPost post = new HttpPost(uploadMirthUrl);
 	 	  		/**
 	 	  		 *  add header Authorization
 	 	  		 */
 	 	 		String accessToken = (String) request.getSession().getAttribute("accessToken");
 	 	  		post.setHeader("Authorization", "Bearer " + accessToken);
 	 	  		
 	 	  		String basePath = getBasePath(request);
 	 	  		post.setHeader("OCBasePath", basePath);

 	 	 		post.setHeader("Accept", 
 	 	 	             "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
 	 	 		post.setHeader("Accept-Language", "en-US,en;q=0.5"); 		
 	 	 		post.setHeader("Connection", "keep-alive"); 		
 	 			
 	 	 		MultipartEntityBuilder builder = MultipartEntityBuilder.create();
 	 		  	builder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);
 	 		  	String partNm = null;
 	 			FileBody fileBody = new FileBody(file, ContentType.TEXT_PLAIN);
 	 			partNm = "uploadedData" + i;
 	 	  		builder.addPart(partNm, fileBody);
 	 	  		
 	 	  		// add mapping file
 	 	  		builder.addPart(mappingpartNm, mappingFileBody);
 	 	  		
 	 	  		HttpEntity entity = builder.build();   		
 	 	  		post.setEntity(entity);
 	 	  		
 	 	  		CloseableHttpClient httpClient = HttpClients.createDefault();
 	 	  		HttpResponse response = httpClient.execute(post);
 	 	  		
 	 	  	    //print result	
 	 	 		int responseCode = response.getStatusLine().getStatusCode();

 	 	 		System.out.println("\nSending 'POST' request to URL : " + uploadMirthUrl); 	
 	 	 		System.out.println("Response Code : " + responseCode);

 	 	 		BufferedReader rd = new BufferedReader(
 	 	 	                new InputStreamReader(response.getEntity().getContent()));

 	 	 		StringBuffer result = new StringBuffer();
 	 	 		String line = "";
 	 	 		while ((line = rd.readLine()) != null) {
 	 	 			result.append(line);
 	 	 		}
 	 	        
 	 	 		String responseStr = processResponse(result.toString());
 	 	 		addPageMessage(responseStr);
 	 	 		 
 	 	 		System.out.println(responseStr);
 	 	  		
 			}
 			
 	  		i++;
 		}
  
  }

  	public static String getBasePath(HttpServletRequest request) {
  		StringBuffer basePath = new StringBuffer();
  		String scheme = request.getScheme();
  		String domain = request.getServerName();
  		int port = request.getServerPort();
  		basePath.append(scheme);
  		basePath.append("://");
  		basePath.append(domain);
  		if("http".equalsIgnoreCase(scheme) && 80 != port) {
  			basePath.append(":").append(String.valueOf(port));
  		} else if("https".equalsIgnoreCase(scheme) && port != 443) {
  			basePath.append(":").append(String.valueOf(port));
  		}
  		return basePath.toString();
  	}

  	/**
  	 * 
  	 * @param reponseStr
  	 * {
		"message": "VALIDATION FAILED",
		"errors": [{
		"code": "errorCode.ValidationFailed",
		"message": "Your Participant OID WAL-001 does not reference an existing Participant in the Study. \nThe Item OID HeightOID did not generate any results in the database. Check it and try again. \nThe Item OID HeightUnitsOID did not generate any results in the database. Check it and try again. \nThe Item OID WeightOID did not generate any results in the database. Check it and try again. \nThe Item OID WeightUnitsOID did not generate any results in the database. Check it and try again. \nThe Item OID EnrollDateOID did not generate any results in the database. Check it and try again. \nYour Participant OID WAL-002 does not reference an existing Participant in the Study. \nThe Item OID HeightOID did not generate any results in the database. Check it and try again. \nThe Item OID HeightUnitsOID did not generate any results in the database. Check it and try again. \nThe Item OID WeightOID did not generate any results in the database. Check it and try again. \nThe Item OID WeightUnitsOID did not generate any results in the database. Check it and try again. \nThe Item OID EnrollDateOID did not generate any results in the database. Check it and try again. \nYour Participant OID WAL-003 does not reference an existing Participant in the Study. \nThe Item OID HeightOID did not generate any results in the database. Check it and try again. \nThe Item OID HeightUnitsOID did not generate any results in the database. Check it and try again. \nThe Item OID WeightOID did not generate any results in the database. Check it and try again. \nThe Item OID WeightUnitsOID did not generate any results in the database. Check it and try again. \nThe Item OID EnrollDateOID did not generate any results in the database. Check it and try again. \n"
			}]
		}
  	 * @return
  	 */
  	
	public static String processResponse(String reponseStr) {
		
		if(reponseStr == null || reponseStr.trim().length()==0) {
			return "";
		}
		
		StringBuffer response = new StringBuffer();
  		JSONObject obj = new JSONObject(reponseStr);
  		String message = obj.getString("message");
  		
  		response.append(message);
  		response.append("\n");

  		try {
  			JSONArray errorsArr = obj.getJSONArray("errors");
  	  		for (int i = 0; i < errorsArr.length(); i++)
  	  		{
  	  		    String code = errorsArr.getJSONObject(i).getString("code");
  	  		    String errorMsg = errorsArr.getJSONObject(i).getString("message");
  	  		    response.append(code);
  	  		    response.append(":");
  	  		    response.append(errorMsg);
  	  		    response.append("\n");
  	  		}
  		}catch(org.json.JSONException e) {
  			Object msg = obj.get("detailMessages");  	  		
  		    response.append(msg.toString());  	  		   
  		    response.append("\n");  	  		
  		}
  		
  		return response.toString();
  	}
	
	public ArrayList<File> splitDataFileAndProcesDataRowbyRow(File file) {
		ArrayList<File> fileList = new ArrayList<>();
	    BufferedReader reader;
	    
	    try {
            int count =1;	    	
	    		    	
	    	File splitFile;
	    	String importFileDir = this.getImportFileDir();
	    	
	    	reader = new BufferedReader(new FileReader(file));
	    	
	    	String orginalFileName = file.getName();
	    	int pos = orginalFileName.indexOf(".");
	    	orginalFileName = orginalFileName.substring(0,pos);
	    	
	    	String columnLine = reader.readLine();
	    	String line = columnLine;
	    	
	    	BufferedWriter bw = null;
	    	FileOutputStream fos = null;
	    	
	    	while (line != null) {
				
				// read next line
				line = reader.readLine();
				
				if(line != null) {
					splitFile = new File(importFileDir + orginalFileName +"_"+ count + ".txt");				
					fos = new FileOutputStream(splitFile);			 
					bw = new BufferedWriter(new OutputStreamWriter(fos));
				 
					bw.write(columnLine);				
					bw.write("\r");
				
					bw.write(line);	
					fileList.add(splitFile);
				}
				
			    if(bw !=null) {
			    	bw.close();
			    }
				
							
				count++;
				
			}
			reader.close();
	        
	       
	    } catch (Exception e) {
	        e.printStackTrace();
	    }
	    
		return fileList;
	}
	
	
	public void sendOneDataRowPerRequestByHttpClientToMirth(List<File> files) throws Exception {

  		String uploadMirthUrl = CoreResources.getField("uploadMirthUrl");
  		
  		/**
  		 *  prepare mapping file
  		 */
  		FileBody mappingFileBody = null;
  		String  mappingpartNm = null;
  		for (File file : files) {
  			
  			if(file.getName().toLowerCase().indexOf("mapping") > -1) {
  				mappingFileBody = new FileBody(file, ContentType.TEXT_PLAIN);
  				mappingpartNm = "uploadedData";  	 	  		
  	 	  		
  	 	  		break;
  			}
 			
 		}
  		
	  	int i = 1;	  		
 		for (File file : files) {
 			// skip mapping file
 			if(file.getName().toLowerCase().indexOf("mapping") > -1) {
 				;
 			}else {
 				ArrayList<File> dataFileList = splitDataFileAndProcesDataRowbyRow(file);
 				
 				Iterator dataFilesIt = dataFileList.iterator();
 				
 				while(dataFilesIt.hasNext()) {
 					File rowFile = (File) dataFilesIt.next();
 					
 					HttpPost post = new HttpPost(uploadMirthUrl);
 	 	 	  		/**
 	 	 	  		 *  add header Authorization
 	 	 	  		 */
 	 	 	 		String accessToken = (String) request.getSession().getAttribute("accessToken");
 	 	 	  		post.setHeader("Authorization", "Bearer " + accessToken);
 	 	 	  		
 	 	 	  		String basePath = getBasePath(request);
 	 	 	  		post.setHeader("OCBasePath", basePath);

 	 	 	 		post.setHeader("Accept", 
 	 	 	 	             "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
 	 	 	 		post.setHeader("Accept-Language", "en-US,en;q=0.5"); 		
 	 	 	 		post.setHeader("Connection", "keep-alive");
 	 	 	 		
 	 	 	 		String originalFileName = rowFile.getName();
 	 	 	 	    post.setHeader("originalFileName", originalFileName);
 	 	 			
 	 	 	 		MultipartEntityBuilder builder = MultipartEntityBuilder.create();
 	 	 		  	builder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);
 	 	 		  	String partNm = null;
 	 	 			FileBody fileBody = new FileBody(rowFile, ContentType.TEXT_PLAIN);
 	 	 			partNm = "uploadedData" + i;
 	 	 	  		builder.addPart(partNm, fileBody);
 	 	 	  		
 	 	 	  		// add mapping file
 	 	 	  		builder.addPart(mappingpartNm, mappingFileBody);
 	 	 	  		
 	 	 	  		HttpEntity entity = builder.build();   		
 	 	 	  		post.setEntity(entity);
 	 	 	  		
 	 	 	  		CloseableHttpClient httpClient = HttpClients.createDefault();
 	 	 	  		HttpResponse response = httpClient.execute(post);
 	 	 	  		
 	 	 	  	    //print result	
 	 	 	 		int responseCode = response.getStatusLine().getStatusCode();

 	 	 	 		System.out.println("\nSending 'POST' request to URL : " + uploadMirthUrl); 	
 	 	 	 		System.out.println("Response Code : " + responseCode);

 	 	 	 		BufferedReader rd = new BufferedReader(
 	 	 	 	                new InputStreamReader(response.getEntity().getContent()));

 	 	 	 		StringBuffer result = new StringBuffer();
 	 	 	 		String line = "";
 	 	 	 		while ((line = rd.readLine()) != null) {
 	 	 	 			result.append(line);
 	 	 	 		}
 	 	 	        
 	 	 	 		String responseStr = processResponse(result.toString());
 	 	 	 		addPageMessage(responseStr);
 	 	 	 		 
 	 	 	 		System.out.println(responseStr);
 	 	 	 		
 	 	 	 	    TimeUnit.MILLISECONDS.sleep(10);
 				}
 				
 			   // after sent, then delete from disk
 				dataFilesIt = dataFileList.iterator();
 				while(dataFilesIt.hasNext()) {
 					File rowFile = (File) dataFilesIt.next();					 					
 					this.getRestfulServiceHelper().getImportDataHelper().deleteTempImportFile(rowFile);
	 	 	  		
 				}
	 	 	  		
 			}
 			
 	  		i++;
 		}
  
 		this.getRestfulServiceHelper().getImportDataHelper().saveFileToImportFolder(files);
  }

/**
 *  This method will call OC Restful API directly
 * @param files
 * @throws Exception
 */
	public void sendOneDataRowPerRequestByHttpClient(List<File> files,HttpServletRequest request) throws Exception {
		this.getRestfulServiceHelper().sendOneDataRowPerRequestByHttpClient(files, request);
	}
	
	
	public String getImportFileDir() {
		  if (importFileDir != null) {
			  return importFileDir;
		  }else {
			  String dir = SQLInitServlet.getField("filePath");
	          if (!new File(dir).exists()) {
	              logger.info("The filePath in datainfo.properties is invalid " + dir);             
	          }
	          // All the uploaded files will be saved in filePath/crf/original/
	          String theDir = dir + "import" + File.separator + "original" + File.separator;
	          if (!new File(theDir).isDirectory()) {
	              new File(theDir).mkdirs();
	              logger.info("Made the directory " + theDir);
	          }
	        
	         importFileDir = theDir;
		  }
		 
		return importFileDir;
	}
	
	public void setHttpServletRequest(HttpServletRequest request) {
		this.request = request;
	}

	public RestfulServiceHelper getRestfulServiceHelper() {
		if(restfulServiceHelper == null) {
			restfulServiceHelper = new RestfulServiceHelper(this.getSM().getDataSource());
		}
		return restfulServiceHelper;
	}

	public void setRestfulServiceHelper(RestfulServiceHelper restfulServiceHelper) {
		this.restfulServiceHelper = restfulServiceHelper;
	}
	
	public SessionManager getSM() {
		UserAccountBean ub = (UserAccountBean) session.getAttribute(USER_BEAN_NAME);
		String userName = request.getRemoteUser();
		
		if(this.sm == null) {
			 try {
				sm = new SessionManager(ub, userName, SpringServletAccess.getApplicationContext(context));
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}	
		}
		
		return sm;
	}
}
