package core.org.akaza.openclinica.service;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import core.org.akaza.openclinica.bean.login.UserAccountBean;
import core.org.akaza.openclinica.dao.core.CoreResources;
import core.org.akaza.openclinica.dao.login.UserAccountDAO;
import core.org.akaza.openclinica.logic.importdata.ImportDataHelper;
import org.apache.commons.dbcp2.BasicDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

@Service("logFileService")
public class LogFileServiceImpl implements LogFileService {
	protected final Logger logger = LoggerFactory.getLogger(getClass().getName());

	private final static String STUDY_EVENT_LOG_DIR = "study-event-schedule";
	private String personalStudyEventScheduleLogFileDir;
	private String currentUserName;
    private int currentActiveStudyId;
    
    @Autowired
    @Qualifier("dataSource")
    private BasicDataSource dataSource;
	
	public String getPersonalStudyEventScheduleLogFileDir() {
		return personalStudyEventScheduleLogFileDir;
	}


	public void setPersonalStudyEventScheduleLogFileDir(String personalStudyEventScheduleLogFileDir) {
		this.personalStudyEventScheduleLogFileDir = personalStudyEventScheduleLogFileDir;
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


	public List<File> getUserImportLogFiles(HttpServletRequest request){
	
		 ImportDataHelper importDataHelper = new ImportDataHelper();
		
		 List<File> fileList = new ArrayList<File>();
		 File [] fileObjects = null;
		 String [] fileNames = null;
		 String fname=null;
				
		 String fileDir = importDataHelper.getPersonalImportFileDir(request);
		
		 if(importDataHelper.hasDMrole(request)) {
	    		fileList = importDataHelper.getPersonalImportLogFile(request,null);
				
		}else{
			    //System.out.println("\nfileDir=============: " + fileDir);
				File f = new File(fileDir);
				
				fileObjects= f.listFiles();
				fileList = Arrays.asList(fileObjects);
				
		}
		 
		return fileList;			
			
	}
	
	
	public List<File> getUserStudyEventScheduleLogFiles(HttpServletRequest request){
		
		 ImportDataHelper importDataHelper = new ImportDataHelper();
		
		 List<File> fileList = new ArrayList<File>();
		 File [] fileObjects = null;
		 String [] fileNames = null;
		 String fname=null;
				
		 String fileDir = getUserStudyEventScheduleLogFileDir(request);
		
		 if(importDataHelper.hasDMrole(request)) {
	    		fileList = getPersonalStudyEventScheduleLogFiles(request,null);
				
		}else{
			    //System.out.println("\nfileDir=============: " + fileDir);
				File f = new File(fileDir);
				
				fileObjects= f.listFiles();
				fileList = Arrays.asList(fileObjects);
				
		}
		 
		return fileList;			
			
	}
	
	public String getUserStudyEventScheduleLogFileDir(HttpServletRequest request) {
  	  String userName = "";
  	  boolean prepareNewDir = false; 
  	  UserAccountBean userBean = null;
  	  int activeStudyId=-999;
  	  
		  if (personalStudyEventScheduleLogFileDir != null) {
			  
	          userBean = this.getUserAccount(request);
	          if (userBean == null) {
	                String err_msg = "errorCode.InvalidUser:"+ "Please send request as a valid user";	               
	                return err_msg;
	          }else {	        	  
	        	  userName = userBean.getName();
	        	  activeStudyId = userBean.getActiveStudyId();
	        	  
	        	  if(this.getCurrentUserName().equals(userName) && this.getCurrentActiveStudyId()==activeStudyId) {
	        		  return personalStudyEventScheduleLogFileDir;
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
	          
	          
	          String theDir = dir + STUDY_EVENT_LOG_DIR + File.separator + activeStudyId+ File.separator + userName + File.separator;
        	         
	          if (!new File(theDir).isDirectory()) {
	              new File(theDir).mkdirs();
	              logger.info("Made the directory " + theDir);
	          }
	        
	          personalStudyEventScheduleLogFileDir = theDir;
		  }
		 
		return personalStudyEventScheduleLogFileDir;
	}
	
	public ArrayList<File> getPersonalStudyEventScheduleLogFiles(HttpServletRequest request,File fileDir) {
	    
    	ArrayList<File> fileList = new ArrayList<>();
    	String logFileDir = null;
    	File fileFolder = null;
    	
    	if(fileDir != null) {
    		fileFolder = fileDir;
    	}else {
    		
    		logFileDir = this.getUserStudyEventScheduleLogFileDir(request);
	    	 // check user role
	    	if(this.hasDMrole(request)) {
	    	
	    		//C:\tools\apache-tomcat-7.0.93\openclinica.data\study-event-schedule\11\root_1
	    		if(logFileDir.indexOf("\\") > -1) {
	    			 if(logFileDir.endsWith("\\")) {
		            	 int endIndex = logFileDir.lastIndexOf("\\");	    		
		         		logFileDir = logFileDir.substring(0, endIndex);
		         		// 2nd time
		         		endIndex = logFileDir.lastIndexOf("\\");	    		
		         		logFileDir = logFileDir.substring(0, endIndex);
		            }else {
		            	 int endIndex = logFileDir.lastIndexOf("\\");	    		
			         	 logFileDir = logFileDir.substring(0, endIndex);
		            }
	    		}else {
	    			// LINUX:  /opt/tomcat/openclinica.data/import/2171/customcrc_351/
		    		if(logFileDir.indexOf("/") > -1) {
		    			 if(logFileDir.endsWith("/")) {
			            	 int endIndex = logFileDir.lastIndexOf("/");	    		
			         		logFileDir = logFileDir.substring(0, endIndex);
			         		// 2nd time
			         		endIndex = logFileDir.lastIndexOf("/");	    		
			         		logFileDir = logFileDir.substring(0, endIndex);
			            }else {
			            	 int endIndex = logFileDir.lastIndexOf("/");	    		
				         	 logFileDir = logFileDir.substring(0, endIndex);
			            }
		    		}
	    		}
	    		
	    		
	    	}
	    	
	    	fileFolder = new File(logFileDir);
    	}
    	
    	// recursive call
    	for (final File fileEntry : fileFolder.listFiles()) {
    	      if (fileEntry.isDirectory()) {
    	    	  ArrayList<File> fileListFromSubDir = getPersonalStudyEventScheduleLogFiles(null,fileEntry);
    	    	  if(fileListFromSubDir!=null && fileListFromSubDir.size() > 0) {
    	    		  fileList.addAll(fileListFromSubDir);
    	    	  }
    	    	  
    	      } else {
    	        if (fileEntry.isFile()) {
    	          String fileName = fileEntry.getName();
    	          if (fileName.endsWith("_log.txt")) {
    	        	  fileList.add(fileEntry);
    	          }
    	            
    	        }

    	      }
    	    }
    	
    	return fileList;
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
    	        UserAccountDAO userAccountDao = new UserAccountDAO(this.dataSource);
    	        userBean = (UserAccountBean) userAccountDao.findByUserName(username);
    	}
    	
    	return userBean;
       
	}
    
    /**
     * 
     * @param request
     * @return
     */
    public boolean hasDMrole(HttpServletRequest request) {			
	 UserAccountBean userBean = this.getUserAccount(request);
	 
	 String activeStudyRoleName = userBean.getActiveStudyRoleName();
	 
	 if(activeStudyRoleName != null && activeStudyRoleName.toLowerCase().equals("coordinator")) {
		 return true;
	 }else{
		 return false;
	 }
	
		
    }
    
    public void dowloadFile(File f, String contentType,HttpServletResponse response) throws Exception {

        response.setHeader("Content-disposition", "attachment; filename=\"" + f.getName() + "\";");
        response.setContentType("text/xml");
        response.setHeader("Pragma", "public");

        ServletOutputStream op = response.getOutputStream();

        DataInputStream in = null;
        try {
            response.setContentType("text/xml");
            response.setHeader("Pragma", "public");
            response.setContentLength((int) f.length());

            byte[] bbuf = new byte[(int) f.length()];
            in = new DataInputStream(new FileInputStream(f));
            int length;
            while (in != null && (length = in.read(bbuf)) != -1) {
                op.write(bbuf, 0, length);
            }

            in.close();
            op.flush();
            op.close();
        } catch (Exception ee) {
            logger.error("Error while writing to the output stream: ",ee);
        } finally {
            if (in != null) {
                in.close();
            }
            if (op != null) {
                op.close();
            }
        }
    }
    
    /**
     * 
     * @param studyID
     * @param parentNm
     * @param fileNm
     * @return
     */
    public File getLogFileByStudyIDParentNm(String studyID, String parentNm, String fileNm,String typeDir) {
	
		  String dir = CoreResources.getField("filePath");
          if (!new File(dir).exists()) {
              logger.info("The filePath in datainfo.properties is invalid " + dir);             
          }
          // All the uploaded files will be saved in filePath/crf/original/
          String theDir = dir + typeDir + File.separator + studyID + File.separator + parentNm + File.separator;
          if (!new File(theDir).isDirectory()) {
              new File(theDir).mkdirs();
              logger.info("Made the directory " + theDir);
          }        
       		 		  
		File fileFolder = new File(theDir);
	    	
    	for (final File fileEntry : fileFolder.listFiles()) {
    	      if (fileEntry.isDirectory()) {
    	       ;
    	      } else {
    	        if (fileEntry.isFile()) {
    	          String fileName = fileEntry.getName();
    	          if (fileName.equals(fileNm)) {
    	        	  return fileEntry;
    	          }
    	            
    	        }

    	      }
    	    }
    	
    	return null;
		
	}
}
