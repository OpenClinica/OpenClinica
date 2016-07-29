package org.akaza.openclinica.ws;

import org.akaza.openclinica.bean.core.Status;
import org.akaza.openclinica.bean.extract.odm.FullReportBean;
import org.akaza.openclinica.bean.login.StudyUserRoleBean;
import org.akaza.openclinica.bean.login.UserAccountBean;
import org.akaza.openclinica.bean.managestudy.StudyBean;
import org.akaza.openclinica.bean.odmbeans.ODMBean;
import org.akaza.openclinica.dao.core.CoreResources;
import org.akaza.openclinica.dao.hibernate.RuleSetRuleDao;
import org.akaza.openclinica.dao.login.UserAccountDAO;
import org.akaza.openclinica.dao.managestudy.StudyDAO;
import org.akaza.openclinica.i18n.util.ResourceBundleProvider;
import org.akaza.openclinica.logic.odmExport.AdminDataCollector;
import org.akaza.openclinica.logic.odmExport.MetaDataCollector;
import org.akaza.openclinica.ws.bean.BaseStudyDefinitionBean;
import org.akaza.openclinica.ws.validator.StudyMetadataRequestValidator;
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
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Source;
import javax.xml.transform.dom.DOMSource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

@Endpoint
public class StudyEndpoint {

    protected final Logger logger = LoggerFactory.getLogger(getClass().getName());
    private static final String NAMESPACE_URI_V1 = "http://openclinica.org/ws/study/v1";

    private final DataSource dataSource;
    StudyDAO studyDao;
    UserAccountDAO userAccountDao;
    private final MessageSource messages;
    private final CoreResources coreResources;
    private final RuleSetRuleDao ruleSetRuleDao;
  
    private final Locale locale;

    public StudyEndpoint(DataSource dataSource, MessageSource messages, 
    		CoreResources coreResources, RuleSetRuleDao ruleSetRuleDao) {
        this.dataSource = dataSource;
        this.messages = messages;
        this.locale = new Locale("en_US");
        this.coreResources = coreResources;
        this.ruleSetRuleDao = ruleSetRuleDao;
        
    }

    /**
     * if NAMESPACE_URI_V1:getStudyListRequest execute this method
     * 
     * @return
     * @throws Exception
     */
    @PayloadRoot(localPart = "getMetadataRequest", namespace = NAMESPACE_URI_V1)
    public Source getStudyMetadata(@XPathParam("//study:studyMetadata") NodeList studyNodeList) throws Exception {
        ResourceBundleProvider.updateLocale(new Locale("en_US"));
        Element studyRefElement = (Element) studyNodeList.item(0);

        //StudyMetadataRequestBean studyMetadataRequestBean = unMarshallRequest(studyRefElement);
        BaseStudyDefinitionBean studyMetadataRequestBean = unMarshallRequest(studyRefElement);
        DataBinder dataBinder = new DataBinder((studyMetadataRequestBean));
        Errors errors = dataBinder.getBindingResult();
        StudyMetadataRequestValidator studyMetadataRequestValidator = new StudyMetadataRequestValidator(dataSource);
        studyMetadataRequestValidator.validate((studyMetadataRequestBean), errors);
        if (!errors.hasErrors()) {

            return new DOMSource(mapSuccessConfirmation(getStudy(studyMetadataRequestBean),
                    messages.getMessage("studyEndpoint.success", null, "Success", locale)));
        } else {
            return new DOMSource(mapConfirmation(messages.getMessage("studyEndpoint.fail", null, "Fail", locale), errors));
        }
    }

