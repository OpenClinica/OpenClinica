package org.akaza.openclinica.ws.logic;

import org.akaza.openclinica.ws.bean.RegisterSubjectBean;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class DomParsingService {

    
    
    public DomParsingService() {
        
    }
    
    
    
    /**
     * get element value, generic parser for getting the value of an attribute in an XML line.
     * 
     * @param subject, our XML node.
     * @param namespace, our namespace, typically will be http://clinicalconnector.nci.nih.gov, but could also be ISO 21090
     * @param xmlLine, the xml line we want to parse
     * @param attrName, the attribute we want to grab, could be 'value' or 'code' or something generic like that
     * @return
     */
    public String getElementValue(Node subject, String namespace, String xmlLine, String attrName) {
        String ret = "";
        Element subjectElement = (Element) subject;
        NodeList xmlNode = subjectElement.getElementsByTagNameNS(namespace, xmlLine);
        Node xmlNodeValue = xmlNode.item(0);
        if (xmlNodeValue.hasAttributes()) {
            NamedNodeMap nodeMap = xmlNodeValue.getAttributes();
            Node nodeValue = nodeMap.getNamedItem(attrName);
            ret = nodeValue.getNodeValue();
        }
        return ret;
        
    }
}
