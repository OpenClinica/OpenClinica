/*
 * OpenClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).
 * For details see: http://www.openclinica.org/license
 *
 * Copyright 2003-2009 Akaza Research
 */
package org.akaza.openclinica.ws;

import org.akaza.openclinica.bean.login.UserAccountBean;
import org.akaza.openclinica.dao.login.UserAccountDAO;
import org.akaza.openclinica.exception.OpenClinicaSystemException;
import org.akaza.openclinica.i18n.util.ResourceBundleProvider;
import org.akaza.openclinica.service.EventServiceInterface;

import org.akaza.openclinica.ws.bean.StudyEventTransferBean;
import org.akaza.openclinica.ws.validator.StudyEventTransferValidator;
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
import java.util.Date;
import java.util.HashMap;
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
public class EventEndpoint {

    protected final Logger logger = LoggerFactory.getLogger(getClass().getName());
    private final String NAMESPACE_URI_V1 = "http://openclinica.org/ws/event/v1";
    private String dateFormat;

    private final EventServiceInterface eventService;
    private final MessageSource messages;
    private final DataSource dataSource;
    Locale locale;

    /**
     * Constructor
     * 
     * @param subjectService
     * @param cctsService
     */
    public EventEndpoint(EventServiceInterface eventService, DataSource dataSource, MessageSource messages) {
        this.eventService = eventService;
        this.messages = messages;
        this.dataSource = dataSource;
        this.locale = new Locale("en_US");
    }

    /**
     * if NAMESPACE_URI_V1:scheduleRequest execute this method
     * 
     */
    @PayloadRoot(localPart = "scheduleRequest", namespace = NAMESPACE_URI_V1)
    public Source createSubject(@XPathParam("//e:event") NodeList event, @XPathParam("//e:study") String study,
            @XPathParam("//e:eventDefinitionOID") String eventDefinitionOID, @XPathParam("//e:location") String location,
            @XPathParam("//e:startDate") String startDate, @XPathParam("//e:startTime") String startTime, @XPathParam("//e:endDate") String endDate,
            @XPathParam("//e:endTime") String endTime) throws Exception {
        ResourceBundleProvider.updateLocale(new Locale("en_US"));
        Element eventElement = (Element) event.item(0);
        StudyEventTransferBean studyEventTransferBean = unMarshallToEventTransfer(eventElement);

        DataBinder dataBinder = new DataBinder((studyEventTransferBean));
        Errors errors = dataBinder.getBindingResult();
        StudyEventTransferValidator studyEventTransferValidator = new StudyEventTransferValidator(dataSource);
        studyEventTransferValidator.validate((studyEventTransferBean), errors);

        if (!errors.hasErrors()) {

            try {
                HashMap<String, String> h =
                    getEventService().scheduleEvent(studyEventTransferBean.getUser(), studyEventTransferBean.getStartDateTime(),
                            studyEventTransferBean.getEndDateTime(), studyEventTransferBean.getLocation(), studyEventTransferBean.getStudyUniqueId(),
                            studyEventTransferBean.getSiteUniqueId(), studyEventTransferBean.getEventDefinitionOID(), 
                            studyEventTransferBean.getSubjectLabel());
                return new DOMSource(mapSuccessConfirmation(h));
            } catch (OpenClinicaSystemException ose) {
                errors.reject("eventEndpoint.cannot_schedule", "Cannot schedule an event for this Subject.");
                return new DOMSource(mapFailConfirmation(errors));
            }
        } else {
            return new DOMSource(mapFailConfirmation(errors));
        }
    }

    /**
     * Create Success Response
     * 
     * @param confirmation
     * @return
     * @throws Exception
     */
    private Element mapSuccessConfirmation(HashMap<String, String> confirmation) throws Exception {
        DocumentBuilderFactory dbfac = DocumentBuilderFactory.newInstance();
        DocumentBuilder docBuilder = dbfac.newDocumentBuilder();
        Document document = docBuilder.newDocument();

        Element responseElement = document.createElementNS(NAMESPACE_URI_V1, "scheduleResponse");
        Element resultElement = document.createElementNS(NAMESPACE_URI_V1, "result");
        Element eventDefinitionOID = document.createElementNS(NAMESPACE_URI_V1, "eventDefinitionOID");
        Element studySubjectOID = document.createElementNS(NAMESPACE_URI_V1, "studySubjectOID");
        Element eventOrdinal = document.createElementNS(NAMESPACE_URI_V1, "studyEventOrdinal");
        resultElement.setTextContent(messages.getMessage("eventEndpoint.success", null, "Success", locale));
        eventDefinitionOID.setTextContent(confirmation.get("eventDefinitionOID"));
        studySubjectOID.setTextContent(confirmation.get("studySubjectOID"));
        eventOrdinal.setTextContent(confirmation.get("studyEventOrdinal"));
        responseElement.appendChild(resultElement);
        responseElement.appendChild(eventDefinitionOID);
        responseElement.appendChild(studySubjectOID);
        responseElement.appendChild(eventOrdinal);
        return responseElement;

    }

