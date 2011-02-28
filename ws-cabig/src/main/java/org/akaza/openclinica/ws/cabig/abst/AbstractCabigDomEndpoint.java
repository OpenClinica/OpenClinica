/*
 * OpenClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: http://www.openclinica.org/license
 * copyright 2010-2011 Akaza Research

 * Development of this web service or portions thereof has been funded
 * by Federal Funds from the National Cancer Institute, 
 * National Institutes of Health, under Contract No. HHSN261200800001E.
 * In addition to the GNU LGPL license, this code is also available
 * from NCI CBIIT repositories under the terms of the caBIG Software License. 
 * For details see: https://cabig.nci.nih.gov/adopt/caBIGModelLicense
 */
package org.akaza.openclinica.ws.cabig.abst;

import org.akaza.openclinica.bean.core.Role;
import org.akaza.openclinica.bean.login.UserAccountBean;
import org.akaza.openclinica.dao.core.CoreResources;
import org.akaza.openclinica.dao.login.UserAccountDAO;
import org.akaza.openclinica.dao.managestudy.StudyDAO;
import org.akaza.openclinica.dao.managestudy.StudyEventDAO;
import org.akaza.openclinica.dao.managestudy.StudyEventDefinitionDAO;
import org.akaza.openclinica.dao.managestudy.StudySubjectDAO;
import org.akaza.openclinica.dao.service.StudyParameterValueDAO;
import org.akaza.openclinica.dao.submit.CRFVersionDAO;
import org.akaza.openclinica.dao.submit.EventCRFDAO;
import org.akaza.openclinica.dao.submit.ItemDAO;
import org.akaza.openclinica.dao.submit.ItemDataDAO;
import org.akaza.openclinica.dao.submit.ItemGroupDAO;
import org.akaza.openclinica.dao.submit.SubjectDAO;
import org.akaza.openclinica.exception.OpenClinicaException;
import org.akaza.openclinica.ws.cabig.exception.CCSystemFaultException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.MessageSource;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.ws.server.endpoint.AbstractDomPayloadEndpoint;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import javax.sql.DataSource;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

public class AbstractCabigDomEndpoint extends AbstractDomPayloadEndpoint {

    protected final Logger logger = LoggerFactory.getLogger(getClass().getName());
    public final String NAMESPACE_URI_V1 = "http://openclinica.org/ws/cabig/v1";// TODO keep or toss?
    public final String CONNECTOR_NAMESPACE_V1 = "http://clinicalconnector.nci.nih.gov";
    public final String XSL_NAMESPACE = "http://www.w3.org/2001/XMLSchema-instance";
    public final String ISO_21090_NAMESPACE = "uri:iso.org:21090";
    public final String SOAP_NAMESPACE = "http://www.w3.org/2003/05/soap-envelope";

    SubjectDAO subjectDao;
    StudyDAO studyDao;
    StudySubjectDAO studySubjectDao;
    StudyParameterValueDAO studyParamValueDao;
    ItemDAO itemDao;
    ItemGroupDAO itemGroupDao;
    CRFVersionDAO crfVersionDao;
    StudyEventDefinitionDAO studyEventDefinitionDao;
    StudyEventDAO studyEventDao;
    EventCRFDAO eventCrfDao;
    ItemDataDAO itemDataDao;
    UserAccountDAO userAccountDao;

    public DataSource dataSource;
    public MessageSource messages;
    public CoreResources coreResources;
    public Locale locale;

    public AbstractCabigDomEndpoint(DataSource dataSource, MessageSource messages, CoreResources coreResources) {
        this.dataSource = dataSource;
        this.messages = messages;
        this.coreResources = coreResources;

        this.locale = new Locale("en_US");
        this.setAlwaysTransform(true);
    }

    protected Element invokeInternal(Element requestElement, Document document) throws Exception {
        return requestElement;
    }

    /**
     * the dao getters.
     * 
     * @return
     */
    public SubjectDAO getSubjectDao() {
        subjectDao = subjectDao != null ? subjectDao : new SubjectDAO(dataSource);
        return subjectDao;
    }

    public StudyDAO getStudyDao() {
        studyDao = studyDao != null ? studyDao : new StudyDAO(dataSource);
        return studyDao;
    }

    public StudySubjectDAO getStudySubjectDao() {
        studySubjectDao = studySubjectDao != null ? studySubjectDao : new StudySubjectDAO(dataSource);
        return studySubjectDao;
    }

