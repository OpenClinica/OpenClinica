package org.akaza.openclinica.bean.rule;

import org.akaza.openclinica.exception.OpenClinicaSystemException;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Source;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

public class XmlSchemaValidationHelper {

    public void validateAgainstSchema(File xmlFile, File xsdFile) {
        try {
            // parse an XML document into a DOM tree
            DocumentBuilder parser = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            Document document = parser.parse(xmlFile);

            // create a SchemaFactory capable of understanding WXS schemas
            SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);

            // load a WXS schema, represented by a Schema instance
            Source schemaFile = new StreamSource(xsdFile);
            Schema schema = factory.newSchema(schemaFile);

            // create a Validator instance, which can be used to validate an
            // instance document
            Validator validator = schema.newValidator();

            // validate the DOM tree
            validator.validate(new DOMSource(document));

        } catch (FileNotFoundException ex) {
            throw new OpenClinicaSystemException("File was not found", ex.getCause());
        } catch (IOException ioe) {
            throw new OpenClinicaSystemException("IO Exception", ioe.getCause());
        } catch (SAXParseException spe) {
        	//spe.printStackTrace();
            throw new OpenClinicaSystemException("Line : " + spe.getLineNumber() + " - " + spe.getMessage(), spe.getCause());
        } catch (SAXException e) {
            throw new OpenClinicaSystemException(e.getMessage(), e.getCause());
        } catch (ParserConfigurationException pce) {
            throw new OpenClinicaSystemException(pce.getMessage(), pce.getCause());
        }
    }
}
