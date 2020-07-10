package org.akaza.openclinica.controller;

import core.org.akaza.openclinica.bean.login.UserAccountBean;
import core.org.akaza.openclinica.bean.managestudy.StudySubjectBean;
import core.org.akaza.openclinica.bean.submit.crfdata.ODMContainer;
import core.org.akaza.openclinica.dao.core.CoreResources;
import core.org.akaza.openclinica.dao.hibernate.*;
import core.org.akaza.openclinica.dao.managestudy.StudySubjectDAO;
import core.org.akaza.openclinica.domain.datamap.*;
import core.org.akaza.openclinica.domain.enumsupport.JobType;
import core.org.akaza.openclinica.domain.user.UserAccount;
import core.org.akaza.openclinica.exception.OpenClinicaSystemException;
import core.org.akaza.openclinica.i18n.util.ResourceBundleProvider;
import core.org.akaza.openclinica.service.*;
import core.org.akaza.openclinica.service.auth.TokenService;
import core.org.akaza.openclinica.service.crfdata.ErrorObj;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.akaza.openclinica.controller.dto.FormUpdateRequestDTO;
import org.akaza.openclinica.controller.dto.StudyEventResponseDTO;
import org.akaza.openclinica.controller.dto.StudyEventScheduleRequestDTO;
import org.akaza.openclinica.controller.dto.StudyEventUpdateRequestDTO;
import org.akaza.openclinica.controller.helper.RestfulServiceHelper;
import org.akaza.openclinica.service.UserService;
import org.akaza.openclinica.service.ValidateService;
import org.apache.commons.collections4.map.PassiveExpiringMap;
import org.apache.commons.dbcp2.BasicDataSource;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;


@Controller
@RequestMapping( value = "/auth/api" )
@Api( value = "Form", tags = {"Form"}, description = "REST API for Form" )
public class FormController {

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
	private EventCRFService eventService;

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

	protected final Logger logger = LoggerFactory.getLogger(getClass().getName());
	public static final String UPDATE = "update";

	@ApiOperation( value = "To Update an event for participant at site level", notes = "Will read the information of StudyOID, ParticipantID, StudyEventOID, Event Repeat Key, Start Date, End Date and Event Status" )
	@RequestMapping( value = "clinicaldata/studies/{studyOID}/sites/{siteOID}/forms", method = RequestMethod.PUT )
	public ResponseEntity<Object> updateEventAtSiteLevel(HttpServletRequest request,
														 @RequestBody FormUpdateRequestDTO formUpdateRequestDTO,
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
		eventService.populateOdmContainerForFormUpdate(odmContainer, formUpdateRequestDTO, siteOid);
		Object result = eventService.eventCrfProcess(odmContainer, studyOid, siteOid, userAccountBean, UPDATE);

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

}