    /**
     * Process createEvent request by creating StudyEventTransferBean from received payload.
     * 
     * @param subjectElement
     * @return SubjectTransferBean
     * @throws ParseException
     */
    private StudyEventTransferBean unMarshallToEventTransfer(Element eventElement)
    	throws ParseException {

   

    Element eventDefinitionOidElement = DomUtils.getChildElementByTagName(eventElement, "eventDefinitionOID");
    Element locationElement = DomUtils.getChildElementByTagName(eventElement, "location");
    Element startDateElement = DomUtils.getChildElementByTagName(eventElement, "startDate");
    Element endDateElement = DomUtils.getChildElementByTagName(eventElement, "endDate");
    Element startTimeElement = DomUtils.getChildElementByTagName(eventElement, "startTime");
    Element endTimeElement = DomUtils.getChildElementByTagName(eventElement, "endTime");

    Element subjectRefElement = DomUtils.getChildElementByTagName(eventElement, "studySubjectRef");
    Element labelElement = DomUtils.getChildElementByTagName(subjectRefElement, "label");

    Element studyRefElement = DomUtils.getChildElementByTagName(eventElement, "studyRef");
    Element studyIdentifierElement = DomUtils.getChildElementByTagName(studyRefElement, "identifier");
    Element siteRef = DomUtils.getChildElementByTagName(studyRefElement, "siteRef");
    Element siteIdentifierElement = siteRef == null ? null : DomUtils.getChildElementByTagName(siteRef, "identifier");

    String studySubjectId = DomUtils.getTextValue(labelElement).trim();
    String eventDefinitionOID = DomUtils.getTextValue(eventDefinitionOidElement).trim();
    String studyIdentifier = DomUtils.getTextValue(studyIdentifierElement).trim();
    String siteIdentifier = siteIdentifierElement == null ? null : DomUtils.getTextValue(siteIdentifierElement).trim();
    String location = locationElement == null ? null : DomUtils.getTextValue(locationElement).trim();
    String startDate = DomUtils.getTextValue(startDateElement).trim();
    String startTime = startTimeElement == null ? null : DomUtils.getTextValue(startTimeElement).trim();
    String endDate = endDateElement == null ? null : DomUtils.getTextValue(endDateElement).trim();
    String endTime = endTimeElement == null ? null : DomUtils.getTextValue(endTimeElement).trim();

    StudyEventTransferBean studyEventTransferBean =
        new StudyEventTransferBean(studySubjectId, studyIdentifier, siteIdentifier, eventDefinitionOID, location, getDate(startDate, startTime), getDate(
                endDate, endTime), getUserAccount());
    return studyEventTransferBean;
}
    
    /**
     * Create Error Response
     * 
     * @param confirmation
     * @return
     * @throws Exception
     */
    private Element mapFailConfirmation(Errors errors) throws Exception {
        DocumentBuilderFactory dbfac = DocumentBuilderFactory.newInstance();
        DocumentBuilder docBuilder = dbfac.newDocumentBuilder();
        Document document = docBuilder.newDocument();

        Element responseElement = document.createElementNS(NAMESPACE_URI_V1, "scheduleResponse");
        Element resultElement = document.createElementNS(NAMESPACE_URI_V1, "result");
        resultElement.setTextContent(messages.getMessage("eventEndpoint.fail", null, "Fail", locale));
        responseElement.appendChild(resultElement);
        for (ObjectError error : errors.getAllErrors()) {
            Element errorElement = document.createElementNS(NAMESPACE_URI_V1, "error");
            String theMessage = messages.getMessage(error.getCode(), error.getArguments(), locale);
            errorElement.setTextContent(theMessage);
            responseElement.appendChild(errorElement);
        }
        return responseElement;

    }

    /**
     * Helper Method to resolve dates
     * 
     * @param dateAsString
     * @return
     * @throws ParseException
     */
    private Date getDate(String dateAsString, String hourMinuteAsString) throws ParseException {
        Date d = null;
        if (dateAsString != null && dateAsString.length() != 0) {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
            dateAsString += hourMinuteAsString == null ? " 00:00" : " " + hourMinuteAsString;
            d = sdf.parse(dateAsString);
            if (!sdf.format(d).equals(dateAsString)) {
                throw new OpenClinicaSystemException("Date not parseable");
            }
            return sdf.parse(dateAsString);
        } else {
            return null;
        }

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

    public EventServiceInterface getEventService() {
        return eventService;
    }

}