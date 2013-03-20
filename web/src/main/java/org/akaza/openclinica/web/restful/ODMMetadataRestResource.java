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
/**
 *  Rest service for ODM metadata 
 *  usage ROOT_CONTEXT/rest/metadata/{format}/{mode}/{STUDYOID}
 *  format:xml/ json
 *  mode:view
 * @author jnyayapathi
 *
 */
@Path("/metadata")
@Component
@Scope("prototype")
public class ODMMetadataRestResource {
	
	  private static final Logger LOGGER = LoggerFactory.getLogger(ODMMetadataRestResource.class);
private MetadataCollectorResource metadataCollectorResource;

	
	public MetadataCollectorResource getMetadataCollectorResource() {
	return metadataCollectorResource;
}


public void setMetadataCollectorResource(
		MetadataCollectorResource metadataCollectorResource) {
	this.metadataCollectorResource = metadataCollectorResource;
}


	@GET
	@Path("/xml/view/{studyOID}")
	@Produces(MediaType.TEXT_XML)
	public String getODMMetadata(@PathParam("studyOID") String studyOID ){
		LOGGER.debug("returning here........"+studyOID);
		//return "ODM";
		
		return metadataCollectorResource.collectODMMetadata(studyOID);
	}
	
	@GET
	@Path("/json/view/{studyOID}")
	@Produces(MediaType.APPLICATION_JSON)
	public String getODMMetadataJson(@PathParam("studyOID") String studyOID ){
		LOGGER.debug("returning here........"+studyOID);
		//return "ODM";
		
		return metadataCollectorResource.collectODMMetadataJson(studyOID);
	}
	
	
}
