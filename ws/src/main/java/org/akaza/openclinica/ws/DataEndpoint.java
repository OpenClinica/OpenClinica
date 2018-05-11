package org.akaza.openclinica.ws;

import org.akaza.openclinica.bean.login.UserAccountBean;
import org.akaza.openclinica.bean.managestudy.StudyBean;
import org.akaza.openclinica.bean.submit.DisplayItemBeanWrapper;
import org.akaza.openclinica.bean.submit.crfdata.ODMContainer;
import org.akaza.openclinica.bean.submit.crfdata.SubjectDataBean;
import org.akaza.openclinica.dao.core.CoreResources;
import org.akaza.openclinica.dao.login.UserAccountDAO;
import org.akaza.openclinica.i18n.util.ResourceBundleProvider;
import org.akaza.openclinica.logic.rulerunner.ExecutionMode;
import org.akaza.openclinica.logic.rulerunner.ImportDataRuleRunnerContainer;
import org.akaza.openclinica.service.rule.RuleSetServiceInterface;
import org.akaza.openclinica.web.crfdata.DataImportService;
import org.akaza.openclinica.web.crfdata.ImportCRFInfo;
import org.akaza.openclinica.web.crfdata.ImportCRFInfoContainer;
import org.akaza.openclinica.ws.bean.BaseStudyDefinitionBean;
import org.akaza.openclinica.ws.validator.CRFDataImportValidator;
import org.exolab.castor.mapping.Mapping;
import org.exolab.castor.xml.Unmarshaller;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.MessageSource;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.validation.DataBinder;
import org.springframework.validation.Errors;
import org.springframework.validation.ObjectError;
import org.springframework.ws.server.endpoint.annotation.Endpoint;
import org.springframework.ws.server.endpoint.annotation.PayloadRoot;
import org.springframework.ws.server.endpoint.annotation.XPathParam;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;

import javax.sql.DataSource;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.InputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

@Endpoint
public class DataEndpoint {

    protected static final Logger LOG = LoggerFactory.getLogger(DataEndpoint.class);
    private final String NAMESPACE_URI_V1 = "http://openclinica.org/ws/data/v1";

    private final String ODM_HEADER_NAMESPACE = "<ODM xmlns=\"http://www.cdisc.org/ns/odm/v1.3\" targetNamespace=\"http://openclinica.org/ws/data/v1\" xmlns:OpenClinica=\"http://www.openclinica.org/ns/openclinica_odm/v1.3\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"http://www.cdisc.org/ns/odm/v1.3\">";
    private final DataSource dataSource;
    private final MessageSource messages;
    private final CoreResources coreResources;
    private final Locale locale;
    private final DataImportService dataImportService = new DataImportService();

    private RuleSetServiceInterface ruleSetService;

    private TransactionTemplate transactionTemplate;

    public DataEndpoint(DataSource dataSource, MessageSource messages, CoreResources coreResources) {
        this.dataSource = dataSource;
        this.messages = messages;
        this.coreResources = coreResources;
        this.ruleSetService = getRuleSetService();

        this.locale = new Locale("en_US");
    }

    /**
     * if NAMESPACE_URI_V1:importDataRequest execute this method
     * 
     * @return
     * @throws Exception
     */
    @PayloadRoot(localPart = "importRequest", namespace = NAMESPACE_URI_V1)
    public Source importData(@XPathParam("//ODM") final Element odmElement) throws Exception {
        return getTransactionTemplate().execute(new TransactionCallback<Source>() {
            public Source doInTransaction(TransactionStatus status) {
                try {
                    return importDataInTransaction(odmElement);
                } catch (Exception e) {
                    throw new RuntimeException("Error processing data import request", e);
                }
            }
        });
    }

