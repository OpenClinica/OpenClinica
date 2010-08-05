/*
 * OpenClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).
 * For details see: http://www.openclinica.org/license
 *
 * Copyright 2003-2009 Akaza Research
 */
package org.akaza.openclinica.ws;

import org.akaza.openclinica.bean.core.Status;
import org.akaza.openclinica.bean.login.StudyUserRoleBean;
import org.akaza.openclinica.bean.login.UserAccountBean;
import org.akaza.openclinica.bean.managestudy.StudyBean;
import org.akaza.openclinica.bean.managestudy.StudyEventBean;
import org.akaza.openclinica.bean.managestudy.StudySubjectBean;
import org.akaza.openclinica.bean.managestudy.SubjectTransferBean;
import org.akaza.openclinica.bean.submit.SubjectBean;
import org.akaza.openclinica.dao.admin.CRFDAO;
import org.akaza.openclinica.dao.login.UserAccountDAO;
import org.akaza.openclinica.dao.managestudy.EventDefinitionCRFDAO;
import org.akaza.openclinica.dao.managestudy.StudyDAO;
import org.akaza.openclinica.dao.managestudy.StudyEventDAO;
import org.akaza.openclinica.dao.managestudy.StudyEventDefinitionDAO;
import org.akaza.openclinica.dao.managestudy.StudySubjectDAO;
import org.akaza.openclinica.dao.submit.CRFVersionDAO;
import org.akaza.openclinica.dao.submit.SubjectDAO;
import org.akaza.openclinica.exception.OpenClinicaSystemException;
import org.akaza.openclinica.i18n.util.ResourceBundleProvider;
import org.akaza.openclinica.service.subject.SubjectServiceInterface;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.util.xml.DomUtils;
import org.springframework.ws.server.endpoint.annotation.Endpoint;
import org.springframework.ws.server.endpoint.annotation.PayloadRoot;
import org.springframework.ws.server.endpoint.annotation.XPathParam;
import org.akaza.openclinica.ws.generated.CreateCrfResponse;
import org.akaza.openclinica.ws.generated.CrfType;
import org.akaza.openclinica.ws.generated.EventType;
import org.akaza.openclinica.ws.generated.ListStudySubjectsInStudyType;
import org.akaza.openclinica.ws.generated.ListStudySubjectsInStudyResponse;
import org.akaza.openclinica.ws.generated.StudyRefType;
import org.akaza.openclinica.ws.generated.SiteRefType;
import org.akaza.openclinica.ws.generated.StudySubjectType;
import org.akaza.openclinica.ws.generated.StudySubjectsType;
import org.akaza.openclinica.ws.generated.EventsType;
import org.akaza.openclinica.ws.generated.StudySubjectWithEventsType;
import org.akaza.openclinica.ws.generated.GenderType;
import org.akaza.openclinica.ws.generated.SubjectType;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import java.io.FileOutputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.GregorianCalendar;
import java.util.Locale;

import javax.sql.DataSource;
import javax.xml.bind.JAXBElement;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Source;
import javax.xml.transform.dom.DOMSource;

/**
 * @author Krikor Krumlian
 * 
 */
@Endpoint
public class StudySubjectEndpoint {

    protected final Logger logger = LoggerFactory.getLogger(getClass().getName());
    private final String NAMESPACE_URI_V1 = "http://openclinica.org/ws/studySubject/v1";
    private final String SUCCESS_MESSAGE = "success";
    private final String FAIL_MESSAGE = "fail";
    private String dateFormat;

    private final SubjectServiceInterface subjectService;
    StudyDAO studyDao;
    UserAccountDAO userAccountDao;
    SubjectDAO subjectDao;

    private final DataSource dataSource;

