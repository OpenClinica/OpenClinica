package org.akaza.openclinica.control.submit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

/*
 * A simple xml parser based on SAX. This currently supports
 * flat data.
 * @author Krikor Krumlian
 */
public class XmlParser extends DefaultHandler {

    protected final Logger logger = LoggerFactory.getLogger(getClass().getName());
    List<HashMap<String, String>> allCrfData;
    private HashMap<String, String> crfData;
    private String tempVal;

    public XmlParser() {
        allCrfData = new ArrayList<HashMap<String, String>>();
    }

    public List<HashMap<String, String>> getData(File f) {
        parseDocument(f);
        printData();
        return allCrfData;
    }

    private void parseDocument(File f) {

        // get a factory
        SAXParserFactory spf = SAXParserFactory.newInstance();
        try {

            // get a new instance of parser
            SAXParser sp = spf.newSAXParser();

            // parse the file and also register this class for call backs
            sp.parse(f, this);

        } catch (SAXException se) {
            se.printStackTrace();
        } catch (ParserConfigurationException pce) {
            pce.printStackTrace();
        } catch (IOException ie) {
            ie.printStackTrace();
        }
    }

    /**
     * Iterate through the list and print the contents
     */
    private void printData() {

        logger.info("No of Records '" + allCrfData.size() + "'.");

        Iterator it = allCrfData.iterator();
        while (it.hasNext()) {
            logger.info(it.next().toString());
        }
    }

    // Event Handlers
    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        // reset
        tempVal = "";
        if (qName.equalsIgnoreCase("crf_data")) {
            crfData = new HashMap<String, String>();

        }
    }

    @Override
    public void characters(char[] ch, int start, int length) throws SAXException {
        tempVal = new String(ch, start, length);
    }

    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {

        if (qName.equalsIgnoreCase("crf_data")) {
            // add it to the list
            allCrfData.add(crfData);
        } else if (qName.equalsIgnoreCase("data")) {
            // do nothing
        } else {
            crfData.put(qName, tempVal);
        }
    }

}