    protected Source importDataInTransaction(Element odmElement) throws Exception {
        ResourceBundleProvider.updateLocale(new Locale("en_US"));

        // logger.debug("rootElement=" + odmElement);
        LOG.debug("rootElement=" + odmElement);

        // String xml = null;
        UserAccountBean userBean = null;

        try {
            if (odmElement == null) {
                return new DOMSource(mapFailConfirmation(null, "Your XML is not well-formed."));
            }
            // xml = node2String(odmElement);
            // xml = xml.replaceAll("<ODM>", this.ODM_HEADER_NAMESPACE);
            ODMContainer odmContainer = unmarshallToODMContainer(odmElement);
            // Element clinicalDataNode = (Element) odmElement.getElementsByTagName("ClinicalData").item(0);
            // String studyUniqueID = clinicalDataNode.getAttribute("StudyOID");
            String studyUniqueID = odmContainer.getCrfDataPostImportContainer().getStudyOID();
            userBean = getUserAccount();
            // CRFDataImportBean crfDataImportBean = new CRFDataImportBean(studyUniqueID, userBean);
            BaseStudyDefinitionBean crfDataImportBean = new BaseStudyDefinitionBean(studyUniqueID, userBean);

            DataBinder dataBinder = new DataBinder(crfDataImportBean);
            Errors errors = dataBinder.getBindingResult();
            CRFDataImportValidator crfDataImportValidator = new CRFDataImportValidator(dataSource);
            crfDataImportValidator.validate(crfDataImportBean, errors);

            if (!errors.hasErrors()) {
                StudyBean studyBean = crfDataImportBean.getStudy();

                List<DisplayItemBeanWrapper> displayItemBeanWrappers = new ArrayList<DisplayItemBeanWrapper>();
                HashMap<Integer, String> importedCRFStatuses = new HashMap<Integer, String>();

                List<String> errorMessagesFromValidation = dataImportService.validateMetaData(odmContainer, dataSource, coreResources, studyBean, userBean,
                        displayItemBeanWrappers, importedCRFStatuses);

                if (errorMessagesFromValidation.size() > 0) {
                    String err_msg = convertToErrorString(errorMessagesFromValidation);
                    return new DOMSource(mapFailConfirmation(null, err_msg));
                }

                ImportCRFInfoContainer importCrfInfo = new ImportCRFInfoContainer(odmContainer, dataSource);

                errorMessagesFromValidation = dataImportService.validateData(odmContainer, dataSource, coreResources, studyBean, userBean,
                        displayItemBeanWrappers, importedCRFStatuses);

                if (errorMessagesFromValidation.size() > 0) {
                    String err_msg = convertToErrorString(errorMessagesFromValidation);
                    return new DOMSource(mapFailConfirmation(null, err_msg));
                }

                // setup ruleSets to run if applicable
                ArrayList<SubjectDataBean> subjectDataBeans = odmContainer.getCrfDataPostImportContainer().getSubjectData();
                List<ImportDataRuleRunnerContainer> containers = dataImportService.runRulesSetup(dataSource, studyBean, userBean, subjectDataBeans,
                        ruleSetService);

                List<String> auditMsgs = new DataImportService().submitData(odmContainer, dataSource, studyBean, userBean, displayItemBeanWrappers,
                        importedCRFStatuses);

                // run rules if applicable
                List<String> ruleActionMsgs = dataImportService.runRules(studyBean, userBean, containers, ruleSetService, ExecutionMode.SAVE);

                List<String> skippedCRFMsgs = getSkippedCRFMessages(importCrfInfo);

                return new DOMSource(mapConfirmation(auditMsgs, ruleActionMsgs, skippedCRFMsgs, importCrfInfo));
            } else {
                return new DOMSource(mapFailConfirmation(errors, null));
            }

            // //
        } catch (Exception e) {
            // return new DOMSource(mapFailConfirmation(null,"Your XML is not well-formed. "+ npe.getMessage()));
            LOG.error("Error processing data import request", e);
            throw new Exception(e);
        }
        // return new DOMSource(mapConfirmation(xml, studyBean, userBean));
    }

    private List<String> getSkippedCRFMessages(ImportCRFInfoContainer importCrfInfo) {
        List<String> msgList = new ArrayList<String>();

        ResourceBundle respage = ResourceBundleProvider.getPageMessagesBundle();
        ResourceBundle resword = ResourceBundleProvider.getWordsBundle();
        MessageFormat mf = new MessageFormat("");
        mf.applyPattern(respage.getString("crf_skipped"));

        for (ImportCRFInfo importCrf : importCrfInfo.getImportCRFList()) {
            if (!importCrf.isProcessImport()) {
                String preImportStatus = "";

                if (importCrf.getPreImportStage().isInitialDE())
                    preImportStatus = resword.getString("initial_data_entry");
                else if (importCrf.getPreImportStage().isInitialDE_Complete())
                    preImportStatus = resword.getString("initial_data_entry_complete");
                else if (importCrf.getPreImportStage().isDoubleDE())
                    preImportStatus = resword.getString("double_data_entry");
                else if (importCrf.getPreImportStage().isDoubleDE_Complete())
                    preImportStatus = resword.getString("data_entry_complete");
                else if (importCrf.getPreImportStage().isAdmin_Editing())
                    preImportStatus = resword.getString("administrative_editing");
                else if (importCrf.getPreImportStage().isLocked())
                    preImportStatus = resword.getString("locked");
                else
                    preImportStatus = resword.getString("invalid");

                Object[] arguments = { importCrf.getStudyOID(), importCrf.getStudySubjectOID(), importCrf.getStudyEventOID(), importCrf.getFormOID(),
                        preImportStatus };
                msgList.add(mf.format(arguments));
            }
        }
        return msgList;
    }

