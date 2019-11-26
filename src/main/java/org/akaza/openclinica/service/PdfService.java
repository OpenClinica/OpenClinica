/**
 *
 */
package org.akaza.openclinica.service;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import org.apache.pdfbox.pdmodel.PDDocument;

/**
 * @author Tao Li
 *
 */
public interface PdfService {

    File mergePDF(ArrayList<File> files,
                  String fullFinalFilePathName) throws IOException;

    String getCaseBookFileRootPath();

    int addFooter(PDDocument document, String footerMsg, int page_counter) throws IOException;
}