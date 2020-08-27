package org.akaza.openclinica.controller;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import javax.servlet.http.HttpServletRequest;

import core.org.akaza.openclinica.service.auth.TokenService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

import core.org.akaza.openclinica.bean.login.UserAccountBean;
import core.org.akaza.openclinica.bean.managestudy.StudySubjectBean;
import core.org.akaza.openclinica.bean.submit.crfdata.ODMContainer;
import org.akaza.openclinica.controller.dto.*;
import org.akaza.openclinica.controller.helper.RestfulServiceHelper;
import core.org.akaza.openclinica.dao.core.CoreResources;
import core.org.akaza.openclinica.dao.hibernate.*;
import core.org.akaza.openclinica.dao.managestudy.StudySubjectDAO;
import core.org.akaza.openclinica.domain.datamap.*;
import core.org.akaza.openclinica.domain.enumsupport.JobType;
import core.org.akaza.openclinica.domain.user.UserAccount;
import core.org.akaza.openclinica.exception.OpenClinicaSystemException;
import core.org.akaza.openclinica.i18n.util.ResourceBundleProvider;
import core.org.akaza.openclinica.service.*;
import core.org.akaza.openclinica.service.crfdata.ErrorObj;
import org.akaza.openclinica.service.UserService;
import org.akaza.openclinica.service.ValidateService;
import org.apache.commons.dbcp2.BasicDataSource;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.commons.collections4.map.PassiveExpiringMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
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

	@Autowired
	private StudyBuildService studyBuildService;

	@Autowired
	private TokenService tokenService;

	PassiveExpiringMap<String, Future<ResponseEntity<Object>>> expiringMap =
			new PassiveExpiringMap<>(24, TimeUnit.HOURS);

	protected final Logger logger = LoggerFactory.getLogger(getClass().getName());
	public static final String DASH = "-";
	public static final String FILE_HEADER_MAPPING = "ParticipantID, StudyEventOID, Ordinal, StartDate, EndDate";
	public static final String SEPERATOR = ",";
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
		String studyOid = (String) request.getSession().getAttribute("studyOid");
		UserAccountBean ub = (UserAccountBean) request.getSession().getAttribute("userBean");

		getRestfulServiceHelper().setSchema(studyOid, request);
		ResourceBundleProvider.updateLocale(new Locale("en_US"));
		if (ub == null) {
			logger.info("userAccount is null");
			return null;
		}
		StudySubjectDAO studySubjectDAO = new StudySubjectDAO(dataSource);

		String userName = ub.getName();
		int lastIndexOfDot = userName.lastIndexOf(".");
		String subjectOid = userName.substring(lastIndexOfDot + 1);
		StudySubjectBean studySubject = studySubjectDAO.findByOid(subjectOid);

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
			String accessToken = (String) request.getSession().getAttribute("accessToken");
			participateService.completeData(studyEvent, eventDefCrfs, eventCrfs, accessToken, studyOid, subjectOid, ub);
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
			restfulServiceHelper = new RestfulServiceHelper(this.dataSource, studyBuildService, studyDao);
		}
		return restfulServiceHelper;
	}








	@ApiOperation( value = "To check schedule job status with job ID", notes = " the job ID is included in the response when you run bulk schedule task",hidden = true )
	@SuppressWarnings( "unchecked" )
	@RequestMapping( value = "/scheduleJobs/{uuid}", method = RequestMethod.GET )
	public ResponseEntity<Object> checkScheduleStatus(@PathVariable( "uuid" ) String scheduleUuid,
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


	@ApiOperation( value = "To schedule an event for participants at site level in bulk", notes = "Will read the information of StudyOID,ParticipantID, StudyEventOID, Event Repeat Key, Start Date, End Date and Event Status" )
	@ApiResponses( value = {
			@ApiResponse( code = 200, message = "Successful operation" ),
			@ApiResponse( code = 400, message = "Bad Request -- Normally means Found validation errors, for detail please see the error list: <br /> " )} )
	@RequestMapping( value = "clinicaldata/studies/{studyOID}/sites/{siteOID}/events/bulk", method = RequestMethod.POST, consumes = {"multipart/form-data"} )
	public ResponseEntity<Object> bulkSheduleEventAtSiteLevel(HttpServletRequest request,
															  MultipartFile file,
															  @PathVariable( "studyOID" ) String studyOid,
															  @PathVariable( "siteOID" ) String siteOid) throws Exception {

		ResponseEntity response = null;
		utilService.setSchemaFromStudyOid(studyOid);
		String schema = CoreResources.getRequestSchema();
		UserAccountBean userAccountBean = utilService.getUserAccountFromRequest(request);

		Study site = studyDao.findByOcOID(siteOid.trim());
		Study study = studyDao.findByOcOID(studyOid.trim());

		try {
			utilService.checkFileFormat(file,FILE_HEADER_MAPPING);
			validateService.validateStudyAndRoles(studyOid, siteOid, userAccountBean);
			csvService.validateCSVFileHeaderForScheduleEvents( file, study.getOc_oid(), siteOid);

		} catch (OpenClinicaSystemException e) {
			return new ResponseEntity(validateService.getResponseForException(e, studyOid, siteOid), HttpStatus.BAD_REQUEST);
		}


		UserAccount userAccount = userAccountDao.findById(userAccountBean.getId());
		String uuid = startBulkEventJob(file, schema, studyOid, siteOid, userAccountBean);

		logger.info("REST request to Import Job uuid {} ", uuid);
		return new ResponseEntity<Object>("job uuid: " + uuid, HttpStatus.OK);
	}


	@ApiOperation( value = "To schedule an event for participant at site level", notes = "Will read the information of StudyOID,ParticipantID, StudyEventOID, Start Date and End Date" )
	@RequestMapping( value = "clinicaldata/studies/{studyOID}/sites/{siteOID}/events", method = RequestMethod.POST )
	public ResponseEntity<Object> scheduleEventAtSiteLevel(HttpServletRequest request,
														   @RequestBody StudyEventScheduleRequestDTO studyEventScheduleRequestDTO,
														   @PathVariable( "studyOID" ) String studyOid,
														   @PathVariable( "siteOID" ) String siteOid) throws Exception {

		utilService.setSchemaFromStudyOid(studyOid);
		UserAccountBean userAccountBean = utilService.getUserAccountFromRequest(request);

		try {
			validateService.validateStudyAndRoles(studyOid, siteOid, userAccountBean);
		} catch (OpenClinicaSystemException e) {
			return new ResponseEntity(validateService.getResponseForException(e, studyOid, siteOid), HttpStatus.BAD_REQUEST);
		}

		ODMContainer odmContainer = new ODMContainer();
		studyEventService.populateOdmContainerForEventSchedule(odmContainer, studyEventScheduleRequestDTO, siteOid);
		Object result = studyEventService.studyEventProcess(odmContainer, studyOid, siteOid, userAccountBean, CREATE);
		try {
			if (result instanceof ErrorObj)
				throw new OpenClinicaSystemException(((ErrorObj) result).getMessage());
			else if (result instanceof StudyEventResponseDTO)
				return new ResponseEntity<Object>(result, HttpStatus.OK);
		} catch (OpenClinicaSystemException e) {
			return new ResponseEntity(validateService.getResponseForException(e, studyOid, siteOid), HttpStatus.BAD_REQUEST);

		}
		return null;
	}

	@ApiOperation( value = "To Update an event for participant at site level", notes = "Will read the information of StudyOID, ParticipantID, StudyEventOID, Event Repeat Key, Start Date, End Date and Event Status" )
	@RequestMapping( value = "clinicaldata/studies/{studyOID}/sites/{siteOID}/events", method = RequestMethod.PUT )
	public ResponseEntity<Object> updateEventAtSiteLevel(HttpServletRequest request,
														 @RequestBody StudyEventUpdateRequestDTO studyEventUpdateRequestDTO,
														 @PathVariable( "studyOID" ) String studyOid,
														 @PathVariable( "siteOID" ) String siteOid) throws Exception {

		utilService.setSchemaFromStudyOid(studyOid);
		UserAccountBean userAccountBean = utilService.getUserAccountFromRequest(request);

		try {
			validateService.validateStudyAndRoles(studyOid, siteOid, userAccountBean);
		} catch (OpenClinicaSystemException e) {
			return new ResponseEntity(validateService.getResponseForException(e, studyOid, siteOid), HttpStatus.BAD_REQUEST);

		}

		ODMContainer odmContainer = new ODMContainer();
		studyEventService.populateOdmContainerForEventUpdate(odmContainer, studyEventUpdateRequestDTO, siteOid);
		Object result = studyEventService.studyEventProcess(odmContainer, studyOid, siteOid, userAccountBean, UPDATE);

		try {
			if (result instanceof ErrorObj)
				throw new OpenClinicaSystemException(((ErrorObj) result).getMessage());
			else if (result instanceof StudyEventResponseDTO)
				return new ResponseEntity<Object>(result, HttpStatus.OK);
		} catch (OpenClinicaSystemException e) {
			return new ResponseEntity(validateService.getResponseForException(e, studyOid, siteOid), HttpStatus.BAD_REQUEST);

		}
		return null;
	}

	@ApiOperation( value = "To Update an event for participant at site level", notes = "Will read the information of StudyOID, ParticipantID, StudyEventOID, Event Repeat Key, Start Date, End Date and Event Status" )
	@RequestMapping( value = "{studyOID}/events/check", method = RequestMethod.GET )
	public ResponseEntity<Object> checkWorking(HttpServletRequest request,
														 @PathVariable( "studyOID" ) String studyOid) throws Exception {
		Study s = studyDao.findByOcOID(studyOid);
		return null;
	}

	public String startBulkEventJob(MultipartFile file, String schema, String studyOid, String siteOid, UserAccountBean userAccountBean) {
		utilService.setSchemaFromStudyOid(studyOid);

		Study site = studyDao.findByOcOID(siteOid);
		Study study = studyDao.findByOcOID(studyOid);
		UserAccount userAccount = userAccountDao.findById(userAccountBean.getId());
		JobDetail jobDetail = userService.persistJobCreated(study, site, userAccount, JobType.SCHEDULE_EVENT, file.getOriginalFilename());
		CompletableFuture<Object> future = CompletableFuture.supplyAsync(() -> {
			try {
				studyEventService.scheduleOrUpdateBulkEvent(file, study, siteOid, userAccountBean, jobDetail, schema);
			} catch (Exception e) {
				logger.error("Exception is thrown while processing dataImport: " + e);
			}
			return null;

		});
		return jobDetail.getUuid();
	}
}


