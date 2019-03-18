package org.akaza.openclinica.controller.helper;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.sql.DataSource;

import org.akaza.openclinica.bean.login.UserAccountBean;
import org.akaza.openclinica.dao.core.CoreResources;
import org.akaza.openclinica.dao.login.UserAccountDAO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

public class MessageLogger {

	protected final Logger logger = LoggerFactory.getLogger(getClass().getName());
	
	private DataSource dataSource;	
	protected UserAccountBean ub;
		
	private String personalLogFileDir = null;
	private String currentUserName;
	private int currentActiveStudyId;
	
	public MessageLogger(DataSource ds) {
		super();
		this.dataSource = ds;
	}
	
	/**
     * 
     * @param orginalFileName
     * @param msg
     */
    public void writeToLog(String subDir,String orginalFileName, String headerLine,String msg,HttpServletRequest request) {
		
    	BufferedWriter bw = null;
		FileWriter fw = null;
		boolean isNewFile = false;

	    String logFileName;
	   
	    if(orginalFileName == null) {
	    	;
	    }else {
	    	 try {
	             int count =1;	    	
	 	    		    	
	 	    	File logFile;
	 	    	String importFileDir = this.getPersonalLogFileDir(request,subDir);
	     	    
	 	    	logFileName = importFileDir + orginalFileName;
	 			logFile = new File(logFileName);
	 			
	 			/**
	 			 *  create new file and add headerLine as first line
	 			 *  example :
	 			 *  RowNo | ParticipantID | Status | Message
	 			 */
	 			if(!logFile.exists()) {
	 				logFile.createNewFile();
	 				isNewFile = true;				
	 			}
	 			
	 			// true = append file
	 			fw = new FileWriter(logFile.getAbsoluteFile(), true);
	 			bw = new BufferedWriter(fw);
	            
	 			// create new file and prepare header line
	 			if(isNewFile) {				
	 				bw.write(headerLine);	
	 				bw.write("\n");
	 			}
	 			
	 			if(msg != null) {
	 				bw.write(msg);	
	 				bw.write("\n");
	 			}	
	 			
	 			bw.close();						
	 	       
	 	    } catch (Exception e) {
	 	        e.printStackTrace();
	 	    }finally {
	 			try {
	 				if (bw != null)
	 					bw.close();
	 				if (fw != null)
	 					fw.close();
	 			} catch (IOException ex) {
	 				ex.printStackTrace();
	 			}
	 		}
	    }
	    
	   	    
	} 
    /**
     * 
     * @param request
     * @param subDir
     * @return
     */
    public String getPersonalLogFileDir(HttpServletRequest request, String subDir) {
  	  String userName = "";
  	  boolean prepareNewDir = false; 
  	  UserAccountBean userBean = null;
  	  int activeStudyId=-999;
  	  
		  if (personalLogFileDir != null) {
			
	          userBean = this.getUserAccount(request);

	          if (userBean == null) {
	                String err_msg = "errorCode.InvalidUser:"+ "Please send request as a valid user";	               
	                return err_msg;
	          }else {	        	  
	        	  userName = userBean.getName();
	        	  activeStudyId = userBean.getActiveStudyId();
	        	  
	        	  if(this.getCurrentUserName().equals(userName) && this.getCurrentActiveStudyId()==activeStudyId) {
	        		  return personalLogFileDir;
	        	  }else {
	        		  prepareNewDir = true;	        		  
	        	  }
	          }	  
			  
		  }else {
			  prepareNewDir = true;  
		  }	 
		  
		  if(prepareNewDir) {
			  String dir = CoreResources.getField("filePath");
	          if (!new File(dir).exists()) {
	              logger.info("The filePath in datainfo.properties is invalid " + dir);             
	          }
	          // All the uploaded files will be saved in filePath/import/userName/
	          userBean = this.getUserAccount(request);

	          if (userBean == null) {
	                String err_msg = "errorCode.InvalidUser:"+ "Please send request as a valid user";	               
	                return err_msg;
	          }else {	        	 
	        	  userName = (userBean.getName()+"_" +userBean.getId()).toLowerCase().replace(" ","");
	        	  activeStudyId = userBean.getActiveStudyId();
	        	  
	        	  this.setCurrentUserName(userName);
	        	  this.setCurrentActiveStudyId(activeStudyId);
	          }
	          
	          
	          String theDir = dir + subDir + File.separator + activeStudyId+ File.separator + userName + File.separator;
        	         
	          if (!new File(theDir).isDirectory()) {
	              new File(theDir).mkdirs();
	              logger.info("Made the directory " + theDir);
	          }
	        
	          personalLogFileDir = theDir;
		  }
		 
		return personalLogFileDir;
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

	public String getCurrentUserName() {
		return currentUserName;
	}

	public void setCurrentUserName(String currentUserName) {
		this.currentUserName = currentUserName;
	}

	public int getCurrentActiveStudyId() {
		return currentActiveStudyId;
	}

	public void setCurrentActiveStudyId(int currentActiveStudyId) {
		this.currentActiveStudyId = currentActiveStudyId;
	}
}
