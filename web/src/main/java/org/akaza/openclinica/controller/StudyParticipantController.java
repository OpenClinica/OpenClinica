package org.akaza.openclinica.controller;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ResourceBundle;

import javax.servlet.http.HttpServletRequest;
import javax.sql.DataSource;

import org.akaza.openclinica.bean.core.Role;
import org.akaza.openclinica.bean.core.Status;
import org.akaza.openclinica.bean.login.ErrorObject;
import org.akaza.openclinica.bean.login.ResponseDTO;
import org.akaza.openclinica.bean.login.ResponseFailureStudyParticipantDTO;
import org.akaza.openclinica.bean.login.ResponseFailureStudyParticipantSingleDTO;
import org.akaza.openclinica.bean.login.ResponseStudyParticipantsBulkDTO;
import org.akaza.openclinica.bean.login.ResponseSuccessListAllParticipantsByStudyDTO;
import org.akaza.openclinica.bean.login.ResponseSuccessStudyParticipantDTO;
import org.akaza.openclinica.bean.login.StudyParticipantDTO;
import org.akaza.openclinica.bean.login.StudyUserRoleBean;
import org.akaza.openclinica.bean.login.UserAccountBean;
import org.akaza.openclinica.bean.managestudy.StudyBean;
import org.akaza.openclinica.bean.managestudy.StudySubjectBean;
import org.akaza.openclinica.bean.managestudy.SubjectTransferBean;
import org.akaza.openclinica.controller.helper.RestfulServiceHelper;
import org.akaza.openclinica.dao.core.CoreResources;
import org.akaza.openclinica.dao.login.UserAccountDAO;
import org.akaza.openclinica.dao.managestudy.StudyDAO;
import org.akaza.openclinica.dao.managestudy.StudySubjectDAO;
import org.akaza.openclinica.exception.OpenClinicaException;
import org.akaza.openclinica.exception.OpenClinicaSystemException;
import org.akaza.openclinica.service.participant.ParticipantService;
import org.akaza.openclinica.validator.ParticipantValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.validation.DataBinder;
import org.springframework.validation.Errors;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import io.swagger.annotations.Api;

@Controller
@Api(value = "Participant", tags = { "Participant" }, description = "REST API for Study Participant")
@RequestMapping(value ="/auth/api/clinicaldata/studies")
public class StudyParticipantController {
	
		@Autowired
		@Qualifier("dataSource")
		private DataSource dataSource;
		
		@Autowired
		private UserAccountDAO udao;
		
		@Autowired
		private ParticipantService participantService;
	   
		private StudyDAO studyDao;
		private StudySubjectDAO ssDao;
		private UserAccountDAO userAccountDao;
		
		private String dateFormat;	 
		protected final Logger logger = LoggerFactory.getLogger(getClass().getName());
		
		@RequestMapping(value = "/{studyOID}/participants", method = RequestMethod.POST)
		public ResponseEntity<Object> createNewStudyParticipantAtStudyLevel(HttpServletRequest request, 
				@RequestBody HashMap<String, Object> map,
				@PathVariable("studyOID") String studyOID) throws Exception {
			
			return this.createNewStudySubject(request, map, studyOID, null);
		}
		
		
		@RequestMapping(value = "/{studyOID}/sites/{siteOID}/participants", method = RequestMethod.POST)
		public ResponseEntity<Object> createNewStudyParticipantAtSiteyLevel(HttpServletRequest request, 
				@RequestBody HashMap<String, Object> map,
				@PathVariable("studyOID") String studyOID,
				@PathVariable("siteOID") String siteOID) throws Exception {
			
			return this.createNewStudySubject(request, map, studyOID, siteOID);
		}
		
