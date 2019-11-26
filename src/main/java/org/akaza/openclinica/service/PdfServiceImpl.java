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



import org.springframework.stereotype.Service;

/**
 * @author Tao Li
 *
 */
@Service( "PdfService" )
public class PdfServiceImpl implements PdfService {

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
                         String fullFinalFilePathName) throws IOException {

        //Instantiating PDFMergerUtility class
        PDFMergerUtility PDFmerger = new PDFMergerUtility();

        File finalFile = new File(fullFinalFilePathName);
        PDFmerger.setDestinationFileName(fullFinalFilePathName);

        //Loading an existing PDF document   
        int page_counter = 1;
        String footerMsg = "OpenClinica CaseBook ";

        ArrayList<PDDocument>  pDDocuments = new ArrayList<>();
        for(File file: files) {
            PDDocument doc = PDDocument.load(file);

            page_counter = this.addFooter(doc, footerMsg, page_counter);

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


    public int addFooter(PDDocument document, String footerMsg, int page_counter) throws IOException {

        String footerMessage = "Page ";
        if(footerMsg != null && !(footerMsg.isEmpty())) {
            footerMessage = footerMsg + "     "+ footerMessage;
        }


        PDFont font = PDType1Font.TIMES_ROMAN;
        float fontSize = 10.0f;

        for( PDPage page : document.getPages() )
        {
            PDRectangle pageSize = page.getMediaBox();
            String message = footerMessage + page_counter;
            float stringWidth = font.getStringWidth( message )*fontSize/1000f;
            // calculate to center of the page
            int rotation = page.getRotation();
            boolean rotate = rotation == 90 || rotation == 270;
            float pageWidth = rotate ? pageSize.getHeight() : pageSize.getWidth();
            float pageHeight = rotate ? pageSize.getWidth() : pageSize.getHeight();
            float centerX = rotate ? pageHeight : (pageWidth - stringWidth - stringWidth/2);
            float centerY = rotate ? (pageWidth - stringWidth) :  10;

            // append the content to the existing stream
            try (PDPageContentStream contentStream = new PDPageContentStream(document, page, AppendMode.APPEND, true, true))
            {
                contentStream.beginText();
                // set font and font size
                contentStream.setFont( font, fontSize );
                // set text color to red
                contentStream.setNonStrokingColor(0, 0, 0);
                if (rotate)
                {
                    // rotate the text according to the page rotation
                    contentStream.setTextMatrix(Matrix.getRotateInstance(Math.PI / 2, centerX, centerY));
                }
                else
                {
                    contentStream.setTextMatrix(Matrix.getTranslateInstance(centerX, centerY));
                }
                contentStream.showText(message);
                contentStream.endText();
            }

            page_counter++;
        }

        return page_counter;

    }





}