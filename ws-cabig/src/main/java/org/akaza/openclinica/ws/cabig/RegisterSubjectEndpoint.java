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

package org.akaza.openclinica.ws.cabig;

import org.akaza.openclinica.bean.core.Role;
import org.akaza.openclinica.bean.core.Status;
import org.akaza.openclinica.bean.login.UserAccountBean;
import org.akaza.openclinica.bean.managestudy.StudySubjectBean;
import org.akaza.openclinica.bean.submit.SubjectBean;
import org.akaza.openclinica.dao.core.CoreResources;
import org.akaza.openclinica.exception.OpenClinicaException;
import org.akaza.openclinica.ws.bean.RegisterSubjectBean;
import org.akaza.openclinica.ws.cabig.abst.AbstractCabigDomEndpoint;
import org.akaza.openclinica.ws.cabig.exception.CCBusinessFaultException;
import org.akaza.openclinica.ws.cabig.exception.CCSystemFaultException;
import org.akaza.openclinica.ws.logic.RegisterSubjectService;
import org.springframework.context.MessageSource;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.sql.DataSource;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

public class RegisterSubjectEndpoint extends AbstractCabigDomEndpoint {

    // protected final Logger logger = LoggerFactory.getLogger(getClass().getName());
    // private final String NAMESPACE_URI_V1 = "http://openclinica.org/ws/cabig/v1";// TODO keep or toss?
    // private final String CONNECTOR_NAMESPACE_V1 = "http://clinicalconnector.nci.nih.gov";
    // private final String XSL_NAMESPACE = "http://www.w3.org/2001/XMLSchema-instance";
    // private final String ISO_21090_NAMESPACE = "uri:iso.org:21090";
    //
    // private final DataSource dataSource;
    // private final MessageSource messages;
    // private final CoreResources coreResources;// TODO keep or toss?
    // private final Locale locale;
    private final RegisterSubjectService subjectService;

    // SubjectDAO subjectDao;
    // StudyDAO studyDao;
    // StudySubjectDAO studySubjectDao;

    public RegisterSubjectEndpoint(DataSource dataSource, MessageSource messages, CoreResources coreResources) {

        super(dataSource, messages, coreResources);
        this.subjectService = new RegisterSubjectService();
    }

    protected Element invokeInternal(Element requestElement, Document document) throws Exception {
        System.out.println("Request text ");
        SubjectBean finalSubjectBean = new SubjectBean();
        finalSubjectBean.setUniqueIdentifier("");
        // the above line is for the return statement
        NodeList subjects = requestElement.getElementsByTagNameNS(CONNECTOR_NAMESPACE_V1, "studySubject");
        try {
            if (subjects.getLength() > 0) {
                System.out.println("found study subject: " + subjects.getLength());
                // logNodeList(subjects);
                for (int i = 0; i < subjects.getLength(); i++) {
                    // will subjects always be sent one at a time? nta
                    Node childNode = subjects.item(i);

                    // get user account bean from security here
                    UserAccountBean user = this.getUserAccount();
                    // is this user allowed to create subjects? if not, throw a ccsystem fault exception
                    Role r = user.getActiveStudyRole();
                    if (r != null
                        && (r.equals(Role.COORDINATOR) || r.equals(Role.STUDYDIRECTOR) || r.equals(Role.INVESTIGATOR) || r.equals(Role.RESEARCHASSISTANT) || r
                                .equals(Role.ADMIN))) {
                        // you may pass
                    } else {
                        throw new CCSystemFaultException("You do not possess the correct privileges to create a subject.");
                    }

                    RegisterSubjectBean subjectBean = subjectService.generateSubjectBean(user, childNode, getStudyDao(), getStudyParamValueDao());
                    // performedSubjectMilestone
                    NodeList performedMilestones = requestElement.getElementsByTagNameNS(CONNECTOR_NAMESPACE_V1, "performedSubjectMilestone");
                    if (performedMilestones.getLength() > 0) {
                        for (int j = 0; j < performedMilestones.getLength(); j++) {
                            Node milestone = performedMilestones.item(j);
                            subjectBean = subjectService.attachStudyIdentifiers(subjectBean, milestone);
                        }
                    }
                    // will there ever be more than one subject-study pair sent in a message? tba

                    // are there errors here? if so, throw a ccbusiness fault
                    finalSubjectBean = subjectService.generateSubjectBean(subjectBean);

                    SubjectBean testSubjectBean = getSubjectDao().findByUniqueIdentifier(subjectBean.getUniqueIdentifier());

                    boolean updateMe = false;
                    if (testSubjectBean.getId() > 0) {
                        // if its identical, restore and renew
                        if (subjectService.isSubjectIdentical(subjectBean, testSubjectBean)) {
                            testSubjectBean.setUpdater(user);
                            testSubjectBean.setDobCollected(true);// change?
                            testSubjectBean.setStatus(Status.AVAILABLE);
                            updateMe = true;
                            finalSubjectBean = testSubjectBean;
                            // reset the final subject bean with test subject bean
                            // set a boolean flag to update instead of creation
                        } else {
                            // otherwise, throw an error
                            throw new CCBusinessFaultException("You already have a subject in the database with the unique identifier of "
                                + subjectBean.getUniqueIdentifier() + ".  Please review your data and re-submit your request.");
                        }
                    }
                    StudySubjectBean studySubjectBean = subjectService.generateStudySubjectBean(subjectBean, finalSubjectBean, subjectBean.getStudyBean());

                    StudySubjectBean testStudySubjectBean = // getStudySubjectDao().findTheGreatestLabel()
                        getStudySubjectDao().findByLabelAndStudy(subjectBean.getStudySubjectLabel(), subjectBean.getStudyBean());
                    boolean updateStudySubject = false;
                    if (testStudySubjectBean.getId() > 0) {
                        // same check here, if its identical, restore and renew, otherwise throw the error
                        if (subjectService.isStudySubjectIdentical(subjectBean, finalSubjectBean, testStudySubjectBean, subjectBean.getStudyBean())) {
                            testStudySubjectBean.setUpdater(user);
                            testStudySubjectBean.setStatus(Status.AVAILABLE);
                            updateStudySubject = true;
                            studySubjectBean = testStudySubjectBean;
                        } else {
                            // also renew all the CRFs and Items?

                            throw new CCBusinessFaultException("You already have a study subject in the database with the SSID of "
                                + subjectBean.getStudySubjectLabel() + ".  Please change it and try your request again.");
                        }
                    }
                    // ///////////////////////////////////////////////////////////////////////////////////////////////
                    // below is point of no return - we have caught all the errors and are committing to the database
                    // ///////////////////////////////////////////////////////////////////////////////////////////////
                    if (updateMe) {
                        finalSubjectBean = (SubjectBean) getSubjectDao().update(finalSubjectBean);
                    } else {
                        finalSubjectBean = getSubjectDao().create(finalSubjectBean);
                    }

                    // needs to be generated with an id, so we set it here, for creation purposes
                    studySubjectBean.setSubjectId(finalSubjectBean.getId());

                    if (updateStudySubject) {
                        studySubjectBean = (StudySubjectBean) getStudySubjectDao().update(studySubjectBean);
                    } else {
                        studySubjectBean = getStudySubjectDao().create(studySubjectBean, false);
                    }
                    System.out.println("finished creation");
                }
            }
            // return success message here
            return mapRegisterSubjectConfirmation(finalSubjectBean.getUniqueIdentifier());
            // TODO is it actually primary key? nta
        } catch (Exception npe) {
            npe.printStackTrace();
            // TODO figure out exception and send response
            if (npe.getClass().getName().startsWith("org.akaza.openclinica.ws.cabig.exception")) {
                System.out.println("found " + npe.getClass().getName());
                OpenClinicaException ope = (OpenClinicaException) npe;
                return mapSubjectErrorConfirmation("", ope);
            } else {
                System.out.println(" did not find openclinica exception, found " + npe.getClass().getName());
                return mapSubjectErrorConfirmation(npe.getMessage());
            }
        }

    }