		@RequestMapping(value = "/{studyOID}/participants/bulk", method = RequestMethod.POST,consumes = {"multipart/form-data"})
		public ResponseEntity<Object> createNewStudyParticipantAtStudyLevel(HttpServletRequest request, 
				@RequestParam("file") MultipartFile file,
				//@RequestPart("json") Optional<JsonPojo> map,
				//@RequestParam("size") Integer size,				
				@PathVariable("studyOID") String studyOID) throws Exception {
			
		
			 return createNewStudyParticipantsInBulk(request, file, studyOID, null);
			
		}
		
		
		@RequestMapping(value = "/{studyOID}/sites/{siteOID}/participants/bulk", method = RequestMethod.POST,consumes = {"multipart/form-data"})
		public ResponseEntity<Object> createNewStudyParticipantAtSiteyLevel(HttpServletRequest request,
				@RequestParam("file") MultipartFile file,
				//@RequestParam("size") Integer size,				
				@PathVariable("studyOID") String studyOID,
				@PathVariable("siteOID") String siteOID) throws Exception {
			
			
            return createNewStudyParticipantsInBulk(request, file, studyOID, siteOID);
		}


		/**
		 * @param request
		 * @param file
		 * @param studyOID
		 * @param siteOID
		 * @return
		 * @throws Exception
		 */
		private ResponseEntity<Object> createNewStudyParticipantsInBulk(HttpServletRequest request, MultipartFile file,
				String studyOID, String siteOID) throws Exception {
			ResponseEntity response = null;
			
			ResponseStudyParticipantsBulkDTO responseStudyParticipantsBulkDTO = new ResponseStudyParticipantsBulkDTO();
			UserAccountBean  user = this.participantService.getUserAccount(request);
			String createdBy = user.getLastName() + " " + user.getFirstName(); 			
			responseStudyParticipantsBulkDTO.setCreatedBy(createdBy);  
			
			SimpleDateFormat  format = new SimpleDateFormat("dd-M-yyyy hh:mm:ss");
			String  DateToStr = format.format(new Date());
			responseStudyParticipantsBulkDTO.setCreatedAt(DateToStr);
			
			if (!file.isEmpty()) {
				
				try {
					 ArrayList<String> subjectKeyList = RestfulServiceHelper.readCSVFile(file);
				     
				     return this.createNewStudySubjectsInBulk(request, null, studyOID, siteOID, subjectKeyList);

				  } catch (Exception e) {
				    System.err.println(e.getMessage()); 
				    
					String validation_failed_message = e.getMessage();
				    responseStudyParticipantsBulkDTO.setMessage(validation_failed_message);					
				  }
				
			}else {								
				responseStudyParticipantsBulkDTO.setMessage("Can not read file " + file.getOriginalFilename());			 	
			}
			
			response = new ResponseEntity(responseStudyParticipantsBulkDTO, org.springframework.http.HttpStatus.BAD_REQUEST);
			return response;
		}

				
		/**
		 * 
		 * @param request
		 * @param map
		 * @return
		 * @throws Exception
		 */		
		public ResponseEntity<Object> createNewStudySubject(HttpServletRequest request, 
				@RequestBody HashMap<String, Object> map,
				 String studyOID,
				String siteOID) throws Exception {
			
			ArrayList<String> errorMessages = new ArrayList<String>();
			ErrorObject errorOBject = null;
			ResponseEntity<Object> response = null;
			String validation_failed_message = "VALIDATION FAILED";
			String validation_passed_message = "SUCCESS";
	   		    						
			SubjectTransferBean subjectTransferBean = this.transferToSubject(map);
			subjectTransferBean.setStudyOid(studyOID);
			String uri = request.getRequestURI();
			if(uri.indexOf("/sites/") >  0) {
				subjectTransferBean.setSiteIdentifier(siteOID);
			}
			
			StudyParticipantDTO studyParticipantDTO = this.buildStudyParticipantDTO(map);
					
			subjectTransferBean.setOwner(this.participantService.getUserAccount(request));
			StudyBean study = this.setSchema(studyOID, request);
			subjectTransferBean.setStudy(study);
			
			ParticipantValidator participantValidator = new ParticipantValidator(dataSource);
	        Errors errors = null;
	        
	        DataBinder dataBinder = new DataBinder(subjectTransferBean);
	        errors = dataBinder.getBindingResult();
	        participantValidator.validate(subjectTransferBean, errors);
	        
	        if(errors.hasErrors()) {
	        	ArrayList validerrors = new ArrayList(errors.getAllErrors());
	        	Iterator errorIt = validerrors.iterator();
	        	
	        	while(errorIt.hasNext()) {
	        		ObjectError oe = (ObjectError) errorIt.next();	        			        	
	        		errorMessages.add(oe.getDefaultMessage());
	        		
	        	}
	        }
	        
	        if (errorMessages != null && errorMessages.size() != 0) {
	        	ResponseFailureStudyParticipantDTO responseFailure = new ResponseFailureStudyParticipantDTO();
	        	responseFailure.setMessage(errorMessages);
	        	responseFailure.getParams().add("studyOID " + studyOID);
	        	if(subjectTransferBean.getSiteIdentifier() != null) {
	        		responseFailure.getParams().add("siteOID " + subjectTransferBean.getSiteIdentifier());
	        	}
	    		
	    		response = new ResponseEntity(responseFailure, org.springframework.http.HttpStatus.BAD_REQUEST);
	        } else {        				
			  	String label = create(subjectTransferBean,study);	           
			  	studyParticipantDTO.setSubjectKey(label);
	            ResponseSuccessStudyParticipantDTO responseSuccess = new ResponseSuccessStudyParticipantDTO();
	            
	            responseSuccess.setSubjectKey(studyParticipantDTO.getSubjectKey());	            
	            responseSuccess.setStatus("Available");

				response = new ResponseEntity(responseSuccess, org.springframework.http.HttpStatus.OK);
	        }
	        
			return response;
			
		}
		
