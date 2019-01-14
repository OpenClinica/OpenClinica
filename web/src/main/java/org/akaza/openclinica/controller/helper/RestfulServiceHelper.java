package org.akaza.openclinica.controller.helper;

import org.akaza.openclinica.bean.core.Role;
import org.akaza.openclinica.bean.core.UserType;
import org.akaza.openclinica.bean.login.StudyUserRoleBean;
import org.akaza.openclinica.bean.login.UserAccountBean;
import org.akaza.openclinica.bean.managestudy.StudyBean;
import org.akaza.openclinica.control.SpringServletAccess;
import org.akaza.openclinica.control.submit.ImportCRFInfoSummary;
import org.akaza.openclinica.dao.core.CoreResources;
import org.akaza.openclinica.dao.login.UserAccountDAO;
import org.akaza.openclinica.dao.managestudy.StudyDAO;
import org.akaza.openclinica.exception.OpenClinicaSystemException;
import org.akaza.openclinica.logic.importdata.ImportDataHelper;
import org.akaza.openclinica.logic.importdata.PipeDelimitedDataHelper;
import org.akaza.openclinica.service.UserStatus;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
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
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.validation.Errors;
import org.springframework.web.multipart.MultipartFile;

import liquibase.util.StringUtils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.TimeUnit;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.sql.DataSource;
@Service("serviceHelper")
public class RestfulServiceHelper {
	
	private final static Logger log = LoggerFactory.getLogger("RestfulServiceHelper");
	
	//CSV file header	
	private static final String [] FILE_HEADER_MAPPING = {"ParticipantID"};
	private static final String ParticipantID_header = "ParticipantID";
	
	
	private DataSource dataSource;	
	private StudyDAO studyDao; 
	private UserAccountDAO userAccountDAO;
	private PipeDelimitedDataHelper importDataHelper;

	
	public RestfulServiceHelper(DataSource dataSource2) {
		dataSource = dataSource2;
	}


