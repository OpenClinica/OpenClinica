package org.akaza.openclinica.web.pform.formlist;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Singleton;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;

import org.akaza.openclinica.domain.xform.XformParserHelper;
import org.akaza.openclinica.web.pform.OpenRosaServices;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

@Singleton
public class QueryFormDecorator extends FormDecorator {
    public static final String QUERY = "-query";
    public static final String COMMENT = "_comment";

    protected final Logger logger = LoggerFactory.getLogger(getClass().getName());

    public QueryFormDecorator(Form form) {
        super(form);
    }

    @Override
    public String decorate(XformParserHelper xformParserHelper) throws Exception {
        String xform = "";
        try {
            xform = applyQueryFormDecorator(form.decorate(), xformParserHelper);
        } catch (Exception e) {
            logger.error(e.getMessage());
            logger.error(ExceptionUtils.getStackTrace(e));
            throw new Exception(e);
        }
        return xform;
    }

    private String applyQueryFormDecorator(String xform, XformParserHelper xformParserHelper) throws Exception {
        InputStream is = new ByteArrayInputStream(xform.getBytes());
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(false);
        Document doc = factory.newDocumentBuilder().parse(is);
        Element html = doc.getDocumentElement();

        html.setAttributeNS("http://www.w3.org/2000/xmlns/", "xmlns:enk", "http://enketo.org/xforms");
        NamedNodeMap attribs = html.getAttributes();

        // MODEL Elements
        XPathFactory xPathfactory = XPathFactory.newInstance();
        XPath xpath = xPathfactory.newXPath();
        XPathExpression expr = null;
        expr = xpath.compile("/html/head/model");
        Node modelNode = (Node) expr.evaluate(doc, XPathConstants.NODE);
        NodeList modelChildNodes = modelNode.getChildNodes();
        int modelChildLength = modelChildNodes.getLength();
        List<String> nodesetAttrs = new ArrayList<>();

        // Iterate Model to locate BIND elements
        for (int i = 0; i < modelChildLength; i++) {
            Node modelChildNode = modelChildNodes.item(i);
            if ("bind".equals(modelChildNode.getNodeName())) {
                NamedNodeMap attr = modelChildNode.getAttributes();
                Node nodesetAttr = attr.getNamedItem("nodeset");
                Node relevantAttr = attr.getNamedItem("relevant");
                Node readonlyAttr = attr.getNamedItem("readonly");
                Node requiredAttr = attr.getNamedItem("required");
                Node constraintAttr = attr.getNamedItem("constraint");
                String nodeValue = nodesetAttr.getNodeValue() + COMMENT;

                if (constraintAttr != null) {
                    String constraintValue = constraintAttr.getNodeValue() + " or " + nodeValue + " !='' ";
                    constraintAttr.setNodeValue(constraintValue);
                }

                if (requiredAttr != null && requiredAttr.getNodeValue().equalsIgnoreCase("true()")) {
                    String requiredValue = nodeValue + " ='' ";
                    requiredAttr.setNodeValue(requiredValue);
                }

                String str = nodesetAttr.getNodeValue();
                Element bind = doc.createElement("bind");

                if (readonlyAttr == null || (readonlyAttr != null && !readonlyAttr.getNodeValue().equalsIgnoreCase("true()"))) {
                    if (relevantAttr != null) {
                        bind.setAttribute("relevant", relevantAttr.getNodeValue());
                    }
                    bind.setAttribute("nodeset", nodesetAttr.getNodeValue() + COMMENT);
                    bind.setAttribute("enk:for", str);
                    bind.setAttribute("type", "string");
                    modelNode.appendChild(bind);
                } else {
                    nodesetAttrs.add(nodesetAttr.getNodeValue());
                }
            }
        }

        // Iterate Model to locate INSTANCE elements
        for (int i = 0; i < modelChildLength; i++) {
            Node modelChildNode = modelChildNodes.item(i);
            if ("instance".equals(modelChildNode.getNodeName()) && modelChildNode.getAttributes().getNamedItem("id") == null
                    && modelChildNode.getFirstChild() != null) {
                Node crfNode = modelChildNode.getFirstChild().getNextSibling();
                String crfPath = "/" + crfNode.getNodeName();
                xformParserHelper.addCommentElementInInstance(doc, crfNode, crfPath, nodesetAttrs);
            }
        }

        // BODY Element
        expr = xpath.compile("/html/body");

        // Iterate Body Nodes
        Node bodyNode = (Node) expr.evaluate(doc, XPathConstants.NODE);
        xformParserHelper.iterateUserControlsInBodyAndAddCommentElement(doc, bodyNode, nodesetAttrs);

        // Add Users instance
        Element newInsanceNode = createUsersInstanceElement(doc);
        modelNode.appendChild(newInsanceNode);

        TransformerFactory transformFactory = TransformerFactory.newInstance();
        Transformer transformer = transformFactory.newTransformer();
        transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
        transformer.setOutputProperty(OutputKeys.INDENT, "no");
        transformer.setOutputProperty(OutputKeys.METHOD, "xml");
        StringWriter writer = new StringWriter();
        StreamResult result = new StreamResult(writer);
        DOMSource source = new DOMSource(doc);
        transformer.transform(source, result);
        String modifiedXform = writer.toString();
        OpenRosaServices openRosaServices = new OpenRosaServices();
        modifiedXform = openRosaServices.applyXformAttributes(modifiedXform, attribs);
        logger.debug("Finalized xform source: " + modifiedXform);
        return modifiedXform;
    }

    private Element createUsersInstanceElement(Document doc) {
        Element instance = doc.createElement("instance");
        instance.setAttribute("id", "_users");
        instance.setAttribute("src", "jr://file-csv/users.xml");
        return instance;
    }

}
