package org.akaza.openclinica.controller;

import core.org.akaza.openclinica.bean.login.UserAccountBean;
import core.org.akaza.openclinica.service.*;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.akaza.openclinica.controller.dto.FormRequestDTO;
import org.akaza.openclinica.controller.dto.FormResponseDTO;
import org.akaza.openclinica.service.ValidateService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;


@Controller
@RequestMapping( value = "/auth/api" )
@Api( value = "Form", tags = {"Form"}, description = "REST API for Form" )
public class FormController {

    @Autowired
    private EventCRFService eventService;

    @Autowired
    private UtilService utilService;

    @Autowired
    private ValidateService validateService;

    protected final Logger logger = LoggerFactory.getLogger(getClass().getName());
    public static final String CREATE = "create";
    public static final String UPDATE = "update";

    @ApiOperation(value = "To create a form for a participant at site level", notes = "Both the study OID and the site OID are required url encoded parameters. " +
            "The DTO has fields for the participant ID, study event OID, study event repeat key (if relevant) and form OID for further identification of the correct form." +
            "The form fields which can be updated are the formWorkflowStatus, required, relevant and editable.")
    @RequestMapping(value = "clinicaldata/studies/{studyOID}/sites/{siteOID}/forms", method = RequestMethod.POST)
    public ResponseEntity<FormResponseDTO> createFormAtSiteLevel(HttpServletRequest request,
                                                                 @RequestBody FormRequestDTO formRequestDTO,
                                                                 @PathVariable( "studyOID" ) String studyOid,
                                                                 @PathVariable( "siteOID" ) String siteOid) throws Exception {

        utilService.setSchemaFromStudyOid(studyOid);
        UserAccountBean userAccountBean = utilService.getUserAccountFromRequest(request);

        try {
            validateService.validateStudyAndRoles(studyOid, siteOid, userAccountBean);
            FormResponseDTO result = eventService.createEventCrf(formRequestDTO, studyOid, siteOid, userAccountBean);
            return new ResponseEntity<FormResponseDTO>(result, HttpStatus.OK);
        } catch (CustomRuntimeException exception) {
            logger.error("Exception during eventCRF create: " + exception);
            return new ResponseEntity(validateService.getResponseForException(exception, studyOid, siteOid), HttpStatus.BAD_REQUEST);
        }
    }

    @ApiOperation(value = "To update a form for a participant at site level", notes = "Both the study OID and the site OID are required url encoded parameters. " +
            "The DTO has fields for the participant ID, study event OID, study event repeat key (if relevant) and form OID for further identification of the correct form." +
            "The form fields which can be updated are the formWorkflowStatus, required, relevant and editable.")
    @RequestMapping(value = "clinicaldata/studies/{studyOID}/sites/{siteOID}/forms", method = RequestMethod.PUT)
    public ResponseEntity<FormResponseDTO> updateFormAtSiteLevel(HttpServletRequest request,
                                                                 @RequestBody FormRequestDTO formUpdateRequestDTO,
                                                                 @PathVariable( "studyOID" ) String studyOid,
                                                                 @PathVariable( "siteOID" ) String siteOid) throws Exception {

        utilService.setSchemaFromStudyOid(studyOid);
        UserAccountBean userAccountBean = utilService.getUserAccountFromRequest(request);

        try {
            validateService.validateStudyAndRoles(studyOid, siteOid, userAccountBean);
            FormResponseDTO result = eventService.updateEventCrf(formUpdateRequestDTO, studyOid, siteOid, userAccountBean);
            return new ResponseEntity<FormResponseDTO>(result, HttpStatus.OK);
        } catch (CustomRuntimeException exception) {
            logger.error("Exception during eventCRF update: " + exception);
            return new ResponseEntity(validateService.getResponseForException(exception, studyOid, siteOid), HttpStatus.BAD_REQUEST);
        }

    }

}

