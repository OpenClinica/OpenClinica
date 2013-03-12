package org.akaza.openclinica.web.restful;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
/***
 * 
 * @author jnyayapathi
 *
 */
@Component
@Path("/clinicalData")

//@Scope("prototype")

public class ODMClinicaDataResource {
	
	//@Context
	//UriInfo uriInfo;
	
	@GET
	@Path("/xml")
	@Produces(MediaType.TEXT_XML)
	public String getODMMetadata(){
		return "ODM";
	}
	
	
	
}
