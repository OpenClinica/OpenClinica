package org.akaza.openclinica.controller;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.akaza.openclinica.service.OdmImportServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping(value = "/protocolversion")
public class OdmImportController {

    @Autowired
    OdmImportServiceImpl odmImportServiceImpl;

    @RequestMapping(value = "/", method = RequestMethod.POST)
    public @ResponseBody void importOdmToOC(@RequestBody org.cdisc.ns.odm.v130.ODM odm, HttpServletResponse response, HttpServletRequest request)
            throws Exception {
        odmImportServiceImpl.importOdmToOC(odm);
    }

}
