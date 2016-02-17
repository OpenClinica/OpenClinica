package org.akaza.openclinica.ws;

import org.akaza.openclinica.bean.admin.CRFBean;
import org.akaza.openclinica.bean.login.UserAccountBean;
import org.akaza.openclinica.bean.managestudy.EventDefinitionCRFBean;
import org.akaza.openclinica.bean.managestudy.StudyBean;
import org.akaza.openclinica.bean.managestudy.StudyEventDefinitionBean;
import org.akaza.openclinica.bean.submit.CRFVersionBean;
import org.akaza.openclinica.dao.admin.CRFDAO;
import org.akaza.openclinica.dao.login.UserAccountDAO;
import org.akaza.openclinica.dao.managestudy.EventDefinitionCRFDAO;
import org.akaza.openclinica.dao.managestudy.StudyDAO;
import org.akaza.openclinica.dao.managestudy.StudyEventDefinitionDAO;
import org.akaza.openclinica.dao.submit.CRFVersionDAO;
import org.akaza.openclinica.i18n.util.ResourceBundleProvider;
import org.akaza.openclinica.ws.bean.BaseStudyDefinitionBean;
import org.akaza.openclinica.ws.validator.StudyEventDefinitionRequestValidator;
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

import java.util.List;
import java.util.Locale;

import javax.sql.DataSource;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Source;
import javax.xml.transform.dom.DOMSource;

@Endpoint
public class StudyEventDefinitionEndpoint {

    protected final Logger logger = LoggerFactory.getLogger(getClass().getName());
    private final String NAMESPACE_URI_V1 = "http://openclinica.org/ws/studyEventDefinition/v1";
    private final DataSource dataSource;
    private final MessageSource messages;
    private final Locale locale;
    StudyDAO studyDao;
    UserAccountDAO userAccountDao;
    CRFDAO crfDao;
    CRFVersionDAO crfVersionDao;
    StudyEventDefinitionDAO studyEventDefinitionDao;
    EventDefinitionCRFDAO eventDefinitionCRFDao;

    public StudyEventDefinitionEndpoint(DataSource dataSource, MessageSource messages) {
        this.dataSource = dataSource;
        this.messages = messages;
        this.locale = new Locale("en_US");
    }

    /**
     * if NAMESPACE_URI_V1:getStudyListRequest execute this method
     * 
     * @return
     * @throws Exception
     */
    @PayloadRoot(localPart = "listAllRequest", namespace = NAMESPACE_URI_V1)
    public Source getStudyList(@XPathParam("//sed:studyEventDefinitionListAll") NodeList studyNodeList) throws Exception {
        ResourceBundleProvider.updateLocale(new Locale("en_US"));
        Element studyRefElement = (Element) studyNodeList.item(0);

       // StudyEventDefinitionRequestBean studyEventDefinitionRequestBean = unMarshallRequest(studyRefElement);
        BaseStudyDefinitionBean studyEventDefinitionRequestBean = unMarshallRequest(studyRefElement);
        
        DataBinder dataBinder = new DataBinder((studyEventDefinitionRequestBean));
        Errors errors = dataBinder.getBindingResult();
        StudyEventDefinitionRequestValidator studyEventDefinitionRequestValidator = new StudyEventDefinitionRequestValidator(dataSource);
        studyEventDefinitionRequestValidator.validate((studyEventDefinitionRequestBean), errors);
        if (!errors.hasErrors()) {

            return new DOMSource(mapConfirmation(getStudy(studyEventDefinitionRequestBean),
                    messages.getMessage("studyEventDefinitionEndpoint.success", null, "Success", locale)));
        } else {
            return new DOMSource(mapFailConfirmation(messages.getMessage("studyEventDefinitionEndpoint.fail", null, "Fail", locale), errors));
        }
    }

   // StudyBean getStudy(StudyEventDefinitionRequestBean studyEventDefinitionRequestBean) {
    	 StudyBean getStudy(BaseStudyDefinitionBean studyEventDefinitionRequestBean) {
    		         StudyBean study = null;
        if (studyEventDefinitionRequestBean.getStudyUniqueId() != null && studyEventDefinitionRequestBean.getSiteUniqueId() == null) {
            study = getStudyDao().findByUniqueIdentifier(studyEventDefinitionRequestBean.getStudyUniqueId());
        }
        if (studyEventDefinitionRequestBean.getStudyUniqueId() != null && studyEventDefinitionRequestBean.getSiteUniqueId() != null) {
            study = getStudyDao().findByUniqueIdentifier(studyEventDefinitionRequestBean.getSiteUniqueId());
        }
        return study;

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
        return (UserAccountBean) getUserAccountDao().findByUserName(username);
    }

