package org.akaza.openclinica.controller.helper;

import org.akaza.openclinica.bean.core.Role;
import org.akaza.openclinica.bean.login.StudyUserRoleBean;
import org.akaza.openclinica.bean.login.UserAccountBean;
import org.akaza.openclinica.bean.managestudy.StudyBean;
import org.akaza.openclinica.control.SpringServletAccess;
import org.akaza.openclinica.control.submit.ImportCRFInfoSummary;
import org.akaza.openclinica.controller.dto.StudyEventScheduleDTO;
import org.akaza.openclinica.dao.core.CoreResources;
import org.akaza.openclinica.dao.login.UserAccountDAO;
import org.akaza.openclinica.dao.managestudy.StudyDAO;
import org.akaza.openclinica.exception.OpenClinicaException;
import org.akaza.openclinica.exception.OpenClinicaSystemException;
import org.akaza.openclinica.logic.importdata.PipeDelimitedDataHelper;
import org.akaza.openclinica.web.restful.errors.ErrorConstants;
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
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.validation.Errors;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.sql.DataSource;
import java.io.*;
import java.sql.Connection;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;

import static org.akaza.openclinica.control.core.SecureController.USER_BEAN_NAME;
@Service("serviceHelper")
public class RestfulServiceHelper {
	
	private final static Logger log = LoggerFactory.getLogger("RestfulServiceHelper");
	
	//CSV file header
	private static final String ParticipantID_header = "ParticipantID";


	private DataSource dataSource;	
	private StudyDAO studyDao; 
	private UserAccountDAO userAccountDAO;
	private PipeDelimitedDataHelper importDataHelper;
	private MessageLogger messageLogger;

	
	public RestfulServiceHelper(DataSource dataSource2) {
		dataSource = dataSource2;
	}
	
	 /**
	  * 
	  * @param studyOid
	  * @param request
	  * @return
	 * @throws Exception 
	  */
	 public StudyBean setSchema(String studyOid, HttpServletRequest request) throws OpenClinicaSystemException {
		// first time, the default DB schema for restful service is public
		 StudyBean study = getStudyDao().findByPublicOid(studyOid);

		 Connection con;
		 String schemaNm="";

		 if (study == null) {
			 throw new OpenClinicaSystemException("errorCode.studyNotExist","The study identifier you provided:" + studyOid + " is not valid.");

		 } else {
			schemaNm = study.getSchemaName();
		 }
		 request.setAttribute("requestSchema", schemaNm);
		 // get correct study from the right DB schema
		 study = getStudyDao().findByOid(studyOid);

		 return study;
	 }

	 /**
	  * 
	  * @param file
	  * @return
	  * @throws IOException
	  */
	 public static String readFileToString(MultipartFile file) throws IOException{
         StringBuilder sb = new StringBuilder();
         try(Scanner sc = new Scanner(file.getInputStream())){
        	 String currentLine;
		
        	 while (sc.hasNextLine()) {
        		 currentLine = sc.nextLine();
		         sb.append(currentLine);
		     }
		
		 }
		
	       return sb.toString();
	 }
	 
	 
	 
	 public boolean verifyRole(String userName,  String study_oid,
				String site_oid, Errors e) {
			
		 boolean hasRolePermission = true;
			// check for site role & user permission if ok -> return yes,
			//if no-> check for study permissions & role
		  String studyOid = study_oid;
	      String siteOid = site_oid;
			
	      StudyUserRoleBean studyLevelRole = this.getUserAccountDAO().findTheRoleByUserNameAndStudyOid(userName,studyOid);
			if(studyLevelRole == null) {
				if (siteOid != null) {
	 	        	
	 	 	        	StudyUserRoleBean siteLevelRole = this.getUserAccountDAO().findTheRoleByUserNameAndStudyOid(userName,siteOid);
	 	 	        	if(siteLevelRole == null) {
	 	 	        		 e.reject(ErrorConstants.ERR_NO_ROLE_SETUP, "You do not have any role set up for user " + userName + " in study site " + siteOid );
	 	 	        		hasRolePermission = false;
	 	 	        	}else if(siteLevelRole.getId() == 0 || siteLevelRole.getRole().equals(Role.MONITOR)) {
	 	 				    e.reject(ErrorConstants.ERR_NO_SUFFICIENT_PRIVILEGES, "You do not have sufficient privileges to proceed with this operation.");
	 	 				  hasRolePermission = false;
	 	 				}
	 	 	        
		        }else {
		        	 e.reject(ErrorConstants.ERR_NO_ROLE_SETUP, "You do not have any role set up for user " + userName + " in study " + studyOid );
		        	 hasRolePermission = false;
		        }	 		 
	        
		    }else {
		    	if(studyLevelRole.getId() == 0 || studyLevelRole.getRole().equals(Role.MONITOR)) {
	 				    e.reject(ErrorConstants.ERR_NO_SUFFICIENT_PRIVILEGES, "You do not have sufficient privileges to proceed with this operation.");
	 				   hasRolePermission = false;
	 				}
		    }
			
			
			return hasRolePermission;
			
	 }
	 
