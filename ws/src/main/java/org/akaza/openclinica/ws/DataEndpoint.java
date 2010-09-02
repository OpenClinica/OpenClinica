package org.akaza.openclinica.ws;

import org.akaza.openclinica.bean.login.UserAccountBean;
import org.akaza.openclinica.bean.managestudy.StudyBean;
import org.akaza.openclinica.dao.core.CoreResources;
import org.akaza.openclinica.dao.login.UserAccountDAO;
import org.akaza.openclinica.dao.managestudy.StudyDAO;
import org.akaza.openclinica.i18n.util.ResourceBundleProvider;
import org.akaza.openclinica.web.crfdata.DataImportService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.MessageSource;
import org.springframework.ws.server.endpoint.annotation.Endpoint;
import org.springframework.ws.server.endpoint.annotation.PayloadRoot;
import org.springframework.ws.server.endpoint.annotation.XPathParam;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import javax.sql.DataSource;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

@Endpoint
public class DataEndpoint {

    protected final Logger logger = LoggerFactory.getLogger(getClass().getName());
    private final String NAMESPACE_URI_V1 = "http://openclinica.org/ws/data/v1";

    private final String ODM_HEADER_NAMESPACE = "<ODM xmlns=\"http://www.cdisc.org/ns/odm/v1.3\" targetNamespace=\"http://openclinica.org/ws/data/v1\" xmlns:OpenClinica=\"http://www.openclinica.org/ns/openclinica_odm/v1.3\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"http://www.cdisc.org/ns/odm/v1.3\">";
    private final DataSource dataSource;
    private final MessageSource messages;
    private final CoreResources coreResources;
    private final Locale locale;

    public DataEndpoint(DataSource dataSource, MessageSource messages, CoreResources coreResources) {
        this.dataSource = dataSource;
        this.messages = messages;
        this.coreResources = coreResources;
        
        this.locale = new Locale("en_US");
    }

    /**
     * if NAMESPACE_URI_V1:importDataRequest execute this method
     * 
     * @return
     * @throws Exception
     */
    @PayloadRoot(localPart = "importRequest", namespace = NAMESPACE_URI_V1)
    public Source importData(@XPathParam("//ODM") Element odmElement) throws Exception {

        ResourceBundleProvider.updateLocale(new Locale("en_US"));

        // logger.debug("rootElement=" + odmElement);
        logger.debug("rootElement=" + odmElement);

        String xml = null;
        StudyBean studyBean = null;
        UserAccountBean userBean = null;
        try {
            if (odmElement != null) {
                xml = node2String(odmElement);
                xml = xml.replaceAll("<ODM>", this.ODM_HEADER_NAMESPACE);
                Element clinicalDataNode = (Element) odmElement.getElementsByTagName("ClinicalData").item(0);
                studyBean = new StudyDAO(dataSource).findByOid(clinicalDataNode.getAttribute("StudyOID"));
                logger.debug("found study " + clinicalDataNode.getAttribute("StudyOID"));
                // userBean = (UserAccountBean) new UserAccountDAO(dataSource).findByPK(Integer.parseInt(clinicalDataNode.getAttribute("UserID")));
                userBean = getUserAccount();
            }

            // return fail messages iff we get proper error messages from the import, tbh
            if (odmElement != null) {
                // return new DOMSource(mapConfirmation(xml, studyBean, userBean, messages.getMessage("dataEndpoint.success", null, "Success", locale)));
                return new DOMSource(mapConfirmation(xml, studyBean, userBean));
            } else {
                // return new DOMSource(mapConfirmation(xml, studyBean, userBean, messages.getMessage("dataEndpoint.fail", null, "Success", locale)));
                return new DOMSource(mapFailMessage("Your XML is not well-formed."));
                // TODO something else?
            }
        } catch (NullPointerException npe) {
            return new DOMSource(mapFailMessage("Your XML is not well-formed."));
        }
        // return new DOMSource(mapConfirmation(xml, studyBean, userBean));
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
     * Create a default 'fail' response
     */
    private Element mapFailMessage(String message) throws Exception {
        DocumentBuilderFactory dbfac = DocumentBuilderFactory.newInstance();
        DocumentBuilder docBuilder = dbfac.newDocumentBuilder();
        Document document = docBuilder.newDocument();

        Element responseElement = document.createElementNS(NAMESPACE_URI_V1, "importDataResponse");
        Element resultElement = document.createElementNS(NAMESPACE_URI_V1, "result");
        
        String confirmation = messages.getMessage("dataEndpoint.fail", null, "Success", locale);
        resultElement.setTextContent(confirmation);
        responseElement.appendChild(resultElement);
        Element msgElement = document.createElementNS(NAMESPACE_URI_V1, "error");
        msgElement.setTextContent(message);
        responseElement.appendChild(msgElement);
        logger.debug("sending fail message " + message);
        return responseElement;
    }

    /**
     * Create Response
     * 
     * @param confirmation
     * @return
     * @throws Exception
     */
    private Element mapConfirmation(String xml, StudyBean studyBean, UserAccountBean userBean) throws Exception {

        DocumentBuilderFactory dbfac = DocumentBuilderFactory.newInstance();
        DocumentBuilder docBuilder = dbfac.newDocumentBuilder();
        Document document = docBuilder.newDocument();

        Element responseElement = document.createElementNS(NAMESPACE_URI_V1, "importDataResponse");
        Element resultElement = document.createElementNS(NAMESPACE_URI_V1, "result");
        
        List<String> auditMsgs = new ArrayList<String>();
        if (xml != null) {
            auditMsgs = new DataImportService().importData(dataSource, coreResources, studyBean, userBean, xml);
        } else {
            logger.debug("found null in xml");
        }

        if (auditMsgs != null) {
            String status = auditMsgs.get(0);
            if ("fail".equals(status)) {
                String confirmation = messages.getMessage("dataEndpoint.fail", null, "Success", locale);
                resultElement.setTextContent(confirmation);
                responseElement.appendChild(resultElement);
                Element msgElement = document.createElementNS(NAMESPACE_URI_V1, "error");
                msgElement.setTextContent(auditMsgs.get(1));
                responseElement.appendChild(msgElement);
            } else if ("warn".equals(status)){
                // set a summary here, and set individual warnings for each DN
                String confirmation = messages.getMessage("dataEndpoint.success", null, "Success", locale);
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
            } else {
                // plain success no warnings
                String confirmation = messages.getMessage("dataEndpoint.success", null, "Success", locale);
                resultElement.setTextContent(confirmation);
                responseElement.appendChild(resultElement);
            }
            // TODO set up a success + warnings status above? tbh 08/2010
            //            Element auditMsgsElement = document.createElementNS(NAMESPACE_URI_V1, "auditMessages");
            //            responseElement.appendChild(auditMsgsElement);
            //
            //            for (String msg : auditMsgs) {
            //                Element msgElement = document.createElementNS(NAMESPACE_URI_V1, "auditMessage");
            //                msgElement.setTextContent(msg);
            //                auditMsgsElement.appendChild(msgElement);
            //            }
        }

        return responseElement;
    }

    public static String node2String(Node node) {
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

}