		/** 
		 * @param request
		 * @param map
		 * @return
		 * @throws Exception
		 */		
		public ResponseEntity<Object> createNewStudySubjectsInBulk(HttpServletRequest request, 
				@RequestBody HashMap<String, Object> map,
				String studyOID,
				String siteOID,
				ArrayList subjectKeys) throws Exception {
			
			ArrayList<String> errorMsgsAll = new ArrayList<String>();
			ResponseEntity<Object> response = null;
			ResponseStudyParticipantsBulkDTO responseStudyParticipantsBulkDTO = new ResponseStudyParticipantsBulkDTO();
			
			String validation_failed_message = "Found validation failure,please see detail error message for each subjectKey";
			String validation_passed_message = "SUCCESS";
					
			StudyBean study = this.setSchema(studyOID, request);
			UserAccountBean  user = this.participantService.getUserAccount(request);
			
			int failureCount = 0;
			int uploadCount = subjectKeys.size();
			   		
			for(int i= 0; i < subjectKeys.size(); i++) {
				ArrayList<String> errorMsgs = new ArrayList<String>();
				SubjectTransferBean subjectTransferBean = new SubjectTransferBean();
				subjectTransferBean.setPersonId((String) subjectKeys.get(i));
				subjectTransferBean.setStudySubjectId((String) subjectKeys.get(i));
				subjectTransferBean.setStudyOid(studyOID);
				subjectTransferBean.setStudy(study);
				subjectTransferBean.setOwner(user);
				
				String uri = request.getRequestURI();
				if(uri.indexOf("/sites/") >  0) {
					subjectTransferBean.setSiteIdentifier(siteOID);
				}
							
			    StudyParticipantDTO studyParticipantDTO = new StudyParticipantDTO();			   
			    studyParticipantDTO.setSubjectKey((String) subjectKeys.get(i));
			    
				subjectTransferBean.setOwner(this.participantService.getUserAccount(request));
				ParticipantValidator participantValidator = new ParticipantValidator(dataSource);
				participantValidator.setBulkMode(true);
		        Errors errors = null;
		        
		        DataBinder dataBinder = new DataBinder(subjectTransferBean);
		        errors = dataBinder.getBindingResult();
		        participantValidator.validate(subjectTransferBean, errors);
		        
		        if(errors.hasErrors()) {
		        	ArrayList validerrors = new ArrayList(errors.getAllErrors());
		        	Iterator errorIt = validerrors.iterator();
		        	while(errorIt.hasNext()) {
		        		ObjectError oe = (ObjectError) errorIt.next();		        				        		
						errorMsgs.add(oe.getDefaultMessage());		        		
		        	}
		        }
		        
		        if (errorMsgs != null && errorMsgs.size() != 0) {		        	
		        	ResponseFailureStudyParticipantSingleDTO e = new ResponseFailureStudyParticipantSingleDTO();
		        	e.setSubjectKey(studyParticipantDTO.getSubjectKey());
		        	e.setMessage(errorMsgs);
		        	
		        	failureCount++;
		        	responseStudyParticipantsBulkDTO.getFailedParticipants().add(e);
		        } else {        				
				  	String label = create(subjectTransferBean,study);
				  	ResponseSuccessStudyParticipantDTO e = new ResponseSuccessStudyParticipantDTO();
				  	e.setSubjectKey(studyParticipantDTO.getSubjectKey());
				  	e.setStatus("Available");
				  	responseStudyParticipantsBulkDTO.getParticipants().add(e);
		           
		        }		        
		      
		        errorMsgsAll.addAll(errorMsgs);
			}
			
			String createdBy = user.getLastName() + " " + user.getFirstName(); 			
			responseStudyParticipantsBulkDTO.setCreatedBy(createdBy);  
			
			SimpleDateFormat  format = new SimpleDateFormat("dd-M-yyyy hh:mm:ss");
			String  DateToStr = format.format(new Date());
			responseStudyParticipantsBulkDTO.setCreatedAt(DateToStr);
			
			responseStudyParticipantsBulkDTO.setFailureCount(failureCount);
			responseStudyParticipantsBulkDTO.setUploadCount(uploadCount - failureCount);
			
		  if (errorMsgsAll != null && errorMsgsAll.size() != 0 && failureCount == uploadCount ) {	
			  responseStudyParticipantsBulkDTO.setMessage(validation_failed_message);
			  response = new ResponseEntity(responseStudyParticipantsBulkDTO, org.springframework.http.HttpStatus.BAD_REQUEST);
	      } else {
	    	  responseStudyParticipantsBulkDTO.setMessage(validation_passed_message); 
	            ResponseSuccessStudyParticipantDTO responseSuccess = new ResponseSuccessStudyParticipantDTO();	           
				response = new ResponseEntity(responseStudyParticipantsBulkDTO, org.springframework.http.HttpStatus.OK);
	      }

		  return response;
			
		}
		
		
		
