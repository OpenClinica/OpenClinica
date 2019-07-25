package org.akaza.openclinica.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;

@Controller
@RequestMapping(value = "/api/dicom")
public class DicomController {

    @RequestMapping(value = "/participantID/{participantID}/accessionID/{accessionID}/upload", method = RequestMethod.POST)
    public ResponseEntity<Object> importDataPipeDelimitedFile(HttpServletRequest request,
                                                              @PathVariable( "participantID" ) String participantID,
                                                              @PathVariable( "accessionID" ) String accessionID,
                                                              @RequestParam( "file" ) MultipartFile file) throws Exception {

        if (file.isEmpty()) {
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
            return ResponseEntity.ok("UPLOAD SUCCESS");
        } else {
            return ResponseEntity.ok("UPLOAD FAILED");
        }
    }
}
