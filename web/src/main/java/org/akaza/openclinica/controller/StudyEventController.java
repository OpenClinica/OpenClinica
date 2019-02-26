package org.akaza.openclinica.controller;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.sql.DataSource;

import io.swagger.annotations.Api;
import org.akaza.openclinica.bean.login.UserAccountBean;
import org.akaza.openclinica.bean.managestudy.StudyBean;
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
import org.akaza.openclinica.domain.Status;
import org.akaza.openclinica.domain.datamap.EventCrf;
import org.akaza.openclinica.domain.datamap.EventDefinitionCrf;
import org.akaza.openclinica.domain.datamap.Study;
import org.akaza.openclinica.domain.datamap.StudyEvent;
import org.akaza.openclinica.domain.datamap.StudyEventDefinition;
import org.akaza.openclinica.domain.datamap.StudyParameterValue;
import org.akaza.openclinica.domain.datamap.StudySubject;
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
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

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

    @ApiOperation(value = "To schedule an event for participants at study or site level in bulk",  notes = "Will read the CSV file(CSV contains ParticipantID, StudyEventOID, Ordinal, Start Date, End Date)")
	@ApiResponses(value = {
	        @ApiResponse(code = 200, message = "Successful operation"),
	        @ApiResponse(code = 400, message = "Bad Request -- Normally means Found validation errors, for detail please see the error list: <br /> ")})
	@RequestMapping(value = "clinicaldata/studies/{studyOID}/sites/{sitesOID}/events/bulk ", method = RequestMethod.POST,consumes = {"multipart/form-data"})
	public ResponseEntity<Object> scheduleEventAtSiteLevel(HttpServletRequest request,
			@RequestParam("file") MultipartFile file,
			//@RequestParam("size") Integer size,				
			@PathVariable("studyOID") String studyOID,
			@PathVariable("siteOID") String siteOID) throws Exception {
		
		
        return scheduleEventInBulk(request, file, studyOID, siteOID);
	}
    
    @ApiOperation(value = "To schedule an event for participants at study or site level in bulk",  notes = "Will read the CSV file(CSV contains ParticipantID, StudyEventOID, Ordinal, Start Date, End Date)")
	@ApiResponses(value = {
	        @ApiResponse(code = 200, message = "Successful operation"),
	        @ApiResponse(code = 400, message = "Bad Request -- Normally means Found validation errors, for detail please see the error list: <br /> ")})
	@RequestMapping(value = "clinicaldata/studies/{studyOID}/events/bulk ", method = RequestMethod.POST,consumes = {"multipart/form-data"})
	public ResponseEntity<Object> scheduleEventAtStudyLevel(HttpServletRequest request,
			@RequestParam("file") MultipartFile file,
			//@RequestParam("size") Integer size,				
			@PathVariable("studyOID") String studyOID) throws Exception {
		
		
        return scheduleEventInBulk(request, file, studyOID, null);
	}
    
    private void scheduleEventInBulk(HttpServletRequest request, File file, String studyOID, String siteOID);{

    	String studyEventOID = null;
    	String studySubjectOID = null;
    	String errMsg = null;
    	StudyBean currentStudy = null;
    	StudyBean currentSiteStudy = null;
    	StudyEventDefinitionBean definition = null;
    	StudySubjectBean studySubject = null;
    	String startDateStr;
    	String endDateStr;
    	Date startDate;
    	Date endDate;
    	
    	/**
    	 * Step 1: check study
    	 */
    	StudyDAO studyDao = new StudyDAO(sm.getDataSource());
    	
    	// check study level first
    	currentStudy = studyDao.findByOid(studyOID);
    	
    	if(currentStudy == null) {
    		errMsg = "A new study event could not be scheduled if its study {" + studyOID "} is not exsiting in the system.";
    	}
    	
    	if (currentStudy.getStatus().equals(Status.LOCKED)) {
    		errMsg = "A new study event could not be scheduled if its study {" + studyOID "} has been LOCKED.";
    	}
    	// continue check site level
    	if(siteOID != null) {
    		currentSiteStudy = studyDao.findSiteByOid(studyOID,siteOID);
    		
    		if(currentSiteStudy == null) {
        		errMsg = "A new study event could not be scheduled if its study site {" + siteOID "} is not exsiting in the system.";
        	}
    	}
    	
    	if(currentSiteStudy != null) {
    		currentStudy = currentSiteStudy;
    	}
    	
    	    
        /**
         *  Step 2: check Subject              	
         */
        StudySubjectDAO sdao = new StudySubjectDAO(sm.getDataSource());       
     
        studySubject = (StudySubjectBean) sdao.findByOid(studySubjectOID);
        //StudySubjectBean studySubject = sdao.findByLabelAndStudy(fp.getString(INPUT_STUDY_SUBJECT_LABEL), currentStudy);
        Status subjectStatus = studySubject.getStatus();
        if ("removed".equalsIgnoreCase(subjectStatus.getName()) || "auto-removed".equalsIgnoreCase(subjectStatus.getName())) {
        	errMsg = "A new study event could not be scheduled if its study subject {" + studySubjectOID +"} has been removed."

        	//return and stop here
        }
      
       
        // Step 3: check study event                    
        StudyEventDefinitionDAO seddao = new StudyEventDefinitionDAO(sm.getDataSource());
        definition = studyEventDefinitionDAO.findByOidAndStudy(studyEventOID,
        		currentStudy.getId(), currentStudy.getParentStudyId());
   
        StudyBean studyWithEventDefinitions = currentStudy;
        if (currentStudy.getParentStudyId() > 0) {
            studyWithEventDefinitions = new StudyBean();
            studyWithEventDefinitions.setId(currentStudy.getParentStudyId());
        }
        // find all active definitions with CRFs
        ArrayList<StudyEventDefinitionBean> eventDefinitions = seddao.findAllActiveByStudy(studyWithEventDefinitions);
        ArrayList<StudyEventDefinitionBean> tempList = new ArrayList<>();
        for (StudyEventDefinitionBean eventDefinition : eventDefinitions) {
            if (!eventDefinition.getType().equals(COMMON)) {
            	tempList.add(eventDefinition);
            }
        }
        
        eventDefinitions = new ArrayList(tempList);       
        Collections.sort(eventDefinitions);
      
        ArrayList eventDefinitionsScheduled = new ArrayList(eventDefinitions);

     ////////////////////

            String dateCheck2 = request.getParameter("startDate");
            String endCheck2 = request.getParameter("endDate");
            logger.debug(dateCheck2 + "; " + endCheck2);
     
         
            Date start = getInputStartDate();
            Date end = null;
          
            if (!subjectMayReceiveStudyEvent(sm.getDataSource(), definition, studySubject)) {
            	errMsg ="This type of event may not be scheduled to the specified participant, since the event definition is not repeating, and an event of this type already exists for the specified participant.";
               
            }

            
            // check startDate and end Date
            if (!"".equals(strEnd) && !errors.containsKey(INPUT_STARTDATE_PREFIX) && !errors.containsKey(INPUT_ENDDATE_PREFIX)) {
                end = getInputEndDate();
                if (!fp.getString(INPUT_STARTDATE_PREFIX + "Date").equals(fp.getString(INPUT_ENDDATE_PREFIX + "Date"))) {
                    if (end.before(start)) {
                        Validator.addError(errors, INPUT_ENDDATE_PREFIX, resexception.getString("input_provided_not_occure_after_previous_start_date_time"));
                    }
                } else {
                    // if in same date, only check when both had time entered
                    if (fp.timeEntered(INPUT_STARTDATE_PREFIX) && fp.timeEntered(INPUT_ENDDATE_PREFIX)) {
                        if (end.before(start) || end.equals(start)) {
                            Validator.addError(errors, INPUT_ENDDATE_PREFIX,
                                    resexception.getString("input_provided_not_occure_after_previous_start_date_time"));
                        }
                    }
                }
            }

            // pass all validation check
            StudyEventDAO sed = new StudyEventDAO(sm.getDataSource());

            StudyEventBean studyEvent = new StudyEventBean();
            Date today = new Date();
            studyEvent.setCreatedDate(today);
            studyEvent.setUpdatedDate(today);
            studyEvent.setStudyEventDefinitionId(definition.getId());
            studyEvent.setStudySubjectId(studySubject.getId());
              
                studyEvent.setDateStarted(startDate);
                studyEvent.setDateEnded(endDate);
              
                studyEvent.setOwner(ub);
                studyEvent.setStatus(Status.AVAILABLE);                                
                studyEvent.setStudySubjectId(studySubject.getId());
                studyEvent.setSubjectEventStatus(SubjectEventStatus.SCHEDULED);

                studySubject = unsignSignedParticipant(studySubject);
                sdao.update(studySubject);

                studyEvent.setSampleOrdinal(sed.getMaxSampleOrdinal(definition, studySubject) + 1);

                studyEvent = (StudyEventBean) sed.create(studyEvent);
             
                if (!studyEvent.isActive()) {
                    throw new OpenClinicaException(restext.getString("event_not_created_in_database"), "2");
                }
               
///////////
               
                StudyEventBean studyEventScheduled = new StudyEventBean();
                studyEventScheduled.setStudyEventDefinitionId(scheduledDefinitionIds[i]);
                studyEventScheduled.setStudySubjectId(studySubject.getId());

                // YW 11-14-2007
                if ("-1".equals(fp.getString(INPUT_STARTDATE_PREFIX_SCHEDULED[i] + "Hour"))
                        && "-1".equals(fp.getString(INPUT_STARTDATE_PREFIX_SCHEDULED[i] + "Minute"))
                        && "".equals(fp.getString(INPUT_STARTDATE_PREFIX_SCHEDULED[i] + "Half"))) {
                    studyEventScheduled.setStartTimeFlag(false);
                } else {
                    studyEventScheduled.setStartTimeFlag(true);
                }
                // YW >>

                studyEventScheduled.setDateStarted(startScheduled[i]);
                // YW, 3-12-2008, 2220 fix<<
                if (!"".equals(strEndScheduled[i])) {
                    endScheduled[i] = fp.getDateTime(INPUT_ENDDATE_PREFIX_SCHEDULED[i]);
                    if ("-1".equals(fp.getString(INPUT_ENDDATE_PREFIX_SCHEDULED[i] + "Hour"))
                            && "-1".equals(fp.getString(INPUT_ENDDATE_PREFIX_SCHEDULED[i] + "Minute"))
                            && "".equals(fp.getString(INPUT_ENDDATE_PREFIX_SCHEDULED[i] + "Half"))) {
                        studyEventScheduled.setEndTimeFlag(false);
                    } else {
                        studyEventScheduled.setEndTimeFlag(true);
                    }
                }
                studyEventScheduled.setDateEnded(endScheduled[i]);
                // YW >>
                studyEventScheduled.setOwner(ub);
                studyEventScheduled.setStatus(Status.AVAILABLE);
                studyEventScheduled.setLocation(fp.getString(INPUT_SCHEDULED_LOCATION[i]));
                studyEvent.setSubjectEventStatus(SubjectEventStatus.SCHEDULED);

                // subjectsExistingEvents =
                // sed.findAllByStudyAndStudySubjectId(
                // currentStudy,
                // studySubject.getId());
                studyEventScheduled.setSampleOrdinal(sed.getMaxSampleOrdinal(definitionScheduleds.get(i), studySubject) + 1);
                // System.out.println("create scheduled events");
                studyEventScheduled = (StudyEventBean) sed.create(studyEventScheduled);
                if (!studyEventScheduled.isActive()) {
                    throw new OpenClinicaException(restext.getString("scheduled_event_not_created_in_database"), "2");
                }

          
                       
                    


               
                return;
           
      
    
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
    public static boolean subjectMayReceiveStudyEvent(DataSource ds, StudyEventDefinitionBean studyEventDefinition, StudySubjectBean studySubject) {

        if (studyEventDefinition.isRepeating()) {          
            return true;
        }

        StudyEventDAO sedao = new StudyEventDAO(ds);
        ArrayList allEvents = sedao.findAllByDefinitionAndSubject(studyEventDefinition, studySubject);

        if (allEvents.size() > 0) {           
            return false;
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


