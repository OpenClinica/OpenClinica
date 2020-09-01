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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;
import javax.servlet.RequestDispatcher;
import javax.servlet.http.HttpServletRequest;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.net.HttpURLConnection;


import core.org.akaza.openclinica.bean.core.Role;
import core.org.akaza.openclinica.bean.login.UserAccountBean;
import core.org.akaza.openclinica.bean.rule.FileUploadHelper;
import core.org.akaza.openclinica.bean.rule.XmlSchemaValidationHelper;
import core.org.akaza.openclinica.service.StudyBuildService;
import org.akaza.openclinica.control.SpringServletAccess;
import org.akaza.openclinica.control.core.SecureController;
import org.akaza.openclinica.control.form.FormProcessor;
import org.akaza.openclinica.controller.helper.RestfulServiceHelper;
import core.org.akaza.openclinica.core.SessionManager;
import core.org.akaza.openclinica.core.form.StringUtil;
import core.org.akaza.openclinica.dao.core.CoreResources;
import core.org.akaza.openclinica.i18n.core.LocaleResolver;
import org.akaza.openclinica.service.CsvFileConverterServiceImpl;
import org.akaza.openclinica.service.ExcelFileConverterServiceImpl;
import org.akaza.openclinica.service.SasFileConverterServiceImpl;
import org.akaza.openclinica.view.Page;
import core.org.akaza.openclinica.web.InsufficientPermissionException;
import core.org.akaza.openclinica.web.SQLInitServlet;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.mock.web.MockHttpServletRequest;


public class UploadCRFDataToHttpServerServlet extends SecureController {

    Locale locale;
    static private String importFileDir;

    private XmlSchemaValidationHelper schemaValidator = new XmlSchemaValidationHelper();
    private FileUploadHelper uploadHelper = new FileUploadHelper();
    private RestfulServiceHelper restfulServiceHelper;

    

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
        HashMap hm = new HashMap();
        
        String submitted =  (String) request.getParameter("submitted");
        if(submitted!=null && submitted.equals("true")) {
        	 request.removeAttribute("submitted");
        	 
        	 String message = "The Application is processing your files, you can come back later to check the status and with detail in log file";	          
	         this.addPageMessage(message);
        	 forwardPage(Page.UPLOAD_CRF_DATA_TO_MIRTH);
        	 
        	 return;
        }
       
        if (StringUtil.isBlank(action)) {
            logger.info("action is blank");
          
            forwardPage(Page.UPLOAD_CRF_DATA_TO_MIRTH);
        }else if ("confirm".equalsIgnoreCase(action)) {
            String dir = SQLInitServlet.getField("filePath");
            if (!new File(dir).exists()) {
                logger.info("The filePath in datainfo.properties is invalid " + dir);
                addPageMessage(respage.getString("system_configuration_filepath_not_valid"));
                forwardPage(Page.UPLOAD_CRF_DATA_TO_MIRTH);
            }
            // All the uploaded files will be saved in filePath/crf/original/
            String theDir = dir + "import" + File.separator + "original" + File.separator;
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
         		 String message = "errorCode.notCorrectFileNumber: Please upload one data file and one mapping file with .properties file extension.";
         		 this.addPageMessage(message);
         		 removeFiles(files);
         		 forwardPage(Page.UPLOAD_CRF_DATA_TO_MIRTH);
                 return;  
         	  }
         	  
         	  if (!foundMappingFile) {            		         		
         		 String message = "errorCode.noMappingfileFound: Please upload one data file and one mapping file with .properties file extension.";
         		 this.addPageMessage(message);
         		 removeFiles(files);
         		 forwardPage(Page.UPLOAD_CRF_DATA_TO_MIRTH);
                 return;
         	  }
         	  else {
         		 try {
         			hm = this.getRestfulServiceHelper().getImportDataHelper().validateMappingFile(mappingFile); 
         		 }catch(Exception e) {
         			 String message = e.getMessage(); 
             		 this.addPageMessage(message);
             		 removeFiles(files);
             		 forwardPage(Page.UPLOAD_CRF_DATA_TO_MIRTH);
                     return;
         		 }
         		
         	  }
               
         	  
               // here process all files in one request 
               //sendRequestByHttpClient(files);
               
