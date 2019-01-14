package org.akaza.openclinica.logic.importdata;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;

import javax.servlet.http.HttpServletRequest;

import org.akaza.openclinica.bean.admin.CRFBean;
import org.akaza.openclinica.bean.core.AuditableEntityBean;
import org.akaza.openclinica.bean.core.EntityBean;
import org.akaza.openclinica.bean.core.Status;
import org.akaza.openclinica.bean.core.SubjectEventStatus;
import org.akaza.openclinica.bean.login.UserAccountBean;
import org.akaza.openclinica.bean.managestudy.StudyBean;
import org.akaza.openclinica.bean.managestudy.StudyEventBean;
import org.akaza.openclinica.bean.managestudy.StudyEventDefinitionBean;
import org.akaza.openclinica.bean.managestudy.StudySubjectBean;
import org.akaza.openclinica.bean.submit.CRFVersionBean;
import org.akaza.openclinica.bean.submit.EventCRFBean;
import org.akaza.openclinica.bean.submit.FormLayoutBean;
import org.akaza.openclinica.bean.submit.SubjectBean;

import org.akaza.openclinica.core.SessionManager;
import org.akaza.openclinica.dao.admin.CRFDAO;
import org.akaza.openclinica.dao.core.CoreResources;
import org.akaza.openclinica.dao.login.UserAccountDAO;
import org.akaza.openclinica.dao.managestudy.StudyDAO;
import org.akaza.openclinica.dao.managestudy.StudyEventDAO;
import org.akaza.openclinica.dao.managestudy.StudyEventDefinitionDAO;
import org.akaza.openclinica.dao.managestudy.StudySubjectDAO;
import org.akaza.openclinica.dao.submit.CRFVersionDAO;
import org.akaza.openclinica.dao.submit.EventCRFDAO;
import org.akaza.openclinica.dao.submit.FormLayoutDAO;
import org.akaza.openclinica.dao.submit.SubjectDAO;
import org.akaza.openclinica.exception.OpenClinicaSystemException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.multipart.MultipartFile;

/**
 * ImportDataHelper the entire focus of this piece of code is to generate the
 * necessary EventCRFBeans after uploading XML to the Database. Currently being
 * used by ImportCRFDataServlet. Created as part of refactoring efforts.
 * 
 * @author Tom Hickerson, 04/2008
 * @category logic classes
 */
public class ImportDataHelper {

    protected final Logger logger = LoggerFactory.getLogger(getClass().getName());
    protected SessionManager sm;
    protected UserAccountBean ub;
    
    static private String importFileDir;
    private String personalImportFileDir;
    private String currentUserName;

    public void setSessionManager(SessionManager sm) {
        this.sm = sm;
    }

    public void setUserAccountBean(UserAccountBean ub) {
        this.ub = ub;
    }

