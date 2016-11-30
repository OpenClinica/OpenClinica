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
    protected final Logger logger = LoggerFactory.getLogger(getClass().getName());

    public QueryFormDecorator(Form form) {
        super(form);
    }

    @Override
    public String decorate() throws Exception {
        String xform = "";
        try {
            xform = applyQueryFormDecorator(form.decorate());
        } catch (Exception e) {
            logger.error(e.getMessage());
            logger.error(ExceptionUtils.getStackTrace(e));
            throw new Exception(e);
        }
        return xform;
    }

    private String applyQueryFormDecorator(String xform) throws Exception {
        InputStream is = new ByteArrayInputStream(xform.getBytes());
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(false);
        Document doc = factory.newDocumentBuilder().parse(is);
        Element html = doc.getDocumentElement();

        html.setAttributeNS("http://www.w3.org/2000/xmlns/", "xmlns:enk", "http://enketo.org/xforms");
        NamedNodeMap attribs = html.getAttributes();

        XPathFactory xPathfactory = XPathFactory.newInstance();
        XPath xpath = xPathfactory.newXPath();
        XPathExpression expr = null;
        expr = xpath.compile("/html/head/model");
        Node modelNode = (Node) expr.evaluate(doc, XPathConstants.NODE);
        NodeList modelChildNodes = modelNode.getChildNodes();
        int modelChildLength = modelChildNodes.getLength();
        List<String> nodesetAttrs = new ArrayList<>();
        for (int i = 0; i < modelChildLength; i++) {
            Node modelChildNode = modelChildNodes.item(i);
            if (modelChildNode.getNodeType() != Node.TEXT_NODE && "bind".equals(modelChildNode.getNodeName())) {
                NamedNodeMap attr = modelChildNode.getAttributes();
                Node nodesetAttr = attr.getNamedItem("nodeset");
                Node relevantAttr = attr.getNamedItem("relevant");
                Node readonlyAttr = attr.getNamedItem("readonly");
                String str = nodesetAttr.getNodeValue();
                Element bind = doc.createElement("bind");

                if (readonlyAttr == null || (readonlyAttr != null && !readonlyAttr.getNodeValue().equalsIgnoreCase("true()"))) {
                    if (relevantAttr != null) {
                        bind.setAttribute("relevant", relevantAttr.getNodeValue());
                    }
                    bind.setAttribute("nodeset", nodesetAttr.getNodeValue() + "_comment");
                    bind.setAttribute("enk:for", str);
                    bind.setAttribute("type", "string");
                    modelNode.appendChild(bind);
                    modelNode.appendChild(doc.createTextNode("\n"));
                } else {
                    nodesetAttrs.add(nodesetAttr.getNodeValue());
                }
            }
        }
        for (int i = 0; i < modelChildLength; i++) {
            Node modelChildNode = modelChildNodes.item(i);
            if (modelChildNode.getNodeType() != Node.TEXT_NODE && ("instance".equals(modelChildNode.getNodeName())
                    && modelChildNode.getAttributes().getNamedItem("id") == null && modelChildNode.getFirstChild() != null)) {
                Node icrfNode = modelChildNode.getFirstChild().getNextSibling();
                String crfPath = "/" + icrfNode.getNodeName();
                NodeList igroupNodes = icrfNode.getChildNodes();
                int igroupNodesLength = igroupNodes.getLength();
                for (int m = 0; m < igroupNodesLength; m++) {
                    Node igroupNode = igroupNodes.item(m).getNextSibling();
                    if (igroupNode != null && igroupNode.getNodeType() != Node.TEXT_NODE && !igroupNode.getNodeName().equals("meta")) {
                        String groupPath = crfPath + "/" + igroupNode.getNodeName();
                        NodeList icontexts = igroupNode.getChildNodes();
                        int icontextsLength = icontexts.getLength();
                        for (int j = 0; j < icontextsLength; j++) {
                            Node icontextNode = icontexts.item(j);
                            String itemPath = groupPath + "/" + icontextNode.getNodeName();
                            if (icontextNode.getNodeType() != Node.TEXT_NODE && !nodesetAttrs.contains(itemPath)) {
                                Element newChildNode = doc.createElement(icontextNode.getNodeName() + "_comment");
                                igroupNode.appendChild(newChildNode);
                                igroupNode.appendChild(doc.createTextNode("\n"));
                            }
                        }
                    }
                }
            }
        }
        expr = xpath.compile("/html/body");

        Node bodyNode = (Node) expr.evaluate(doc, XPathConstants.NODE);
        NodeList bodyChildNodes = bodyNode.getChildNodes();
        int bodyChildLength = bodyChildNodes.getLength();
        for (int b = 0; b < bodyChildLength; b++) {
            Node bodyChildNode = bodyChildNodes.item(b);

            if (bodyChildNode.getNodeType() != Node.TEXT_NODE && ("group".equals(bodyChildNode.getNodeName()))) {
                Node groupNode = bodyChildNode;
                NodeList groupChildNodes = groupNode.getChildNodes();
                int groupChildLength = groupChildNodes.getLength();
                for (int c = 0; c < groupChildLength; c++) {
                    Node groupChildNode = groupChildNodes.item(c);

                    if (groupChildNode.getNodeType() != Node.TEXT_NODE && ("repeat".equals(groupChildNode.getNodeName()))) {
                        Node repeatNode = groupChildNode;
                        NodeList repeatChildNodes = repeatNode.getChildNodes();
                        int repeatChildLegth = repeatChildNodes.getLength();
                        for (int j = 0; j < repeatChildLegth; j++) {
                            Node repeatChildNode = repeatChildNodes.item(j);
                            if (repeatChildNode.getNodeType() != Node.TEXT_NODE && repeatChildNode.getAttributes() != null
                                    && repeatChildNode.getAttributes().getNamedItem("ref") != null
                                    && !nodesetAttrs.contains(repeatChildNode.getAttributes().getNamedItem("ref").getNodeValue())
                                    && ("input".equals(repeatChildNode.getNodeName()) || "select1".equals(repeatChildNode.getNodeName())
                                            || "select".equals(repeatChildNode.getNodeName()) || "upload".equals(repeatChildNode.getNodeName()))) {
                                Element newChildNode = createChildElement(doc, repeatChildNode, repeatChildNode.getNodeName());
                                repeatNode.appendChild(newChildNode);
                                repeatNode.appendChild(doc.createTextNode("\n"));
                            }
                        }
                    }

                    if (groupChildNode.getNodeType() != Node.TEXT_NODE && groupChildNode.getAttributes() != null
                            && groupChildNode.getAttributes().getNamedItem("ref") != null
                            && !nodesetAttrs.contains(groupChildNode.getAttributes().getNamedItem("ref").getNodeValue())
                            && ("input".equals(groupChildNode.getNodeName()) || "select1".equals(groupChildNode.getNodeName())
                                    || "select".equals(groupChildNode.getNodeName()) || "upload".equals(groupChildNode.getNodeName()))) {
                        Element newChildNode = createChildElement(doc, groupChildNode, groupChildNode.getNodeName());
                        groupNode.appendChild(newChildNode);
                        groupNode.appendChild(doc.createTextNode("\n"));
                    }
                }
            }
            if (bodyChildNode.getNodeType() != Node.TEXT_NODE && bodyChildNode.getAttributes() != null
                    && bodyChildNode.getAttributes().getNamedItem("ref") != null
                    && !nodesetAttrs.contains(bodyChildNode.getAttributes().getNamedItem("ref").getNodeValue())
                    && ("input".equals(bodyChildNode.getNodeName()) || "select1".equals(bodyChildNode.getNodeName())
                            || "select".equals(bodyChildNode.getNodeName()) || "upload".equals(bodyChildNode.getNodeName()))) {
                Element newChildNode = createChildElement(doc, bodyChildNode, bodyChildNode.getNodeName());
                bodyNode.appendChild(newChildNode);
                bodyNode.appendChild(doc.createTextNode("\n"));
            }
        }

        Element newInsanceNode = createInstanceElement(doc);
        modelNode.appendChild(newInsanceNode);
        modelNode.appendChild(doc.createTextNode("\n"));

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

    private Element createInstanceElement(Document doc) {
        Element instance = doc.createElement("instance");
        instance.setAttribute("id", "_users");
        instance.setAttribute("src", "jr://file-csv/users.xml");
        return instance;
    }

    private Element createChildElement(Document doc, Node childNode, String inputType) {

        NamedNodeMap attr = childNode.getAttributes();
        Node refAttr = attr.getNamedItem("ref");
        Element input = doc.createElement("input");

        input.setAttribute("appearance", "dn w1");
        input.setAttribute("ref", refAttr.getNodeValue() + "_comment");

        Element label = doc.createElement("label");
        label.appendChild(doc.createTextNode("Comment:"));
        input.appendChild(label);
        return input;
    }

}
