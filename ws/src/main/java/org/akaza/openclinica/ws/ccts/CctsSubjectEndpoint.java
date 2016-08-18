/* 
 * OpenClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).
 * For details see: http://www.openclinica.org/license
 *
 * Copyright 2003-2009 Akaza Research 
 */
package org.akaza.openclinica.ws.ccts;

import org.akaza.openclinica.bean.login.UserAccountBean;
import org.akaza.openclinica.bean.managestudy.SubjectTransferBean;
import org.akaza.openclinica.service.subject.SubjectServiceInterface;
import org.akaza.openclinica.ws.logic.CctsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.util.xml.DomUtils;
import org.springframework.ws.server.endpoint.annotation.Endpoint;
import org.springframework.ws.server.endpoint.annotation.PayloadRoot;
import org.springframework.ws.server.endpoint.annotation.XPathParam;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Source;
import javax.xml.transform.dom.DOMSource;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author Krikor Krumlian
 *
 */
@Endpoint
public class CctsSubjectEndpoint {

    protected final Logger logger = LoggerFactory.getLogger(getClass().getName());
    private final String NAMESPACE_URI_V1 = "http://openclinica.org/ws/ccts/subject/v1";
    private final String SUCCESS_MESSAGE = "success";
    private String dateFormat;

    private final SubjectServiceInterface subjectService;
    private final CctsService cctsService;

    /**
     * Constructor
     * @param subjectService
     * @param cctsService
     */
    public CctsSubjectEndpoint(SubjectServiceInterface subjectService, CctsService cctsService) {
        this.subjectService = subjectService;
        this.cctsService = cctsService;
    }

    /**
     * if NAMESPACE_URI_V1:commitRequest execute this method
     * @param gridId
     * @param subject
     * @param studyOid
     * @return
     * @throws Exception
     */
    @PayloadRoot(localPart = "commitRequest", namespace = NAMESPACE_URI_V1)
    public Source createSubject(@XPathParam("//s:gridId") String gridId, @XPathParam("//s:subject") NodeList subject,
            @XPathParam("//s:study/@oid") String studyOid) throws Exception {
        Element subjectElement = (Element) (subject.item(0));
        SubjectTransferBean subjectTranferBean = unMarshallToSubjectTransfer(gridId, subjectElement, studyOid);
        // TODO: Add Logic
        logger.debug("In CreateSubject");
        return new DOMSource(mapConfirmation(SUCCESS_MESSAGE));
    }

    /**
     * if NAMESPACE_URI_V1:commitRequest execute this method
     * @param gridId
     * @param subject
     * @param studyOid
     * @return
     * @throws Exception
     */
    @PayloadRoot(localPart = "rollbackRequest", namespace = NAMESPACE_URI_V1)
    public Source rollBackSubject(@XPathParam("//s:gridId") String gridId, @XPathParam("//s:subject") NodeList subject,
            @XPathParam("//s:study/@oid") String studyOid) throws Exception {
        Element subjectElement = (Element) (subject.item(0));
        SubjectTransferBean subjectTranferBean = unMarshallToSubjectTransfer(gridId, subjectElement, studyOid);
        // TODO: Add Logic 
        return new DOMSource(mapConfirmation(SUCCESS_MESSAGE));
    }

    /**
     * UnMarshall SubjectTransferBean, aka create SubjectTransferBean from XML
     * @param gridId
     * @param subjectElement
     * @param studyOidValue
     * @return
     * @throws ParseException
     */
    private SubjectTransferBean unMarshallToSubjectTransfer(String gridId, Element subjectElement, String studyOidValue) throws ParseException {

        Element personIdElement = DomUtils.getChildElementByTagName(subjectElement, "personId");
        Element studySubjectIdElement = DomUtils.getChildElementByTagName(subjectElement, "studySubjectId");
        Element secondaryIdElement = DomUtils.getChildElementByTagName(subjectElement, "secondaryId");
        Element enrollmentDateElement = DomUtils.getChildElementByTagName(subjectElement, "enrollmentDate");
        Element genderElement = DomUtils.getChildElementByTagName(subjectElement, "gender");
        Element dateOfBirthElement = DomUtils.getChildElementByTagName(subjectElement, "dateOfBirth");

        String personIdValue = DomUtils.getTextValue(personIdElement);
        String studySubjectIdValue = DomUtils.getTextValue(studySubjectIdElement);
        String genderValue = DomUtils.getTextValue(genderElement);
        String secondaryIdValue = DomUtils.getTextValue(secondaryIdElement);
        String enrollmentDateValue = DomUtils.getTextValue(enrollmentDateElement);
        String dateOfBirthValue = DomUtils.getTextValue(dateOfBirthElement);

        SubjectTransferBean subjectTransferBean = new SubjectTransferBean();

        subjectTransferBean.setStudyOid(studyOidValue);
        subjectTransferBean.setPersonId(personIdValue);
        subjectTransferBean.setStudySubjectId(studySubjectIdValue);
        subjectTransferBean.setGender(genderValue.toCharArray()[0]);
        subjectTransferBean.setDateOfBirth(getDate(dateOfBirthValue));
        //subjectTransferBean.setSecondaryId(secondaryIdValue);
        subjectTransferBean.setEnrollmentDate(getDate(enrollmentDateValue));
        return subjectTransferBean;

    }

    /**
     * Create Response 
     * @param confirmation
     * @return
     * @throws Exception
     */
    private Element mapConfirmation(String confirmation) throws Exception {
        DocumentBuilderFactory dbfac = DocumentBuilderFactory.newInstance();
        DocumentBuilder docBuilder = dbfac.newDocumentBuilder();
        Document document = docBuilder.newDocument();

        Element responseElement = document.createElementNS(NAMESPACE_URI_V1, "commitResponse");
        Element resultElement = document.createElementNS(NAMESPACE_URI_V1, "result");
        resultElement.setTextContent(confirmation);
        responseElement.appendChild(resultElement);
        return responseElement;

    }

    /**
     * Helper Method to resolve dates
     * @param dateAsString
     * @return
     * @throws ParseException
     */
    private Date getDate(String dateAsString) throws ParseException {
        SimpleDateFormat sdf = new SimpleDateFormat(getDateFormat());
        return sdf.parse(dateAsString);
    }

    /**
     * Helper Method to get the user account
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
        // TODO: Call UserAccountDao.findByUserName()
        return null;
    }

    /**
     * @return
     */
    public String getDateFormat() {
        return dateFormat;
    }

    /**
     * @param dateFormat
     */
    public void setDateFormat(String dateFormat) {
        this.dateFormat = dateFormat;
    }

}