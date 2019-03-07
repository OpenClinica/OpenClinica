package org.akaza.openclinica.service;

import java.util.ArrayList;
import java.util.Date;

import javax.servlet.http.HttpServletRequest;
import javax.sql.DataSource;

import org.akaza.openclinica.bean.core.Status;
import org.akaza.openclinica.bean.core.SubjectEventStatus;
import org.akaza.openclinica.bean.login.RestReponseDTO;
import org.akaza.openclinica.bean.login.UserAccountBean;
import org.akaza.openclinica.bean.managestudy.StudyBean;
import org.akaza.openclinica.bean.managestudy.StudyEventBean;
import org.akaza.openclinica.bean.managestudy.StudyEventDefinitionBean;
import org.akaza.openclinica.bean.managestudy.StudySubjectBean;
import org.akaza.openclinica.controller.helper.RestfulServiceHelper;
import org.akaza.openclinica.dao.managestudy.StudyDAO;
import org.akaza.openclinica.dao.managestudy.StudyEventDAO;
import org.akaza.openclinica.dao.managestudy.StudyEventDefinitionDAO;
import org.akaza.openclinica.dao.managestudy.StudySubjectDAO;
import org.akaza.openclinica.exception.OpenClinicaException;
import org.akaza.openclinica.web.restful.errors.ErrorConstants;
import org.apache.commons.dbcp2.BasicDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service("StudyEventService")
public class StudyEventServiceImpl implements StudyEventService {

    protected final Logger logger = LoggerFactory.getLogger(getClass().getName());

    @Autowired
    @Qualifier("dataSource")
    private BasicDataSource dataSource;
    
	private RestfulServiceHelper restfulServiceHelper;
	