	/**
	 * @param file
	 * @return
	 * @throws Exception 
	 */
	public static ArrayList<String> readCSVFile(MultipartFile file) throws Exception {
		
		ArrayList<String> subjectKeyList = new ArrayList<>();
		 
		try {
			 BufferedReader reader;
				
			 String line;
			 InputStream is = file.getInputStream();
			 reader = new BufferedReader(new InputStreamReader(is));
			 
			//Create the CSVFormat object with the header mapping		 
			 CSVFormat csvFileFormat = CSVFormat.DEFAULT.withHeader(FILE_HEADER_MAPPING).withFirstRecordAsHeader().withTrim();

	         CSVParser csvParser = new CSVParser(reader, csvFileFormat);
	       
	         try {
	        	//Get a list of CSV file records              	         
		         for (CSVRecord csvRecord : csvParser) {		      	
		        	     	          	 
		        	  String participantID = csvRecord.get(ParticipantID_header);
		        	  
		        	  if (StringUtils.isNotEmpty(participantID)) {
		     			 subjectKeyList.add(participantID);     							     				         
		     		 }
		         }
	         }catch(java.lang.IllegalArgumentException e) {
	        	 subjectKeyList = readFile(file);
	        	
		         
		     
	         }
	         
		}catch (Exception e) {
			throw new Exception(" This CSV format is not supported ");
	    }
		
        
		 
		return subjectKeyList;
	}
	
	
	/**
	 * @param file
	 * @return
	 * @throws IOException
	 */
	private static ArrayList<String> readFile(MultipartFile file) throws IOException {
		
		ArrayList<String> subjectKeyList = new ArrayList<>();
		
		try(Scanner sc = new Scanner(file.getInputStream())){
			
			 String line;
			
			 int lineNm = 1;
			 int position = 0;
			 
			 while (sc.hasNextLine()) {
				 line = sc.nextLine();
				 String[] lineVal= line.split(",", 0);
				 
				 // check ParticipantID column number
				 if(lineNm ==1) {
					 
					 for(int i=0; i < lineVal.length;i++) {
						 lineVal.equals(ParticipantID_header);
						 position = i;
						 
						 break;
					 }
				 }else {
					 subjectKeyList.add(lineVal[position]);
				 }
				 
				 
				
				 lineNm++;
			 }
			
		} catch (Exception e) {
			log.error("Exception with cause = {} {}", e.getCause(), e.getMessage());
	    }
		
		return subjectKeyList;
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
	 	 	        		 e.reject("errorCode.noRoleSetUp", "You do not have any role set up for user " + userName + " in study site " + siteOid );
	 	 	        		hasRolePermission = false;
	 	 	        	}else if(siteLevelRole.getId() == 0 || siteLevelRole.getRole().equals(Role.MONITOR)) {
	 	 				    e.reject("errorCode.noSufficientPrivileges", "You do not have sufficient privileges to proceed with this operation.");
	 	 				  hasRolePermission = false;
	 	 				}
	 	 	        
		        }else {
		        	 e.reject("errorCode.noRoleSetUp", "You do not have any role set up for user " + userName + " in study " + studyOid );
		        	 hasRolePermission = false;
		        }	 		 
	        
		    }else {
		    	if(studyLevelRole.getId() == 0 || studyLevelRole.getRole().equals(Role.MONITOR)) {
	 				    e.reject("errorCode.noSufficientPrivileges", "You do not have sufficient privileges to proceed with this operation.");
	 				   hasRolePermission = false;
	 				}
		    }
			
			
			return hasRolePermission;
			
	 }
	 
	 /**
     * Helper Method to get the user account
     * 
     * @return UserAccountBean
     */
    public UserAccountBean getUserAccount(HttpServletRequest request) {
    	UserAccountBean userBean;    
    	
    	if(request.getSession().getAttribute("userBean") != null) {
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


	
	 public ImportCRFInfoSummary sendOneDataRowPerRequestByHttpClientToMirth(List<File> files,HttpServletRequest request) throws Exception {

	  		String uploadMirthUrl = CoreResources.getField("uploadMirthUrl");
	  		ImportCRFInfoSummary importCRFInfoSummary  = new ImportCRFInfoSummary();
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
	  				studyOID =this.getImportDataHelper().getStudyOidFromMappingFile(file);
	  	 	  		
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

	 	 	 	 		//System.out.println("\nSending 'POST' request to URL : " + uploadMirthUrl); 	
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
		 	 	  		
	 			}
	 			
	 	  		i++;
	 		}
	  
	 		this.getImportDataHelper().saveFileToImportFolder(files,studyOID);
			
	 		return importCRFInfoSummary;
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
	  
	 		this.getImportDataHelper().saveFileToImportFolder(files,studyOID);
			
	 		return importCRFInfoSummary;
	  }
	 
	 public ImportCRFInfoSummary sendOneDataRowPerRequestByHttpClient(List<File> files,MockHttpServletRequest request,boolean ismock) throws Exception {
		   
	  		String importDataWSUrl = (String) request.getAttribute("importDataWSUrl");
	  		String accessToken = (String) request.getAttribute("accessToken");
	  		String basePath =  (String) request.getAttribute("basePath");
	  		
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
	 				
	 				while(dataFilesIt.hasNext()) {
	 					File rowFile = (File) dataFilesIt.next();
	 					
	 					HttpPost post = new HttpPost(importDataWSUrl);
	 	 	 	  		/**
	 	 	 	  		 *  add header Authorization
	 	 	 	  		 */	 	 	 	 		
	 	 	 	  		post.setHeader("Authorization", "Bearer " + accessToken);	 	 	 	  			 	 	 	  		
	 	 	 	  		post.setHeader("OCBasePath", basePath);
	 	 	 	  		
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
	 	 	 	 		
	 	 	 	 	    //TimeUnit.MILLISECONDS.sleep(1);
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
	  
	 		this.getImportDataHelper().saveFileToImportFolder(files,studyOID);
			
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
				importDataHelper = new PipeDelimitedDataHelper();
			}
			return importDataHelper;
		}

		public void setImportDataHelper(PipeDelimitedDataHelper importDataHelper) {
			this.importDataHelper = importDataHelper;
		}
}