              //sendOneFilePerRequestByHttpClient(files);
         	  
	          // Async
       	 	          	          
         	 /* MockHttpServletRequest requestMock = getMockRequest(request);
     		
	          AsyncContext asyncContext = request.startAsync();
	          asyncContext.addListener(new UploadDataAsyncListener());

	          AsyncContext finalAsyncContext = asyncContext;
	          asyncContext.start(new Runnable() {
	              @Override
	              public void run () {
	            	  try {
						sendOneDataRowPerRequestByHttpClient(files,requestMock);
	            		
					} catch (Exception e) {						
						// TODO Auto-generated catch block
						e.printStackTrace();
						
						//finalAsyncContext.complete();
					}
	                  
	              }
	          });*/
	          
	          MockHttpServletRequest requestMock = getMockRequest(request);
	          
	          // redirect first
	          this.response.sendRedirect("/OpenClinica/UploadCRFData?submitted=true");	          
	         
	          //////////////// Start of heavy thread run/////////////////////
	          //sendOneDataRowPerRequestByHttpClient(files,requestMock);
	          final HashMap hmIn =hm;
	          new Thread(new Runnable() {
	              public void run(){
	               try {
	            	  
					
					sendOneDataRowPerRequestByHttpClient(files, requestMock,hmIn );
				} catch (Exception e) {
					// TODO Auto-generated catch block
					logger.error("Error sending data row for request: ",e);
				} ;
	              }
	          }).start();
	         
      		///////////////// end of heavy thread run/////////////////////
	          