	 /**
     *   DAOs
     */
    private StudyDAO msStudyDao = null;
    private StudySubjectDAO msStudySubjectDAO = null;
    private StudyEventDefinitionDAO sedDao = null;
    private StudyEventDAO seDao = null;
    private final String COMMON = "common";
    
	
	public ResponseEntity<Object> scheduleEvent(HttpServletRequest request, String studyOID, String siteOID,String studyEventOID,String participantId,String sampleOrdinalStr, String startDate,String endDate){
	    	ResponseEntity response = null;
	    	RestReponseDTO responseDTO = new RestReponseDTO();
	    	ArrayList<String>  errors = new ArrayList<String>();
	    	responseDTO.setErrors(errors);
	    	
	    	String message="";
	    	
	    	String studySubjectKey = participantId;
	    	String errMsg = null;
	    	StudyBean currentStudy = null;
	    	StudyBean currentSiteStudy = null;
	    	StudyEventDefinitionBean definition = null;
	    	StudySubjectBean studySubject = null;
	    	String startDateStr;
	    	String endDateStr;
	    	
		    try {   		    	
		    	/**
		    	 *  basic check 1: startDate and end Date                         
		    	 */
		    	Date startDt = null;
				Date endDt = null;
				
				if(startDate == null) {
					errMsg = "start date is missing";
		        	throw new OpenClinicaException(errMsg,ErrorConstants.ERR_NO_START_DATE);
				}else {
					startDt = this.getRestfulServiceHelper().getDateTime(startDate);	
				}
				
				
				endDt = this.getRestfulServiceHelper().getDateTime(endDate);
				
		    	if(startDt == null) {
		    		errMsg = "start date can't be parsed as a valid date,please enter in correct date format";
		        	throw new OpenClinicaException(errMsg,ErrorConstants.ERR_START_DATE);
		    	}else if(endDt != null) { 
		    		
		    		if(endDt.before(startDt)) {
			        	errMsg = "The endDate can not before startDate";
			        	throw new OpenClinicaException(errMsg,ErrorConstants.ERR_END_DATE_BEFORE_START_DATE);
			        }
		    	}	
		        
		    	/**
		    	 *  basic check 2: sampleOrdinal                         
		    	 */
		    	int sampleOrdinal = -999;
		    	try {
		    		if(sampleOrdinalStr != null) {
			    		sampleOrdinal = Integer.parseInt(sampleOrdinalStr);			
			    	}else {
			    		sampleOrdinal = 1;
			    	}
		    	}catch(NumberFormatException e) {
		    		errMsg = "The inputted ordinal is not an integer";
		        	throw new OpenClinicaException(errMsg,ErrorConstants.ERR_NOT_INTEGER);
		    	}
		    	
		    	/**
		    	 * Step 1: check study
		    	 */
		    	StudyDAO studyDao = this.getMsStudyDao();
		    	
		    	// check study first
		    	currentStudy = studyDao.findByOid(studyOID);
		    	
		    	if(currentStudy == null) {
		    		errMsg = "A new study event could not be scheduled, because the study {" + studyOID + "} is not existing in the system.";
		    		throw new OpenClinicaException(errMsg,ErrorConstants.ERR_STUDY_NOT_EXIST);
		    	}else if (currentStudy.getStatus().equals(Status.LOCKED)) {
		    		errMsg = "A new study event could not be scheduled, because the study {" + studyOID +"} has been LOCKED.";
		    		throw new OpenClinicaException(errMsg,ErrorConstants.ERR_STUDY_LOCKED);
		    	}
		    	// continue check site
		    	if(siteOID != null) {
		    		currentSiteStudy = studyDao.findSiteByOid(studyOID,siteOID);
		    		
		    		if(currentSiteStudy == null) {
		        		errMsg = "A new study event could not be scheduled if its study site {" + siteOID +"} is not existing in the system.";
		        		throw new OpenClinicaException(errMsg,ErrorConstants.ERR_SITE_NOT_EXIST);
		        	}
		    	}
		    	
		    	if(currentSiteStudy != null) {
		    		currentStudy = currentSiteStudy;
		    	}
		    	
		    	/**
		    	 *  step 2: permission check                        
		    	 */
		    	UserAccountBean  ub = this.getRestfulServiceHelper().getUserAccount(request);  
		    	String userName = ub.getName();
		    	if(studyOID != null && siteOID != null) {
		    		errMsg = this.getRestfulServiceHelper().verifyRole(userName, studyOID, siteOID);
		    	}else {
		    		errMsg = this.getRestfulServiceHelper().verifyRole(userName, studyOID, null);
		    	}
		    	
		        if (errMsg != null) {           
		             throw new OpenClinicaException(errMsg, errMsg);
		        }  
		    	    
		        /**
		         *  Step 3: check Subject/Participant              	
		         */
		        StudySubjectDAO sdao = this.getMsStudySubjectDAO();       
		     
		        studySubject = (StudySubjectBean) sdao.findByLabelAndStudy(participantId, currentStudy);
		        if(studySubject == null || (studySubject.getId() == 0 && studySubject.getLabel().trim().length() == 0)) {
		        	errMsg = "A study event could not be scheduled if the study subject {" + studySubjectKey +"} can not be found in the system.";
		        	throw new OpenClinicaException(errMsg,ErrorConstants.ERR_NO_SUBJECT_FOUND);
		        }
		        Status subjectStatus = studySubject.getStatus();
		        if ("removed".equalsIgnoreCase(subjectStatus.getName()) || "auto-removed".equalsIgnoreCase(subjectStatus.getName())) {
		        	errMsg = "A study event could not be scheduled if the study subject {" + studySubjectKey +"} has been removed.";
		        	throw new OpenClinicaException(errMsg,ErrorConstants.ERR_SUBJECT_REMOVED);
		        }
		      
		       
		        /**
		         *  Step 4: check study event                    
		         */
		        StudyEventDefinitionDAO seddao = this.getSedDao();
		        definition = seddao.findByOidAndStudy(studyEventOID,
		        		currentStudy.getId(), currentStudy.getParentStudyId());
		        
		        StudyBean studyWithEventDefinitions = currentStudy;
		        if (currentStudy.getParentStudyId() > 0) {
		            studyWithEventDefinitions = new StudyBean();
		            studyWithEventDefinitions.setId(currentStudy.getParentStudyId());
		        }
		        // find all active definitions with CRFs
		        if(definition == null) {
		        	errMsg ="The definition of event(" + studyEventOID + ") can not be found in the study(" + studyOID + ").";
		        	throw new OpenClinicaException(errMsg,ErrorConstants.ERR_EVENT_NOT_EXIST);
		        }else if (definition.getType().equals(COMMON)) {
		        	errMsg ="The type of event(" + studyEventOID + ") in the study(" + studyOID + ") is not a visit based event.";
		        	throw new OpenClinicaException(errMsg,ErrorConstants.ERR_WRONG_EVENT_TYPE);
		        }else if(!(definition.isRepeating())) {
		        	if(sampleOrdinal != 1) {
		        		errMsg ="The type of event(" + studyEventOID + ") in the study(" + studyOID + ") is a visit based NON repeating event,so ordinal must be 1.";
			        	throw new OpenClinicaException(errMsg,ErrorConstants.ERR_ORDINAL_NOT_ONE_FOR_NONREPEATING);
		        	}
		        }else{
		        	// repeating visited based event
		        }
		        
		            
		        if (!subjectMayReceiveStudyEvent(dataSource, definition, studySubject,sampleOrdinal)) {
		        	errMsg ="This event can't be scheduled, since the event is NON repeating, and an event of this type already exists for the specified participant.";
		        	throw new OpenClinicaException(errMsg,ErrorConstants.ERR_NON_REPEATING_ALREADY_EXISIT);
		           
		        }
		      
		       /**
		        * At this stage, it has passed all validation check
		        */
		        StudyEventDAO sed = this.getSeDao();
		        int maxSampleOrdinal = sed.getMaxSampleOrdinal(definition, studySubject) + 1;
		        if(sampleOrdinal > maxSampleOrdinal) {
		        	errMsg ="This type of event may not be scheduled to the specified participant, because the ordinal is out of sequential scope,the current ordinal can't be greater than " + maxSampleOrdinal;
		        	throw new OpenClinicaException(errMsg,ErrorConstants.ERR_GREATER_THAN_MAX_ORDINAL);
		        }
		        	        
		        StudyEventBean studyEvent = new StudyEventBean();
		        Date today = new Date();
		        studyEvent.setCreatedDate(today);
		        studyEvent.setUpdatedDate(today);
		        studyEvent.setStudyEventDefinitionId(definition.getId());
		        studyEvent.setStudySubjectId(studySubject.getId());
		              
		        studyEvent.setDateStarted(startDt);
		        studyEvent.setDateEnded(endDt);	        	       
		        studyEvent.setOwner(ub);
		        studyEvent.setStatus(Status.AVAILABLE);                                
		        studyEvent.setStudySubjectId(studySubject.getId());
		        studyEvent.setSubjectEventStatus(SubjectEventStatus.SCHEDULED);
		
		        studySubject = unsignSignedParticipant(studySubject);
		        sdao.update(studySubject);		       
		       	studyEvent.setSampleOrdinal(sampleOrdinal);	       
		
		        studyEvent = (StudyEventBean) sed.create(studyEvent);
		     
		        if (!studyEvent.isActive()) {
		            throw new OpenClinicaException("Event is not scheduled","NotActive");
		        }
		
	    	}catch(OpenClinicaException e) {
				 message = "Scheduled event " + studyEventOID + " for participant "+ participantId + " in study " + studyOID + " Failed.";
			     responseDTO.setMessage(message);	     
			     responseDTO.getErrors().add(e.getOpenClinicaMessage());
		
				 response = new ResponseEntity(responseDTO, org.springframework.http.HttpStatus.BAD_REQUEST);
			     return response;
	    	}
	       
	        /**
	         *  no any error, reply successful response
	         */	           	            
	        message = "Scheduled event " + studyEventOID + " for participant "+ participantId + " in study " + studyOID + " sucessfully.";
	        responseDTO.setMessage(message);

			response = new ResponseEntity(responseDTO, org.springframework.http.HttpStatus.OK);
	        return response;
	      
	    
	    }
	    
	    
	    /**
	     * Determines whether a subject may receive an additional study event. This
	     * is true if:
	     * <ul>
	     * <li>The study event definition is repeating; or
	     * <li>The subject does not yet have a study event for the given study event
	     * definition
	     * </ul>
	     *
	     * @param studyEventDefinition
	     *            The definition of the study event which is to be added for the
	     *            subject.
	     * @param studySubject
	     *            The subject for which the study event is to be added.
	     * @return <code>true</code> if the subject may receive an additional study
	     *         event, <code>false</code> otherwise.
	     */
	    public static boolean subjectMayReceiveStudyEvent(DataSource ds, StudyEventDefinitionBean studyEventDefinition, StudySubjectBean studySubject, int ordinal) throws OpenClinicaException {

	        StudyEventDAO sedao = new StudyEventDAO(ds);
	        ArrayList<StudyEventBean> allEvents = sedao.findAllByDefinitionAndSubject(studyEventDefinition, studySubject);
	      
	        if (studyEventDefinition.isRepeating()) {  
	        	for(StudyEventBean studyEvent:allEvents) {
	        		if(studyEvent.getSampleOrdinal() == ordinal) {
	        			throw new OpenClinicaException("found repeating event with same ordinal ","sameOrdinal");
	        		}
	        	}
	           
	        }else {
	        	  if (allEvents.size() > 0) {           
	                  return false;
	              }
	        }
	      

	        return true;
	    }
	    