		@RequestMapping(value = "/{studyOID}/participants", method = RequestMethod.GET)
		public ResponseEntity<Object> listStudySubjectsInStudy(@PathVariable("studyOID") String studyOid,HttpServletRequest request) throws Exception {
		
			return listStudySubjects(studyOid, null, request);
		}

		
		@RequestMapping(value = "/{studyOID}/sites/{sitesOID}/participants", method = RequestMethod.GET)
		public ResponseEntity<Object> listStudySubjectsInStudySite(@PathVariable("studyOID") String studyOid,@PathVariable("sitesOID") String siteOid,HttpServletRequest request) throws Exception {
		
			return listStudySubjects(studyOid, siteOid, request);
		}


		/**
		 * @param studyOid
		 * @param siteOid
		 * @param request
		 * @return
		 * @throws Exception
		 */
		private ResponseEntity<Object> listStudySubjects(String studyOid, String siteOid, HttpServletRequest request)
				throws Exception {
			ArrayList<ErrorObject> errorObjects = new ArrayList<ErrorObject>();
			ErrorObject errorOBject = null;
			ResponseEntity<Object> response = null;
			String validation_failed_message = "VALIDATION FAILED";
			String validation_passed_message = "SUCCESS";		
			
			 try {
		         	     
		            StudyBean studyBean = null;
		            try {
		            	StudyBean study = this.setSchema(studyOid, request);
		            	studyBean = this.participantService.validateRequestAndReturnStudy(studyOid, siteOid,request);
		            } catch (OpenClinicaSystemException e) {	                	               	                
		                errorOBject = createErrorObject("List Study Object failed", "studyRef:  " + studyOid + " siteRef: " + siteOid, e.getErrorCode());
		    			errorObjects.add(errorOBject);
		    			ResponseDTO responseDTO = new  ResponseDTO();
		    			responseDTO.setErrors(errorObjects);
		    			responseDTO.setMessage(e.getMessage());
		        		response = new ResponseEntity(responseDTO, org.springframework.http.HttpStatus.EXPECTATION_FAILED);
		            }
		            
		            if(studyBean != null) {
		            	ResponseSuccessListAllParticipantsByStudyDTO responseSuccess =  new ResponseSuccessListAllParticipantsByStudyDTO();
		            	
		            	ArrayList<StudyParticipantDTO> studyParticipantDTOs = getStudyParticipantDTOs(studyOid, siteOid,studyBean);
		            	  
		 	            responseSuccess.setMessage(validation_passed_message +  " - Found Study Subjects: " + studyParticipantDTOs.size() );
		            	responseSuccess.setStudyOid(studyOid);
		            	responseSuccess.setSiteOid(siteOid);
		 	            responseSuccess.setStudySubjects(studyParticipantDTOs);
		 	          
		            	
		 	            response = new ResponseEntity(responseSuccess, org.springframework.http.HttpStatus.OK);
		            }	           
		           
		        } catch (Exception eee) {
		            eee.printStackTrace();
		            throw eee;
		        }
			 
			return response;
		}
		