	          return;

              
            } catch (Exception e) {
                logger.error("*** Found exception during file upload***",e);

                String message = "Please selected correct files to resubmit.";  	          
  	            this.addPageMessage(message);
  	          
                forwardPage(Page.UPLOAD_CRF_DATA_TO_MIRTH);
            }
            
           
                    	 
        } else if ("download".equalsIgnoreCase(action)) {
        	String studyID= request.getParameter("studyId");
        	String parentNm= request.getParameter("parentNm");
            String fileName= request.getParameter("fileId");          
            File file = this.getRestfulServiceHelper().getImportDataHelper().getImportFileByStudyIDParentNm(studyID, parentNm, fileName);
            dowloadFile(file, "text/csv");
            
        } else if ("delete".equalsIgnoreCase(action)) {
        	String studyID= request.getParameter("studyId");
        	String parentNm= request.getParameter("parentNm");
            String fileName= request.getParameter("fileId");           
            File tempFile = this.getRestfulServiceHelper().getImportDataHelper().getImportFileByStudyIDParentNm(studyID, parentNm, fileName);
           
        	if(tempFile!=null && tempFile.exists()) {
        		tempFile.delete();
        	}
        	
        	String fromUrl = request.getParameter("fromUrl");
        	if(fromUrl.equals("UploadCRFData")) {
        		forwardPage(Page.UPLOAD_CRF_DATA_TO_MIRTH);
        	}else if(fromUrl.equals("listLog")) {
        		RequestDispatcher dis = request.getRequestDispatcher("/pages/Log/listFiles");
                dis.forward(request, response);
        	}else {
        		RequestDispatcher dis = request.getRequestDispatcher("/pages/Log/listFiles");
                dis.forward(request, response);
        	}
            
        }
        
        

    }

    /**
	 * @param files
	 */
	private void removeFiles(List<File> files) {
		// remove temporary uploaded files
		 for (File file : files) {    
			 if(file.exists()) {
				 file.delete();
			 }
		 }
	}
	
	/**
	 * @return
	 */
	private MockHttpServletRequest getMockRequest(HttpServletRequest request) {
		MockHttpServletRequest requestMock = new MockHttpServletRequest();
		
		String remoteAddress = this.getBasePath(request);	  		
		String importDataWSUrl = remoteAddress + "/OpenClinica/pages/auth/api/clinicaldata/";	  	
		requestMock.setAttribute("importDataWSUrl", importDataWSUrl);
		
		String accessToken = (String) request.getSession().getAttribute("accessToken");
		requestMock.setAttribute("accessToken", accessToken);
		
		String basePath = remoteAddress;
		requestMock.setAttribute("basePath", basePath);
		
		UserAccountBean ub = (UserAccountBean) request.getSession().getAttribute(USER_BEAN_NAME);
		requestMock.getSession(true).setAttribute(USER_BEAN_NAME, ub);
		
		return requestMock;
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
 			 addPageMessage("Upload and import file successfully!");
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

 		//System.out.println("\nSending 'POST' request to URL : " + uploadMirthUrl); 	
 		//System.out.println("Response Code : " + responseCode);

 		BufferedReader rd = new BufferedReader(
 	                new InputStreamReader(response.getEntity().getContent()));

 		StringBuffer result = new StringBuffer();
 		String line = "";
 		while ((line = rd.readLine()) != null) {
 			result.append(line);
 		}

 		//System.out.println(result.toString());

  
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
  		//PIPETEXT
  		post.setHeader("PIPETEXT", "PIPETEXT");

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

 		//System.out.println("\nSending 'POST' request to URL : " + uploadMirthUrl); 	
 		//System.out.println("Response Code : " + responseCode);

 		BufferedReader rd = new BufferedReader(
 	                new InputStreamReader(response.getEntity().getContent()));

 		StringBuffer result = new StringBuffer();
 		String line = "";
 		while ((line = rd.readLine()) != null) {
 			result.append(line);
 		}
        
 		//System.out.println(result.toString());
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
  			
  			if(file.getName().toLowerCase().endsWith(".properties")) {
  				mappingFileBody = new FileBody(file, ContentType.TEXT_PLAIN);
  				mappingpartNm = "uploadedData";  	 	  		
  	 	  		
  	 	  		break;
  			}
 			
 		}
  		
	  	int i = 1;	  		
 		for (File file : files) {
 			// skip mapping file
 			if(file.getName().toLowerCase().endsWith(".properties")) {
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
 	 	  	    //PIPETEXT
 	 	  		post.setHeader("PIPETEXT", "PIPETEXT");


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

 	 	 		//System.out.println("\nSending 'POST' request to URL : " + uploadMirthUrl); 	
 	 	 		//System.out.println("Response Code : " + responseCode);

 	 	 		BufferedReader rd = new BufferedReader(
 	 	 	                new InputStreamReader(response.getEntity().getContent()));

 	 	 		StringBuffer result = new StringBuffer();
 	 	 		String line = "";
 	 	 		while ((line = rd.readLine()) != null) {
 	 	 			result.append(line);
 	 	 		}
 	 	        
 	 	 		String responseStr = processResponse(result.toString());
 	 	 		addPageMessage(responseStr);
 	 	 		 
 	 	 		//System.out.println(responseStr);
 	 	  		
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
	        logger.error("Error processing the data row by row: ",e);

	    }
	    
		return fileList;
	}
	
	
	public void sendOneDataRowPerRequestByHttpClientToMirth(List<File> files) throws Exception {

  		String uploadMirthUrl = CoreResources.getField("uploadMirthUrl");
  		String studyOID = null;
  		
  		/**
  		 *  prepare mapping file
  		 */
  		FileBody mappingFileBody = null;
  		String  mappingpartNm = null;
  		for (File file : files) {
  			
  			if(file.getName().toLowerCase().endsWith(".properties")) {
  				mappingFileBody = new FileBody(file, ContentType.TEXT_PLAIN);
  				mappingpartNm = "uploadedData";  	 	  		
  				studyOID=this.getRestfulServiceHelper().getImportDataHelper().getStudyOidFromMappingFile(file);
  				
  	 	  		break;
  			}
 			
 		}
  		
	  	int i = 1;	  		
 		for (File file : files) {
 			// skip mapping file
 			if(file.getName().toLowerCase().endsWith(".properties")) {
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
 	 	 	  	    //PIPETEXT
 	 	 	  		post.setHeader("PIPETEXT", "PIPETEXT");


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

 	 	 	 		//System.out.println("\nSending 'POST' request to URL : " + uploadMirthUrl); 	
 	 	 	 		//System.out.println("Response Code : " + responseCode);

 	 	 	 		BufferedReader rd = new BufferedReader(
 	 	 	 	                new InputStreamReader(response.getEntity().getContent()));

 	 	 	 		StringBuffer result = new StringBuffer();
 	 	 	 		String line = "";
 	 	 	 		while ((line = rd.readLine()) != null) {
 	 	 	 			result.append(line);
 	 	 	 		}
 	 	 	        
 	 	 	 		String responseStr = processResponse(result.toString());
 	 	 	 		addPageMessage(responseStr);
 	 	 	 		 
 	 	 	 		//System.out.println(responseStr);
 	 	 	 		
 	 	 	 	    TimeUnit.MILLISECONDS.sleep(10);
 				}
 				
 			   // after sent, then delete from disk
 				dataFilesIt = dataFileList.iterator();
 				while(dataFilesIt.hasNext()) {
 					File rowFile = (File) dataFilesIt.next();					 					
 					this.getRestfulServiceHelper().getImportDataHelper().deleteTempImportFile(rowFile,studyOID);
	 	 	  		
 				}
	 	 	  		
 			}
 			
 	  		i++;
 		}
  
 		this.getRestfulServiceHelper().getImportDataHelper().saveFileToImportFolder(files,studyOID);
  }

