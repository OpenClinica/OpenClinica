/*
 * OpenClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).
 * For details see: http://www.openclinica.org/license
 *
 * Copyright 2003-2009 Akaza Research
 */
package org.akaza.openclinica.ws;

import org.akaza.openclinica.bean.core.DatasetItemStatus;
import org.akaza.openclinica.bean.core.Status;
import org.akaza.openclinica.bean.extract.DatasetBean;
import org.akaza.openclinica.bean.extract.odm.FullReportBean;
import org.akaza.openclinica.bean.login.StudyUserRoleBean;
import org.akaza.openclinica.bean.login.UserAccountBean;
import org.akaza.openclinica.bean.managestudy.StudyBean;
import org.akaza.openclinica.bean.managestudy.StudyEventBean;
import org.akaza.openclinica.bean.managestudy.StudyEventDefinitionBean;
import org.akaza.openclinica.bean.managestudy.StudySubjectBean;
import org.akaza.openclinica.bean.managestudy.SubjectTransferBean;
import org.akaza.openclinica.bean.odmbeans.ODMBean;
import org.akaza.openclinica.bean.submit.SubjectBean;
import org.akaza.openclinica.dao.core.CoreResources;
import org.akaza.openclinica.dao.hibernate.RuleSetRuleDao;
import org.akaza.openclinica.dao.login.UserAccountDAO;
import org.akaza.openclinica.dao.managestudy.StudyDAO;
import org.akaza.openclinica.dao.managestudy.StudyEventDAO;
import org.akaza.openclinica.dao.managestudy.StudyEventDefinitionDAO;
import org.akaza.openclinica.dao.managestudy.StudySubjectDAO;
import org.akaza.openclinica.dao.submit.ItemDAO;
import org.akaza.openclinica.dao.submit.SubjectDAO;
import org.akaza.openclinica.exception.OpenClinicaSystemException;
import org.akaza.openclinica.i18n.util.ResourceBundleProvider;
import org.akaza.openclinica.logic.odmExport.AdminDataCollector;
import org.akaza.openclinica.logic.odmExport.ClinicalDataCollector;
import org.akaza.openclinica.logic.odmExport.ClinicalDataUnit;
import org.akaza.openclinica.logic.odmExport.MetaDataCollector;
import org.akaza.openclinica.logic.odmExport.OdmStudyBase;
import org.akaza.openclinica.service.subject.SubjectServiceInterface;
import org.akaza.openclinica.ws.bean.SubjectStudyDefinitionBean;
import org.akaza.openclinica.ws.validator.SubjectTransferValidator;
import org.hibernate.mapping.Collection;
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
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.LinkedHashMap;
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
    private final CoreResources coreResources;
    private final RuleSetRuleDao ruleSetRuleDao;
    private final static String sqlStatement ="select distinct * from extract_data_table where study_event_definition_id in";
    		//"(select study_event_definition_id from study_event_definition) " +
    		//"and item_id in " +
    		//"(select item_id from versioning_map where crf_version_id in (select distinct crf_version_id from event_crf ec where ec.study_event_id in " +
    		//"(select study_event_id from study_event))) " +
    		//" and (date(date_created) >= date('1900-01-01')) and (date(date_created) <= date('2100-12-31')) order by date_start asc";
  
    /**
     * Constructor
     * 
     * @param subjectService
     * @param dataSource
     */
    public StudySubjectEndpoint(SubjectServiceInterface subjectService, DataSource dataSource, MessageSource messages,CoreResources coreResources, RuleSetRuleDao ruleSetRuleDao) {
        this.subjectService = subjectService;
        this.dataSource = dataSource;
        this.messages = messages;
        this.locale = new Locale("en_US");
        this.coreResources = coreResources;
        this.ruleSetRuleDao = ruleSetRuleDao;
       
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
     * @param requestElement
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
    
    
    
    @PayloadRoot(localPart = "getStudySubjectEventRequest", namespace = NAMESPACE_URI_V1)
    public Source getStudySubjectEvent(@XPathParam("//studySubject:studySubject") NodeList subject) throws Exception
    {
    	try{
    	 ResourceBundleProvider.updateLocale(new Locale("en_US"));
         

    	 Element subjectElement = (Element) subject.item(0);
    	 
    	 SubjectStudyDefinitionBean subjectStudyBean = unMarshallToSubjectStudy(subjectElement);//,studyElement);
    	 DataBinder dataBinder = new DataBinder((subjectStudyBean));
    	 Errors errors = dataBinder.getBindingResult();
         SubjectTransferBean subjectTransferBean = unMarshallToSubjectTransfer(subjectElement);
         SubjectTransferValidator subjectTransferValidator = new SubjectTransferValidator(dataSource);
         subjectTransferValidator.validateIsSubjectExists((subjectStudyBean), errors);
         
         if (subjectStudyBean.getSubjectOIDId() == null ){//case for core misfunction
             errors.reject("studySubjectEndpoint.fail");
             
        }
         if (!errors.hasErrors()) {
        	 
             StudySubjectDAO studySubjectDao = new StudySubjectDAO(dataSource);
        	 StudyDAO studyDao = new StudyDAO(dataSource);
        	String name = subjectStudyBean.getSiteUniqueId()==null?subjectStudyBean.getStudyUniqueId(): subjectStudyBean.getSiteUniqueId();
        	StudyBean currentStudy =  (StudyBean)studyDao.findByUniqueIdentifier(name);
             StudySubjectBean ssbean = studySubjectDao.findByLabelAndStudy(subjectStudyBean.getSubjectLabel(),currentStudy);
        	 return new DOMSource(mapSuccessConfirmation( subjectStudyBean.getStudy(),messages.getMessage("studyEndpoint.success", null, "Success", locale)
        			 ,ssbean.getId()));
        	

         } else {
             return new DOMSource(mapConfirmation(messages.getMessage("studySubjectEndpoint.fail", null, "Fail", locale), null, errors));
             
         }
    	}catch(Exception e){
    		e.printStackTrace();
    		throw e;
    	}
    }
    
    
    private Element mapSuccessConfirmation(StudyBean study, String confirmation,int studySubjectId) throws Exception {
        DocumentBuilderFactory dbfac = DocumentBuilderFactory.newInstance();
        DocumentBuilder docBuilder = dbfac.newDocumentBuilder();
        Document document = docBuilder.newDocument();

        Element responseElement = document.createElementNS(NAMESPACE_URI_V1, "createResponse");
        Element resultElement = document.createElementNS(NAMESPACE_URI_V1, "result");
        resultElement.setTextContent(confirmation);
        responseElement.appendChild(resultElement);
        Element odmElement = document.createElementNS(NAMESPACE_URI_V1, "odm");
        String reportText = getReport(study,studySubjectId);
        odmElement.setTextContent(reportText);//meta.getXmlOutput().toString());
        responseElement.appendChild(odmElement);

        return responseElement;

    }
    private String getReport(StudyBean currentStudy,int studySubjectId){
        DatasetBean dataset = setDataSetValues(currentStudy, studySubjectId);
        MetaDataCollector mdc = new MetaDataCollector(dataSource, currentStudy,ruleSetRuleDao);
        AdminDataCollector adc = new AdminDataCollector(dataSource, currentStudy);
        ClinicalDataCollector cdc =  new ClinicalDataCollector(dataSource,dataset,currentStudy);
        FullReportBean report = new FullReportBean();
        MetaDataCollector.setTextLength(200);  
        ODMBean odmb = mdc.getODMBean();
        
        
        
        
        odmb.setSchemaLocation("http://www.cdisc.org/ns/odm/v1.3 OpenClinica-ODM1-3-0-OC2-0.xsd");
        ArrayList<String> xmlnsList = new ArrayList<String>();
        xmlnsList.add("xmlns=\"http://www.cdisc.org/ns/odm/v1.3\"");
        //xmlnsList.add("xmlns:OpenClinica=\"http://www.openclinica.org/ns/openclinica_odm/v1.3\"");
        xmlnsList.add("xmlns:OpenClinica=\"http://www.openclinica.org/ns/odm_ext_v130/v3.1\"");
        xmlnsList.add("xmlns:OpenClinicaRules=\"http://www.openclinica.org/ns/rules/v3.1\"");
        odmb.setXmlnsList(xmlnsList);
        odmb.setODMVersion("oc1.3");
        mdc.setODMBean(odmb);
        adc.setOdmbean(odmb);
        //mdc.collectFileData();
        cdc.setODMBean(odmb);
        cdc.setDataset(dataset); 
        cdc.populateStudyBaseMap(currentStudy.getId());
        cdc.setStudyBaseMap((LinkedHashMap<String, OdmStudyBase>) mdc.getStudyBaseMap());
               report.setAdminDataMap(adc.getOdmAdminDataMap());
        report.setOdmStudyMap(mdc.getOdmStudyMap());
        cdc.collectOdmClinicalDataMap(studySubjectId, currentStudy,dataset);
        
        //cdc.collectFileData();
        //although we do not have study metadata as part of this response, setting the map information to avoid null pointer.
        
    
        report.setCoreResources(coreResources);
        report.setOdmBean(mdc.getODMBean());
        report.setClinicalDataMap(cdc.getOdmClinicalDataMap());
        report.setODMVersion("oc1.3");
        report.createOdmXml(true);
        return  report.getXmlOutput().toString().trim();
    }
//Fake DataSet
    private DatasetBean setDataSetValues(StudyBean currentStudy,int studySubjectId) {
    	DatasetBean dataset = new DatasetBean(); 
    	dataset.setName("jikan");         
         DatasetItemStatus distatus = DatasetItemStatus.COMPLETED_AND_NONCOMPLETED;
         dataset.setDatasetItemStatus(distatus);
         dataset.setShowCRFinterviewerDate(true);
         dataset.setShowCRFstatus(true);
         dataset.setShowCRFversion(true);
         dataset.setShowEventStart(true);
         dataset.setShowSubjectStatus(true);
         dataset.setShowSubjectUniqueIdentifier(true);
         dataset.setSQLStatement(getSqlStatement(currentStudy.getId(),studySubjectId));
         dataset.setCollectItemData(false);//JN:Added this to have more control on when to display item data values
         dataset.setShowSubjectGender(true);
         dataset.setShowSubjectGroupInformation(true);
         dataset.setShowEventEnd(true);
         dataset.setShowEventEndTime(true);
         dataset.setShowEventStatus(true);
         dataset.setShowCRFinterviewerName(true);
         dataset.setCollectFormAuditData(false);//not collecting form audit
         dataset.setCollectFormDNdata(false);
         dataset.setCollectStudyEventAuditLogs(false);
         dataset.setShowSubjectGroupInformation(true);
         dataset.setShowSubjectDataAuditLogs(false);
         dataset.setCollectFormsWithNoEventCRFS(true);
		return dataset;
	}

//The ODM bean expects the sql statement in this format, this might be a good candidate for util method for the cases of leveraging the CRF 
    private String getSqlStatement(int studyId,int studySubjectId) {
    	String sqlStatement = StudySubjectEndpoint.sqlStatement;
    	StudyEventDAO studyEventDao = new StudyEventDAO(this.dataSource);
    	ItemDAO itemDao = new ItemDAO(this.dataSource);
    	
    	String results1 = studyEventDao.getStudyEventIdsForWSReq(studyId, studySubjectId);
    
    	sqlStatement+="("+results1+")";
    	sqlStatement+="and item_id in ";
    
    	sqlStatement+="("+itemDao.getItemsForSpecificEvents(studyId, studySubjectId)+")";
    	sqlStatement+=" and (date(date_created) >= date('1900-01-01')) and (date(date_created) <= date('2100-12-31')) order by date_start asc";
    	
    	return sqlStatement;
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
            subjectType.setGender(GenderType.fromValue(String.valueOf(subjectBean.getGender())));
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
        StudyEventDefinitionBean eb=null;
        for (StudyEventBean studyEventBean : events) {
        	 StudyEventDefinitionBean sed = (StudyEventDefinitionBean) studyEventDefinitionDao.findByPK(studyEventBean.getStudyEventDefinitionId());
        	 studyEventBean.setStudyEventDefinition(sed);
            
             EventType eventType = new EventType();
            eventType.setEventDefinitionOID(studyEventBean.getStudyEventDefinition().getOid());
            eventType.setLocation(studyEventBean.getLocation());
            if ( studyEventBean.getDateStarted() != null){
            	eventType.setStartDate(getXMLGregorianCalendarDate(studyEventBean.getDateStarted()));
            	eventType.setStartTime(getXMLGregorianCalendarTime(studyEventBean.getDateStarted()));
            }
            if ( studyEventBean.getDateEnded() != null){
	            eventType.setEndDate(getXMLGregorianCalendarDate(studyEventBean.getDateEnded()));
	            eventType.setEndTime(getXMLGregorianCalendarTime(studyEventBean.getDateEnded()));
            }
            
            
            eventsType.getEvent().add(eventType);
            logger.debug(eventType.getEventDefinitionOID()+" "+eventType.getStartDate());
            
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
        String studySubjectIdValue = DomUtils.getTextValue(studySubjectIdElement).trim();
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
     * @param subjectElement
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
        Date dd = null;
        if(!dateAsString.isEmpty())
        {  dd = sdf.parse(dateAsString);
        Calendar c = Calendar.getInstance();
        c.setTime(dd);
        if (c.get(Calendar.YEAR) < 1900 || c.get(Calendar.YEAR) > 9999) {
        	throw new Exception("Unparsable date: "+dateAsString);
        }
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
        studyDao = new StudyDAO(dataSource);
        return studyDao;
    }

    public UserAccountDAO getUserAccountDao() {
        userAccountDao = new UserAccountDAO(dataSource);
        return userAccountDao;
    }

    public SubjectDAO getSubjectDao() {
        subjectDao = new SubjectDAO(dataSource);
        return subjectDao;
    }

    public MessageSource getMessages() {
        return messages;
    }

}