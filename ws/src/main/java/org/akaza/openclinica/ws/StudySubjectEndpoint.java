/*
 * OpenClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).
 * For details see: http://www.openclinica.org/license
 *
 * Copyright 2003-2009 Akaza Research
 */
package org.akaza.openclinica.ws;

import org.akaza.openclinica.bean.admin.CRFBean;
import org.akaza.openclinica.bean.core.Status;
import org.akaza.openclinica.bean.core.SubjectEventStatus;
import org.akaza.openclinica.bean.login.StudyUserRoleBean;
import org.akaza.openclinica.bean.login.UserAccountBean;
import org.akaza.openclinica.bean.managestudy.*;
import org.akaza.openclinica.bean.submit.CRFVersionBean;
import org.akaza.openclinica.bean.submit.EventCRFBean;
import org.akaza.openclinica.bean.submit.SubjectBean;
import org.akaza.openclinica.dao.admin.CRFDAO;
import org.akaza.openclinica.dao.login.UserAccountDAO;
import org.akaza.openclinica.dao.managestudy.StudyDAO;
import org.akaza.openclinica.dao.managestudy.StudyEventDAO;
import org.akaza.openclinica.dao.managestudy.StudyEventDefinitionDAO;
import org.akaza.openclinica.dao.managestudy.StudySubjectDAO;
import org.akaza.openclinica.dao.submit.CRFVersionDAO;
import org.akaza.openclinica.dao.submit.EventCRFDAO;
import org.akaza.openclinica.dao.submit.SubjectDAO;
import org.akaza.openclinica.exception.OpenClinicaSystemException;
import org.akaza.openclinica.i18n.util.ResourceBundleProvider;
import org.akaza.openclinica.service.subject.SubjectServiceInterface;
import org.akaza.openclinica.ws.bean.SubjectStudyDefinitionBean;
import org.akaza.openclinica.ws.validator.SubjectTransferValidator;
import org.openclinica.ws.beans.*;
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

