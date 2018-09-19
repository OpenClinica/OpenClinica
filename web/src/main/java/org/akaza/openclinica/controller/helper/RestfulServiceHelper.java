package org.akaza.openclinica.controller.helper;

import org.akaza.openclinica.bean.core.Role;
import org.akaza.openclinica.bean.login.StudyUserRoleBean;
import org.akaza.openclinica.bean.login.UserAccountBean;
import org.akaza.openclinica.bean.managestudy.StudyBean;
import org.akaza.openclinica.control.SpringServletAccess;
import org.akaza.openclinica.dao.core.CoreResources;
import org.akaza.openclinica.dao.login.UserAccountDAO;
import org.akaza.openclinica.dao.managestudy.StudyDAO;
import org.akaza.openclinica.exception.OpenClinicaSystemException;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.validation.Errors;
import org.springframework.web.multipart.MultipartFile;

import liquibase.util.StringUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.sql.DataSource;

public class RestfulServiceHelper {
	
	private final static Logger log = LoggerFactory.getLogger("RestfulServiceHelper");
	
	//CSV file header	
	private static final String [] FILE_HEADER_MAPPING = {"ParticipantID"};
	private static final String ParticipantID_header = "ParticipantID";
	
	
	private DataSource dataSource;	
	private StudyDAO studyDao; 
	private UserAccountDAO userAccountDAO;

	
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
    	        UserAccountDAO userAccountDao = new UserAccountDAO(dataSource);
    	        userBean = (UserAccountBean) userAccountDao.findByUserName(username);
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
}
