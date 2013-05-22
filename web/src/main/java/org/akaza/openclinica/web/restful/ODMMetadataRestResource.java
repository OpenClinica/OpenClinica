package org.akaza.openclinica.web.restful;

import java.io.IOException;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.sun.jersey.api.view.Viewable;
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
	
	
  @GET
  @Path("/html/print/{studyOID}/{eventOID}/{formVersionOID}")
  public Viewable getPrintCRFController( 
    @Context HttpServletRequest request,
    @Context HttpServletResponse response, 
    @PathParam("studyOID") String studyOID,
    @PathParam("eventOID") String eventOID,
    @PathParam("formVersionOID") String formVersionOID
    ) throws Exception {
      request.setAttribute("studyOID", studyOID);
      request.setAttribute("eventOID", eventOID);
      request.setAttribute("formVersionOID", formVersionOID);
      return new Viewable("/WEB-INF/jsp/printcrf.jsp", null);
  }
	
  
  
  @GET
	@Path("/xml/view/{studyOID}/{studyEventDefinitionOId}/{formVersionOID}")
	@Produces(MediaType.TEXT_XML)
	public String getODMMetadataWithFormVersionOID(@PathParam("studyOID") String studyOID,@PathParam("formVersionOID") String formVersionOID ){
	  
		LOGGER.debug("returning here........"+formVersionOID);
		//return "ODM";
		
		return metadataCollectorResource.collectODMMetadataForForm(studyOID,formVersionOID);
	}
  
  
  @GET
 	@Path("/json/view/{studyOID}/{studyEventDefinitionOId}/{formVersionOID}")
 	@Produces(MediaType.APPLICATION_JSON)
 	public String getODMMetadataJson(@PathParam("studyOID") String studyOID,@PathParam("formVersionOID") String formVersionOID ){
 	  
 		LOGGER.debug("returning here........"+formVersionOID);
 		//return "ODM";
 		
 		return metadataCollectorResource.collectODMMetadataJson(studyOID,formVersionOID);
 	}
	
}