/**
 *  This method will call OC Restful API directly
 * @param files
 * @throws Exception
 */
	public void sendOneDataRowPerRequestByHttpClient(List<File> files,HttpServletRequest request,HashMap hm) throws Exception {
		/*HttpServletRequest requestMock = new MockHttpServletRequest();
		
		String remoteAddress = this.getBasePath(request);	  		
	  	String importDataWSUrl = remoteAddress + "/OpenClinica/pages/auth/api/clinicaldata/";	  	
		requestMock.setAttribute("importDataWSUrl", importDataWSUrl);
		
		String accessToken = (String) request.getSession().getAttribute("accessToken");
		requestMock.setAttribute("accessToken", accessToken);
		
		String basePath = remoteAddress;
		requestMock.setAttribute("basePath", basePath);
		
		this.getRestfulServiceHelper().sendOneDataRowPerRequestByHttpClient(files, requestMock);*/
		
		this.getRestfulServiceHelper().sendOneDataRowPerRequestByHttpClient(files, request,hm);
		
	}
	
	
	public void sendOneDataRowPerRequestByHttpClient(List<File> files,MockHttpServletRequest mockRequest,HashMap hm) throws Exception {
		
		
		this.getRestfulServiceHelper().sendOneDataRowPerRequestByHttpClient(files, mockRequest, true,hm);
		/*SendOneDataRowPerRequestRunnable sendOneDataRowPerRequestRunnable = new SendOneDataRowPerRequestRunnable(this.getRestfulServiceHelper(), files, request);
		Thread sendOneDataRowPerRequest = new Thread(sendOneDataRowPerRequestRunnable, "sendOneDataRowPerRequest");
		sendOneDataRowPerRequest.start();*/
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
			restfulServiceHelper = new RestfulServiceHelper(this.getSM().getDataSource(), getStudyBuildService(), getStudyDao(), getSasFileConverterService(), getExcelFileConverterService(), getCsvFileConverterService());
		}
		return restfulServiceHelper;
	}

	protected SasFileConverterServiceImpl getSasFileConverterService(){
		return (SasFileConverterServiceImpl) SpringServletAccess.getApplicationContext(context).getBean("sasFileConverterService");
	}

	protected ExcelFileConverterServiceImpl getExcelFileConverterService(){
		return (ExcelFileConverterServiceImpl) SpringServletAccess.getApplicationContext(context).getBean("excelFileConverterService");
	}

	protected CsvFileConverterServiceImpl getCsvFileConverterService(){
		return (CsvFileConverterServiceImpl) SpringServletAccess.getApplicationContext(context).getBean("csvFileConverterService");
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
				logger.error("Session Manager is not initializing properly: ",e);
			}	
		}
		
		return sm;
	}
}
