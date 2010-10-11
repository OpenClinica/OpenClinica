package org.akaza.openclinica.bean.service;

import org.akaza.openclinica.dao.core.CoreResources;
import org.apache.fop.apps.FOUserAgent;
import org.apache.fop.apps.Fop;
import org.apache.fop.apps.FOPException;
import org.apache.fop.apps.FopFactory;
import org.apache.fop.apps.FormattingResults;
import org.apache.fop.apps.MimeConstants;
import org.apache.fop.apps.PageSequenceResults;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.OutputStream;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.Source;
import javax.xml.transform.Result;
import javax.xml.transform.stream.StreamSource;
import javax.xml.transform.sax.SAXResult;
/**
 * PdfProcessingFunction, a post-processing function for Extract Data
 * By Tom Hickerson, 09/2010
 * @author thickerson
 *
 */
public class PdfProcessingFunction extends ProcessingFunction {

    private FopFactory fopFactory = FopFactory.newInstance();
    
    public PdfProcessingFunction() {
        fileType = "pdf";
    }
    
    /*
     * The run() method.  Note that we will assume that all variables (i.e. file
     * paths) are set here.
     * 
     * Running this will open a file stream, perform a transform with the *.fo 
     * file (note, does not necessarily have to have a *.fo suffix) and then 
     * return a success/fail message.
     * (non-Javadoc)
     * @see org.akaza.openclinica.bean.service.ProcessingInterface#run()
     */
    public ProcessingResultType run() {
        
        OutputStream out = null;    
        try
        {
            FOUserAgent foUserAgent = fopFactory.newFOUserAgent();
            //set the renderer to be PDF
            // the expected sequence here will be xml -> xslt -> fo -> pdf
            // where fo is the transformed file
            File outputFile = new File(this.getODMXMLFileName() + ".pdf");
            File xslFile = new File(this.getTransformFileName());
            out = new FileOutputStream(outputFile);
            out = new BufferedOutputStream(out);
            Fop fop = fopFactory.newFop(MimeConstants.MIME_PDF, foUserAgent, out);
            
            TransformerFactory factory = TransformerFactory.newInstance();
            Transformer transformer = factory.newTransformer(); // identity transformer
            Source src = new StreamSource(xslFile);

            // Resulting SAX events (the generated FO) must be piped through to FOP
            Result res = new SAXResult(fop.getDefaultHandler());

            // Start XSLT transformation and FOP processing
            transformer.transform(src, res);

            // Result processing
            FormattingResults foResults = fop.getResults();
            java.util.List pageSequences = foResults.getPageSequences();
            for (java.util.Iterator it = pageSequences.iterator(); it.hasNext();) {
                PageSequenceResults pageSequenceResults = (PageSequenceResults)it.next();
                System.out.println("PageSequence "
                        + (String.valueOf(pageSequenceResults.getID()).length() > 0
                                ? pageSequenceResults.getID() : "<no id>")
                                + " generated " + pageSequenceResults.getPageCount() + " pages.");
            }
            System.out.println("Generated " + foResults.getPageCount() + " pages in total.");
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
            ProcessingResultType resultError = ProcessingResultType.FAIL;
            resultError.setUrl(CoreResources.getField("sysURL.base") + "ViewDatasets"); // view datasets page
            resultError.setArchiveMessage("Failure thrown: " + e.getMessage());
            resultError.setDescription("Your job failed with the message of: " + e.getMessage());
            return resultError;
        } finally {
            
        }
        // otherwise return a success with the URL link
        
        ProcessingResultType resultSuccess = ProcessingResultType.SUCCESS;
        resultSuccess.setUrl(CoreResources.getField("sysURL.base") + 
                "AccessFile?fileId="); // to the pdf
        resultSuccess.setArchiveMessage("Success");
        resultSuccess.setDescription("Your job succeeded please find the URL below");
        return resultSuccess;
        
        
    }
    
    
}
