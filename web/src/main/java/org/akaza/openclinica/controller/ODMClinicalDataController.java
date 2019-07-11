package org.akaza.openclinica.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.akaza.openclinica.controller.helper.RestfulServiceHelper;
import org.akaza.openclinica.i18n.util.ResourceBundleProvider;
import org.akaza.openclinica.web.restful.ODMClinicaDataResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import javax.servlet.http.HttpServletRequest;
import javax.sql.DataSource;
import java.util.Locale;

@Controller


@RequestMapping(value = "/auth/api/clinicaldata")
@Api(value = "ODMClinicalDataController", tags = {"Clinical Data"}, description = "REST API for Data Import")
public class ODMClinicalDataController {

    protected final Logger logger = LoggerFactory.getLogger(getClass().getName());
    @Autowired
    @Qualifier("odmClinicalDataRestResource")
    ODMClinicaDataResource odmClinicaDataResource;
    private RestfulServiceHelper serviceHelper;
    @Autowired
    @Qualifier("dataSource")
    private DataSource dataSource;

    @ApiOperation(value = "Retrieve clinical data in JSON or CDISC ODM XML format")
    @RequestMapping(value = "/{studyOID}/{studySubjectIdentifier}/{studyEventOID}/{formVersionOID}", method = RequestMethod.GET, produces={MediaType.APPLICATION_JSON_VALUE}, headers = "Accept=application/json")
    public @ResponseBody
    ResponseEntity<Object> getJsonClinicalData(@ApiParam(value = "Study OID", required = true) @PathVariable("studyOID") String studyOID,
                                               @ApiParam(value = "Participant OID. Use * to retrieve clinical data for all participants.", required = true) @PathVariable("studySubjectIdentifier") String studySubjectIdentifier,
                                               @ApiParam(value = "Study event OID. Use * to retrieve clinical data for all study events of the given participant OID.", required = true) @PathVariable("studyEventOID") String studyEventOID,
                                               @ApiParam(value = "Form OID. Use * to retrieve clinical data for all forms of given study event OID.", required = true) @PathVariable("formVersionOID") String formVersionOID,
                                               @ApiParam(value = "Use this parameter to include or exclude study metadata in response. Possible values - y or n.", required = false) @RequestParam(value = "includeMetadata", defaultValue = "y", required = false) String includeMetadata,
                                               @ApiParam(value = "Use this parameter to include or exclude audit logs in response. Possible values - y or n.", required = false) @RequestParam(value = "includeAudits", defaultValue = "n", required = false) String includeAudits, HttpServletRequest request,
                                               @ApiParam(value = "Use this parameter to include or exclude queries in response. Possible values - y or n.", required = false) @RequestParam(value = "includeDNs", defaultValue = "n", required = false) String includeDns,
                                               @ApiParam(value = "Use this parameter to include or exclude archived event and form data. Possible values - y or n.", required = false) @RequestParam(value = "showArchived", defaultValue = "n", required = false) String showArchived,
                                               @ApiIgnore @RequestParam(value = "clinicaldata", defaultValue = "y", required = false) String clinical,
                                               @ApiIgnore @RequestParam(value = "links", defaultValue = "n", required = false) String links) throws Exception {
        getRestfulServiceHelper().setSchema(studyOID, request);
//@ApiParam(hidden=true)
        ResourceBundleProvider.updateLocale(new Locale("en_US"));

        Object result = odmClinicaDataResource.getODMClinicaldata(studyOID, formVersionOID, studyEventOID, studySubjectIdentifier, includeDns, includeAudits,
                request, includeMetadata, clinical, showArchived,"no", links);

        ResponseEntity<Object> response = null;
        if (result != null) {
            response = new ResponseEntity(result, org.springframework.http.HttpStatus.OK);
        }

        return response;
    }

    @RequestMapping(value = "/{studyOID}/{studySubjectIdentifier}/{studyEventOID}/{formVersionOID}", method = RequestMethod.GET, produces={MediaType.APPLICATION_XML_VALUE}, headers = "Accept=application/xml")
    public @ResponseBody
    ResponseEntity<Object> getXMLClinicalData(@PathVariable("studyOID") String studyOID, @PathVariable("formVersionOID") String formVersionOID,
                                           @PathVariable("studyEventOID") String studyEventOID, @PathVariable("studySubjectIdentifier") String studySubjectIdentifier,
                                           @RequestParam(value = "includeDNs", defaultValue = "n", required = false) String includeDns,
                                           @RequestParam(value = "includeAudits", defaultValue = "n", required = false) String includeAudits, HttpServletRequest request,
                                           @RequestParam(value = "showArchived", defaultValue = "n", required = false) String showArchived,
                                           @RequestParam(value = "clinicaldata", defaultValue = "y", required = false) String clinical,
                                           @RequestParam(value = "includeMetadata", defaultValue = "y", required = false) String includeMetadata,
                                           @ApiIgnore @RequestParam(value = "links", defaultValue = "n", required = false) String links) throws Exception {
        getRestfulServiceHelper().setSchema(studyOID, request);

        ResourceBundleProvider.updateLocale(new Locale("en_US"));

        Object result = odmClinicaDataResource.getODMXMLData(studyOID, formVersionOID, studyEventOID, studySubjectIdentifier, includeDns, includeAudits,
                request, includeMetadata, clinical, showArchived,"no", links);

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