    public EventCRFBean createEventCRF(HashMap<String, String> importedObject) {

        EventCRFBean eventCrfBean = null;

        int studyEventId = importedObject.get("study_event_id") == null ? -1 : Integer.parseInt(importedObject.get("study_event_id"));

        String crfVersionName = importedObject.get("crf_version_name") == null ? "" : importedObject.get("crf_version_name").toString();
        String crfName = importedObject.get("crf_name") == null ? "" : importedObject.get("crf_name").toString();

        String eventDefinitionCRFName = importedObject.get("event_definition_crf_name") == null ? ""
                : importedObject.get("event_definition_crf_name").toString();
        String subjectName = importedObject.get("subject_name") == null ? "" : importedObject.get("subject_name").toString();
        String studyName = importedObject.get("study_name") == null ? "" : importedObject.get("study_name").toString();

        logger.info("found the following: study event id " + studyEventId + ", crf version name " + crfVersionName + ", crf name " + crfName
                + ", event def crf name " + eventDefinitionCRFName + ", subject name " + subjectName + ", study name " + studyName);
        // << tbh
        int eventCRFId = 0;

        EventCRFDAO eventCrfDao = new EventCRFDAO(sm.getDataSource());
        StudyDAO studyDao = new StudyDAO(sm.getDataSource());
        StudySubjectDAO studySubjectDao = new StudySubjectDAO(sm.getDataSource());
        StudyEventDefinitionDAO studyEventDefinistionDao = new StudyEventDefinitionDAO(sm.getDataSource());
        CRFVersionDAO crfVersionDao = new CRFVersionDAO(sm.getDataSource());
        FormLayoutDAO fldao = new FormLayoutDAO(sm.getDataSource());
        StudyEventDAO studyEventDao = new StudyEventDAO(sm.getDataSource());
        CRFDAO crfdao = new CRFDAO(sm.getDataSource());
        SubjectDAO subjectDao = new SubjectDAO(sm.getDataSource());

        StudyBean studyBean = (StudyBean) studyDao.findByName(studyName);
        // .findByPK(studyId);

        // generate the subject bean first, so that we can have the subject id
        // below...
        SubjectBean subjectBean = subjectDao// .findByUniqueIdentifierAndStudy(subjectName,
                // studyBean.getId());
                .findByUniqueIdentifier(subjectName);

        StudySubjectBean studySubjectBean = studySubjectDao.findBySubjectIdAndStudy(subjectBean.getId(), studyBean);
        // .findByLabelAndStudy(subjectName, studyBean);
        logger.info("::: found study subject id here: " + studySubjectBean.getId() + " with the following: subject ID " + subjectBean.getId()
                + " study bean name " + studyBean.getName());

        StudyEventBean studyEventBean = (StudyEventBean) studyEventDao.findByPK(studyEventId);
        // TODO need to replace, can't really replace

        logger.info("found study event status: " + studyEventBean.getStatus().getName());

        // [study] event should be scheduled, event crf should be not started

        FormLayoutBean formLayout = (FormLayoutBean) fldao.findByFullName(crfVersionName, crfName);
        List<CRFVersionBean> crfVersions = crfVersionDao.findAllByCRFId(formLayout.getCrfId());
        CRFVersionBean crfVersion = crfVersions.get(0);
        // .findByPK(crfVersionId);
        // replaced by findByName(name, version)

        logger.info("found crf version name here: " + crfVersion.getName());

        EntityBean crf = crfdao.findByPK(crfVersion.getCrfId());

        logger.info("found crf name here: " + crf.getName());

        // trying it again up here since down there doesn't seem to work, tbh
        StudyEventDefinitionBean studyEventDefinitionBean = (StudyEventDefinitionBean) studyEventDefinistionDao.findByName(eventDefinitionCRFName);
        // .findByEventDefinitionCRFId(eventDefinitionCRFId);
        // replaced by findbyname

        if (studySubjectBean.getId() <= 0 && studyEventBean.getId() <= 0 && crfVersion.getId() <= 0 && studyBean.getId() <= 0
                && studyEventDefinitionBean.getId() <= 0) {
            logger.info("Throw an Exception, One of the provided ids is not valid");
        }

        // >> tbh repeating items:
        ArrayList eventCrfBeans = eventCrfDao.findByEventSubjectVersion(studyEventBean, studySubjectBean, crfVersion);
        // TODO repeating items here? not yet
        if (eventCrfBeans.size() > 1) {
            logger.info("found more than one");
        }
        if (!eventCrfBeans.isEmpty() && eventCrfBeans.size() == 1) {
            eventCrfBean = (EventCRFBean) eventCrfBeans.get(0);
            logger.info("This EventCrfBean was found");
        }
        if (!eventCrfBeans.isEmpty() && eventCrfBeans.size() > 1) {
            logger.info("Throw a System exception , result should either be 0 or 1");
        }

        if (eventCrfBean == null) {

            StudyBean studyWithSED = studyBean;
            if (studyBean.getParentStudyId() > 0) {
                studyWithSED = new StudyBean();
                studyWithSED.setId(studyBean.getParentStudyId());
            }

            AuditableEntityBean studyEvent = studyEventDao.findByPKAndStudy(studyEventId, studyWithSED);
            // TODO need to replace

            if (studyEvent.getId() <= 0) {
                logger.info("Hello Exception");
            }

            eventCrfBean = new EventCRFBean();
            // eventCrfBean.setCrf((CRFBean)crf);
            // eventCrfBean.setCrfVersion(crfVersion);
            if (eventCRFId == 0) {// no event CRF created yet
                // ???
                if (studyBean.getStudyParameterConfig().getInterviewerNameDefault().equals("blank")) {
                    eventCrfBean.setInterviewerName("");
                } else {
                    // default will be event's owner name
                    eventCrfBean.setInterviewerName(studyEventBean.getOwner().getName());
                }

                if (!studyBean.getStudyParameterConfig().getInterviewDateDefault().equals("blank")) {
                    if (studyEventBean.getDateStarted() != null) {
                        eventCrfBean.setDateInterviewed(studyEventBean.getDateStarted());// default
                        // date
                    } else {
                        // logger.info("evnet start date is null, so date
                        // interviewed is null");
                        eventCrfBean.setDateInterviewed(null);
                    }
                } else {
                    eventCrfBean.setDateInterviewed(null);
                }

                eventCrfBean.setAnnotations("");
                eventCrfBean.setCreatedDate(new Date());
                eventCrfBean.setCRFVersionId(crfVersion.getId());
                // eventCrfBean.setCrfVersion((CRFVersionBean)crfVersion);
                eventCrfBean.setOwner(ub);
                // eventCrfBean.setCrf((CRFBean)crf);
                eventCrfBean.setStatus(Status.AVAILABLE);
                eventCrfBean.setCompletionStatusId(1);
                // problem with the line below
                eventCrfBean.setStudySubjectId(studySubjectBean.getId());
                eventCrfBean.setStudyEventId(studyEventId);
                eventCrfBean.setValidateString("");
                eventCrfBean.setValidatorAnnotations("");
                eventCrfBean.setFormLayout(formLayout);

                try {
                    eventCrfBean = (EventCRFBean) eventCrfDao.create(eventCrfBean);
                    // TODO review
                    // eventCrfBean.setCrfVersion((CRFVersionBean)crfVersion);
                    // eventCrfBean.setCrf((CRFBean)crf);
                } catch (Exception ee) {
                    logger.info(ee.getMessage());
                    logger.info("throws with crf version id " + crfVersion.getId() + " and study event id " + studyEventId + " study subject id "
                            + studySubjectBean.getId());
                }
                // note that you need to catch an exception if the numbers are
                // bogus, ie you can throw an error here
                // however, putting the try catch allows you to pass which is
                // also bad
                // logger.info("CREATED EVENT CRF");
            } else {
                // there is an event CRF already, only need to update
                // is the status not started???

                logger.info("*** already-started event CRF with msg: " + eventCrfBean.getStatus().getName());
                if (eventCrfBean.getStatus().equals(Status.PENDING)) {
                    logger.info("Not Started???");
                }
                eventCrfBean = (EventCRFBean) eventCrfDao.findByPK(eventCRFId);
                eventCrfBean.setCRFVersionId(crfVersion.getId());

                eventCrfBean.setUpdatedDate(new Date());
                eventCrfBean.setUpdater(ub);
                eventCrfBean = (EventCRFBean) eventCrfDao.update(eventCrfBean);

                // eventCrfBean.setCrfVersion((CRFVersionBean)crfVersion);
                // eventCrfBean.setCrf((CRFBean)crf);
            }

            if (eventCrfBean.getId() <= 0) {
                logger.info("error");
            } else {
                // TODO change status here, tbh
                // 2/08 this part seems to work, tbh
                studyEventBean.setSubjectEventStatus(SubjectEventStatus.DATA_ENTRY_STARTED);
                studyEventBean.setUpdater(ub);
                studyEventBean.setUpdatedDate(new Date());
                studyEventDao.update(studyEventBean);

            }

        }
        eventCrfBean.setCrfVersion(crfVersion);
        eventCrfBean.setCrf((CRFBean) crf);

        // repeating?
        return eventCrfBean;
    }
    
    
    public String getImportFileDir(String studyOID) {
		  if (importFileDir != null) {
			  return importFileDir;
		  }else {
			  String dir = CoreResources.getField("filePath");
	          if (!new File(dir).exists()) {
	              logger.info("The filePath in datainfo.properties is invalid " + dir);             
	          }
	          // All the uploaded files will be saved in filePath/crf/original/
	          String theDir = dir + "import" + File.separator + studyOID + File.separator + "original" + File.separator;
	          if (!new File(theDir).isDirectory()) {
	              new File(theDir).mkdirs();
	              logger.info("Made the directory " + theDir);
	          }
	        
	         importFileDir = theDir;
		  }
		 
		return importFileDir;
	}
    