	 /**
	  * 
	  * @param userName
	  * @param study_oid
	  * @param site_oid
	  * @return
	  */
	 public String verifyRole(String userName,  String study_oid,
				String site_oid) {
		
		  String studyOid = study_oid;
	      String siteOid = site_oid;
	      String err_msg = null;
			
	      StudyUserRoleBean studyLevelRole = this.getUserAccountDAO().findTheRoleByUserNameAndStudyOid(userName,studyOid);
			if(studyLevelRole == null) {
				if (siteOid != null) {
	 	        	
	 	 	        	StudyUserRoleBean siteLevelRole = this.getUserAccountDAO().findTheRoleByUserNameAndStudyOid(userName,siteOid);
	 	 	        	if(siteLevelRole == null) {
	 	 	        		err_msg= ErrorConstants.ERR_NO_ROLE_SETUP + " You do not have any role set up for user " + userName + " in study site " + siteOid;	 	 	        			 	 	        	
	 	 	        	}else if(siteLevelRole.getId() == 0 || siteLevelRole.getRole().equals(Role.MONITOR)) {	 	 				    
		 	 				err_msg= ErrorConstants.ERR_NO_SUFFICIENT_PRIVILEGES + " You do not have sufficient privileges to proceed with this operation.";	 	 	        	  		 	 			
	 	 				}
	 	 	        
		        }else {
		        	 err_msg=ErrorConstants.ERR_NO_ROLE_SETUP + " You do not have any role set up for user " + userName + " in study " + studyOid;
	
		        }	 		 
	        
		    }else {
		    	if(studyLevelRole.getId() == 0 || studyLevelRole.getRole().equals(Role.MONITOR)) {
		    		err_msg = ErrorConstants.ERR_NO_SUFFICIENT_PRIVILEGES + " You do not have sufficient privileges to proceed with this operation.";
	
	 				}
		    }
			
		
			
			return err_msg;
			
	 }
	 /**
     * Helper Method to get the user account
     * 
     * @return UserAccountBean
     */
    public UserAccountBean getUserAccount(HttpServletRequest request) {
    	UserAccountBean userBean;    
    	
    	if(request.getSession()!= null && request.getSession().getAttribute("userBean") != null) {
    		userBean = (UserAccountBean) request.getSession().getAttribute("userBean");
    		
    	}else {
    		 Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    	        String username = null;
    	        if (principal instanceof UserDetails) {
    	            username = ((UserDetails) principal).getUsername();
    	        } else {
    	            username = principal.toString();
    	        }

			String schema = CoreResources.getRequestSchema();
			CoreResources.setRequestSchema("public");
    	        UserAccountDAO userAccountDAO = new UserAccountDAO(dataSource);
    	        userBean = (UserAccountBean) userAccountDAO.findByUserName(username);
			CoreResources.setRequestSchema(schema);

    	}
    	
    	return userBean;
       
	}
    
   
    public File getXSDFile(HttpServletRequest request,String fileNm) {
    	HttpSession session = request.getSession();
        ServletContext context = session.getServletContext();
        
    	return new File(SpringServletAccess.getPropertiesDir(context) + fileNm);
    }
    /**
	 * 
	 * @return
	 */
	 public StudyDAO getStudyDao() {
        studyDao = studyDao != null ? studyDao : new StudyDAO(dataSource);
        return studyDao;
     }
	 

