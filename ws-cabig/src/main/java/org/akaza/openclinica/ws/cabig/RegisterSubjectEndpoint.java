package org.akaza.openclinica.ws.cabig;

import org.akaza.openclinica.dao.core.CoreResources;
import org.akaza.openclinica.dao.login.UserAccountDAO;
import org.akaza.openclinica.dao.managestudy.StudyDAO;
import org.akaza.openclinica.dao.managestudy.StudySubjectDAO;
import org.akaza.openclinica.dao.submit.SubjectDAO;
import org.akaza.openclinica.bean.core.Role;
import org.akaza.openclinica.bean.core.Status;
import org.akaza.openclinica.bean.login.UserAccountBean;
import org.akaza.openclinica.bean.managestudy.StudyBean;
import org.akaza.openclinica.bean.managestudy.StudySubjectBean;
import org.akaza.openclinica.bean.submit.SubjectBean;
import org.akaza.openclinica.exception.OpenClinicaException;
import org.akaza.openclinica.ws.bean.RegisterSubjectBean;
import org.akaza.openclinica.ws.logic.RegisterSubjectService;
import org.akaza.openclinica.ws.cabig.exception.CCDataValidationFaultException;
import org.akaza.openclinica.ws.cabig.exception.CCSystemFaultException;
import org.akaza.openclinica.ws.cabig.exception.CCBusinessFaultException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.oxm.Marshaller;
import org.springframework.util.xml.DomUtils;
import org.springframework.ws.server.endpoint.AbstractDomPayloadEndpoint;
import org.springframework.context.MessageSource;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.NamedNodeMap;

import java.util.Locale;

import javax.sql.DataSource;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Source;
import javax.xml.transform.dom.DOMSource;


public class RegisterSubjectEndpoint extends AbstractDomPayloadEndpoint {

    protected final Logger logger = LoggerFactory.getLogger(getClass().getName());
    private final String NAMESPACE_URI_V1 = "http://openclinica.org/ws/cabig/v1";// TODO keep or toss?
    private final String CONNECTOR_NAMESPACE_V1 = "http://clinicalconnector.nci.nih.gov";
    private final String XSL_NAMESPACE = "http://www.w3.org/2001/XMLSchema-instance";
    private final String ISO_21090_NAMESPACE = "uri:iso.org:21090";

    private final DataSource dataSource;
    private final MessageSource messages;
    private final CoreResources coreResources;// TODO keep or toss?
    private final Locale locale;
    private final RegisterSubjectService subjectService;
    SubjectDAO subjectDao;
    StudyDAO studyDao;
    StudySubjectDAO studySubjectDao;
    
    public RegisterSubjectEndpoint(DataSource dataSource, MessageSource messages, CoreResources coreResources) {
        
        this.dataSource = dataSource;
        this.messages = messages;
        this.coreResources = coreResources;
        
        this.locale = new Locale("en_US");
        this.setAlwaysTransform(true);
        this.subjectService = new RegisterSubjectService();
    }
    
