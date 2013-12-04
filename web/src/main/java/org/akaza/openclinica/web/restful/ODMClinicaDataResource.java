package org.akaza.openclinica.web.restful;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;

import net.sf.json.JSON;
import net.sf.json.JSONObject;
import net.sf.json.xml.XMLSerializer;

import org.akaza.openclinica.bean.extract.odm.FullReportBean;
import org.akaza.openclinica.i18n.core.LocaleResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.sun.jersey.api.view.Viewable;

/***
 * * Rest service for ODM clinical data usage
 * ROOT_CONTEXT/rest/clinicaldata/{format}/{mode}/{STUDYOID} format:xml/ json
 * mode:view
 * 
 * @author jnyayapathi
 * 
 */

@Path("/clinicaldata")
@Component
@Scope("prototype")
public class ODMClinicaDataResource {
	private static final Logger LOGGER = LoggerFactory
			.getLogger(ODMClinicaDataResource.class);
	private static final int INDENT_LEVEL = 2;

	public ClinicalDataCollectorResource getClinicalDataCollectorResource() {
		return clinicalDataCollectorResource;
	}

	public void setClinicalDataCollectorResource(
			ClinicalDataCollectorResource clinicalDataCollectorResource) {
		this.clinicalDataCollectorResource = clinicalDataCollectorResource;
	}

	private ClinicalDataCollectorResource clinicalDataCollectorResource;

	private MetadataCollectorResource metadataCollectorResource;

	public MetadataCollectorResource getMetadataCollectorResource() {
		return metadataCollectorResource;
	}

	public void setMetadataCollectorResource(
			MetadataCollectorResource metadataCollectorResource) {
		this.metadataCollectorResource = metadataCollectorResource;
	}

	@GET
	@Path("/json/view/{studyOID}/{studySubjectOID}/{studyEventOID}/{formVersionOID}")
	@Produces(MediaType.APPLICATION_JSON)
	public String getODMClinicaldata(@PathParam("studyOID") String studyOID,
			@PathParam("formVersionOID") String formVersionOID,
			@PathParam("studyEventOID") String studyEventOID,
			@PathParam("studySubjectOID") String studySubjOID,
			@DefaultValue("n") @QueryParam("includeDNs") String includeDns,
			@DefaultValue("n") @QueryParam("includeAudits") String includeAudits,@Context HttpServletRequest request) {
		LOGGER.debug("Requesting clinical data resource");
		boolean includeDN=false;
		boolean includeAudit= false;
		if(includeDns.equalsIgnoreCase("no")||includeDns.equalsIgnoreCase("n")) includeDN=false;
		if(includeAudits.equalsIgnoreCase("no")||includeAudits.equalsIgnoreCase("n")) includeAudit=false;
		if(includeDns.equalsIgnoreCase("yes")||includeDns.equalsIgnoreCase("y")) includeDN=true;
		if(includeAudits.equalsIgnoreCase("yes")||includeAudits.equalsIgnoreCase("y")) includeAudit=true;
		XMLSerializer xmlSerializer = new XMLSerializer();
		FullReportBean report = getMetadataCollectorResource()
				.collectODMMetadataForClinicalData(
						studyOID,
						formVersionOID,
						getClinicalDataCollectorResource()
								.generateClinicalData(studyOID, studySubjOID,
										studyEventOID, formVersionOID,includeDN,includeAudit,request.getLocale()));
		report.createOdmXml(true);
		//xmlSerializer.setForceTopLevelObject(true);
		xmlSerializer.setTypeHintsEnabled(true);
		JSON json = xmlSerializer.read(report.getXmlOutput().toString().trim());
  
		JSONClinicalDataPostProcessor processor = new JSONClinicalDataPostProcessor(request.getLocale());
        processor.process((JSONObject) json);

//        JSONClinicalDataPostProcessor processor = new JSONClinicalDataPostProcessor(LocaleResolver.getLocale(request));
//        processor.process(json);

		return json.toString(INDENT_LEVEL);
	}

	@GET
	@Path("/html/print/{studyOID}/{studySubjectOID}/{eventOID}/{formVersionOID}")
	public Viewable getPrintCRFController(@Context HttpServletRequest request,
			@Context HttpServletResponse response,
			@PathParam("studyOID") String studyOID,
			@PathParam("studySubjectOID") String studySubjectOID,
			@PathParam("eventOID") String eventOID,
			@PathParam("formVersionOID") String formVersionOID,	@DefaultValue("n") @QueryParam("includeDNs") String includeDns,
			@DefaultValue("n") @QueryParam("includeAudits") String includeAudits)
			throws Exception {
		request.setCharacterEncoding("UTF-8");
		request.setAttribute("studyOID", studyOID);
		request.setAttribute("studySubjectOID", studySubjectOID);
		request.setAttribute("eventOID", eventOID);
		request.setAttribute("formVersionOID", formVersionOID);
		request.setAttribute("includeAudits", includeAudits);
		request.setAttribute("includeDNs", includeDns);
		return new Viewable("/WEB-INF/jsp/printcrf.jsp", null);
	}

	@GET
	@Path("/xml/view/{studyOID}/{studySubjectOID}/{studyEventOID}/{formVersionOID}")
	@Produces(MediaType.TEXT_XML)
	public String getODMMetadata(@PathParam("studyOID") String studyOID,
			@PathParam("formVersionOID") String formVersionOID,
			@PathParam("studySubjectOID") String studySubjOID,
			@PathParam("studyEventOID") String studyEventOID,
			@DefaultValue("n") @QueryParam("includeDNs") String includeDns,
			@DefaultValue("n") @QueryParam("includeAudits") String includeAudits,@Context HttpServletRequest request) {
		LOGGER.debug("Requesting clinical data resource");
		boolean includeDN=false;
		boolean includeAudit= false;
		if(includeDns.equalsIgnoreCase("no")||includeDns.equalsIgnoreCase("n")) includeDN=false;
		if(includeAudits.equalsIgnoreCase("no")||includeAudits.equalsIgnoreCase("n")) includeAudit=false;
		if(includeDns.equalsIgnoreCase("yes")||includeDns.equalsIgnoreCase("y")) includeDN=true;
		if(includeAudits.equalsIgnoreCase("yes")||includeAudits.equalsIgnoreCase("y")) includeAudit=true;
		FullReportBean report = getMetadataCollectorResource()
				.collectODMMetadataForClinicalData(
						studyOID,
						formVersionOID,
						getClinicalDataCollectorResource()
								.generateClinicalData(studyOID, studySubjOID,
										studyEventOID, formVersionOID,includeDN,includeAudit,request.getLocale()));

		report.createOdmXml(true);
		LOGGER.debug(report.getXmlOutput().toString().trim());

		return report.getXmlOutput().toString().trim();
	}

}
