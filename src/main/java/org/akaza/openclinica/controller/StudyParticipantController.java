package org.akaza.openclinica.controller;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.CompletableFuture;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.sql.DataSource;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.core.Context;

import core.org.akaza.openclinica.web.rest.client.auth.impl.KeycloakClientImpl;
import core.org.akaza.openclinica.dao.hibernate.StudySubjectDao;
import core.org.akaza.openclinica.domain.datamap.StudySubject;
import core.org.akaza.openclinica.i18n.core.LocaleResolver;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import core.org.akaza.openclinica.bean.login.*;
import core.org.akaza.openclinica.bean.managestudy.StudySubjectBean;
import core.org.akaza.openclinica.bean.managestudy.SubjectTransferBean;
import org.akaza.openclinica.controller.dto.AddParticipantRequestDTO;
import org.akaza.openclinica.controller.dto.AddParticipantResponseDTO;
import org.akaza.openclinica.controller.helper.RestfulServiceHelper;
import core.org.akaza.openclinica.dao.core.CoreResources;
import core.org.akaza.openclinica.dao.hibernate.StudyDao;
import core.org.akaza.openclinica.dao.hibernate.UserAccountDao;
import core.org.akaza.openclinica.dao.login.UserAccountDAO;
import core.org.akaza.openclinica.dao.managestudy.StudySubjectDAO;
import core.org.akaza.openclinica.domain.datamap.JobDetail;
import core.org.akaza.openclinica.domain.datamap.Study;
import core.org.akaza.openclinica.domain.enumsupport.JobType;
import core.org.akaza.openclinica.domain.user.UserAccount;
import core.org.akaza.openclinica.exception.OpenClinicaSystemException;
import core.org.akaza.openclinica.i18n.util.ResourceBundleProvider;
import core.org.akaza.openclinica.service.*;
import core.org.akaza.openclinica.service.rest.errors.ParameterizedErrorVM;
import org.akaza.openclinica.service.PdfService;
import org.akaza.openclinica.service.ValidateService;
import org.akaza.openclinica.web.restful.errors.ErrorConstants;
import org.akaza.openclinica.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

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
	PdfService pdfService;

	@Autowired
	private UserAccountDao uAccountDao;

	@Autowired
	private StudyDao studyDao;
	@Autowired
	private StudySubjectDao studySubjectDao;

	private StudySubjectDAO ssDao;
	private UserAccountDAO userAccountDao;

	@Autowired
	KeycloakClientImpl keycloakClient;

	@Autowired
	private StudyParticipantService studyParticipantService;

	@Autowired
	PermissionService permissionService;

	@Autowired
	private StudyBuildService studyBuildService;

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
					+ "<br />participantIDAlreadyExists					   : Participant ID already exists."
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
		String realm = keycloakClient.getRealmName(accessToken, customerUuid);
		Study tenantStudy = studyDao.findByOcOID(studyOid);
		Study siteStudy = studyDao.findByOcOID(siteOid);
		Study tenantStudyBean = studyDao.findByOcOID(studyOid);
		ResourceBundle textsBundle = ResourceBundleProvider.getTextsBundle(request.getLocale());
		AddParticipantResponseDTO result=null;
		try {
			validateService.validateStudyAndRoles(studyOid, siteOid, userAccountBean);
			if ((register.equalsIgnoreCase("y") || register.equalsIgnoreCase("yes")) && !validateService.isParticipateActive(tenantStudy))
				throw new OpenClinicaSystemException(ErrorConstants.ERR_PARTICIPATE_INACTIVE);
			if (utilService.isParticipantIDSystemGenerated(tenantStudyBean))
				throw new OpenClinicaSystemException(ErrorConstants.ERR_SYSTEM_GENERATED_ID_ENABLED);
			 result = studyParticipantService.addParticipant(addParticipantRequestDTO, userAccountBean, tenantStudy, siteStudy,realm, customerUuid, textsBundle, accessToken, register);

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
					+ "<br />participantIDAlreadyExists					   : Participant ID already exists."
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
		Study tenantStudyBean=studyDao.findByOcOID(studyOid);

		String customerUuid = utilService.getCustomerUuidFromRequest(request);
		String accessToken = utilService.getAccessTokenFromRequest(request);
		String realm = keycloakClient.getRealmName(accessToken, customerUuid);
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

		String uuid = startBulkAddParticipantJob(file, schema, studyOid, siteOid, userAccountBean,realm,customerUuid,textsBundle,accessToken,register);

		logger.info("REST request to Import Job uuid {} ", uuid);
		return new ResponseEntity<Object>("job uuid: " + uuid, HttpStatus.OK);

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

		boolean includeRelatedInfo = false;
		if(includeParticipateInfo!=null && includeParticipateInfo.trim().toUpperCase().equals("Y")) {
			includeRelatedInfo = true;
		}

		String accessToken = utilService.getAccessTokenFromRequest(request);
		String customerUuid = utilService.getCustomerUuidFromRequest(request);
		String realm = keycloakClient.getRealmName(accessToken, customerUuid);
		StudyParticipantDetailDTO result =  null;
		
		try {			
			validateService.validateStudyAndRolesForRead(studyOid, siteOid, userAccountBean,includeRelatedInfo);
			boolean isStudyLevelUser = utilService.checkStudyLevelUser(userAccountBean.getRoles(), siteOid);
			result = userService.extractParticipantInfo(studyOid,siteOid,accessToken,realm,userAccountBean,participantID,includeRelatedInfo, isStudyLevelUser);
		} catch (OpenClinicaSystemException e) {
			return new ResponseEntity(validateService.getResponseForException(e, studyOid, siteOid), HttpStatus.BAD_REQUEST);
		}

		return new ResponseEntity<Object>(result, HttpStatus.OK);
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

			Study study = null;
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
        responseSuccess.setSiteOID(siteOid);
        if (siteOid != null) {
          Study site = studyDao.findByOcOID(siteOid);
          responseSuccess.setSiteID(site.getUniqueIdentifier());
          responseSuccess.setSiteName(site.getName());
        }
		 	  response = new ResponseEntity(responseSuccess, org.springframework.http.HttpStatus.OK);
		  }

		} catch (Exception eee) {
			logger.error("Error while listing study subjects: ",eee);
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
	private ArrayList<StudyParticipantDTO> getStudyParticipantDTOs(String studyOid, String siteOid,Study study) throws Exception {

		Study studyToCheck;
		/**
		 *  pass in site OID, so will return data in site level
		 */
		if(siteOid != null) {
			studyToCheck = studyDao.findByOcOID(siteOid);
		}else {
			studyToCheck = study;
		}


		List<StudySubject> studySubjects = studySubjectDao.findAllByStudy(studyToCheck);

		ArrayList studyParticipantDTOs = new ArrayList<StudyParticipantDTO>();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
		sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
		for(StudySubject studySubject:studySubjects) {
			StudyParticipantDTO spDTO= new StudyParticipantDTO();

			spDTO.setSubjectOid(studySubject.getOcOid());
			spDTO.setSubjectKey(studySubject.getLabel());
			spDTO.setStatus(studySubject.getStatus().getName());
			if(studySubject.getUserAccount()!=null) {
				spDTO.setCreatedBy(studySubject.getUserAccount().getUserName());
			}
			if(studySubject.getDateCreated()!=null) {

				spDTO.setCreatedAt(sdf.format(studySubject.getDateCreated()));
			}
			if(studySubject.getDateUpdated() !=null) {
				spDTO.setLastModified(sdf.format(studySubject.getDateUpdated()));
			}
			if(studySubject.getUpdateId() != null && studySubject.getUpdateId() > 0) {
				spDTO.setLastModifiedBy(uAccountDao.findById(studySubject.getUpdateId()).getUserName());
			}


			studyParticipantDTOs.add(spDTO);
		}

		return studyParticipantDTOs;
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

	public UserAccountDAO getUserAccountDao() {
		userAccountDao = userAccountDao != null ? userAccountDao : new UserAccountDAO(dataSource);
		return userAccountDao;
	}

	public StudySubjectDAO getStudySubjectDAO() {
		ssDao = ssDao != null ? ssDao : new StudySubjectDAO(dataSource);
		return ssDao;
	}

	public  RestfulServiceHelper getRestfulServiceHelper() {
		serviceHelper = serviceHelper != null ? serviceHelper : new RestfulServiceHelper(dataSource, studyBuildService, studyDao);
		return serviceHelper;
	}
		public String startBulkAddParticipantJob(MultipartFile file, String schema, String studyOid, String siteOid,UserAccountBean userAccountBean, String realm,String customerUuid, ResourceBundle textsBundle,String accessToken, String register) {
		utilService.setSchemaFromStudyOid(studyOid);
		UserAccount userAccount = uAccountDao.findById(userAccountBean.getId());

		Study site = studyDao.findStudyWithSPVByOcOID(siteOid);
		Study study = studyDao.findStudyWithSPVByOcOID(studyOid);
		JobDetail jobDetail = userService.persistJobCreated(study, site, userAccount, JobType.BULK_ADD_PARTICIPANTS, file.getOriginalFilename());
		CompletableFuture<Object> future = CompletableFuture.supplyAsync(() -> {
			try {
				studyParticipantService.startBulkAddParticipantJob(file, study, site,userAccountBean, jobDetail, schema, realm,customerUuid,  textsBundle, accessToken,  register);

			} catch (Exception e) {
				logger.error("Exception is thrown while processing dataImport: " + e);
			}
			return null;

		});
		return jobDetail.getUuid();
	}
	
		@ApiOperation(value = "To get PDF version casebook for one specific participant at site level",  notes = "only work for authorized users with the right acecss permission ")
		@RequestMapping(value = "/studies/{studyOid}/sites/{siteOid}/participants/{participantId}/casebook", method = RequestMethod.POST)
		public ResponseEntity<Object> getSiteLevelParticipantCaseBookInPDF(@PathVariable("studyOid") String studyOid,		
				                                       @PathVariable("siteOid") String siteOid,
													   @PathVariable("participantId") String participantId, 
													   @ApiParam( value = "optional parameter format the paper format. Valid values are: Letter, Legal, Tabloid, Ledger, A0, A1, A2, A3, A4, A5, and A6. Default is A4.", required = false ) @DefaultValue("A4") @RequestParam(value="format",defaultValue = "A4",required = false) String format,
											           @ApiParam( value = "optional parameter margin the paper margin. Valid units are: in, cm, and mm. Example values are 2.1in, 2cm, 10mm. Default is 0.5in.", required = false ) @DefaultValue("0.5in") @RequestParam(value="margin",defaultValue = "0.5in",required = false) String margin,
											           @ApiParam( value = "optional parameter landscape whether paper orientation is landscape. Valid values are true, false. Default is false.", required = false ) @RequestParam(value="landscape",defaultValue = "false",required = false) String landscape,
											           @Context HttpServletRequest request
											          ) throws IOException {
												 
			
			  utilService.setSchemaFromStudyOid(siteOid);	
			  String schema = CoreResources.getRequestSchema();
		 	  UserAccountBean userAccountBean = utilService.getUserAccountFromRequest(request);
	 			 	  
			  try {				 
				  validateService.validateStudyAndRoles(studyOid.trim(),siteOid.trim(),userAccountBean);			 	 			 	 		 	 
			 	  String uuid = startBulkCaseBookPDFJob(schema,studyOid,siteOid, participantId, request, userAccountBean, format, margin, landscape);

				  logger.info("REST request to Casebook PDF Job uuid {} ", uuid);			
				  return new ResponseEntity<Object>("job uuid: " + uuid, HttpStatus.OK);
			  
				  } catch (OpenClinicaSystemException e) {
						return new ResponseEntity(validateService.getResponseForException(e, studyOid, siteOid), HttpStatus.BAD_REQUEST);
				}
			 	 
			 }

		
	@ApiOperation(value = "To get PDF version casebook for one specific participant at study level",  notes = "only work for authorized users with the right acecss permission ")
	@RequestMapping(value = "/studies/{studyOid}/participants/{participantId}/casebook", method = RequestMethod.POST)
	public ResponseEntity<Object> getStudyLevelParticipantCaseBookInPDF(@PathVariable("studyOid") String studyOid,		
												   @PathVariable("participantId") String participantId, 
												   @ApiParam( value = "optional parameter format the paper format. Valid values are: Letter, Legal, Tabloid, Ledger, A0, A1, A2, A3, A4, A5, and A6. Default is A4.", required = false ) @DefaultValue("A4") @RequestParam(value="format",defaultValue = "A4",required = false) String format,
										           @ApiParam( value = "optional parameter margin the paper margin. Valid units are: in, cm, and mm. Example values are 2.1in, 2cm, 10mm. Default is 0.5in.", required = false ) @DefaultValue("0.5in") @RequestParam(value="margin",defaultValue = "0.5in",required = false) String margin,
										           @ApiParam( value = "optional parameter landscape whether paper orientation is landscape. Valid values are true, false. Default is false.", required = false ) @RequestParam(value="landscape",defaultValue = "false",required = false) String landscape,
										           @Context HttpServletRequest request
										          ) throws IOException {
											 
		
		  utilService.setSchemaFromStudyOid(studyOid);	
		  String schema = CoreResources.getRequestSchema();
	 	  UserAccountBean userAccountBean = utilService.getUserAccountFromRequest(request);
	 	
		  try {				 
			  validateService.validateStudyAndRoles(studyOid.trim(),userAccountBean);			 	 			 	 		 	 
		 	  String uuid = startBulkCaseBookPDFJob(schema,studyOid,null, participantId, request, userAccountBean, format, margin, landscape);

			  logger.info("REST request to Casebook PDF Job uuid {} ", uuid);			
			  return new ResponseEntity<Object>("job uuid: " + uuid, HttpStatus.OK);
		  
			  } catch (OpenClinicaSystemException e) {
					return new ResponseEntity(validateService.getResponseForException(e, studyOid, null), HttpStatus.BAD_REQUEST);
			}
		 	 
		 }

	
	private String startBulkCaseBookPDFJob(String schema,
										   String studyOid,
										   String siteOid, 
										   String participantId, 											 
										   HttpServletRequest request,
										   UserAccountBean userAccountBean, 
										   String format, 
										   String margin, 
										   String landscape) {
									 	 


		    final	Study 	site = siteOid==null? null:studyDao.findByOcOID(siteOid);	    			
			final	Study	study = studyOid==null? null:studyDao.findByOcOID(studyOid);						
			
			UserAccount userAccount = uAccountDao.findById(userAccountBean.getId());
			
			// use study or site to check subject
			Study sTemp = null;			
			if(siteOid !=null) {
				sTemp = site;
				
			}else {
				sTemp = study;
			}			
			final StudySubject ss= studySubjectDao.findByLabelAndStudy(participantId.trim(), sTemp);			
			if(ss == null) {
				throw new  OpenClinicaSystemException(ErrorConstants.ERR_PARTICIPANT_NOT_FOUND,"Bad request");
			}

			//Setting the destination file
	        String fullFinalFilePathName = this.getMergedPDFcasebookFileName(studyOid, participantId);
	        int index= fullFinalFilePathName.lastIndexOf(File.separator);
	      
	    	String fileName = fullFinalFilePathName.substring(index + 1);
	    	
			JobDetail jobDetail = userService.persistJobCreated(study, site, userAccount, JobType.PARTICIPANT_PDF_CASEBOOK, fileName);
			jobDetail.setLogPath(fileName);
			ServletContext servletContext = request.getServletContext();
			String accessToken = (String) request.getSession().getAttribute("accessToken");
			servletContext.setAttribute("accessToken", accessToken);
			servletContext.setAttribute("studyID", study.getStudyId()+"");		
			Locale local = LocaleResolver.resolveLocale(request);
			List<String> permissionTagsString =permissionService.getPermissionTagsList((Study)request.getSession().getAttribute("study"),request);
			CompletableFuture<Object> future = CompletableFuture.supplyAsync(() -> {
				try {
					 ResourceBundleProvider.updateLocale(local);
					 String userAccountID = userAccountBean.getId() +"";
					 this.studyParticipantService.startCaseBookPDFJob(jobDetail,schema,study, site,ss, servletContext, userAccountID, fullFinalFilePathName,format, margin, landscape,permissionTagsString);
				 	
					} catch (Exception e) {
						logger.error("Exception is thrown while processing CaseBook PDF: " + e);
						return e.getMessage();
					}
				return null;

			});
			return jobDetail.getUuid();
	
	}
	
	
	/**
	 * @param studyOID
	 * @param studySubjectIdentifier
	 * @return
	 */
	public String getMergedPDFcasebookFileName(String studyOID, String participantId) {
		Date now = new Date();	
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd-hhmmssSSSZ");
		String timeStamp = simpleDateFormat.format(now);
        String pathStr = pdfService.getCaseBookFileRootPath();
    	String fileName = "Participant_"+participantId+"_Casebook_"+timeStamp+".pdf";
    	String fullFinalFilePathName = pathStr + File.separator + fileName;
		return fullFinalFilePathName;
	}
	

}
