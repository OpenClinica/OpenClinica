package org.akaza.openclinica.ws;

import org.akaza.openclinica.bean.login.UserAccountBean;
import org.akaza.openclinica.bean.managestudy.StudyBean;
import org.akaza.openclinica.dao.login.UserAccountDAO;
import org.akaza.openclinica.dao.managestudy.StudyDAO;
import org.akaza.openclinica.i18n.util.ResourceBundleProvider;
import org.akaza.openclinica.web.crfdata.DataImportService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.MessageSource;
import org.springframework.ws.server.endpoint.annotation.PayloadRoot;
import org.springframework.ws.server.endpoint.annotation.XPathParam;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import java.io.StringWriter;
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

public class DataEndpoint {

    protected final Logger logger = LoggerFactory.getLogger(getClass().getName());
    private final String NAMESPACE_URI_V1 = "http://openclinica.org/ws/data/v1";

    private final DataSource dataSource;
    private final MessageSource messages;
    private final Locale locale;

    public DataEndpoint(DataSource dataSource, MessageSource messages) {
        this.dataSource = dataSource;
        this.messages = messages;
        this.locale = new Locale("en_US");
    }

    /**
     * if NAMESPACE_URI_V1:importDataRequest execute this method
     * 
     * @return
     * @throws Exception
     */
    @PayloadRoot(localPart = "importDataRequest", namespace = NAMESPACE_URI_V1)
    public Source importData(@XPathParam("//ODM") Element odmElement) throws Exception {

        ResourceBundleProvider.updateLocale(new Locale("en_US"));

        logger.debug("rootElement=" + odmElement);

        String xml = null;
        StudyBean studyBean = null;
        UserAccountBean userBean = null;

        if (odmElement != null) {
            xml = node2String(odmElement);
            Element clinicalDataNode = (Element) odmElement.getElementsByTagName("ClinicalData").item(0);
            studyBean = new StudyDAO(dataSource).findByOid(clinicalDataNode.getAttribute("StudyOID"));
            userBean = (UserAccountBean) new UserAccountDAO(dataSource).findByPK(Integer.parseInt(clinicalDataNode.getAttribute("UserID")));
        }

        if (odmElement != null)
            return new DOMSource(mapConfirmation(xml, studyBean, userBean, messages.getMessage("dataEndpoint.success", null, "Success", locale)));
        else
            return new DOMSource(mapConfirmation(xml, studyBean, userBean, messages.getMessage("dataEndpoint.fail", null, "Success", locale)));
    }

    /**
     * Create Response
     * 
     * @param confirmation
     * @return
     * @throws Exception
     */
    private Element mapConfirmation(String xml, StudyBean studyBean, UserAccountBean userBean, String confirmation) throws Exception {

        DocumentBuilderFactory dbfac = DocumentBuilderFactory.newInstance();
        DocumentBuilder docBuilder = dbfac.newDocumentBuilder();
        Document document = docBuilder.newDocument();

        Element responseElement = document.createElementNS(NAMESPACE_URI_V1, "importDataResponse");
        Element resultElement = document.createElementNS(NAMESPACE_URI_V1, "result");
        resultElement.setTextContent(confirmation);
        responseElement.appendChild(resultElement);

        List<String> auditMsgs = new DataImportService().importData(dataSource, studyBean, userBean, xml);

        if (auditMsgs != null) {
            Element auditMsgsElement = document.createElementNS(NAMESPACE_URI_V1, "auditMessages");
            responseElement.appendChild(auditMsgsElement);

            for (String msg : auditMsgs) {
                Element msgElement = document.createElementNS(NAMESPACE_URI_V1, "auditMessage");
                msgElement.setTextContent(msg);
                auditMsgsElement.appendChild(msgElement);
            }
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
