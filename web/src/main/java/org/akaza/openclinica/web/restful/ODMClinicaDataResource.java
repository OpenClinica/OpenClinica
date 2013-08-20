package org.akaza.openclinica.web.restful;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
/***
 * 
 * @author jnyayapathi
 *
 */

@Path("/clinicaldata")
@Component
@Scope("prototype")

public class ODMClinicaDataResource {
	 private static final Logger LOGGER = LoggerFactory.getLogger(ODMClinicaDataResource.class);

	 public ClinicalDataCollectorResource getClinicalDataCollectorResource() {
		return clinicalDataCollectorResource;
	}

	public void setClinicalDataCollectorResource(
			ClinicalDataCollectorResource clinicalDataCollectorResource) {
		this.clinicalDataCollectorResource = clinicalDataCollectorResource;
	}

	private ClinicalDataCollectorResource clinicalDataCollectorResource;
	
	@GET
	@Path("/xml/view/{studyOID}/{studySubjectOID}/{studyEventOID}/{formVersionOID}")
	@Produces(MediaType.TEXT_XML)
	public String getODMMetadata(@PathParam("studyOID") String studyOID,@PathParam("formVersionOID") String formVersionOID,@PathParam("studySubjectOID") String studySubjOID){
		LOGGER.debug("Requesting clinical data resource");
		return getClinicalDataCollectorResource().generateClinicalData(studyOID, studySubjOID);
	}
	
	
	
}
