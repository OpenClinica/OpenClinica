package org.akaza.openclinica.web.restful;

import java.util.Locale;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.sun.jersey.api.view.Viewable;

import freemarker.template.Configuration;
import freemarker.template.ObjectWrapper;
import freemarker.template.TemplateExceptionHandler;
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
	
  
//  @GET
 // @Path("/pdf/print/{studyOID}/{eventOID}/{formVersionOID}")
  //JN: Commenting out this part of pdf generation written by Nick as the approach might be different. Look for these files in mercurial repo for history. 
 /*	public javax.ws.rs.core.Response getPdf(@PathParam("studyOID") String studyOID,@PathParam("formVersionOID") String formVersionOID, 
 	    @PathParam("eventOID") String eventOID, @Context HttpServletRequest request, @Context HttpServletResponse response ) {
      JSON json = metadataCollectorResource.collectODMMetadataJson(studyOID,formVersionOID);
      try {
		getPrintServer(request, response, json, studyOID, eventOID, formVersionOID);
	} catch (Exception e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
    return javax.ws.rs.core.Response.ok().type("application/pdf").build(); 
 }*/
 	
 /*	
   @GET
  @Path("/pdf/print/{studyOID}/{eventOID}/{formVersionOID}")
 	public javax.ws.rs.core.Response getPdf2(@PathParam("studyOID") String studyOID,@PathParam("formVersionOID") String formVersionOID, 
 	    @Context HttpServletRequest request, @Context HttpServletResponse response ) {
      JSON json = metadataCollectorResource.collectODMMetadataJson(studyOID,formVersionOID);
      try {
		getPrintServer(request, response, json);
	} catch (Exception e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
      File file = new File("...");
      return javax.ws.rs.core.Response.ok(file, MediaType.APPLICATION_OCTET_STREAM)
            .header("content-disposition", "attachment; filename =" + file.getName())
            .build(); 
 	}
 	*/
  
  
  @GET
	@Path("/xml/view/{studyOID}/{studyEventDefinitionOId}/{formVersionOID}")
	@Produces(MediaType.TEXT_XML)
	public String getODMMetadataWithFormVersionOID(@PathParam("studyOID") String studyOID,@PathParam("formVersionOID") String formVersionOID ){
		LOGGER.debug("returning here........"+formVersionOID);
		return metadataCollectorResource.collectODMMetadataForForm(studyOID,formVersionOID);
	}
  
  
    @GET
 	@Path("/json/view/{studyOID}/{studyEventDefinitionOId}/{formVersionOID}")
 	@Produces(MediaType.APPLICATION_JSON)
 	public String getODMMetadataJson(@PathParam("studyOID") String studyOID,@PathParam("formVersionOID") String formVersionOID ){
 		LOGGER.debug("returning here........"+formVersionOID);
    	return metadataCollectorResource.collectODMMetadataJsonString(studyOID,formVersionOID);
 	}
 	
 	
 /**/  
  
  private Configuration initFreemarker(ServletContext context) {
    // Initialize the FreeMarker configuration;
    // - Create a configuration instance
    Configuration cfg = new freemarker.template.Configuration();
    // - Templates are stoted in the WEB-INF/templates directory of the Web app.
    cfg.setServletContextForTemplateLoading(context, "WEB-INF/template");
    // - Set update dealy to 0 for now, to ease debugging and testing.
    //   Higher value should be used in production environment.
    cfg.setTemplateUpdateDelay(0);
    // - Set an error handler that prints errors so they are readable with
    //   a HTML browser.
    cfg.setTemplateExceptionHandler(TemplateExceptionHandler.HTML_DEBUG_HANDLER);
    // - Use beans wrapper (recommmended for most applications)
    cfg.setObjectWrapper(ObjectWrapper.BEANS_WRAPPER);
    // - Set the default charset of the template files
    cfg.setDefaultEncoding("ISO-8859-1");
    // - Set the charset of the output. This is actually just a hint, that
    //   templates may require for URL encoding and for generating META element
    //   that uses http-equiv="Content-type".
    cfg.setOutputEncoding("UTF-8");
    // - Set the default locale
    cfg.setLocale(Locale.US);
    
    return cfg;
  }
	
}
