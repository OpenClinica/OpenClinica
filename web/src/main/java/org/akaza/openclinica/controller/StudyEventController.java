package org.akaza.openclinica.controller;

import java.io.File;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.sql.DataSource;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import io.swagger.annotations.ApiParam;

import org.akaza.openclinica.bean.core.SubjectEventStatus;
import org.akaza.openclinica.bean.login.RestReponseDTO;
import org.akaza.openclinica.bean.login.ResponseSuccessStudyParticipantDTO;
import org.akaza.openclinica.bean.login.UserAccountBean;
import org.akaza.openclinica.bean.managestudy.StudyBean;
import org.akaza.openclinica.bean.managestudy.StudyEventBean;
import org.akaza.openclinica.bean.managestudy.StudyEventDefinitionBean;
import org.akaza.openclinica.bean.managestudy.StudySubjectBean;
import org.akaza.openclinica.controller.helper.RestfulServiceHelper;
import org.akaza.openclinica.dao.hibernate.EventCrfDao;
import org.akaza.openclinica.dao.hibernate.EventDefinitionCrfDao;
import org.akaza.openclinica.dao.hibernate.StudyEventDao;
import org.akaza.openclinica.dao.hibernate.StudyEventDefinitionDao;
import org.akaza.openclinica.dao.hibernate.StudyParameterValueDao;
import org.akaza.openclinica.dao.hibernate.StudySubjectDao;
import org.akaza.openclinica.dao.managestudy.StudySubjectDAO;
import org.akaza.openclinica.dao.managestudy.StudyDAO;
import org.akaza.openclinica.dao.managestudy.StudyEventDAO;
import org.akaza.openclinica.dao.managestudy.StudyEventDefinitionDAO;
import org.akaza.openclinica.bean.core.Status;
import org.akaza.openclinica.domain.datamap.EventCrf;
import org.akaza.openclinica.domain.datamap.EventDefinitionCrf;
import org.akaza.openclinica.domain.datamap.Study;
import org.akaza.openclinica.domain.datamap.StudyEvent;
import org.akaza.openclinica.domain.datamap.StudyEventDefinition;
import org.akaza.openclinica.domain.datamap.StudyParameterValue;
import org.akaza.openclinica.domain.datamap.StudySubject;
import org.akaza.openclinica.exception.OpenClinicaException;
import org.akaza.openclinica.i18n.util.ResourceBundleProvider;
import org.akaza.openclinica.patterns.ocobserver.StudyEventChangeDetails;
import org.akaza.openclinica.patterns.ocobserver.StudyEventContainer;
import org.akaza.openclinica.service.ParticipateService;
import org.akaza.openclinica.service.pmanage.ParticipantPortalRegistrar;
import org.apache.commons.dbcp2.BasicDataSource;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;


@Controller
@RequestMapping( value = "/auth/api" )
@Api( value = "Study Event", tags = {"Study Event"}, description = "REST API for Study Event" )
public class StudyEventController {

    @Autowired
    private ParticipateService participateService;

    @Autowired
    @Qualifier( "dataSource" )
    private BasicDataSource dataSource;

    @Autowired
    private EventCrfDao eventCrfDao;

    @Autowired
    private StudyEventDao studyEventDao;

    @Autowired
    private StudySubjectDao studySubjectDao;

    @Autowired
    private StudyEventDefinitionDao studyEventDefinitionDao;

    @Autowired
    private EventDefinitionCrfDao eventDefinitionCrfDao;
    private RestfulServiceHelper restfulServiceHelper;

    private final String COMMON = "common";
    protected final Logger logger = LoggerFactory.getLogger(getClass().getName());