    public StudyParameterValueDAO getStudyParamValueDao() {
        studyParamValueDao = studyParamValueDao != null ? studyParamValueDao : new StudyParameterValueDAO(dataSource);
        return studyParamValueDao;
    }

    public ItemDAO getItemDao() {
        itemDao = itemDao != null ? itemDao : new ItemDAO(dataSource);
        return itemDao;
    }

    public ItemGroupDAO getItemGroupDao() {
        itemGroupDao = itemGroupDao != null ? itemGroupDao : new ItemGroupDAO(dataSource);
        return itemGroupDao;
    }

    public CRFVersionDAO getCrfVersionDao() {
        crfVersionDao = crfVersionDao != null ? crfVersionDao : new CRFVersionDAO(dataSource);
        return crfVersionDao;
    }

    public StudyEventDefinitionDAO getStudyEventDefinitionDao() {
        studyEventDefinitionDao = studyEventDefinitionDao != null ? studyEventDefinitionDao : new StudyEventDefinitionDAO(dataSource);
        return studyEventDefinitionDao;
    }

    public StudyEventDAO getStudyEventDao() {
        studyEventDao = studyEventDao != null ? studyEventDao : new StudyEventDAO(dataSource);
        return studyEventDao;
    }

    public EventCRFDAO getEventCrfDao() {
        eventCrfDao = eventCrfDao != null ? eventCrfDao : new EventCRFDAO(dataSource);
        return eventCrfDao;
    }

    public ItemDataDAO getItemDataDao() {
        itemDataDao = itemDataDao != null ? itemDataDao : new ItemDataDAO(dataSource);
        return itemDataDao;
    }

    public UserAccountDAO getUserAccountDao() {
        userAccountDao = userAccountDao != null ? userAccountDao : new UserAccountDAO(dataSource);
        return userAccountDao;
    }

