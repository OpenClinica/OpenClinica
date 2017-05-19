package org.akaza.openclinica.controller;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.akaza.openclinica.service.OdmImportServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping(value = "/studyversion")
public class OdmImportController {

    @Autowired
    OdmImportServiceImpl odmImportServiceImpl;

    @RequestMapping(value = "/boardId/{boardId}", method = RequestMethod.POST)
    public @ResponseBody void importOdmToOC(@RequestBody org.cdisc.ns.odm.v130.ODM odm, @PathVariable("boardId") String boardId)
            throws Exception {
        odmImportServiceImpl.importOdmToOC(odm, boardId);
    }

}