    /**
     * @api {put} /pages/auth/api/v1/studyevent/studysubject/{studySubjectOid}/studyevent/{studyEventDefOid}/ordinal/{ordinal}/complete Complete a Participant Event
     * @apiName completeParticipantEvent
     * @apiPermission Authenticate using api-key. admin
     * @apiVersion 1.0.0
     * @apiParam {String} studySubjectOid Study Subject OID.
     * @apiParam {String} studyEventDefOid Study Event Definition OID.
     * @apiParam {Integer} ordinal Ordinal of Study Event Repetition.
     * @apiGroup Form
     * @apiHeader {String} api_key Users unique access-key.
     * @apiDescription Completes a participant study event.
     * @apiErrorExample {json} Error-Response:
     * HTTP/1.1 403 Forbidden
     * {
     * "code": "403",
     * "message": "Request Denied.  Operation not allowed."
     * }
     * @apiSuccessExample {json} Success-Response:
     * HTTP/1.1 200 OK
     * {
     * "code": "200",
     * "message": "Success."
     * }
     */
    @RequestMapping( value = "/studyevent/{studyEventDefOid}/ordinal/{ordinal}/complete", method = RequestMethod.PUT )
    public @ResponseBody
    Map<String, String> completeParticipantEvent(HttpServletRequest request, @PathVariable( "studyEventDefOid" ) String studyEventDefOid,
                                                 @PathVariable( "ordinal" ) Integer ordinal)
            throws Exception {
        String studyOid=(String)request.getSession().getAttribute("studyOid");
        UserAccountBean ub =(UserAccountBean) request.getSession().getAttribute("userBean");

        getRestfulServiceHelper().setSchema(studyOid, request);
        ResourceBundleProvider.updateLocale(new Locale("en_US"));
        if(ub==null){
            logger.info("userAccount is null");
            return null;
        }
        StudyBean currentStudy = participateService.getStudy(studyOid);
        StudySubjectDAO studySubjectDAO = new StudySubjectDAO(dataSource);

        String userName=ub.getName();
        int lastIndexOfDot= userName.lastIndexOf(".");
        String subjectOid=userName.substring(lastIndexOfDot+1);
        StudySubjectBean studySubject= studySubjectDAO.findByOid(subjectOid);

        StudyEvent studyEvent = studyEventDao.fetchByStudyEventDefOIDAndOrdinal(studyEventDefOid, ordinal, studySubject.getId());
        StudyEventDefinition studyEventDefinition = studyEventDefinitionDao.findByStudyEventDefinitionId(studyEvent.getStudyEventDefinition().getStudyEventDefinitionId());
        Study study = studyEventDefinition.getStudy();
        Map<String, String> response = new HashMap<String, String>();

        // Verify this request is allowed.
        if (!participateService.mayProceed(study.getOc_oid())) {
            response.put("code", String.valueOf(HttpStatus.FORBIDDEN.value()));
            response.put("message", "Request Denied.  Operation not allowed.");
            return response;
        }

        // Get list of eventCRFs
        // By this point we can assume all Participant forms have been submitted at least once and have an event_crf entry.
        // Non-Participant forms may not have an entry.
        List<EventDefinitionCrf> eventDefCrfs = eventDefinitionCrfDao.findByStudyEventDefinitionId(studyEventDefinition.getStudyEventDefinitionId());
        List<EventCrf> eventCrfs = eventCrfDao.findByStudyEventIdStudySubjectId(studyEvent.getStudyEventId(), studySubject.getOid());


        try {
            participateService.completeData(studyEvent, eventDefCrfs, eventCrfs);
        } catch (Exception e) {
            // Transaction has been rolled back due to an exception.
            logger.error("Error encountered while completing Study Event: " + e.getMessage());
            logger.error(ExceptionUtils.getStackTrace(e));

            response.put("code", String.valueOf(HttpStatus.INTERNAL_SERVER_ERROR.value()));
            response.put("message", "Error encountered while completing participant event.");
            return response;

        }


        response.put("code", String.valueOf(HttpStatus.OK.value()));
        response.put("message", "Success.");
        return response;
        //return new ResponseEntity<String>("<message>Success</message>", org.springframework.http.HttpStatus.OK);

    }
    public RestfulServiceHelper getRestfulServiceHelper() {
        if (restfulServiceHelper == null) {
            restfulServiceHelper = new RestfulServiceHelper(this.dataSource);
        }
        return restfulServiceHelper;
    }

    
    
    @ApiOperation(value = "To schedule an event for participant at study level",  notes = "Will read the information of SudyOID,ParticipantID, StudyEventOID, Ordinal, Start Date, End Date")
	@ApiResponses(value = {
	        @ApiResponse(code = 200, message = "Successful operation"),
	        @ApiResponse(code = 400, message = "Bad Request -- Normally means Found validation errors, for detail please see the error list: <br /> ")})
	@RequestMapping(value = "clinicaldata/studies/{studyOID}/participants/{subjectKey}/events", method = RequestMethod.POST,consumes = {"multipart/form-data"})
	public ResponseEntity<Object> scheduleEventAtStudyLevel(HttpServletRequest request,
			@RequestParam(value = "ordinal",required = false) String ordinal,
			@RequestParam("studyEventOID") String studyEventOID,
			@PathVariable("subjectKey") String subjectKey,			
			@ApiParam(value = "date format: yyyy-MM-dd") @RequestParam(value = "endDate",required = false) String endDate,
			@ApiParam(value = "date format: yyyy-MM-dd", required = true) @RequestParam("startDate") String startDate,			
			@PathVariable("studyOID") String studyOID) throws Exception {
		
		
    	return scheduleEvent(request, studyOID, null,studyEventOID,subjectKey,ordinal,startDate,endDate);
	}
    
