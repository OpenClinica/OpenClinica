package org.akaza.openclinica.domain.xform;

import java.util.List;
import java.util.Set;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

public class XformParserHelper {

    public void addCommentElementInInstance(Document doc, Node crfNode, String path, List<String> nodesetAttrs) {
        int outerNodeLength = crfNode.getChildNodes().getLength();
        if (outerNodeLength == 0 && !nodesetAttrs.contains(path + "/" + crfNode.getNodeName())) {
            Element newChildNode = doc.createElement(crfNode.getNodeName() + "_comment");
            crfNode.getParentNode().appendChild(newChildNode);
        }

        for (int b = 0; b < outerNodeLength; b++) {
            Node node = crfNode.getChildNodes().item(b);
            if (node instanceof Element && !node.getNodeName().equals("formhub")) {
                if (node.hasChildNodes() && !(node.getChildNodes().getLength() == 1 && !(node.getFirstChild() instanceof Element))) {
                    addCommentElementInInstance(doc, node, path + "/" + node.getNodeName(), nodesetAttrs);
                } else {
                    if (!nodesetAttrs.contains(path + "/" + node.getNodeName())) {
                        Element newChildNode = doc.createElement(node.getNodeName() + "_comment");
                        node.getParentNode().appendChild(newChildNode);
                    }
                }
            }
        }
    }

    private Element createCommentElementInBody(Document doc, Node childNode, String inputType) {

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

    public void iterateUserControlsInBodyAndAddCommentElement(Document doc, Node bodyNode, List<String> nodesetAttrs) {
        int bodyNodeLength = bodyNode.getChildNodes().getLength();
        if (bodyNodeLength == 0 && !nodesetAttrs.contains(bodyNode.getAttributes().getNamedItem("ref").getNodeValue())) {
            Element newChildNode = createCommentElementInBody(doc, bodyNode, bodyNode.getNodeName());
            bodyNode.appendChild(newChildNode);
        }
        for (int b = 0; b < bodyNodeLength; b++) {
            Node node = bodyNode.getChildNodes().item(b);
            if (node instanceof Element) {
                if (("repeat".equals(node.getNodeName()) || "group".equals(node.getNodeName())) && node.hasChildNodes()) {
                    iterateUserControlsInBodyAndAddCommentElement(doc, node, nodesetAttrs);
                } else {
                    if (node instanceof Element && node.getAttributes() != null && node.getAttributes().getNamedItem("ref") != null
                            && !nodesetAttrs.contains(node.getAttributes().getNamedItem("ref").getNodeValue()) && ("input".equals(node.getNodeName())
                                    || "select1".equals(node.getNodeName()) || "select".equals(node.getNodeName()) || "upload".equals(node.getNodeName()))) {
                        Element newChildNode = createCommentElementInBody(doc, node, node.getNodeName());
                        node.getParentNode().appendChild(newChildNode);
                    }

                }
            }
        }
    }

    public List<String> instanceItemPaths(Node outerNode, List<String> list, String path) {
        int outerNodeLength = outerNode.getChildNodes().getLength();
        if (outerNodeLength == 0)
            list.add(path);
        for (int b = 0; b < outerNodeLength; b++) {
            Node node = outerNode.getChildNodes().item(b);
            if (node instanceof Element) {
                if (node.hasChildNodes()) {
                    list = instanceItemPaths(node, list, path + "/" + node.getNodeName());
                } else {
                    list.add(path + "/" + node.getNodeName());
                }
            }
        }
        return list;
    }

    public Set<Node> instanceItemNodes(Node outerNode, Set<Node> set) {
        int outerNodeLength = outerNode.getChildNodes().getLength();
        if (outerNodeLength == 0)
            set.add(outerNode);
        for (int b = 0; b < outerNodeLength; b++) {
            Node node = outerNode.getChildNodes().item(b);
            if (node instanceof Element && !node.getNodeName().equals("formhub") && !node.getNodeName().equals("meta")) {
                if (node.hasChildNodes() && !(node.getChildNodes().getLength() == 1 && !(node.getFirstChild() instanceof Element))) {
                    set = instanceItemNodes(node, set);
                } else {
                    set.add(node);
                }
            }
        }
        return set;
    }

    public Set<Node> instanceEnketoAttr(Node outerNode, Set<Node> set) {
        int outerNodeLength = outerNode.getChildNodes().getLength();
        if (outerNodeLength == 0)
            set.add(outerNode);
        for (int b = 0; b < outerNodeLength; b++) {
            Node node = outerNode.getChildNodes().item(b);
            if (node instanceof Element && !node.getNodeName().equals("formhub") && !node.getNodeName().equals("meta")) {
                if (node.getAttributes() != null && node.getAttributes().getNamedItem("enk:ordinal") != null) {
                    set.add(node);
                } else {
                    set = instanceEnketoAttr(node, set);
                }
            }
        }
        return set;
    }

    public List<String> bodyRepeatNodePaths(Node outerNode, List<String> list) {
        int outerNodeLength = outerNode.getChildNodes().getLength();
        for (int b = 0; b < outerNodeLength; b++) {
            Node node = outerNode.getChildNodes().item(b);
            if (node instanceof Element) {
                if (node.getNodeName().equals("repeat")) {
                    Node repeatNode = node.getAttributes().getNamedItem("nodeset");
                    list.add(repeatNode.getNodeValue());
                }
                if (node.hasChildNodes()) {
                    list = bodyRepeatNodePaths(node, list);
                }
            }
        }
        return list;
    }

    public Set<Node> bodyRepeatNodes(Node outerNode, Set<Node> set) {
        int outerNodeLength = outerNode.getChildNodes().getLength();
        for (int b = 0; b < outerNodeLength; b++) {
            Node node = outerNode.getChildNodes().item(b);
            if (node instanceof Element && !node.getNodeName().equals("formhub") && !node.getNodeName().equals("meta")) {
                if (node.getNodeName().equals("repeat")) {
                    set.add(node);
                }
                if (node.hasChildNodes()) {
                    set = bodyRepeatNodes(node, set);
                }
            }
        }
        return set;
    }

}