    public UserAccountDAO getUserAccountDAO() {
    	userAccountDAO = userAccountDAO != null ? userAccountDAO : new UserAccountDAO(dataSource);
        return userAccountDAO;
    }
	 
	 /**
	  *  this will call OC Restful API directly:
	  *  ${remoteAddress}/OpenClinica/pages/auth/api/clinicaldata/
	  *  
	  * @param files
	  * @param request
	  * @return
	  * @throws Exception
	  */
	 public ImportCRFInfoSummary sendOneDataRowPerRequestByHttpClient(List<File> files,HttpServletRequest request) throws Exception {
		    String remoteAddress = this.getBasePath(request);
	  		
	  		String importDataWSUrl = remoteAddress + "/OpenClinica/pages/auth/api/clinicaldata/";
	  		ImportCRFInfoSummary importCRFInfoSummary  = new ImportCRFInfoSummary();
	  		ArrayList<File> tempODMFileList = new ArrayList<>();
	  		String studyOID = null;
	  		
	  		/**
	  		 *  prepare mapping file
	  		 */
	  		File mappingFile = null;
	  		String  mappingpartNm = null;
	  		for (File file : files) {
	  			
	  			if(file.getName().toLowerCase().endsWith(".properties")) {
	  				mappingFile = file;
	  				mappingpartNm = "uploadedData"; 
	  				studyOID = this.getImportDataHelper().getStudyOidFromMappingFile(file);
	  	 	  		
	  	 	  		break;
	  			}
	 			
	 		}
	  		
		  	int i = 1;	  		
	 		for (File file : files) {
	 			// skip mapping file
	 			if(file.getName().toLowerCase().endsWith(".properties")) {
	 				;
	 			}else {
	 				ArrayList<File> dataFileList = splitDataFileAndProcesDataRowbyRow(file,studyOID);
	 				
	 				Iterator dataFilesIt = dataFileList.iterator();
	 				
	 				while(dataFilesIt.hasNext()) {
	 					File rowFile = (File) dataFilesIt.next();
	 					
	 					HttpPost post = new HttpPost(importDataWSUrl);
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
	 	 	 			
	 	 	 	 		MultipartEntityBuilder builder = MultipartEntityBuilder.create();
	 	 	 		  	builder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);
	 	 	 		  	String partNm = null;
	 	 	 		  	/**
	 	 	 		  	 *  Here will only send ODM XML to OC API
	 	 	 		  	 *  
	 	 	 		  	 */
	 	 	 		  	String dataStr = this.getImportDataHelper().transformTextToODMxml(mappingFile,rowFile);
	 	 	 		  	File odmXmlFile = this.getImportDataHelper().saveDataToFile(dataStr, originalFileName,studyOID);
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
	 	 	 	 		if(responseStr!=null && responseStr.toLowerCase().indexOf("error")>-1) {
	 	 	 	 			importCRFInfoSummary.setFailCnt(importCRFInfoSummary.getFailCnt()+1);
	 	 	 	 		}else {
	 	 	 	 			importCRFInfoSummary.setPassCnt(importCRFInfoSummary.getPassCnt() +1);
	 	 	 	 		}
	 	 	 	 
	 	 	 	 	    importCRFInfoSummary.getDetailMessages().add(responseStr);
	 	 	 	 		//System.out.println(responseStr);
	 	 	 	 		
	 	 	 	 	    //TimeUnit.MILLISECONDS.sleep(5);
	 				}
	 				
	 			   // after sent, then delete from disk
	 				dataFilesIt = dataFileList.iterator();
	 				while(dataFilesIt.hasNext()) {
	 					File rowFile = (File) dataFilesIt.next();					 					
		 	 	  		this.getImportDataHelper().deleteTempImportFile(rowFile,studyOID);
		 	 	  		
	 				}
	 				
	 				dataFilesIt = tempODMFileList.iterator();
	 				while(dataFilesIt.hasNext()) {
	 					File tempODMFile = (File) dataFilesIt.next();					 					
		 	 	  		this.getImportDataHelper().deleteTempImportFile(tempODMFile,studyOID);
		 	 	  		
	 				}
		 	 	  		
	 			}
	 			
	 	  		i++;
	 		}
	 		 // not save original data
	 		//this.getImportDataHelper().saveFileToImportFolder(files,studyOID);
			
