/*
 * OpenClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).
 * For details see: http://www.openclinica.org/license
 *
 * Copyright 2003-2009 Akaza Research
 */
package org.akaza.openclinica.ws;

import org.akaza.openclinica.bean.login.UserAccountBean;
import org.akaza.openclinica.bean.managestudy.StudyBean;
import org.akaza.openclinica.bean.managestudy.StudySubjectBean;
import org.akaza.openclinica.bean.managestudy.SubjectTransferBean;
import org.akaza.openclinica.bean.submit.SubjectBean;
import org.akaza.openclinica.dao.login.UserAccountDAO;
import org.akaza.openclinica.dao.managestudy.StudyDAO;
import org.akaza.openclinica.dao.managestudy.StudySubjectDAO;
import org.akaza.openclinica.i18n.util.ResourceBundleProvider;
import org.akaza.openclinica.service.subject.SubjectServiceInterface;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.context.SecurityContextHolder;
import org.springframework.security.userdetails.UserDetails;
import org.springframework.util.xml.DomUtils;
import org.springframework.ws.server.endpoint.annotation.Endpoint;
import org.springframework.ws.server.endpoint.annotation.PayloadRoot;
import org.springframework.ws.server.endpoint.annotation.XPathParam;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import javax.sql.DataSource;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Source;
import javax.xml.transform.dom.DOMSource;

/**
 * @author Krikor Krumlian
 * 
 */
@Endpoint
public class SubjectEndpoint {

    protected final Logger logger = LoggerFactory.getLogger(getClass().getName());
    private final String NAMESPACE_URI_V1 = "http://openclinica.org/ws/subject/v1";
    private final String SUCCESS_MESSAGE = "success";
    private final String FAIL_MESSAGE = "fail";
    private String dateFormat;

    private final SubjectServiceInterface subjectService;
    private final DataSource dataSource;

    /**
     * Constructor
     * 
     * @param subjectService
     * @param cctsService
     */
    public SubjectEndpoint(SubjectServiceInterface subjectService, DataSource dataSource) {
        this.subjectService = subjectService;
        this.dataSource = dataSource;
    }

    /**
     * if NAMESPACE_URI_V1:commitRequest execute this method
     * 
     * @param gridId
     * @param subject
     * @param studyOid
     * @return
     * @throws Exception
     */
    @PayloadRoot(localPart = "createRequest", namespace = NAMESPACE_URI_V1)
    public Source createSubject(@XPathParam("//s:subject") NodeList subject, @XPathParam("//s:study/@uniqueIdentifier") String studyIdentifier)
            throws Exception {
        ResourceBundleProvider.updateLocale(new Locale("en_US"));
        Element subjectElement = (Element) subject.item(0);
        SubjectTransferBean subjectTransferBean = unMarshallToSubjectTransfer(subjectElement, studyIdentifier);
        // TODO: Add Logic
        if (subjectService.validate(subjectTransferBean)) {
            create(subjectTransferBean);
            return new DOMSource(mapConfirmation(SUCCESS_MESSAGE));
        } else {
            return new DOMSource(mapConfirmation(FAIL_MESSAGE));
        }
    }

    /**
     * UnMarshall SubjectTransferBean, aka create SubjectTransferBean from XML
     * 
     * @param gridId
     * @param subjectElement
     * @param studyOidValue
     * @return
     * @throws ParseException
     */
    private SubjectTransferBean unMarshallToSubjectTransfer(Element subjectElement, String studyOidValue) throws ParseException {

        Element studySubjectIdElement = DomUtils.getChildElementByTagName(subjectElement, "studySubjectId");
        Element enrollmentDateElement = DomUtils.getChildElementByTagName(subjectElement, "enrollmentDate");
        Element personIdElement = DomUtils.getChildElementByTagName(subjectElement, "personId");
        Element secondaryIdElement = DomUtils.getChildElementByTagName(subjectElement, "secondaryId");
        Element genderElement = DomUtils.getChildElementByTagName(subjectElement, "sex");
        Element dateOfBirthElement = DomUtils.getChildElementByTagName(subjectElement, "dateOfBirth");
        Element yearOfBirthElement = DomUtils.getChildElementByTagName(subjectElement, "yearOfBirth");

        String personIdValue = personIdElement == null ? null : DomUtils.getTextValue(personIdElement);
        String studySubjectIdValue = DomUtils.getTextValue(studySubjectIdElement);
        String genderValue = genderElement == null ? null : DomUtils.getTextValue(genderElement);
        String secondaryIdValue = secondaryIdElement == null ? null : DomUtils.getTextValue(secondaryIdElement);
        String enrollmentDateValue = DomUtils.getTextValue(enrollmentDateElement);
        String dateOfBirthValue = dateOfBirthElement == null ? null : DomUtils.getTextValue(dateOfBirthElement);
        String yearOfBirthValue = yearOfBirthElement == null ? null : DomUtils.getTextValue(yearOfBirthElement);

        SubjectTransferBean subjectTransferBean = new SubjectTransferBean();

        subjectTransferBean.setStudyOid(studyOidValue);
        subjectTransferBean.setPersonId(personIdValue);
        subjectTransferBean.setStudySubjectId(studySubjectIdValue);
        if (genderValue == null) {
            // Do nothing
        } else {
            subjectTransferBean.setGender(genderValue.toCharArray()[0]);
        }
        subjectTransferBean.setDateOfBirth(dateOfBirthValue == null ? null : getDate(dateOfBirthValue));
        subjectTransferBean.setSecondaryId(secondaryIdValue);
        subjectTransferBean.setYearOfBirth(yearOfBirthValue);
        subjectTransferBean.setEnrollmentDate(getDate(enrollmentDateValue));

        subjectTransferBean.setOwner(getUserAccount());

        return subjectTransferBean;
    }

    private void create(SubjectTransferBean subjectTransferBean) {
        boolean isSubjectInMain = doesSubjectExist(subjectTransferBean);

        if (isSubjectInMain) {
            // TODO : either return something or throw exception or don't do anything
            logger.debug("SubjectInMain");
        } else {
            logger.debug("creating subject transfer");
            createSubject(subjectTransferBean);
        }
    }

    private boolean doesSubjectExist(SubjectTransferBean subjectTransferBean) {
        // TODO: Implement this
        StudySubjectDAO ssdao = new StudySubjectDAO(dataSource);
        StudyDAO studyDao = new StudyDAO(dataSource);
        StudyBean studyBean = studyDao.findByUniqueIdentifier(subjectTransferBean.getStudyOid());
        StudySubjectBean ssbean = ssdao.findByLabelAndStudy(subjectTransferBean.getStudySubjectId(), studyBean);
        return ssbean.getId() > 0 ? true : false;
    }

    private boolean createSubject(SubjectTransferBean subjectTransfer) {
        StudyBean study = new StudyBean();
        study.setIdentifier(subjectTransfer.getStudyOid());
        SubjectBean subject = new SubjectBean();
        subject.setUniqueIdentifier(subjectTransfer.getPersonId());
        subject.setLabel(subjectTransfer.getStudySubjectId());
        subject.setDateOfBirth(subjectTransfer.getDateOfBirth());
        subject.setGender(subjectTransfer.getGender());
        if (subjectTransfer.getOwner() != null) {
            subject.setOwner(subjectTransfer.getOwner());
        }
        subject.setCreatedDate(new Date());
        return "yes".equals(this.subjectService.createSubject(subject, study, subjectTransfer.getEnrollmentDate(), null)) ? true : false;
    }

    /**
     * Create Response
     * 
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
     * 
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