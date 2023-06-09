package org.akaza.openclinica.core;

import org.castor.xml.XMLConfiguration;
import org.exolab.castor.xml.XMLContext;

public class XMLContextFactory {

    public static XMLContext getXmlContext(){
        XMLContext xmlContext = new XMLContext();
        xmlContext.setProperty(XMLConfiguration.NAMESPACES, "true");
        xmlContext.setProperty(org.castor.xml.XMLProperties.SERIALIZER_FACTORY,
                org.exolab.castor.xml.XercesXMLSerializerFactory.class.getName());
        return xmlContext;
    }

    public static XMLContext getXmlContextNoNamespace(){
        XMLContext xmlContext = new XMLContext();
        xmlContext.setProperty(org.castor.xml.XMLProperties.SERIALIZER_FACTORY,
                org.exolab.castor.xml.XercesXMLSerializerFactory.class.getName());
        return xmlContext;
    }

}
