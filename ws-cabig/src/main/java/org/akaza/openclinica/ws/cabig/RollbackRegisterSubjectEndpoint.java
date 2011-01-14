package org.akaza.openclinica.ws.cabig;

import org.akaza.openclinica.dao.core.CoreResources;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ws.server.endpoint.annotation.Endpoint;
import org.springframework.ws.server.endpoint.annotation.PayloadRoot;
import org.springframework.ws.server.endpoint.annotation.XPathParam;
import org.springframework.context.MessageSource;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import java.util.Locale;

import javax.sql.DataSource;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Source;
import javax.xml.transform.dom.DOMSource;

@Endpoint 
public class RollbackRegisterSubjectEndpoint {

    protected final Logger logger = LoggerFactory.getLogger(getClass().getName());
    private final String NAMESPACE_URI_V1 = "http://openclinica.org/ws/cabig/v1";
    private final String CONNECTOR_NAMESPACE_V1 = "http://clinicalconnector.nci.nih.gov";

    private final DataSource dataSource;
    private final MessageSource messages;
    private final CoreResources coreResources;
    private final Locale locale;
    
    public RollbackRegisterSubjectEndpoint(DataSource dataSource, MessageSource messages, CoreResources coreResources) {
        this.dataSource = dataSource;
        this.messages = messages;
        this.coreResources = coreResources;
        
        this.locale = new Locale("en_US");
    }
    
    //@PayloadRoot(localPart = "RegisterSubjectRequest", namespace = CONNECTOR_NAMESPACE_V1)
    public Source registerSubject(@XPathParam("//connector:studySubject") NodeList subjects, 
            @XPathParam("//connector:studySubject/birthDate/@value") String birthdate) throws Exception {
    
        Element subjectElement = (Element) (subjects.item(0));
        System.out.println("rootElement=");
        logger.debug("rootElement=");
        System.out.println("rootElement=" + subjectElement.toString());
        
        return new DOMSource(mapConfirmation());
    }
    
    private Element mapConfirmation() throws Exception {
        DocumentBuilderFactory dbfac = DocumentBuilderFactory.newInstance();
        DocumentBuilder docBuilder = dbfac.newDocumentBuilder();
        Document document = docBuilder.newDocument();
        
        Element responseElement = document.createElementNS(CONNECTOR_NAMESPACE_V1, "importDataResponse");
        
        return responseElement;
    }
    
}