import javax.sql.DataSource;
import javax.xml.bind.JAXBElement;
import javax.xml.datatype.DatatypeConstants;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Source;
import javax.xml.transform.dom.DOMSource;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

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
    	Errors errors = null;
    	try {
        	
            ResourceBundleProvider.updateLocale(locale);
            Element subjectElement = (Element) subject.item(0);
            SubjectTransferBean subjectTransferBean = unMarshallToSubjectTransfer(subjectElement);

            DataBinder dataBinder = new DataBinder((subjectTransferBean));
            errors = dataBinder.getBindingResult();
            subjectTransferBean.setOwner(getUserAccount());
            SubjectTransferValidator subjectTransferValidator = new SubjectTransferValidator(dataSource);
            subjectTransferValidator.validate((subjectTransferBean), errors);
            if (!errors.hasErrors()) {
                String label = create(subjectTransferBean);
                return new DOMSource(mapConfirmation(messages.getMessage("studySubjectEndpoint.success", null, "Success", locale), label, errors));
            } else {
               return new DOMSource(mapConfirmation(messages.getMessage("studySubjectEndpoint.fail", null, "Fail", locale), null, errors));
            }
        } catch (NullPointerException npe) {
            npe.printStackTrace();
            return new DOMSource(mapConfirmation(messages.getMessage("studySubjectEndpoint.fail", null, "Null Pointer Exception", locale), null, errors));
        } catch(Exception e){
        	  List<String> error_messages = new ArrayList<String>();
        	  error_messages.add(e.getMessage());
        	  return new DOMSource(mapConfirmation(messages.getMessage("studySubjectEndpoint.fail", null, "Fail", locale), null,  errors, "label", error_messages) );
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
        try {
            ResourceBundleProvider.updateLocale(new Locale("en_US"));
            ListStudySubjectsInStudyType listStudySubjectsInStudyType = requestElement.getValue();
            StudyBean study = null;
            try {
                study = validateRequestAndReturnStudy(listStudySubjectsInStudyType.getStudyRef());
            } catch (OpenClinicaSystemException e) {
                e.printStackTrace();
                ListAllByStudyResponse response = new ListAllByStudyResponse();
                response.setResult(messages.getMessage("studySubjectEndpoint.fail", null, "Fail", locale));
                response.getError().add(messages.getMessage(e.getErrorCode(), null, e.getErrorCode(), locale));
                return response;
            }
            return mapListStudySubjectsInStudyResponse(study, messages.getMessage("studySubjectEndpoint.success", null, "Success", locale),
                    listStudySubjectsInStudyType.getStudyRef());
        } catch (Exception eee) {
            eee.printStackTrace();
            throw eee;
        }
    }

    
    /**
       * Use this method to find if studysubject exists by study/site/subject lable.
     * 
     * @param subject
     * @return studySubjectOID
     * @throws Exception
  */
    @PayloadRoot(localPart = "isStudySubjectRequest", namespace = NAMESPACE_URI_V1)
    public Source isStudySubject( @XPathParam("//studySubject:studySubject") NodeList subject
    		) throws Exception {
    	ResourceBundleProvider.updateLocale(locale);
        Element subjectElement = (Element) subject.item(0);
       // Element studyElement = (Element) study.item(0);
        SubjectStudyDefinitionBean subjectStudyBean = unMarshallToSubjectStudy(subjectElement);//,studyElement);

        DataBinder dataBinder = new DataBinder((subjectStudyBean));
        Errors errors = dataBinder.getBindingResult();
        SubjectTransferValidator subjectTransferValidator = new SubjectTransferValidator(dataSource);
        subjectTransferValidator.validateIsSubjectExists((subjectStudyBean), errors);
        if (subjectStudyBean.getSubjectOIDId() == null ){//case for core misfunction
            errors.reject("studySubjectEndpoint.fail");
            
       }
        if (!errors.hasErrors()) {
           
            return new DOMSource(mapConfirmation(messages.getMessage("studySubjectEndpoint.success", null, 
            		"Success", locale), subjectStudyBean.getSubjectOIDId(), errors,"subjectOID"));

        } else {
            return new DOMSource(mapConfirmation(messages.getMessage("studySubjectEndpoint.fail", null, "Fail", locale), null, errors));
            
        }
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
            if ( studySubjectBean.getEnrollmentDate() != null){
            	studySubjectType.setEnrollmentDate(getXMLGregorianCalendarDate(studySubjectBean.getEnrollmentDate()));
            }
            SubjectBean subjectBean = (SubjectBean) getSubjectDao().findByPK(studySubjectBean.getSubjectId());
            subjectType.setUniqueIdentifier(subjectBean.getUniqueIdentifier());
            String genderStr = String.valueOf(subjectBean.getGender());
            if (!"".equals(genderStr.trim())){
            	subjectType.setGender(GenderType.fromValue(genderStr));
            }
            if ( subjectBean.getDateOfBirth() != null){
            	subjectType.setDateOfBirth(getXMLGregorianCalendarDate(subjectBean.getDateOfBirth()));
            }
            studySubjectType.setSubject(subjectType);
            // studySubjectType.setStudyRef(studyRef);
            logger.debug(studySubjectBean.getLabel());
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
        	 StudyEventDefinitionBean sed = (StudyEventDefinitionBean) studyEventDefinitionDao.findByPK(studyEventBean.getStudyEventDefinitionId());
        	 studyEventBean.setStudyEventDefinition(sed);


             EventResponseType eventResponseType = new EventResponseType();
            eventResponseType.setEventDefinitionOID(studyEventBean.getStudyEventDefinition().getOid());
            eventResponseType.setStatus(studyEventBean.getStatus().getName());
            eventResponseType.setOccurrence(studyEventBean.getSampleOrdinal() + "");
            eventResponseType.setSubjectEventStatus(studyEventBean.getSubjectEventStatus().getName());
            eventResponseType.setLocation(studyEventBean.getLocation());
            if ( studyEventBean.getDateStarted() != null){
            	eventResponseType.setStartDate(getXMLGregorianCalendarDate(studyEventBean.getDateStarted()));
            	eventResponseType.setStartTime(getXMLGregorianCalendarTime(studyEventBean.getDateStarted()));
            }
            if ( studyEventBean.getDateEnded() != null){
	            eventResponseType.setEndDate(getXMLGregorianCalendarDate(studyEventBean.getDateEnded()));
	            eventResponseType.setEndTime(getXMLGregorianCalendarTime(studyEventBean.getDateEnded()));
            }
            EventCrfInformationList eventCrfInformationList = createEventCrfInformationList(studyEventBean);
            eventResponseType.getEventCrfInformation().add(eventCrfInformationList);
            
            eventsType.getEvent().add(eventResponseType);
            logger.debug(eventResponseType.getEventDefinitionOID()+" "+eventResponseType.getStartDate());
            
        }
        return eventsType;
    }

    private EventCrfInformationList createEventCrfInformationList(StudyEventBean studyEventBean) {
        EventCrfInformationList eventCrfInformationList = new EventCrfInformationList ();

        CRFVersionDAO crfVersionDAO = new CRFVersionDAO(dataSource);
        EventCRFDAO eventCRFDAO = new EventCRFDAO(dataSource);
        CRFDAO crfdao = new CRFDAO(dataSource);

        List<EventCRFBean> eventCRFBeanList = eventCRFDAO.findAllByStudyEvent(studyEventBean);
        for (EventCRFBean eventCRFBean : eventCRFBeanList) {
            CRFVersionBean crfVersionBean = (CRFVersionBean) crfVersionDAO.findByPK(eventCRFBean.getCRFVersionId());
            CRFBean crf = crfdao.findByVersionId(crfVersionBean.getCrfId());
            EventCrfType eventCrfType = new EventCrfType();
            eventCrfType.setStatus(eventCRFBean.getStage().getName());
            eventCrfType.setName(crf.getName());
            eventCrfType.setVersion(crfVersionBean.getName());
            eventCrfType.setOid(crfVersionBean.getOid());
            eventCrfInformationList.getEventCrf().add(eventCrfType);
        }

        return eventCrfInformationList;
    }



    /**
     * Validate the listStudySubjectsInStudy request.
     * 
     * @param studyRef
     * @return StudyBean
     */
    private StudyBean validateRequestAndReturnStudy(StudyRefType studyRef) {

        String studyIdentifier = studyRef == null ? null : studyRef.getIdentifier().trim();
        String siteIdentifier = studyRef.getSiteRef() == null ? null : studyRef.getSiteRef().getIdentifier().trim();

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
    private SubjectTransferBean unMarshallToSubjectTransfer(Element subjectElement) throws ParseException, Exception {

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

        String personIdValue = personIdElement == null ? "" : DomUtils.getTextValue(personIdElement).trim();
        String studySubjectIdValue = studySubjectIdElement == null ? "" : DomUtils.getTextValue(studySubjectIdElement).trim();
        String genderValue = genderElement == null ? null : DomUtils.getTextValue(genderElement).trim();
        String secondaryIdValue = secondaryIdElement == null ? null : DomUtils.getTextValue(secondaryIdElement).trim();
        String enrollmentDateValue = DomUtils.getTextValue(enrollmentDateElement).trim();
        String dateOfBirthValue = dateOfBirthElement == null ? null : DomUtils.getTextValue(dateOfBirthElement).trim();
        String yearOfBirthValue = yearOfBirthElement == null ? null : DomUtils.getTextValue(yearOfBirthElement).trim();
        String studyIdentifier = studyIdentifierElement == null ? null : DomUtils.getTextValue(studyIdentifierElement).trim();
        String siteIdentifier = siteIdentifierElement == null ? null : DomUtils.getTextValue(siteIdentifierElement).trim();

        SubjectTransferBean subjectTransferBean = new SubjectTransferBean();

        subjectTransferBean.setStudyOid(studyIdentifier);
        subjectTransferBean.setSiteIdentifier(siteIdentifier);
        subjectTransferBean.setPersonId(personIdValue);
        subjectTransferBean.setStudySubjectId(studySubjectIdValue);
        if (genderValue == null || genderValue.length()<1) {
            // Do nothing
        } else {
            subjectTransferBean.setGender(genderValue.toCharArray()[0]);
        }
        subjectTransferBean.setDateOfBirth((dateOfBirthValue == null || dateOfBirthValue.length()==0)? null : getDate(dateOfBirthValue));
        subjectTransferBean.setSecondaryId(secondaryIdValue == null ? "" : secondaryIdValue);
        subjectTransferBean.setYearOfBirth(yearOfBirthValue);
        subjectTransferBean.setEnrollmentDate(getDate(enrollmentDateValue));

        // subjectTransferBean.setOwner(getUserAccount());

        return subjectTransferBean;
    }

    /**
     * Process createStudySubject request by creating SubjectStudyDefinitionBean from received payload.
     * 
     * @param subjectStudyElement
     * @return SubjectTransferBean
     * @throws ParseException
     */
    private SubjectStudyDefinitionBean unMarshallToSubjectStudy(Element subjectStudyElement) throws ParseException {

        Element studySubjectIdElement = DomUtils.getChildElementByTagName(subjectStudyElement, "label");
        Element study = DomUtils.getChildElementByTagName(subjectStudyElement, "studyRef");
        Element studyIdentifierElement = DomUtils.getChildElementByTagName(study, "identifier");
        Element site = DomUtils.getChildElementByTagName(study, "siteRef");
        Element siteIdentifierElement = site == null ? null : DomUtils.getChildElementByTagName(site, "identifier");

        String studySubjectIdValue = DomUtils.getTextValue(studySubjectIdElement).trim();
        String studyIdentifier = studyIdentifierElement == null ? null : DomUtils.getTextValue(studyIdentifierElement).trim();
        String siteIdentifier = siteIdentifierElement == null ? null : DomUtils.getTextValue(siteIdentifierElement).trim();

        SubjectStudyDefinitionBean subjectTransferBean = new SubjectStudyDefinitionBean(
        		studyIdentifier,  siteIdentifier, 
        		getUserAccount(),  studySubjectIdValue);

  
        return subjectTransferBean;
    }
    /**
     * Create the Subject object if it is not already in the system.
     * 
     * @param subjectTransferBean
     * @return String
     */
    private String create(SubjectTransferBean subjectTransferBean) {
        //boolean isSubjectInMain = doesSubjectExist(subjectTransferBean);

//        if (isSubjectInMain) {
//            // TODO : either return something or throw exception or don't do anything
//            logger.debug("SubjectInMain");
//            throw new OpenClinicaSystemException("Duplicate label");
//        } else {
            logger.debug("creating subject transfer");
            return createSubject(subjectTransferBean);
        //}
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
        // below added tbh 04/2011
        if (subject.getDateOfBirth() != null) {
        	subject.setDobCollected(true);
        } else {
        	subject.setDobCollected(false);
        }
        // >> above added tbh 04/2011, mantis issue having to 
        // deal with not being able to change DOB after a submit
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
    
    private Element mapConfirmation(String confirmation, String studySubjectLabel, Errors errors) throws Exception  	{
    	return  mapConfirmation( confirmation,  studySubjectLabel,  errors, "label", null) ;    	}
    
    private Element mapConfirmation(String confirmation, String studySubjectLabel, Errors errors, String label) throws Exception  	{
    	return  mapConfirmation( confirmation,  studySubjectLabel,  errors, label, null) ;    
    }

    private Element mapConfirmation(String confirmation, String studySubjectLabel, Errors errors, String label,  List<String> error_messages) throws Exception {
        DocumentBuilderFactory dbfac = DocumentBuilderFactory.newInstance();
        DocumentBuilder docBuilder = dbfac.newDocumentBuilder();
        Document document = docBuilder.newDocument();

        Element responseElement = document.createElementNS(NAMESPACE_URI_V1, "createResponse");
        Element resultElement = document.createElementNS(NAMESPACE_URI_V1, "result");
        resultElement.setTextContent(confirmation);
        responseElement.appendChild(resultElement);
        
        if ( studySubjectLabel != null){
	        Element labelElement = document.createElementNS(NAMESPACE_URI_V1, label);
	        labelElement.setTextContent(studySubjectLabel);
	        responseElement.appendChild(labelElement);
        }
     
        if ( errors != null){
	        for (ObjectError error : errors.getAllErrors()) {
	            Element errorElement = document.createElementNS(NAMESPACE_URI_V1, "error");
	            String theMessage = messages.getMessage(error.getCode(), error.getArguments(), locale);
	            errorElement.setTextContent(theMessage);
	            responseElement.appendChild(errorElement);
	        }
        }
        if ( error_messages != null && error_messages.size()>0){
	    	StringBuilder output_msg = new StringBuilder();
	        for (String mes : error_messages){
	        	output_msg.append(mes);
	        }
	    	Element msgElement = document.createElementNS(NAMESPACE_URI_V1, "error");
	        msgElement.setTextContent(output_msg.toString());
	        responseElement.appendChild(msgElement);
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
        GregorianCalendar gc = new GregorianCalendar(locale);
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
    private Date getDate(String dateAsString) throws ParseException, Exception {
        SimpleDateFormat sdf = new SimpleDateFormat(getDateFormat());
        sdf.setLenient(false);
        Date dd = sdf.parse(dateAsString);
        Calendar c = Calendar.getInstance();
        c.setTime(dd);
        if (c.get(Calendar.YEAR) < 1900 || c.get(Calendar.YEAR) > 9999) {
        	throw new Exception("Unparsable date: "+dateAsString);
        }
        return dd;
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