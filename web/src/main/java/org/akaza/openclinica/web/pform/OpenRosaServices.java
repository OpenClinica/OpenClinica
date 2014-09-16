package org.akaza.openclinica.web.pform;

import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.TimeZone;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;

import org.akaza.openclinica.bean.admin.CRFBean;
import org.akaza.openclinica.bean.managestudy.StudyBean;
import org.akaza.openclinica.bean.submit.CRFVersionBean;
import org.akaza.openclinica.dao.admin.CRFDAO;
import org.akaza.openclinica.dao.core.CoreResources;
import org.akaza.openclinica.dao.managestudy.StudyDAO;
import org.akaza.openclinica.dao.submit.CRFVersionDAO;
import org.akaza.openclinica.web.pform.formlist.XFormList;
import org.akaza.openclinica.web.pform.formlist.XForm;
import org.apache.commons.codec.digest.DigestUtils;
import org.exolab.castor.mapping.Mapping;
import org.exolab.castor.xml.Marshaller;
import org.exolab.castor.xml.XMLContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestHeader;

@Path("/openrosa")
@Component
public class OpenRosaServices{

	private static final Logger LOGGER = LoggerFactory.getLogger(OpenRosaServices.class);
	private DataSource dataSource;
	private CoreResources coreResources;

	@GET
	@Path("/{studyOID}/formList")
	@Produces(MediaType.APPLICATION_XML)
	public String getFormList(@Context HttpServletRequest request,
			@Context HttpServletResponse response,
			@PathParam("studyOID") String studyOID,
			@QueryParam("formID") String crfOID,
			@RequestHeader("Authorization") String authorization)
	{
        
		StudyDAO sdao = new StudyDAO(getDataSource());
		StudyBean study = sdao.findByOid(studyOID);

		CRFDAO cdao = new CRFDAO(getDataSource());
        Collection<CRFBean> crfs = cdao.findAll();

        CRFVersionDAO cVersionDao = new CRFVersionDAO(getDataSource());
		Collection<CRFVersionBean> crfVersions = cVersionDao.findAll();

        try
        {
        	XFormList formList = new XFormList();
        	for (CRFBean crf : crfs)
			{
        		for (CRFVersionBean version : crfVersions)
        		{
        			if (version.getCrfId() == crf.getId())
        			{
						XForm form = new XForm(crf, version);
						//TODO: Need to generate hash based on contents of XForm.  Will be done in a later story.
						//TODO: For now all XForms get a date based hash to trick Enketo into always downloading
						//TODO: them.
						Calendar cal = Calendar.getInstance();
						cal.setTime(new Date());
						form.setHash(DigestUtils.md5Hex(String.valueOf(cal.getTimeInMillis())));
						
						String urlBase = getCoreResources().getDataInfo().getProperty("sysURL").split("/MainMenu")[0];
						form.setDownloadURL(urlBase + "/rest2/openrosa/" + studyOID + "/formXml?formId=" + version.getOid());
						formList.add(form);
        			}
        		}
			}
			
	        // Create the XML formList using a Castor mapping file.
	        XMLContext xmlContext = new XMLContext();
	        Mapping mapping = xmlContext.createMapping();
	        mapping.loadMapping(getCoreResources().getURL("openRosaFormListMapping.xml"));	
	        xmlContext.addMapping(mapping);

	        Marshaller marshaller = xmlContext.createMarshaller();
	        StringWriter writer = new StringWriter();
	        marshaller.setWriter(writer);
	        marshaller.marshal(formList);
		
	        // Set response headers
			Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
			Date currentDate = new Date();
			cal.setTime(currentDate);
			SimpleDateFormat format = new SimpleDateFormat("E, dd MMM yyyy HH:mm:ss zz");
			format.setCalendar(cal);
			response.setHeader("Date",  format.format(currentDate));
			response.setHeader("X-OpenRosa-Version", "1.0");

			return writer.toString();
        }
        catch (Exception e)
        {
        	LOGGER.debug(e.getMessage());
        	return "<Error>" + e.getMessage() + "</Error>";
        }
	}
	
	@GET
	@Path("/{studyOID}/formXml")
	@Produces(MediaType.APPLICATION_XML)
	public String getFormXml(@Context HttpServletRequest request,
			@Context HttpServletResponse response,
			@PathParam("studyOID") String studyOID,
			@QueryParam("formID") String crfOID,
			@RequestHeader("Authorization") String authorization)
	{	
		
		String xform = null;

		// get parameters
		String formId = request.getParameter("formId");
		if (formId == null) {
			return "<error>formID is null :(</error>";
		}
			
		try
		{
			OpenRosaXmlGenerator generator = new OpenRosaXmlGenerator(coreResources);
			xform = generator.buildForm(formId);
		} 
		catch (Exception e) 
		{
			return "<error>" + e.getMessage() + "</error>";
		}
		
		response.setHeader("Content-Type", "text/xml; charset=UTF-8");		
		response.setHeader("Content-Disposition", "attachment; filename=\"" + crfOID + ".xml" +  "\";");
		response.setContentType("text/xml; charset=utf-8");
		return xform;
		    
	}    

	
	
	public DataSource getDataSource() {
		return dataSource;
	}

	public void setDataSource(DataSource dataSource) {
		this.dataSource = dataSource;
	}

	public CoreResources getCoreResources() {
		return coreResources;
	}

	public void setCoreResources(CoreResources coreResources) {
		this.coreResources = coreResources;
	}
	  
}
