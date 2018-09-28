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

import javax.servlet.http.HttpServletRequest;
import javax.sql.DataSource;
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

    @RequestMapping(value = "/json/view/{studyOID}/{studySubjectIdentifier}/{studyEventOID}/{formVersionOID}", method = RequestMethod.GET)
    public @ResponseBody
    ResponseEntity<Object> getClinicalData(@PathVariable("studyOID") String studyOID, @PathVariable("formVersionOID") String formVersionOID,
                                           @PathVariable("studyEventOID") String studyEventOID, @PathVariable("studySubjectIdentifier") String studySubjectIdentifier,
                                           @RequestParam(value = "includeDNs", defaultValue = "n", required = false) String includeDns,
                                           @RequestParam(value = "includeAudits", defaultValue = "n", required = false) String includeAudits, HttpServletRequest request,
                                           @RequestParam(value = "clinicalData", defaultValue = "n", required = false) String clinicalData,
                                           @RequestParam(value = "showArchived", defaultValue = "n", required = false) String showArchived) throws Exception {

        getRestfulServiceHelper().setSchema(studyOID, request);

        ResourceBundleProvider.updateLocale(new Locale("en_US"));

        String result = odmClinicaDataResource.getODMClinicaldata(studyOID, formVersionOID, studyEventOID, studySubjectIdentifier, includeDns, includeAudits,
                request, clinicalData, showArchived);
        ObjectMapper objectMapper = new ObjectMapper();
        ResponseEntity<Object> response = null;
        if (result != null) {

            response = new ResponseEntity(objectMapper.readTree(result), org.springframework.http.HttpStatus.OK);
        }

        return response;

    }

    public RestfulServiceHelper getRestfulServiceHelper() {
        if (serviceHelper == null) {
            serviceHelper = new RestfulServiceHelper(this.dataSource);
        }

        return serviceHelper;
    }

    @RequestMapping(value = "/xml/view/{studyOID}/{studySubjectIdentifier}/{studyEventOID}/{formVersionOID}", method = RequestMethod.GET, produces={MediaType.APPLICATION_XML_VALUE})
    public @ResponseBody
    ResponseEntity<ODM> getODMXMLClinicaldata(@PathVariable("studyOID") String studyOID, @PathVariable("formVersionOID") String formVersionOID,
                                              @PathVariable("studyEventOID") String studyEventOID, @PathVariable("studySubjectIdentifier") String studySubjectIdentifier,
                                              @RequestParam(value = "includeDNs", defaultValue = "n", required = false) String includeDns,
                                              @RequestParam(value = "includeAudits", defaultValue = "n", required = false) String includeAudits, HttpServletRequest request,
                                              @RequestParam(value = "clinicalData", defaultValue = "n", required = false) String clinicalData,
                                              @RequestParam(value = "showArchived", defaultValue = "n", required = false) String showArchived) throws Exception {

        getRestfulServiceHelper().setSchema(studyOID, request);

        String result = odmClinicaDataResource.getODMXMLData(studyOID, formVersionOID, studyEventOID, studySubjectIdentifier, includeDns, includeAudits,
                request, clinicalData, showArchived);

        ResponseEntity<ODM> response = null;
        if (result != null) {
            response = new ResponseEntity(result, org.springframework.http.HttpStatus.OK);
        }

        return response;

    }

}
