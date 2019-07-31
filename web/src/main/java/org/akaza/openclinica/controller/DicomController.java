package org.akaza.openclinica.controller;

import org.akaza.openclinica.bean.login.UserAccountBean;
import org.akaza.openclinica.service.UtilService;
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

    protected final Logger logger = LoggerFactory.getLogger(getClass().getName());

    @RequestMapping(value = "/participantID/{participantID}/accessionID/{accessionID}/upload", method = RequestMethod.POST)
    public ResponseEntity<Object> importDataPipeDelimitedFile(HttpServletRequest request,
                                                              @PathVariable( "participantID" ) String participantID,
                                                              @PathVariable( "accessionID" ) String accessionID,
                                                              @RequestParam( "file" ) MultipartFile file) throws Exception {

        UserAccountBean ownerUserAccountBean = utilService.getUserAccountFromRequest(request);
        if (ownerUserAccountBean != null && ownerUserAccountBean.getId() < 1) {
            return  new ResponseEntity(HttpStatus.UNAUTHORIZED);
        }

        if (file.isEmpty()) {
            System.out.println("@@@@@@@@@ MISSING FILE @@@@@@@@@");
            return ResponseEntity.ok("MISSING FILE");
        } else {
            try {
                String filename = file.getOriginalFilename();
                System.out.println("@@@@@@@@@ Upload  "+ filename);
                long filesize = file.getSize();
                double kilobytes = (filesize / 1024);
                double megabytes = (kilobytes / 1024);
                System.out.println("@@@@@@@@@ Size  : "+ megabytes + " MB");
            } catch (Exception e) {
                System.out.println("@@@@@@@@@ Error Upload : "+ e.getMessage());
            }
        }

        if (participantID.equalsIgnoreCase("1")) {
            System.out.println("@@@@@@@@@ UPLOAD SUCCESS @@@@@@@@@");
            return ResponseEntity.ok("UPLOAD SUCCESS");
        } else if (participantID.equalsIgnoreCase("2")) {
            System.out.println("@@@@@@@@@ UPLOAD FAILED @@@@@@@@@");
            return ResponseEntity.ok("UPLOAD FAILED");
        } else {
            System.out.println("@@@@@@@@@ UPLOAD PARTIAL @@@@@@@@@");
            return ResponseEntity.ok("UPLOAD PARTIAL");
        }
    }
}