	 		return importCRFInfoSummary;
	  }
	 
	 public ImportCRFInfoSummary sendOneDataRowPerRequestByHttpClient(List<File> files,MockHttpServletRequest request,boolean ismock) throws Exception {
		   
	  		String importDataWSUrl = (String) request.getAttribute("importDataWSUrl");
	  		String accessToken = (String) request.getAttribute("accessToken");
	  		String basePath =  (String) request.getAttribute("basePath");
	  		UserAccountBean ub = (UserAccountBean) request.getSession().getAttribute(USER_BEAN_NAME);

	  		
	  		ImportCRFInfoSummary importCRFInfoSummary  = new ImportCRFInfoSummary();
	  		ArrayList<File> tempODMFileList = new ArrayList<>();
	  		String studyOID = null;
	  		
	  		/**
	  		 *  prepare mapping file
	  		 */
	  		File mappingFile = null;
	  		String  mappingpartNm = null;
	  		for (File file : files) {
	  			
	  			if(file.getName().toLowerCase().endsWith(".properties")) {
	  				mappingFile = file;
	  				mappingpartNm = "uploadedData"; 
	  				studyOID= this.getImportDataHelper().getStudyOidFromMappingFile(file);
	  	 	  		
	  	 	  		break;
	  			}
	 			
	 		}
	  		
		  	int i = 1;	  		
	 		for (File file : files) {
	 			// skip mapping file
	 			if(file.getName().toLowerCase().endsWith(".properties")) {
	 				;
	 			}else {
	 				ArrayList<File> dataFileList = splitDataFileAndProcesDataRowbyRow(file,studyOID);
	 				
	 				Iterator dataFilesIt = dataFileList.iterator();
	 				
	 				File rowFile = null;
					String skipMatchCriteria = null;
	 				while(dataFilesIt.hasNext()) {
	 					try {
	 						rowFile = (File) dataFilesIt.next();
		 					
		 					HttpPost post = new HttpPost(importDataWSUrl);
		 	 	 	  		/**
		 	 	 	  		 *  add header Authorization
		 	 	 	  		 */	 	 	 	 		
		 	 	 	  		post.setHeader("Authorization", "Bearer " + accessToken);	 	 	 	  			 	 	 	  		
		 	 	 	  		post.setHeader("OCBasePath", basePath);
		 	 	 	  	    //PIPETEXT
		 	 	 	  		post.setHeader("PIPETEXT", "PIPETEXT");
		 	 	 	  		
		 	 	 	  		//SkipMatchCriteria
							if (skipMatchCriteria == null){
								skipMatchCriteria = this.getImportDataHelper().getSkipMatchCriteria(rowFile, mappingFile);
							}
		 	 	 	  	    post.setHeader("SkipMatchCriteria", skipMatchCriteria);
		 	 	 	  	
		 	 	 	 		post.setHeader("Accept", 
		 	 	 	 	             "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
		 	 	 	 		post.setHeader("Accept-Language", "en-US,en;q=0.5"); 		
		 	 	 	 		post.setHeader("Connection", "keep-alive");
		 	 	 	 		
		 	 	 	 		String originalFileName = rowFile.getName();
		 	 	 	 	    post.setHeader("originalFileName", originalFileName);
		 	 	 			
		 	 	 	 		MultipartEntityBuilder builder = MultipartEntityBuilder.create();
		 	 	 		  	builder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);
		 	 	 		  	String partNm = null;
		 	 	 		  	/**
		 	 	 		  	 *  Here will only send ODM XML to OC API
		 	 	 		  	 *  
		 	 	 		  	 */
		 	 	 		  	String dataStr = this.getImportDataHelper().transformTextToODMxml(mappingFile,rowFile);
		 	 	 		  	File odmXmlFile = this.getImportDataHelper().saveDataToFile(dataStr, originalFileName,studyOID);
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
		 	 	 	 		if(responseStr!=null && responseStr.toLowerCase().indexOf("error")>-1) {
		 	 	 	 			importCRFInfoSummary.setFailCnt(importCRFInfoSummary.getFailCnt()+1);
		 	 	 	 		}else {
		 	 	 	 			importCRFInfoSummary.setPassCnt(importCRFInfoSummary.getPassCnt() +1);
		 	 	 	 		}
		 	 	 	 
		 	 	 	 	    importCRFInfoSummary.getDetailMessages().add(responseStr);
		 	 	 	 		//System.out.println(responseStr);
		 	 	 	 		
		 	 	 	 	    //TimeUnit.MILLISECONDS.sleep(1);
	 					}catch(OpenClinicaSystemException e) {
	 						String originalFileName = rowFile.getName();            	
	 		            	String recordNum = null;
	 		            	String participantID = this.getImportDataHelper().getParticipantID(mappingFile, rowFile);
	 		            	if(originalFileName !=null) {
	 		            		recordNum = originalFileName.substring(originalFileName.lastIndexOf("_")+1,originalFileName.indexOf("."));
	 		            		originalFileName = originalFileName.substring(0, originalFileName.lastIndexOf("_"));
	 		            	}
	 		            	String msg = e.getErrorCode() + ":" + e.getMessage();
	 		            	msg = recordNum + "|" + participantID + "|FAILED|" + msg;
	 			    		this.getImportDataHelper().writeToMatchAndSkipLog(originalFileName, msg,request);
	 		            
	 					}
	 					
	 				}
	 				
	 			   // after sent, then delete from disk
	 				dataFilesIt = dataFileList.iterator();
	 				while(dataFilesIt.hasNext()) {
	 					rowFile = (File) dataFilesIt.next();					 					
		 	 	  		this.getImportDataHelper().deleteTempImportFile(rowFile,studyOID);
		 	 	  		
	 				}
	 				
	 				dataFilesIt = tempODMFileList.iterator();
	 				while(dataFilesIt.hasNext()) {
	 					File tempODMFile = (File) dataFilesIt.next();					 					
		 	 	  		this.getImportDataHelper().deleteTempImportFile(tempODMFile,studyOID);
		 	 	  		
	 				}
		 	 	  		
	 			}
	 			
	 	  		i++;
	 		}
	        // not save original data
	 		//this.getImportDataHelper().saveFileToImportFolder(files,studyOID);
			
	 		return importCRFInfoSummary;
	  }

	    public ArrayList<File> splitDataFileAndProcesDataRowbyRow(File file,String studyOID) {
			ArrayList<File> fileList = new ArrayList<>();
		    BufferedReader reader;
		    
		    try {
	            int count =1;	    	
		    		    	
		    	File splitFile;
		    	String importFileDir = this.getImportDataHelper().getImportFileDir(studyOID);
		    	
		    	reader = new BufferedReader(new FileReader(file));
		    	
		    	String orginalFileName = file.getName();
		    	int pos = orginalFileName.indexOf(".");
		    	if(pos > 0) {
		    		orginalFileName = orginalFileName.substring(0,pos);
		    	}
		    	
		    	
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


	    public PipeDelimitedDataHelper getImportDataHelper() {
			if(importDataHelper == null) {
				importDataHelper = new PipeDelimitedDataHelper(this.dataSource);
			}
			return importDataHelper;
		}

		public void setImportDataHelper(PipeDelimitedDataHelper importDataHelper) {
			this.importDataHelper = importDataHelper;
		}
		
		/**
		 * 
		 * @param dateTimeStr:
		 * yyyy-MM-dd
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
					String errMsg = "The input date("+ dateTimeStr + ") can't be parsed, please use the correct format " + dataFormat;
		        	throw new OpenClinicaException(errMsg,ErrorConstants.ERR_PARSE_DATE);
				}
		       
		        return result;
		    }


		public MessageLogger getMessageLogger() {
			
			if(messageLogger == null) {
				messageLogger = new MessageLogger(this.dataSource);
			}
			
			return messageLogger;
		}


		public void setMessageLogger(MessageLogger messageLogger) {
			this.messageLogger = messageLogger;
		}
}
