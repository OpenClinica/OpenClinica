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
import org.akaza.openclinica.dao.login.UserAccountDAO;
import org.akaza.openclinica.dao.managestudy.StudyDAO;
import org.akaza.openclinica.dao.managestudy.StudyEventDAO;
import org.akaza.openclinica.dao.managestudy.StudyEventDefinitionDAO;
import org.akaza.openclinica.dao.managestudy.StudySubjectDAO;
import org.akaza.openclinica.dao.submit.SubjectDAO;
import org.akaza.openclinica.exception.OpenClinicaSystemException;
import org.akaza.openclinica.i18n.util.ResourceBundleProvider;
import org.akaza.openclinica.service.subject.SubjectServiceInterface;
import org.akaza.openclinica.validator.SubjectTransferValidator;
import org.openclinica.ws.beans.EventType;
import org.openclinica.ws.beans.EventsType;
import org.openclinica.ws.beans.GenderType;
import org.openclinica.ws.beans.ListStudySubjectsInStudyType;
import org.openclinica.ws.beans.StudyRefType;
import org.openclinica.ws.beans.StudySubjectWithEventsType;
import org.openclinica.ws.beans.StudySubjectsType;
import org.openclinica.ws.beans.SubjectType;
import org.openclinica.ws.studysubject.v1.ListAllByStudyResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.MessageSource;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.util.xml.DomUtils;
import org.springframework.validation.DataBinder;
import org.springframework.validation.Errors;
import org.springframework.validation.ObjectError;
import org.springframework.ws.server.endpoint.annotation.Endpoint;
import org.springframework.ws.server.endpoint.annotation.PayloadRoot;
import org.springframework.ws.server.endpoint.annotation.XPathParam;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;

