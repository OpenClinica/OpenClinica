/**
 *
 */
package org.akaza.openclinica.service;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import org.apache.pdfbox.pdmodel.PDDocument;

import core.org.akaza.openclinica.domain.datamap.Study;
import core.org.akaza.openclinica.domain.datamap.StudyEvent;
import core.org.akaza.openclinica.domain.datamap.StudySubject;

/**
 * @author Tao Li
 *
 */
public interface PdfService {

    File mergePDF(ArrayList<File> files,
                  String fullFinalFilePathName,ArrayList<String[]> pdfHeaders,ArrayList<String> pdfLeftFooters) throws IOException;

    String getCaseBookFileRootPath();

    int addHeaderOrFooter(PDDocument document, String[] headerMsg,String footerTime,String footerMsg, int page_counter) throws IOException;
    
    String[] preparePdfHeader(Study study, Study site, StudyEvent studyEvent);
    
    void writeToFile(String message,  String fileName, StudySubject ss);
    
    String preparePdfFooterTime();
    
}