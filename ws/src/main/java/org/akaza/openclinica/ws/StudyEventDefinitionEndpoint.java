package org.akaza.openclinica.ws;

import org.akaza.openclinica.bean.admin.CRFBean;
import org.akaza.openclinica.bean.core.Status;
import org.akaza.openclinica.bean.login.StudyUserRoleBean;
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
import org.akaza.openclinica.exception.OpenClinicaSystemException;
import org.akaza.openclinica.i18n.util.ResourceBundleProvider;
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
    private final String SUCCESS_MESSAGE = "success";
    private final String FAIL_MESSAGE = "fail";

    private final DataSource dataSource;
    StudyDAO studyDao;
    UserAccountDAO userAccountDao;
    CRFDAO crfDao;
    CRFVersionDAO crfVersionDao;
    StudyEventDefinitionDAO studyEventDefinitionDao;
    EventDefinitionCRFDAO eventDefinitionCRFDao;

    public StudyEventDefinitionEndpoint(DataSource dataSource) {
        this.dataSource = dataSource;
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
        StudyBean study = null;
        try {
            study = unMarshallRequest(studyRefElement);
        } catch (Exception e) {
            return new DOMSource(mapFailConfirmation(FAIL_MESSAGE, e.getMessage()));
        }
        return new DOMSource(mapConfirmation(study, SUCCESS_MESSAGE));
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

    private StudyBean unMarshallRequest(Element studyEventDefinitionListAll) {

        Element studyRefElement = DomUtils.getChildElementByTagName(studyEventDefinitionListAll, "studyRef");
        Element studyIdentifierElement = DomUtils.getChildElementByTagName(studyRefElement, "identifier");
        Element siteRef = DomUtils.getChildElementByTagName(studyRefElement, "siteRef");
        Element siteIdentifierElement = siteRef == null ? null : DomUtils.getChildElementByTagName(siteRef, "identifier");

        String studyIdentifier = studyIdentifierElement == null ? null : DomUtils.getTextValue(studyIdentifierElement);
        String siteIdentifier = siteIdentifierElement == null ? null : DomUtils.getTextValue(siteIdentifierElement);

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

                element = document.createElementNS(NAMESPACE_URI_V1, "sourceDataVerificaiton");
                element.setTextContent(String.valueOf(eventCrf.getSourceDataVerification()));
                eventDefinitionCrfElement.appendChild(element);

                Element crfElement = document.createElementNS(NAMESPACE_URI_V1, "crf");
                eventDefinitionCrfElement.appendChild(crfElement);

                CRFBean crfBean = (CRFBean) crfDao.findByPK(eventCrf.getCrfId());

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

    private Element mapFailConfirmation(String confirmation, String message) throws Exception {
        DocumentBuilderFactory dbfac = DocumentBuilderFactory.newInstance();
        DocumentBuilder docBuilder = dbfac.newDocumentBuilder();
        Document document = docBuilder.newDocument();

        Element responseElement = document.createElementNS(NAMESPACE_URI_V1, "listAllResponse");
        Element resultElement = document.createElementNS(NAMESPACE_URI_V1, "result");
        resultElement.setTextContent(confirmation);
        responseElement.appendChild(resultElement);

        Element errorElement = document.createElementNS(NAMESPACE_URI_V1, "error");
        errorElement.setTextContent(message);
        responseElement.appendChild(errorElement);

        return responseElement;

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