    public String getPersonalImportFileDir(HttpServletRequest request) {
    	  String userName = "";
    	  boolean prepareNewDir = false; 
    	  UserAccountBean userBean = null;
    	  int activeStudyId=-999;
    	  
		  if (personalImportFileDir != null) {
			  // All the uploaded files will be saved in filePath/import/userName/
	          userBean = this.getUserAccount(request);

	          if (userBean == null) {
	                String err_msg = "errorCode.InvalidUser:"+ "Please send request as a valid user";	               
	                return err_msg;
	          }else {	        	  
	        	  userName = userBean.getName();
	        	  if(this.getCurrentUserName().equals(userName)) {
	        		  return personalImportFileDir;
	        	  }else {
	        		  prepareNewDir = true;
	        		  activeStudyId = userBean.getActiveStudyId();
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
	        	  this.setCurrentUserName(userName);
	        	  userName = (userBean.getName()+"_" +userBean.getId()).toLowerCase().replace(" ","");
	        	  activeStudyId = userBean.getActiveStudyId();
	          }
	          
	          
	          String theDir = dir + "import" + File.separator + activeStudyId+ File.separator + userName + File.separator;
          	         
	          if (!new File(theDir).isDirectory()) {
	              new File(theDir).mkdirs();
	              logger.info("Made the directory " + theDir);
	          }
	        
	          personalImportFileDir = theDir;
		  }
		 
		return personalImportFileDir;
	}
   
    /**
     *  get data manager log file folder 
     * @param request
     * @return
     */
    public String getDMImportFileDir(HttpServletRequest request) {
  	  String userName = "";
  	  boolean prepareNewDir = false; 
  	  UserAccountBean userBean = null;
  	  int activeStudyId=-999;
  	  
		  if (personalImportFileDir != null) {
			  // All the uploaded files will be saved in filePath/import/userName/
	          userBean = this.getUserAccount(request);

	          if (userBean == null) {
	                String err_msg = "errorCode.InvalidUser:"+ "Please send request as a valid user";	               
	                return err_msg;
	          }else {	        	  
	        	  userName = userBean.getName();
	        	  if(this.getCurrentUserName().equals(userName)) {
	        		  return personalImportFileDir;
	        	  }else {
	        		  prepareNewDir = true;
	        		  activeStudyId = userBean.getActiveStudyId();
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
	        	  this.setCurrentUserName(userName);
	        	  userName = (userBean.getName()+"_" +userBean.getId()).toLowerCase().replace(" ","");
	        	  activeStudyId = userBean.getActiveStudyId();
	          }
	          
	          
	          String theDir = dir + "import" + File.separator + activeStudyId+ File.separator;
        	         
	          if (!new File(theDir).isDirectory()) {
	              new File(theDir).mkdirs();
	              logger.info("Made the directory " + theDir);
	          }
	        
	          personalImportFileDir = theDir;
		  }
		 
		return personalImportFileDir;
	}

    public void deleteTempImportFile(File file,String studyOID) {
    	String fileName = file.getName();
    	String importFileDir = this.getImportFileDir(studyOID);
    	
    	File tempFile = new File(importFileDir + fileName);
    	
    	if(tempFile.exists()) {
    		tempFile.delete();
    	}
    }
    
    public void deletePersonalTempImportFile(String fileName,HttpServletRequest request) {
    	
    	String importFileDir = this.getPersonalImportFileDir(request);
    	
    	File tempFile = new File(importFileDir + fileName);
    	
    	if(tempFile.exists()) {
    		tempFile.delete();
    	}
    }
    
    public ArrayList<File> splitDataFileAndProcesDataRowbyRow(File file,HttpServletRequest request) {
		ArrayList<File> fileList = new ArrayList<>();
	    BufferedReader reader;
	    
	    try {
            int count =1;	    	
	    		    	
	    	File splitFile;
	    	String importFileDir = this.getPersonalImportFileDir(request);
	    	
	    	reader = new BufferedReader(new FileReader(file));
	    	
	    	String orginalFileName = file.getName();
	    	int pos = orginalFileName.indexOf(".");
	    	orginalFileName = orginalFileName.substring(0,pos);
	    	
	    	String columnLine = reader.readLine();
	    	String line = columnLine;	    
	    	
	    	while (line != null) {
				//System.out.println(line);
				// read next line
				line = reader.readLine();
				
				splitFile = new File(importFileDir + orginalFileName +"_"+ count + ".txt");				
				FileOutputStream fos = new FileOutputStream(splitFile);			 
				BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(fos));
			 
				bw.write(columnLine);				
				bw.write("\n");
				if(line != null) {
					bw.write(line);	
					fileList.add(splitFile);
				}
				
			 
				bw.close();
							
				count++;
				
			}
			reader.close();
	        
	       
	    } catch (Exception e) {
	        e.printStackTrace();
	    }
	    
		return fileList;
	}
	
	/**
	 * 
	 * @param files
	 * @throws Exception
	 */
	public void saveFileToImportFolder(List<File> files,String studyOID) throws Exception {

  		
	  	File uplodedFile;
	  	String 	orginalFileName;
	  	BufferedReader reader;
	  	String line;
	  	String importFileDir = this.getImportFileDir(studyOID);
	  	
 		for (File file : files) {
 			reader = new BufferedReader(new FileReader(file));
	    	
 			orginalFileName = file.getName();
 			uplodedFile = new File(importFileDir + orginalFileName);				
			FileOutputStream fos = new FileOutputStream(uplodedFile);			 
			BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(fos));
		 
			line = reader.readLine();
			while(line != null) {
				bw.write(line);
				bw.write("\r");
				line = reader.readLine();
			}
			
		 
			bw.close();
						
 	 	 	 		
 				
 				
 		}
  
  }
	