    /**
     * Helper Method to get the user account
     * 
     * 
     * @return UserAccountBean
     */
    protected UserAccountBean getUserAccount() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String username = null;
        if (principal instanceof UserDetails) {
            username = ((UserDetails) principal).getUsername();
        } else {
            username = principal.toString();
        }
        UserAccountDAO userAccountDao = new UserAccountDAO(dataSource);
        return (UserAccountBean) userAccountDao.findByUserName(username);
    }

    /**
     * simple helper method to figure out structure of node list passed to it
     * 
     * @param nlist
     */
    public void logNodeList(NodeList nlist) {
        if (nlist.getLength() > 0) {
            for (int i = 0; i < nlist.getLength(); i++) {
                try {
                    Node childNode = nlist.item(i);
                    System.out.println("node: " + childNode.getNodeName() + " -> " + childNode.getNodeValue());
                    // + " : " + childNode.getTextContent());
                    if (childNode.getChildNodes().getLength() > 0) {
                        System.out.println("found child nodes: " + childNode.getChildNodes().getLength());
                        logNodeList(childNode.getChildNodes());
                    }
                    if (childNode.hasAttributes()) {
                        System.out.print("found attributes " + childNode.getAttributes().getLength());
                        System.out.println(": " + childNode.getAttributes().item(0).getNodeName());
                    }
                } catch (Exception ee) {
                    // trying to catch all NPEs here, tbh
                    System.out.println("found a nullpointer");
                }
            }
        }
    }

    /**
     * custom export to generate the register subject response
     * 
     * @param studySubjectId
     * @return
     * @throws Exception
     */
    public Element mapRegisterSubjectConfirmation(String studySubjectId) throws Exception {
        DocumentBuilderFactory dbfac = DocumentBuilderFactory.newInstance();
        DocumentBuilder docBuilder = dbfac.newDocumentBuilder();
        Document document = docBuilder.newDocument();

        Element responseElement = document.createElementNS(CONNECTOR_NAMESPACE_V1, "RegisterSubjectResponse");

        Element indicator = document.createElementNS(CONNECTOR_NAMESPACE_V1, "indicator");
        Attr typeAttr = document.createAttributeNS(XSL_NAMESPACE, "type");
        typeAttr.setNodeValue("BL");
        // indicator.setAttributeNS(XSL_NAMESPACE, "type", "II");
        indicator.setAttributeNode(typeAttr);
        indicator.setAttribute("value", "true");

        responseElement.appendChild(indicator);
        Element patientIdentifier = document.createElementNS(CONNECTOR_NAMESPACE_V1, "patientIdentifier");
        patientIdentifier.setAttribute("root", "2.16.840.1.113883.3.26.7.6");
        Attr typeAttr2 = document.createAttributeNS(XSL_NAMESPACE, "type");
        typeAttr2.setNodeValue("II");
        patientIdentifier.setAttributeNode(typeAttr2);
        // extension="503" identifierName="Patient Position" displayable="false"
        patientIdentifier.setAttribute("extension", studySubjectId);

        patientIdentifier.setAttribute("identifierName", "Study Subject Identifier");
        patientIdentifier.setAttribute("displayable", "false");
        responseElement.appendChild(patientIdentifier);
        return responseElement;
    }

    public Element mapGenericErrorConfirmation(String message, OpenClinicaException exception, HashMap<String, String> validations) throws Exception {
        DocumentBuilderFactory dbfac = DocumentBuilderFactory.newInstance();
        DocumentBuilder docBuilder = dbfac.newDocumentBuilder();
        Document document = docBuilder.newDocument();

        // Element responseElement = document.createElementNS(CONNECTOR_NAMESPACE_V1, "RegisterSubjectResponse");
        // Element indicator = document.createElementNS(CONNECTOR_NAMESPACE_V1, "indicator");
        // Attr typeAttr = document.createAttributeNS(XSL_NAMESPACE, "type");
        // typeAttr.setNodeValue("BL");
        // // indicator.setAttributeNS(XSL_NAMESPACE, "type", "II");
        // indicator.setAttributeNode(typeAttr);
        // indicator.setAttribute("value", "false");
        // responseElement.appendChild(indicator);
        // append message here
        // 7520, changing the error messages to show the error types

        Element faultElement = document.createElementNS(this.SOAP_NAMESPACE, "Fault");
        Element faultCode = document.createElementNS(SOAP_NAMESPACE, "Code");
        Element faultValue = document.createElementNS(SOAP_NAMESPACE, "Value");
        faultValue.setTextContent("Receiver");
        faultCode.appendChild(faultValue);
        faultElement.appendChild(faultCode);

        Element faultReason = document.createElementNS(SOAP_NAMESPACE, "Reason");
        Element faultText = document.createElementNS(SOAP_NAMESPACE, "Text");
        faultText.setTextContent(exception.className + "FaultMessage");// to be revised
        faultReason.appendChild(faultText);
        faultElement.appendChild(faultReason);

        Element faultDetail = document.createElementNS(SOAP_NAMESPACE, "Detail");
        Element faultDetailMessage = document.createElementNS(CONNECTOR_NAMESPACE_V1, exception.className + "Fault"); // to be revised
        // add type
        faultDetailMessage.setAttributeNS(this.XSL_NAMESPACE, "type", exception.className + "Error");
        // Element errormessage = document.createElementNS(CONNECTOR_NAMESPACE_V1, "message");
        // String confirmation = messages.getMessage("dataEndpoint.success", null, "Success", locale);
        Element code = document.createElementNS(CONNECTOR_NAMESPACE_V1, "code");
        code.setTextContent(exception.errorID);
        // TODO change to accept error codes
        // errormessage.appendChild(code);
        Element reason = document.createElementNS(CONNECTOR_NAMESPACE_V1, "message");
        reason.setTextContent(exception.message);
        if ("CCBusiness".equals(exception.className)) {
            faultDetailMessage.appendChild(reason);
            faultDetailMessage.appendChild(code);
        } else if ("CCDataValidation".equals(exception.className)) {
            faultDetailMessage.appendChild(reason);
            faultDetailMessage.appendChild(code);
            java.util.Iterator itValidations = validations.entrySet().iterator();
            while (itValidations.hasNext()) {
                Map.Entry pair = (Map.Entry) itValidations.next();
                Element validationMessage = document.createElementNS(CONNECTOR_NAMESPACE_V1, "ValidationError");
                Element inputName = document.createElementNS(CONNECTOR_NAMESPACE_V1, "inputName");
                inputName.setTextContent((String) pair.getKey());
                validationMessage.appendChild(inputName);
                // Element attributeName = document.createElementNS(CONNECTOR_NAMESPACE_V1, "attributeName");
                // attributeName.setTextContent((String) pair.getKey());
                // validationMessage.appendChild(attributeName);
                Element inputMessage = document.createElementNS(CONNECTOR_NAMESPACE_V1, "message");
                inputMessage.setTextContent((String) pair.getValue());// was pair.getValue()
                validationMessage.appendChild(inputMessage);
                faultDetailMessage.appendChild(validationMessage);
            }
        } else {
            // default code + message combination
            // errormessage.appendChild(reason);
            faultDetailMessage.appendChild(code);
            faultDetailMessage.appendChild(reason);
        }
        // append validation messages here

        faultDetail.appendChild(faultDetailMessage);
        faultElement.appendChild(faultDetail);
        // responseElement.appendChild(errormessage);
        // add subject message here?
        // return responseElement;
        return faultElement;
    }

    public Element mapSubjectErrorConfirmation(String message, OpenClinicaException exception, HashMap<String, String> validations) throws Exception {
        return mapGenericErrorConfirmation(message, exception, validations);
    }

    public Element mapSubjectErrorConfirmation(String message) throws Exception {

        return mapGenericErrorConfirmation(message, new CCSystemFaultException(""), new HashMap<String, String>());
    }

    public Element mapCreateStudyConfirmation(String studyIdentifierStr) throws Exception {
        DocumentBuilderFactory dbfac = DocumentBuilderFactory.newInstance();
        DocumentBuilder docBuilder = dbfac.newDocumentBuilder();
        Document document = docBuilder.newDocument();

        Element responseElement = document.createElementNS(CONNECTOR_NAMESPACE_V1, "CreateStudyResponse");
        // dry
        Element indicator = document.createElementNS(CONNECTOR_NAMESPACE_V1, "indicator");
        Attr typeAttr = document.createAttributeNS(XSL_NAMESPACE, "type");
        typeAttr.setNodeValue("BL");
        // indicator.setAttributeNS(XSL_NAMESPACE, "type", "II");
        indicator.setAttributeNode(typeAttr);
        indicator.setAttribute("value", "true");

        responseElement.appendChild(indicator);
        // dry
        Element studyIdentifier = document.createElementNS(CONNECTOR_NAMESPACE_V1, "studyIdentifier");
        studyIdentifier.setAttribute("root", "2.16.840.1.113883.3.26.7.6");
        Attr typeAttr2 = document.createAttributeNS(XSL_NAMESPACE, "type");
        typeAttr2.setNodeValue("II");
        studyIdentifier.setAttributeNode(typeAttr2);
        // extension="503" identifierName="Patient Position" displayable="false"
        studyIdentifier.setAttribute("extension", studyIdentifierStr);

        studyIdentifier.setAttribute("identifierName", "Study Identifier");
        studyIdentifier.setAttribute("displayable", "false");
        responseElement.appendChild(studyIdentifier);
        return responseElement;
    }

    public Element mapStudyErrorConfirmation(String message) throws Exception {

        return mapGenericErrorConfirmation(message, new CCSystemFaultException(""), new HashMap<String, String>());
    }

    public Element mapStudyErrorConfirmation(String message, OpenClinicaException exception, HashMap<String, String> validations) throws Exception {

        return mapGenericErrorConfirmation(message, exception, validations);
    }

    public Element mapLoadLabsConfirmation() throws Exception {
        DocumentBuilderFactory dbfac = DocumentBuilderFactory.newInstance();
        DocumentBuilder docBuilder = dbfac.newDocumentBuilder();
        Document document = docBuilder.newDocument();

        Element responseElement = document.createElementNS(CONNECTOR_NAMESPACE_V1, "LoadLabsResponse");
        // dry
        Element indicator = document.createElementNS(CONNECTOR_NAMESPACE_V1, "indicator");
        Attr typeAttr = document.createAttributeNS(XSL_NAMESPACE, "type");
        typeAttr.setNodeValue("BL");
        // indicator.setAttributeNS(XSL_NAMESPACE, "type", "II");
        indicator.setAttributeNode(typeAttr);
        indicator.setAttribute("value", "true");

        responseElement.appendChild(indicator);
        // dry
        return responseElement;
    }

    public Element mapLoadLabsErrorConfirmation(String message, OpenClinicaException exception, HashMap<String, String> validations) throws Exception {

        return mapGenericErrorConfirmation(message, exception, validations);
    }

    public boolean canUserRegisterSubject(UserAccountBean user) {
        Role r = user.getActiveStudyRole();
        if (r != null
            && (r.equals(Role.COORDINATOR) || r.equals(Role.STUDYDIRECTOR) || r.equals(Role.INVESTIGATOR) || r.equals(Role.RESEARCHASSISTANT) || r
                    .equals(Role.ADMIN))) {
            // you may pass
            return true;
        } else {
            return false;
        }
    }

}