    private Element mapSuccessConfirmation(StudyBean study, String confirmation) throws Exception {
        DocumentBuilderFactory dbfac = DocumentBuilderFactory.newInstance();
        DocumentBuilder docBuilder = dbfac.newDocumentBuilder();
        Document document = docBuilder.newDocument();

        Element responseElement = document.createElementNS(NAMESPACE_URI_V1, "createResponse");
        Element resultElement = document.createElementNS(NAMESPACE_URI_V1, "result");
        resultElement.setTextContent(confirmation);
        responseElement.appendChild(resultElement);
        
        Element odmElement = document.createElementNS(NAMESPACE_URI_V1, "odm");
        String reportText = getReport(study);
        odmElement.setTextContent(reportText);//meta.getXmlOutput().toString());
        responseElement.appendChild(odmElement);

        return responseElement;

    }

    
    private String getReport(StudyBean currentStudy){
//    	ServletContext servletContext =
//    	    (ServletContext) context.getMessageContext().get(MessageContext.SERVLET_CONTEXT);
        MetaDataCollector mdc = new MetaDataCollector(dataSource, currentStudy,ruleSetRuleDao);
        AdminDataCollector adc = new AdminDataCollector(dataSource, currentStudy);
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
        mdc.collectFileData();
        adc.collectFileData();
        
        FullReportBean report = new FullReportBean();
        report.setAdminDataMap(adc.getOdmAdminDataMap());
        report.setOdmStudyMap(mdc.getOdmStudyMap());
        report.setCoreResources(coreResources);
        report.setOdmBean(mdc.getODMBean());
        report.setODMVersion("oc1.3");
        report.createStudyMetaOdmXml(Boolean.FALSE);
       
        return  report.getXmlOutput().toString().trim();
    }
    private Element mapConfirmation(String confirmation, Errors errors) throws Exception {
        DocumentBuilderFactory dbfac = DocumentBuilderFactory.newInstance();
        DocumentBuilder docBuilder = dbfac.newDocumentBuilder();
        Document document = docBuilder.newDocument();

        Element responseElement = document.createElementNS(NAMESPACE_URI_V1, "createResponse");
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

    //private StudyMetadataRequestBean unMarshallRequest(Element studyEventDefinitionListAll) {
    	private BaseStudyDefinitionBean unMarshallRequest(Element studyEventDefinitionListAll) {

        Element studyIdentifierElement = DomUtils.getChildElementByTagName(studyEventDefinitionListAll, "identifier");
        String studyIdentifier = studyIdentifierElement == null ? null : DomUtils.getTextValue(studyIdentifierElement).trim();   
        BaseStudyDefinitionBean studyMetadataRequest = new BaseStudyDefinitionBean(studyIdentifier,  getUserAccount());
        return studyMetadataRequest;

    }

    StudyBean getStudy(BaseStudyDefinitionBean studyMetadataRequest) {
        StudyBean study = null;
        if (studyMetadataRequest.getStudyUniqueId() != null && studyMetadataRequest.getSiteUniqueId() == null) {
            study = getStudyDao().findByUniqueIdentifier(studyMetadataRequest.getStudyUniqueId());
        }
        if (studyMetadataRequest.getStudyUniqueId() != null && studyMetadataRequest.getSiteUniqueId() != null) {
            study = getStudyDao().findByUniqueIdentifier(studyMetadataRequest.getSiteUniqueId());
        }
        return study;

    }

    /**
     * if NAMESPACE_URI_V1:getStudyListRequest execute this method
     * 
     * @return
     * @throws Exception
     */
    @PayloadRoot(localPart = "listAllRequest", namespace = NAMESPACE_URI_V1)
    public Source getStudyList() throws Exception {
        ResourceBundleProvider.updateLocale(new Locale("en_US"));
        return new DOMSource(mapConfirmation(messages.getMessage("studyEndpoint.success", null, "Success", locale)));
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

    private HashMap<Integer, ArrayList<StudyBean>> getStudies() {

        ArrayList<StudyUserRoleBean> studyUserRoleBeans = getUserAccountDao().findStudyByUser(getUserAccount().getName(), (ArrayList) getStudyDao().findAll());

        HashMap<Integer, ArrayList<StudyBean>> validStudySiteMap = new HashMap<Integer, ArrayList<StudyBean>>();
        for (int i = 0; i < studyUserRoleBeans.size(); i++) {
            StudyUserRoleBean sr = studyUserRoleBeans.get(i);
            StudyBean study = (StudyBean) studyDao.findByPK(sr.getStudyId());
            if (study != null && study.getStatus().equals(Status.PENDING)) {
                sr.setStatus(study.getStatus());
            }
            if (study.isSite(study.getParentStudyId()) && !sr.isInvalid()) {
                if (validStudySiteMap.get(study.getParentStudyId()) == null) {
                    ArrayList<StudyBean> sites = new ArrayList<StudyBean>();
                    sites.add(study);
                    validStudySiteMap.put(study.getParentStudyId(), sites);
                } else {
                    validStudySiteMap.get(study.getParentStudyId()).add(study);
                }
            } else if (!study.isSite(study.getParentStudyId())) {
                if (validStudySiteMap.get(study.getId()) == null) {
                    ArrayList<StudyBean> sites = new ArrayList<StudyBean>();
                    validStudySiteMap.put(study.getId(), sites);
                }
            }
        }
        return validStudySiteMap;
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

        Element responseElement = document.createElementNS(NAMESPACE_URI_V1, "listAllResponse");
        Element resultElement = document.createElementNS(NAMESPACE_URI_V1, "result");
        resultElement.setTextContent(confirmation);
        responseElement.appendChild(resultElement);

        Element studyListElement = document.createElementNS(NAMESPACE_URI_V1, "studies");
        responseElement.appendChild(studyListElement);

        for (Map.Entry<Integer, ArrayList<StudyBean>> entry : getStudies().entrySet()) {
            StudyBean study = (StudyBean) getStudyDao().findByPK(entry.getKey());
            studyListElement.appendChild(createStudyWithSiteElement(document, study, entry.getValue()));
        }

        return responseElement;
    }

    private Element createStudyWithSiteElement(Document document, StudyBean study, ArrayList<StudyBean> sites) {

        Element studyElement = createStudyElement(document, "study", study);
        if (sites.size() > 0) {
            Element siteListElement = document.createElementNS(NAMESPACE_URI_V1, "sites");
            studyElement.appendChild(siteListElement);
            for (StudyBean siteBean : sites) {
                Element siteElement = createStudyElement(document, "site", siteBean);
                siteListElement.appendChild(siteElement);
            }
        }
        return studyElement;
    }

    private Element createStudyElement(Document document, String studyOrSite, StudyBean study) {

        Element studyElement = document.createElementNS(NAMESPACE_URI_V1, studyOrSite);

        Element element = document.createElementNS(NAMESPACE_URI_V1, "identifier");
        element.setTextContent(study.getIdentifier() + "");
        studyElement.appendChild(element);

        element = document.createElementNS(NAMESPACE_URI_V1, "oid");
        element.setTextContent(study.getOid());
        studyElement.appendChild(element);

        element = document.createElementNS(NAMESPACE_URI_V1, "name");
        element.setTextContent(study.getName());
        studyElement.appendChild(element);

        return studyElement;

    }

    public StudyDAO getStudyDao() {
        studyDao = studyDao != null ? studyDao : new StudyDAO(dataSource);
        return studyDao;
    }

    public UserAccountDAO getUserAccountDao() {
        userAccountDao = userAccountDao != null ? userAccountDao : new UserAccountDAO(dataSource);
        return userAccountDao;
    }
    
   

}
