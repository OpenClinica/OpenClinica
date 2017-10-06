package org.akaza.openclinica.controller;

import org.akaza.openclinica.service.CustomRuntimeException;
import org.akaza.openclinica.service.OdmImportServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Controller
@RequestMapping(value = "/studyversion")
public class OdmImportController {

    @Autowired
    OdmImportServiceImpl odmImportServiceImpl;

    @RequestMapping(value = "/boardId/{boardId}", method = RequestMethod.POST)
    public ResponseEntity<Object> importOdmToOC(@RequestBody org.cdisc.ns.odm.v130.ODM odm, @PathVariable("boardId") String boardId) throws Exception {

        try {
            odmImportServiceImpl.importOdm(odm, boardId);
            return new ResponseEntity<>(HttpStatus.OK);
        } catch (CustomRuntimeException e) {
            return new ResponseEntity<>(e.getErrList(), HttpStatus.BAD_REQUEST);
        }

    }

}
