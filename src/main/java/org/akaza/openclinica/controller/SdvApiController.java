package org.akaza.openclinica.controller;

import core.org.akaza.openclinica.bean.login.UserAccountBean;
import core.org.akaza.openclinica.exception.OpenClinicaSystemException;
import core.org.akaza.openclinica.service.StudyBuildService;
import core.org.akaza.openclinica.service.UtilService;
import core.org.akaza.openclinica.web.table.sdv.SDVUtil;
import io.swagger.annotations.Api;
import org.akaza.openclinica.controller.dto.SdvDTO;
import org.akaza.openclinica.service.ValidateService;
import org.akaza.openclinica.web.restful.errors.ErrorConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletRequest;

@Controller
@Api(value = "SDV", tags = { "SDV" }, description = "REST API for SDV Controller")
@RequestMapping(value ="/auth/api/sdv")
public class SdvApiController {

    @Autowired
    private StudyBuildService studyBuildService;

    @Autowired
    private ValidateService validateService;

    @Autowired
    @Qualifier("sdvUtil")
    private SDVUtil sdvUtil;

    @Autowired
    UtilService utilService;

        @RequestMapping(value = "studies/{studyOid}/events/{StudyEventOid}/forms/{FormOid}/participants/{ParticipantId}/viewSdvForm", method = RequestMethod.GET)
    public ResponseEntity<Object> viewFormDetailsForSDV(HttpServletRequest request,
                                                        @PathVariable("studyOid") String studyOID,
                                                        @PathVariable("FormOid") String formOID,
                                                        @PathVariable("StudyEventOid") String studyEventOID,
                                                        @PathVariable("ParticipantId") String studySubjectLabel,
                                                        @RequestParam( value = "changedAfterSdvOnlyFilter", defaultValue = "y", required = false ) String changedAfterSdvOnlyFilter){
        studyBuildService.setRequestSchemaByStudyOrParentStudy(studyOID);
            UserAccountBean userAccountBean = utilService.getUserAccountFromRequest(request);
        boolean changedAfterSdvOnlyFilterFlag=true;
        if(changedAfterSdvOnlyFilter.equals("n"))
            changedAfterSdvOnlyFilterFlag = false;
        SdvDTO responseDTO = null;
        try {
            validateService.validateForSdvItemForm(studyOID, studyEventOID, studySubjectLabel, formOID, userAccountBean);
            responseDTO = sdvUtil.getFormDetailsForSDV(formOID, studyEventOID, studySubjectLabel, changedAfterSdvOnlyFilterFlag);
        }
        catch(OpenClinicaSystemException e) {
            return new ResponseEntity<>(validateService.getResponseForException(e, studyOID, ""), HttpStatus.BAD_REQUEST);
        }
        return new ResponseEntity<>(responseDTO, HttpStatus.OK);
    }

}