  // private StudyEventDefinitionRequestBean unMarshallRequest(Element studyEventDefinitionListAll) {
    private BaseStudyDefinitionBean  unMarshallRequest(Element studyEventDefinitionListAll) {
        Element studyRefElement = DomUtils.getChildElementByTagName(studyEventDefinitionListAll, "studyRef");
        Element studyIdentifierElement = DomUtils.getChildElementByTagName(studyRefElement, "identifier");
        Element siteRef = DomUtils.getChildElementByTagName(studyRefElement, "siteRef");
        Element siteIdentifierElement = siteRef == null ? null : DomUtils.getChildElementByTagName(siteRef, "identifier");

        String studyIdentifier = studyIdentifierElement == null ? null : DomUtils.getTextValue(studyIdentifierElement).trim();
        String siteIdentifier = siteIdentifierElement == null ? null : DomUtils.getTextValue(siteIdentifierElement).trim();

//        StudyEventDefinitionRequestBean studyEventDefinitionRequestBean =
//            new StudyEventDefinitionRequestBean(studyIdentifier, siteIdentifier, getUserAccount());
       
        BaseStudyDefinitionBean studyEventDefinitionRequestBean =
        				new BaseStudyDefinitionBean(studyIdentifier, siteIdentifier, getUserAccount());
  
        return studyEventDefinitionRequestBean;

    }

    /**
     * Create Response
     * 
     * @param confirmation
     * @return
     * @throws Exception
     */
    private Element mapConfirmation(StudyBean study, String confirmation) throws Exception {
        DocumentBuilderFactory dbfac = DocumentBuilderFactory.newInstance();
        DocumentBuilder docBuilder = dbfac.newDocumentBuilder();
        Document document = docBuilder.newDocument();

        Element responseElement = document.createElementNS(NAMESPACE_URI_V1, "listAllResponse");
        Element resultElement = document.createElementNS(NAMESPACE_URI_V1, "result");
        resultElement.setTextContent(confirmation);
        responseElement.appendChild(resultElement);

        Element studyListElement = document.createElementNS(NAMESPACE_URI_V1, "studyEventDefinitions");
        responseElement.appendChild(studyListElement);

        List<StudyEventDefinitionBean> eventList = getStudyEventDefinitionDao().findAllByStudy(study);
        for (int index = 0; index < eventList.size(); index++) {
            StudyEventDefinitionBean event = eventList.get(index);

            Element studyElement = document.createElementNS(NAMESPACE_URI_V1, "studyEventDefinition");

            Element element = document.createElementNS(NAMESPACE_URI_V1, "oid");
            element.setTextContent(event.getOid());
            studyElement.appendChild(element);

            element = document.createElementNS(NAMESPACE_URI_V1, "name");
            element.setTextContent(event.getName());
            studyElement.appendChild(element);

            studyListElement.appendChild(studyElement);

            List<EventDefinitionCRFBean> eventCrfs = (List<EventDefinitionCRFBean>) getEventDefinitionCRFDao().findAllByDefinition(study, event.getId());

            Element eventDefinitionCrfListElement = document.createElementNS(NAMESPACE_URI_V1, "eventDefinitionCrfs");
            studyElement.appendChild(eventDefinitionCrfListElement);

            for (int i = 0; i < eventCrfs.size(); i++) {
                EventDefinitionCRFBean eventCrf = eventCrfs.get(i);

                Element eventDefinitionCrfElement = document.createElementNS(NAMESPACE_URI_V1, "eventDefinitionCrf");
                eventDefinitionCrfListElement.appendChild(eventDefinitionCrfElement);

                element = document.createElementNS(NAMESPACE_URI_V1, "required");
                element.setTextContent(String.valueOf(eventCrf.isRequiredCRF()));
                eventDefinitionCrfElement.appendChild(element);

                element = document.createElementNS(NAMESPACE_URI_V1, "doubleDataEntry");
                element.setTextContent(String.valueOf(eventCrf.isDoubleEntry()));
                eventDefinitionCrfElement.appendChild(element);

                element = document.createElementNS(NAMESPACE_URI_V1, "passwordRequired");
                element.setTextContent(String.valueOf(eventCrf.isElectronicSignature()));
                eventDefinitionCrfElement.appendChild(element);

                element = document.createElementNS(NAMESPACE_URI_V1, "hideCrf");
                element.setTextContent(String.valueOf(eventCrf.isHideCrf()));
                eventDefinitionCrfElement.appendChild(element);

                element = document.createElementNS(NAMESPACE_URI_V1, "participantForm");
                element.setTextContent(String.valueOf(eventCrf.isParticipantForm()));
                eventDefinitionCrfElement.appendChild(element);

                element = document.createElementNS(NAMESPACE_URI_V1, "allowAnonymousSubmission");
                element.setTextContent(String.valueOf(eventCrf.isAllowAnonymousSubmission()));
                eventDefinitionCrfElement.appendChild(element);

                element = document.createElementNS(NAMESPACE_URI_V1, "submissionUrl");
                element.setTextContent(String.valueOf(eventCrf.getSubmissionUrl()));
                eventDefinitionCrfElement.appendChild(element);

                element = document.createElementNS(NAMESPACE_URI_V1, "offline");
                element.setTextContent(String.valueOf(eventCrf.isOffline()));
                eventDefinitionCrfElement.appendChild(element);

                element = document.createElementNS(NAMESPACE_URI_V1, "sourceDataVerificaiton");
                element.setTextContent(String.valueOf(eventCrf.getSourceDataVerification()));
                eventDefinitionCrfElement.appendChild(element);

                Element crfElement = document.createElementNS(NAMESPACE_URI_V1, "crf");
                eventDefinitionCrfElement.appendChild(crfElement);

                CRFBean crfBean = (CRFBean) getCrfDao().findByPK(eventCrf.getCrfId());

                element = document.createElementNS(NAMESPACE_URI_V1, "oid");
                element.setTextContent(crfBean.getOid());
                crfElement.appendChild(element);

                element = document.createElementNS(NAMESPACE_URI_V1, "name");
                element.setTextContent(crfBean.getName());
                crfElement.appendChild(element);

                Element crfVersionElement = document.createElementNS(NAMESPACE_URI_V1, "defaultCrfVersion");
                eventDefinitionCrfElement.appendChild(crfVersionElement);

                CRFVersionBean crfVersionBean = (CRFVersionBean) getCrfVersionDao().findByPK(eventCrf.getDefaultVersionId());
                element = document.createElementNS(NAMESPACE_URI_V1, "oid");
                element.setTextContent(crfVersionBean.getOid());
                crfVersionElement.appendChild(element);

                element = document.createElementNS(NAMESPACE_URI_V1, "name");
                element.setTextContent(crfVersionBean.getName());
                crfVersionElement.appendChild(element);
            }
        }

        return responseElement;

    }

