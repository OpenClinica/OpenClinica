package org.akaza.openclinica.controller;

import org.akaza.openclinica.bean.login.UserAccountBean;
import org.akaza.openclinica.service.DicomServiceClient;
import org.akaza.openclinica.service.UtilService;
import org.akaza.openclinica.service.randomize.RandomizationService;
import org.akaza.openclinica.web.util.ErrorConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;

@Controller
@RequestMapping(value = "/auth/api/dicom")
public class DicomController {

    @Autowired
    private UtilService utilService;
    @Autowired
    private DicomServiceClient dicomServiceClient;

    protected final Logger logger = LoggerFactory.getLogger(getClass().getName());

    @RequestMapping(method = RequestMethod.POST)
    public ResponseEntity<Object> importDataPipeDelimitedFile(HttpServletRequest request,
                                                              @RequestParam( "participantId" ) String participantID,
                                                              @RequestParam( "accessionId" ) String accessionID,
                                                              @RequestParam( "target" ) String target,
                                                              @RequestParam( "file" ) MultipartFile file) throws Exception {

        UserAccountBean ownerUserAccountBean = utilService.getUserAccountFromRequest(request);
        if (ownerUserAccountBean != null && ownerUserAccountBean.getId() < 1) {
            return  new ResponseEntity(HttpStatus.UNAUTHORIZED);
        }

        if (file.isEmpty()) {
            logger.error("@@@@@@@@@ MISSING FILE @@@@@@@@@");
            return ResponseEntity.badRequest()
                    .body(ErrorConstants.ERR_MISSING_FILE);
        } else {
            String accessToken = (String) request.getSession().getAttribute("accessToken");
            return dicomServiceClient.uploadDicom(accessToken, file, participantID, accessionID, target);
        }
    }
}
