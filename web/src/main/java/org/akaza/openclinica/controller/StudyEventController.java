package org.akaza.openclinica.controller;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

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
import org.akaza.openclinica.bean.login.StudyUserRoleBean;
import org.akaza.openclinica.bean.login.UserAccountBean;
import org.akaza.openclinica.bean.managestudy.StudyBean;
import org.akaza.openclinica.bean.managestudy.StudyEventBean;
import org.akaza.openclinica.bean.managestudy.StudyEventDefinitionBean;
import org.akaza.openclinica.bean.managestudy.StudySubjectBean;
import org.akaza.openclinica.bean.submit.crfdata.CRFDataPostImportContainer;
import org.akaza.openclinica.bean.submit.crfdata.ODMContainer;
import org.akaza.openclinica.bean.submit.crfdata.StudyEventDataBean;
import org.akaza.openclinica.bean.submit.crfdata.SubjectDataBean;
import org.akaza.openclinica.controller.dto.StudyEventResponseDTO;
import org.akaza.openclinica.controller.dto.StudyEventScheduleDTO;
import org.akaza.openclinica.controller.dto.StudyEventScheduleRequestDTO;
import org.akaza.openclinica.controller.dto.StudyEventUpdateRequestDTO;
import org.akaza.openclinica.controller.helper.RestfulServiceHelper;
import org.akaza.openclinica.dao.core.CoreResources;
import org.akaza.openclinica.dao.hibernate.*;
import org.akaza.openclinica.dao.login.UserAccountDAO;
import org.akaza.openclinica.dao.managestudy.StudySubjectDAO;
import org.akaza.openclinica.dao.managestudy.StudyDAO;
import org.akaza.openclinica.dao.managestudy.StudyEventDAO;
import org.akaza.openclinica.dao.managestudy.StudyEventDefinitionDAO;
import org.akaza.openclinica.bean.core.Status;
import org.akaza.openclinica.domain.datamap.*;
import org.akaza.openclinica.domain.enumsupport.EndpointType;
import org.akaza.openclinica.domain.enumsupport.JobType;
import org.akaza.openclinica.domain.user.UserAccount;
import org.akaza.openclinica.exception.OpenClinicaException;
import org.akaza.openclinica.exception.OpenClinicaSystemException;
import org.akaza.openclinica.i18n.util.ResourceBundleProvider;
import org.akaza.openclinica.patterns.ocobserver.StudyEventChangeDetails;
import org.akaza.openclinica.patterns.ocobserver.StudyEventContainer;
import org.akaza.openclinica.service.*;
import org.akaza.openclinica.service.crfdata.ErrorObj;
import org.akaza.openclinica.service.pmanage.ParticipantPortalRegistrar;
import org.akaza.openclinica.service.rest.errors.ParameterizedErrorVM;
import org.akaza.openclinica.web.restful.errors.ErrorConstants;
import org.apache.commons.dbcp2.BasicDataSource;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.commons.collections4.map.PassiveExpiringMap;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
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
	private UserAccountDao userAccountDao;

	@Autowired
	private StudyDao studyDao;

    @Autowired
    private StudyEventDefinitionDao studyEventDefinitionDao;

    @Autowired
    private EventDefinitionCrfDao eventDefinitionCrfDao;
    private RestfulServiceHelper restfulServiceHelper;

    @Autowired
	private StudyEventService studyEventService;

    @Autowired
	private CSVService csvService;

	@Autowired
	private UserService userService;
	
	@Autowired
    private UtilService utilService;

	@Autowired
	private ValidateService validateService;

    PassiveExpiringMap<String, Future<ResponseEntity<Object>>> expiringMap =
            new PassiveExpiringMap<>(24, TimeUnit.HOURS);
    
    protected final Logger logger = LoggerFactory.getLogger(getClass().getName());
	public static final String DASH = "-";
	public static final String SCHEDULE_EVENT = "_Schedule Event";
	public static final String FILE_HEADER_MAPPING = "ParticipantID, StudyEventOID, Ordinal, StartDate, EndDate";
	public static final String SEPERATOR = ",";
	SimpleDateFormat sdf_fileName = new SimpleDateFormat("yyyy-MM-dd'-'HHmmssSSS'Z'");
	public static final String CREATE = "create";
	public static final String UPDATE = "update";

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

    @ApiOperation(value = "To schedule an event for participant at site level",  notes = "Will read the information of SudyOID,ParticipantID, StudyEventOID, Ordinal, Start Date, End Date")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successful operation"),
            @ApiResponse(code = 400, message = "Bad Request -- Normally means Found validation errors, for detail please see the error list: <br /> ")})
    @RequestMapping(value = "clinicaldata/studies/{studyOID}/sites/{siteOID}/participants/{subjectKey}/events", method = RequestMethod.POST)
    public ResponseEntity<Object> scheduleEventAtSiteLevel(HttpServletRequest request,
            @RequestBody StudyEventScheduleRequestDTO studyEventScheduleRequestDTO,
            @PathVariable("subjectKey") String subjectKey,
            @PathVariable("studyOID") String studyOID,
            @PathVariable("siteOID") String siteOID) throws Exception {
		utilService.setSchemaFromStudyOid(studyOID);

		String studyEventOID = studyEventScheduleRequestDTO.getStudyEventOID();
        String startDate = studyEventScheduleRequestDTO.getStartDate();
        String endDate = studyEventScheduleRequestDTO.getEndDate();
        
    	
    	return scheduleEvent(request, studyOID, siteOID,studyEventOID,subjectKey,startDate,endDate);
	}
    

    
    @ApiOperation(value = "To schedule an event for participants at site level in bulk",  notes = "Will read the information of SudyOID,ParticipantID, StudyEventOID, Ordinal, Start Date, End Date")
	@ApiResponses(value = {
	        @ApiResponse(code = 200, message = "Successful operation"),
	        @ApiResponse(code = 400, message = "Bad Request -- Normally means Found validation errors, for detail please see the error list: <br /> ")})
	@RequestMapping(value = "clinicaldata/studies/{studyOID}/sites/{siteOID}/events/bulk", method = RequestMethod.POST,consumes = {"multipart/form-data"})
	public ResponseEntity<Object> scheduleBulkEventAtSiteLevel(HttpServletRequest request,
			MultipartFile file,
			@PathVariable("studyOID") String studyOID,
			@PathVariable("siteOID") String siteOID) throws Exception {
		
    	ResponseEntity response = null;
    	utilService.setSchemaFromStudyOid(studyOID);
        String schema = CoreResources.getRequestSchema();
         
    	response= checkFileFormat(file);
    	if(response != null) {
    		return response;
    	}    	
    	
    	UserAccountBean ub = getUserAccount(request);

		Study site = studyDao.findByOcOID(siteOID.trim());
		Study study = studyDao.findByOcOID(studyOID.trim());
		
		response= checkStudy(study);
    	if(response != null) {
    		return response;
    	}   
    	
		UserAccount userAccount = userAccountDao.findById(ub.getId());
		JobDetail jobDetail= userService.persistJobCreated(study, site, userAccount, JobType.SCHEDULE_EVENT,file.getOriginalFilename());

    	 CompletableFuture<ResponseEntity<Object>> future = CompletableFuture.supplyAsync(() -> {
    		 return scheduleEvent(request,file, study, siteOID,ub,jobDetail,schema);
    	 });

		String uuid = jobDetail.getUuid();
          synchronized (expiringMap) {
              expiringMap.put(uuid, future);
          }
          
          RestReponseDTO responseDTO = new RestReponseDTO();
    		String finalMsg = "The schedule job is running, here is the schedule job ID:" + uuid;
    		responseDTO.setMessage(finalMsg);
    		response = new ResponseEntity(responseDTO, org.springframework.http.HttpStatus.OK);
    		
    		return response;
    	
	}
    
	private ResponseEntity checkFileFormat(MultipartFile file) {
		ResponseEntity response = null;
		RestReponseDTO responseDTO = new RestReponseDTO();
		String finalMsg = null;
		
		//only support csv file
        if (file !=null && file.getSize() > 0) {
      	  String fileNm = file.getOriginalFilename();
      	  
      	  if (fileNm!=null && fileNm.endsWith(".csv")) {
      		 String line;
      		 BufferedReader reader;
			 InputStream is;
			try {
				 is = file.getInputStream();			
				 reader = new BufferedReader(new InputStreamReader(is));					 
				 CSVFormat csvFileFormat = CSVFormat.DEFAULT.withHeader(FILE_HEADER_MAPPING).withFirstRecordAsHeader().withTrim();
	
		         CSVParser csvParser = new CSVParser(reader, csvFileFormat);
		         csvParser.parse(reader, csvFileFormat);
			} catch (Exception e) {
				finalMsg = ErrorConstants.ERR_NOT_CSV_FILE+ ":The file format is not supported, please use correct CSV file, like *.csv ";
	       	 	responseDTO.setMessage(finalMsg);
	       		response = new ResponseEntity(responseDTO, org.springframework.http.HttpStatus.BAD_REQUEST);
			}
			
      	  }else {     		      		             
       		 finalMsg = ErrorConstants.ERR_NOT_CSV_FILE+ ":The file format is not supported, please use correct CSV file, like *.csv ";
       	 	 responseDTO.setMessage(finalMsg);
       		 response = new ResponseEntity(responseDTO, org.springframework.http.HttpStatus.BAD_REQUEST);      		       		
      	  } 
      	  
      	  
        }else {
        	 finalMsg = ErrorConstants.ERR_BLANK_FILE+ ":The file null or blank";
       	 	 responseDTO.setMessage(finalMsg);
       		 response = new ResponseEntity(responseDTO, org.springframework.http.HttpStatus.BAD_REQUEST);       		       		
        }
        
		return response;
	}
    
	private ResponseEntity checkStudy(Study study) {
		ResponseEntity response = null;
		RestReponseDTO responseDTO = new RestReponseDTO();
		String finalMsg = null;
		
        if (study == null) {
        	 finalMsg = ErrorConstants.ERR_STUDY_NOT_EXIST+ ":please use correct studyOID";
       	 	 responseDTO.setMessage(finalMsg);
       		 response = new ResponseEntity(responseDTO, org.springframework.http.HttpStatus.BAD_REQUEST);       		       		
        }
        
		return response;
	}


	@Transactional
	public ResponseEntity<Object> scheduleEvent(HttpServletRequest request,MultipartFile file, Study study, String siteOID,UserAccountBean ub,JobDetail jobDetail,String schema) {
			
		ResponseEntity response = null;
		String logFileName = null;
		CoreResources.setRequestSchema(schema);
		 
		sdf_fileName.setTimeZone(TimeZone.getTimeZone("GMT"));
		String fileName = study.getUniqueIdentifier() + DASH + study.getEnvType() + SCHEDULE_EVENT +"_"+ sdf_fileName.format(new Date())+".csv";


		String filePath = userService.getFilePath(JobType.SCHEDULE_EVENT) + File.separator + fileName;
		jobDetail.setLogPath(filePath);
		
		try {
			 
				// read csv file
				 ArrayList<StudyEventScheduleDTO> studyEventScheduleDTOList = csvService.readStudyEventScheduleBulkCSVFile(file, study.getOc_oid(), siteOID);
				 
				 //schedule events
				 for(StudyEventScheduleDTO studyEventScheduleDTO:studyEventScheduleDTOList) {
					 String studyEventOID = studyEventScheduleDTO.getStudyEventOID();
					 String participantId = studyEventScheduleDTO.getSubjectKey();
					 String sampleOrdinalStr = studyEventScheduleDTO.getOrdinal();
					 String startDate = studyEventScheduleDTO.getStartDate();
					 String endDate = studyEventScheduleDTO.getEndDate();
					 int rowNum = studyEventScheduleDTO.getRowNum();
					 
					 RestReponseDTO responseTempDTO = null;	    	    	
				     String status="";
				     String message="";
				     	
				     responseTempDTO = studyEventService.scheduleStudyEvent(ub, study.getOc_oid(), siteOID, studyEventOID, participantId, startDate, endDate);
				    
				     /**
			         *  response
			         */
			    	if(responseTempDTO.getErrors().size() > 0) {
			    		status = "Failed";
			    		message = responseTempDTO.getErrors().get(0).toString();
			 	      
			    	}else{
			    		status = "Sucessful";
			    		message = "Scheduled";
			 	       
			    	}
                    
			    	/**
			         * log error / info into log file 
			         * Response should be a logfile with columns - 
			         * Row, ParticipantID, StudyEventOID, Ordinal, Status, ErrorMessage
			         */ 
                	String recordNum = null;
                	String headerLine = "Row"+SEPERATOR+"ParticipantID"+SEPERATOR+"StudyEventOID"+SEPERATOR+"Ordinal"+SEPERATOR+"Status"+SEPERATOR+"ErrorMessage";
                	String msg = null;
                	msg = rowNum + SEPERATOR + participantId + SEPERATOR + studyEventOID +SEPERATOR+ sampleOrdinalStr +SEPERATOR + status + SEPERATOR+message;
    	    		this.getRestfulServiceHelper().getMessageLogger().writeToLog(filePath, headerLine, msg, ub);
    	    		
    	    		
				 }
				 
			
			
		} catch (Exception e) {
			userService.persistJobFailed(jobDetail,fileName);
			logger.error("Error " + e.getMessage());
		}
		
		RestReponseDTO responseDTO = new RestReponseDTO();
		String finalMsg = "Please check detail schedule information in log file:" + logFileName;
		responseDTO.setMessage(finalMsg);
		response = new ResponseEntity(responseDTO, org.springframework.http.HttpStatus.OK);
		userService.persistJobCompleted(jobDetail,fileName);

		return response;
	}
	public ResponseEntity<Object> scheduleEvent(HttpServletRequest request, String studyOID, String siteOID,String studyEventOID,String participantId, String startDate,String endDate){
	    	ResponseEntity response = null;
	    	RestReponseDTO responseDTO = null;	    	    	
	    	String message="";
	    	
	    	
	    	responseDTO = studyEventService.scheduleStudyEvent(request, studyOID, siteOID, studyEventOID, participantId,  startDate, endDate);
	    	
	    	/**
	         *  response
	         */
	    	if(responseDTO.getErrors().size() > 0) {
	    		StringBuffer errorMsg = new StringBuffer();
	    		for(String errorMessage:responseDTO.getErrors()) {
	    			String errorCode = errorMessage.substring(0, errorMessage.indexOf(":"));
	    			errorMsg.append(errorCode);
	    			errorMsg.append(" ");
	    		}
	    		
	 	      
                HashMap<String, String> map = new HashMap<>();
                map.put("studyOID", studyOID);
                
                if(siteOID !=null && siteOID.trim().length() > 0) {
                	map.put("siteOID", siteOID);
                }
               
               	map.put("studyEventOID", studyEventOID);                              
               	map.put("subjectKey", participantId);
                              
                if(endDate !=null && endDate.trim().length() > 0) {
                	map.put("endDate", endDate);
                }
               
               	map.put("startDate", startDate);
               
                
    			ParameterizedErrorVM responseDTOerror =new ParameterizedErrorVM(errorMsg.toString(), map);
    			
        		response = new ResponseEntity(responseDTOerror, org.springframework.http.HttpStatus.EXPECTATION_FAILED);
        		
	    	}else{
	    		message = "Scheduled event " + studyEventOID + " for participant "+ participantId + " in study " + studyOID + " successfully.";
	 	        responseDTO.setMessage(message);

	 			response = new ResponseEntity(responseDTO, org.springframework.http.HttpStatus.OK);
	    	}
	       
	    	return response;
	      
	    
	    }
	
	@ApiOperation(value = "To check schedule job status with job ID",  notes = " the job ID is included in the response when you run bulk schedule task")
	@SuppressWarnings("unchecked")
    @RequestMapping(value = "/scheduleJobs/{uuid}", method = RequestMethod.GET)
    public ResponseEntity<Object>  checkScheduleStatus(@PathVariable("uuid") String scheduleUuid,
                                                           HttpServletRequest request) {

	    Future<ResponseEntity<Object>> future = null;
        synchronized (expiringMap) {
            future = expiringMap.get(scheduleUuid);
        }
        if (future == null) {
            logger.info("Schedule Future :" + scheduleUuid + " couldn't be found");
            return new ResponseEntity<>("Schedule Future :" + scheduleUuid + " couldn't be found", HttpStatus.BAD_REQUEST);
        } else if (future.isDone()) {
            try {
                ResponseEntity<Object> objectResponseEntity = future.get();
                return new ResponseEntity<>("Completed", HttpStatus.OK);
            } catch (InterruptedException e) {
            	logger.error("Error " + e.getMessage());              
                return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
            } catch (CustomRuntimeException e) {
                
                return new ResponseEntity<>(e.getErrList(), HttpStatus.BAD_REQUEST);
            } catch (ExecutionException e) {
                Throwable cause = e.getCause();
                if (cause != null && cause instanceof CustomRuntimeException) {
                
                    return new ResponseEntity<>(((CustomRuntimeException) cause).getErrList(), HttpStatus.BAD_REQUEST);
                } else {
                    List<ErrorObj> err = new ArrayList<>();
                    ErrorObj errorObj = new ErrorObj(e.getMessage(), e.getMessage());
                    err.add(errorObj);
                    logger.error("Error " + e.getMessage());
                    
                    return new ResponseEntity<>(err, HttpStatus.BAD_REQUEST);
                }
            }
        } else {
            return new ResponseEntity<>(HttpStatus.PROCESSING.getReasonPhrase(), HttpStatus.OK);
        }
    
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

	@ApiOperation( value = "To schedule an event for participant at site level", notes = "Will read the information of SudyOID,ParticipantID, StudyEventOID, Ordinal, Start Date, End Date" )
	@RequestMapping( value = "clinicaldata/studies/{studyOID}/sites/{siteOID}/events", method = RequestMethod.POST )
	public ResponseEntity<Object> scheduleEventAtSiteLevel(HttpServletRequest request,
														   @RequestBody StudyEventScheduleRequestDTO studyEventScheduleRequestDTO,
														   @PathVariable( "studyOID" ) String studyOid,
														   @PathVariable( "siteOID" ) String siteOid) throws Exception {


		return validateEndpoint(studyOid, siteOid, request, studyEventScheduleRequestDTO, null, EndpointType.EVENT_POST);

	}


	@ApiOperation( value = "To Update an event for participant at site level", notes = "Will read the information of SudyOID,ParticipantID, StudyEventOID, Ordinal, Start Date, End Date , Event Repeat Key , Event Status" )
	@RequestMapping( value = "clinicaldata/studies/{studyOID}/sites/{siteOID}/events", method = RequestMethod.PUT )
	public ResponseEntity<Object> updateEventAtSiteLevel(HttpServletRequest request,
														 @RequestBody StudyEventUpdateRequestDTO studyEventUpdateRequestDTO,
														 @PathVariable( "studyOID" ) String studyOid,
														 @PathVariable( "siteOID" ) String siteOid) throws Exception {


		return validateEndpoint(studyOid, siteOid, request, null, studyEventUpdateRequestDTO, EndpointType.EVENT_PUT);

	}

	private ResponseEntity<Object> validateEndpoint(String studyOid, String siteOid, HttpServletRequest request, StudyEventScheduleRequestDTO studyEventScheduleRequestDTO, StudyEventUpdateRequestDTO studyEventUpdateRequestDTO, EndpointType endpointType) {

		utilService.setSchemaFromStudyOid(studyOid);
		Study tenantStudy = getTenantStudy(studyOid);
		Study tenantSite = getTenantStudy(siteOid);
		ResponseEntity<Object> response = null;
		UserAccountBean userAccountBean = utilService.getUserAccountFromRequest(request);
		ArrayList<StudyUserRoleBean> userRoles = userAccountBean.getRoles();
		if (studyOid != null)
			studyOid = studyOid.toUpperCase();
		if (siteOid != null)
			siteOid = siteOid.toUpperCase();

		try {			
			if (!validateService.isStudyOidValid(studyOid)) {
				throw new OpenClinicaSystemException(ErrorConstants.ERR_STUDY_NOT_EXIST);
			}
			if (!validateService.isStudyOidValidStudyLevelOid(studyOid)) {
				throw new OpenClinicaSystemException(ErrorConstants.ERR_STUDY_NOT_Valid_OID);
			}
			if (!validateService.isSiteOidValid(siteOid)) {
				throw new OpenClinicaSystemException(ErrorConstants.ERR_SITE_NOT_EXIST);
			}
			if (!validateService.isSiteOidValidSiteLevelOid(siteOid)) {
				throw new OpenClinicaSystemException(ErrorConstants.ERR_SITE_NOT_Valid_OID);
			}
			if (!validateService.isStudyAvailable(studyOid)) {
				throw new OpenClinicaSystemException(ErrorConstants.ERR_STUDY_NOT_AVAILABLE);
			}
			if (siteOid!=null && !validateService.isStudyAvailable(siteOid)) {
				throw new OpenClinicaSystemException(ErrorConstants.ERR_SITE_NOT_AVAILABLE);
			}
			if (!validateService.isStudyToSiteRelationValid(studyOid, siteOid)) {
				throw new OpenClinicaSystemException(ErrorConstants.ERR_STUDY_TO_SITE_NOT_Valid_OID);
			}

			if (!validateService.isUserHasAccessToStudy(userRoles, studyOid) && !validateService.isUserHasAccessToSite(userRoles, siteOid)) {
				throw new OpenClinicaSystemException(ErrorConstants.ERR_NO_ROLE_SETUP);
			} else if (!validateService.isUserHas_CRC_INV_DM_DEP_DS_RoleInSite(userRoles, siteOid)) {
				throw new OpenClinicaSystemException(ErrorConstants.ERR_NO_SUFFICIENT_PRIVILEGES);
			}


			Object result = null;

			ODMContainer odmContainer = new ODMContainer();
			if (endpointType.equals(EndpointType.EVENT_POST)) {
				populateOdmContainerForEventSchedule(odmContainer, studyEventScheduleRequestDTO, siteOid);
				result = studyEventService.studyEventProcess(odmContainer, studyOid, siteOid, userAccountBean, CREATE);

			} else if (endpointType.equals(EndpointType.EVENT_PUT)) {
				populateOdmContainerForEventUpdate(odmContainer, studyEventUpdateRequestDTO, siteOid);
				result = studyEventService.studyEventProcess(odmContainer, studyOid, siteOid, userAccountBean, UPDATE);
			}

			if (result instanceof ErrorObj)
				throw new OpenClinicaSystemException(((ErrorObj) result).getMessage());
			else if (result instanceof StudyEventResponseDTO)
				return new ResponseEntity<Object>(result, HttpStatus.OK);

		} catch (OpenClinicaSystemException e) {
			String errorMsg = e.getErrorCode();
			HashMap<String, String> map = new HashMap<>();
			map.put("studyOid", studyOid);
			map.put("siteOid", siteOid);
			org.akaza.openclinica.service.rest.errors.ParameterizedErrorVM responseDTO = new ParameterizedErrorVM(errorMsg, map);
			response = new ResponseEntity(responseDTO, org.springframework.http.HttpStatus.EXPECTATION_FAILED);
			return response;
		}

		return null;
	}

	private void populateOdmContainerForEventUpdate(ODMContainer odmContainer, StudyEventUpdateRequestDTO studyEventUpdateRequestDTO, String siteOid) {
		ArrayList<StudyEventDataBean> studyEventDataBeans = new ArrayList<>();
		StudyEventDataBean studyEventDataBean = new StudyEventDataBean();
		studyEventDataBean.setStudyEventOID(studyEventUpdateRequestDTO.getStudyEventOID());
		studyEventDataBean.setStartDate(studyEventUpdateRequestDTO.getStartDate());
		studyEventDataBean.setEndDate(studyEventUpdateRequestDTO.getEndDate());
		studyEventDataBean.setStudyEventRepeatKey(studyEventUpdateRequestDTO.getStudyEventRepeatKey());
		studyEventDataBean.setEventStatus(studyEventUpdateRequestDTO.getEventStatus());
		studyEventDataBeans.add(studyEventDataBean);

		ArrayList<SubjectDataBean> subjectDataBeans = new ArrayList<>();
		SubjectDataBean subjectDataBean = new SubjectDataBean();
		subjectDataBean.setStudySubjectID(studyEventUpdateRequestDTO.getSubjectKey());
		subjectDataBean.setStudyEventData(studyEventDataBeans);
		subjectDataBeans.add(subjectDataBean);

		CRFDataPostImportContainer importContainer = new CRFDataPostImportContainer();
		importContainer.setStudyOID(siteOid);
		importContainer.setSubjectData(subjectDataBeans);

		odmContainer.setCrfDataPostImportContainer(importContainer);
	}


	private void populateOdmContainerForEventSchedule(ODMContainer odmContainer, StudyEventScheduleRequestDTO studyEventScheduleRequestDTO, String siteOid) {
		ArrayList<StudyEventDataBean> studyEventDataBeans = new ArrayList<>();
		StudyEventDataBean studyEventDataBean = new StudyEventDataBean();
		studyEventDataBean.setStudyEventOID(studyEventScheduleRequestDTO.getStudyEventOID());
		studyEventDataBean.setStartDate(studyEventScheduleRequestDTO.getStartDate());
		studyEventDataBean.setEndDate(studyEventScheduleRequestDTO.getEndDate());
		studyEventDataBeans.add(studyEventDataBean);

		ArrayList<SubjectDataBean> subjectDataBeans = new ArrayList<>();
		SubjectDataBean subjectDataBean = new SubjectDataBean();
		subjectDataBean.setStudySubjectID(studyEventScheduleRequestDTO.getSubjectKey());
		subjectDataBean.setStudyEventData(studyEventDataBeans);
		subjectDataBeans.add(subjectDataBean);

		CRFDataPostImportContainer importContainer = new CRFDataPostImportContainer();
		importContainer.setStudyOID(siteOid);
		importContainer.setSubjectData(subjectDataBeans);

		odmContainer.setCrfDataPostImportContainer(importContainer);
	}

	private Study getTenantStudy(String studyOid) {
		return studyDao.findByOcOID(studyOid);
	}

}