    /**
     * Constructor
     * 
     * @param subjectService
     * @param cctsService
     */
    public StudySubjectEndpoint(SubjectServiceInterface subjectService, DataSource dataSource) {
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
    public Source createSubject(@XPathParam("//studySubject:studySubject") NodeList subject) throws Exception {
        ResourceBundleProvider.updateLocale(new Locale("en_US"));
        Element subjectElement = (Element) subject.item(0);
        SubjectTransferBean subjectTransferBean = unMarshallToSubjectTransfer(subjectElement);
        // TODO: Add Logic
        if (subjectService.validate(subjectTransferBean)) {
            String label = create(subjectTransferBean);
            return new DOMSource(mapConfirmation(SUCCESS_MESSAGE, label));
        } else {
            return new DOMSource(mapConfirmation(FAIL_MESSAGE, null));
        }
    }
    
    

    @PayloadRoot(localPart = "listStudySubjectsInStudyRequest", namespace = NAMESPACE_URI_V1)
    public ListStudySubjectsInStudyResponse listStudySubjectsInStudy(JAXBElement<ListStudySubjectsInStudyType> requestElement) throws Exception {
        ResourceBundleProvider.updateLocale(new Locale("en_US"));
        ListStudySubjectsInStudyType listStudySubjectsInStudyType = requestElement.getValue();
        StudyBean study = null;
        try {
            study = validateRequestAndReturnStudy(listStudySubjectsInStudyType.getStudyRef());
        } catch (Exception e) {
            ListStudySubjectsInStudyResponse response = new ListStudySubjectsInStudyResponse();
            response.setResult(FAIL_MESSAGE);
            response.getError().add(e.getMessage());
            return response;
        }
        //return new DOMSource(mapConfirmation(study, SUCCESS_MESSAGE),listStudySubjectsInStudyType.getStudyRef());
        return mapConfirmation(study, SUCCESS_MESSAGE, listStudySubjectsInStudyType.getStudyRef());
    }
    
    private ListStudySubjectsInStudyResponse mapConfirmation(StudyBean study, String message, StudyRefType studyRef) throws Exception{
        ListStudySubjectsInStudyResponse response  = new ListStudySubjectsInStudyResponse();
        response.setResult(message);
        StudySubjectsType studySubjectsType = new StudySubjectsType();
        response.setStudySubjects(studySubjectsType);
        List<StudySubjectBean> studySubjects = this.subjectService.getStudySubject(study);
        for (StudySubjectBean studySubjectBean : studySubjects) {
            StudySubjectWithEventsType studySubjectType = new StudySubjectWithEventsType();
            SubjectType subjectType = new SubjectType();
            studySubjectType.setLabel(studySubjectBean.getLabel());
            studySubjectType.setSecondaryLabel(studySubjectBean.getSecondaryLabel());
            GregorianCalendar gc = new GregorianCalendar();
            gc.setTime(studySubjectBean.getEnrollmentDate());
            DatatypeFactory df = DatatypeFactory.newInstance();
            df.newXMLGregorianCalendar(gc);
            studySubjectType.setEnrollmentDate(df.newXMLGregorianCalendar(gc));
            SubjectBean subjectBean = (SubjectBean)getSubjectDao().findByPK(studySubjectBean.getSubjectId());
            subjectType.setUniqueIdentifier(subjectBean.getUniqueIdentifier());
            subjectType.setGender(GenderType.fromValue(String.valueOf(subjectBean.getGender())));
            studySubjectType.setSubject(subjectType);
            //studySubjectType.setStudyRef(studyRef);
            studySubjectType.setEvents(getEvents(studySubjectBean));
            studySubjectsType.getStudySubject().add(studySubjectType);
            

        }
        return response;
    }
    
    private EventsType getEvents(StudySubjectBean studySubject) throws Exception{
        StudyEventDAO eventDao = new StudyEventDAO(dataSource);
        StudyEventDefinitionDAO studyEventDefinitionDao = new StudyEventDefinitionDAO(dataSource);
        EventsType eventsType = new EventsType();
        List<StudyEventBean> events = eventDao.findAllByStudySubject(studySubject);
        for (StudyEventBean studyEventBean : events) {
            EventType eventType = new EventType();
            eventType.setEventDefinitionOID(studyEventDefinitionDao.findByEventDefinitionCRFId(studyEventBean.getStudyEventDefinitionId()).getOid());
            eventType.setLocation(studyEventBean.getLocation());
            GregorianCalendar gc = new GregorianCalendar();
            gc.setTime(studyEventBean.getDateStarted());
            DatatypeFactory df = DatatypeFactory.newInstance();
            df.newXMLGregorianCalendar(gc);
            eventType.setStartDate(df.newXMLGregorianCalendar(gc));
            eventsType.getEvent().add(eventType);
        }
        return eventsType;
    }
    
    
    private StudyBean validateRequestAndReturnStudy(StudyRefType studyRef) {

        String studyIdentifier = studyRef == null ? null : studyRef.getIdentifier();
        String siteIdentifier = studyRef.getSiteRef() == null ? null : studyRef.getSiteRef().getIdentifier();

        if (studyIdentifier == null && siteIdentifier == null) {
            throw new OpenClinicaSystemException("Provide a valid study");
        }
        if (studyIdentifier != null && siteIdentifier == null) {
            StudyBean study = (StudyBean) getStudyDao().findByName(studyIdentifier);
            if (study.getId() == 0) {
                throw new OpenClinicaSystemException("Invalid study");
            }
        }
        if (studyIdentifier != null && siteIdentifier == null) {
            StudyBean study = (StudyBean) getStudyDao().findByName(studyIdentifier);
            if (study.getId() == 0) {
                throw new OpenClinicaSystemException("Invalid study");
            }
            StudyUserRoleBean studySur = getUserAccountDao().findRoleByUserNameAndStudyId(getUserAccount().getName(), study.getId());
            if (studySur.getStatus() != Status.AVAILABLE) {
                throw new OpenClinicaSystemException("Invalid roles ");
            }
            return study;
        }
        if (studyIdentifier != null && siteIdentifier != null) {
            StudyBean study = (StudyBean) getStudyDao().findByName(studyIdentifier);
            StudyBean site = (StudyBean) getStudyDao().findByName(siteIdentifier);
            if (study.getId() == 0 || site.getId() == 0 || site.getParentStudyId() != study.getId()) {
                throw new OpenClinicaSystemException("Invalid study or site");
            }
            StudyUserRoleBean siteSur = getUserAccountDao().findRoleByUserNameAndStudyId(getUserAccount().getName(), site.getId());
            if (siteSur.getStatus() != Status.AVAILABLE) {
                throw new OpenClinicaSystemException("Invalid roles ");
            }
            return site;
        }

        return null;

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
    private SubjectTransferBean unMarshallToSubjectTransfer(Element subjectElement) throws ParseException {

        Element studySubjectIdElement = DomUtils.getChildElementByTagName(subjectElement, "label");
        Element secondaryIdElement = DomUtils.getChildElementByTagName(subjectElement, "secondaryLabel");
        Element enrollmentDateElement = DomUtils.getChildElementByTagName(subjectElement, "enrollmentDate");
        Element subject = DomUtils.getChildElementByTagName(subjectElement, "subject");
        Element personIdElement = DomUtils.getChildElementByTagName(subject, "uniqueIdentifier");
        Element genderElement = DomUtils.getChildElementByTagName(subject, "gender");
        Element dateOfBirthElement = DomUtils.getChildElementByTagName(subject, "dateOfBirth");
        Element yearOfBirthElement = DomUtils.getChildElementByTagName(subject, "yearOfBirth");
        Element study = DomUtils.getChildElementByTagName(subjectElement, "studyRef");
        Element studyIdentifierElement = DomUtils.getChildElementByTagName(study, "identifier");
        Element site = DomUtils.getChildElementByTagName(study, "siteRef");
        Element siteIdentifierElement = site == null ? null : DomUtils.getChildElementByTagName(site, "identifier");

        String personIdValue = personIdElement == null ? null : DomUtils.getTextValue(personIdElement);
        String studySubjectIdValue = DomUtils.getTextValue(studySubjectIdElement);
        String genderValue = genderElement == null ? null : DomUtils.getTextValue(genderElement);
        String secondaryIdValue = secondaryIdElement == null ? null : DomUtils.getTextValue(secondaryIdElement);
        String enrollmentDateValue = DomUtils.getTextValue(enrollmentDateElement);
        String dateOfBirthValue = dateOfBirthElement == null ? null : DomUtils.getTextValue(dateOfBirthElement);
        String yearOfBirthValue = yearOfBirthElement == null ? null : DomUtils.getTextValue(yearOfBirthElement);
        String studyIdentifier = studyIdentifierElement == null ? null : DomUtils.getTextValue(studyIdentifierElement);
        String siteIdentifier = siteIdentifierElement == null ? null : DomUtils.getTextValue(siteIdentifierElement);

        SubjectTransferBean subjectTransferBean = new SubjectTransferBean();

        subjectTransferBean.setStudyOid(studyIdentifier);
        subjectTransferBean.setSiteIdentifier(siteIdentifier);
        subjectTransferBean.setPersonId(personIdValue);
        subjectTransferBean.setStudySubjectId(studySubjectIdValue);
        if (genderValue == null) {
            // Do nothing
        } else {
            subjectTransferBean.setGender(genderValue.toCharArray()[0]);
        }
        subjectTransferBean.setDateOfBirth(dateOfBirthValue == null ? null : getDate(dateOfBirthValue));
        subjectTransferBean.setSecondaryId(secondaryIdValue == null ? "" : secondaryIdValue);
        subjectTransferBean.setYearOfBirth(yearOfBirthValue);
        subjectTransferBean.setEnrollmentDate(getDate(enrollmentDateValue));

        //subjectTransferBean.setOwner(getUserAccount());

        return subjectTransferBean;
    }

    private String create(SubjectTransferBean subjectTransferBean) {
        boolean isSubjectInMain = doesSubjectExist(subjectTransferBean);

        if (isSubjectInMain) {
            // TODO : either return something or throw exception or don't do anything
            logger.debug("SubjectInMain");
            throw new OpenClinicaSystemException("Duplicate label");
        } else {
            logger.debug("creating subject transfer");
            return createSubject(subjectTransferBean);
        }
    }

    private boolean doesSubjectExist(SubjectTransferBean subjectTransferBean) {
        // TODO: Implement this
        StudySubjectDAO ssdao = new StudySubjectDAO(dataSource);
        StudyDAO studyDao = new StudyDAO(dataSource);
        StudyBean studyBean = studyDao.findByUniqueIdentifier(subjectTransferBean.getStudy().getIdentifier());
        StudySubjectBean ssbean = ssdao.findByLabelAndStudy(subjectTransferBean.getStudySubjectId(), studyBean);
        return ssbean.getId() > 0 ? true : false;
    }

    private String createSubject(SubjectTransferBean subjectTransfer) {
        SubjectBean subject = new SubjectBean();
        subject.setUniqueIdentifier(subjectTransfer.getPersonId());
        subject.setLabel(subjectTransfer.getStudySubjectId());
        subject.setDateOfBirth(subjectTransfer.getDateOfBirth());
        subject.setGender(subjectTransfer.getGender());
        if (subjectTransfer.getOwner() != null) {
            subject.setOwner(subjectTransfer.getOwner());
        }
        subject.setCreatedDate(new Date());
        return this.subjectService.createSubject(subject, subjectTransfer.getStudy(), subjectTransfer.getEnrollmentDate(), subjectTransfer.getSecondaryId());
    }

    /**
     * Create Response
     * 
     * @param confirmation
     * @return
     * @throws Exception
     */
    private Element mapConfirmation(String confirmation, String theLabel) throws Exception {
        DocumentBuilderFactory dbfac = DocumentBuilderFactory.newInstance();
        DocumentBuilder docBuilder = dbfac.newDocumentBuilder();
        Document document = docBuilder.newDocument();

        Element responseElement = document.createElementNS(NAMESPACE_URI_V1, "createResponse");
        Element resultElement = document.createElementNS(NAMESPACE_URI_V1, "result");
        Element label = document.createElementNS(NAMESPACE_URI_V1, "label");
        resultElement.setTextContent(confirmation);
        label.setTextContent(theLabel);
        responseElement.appendChild(resultElement);
        responseElement.appendChild(label);
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
    
    public StudyDAO getStudyDao() {
        studyDao = studyDao != null ? studyDao : new StudyDAO(dataSource);
        return studyDao;
    }

    public UserAccountDAO getUserAccountDao() {
        userAccountDao = userAccountDao != null ? userAccountDao : new UserAccountDAO(dataSource);
        return userAccountDao;
    }
    
    public SubjectDAO getSubjectDao() {
        subjectDao = subjectDao != null ? subjectDao : new SubjectDAO(dataSource);
        return subjectDao;
    }

}