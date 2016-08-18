package org.akaza.openclinica.controller;


import org.akaza.openclinica.i18n.util.ResourceBundleProvider;
import org.akaza.openclinica.web.restful.ODMClinicaDataResource;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.Locale;

@Controller
@RequestMapping(value = "/auth/api/v1/clinicaldata")
@ResponseStatus(value = org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR)
public class ODMClinicalDataController {

	protected final Logger logger = LoggerFactory.getLogger(getClass().getName());

	@Autowired
	@Qualifier("odmClinicalDataRestResource")
	ODMClinicaDataResource odmClinicaDataResource;

	@RequestMapping(value = "/json/view/{studyOID}/{studySubjectIdentifier}/{studyEventOID}/{formVersionOID}", method = RequestMethod.GET)
	public @ResponseBody
	JsonNode getClinicalData(
			@PathVariable("studyOID") String studyOID,
			@PathVariable("formVersionOID") String formVersionOID,
			@PathVariable("studyEventOID") String studyEventOID,
			@PathVariable("studySubjectIdentifier") String studySubjectIdentifier,
			@RequestParam(value = "includeDNs", defaultValue = "n", required = false) String includeDns,
			@RequestParam(value = "includeAudits", defaultValue = "n", required = false) String includeAudits,
			HttpServletRequest request) throws Exception {

		ResourceBundleProvider.updateLocale(new Locale("en_US"));

		String result = odmClinicaDataResource.getODMClinicaldata(
				studyOID,formVersionOID,studyEventOID,studySubjectIdentifier,includeDns,includeAudits,request);
		ObjectMapper objectMapper = new ObjectMapper();
		return objectMapper.readTree(result);

	}

}
