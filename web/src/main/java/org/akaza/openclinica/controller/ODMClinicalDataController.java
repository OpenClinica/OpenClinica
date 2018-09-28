package org.akaza.openclinica.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.annotations.Api;
import org.akaza.openclinica.controller.helper.RestfulServiceHelper;
import org.akaza.openclinica.i18n.util.ResourceBundleProvider;
import org.akaza.openclinica.web.restful.ODMClinicaDataResource;
import org.cdisc.ns.odm.v130.ODM;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.mail.internet.ContentType;
import javax.servlet.http.HttpServletRequest;
import javax.sql.DataSource;
import javax.ws.rs.HeaderParam;
import java.util.Locale;

@Controller


@RequestMapping(value = "/auth/api/clinicaldata")
@Api(value = "Study", tags = {"Study"}, description = "REST API for Study")
public class ODMClinicalDataController {

    protected final Logger logger = LoggerFactory.getLogger(getClass().getName());
    @Autowired
    @Qualifier("odmClinicalDataRestResource")
    ODMClinicaDataResource odmClinicaDataResource;
    private RestfulServiceHelper serviceHelper;
    @Autowired
    @Qualifier("dataSource")
    private DataSource dataSource;

    @RequestMapping(value = "/{studyOID}/{studySubjectIdentifier}/{studyEventOID}/{formVersionOID}", method = RequestMethod.GET, produces={MediaType.APPLICATION_JSON_VALUE}, headers = "Accept=application/json")
    public @ResponseBody
    ResponseEntity<Object> getMultiJsonClinicalData(@PathVariable("studyOID") String studyOID, @PathVariable("formVersionOID") String formVersionOID,
                                                    @PathVariable("studyEventOID") String studyEventOID, @PathVariable("studySubjectIdentifier") String studySubjectIdentifier,
                                                    @RequestParam(value = "includeDNs", defaultValue = "n", required = false) String includeDns,
                                                    @RequestParam(value = "includeAudits", defaultValue = "n", required = false) String includeAudits, HttpServletRequest request,
                                                    @RequestParam(value = "clinicalData", defaultValue = "n", required = false) String clinicalData,
                                                    @RequestParam(value = "showArchived", defaultValue = "n", required = false) String showArchived) throws Exception {
        getRestfulServiceHelper().setSchema(studyOID, request);

        ResourceBundleProvider.updateLocale(new Locale("en_US"));

        Object result = odmClinicaDataResource.getODMClinicaldata(studyOID, formVersionOID, studyEventOID, studySubjectIdentifier, includeDns, includeAudits,
                request, clinicalData, showArchived);

        ResponseEntity<Object> response = null;
        if (result != null) {
            response = new ResponseEntity(result, org.springframework.http.HttpStatus.OK);
        }

        return response;
    }

    @RequestMapping(value = "/{studyOID}/{studySubjectIdentifier}/{studyEventOID}/{formVersionOID}", method = RequestMethod.GET, produces={MediaType.APPLICATION_XML_VALUE}, headers = "Accept=application/xml")
    public @ResponseBody
    ResponseEntity<Object> getMultiXMLClinicalData(@PathVariable("studyOID") String studyOID, @PathVariable("formVersionOID") String formVersionOID,
                                           @PathVariable("studyEventOID") String studyEventOID, @PathVariable("studySubjectIdentifier") String studySubjectIdentifier,
                                           @RequestParam(value = "includeDNs", defaultValue = "n", required = false) String includeDns,
                                           @RequestParam(value = "includeAudits", defaultValue = "n", required = false) String includeAudits, HttpServletRequest request,
                                           @RequestParam(value = "clinicalData", defaultValue = "n", required = false) String clinicalData,
                                           @RequestParam(value = "showArchived", defaultValue = "n", required = false) String showArchived) throws Exception {
        getRestfulServiceHelper().setSchema(studyOID, request);

        ResourceBundleProvider.updateLocale(new Locale("en_US"));

        Object result = odmClinicaDataResource.getODMXMLData(studyOID, formVersionOID, studyEventOID, studySubjectIdentifier, includeDns, includeAudits,
                request, clinicalData, showArchived);

        ResponseEntity<Object> response = null;
        if (result != null) {
            response = new ResponseEntity(result, org.springframework.http.HttpStatus.OK);
        }

        return response;
    }


    public RestfulServiceHelper getRestfulServiceHelper() {
        if (serviceHelper == null) {
            serviceHelper = new RestfulServiceHelper(this.dataSource);
        }

        return serviceHelper;
    }
}