    private ResponseEntity<Object> scheduleEvent(HttpServletRequest request, String studyOID, String siteOID,String studyEventOID,String participantId,String sampleOrdinalStr, String startDate,String endDate){
    	ResponseEntity response = null;
    	RestReponseDTO responseDTO = new RestReponseDTO();
    	ArrayList<String>  errors = new ArrayList<String>();
    	responseDTO.setErrors(errors);
    	
    	String message="";
    	
    	String studySubjectOID = null;
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
	        	throw new OpenClinicaException(errMsg,"StartDateMissing");
			}else {
				startDt = this.getRestfulServiceHelper().getDateTime(startDate);	
			}
			
			
			endDt = this.getRestfulServiceHelper().getDateTime(endDate);
			
	    	if(startDt == null) {
	    		errMsg = "start date can't be parsed as a valid date,please enter in correct date format";
	        	throw new OpenClinicaException(errMsg,"StartDateError");
	    	}else if(endDt != null) { 
	    		
	    		if(endDt.before(startDt)) {
		        	errMsg = "The endDate can not before startDate";
		        	throw new OpenClinicaException(errMsg,"endDateBeforeStartDate");
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
	        	throw new OpenClinicaException(errMsg,"ordinalNotNumber");
	    	}
	    	
	    	/**
	    	 * Step 1: check study
	    	 */
	    	StudyDAO studyDao = new StudyDAO(dataSource);
	    	
	    	// check study first
	    	currentStudy = studyDao.findByOid(studyOID);
	    	
	    	if(currentStudy == null) {
	    		errMsg = "A new study event could not be scheduled, because the study {" + studyOID + "} is not exsiting in the system.";
	    		throw new OpenClinicaException(errMsg,"NoStudyFound");
	    	}else if (currentStudy.getStatus().equals(Status.LOCKED)) {
	    		errMsg = "A new study event could not be scheduled, because the study {" + studyOID +"} has been LOCKED.";
	    		throw new OpenClinicaException(errMsg,"studyLocked");
	    	}
	    	// continue check site
	    	if(siteOID != null) {
	    		currentSiteStudy = studyDao.findSiteByOid(studyOID,siteOID);
	    		
	    		if(currentSiteStudy == null) {
	        		errMsg = "A new study event could not be scheduled if its study site {" + siteOID +"} is not exsiting in the system.";
	        		throw new OpenClinicaException(errMsg,"NoStudySiteFound");
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
	        StudySubjectDAO sdao = new StudySubjectDAO(dataSource);       
	     
	        studySubject = (StudySubjectBean) sdao.findByLabelAndStudy(participantId, currentStudy);
	        if(studySubject == null || (studySubject.getId() == 0 && studySubject.getLabel().trim().length() == 0)) {
	        	errMsg = "A study event could not be scheduled if the study subject {" + studySubjectOID +"} can not be found in the system.";
	        	throw new OpenClinicaException(errMsg,"subjectRemoved");
	        }
	        Status subjectStatus = studySubject.getStatus();
	        if ("removed".equalsIgnoreCase(subjectStatus.getName()) || "auto-removed".equalsIgnoreCase(subjectStatus.getName())) {
	        	errMsg = "A study event could not be scheduled if the study subject {" + studySubjectOID +"} has been removed.";
	        	throw new OpenClinicaException(errMsg,"subjectRemoved");
	        }
	      
	       
	        /**
	         *  Step 4: check study event                    
	         */
	        StudyEventDefinitionDAO seddao = new StudyEventDefinitionDAO(dataSource);
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
	        	throw new OpenClinicaException(errMsg,"noEventFound");
	        }else if (definition.getType().equals(COMMON)) {
	        	errMsg ="The type of event(" + studyEventOID + ") in the study(" + studyOID + ") is not a visit based event.";
	        	throw new OpenClinicaException(errMsg,"wrongTypeEvent");
	        }else if(!(definition.isRepeating())) {
	        	if(sampleOrdinal != 1) {
	        		errMsg ="The type of event(" + studyEventOID + ") in the study(" + studyOID + ") is a visit based NON repeating event,so ordinal must be 1.";
		        	throw new OpenClinicaException(errMsg,"ordinalNot1ForNONRepeating");
	        	}
	        }else{
	        	// repeating visited based event
	        }
	        
	            
	        if (!subjectMayReceiveStudyEvent(dataSource, definition, studySubject,sampleOrdinal)) {
	        	errMsg ="This event can't be scheduled, since the event is NON repeating, and an event of this type already exists for the specified participant.";
	        	throw new OpenClinicaException(errMsg,"NoneRepeatingFoundExist");
	           
	        }
	      
	       /**
	        * At this stage, it has passed all validation check
	        */
	        StudyEventDAO sed = new StudyEventDAO(dataSource);
	        int maxSampleOrdinal = sed.getMaxSampleOrdinal(definition, studySubject) + 1;
	        if(sampleOrdinal > maxSampleOrdinal) {
	        	errMsg ="This type of event may not be scheduled to the specified participant, because the ordinal is out of sequential scope,the current ordinal can't be greater than " + maxSampleOrdinal;
	        	throw new OpenClinicaException(errMsg,"greaterThanMaxSampleOrdinal");
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
    
}


