package org.akaza.openclinica.web.pform;

import org.akaza.openclinica.bean.submit.CRFVersionBean;
import org.akaza.openclinica.bean.submit.ItemBean;
import org.akaza.openclinica.bean.submit.SectionBean;
import org.akaza.openclinica.control.managestudy.CRFVersionMetadataUtil;
import org.akaza.openclinica.dao.core.CoreResources;
import org.akaza.openclinica.dao.submit.CRFVersionDAO;
import org.akaza.openclinica.web.pform.dto.*;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.exception.ExceptionUtils;

import org.exolab.castor.mapping.Mapping;
import org.exolab.castor.xml.Marshaller;
import org.exolab.castor.xml.Unmarshaller;
import org.exolab.castor.xml.XMLContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;


import java.io.*;
import java.util.ArrayList;

import javax.sql.DataSource;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;


public class OpenRosaXmlGenerator {

	private XMLContext xmlContext = null;
	private DataSource dataSource = null;
    protected final Logger log = LoggerFactory.getLogger(OpenRosaXmlGenerator.class);

    public static final String OC_DATATYPE_STRING = "st";
    
    public OpenRosaXmlGenerator(CoreResources core,DataSource dataSource) throws Exception
    {
    	this.dataSource = dataSource;

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
    	try
    	{
	    	CRFVersionDAO versionDAO = new CRFVersionDAO(dataSource);
	    	CRFVersionBean crfVersion = versionDAO.findByOid(formId);
	    	CRFVersionMetadataUtil metadataUtil = new CRFVersionMetadataUtil(dataSource);
	    	ArrayList<SectionBean> crfSections = metadataUtil.retrieveFormMetadata(crfVersion);
	    	
	    	StringWriter writer = new StringWriter();
	    	IOUtils.copy(getClass().getResourceAsStream("/properties/xform_template.xml"), writer, "UTF-8");
	    	String xform = writer.toString();	    	
	    	HtmlDTO html = buildJavaXForm(xform);

	    	mapBeansToDTO(html,crfVersion,crfSections);

	    	String xformMinusInstance =  buildStringXForm(html);
	    	String preInstance = xformMinusInstance.substring(0,xformMinusInstance.indexOf("<instance>"));
			String instance = buildInstance(html.getHead().getModel(),crfVersion,crfSections);
	    	String postInstance = xformMinusInstance.substring(xformMinusInstance.indexOf("</instance>")+"</instance>".length());
	    	
	    	return preInstance + "<instance>\n" + instance + "\n</instance>" + postInstance;
        }
    	catch (Exception e)
    	{
    		log.error(e.getMessage());
    		log.error(ExceptionUtils.getStackTrace(e));
    		throw new Exception(e);
    	}
    }
        
    private void mapBeansToDTO(HtmlDTO html, CRFVersionBean crfVersion,
			ArrayList<SectionBean> crfSections) throws Exception
    {
		BodyDTO body = html.getBody();
		ArrayList<GroupDTO> groups = new ArrayList<GroupDTO>();
		GroupDTO group = new GroupDTO();
		group.setInput(new ArrayList<InputDTO>());
		ArrayList<BindDTO> bindList = new ArrayList<BindDTO>();
	
    	html.getHead().setTitle(crfVersion.getOid());

		for (SectionBean section:crfSections)
		{
			ArrayList<ItemBean> items = section.getItems();
			for (ItemBean item:items)
			{
				if (item.getDataType().getName().equals(OC_DATATYPE_STRING))
				{
					String reference = "/" + crfVersion.getOid() + "/" + item.getOid();
					//Build Binding
					BindDTO bind = new BindDTO();
					bind.setNodeSet(reference);
					bind.setType("string");
					bindList.add(bind);
					//Build Question
					LabelDTO label = new LabelDTO();
					InputDTO input = new InputDTO();
					label.setLabel(item.getItemMeta().getLeftItemText());
					input.setLabel(label);
					input.setRef(reference);
					group.getInput().add(input);
				}
				else
				{
					log.debug("Unsupported datatype encountered while loading PForm (" + item.getDataType().getName() + "). Skipping.");
				}
			}
		}
		groups.add(group);
		body.setGroup(groups);
		html.getHead().getModel().setBind(bindList);
	}

	private String buildInstance(ModelDTO model, CRFVersionBean crfVersion,
		ArrayList<SectionBean> crfSections)  throws Exception
	{
		DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder build = docFactory.newDocumentBuilder();
        Document doc = build.newDocument();
        Element root = doc.createElement(crfVersion.getOid());
        root.setAttribute("id", crfVersion.getOid());
        doc.appendChild(root);
        for (SectionBean section:crfSections)
        {
        	ArrayList<ItemBean> items = section.getItems();
	        for (ItemBean item:items) {
				if (item.getDataType().getName().equals(OC_DATATYPE_STRING))
				{
		            Element question = doc.createElement(item.getOid());
		            root.appendChild(question);
				}
	        }
        }

        TransformerFactory transformFactory = TransformerFactory.newInstance();
        Transformer transformer = transformFactory.newTransformer();
        transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        StringWriter writer = new StringWriter();
        StreamResult result = new StreamResult(writer);
        DOMSource source = new DOMSource(doc);
        transformer.transform(source, result);
        return writer.toString();

		
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