package org.akaza.openclinica.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.akaza.openclinica.bean.login.*;
import org.akaza.openclinica.bean.managestudy.StudyBean;
import org.akaza.openclinica.bean.managestudy.StudySubjectBean;
import org.akaza.openclinica.bean.managestudy.SubjectTransferBean;
import org.akaza.openclinica.controller.dto.AddParticipantRequestDTO;
import org.akaza.openclinica.controller.dto.AddParticipantResponseDTO;
import org.akaza.openclinica.controller.dto.ParticipantRestfulRequestDTO;
import org.akaza.openclinica.controller.dto.StudyEventResponseDTO;
import org.akaza.openclinica.controller.helper.RestfulServiceHelper;
import org.akaza.openclinica.dao.core.CoreResources;
import org.akaza.openclinica.dao.hibernate.StudyDao;
import org.akaza.openclinica.dao.hibernate.UserAccountDao;
import org.akaza.openclinica.dao.login.UserAccountDAO;
import org.akaza.openclinica.dao.managestudy.StudyDAO;
import org.akaza.openclinica.dao.managestudy.StudySubjectDAO;
import org.akaza.openclinica.domain.datamap.JobDetail;
import org.akaza.openclinica.domain.datamap.Study;
import org.akaza.openclinica.domain.enumsupport.JobType;
import org.akaza.openclinica.domain.user.UserAccount;
import org.akaza.openclinica.exception.OpenClinicaSystemException;
import org.akaza.openclinica.i18n.util.ResourceBundleProvider;
import org.akaza.openclinica.service.*;
import org.akaza.openclinica.service.crfdata.ErrorObj;
import org.akaza.openclinica.service.rest.errors.ParameterizedErrorVM;
import org.akaza.openclinica.validator.ParticipantValidator;
import org.akaza.openclinica.web.restful.errors.ErrorConstants;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Controller;
import org.springframework.validation.DataBinder;
import org.springframework.validation.Errors;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.sql.DataSource;
import java.text.ParseException;
import java.util.*;
import java.util.concurrent.CompletableFuture;

@Controller
@Api(value = "Participant", tags = { "Participant" }, description = "REST API for Study Participant")
@RequestMapping(value ="/auth/api/clinicaldata")
public class StudyParticipantController {

	@Autowired
	@Qualifier("dataSource")
	private DataSource dataSource;

	@Autowired
	private UserAccountDao uDao;

	@Autowired
	private ParticipantService participantService;

	@Autowired
	private UtilService utilService;

	@Autowired
	private ValidateService validateService;

	@Autowired
	private UserService userService;

	@Autowired
	private CSVService csvService;

	@Autowired
	private UserAccountDao uAccountDao;

	@Autowired
	private StudyDao studyDao;

	private StudyDAO studyDAO;
	private StudySubjectDAO ssDao;
	private UserAccountDAO userAccountDao;

	@Autowired
	private StudyParticipantService studyParticipantService;

	private RestfulServiceHelper serviceHelper;
	private String dateFormat;
	protected final Logger logger = LoggerFactory.getLogger(getClass().getName());
	public static final String FILE_HEADER_MAPPING = "ParticipantID, StudyEventOID, Ordinal, StartDate, EndDate";



