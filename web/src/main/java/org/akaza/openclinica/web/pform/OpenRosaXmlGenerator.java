package org.akaza.openclinica.web.pform;

import org.akaza.openclinica.dao.core.CoreResources;
import org.akaza.openclinica.web.pform.dto.*;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.exception.ExceptionUtils;

import org.exolab.castor.mapping.Mapping;
import org.exolab.castor.xml.Marshaller;
import org.exolab.castor.xml.Unmarshaller;
import org.exolab.castor.xml.XMLContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import java.io.*;


public class OpenRosaXmlGenerator {

	private XMLContext xmlContext = null;
    protected final Logger log = LoggerFactory.getLogger(OpenRosaXmlGenerator.class);

    public OpenRosaXmlGenerator(CoreResources core) throws Exception
    {
    	try
    	{
	    	xmlContext = new XMLContext();
	        Mapping mapping = xmlContext.createMapping();
	        mapping.loadMapping(core.getURL("openRosaXFormMapping.xml"));
	        xmlContext.addMapping(mapping);        	
        }
    	catch (Exception e)
    	{
    		log.error(e.getMessage());
    		log.error(ExceptionUtils.getStackTrace(e));
    		throw new Exception(e);
    	}
    }
    
    public String buildForm(String formId) throws Exception
    {
    	//TODO: Eventually this function needs to query the DB for the relevant data
    	//TODO: and build the DTO objects.  Then it would run the Castor transformation.

    	try
    	{
	    	StringWriter writer = new StringWriter();
	    	IOUtils.copy(getClass().getResourceAsStream("/properties/xform_template.xml"), writer, "UTF-8");
	    	String xform = writer.toString();
	    	
	    	HtmlDTO html = buildJavaXForm(xform);
	    	String xformString =  buildStringXForm(html);
	    	return xformString;
        }
    	catch (Exception e)
    	{
    		log.error(e.getMessage());
    		log.error(ExceptionUtils.getStackTrace(e));
    		throw new Exception(e);
    	}
    }
        
    private HtmlDTO buildJavaXForm(String content) throws Exception 
    {
        //XML to Object
        Reader reader = new StringReader(content);
        Unmarshaller unmarshaller = xmlContext.createUnmarshaller();
        unmarshaller.setClass(HtmlDTO.class);
        HtmlDTO html = (HtmlDTO) unmarshaller.unmarshal(reader);
        reader.close();
        return html;

    }
    
    private String buildStringXForm(HtmlDTO html) throws Exception
    {
    	StringWriter writer = new StringWriter();

    	Marshaller marshaller = xmlContext.createMarshaller();        
        marshaller.setNamespaceMapping("h", "http://www.w3.org/1999/xhtml");
        marshaller.setNamespaceMapping("jr", "http://openrosa.org/javarosa");
        marshaller.setNamespaceMapping("xsd","http://www.w3.org/2001/XMLSchema");
        marshaller.setNamespaceMapping("ev", "http://www.w3.org/2001/xml-events");
        marshaller.setNamespaceMapping("", "http://www.w3.org/2002/xforms");        
        marshaller.setWriter(writer);
        marshaller.marshal(html);
        return writer.toString();	
    }

}