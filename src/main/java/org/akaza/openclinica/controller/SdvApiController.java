package org.akaza.openclinica.controller;

import core.org.akaza.openclinica.service.UtilService;
import core.org.akaza.openclinica.web.table.sdv.SDVUtil;
import io.swagger.annotations.Api;
import org.akaza.openclinica.controller.dto.SdvDTO;
import org.akaza.openclinica.service.ValidateService;
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
    private UtilService utilService;

    @Autowired
    private ValidateService validateService;
    @Autowired
    @Qualifier("sdvUtil")
    private SDVUtil sdvUtil;
        @RequestMapping(value = "studies/{studyOid}/events/{StudyEventOid}/forms/{FormOid}/participants/{StudySubjectOid}/viewSdvForm", method = RequestMethod.GET)
    public ResponseEntity<Object> viewFormDetailsForSDV(HttpServletRequest request,
                                                        @PathVariable("studyOid") String studyOID,
                                                        @PathVariable("FormOid") String formOID,
                                                        @PathVariable("StudyEventOid") String studyEventOID,
                                                        @PathVariable("StudySubjectOid") String studySubjectOID,
                                                        @RequestParam( value = "changedAfterSdvOnlyFilter", defaultValue = "y", required = false ) String changedAfterSdvOnlyFilter){
//    @RequestMapping(value = "/sdv/viewSdvForm", method = RequestMethod.GET)
//    public ResponseEntity<Object> viewFormDetailsForSDV(HttpServletRequest request,
//                                                        @RequestParam( value = "changedAfterSdvOnlyFilter", defaultValue = "y", required = false ) String changedAfterSdvOnlyFilter){
        utilService.setSchemaFromStudyOid(studyOID);
        boolean changedAfterSdvOnlyFilterFlag=true;
        if(changedAfterSdvOnlyFilter.equals("n"))
            changedAfterSdvOnlyFilterFlag = false;
        SdvDTO responseDTO = null;
        try {
//            String formOID="F_F1";
//            String studyEventOID="SE_EVENT1";
//            String studySubjectOID ="SS_P1";
            responseDTO = sdvUtil.getFormDetailsForSDV(formOID, studyEventOID, studySubjectOID, changedAfterSdvOnlyFilterFlag);
        }
        catch(Exception e) {
            return new ResponseEntity<>(e, HttpStatus.BAD_REQUEST);
        }
        return new ResponseEntity<>(responseDTO, HttpStatus.OK);
    }

}
