package org.akaza.openclinica.domain.xform;

import java.util.List;
import java.util.Set;

import org.springframework.validation.Errors;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class XformParserHelper {
    public static final String ENKETO_ORDINAL = "enk:ordinal";
    public static final String ENKETO_LAST_USED_ORDINAL = "enk:last-used-ordinal";
    public static final String FS_QUERY_ATTRIBUTE = "oc:queryParent";
    public static final String QUERY_SUFFIX = "_comment";
    public static final String JR_TEMPLATE = "jr:template";

    public List<String> instanceItemPaths(Node outerNode, List<String> list, String path, Errors errors) {
        int outerNodeLength = outerNode.getChildNodes().getLength();
        if (outerNodeLength == 0)
            list.add(path);
        for (int b = 0; b < outerNodeLength; b++) {
            Node node = outerNode.getChildNodes().item(b);
            if (node instanceof Element && !node.getNodeName().equals("formhub") && !node.getNodeName().equals("meta")) {
                if (node.hasChildNodes() && node.getChildNodes().getLength() != 1) {
                    list = instanceItemPaths(node, list, path + "/" + node.getNodeName(), errors);
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
                if (node.getAttributes() != null && node.getAttributes().getNamedItem(ENKETO_ORDINAL) != null) {
                    set.add(node);
                } else {
                    set = instanceEnketoAttr(node, set);
                }
            }
        }
        return set;
    }

}