    protected Element invokeInternal(
            Element requestElement,
            Document document) throws Exception {
        System.out.println("Request text ");
        SubjectBean finalSubjectBean = new SubjectBean();
        finalSubjectBean.setLabel("");
        NodeList subjects = requestElement.getElementsByTagNameNS(CONNECTOR_NAMESPACE_V1, "studySubject");
        try {
            if (subjects.getLength() > 0) {
                System.out.println("found study subject: " + subjects.getLength());
                logNodeList(subjects);
                for (int i=0; i < subjects.getLength(); i++) {
                    // will subjects always be sent one at a time? nta
                    Node childNode = subjects.item(i);
                    // System.out.println("found birthday: " + getBirthdate(childNode));
                    // get user account bean from security here
                    UserAccountBean user = this.getUserAccount();
                    // is this user allowed to create subjects? if not, throw a ccsystem fault exception
                    Role r = user.getActiveStudyRole();
                    if (r != null && (r.equals(Role.COORDINATOR) || r.equals(Role.STUDYDIRECTOR) ||
                            r.equals(Role.INVESTIGATOR) || r.equals(Role.RESEARCHASSISTANT) || r.equals(Role.ADMIN))) {
                        // you may pass
                    } else {
                        throw new CCSystemFaultException("You do not possess the correct privileges to create a subject.");
                    }
                    
                    RegisterSubjectBean subjectBean = subjectService.generateSubjectBean(user, childNode);
                    // performedSubjectMilestone
                    NodeList performedMilestones = requestElement.getElementsByTagNameNS(CONNECTOR_NAMESPACE_V1, "performedSubjectMilestone");
                    if (performedMilestones.getLength() > 0) {
                        for (int j = 0; j < performedMilestones.getLength(); j++) {
                            Node milestone = performedMilestones.item(j);
                            subjectBean = subjectService.attachStudyIdentifiers(subjectBean, milestone);
                        }
                    }
                    // will there ever be more than one subject-study pair sent in a message? tba
                    StudyBean studyBean = new StudyBean();
                    StudyBean siteBean = new StudyBean();
                    
                    if (subjectBean.getSiteUniqueIdentifier() != null) {
                        siteBean = getStudyDao().findByUniqueIdentifier(subjectBean.getSiteUniqueIdentifier());
                    } 
                    studyBean = getStudyDao().findByUniqueIdentifier(subjectBean.getStudyUniqueIdentifier());
                    
                    if (studyBean.getId() <= 0) {
                        // if no study exists with that name, there is an error
                        throw new CCBusinessFaultException("No study exists with that name, please review your information and re-submit the request.");
                    }
                    if (siteBean.getId() > 0) {
                        // if there is a site bean, the study bean should be its parent, otherwise there is an error
                        if ((siteBean.getParentStudyId() != studyBean.getId()) && (siteBean.getParentStudyId() != 0)) {
                            throw new CCBusinessFaultException("Your parent and child study relationship is mismatched." + 
                                    "  Please enter correct study and site information.");
                        }
                        studyBean = siteBean;
                    }
                    
                    // are there errors here? if so, throw a ccbusiness fault
                    finalSubjectBean = subjectService.generateSubjectBean(subjectBean);
                    
                    SubjectBean testSubjectBean = getSubjectDao().findByUniqueIdentifier(subjectBean.getUniqueIdentifier());
                    if (testSubjectBean.getId() > 0) {
                        throw new CCBusinessFaultException("You already have a subject in the database with the unique identifier of " + 
                                subjectBean.getUniqueIdentifier() + ".  Please review your data and re-submit your request.");
                    }
                    // below is point of no return - we have caught all the error and are committing to the database
                    
                    finalSubjectBean = getSubjectDao().create(finalSubjectBean);
                    
                    StudySubjectBean studySubjectBean = subjectService.generateStudySubjectBean(subjectBean, finalSubjectBean, studyBean);
                    
                    studySubjectBean = getStudySubjectDao().create(studySubjectBean, false);
                    System.out.println("finished creation");
                }
            }
            // return success message here
            return mapConfirmation(finalSubjectBean.getLabel());
            //TODO is it actually primary key? nta
        } catch (Exception npe) {
            npe.printStackTrace();
            // TODO figure out exception and send response
            if (npe.getClass().getName().startsWith("org.akaza.openclinica.ws.cabig.exception")) {
                System.out.println("found " + npe.getClass().getName());
                OpenClinicaException ope = (OpenClinicaException) npe;
                return mapErrorConfirmation("", ope);
            } else {
                System.out.println(" did not find openclinica exception, found " + npe.getClass().getName());
                return mapErrorConfirmation(npe.getMessage());
            }
        }
        
        
    }
    
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
    
