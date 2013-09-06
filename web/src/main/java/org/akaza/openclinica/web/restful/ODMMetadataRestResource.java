package org.akaza.openclinica.web.restful;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.RandomAccessFile;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;

import net.sf.json.JSON;
import net.sf.json.xml.XMLSerializer;

import org.akaza.openclinica.renderer.ODMRenderer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.sun.jersey.api.view.Viewable;

import freemarker.template.Configuration;
import freemarker.template.ObjectWrapper;
import freemarker.template.Template;
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
	
  
  @GET
  @Path("/pdf/print/{studyOID}/{eventOID}/{formVersionOID}")
 	public javax.ws.rs.core.Response getPdf(@PathParam("studyOID") String studyOID,@PathParam("formVersionOID") String formVersionOID, 
 	    @PathParam("eventOID") String eventOID, @Context HttpServletRequest request, @Context HttpServletResponse response ) {
      JSON json = metadataCollectorResource.collectODMMetadataJson(studyOID,formVersionOID);
      try {
		getPrintServer(request, response, json, studyOID, eventOID, formVersionOID);
	} catch (Exception e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
    return javax.ws.rs.core.Response.ok().type("application/pdf").build(); 
 }
 	
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
 	
 	
  public  void getPrintServer(HttpServletRequest request, HttpServletResponse response, JSON json, String studyOID, String eventOID, String formVersionOID) throws Exception{
    String APP_TMP_DIR = "tmp";
    String APP_BIN_DIR = "bin";
    String TMP_HTML_FILE = "crf.html";
    String TMP_PDF_FILE = "crf.pdf";
    HttpSession session = request.getSession();      
    ServletContext context = session.getServletContext();

    String WKHTML_TO_PDF = context.getInitParameter("WKHTML_TO_PDF");
    String APP_BASE_DIR = context.getRealPath("/");
    Map templateVars = new HashMap();
    XMLSerializer xmlSerializer = new XMLSerializer();
    String htmlOutput = APP_BASE_DIR + APP_TMP_DIR + File.separator + TMP_HTML_FILE;
    File htmlFile = new File(htmlOutput);
    FileWriter txt = new FileWriter(htmlFile);
    PrintWriter out = new PrintWriter(txt);
   
    // create freemarker templates and render html elements 
    Configuration cfg = initFreemarker(context);
    Template t = cfg.getTemplate("crf.ftl");
    ODMRenderer odmRenderer = new ODMRenderer(json, cfg, templateVars);
    String formDef = odmRenderer.render(json, studyOID, eventOID, formVersionOID);
    templateVars.put("formDef", formDef);
    StringWriter sw = new StringWriter(); 
    t.process(templateVars, sw);
   
    // write to crf.html 
    out.write(sw.toString());
    out.close();
    String pdfOutput =  APP_BASE_DIR + APP_TMP_DIR + File.separator + TMP_PDF_FILE;
    
    // convert crf.html to crf.pdf with wkhtmtltopdf 
    String[] cmd = new String[] {WKHTML_TO_PDF, htmlOutput, pdfOutput };
    ProcessBuilder pb = new ProcessBuilder(cmd);
    pb.redirectErrorStream(true);
    Process process = pb.start();
    BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
    String line;
    while ((line = reader.readLine()) != null) {
      System.out.println("tasklist: " + line);
    }
    process.waitFor();
    
    // stream crf.pdf back to the browser for downloading 
    RandomAccessFile pdfFile = new RandomAccessFile(pdfOutput, "r");
    byte[] outputBytes = new byte[(int)pdfFile.length()];
    pdfFile.read(outputBytes);
    
    response.setHeader("Pragma", "no-cache");  
    response.setHeader("Cache-control", "private");  
    response.setDateHeader("Expires", 0);  
    response.setContentType("application/pdf");  
    response.setHeader("Content-Disposition", "attachment; filename=test.pdf");  
 
    if (outputBytes != null) {  
      response.setContentLength(outputBytes.length);  
      ServletOutputStream sos = response.getOutputStream();  
      sos.write(outputBytes);  
      sos.flush();  
      sos.close();  
      pdfFile.close();
    }
  }
  
  
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