    /**
     * 
     * @param orginalFileName
     * @param msg
     */
    public void writeToMatchAndSkipLog(String orginalFileName, String msg,HttpServletRequest request) {
		
    	BufferedWriter bw = null;
		FileWriter fw = null;
		boolean isNewFile = false;

	    String logFileName;
	    
	    try {
            int count =1;	    	
	    		    	
	    	File logFile;
	    	String importFileDir = this.getPersonalImportFileDir(request);
    	    
	    	//orginalFileName like: pipe_delimited_local_skip
	    	
	    	logFileName = importFileDir + orginalFileName + "_log.txt";
			logFile = new File(logFileName);
			
			/**
			 *  create new file and add first line
			 *  RowNo | ParticipantID | Status | Message
			 */
			if(!logFile.exists()) {
				logFile.createNewFile();
				isNewFile = true;				
			}
			
			// true = append file
			fw = new FileWriter(logFile.getAbsoluteFile(), true);
			bw = new BufferedWriter(fw);
            
			if(isNewFile) {				
				bw.write("RowNo|ParticipantID|Status|Message");	
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
    
    /**
     * 
     * @param request
     * @return
     */
    public ArrayList<File> getPersonalImportLogFile(HttpServletRequest request,File fileDir) {
    
    	ArrayList<File> fileList = new ArrayList<>();
    	String importFileDir = null;
    	File fileFolder = null;
    	
    	if(fileDir != null) {
    		fileFolder = fileDir;
    	}else {
	    	 // check user role
	    	if(this.hasDMrole(request)) {
	    		importFileDir = this.getDMImportFileDir(request);
	    	}else {
	    		importFileDir = this.getPersonalImportFileDir(request);
	    	}
	    	
	    	fileFolder = new File(importFileDir);
    	}
    	
    	// recursive call
    	for (final File fileEntry : fileFolder.listFiles()) {
    	      if (fileEntry.isDirectory()) {
    	    	  ArrayList<File> fileListFromSubDir = getPersonalImportLogFile(null,fileEntry);
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
    
    public File getPersonalImportLogFile(String fileNm,HttpServletRequest request) {
        
    	String importFileDir = this.getPersonalImportFileDir(request);    	
    	File fileFolder = new File(importFileDir);
    	
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
    	        UserAccountDAO userAccountDao = new UserAccountDAO(sm.getDataSource());
    	        userBean = (UserAccountBean) userAccountDao.findByUserName(username);
    	}
    	
    	return userBean;
       
	}
    
    public File[] convert(MultipartFile[] files,String studyOID)
    {    
        int size =  files.length;
        
        File[]  fileArray = new File[size];
        
        int i = 0;
        for(MultipartFile file :files) {
        	File convFile = new File(this.getImportFileDir(studyOID) + file.getOriginalFilename());
            try {
				convFile.createNewFile();
				FileOutputStream fos = new FileOutputStream(convFile); 
	            fos.write(file.getBytes());
	            fos.close(); 
	            fileArray[i]= convFile;
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} 
           
            i++;
        }
    	
        
        return fileArray;
    }  
    
    /**
     *  FormOID=F_DEMOGRAPHICS
		FormVersion=1
		StudyOID=S_TEST_STU(TEST)
		StudyEventOID=SE_COMMON

     * @param file
     * @throws IOException
     */
    public void validateMappingFile(File file) throws IOException,OpenClinicaSystemException{
       
    	boolean foundFormOID=false;
    	boolean foundFormVersion=false;
    	boolean foundStudyOID=false;
    	boolean foundStudyEventOID=false;
    			
        try(Scanner sc = new Scanner(file)){
       	
         String currentLine;
		
       	 while (sc.hasNextLine()) {
       		 currentLine = sc.nextLine();
       		 String[] mappingRow = currentLine.split("=");
       		 
       		 if(mappingRow.length == 2) {
       			 String keyWord = mappingRow[0];
           		 String value = mappingRow[1];
           		 if(keyWord != null && keyWord.trim().startsWith("FormOID") && value != null && value.trim().length() >0) {
           			foundFormOID=true;
           		 }
           		 
           		 if(keyWord != null && keyWord.trim().startsWith("FormVersion") && value != null && value.trim().length() >0) {
           			foundFormVersion=true;
                 }
           		 
           		 if(keyWord != null && keyWord.trim().startsWith("StudyOID") && value != null && value.trim().length() >0) {
           			foundStudyOID=true;
            	 }
           		 
           		 if(keyWord != null && keyWord.trim().startsWith("StudyEventOID") && value != null && value.trim().length() >0) {
           			foundStudyEventOID=true;
            	 } 
       		 }
       		
       		 
		    
		     }
		
		 }
		
        if(!foundFormOID) {
        	 throw new OpenClinicaSystemException("errorCode.noFormOID", "Please check mapping file, make sure that it has correct FormOID configuration.  ");
        }
        
        if(!foundFormVersion) {
       	 	throw new OpenClinicaSystemException("errorCode.noFormVersion", "Please check mapping file, make sure that it has correct FormVersion configuration.  ");
        }
        
        if(!foundStudyOID) {
       	 	throw new OpenClinicaSystemException("errorCode.noStudyOID", "Please check mapping file, make sure that it has correct StudyOID configuration.  ");
        }
        
        if(!foundStudyEventOID) {
       	 	throw new OpenClinicaSystemException("errorCode.noStudyEventOID", "Please check mapping file, make sure that it has correct StudyEventOID configuration.  ");
        }
        
    	
	 }
   /**
    * 
    * @param dataStr
    * @param orginalFileName
    * @return
    */
    public File saveDataToFile(String  dataStr,String orginalFileName,String studyOID) {
		
    	File dataFile = null;
    	
	    try {	    		    	
	    	String importFileDir = this.getImportFileDir(studyOID);	    	
	    	
	    	int pos = orginalFileName.indexOf(".");
	    	orginalFileName = orginalFileName.substring(0,pos);	    	
	    
	    	BufferedWriter bw = null;
	    	FileOutputStream fos = null;		
				
			if(dataStr != null) {
				dataFile = new File(importFileDir + orginalFileName + ".xml");				
				fos = new FileOutputStream(dataFile);			 
				bw = new BufferedWriter(new OutputStreamWriter(fos));
			 
				bw.write(dataStr);				
				
			}
			
		    if(bw !=null) {
		    	bw.close();
		    }
				    
	       
	    } catch (Exception e) {
	        e.printStackTrace();
	    }
	    
		return dataFile;
	}

public String getCurrentUserName() {
	return currentUserName;
}

public void setCurrentUserName(String currentUserName) {
	this.currentUserName = currentUserName;
}
   }