		/**
		 * @param studyIdentifier
		 * @param siteIdentifier
		 * @param study
		 * @return
		 * @throws Exception
		 */
		 private ArrayList<StudyParticipantDTO> getStudyParticipantDTOs(String studyOid, String siteOid,StudyBean study) throws Exception {
			 	      
		        List<StudySubjectBean> studySubjects = this.getStudySubjectDAO().findAllByStudy(study);
		        
		        ArrayList studyParticipantDTOs = new ArrayList<StudyParticipantDTO>(); 
		        
		        for(StudySubjectBean studySubject:studySubjects) {
		        	StudyParticipantDTO spDTO= new StudyParticipantDTO();
		        	        			        			        	
		        	spDTO.setSubjectKey(studySubject.getLabel());		        	
		        			        	
		        	studyParticipantDTOs.add(spDTO);
		        }
		        
		        return studyParticipantDTOs;
		    }
		 
		
	    
		/**
	     * Create the Subject object if it is not already in the system.
	     * 
	     * @param subjectTransferBean
	     * @return String
		 * @throws OpenClinicaException 
	     */
	    private String create(SubjectTransferBean subjectTransferBean,StudyBean currentStudy) throws OpenClinicaException {
	          logger.debug("creating subject transfer");
	          return this.participantService.createParticipant(subjectTransferBean,currentStudy);    
	    }
	    
	   
	    
		
		
	    /**
	     * 
	     * @param resource
	     * @param code
	     * @param field
	     * @return
	     */
		public ErrorObject createErrorObject(String resource, String code, String field) {
			ErrorObject errorOBject = new ErrorObject();
			errorOBject.setResource(resource);
			errorOBject.setCode(code);
			errorOBject.setField(field);
			return errorOBject;
		}
		
		/**
		 * 
		 * @param roleName
		 * @param resterm
		 * @return
		 */
		public Role getStudyRole(String roleName, ResourceBundle resterm) {
			if (roleName.equalsIgnoreCase(resterm.getString("Study_Director").trim())) {
				return Role.STUDYDIRECTOR;
			} else if (roleName.equalsIgnoreCase(resterm.getString("Study_Coordinator").trim())) {
				return Role.COORDINATOR;
			} else if (roleName.equalsIgnoreCase(resterm.getString("Investigator").trim())) {
				return Role.INVESTIGATOR;
			} else if (roleName.equalsIgnoreCase(resterm.getString("Data_Entry_Person").trim())) {
				return Role.RESEARCHASSISTANT;
			} else if (roleName.equalsIgnoreCase(resterm.getString("Monitor").trim())) {
				return Role.MONITOR;
			} else
				return null;
		}

