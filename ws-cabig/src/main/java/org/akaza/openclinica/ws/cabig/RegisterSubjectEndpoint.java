package org.akaza.openclinica.ws.cabig;

import org.akaza.openclinica.dao.core.CoreResources;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.oxm.Marshaller;
import org.springframework.util.xml.DomUtils;
import org.springframework.ws.server.endpoint.AbstractDomPayloadEndpoint;
import org.springframework.ws.server.endpoint.AbstractMarshallingPayloadEndpoint;
import org.springframework.ws.server.endpoint.annotation.Endpoint;
import org.springframework.ws.server.endpoint.annotation.PayloadRoot;
import org.springframework.ws.server.endpoint.annotation.XPathParam;
import org.springframework.ws.soap.addressing.server.annotation.Action;
import org.springframework.context.MessageSource;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.Locale;

import javax.sql.DataSource;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Source;
import javax.xml.transform.dom.DOMSource;

// @Endpoint
public class RegisterSubjectEndpoint extends AbstractDomPayloadEndpoint {//extends AbstractMarshallingPayloadEndpoint {//

    protected final Logger logger = LoggerFactory.getLogger(getClass().getName());
    private final String NAMESPACE_URI_V1 = "http://openclinica.org/ws/cabig/v1";
    private final String CONNECTOR_NAMESPACE_V1 = "http://clinicalconnector.nci.nih.gov";

    private final DataSource dataSource;
    private final MessageSource messages;
    private final CoreResources coreResources;
    private final Locale locale;
    
    public RegisterSubjectEndpoint(DataSource dataSource, MessageSource messages, CoreResources coreResources) {//, Marshaller jaxp2Marshaller) {
        // super(jaxp2Marshaller);
        this.dataSource = dataSource;
        this.messages = messages;
        this.coreResources = coreResources;
        
        this.locale = new Locale("en_US");
        this.setAlwaysTransform(true);
    }
    
    protected Element invokeInternal(//Object request) throws Exception {
            Element requestElement,
            Document document) throws Exception {
        System.out.println("Request text ");
        // System.out.println("rootElement=" + requestElement.toString());
        NodeList subjects = requestElement.getElementsByTagNameNS(CONNECTOR_NAMESPACE_V1, "RegisterSubjectRequest");
        System.out.println("found " + subjects.getLength() + " subjects");
        subjects = requestElement.getElementsByTagName("ns2:RegisterSubjectRequest");
        System.out.println("found " + subjects.getLength() + " subjects with literal tag name");
        NodeList childNodes = requestElement.getChildNodes();
        System.out.println("found " + childNodes.getLength() + " child nodes");
        for (int i=0; i<childNodes.getLength(); i++) {
            Node childNode = childNodes.item(i);
            System.out.println("node: " + childNode.getNodeName() + " -> " + childNode.getNodeValue());
            if (childNode.getChildNodes().getLength() > 0) {
                System.out.println("found child nodes: " + childNode.getChildNodes().getLength());
            }
        }
        
        subjects = requestElement.getElementsByTagNameNS(CONNECTOR_NAMESPACE_V1, "studySubject");
        if (subjects.getLength() > 0) {
            System.out.println("found study subject: " + subjects.getLength());
            for (int i=0; i<subjects.getLength(); i++) {
                Node childNode = subjects.item(i);
                System.out.println("node: " + childNode.getNodeName() + " -> " + childNode.getNodeValue());
                if (childNode.getChildNodes().getLength() > 0) {
                    System.out.println("found child nodes: " + childNode.getChildNodes().getLength());
                }
            }
        }
        
        // String requestText = requestElement.getTextContent();
        // String strDocument = document.toString();
        // NodeList subjects = document.getElementsByTagName("RegisterSubjectRequest");
        // Element subjectElement = (Element) subjects.item(0);
        // System.out.println("rootElement=" + subjectElement.toString());
        
        // System.out.println("found request: " + requestText);
        // System.out.println("found document: " + strDocument);
        return mapConfirmation();//new DOMSource(mapConfirmation());
    }

    
    // @Action("registerSubject")
    // @PayloadRoot(localPart = "RegisterSubjectRequest", namespace = CONNECTOR_NAMESPACE_V1)
    public Source registerSubject() throws Exception {
        // todo: use jaxp marshaller to extract the studysubject beans? 
    // public Source registerSubject(@XPathParam("/connector:RegisterSubjectRequest/studySubject") Element subjectElement, 
       //     @XPathParam("/connector:RegisterSubjectRequest/studySubject/birthDate/@value") String birthdate) throws Exception {
        try { 
            System.out.println("rootElement=");
            logger.debug("rootElement=");
        // todo: pull out the action from the header somehow, perhaps using soap util?
        // Element subjectElement = (Element) (subjects.item(0));
        
        // System.out.println("rootElement=" + subjectElement.toString());
        
            return new DOMSource(mapConfirmation());
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }
    
    private Element mapConfirmation() throws Exception {
        DocumentBuilderFactory dbfac = DocumentBuilderFactory.newInstance();
        DocumentBuilder docBuilder = dbfac.newDocumentBuilder();
        Document document = docBuilder.newDocument();
        
        Element responseElement = document.createElementNS(CONNECTOR_NAMESPACE_V1, "RegisterSubjectResponse");
        Element resultElement = document.createElementNS(CONNECTOR_NAMESPACE_V1, "result");
        String confirmation = messages.getMessage("dataEndpoint.success", null, "Success", locale);
        resultElement.setTextContent(confirmation);
        responseElement.appendChild(resultElement);
        return responseElement;
    }
    
//    @Action("registerSubject")
//    public Source registerSubject() throws Exception {
//        logger.debug("FOUND REGISTER SUBJECT");
//        return new DOMSource(mapConfirmation());
//    }
//    
//    @Action("rollbackRegisterSubject")
//    public Source rollbackRegisterSubject() throws Exception {
//        logger.debug("FOUND ROLLBACK REGISTER SUBJECT");
//        return new DOMSource(mapConfirmation());
//    }
}