    /**
     * Helper Method to get the user account
     * TODO: place in a superclass, so that all endpoints dont need this code
     * 
     * @return UserAccountBean
     */
    private UserAccountBean getUserAccount() {
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
     * TODO: place in a superclass, so we don't clutter up the endpoint
     * @param nlist
     */
    private void logNodeList(NodeList nlist) {
        if (nlist.getLength() > 0) {
            for (int i=0; i < nlist.getLength(); i++) {
                try {
                    Node childNode = nlist.item(i);
                    System.out.println("node: " + childNode.getNodeName() + 
                            " -> " + childNode.getNodeValue() +
                            " : " + childNode.getTextContent());
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
     * returns a birthday, just a test class
     * TODO delete
     * @param subject
     * @return
     */
    private String getBirthdate(Node subject) {
        String ret = "";
        Element subjectElement = (Element) subject;
        NodeList birthDate = subjectElement.getElementsByTagNameNS(CONNECTOR_NAMESPACE_V1, "birthDate");
        Node birthDateValue = birthDate.item(0);
        if (birthDateValue.hasAttributes()) {
            NamedNodeMap nodeMap = birthDateValue.getAttributes();
            Node nodeValue = nodeMap.getNamedItem("value");
            ret = nodeValue.getNodeValue();//birthDateValue.getAttributes().getNamedItemNS(CONNECTOR_NAMESPACE_V1, "value").getNodeValue();
        }
        return ret;
        
    }
    
    private Element mapErrorConfirmation(String message, OpenClinicaException exception) throws Exception {
        DocumentBuilderFactory dbfac = DocumentBuilderFactory.newInstance();
        DocumentBuilder docBuilder = dbfac.newDocumentBuilder();
        Document document = docBuilder.newDocument();
        
        Element responseElement = document.createElementNS(CONNECTOR_NAMESPACE_V1, "RegisterSubjectResponse");
        Element indicator = document.createElementNS(CONNECTOR_NAMESPACE_V1, "indicator");
        Attr typeAttr = document.createAttributeNS(XSL_NAMESPACE, "type");
        typeAttr.setNodeValue("BL");
        // indicator.setAttributeNS(XSL_NAMESPACE, "type", "II");
        indicator.setAttributeNode(typeAttr);
        indicator.setAttribute("value", "false");
        responseElement.appendChild(indicator);
        // append message here
        Element errormessage = document.createElementNS(CONNECTOR_NAMESPACE_V1, "message");
        //String confirmation = messages.getMessage("dataEndpoint.success", null, "Success", locale);
        Element code = document.createElementNS(CONNECTOR_NAMESPACE_V1, "code");
        code.setTextContent(exception.errorID);
        errormessage.appendChild(code);
        Element reason = document.createElementNS(CONNECTOR_NAMESPACE_V1, "reason");
        reason.setTextContent(exception.message);
        errormessage.appendChild(reason);
        responseElement.appendChild(errormessage);
        // add subject message here?
        return responseElement;
    }
    
    private Element mapErrorConfirmation(String message) throws Exception {
        DocumentBuilderFactory dbfac = DocumentBuilderFactory.newInstance();
        DocumentBuilder docBuilder = dbfac.newDocumentBuilder();
        Document document = docBuilder.newDocument();
        
        Element responseElement = document.createElementNS(CONNECTOR_NAMESPACE_V1, "RegisterSubjectResponse");
        Element indicator = document.createElementNS(CONNECTOR_NAMESPACE_V1, "indicator");
        Attr typeAttr = document.createAttributeNS(XSL_NAMESPACE, "type");
        typeAttr.setNodeValue("BL");
        // indicator.setAttributeNS(XSL_NAMESPACE, "type", "II");
        indicator.setAttributeNode(typeAttr);
        indicator.setAttribute("value", "false");
        responseElement.appendChild(indicator);
        // append message here
        Element errormessage = document.createElementNS(CONNECTOR_NAMESPACE_V1, "message");
        //String confirmation = messages.getMessage("dataEndpoint.success", null, "Success", locale);
        Element code = document.createElementNS(CONNECTOR_NAMESPACE_V1, "code");
        code.setTextContent("CCSystemFault");
        errormessage.appendChild(code);
        Element reason = document.createElementNS(CONNECTOR_NAMESPACE_V1, "reason");
        reason.setTextContent(message);
        errormessage.appendChild(reason);
        responseElement.appendChild(errormessage);
        // add subject message here?
        return responseElement;
    }

    
    private Element mapConfirmation(String studySubjectId) throws Exception {
        DocumentBuilderFactory dbfac = DocumentBuilderFactory.newInstance();
        DocumentBuilder docBuilder = dbfac.newDocumentBuilder();
        Document document = docBuilder.newDocument();
        
        Element responseElement = document.createElementNS(CONNECTOR_NAMESPACE_V1, "RegisterSubjectResponse");
//        Element resultElement = document.createElementNS(CONNECTOR_NAMESPACE_V1, "result");
//        String confirmation = messages.getMessage("dataEndpoint.success", null, "Success", locale);
//        resultElement.setTextContent(confirmation);
//        responseElement.appendChild(resultElement);
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
        
        patientIdentifier.setAttribute("identifierName", "Patient Position");
        patientIdentifier.setAttribute("displayable", "false");
        responseElement.appendChild(patientIdentifier);
        return responseElement;
    }
    
}
