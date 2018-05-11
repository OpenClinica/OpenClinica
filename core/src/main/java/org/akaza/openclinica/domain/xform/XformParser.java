package org.akaza.openclinica.domain.xform;

import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;

import javax.sql.DataSource;

import org.akaza.openclinica.dao.core.CoreResources;
import org.akaza.openclinica.domain.xform.dto.Html;
import org.exolab.castor.mapping.Mapping;
import org.exolab.castor.xml.Marshaller;
import org.exolab.castor.xml.Unmarshaller;
import org.exolab.castor.xml.XMLContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class XformParser {
    private XMLContext xmlContext = null;
    private DataSource dataSource = null;
    protected final Logger log = LoggerFactory.getLogger(XformParser.class);
    private CoreResources coreResources;

    /*
     * public XformParser() throws Exception { try { xmlContext = new XMLContext(); Mapping mapping =
     * xmlContext.createMapping(); mapping.loadMapping(coreResources.getURL("openRosaXFormMapping.xml"));
     * xmlContext.addMapping(mapping); } catch (Exception e) { log.error(e.getMessage());
     * log.error(ExceptionUtils.getStackTrace(e)); throw new Exception(e); } }
     */
    public String marshall(Html html) throws Exception {
        StringWriter writer = new StringWriter();

        xmlContext = new XMLContext();
        Mapping mapping = xmlContext.createMapping();
        mapping.loadMapping(coreResources.getURL("xformMapping.xml"));
        xmlContext.addMapping(mapping);

        Marshaller marshaller = xmlContext.createMarshaller();
        marshaller.setNamespaceMapping("h", "http://www.w3.org/1999/xhtml");
        marshaller.setNamespaceMapping("jr", "http://openrosa.org/javarosa");
        marshaller.setNamespaceMapping("xsd", "http://www.w3.org/2001/XMLSchema");
        marshaller.setNamespaceMapping("ev", "http://www.w3.org/2001/xml-events");
        marshaller.setNamespaceMapping("", "http://www.w3.org/2002/xforms");
        marshaller.setNamespaceMapping("enk", "http://enketo.org/xforms");
        marshaller.setNamespaceMapping("oc", "http://openclinica.org/xforms");

        marshaller.setWriter(writer);
        marshaller.marshal(html);
        String xform = writer.toString();
        return xform;
    }

    public Html unMarshall(String xml) throws Exception {

        xmlContext = new XMLContext();
        Mapping mapping = xmlContext.createMapping();
        mapping.loadMapping(coreResources.getURL("xformMapping.xml"));
        xmlContext.addMapping(mapping);

        // XML to Object
        Reader reader = new StringReader(xml);
        Unmarshaller unmarshaller = xmlContext.createUnmarshaller();
        unmarshaller.setClass(Html.class);
        unmarshaller.setWhitespacePreserve(false);
        Html html = (Html) unmarshaller.unmarshal(reader);
        reader.close();
        return html;
    }

    public XMLContext getXmlContext() {
        return xmlContext;
    }

    public void setXmlContext(XMLContext xmlContext) {
        this.xmlContext = xmlContext;
    }

    public DataSource getDataSource() {
        return dataSource;
    }

    public void setDataSource(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public CoreResources getCoreResources() {
        return coreResources;
    }

    public void setCoreResources(CoreResources coreResources) {
        this.coreResources = coreResources;
    }

}