	@ApiOperation(value = "Add a participant with or without their contact information to a given Study site.",  notes = "Will read the subjectKey", hidden = false)
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "Successful operation"),
			@ApiResponse(code = 400, message = "Bad Request -- Normally means Found validation errors, for detail please see the error list: <br /> "
					+ "<br />Error Code                                            Descriptions"
					+ "<br />bulkUploadNotSupportSystemGeneratedSetting    : Bulk particpant ID upload is not supproted when participant ID setting is set to System-generated."
					+ "<br />notSupportedFileFormat                        : File format is not supported. Only CSV file please."
					+ "<br />noSufficientPrivileges                        : User does not have sufficient privileges to perform this operation."
					+ "<br />noRoleSetUp                                   : User has no roles setup under the given Study/Site."
					+ "<br />participantIDContainsUnsupportedHTMLCharacter : Participant ID contains unsupported characters."
					+ "<br />participantIDLongerThan30Characters	       : Participant ID exceeds 30 characters limit."
					+ "<br />participantIDNotUnique                        : Participant ID already exists."
					+ "<br />studyHasSystemGeneratedIdEnabled              : Study is set to have system-generated ID, hence no new participant can be added."
					+ "<br />firstNameTooLong                              : First Name length should not exceed 35 characters."
					+ "<br />lastNameTooLong                               : Last Name length should not exceed 35 characters."
					+ "<br />identifierTooLong                             : Identifier Name length should not exceed 35 characters."
					+ "<br />emailAddressTooLong                           : Email Address length should not exceed 255 characters."
					+ "<br />invalidEmailAddress                           : Email Address contains invalid characters or format."
					+ "<br />phoneNumberTooLong                            : Phone number length should not exceed 15 characters."
					+ "<br />invalidPhoneNumber                            : Phone number should not contain alphabetic characters."
					+ "<br />participateModuleNotActive                    : Participant Module is Not Active."
					+ "<br />participantsEnrollmentCapReached              : Participant Enrollment List has reached. No new participants can be added.")})
	@RequestMapping( value = "/studies/{studyOID}/sites/{siteOID}/participants", method = RequestMethod.POST )
	public ResponseEntity<Object> addParticipantAtSiteLevel(HttpServletRequest request,
															@ApiParam( value = "Provide Participant ID and their contact information. Participant ID is required.", required = true ) @RequestBody AddParticipantRequestDTO addParticipantRequestDTO,
															@ApiParam( value = "Study OID", required = true ) @PathVariable( "studyOID" ) String studyOid,
															@ApiParam( value = "Site OID", required = true ) @PathVariable( "siteOID" ) String siteOid,
															@ApiParam( value = "Use this parameter to register the participant to OpenClinica Participate module. Possible values - y or n. Note: Module should be active for the given study.", required = false ) @RequestParam( value = "register", defaultValue = "n", required = false ) String register) throws Exception {


		utilService.setSchemaFromStudyOid(studyOid);
		UserAccountBean userAccountBean = utilService.getUserAccountFromRequest(request);
		String customerUuid = utilService.getCustomerUuidFromRequest(request);
		String accessToken = utilService.getAccessTokenFromRequest(request);
		Study tenantStudy = studyDao.findByOcOID(studyOid);
		StudyDAO sDao = new StudyDAO(dataSource);
		StudyBean tenantStudyBean = sDao.findByOid(studyOid);
		ResourceBundle textsBundle = ResourceBundleProvider.getTextsBundle(request.getLocale());
		AddParticipantResponseDTO result=null;
		try {
			validateService.validateStudyAndRoles(studyOid, siteOid, userAccountBean);
			if ((register.equalsIgnoreCase("y") || register.equalsIgnoreCase("yes")) && !validateService.isParticipateActive(tenantStudy))
				throw new OpenClinicaSystemException(ErrorConstants.ERR_PARTICIPATE_INACTIVE);
			if (utilService.isParticipantIDSystemGenerated(tenantStudyBean))
				throw new OpenClinicaSystemException(ErrorConstants.ERR_SYSTEM_GENERATED_ID_ENABLED);
			 result = studyParticipantService.addParticipant(addParticipantRequestDTO, userAccountBean, studyOid, siteOid, customerUuid, textsBundle, accessToken, register);

		} catch (OpenClinicaSystemException e) {
			return new ResponseEntity(validateService.getResponseForException(e, studyOid, siteOid), HttpStatus.BAD_REQUEST);

		}
		return new ResponseEntity<Object>(result, HttpStatus.OK);
	}


	@ApiOperation(value = "Add or Update list of participants and their contact information for OpenClinica Participate module.",  notes = "Will read subjectKeys and PII from the CSV file", hidden = false)
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "Successful operation"),
			@ApiResponse(code = 400, message = "Bad Request -- Normally means Found validation errors, for detail please see the error list: <br /> "
					+ "<br />Error Code                                            Descriptions"
					+ "<br />bulkUploadNotSupportSystemGeneratedSetting    : Bulk particpant ID upload is not supproted when participant ID setting is set to System-generated."
					+ "<br />notSupportedFileFormat                        : File format is not supported. Only CSV file please."
					+ "<br />noSufficientPrivileges                        : User does not have sufficient privileges to perform this operation."
					+ "<br />noRoleSetUp                                   : User has no roles setup under the given Study/Site."
					+ "<br />participantIDContainsUnsupportedHTMLCharacter : Participant ID contains unsupported characters."
					+ "<br />participantIDLongerThan30Characters	       : Participant ID exceeds 30 characters limit."
					+ "<br />participantIDNotUnique                        : Participant ID already exists."
					+ "<br />studyHasSystemGeneratedIdEnabled              : Study is set to have system-generated ID, hence no new participant can be added."
					+ "<br />firstNameTooLong                              : First Name length should not exceed 35 characters."
					+ "<br />lastNameTooLong                               : Last Name length should not exceed 35 characters."
					+ "<br />identifierTooLong                             : Identifier Name length should not exceed 35 characters."
					+ "<br />emailAddressTooLong                           : Email Address length should not exceed 255 characters."
					+ "<br />invalidEmailAddress                           : Email Address contains invalid characters or format."
					+ "<br />phoneNumberTooLong                            : Phone number length should not exceed 15 characters."
					+ "<br />invalidPhoneNumber                            : Phone number should not contain alphabetic characters."
					+ "<br />participateModuleNotActive                    : Participant Module is Not Active."
					+ "<br />participantsEnrollmentCapReached              : Participant Enrollment List has reached. No new participants can be added.")})
	@RequestMapping(value = "/studies/{studyOid}/sites/{siteOid}/participants/bulk", method = RequestMethod.POST,consumes = {"multipart/form-data"})
	public ResponseEntity<Object> createStudyParticipantAtSiteLevelInBulk(HttpServletRequest request,
																		  @ApiParam(value = "A CSV file comprising of the headers - ParticipantID, FirstName, EmailAddress, MobileNumber. ParticipantID header value is a must for every record in the file.", required = true) @RequestParam("file") MultipartFile file,
																		  //@RequestParam("size") Integer size,
																		  @ApiParam(value = "Study OID", required = true) @PathVariable("studyOid") String studyOid,
																		  @ApiParam(value = "Site OID", required = true) @PathVariable("siteOid") String siteOid,
																		  @ApiParam(value = "Use this parameter to register the participants for OpenClinica Participate module. Possible values - y or n. Note: Module should be active for the given study.", required = false) @RequestParam( value = "register", defaultValue = "n", required = false ) String register) throws Exception {

		ResponseEntity response = null;
		utilService.setSchemaFromStudyOid(studyOid);
		String schema = CoreResources.getRequestSchema();
		UserAccountBean userAccountBean = utilService.getUserAccountFromRequest(request);

		Study site = studyDao.findByOcOID(siteOid.trim());
		Study study = studyDao.findByOcOID(studyOid.trim());
		StudyDAO sDao = new StudyDAO(dataSource);
		StudyBean tenantStudyBean=sDao.findByOid(studyOid);

		String customerUuid = utilService.getCustomerUuidFromRequest(request);
		String accessToken = utilService.getAccessTokenFromRequest(request);
		ResourceBundle textsBundle = ResourceBundleProvider.getTextsBundle(request.getLocale());

		try {
			// needs to be updated file header mapping constant value
			utilService.checkFileFormat(file,FILE_HEADER_MAPPING);
			validateService.validateStudyAndRoles(studyOid, siteOid, userAccountBean);
			if ((register.equalsIgnoreCase("y") || register.equalsIgnoreCase("yes") ) && !validateService.isParticipateActive(study))
				throw new OpenClinicaSystemException(ErrorConstants.ERR_PARTICIPATE_INACTIVE);
			if (utilService.isParticipantIDSystemGenerated(tenantStudyBean))
				throw new OpenClinicaSystemException(ErrorConstants.ERR_SYSTEM_GENERATED_ID_ENABLED);

			csvService.validateCSVFileHeaderForAddParticipants( file, study.getOc_oid(), siteOid);

		} catch (OpenClinicaSystemException e) {
			return new ResponseEntity(validateService.getResponseForException(e, studyOid, siteOid), HttpStatus.BAD_REQUEST);

		}

		String uuid = startBulkAddParticipantJob(file, schema, studyOid, siteOid, userAccountBean,customerUuid,textsBundle,accessToken,register);

		logger.info("REST request to Import Job uuid {} ", uuid);
		return new ResponseEntity<Object>("job uuid: " + uuid, HttpStatus.OK);

	}





	/**
	 * @param request
	 * @param file
	 * @param studyOID
	 * @param siteOID
	 * @return
	 * @throws Exception
	 */
	private ResponseEntity<String> createNewStudyParticipantsInBulk(HttpServletRequest request, MultipartFile file,
																	String studyOID, String siteOID, Map<String, Object> map) throws Exception {
		ResponseEntity response = null;

		UserAccountBean  user = this.participantService.getUserAccount(request);
		String message = null;

		if (!file.isEmpty()) {

			try {
				String fileNm = file.getOriginalFilename();
				//only support CSV file
				if(!(fileNm.endsWith(".csv")) ){
					throw new OpenClinicaSystemException("errorCode.notSupportedFileFormat","The file format is not supported at this time, please send CSV file, like *.csv ");
				}

				csvService.validateBulkParticipantCSVFile(file);

				return this.createNewStudySubjectsInBulk(request, map, studyOID, siteOID, file);

			}catch(OpenClinicaSystemException e) {
				String validation_failed_message = e.getErrorCode();
				message = validation_failed_message;
			}catch (Exception e) {

				String validation_failed_message = e.getMessage();
				message = validation_failed_message;
			}

		} else {
			message = "Can not read file " + file.getOriginalFilename();
		}

		response = new ResponseEntity(message, HttpStatus.BAD_REQUEST);
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
														String siteOID,UserAccountBean userAccountBean) throws Exception {

		ArrayList<String> errorMessages = new ArrayList<String>();
		ResponseEntity<Object> response = null;

		SubjectTransferBean subjectTransferBean = this.transferToSubject(map);
		subjectTransferBean.setStudyOid(studyOID);
		String uri = request.getRequestURI();
		if(uri.indexOf("/sites/") >  0) {
			subjectTransferBean.setSiteIdentifier(siteOID);
		}

		StudyParticipantDTO studyParticipantDTO = this.buildStudyParticipantDTO(map);

		subjectTransferBean.setOwner(this.participantService.getUserAccount(request));

		StudyBean tenantstudyBean = null;
		try {
			tenantstudyBean = this.getRestfulServiceHelper().setSchema(studyOID, request);
			subjectTransferBean.setStudy(tenantstudyBean);
		}catch(OpenClinicaSystemException oe) {
			throw new Exception(oe.getErrorCode());
		}

		ParticipantValidator participantValidator = new ParticipantValidator(dataSource);
		Errors errors = null;

		DataBinder dataBinder = new DataBinder(subjectTransferBean);
		errors = dataBinder.getBindingResult();


		if(siteOID != null) {
			StudyBean siteStudy = getStudyDAO().findSiteByOid(subjectTransferBean.getStudyOid(), siteOID);
			subjectTransferBean.setSiteStudy(siteStudy);
		}

		Study tenantStudy = studyDao.findByOcOID(tenantstudyBean.getOid());

		if(subjectTransferBean.isRegister() && !validateService.isParticipateActive(tenantStudy)){
			errors.reject( ErrorConstants.ERR_PARTICIPATE_INACTIVE,"Participant module is not active");
		}

		if (utilService.isParticipantIDSystemGenerated(tenantstudyBean)){
			errors.reject( "errorCode.studyHasSystemGeneratedIdEnabled","Study is set to have system-generated ID, hence no new participant can be added");
		}
		participantValidator.validateParticipantData(subjectTransferBean, errors);

		participantValidator.validate(subjectTransferBean, errors);


		if(errors.hasErrors()) {
			ArrayList validerrors = new ArrayList(errors.getAllErrors());
			Iterator errorIt = validerrors.iterator();

			while(errorIt.hasNext()) {
				ObjectError oe = (ObjectError) errorIt.next();
				if(oe.getCode()!=null) {
					errorMessages.add(oe.getCode());
				}else {
					errorMessages.add(oe.getDefaultMessage());
				}


			}
		}

		if (errorMessages != null && errorMessages.size() != 0) {
			String errorMsg = errorMessages.get(0);
			HashMap<String, String> pmap = new HashMap<>();
			pmap.put("studyOid", studyOID);
			pmap.put("siteOid", subjectTransferBean.getSiteIdentifier());
			ParameterizedErrorVM responseDTO =new ParameterizedErrorVM(errorMsg,pmap);
			response = new ResponseEntity(responseDTO, org.springframework.http.HttpStatus.BAD_REQUEST);
		} else {
			String accessToken = utilService.getAccessTokenFromRequest(request);
			String customerUuid = utilService.getCustomerUuidFromRequest(request);

			String label = participantService.createParticipant(subjectTransferBean,tenantstudyBean, accessToken, customerUuid, userAccountBean, request.getLocale());
			studyParticipantDTO.setSubjectKey(label);

			StudySubjectBean subject = this.getStudySubjectDAO().findByLabel(label);

			studyParticipantDTO.setSubjectOid(subject.getOid());

			ResponseSuccessStudyParticipantDTO responseSuccess = new ResponseSuccessStudyParticipantDTO();

			responseSuccess.setSubjectKey(studyParticipantDTO.getSubjectKey());
			responseSuccess.setSubjectOid(studyParticipantDTO.getSubjectOid());
			responseSuccess.setStatus("Available");
			responseSuccess.setParticipateStatus(subject.getUserStatus()!=null?subject.getUserStatus().getValue():"");

			response = new ResponseEntity(responseSuccess, org.springframework.http.HttpStatus.OK);
		}

		return response;

	}

	public String startParticipantImportJob(String studyOid, String siteOid, HttpServletRequest request, String accessToken,
											String customerUuid, UserAccountBean userAccountBean, MultipartFile file, Map<String, Object> map) {
		utilService.setSchemaFromStudyOid(studyOid);
		Study site = studyDao.findByOcOID(siteOid);
		Study study = studyDao.findByOcOID(studyOid);
		StudyBean siteBean = getStudyDAO().findSiteByOid(studyOid, siteOid);
		StudyBean studyBean = getStudyDAO().findByOid(studyOid);

		UserAccount userAccount = uDao.findById(userAccountBean.getId());
		JobDetail jobDetail = userService.persistJobCreated(study, site, userAccount, JobType.BULK_ADD_PARTICIPANTS,file.getOriginalFilename());
		CompletableFuture.supplyAsync(() -> {
			try {
				participantService.processBulkParticipants(studyBean, studyOid, siteBean, siteOid, userAccountBean, accessToken, customerUuid, file,
						jobDetail, request.getLocale(), request.getRequestURI(), map);
			} catch (Exception e) {
				logger.error("Error in Bulk participant job:" + e);
			}
			return null;
		});
		return jobDetail.getUuid();
	}

	/**
	 * @param request
	 * @param map
	 * @return
	 * @throws Exception
	 */
	public ResponseEntity<String> createNewStudySubjectsInBulk(HttpServletRequest request,
															   @RequestBody Map<String, Object> map,
															   String studyOID,
															   String siteOID, MultipartFile file) throws Exception {

		StudyBean studyBean = null;
		studyBean =  this.getStudyDAO().findByOid(studyOID);
		if(utilService.isParticipantIDSystemGenerated(studyBean)) {

			throw new OpenClinicaSystemException("errorCode.bulkUploadNotSupportSystemGeneratedSetting","This study has set up participant ID to be System-generated, bulk upload is not supported at this time ");
		}


		UserAccountBean  user = this.participantService.getUserAccount(request);

		String accessToken = utilService.getAccessTokenFromRequest(request);
		String customerUuid = utilService.getCustomerUuidFromRequest(request);

		String uuid = startParticipantImportJob( studyOID,  siteOID,  request, accessToken,  customerUuid,  user, file, map);

		logger.info("REST request to Extract Participants info ");
		return new ResponseEntity<String>("job uuid: "+ uuid,HttpStatus.OK);

	}


	@ApiOperation(value = "To get all participants at study level",  notes = "only work for authorized users with the right acecss permission")
	@RequestMapping(value = "/studies/{studyOID}/participants", method = RequestMethod.GET)
	public ResponseEntity<Object> listStudySubjectsInStudy(@PathVariable("studyOID") String studyOid,HttpServletRequest request) throws Exception {
		if (studyOid != null)
			studyOid = studyOid.toUpperCase();

		return listStudySubjects(studyOid, null, request);
	}

	@ApiOperation(value = "To get all participants at site level",  notes = "only work for authorized users with the right acecss permission ")
	@RequestMapping(value = "/studies/{studyOID}/sites/{sitesOID}/participants", method = RequestMethod.GET)
	public ResponseEntity<Object> listStudySubjectsInStudySite(@PathVariable("studyOID") String studyOid,@PathVariable("sitesOID") String siteOid,HttpServletRequest request) throws Exception {
		if (studyOid != null)
			studyOid = studyOid.toUpperCase();
		if (siteOid != null)
			siteOid = siteOid.toUpperCase();

		return listStudySubjects(studyOid, siteOid, request);
	}


	@ApiOperation(value = "To get one participant information in study or study site",  notes = "only work for authorized users with the right acecss permission")
	@RequestMapping(value = "/studies/{studyOID}/sites/{sitesOID}/participant", method = RequestMethod.GET)
	public ResponseEntity<Object> getStudySubjectInfo(
			@ApiParam(value = "Study OID", required = true) @PathVariable("studyOID") String studyOid,
			@ApiParam(value = "Site OID",required = true) @PathVariable("sitesOID") String siteOid,
			@ApiParam(value = "participant ID", required = true) @RequestParam( value = "participantID") String participantID,
			@ApiParam(value = "Use this parameter to retrieve participant's access code and status for OpenClinica Participant module. Possible values - y or n.", required = false) @RequestParam( value = "includeParticipateInfo", defaultValue = "n", required = false ) String includeParticipateInfo,
			HttpServletRequest request) throws Exception {

		utilService.setSchemaFromStudyOid(studyOid);
		UserAccountBean userAccountBean = utilService.getUserAccountFromRequest(request);

		try {
			validateService.validateStudyAndRoles(studyOid, siteOid, userAccountBean);
		} catch (OpenClinicaSystemException e) {
			return new ResponseEntity(validateService.getResponseForException(e, studyOid, siteOid), HttpStatus.BAD_REQUEST);

		}

		if (studyOid != null)
			studyOid = studyOid.toUpperCase();

		if (siteOid != null)
			siteOid = siteOid.toUpperCase();

		boolean includeRelatedInfo = false;
		if(includeParticipateInfo!=null && includeParticipateInfo.trim().toUpperCase().equals("Y")) {
			includeRelatedInfo = true;
		}

		Object result = getStudySubjectInfo(studyOid, siteOid, participantID,includeRelatedInfo,request);
		
		try {
			if (result instanceof ErrorObj)
				throw new OpenClinicaSystemException(((ErrorObj) result).getMessage());
			else if (result instanceof StudyParticipantDetailDTO)
				return new ResponseEntity<Object>(result, HttpStatus.OK);
			else if (result instanceof ParameterizedErrorVM)
				return new ResponseEntity<Object>(result, HttpStatus.BAD_REQUEST);
			
		} catch (OpenClinicaSystemException e) {
			return new ResponseEntity(validateService.getResponseForException(e, studyOid, siteOid), HttpStatus.BAD_REQUEST);

		}
		return null;
	}

	/**
	 * @param studyOid
	 * @param siteOid
	 * @param request
	 * @return
	 * @throws Exception
	 */
	private Object getStudySubjectInfo(String studyOid, String siteOid, String participantID,boolean includeRelatedInfo,HttpServletRequest request)
			throws Exception {
		Object response = null;
		try {

			StudyBean study = null;
			try {
				study = this.getRestfulServiceHelper().setSchema(studyOid, request);
				study = participantService.validateRequestAndReturnStudy(studyOid, siteOid,request);

				if(study != null) {
					StudyParticipantDetailDTO responseSuccess =  new StudyParticipantDetailDTO();

					responseSuccess = getStudyParticipantDTO(request,studyOid, siteOid,study,participantID,includeRelatedInfo);

					return responseSuccess;
				}

			} catch (OpenClinicaSystemException e) {

				String errorMsg = e.getErrorCode();
				HashMap<String, String> map = new HashMap<>();
				map.put("studyOid", studyOid);
				map.put("siteOid", siteOid);
				map.put("participantID", participantID);
				ParameterizedErrorVM responseErrorDTO =new ParameterizedErrorVM(errorMsg, map);

				return responseErrorDTO;
			}



		} catch (Exception eee) {
			logger.error("Error...", eee);
			return new ErrorObj("Unexpected error:",eee.getMessage());
		}

		return response;
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
		ResponseEntity<Object> response = null;
		try {

			StudyBean study = null;
			try {
				study = this.getRestfulServiceHelper().setSchema(studyOid, request);
				study = participantService.validateRequestAndReturnStudy(studyOid, siteOid,request);
			} catch (OpenClinicaSystemException e) {

				String errorMsg = e.getErrorCode();
				HashMap<String, String> map = new HashMap<>();
				map.put("studyOid", studyOid);
				map.put("siteOid", siteOid);
				ParameterizedErrorVM responseDTO =new ParameterizedErrorVM(errorMsg, map);

				response = new ResponseEntity(responseDTO, HttpStatus.BAD_REQUEST);

				return response;
			}

			if(study != null) {
				ResponseSuccessListAllParticipantsByStudyDTO responseSuccess =  new ResponseSuccessListAllParticipantsByStudyDTO();

				ArrayList<StudyParticipantDTO> studyParticipantDTOs = getStudyParticipantDTOs(studyOid, siteOid,study);
				responseSuccess.setStudyParticipants(studyParticipantDTOs);

				response = new ResponseEntity(responseSuccess, org.springframework.http.HttpStatus.OK);
			}

		} catch (Exception eee) {
			eee.printStackTrace();
			throw eee;
		}

		return response;
	}

	/**
	 * @param studyOid
	 * @param siteOid
	 * @param study
	 * @return
	 * @throws Exception
	 */
	private ArrayList<StudyParticipantDTO> getStudyParticipantDTOs(String studyOid, String siteOid,StudyBean study) throws Exception {

		StudyBean studyToCheck;
		/**
		 *  pass in site OID, so will return data in site level
		 */
		if(siteOid != null) {
			studyToCheck = this.getStudyDAO().findByOid(siteOid);
		}else {
			studyToCheck = study;
		}


		List<StudySubjectBean> studySubjects = this.getStudySubjectDAO().findAllByStudy(studyToCheck);

		ArrayList studyParticipantDTOs = new ArrayList<StudyParticipantDTO>();

		for(StudySubjectBean studySubject:studySubjects) {
			StudyParticipantDTO spDTO= new StudyParticipantDTO();

			spDTO.setSubjectOid(studySubject.getOid());
			spDTO.setSubjectKey(studySubject.getLabel());
			spDTO.setStatus(studySubject.getStatus().getName());
			if(studySubject.getOwner()!=null) {
				spDTO.setCreatedBy(studySubject.getOwner().getName());
			}
			if(studySubject.getCreatedDate()!=null) {
				spDTO.setCreatedAt(studySubject.getCreatedDate().toLocaleString());
			}
			if(studySubject.getUpdatedDate() !=null) {
				spDTO.setLastModified(studySubject.getUpdatedDate().toLocaleString());
			}
			if(studySubject.getUpdater() != null) {
				spDTO.setLastModifiedBy(studySubject.getUpdater().getName());
			}


			studyParticipantDTOs.add(spDTO);
		}

		return studyParticipantDTOs;
	}


	/**
	 * @param studyOid
	 * @param siteOid
	 * @param study
	 * @return
	 * @throws Exception
	 */
	private StudyParticipantDetailDTO getStudyParticipantDTO(HttpServletRequest request,String studyOid, String siteOid,StudyBean study,String participantID,boolean incRelatedInfo) throws Exception {

		StudyBean studyToCheck;
		/**
		 *  pass in site OID, so will return data in site level
		 */
		if(siteOid != null) {
			studyToCheck = this.getStudyDAO().findByOid(siteOid);
		}else {
			studyToCheck = study;
		}

		String accessToken = utilService.getAccessTokenFromRequest(request);
		String customerUuid = utilService.getCustomerUuidFromRequest(request);
		UserAccountBean userAccountBean = utilService.getUserAccountFromRequest(request);
		utilService.setSchemaFromStudyOid(studyOid);
		String schema = CoreResources.getRequestSchema();
		StudyParticipantDetailDTO spDTO = userService.extractParticipantInfo(studyOid, siteOid, accessToken, customerUuid, userAccountBean, schema,participantID,incRelatedInfo);

		return spDTO;
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
	 * @param map
	 * @return
	 * @throws ParseException
	 * @throws Exception
	 */
	private SubjectTransferBean transferToSubject(HashMap<String, Object> map) throws ParseException, Exception {

		String personId = (String) map.get("subjectKey");
		String firstName = (String) map.get("firstName");
		String lastName = (String) map.get("lastName");
		String identifier = (String) map.get("identifier");

		String emailAddress = (String) map.get("emailAddress");
		String phoneNumber = (String) map.get("phoneNumber");
		String register = (String) map.get("register");

		SubjectTransferBean subjectTransferBean = new SubjectTransferBean();

		subjectTransferBean.setPersonId(personId);
		subjectTransferBean.setStudySubjectId(personId);
		subjectTransferBean.setFirstName(firstName);
		subjectTransferBean.setLastName(lastName);
		subjectTransferBean.setIdentifier(identifier);

		subjectTransferBean.setEmailAddress(emailAddress!=null && emailAddress.length()!=0?emailAddress:null);
		subjectTransferBean.setPhoneNumber(phoneNumber!=null && phoneNumber.length()!=0?phoneNumber:null);

		if(register.equalsIgnoreCase("Y")|| register.equalsIgnoreCase("YES"))
			subjectTransferBean.setRegister(true);

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
	public StudyDAO getStudyDAO() {
		studyDAO = studyDAO
				!= null ? studyDAO : new StudyDAO(dataSource);
		return studyDAO;
	}

	public UserAccountDAO getUserAccountDao() {
		userAccountDao = userAccountDao != null ? userAccountDao : new UserAccountDAO(dataSource);
		return userAccountDao;
	}

	public StudySubjectDAO getStudySubjectDAO() {
		ssDao = ssDao != null ? ssDao : new StudySubjectDAO(dataSource);
		return ssDao;
	}

	public  RestfulServiceHelper getRestfulServiceHelper() {
		serviceHelper = serviceHelper != null ? serviceHelper : new RestfulServiceHelper(dataSource);
		return serviceHelper;
	}
	public String startBulkAddParticipantJob(MultipartFile file, String schema, String studyOid, String siteOid,UserAccountBean userAccountBean, String customerUuid, ResourceBundle textsBundle,String accessToken, String register) {
		utilService.setSchemaFromStudyOid(studyOid);
		UserAccount userAccount = uAccountDao.findById(userAccountBean.getId());

		Study site = studyDao.findByOcOID(siteOid);
		Study study = studyDao.findByOcOID(studyOid);
		JobDetail jobDetail = userService.persistJobCreated(study, site, userAccount, JobType.BULK_ADD_PARTICIPANTS, file.getOriginalFilename());
		CompletableFuture<Object> future = CompletableFuture.supplyAsync(() -> {
			try {
				studyParticipantService.startBulkAddParticipantJob(file, study, site,userAccountBean, jobDetail, schema, customerUuid,  textsBundle, accessToken,  register);
			} catch (Exception e) {
				logger.error("Exception is thrown while processing dataImport: " + e);
			}
			return null;

		});
		return jobDetail.getUuid();
	}
}
