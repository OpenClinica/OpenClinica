/**
 *
 */
package org.akaza.openclinica.service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.text.MessageFormat;

import core.org.akaza.openclinica.dao.core.CoreResources;
import core.org.akaza.openclinica.domain.datamap.Study;
import core.org.akaza.openclinica.domain.enumsupport.JobType;
import org.apache.pdfbox.multipdf.PDFMergerUtility;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.PDPageContentStream.AppendMode;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.util.Matrix;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * @author Tao Li
 *
 */
@Service( "PdfService" )
public class PdfServiceImpl implements PdfService {
	protected final Logger logger = LoggerFactory.getLogger(getClass().getName());
	
	 static final MessageFormat pdfHeaderFormat1 =  new MessageFormat("{0}: {1} - Participant {2}");
	 static final MessageFormat pdfHeaderFormat2 =  new MessageFormat("{0} - Participant {2}");

    /**
     *
     */
    public PdfServiceImpl() {

    }


    /**
     * AC5: The PDF Casebook will be called "Participant <Participant ID> Casebook <current timestamp>.pdf".
     * @param files
     * @return File
     * @throws IOException
     */
    public File mergePDF(ArrayList<File> files,
                         String fullFinalFilePathName,String pdfHeader) throws IOException {

        //Instantiating PDFMergerUtility class
        PDFMergerUtility PDFmerger = new PDFMergerUtility();

        File finalFile = new File(fullFinalFilePathName);
        PDFmerger.setDestinationFileName(fullFinalFilePathName);

        //Loading an existing PDF document   
        int page_counter = 1;        

        ArrayList<PDDocument>  pDDocuments = new ArrayList<>();
        //get all page numbers
        int totalPageNumber = 0;
        for(File file: files) {
            PDDocument docTemp = PDDocument.load(file);
            totalPageNumber += docTemp.getNumberOfPages();
            docTemp.close();
        }
        String footerMsg = "Page X of " + totalPageNumber;
        String headerMsg = pdfHeader;
        
        for(File file: files) {
            PDDocument doc = PDDocument.load(file);

            page_counter = this.addHeaderOrFooter(doc, headerMsg,footerMsg, page_counter);

            // after add footer, use the new content
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            doc.save(out);
            ByteArrayInputStream in = new ByteArrayInputStream(out.toByteArray());

            // track doc and  keep it open
            pDDocuments.add(doc);

            //adding the source files
            PDFmerger.addSource(in);        
        }

        //Merging all PDFs
        PDFmerger.mergeDocuments(null);

        //after merge, to close the documents
        for(PDDocument doc:pDDocuments) {
            doc.close();
        }
        //after merge, to remove the sub temp files
        for(File file: files) {
            file.delete();
        }
        //return the new file
        return finalFile;
    }


    /**
     *
     * @return
     */
    public String getCaseBookFileRootPath() {
        String dirPath = CoreResources.getField("filePath") + "bulk_jobs" + File.separator + JobType.PARTICIPANT_PDF_CASEBOOK.toString().toLowerCase();
        File directory = new File(dirPath);
        if (!directory.exists()) {
            directory.mkdirs();
        }
        return dirPath;
    }

    /**
     * will return next available page number
     */
    public int addHeaderOrFooter(PDDocument document, String headerMsg,String footerMsg, int page_counter) throws IOException {

        String footerMessage = null;             
        PDFont font = PDType1Font.TIMES_ROMAN;
        float fontSize = 10.0f;

        for( PDPage page : document.getPages() )
        {
            PDRectangle pageSize = page.getMediaBox();
            footerMessage = footerMsg.replaceAll("X", page_counter+"");
        
            float footerWidth = font.getStringWidth( footerMessage )*fontSize/1000f;
            // calculate to center of the page
            int rotation = page.getRotation();
            boolean rotate = rotation == 90 || rotation == 270;
            float pageWidth = rotate ? pageSize.getHeight() : pageSize.getWidth();
            float pageHeight = rotate ? pageSize.getWidth() : pageSize.getHeight();
            float footerCenterX = rotate ? pageHeight : (pageWidth - footerWidth - footerWidth/2);
            float footerCenterY = rotate ? (pageWidth - footerWidth) :  10;
            
            float stringHeight = fontSize; 
            float headerCenterX = rotate ? 5 : stringHeight+ 5;
            float headerCenterY = rotate ? stringHeight+ 5 :  pageHeight - stringHeight;

            // append the content to the existing stream
            try (PDPageContentStream contentStream = new PDPageContentStream(document, page, true, true, true))
            {
            	contentStream.beginText();
            	// add header
            	if(headerMsg != null) {            		
                    // set font and font size
                    contentStream.setFont( font, fontSize);
                    contentStream.moveTextPositionByAmount(headerCenterX, headerCenterY);
                    contentStream.drawString(headerMsg);                   
            	}            	 
                 
                // add footer             
                // set font and font size
                contentStream.setFont( font, fontSize );
                // set text color to red
                contentStream.setNonStrokingColor(0, 0, 0);
                if (rotate)
                {
                    // rotate the text according to the page rotation
                    contentStream.setTextMatrix(Matrix.getRotateInstance(Math.PI / 2, footerCenterX, footerCenterY));
                }
                else
                {
                    contentStream.setTextMatrix(Matrix.getTranslateInstance(footerCenterX, footerCenterY));
                }
                contentStream.showText(footerMessage);
                contentStream.endText();
            }

            page_counter++;
        }

        return page_counter;

    }


   public String preparePdfHeader(Study study, Study site, String studySubjectIdentifier) {
	  
	    String siteName = null;
	    String studyName = null;
	    String participantID = studySubjectIdentifier.trim();	    		   	   
	    		  		    
	    if(study != null) {				
			studyName = study.getName();		
		}
	    if(site !=null) {		    	
	    	siteName = site.getName();	    	
	    }
	    Object[] headerArgs = {studyName, siteName,participantID};
	    
	    String pdfHeader;
		if(siteName !=null) {
	    	pdfHeader = pdfHeaderFormat1.format(headerArgs);
	    }else {
	    	pdfHeader = pdfHeaderFormat2.format(headerArgs);
	    }
	    
	    return pdfHeader;
   }

   
   /**
    *  This is used to write errors to the log file in pdf format
    * @param msg
    * @param fileName
    */
   public void writeToFile(String message, String fileName) {
    
       PDDocument doc = new PDDocument();
       try {
           PDPage page = new PDPage();
           doc.addPage(page);
           
           PDFont font = PDType1Font.HELVETICA_BOLD;

           PDPageContentStream contents = new PDPageContentStream(doc, page);
           contents.beginText();
           contents.setFont(font, 10);
           contents.newLineAtOffset(20, 700);
           contents.showText(message);
           contents.endText();
           contents.close();
           
           doc.save(fileName);
       }catch(IOException e) {
    	   logger.error("Error " + e.getMessage());
       }
       finally {
    	   try {
    		   doc.close();
    	   }catch(IOException e) {
        	   logger.error("Error " + e.getMessage());
           }
           
       } 
   }

}