import javax.sql.DataSource;
import javax.xml.bind.JAXBElement;
import javax.xml.datatype.DatatypeConstants;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
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
    private String dateFormat;

    private final SubjectServiceInterface subjectService;
    private final DataSource dataSource;
    private final MessageSource messages;
    StudyDAO studyDao;
    UserAccountDAO userAccountDao;
    SubjectDAO subjectDao;
    private final Locale locale;

    /**
     * Constructor
     * 
     * @param subjectService
     * @param dataSource
     */
    public StudySubjectEndpoint(SubjectServiceInterface subjectService, DataSource dataSource, MessageSource messages) {
        this.subjectService = subjectService;
        this.dataSource = dataSource;
        this.messages = messages;
        this.locale = new Locale("en_US");
    }

    /**
     * Use this method to create new study subjects in OpenClinica.
     * 
     * @param subject
     * @return Source
     * @throws Exception
     */
    @PayloadRoot(localPart = "createRequest", namespace = NAMESPACE_URI_V1)
    public Source createStudySubject(@XPathParam("//studySubject:studySubject") NodeList subject) throws Exception {
        ResourceBundleProvider.updateLocale(locale);
        Element subjectElement = (Element) subject.item(0);
        SubjectTransferBean subjectTransferBean = unMarshallToSubjectTransfer(subjectElement);

        DataBinder dataBinder = new DataBinder((subjectTransferBean));
        Errors errors = dataBinder.getBindingResult();
        SubjectTransferValidator subjectTransferValidator = new SubjectTransferValidator(dataSource);
        subjectTransferValidator.validate((subjectTransferBean), errors);
        if (!errors.hasErrors()) {
            String label = create(subjectTransferBean);
            return new DOMSource(mapConfirmation(messages.getMessage("studySubjectEndpoint.success", null, "Success", locale), label, errors));
        } else {
            return new DOMSource(mapConfirmation(messages.getMessage("studySubjectEndpoint.fail", null, "Fail", locale), null, errors));
        }
    }

    /**
     * Use this method to list all study subjects. Scheduled event data will also be show if available.
     * 
     * @param requestElement
     * @return ListAllByStudyResponse
     * @throws Exception
     */
    @PayloadRoot(localPart = "listAllByStudyRequest", namespace = NAMESPACE_URI_V1)
    public ListAllByStudyResponse listStudySubjectsInStudy(JAXBElement<ListStudySubjectsInStudyType> requestElement) throws Exception {
        ResourceBundleProvider.updateLocale(new Locale("en_US"));
        ListStudySubjectsInStudyType listStudySubjectsInStudyType = requestElement.getValue();
        StudyBean study = null;
        try {
            study = validateRequestAndReturnStudy(listStudySubjectsInStudyType.getStudyRef());
        } catch (OpenClinicaSystemException e) {
            ListAllByStudyResponse response = new ListAllByStudyResponse();
            response.setResult(messages.getMessage("studySubjectEndpoint.fail", null, "Fail", locale));
            response.getError().add(messages.getMessage(e.getErrorCode(), null, e.getErrorCode(), locale));
            return response;
        }
        return mapListStudySubjectsInStudyResponse(study, messages.getMessage("studySubjectEndpoint.success", null, "Success", locale),
                listStudySubjectsInStudyType.getStudyRef());
    }

    /**
     * Build the response for listStudySubjectsInStudy method
     * 
     * @param study
     * @param message
     * @param studyRef
     * @return ListAllByStudyResponse
     * @throws Exception
     */
    private ListAllByStudyResponse mapListStudySubjectsInStudyResponse(StudyBean study, String message, StudyRefType studyRef) throws Exception {
        ListAllByStudyResponse response = new ListAllByStudyResponse();
        response.setResult(message);
        StudySubjectsType studySubjectsType = new StudySubjectsType();
        response.setStudySubjects(studySubjectsType);
        List<StudySubjectBean> studySubjects = this.subjectService.getStudySubject(study);
        for (StudySubjectBean studySubjectBean : studySubjects) {
            StudySubjectWithEventsType studySubjectType = new StudySubjectWithEventsType();
            SubjectType subjectType = new SubjectType();
            studySubjectType.setLabel(studySubjectBean.getLabel());
            studySubjectType.setSecondaryLabel(studySubjectBean.getSecondaryLabel());
            studySubjectType.setEnrollmentDate(getXMLGregorianCalendarDate(studySubjectBean.getEnrollmentDate()));
            SubjectBean subjectBean = (SubjectBean) getSubjectDao().findByPK(studySubjectBean.getSubjectId());
            subjectType.setUniqueIdentifier(subjectBean.getUniqueIdentifier());
            subjectType.setGender(GenderType.fromValue(String.valueOf(subjectBean.getGender())));
            subjectType.setDateOfBirth(getXMLGregorianCalendarDate(subjectBean.getDateOfBirth()));
            studySubjectType.setSubject(subjectType);
            // studySubjectType.setStudyRef(studyRef);
            studySubjectType.setEvents(getEvents(studySubjectBean));
            studySubjectsType.getStudySubject().add(studySubjectType);

        }
        return response;
    }

    /**
     * Build Events sub section for ListStudySubjectsInStudyResponse
     * 
     * @param studySubject
     * @return EventsType
     * @throws Exception
     */
    private EventsType getEvents(StudySubjectBean studySubject) throws Exception {
        StudyEventDAO eventDao = new StudyEventDAO(dataSource);
        StudyEventDefinitionDAO studyEventDefinitionDao = new StudyEventDefinitionDAO(dataSource);
        EventsType eventsType = new EventsType();
        List<StudyEventBean> events = eventDao.findAllByStudySubject(studySubject);
        for (StudyEventBean studyEventBean : events) {
            EventType eventType = new EventType();
            eventType.setEventDefinitionOID(studyEventDefinitionDao.findByEventDefinitionCRFId(studyEventBean.getStudyEventDefinitionId()).getOid());
            eventType.setLocation(studyEventBean.getLocation());
            eventType.setStartDate(getXMLGregorianCalendarDate(studyEventBean.getDateStarted()));
            eventType.setStartTime(getXMLGregorianCalendarTime(studyEventBean.getDateStarted()));
            eventsType.getEvent().add(eventType);
        }
        return eventsType;
    }

    /**
     * Validate the listStudySubjectsInStudy request.
     * 
     * @param studyRef
     * @return StudyBean
     */
    private StudyBean validateRequestAndReturnStudy(StudyRefType studyRef) {

        String studyIdentifier = studyRef == null ? null : studyRef.getIdentifier();
        String siteIdentifier = studyRef.getSiteRef() == null ? null : studyRef.getSiteRef().getIdentifier();

        if (studyIdentifier == null && siteIdentifier == null) {
            throw new OpenClinicaSystemException("studySubjectEndpoint.provide_valid_study_site", "Provide a valid study/site.");
        }
        if (studyIdentifier != null && siteIdentifier == null) {
            StudyBean study = getStudyDao().findByUniqueIdentifier(studyIdentifier);
            if (study == null) {
                throw new OpenClinicaSystemException("studySubjectEndpoint.invalid_study_identifier", "The study identifier you provided is not valid.");
            }
            StudyUserRoleBean studySur = getUserAccountDao().findRoleByUserNameAndStudyId(getUserAccount().getName(), study.getId());
            if (studySur.getStatus() != Status.AVAILABLE) {
                throw new OpenClinicaSystemException("studySubjectEndpoint.insufficient_permissions",
                        "You do not have sufficient privileges to proceed with this operation.");
            }
            return study;
        }
        if (studyIdentifier != null && siteIdentifier != null) {
            StudyBean study = getStudyDao().findByUniqueIdentifier(studyIdentifier);
            StudyBean site = getStudyDao().findByUniqueIdentifier(siteIdentifier);
            if (study == null || site == null || site.getParentStudyId() != study.getId()) {
                throw new OpenClinicaSystemException("studySubjectEndpoint.invalid_study_site_identifier",
                        "The study/site identifier you provided is not valid.");
            }
            StudyUserRoleBean siteSur = getUserAccountDao().findRoleByUserNameAndStudyId(getUserAccount().getName(), site.getId());
            if (siteSur.getStatus() != Status.AVAILABLE) {
                throw new OpenClinicaSystemException("studySubjectEndpoint.insufficient_permissions",
                        "You do not have sufficient privileges to proceed with this operation.");
            }
            return site;
        }
        return null;
    }

    /**
     * Process createStudySubject request by creating SubjectTransferBean from received payload.
     * 
     * @param subjectElement
     * @return SubjectTransferBean
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

        // subjectTransferBean.setOwner(getUserAccount());

        return subjectTransferBean;
    }

    /**
     * Create the Subject object if it is not already in the system.
     * 
     * @param subjectTransferBean
     * @return String
     */
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

    /**
     * @param subjectTransferBean
     * @return
     */
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
     * Create createStudySubject request Build the response for createStudySubject method
     * 
     * @param confirmation
     *            operation result
     * @param studySubjectLabel
     *            the studySubject label
     * @return Element
     * @throws Exception
     * @see #createStudySubject
     */
    private Element mapConfirmation(String confirmation, String studySubjectLabel, Errors errors) throws Exception {
        DocumentBuilderFactory dbfac = DocumentBuilderFactory.newInstance();
        DocumentBuilder docBuilder = dbfac.newDocumentBuilder();
        Document document = docBuilder.newDocument();

        Element responseElement = document.createElementNS(NAMESPACE_URI_V1, "createResponse");
        Element resultElement = document.createElementNS(NAMESPACE_URI_V1, "result");
        resultElement.setTextContent(confirmation);
        Element labelElement = document.createElementNS(NAMESPACE_URI_V1, "label");
        labelElement.setTextContent(studySubjectLabel);
        responseElement.appendChild(resultElement);
        responseElement.appendChild(labelElement);

        for (ObjectError error : errors.getAllErrors()) {
            Element errorElement = document.createElementNS(NAMESPACE_URI_V1, "error");
            String theMessage = messages.getMessage(error.getCode(), error.getArguments(), locale);
            errorElement.setTextContent(theMessage);
            responseElement.appendChild(errorElement);
        }
        return responseElement;

    }

    /**
     * Helper method that translates a date object to an XMLGregorianCalendar which is the data type used in jaxb generated classes.
     * 
     * @param date
     * @return XMLGregorianCalendar
     * @throws Exception
     */
    private XMLGregorianCalendar getXMLGregorianCalendarDate(Date date) throws Exception {
        GregorianCalendar gc = new GregorianCalendar();
        gc.setTime(date);
        DatatypeFactory df = DatatypeFactory.newInstance();
        XMLGregorianCalendar gcDate =
            df.newXMLGregorianCalendarDate(gc.get(Calendar.YEAR), gc.get(Calendar.MONTH) + 1, gc.get(Calendar.DAY_OF_MONTH), DatatypeConstants.FIELD_UNDEFINED);
        return gcDate;
    }

    /**
     * Helper method that translates a date object (specifically time) to an XMLGregorianCalendar which is the data type used in jaxb generated classes.
     * 
     * @param date
     * @return XMLGregorianCalendar
     * @throws Exception
     */
    private XMLGregorianCalendar getXMLGregorianCalendarTime(Date date) throws Exception {
        GregorianCalendar gc = new GregorianCalendar();
        gc.setTime(date);
        DatatypeFactory df = DatatypeFactory.newInstance();
        XMLGregorianCalendar gcTime =
            df.newXMLGregorianCalendarTime(gc.get(Calendar.HOUR_OF_DAY), gc.get(Calendar.MINUTE), gc.get(Calendar.SECOND), null,
                    DatatypeConstants.FIELD_UNDEFINED);
        return gcTime;
    }

    /**
     * Helper Method to resolve a date provided as a string to a Date object.
     * 
     * @param dateAsString
     * @return Date
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

    public String getDateFormat() {
        return dateFormat;
    }

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

    public MessageSource getMessages() {
        return messages;
    }

}