		/**
		 * 
		 * @param map
		 * @return
		 * @throws ParseException
		 * @throws Exception
		 */
		 private SubjectTransferBean transferToSubject(HashMap<String, Object> map) throws ParseException, Exception {
		 	   			
			    String personId = (String) map.get("subjectKey");
			   
			    
			    SubjectTransferBean subjectTransferBean = new SubjectTransferBean();		       
		        	
		        subjectTransferBean.setPersonId(personId);
		        subjectTransferBean.setStudySubjectId(personId);
		        
		        return subjectTransferBean;
			 
		 }
		 
		 /**
		  * 
		  * @param map
		  * @return
		  * @throws ParseException
		  * @throws Exception
		  */
		 private StudyParticipantDTO buildStudyParticipantDTO(HashMap<String, Object> map) throws ParseException, Exception {
		 				
			    String subjectKey = (String) map.get("subjectKey");
			  
			    StudyParticipantDTO studyParticipanttDTO = new StudyParticipantDTO();			   
			    studyParticipanttDTO.setSubjectKey(subjectKey);        			  
		      
		        return studyParticipanttDTO;
			 
		 }
		 
		 
		 /**
	     * Helper Method to resolve a date provided as a string to a Date object.
	     * 
	     * @param dateAsString
	     * @return Date
	     * @throws ParseException
	     */
	    private Date getDate(String dateAsString) throws ParseException, Exception {
	        SimpleDateFormat sdf = new SimpleDateFormat(getDateFormat());
	        sdf.setLenient(false);
	        Date dd = sdf.parse(dateAsString);
	        Calendar c = Calendar.getInstance();
	        c.setTime(dd);
	        if (c.get(Calendar.YEAR) < 1900 || c.get(Calendar.YEAR) > 9999) {
	        	throw new Exception("Unparsable date: "+dateAsString);
	        }
	        return dd;
	    }

	    private int getYear(Date dt) throws ParseException, Exception {       
	        Calendar c = Calendar.getInstance();
	        c.setTime(dt);
	        
	        return c.get(Calendar.YEAR);
	    }
	    /**
	     * 
	     * @return
	     */
	    public String getDateFormat() {
			if(dateFormat == null) {
				dateFormat = "yyyy-MM-dd";
			}
			return dateFormat;
		}

	    /**
	     * 
	     * @param dateFormat
	     */
		public void setDateFormat(String dateFormat) {
			this.dateFormat = dateFormat;
		}
	   
		/**
		 * 
		 * @return
		 */
		 public StudyDAO getStudyDao() {
	        studyDao = studyDao != null ? studyDao : new StudyDAO(dataSource);
	        return studyDao;
	    }
		 
		 public UserAccountDAO getUserAccountDao() {
		        userAccountDao = userAccountDao != null ? userAccountDao : new UserAccountDAO(dataSource);
		        return userAccountDao;
		    }
		 
		 public StudySubjectDAO getStudySubjectDAO() {
		        ssDao = ssDao != null ? ssDao : new StudySubjectDAO(dataSource);
		        return ssDao;
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
				 throw new OpenClinicaSystemException("The study identifier you provided:" + studyOid + " is not valid.");
				 
	          }else {
	        	  schemaNm = study.getSchemaName();
	          }
			 
			 
			try {
				request.setAttribute("requestSchema",schemaNm);
				request.setAttribute("changeStudySchema",schemaNm);
				con = dataSource.getConnection();
				CoreResources.setSchema(con);
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	        // get correct study from the right DB schema 
			study = getStudyDao().findByOid(studyOid);
	         
	         return study;
		 }

}