    // public SubjectDAO getSubjectDao() {
    // subjectDao = subjectDao != null ? subjectDao : new SubjectDAO(dataSource);
    // return subjectDao;
    // }
    //    
    // public StudyDAO getStudyDao() {
    // studyDao = studyDao != null ? studyDao : new StudyDAO(dataSource);
    // return studyDao;
    // }
    //    
    // public StudySubjectDAO getStudySubjectDao() {
    // studySubjectDao = studySubjectDao != null ? studySubjectDao : new StudySubjectDAO(dataSource);
    // return studySubjectDao;
    // }

    /**
     * Helper Method to get the user account TODO: place in a superclass, so that all endpoints dont need this code
     * 
     * @return UserAccountBean
     */
    // private UserAccountBean getUserAccount() {
    // Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    // String username = null;
    // if (principal instanceof UserDetails) {
    // username = ((UserDetails) principal).getUsername();
    // } else {
    // username = principal.toString();
    // }
    // UserAccountDAO userAccountDao = new UserAccountDAO(dataSource);
    // return (UserAccountBean) userAccountDao.findByUserName(username);
    // }
    /**
     * simple helper method to figure out structure of node list passed to it TODO: place in a superclass, so we don't clutter up the endpoint
     * 
     * @param nlist
     */
    // private void logNodeList(NodeList nlist) {
    // if (nlist.getLength() > 0) {
    // for (int i=0; i < nlist.getLength(); i++) {
    // try {
    // Node childNode = nlist.item(i);
    // System.out.println("node: " + childNode.getNodeName() +
    // " -> " + childNode.getNodeValue() +
    // " : " + childNode.getTextContent());
    // if (childNode.getChildNodes().getLength() > 0) {
    // System.out.println("found child nodes: " + childNode.getChildNodes().getLength());
    // logNodeList(childNode.getChildNodes());
    // }
    // if (childNode.hasAttributes()) {
    // System.out.print("found attributes " + childNode.getAttributes().getLength());
    // System.out.println(": " + childNode.getAttributes().item(0).getNodeName());
    // }
    // } catch (Exception ee) {
    // // trying to catch all NPEs here, tbh
    // System.out.println("found a nullpointer");
    // }
    // }
    // }
    // }
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
        // String confirmation = messages.getMessage("dataEndpoint.success", null, "Success", locale);
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
        // String confirmation = messages.getMessage("dataEndpoint.success", null, "Success", locale);
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
        // Element resultElement = document.createElementNS(CONNECTOR_NAMESPACE_V1, "result");
        // String confirmation = messages.getMessage("dataEndpoint.success", null, "Success", locale);
        // resultElement.setTextContent(confirmation);
        // responseElement.appendChild(resultElement);
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
