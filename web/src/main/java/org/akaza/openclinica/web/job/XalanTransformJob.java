/*
 * LibreClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: https://libreclinica.org/license
 * LibreClinica, copyright (C) 2020
 */
package org.akaza.openclinica.web.job;

import org.akaza.openclinica.i18n.util.ResourceBundleProvider;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.context.ApplicationContext;
import org.springframework.scheduling.quartz.JobDetailFactoryBean;
import org.springframework.scheduling.quartz.QuartzJobBean;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Locale;
import java.util.ResourceBundle;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

/**
 * Xalan Transform Job, an XSLT transform job using the Xalan classes
 * @author thickerson
 *
 */
public class XalanTransformJob extends QuartzJobBean {

    private static final Logger logger = LoggerFactory.getLogger(XalanTransformJob.class);

    public static final String DATASET_ID = "dsId";
    public static final String EMAIL = "contactEmail";
    public static final String USER_ID = "user_id";
    public static final String XSL_FILE_PATH = "xslFilePath";
    public static final String XML_FILE_PATH = "xmlFilePath";
    public static final String SQL_FILE_PATH = "sqlFilePath";
    
    protected void executeInternal(JobExecutionContext context) throws JobExecutionException {
        // need to generate a Locale so that user beans and other things will
        // generate normally
        // TODO make dynamic?
        Locale locale = new Locale("en-US");
        ResourceBundleProvider.updateLocale(locale);
        ResourceBundle pageMessages = ResourceBundleProvider.getPageMessagesBundle();
        JobDataMap dataMap = context.getMergedJobDataMap();
        // get the file information from the job
        String alertEmail = dataMap.getString(EMAIL);
        try {
            TransformerFactory tFactory = TransformerFactory.newInstance();
            
            // Use the TransformerFactory to instantiate a Transformer that will work with  
            // the stylesheet you specify. This method call also processes the stylesheet
            // into a compiled Templates object.
            java.io.InputStream in = new java.io.FileInputStream(dataMap.getString(XSL_FILE_PATH));
            // tFactory.setAttribute("use-classpath", Boolean.TRUE);
            // tFactory.setErrorListener(new ListingErrorHandler());
            Transformer transformer = tFactory.newTransformer(new StreamSource(in));

            // Use the Transformer to apply the associated Templates object to an XML document
            // (foo.xml) and write the output to a file (foo.out).
          //  System.out.println("--> job starting: ");
            final long start = System.currentTimeMillis();
            transformer.transform(new StreamSource(dataMap.getString(XML_FILE_PATH)), 
                    new StreamResult(new FileOutputStream(dataMap.getString(SQL_FILE_PATH))));
            final long done = System.currentTimeMillis() - start;
           // System.out.println("--> job completed in " + done + " ms");
        } catch (TransformerConfigurationException e) {
            logger.error("XML Transformer was not configured properly: ", e);
        } catch (FileNotFoundException e) {
            logger.error("XLS file not found: ", e);
        } catch (TransformerFactoryConfigurationError e) {
            logger.error("TransformerFactory was not configured properly: ", e);
        } catch (TransformerException e) {
            logger.error("Error while transforming: ", e);
        }
    }

}