	    private StudySubjectBean unsignSignedParticipant(StudySubjectBean studySubject) {
	        Status subjectStatus = studySubject.getStatus();
	        if (subjectStatus.equals(Status.SIGNED)){
	            studySubject.setStatus(Status.AVAILABLE);
	        }
	        return studySubject;
	    }
		public StudyDAO getMsStudyDao() {
			
			if(msStudyDao ==  null) {
				msStudyDao = new StudyDAO(dataSource);	
			}
			
			return msStudyDao;
		}

		 public RestfulServiceHelper getRestfulServiceHelper() {
		        if (restfulServiceHelper == null) {
		            restfulServiceHelper = new RestfulServiceHelper(this.dataSource);
		        }
		        return restfulServiceHelper;
		    }
		 
		 public void setMsStudyDao(StudyDAO msStudyDao) {
				this.msStudyDao = msStudyDao;
			}
			
			public StudySubjectDAO getMsStudySubjectDAO() {
				if(msStudySubjectDAO == null) {
					msStudySubjectDAO = new StudySubjectDAO(dataSource); 
				}
				
				return msStudySubjectDAO;
			}
			public void setMsStudySubjectDAO(StudySubjectDAO msStudySubjectDAO) {
				this.msStudySubjectDAO = msStudySubjectDAO;
			}
			
			public StudyEventDefinitionDAO getSedDao() {
				if(sedDao == null) {
					 sedDao = new StudyEventDefinitionDAO(dataSource);
				}
				
				return sedDao;
			}
			public void setSedDao(StudyEventDefinitionDAO sedDao) {
				this.sedDao = sedDao;
			}
			
			public StudyEventDAO getSeDao() {
				if(seDao == null) {
					 seDao = new StudyEventDAO(dataSource);
				}
				return seDao;
			}
			public void setSeDao(StudyEventDAO seDao) {
				this.seDao = seDao;
			}
	 
}