    private ODMContainer unmarshallToODMContainer(Element odmElement) throws Exception {
        ResourceBundle respage = ResourceBundleProvider.getPageMessagesBundle();

        String xml = node2String(odmElement);
        xml = xml.replaceAll("<ODM>", this.ODM_HEADER_NAMESPACE);

        if (xml == null)
            throw new Exception(respage.getString("unreadable_file"));

        Mapping myMap = new Mapping();

        // InputStream xsdFile = coreResources.getInputStream("ODM1-3-0.xsd");//new File(propertiesPath + File.separator
        // + "ODM1-3-0.xsd");
        // InputStream xsdFile2 = coreResources.getInputStream("ODM1-2-1.xsd");//new File(propertiesPath +
        // File.separator + "ODM1-2-1.xsd");
        InputStream mapInputStream = coreResources.getInputStream("cd_odm_mapping.xml");

        myMap.loadMapping(new InputSource(mapInputStream));
        Unmarshaller um1 = new Unmarshaller(myMap);
        ODMContainer odmContainer = new ODMContainer();

        try {
            LOG.debug(xml);
            // File xsdFileFinal = new File(xsdFile);
            // schemaValidator.validateAgainstSchema(xml, xsdFile);
            // removing schema validation since we are presented with the chicken v egg error problem
            odmContainer = (ODMContainer) um1.unmarshal(new StringReader(xml));
            LOG.debug("Found crf data container for study oid: " + odmContainer.getCrfDataPostImportContainer().getStudyOID());
            LOG.debug("found length of subject list: " + odmContainer.getCrfDataPostImportContainer().getSubjectData().size());
            return odmContainer;

        } catch (Exception me1) {
            // fail against one, try another
            me1.printStackTrace();
            LOG.debug("failed in unmarshaling, trying another version = " + me1.getMessage());
            // htaycher: use only one schema according to Tom
            // try {
            // // schemaValidator.validateAgainstSchema(xml, xsdFile2);
            // // for backwards compatibility, we also try to validate vs
            // // 1.2.1 ODM 06/2008
            // odmContainer = (ODMContainer) um1.unmarshal(new StringReader(xml));
            // } catch (Exception me2) {
            // // not sure if we want to report me2
            // me2.printStackTrace();
            // // break here with an exception
            // logger.debug("found an error with XML: " + me2.getMessage());
            // throw new Exception();
            //
            // }
            throw new Exception();
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
     * Create Error Response
     * 
     * @param confirmation
     * @return
     * @throws Exception
     */
    private Element mapFailConfirmation(Errors errors, String message) throws Exception {
        DocumentBuilderFactory dbfac = DocumentBuilderFactory.newInstance();
        DocumentBuilder docBuilder = dbfac.newDocumentBuilder();
        Document document = docBuilder.newDocument();

        Element responseElement = document.createElementNS(NAMESPACE_URI_V1, "importDataResponse");
        Element resultElement = document.createElementNS(NAMESPACE_URI_V1, "result");

        String confirmation = messages.getMessage("dataEndpoint.fail", null, "Fail", locale);
        resultElement.setTextContent(confirmation);
        responseElement.appendChild(resultElement);

        if (errors != null) {
            for (ObjectError error : errors.getAllErrors()) {
                Element errorElement = document.createElementNS(NAMESPACE_URI_V1, "error");
                String theMessage = messages.getMessage(error.getCode(), error.getArguments(), locale);
                errorElement.setTextContent(theMessage);
                responseElement.appendChild(errorElement);
            }
        }
        if (message != null) {

            Element msgElement = document.createElementNS(NAMESPACE_URI_V1, "error");
            msgElement.setTextContent(message);
            responseElement.appendChild(msgElement);
            LOG.debug("sending fail message " + message);
        }
        return responseElement;

    }

    /**
     * Create Response
     * 
     * @param confirmation
     * @return
     * @throws Exception
     */
    private Element mapConfirmation(List<String> auditMsgs, List<String> ruleActionMsgs, List<String> skippedCRFMsgs, ImportCRFInfoContainer importCRFs)
            throws Exception {
        DocumentBuilderFactory dbfac = DocumentBuilderFactory.newInstance();
        DocumentBuilder docBuilder = dbfac.newDocumentBuilder();
        Document document = docBuilder.newDocument();

        Element responseElement = document.createElementNS(NAMESPACE_URI_V1, "importDataResponse");
        Element resultElement = document.createElementNS(NAMESPACE_URI_V1, "result");

        String totalCRFs = String.valueOf(importCRFs.getImportCRFList().size());
        String importedCRFs = String.valueOf(importCRFs.getImportCRFList().size() - importCRFs.getCountSkippedEventCrfs());

        if (auditMsgs != null) {
            String status = auditMsgs.get(0);
            if ("fail".equals(status)) {
                String confirmation = messages.getMessage("dataEndpoint.fail", null, "Fail", locale);
                resultElement.setTextContent(confirmation);
                responseElement.appendChild(resultElement);
                Element msgElement = document.createElementNS(NAMESPACE_URI_V1, "error");
                auditMsgs.remove(0);
                StringBuffer output_msg = new StringBuffer("");
                for (String mes : auditMsgs) {
                    output_msg.append(mes);
                }
                msgElement.setTextContent(output_msg.toString());
                responseElement.appendChild(msgElement);
            } else if ("warn".equals(status)) {
                // set a summary here, and set individual warnings for each DN
                String confirmation = messages.getMessage("dataEndpoint.success", new Object[] { importedCRFs, totalCRFs }, "Success", locale);
                resultElement.setTextContent(confirmation);
                responseElement.appendChild(resultElement);
                Element msgElement = document.createElementNS(NAMESPACE_URI_V1, "summary");
                msgElement.setTextContent(auditMsgs.get(1));
                responseElement.appendChild(msgElement);
                String listOfDns = auditMsgs.get(2);
                String[] splitListOfDns = listOfDns.split("---");
                for (String dn : splitListOfDns) {
                    Element warning = document.createElementNS(NAMESPACE_URI_V1, "warning");
                    warning.setTextContent(dn);
                    responseElement.appendChild(warning);
                }
                for (String s : skippedCRFMsgs) {
                    Element skipMsg = document.createElementNS(NAMESPACE_URI_V1, "warning");
                    skipMsg.setTextContent(s);
                    responseElement.appendChild(skipMsg);
                }
            } else {
                if (ruleActionMsgs != null && !ruleActionMsgs.isEmpty()) {
                    // if there is message from rule. Import data success with rule message
                    String confirmation = messages.getMessage("dataEndpoint.success", new Object[] { importedCRFs, totalCRFs }, "Success", locale);
                    resultElement.setTextContent(confirmation);
                    responseElement.appendChild(resultElement);
                    for (String s : ruleActionMsgs) {
                        Element ruleMsg = document.createElementNS(NAMESPACE_URI_V1, "rule_action_warning");
                        ruleMsg.setTextContent(s);
                        responseElement.appendChild(ruleMsg);
                    }
                } else {
                    // plain success no warnings
                    String confirmation = messages.getMessage("dataEndpoint.success", new Object[] { importedCRFs, totalCRFs }, "Success", locale);
                    resultElement.setTextContent(confirmation);
                    responseElement.appendChild(resultElement);
                }
                for (String s : skippedCRFMsgs) {
                    Element skipMsg = document.createElementNS(NAMESPACE_URI_V1, "warning");
                    skipMsg.setTextContent(s);
                    responseElement.appendChild(skipMsg);
                }
            }
        }

        return responseElement;
    }

    private static String node2String(Node node) {
        try {
            TransformerFactory tFactory = TransformerFactory.newInstance();

            Transformer transformer = tFactory.newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");

            StringWriter outStream = new StringWriter();
            DOMSource source = new DOMSource(node);
            StreamResult result = new StreamResult(outStream);
            transformer.transform(source, result);
            return outStream.getBuffer().toString();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    private String convertToErrorString(List<String> errorMessages) {
        StringBuilder result = new StringBuilder();
        for (String str : errorMessages) {
            result.append(str + " \n");
        }

        return result.toString();
    }

    public RuleSetServiceInterface getRuleSetService() {
        return ruleSetService;
    }

    public void setRuleSetService(RuleSetServiceInterface ruleSetService) {
        this.ruleSetService = ruleSetService;
    }

    public TransactionTemplate getTransactionTemplate() {
        return transactionTemplate;
    }

    public void setTransactionTemplate(TransactionTemplate transactionTemplate) {
        this.transactionTemplate = transactionTemplate;
    }

}