    private Element mapFailConfirmation(String confirmation, Errors errors) throws Exception {
        DocumentBuilderFactory dbfac = DocumentBuilderFactory.newInstance();
        DocumentBuilder docBuilder = dbfac.newDocumentBuilder();
        Document document = docBuilder.newDocument();

        Element responseElement = document.createElementNS(NAMESPACE_URI_V1, "listAllResponse");
        Element resultElement = document.createElementNS(NAMESPACE_URI_V1, "result");
        resultElement.setTextContent(confirmation);
        responseElement.appendChild(resultElement);

        for (ObjectError error : errors.getAllErrors()) {
            Element errorElement = document.createElementNS(NAMESPACE_URI_V1, "error");
            String theMessage = messages.getMessage(error.getCode(), error.getArguments(), locale);
            errorElement.setTextContent(theMessage);
            responseElement.appendChild(errorElement);
        }

        return responseElement;

    }

    public DataSource getDataSource() {
        return dataSource;
    }

    public StudyDAO getStudyDao() {
        studyDao = studyDao != null ? studyDao : new StudyDAO(dataSource);
        return studyDao;
    }

    public CRFDAO getCrfDao() {
        crfDao = crfDao != null ? crfDao : new CRFDAO(dataSource);
        return crfDao;
    }

    public CRFVersionDAO getCrfVersionDao() {
        crfVersionDao = crfVersionDao != null ? crfVersionDao : new CRFVersionDAO(dataSource);
        return crfVersionDao;
    }

    public StudyEventDefinitionDAO getStudyEventDefinitionDao() {
        studyEventDefinitionDao = studyEventDefinitionDao != null ? studyEventDefinitionDao : new StudyEventDefinitionDAO(dataSource);
        return studyEventDefinitionDao;
    }

    public EventDefinitionCRFDAO getEventDefinitionCRFDao() {
        eventDefinitionCRFDao = eventDefinitionCRFDao != null ? eventDefinitionCRFDao : new EventDefinitionCRFDAO(dataSource);
        return eventDefinitionCRFDao;
    }

    public UserAccountDAO getUserAccountDao() {
        userAccountDao = userAccountDao != null ? userAccountDao : new UserAccountDAO(dataSource);
        return userAccountDao;
    }

}
